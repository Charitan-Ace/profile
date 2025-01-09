package com.charitan.profile.donor.internal;

import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.external.dtos.DonorTransactionDTO;
import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import com.charitan.profile.stripe.StripeExternalAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DonorService implements DonorExternalAPI, DonorInternalAPI {
    @Autowired
    private DonorRepository donorRepository;
    @Autowired
    private StripeExternalAPI stripeExternalAPI;
    private final RedisTemplate<String, DonorDTO> redisTemplate;
    private final RedisTemplate<String, String> redisZSetTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DONOR_CACHE_PREFIX = "donor:";
    private static final String DONOR_LIST_CACHE_KEY = "donors:all";

    private static final String DONOR_LIST_CACHE_KEY_LAST_NAME = DONOR_LIST_CACHE_KEY + ":lastname";
    private static final String DONOR_LIST_CACHE_KEY_FIRST_NAME = DONOR_LIST_CACHE_KEY + ":firstname";

    // Constructor explicitly marked with @Qualifier for RedisTemplate
    public DonorService(@Qualifier("REDIS_DONORS") RedisTemplate<String, DonorDTO> redisTemplate,
                        @Qualifier("REDIS_DONORS_ZSET") RedisTemplate<String, String> redisZSetTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisZSetTemplate = redisZSetTemplate;
    }

    //TODO: send email on successful creation
    @Override
    public void createDonor(DonorCreationRequest request) {

        if (donorRepository.existsById(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor is already created.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    request.getEmail(),
                    request.getFirstName() + " " + request.getLastName(),
                    "Donor ID: " + request.getUserId(),
                    Map.of("donorId", String.valueOf(request.getUserId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Donor donor = new Donor(request.getUserId(), request.getLastName(), request.getFirstName(), request.getAddress(), stripeId, request.getAssetsKey());

        donorRepository.save(donor);

        // Add the new donor to the cache
        redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + request.getUserId(), new DonorDTO(donor));

        addToRedisZSet(donor);
    }

    @Override
    public DonorTransactionDTO getInfoForTransaction(UUID userId) {
        // Check if data is in cache
        String cacheKey = DONOR_CACHE_PREFIX + userId;
        DonorDTO cachedDonor = (DonorDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDonor != null) {
            return new DonorTransactionDTO(cachedDonor);
        }

        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        // Add the missing donor to the cache
        redisTemplate.opsForValue().set(cacheKey, new DonorDTO(donor));

        addToRedisZSet(donor);

        return new DonorTransactionDTO(donor);
    }

    @Override
    public void updateDonor(DonorUpdateRequest request) {

        Donor donor = donorRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        if (!Objects.equals(request.getFirstName(), donor.getFirstName()) && request.getFirstName() != null) {
            // Remove the old firstName from sorted set
            String compositeKey = donor.getFirstName().trim().toLowerCase() + ":" + donor.getUserId();
            redisZSetTemplate.opsForZSet().remove(DONOR_LIST_CACHE_KEY_FIRST_NAME, compositeKey);

            // Add to sorted set with a lexicographical member key
            compositeKey = request.getFirstName().trim().toLowerCase() + ":" + donor.getUserId();
            redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY_FIRST_NAME, compositeKey, 0);

            donor.setFirstName(request.getFirstName());
        }
        if (!Objects.equals(request.getLastName(), donor.getLastName()) && request.getLastName() != null) {
            // Remove the old lastName from sorted set
            String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
            redisZSetTemplate.opsForZSet().remove(DONOR_LIST_CACHE_KEY_LAST_NAME, compositeKey);

            // Add to sorted set with a lexicographical member key
            compositeKey = request.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
            redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY_LAST_NAME, compositeKey, 0);

            donor.setLastName(request.getLastName());
        }
        if (!Objects.equals(request.getAddress(), donor.getAddress()) && request.getAddress() != null) {
            donor.setAddress(request.getAddress());
        }

        donorRepository.save(donor);

        // Update cache for this donor
        redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + request.getUserId(), new DonorDTO(donor));
    }

    @Override
    public DonorDTO getInfo(UUID userId) {

        // Check if data is in cache
        String cacheKey = DONOR_CACHE_PREFIX + userId;
        DonorDTO cachedDonor = (DonorDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDonor != null) {
            System.out.println("Cache");
            return cachedDonor;
        }

        System.out.println("DB");
        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        redisTemplate.opsForValue().set(cacheKey, new DonorDTO(donor));

        addToRedisZSet(donor);
        
        return new DonorDTO(donor);
    }

    @Override
    @PreAuthorize("hasRole('DONOR')")
    public DonorDTO getMyInfo() {

        UUID userId = getCurrentDonorId();
        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        // Check if data is in cache
        String cacheKey = DONOR_CACHE_PREFIX + userId;
        DonorDTO cachedDonor = (DonorDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDonor != null) {
            System.out.println("Cache");
            return cachedDonor;
        }

        System.out.println("DB");

        redisTemplate.opsForValue().set(cacheKey, new DonorDTO(donor));

        addToRedisZSet(donor);

        return new DonorDTO(donor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<DonorDTO> getAll(int pageNo, int pageSize, String order, String filter, String keyword) {

        int start = (int) pageNo * pageSize;
        int end = start + pageSize - 1;
        String cacheKey = DONOR_LIST_CACHE_KEY + ":" + filter.toLowerCase();

        int donorInDB = (int) donorRepository.count();

        if (start > donorInDB) {
            return Page.empty(PageRequest.of(pageNo, pageSize));
        }

        if (end > donorInDB) {
            end = donorInDB;
        }

        Long redisZSetSize = redisZSetTemplate.opsForZSet().size(cacheKey);

        System.out.println("redisZSetSize: " + redisZSetSize);
        System.out.println("db size: " + donorInDB);
        // If Redis cache is empty
        if (redisZSetSize != donorInDB) {
            System.out.println("From DB");
            Page<Donor> donorPage;

            if (keyword != null && !keyword.isEmpty()) {
                // Search with keyword in DB
                if (order.equalsIgnoreCase("ascending")) {
                    donorPage = filter.equalsIgnoreCase("firstName")
                            ? donorRepository.findByFirstNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("firstName").ascending()))
                            : donorRepository.findByLastNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("lastName").ascending()));
                } else {
                    donorPage = filter.equalsIgnoreCase("firstName")
                            ? donorRepository.findByFirstNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("firstName").descending()))
                            : donorRepository.findByLastNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("lastName").descending()));
                }
            } else {
                if (filter.equalsIgnoreCase("firstName")) {
                    donorPage = order.equalsIgnoreCase("ascending")
                            ? donorRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("firstName").ascending()))
                            : donorRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("firstName").descending()));
                } else {
                    donorPage = order.equalsIgnoreCase("ascending")
                            ? donorRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("lastName").ascending()))
                            : donorRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("lastName").descending()));
                }
            }

            List<DonorDTO> donors = donorPage.getContent()
                    .stream()
                    .map(DonorDTO::new)
                    .collect(Collectors.toList());

            // Cache fetched data in Redis
            donorPage.getContent().forEach(donor -> {
                redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + donor.getUserId(), new DonorDTO(donor));
                addToRedisZSet(donor);
            });

            return new PageImpl<>(donors, PageRequest.of(pageNo, pageSize), donorPage.getTotalElements());
        }

        System.out.println("From redis");

        List<String> matchingKeys = new ArrayList<>();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        ScanOptions options = ScanOptions.scanOptions()
                .match("*" + keyword + "*:*") // Match elements containing the pattern
                .count(10) // Scan in batches of 'batchSize'
                .build();

        // Initial cursor value for SCAN is "0"
        byte[] cursor = "0".getBytes(); // Start with the cursor set to "0"
        do {
            Cursor<Tuple> scanCursor = connection.zScan(cacheKey.getBytes(StandardCharsets.UTF_8), options);

            while (scanCursor.hasNext()) {
                Tuple tuple = scanCursor.next();
                String element = new String(tuple.getValue(), StandardCharsets.UTF_8); // Get the value (the member of the sorted set)
                matchingKeys.add(element.replace("\"", ""));
            }

            scanCursor.close();
        } while (!new String(cursor, StandardCharsets.UTF_8).equals("0")); // Continue until the cursor returns "0"

        System.out.println("Matching keys: " + Arrays.toString(matchingKeys.toArray()));

        // Adjust the indices to handle edge cases
        if (start >= matchingKeys.size()) {
            // If start index exceeds the size of matchingKeys, return an empty page
            return new PageImpl<>(Collections.emptyList(), Pageable.ofSize(end - start + 1), matchingKeys.size());
        }

        if (end >= matchingKeys.size()) {
            // If end index exceeds the size of matchingKeys, adjust it to the last index
            end = matchingKeys.size() - 1;
        }

        List<String> resultKeys;
        // Extract the sublist based on the adjusted start and end indices
        if (order.equalsIgnoreCase("ascending")) {
            matchingKeys.sort(Comparator.naturalOrder());
            resultKeys = matchingKeys.subList(start, end + 1);
        } else {
            matchingKeys.sort(Comparator.reverseOrder());
            resultKeys = matchingKeys.subList(start, end + 1);
        }

        System.out.println("Result keys " + Arrays.toString(resultKeys.toArray()));

        List<UUID> extractID = new ArrayList<>();
        for (String key : resultKeys) {
            extractID.add(UUID.fromString(key.split(":")[1]));
        }

        System.out.println("Extract ID " + Arrays.toString(extractID.toArray()));


        // Fetch donor details from the cache
        List<DonorDTO> donors = extractID.stream()
                .map(id -> (DonorDTO) redisTemplate.opsForValue().get(DONOR_CACHE_PREFIX + id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(donors, PageRequest.of(pageNo, pageSize), matchingKeys.size());
    }

    private void addToRedisZSet(Donor donor) {
        // Add to sorted set with a lexicographical member key
        String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
        redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY_LAST_NAME, compositeKey, 0);

        compositeKey = donor.getFirstName().trim().toLowerCase() + ":" + donor.getUserId();
        redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY_FIRST_NAME, compositeKey, 0);

    }

    private UUID getCurrentDonorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                // Assuming CustomUserDetails holds the User ID
                return ((CustomUserDetails) principal).getUserId();
            }
        }

        throw new RuntimeException("Current charity id is not found");
    }
}

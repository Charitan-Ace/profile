package com.charitan.profile.donor.internal;

import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.external.dtos.DonorTransactionDTO;
import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
import com.charitan.profile.stripe.StripeExternalAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        // Add to sorted set with a lexicographical member key
        String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
        redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY, compositeKey, 0); // Score is 0 for lexicographical order
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

        // Add to sorted set with a lexicographical member key
        String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
        redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY, compositeKey, 0);

        return new DonorTransactionDTO(donor);
    }

    @Override
    public void updateDonor(DonorUpdateRequest request) {

        Donor donor = donorRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        if (!Objects.equals(request.getFirstName(), donor.getFirstName())) {
            donor.setFirstName(request.getFirstName());
        }
        if (!Objects.equals(request.getLastName(), donor.getLastName())) {
            // Remove the old lastName from sorted set
            String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
            redisTemplate.opsForZSet().remove(DONOR_LIST_CACHE_KEY, compositeKey);

            // Add to sorted set with a lexicographical member key
            compositeKey = request.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
            redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY, compositeKey, 0);

            donor.setLastName(request.getLastName());
        }
        if (!Objects.equals(request.getAddress(), donor.getAddress())) {
            donor.setAddress(request.getAddress());
        }

        donorRepository.save(donor);

        // Update cache for this donor
        redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + request.getUserId(), new DonorDTO(donor));
    }

    //TODO: get email from auth service
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

        // Add to sorted set with a lexicographical member key
        String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
        redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY, compositeKey, 0);

        return new DonorDTO(donor);
    }

    public Page<DonorDTO> getAll(int pageNo, int pageSize) {

        long start = (long) pageNo * pageSize;
        long end = start + pageSize - 1;

        long donorInDB = donorRepository.count();

        if (start > donorInDB) {
            return Page.empty(PageRequest.of(pageNo, pageSize));
        }

        if (end > donorInDB) {
            end = donorInDB;
        }

        List<String> compositeKeys = new ArrayList<>(redisZSetTemplate.opsForZSet().range(DONOR_LIST_CACHE_KEY, start, end));

        if (compositeKeys.isEmpty() || compositeKeys.size() != donorInDB) {

            System.out.println("From db");
            // Fallback to database if cache is empty
            Page<Donor> donorPage = donorRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("lastName").ascending()));
            List<DonorDTO> donors = donorPage.getContent()
                    .stream()
                    .map(DonorDTO::new)
                    .collect(Collectors.toList());

            // Cache the fetched donor list for future use
            donorPage.getContent().forEach(donor -> {
                // Cache each donor's data (DonorDTO) in Redis
                redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + donor.getUserId(), new DonorDTO(donor));

                // Add donor to the sorted set with lexicographical order based on lastName
                String compositeKey = donor.getLastName().trim().toLowerCase() + ":" + donor.getUserId();
                redisZSetTemplate.opsForZSet().add(DONOR_LIST_CACHE_KEY, compositeKey, 0);
            });

            return new PageImpl<>(donors, PageRequest.of(pageNo, pageSize), donorPage.getTotalElements());
        }

        System.out.println("From redis");

        List<UUID> donorIds = compositeKeys.stream()
                .map(key -> key.split(":")[1])  // Extract UUID string part
                .map(UUID::fromString)  // Convert to UUID object
                .toList();

        // Fetch full donor details from the cache
        List<DonorDTO> donors = donorIds.stream()
                .map(id -> (DonorDTO) redisTemplate.opsForValue().get(DONOR_CACHE_PREFIX + id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(donors, PageRequest.of(pageNo, pageSize), redisTemplate.opsForZSet().size(DONOR_LIST_CACHE_KEY));
    }
}
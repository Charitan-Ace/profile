package com.charitan.profile.charity.internal;

import com.charitan.profile.asset.AssetExternalService;
import com.charitan.profile.charity.external.CharityExternalAPI;
import com.charitan.profile.charity.external.dtos.CharityCreationRequest;
import com.charitan.profile.charity.external.dtos.ExternalCharityDTO;
import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharitySelfUpdateRequest;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import com.charitan.profile.stripe.StripeExternalAPI;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CharityService implements CharityExternalAPI, CharityInternalAPI {
    private final CharityRepository charityRepository;
    private final StripeExternalAPI stripeExternalAPI;
    private final RedisTemplate<String, CharityDTO> redisTemplate;
    private final RedisTemplate<String, String> redisZSetTemplate;

    private static final String CHARITY_CACHE_PREFIX = "charity:";
    private static final String CHARITY_LIST_CACHE_KEY = "charities:all";

    private static final String CHARITY_LIST_CACHE_KEY_COMPANY_NAME = CHARITY_LIST_CACHE_KEY + ":companyname";
    private static final String CHARITY_LIST_CACHE_KEY_TAX_CODE = CHARITY_LIST_CACHE_KEY + ":taxcode";
    private static final String CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE = CHARITY_LIST_CACHE_KEY + ":organizationtype";

    public CharityService(
            CharityRepository charityRepository,
            StripeExternalAPI stripeExternalAPI,
            @Qualifier("REDIS_CHARITIES") RedisTemplate<String, CharityDTO> redisTemplate,
            @Qualifier("REDIS_CHARITIES_ZSET") RedisTemplate<String, String> redisZSetTemplate) {
        this.charityRepository = charityRepository;
        this.stripeExternalAPI = stripeExternalAPI;
        this.redisTemplate = redisTemplate;
        this.redisZSetTemplate = redisZSetTemplate;
    }

    //TODO: send email on successful creation
    @Override
    public void createCharity(CharityCreationRequest request) {

        if (charityRepository.existsById(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Charity is already created.");
        }

        // Validate and parse organizationType
        OrganizationType organizationType;
        try {
            organizationType = OrganizationType.valueOf(request.getOrganizationType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid organization type.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    request.getEmail(),
                    request.getCompanyName(),
                    "Charity ID: " + request.getUserId(),
                    Map.of("charityId", String.valueOf(request.getUserId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Charity charity = new Charity(request.getUserId(), request.getCompanyName(), request.getAddress(), request.getTaxCode(), organizationType, stripeId, "", "");

        charityRepository.save(charity);

        // Add the new charity to the cache
        redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + request.getUserId(), new CharityDTO(charity));

        addToRedisZSet(charity);
    }

    @Override
    public CharityDTO updateCharity(CharityUpdateRequest request) {

        Charity charity = charityRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        if (!Objects.equals(request.getCompanyName(), charity.getCompanyName()) && request.getCompanyName() != null) {
            String compositeKey = charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey);

            compositeKey = request.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey, 0);

            compositeKey = charity.getOrganizationType().name().toLowerCase()+ ":" + request.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey, 0);

            charity.setCompanyName(request.getCompanyName());
        }
        if (!Objects.equals(request.getTaxCode(), charity.getTaxCode()) && request.getTaxCode() != null) {

            if (charityRepository.existsCharityByTaxCode(request.getTaxCode())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tax code already been used.");
            }

            String compositeKey = charity.getTaxCode().trim() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey);

            compositeKey = request.getTaxCode().trim() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey, 0);

            charity.setTaxCode(request.getTaxCode());
        }
        if (!Objects.equals(request.getOrganizationType().trim().toLowerCase(), charity.getOrganizationType().name().trim().toLowerCase()) && request.getOrganizationType() != null) {

            // Validate and parse organizationType
            OrganizationType organizationType;
            try {
                organizationType = OrganizationType.valueOf(request.getOrganizationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid organization type.");
            }

            String compositeKey = charity.getOrganizationType().name().toLowerCase() + ":" + charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey);

            compositeKey = request.getOrganizationType().toLowerCase() + ":" + charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey, 0);

            charity.setOrganizationType(organizationType);
        }
        if (request.getAddress() != null) {
            charity.setAddress(request.getAddress());
        }

        charityRepository.save(charity);

        // Update cache for this donor
        redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + charity.getUserId(), new CharityDTO(charity));

        return new CharityDTO(charity);
    }

    @Override
    public CharityDTO updateMyInfo(CharitySelfUpdateRequest request, UUID userId) {

        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        if (!Objects.equals(request.getCompanyName(), charity.getCompanyName()) && request.getCompanyName() != null) {
            String compositeKey = charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey);

            compositeKey = request.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey, 0);

            compositeKey = charity.getOrganizationType().name().toLowerCase()+ ":" + request.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey, 0);

            charity.setCompanyName(request.getCompanyName());
        }
        if (!Objects.equals(request.getTaxCode(), charity.getTaxCode()) && request.getTaxCode() != null) {

            if (charityRepository.existsCharityByTaxCode(request.getTaxCode())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tax code already been used.");
            }

            String compositeKey = charity.getTaxCode().trim() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey);

            compositeKey = request.getTaxCode().trim() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey, 0);

            charity.setTaxCode(request.getTaxCode());
        }
        if (!Objects.equals(request.getOrganizationType().trim().toLowerCase(), charity.getOrganizationType().name().trim().toLowerCase()) && request.getOrganizationType() != null) {

            // Validate and parse organizationType
            OrganizationType organizationType;
            try {
                organizationType = OrganizationType.valueOf(request.getOrganizationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid organization type.");
            }

            String compositeKey = charity.getOrganizationType().name().toLowerCase() + ":" + charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().remove(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey);

            compositeKey = request.getOrganizationType().toLowerCase() + ":" + charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
            redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey, 0);

            charity.setOrganizationType(organizationType);
        }
        if (request.getAddress() != null) {
            charity.setAddress(request.getAddress());
        }

        if (request.getAvatar() != null) {
            charity.setAssetsKey(request.getAvatar());
        }

        if (request.getVideo() != null) {
            charity.setVideo(request.getVideo());
        }

        charityRepository.save(charity);

        // Update cache for this donor
        redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + charity.getUserId(), new CharityDTO(charity));

        return new CharityDTO(charity);
    }

    @Override
    public CharityDTO getMyInfo() {

        UUID userId = getCurrentCharityId();
        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        // Check if data is in cache
        String cacheKey = CHARITY_CACHE_PREFIX + userId;
        CharityDTO cachedCharity = (CharityDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedCharity != null) {
            return cachedCharity;
        }

        redisTemplate.opsForValue().set(cacheKey, new CharityDTO(charity));

        addToRedisZSet(charity);
        return new CharityDTO(charity);
    }

    @Override
    public CharityDTO getInfo(UUID userId) {

        // Check if data is in cache
        String cacheKey = CHARITY_CACHE_PREFIX + userId;
        CharityDTO cachedCharity = (CharityDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedCharity != null) {
            return cachedCharity;
        }

        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        redisTemplate.opsForValue().set(cacheKey, new CharityDTO(charity));

        addToRedisZSet(charity);

        return new CharityDTO(charity);
    }

    @Override
    public ExternalCharityDTO getCharity(UUID userId) {
        // Check if data is in cache

        String cacheKey = CHARITY_CACHE_PREFIX + userId;
        CharityDTO cachedCharity = (CharityDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedCharity != null) {
            return new ExternalCharityDTO(cachedCharity);
        }

        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        redisTemplate.opsForValue().set(cacheKey, new CharityDTO(charity));

        addToRedisZSet(charity);
        return new ExternalCharityDTO(charity);
    }

    public Page<CharityDTO> getAll(int pageNo, int pageSize, String order, String filter, String keyword) {

        int start = (int) pageNo * pageSize;
        int end = start + pageSize - 1;
        String cacheKey;
        if (filter.equalsIgnoreCase("companyName") || filter.equalsIgnoreCase("taxCode")) {
            cacheKey = CHARITY_LIST_CACHE_KEY + ":" + filter.toLowerCase();
        } else {
            cacheKey = CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE;
        }
        System.out.println("cacheKey: " + cacheKey);

        int charityInDB = (int) charityRepository.count();

        if (start > charityInDB) {
            return Page.empty(PageRequest.of(pageNo, pageSize));
        }

        if (end > charityInDB) {
            end = charityInDB;
        }

        Long redisZSetSize = redisZSetTemplate.opsForZSet().size(cacheKey);
        // If Redis cache is empty
        if (redisZSetSize != charityInDB) {
            System.out.println("From DB");
            Page<Charity> charityPage;

            if (keyword != null && !keyword.isEmpty()) {
                // Search with keyword in DB
                charityPage = filter.equalsIgnoreCase("companyName")
                        ? charityRepository.findByCompanyNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").ascending()))
                        : charityRepository.findByTaxCodeContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").ascending()));
            } else {
                if (filter.equalsIgnoreCase("companyName")) {
                    charityPage = order.equalsIgnoreCase("ascending")
                            ? charityRepository.findByCompanyNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").ascending()))
                            : charityRepository.findByCompanyNameContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").descending()));
                } else if (filter.equalsIgnoreCase("taxCode")) {
                    charityPage = order.equalsIgnoreCase("ascending")
                            ? charityRepository.findByTaxCodeContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").ascending()))
                            : charityRepository.findByTaxCodeContainingIgnoreCase(keyword, PageRequest.of(pageNo, pageSize, Sort.by("companyName").descending()));
                } else {
                    charityPage = charityRepository.findByOrganizationType(OrganizationType.valueOf(filter.toUpperCase()), PageRequest.of(pageNo, pageSize, Sort.by("companyName").ascending()));
                }
            }

            List<CharityDTO> charities = charityPage.getContent()
                    .stream()
                    .map(CharityDTO::new)
                    .toList();

            charityPage.getContent().forEach(charity -> {
                redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + charity.getUserId(), new CharityDTO(charity));
                addToRedisZSet(charity);
            });

            return new PageImpl<>(charities, PageRequest.of(pageNo, pageSize), charityPage.getTotalElements());
        }

        System.out.println("From redis");

        List<String> matchingKeys = new ArrayList<>();
        RedisConnection connection = redisZSetTemplate.getConnectionFactory().getConnection();

        ScanOptions options;
        if (!cacheKey.equalsIgnoreCase(CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE)) {
            options = ScanOptions.scanOptions()
                    .match("*" + keyword.toLowerCase() + "*:*") // Match elements containing the pattern
                    .count(10) // Scan in batches of 'batchSize'
                    .build();
        } else {
            System.out.println("Filter options");
            options = ScanOptions.scanOptions()
                    .match("*" + filter.toLowerCase() + "*:*") // Match elements containing the pattern
                    .count(10) // Scan in batches of 'batchSize'
                    .build();
        }

        // Initial cursor value for SCAN is "0"
        byte[] cursor = "0".getBytes(); // Start with the cursor set to "0"
        do {
            Cursor<Tuple> scanCursor = connection.zScan(cacheKey.getBytes(StandardCharsets.UTF_8), options);

            while (scanCursor.hasNext()) {
                Tuple tuple = scanCursor.next();
                String element = new String(tuple.getValue(), StandardCharsets.UTF_8); // Get the value (the member of the sorted set)
                System.out.println(element);
                if (cacheKey.equalsIgnoreCase(CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE)) {
                    if (!keyword.isEmpty()) {
                        if (element.replace("\"", "").split(":")[1].toLowerCase().contains(keyword.trim().toLowerCase())) {
                            matchingKeys.add(element.replace("\"", ""));
                        }
                    } else {
                        matchingKeys.add(element.replace("\"", ""));
                    }
                } else {
                    matchingKeys.add(element.replace("\"", ""));
                }
            }

            scanCursor.close();
        } while (!new String(cursor, StandardCharsets.UTF_8).equals("0")); // Continue until the cursor returns "0"

        System.out.println(Arrays.toString(matchingKeys.toArray()));

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

        System.out.println(Arrays.toString(resultKeys.toArray()));

        List<UUID> extractID = new ArrayList<>();
        for (String key : resultKeys) {
            if (cacheKey.equalsIgnoreCase(CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE)) {
                extractID.add(UUID.fromString(key.split(":")[2]));
            } else {
                extractID.add(UUID.fromString(key.split(":")[1]));
            }
        }
        System.out.println(Arrays.toString(extractID.toArray()));

        List<CharityDTO> charities = extractID.stream()
                .map(id -> (CharityDTO) redisTemplate.opsForValue().get(CHARITY_CACHE_PREFIX + id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        return new PageImpl<>(charities, PageRequest.of(pageNo, pageSize), matchingKeys.size());
    }

    private void addToRedisZSet(Charity charity) {
        String compositeKey = charity.getCompanyName().trim().toLowerCase() + ":" + charity.getUserId();
        redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_COMPANY_NAME, compositeKey, 0);

        compositeKey = charity.getTaxCode().trim().toLowerCase() + ":" + charity.getUserId();
        redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_TAX_CODE, compositeKey, 0);

        compositeKey = charity.getOrganizationType().name().toLowerCase() + ":" + charity.getCompanyName() + ":" + charity.getUserId();
        redisZSetTemplate.opsForZSet().add(CHARITY_LIST_CACHE_KEY_ORGANIZATION_TYPE, compositeKey, 0);
    }

    private UUID getCurrentCharityId() {
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

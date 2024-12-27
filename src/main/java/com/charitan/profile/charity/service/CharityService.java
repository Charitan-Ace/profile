package com.charitan.profile.charity.service;

import com.charitan.profile.charity.CharityExternalAPI;
import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.charity.dto.CharityDTO;
import com.charitan.profile.charity.dto.CharityUpdateRequest;
import com.charitan.profile.charity.entity.Charity;
import com.charitan.profile.charity.enums.OrganizationType;
import com.charitan.profile.charity.repository.CharityRepository;
import com.charitan.profile.stripe.StripeExternalAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CharityService implements CharityExternalAPI {
    @Autowired
    private CharityRepository charityRepository;
    @Autowired
    private StripeExternalAPI stripeExternalAPI;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHARITY_CACHE_PREFIX = "charity:";
    private static final String CHARITY_LIST_CACHE_KEY = "charities:all";

    public CharityService(@Qualifier("REDIS_CHARITIES") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

        Charity charity = new Charity(request.getUserId(), request.getCompanyName(), request.getAddress(), request.getTaxCode(), organizationType, stripeId, request.getAssetsKey());

        charityRepository.save(charity);

        // Add the new donor to the cache
        redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + request.getUserId(), new CharityDTO(charity));

        // Clear cache list on creation
        redisTemplate.delete(CHARITY_LIST_CACHE_KEY);
    }

    @Override
    public void updateCharity(CharityUpdateRequest request) {

        Charity charity = charityRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        if (request.getCompanyName() != null) {
            charity.setCompanyName(request.getCompanyName());
        }
        if (request.getTaxCode() != null) {
            charity.setTaxCode(request.getTaxCode());
        }
        if (request.getAddress() != null) {
            charity.setAddress(request.getAddress());
        }

        charityRepository.save(charity);

        // Update cache for this donor
        redisTemplate.opsForValue().set(CHARITY_CACHE_PREFIX + request.getUserId(), new CharityDTO(charity));

        // Clear cached list
        redisTemplate.delete(CHARITY_LIST_CACHE_KEY);
    }

    //TODO: get email from auth service
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

        return new CharityDTO(charity);
    }

    public Page<CharityDTO> getAll(int pageNo, int pageSize) {

        // Check if paginated donor list is in cache
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("companyName").descending());

        String fieldKey = "page:" + pageNo + ":size:" + pageSize;
        // Retrieve the cached donors list from the hash
        List<CharityDTO> cachedCharities = (List<CharityDTO>) redisTemplate.opsForHash().get(CHARITY_LIST_CACHE_KEY, fieldKey);

        if (cachedCharities != null) {
            return new PageImpl<>(cachedCharities, pageable, cachedCharities.size());
        }

        Page<Charity> charitiesPage = charityRepository.findAll(pageable);

        List<CharityDTO> charitiesResponses = charitiesPage.getContent().stream()
                .map(CharityDTO::new)
                .collect(Collectors.toList());

        // Cache the result
        redisTemplate.opsForHash().put(CHARITY_LIST_CACHE_KEY, "page:" + pageNo + ":size:" + pageSize, charitiesResponses);        System.out.println("Cache");
        return new PageImpl<>(charitiesResponses, pageable, charitiesPage.getTotalElements());
    }
}

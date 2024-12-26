package com.charitan.profile.donor.service;

import com.charitan.profile.donor.DonorExternalAPI;
import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.donor.dto.DonorDTO;
import com.charitan.profile.donor.dto.DonorUpdateRequest;
import com.charitan.profile.donor.entity.Donor;
import com.charitan.profile.donor.repository.DonorRepository;
import com.charitan.profile.stripe.StripeExternalAPI;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DonorService implements DonorExternalAPI {
    @Autowired
    private DonorRepository donorRepository;
    private final StripeExternalAPI stripeExternalAPI;
    @Qualifier("REDIS_PROFILE")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DONOR_CACHE_PREFIX = "donor:";
    private static final String DONOR_LIST_CACHE_KEY = "donors:all";

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

        // Clear cache list on creation
        redisTemplate.delete(DONOR_LIST_CACHE_KEY);
    }

    @Override
    public void updateDonor(DonorUpdateRequest request) {

        Donor donor = donorRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        if (request.getFirstName() != null) {
            donor.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            donor.setLastName(request.getLastName());
        }
        if (request.getAddress() != null) {
            donor.setAddress(request.getAddress());
        }

        donorRepository.save(donor);

        // Update cache for this donor
        redisTemplate.opsForValue().set(DONOR_CACHE_PREFIX + request.getUserId(), new DonorDTO(donor));

        // Clear cached list
        redisTemplate.delete(DONOR_LIST_CACHE_KEY);
    }

    //TODO: get email from auth service
    @Override
    public DonorDTO getInfo(UUID userId) {

        // Check if data is in cache
        String cacheKey = DONOR_CACHE_PREFIX + userId;
        DonorDTO cachedDonor = (DonorDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDonor != null) {
            return cachedDonor;
        }

        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        DonorDTO donorDTO = new DonorDTO(donor);
        redisTemplate.opsForValue().set(cacheKey, donorDTO);

        return new DonorDTO(donor);
    }

    public Page<DonorDTO> getAll(int pageNo, int pageSize) {

        // Check if paginated donor list is in cache
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("lastName").descending());

        String fieldKey = "page:" + pageNo + ":size:" + pageSize;
        // Retrieve the cached donors list from the hash
        List<DonorDTO> cachedDonors = (List<DonorDTO>) redisTemplate.opsForHash().get(DONOR_LIST_CACHE_KEY, fieldKey);

        if (cachedDonors != null) {
            return new PageImpl<>(cachedDonors, pageable, cachedDonors.size());
        }

        Page<Donor> donorsPage = donorRepository.findAll(pageable);

        List<DonorDTO> donorsResponses = donorsPage.getContent().stream()
                .map(DonorDTO::new)
                .collect(Collectors.toList());

        // Cache the result
        redisTemplate.opsForHash().put(DONOR_LIST_CACHE_KEY, "page:" + pageNo + ":size:" + pageSize, donorsResponses);        System.out.println("Cache");
        return new PageImpl<>(donorsResponses, pageable, donorsPage.getTotalElements());
    }
}

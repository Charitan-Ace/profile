package com.charitan.profile.stripe.service;

import com.charitan.profile.stripe.StripeExternalAPI;

import com.stripe.model.Customer;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class StripeService implements StripeExternalAPI {
    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.stripeSecretKey; // Set the API key after the bean is constructed
    }
    public String createStripeCustomer(String email, String name, String description, Map<String, String> metadata) {
        try {
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("email", email);
            customerParams.put("name", name);
            customerParams.put("description", description);
            customerParams.put("metadata", metadata);

            Customer stripeCustomer = Customer.create(customerParams);
            return stripeCustomer.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage(), e);
        }
    }
}

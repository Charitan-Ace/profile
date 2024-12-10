package com.charitan.profile.stripe.service;

import com.charitan.profile.stripe.StripeExternalAPI;
import io.github.cdimascio.dotenv.Dotenv;

import com.stripe.model.Customer;
import com.stripe.Stripe;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class StripeService implements StripeExternalAPI {

    private final String stripeSecretKey;

    public StripeService() {
        Dotenv dotenv = Dotenv.load();
        this.stripeSecretKey = dotenv.get("STRIPE_SECRET_KEY");
        Stripe.apiKey = this.stripeSecretKey; // Set Stripe API key globally
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

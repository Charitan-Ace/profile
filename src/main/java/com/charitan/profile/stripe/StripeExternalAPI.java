package com.charitan.profile.stripe;

import java.util.Map;

public interface StripeExternalAPI {
    public String createStripeCustomer(String email, String name, String description, Map<String, String> metadata);
}

package com.charitan.profile.jwt.external;

import io.jsonwebtoken.Claims;
import org.springframework.kafka.listener.ConsumerSeekAware;

import java.security.PublicKey;

public interface JwtExternalAPI {
    public void setSigPublicKey(PublicKey sigPublicKey);
    public Claims parseJwsPayload(String jws);
}

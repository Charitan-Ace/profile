package com.charitan.profile.jwt.internal;

import com.charitan.profile.jwt.external.JwtExternalAPI;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Setter;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
@Setter
public class JwtService implements JwtExternalAPI {

    private PublicKey sigPublicKey;

    public Claims parseJwsPayload(String jws) {

        System.out.println("Sig public key:" + this.sigPublicKey.getFormat());
        return Jwts.parser()
                .verifyWith(sigPublicKey)
                .build()
                .parseSignedClaims(jws)
                .getPayload();
    }
}
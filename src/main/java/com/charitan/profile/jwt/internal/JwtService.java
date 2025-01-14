package com.charitan.profile.jwt.internal;

import com.charitan.profile.jwt.external.JwtExternalAPI;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@Setter
public class JwtService implements JwtExternalAPI {

  private PublicKey sigPublicKey;

  public Claims parseJwsPayload(String jws) {

    System.out.println("Sig public key:" + this.sigPublicKey.getFormat());
    return Jwts.parser().verifyWith(sigPublicKey).build().parseSignedClaims(jws).getPayload();
  }
}

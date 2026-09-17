package philip.emerald.ace.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
public class JwtTokens {
 private final Algorithm algorithm;
 public JwtTokens(@Value("${ace.jwt-secret}") String secret){if(secret.length()<32)throw new IllegalArgumentException("JWT_SECRET must have at least 32 random characters");algorithm=Algorithm.HMAC256(secret);}
 public String issue(Accounts.User u,String id){return JWT.create().withIssuer("super-ace").withAudience("ace-web").withSubject(u.id()).withJWTId(id).withClaim("role",u.role().name()).withClaim("playerCode",u.publicCode()).withIssuedAt(Instant.now()).withExpiresAt(Instant.now().plusSeconds(900)).sign(algorithm);}
 public DecodedJWT verify(String token){try{return JWT.require(algorithm).withIssuer("super-ace").withAudience("ace-web").build().verify(token);}catch(Exception e){throw Accounts.unauthorized();}}
}

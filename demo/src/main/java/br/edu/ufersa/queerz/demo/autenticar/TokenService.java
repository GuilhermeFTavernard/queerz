package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;
    private static final String ISSUER = "Ecommerce Orders API";
    private static final String AUDIENCE = "ecommerce-orders-service";
    public String gerarToken(Usuario user){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .withSubject(user.getId().toString())
                    .withClaim("nome", user.getNome())
                    .withClaim("email", user.getEmail())
                    .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT de autenticação", exception);
        }
    }

    public String validarTokenEObterSubject(String tokenJWT){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            return null;
        }
    }
}

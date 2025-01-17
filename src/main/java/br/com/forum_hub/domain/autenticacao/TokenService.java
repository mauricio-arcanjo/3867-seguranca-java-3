package br.com.forum_hub.domain.autenticacao;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {

    public String gerarToken(Usuario usuario){
        try {
            var algorithm = Algorithm.HMAC256("12345678");

            return JWT.create()
                    .withIssuer("Forum Hub") //Name of application that issued token
                    .withSubject(usuario.getUsername()) // User identification
                    .withExpiresAt(expiracao(30))
                    .sign(algorithm);

        } catch (JWTVerificationException exception){
            throw new RegraDeNegocioException("Erro ao gerar token JWT de acesso");
        }
    }
    //Usado para revalidar o acesso após a expiração do token principal
    public String gerarRefreshToken(Usuario usuario) {
        try {
            var algorithm = Algorithm.HMAC256("12345678");

            return JWT.create()
                    .withIssuer("Forum Hub") //Name of application that issued token
                    .withSubject(usuario.getId().toString()) // User identification changed from userName to Id so tokens can be different
                    .withExpiresAt(expiracao(120)) // Expiração um pouco mais longa que o token principal
                    .sign(algorithm);

        } catch (JWTVerificationException exception){
            throw new RegraDeNegocioException("Erro ao gerar token JWT de acesso");
        }
    }

    public String verificarToken(String token){

        DecodedJWT decodedJWT;

        try {
            var algorithm = Algorithm.HMAC256("12345678");

            JWTVerifier verifier = JWT.require(algorithm)
                                        .withIssuer("Forum Hub")
                                        .build();

            decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();

        } catch (JWTVerificationException exception){
            throw new RegraDeNegocioException("Erro ao verificar token JWT de acesso");
        }
    }

    private Instant expiracao(Integer minutos) {
        return LocalDateTime.now().plusMinutes(minutos).toInstant(ZoneOffset.of("-05:00"));
    }


}

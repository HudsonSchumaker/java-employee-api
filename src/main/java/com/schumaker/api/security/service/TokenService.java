package com.schumaker.api.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.schumaker.api.security.model.entity.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final String ISSUER = "employee-api";

    @Value("${employee.api.jwt.secret}")
    private String secret;

    public String generateToken(AuthUser authUser) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(authUser.getEmail())
                    .withExpiresAt(getTokenExpiration())
                    .sign(algorithm);

        } catch (JWTCreationException exception){
            throw new RuntimeException("Error during generating token", exception);
        }
    }

    public String getSubject(String token) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            return  JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception){
            throw new RuntimeException("Token expired or invalid", exception);
        }
    }

    private Instant getTokenExpiration() {
        return LocalDateTime.now().plusHours(4).toInstant(ZoneOffset.UTC);
    }
}

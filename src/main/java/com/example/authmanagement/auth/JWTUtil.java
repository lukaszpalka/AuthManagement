package com.example.authmanagement.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.user.UserDto;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JWTUtil {

    private final RSAKeyUtil rsaKeyUtil;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JWTUtil(final RSAKeyUtil rsaKeyUtil) {
        this.rsaKeyUtil = rsaKeyUtil;
        this.algorithm = Algorithm.RSA256((RSAPublicKey) rsaKeyUtil.getPublicKey(), (RSAPrivateKey) rsaKeyUtil.getPrivateKey());
        this.verifier = JWT.require(this.algorithm)
                .build();
    }

    public String createAccessToken(UserDto userDto) {
        return JWT.create()
                .withSubject(userDto.username())
                .withClaim("id", userDto.id())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);
    }

    public String createRefreshToken(UserDto userDto) {
        return JWT.create()
                .withSubject(userDto.username())
                .withClaim("id", userDto.id())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(2592000)))
                .sign(algorithm);
    }

    public String createTokenWithCustomExpirationDate(UserDto userDto, Date date) {
        return JWT.create()
                .withSubject(userDto.username())
                .withClaim("id", userDto.id())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }

    public LocalDateTime getExpirationDate(DecodedJWT decodedJWT) {
        return LocalDateTime.ofInstant(decodedJWT.getExpiresAt().toInstant(), ZoneId.systemDefault());
    }

    public LocalDateTime getExpirationDate(String token) {
        if (token == null) {
            return null;
        } else return LocalDateTime.ofInstant(verifyToken(token).getExpiresAt().toInstant(), ZoneId.systemDefault());
    }

    private RSAPublicKey getPublicKey() {
        return (RSAPublicKey) rsaKeyUtil.getPublicKey();
    }
}

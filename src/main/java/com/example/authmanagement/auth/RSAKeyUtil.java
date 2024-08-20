package com.example.authmanagement.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Getter
@Component
public class RSAKeyUtil {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RSAKeyUtil(@Value("${auth0.rsa.public-key}") String publicKeyPem, @Value("${auth0.rsa.private-key}") String privateKeyPem) throws Exception {
        this.publicKey = loadPublicKey(publicKeyPem);
        this.privateKey = loadPrivateKey(privateKeyPem);
    }

    private PublicKey loadPublicKey(String publicKeyPem) throws Exception {
        byte[] encoded = Base64.getDecoder().decode(publicKeyPem);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey loadPrivateKey(String privateKeyPem) throws Exception {
        byte[] encoded = Base64.getDecoder().decode(privateKeyPem);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }
}

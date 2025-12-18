package com.htv.oauth2.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        // Generate RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Get keys
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Save private key (PKCS#8)
        // Thay đoạn lưu Private Key bằng cách này để đảm bảo xuống dòng chuẩn
        String privateKeyContent = Base64.getMimeEncoder(64, System.lineSeparator().getBytes())
                .encodeToString(privateKey.getEncoded());
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----" + System.lineSeparator() +
                privateKeyContent + System.lineSeparator() +
                "-----END PRIVATE KEY-----";

        Files.writeString(Paths.get("src/main/resources/keys/private_key.pem"), privateKeyPEM);

        Files.writeString(
                Paths.get("src/main/resources/keys/private_key.pem"),
                privateKeyPEM
        );

        // Save public key
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----"+ System.lineSeparator() +
                Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(publicKey.getEncoded()) +
                System.lineSeparator() + "-----END PUBLIC KEY-----" + System.lineSeparator();

        Files.writeString(
                Paths.get("src/main/resources/keys/public_key.pem"),
                publicKeyPEM
        );

        System.out.println("✅ Keys generated successfully!");
        System.out.println("Private key format: " + privateKey.getFormat());
        System.out.println("Public key format: " + publicKey.getFormat());
    }
}

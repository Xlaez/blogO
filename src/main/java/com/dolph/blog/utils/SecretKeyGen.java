package com.dolph.blog.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class SecretKeyGen {
    private static final String SECRET_KEY_FILE_PATH = "secret.txt";
    private static SecretKey secretKey;

    static{
        try {
            secretKey = loadOrGenerateSecretKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretKey loadOrGenerateSecretKey() throws IOException {
        File file = new File(SECRET_KEY_FILE_PATH);
        if(file.exists()){
            try{
                byte[] encodedKey = Files.readAllBytes(file.toPath());
                return Keys.hmacShaKeyFor(encodedKey);
            }catch(IOException e){
                log.error("Secret Key Generator error: {}", e.getMessage());
                throw e;
            }
        }

        SecretKey newSecretKey = Keys.secretKeyFor(SignatureAlgorithm.ES512);

        try{
            Files.write(file.toPath(), newSecretKey.getEncoded());
        }catch(IOException e){
            log.error("Secret Key Generator error: {}", e.getMessage());
            throw e;
        }

        return newSecretKey;
    }
}

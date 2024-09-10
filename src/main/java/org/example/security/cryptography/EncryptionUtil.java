package org.example.security.cryptography;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.core.Block;

import static org.example.utils.JsonUtil.parseTransaction;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    static Dotenv dotenv = Dotenv.load();
    private static final String encodedKey = dotenv.get("KEY");
    private static final SecretKeySpec secretKey = new SecretKeySpec(Base64.getUrlDecoder().decode(encodedKey), ALGORITHM);

    public static String encryptBlock(Block block) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            ObjectMapper objectMapper = new ObjectMapper();
            String blockJson = objectMapper.writeValueAsString(block);

            byte[] encryptedBytes = cipher.doFinal(blockJson.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar bloco", e);
        }
    }

    public static Block decryptBlock(String encryptedBlock) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedBlock);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String decryptedString = new String(decryptedBytes);

            return parseBlock(decryptedString);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Erro ao descriptografar bloco", e);
        }
    }

    private static Block parseBlock(String blockJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(blockJson, Block.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            throw new RuntimeException("Erro ao converter JSON para Block", e);
        }
    }
}

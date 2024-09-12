package org.example.security.signatures;

import java.security.*;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.core.Block;
import org.example.security.utils.KeyLoaderUtil;

public class SignatureUtil {

    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            Dotenv dotenv = Dotenv.load();
            privateKey = KeyLoaderUtil.loadPrivateKey(dotenv.get("PRIVATE_KEY_PATH"));
            publicKey = KeyLoaderUtil.loadPublicKey(dotenv.get("PUBLIC_KEY_PATH"));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar chaves", e);
        }
    }

    public static String signTransaction(Block transaction) {
        try {

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            String blockJson = objectMapper.writeValueAsString(transaction);
            signature.update(blockJson.getBytes("UTF-8"));
            byte[] signedData = signature.sign();
            return Base64.getEncoder().encodeToString(signedData);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao assinar transação", e);
        }
    }

    public static boolean verifySignature(Block transaction, String signature) {
        try {
            transaction.setSignature(null);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);

            String blockJson = objectMapper.writeValueAsString(transaction);
            verifier.update(blockJson.getBytes("UTF-8"));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            boolean isValid = verifier.verify(signatureBytes);

            transaction.setSignature(signature);

            return isValid;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar assinatura da transação", e);
        }
    }
}

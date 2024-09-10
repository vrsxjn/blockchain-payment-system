package org.example.security.utils;

public class SecurityUtils {

    public static boolean isValidBase64(String input) {
        try {
            java.util.Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidHex(String input) {
        return input.matches("^[0-9a-fA-F]+$");
    }
}

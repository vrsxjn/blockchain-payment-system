package org.example.utils;

public class BlockchainUtils {
    private static final int DIFFICULTY = 4;
    private static final int REWARD = 50;
    public static int getDifficulty() {
        return DIFFICULTY;
    }
    public static int getReward() {
        return REWARD;
    }
    public static boolean isValidHash(String hash) {
        String target = new String(new char[DIFFICULTY]).replace('\0', '0');
        return hash.startsWith(target);
    }
}

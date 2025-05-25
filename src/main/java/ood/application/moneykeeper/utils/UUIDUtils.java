package ood.application.moneykeeper.utils;

import java.util.UUID;

public class UUIDUtils {
    public static String getFirst6Chars(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        String uuidStr = uuid.toString().replace("-", "");
        return uuidStr.substring(0, Math.min(uuidStr.length(), 6));
    }

    public static String generateShortUUID() {
        return getFirst6Chars(UUID.randomUUID());
    }
}
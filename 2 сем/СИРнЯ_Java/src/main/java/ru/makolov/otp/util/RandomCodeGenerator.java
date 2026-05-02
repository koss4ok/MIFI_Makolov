package ru.makolov.otp.util;

import java.security.SecureRandom;

public final class RandomCodeGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RandomCodeGenerator() {
    }

    public static String digits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}

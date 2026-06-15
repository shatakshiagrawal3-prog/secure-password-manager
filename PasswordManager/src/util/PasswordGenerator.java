package util;

import java.security.SecureRandom;

/**
 * Utility Class: PasswordGenerator
 *
 * Generates a strong random password of a given length.
 * Always includes at least one uppercase, lowercase, digit, and special character.
 * Uses SecureRandom (more secure than Random).
 */
public class PasswordGenerator {

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+";
    private static final String ALL     = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random password.
     * @param length Desired length (minimum 8)
     * @return A strong random password string
     */
    public static String generate(int length) {
        if (length < 8) length = 8;

        StringBuilder sb = new StringBuilder();

        // Guarantee at least one of each required character type
        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill the rest with random characters from the full set
        for (int i = 4; i < length; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        // Shuffle the characters so the required ones aren't always first
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        return new String(arr);
    }
}

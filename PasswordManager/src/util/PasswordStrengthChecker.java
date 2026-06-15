package util;

/**
 * Utility Class: PasswordStrengthChecker
 *
 * Checks how strong a password is based on:
 * - Length (8+ chars = 1 point, 12+ = 1 more point)
 * - Has uppercase letters
 * - Has lowercase letters
 * - Has digits (0-9)
 * - Has special characters (!@#$ etc.)
 *
 * Score → Strength:
 *   0-2 → WEAK
 *   3   → MODERATE
 *   4   → STRONG
 *   5-6 → VERY STRONG
 */
public class PasswordStrengthChecker {

    // Enum to represent strength levels
    public enum Strength {
        WEAK,
        MODERATE,
        STRONG,
        VERY_STRONG
    }

    /** Calculates a score from 0 to 6 for the given password */
    public static int getScore(String password) {
        int score = 0;
        if (password.length() >= 8)                      score++; // At least 8 chars
        if (password.length() >= 12)                     score++; // At least 12 chars
        if (password.matches(".*[A-Z].*"))               score++; // Has uppercase
        if (password.matches(".*[a-z].*"))               score++; // Has lowercase
        if (password.matches(".*[0-9].*"))               score++; // Has digit
        if (password.matches(".*[!@#$%^&*()_+].*"))     score++; // Has special char
        return score;
    }

    /** Returns the Strength level for the given password */
    public static Strength check(String password) {
        int score = getScore(password);
        if (score <= 2) return Strength.WEAK;
        if (score == 3) return Strength.MODERATE;
        if (score == 4) return Strength.STRONG;
        return Strength.VERY_STRONG;
    }

    /**
     * Returns true if the password is acceptable (score >= 4 = STRONG or better).
     * Used when the user tries to set a new password.
     */
    public static boolean isAcceptable(String password) {
        return getScore(password) >= 4;
    }
}

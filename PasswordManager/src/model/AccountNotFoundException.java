package model;

/**
 * Thrown when an operation is attempted on a website/account that does not exist in the PasswordManager.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String website) {
        super("No account found for: " + website);
    }
}

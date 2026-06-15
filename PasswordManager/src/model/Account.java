package model;

import interfaces.Searchable;
import interfaces.Exportable;
import interfaces.Encryptable;
import util.PasswordStrengthChecker;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Abstract Class: Account
 *
 * This is the base class for all account types (Social, Banking, Email, Work).
 * It demonstrates key OOP concepts:
 *  - Abstraction (abstract class with abstract method getType())
 *  - Encapsulation (private fields with getters/setters)
 *  - Polymorphism (each subclass provides its own getType())
 *  - Interfaces (Searchable, Exportable, Encryptable)
 */
public abstract class Account implements Searchable, Exportable, Encryptable {

    private String website;
    private String username;
    private String password;
    private String tag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ArrayList<String> passwordHistory;     // Stores last 5 passwords

    public Account(String website, String username, String password, String tag) {
        this.website = website;
        this.username = username;
        this.password = password;
        this.tag = tag;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.passwordHistory = new ArrayList<>();
    }

    public String getWebsite()  { return website; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getTag()      { return tag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public ArrayList<String> getPasswordHistory() { return passwordHistory; }

    public void setUsername(String username) { this.username = username; }
    public void setTag(String tag)           { this.tag = tag; }


    public void setPassword(String newPassword) {
        if (this.password != null && !this.password.isEmpty()) {
            passwordHistory.add(this.password);
            if (passwordHistory.size() > 5) {
                passwordHistory.remove(0); // Remove oldest if history is full
            }
        }
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }


    public abstract String getType();


    @Override
    public boolean matches(String keyword) {
        String kw = keyword.toLowerCase();
        return website.toLowerCase().contains(kw)
            || username.toLowerCase().contains(kw)
            || getType().toLowerCase().contains(kw)
            || tag.toLowerCase().contains(kw);
    }

    /** Returns a CSV row for this account */
    @Override
    public String toCSV() {
        return getType() + "," + website + "," + username + "," + password + "," + tag;
    }

    @Override
    public String toDisplayString() {
        return "[" + getType() + "] " + website + " | " + username + " | Tag: " + tag;
    }

    @Override
    public String toString() { return toDisplayString(); }

    // ---- AES Encryption / Decryption ----

    private byte[] buildKey(String key) {
        byte[] keyBytes = new byte[16];
        byte[] input = key.getBytes();
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (i < input.length) ? input[i] : 0;
        }
        return keyBytes;
    }

    /** Encrypts the password using AES and returns a Base64 string */
    @Override
    public String encrypt(String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(buildKey(key), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes()));
        } catch (Exception e) {
            return password; 
        }
    }

    /** Decrypts the password from a Base64 AES string */
    @Override
    public String decrypt(String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(buildKey(key), "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(password)));
        } catch (Exception e) {
            return password; 
        }
    }
}

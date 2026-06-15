package core;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Class: UserManager
 *
 * Handles user registration and login.
 * Passwords are stored as SHA-256 hashes (never in plain text).
 * User data is saved to "data/users.dat" file.
 *
 * OOP Concepts used:
 *  - Encapsulation (private fields, public methods)
 *  - File I/O for persistent storage
 */
public class UserManager {

    private static final String USERS_FILE = "data/users.dat";

    // Stores username (lowercase) → hashed password
    private Map<String, String> users = new HashMap<>();

    public UserManager() {
        // Make sure the data folder exists
        new File("data").mkdirs();
        loadUsers();
    }

    /**
     * Registers a new user.
     * @return true if registration succeeded, false if username already taken
     */
    public boolean register(String username, String password) {
        String key = username.toLowerCase();
        if (users.containsKey(key)) {
            return false; // Username already exists
        }
        users.put(key, hash(password));
        saveUsers();
        return true;
    }

    /**
     * Checks if the given username + password are correct.
     * @return true if credentials match
     */
    public boolean authenticate(String username, String password) {
        String key = username.toLowerCase();
        return users.containsKey(key) && users.get(key).equals(hash(password));
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public static String getDataFileFor(String username) {
        return "data/" + username.toLowerCase() + "_passwords.dat";
    }


    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input; 
        }
    }

    private void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void saveUsers() {
        try (FileWriter fw = new FileWriter(USERS_FILE)) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                fw.write(entry.getKey() + "|" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            System.out.println("Could not save users: " + e.getMessage());
        }
    }
}

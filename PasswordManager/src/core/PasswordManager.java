package core;

import model.*;
import util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class: PasswordManager
 *
 * The main logic class. Manages a list of Account objects for a logged-in user.
 * Handles adding, deleting, editing, searching, sorting, saving, and loading accounts.
 *
 * OOP Concepts used:
 *  - Composition (contains a list of Account objects)
 *  - Polymorphism (works with any subclass of Account)
 *  - File I/O for persistence
 */
public class PasswordManager {

    private ArrayList<Account> accounts = new ArrayList<>();

    //used to encrypt/decrypt passwords before saving to file
    private static final String ENC_KEY = "PM_AES_KEY";

    // The file path where this user's accounts are saved
    private final String dataFile;

    public PasswordManager(String username) {
        this.dataFile = UserManager.getDataFileFor(username);
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    
    public void delete(String website) {
        boolean removed = accounts.removeIf(a -> a.getWebsite().equalsIgnoreCase(website));
        if (!removed) {
            throw new AccountNotFoundException(website);
        }
    }

    public void editPassword(String website, String newPassword) {
        if (!PasswordStrengthChecker.isAcceptable(newPassword)) {
            return;
        }
        boolean found = false;
        for (Account a : accounts) {
            if (a.getWebsite().equalsIgnoreCase(website)) {
                a.setPassword(newPassword);
                found = true;
            }
        }
        if (!found) {
            throw new AccountNotFoundException(website);
        }
    }

    public void sortByType() {
        accounts.sort(Comparator.comparing(Account::getType));
    }

    public void sortByWebsite() {
        accounts.sort(Comparator.comparing(Account::getWebsite));
    }


    public void exportToCSV() {
        try (FileWriter fw = new FileWriter("export.csv")) {
            fw.write("Type,Website,Username,Password,Tag\n");
            for (Account a : accounts) {
                fw.write(a.toCSV() + "\n");
            }
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }


    public void saveToFile() {
        try (FileWriter fw = new FileWriter(dataFile)) {
            for (Account a : accounts) {
                fw.write(a.getType() + "|" + a.getWebsite() + "|" + a.getUsername()
                        + "|" + a.encrypt(ENC_KEY) + "|" + a.getTag() + "\n");
            }
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }


    public void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String type     = parts[0];
                String website  = parts[1];
                String username = parts[2];
                String encPwd   = parts[3]; // Still encrypted
                String tag      = parts[4];

                Account acc = null;
                switch (type) {
                    case "Social Media":
                        acc = new SocialAccount(website, username, encPwd, website, tag);
                        break;
                    case "Banking":
                        acc = new BankingAccount(website, username, encPwd, website, tag);
                        break;
                    case "Email":
                        acc = new EmailAccount(website, username, encPwd, "N/A", tag);
                        break;
                    case "Work":
                        acc = new WorkAccount(website, username, encPwd, "N/A", tag);
                        break;
                }

                if (acc != null) {
                    String decrypted = acc.decrypt(ENC_KEY);
                    acc.getPasswordHistory().clear();
                    acc.setPassword(decrypted);
                    acc.getPasswordHistory().clear();
                    accounts.add(acc);
                }
            }
        } catch (Exception e) {
            System.out.println("No saved data found. Starting fresh.");
        }
    }

    public void backup() {
        BackupManager.backup(accounts, ENC_KEY);
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }
}

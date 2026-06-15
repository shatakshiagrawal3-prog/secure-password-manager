package util;

import model.Account;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Utility Class: BackupManager
 *
 * Creates a backup file of all accounts in the "backups/" folder.
 * Each backup file is named with the current date and time.
 * Passwords are stored encrypted in the backup.
 */
public class BackupManager {

    private static final String BACKUP_DIR = "backups";

    /**
     * Creates a backup of all accounts to a timestamped file.
     * @param accounts  List of accounts to back up
     * @param encKey    Encryption key used to encrypt passwords in backup
     */
    public static void backup(ArrayList<Account> accounts, String encKey) {
        // Create the backup directory if it doesn't exist
        new File(BACKUP_DIR).mkdirs();

        // Create a file name like: backups/backup_20260418_143000.txt
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = BACKUP_DIR + "/backup_" + timestamp + ".txt";

        try (FileWriter fw = new FileWriter(filename)) {
            for (Account a : accounts) {
                fw.write(a.getType() + "," + a.getWebsite() + "," + a.getUsername()
                        + "," + a.encrypt(encKey) + "," + a.getTag() + "\n");
            }
            System.out.println("Backup saved to: " + filename);
        } catch (Exception e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }
}

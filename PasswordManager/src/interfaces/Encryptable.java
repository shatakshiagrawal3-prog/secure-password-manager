package interfaces;

/**
 * Interface: Encryptable
 * Any class that stores sensitive data should implement this
 * to support encryption and decryption.
 */
public interface Encryptable {
    String encrypt(String key);
    String decrypt(String key);
}

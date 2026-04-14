package com.example.mannydasilvaweighttrackingapp;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtils
 * This utility class is responsible for hashing and verifying user passwords.
 *
 * This class:
 * - Generates random salts
 * - Hashes passwords using PBKDF2 with HMAC-SHA256
 * - Verifies passwords using constant-time comparison
 */
public final class PasswordUtils {

    // Number of bytes used for generated salt
    private static final int SALT_BYTES = 16;

    // Number of PBKDF2 iterations
    private static final int ITERATIONS = 120_000;

    // Length of derived key in bits
    private static final int KEY_LENGTH_BITS = 256;

    /**
     * Private constructor
     */
    private PasswordUtils() { }

    /**
     * generateSaltBase64()
     * Generates a random salt and returns it encoded in Base64.
     *
     * @return String - The random salt encoded in Base64 format.
     */
    public static String generateSaltBase64() {

        // Allocate byte array for salt
        byte[] salt = new byte[SALT_BYTES];

        // Fill salt with random bytes
        new SecureRandom().nextBytes(salt);

        // Encode salt as Base64 string
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * hashPasswordBase64(String plainPassword, String saltBase64)
     * Hashes a password using PBKDF2 with HMAC-SHA256 and returns the result in Base64.
     *
     * @param plainPassword - The plain text password entered by the user.
     * @param saltBase64 - The Base64-encoded salt.
     * @return String - The hashed password encoded in Base64.
     */
    public static String hashPasswordBase64(String plainPassword, String saltBase64) {

        // Decode Base64 salt back into raw bytes
        byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);

        // Perform PBKDF2 hashing
        byte[] hash = pbkdf2(plainPassword, salt);

        // Return hash encoded as Base64
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    /**
     * verifyPassword(String plainPassword, String saltBase64, String expectedHashBase64)
     * Verifies whether a provided password matches the stored salted hash.
     *
     * @param plainPassword - The password entered by user.
     * @param saltBase64 - The stored salt in Base64 format.
     * @param expectedHashBase64 - The stored hashed password in Base64 format.
     * @return boolean - True if password matches, otherwise false.
     */
    public static boolean verifyPassword(String plainPassword, String saltBase64, String expectedHashBase64) {

        // Re-hash the input password using the stored salt
        String actual = hashPasswordBase64(plainPassword, saltBase64);

        // Compare using constant-time comparison
        return constantTimeEquals(actual, expectedHashBase64);
    }

    /**
     * pbkdf2(String password, byte[] salt)
     * Performs PBKDF2 hashing using HMAC-SHA256.
     *
     * @param password - The plain text password.
     * @param salt - The raw salt bytes.
     * @return byte[] - The derived key (hashed password).
     */
    private static byte[] pbkdf2(String password, byte[] salt) {

        try {
            // Define PBKDF2 key specification
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH_BITS
            );

            // Use SHA-256 based PBKDF2
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // Generate derived key
            return skf.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            // Runtime exception
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * constantTimeEquals(String a, String b)
     * Compares two strings in constant time.
     *
     * @param a - The first string.
     * @param b - The second string.
     * @return boolean - True if values are equal.
     */
    private static boolean constantTimeEquals(String a, String b) {

        // Prevent null comparison
        if (a == null || b == null) return false;

        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);

        // Length mismatch means not equal
        if (x.length != y.length) return false;

        int result = 0;

        // Compare byte-by-byte without early exit
        for (int i = 0; i < x.length; i++) {
            result |= x[i] ^ y[i];
        }

        return result == 0;
    }
}
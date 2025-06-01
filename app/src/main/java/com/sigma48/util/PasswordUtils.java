
package com.sigma48.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Konstruktor privat untuk mencegah pembuatan instance dari class ini
    private PasswordUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    // Metode untuk menghasilkan hash dari plainPassword menggunakan algoritma BCrypt
    public static String hashPassword(String plainPassword) {
        // Validasi untuk memastikan plainPassword tidak null atau kosong
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        // Menghasilkan hash dengan BCrypt
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Metode untuk memverifikasi apakah plainPassword cocok dengan hashedPassword
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // Validasi untuk memastikan plainPassword dan hashedPassword tidak null atau kosong
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            throw new IllegalArgumentException("Password and hash cannot be null or empty.");
        }

        // Verifikasi kecocokan antara plainPassword dan hashedPassword menggunakan BCrypt
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Menangani jika format hash tidak valid
            throw new IllegalArgumentException("Invalid hash format.", e);
        }
    }
}

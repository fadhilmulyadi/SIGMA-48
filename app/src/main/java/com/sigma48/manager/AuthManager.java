package com.sigma48.manager;

import com.sigma48.dao.UserDao;
import com.sigma48.model.User;
import com.sigma48.util.PasswordUtils;

import java.util.Optional;

public class AuthManager {

    // Atribut privat untuk encapsulation
    private UserDao userDao;
    private User currentUser; // Menyimpan sesi pengguna yang sedang login.

    // Konstruktor untuk inisialisasi UserDao
    public AuthManager(UserDao userDao) {
        this.userDao = userDao;
        this.currentUser = null; // Set currentUser ke null pada awalnya
    }

    // Metode untuk login, menggunakan prinsip abstraction dan encapsulation
    public boolean login(String username, String password) {
        // Validasi input username dan password
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Mencari user berdasarkan username
        Optional<User> userOptional = userDao.findByUsername(username);
        
        // Jika user ditemukan dan password cocok, set currentUser
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordUtils.checkPassword(password, user.getPasswordHash())) {
                this.currentUser = user; // Set user yang sedang login
                return true;
            }
        }

        // Jika gagal login, set currentUser ke null dan return false
        this.currentUser = null;
        return false;
    }

    // Metode untuk logout, menghapus sesi login
    public void logout() {
        this.currentUser = null;
    }

    // Metode untuk mendapatkan user yang sedang login
    public User getCurrentUser() {
        return this.currentUser;
    }

    // Metode untuk mengecek apakah ada user yang sedang login
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }
}

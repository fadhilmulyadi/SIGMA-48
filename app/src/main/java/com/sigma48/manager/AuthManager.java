package com.sigma48.manager;

import com.sigma48.dao.UserDao;
import com.sigma48.model.User;
import com.sigma48.util.PasswordUtils;
import java.time.LocalDateTime;
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
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        Optional<User> userOptional = userDao.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordUtils.checkPassword(password, user.getPasswordHash())) {
                // PERUBAHAN PENTING: Cek apakah akun aktif sebelum login
                if (!user.isActive()) {
                    System.err.println("AuthManager: Login gagal. Akun " + username + " tidak aktif.");
                    this.currentUser = null;
                    return false;
                }
                
                this.currentUser = user;
                
                user.setLastLogin(LocalDateTime.now());
                userDao.saveUser(user);
                
                return true;
            }
        }

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

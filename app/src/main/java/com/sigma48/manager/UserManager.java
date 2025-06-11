package com.sigma48.manager;

import com.sigma48.dao.UserDao;
import com.sigma48.model.Agent;
import com.sigma48.model.User;
import com.sigma48.model.Role;
import com.sigma48.util.PasswordUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManager {
    private UserDao userDao;

    public UserManager(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    public Optional<User> findUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return userDao.findUserById(userId);
    }

    public List<User> getUsersByRole(Role role) {
        if (role == null) {
            return new ArrayList<>();
        }
        List<User> allUsers = getAllUsers();
        return allUsers.stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    public Optional<User> findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userDao.findByUsername(username);
    }

    public Optional<User> createUser(String username, String plainPassword, Role role, List<String> spesialisasiIfAgent, boolean isActive) {
        // Validasi input dasar
        if (username == null || username.trim().isEmpty() || 
            plainPassword == null || plainPassword.isEmpty() || role == null) {
            System.err.println("UserManager: Username, password, dan role tidak boleh kosong.");
            return Optional.empty();
        }

        // Cek apakah username sudah ada
        if (userDao.findByUsername(username).isPresent()) {
            System.err.println("UserManager: Username '" + username + "' sudah digunakan.");
            return Optional.empty();
        }

        String passwordHash = PasswordUtils.hashPassword(plainPassword);
        User newUser;

        if (role == Role.AGEN_LAPANGAN) {
            Agent newAgent = new Agent();
            newAgent.setUsername(username);
            newAgent.setPasswordHash(passwordHash);
            newAgent.setRole(role); 
            
            if (spesialisasiIfAgent != null) {
                newAgent.setSpesialisasi(spesialisasiIfAgent);
            }
            newUser = newAgent;
        } else {
            newUser = new User(username, passwordHash, role);
        }
        
        newUser.setActive(isActive); // Gunakan status dari parameter

        boolean saved = userDao.saveUser(newUser);
        return saved ? Optional.of(newUser) : Optional.empty();
    }

    public boolean updateUser(String userId, String newUsername, Role newRole, 
                              Boolean isActive, List<String> newSpesialisasiIfAgent) {
        Optional<User> userOptional = userDao.findUserById(userId);
        if (!userOptional.isPresent()) {
            System.err.println("UserManager: Pengguna dengan ID '" + userId + "' tidak ditemukan.");
            return false;
        }
        User userToUpdate = userOptional.get();

        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equalsIgnoreCase(userToUpdate.getUsername())) {
            if (userDao.findByUsername(newUsername).isPresent()) {
                System.err.println("UserManager: Username baru '" + newUsername + "' sudah digunakan.");
                return false;
            }
            userToUpdate.setUsername(newUsername);
        }

        if (newRole != null && userToUpdate.getRole() != newRole) {
            userToUpdate.setRole(newRole);
        }
        
        if (userToUpdate instanceof Agent) {
            ((Agent) userToUpdate).setSpesialisasi(newSpesialisasiIfAgent != null ? newSpesialisasiIfAgent : new ArrayList<>());
        }

        if (isActive != null) {
            userToUpdate.setActive(isActive);
        }
        
        return userDao.saveUser(userToUpdate);
    }

    public boolean resetUserPassword(String userId, String newPlainPassword) {
        if (newPlainPassword == null || newPlainPassword.trim().isEmpty()) {
            System.err.println("UserManager: Password baru tidak boleh kosong.");
            return false;
        }
        Optional<User> userOptional = userDao.findUserById(userId);
        if (!userOptional.isPresent()) {
            System.err.println("UserManager: Pengguna dengan ID '" + userId + "' tidak ditemukan untuk reset password.");
            return false;
        }
        User userToUpdate = userOptional.get();
        userToUpdate.setPasswordHash(PasswordUtils.hashPassword(newPlainPassword));
        return userDao.saveUser(userToUpdate);
    }

    public boolean setUserStatus(String userId, boolean activeStatus) {
        Optional<User> userOptional = userDao.findUserById(userId);
        if (!userOptional.isPresent()) {
            System.err.println("UserManager: Pengguna dengan ID '" + userId + "' tidak ditemukan untuk mengubah status.");
            return false;
        }
        User userToUpdate = userOptional.get();
        userToUpdate.setActive(activeStatus);
        return userDao.saveUser(userToUpdate);
    }

    public boolean deleteUser(String userId) {
        return userDao.deleteUser(userId);
    }
}
package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sigma48.model.User;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final String USERS_FILE_PATH = "data/users.json";
    private ObjectMapper objectMapper;

    public UserDao() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        initializeUsersFile();
    }

    private void initializeUsersFile() {
        try {
            File usersFile = new File(USERS_FILE_PATH);
            if (!usersFile.exists()) {
                if (usersFile.getParentFile() != null && !usersFile.getParentFile().exists()) {
                    usersFile.getParentFile().mkdirs();
                }
                objectMapper.writeValue(usersFile, new ArrayList<User>());
                System.out.println("File " + USERS_FILE_PATH + " berhasil diinisialisasi.");
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi file pengguna: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return getAllUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
    
    public boolean saveUser(User userToSave) {
        try {
            List<User> users = getAllUsers();
            users.removeIf(u -> u.getId().equals(userToSave.getId()));
            users.add(userToSave);
            writeAllUsersToFile(users);
            return true;
        } catch (IOException e) {
            System.err.println("Error menyimpan data pengguna: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<User> findUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        List<User> users = getAllUsers();
        return users.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findFirst();
    }
    
    public List<User> getAllUsers() {
        try {
            return readAllUsersFromFile();
        } catch (IOException e) {
            System.err.println("Error membaca semua data pengguna: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<User> readAllUsersFromFile() throws IOException {
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists() || usersFile.length() == 0) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
    }
    
    private void writeAllUsersToFile(List<User> users) throws IOException {
        objectMapper.writeValue(new File(USERS_FILE_PATH), users);
    }

    public boolean deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            System.err.println("UserDao: ID pengguna tidak boleh kosong untuk dihapus.");
            return false;
        }
        try {
            List<User> users = getAllUsers();
            boolean removed = users.removeIf(user -> user.getId().equals(userId));
            if (removed) {
                writeAllUsersToFile(users);
                System.out.println("User dengan ID: " + userId + " berhasil dihapus.");
                return true;
            } else {
                System.out.println("User dengan ID: " + userId + " tidak ditemukan untuk dihapus.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error menghapus pengguna: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

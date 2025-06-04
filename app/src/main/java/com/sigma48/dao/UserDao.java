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
        
        try {
            File usersFile = new File(USERS_FILE_PATH);
            if (!usersFile.exists()) {
                if (usersFile.getParentFile() != null) {
                    usersFile.getParentFile().mkdirs();
                }
                objectMapper.writeValue(usersFile, new ArrayList<User>());
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi file users.json: " + e.getMessage());
        }
    }

    private List<User> getAllUsersInternal() throws IOException {
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists() || usersFile.length() == 0) {
            return new ArrayList<>(); // Kembalikan list kosong jika file tidak ada atau kosong
        }
        return objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
    }
    
    private void saveAllUsersInternal(List<User> users) throws IOException {
        objectMapper.writeValue(new File(USERS_FILE_PATH), users);
    }

    public Optional<User> findByUsername(String username) {
        try {
            List<User> users = getAllUsersInternal();
            return users.stream()
                        .filter(user -> user.getUsername().equalsIgnoreCase(username))
                        .findFirst();
        } catch (IOException e) {
            System.err.println("Error membaca data pengguna: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean saveUser(User userToSave) {
        try {
            List<User> users = getAllUsersInternal();
            users.removeIf(u -> u.getId().equals(userToSave.getId()));
            users.add(userToSave);
            saveAllUsersInternal(users);
            return true;
        } catch (IOException e) {
            System.err.println("Error menyimpan data pengguna: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            List<User> users = getAllUsersInternal(); // Asumsi metode ini sudah ada dan mengambil semua user
            return users.stream()
                        .filter(user -> user.getId().equals(userId))
                        .findFirst();
        } catch (IOException e) {
            System.err.println("Error membaca data pengguna saat mencari berdasarkan ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<User> getAllUsers() {
           try {
               return getAllUsersInternal();
           } catch (IOException e) {
               System.err.println("Error membaca semua data pengguna: " + e.getMessage());
               e.printStackTrace();
               return new ArrayList<>();
           }
       }
}

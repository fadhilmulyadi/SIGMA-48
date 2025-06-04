package com.sigma48.manager;

import com.sigma48.dao.UserDao;
import com.sigma48.model.User;
import com.sigma48.model.Role;
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
        return userDao.getAllUsers();
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
}
package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.dao.base.GenericDao;
import com.sigma48.model.User;

import java.util.List;
import java.util.Optional;

public class UserDao extends GenericDao<User> {

    public UserDao() {
        super("data/users.json", new TypeReference<List<User>>() {});
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return find(user -> user.getUsername().equalsIgnoreCase(username));
    }
    
    public Optional<User> findUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return find(user -> user.getId().equals(userId));
    }

    public boolean saveUser(User user) {
        if (user == null) return false;
        save(user, u -> u.getId().equals(user.getId()));
        return true;
    }

    public boolean deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return delete(user -> user.getId().equals(userId));
    }
}
package com.sigma48.manager;

import com.sigma48.dao.UserDao;
import com.sigma48.model.Agent;
import com.sigma48.model.Role;
import com.sigma48.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AgentManager {
    private UserDao userDao;

    public AgentManager(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<Agent> getAvailableAgents() {
        List<User> allUsers = userDao.getAll(); 
        if (allUsers == null) {
            return new ArrayList<>();
        }

        return allUsers.stream()
                .filter(user -> user.getRole() == Role.AGEN_LAPANGAN && user instanceof Agent)
                .map(user -> (Agent) user)
                .collect(Collectors.toList());
    }
}
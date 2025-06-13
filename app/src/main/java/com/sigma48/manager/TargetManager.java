package com.sigma48.manager;

import com.sigma48.dao.TargetDao;
import com.sigma48.model.Target;

import java.util.List;
import java.util.Optional;

public class TargetManager {
    private TargetDao targetDao;

    public TargetManager(TargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public List<Target> getAllTargets() {
        return targetDao.getAll();
    }

    public Optional<Target> getTargetById(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return Optional.empty();
        }
        return targetDao.findById(targetId);
    }

    public boolean saveTarget(Target target) {
        if (target == null || target.getNama() == null || target.getNama().trim().isEmpty()) {
            System.err.println("Nama target tidak boleh kosong.");
            return false;
        }
        return targetDao.save(target);
    }

    public boolean deleteTarget(String targetId) {
        return targetDao.delete(targetId);
    }
}
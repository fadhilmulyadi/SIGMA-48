package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.dao.base.GenericDao;
import com.sigma48.model.Target;

import java.util.List;
import java.util.Optional;

public class TargetDao extends GenericDao<Target> {

    public TargetDao() {
        super("data/targets.json", new TypeReference<List<Target>>() {});
    }

    public Optional<Target> findTargetById(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return Optional.empty();
        }
        return find(target -> target.getId().equals(targetId));
    }

    public boolean saveTarget(Target target) {
        if (target == null) return false;
        save(target, t -> t.getId().equals(target.getId()));
        return true;
    }

    public boolean deleteTarget(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return false;
        }
        return delete(target -> target.getId().equals(targetId));
    }
}
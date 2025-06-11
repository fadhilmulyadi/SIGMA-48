package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.dao.base.GenericDao;
import com.sigma48.model.Mission;

import java.util.List;
import java.util.Optional;

public class MissionDao extends GenericDao<Mission> {

    public MissionDao() {
        super("data/missions.json", new TypeReference<List<Mission>>() {});
    }

    public Optional<Mission> findMissionById(String missionId) {
        return find(mission -> mission.getId().equals(missionId));
    }

    public boolean saveMission(Mission mission) {
        save(mission, m -> m.getId().equals(mission.getId()));
        return true;
    }

    public boolean deleteMission(String missionId) {
        return delete(m -> m.getId().equals(missionId));
    }
}
package com.sigma48.manager;

import com.sigma48.dao.MissionDao;
import com.sigma48.dao.TargetDao;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Target;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MissionManager {

    public final MissionDao missionDao;
    private final TargetDao targetDao;

    //Konstruktor
    public MissionManager(MissionDao missionDao, TargetDao targetDao) {
        this.missionDao = missionDao;
        this.targetDao = targetDao;
    }

    //Method createDraftMission
    public Optional<Mission> createDraftMission(String judul, String tujuan, String deskripsi, String targetId) {
        if (judul == null || judul.trim().isEmpty() || tujuan == null || tujuan.trim().isEmpty() || targetId == null || targetId.trim().isEmpty()) {
            System.err.println("Judul, tujuan, dan targetId tidak boleh kosong.");
            return Optional.empty();
        }

        // Validasi apakah target dengan ID yang diberikan ada
        Optional<Target> target = targetDao.findTargetById(targetId);
        if (!target.isPresent()) {
            System.err.println("Target dengan ID " + targetId + " tidak ditemukan.");
            return Optional.empty();
        }

        Mission mission = new Mission();
        mission.setJudul(judul);
        mission.setTujuan(tujuan);
        mission.setDeskripsi(deskripsi != null ? deskripsi : "");
        mission.setTargetId(targetId);

        boolean saved = missionDao.saveMission(mission);
        if (saved) {
            return Optional.of(mission);
        } else {
            System.err.println("Gagal menyimpan draft misi baru ke database/file.");
            return Optional.empty();
        }
    }

    //Method getAllMissions
    public List<Mission> getAllMissions() {
        return missionDao.getAllMissions(); // Mendapatkan seluruh misi dari MissionDao
    }

    //Method getMissionById
    public Optional<Mission> getMissionById(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return Optional.empty(); // ID misi tidak boleh kosong
        }
        return missionDao.findMissionById(missionId);
    }

    //Method updateMissionStatus
    public boolean updateMissionStatus(String missionId, MissionStatus newStatus) {
        if (missionId == null || missionId.trim().isEmpty() || newStatus == null) {
            return false; // Pastikan missionId dan newStatus tidak null atau kosong
        }

        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);

        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }

        Mission mission = missionOptional.get();
        mission.setStatus(newStatus); // Set status misi dengan yang baru
        mission.setUpdatedAt(LocalDateTime.now()); // Update waktu terakhir diperbarui

        missionDao.saveMission(mission); // Simpan perubahan

        return true;
    }
}

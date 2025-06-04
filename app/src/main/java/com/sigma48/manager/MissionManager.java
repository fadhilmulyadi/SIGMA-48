package com.sigma48.manager;

import com.sigma48.dao.MissionDao;
import com.sigma48.dao.TargetDao;
import com.sigma48.dao.UserDao;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.model.Role;
import com.sigma48.model.CoverIdentity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MissionManager {

    public final MissionDao missionDao;
    private final TargetDao targetDao;
    private final UserDao userDao;

    //Konstruktor
    public MissionManager(MissionDao missionDao, TargetDao targetDao, UserDao userDao) {
        this.missionDao = missionDao;
        this.targetDao = targetDao;
        this.userDao = userDao;
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

    //Method updateOperationalPlan
    public boolean updateOperationalPlan(String missionId, String strategi, String protokol, String jenisOperasi, String lokasi, String analisisRisiko) {
        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan untuk update rencana.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        // Hanya update jika nilai baru diberikan
        if (strategi != null) mission.setStrategi(strategi);
        if (protokol != null) mission.setProtokol(protokol);
        if (jenisOperasi != null) mission.setJenisOperasi(jenisOperasi);
        if (lokasi != null) mission.setLokasi(lokasi);
        if (analisisRisiko != null) mission.setAnalisisRisiko(analisisRisiko);
        
        return missionDao.saveMission(mission);
    }
    
    //Method setMissionCommander
    public boolean setMissionCommander(String missionId, String komandanId) {
        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }
        
        Optional<User> userOptional = userDao.findUserById(komandanId);
        if (!userOptional.isPresent() || userOptional.get().getRole() != Role.KOMANDAN_OPERASI) {
            System.err.println("User dengan ID " + komandanId + " tidak ditemukan atau bukan Komandan Operasi.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        mission.setKomandanId(komandanId);
        return missionDao.saveMission(mission);
    }
    
    //Method assignAgentToMission
    public boolean assignAgentToMission(String missionId, String agentId) {
        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }
        
        Optional<User> userOptional = userDao.findUserById(agentId);
        // Validasi apakah user adalah Agen Lapangan
        if (!userOptional.isPresent() || userOptional.get().getRole() != Role.AGEN_LAPANGAN) {
            System.err.println("User dengan ID " + agentId + " tidak ditemukan atau bukan Agen Lapangan.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        mission.addAgent(agentId); // Menggunakan helper method di Mission.java
        return missionDao.saveMission(mission);
    }
    
    //Method removeAgentFromMission
    public boolean removeAgentFromMission(String missionId, String agentId) {
        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }

        Mission mission = missionOptional.get();
        mission.removeAgent(agentId);
        return missionDao.saveMission(mission);
    }

    public boolean assignCoverIdentityToAgent(String missionId, String agentId, CoverIdentity coverIdentity) {
        Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }

        Mission mission = missionOptional.get();
        // Pastikan agen tersebut memang ditugaskan ke misi ini
        if (!mission.getAssignedAgents().contains(agentId)) {
            System.err.println("Agen dengan ID " + agentId + " tidak ditugaskan ke misi " + missionId);
            return false;
        }

        mission.addCoverIdentity(agentId, coverIdentity); // Menggunakan helper method di Mission.java
        return missionDao.saveMission(mission);
    }

    public boolean updateMissionDokBriefingPath(String missionId, String dokBriefingPath) {
    Optional<Mission> missionOptional = missionDao.findMissionById(missionId);
    if (missionOptional.isPresent()) {
        Mission mission = missionOptional.get();
        mission.setDokBriefingPath(dokBriefingPath);
        mission.updateUpdatedAt();
        return missionDao.saveMission(mission);
    }
    System.err.println("Gagal memperbarui path dok briefing: Misi dengan ID '" + missionId + "' tidak ditemukan.");
    return false;
}
}

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //Method getAll
    public List<Mission> getAll() {
        return missionDao.getAll(); // Mendapatkan seluruh misi dari MissionDao
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
        return missionDao.findMissionById(missionId).map(mission -> {
            mission.setStatus(newStatus);
            return missionDao.saveMission(mission);
        }).orElse(false);
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

    public List<Mission> getMissionsForAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Mission> allMissions = missionDao.getAll(); // Mengambil semua misi
        return allMissions.stream()
                .filter(mission -> mission.getAssignedAgents() != null && 
                                mission.getAssignedAgents().contains(agentId))
                .filter(mission -> mission.getStatus() == MissionStatus.READY_FOR_BRIEFING || 
                                mission.getStatus() == MissionStatus.ACTIVE) // Hanya tampilkan yang relevan untuk dikerjakan
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))) // Urutkan dari yang terbaru diupdate
                .collect(Collectors.toList());
    }

    public List<Mission> getActiveMissions() {
        List<Mission> allMissions = missionDao.getAll(); // Mengambil semua misi
        return allMissions.stream()
                .filter(mission -> mission.getStatus() == MissionStatus.ACTIVE)
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))) // Urutkan dari yang terbaru diupdate
                .collect(Collectors.toList());
    }

    public boolean concludeMission(String missionId, MissionStatus finalStatus, String conclusionNotes, String direkturId) {
        if (finalStatus != MissionStatus.COMPLETED && finalStatus != MissionStatus.FAILED) return false;
        Optional<Mission> missionOpt = missionDao.findMissionById(missionId);
        if (!missionOpt.isPresent()) return false;
        Optional<User> direkturOpt = userDao.findUserById(direkturId);
        if (!direkturOpt.isPresent() || direkturOpt.get().getRole() != Role.DIREKTUR_INTELIJEN) return false;
        Mission mission = missionOpt.get();
        mission.setStatus(finalStatus);
        mission.setConclusionNotes(conclusionNotes);
        return missionDao.saveMission(mission);
    }

    public List<Mission> getMissionsForCommanderToPlan(String commanderId) {
        if (commanderId == null || commanderId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Mission> allMissions = missionDao.getAll();
        return allMissions.stream()
                .filter(mission -> commanderId.equals(mission.getKomandanId())) // Misi milik Komandan ini
                .filter(mission -> mission.getStatus() == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN ||
                                   mission.getStatus() == MissionStatus.PLANNED) // Status yang relevan untuk perencanaan
                .sorted(Comparator.comparing(Mission::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))) // Urutkan dari yang paling lama dibuat
                .collect(Collectors.toList());
    }

    public List<Mission> getMissionsByStatus(MissionStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        List<Mission> allMissions = missionDao.getAll();
        return allMissions.stream()
                .filter(mission -> mission.getStatus() == status)
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))) // Urutkan dari yang terbaru diupdate
                .collect(Collectors.toList());
    }

    public Optional<Mission> submitNewDraft(Mission draftMission) {
        if (draftMission == null) return Optional.empty();
        draftMission.setStatus(MissionStatus.DRAFT_ANALIS);
        boolean saved = missionDao.saveMission(draftMission);
        return saved ? Optional.of(draftMission) : Optional.empty();
    }

    public boolean approveDraftAndAssignCommander(String missionId, String komandanId, String direkturId) {
        Optional<Mission> missionOpt = missionDao.findMissionById(missionId);
        if (!missionOpt.isPresent() || missionOpt.get().getStatus() != MissionStatus.DRAFT_ANALIS) {
            System.err.println("MissionManager: Misi tidak ditemukan atau statusnya bukan DRAFT ANALIS.");
            return false;
        }
        Optional<User> komandanUserOpt = userDao.findUserById(komandanId);
        if (!komandanUserOpt.isPresent() || komandanUserOpt.get().getRole() != Role.KOMANDAN_OPERASI) {
            System.err.println("MissionManager: User yang ditunjuk bukan Komandan Operasi yang valid.");
            return false;
        }
        Mission mission = missionOpt.get();
        mission.setKomandanId(komandanId);
        mission.setStatus(MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN);
        return missionDao.saveMission(mission);
    }

    public boolean updateFullMissionDetails(Mission missionWithUpdates) {
        if (missionWithUpdates == null || missionWithUpdates.getId() == null) {
            return false;
        }
        return missionDao.saveMission(missionWithUpdates);
    }

    public boolean finalizePlanningAndSetReadyForBriefing(String missionId, String komandanIdVerifikasi) {
        Optional<Mission> missionOpt = missionDao.findMissionById(missionId);
        if (!missionOpt.isPresent()) return false;
        Mission mission = missionOpt.get();
        if (mission.getKomandanId() == null || !mission.getKomandanId().equals(komandanIdVerifikasi)) return false;
        if (mission.getStrategi() == null || mission.getStrategi().trim().isEmpty() ||
            mission.getProtokol() == null || mission.getProtokol().trim().isEmpty() ||
            mission.getAssignedAgents() == null || mission.getAssignedAgents().isEmpty()) return false;
        if (mission.getStatus() != MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN && mission.getStatus() != MissionStatus.PLANNED) return false;
        mission.setStatus(MissionStatus.READY_FOR_BRIEFING);
        return missionDao.saveMission(mission);
    }

    public List<Mission> getMissionsForCommander(String commanderId) {
        if (commanderId == null || commanderId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return missionDao.getAll().stream()
                .filter(mission -> commanderId.equals(mission.getKomandanId()))
                .collect(Collectors.toList());
    }

    // public void updateMission(Mission updatedMission) {
    //     // 1. Ambil semua misi yang ada dari file.
    //     List<Mission> allMissions = missionDao.getAll();

    //     // 2. Cari misi yang akan di-update berdasarkan ID, lalu ganti dengan data baru.
    //     for (int i = 0; i < allMissions.size(); i++) {
    //         if (allMissions.get(i).getId().equals(updatedMission.getId())) {
    //             // Set waktu update sebelum menyimpan
    //             updatedMission.setUpdatedAt(LocalDateTime.now());
    //             allMissions.set(i, updatedMission); // Ganti objek lama dengan yang baru
    //             break;
    //         }
    //     }

    //     missionDao.saveAll(allMissions);
    //     System.out.println("MissionManager: Misi dengan ID " + updatedMission.getId() + " berhasil diperbarui.");
    // }
}
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
        Optional<Target> target = targetDao.findById(targetId); // Disesuaikan
        if (!target.isPresent()) {
            System.err.println("Target dengan ID " + targetId + " tidak ditemukan.");
            return Optional.empty();
        }

        Mission mission = new Mission();
        mission.setJudul(judul);
        mission.setTujuan(tujuan);
        mission.setDeskripsi(deskripsi != null ? deskripsi : "");
        mission.setTargetId(targetId);

        boolean saved = missionDao.save(mission); // Disesuaikan
        if (saved) {
            return Optional.of(mission);
        } else {
            System.err.println("Gagal menyimpan draft misi baru ke database/file.");
            return Optional.empty();
        }
    }

    //Method getAll
    public List<Mission> getAll() {
        return missionDao.getAll();
    }

    //Method getMissionById
    public Optional<Mission> getMissionById(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return Optional.empty();
        }
        return missionDao.findById(missionId); // Disesuaikan
    }

    //Method updateMissionStatus
    public boolean updateMissionStatus(String missionId, MissionStatus newStatus) {
        return missionDao.findById(missionId).map(mission -> { // Disesuaikan
            mission.setStatus(newStatus);
            return missionDao.save(mission); // Disesuaikan
        }).orElse(false);
    }

    //Method updateOperationalPlan
    public boolean updateOperationalPlan(String missionId, String strategi, String protokol, String jenisOperasi, String lokasi, String analisisRisiko) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan untuk update rencana.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        if (strategi != null) mission.setStrategi(strategi);
        if (protokol != null) mission.setProtokol(protokol);
        if (jenisOperasi != null) mission.setJenisOperasi(jenisOperasi);
        if (lokasi != null) mission.setLokasi(lokasi);
        if (analisisRisiko != null) mission.setAnalisisRisiko(analisisRisiko);
        
        return missionDao.save(mission); // Disesuaikan
    }
    
    //Method setMissionCommander
    public boolean setMissionCommander(String missionId, String komandanId) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }
        
        Optional<User> userOptional = userDao.findById(komandanId); // Disesuaikan
        if (!userOptional.isPresent() || userOptional.get().getRole() != Role.KOMANDAN_OPERASI) {
            System.err.println("User dengan ID " + komandanId + " tidak ditemukan atau bukan Komandan Operasi.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        mission.setKomandanId(komandanId);
        return missionDao.save(mission); // Disesuaikan
    }
    
    //Method assignAgentToMission
    public boolean assignAgentToMission(String missionId, String agentId) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }
        
        Optional<User> userOptional = userDao.findById(agentId); // Disesuaikan
        if (!userOptional.isPresent() || userOptional.get().getRole() != Role.AGEN_LAPANGAN) {
            System.err.println("User dengan ID " + agentId + " tidak ditemukan atau bukan Agen Lapangan.");
            return false;
        }
        
        Mission mission = missionOptional.get();
        mission.addAgent(agentId);
        return missionDao.save(mission); // Disesuaikan
    }
    
    //Method removeAgentFromMission
    public boolean removeAgentFromMission(String missionId, String agentId) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }

        Mission mission = missionOptional.get();
        mission.removeAgent(agentId);
        return missionDao.save(mission); // Disesuaikan
    }

    public boolean assignCoverIdentityToAgent(String missionId, String agentId, CoverIdentity coverIdentity) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (!missionOptional.isPresent()) {
            System.err.println("Misi dengan ID " + missionId + " tidak ditemukan.");
            return false;
        }

        Mission mission = missionOptional.get();
        if (!mission.getAssignedAgents().contains(agentId)) {
            System.err.println("Agen dengan ID " + agentId + " tidak ditugaskan ke misi " + missionId);
            return false;
        }

        mission.addCoverIdentity(agentId, coverIdentity);
        return missionDao.save(mission); // Disesuaikan
    }

    public boolean updateMissionDokBriefingPath(String missionId, String dokBriefingPath) {
        Optional<Mission> missionOptional = missionDao.findById(missionId); // Disesuaikan
        if (missionOptional.isPresent()) {
            Mission mission = missionOptional.get();
            mission.setDokBriefingPath(dokBriefingPath);
            mission.updateUpdatedAt();
            return missionDao.save(mission); // Disesuaikan
        }
        System.err.println("Gagal memperbarui path dok briefing: Misi dengan ID '" + missionId + "' tidak ditemukan.");
        return false;
    }

    public List<Mission> getMissionsForAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return missionDao.getAll().stream()
                .filter(mission -> mission.getAssignedAgents() != null && 
                                mission.getAssignedAgents().contains(agentId))
                .filter(mission -> mission.getStatus() == MissionStatus.READY_FOR_BRIEFING || 
                                mission.getStatus() == MissionStatus.ACTIVE)
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Mission> getActiveMissions() {
        return missionDao.getAll().stream()
                .filter(mission -> mission.getStatus() == MissionStatus.ACTIVE)
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public boolean concludeMission(String missionId, MissionStatus finalStatus, String conclusionNotes, String direkturId) {
        if (finalStatus != MissionStatus.COMPLETED && finalStatus != MissionStatus.FAILED) return false;
        Optional<Mission> missionOpt = missionDao.findById(missionId); // Disesuaikan
        if (!missionOpt.isPresent()) return false;
        Optional<User> direkturOpt = userDao.findById(direkturId); // Disesuaikan
        if (!direkturOpt.isPresent() || direkturOpt.get().getRole() != Role.DIREKTUR_INTELIJEN) return false;
        
        Mission mission = missionOpt.get();
        mission.setStatus(finalStatus);
        mission.setConclusionNotes(conclusionNotes);
        return missionDao.save(mission); // Disesuaikan
    }

    public List<Mission> getMissionsForCommanderToPlan(String commanderId) {
        if (commanderId == null || commanderId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return missionDao.getAll().stream()
                .filter(mission -> commanderId.equals(mission.getKomandanId()))
                .filter(mission -> mission.getStatus() == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN ||
                                   mission.getStatus() == MissionStatus.PLANNED)
                .sorted(Comparator.comparing(Mission::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Mission> getMissionsByStatus(MissionStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        return missionDao.getAll().stream()
                .filter(mission -> mission.getStatus() == status)
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Optional<Mission> submitNewDraft(Mission draftMission) {
        if (draftMission == null) return Optional.empty();
        draftMission.setStatus(MissionStatus.DRAFT_ANALIS);
        boolean saved = missionDao.save(draftMission); // Disesuaikan
        return saved ? Optional.of(draftMission) : Optional.empty();
    }

    public boolean approveDraftAndAssignCommander(String missionId, String komandanId, String direkturId) {
        Optional<Mission> missionOpt = missionDao.findById(missionId); // Disesuaikan
        if (!missionOpt.isPresent() || missionOpt.get().getStatus() != MissionStatus.DRAFT_ANALIS) {
            System.err.println("MissionManager: Misi tidak ditemukan atau statusnya bukan DRAFT ANALIS.");
            return false;
        }
        Optional<User> komandanUserOpt = userDao.findById(komandanId); // Disesuaikan
        if (!komandanUserOpt.isPresent() || komandanUserOpt.get().getRole() != Role.KOMANDAN_OPERASI) {
            System.err.println("MissionManager: User yang ditunjuk bukan Komandan Operasi yang valid.");
            return false;
        }
        Mission mission = missionOpt.get();
        mission.setKomandanId(komandanId);
        mission.setStatus(MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN);
        return missionDao.save(mission); // Disesuaikan
    }

    public boolean updateFullMissionDetails(Mission missionWithUpdates) {
        if (missionWithUpdates == null || missionWithUpdates.getId() == null) {
            return false;
        }
        return missionDao.save(missionWithUpdates); // Disesuaikan
    }

    public boolean finalizePlanningAndSetReadyForBriefing(String missionId, String komandanIdVerifikasi) {
        Optional<Mission> missionOpt = missionDao.findById(missionId); // Disesuaikan
        if (!missionOpt.isPresent()) return false;
        Mission mission = missionOpt.get();
        if (mission.getKomandanId() == null || !mission.getKomandanId().equals(komandanIdVerifikasi)) return false;
        if (mission.getStrategi() == null || mission.getStrategi().trim().isEmpty() ||
            mission.getProtokol() == null || mission.getProtokol().trim().isEmpty() ||
            mission.getAssignedAgents() == null || mission.getAssignedAgents().isEmpty()) return false;
        if (mission.getStatus() != MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN && mission.getStatus() != MissionStatus.PLANNED) return false;
        mission.setStatus(MissionStatus.READY_FOR_BRIEFING);
        return missionDao.save(mission); // Disesuaikan
    }

    public List<Mission> getMissionsForCommander(String commanderId) {
        if (commanderId == null || commanderId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return missionDao.getAll().stream()
                .filter(mission -> commanderId.equals(mission.getKomandanId()))
                .collect(Collectors.toList());
    }
}
package com.sigma48.manager;

import com.sigma48.dao.EvaluationDao;
import com.sigma48.model.Evaluation;
import com.sigma48.model.Mission;
import com.sigma48.model.User;
import com.sigma48.model.OperationEffectiveness;
import com.sigma48.model.Role;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EvaluationManager {
    private EvaluationDao evaluationDao;
    private MissionManager missionManager;
    private UserManager userManager;

    public EvaluationManager(EvaluationDao evaluationDao, MissionManager missionManager, UserManager userManager) {
        this.evaluationDao = evaluationDao;
        this.missionManager = missionManager;
        this.userManager = userManager;
    }

    public Optional<Evaluation> submitEvaluation(String missionId, String agentId, String evaluatorId,
                                               OperationEffectiveness efektivitas,
                                               String catatanMentalAgen, String kondisiFisikAgen,
                                               String catatanUmumEvaluator) {
        // Validasi input dasar
        if (missionId == null || missionId.trim().isEmpty() ||
            evaluatorId == null || evaluatorId.trim().isEmpty() ||
            efektivitas == null) {
            System.err.println("EvaluationManager: Mission ID, Evaluator ID, dan Efektivitas wajib diisi.");
            return Optional.empty();
        }

        // Validasi keberadaan Misi
        Optional<Mission> missionOpt = missionManager.getMissionById(missionId);
        if (!missionOpt.isPresent()) {
            System.err.println("EvaluationManager: Misi dengan ID " + missionId + " tidak ditemukan.");
            return Optional.empty();
        }

        Optional<User> evaluatorOpt = userManager.findUserById(evaluatorId);
        if (!evaluatorOpt.isPresent()) {
            System.err.println("EvaluationManager: Evaluator dengan ID " + evaluatorId + " tidak ditemukan.");
            return Optional.empty();
        }
        User evaluator = evaluatorOpt.get();
        if (evaluator.getRole() != Role.DIREKTUR_INTELIJEN && evaluator.getRole() != Role.KOMANDAN_OPERASI) {
            System.err.println("EvaluationManager: Pengguna " + evaluator.getUsername() + " tidak memiliki wewenang untuk melakukan evaluasi.");
            return Optional.empty();
        }

        if (agentId != null && !agentId.trim().isEmpty()) {
            Optional<User> agentOpt = userManager.findUserById(agentId);
            if (!agentOpt.isPresent() || agentOpt.get().getRole() != Role.AGEN_LAPANGAN) {
                System.err.println("EvaluationManager: Agen dengan ID " + agentId + " tidak ditemukan atau bukan Agen Lapangan.");
                return Optional.empty();
            }
        }


        Evaluation newEvaluation = new Evaluation();
        newEvaluation.setMissionId(missionId);
        newEvaluation.setAgentId(agentId);
        newEvaluation.setEvaluatorId(evaluatorId);
        newEvaluation.setEfektivitasOperasi(efektivitas);
        newEvaluation.setCatatanMentalAgen(catatanMentalAgen);
        newEvaluation.setKondisiFisikAgen(kondisiFisikAgen);
        newEvaluation.setCatatanUmumEvaluator(catatanUmumEvaluator);

        boolean saved = evaluationDao.save(newEvaluation);
        if (saved) {
            return Optional.of(newEvaluation);
        } else {
            System.err.println("EvaluationManager: Gagal menyimpan evaluasi ke database/file.");
            return Optional.empty();
        }
    }

    public List<Evaluation> getEvaluationsForMission(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Evaluation> evaluations = evaluationDao.findEvaluationsByMissionId(missionId);
        evaluations.sort(Comparator.comparing(Evaluation::getTimestampEvaluasi, Comparator.nullsLast(Comparator.reverseOrder())));
        return evaluations;
    }

    public List<Evaluation> getEvaluationsForAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Evaluation> evaluations = evaluationDao.findEvaluationsByAgentId(agentId);
        evaluations.sort(Comparator.comparing(Evaluation::getTimestampEvaluasi, Comparator.nullsLast(Comparator.reverseOrder())));
        return evaluations;
    }
    
    public List<Evaluation> getEvaluationsByEvaluator(String evaluatorId) {
        if (evaluatorId == null || evaluatorId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Evaluation> evaluations = evaluationDao.findEvaluationsByEvaluatorId(evaluatorId);
        evaluations.sort(Comparator.comparing(Evaluation::getTimestampEvaluasi, Comparator.nullsLast(Comparator.reverseOrder())));
        return evaluations;
    }

    public Optional<Evaluation> getEvaluationById(String evaluationId) {
        if (evaluationId == null || evaluationId.trim().isEmpty()) {
            return Optional.empty();
        }
        return evaluationDao.findById(evaluationId);
    }
}
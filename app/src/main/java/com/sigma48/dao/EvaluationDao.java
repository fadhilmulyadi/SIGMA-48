package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.dao.base.GenericDao;
import com.sigma48.model.Evaluation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationDao extends GenericDao<Evaluation> {

    public EvaluationDao() {
        super("data/evaluations.json", new TypeReference<List<Evaluation>>() {});
    }

    public Optional<Evaluation> findEvaluationById(String evaluationId) {
        return find(eval -> eval.getId().equals(evaluationId));
    }

    public List<Evaluation> findEvaluationsByMissionId(String missionId) {
        return getAll().stream()
                .filter(eval -> missionId.equals(eval.getMissionId()))
                .collect(Collectors.toList());
    }
    
    public List<Evaluation> findEvaluationsByAgentId(String agentId) {
         return getAll().stream()
                .filter(eval -> agentId.equals(eval.getAgentId()))
                .collect(Collectors.toList());
    }

    // PERBAIKAN: Menambahkan kembali method ini
    public List<Evaluation> findEvaluationsByEvaluatorId(String evaluatorId) {
        return getAll().stream()
                .filter(eval -> evaluatorId.equals(eval.getEvaluatorId()))
                .collect(Collectors.toList());
    }

    public boolean saveEvaluation(Evaluation evaluationToSave) {
        if (evaluationToSave == null) return false;
        save(evaluationToSave, e -> e.getId().equals(evaluationToSave.getId()));
        return true;
    }
}
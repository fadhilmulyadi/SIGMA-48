package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sigma48.model.Evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationDao {
    private final String EVALUATIONS_FILE_PATH = "data/evaluations.json";
    private ObjectMapper objectMapper;

    public EvaluationDao() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        initializeFile();
    }

    private void initializeFile() {
        try {
            File evaluationsFile = new File(EVALUATIONS_FILE_PATH);
            if (!evaluationsFile.exists()) {
                if (evaluationsFile.getParentFile() != null && !evaluationsFile.getParentFile().exists()) {
                    evaluationsFile.getParentFile().mkdirs();
                }
                // Tulis array JSON kosong jika file baru dibuat
                objectMapper.writeValue(evaluationsFile, new ArrayList<Evaluation>());
                System.out.println("File " + EVALUATIONS_FILE_PATH + " berhasil diinisialisasi.");
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi file " + EVALUATIONS_FILE_PATH + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Evaluation> readAllEvaluationsFromFile() throws IOException {
        File evaluationsFile = new File(EVALUATIONS_FILE_PATH);
        if (!evaluationsFile.exists() || evaluationsFile.length() == 0) {
            return new ArrayList<>(); // Kembalikan list kosong jika file tidak ada atau kosong
        }
        return objectMapper.readValue(evaluationsFile, new TypeReference<List<Evaluation>>() {});
    }

    private void writeAllEvaluationsToFile(List<Evaluation> evaluations) throws IOException {
        objectMapper.writeValue(new File(EVALUATIONS_FILE_PATH), evaluations);
    }

    public List<Evaluation> getAllEvaluations() {
        try {
            return readAllEvaluationsFromFile();
        } catch (IOException e) {
            System.err.println("Error membaca data evaluasi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<Evaluation> findEvaluationById(String evaluationId) {
        if (evaluationId == null || evaluationId.trim().isEmpty()) {
            return Optional.empty();
        }
        return getAllEvaluations().stream()
                .filter(evaluation -> evaluation.getId().equals(evaluationId))
                .findFirst();
    }

    public List<Evaluation> findEvaluationsByMissionId(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return getAllEvaluations().stream()
                .filter(evaluation -> evaluation.getMissionId().equals(missionId))
                .collect(Collectors.toList());
    }

    public List<Evaluation> findEvaluationsByAgentId(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return getAllEvaluations().stream()
                .filter(evaluation -> agentId.equals(evaluation.getAgentId()))
                .collect(Collectors.toList());
    }
    
    public List<Evaluation> findEvaluationsByEvaluatorId(String evaluatorId) {
        if (evaluatorId == null || evaluatorId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return getAllEvaluations().stream()
                .filter(evaluation -> evaluatorId.equals(evaluation.getEvaluatorId()))
                .collect(Collectors.toList());
    }

    public boolean saveEvaluation(Evaluation evaluationToSave) {
        if (evaluationToSave == null) return false;
        try {
            List<Evaluation> evaluations = getAllEvaluations(); 
            evaluations.removeIf(e -> e.getId().equals(evaluationToSave.getId()));
            evaluations.add(evaluationToSave); 
            writeAllEvaluationsToFile(evaluations);
            return true;
        } catch (IOException e) {
            System.err.println("Error menyimpan data evaluasi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvaluation(String evaluationId) {
        if (evaluationId == null || evaluationId.trim().isEmpty()) return false;
        try {
            List<Evaluation> evaluations = getAllEvaluations();
            boolean removed = evaluations.removeIf(evaluation -> evaluation.getId().equals(evaluationId));
            if (removed) {
                writeAllEvaluationsToFile(evaluations);
                return true;
            }
            return false; // Evaluasi tidak ditemukan untuk dihapus
        } catch (IOException e) {
            System.err.println("Error menghapus data evaluasi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
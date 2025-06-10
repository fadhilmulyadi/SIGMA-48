package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sigma48.model.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TargetDao {
    private final String TARGETS_FILE_PATH = "data/targets.json";
    private ObjectMapper objectMapper;

    public TargetDao() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        initializeFile();
    }

    private void initializeFile() {
        try {
            File targetsFile = new File(TARGETS_FILE_PATH);
            if (!targetsFile.exists()) {
                if (targetsFile.getParentFile() != null && !targetsFile.getParentFile().exists()) {
                    targetsFile.getParentFile().mkdirs();
                }
                objectMapper.writeValue(targetsFile, new ArrayList<Target>());
                System.out.println("File " + TARGETS_FILE_PATH + " berhasil diinisialisasi.");
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi file " + TARGETS_FILE_PATH + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Target> readAllTargetsFromFile() throws IOException {
        File targetsFile = new File(TARGETS_FILE_PATH);
        if (!targetsFile.exists() || targetsFile.length() == 0) {
            return new ArrayList<>(); // Kembalikan list kosong jika file tidak ada atau kosong
        }
        return objectMapper.readValue(targetsFile, new TypeReference<List<Target>>() {});
    }

    private void writeAllTargetsToFile(List<Target> targets) throws IOException {
        objectMapper.writeValue(new File(TARGETS_FILE_PATH), targets);
    }

    public List<Target> getAllTargets() {
        try {
            return readAllTargetsFromFile();
        } catch (IOException e) {
            System.err.println("Error membaca data target: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<Target> findTargetById(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return Optional.empty();
        }
        return getAllTargets().stream()
                .filter(target -> target.getId().equals(targetId))
                .findFirst();
    }

    public boolean saveTarget(Target targetToSave) {
        if (targetToSave == null) return false;
        try {
            List<Target> targets = getAllTargets();
            targets.removeIf(t -> t.getId().equals(targetToSave.getId()));
            targets.add(targetToSave);
            writeAllTargetsToFile(targets);
            return true;
        } catch (IOException e) {
            System.err.println("Error menyimpan data target: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTarget(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) return false;
        try {
            List<Target> targets = getAllTargets();
            boolean removed = targets.removeIf(target -> target.getId().equals(targetId));
            if (removed) {
                writeAllTargetsToFile(targets);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error menghapus data target: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
package com.sigma48.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.model.Mission;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MissionDao {

    //Deklarasi Kelas dan Atribut
    private static final String MISSIONS_FILE_PATH = "data/missions.json";
    private final ObjectMapper objectMapper;

    //Konstruktor
    public MissionDao() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Mengaktifkan pretty-print untuk JSON
    this.objectMapper.registerModule(new JavaTimeModule()); // Mendaftarkan JavaTimeModule agar LocalDateTime bisa diproses
    try {
        initializeFile(); // Memanggil method untuk memeriksa dan menginisialisasi file jika perlu
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    // Method Inisialisasi File
    private void initializeFile() throws IOException {
        File file = new File(MISSIONS_FILE_PATH);
        if (!file.exists()) {
            file.createNewFile();
            saveAllMissionsToFile(new ArrayList<>());
        }
    }

    //Method Membaca dan Menulis File
    private List<Mission> readAllMissionsFromFile() throws IOException {
        File file = new File(MISSIONS_FILE_PATH);
        if (file.length() == 0) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<Mission>>(){});
    }

    private void saveAllMissionsToFile(List<Mission> missions) throws IOException {
        File file = new File(MISSIONS_FILE_PATH);
        objectMapper.writeValue(file, missions);
    }

    // Method CRUD Publik

    // Mengambil seluruh daftar misi
    public List<Mission> getAllMissions() {
        try {
            return readAllMissionsFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Mencari misi berdasarkan ID
    public Optional<Mission> findMissionById(String missionId) {
    if (missionId == null || missionId.trim().isEmpty()) {
        return Optional.empty();
    }
    try {
        List<Mission> missions = readAllMissionsFromFile();
        return missions.stream()
                .filter(mission -> mission.getId().equals(missionId))
                .findFirst();
    } catch (IOException e) {
        e.printStackTrace();
        return Optional.empty();
    }
}

    // Menyimpan misi baru atau memperbarui misi yang sudah ada
    public boolean saveMission(Mission missionToSave) {
        if (missionToSave == null) {
            return false;
        }
        try {
            List<Mission> missions = readAllMissionsFromFile();
            missions.removeIf(m -> m.getId().equals(missionToSave.getId()));
            missions.add(missionToSave);
            saveAll(missions);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    //Menghapus misi berdasarkan ID
    public boolean deleteMission(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return false;
        }
        try {
            List<Mission> missions = readAllMissionsFromFile();
            Optional<Mission> missionToRemove = findMissionById(missionId);

            if (missionToRemove.isPresent()) {
                missions.remove(missionToRemove.get());
                saveAll(missions);
                return true;
            } else {
                return false; // Misi dengan ID yang diberikan tidak ditemukan
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveAll(List<Mission> missions) {
        try {
            saveAllMissionsToFile(missions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sigma48.model.Report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportDao {
    private final String REPORTS_FILE_PATH = "data/reports.json";
    private ObjectMapper objectMapper;

    public ReportDao() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Untuk pretty print JSON
        objectMapper.registerModule(new JavaTimeModule());       // Untuk menangani LocalDateTime di Report.waktuLapor
        initializeFile(); // Panggil metode untuk inisialisasi file
    }

    private void initializeFile() {
        try {
            File reportsFile = new File(REPORTS_FILE_PATH);
            if (!reportsFile.exists()) {
                if (reportsFile.getParentFile() != null && !reportsFile.getParentFile().exists()) {
                    reportsFile.getParentFile().mkdirs(); // Buat direktori 'data' jika belum ada
                }
                // Tulis array JSON kosong jika file baru dibuat
                objectMapper.writeValue(reportsFile, new ArrayList<Report>());
                System.out.println("File " + REPORTS_FILE_PATH + " berhasil diinisialisasi.");
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi file " + REPORTS_FILE_PATH + ": " + e.getMessage());
            e.printStackTrace(); // Penting untuk debugging
        }
    }

    private List<Report> readAllReportsFromFile() throws IOException {
        File reportsFile = new File(REPORTS_FILE_PATH);
        if (!reportsFile.exists() || reportsFile.length() == 0) {
            return new ArrayList<>(); // Kembalikan list kosong jika file tidak ada atau kosong
        }
        return objectMapper.readValue(reportsFile, new TypeReference<List<Report>>() {});
    }

    private void writeAllReportsToFile(List<Report> reports) throws IOException {
        objectMapper.writeValue(new File(REPORTS_FILE_PATH), reports);
    }

    public List<Report> getAllReports() {
        try {
            return readAllReportsFromFile();
        } catch (IOException e) {
            System.err.println("Error membaca data laporan: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<Report> findReportById(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return Optional.empty();
        }
        return getAllReports().stream()
                .filter(report -> report.getReportId().equals(reportId))
                .findFirst();
    }

    public List<Report> findReportsByMissionId(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return getAllReports().stream()
                .filter(report -> report.getMissionId().equals(missionId))
                .collect(Collectors.toList());
    }

    public List<Report> findReportsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return getAllReports().stream()
                .filter(report -> report.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public boolean saveReport(Report reportToSave) {
        if (reportToSave == null) return false;
        try {
            List<Report> reports = getAllReports(); // Ambil semua laporan yang ada
            // Hapus laporan lama jika ada (berdasarkan ID) untuk operasi update
            reports.removeIf(r -> r.getReportId().equals(reportToSave.getReportId()));
            reports.add(reportToSave); // Tambahkan laporan baru atau yang sudah diperbarui
            writeAllReportsToFile(reports);
            return true;
        } catch (IOException e) {
            System.err.println("Error menyimpan data laporan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReport(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) return false;
        try {
            List<Report> reports = getAllReports();
            boolean removed = reports.removeIf(report -> report.getReportId().equals(reportId));
            if (removed) {
                writeAllReportsToFile(reports);
                return true;
            }
            return false; // Laporan tidak ditemukan untuk dihapus
        } catch (IOException e) {
            System.err.println("Error menghapus data laporan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
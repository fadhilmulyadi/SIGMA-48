package com.sigma48.manager;

import com.sigma48.dao.ReportDao;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Report;
import com.sigma48.model.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportManager {
    private ReportDao reportDao;
    private MissionManager missionManager;

    public ReportManager(ReportDao reportDao, MissionManager missionManager) {
        this.reportDao = reportDao;
        this.missionManager = missionManager;
    }

    public Optional<Report> submitReport(String missionId, String userId, Role userRole, String isi, List<String> lampiranPaths) {
        // Validasi input dasar
        if (missionId == null || missionId.trim().isEmpty() ||
            userId == null || userId.trim().isEmpty() ||
            userRole == null ||
            isi == null || isi.trim().isEmpty()) {
            System.err.println("ReportManager: Data laporan tidak lengkap (missionId, userId, userRole, isi wajib diisi).");
            return Optional.empty();
        }

        // Buat objek Report baru
        Report newReport = new Report(missionId, userId, userRole, isi);
        if (lampiranPaths != null && !lampiranPaths.isEmpty()) {
            newReport.setLampiran(lampiranPaths);
        }

        // Simpan laporan
        boolean reportSaved = reportDao.saveReport(newReport);
        if (!reportSaved) {
            System.err.println("ReportManager: Gagal menyimpan laporan ke database/file.");
            return Optional.empty();
        }

        // Cek dan update status misi jika ini laporan pertama dan misi belum aktif
        Optional<Mission> missionOpt = missionManager.getMissionById(missionId);
        if (missionOpt.isPresent()) {
            Mission mission = missionOpt.get();
            // Ubah status ke ACTIVE jika status saat ini adalah PLANNED atau READY_FOR_BRIEFING
            if (mission.getStatus() == MissionStatus.PLANNED || 
                mission.getStatus() == MissionStatus.READY_FOR_BRIEFING) {
                boolean statusUpdated = missionManager.updateMissionStatus(missionId, MissionStatus.ACTIVE);
                if (statusUpdated) {
                    System.out.println("ReportManager: Status misi " + missionId + " diubah menjadi ACTIVE.");
                } else {
                    System.err.println("ReportManager: Gagal mengubah status misi " + missionId + " menjadi ACTIVE setelah laporan pertama.");
                }
            }
        } else {
            System.err.println("ReportManager: Misi dengan ID " + missionId + " tidak ditemukan saat mencoba update status pasca-laporan.");
        }

        return Optional.of(newReport);
    }

    public List<Report> getReportsForMission(String missionId) {
        if (missionId == null || missionId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Report> reports = reportDao.findReportsByMissionId(missionId);
        // Urutkan berdasarkan waktuLapor, dari yang paling baru (descending)
        reports.sort(Comparator.comparing(Report::getWaktuLapor, Comparator.nullsLast(Comparator.reverseOrder())));
        return reports;
    }

    public List<Report> getReportsByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Report> reports = reportDao.findReportsByUserId(userId);
        reports.sort(Comparator.comparing(Report::getWaktuLapor, Comparator.nullsLast(Comparator.reverseOrder())));
        return reports;
    }

    public Optional<Report> getReportDetails(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return Optional.empty();
        }
        return reportDao.findReportById(reportId);
    }
}
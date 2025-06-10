package com.sigma48.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Report {

    private String reportId;
    private String missionId;
    private String userId;
    private Role userRoleAtReporting;
    private LocalDateTime waktuLapor;
    private String isi;
    private List<String> lampiran;
    private String lokasi;

    public Report() {
        this.reportId = UUID.randomUUID().toString();
        this.waktuLapor = LocalDateTime.now();
        this.lampiran = new ArrayList<>();
    }

    public Report(String missionId, String userId, Role userRoleAtReporting, String isi) {
        this();
        this.missionId = missionId;
        this.userId = userId;
        this.userRoleAtReporting = userRoleAtReporting;
        this.isi = isi;
    }

    public Report(String missionId, String userId, Role userRoleAtReporting, String isi, List<String> lampiran) {
        this(missionId, userId, userRoleAtReporting, isi);
        if (lampiran != null) {
            this.lampiran = new ArrayList<>(lampiran);
        }
    }

    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Role getUserRoleAtReporting() {
        return userRoleAtReporting;
    }

    public void setUserRoleAtReporting(Role userRoleAtReporting) {
        this.userRoleAtReporting = userRoleAtReporting;
    }

    public LocalDateTime getWaktuLapor() {
        return waktuLapor;
    }

    public void setWaktuLapor(LocalDateTime waktuLapor) {
        this.waktuLapor = waktuLapor;
    }

    public String getIsi() {
        return isi;
    }

    public void setIsi(String isi) {
        this.isi = isi;
    }

    public List<String> getLampiran() {
        return lampiran == null ? new ArrayList<>() : lampiran;
    }

    public void setLampiran(List<String> lampiran) {
        if (lampiran != null) {
            this.lampiran = new ArrayList<>(lampiran);
        } else {
            this.lampiran = new ArrayList<>();
        }
    }

    public void tambahLampiran(String pathFile) {
        if (pathFile != null && !pathFile.trim().isEmpty()) {
            if (this.lampiran == null) {
                this.lampiran = new ArrayList<>();
            }
            this.lampiran.add(pathFile);
        }
    }

    public void hapusLampiran(String pathFile) {
        if (pathFile != null && this.lampiran != null) {
            this.lampiran.remove(pathFile);
        }
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    @Override
    public String toString() {
        return "Report{" +
               "reportId='" + reportId + '\'' +
               ", missionId='" + missionId + '\'' +
               ", userId='" + userId + '\'' +
               ", waktuLapor=" + waktuLapor +
               ", isi='" + (isi != null && isi.length() > 50 ? isi.substring(0, 50) + "..." : isi) + '\'' +
               ", jumlahLampiran=" + (lampiran != null ? lampiran.size() : 0) +
               '}';
    }
}
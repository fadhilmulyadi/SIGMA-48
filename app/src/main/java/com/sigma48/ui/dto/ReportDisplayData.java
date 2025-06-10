package com.sigma48.ui.dto;

import com.sigma48.model.Mission;
import com.sigma48.model.Report;
import com.sigma48.model.User;

import java.time.format.DateTimeFormatter;

public class ReportDisplayData {
    private final String reportId;
    private final String missionId;
    private final String missionJudul;
    private final String submitterName;
    private final String contentSnippet;
    private final String timestamp;

    public ReportDisplayData(Report report, Mission mission, User submitter) {
        this.reportId = report.getReportId();
        this.missionId = report.getMissionId();
        this.missionJudul = mission != null ? mission.getJudul() : "Misi Tidak Diketahui";
        this.submitterName = submitter != null ? submitter.getUsername() : report.getUserId();
        
        String isi = report.getIsi();
        this.contentSnippet = isi != null && isi.length() > 60 ? isi.substring(0, 57) + "..." : isi;
        this.timestamp = report.getWaktuLapor() != null ? report.getWaktuLapor().format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) : "N/A";
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public String getMissionId() {
        return missionId;
    }

    public String getMissionJudul() {
        return missionJudul;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public String getContentSnippet() {
        return contentSnippet;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
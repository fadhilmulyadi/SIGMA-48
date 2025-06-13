package com.sigma48.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Mission {

    private String id = UUID.randomUUID().toString();

    @ToString.Include
    private String judul;

    private String tujuan;
    private String deskripsi;
    private String targetId;
    private String analisisRisiko;
    private String jenisOperasi;
    private String lokasi;
    private String strategi;
    private String protokol;
    private String komandanId;
    private String conclusionNotes;

    @ToString.Include
    private MissionStatus status = MissionStatus.PLANNED;

    private List<String> assignedAgents = new ArrayList<>();
    private Map<String, CoverIdentity> coverIdentities = new HashMap<>();

    private String dokBriefingPath;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Mission(String judul, String tujuan, String deskripsi, String targetId) {
        this(); // panggil konstruktor default
        this.judul = judul;
        this.tujuan = tujuan;
        this.deskripsi = deskripsi;
        this.targetId = targetId;
    }

    // Pemutakhiran waktu modifikasi
    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setJudul(String judul) {
        this.judul = judul;
        updateUpdatedAt();
    }

    public void setTujuan(String tujuan) {
        this.tujuan = tujuan;
        updateUpdatedAt();
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
        updateUpdatedAt();
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
        updateUpdatedAt();
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
        updateUpdatedAt();
    }

    public void setAssignedAgents(List<String> assignedAgents) {
        this.assignedAgents = assignedAgents;
        updateUpdatedAt();
    }

    public void setCoverIdentities(Map<String, CoverIdentity> coverIdentities) {
        this.coverIdentities = coverIdentities;
        updateUpdatedAt();
    }

    public void setDokBriefingPath(String dokBriefingPath) {
        this.dokBriefingPath = dokBriefingPath;
        updateUpdatedAt();
    }

    public void setAnalisisRisiko(String analisisRisiko) {
        this.analisisRisiko = analisisRisiko;
        updateUpdatedAt();
    }

    public void setJenisOperasi(String jenisOperasi) {
        this.jenisOperasi = jenisOperasi;
        updateUpdatedAt();
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
        updateUpdatedAt();
    }

    public void setStrategi(String strategi) {
        this.strategi = strategi;
        updateUpdatedAt();
    }

    public void setProtokol(String protokol) {
        this.protokol = protokol;
        updateUpdatedAt();
    }

    public void setKomandanId(String komandanId) {
        this.komandanId = komandanId;
        updateUpdatedAt();
    }

    public void setConclusionNotes(String conclusionNotes) {
        this.conclusionNotes = conclusionNotes;
        updateUpdatedAt();
    }

    // Helper methods
    public void addAgent(String agentId) {
        if (!assignedAgents.contains(agentId)) {
            assignedAgents.add(agentId);
            updateUpdatedAt();
        }
    }

    public void removeAgent(String agentId) {
        assignedAgents.remove(agentId);
        coverIdentities.remove(agentId);
        updateUpdatedAt();
    }

    public void addCoverIdentity(String agentId, CoverIdentity coverIdentity) {
        coverIdentities.put(agentId, coverIdentity);
        updateUpdatedAt();
    }
}

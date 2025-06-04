package com.sigma48.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;

public class Mission {

    private String id;
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
    private MissionStatus status;
    private List<String> assignedAgents;
    private Map<String, CoverIdentity> coverIdentities;
    private String dokBriefingPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //Konstruktor Default
    public Mission() {
        this.id = UUID.randomUUID().toString();
        this.status = MissionStatus.PLANNED;
        this.assignedAgents = new ArrayList<>();
        this.coverIdentities = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Konstruktor dengan parameter
    public Mission(String judul, String tujuan, String deskripsi, String targetId) {
        this(); // panggil konstruktor default
        this.judul = judul;
        this.tujuan = tujuan;
        this.deskripsi = deskripsi;
        this.targetId = targetId;
    }

    //Getter dan Setter
    public String getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
        updateUpdatedAt();
    }

    public String getTujuan() {
        return tujuan;
    }

    public void setTujuan(String tujuan) {
        this.tujuan = tujuan;
        updateUpdatedAt();
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
        updateUpdatedAt();
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
        updateUpdatedAt();
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
        updateUpdatedAt();
    }

    public List<String> getAssignedAgents() {
        return assignedAgents;
    }

    public void setAssignedAgents(List<String> assignedAgents) {
        this.assignedAgents = assignedAgents;
        updateUpdatedAt();
    }

    public Map<String, CoverIdentity> getCoverIdentities() {
        return coverIdentities;
    }

    public void setCoverIdentities(Map<String, CoverIdentity> coverIdentities) {
        this.coverIdentities = coverIdentities;
        updateUpdatedAt();
    }

    public String getDokBriefingPath() {
        return dokBriefingPath;
    }

    public void setDokBriefingPath(String dokBriefingPath) {
        this.dokBriefingPath = dokBriefingPath;
        updateUpdatedAt();
    }

    public String getAnalisisRisiko() {
        return analisisRisiko;
    }

    public void setAnalisisRisiko(String analisisRisiko) {
        this.analisisRisiko = analisisRisiko;
        updateUpdatedAt();;
    }
    
    public String getJenisOperasi() {
        return jenisOperasi;
    }
    
    public void setJenisOperasi(String jenisOperasi) {
        this.jenisOperasi = jenisOperasi;
        updateUpdatedAt();;
    }
    
    public String getLokasi() {
        return lokasi;
    }
    
    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
        updateUpdatedAt();
    }

    public String getStrategi() {
        return strategi;
    }

    public void setStrategi(String strategi) {
        this.strategi = strategi;
        updateUpdatedAt();
    }

    public String getProtokol() {
        return protokol;
    }
    
    public void setProtokol(String protokol) {
        this.protokol = protokol;
        updateUpdatedAt();
    }

    public void setKomandanId(String komandanId) {
        this.komandanId = komandanId;
        updateUpdatedAt();
    }

    public String getKomandaId() {
        return komandanId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //Metode Tambahan
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

    // Update waktu terakhir
    private void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    //Override toString
    @Override
    public String toString() {
        return judul + " (Status: " + status.getDisplayName() + ")";
    }
}

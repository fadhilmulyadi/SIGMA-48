package com.sigma48.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Target {
    private String id;
    private String nama;
    private TargetType tipe;
    private String lokasi, deskripsi;
    private ThreatLevel ancaman;
    private List<String> evidencePaths;
    private String profileImagePath;

    public Target() {
        this.id = UUID.randomUUID().toString();
        this.tipe = TargetType.LAINNYA;
        this.ancaman = ThreatLevel.TIDAK_DIKETAHUI;
        this.evidencePaths = new ArrayList<>();
        this.profileImagePath = null;
    }

    public Target(String nama, TargetType tipe, String lokasi, ThreatLevel ancaman, String deskripsi) {
        this();
        this.nama = nama;
        this.tipe = tipe;
        this.lokasi = lokasi;
        this.ancaman = ancaman;
        this.deskripsi = deskripsi;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama; 
    }

    public TargetType getTipe() {
        return tipe; 
    }

    public void setTipe(TargetType tipe) {
        this.tipe = tipe;
    }

    public String getLokasi() {
        return lokasi;
    }
    
    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public ThreatLevel getAncaman() {
        return ancaman;
    }

    public void setAncaman(ThreatLevel ancaman) {
        this.ancaman = ancaman;
    }

    public List<String> getEvidencePaths() {
            if (this.evidencePaths == null) {
                this.evidencePaths = new ArrayList<>();
            }
            return evidencePaths;
        }

    public void setEvidencePaths(List<String> evidencePaths) {
        this.evidencePaths = evidencePaths;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    @Override
    public String toString() {
        return nama + " (" + (tipe != null ? tipe.getDisplayName() : "N/A") + ")";
    }
}
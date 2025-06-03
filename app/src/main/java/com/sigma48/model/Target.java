package com.sigma48.model;

import java.util.UUID;

public class Target {
    private String id;
    private String nama;
    private TargetType tipe;
    private String lokasi;
    private ThreatLevel ancaman;

    public Target() {
        this.id = UUID.randomUUID().toString();
        this.tipe = TargetType.LAINNYA;
        this.ancaman = ThreatLevel.TIDAK_DIKETAHUI;
    }

    public Target(String nama, TargetType tipe, String lokasi, ThreatLevel ancaman) {
        this();
        this.nama = nama;
        this.tipe = tipe;
        this.lokasi = lokasi;
        this.ancaman = ancaman;
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

    public ThreatLevel getAncaman() {
        return ancaman;
    }

    public void setAncaman(ThreatLevel ancaman) {
        this.ancaman = ancaman;
    }

    @Override
    public String toString() {
        return nama + " (" + (tipe != null ? tipe.getDisplayName() : "N/A") + ")";
    }
}
package com.sigma48.model;

public enum ThreatLevel {
    TIDAK_DIKETAHUI("Tidak Diketahui"),
    SANGAT_RENDAH("Sangat Rendah"),
    RENDAH("Rendah"),
    SEDANG("Sedang"),
    TINGGI("Tinggi"),
    SANGAT_TINGGI("Sangat Tinggi"),
    KRITIS("Kritis");

    private final String displayName;

    ThreatLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
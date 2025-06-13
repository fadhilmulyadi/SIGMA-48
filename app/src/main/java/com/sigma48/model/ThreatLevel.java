package com.sigma48.model;

public enum ThreatLevel {
    TIDAK_DIKETAHUI("TIDAK DIKETAHUI"),
    SANGAT_RENDAH("SANGAT RENDAH"),
    RENDAH("RENDAH"),
    SEDANG("SEDANG"),
    TINGGI("TINGGI"),
    SANGAT_TINGGI("SANGAT TINGGI"),
    KRITIS("KRITIS");

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
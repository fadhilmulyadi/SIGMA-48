package com.sigma48.model;

public enum TargetType {
    ORGANISASI("Organisasi"),
    INDIVIDU("Individu"),
    FASILITAS("Fasilitas"),
    INFRASTRUKTUR_KRITIS("Infrastruktur Kritis"),
    SISTEM_INFORMASI("Sistem Informasi"),
    JARINGAN_KOMUNIKASI("Jaringan Komunikasi"),
    LAINNYA("Lainnya");

    private final String displayName;

    TargetType(String displayName) {
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
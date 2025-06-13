package com.sigma48.model;

public enum TargetType {
    ORGANISASI("ORGANISASI"),
    INDIVIDU("INDIVIDU"),
    FASILITAS("FASILITAS"),
    INFRASTRUKTUR_KRITIS("INFRASTRUKTUR KRITIS"),
    SISTEM_INFORMASI("SISTEM  INFORMASI"),
    JARINGAN_KOMUNIKASI("JARINGAN KOMUNIKASI"),
    LAINNYA("LAINNYA");

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
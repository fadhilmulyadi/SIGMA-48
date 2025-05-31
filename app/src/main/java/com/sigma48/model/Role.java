package com.sigma48.model;

public enum Role {
    DIREKTUR_INTELIJEN("Direktur Intelijen"),
    ANALIS_INTELIJEN("Analis Intelijen"),
    KOMANDAN_OPERASI("Komandan Operasi"),
    AGEN_LAPANGAN("Agen Lapangan"),
    TEKNISI_DUKUNGAN("Teknisi Dukungan");

    private final String displayName;

    Role(String displayName) {
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
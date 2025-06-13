package com.sigma48.model;

public enum Role {
    DIREKTUR_INTELIJEN("DIREKTUR INTELIJEN"),
    ANALIS_INTELIJEN("ANALIS INTELIJEN"),
    KOMANDAN_OPERASI("KOMANDAN OPERASI"),
    AGEN_LAPANGAN("AGEN LAPAGAN"),
    ADMIN("ADMIN");

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
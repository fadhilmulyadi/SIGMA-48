package com.sigma48.model;

public enum OperationEffectiveness {
    SANGAT_MEMUASKAN("Sangat Memuaskan", "Hasil jauh melebihi ekspektasi, semua target tercapai dengan sempurna."),
    MEMUASKAN("Memuaskan", "Semua target utama tercapai dengan baik, beberapa keberhasilan tambahan."),
    CUKUP("Cukup", "Target utama tercapai, namun ada beberapa kekurangan atau kendala minor."),
    KURANG_MEMUASKAN("Kurang Memuaskan", "Beberapa target utama tidak tercapai atau terdapat masalah signifikan."),
    TIDAK_EFEKTIF("Tidak Efektif / Gagal", "Misi gagal mencapai tujuan utamanya atau dampak negatif signifikan.");

    private final String displayName;
    private final String description;

    OperationEffectiveness(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
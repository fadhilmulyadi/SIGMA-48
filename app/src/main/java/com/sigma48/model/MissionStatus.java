package com.sigma48.model;

public enum MissionStatus {

    DRAFT_ANALIS("DRAFT (OLEH ANALISIS)", "Misi baru dibuat oleh Analis, menunggu review Direktur."),
    MENUNGGU_PERENCANAAN_KOMANDAN("MENUNGGU PERENCANAAN KO", "Disetujui Direktur, Komandan telah ditugaskan, menunggu rencana detail."), // BARU (atau bisa jadi PLANNED yang dimodifikasi)
    PLANNED("PLANNED", "Rencana operasional awal telah dibuat atau Komandan ditugaskan."),
    READY_FOR_BRIEFING("BRIEFING", "Rencana operasional dan agen telah ditetapkan, siap untuk briefing."),
    ACTIVE("ACTIVE", "Misi sedang berjalan dan dilaksanakan oleh agen."),
    COMPLETED("COMPLETED", "Misi telah selesai dilaksanakan dengan sukses."),
    FAILED("FAILED", "Misi gagal mencapai tujuannya."),
    CANCELLED("CANCELLED", "Misi dibatalkan sebelum atau saat pelaksanaan.");

    //Atribut Enum
    private final String displayName;
    private final String description;

    //Konstruktor Enum
    MissionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    //Getter
    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    //Override to string
    @Override
    public String toString() {
        return displayName;
    }
}

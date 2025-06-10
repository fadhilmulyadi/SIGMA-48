package com.sigma48.model;

public enum MissionStatus {

    DRAFT_ANALIS("Draft (Oleh Analis)", "Misi baru dibuat oleh Analis, menunggu review Direktur."), // BARU
    MENUNGGU_PERENCANAAN_KOMANDAN("Menunggu Perencanaan KO", "Disetujui Direktur, Komandan telah ditugaskan, menunggu rencana detail."), // BARU (atau bisa jadi PLANNED yang dimodifikasi)
    PLANNED("Planned", "Rencana operasional awal telah dibuat atau Komandan ditugaskan."), // Maknanya bisa disesuaikan
    READY_FOR_BRIEFING("Siap Briefing", "Rencana operasional dan agen telah ditetapkan, siap untuk briefing."),
    ACTIVE("Active", "Misi sedang berjalan dan dilaksanakan oleh agen."),
    COMPLETED("Completed", "Misi telah selesai dilaksanakan dengan sukses."),
    FAILED("Failed", "Misi gagal mencapai tujuannya."),
    CANCELLED("Cancelled", "Misi dibatalkan sebelum atau saat pelaksanaan.");

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

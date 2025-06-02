package com.sigma48.model;

public enum MissionStatus {

    PLANNED("Planned", "Misi sedang dalam tahap perencanaan awal."),
    READY_FOR_BRIEFING("Ready for Briefing", "Rencana operasional dan agen telah ditetapkan, siap untuk briefing."),
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

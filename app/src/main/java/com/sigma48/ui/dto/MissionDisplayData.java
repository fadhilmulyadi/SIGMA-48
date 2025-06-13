package com.sigma48.ui.dto;

import com.sigma48.model.MissionStatus;

public class MissionDisplayData {
    private final String id;
    private final String judul;
    private final String tujuanSingkat;
    private final String statusDisplayName;
    private final MissionStatus statusEnum;
    private final String targetName;
    private final String tanggalUpdateFormatted;
    private final String komandanName;

     public MissionDisplayData(String id, String judul, String tujuanSingkat,
                              MissionStatus statusEnum, String targetName,
                              String tanggalUpdateFormatted, String komandanName) {
        this.id = id;
        this.judul = judul;
        this.tujuanSingkat = tujuanSingkat != null ? tujuanSingkat : "";
        this.statusEnum = statusEnum;
        this.statusDisplayName = statusEnum != null ? statusEnum.getDisplayName() : "Status Tidak Diketahui";
        this.targetName = targetName != null && !targetName.isEmpty() ? targetName : "Target Belum Ditentukan";
        this.tanggalUpdateFormatted = tanggalUpdateFormatted != null ? tanggalUpdateFormatted : "";
        this.komandanName = komandanName != null && !komandanName.isEmpty() ? komandanName : "Komandan Belum Ditugaskan";
    }

    public String getId() { 
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public String getTujuanSingkat() {
        return tujuanSingkat;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public MissionStatus getStatusEnum() {
        return statusEnum;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTanggalUpdateFormatted() {
        return tanggalUpdateFormatted;
    }

    public String getKomandanName() {
        return komandanName;
    }
}
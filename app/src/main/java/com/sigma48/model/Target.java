package com.sigma48.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@ToString(exclude = {"evidencePaths", "profileImagePath", "deskripsi", "lokasi", "ancaman"})
public class Target {

    private String id = UUID.randomUUID().toString();
    private String nama;
    private TargetType tipe = TargetType.LAINNYA;
    private String lokasi;
    private String deskripsi;
    private ThreatLevel ancaman = ThreatLevel.TIDAK_DIKETAHUI;
    private List<String> evidencePaths = new ArrayList<>();
    private String profileImagePath;

    public Target(String nama, TargetType tipe, String lokasi, ThreatLevel ancaman, String deskripsi) {
        this();
        this.nama = nama;
        this.tipe = tipe;
        this.lokasi = lokasi;
        this.ancaman = ancaman;
        this.deskripsi = deskripsi;
    }

    public List<String> getEvidencePaths() {
        if (evidencePaths == null) {
            evidencePaths = new ArrayList<>();
        }
        return evidencePaths;
    }

    @Override
    public String toString() {
        return nama + " (" + (tipe != null ? tipe.getDisplayName() : "N/A") + ")";
    }
}

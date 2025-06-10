package com.sigma48.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@ToString(exclude = "isi") // custom toString nanti akan ditulis ulang
public class Report {

    private String reportId = UUID.randomUUID().toString();
    private String missionId;
    private String userId;
    private Role userRoleAtReporting;
    private LocalDateTime waktuLapor = LocalDateTime.now();
    private String isi;
    private List<String> lampiran = new ArrayList<>();
    private String lokasi;

    // Constructor tambahan
    public Report(String missionId, String userId, Role userRoleAtReporting, String isi) {
        this.missionId = missionId;
        this.userId = userId;
        this.userRoleAtReporting = userRoleAtReporting;
        this.isi = isi;
    }

    public Report(String missionId, String userId, Role userRoleAtReporting, String isi, List<String> lampiran) {
        this(missionId, userId, userRoleAtReporting, isi);
        if (lampiran != null) {
            this.lampiran = new ArrayList<>(lampiran);
        }
    }

    public List<String> getLampiran() {
        return lampiran == null ? new ArrayList<>() : lampiran;
    }

    public void setLampiran(List<String> lampiran) {
        this.lampiran = lampiran != null ? new ArrayList<>(lampiran) : new ArrayList<>();
    }

    public void tambahLampiran(String pathFile) {
        if (pathFile != null && !pathFile.trim().isEmpty()) {
            this.lampiran.add(pathFile);
        }
    }

    public void hapusLampiran(String pathFile) {
        if (pathFile != null) {
            this.lampiran.remove(pathFile);
        }
    }

    @Override
    public String toString() {
        return "Report{" +
               "reportId='" + reportId + '\'' +
               ", missionId='" + missionId + '\'' +
               ", userId='" + userId + '\'' +
               ", waktuLapor=" + waktuLapor +
               ", isi='" + (isi != null && isi.length() > 50 ? isi.substring(0, 50) + "..." : isi) + '\'' +
               ", jumlahLampiran=" + (lampiran != null ? lampiran.size() : 0) +
               '}';
    }
}

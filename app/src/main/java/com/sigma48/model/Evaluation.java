package com.sigma48.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "catatanMentalAgen", "kondisiFisikAgen", "catatanUmumEvaluator" })
public class Evaluation {

    private String id = UUID.randomUUID().toString();
    private String missionId;
    private String agentId;
    private String evaluatorId;
    private LocalDateTime timestampEvaluasi = LocalDateTime.now();

    private OperationEffectiveness efektivitasOperasi;
    private String catatanMentalAgen;
    private String kondisiFisikAgen;
    private String catatanUmumEvaluator;

    // Constructor tambahan khusus (partial fields)
    public Evaluation(String missionId, String evaluatorId,
                      OperationEffectiveness efektivitasOperasi, String catatanUmumEvaluator) {
        this.id = UUID.randomUUID().toString();
        this.timestampEvaluasi = LocalDateTime.now();
        this.missionId = missionId;
        this.evaluatorId = evaluatorId;
        this.efektivitasOperasi = efektivitasOperasi;
        this.catatanUmumEvaluator = catatanUmumEvaluator;
    }
}

package com.sigma48.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Evaluation {

    private String id;
    private String missionId;
    private String agentId;
    private String evaluatorId;
    private LocalDateTime timestampEvaluasi;
    
    private OperationEffectiveness efektivitasOperasi;
    private String catatanMentalAgen;
    private String kondisiFisikAgen;
    private String catatanUmumEvaluator;

    public Evaluation() {
        this.id = UUID.randomUUID().toString();
        this.timestampEvaluasi = LocalDateTime.now();
    }

    public Evaluation(String missionId, String evaluatorId, 
                      OperationEffectiveness efektivitasOperasi, String catatanUmumEvaluator) {
        this(); // Panggil konstruktor default
        this.missionId = missionId;
        this.evaluatorId = evaluatorId;
        this.efektivitasOperasi = efektivitasOperasi;
        this.catatanUmumEvaluator = catatanUmumEvaluator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMissionId() {
        return missionId;
    }
    
    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }
    
    public LocalDateTime getTimestampEvaluasi() {
        return timestampEvaluasi;
    }
    
    public void setTimestampEvaluasi(LocalDateTime timestampEvaluasi) {
        this.timestampEvaluasi = timestampEvaluasi;
    }
    
    public OperationEffectiveness getEfektivitasOperasi() {
        return efektivitasOperasi;
    }

    public void setEfektivitasOperasi(OperationEffectiveness efektivitasOperasi) {
        this.efektivitasOperasi = efektivitasOperasi;
    }

    public String getCatatanMentalAgen() {
        return catatanMentalAgen;
    }

    public void setCatatanMentalAgen(String catatanMentalAgen) {
        this.catatanMentalAgen = catatanMentalAgen;
    }

    public String getKondisiFisikAgen() {
        return kondisiFisikAgen;
    }

    public void setKondisiFisikAgen(String kondisiFisikAgen) {
        this.kondisiFisikAgen = kondisiFisikAgen;
    }

    public String getCatatanUmumEvaluator() {
        return catatanUmumEvaluator;
    }
    
    public void setCatatanUmumEvaluator(String catatanUmumEvaluator) {
        this.catatanUmumEvaluator = catatanUmumEvaluator;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
               "id='" + id + '\'' +
               ", missionId='" + missionId + '\'' +
               ", agentId='" + (agentId != null ? agentId : "N/A") + '\'' +
               ", evaluatorId='" + evaluatorId + '\'' +
               ", efektivitas=" + (efektivitasOperasi != null ? efektivitasOperasi.getDisplayName() : "N/A") +
               '}';
    }
}
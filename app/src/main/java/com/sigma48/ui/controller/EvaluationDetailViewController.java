package com.sigma48.ui.controller;

import com.sigma48.model.Evaluation;
import com.sigma48.model.Mission;
import com.sigma48.ServiceLocator;
import com.sigma48.ui.controller.base.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.time.format.DateTimeFormatter;

public class EvaluationDetailViewController extends BaseController {

    @FXML private Label missionTitleLabel;
    @FXML private Label timestampLabel;
    @FXML private Label evaluatorNameLabel;
    @FXML private Label effectivenessLabel;
    @FXML private Label agentNameLabel;
    @FXML private TextArea mentalNotesArea;
    @FXML private TextArea physicalNotesArea;
    @FXML private TextArea generalNotesArea;
    @FXML private Button backButton;

    private Mission currentMission;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss");

    public void loadEvaluationDetails(Evaluation evaluation) {
        if (evaluation == null) {
            missionTitleLabel.setText("ERROR: DATA EVALUASI TIDAK DITEMUKAN");
            return;
        }

        ServiceLocator.getMissionManager().getMissionById(evaluation.getMissionId()).ifPresent(mission -> {
            this.currentMission = mission;
            missionTitleLabel.setText(mission.getJudul().toUpperCase());
        });

        timestampLabel.setText(evaluation.getTimestampEvaluasi().format(formatter));

        ServiceLocator.getUserManager().findUserById(evaluation.getEvaluatorId())
            .ifPresent(user -> evaluatorNameLabel.setText(user.getUsername() + " (" + user.getRole().getDisplayName() + ")"));

        if (evaluation.getEfektivitasOperasi() != null) {
            effectivenessLabel.setText(evaluation.getEfektivitasOperasi().getDisplayName());
        }

        if (evaluation.getAgentId() != null && !evaluation.getAgentId().isEmpty()) {
            ServiceLocator.getUserManager().findUserById(evaluation.getAgentId())
                .ifPresent(user -> agentNameLabel.setText(user.getUsername()));
        } else {
            agentNameLabel.setText("Misi Keseluruhan (Tidak spesifik ke agen)");
        }

        mentalNotesArea.setText(evaluation.getCatatanMentalAgen());
        physicalNotesArea.setText(evaluation.getKondisiFisikAgen());
        generalNotesArea.setText(evaluation.getCatatanUmumEvaluator());
    }

    @FXML
    private void handleBack() {
        if (mainDashboardController != null && currentMission != null) {
            mainDashboardController.showMissionPlanningView(currentMission);
        }
    }
}
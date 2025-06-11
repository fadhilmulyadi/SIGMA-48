package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator;
import com.sigma48.manager.TargetManager;
import com.sigma48.manager.UserManager;
import com.sigma48.model.CoverIdentity;
import com.sigma48.model.Mission;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.ui.controller.base.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AgentMissionDetailViewController extends BaseController {

    @FXML private Button backButton;
    @FXML private Label missionTitleLabel;
    @FXML private Label missionStatusLabel;
    @FXML private TextArea tujuanTextArea;
    @FXML private TextArea deskripsiTextArea;
    @FXML private Label targetNameLabel;
    @FXML private Button viewDossierButton;
    @FXML private Label targetDetailsLabel;
    @FXML private Label commanderNameLabel;
    @FXML private Label coverNameLabel;
    @FXML private Label coverRoleLabel;
    @FXML private TextArea strategiTextArea;
    @FXML private TextArea protokolTextArea;
    @FXML private Label jenisOperasiLabel;
    @FXML private Label lokasiLabel;
    @FXML private TextArea risikoTextArea;
    @FXML private Button submitReportButton;

    private Mission currentMission;
    private User currentUser;
    private TargetManager targetManager;
    private UserManager userManager;

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
        this.targetManager = ServiceLocator.getTargetManager();
        this.userManager = ServiceLocator.getUserManager();
    }

    public void loadMissionDetails(Mission mission) {
        this.currentMission = mission;
        
        missionTitleLabel.setText(mission.getJudul());
        missionStatusLabel.setText(mission.getStatus().getDisplayName().toUpperCase());
        tujuanTextArea.setText(mission.getTujuan());
        deskripsiTextArea.setText(mission.getDeskripsi());
        
        targetManager.getTargetById(mission.getTargetId()).ifPresent(target -> {
            targetNameLabel.setText(target.getNama());
            targetDetailsLabel.setText("TIPE: " + target.getTipe().getDisplayName() + " | LOKASI: " + target.getLokasi());
        });

        userManager.findUserById(mission.getKomandanId()).ifPresent(komandan -> {
            commanderNameLabel.setText(komandan.getUsername());
        });

        CoverIdentity cover = mission.getCoverIdentities().get(currentUser.getId());
        if (cover != null) {
            coverNameLabel.setText(cover.getCoverName() != null ? cover.getCoverName() : "Tidak Diatur");
            coverRoleLabel.setText(cover.getCoverRole() != null ? cover.getCoverRole() : "Tidak Diatur");
        } else {
            coverNameLabel.setText("Tidak Ada Identitas Samaran");
            coverRoleLabel.setText("-");
        }

        strategiTextArea.setText(mission.getStrategi());
        protokolTextArea.setText(mission.getProtokol());
        jenisOperasiLabel.setText(mission.getJenisOperasi());
        lokasiLabel.setText(mission.getLokasi());
        risikoTextArea.setText(mission.getAnalisisRisiko());
    }

    @FXML
    private void handleBack() {
        if (mainDashboardController != null) {
            mainDashboardController.loadView("/com/sigma48/fxml/AgentMissionView.fxml", null);
        }
    }

    @FXML
    private void handleViewDossier() {
        if (mainDashboardController != null && currentMission != null) {
            targetManager.getTargetById(currentMission.getTargetId()).ifPresent(target -> {
                mainDashboardController.showDossierView(target, currentMission, "agentDetail");
            });
        }
    }

    @FXML
    private void handleSubmitReport() {
        if (mainDashboardController != null && currentMission != null) {
            mainDashboardController.showReportSubmissionForm(currentMission.getId());
        }
    }
}
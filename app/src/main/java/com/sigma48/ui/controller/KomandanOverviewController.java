package com.sigma48.ui.controller;

import com.sigma48.Main;    
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.ReportManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.manager.UserManager;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Report;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.ui.dto.MissionDisplayData;
import com.sigma48.ui.dto.ReportDisplayData;
import com.sigma48.ServiceLocator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KomandanOverviewController extends BaseController {

    @FXML private Label activeMissionsLedCountLabel;
    @FXML private Label assignedAgentsCountLabel;
    @FXML private Label readyForBriefingCountLabel;

    @FXML private ListView<MissionDisplayData> pendingActionMissionsListView;
    @FXML private ListView<ReportDisplayData> recentReportsListView;
    @FXML private TableView<Mission> missionsTableView; 
    @FXML private Button planNextMissionButton;
    @FXML private Button viewAllMyMissionsButton;

    private MissionManager missionManager;
    private ReportManager reportManager;
    private UserManager userManager;
    private TargetManager targetManager;
    private User currentUser;

    private ObservableList<MissionDisplayData> pendingMissionsData = FXCollections.observableArrayList();
    private ObservableList<ReportDisplayData> recentReportsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Get managers from ServiceLocator
        this.missionManager = ServiceLocator.getMissionManager();
        this.reportManager = ServiceLocator.getReportManager();
        this.userManager = ServiceLocator.getUserManager();
        this.targetManager = ServiceLocator.getTargetManager();
        
        // Get current user
        this.currentUser = Main.authManager.getCurrentUser();
        
        setupListViews();
        setupActionButtons();
        loadDashboardData();
    }

    private void setupListViews() {
        pendingActionMissionsListView.setItems(pendingMissionsData);
        pendingActionMissionsListView.setPlaceholder(new Label("TIDAK ADA MISI YANG PERLU PERENCANAAN."));
        pendingActionMissionsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MissionDisplayData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    VBox itemBox = new VBox(2);
                    Label titleLabel = new Label("• " + item.getJudul());
                    titleLabel.getStyleClass().add("mission-item-title");
                    Label statusLabel = new Label("  Status: " + item.getStatusDisplayName());
                    statusLabel.getStyleClass().add("mission-item-metadata");
                    itemBox.getChildren().addAll(titleLabel, statusLabel);
                    setGraphic(itemBox);
                }
            }
        });
        
        pendingActionMissionsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MissionDisplayData selected = pendingActionMissionsListView.getSelectionModel().getSelectedItem();
                if (selected != null && mainDashboardController != null) {
                    missionManager.getMissionById(selected.getId()).ifPresent(mainDashboardController::showMissionPlanningView);
                }
            }
        });
        
        recentReportsListView.setItems(recentReportsData);
        recentReportsListView.setPlaceholder(new Label("BELUM ADA LAPORAN DARI TIM."));
        recentReportsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ReportDisplayData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    VBox itemBox = new VBox(2);
                    Label reportInfoLabel = new Label(item.getSubmitterName() + " @ " + item.getMissionJudul());
                    reportInfoLabel.getStyleClass().add("form-label-bold");
                    Label snippetLabel = new Label("\"" + item.getContentSnippet() + "\"");
                    snippetLabel.getStyleClass().add("mission-item-details");
                    Label timeLabel = new Label(item.getTimestamp());
                    timeLabel.getStyleClass().add("mission-item-date");
                    itemBox.getChildren().addAll(reportInfoLabel, snippetLabel, timeLabel);
                    setGraphic(itemBox);
                }
            }
        });
        
        recentReportsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ReportDisplayData selected = recentReportsListView.getSelectionModel().getSelectedItem();
                if (selected != null && mainDashboardController != null) {
                    missionManager.getMissionById(selected.getMissionId()).ifPresent(mainDashboardController::showMissionPlanningView);
                }
            }
        });
    }

    private void loadDashboardData() {
        if (currentUser == null || missionManager == null || reportManager == null || userManager == null) {
            activeMissionsLedCountLabel.setText("-");
            assignedAgentsCountLabel.setText("-");
            readyForBriefingCountLabel.setText("-");
            pendingMissionsData.clear();
            recentReportsData.clear();
            return;
        }

        List<Mission> myMissions = missionManager.getMissionsForCommander(currentUser.getId());
        
        activeMissionsLedCountLabel.setText(String.valueOf(myMissions.stream().filter(m -> m.getStatus() == MissionStatus.ACTIVE).count()));
        readyForBriefingCountLabel.setText(String.valueOf(myMissions.stream().filter(m -> m.getStatus() == MissionStatus.READY_FOR_BRIEFING).count()));

        long totalAssignedAgents = myMissions.stream()
            .filter(m -> m.getStatus() == MissionStatus.ACTIVE || m.getStatus() == MissionStatus.READY_FOR_BRIEFING)
            .mapToLong(m -> m.getAssignedAgents().size())
            .sum();
        assignedAgentsCountLabel.setText(String.valueOf(totalAssignedAgents));

        pendingMissionsData.clear();
        missionManager.getMissionsForCommanderToPlan(currentUser.getId()).forEach(mission -> 
            pendingMissionsData.add(new MissionDisplayData(
                mission.getId(), mission.getJudul(), mission.getTujuan(),
                mission.getStatus(), "N/A", "N/A", currentUser.getUsername()
            ))
        );
        
        recentReportsData.clear();
        myMissions.stream()
            .filter(m -> m.getStatus() == MissionStatus.ACTIVE)
            .flatMap(m -> reportManager.getReportsForMission(m.getId()).stream())
            .sorted(Comparator.comparing(Report::getWaktuLapor, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .forEach(report -> {
                Optional<Mission> mission = missionManager.getMissionById(report.getMissionId());
                Optional<User> submitter = userManager.findUserById(report.getUserId());
                recentReportsData.add(new ReportDisplayData(report, mission.orElse(null), submitter.orElse(null)));
            });
    }

    private void setupActionButtons() {
        planNextMissionButton.setOnAction(e -> {
            Optional<Mission> nextMissionToPlan = missionManager.getMissionsForCommanderToPlan(currentUser.getId()).stream().findFirst();
            if (nextMissionToPlan.isPresent()) {
                mainDashboardController.showMissionPlanningView(nextMissionToPlan.get());
            } else {
                mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", "FOR_COMMANDER_PLANNING");
            }
        });
        
        viewAllMyMissionsButton.setOnAction(e -> 
            mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", "FOR_COMMANDER_ALL_ASSIGNED")
        );
    }
}
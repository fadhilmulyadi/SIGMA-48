package com.sigma48.ui.controller;

import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AnalisOverviewController {

    @FXML private Label draftCountLabel;
    @FXML private Label targetCountLabel;
    @FXML private ListView<Mission> recentDraftsListView;
    @FXML private Button buatMisiButton;
    @FXML private Button manajemenTargetButton;

    private MissionManager missionManager;
    private TargetManager targetManager;
    private MainDashboardController mainDashboardController;

    private final ObservableList<Mission> recentDraftsData = FXCollections.observableArrayList();

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setManagers(MissionManager missionManager, TargetManager targetManager) {
        this.missionManager = missionManager;
        this.targetManager = targetManager;
        loadDashboardData();
    }

    @FXML
    public void initialize() {
        setupListView();
        setupActionButtons();
    }

    private void loadDashboardData() {
        if (missionManager == null || targetManager == null) {
            draftCountLabel.setText("-");
            targetCountLabel.setText("-");
            return;
        }

        // Load Counts
        draftCountLabel.setText(String.valueOf(
            missionManager.getMissionsByStatus(MissionStatus.DRAFT_ANALIS).size()
        ));
        targetCountLabel.setText(String.valueOf(targetManager.getAllTargets().size()));

        // Load Recent Drafts
        recentDraftsData.clear();
        recentDraftsData.addAll(
            missionManager.getMissionsByStatus(MissionStatus.DRAFT_ANALIS).stream()
                .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList())
        );
    }

    private void setupListView() {
        recentDraftsListView.setItems(recentDraftsData);
        recentDraftsListView.setPlaceholder(new Label("Belum ada draft misi yang dibuat."));
        recentDraftsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Mission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
                    setText(item.getJudul() + "\n(Diupdate: " + item.getUpdatedAt().format(formatter) + ")");
                }
            }
        });
    }

    private void setupActionButtons() {
        buatMisiButton.setOnAction(e -> {
            if (mainDashboardController != null) {
                mainDashboardController.loadView("/com/sigma48/fxml/MissionCreateForm.fxml", null);
            }
        });
        manajemenTargetButton.setOnAction(e -> {
            if (mainDashboardController != null) {
                mainDashboardController.showTargetManagementView();
            }
        });
    }
}
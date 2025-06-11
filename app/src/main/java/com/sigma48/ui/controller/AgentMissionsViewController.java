package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.manager.UserManager;
import com.sigma48.model.Mission;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.ui.controller.listitems.MissionListItemController;
import com.sigma48.ui.dto.MissionDisplayData;
import com.sigma48.ui.controller.base.BaseController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AgentMissionsViewController extends BaseController {

    @FXML private ListView<MissionDisplayData> missionsListView;

    private MissionManager missionManager;
    private TargetManager targetManager;
    private UserManager userManager;
    private User currentUser;

    private ObservableList<MissionDisplayData> missionDisplayDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.missionManager = ServiceLocator.getMissionManager();
        this.targetManager = ServiceLocator.getTargetManager();
        this.userManager = ServiceLocator.getUserManager();
        this.currentUser = Main.authManager.getCurrentUser();
        this.currentUser = Main.authManager.getCurrentUser();
        missionsListView.setItems(missionDisplayDataList);
        missionsListView.setPlaceholder(new Label("MEMUAT DATA MISI..."));
        missionsListView.setCellFactory(listView -> new MissionListCell());

        missionsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MissionDisplayData selectedData = missionsListView.getSelectionModel().getSelectedItem();
                if (selectedData != null && mainDashboardController != null) {
                    // Cari objek Mission lengkap berdasarkan ID
                    missionManager.getMissionById(selectedData.getId()).ifPresent(mission -> {
                        // Panggil metode baru di MainDashboardController untuk menampilkan detail
                        mainDashboardController.showAgentMissionDetailView(mission);
                    });
                }
            }
        });
        
        loadMissions();
    }

    private void loadMissions() {
        if (currentUser == null || missionManager == null) {
            missionsListView.setPlaceholder(new Label("Gagal memuat: Pengguna atau manajer tidak valid."));
            return;
        }
        missionDisplayDataList.clear();
        List<Mission> assignedMissions = missionManager.getMissionsForAgent(currentUser.getId());

        if (assignedMissions.isEmpty()) {
            missionsListView.setPlaceholder(new Label("Tidak ada misi yang ditugaskan pada Anda saat ini."));
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        for (Mission mission : assignedMissions) {
            String targetName = targetManager.getTargetById(mission.getTargetId())
                .map(Target::getNama).orElse("N/A");
            String komandanName = userManager.findUserById(mission.getKomandanId())
                .map(User::getUsername).orElse("N/A");

            missionDisplayDataList.add(new MissionDisplayData(
                mission.getId(), mission.getJudul(), mission.getTujuan(),
                mission.getStatus(), targetName, mission.getUpdatedAt().format(formatter), komandanName
            ));
        }
    }

    // Inner class untuk kustomisasi tampilan item di ListView
    private static class MissionListCell extends ListCell<MissionDisplayData> {
        private FXMLLoader fxmlLoader;
        private HBox graphicNode;
        private MissionListItemController itemController;

        @Override
        protected void updateItem(MissionDisplayData missionData, boolean empty) {
            super.updateItem(missionData, empty);
            if (empty || missionData == null) {
                setGraphic(null);
            } else {
                if (fxmlLoader == null) {
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/listitems/MissionListItem.fxml"));
                    try {
                        graphicNode = fxmlLoader.load();
                        itemController = fxmlLoader.getController();
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(new Label("Error memuat item."));
                        return;
                    }
                }
                itemController.setData(missionData);
                setGraphic(graphicNode);
            }
        }
    }
}
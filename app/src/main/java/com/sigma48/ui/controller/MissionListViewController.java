package com.sigma48.ui.controller;

import com.sigma48.Main; // Jika perlu akses AuthManager untuk hak akses
import com.sigma48.dao.MissionDao;
import com.sigma48.dao.TargetDao;
import com.sigma48.dao.UserDao;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.Target;
import com.sigma48.model.MissionStatus; // Untuk menampilkan status

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class MissionListViewController {

    @FXML
    private TableView<MissionTableData> missionTableView;
    @FXML
    private TableColumn<MissionTableData, String> idColumn;
    @FXML
    private TableColumn<MissionTableData, String> judulColumn;
    @FXML
    private TableColumn<MissionTableData, String> tujuanColumn;
    @FXML
    private TableColumn<MissionTableData, String> statusColumn;
    @FXML
    private TableColumn<MissionTableData, String> targetColumn;

    @FXML
    private Button refreshButton;


    private MissionManager missionManager;
    private TargetManager targetManager;

    public static class MissionTableData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty judul;
        private final SimpleStringProperty tujuan;
        private final SimpleStringProperty status;
        private final SimpleStringProperty targetName;

        public MissionTableData(Mission mission, String targetName) {
            this.id = new SimpleStringProperty(mission.getId());
            this.judul = new SimpleStringProperty(mission.getJudul());
            this.tujuan = new SimpleStringProperty(mission.getTujuan());
            this.status = new SimpleStringProperty(mission.getStatus().getDisplayName());
            this.targetName = new SimpleStringProperty(targetName);
        }

        public String getId() {
            return id.get();
        }

        public String getJudul() {
            return judul.get();
        }

        public String getTujuan() {
            return tujuan.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getTargetName() {
            return targetName.get();
        }
    }


    @FXML
    public void initialize() {
        this.missionManager = new MissionManager(new MissionDao(), new TargetDao(), new UserDao());
        this.targetManager = new TargetManager(new TargetDao());

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judul"));
        tujuanColumn.setCellValueFactory(new PropertyValueFactory<>("tujuan"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("targetName"));

        loadMissions();
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadMissions();
    }

    private void loadMissions() {
        List<Mission> missions = missionManager.getAllMissions();
        ObservableList<MissionTableData> tableData = FXCollections.observableArrayList();
        for (Mission mission : missions) {
            String targetName = "N/A";
            if (mission.getTargetId() != null && !mission.getTargetId().isEmpty()) {
                Optional<Target> targetOpt = targetManager.getTargetById(mission.getTargetId());
                if (targetOpt.isPresent()) {
                    targetName = targetOpt.get().getNama();
                } else {
                    targetName = "Target ID: " + mission.getTargetId() + " (Tidak Ditemukan)";
                }
            }
            tableData.add(new MissionTableData(mission, targetName));
        }
        missionTableView.setItems(tableData);
    }
}
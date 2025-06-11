package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.manager.UserManager;
import com.sigma48.model.Mission;
import com.sigma48.model.Role;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.model.MissionStatus;
import com.sigma48.ui.dto.MissionDisplayData;
import com.sigma48.ui.controller.listitems.MissionListItemController;
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.ServiceLocator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MissionListViewController extends BaseController {

    @FXML private Label viewTitleLabel;
    @FXML private ListView<MissionDisplayData> missionListView;
    @FXML private Button refreshButton;

    private MissionManager missionManager;
    private TargetManager targetManager;
    private UserManager userManager;
    private User currentUser;

    private ObservableList<MissionDisplayData> missionDisplayDataList = FXCollections.observableArrayList();
    private String currentListContext = "ALL_MISSIONS";

    public void setListContext(String context) {
        this.currentListContext = context;
        updateViewTitle();
    }
    
    private void updateViewTitle() {
        switch (currentListContext) {
            case "FOR_COMMANDER_PLANNING":
                viewTitleLabel.setText("MISI PERLU PERENCANAAN (KOMANDAN)");
                break;
            case "FOR_AGENT_ASSIGNED":
                viewTitleLabel.setText("MISI DITUGASKAN PADA SAYA (AGEN)");
                break;
            case "DRAFT_REVIEW_DIREKTUR":
                 viewTitleLabel.setText("DRAFT MISI (PERLU REVIEW DIREKTUR)");
                 break;
            default: // ALL_MISSIONS
                viewTitleLabel.setText("DAFTAR SEMUA MISI");
                break;
        }
    }

    @FXML
    public void initialize() {
        // Get managers from ServiceLocator
        this.missionManager = ServiceLocator.getMissionManager();
        this.targetManager = ServiceLocator.getTargetManager();
        this.userManager = ServiceLocator.getUserManager();
        this.currentUser = Main.authManager.getCurrentUser();
        
        missionListView.setItems(missionDisplayDataList);
        missionListView.setCellFactory(new Callback<ListView<MissionDisplayData>, ListCell<MissionDisplayData>>() {
            @Override
            public ListCell<MissionDisplayData> call(ListView<MissionDisplayData> listView) {
                return new MissionListCell();
            }
        });

        missionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click
                MissionDisplayData selectedData = missionListView.getSelectionModel().getSelectedItem();
                if (selectedData != null) {
                    handleMissionItemSelected(selectedData.getId());
                }
            }
        });
        updateViewTitle(); // Set judul awal berdasarkan konteks default
    }
    
    // Panggil ini dari MainDashboardController setelah konteks di-set
    public void loadData() {
        if (missionManager == null || targetManager == null || userManager == null || currentUser == null) {
            System.err.println("MissionListViewController: Managers or currentUser not initialized!");
            missionDisplayDataList.clear();
            missionListView.setPlaceholder(new Label("Gagal memuat data misi (dependensi belum siap)."));
            return;
        }
        loadMissions();
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadMissions();
    }

    private void loadMissions() {
        if (currentUser == null || missionManager == null) {
            missionDisplayDataList.clear();
            missionListView.setPlaceholder(new Label("Gagal memuat data misi."));
            return;
        }

        List<Mission> missionsFromManager = new ArrayList<>();
        // Ambil daftar misi berdasarkan peran pengguna dan konteks
        switch (currentListContext) {
            case "FOR_COMMANDER_PLANNING": // Misi yang perlu direncanakan Komandan
                if (currentUser.getRole() == Role.KOMANDAN_OPERASI) {
                    missionsFromManager = missionManager.getMissionsForCommanderToPlan(currentUser.getId()); // Metode baru di MissionManager
                }
                break;
            case "FOR_AGENT_ASSIGNED": // Misi yang ditugaskan ke Agen
                if (currentUser.getRole() == Role.AGEN_LAPANGAN) {
                    missionsFromManager = missionManager.getMissionsForAgent(currentUser.getId());
                }
                break;
            case "DRAFT_REVIEW_DIREKTUR": // Draft yang perlu direview Direktur
                if (currentUser.getRole() == Role.DIREKTUR_INTELIJEN) {
                    missionsFromManager = missionManager.getMissionsByStatus(MissionStatus.DRAFT_ANALIS); // Metode baru di MissionManager
                }
                break;
            default: // ALL_MISSIONS
                if (currentUser.getRole() == Role.DIREKTUR_INTELIJEN || currentUser.getRole() == Role.ADMIN) {
                     missionsFromManager = missionManager.getAll();
                } else if (currentUser.getRole() == Role.ANALIS_INTELIJEN) {
                    missionsFromManager = missionManager.getMissionsByStatus(MissionStatus.DRAFT_ANALIS); // Atau filter lain
                } else if (currentUser.getRole() == Role.KOMANDAN_OPERASI) {
                // Komandan hanya melihat semua misi yang ditugaskan padanya
                    missionsFromManager = missionManager.getMissionsForCommander(currentUser.getId());
                }
                break;
        }
        
        missionDisplayDataList.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        if (missionsFromManager != null && !missionsFromManager.isEmpty()) {
            for (Mission mission : missionsFromManager) {
                String targetName = "N/A";
                if (mission.getTargetId() != null && !mission.getTargetId().isEmpty() && targetManager != null) {
                    Optional<Target> targetOpt = targetManager.getTargetById(mission.getTargetId());
                    targetName = targetOpt.map(Target::getNama).orElse(mission.getTargetId());
                }
                String tujuanSingkat = mission.getTujuan(); // Atau getDeskripsi()
                if (tujuanSingkat != null && tujuanSingkat.length() > 60) {
                    tujuanSingkat = tujuanSingkat.substring(0, 57) + "...";
                }
                String lastUpdateStr = mission.getUpdatedAt() != null ? mission.getUpdatedAt().format(formatter) : "N/A";

                String komandanName = "Belum Ditugaskan";
                if (mission.getKomandanId() != null && !mission.getKomandanId().isEmpty() && userManager != null) {
                    Optional<User> komandanOpt = userManager.findUserById(mission.getKomandanId());
                    if (komandanOpt.isPresent()) {
                        komandanName = komandanOpt.get().getUsername();
                    } else {
                        komandanName = "ID: " + mission.getKomandanId() + " (Tidak Ditemukan)";
                    }
                }

                missionDisplayDataList.add(new MissionDisplayData(
                        mission.getId(),
                        mission.getJudul(),
                        tujuanSingkat,
                        mission.getStatus(),
                        targetName,
                        "Update: " + lastUpdateStr,
                        komandanName
                ));
            }
        }
        
        if (missionDisplayDataList.isEmpty()) {
            missionListView.setPlaceholder(new Label("Tidak ada misi yang sesuai dengan kriteria saat ini."));
        }
    }

    private void handleMissionItemSelected(String missionId) {
        if (missionId == null || mainDashboardController == null) return;

        Optional<Mission> missionOpt = missionManager.getMissionById(missionId);
        if (missionOpt.isPresent()) {
            Mission selectedMission = missionOpt.get();
            User user = Main.authManager.getCurrentUser();

            // Logika navigasi berdasarkan peran dan status misi
            if (user.getRole() == Role.DIREKTUR_INTELIJEN) {
                if (selectedMission.getStatus() == MissionStatus.DRAFT_ANALIS) {
                    mainDashboardController.showMissionPlanningView(selectedMission);
                } else {
                    // Direktur bisa membuka detail/planning untuk misi apa pun
                    mainDashboardController.showMissionPlanningView(selectedMission);
                }
            } else if (user.getRole() == Role.KOMANDAN_OPERASI) {
                // Komandan hanya bisa membuka misi yang ditugaskan padanya untuk perencanaan detail
                if (user.getId().equals(selectedMission.getKomandanId()) &&
                    (selectedMission.getStatus() == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN ||
                     selectedMission.getStatus() == MissionStatus.PLANNED ||
                     selectedMission.getStatus() == MissionStatus.READY_FOR_BRIEFING ||
                     selectedMission.getStatus() == MissionStatus.ACTIVE) ) { // Komandan juga bisa lihat detail misi aktifnya
                    mainDashboardController.showMissionPlanningView(selectedMission);
                } else {
                    // Tampilkan pesan atau dialog read-only detail misi (belum dirancang)
                    System.out.println("Komandan tidak bisa merencanakan misi ini atau status tidak sesuai.");
                }
            } else if (user.getRole() == Role.ANALIS_INTELIJEN) {
                // Analis mungkin bisa melihat detail draftnya, atau misi yang terkait targetnya
                // Untuk sekarang, bisa buka MissionPlanningView dalam mode read-only atau form draft lagi
                if (selectedMission.getStatus() == MissionStatus.DRAFT_ANALIS) {
                    // mainDashboardController.showMissionCreateFormForEdit(selectedMission); // Perlu metode ini
                    System.out.println("Analis membuka draft: " + selectedMission.getJudul());
                }
            } else if (user.getRole() == Role.AGEN_LAPANGAN) {
                // Agen bisa buka form laporan untuk misi aktifnya
                if (selectedMission.getStatus() == MissionStatus.ACTIVE || selectedMission.getStatus() == MissionStatus.READY_FOR_BRIEFING) {
                     mainDashboardController.showReportSubmissionForm(selectedMission.getId());
                }
            }
        }
    }

    private class MissionListCell extends ListCell<MissionDisplayData> {
        private FXMLLoader fxmlLoader;
        private HBox graphicNode;
        private MissionListItemController itemController;

        @Override
        protected void updateItem(MissionDisplayData missionData, boolean empty) {
            super.updateItem(missionData, empty);

            if (empty || missionData == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (fxmlLoader == null) { // Load FXML hanya sekali per cell
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/listitems/MissionListItem.fxml"));
                    try {
                        graphicNode = fxmlLoader.load();
                        itemController = fxmlLoader.getController();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle error loading FXML untuk item
                        setText("Error loading item view");
                        setGraphic(null);
                        return; // Keluar jika gagal load FXML
                    }
                }
                // Set data ke controller item
                itemController.setData(missionData);
                setGraphic(graphicNode); // Set FXML yang sudah di-load sebagai graphic cell
                setText(null); // Pastikan tidak ada teks default dari ListCell
            }
        }
    }
}
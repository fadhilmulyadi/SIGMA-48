package com.sigma48.ui.controller;

import com.sigma48.manager.MissionManager;
import com.sigma48.manager.EvaluationManager;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Mission;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Comparator;
import java.util.List;

public class DirekturOverviewController {

    @FXML private Label aktifMissionsCountLabel;
    @FXML private Label plannedMissionsCountLabel;
    @FXML private Label draftMissionsCountLabel;
    @FXML private Label evalMissionsCountLabel;
    @FXML private ListView<String> notificationsListView;

    @FXML private Button buatMisiBaruButton;
    @FXML private Button lihatSemuaMisiButton;
    @FXML private Button reviewDraftButton;
    @FXML private Button bukaAntrianEvaluasiButton;
    @FXML private Button manajemenTargetButton;

    private MissionManager missionManager;
    private EvaluationManager evaluationManager;
    private MainDashboardController mainDashboardController;

    @FXML
    public void initialize() {
        // Kosongkan semua data awal dan setup tombol
        aktifMissionsCountLabel.setText("-");
        plannedMissionsCountLabel.setText("-");
        draftMissionsCountLabel.setText("-");
        evalMissionsCountLabel.setText("-");
        notificationsListView.setPlaceholder(new Label("Memuat notifikasi..."));
        setupActionButtons();
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setManagers(MissionManager missionManager, EvaluationManager evaluationManager /*, manager lain jika perlu */) {
        this.missionManager = missionManager;
        this.evaluationManager = evaluationManager;
        loadDashboardData(); // Muat data setelah semua manager di-set
    }


    private void loadDashboardData() {
        if (missionManager == null || evaluationManager == null) {
            System.err.println("DirekturOverviewController: Gagal memuat data karena manager belum di-set.");
            notificationsListView.setPlaceholder(new Label("Error: Gagal memuat data."));
            return;
        }

        // Muat Ringkasan Status Misi
        List<Mission> allMissions = missionManager.getAllMissions();
        aktifMissionsCountLabel.setText(String.valueOf(allMissions.stream().filter(m -> m.getStatus() == MissionStatus.ACTIVE).count()));
        plannedMissionsCountLabel.setText(String.valueOf(allMissions.stream().filter(m -> 
            m.getStatus() == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN || 
            m.getStatus() == MissionStatus.READY_FOR_BRIEFING).count()));
        long draftCount = allMissions.stream().filter(m -> m.getStatus() == MissionStatus.DRAFT_ANALIS).count();
        draftMissionsCountLabel.setText(String.valueOf(draftCount));
        
        // Menampilkan jumlah misi yang sudah selesai
        long finishedCount = allMissions.stream().filter(m -> m.getStatus() == MissionStatus.COMPLETED || m.getStatus() == MissionStatus.FAILED).count();
        evalMissionsCountLabel.setText(String.valueOf(finishedCount));

        // Muat Notifikasi
        ObservableList<String> notifItems = FXCollections.observableArrayList();
        if (draftCount > 0) {
            notifItems.add("PERHATIAN: " + draftCount + " draft misi baru menunggu review Anda.");
        }

        allMissions.stream()
            .filter(m -> m.getStatus() == MissionStatus.ACTIVE)
            .sorted(Comparator.comparing(Mission::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(3)
            .forEach(m -> notifItems.add("AKTIF: " + m.getJudul()));
        
        if(notifItems.isEmpty()){
            notificationsListView.setPlaceholder(new Label("Tidak ada notifikasi penting saat ini."));
        }
        notificationsListView.setItems(notifItems);
    }

    private void setupActionButtons() {
        buatMisiBaruButton.setOnAction(e -> {
            if (mainDashboardController != null) 
                mainDashboardController.loadView("/com/sigma48/fxml/MissionCreateForm.fxml", null);
        });
        lihatSemuaMisiButton.setOnAction(e -> {
            if (mainDashboardController != null)
                // Mengirimkan konteks "ALL_MISSIONS" ke MissionListViewController
                mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", "ALL_MISSIONS");
        });
        reviewDraftButton.setOnAction(e -> {
            if (mainDashboardController != null)
                // Mengirimkan konteks "DRAFT_REVIEW_DIREKTUR" ke MissionListViewController
                mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", "DRAFT_REVIEW_DIREKTUR"); 
        });
        bukaAntrianEvaluasiButton.setOnAction(e -> {
            if (mainDashboardController != null)
                mainDashboardController.showEvaluationForm(null); // Membuka form evaluasi kosong
        });
        manajemenTargetButton.setOnAction(e -> {
            if (mainDashboardController != null)
                mainDashboardController.showTargetManagementView();
        });
    }
}
package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.ServiceLocator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

public class MissionCreateFormController extends BaseController {

    @FXML private TextField judulField;
    @FXML private TextArea tujuanArea;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<Target> targetComboBox;
    @FXML private Button tambahTargetButton;
    @FXML private TextArea analisisRisikoArea;
    @FXML private TextField jenisOperasiField;
    @FXML private TextField lokasiField;
    @FXML private Button simpanDraftButton;
    @FXML private Button batalButton;
    @FXML private Label statusMessageLabel;

    private MissionManager missionManager;
    private TargetManager targetManager;
    private User currentUser;

    public void loadInitialData() {
        this.currentUser = Main.authManager.getCurrentUser();
        if (this.targetManager != null) {
            loadTargetsToComboBox();
        } else {
            System.err.println("MissionCreateFormController: TargetManager null. Tidak bisa memuat daftar target.");
            showStatus(statusMessageLabel, "Error: Tidak bisa memuat daftar target.", true);
            targetComboBox.setDisable(true);
            tambahTargetButton.setDisable(true);
        }
        statusMessageLabel.setManaged(false);
        statusMessageLabel.setVisible(false);
    }

    @FXML
    public void initialize() {
        this.missionManager = ServiceLocator.getMissionManager();
        this.targetManager = ServiceLocator.getTargetManager();
        
        targetComboBox.setConverter(new StringConverter<Target>() {
            @Override
            public String toString(Target target) {
                return target != null ? target.getNama() + " (Tipe: " + target.getTipe().getDisplayName() + ")" : null;
            }
            @Override
            public Target fromString(String string) { return null; }
        });
        targetComboBox.setPlaceholder(new Label("Memuat daftar target..."));
        
        // Initialize data
        loadInitialData();
    }

    private void loadTargetsToComboBox() {
        if (targetManager == null) return;
        try {
            List<Target> targets = targetManager.getAllTargets();
            ObservableList<Target> observableTargets = FXCollections.observableArrayList(targets);
            targetComboBox.setItems(observableTargets);
            if (observableTargets.isEmpty()) {
                targetComboBox.setPromptText("Belum ada target. Klik (+) untuk buat baru.");
            } else {
                targetComboBox.setPromptText("Pilih target dari daftar...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(statusMessageLabel, "Gagal memuat daftar target: " + e.getMessage(), true);
            targetComboBox.setPromptText("Error memuat target!");
        }
    }

    @FXML
    private void handleTambahTargetButton(ActionEvent event) {
        if (mainDashboardController != null) {
            mainDashboardController.showTargetManagementView();
        } else {
            showStatus(statusMessageLabel, "Error internal: MainDashboardController tidak tersedia.", true);
        }
    }

    @FXML
    private void handleSimpanDraftButton(ActionEvent event) {
        String judul = judulField.getText().trim();
        String tujuan = tujuanArea.getText().trim();
        Target selectedTarget = targetComboBox.getValue();
        String deskripsi = deskripsiArea.getText().trim();
        String analisisRisiko = analisisRisikoArea.getText().trim();
        String jenisOperasi = jenisOperasiField.getText().trim();
        String lokasi = lokasiField.getText().trim();

        // Validasi input
        if (judul.isEmpty()) {
            showStatus(statusMessageLabel, "Judul Misi wajib diisi!", true);
            judulField.requestFocus(); return;
        }
        if (tujuan.isEmpty()) {
            showStatus(statusMessageLabel, "Tujuan Utama Misi wajib diisi!", true);
            tujuanArea.requestFocus(); return;
        }
        if (selectedTarget == null) {
            showStatus(statusMessageLabel, "Target Utama Misi wajib dipilih atau dibuat!", true);
            targetComboBox.requestFocus(); return;
        }

        if (currentUser == null) {
            showStatus(statusMessageLabel, "Error: Sesi pengguna tidak valid. Silakan login ulang.", true); return;
        }
        if (missionManager == null) {
             showStatus(statusMessageLabel, "Error: MissionManager belum siap untuk menyimpan.", true); return;
        }

        Mission newMission = new Mission();
        newMission.setJudul(judul);
        newMission.setTujuan(tujuan);
        newMission.setDeskripsi(deskripsi);
        newMission.setTargetId(selectedTarget.getId());
        newMission.setAnalisisRisiko(analisisRisiko);
        newMission.setJenisOperasi(jenisOperasi);
        newMission.setLokasi(lokasi);
        newMission.setStatus(MissionStatus.DRAFT_ANALIS);

        Optional<Mission> createdMissionOpt = missionManager.submitNewDraft(newMission);

        if (createdMissionOpt.isPresent()) {
            showStatus(statusMessageLabel, "Draft Misi '" + createdMissionOpt.get().getJudul() + "' berhasil disimpan dengan status DRAFT ANALIS!", false);
            clearForm();
            if (mainDashboardController != null) {
                mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", MissionStatus.DRAFT_ANALIS.name());
            }
        } else {
            showStatus(statusMessageLabel, "Gagal menyimpan draft misi. Periksa detail error di konsol sistem.", true);
        }
    }
    
    @FXML
    private void handleBatalButton(ActionEvent event) {
        clearForm();
        showStatus(statusMessageLabel, "Pembuatan draft misi dibatalkan. Semua input telah dibersihkan.", false);
        if(mainDashboardController != null) {
           mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole());
        }
    }

    private void clearForm() {
        judulField.clear();
        tujuanArea.clear();
        deskripsiArea.clear();
        targetComboBox.getSelectionModel().clearSelection();
        if (targetComboBox.getItems().isEmpty()) {
            targetComboBox.setPromptText("Belum ada target. Klik (+) untuk buat baru.");
        } else {
            targetComboBox.setPromptText("Pilih target dari daftar...");
        }
        analisisRisikoArea.clear();
        jenisOperasiField.clear();
        lokasiField.clear();
    }
}
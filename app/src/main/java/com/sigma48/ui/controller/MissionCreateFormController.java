package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.Target;
import com.sigma48.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*; // Import semua control jika banyak
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

public class MissionCreateFormController {

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
    private MainDashboardController mainDashboardController; // Untuk memanggil dialog target
    private User currentUser;

    // Dipanggil oleh MainDashboardController setelah FXML dimuat
    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setManagers(MissionManager missionManager, TargetManager targetManager) {
        this.missionManager = missionManager;
        this.targetManager = targetManager;
    }

    // Dipanggil oleh MainDashboardController setelah manager di-set
    public void loadInitialData() {
        this.currentUser = Main.authManager.getCurrentUser();
        if (this.targetManager != null) {
            loadTargetsToComboBox();
        } else {
            System.err.println("MissionCreateFormController: TargetManager null. Tidak bisa memuat daftar target.");
            showStatusMessage("Error: Tidak bisa memuat daftar target.", true);
            targetComboBox.setDisable(true);
            tambahTargetButton.setDisable(true);
        }
        statusMessageLabel.setManaged(false);
        statusMessageLabel.setVisible(false);
    }

    @FXML
    public void initialize() {
        // Konfigurasi ComboBox Target
        targetComboBox.setConverter(new StringConverter<Target>() {
            @Override
            public String toString(Target target) {
                return target != null ? target.getNama() + " (Tipe: " + target.getTipe().getDisplayName() + ")" : null;
            }
            @Override
            public Target fromString(String string) { return null; } // Tidak diperlukan untuk pemilihan
        });
        targetComboBox.setPlaceholder(new Label("Memuat daftar target..."));
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
            showStatusMessage("Gagal memuat daftar target: " + e.getMessage(), true);
            targetComboBox.setPromptText("Error memuat target!");
        }
    }

    @FXML
    private void handleTambahTargetButton(ActionEvent event) {
        if (mainDashboardController != null) {
            // Langsung panggil metode untuk menampilkan view Manajemen Target
            mainDashboardController.showTargetManagementView();
        } else {
            showStatusMessage("Error internal: MainDashboardController tidak tersedia.", true);
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
            showStatusMessage("Judul Misi wajib diisi!", true);
            judulField.requestFocus(); return;
        }
        if (tujuan.isEmpty()) {
            showStatusMessage("Tujuan Utama Misi wajib diisi!", true);
            tujuanArea.requestFocus(); return;
        }
        if (selectedTarget == null) {
            showStatusMessage("Target Utama Misi wajib dipilih atau dibuat!", true);
            targetComboBox.requestFocus(); return;
        }

        if (currentUser == null) {
            showStatusMessage("Error: Sesi pengguna tidak valid. Silakan login ulang.", true); return;
        }
        if (missionManager == null) {
             showStatusMessage("Error: MissionManager belum siap untuk menyimpan.", true); return;
        }

        Mission newMission = new Mission(); // ID dan createdAt di-generate otomatis
        newMission.setJudul(judul);
        newMission.setTujuan(tujuan);
        newMission.setDeskripsi(deskripsi);
        newMission.setTargetId(selectedTarget.getId());
        newMission.setAnalisisRisiko(analisisRisiko);
        newMission.setJenisOperasi(jenisOperasi);
        newMission.setLokasi(lokasi);
        newMission.setStatus(MissionStatus.DRAFT_ANALIS); // Status awal untuk draft
        // newMission.setCreatedByUserId(currentUser.getId()); // Jika ada field pencatat di Mission.java

        Optional<Mission> createdMissionOpt = missionManager.submitNewDraft(newMission);

        if (createdMissionOpt.isPresent()) {
            showStatusMessage("Draft Misi '" + createdMissionOpt.get().getJudul() + "' berhasil disimpan dengan status DRAFT ANALIS!", false);
            clearForm();
            // Opsional: Navigasi ke daftar misi atau dashboard
            if (mainDashboardController != null) {
                // Mengarahkan ke daftar misi yang memfilter DRAFT_ANALIS jika ada konteksnya
                mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", MissionStatus.DRAFT_ANALIS.name());
            }
        } else {
            showStatusMessage("Gagal menyimpan draft misi. Periksa detail error di konsol sistem.", true);
        }
    }
    
    @FXML
    private void handleBatalButton(ActionEvent event) {
        clearForm();
        showStatusMessage("Pembuatan draft misi dibatalkan. Semua input telah dibersihkan.", false);
        // Bisa juga navigasi kembali jika form ini bukan tampilan default setelah login
        // if(mainDashboardController != null) {
        //    mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole());
        // }
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

    private void showStatusMessage(String message, boolean isError) {
        statusMessageLabel.setText(message);
        statusMessageLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusMessageLabel.setManaged(true);
        statusMessageLabel.setVisible(true);

        PauseTransition visiblePause = new PauseTransition(Duration.seconds(5)); // Pesan tampil lebih lama
        visiblePause.setOnFinished(ev -> {
            statusMessageLabel.setManaged(false);
            statusMessageLabel.setVisible(false);
        });
        visiblePause.play();
    }
}
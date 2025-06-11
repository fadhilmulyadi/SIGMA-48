package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator;
import com.sigma48.manager.EvaluationManager;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.UserManager;
import com.sigma48.model.Evaluation;
import com.sigma48.model.Mission;
import com.sigma48.model.MissionStatus;
import com.sigma48.model.OperationEffectiveness;
import com.sigma48.model.User; // Termasuk Agent
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.model.Role; // Untuk validasi peran evaluator

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationFormController extends BaseController{

    @FXML private Label evaluatorInfoLabel;
    @FXML private ComboBox<Mission> missionComboBox;
    @FXML private ComboBox<User> agentComboBox; // Akan diisi dengan User yang berperan Agen
    @FXML private ComboBox<OperationEffectiveness> efektivitasComboBox;
    @FXML private TextArea catatanMentalArea;
    @FXML private TextArea kondisiFisikArea;
    @FXML private TextArea catatanUmumArea;
    @FXML private Button simpanEvalButton;
    @FXML private Button batalEvalButton;
    @FXML private Label statusEvalLabel;

    private EvaluationManager evaluationManager;
    private MissionManager missionManager;
    private UserManager userManager;
    private User currentUser; // Pengguna yang melakukan evaluasi (Direktur/Komandan)
    
    private MainDashboardController mainDashboardController;
    private Mission preselectedMission; // Misi yang mungkin sudah dipilih sebelumnya

    // Dipanggil oleh MainDashboardController saat view ini dimuat
    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setSelectedMission(Mission mission) {
        this.preselectedMission = mission;
    }

    // Panggil ini dari MainDashboardController setelah semua dependensi di-set
    public void loadInitialData() {
        this.evaluationManager = ServiceLocator.getEvaluationManager();
        this.missionManager = ServiceLocator.getMissionManager();
        this.userManager = ServiceLocator.getUserManager();
        this.currentUser = Main.authManager.getCurrentUser();
        if (currentUser != null) {
            evaluatorInfoLabel.setText(currentUser.getUsername() + " (" + currentUser.getRole().getDisplayName() + ")");
        } else {
            evaluatorInfoLabel.setText("EVALUATOR TIDAK DIKENALI");
            // Sebaiknya form ini tidak bisa diakses jika tidak ada user login
            simpanEvalButton.setDisable(true);
        }

        if (evaluationManager == null || missionManager == null || userManager == null) {
            showStatus("Error: Manager belum diinisialisasi dengan benar.", true);
            simpanEvalButton.setDisable(true);
            missionComboBox.setDisable(true);
            agentComboBox.setDisable(true);
            efektivitasComboBox.setDisable(true);
            return;
        }
        
        setupComboBoxes(); // Setup konverter dan listener
        loadMissionsToComboBox(); // Isi ComboBox Misi
        efektivitasComboBox.setItems(FXCollections.observableArrayList(OperationEffectiveness.values()));

        statusEvalLabel.setManaged(false);
        statusEvalLabel.setVisible(false);

        if (preselectedMission != null) {
            missionComboBox.setValue(preselectedMission);
            // Jika misi sudah dipilih, ComboBox misi bisa di-disable agar tidak diubah
            // missionComboBox.setDisable(true); 
            // Atau tetap enable jika evaluator boleh memilih misi lain
        }
    }
    
    private void setupComboBoxes() {
        missionComboBox.setConverter(new StringConverter<Mission>() {
            @Override public String toString(Mission mission) { 
                return mission != null ? mission.getJudul() + " (Status: " + mission.getStatus().getDisplayName() + ")" : null; 
            }
            @Override public Mission fromString(String string) { return null; }
        });

        missionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateAgentComboBox(newSelection);
                agentComboBox.setDisable(false);
                agentComboBox.setPromptText("Pilih Agen (Opsional)...");
            } else {
                agentComboBox.getItems().clear();
                agentComboBox.setDisable(true);
                agentComboBox.setPromptText("Pilih misi terlebih dahulu...");
            }
        });

        agentComboBox.setConverter(new StringConverter<User>() {
            @Override public String toString(User user) { 
                return user != null ? user.getUsername() + " (ID: " + user.getId() + ")" : null; 
            }
            @Override public User fromString(String string) { return null; }
        });
        agentComboBox.setDisable(true); // Awalnya disable sampai misi dipilih
    }

    private void loadMissionsToComboBox() {
        // Hanya tampilkan misi yang sudah selesai atau sedang aktif untuk dievaluasi
        List<Mission> evaluableMissions = missionManager.getAll().stream()
                .filter(m -> m.getStatus() == MissionStatus.ACTIVE || 
                             m.getStatus() == MissionStatus.COMPLETED ||
                             m.getStatus() == MissionStatus.FAILED ||
                             m.getStatus() == MissionStatus.READY_FOR_BRIEFING) // Mungkin juga yang siap briefing
                .sorted(Comparator.comparing(Mission::getUpdatedAt).reversed()) // Terbaru dulu
                .collect(Collectors.toList());
        missionComboBox.setItems(FXCollections.observableArrayList(evaluableMissions));
        if (evaluableMissions.isEmpty()){
            missionComboBox.setPromptText("Tidak ada misi yang bisa dievaluasi.");
        } else {
            missionComboBox.setPromptText("Pilih Misi...");
        }
    }

    private void populateAgentComboBox(Mission selectedMission) {
        agentComboBox.getItems().clear();
        if (selectedMission.getAssignedAgents() != null && !selectedMission.getAssignedAgents().isEmpty() && userManager != null) {
            List<User> agentsInMission = new ArrayList<>();
            for (String agentId : selectedMission.getAssignedAgents()) {
                userManager.findUserById(agentId).ifPresent(user -> {
                    if (user.getRole() == Role.AGEN_LAPANGAN) { // Pastikan hanya agen
                        agentsInMission.add(user);
                    }
                });
            }
            agentComboBox.setItems(FXCollections.observableArrayList(agentsInMission));
            if(agentsInMission.isEmpty()) {
                agentComboBox.setPromptText("Tidak ada agen di misi ini.");
                // agentComboBox.setDisable(true); // Biarkan enable agar user tahu tidak ada pilihan
            } else {
                agentComboBox.setPromptText("Pilih Agen (Opsional)...");
            }
        } else {
            agentComboBox.setPromptText("Tidak ada agen ditugaskan ke misi ini.");
            // agentComboBox.setDisable(true);
        }
    }

    @FXML
    private void handleSaveEvaluationButton(ActionEvent event) {
        Mission selectedMission = missionComboBox.getValue();
        User selectedAgent = agentComboBox.getValue(); // Bisa null (jika tidak ada agen dipilih atau ComboBox kosong)
        OperationEffectiveness efektivitas = efektivitasComboBox.getValue();
        String catatanMental = catatanMentalArea.getText().trim();
        String kondisiFisik = kondisiFisikArea.getText().trim();
        String catatanUmum = catatanUmumArea.getText().trim();

        if (currentUser == null) {
            showStatus("Error: Sesi evaluator tidak valid.", true); return;
        }
        if (selectedMission == null) {
            showStatus("Misi yang dievaluasi wajib dipilih!", true); missionComboBox.requestFocus(); return;
        }
        if (efektivitas == null) {
            showStatus("Tingkat Efektivitas wajib dipilih!", true); efektivitasComboBox.requestFocus(); return;
        }
        if (catatanUmum.isEmpty()) {
            showStatus("Catatan Umum Evaluator wajib diisi!", true); catatanUmumArea.requestFocus(); return;
        }
        if (evaluationManager == null) {
             showStatus("Error: EvaluationManager belum siap.", true); return;
        }


        String agentId = (selectedAgent != null) ? selectedAgent.getId() : null;

        Optional<Evaluation> savedEvaluation = evaluationManager.submitEvaluation(
                selectedMission.getId(),
                agentId, // Bisa null
                currentUser.getId(),
                efektivitas,
                catatanMental,
                kondisiFisik,
                catatanUmum
        );

        if (savedEvaluation.isPresent()) {
            showStatus("Evaluasi untuk misi '" + selectedMission.getJudul() + "' berhasil disimpan!", false);
            clearForm();
            // Opsional: navigasi ke daftar evaluasi atau detail misi
            if (mainDashboardController != null) {
                // mainDashboardController.showMissionPlanningView(selectedMission); // Kembali ke detail misi yg sama
                mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole()); // Kembali ke dashboard evaluator
            }
        } else {
            showStatus("Gagal menyimpan evaluasi. Periksa detail error di konsol sistem.", true);
        }
    }

    @FXML
    private void handleCancelEvaluationButton(ActionEvent event) {
        clearForm();
        showStatus("Pembuatan evaluasi dibatalkan.", false);
        if (mainDashboardController != null && currentUser != null) {
            mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole()); // Kembali ke dashboard
        }
    }

    private void clearForm() {
        missionComboBox.getSelectionModel().clearSelection();
        // agentComboBox.getItems().clear(); // Dikosongkan oleh listener missionComboBox
        // agentComboBox.setDisable(true);
        efektivitasComboBox.getSelectionModel().clearSelection();
        catatanMentalArea.clear();
        kondisiFisikArea.clear();
        catatanUmumArea.clear();
        if(preselectedMission != null) {
            missionComboBox.setValue(preselectedMission);
        } else {
            missionComboBox.setPromptText("Pilih Misi...");
        }
        agentComboBox.setPromptText("Pilih Agen (Opsional)...");
    }

    private void showStatus(String message, boolean isError) {
        statusEvalLabel.setText(message);
        statusEvalLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusEvalLabel.setManaged(true);
        statusEvalLabel.setVisible(true);

        PauseTransition visiblePause = new PauseTransition(Duration.seconds(4));
        visiblePause.setOnFinished(ev -> {
            statusEvalLabel.setManaged(false);
            statusEvalLabel.setVisible(false);
        });
        visiblePause.play();
    }
}
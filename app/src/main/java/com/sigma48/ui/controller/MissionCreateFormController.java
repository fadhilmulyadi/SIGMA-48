package com.sigma48.ui.controller;

import com.sigma48.Main; // Akses AuthManager untuk createdByUserId
import com.sigma48.dao.MissionDao; // Atau via ServiceLocator/DI
import com.sigma48.dao.TargetDao;   // Atau via ServiceLocator/DI
import com.sigma48.dao.UserDao;
import com.sigma48.manager.MissionManager;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.Target;
import com.sigma48.model.User;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public class MissionCreateFormController {

    @FXML
    private TextField judulField;
    @FXML
    private TextArea tujuanArea;
    @FXML
    private TextArea deskripsiArea;
    @FXML
    private ComboBox<Target> targetComboBox; // Akan menampilkan objek Target, butuh toString() yang baik di Target.java
    @FXML
    private TextArea analisisRisikoArea;
    @FXML
    private TextField jenisOperasiField;
    @FXML
    private TextField lokasiField;
    
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label statusMessageLabel;

    private MissionManager missionManager;
    private TargetManager targetManager;

    @FXML
    public void initialize() {
        this.missionManager = new MissionManager(new MissionDao(), new TargetDao(), new UserDao());
        this.targetManager = new TargetManager(new TargetDao());

        loadTargetsToComboBox();
        statusMessageLabel.setManaged(false);
        statusMessageLabel.setVisible(false);
    }

    private void loadTargetsToComboBox() {
        List<Target> targets = targetManager.getAllTargets();
        targetComboBox.setItems(FXCollections.observableArrayList(targets));
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        String judul = judulField.getText();
        String tujuan = tujuanArea.getText();
        String deskripsi = deskripsiArea.getText();
        Target selectedTarget = targetComboBox.getValue();
        String analisisRisiko = analisisRisikoArea.getText();
        String jenisOperasi = jenisOperasiField.getText();
        String lokasi = lokasiField.getText();

        if (judul.isEmpty() || tujuan.isEmpty() || selectedTarget == null) {
            showStatusMessage("Judul, Tujuan, dan Target harus diisi!", true);
            return;
        }

        User currentUser = Main.authManager.getCurrentUser();
        if (currentUser == null) {
            showStatusMessage("Error: Tidak ada pengguna yang login untuk membuat misi.", true);
            return;
        }

        Optional<Mission> createdMission = missionManager.createDraftMission(
                judul, tujuan, deskripsi, selectedTarget.getId()
        );

        if (createdMission.isPresent()) {
            Mission mission = createdMission.get();
            mission.setAnalisisRisiko(analisisRisiko);
            mission.setJenisOperasi(jenisOperasi);
            mission.setLokasi(lokasi);
            boolean updated = missionManager.missionDao.saveMission(mission); // save via DAO setelah update

            if (updated) {
                showStatusMessage("Draft Misi '" + mission.getJudul() + "' berhasil disimpan!", false);
                clearForm();
            } else {
                showStatusMessage("Draft Misi berhasil dibuat, tapi gagal menyimpan detail tambahan.", true);
            }
            
        } else {
            showStatusMessage("Gagal menyimpan draft misi. Periksa error di konsol.", true);
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        clearForm();
        System.out.println("Pembuatan misi dibatalkan.");
        showStatusMessage("Pembuatan misi dibatalkan.", false);

    }

    private void clearForm() {
        judulField.clear();
        tujuanArea.clear();
        deskripsiArea.clear();
        targetComboBox.getSelectionModel().clearSelection();
        analisisRisikoArea.clear();
        jenisOperasiField.clear();
        lokasiField.clear();
    }

    private void showStatusMessage(String message, boolean isError) {
        statusMessageLabel.setText(message);
        statusMessageLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusMessageLabel.setManaged(true);
        statusMessageLabel.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            statusMessageLabel.setManaged(false);
            statusMessageLabel.setVisible(false);
        });
        delay.play();
    }
}
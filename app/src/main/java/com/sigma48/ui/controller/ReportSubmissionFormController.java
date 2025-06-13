package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator; // <-- PERUBAHAN: Import ServiceLocator
import com.sigma48.manager.ReportManager;
import com.sigma48.model.Report;
import com.sigma48.model.Role;
import com.sigma48.model.User;
import com.sigma48.ui.controller.base.BaseController; // <-- PERUBAHAN: Import BaseController

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportSubmissionFormController extends BaseController {

    @FXML private Label missionIdLabel;
    @FXML private Label missionJudulLabel;
    @FXML private TextArea isiLaporanArea;
    @FXML private TextField locationTextField;
    @FXML private Button tambahLampiranButton;
    @FXML private Button hapusLampiranButton;
    @FXML private ListView<String> lampiranListView;
    @FXML private Button kirimLaporanButton;
    @FXML private Button batalButton;
    @FXML private Label statusSubmitLabel;

    private ReportManager reportManager;
    private User currentUser;
    private String currentMissionId;
    private final ObservableList<File> attachedFilesObjectList = FXCollections.observableArrayList();
    private final ObservableList<String> attachedFileNamesList = FXCollections.observableArrayList();

    public void setMissionToReport(String missionId, String missionJudul) {
        this.currentMissionId = missionId;
        missionIdLabel.setText(missionId != null ? missionId : "N/A");
        missionJudulLabel.setText(missionJudul != null ? missionJudul : "Judul Misi Tidak Diketahui");
    }

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
        this.reportManager = ServiceLocator.getReportManager();
        isiLaporanArea.setWrapText(true);
        lampiranListView.setItems(attachedFileNamesList);
        hapusLampiranButton.disableProperty().bind(
            lampiranListView.getSelectionModel().selectedItemProperty().isNull()
        );
        statusSubmitLabel.setManaged(false);
        statusSubmitLabel.setVisible(false);
    }

    @FXML
    private void handleAddAttachmentButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Lampiran (Bisa Lebih Dari Satu)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg"));
        
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog((Stage) tambahLampiranButton.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                if (attachedFilesObjectList.stream().noneMatch(existingFile -> existingFile.getAbsolutePath().equals(file.getAbsolutePath()))) {
                    attachedFilesObjectList.add(file);
                    attachedFileNamesList.add(file.getName() + " (" + String.format("%.2f KB", file.length() / 1024.0) + ")");
                } else {
                    super.showStatus(statusSubmitLabel, "File '" + file.getName() + "' sudah ada di daftar lampiran.", true);
                }
            }
        }
    }

    @FXML
    private void handleRemoveAttachmentButtonAction(ActionEvent event) {
        int selectedIndex = lampiranListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < attachedFilesObjectList.size()) {
            attachedFilesObjectList.remove(selectedIndex);
            attachedFileNamesList.remove(selectedIndex);
        }
    }

    @FXML
    private void handleSubmitReportButtonAction(ActionEvent event) {
        String isi = isiLaporanArea.getText().trim();
        String lokasi = locationTextField.getText().trim();

        if (isi.isEmpty() || lokasi.isEmpty()) {
            super.showStatus(statusSubmitLabel, "Isi laporan dan lokasi tidak boleh kosong.", true);
            return;
        }
                
        Optional<Report> submittedReportOpt = reportManager.submitReport(
                currentMissionId,
                currentUser.getId(),
                currentUser.getRole(),
                isi,
                new ArrayList<>(),
                lokasi
        );

        if (!submittedReportOpt.isPresent()) {
            super.showStatus(statusSubmitLabel, "Gagal membuat entri laporan awal. Periksa konsol.", true);
            return;
        }

        Report submittedReport = submittedReportOpt.get();
        if (!attachedFilesObjectList.isEmpty()) {
            try {
                Path destDir = Paths.get("data", "attachments", submittedReport.getReportId());
                Files.createDirectories(destDir);
                List<String> realAttachmentPaths = new ArrayList<>();

                for (File sourceFile : attachedFilesObjectList) {
                    Path destPath = destDir.resolve(sourceFile.getName());
                    Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    realAttachmentPaths.add(destPath.toString());
                }

                submittedReport.setLampiran(realAttachmentPaths);
                
                if (!reportManager.saveReport(submittedReport)) {
                     super.showStatus(statusSubmitLabel, "Gagal menyimpan path lampiran.", true);
                     return;
                }
            } catch (IOException e) {
                super.showStatus(statusSubmitLabel, "Error saat menyalin file lampiran: " + e.getMessage(), true);
                e.printStackTrace();
                return;
            }
        }

        super.showStatus(statusSubmitLabel, "Laporan berhasil dikirim!", false);
        clearForm();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            if (mainDashboardController != null) {
                mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole());
            }
        });
        delay.play();
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        clearForm();
        super.showStatus(statusSubmitLabel, "Pengiriman laporan dibatalkan.", false);
        if (mainDashboardController != null && currentUser != null) {
            if (currentUser.getRole() == Role.AGEN_LAPANGAN) {
                mainDashboardController.loadView("/com/sigma48/fxml/AgentMissionView.fxml", null);
            } else {
                mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole());
            }
        }
    }

    private void clearForm() {
        isiLaporanArea.clear();
        locationTextField.clear();
        attachedFilesObjectList.clear();
        attachedFileNamesList.clear();
    }
}
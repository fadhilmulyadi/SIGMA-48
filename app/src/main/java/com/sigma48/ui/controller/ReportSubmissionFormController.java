package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.ReportManager;
import com.sigma48.model.Role;
import com.sigma48.model.User;
import com.sigma48.model.Report; // Untuk tipe data Optional dari submitReport

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportSubmissionFormController {

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
    private ObservableList<File> attachedFilesObjectList = FXCollections.observableArrayList();
    private ObservableList<String> attachedFileNamesList = FXCollections.observableArrayList();
    private MainDashboardController mainDashboardController;

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    public void setMissionToReport(String missionId, String missionJudul) {
        this.currentMissionId = missionId;
        missionIdLabel.setText(missionId != null ? missionId : "N/A");
        missionJudulLabel.setText(missionJudul != null ? missionJudul : "Judul Misi Tidak Diketahui");
    }

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
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
                new FileChooser.ExtensionFilter("Semua File Didukung", "*.png", "*.jpg", "*.jpeg", "*.mp3", "*.m4a", "*.wav", "*.pdf"));
        
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog((Stage) tambahLampiranButton.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                // Cek duplikasi berdasarkan path absolut untuk menghindari file yang sama ditambahkan berkali-kali
                if (attachedFilesObjectList.stream().noneMatch(existingFile -> existingFile.getAbsolutePath().equals(file.getAbsolutePath()))) {
                    attachedFilesObjectList.add(file);
                    attachedFileNamesList.add(file.getName() + " (" + String.format("%.2f KB", file.length() / 1024.0) + ")");
                } else {
                    showStatus("File '" + file.getName() + "' sudah ada di daftar lampiran.", true);
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
            showStatus("Isi laporan dan lokasi tidak boleh kosong.", true);
            return;
        }
                
        Optional<Report> submittedReportOpt = reportManager.submitReport(
                currentMissionId,
                currentUser.getId(),
                currentUser.getRole(),
                isi,
                new ArrayList<>(), // Kirim list kosong dulu
                lokasi
        );

        if (!submittedReportOpt.isPresent()) {
            showStatus("Gagal membuat entri laporan awal. Periksa konsol.", true);
            return;
        }

        Report submittedReport = submittedReportOpt.get();
        List<String> realAttachmentPaths = new ArrayList<>();

        if (!attachedFilesObjectList.isEmpty()) {
            try {
                Path destDir = Paths.get("data", "attachments", submittedReport.getReportId());
                Files.createDirectories(destDir);

                for (File sourceFile : attachedFilesObjectList) {
                    Path destPath = destDir.resolve(sourceFile.getName());
                    Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    realAttachmentPaths.add(destPath.toString());
                }

                submittedReport.setLampiran(realAttachmentPaths);
                
                boolean finalSaveSuccess = reportManager.saveReport(submittedReport);
                if (!finalSaveSuccess) {
                     showStatus("Gagal menyimpan path lampiran. Laporan tersimpan tanpa lampiran.", true);
                     return;
                }

            } catch (IOException e) {
                showStatus("Error saat menyalin file lampiran: " + e.getMessage(), true);
                e.printStackTrace();
                return;
            }
        }

        showStatus("Laporan berhasil dikirim!", false);
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
        showStatus("Pengiriman laporan dibatalkan.", false);
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

    private void showStatus(String message, boolean isError) {
        statusSubmitLabel.setText(message);
        statusSubmitLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusSubmitLabel.setManaged(true);
        statusSubmitLabel.setVisible(true);

        PauseTransition visiblePause = new PauseTransition(Duration.seconds(4));
        visiblePause.setOnFinished(ev -> {
            statusSubmitLabel.setManaged(false);
            statusSubmitLabel.setVisible(false);
        });
        visiblePause.play();
    }
}
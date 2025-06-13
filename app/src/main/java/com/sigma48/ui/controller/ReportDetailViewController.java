package com.sigma48.ui.controller;

import com.sigma48.manager.ReportManager;
import com.sigma48.model.Report;
import com.sigma48.model.Role;
import com.sigma48.model.User;
import com.sigma48.util.SoundUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportDetailViewController {

    @FXML private BorderPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Label reportIdLabel;
    @FXML private Label agentNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label locationLabel;
    @FXML private TextArea isiLaporanTextArea;
    @FXML private ListView<File> attachmentsListView;
    @FXML private Button simpanPerubahanButton;
    @FXML private Button kembaliButton;

    private MainDashboardController mainDashboardController;
    private Report currentReport;
    private User currentUser;
    private ReportManager reportManager;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss");

    @FXML
    public void initialize() {
        this.reportManager = new ReportManager(new com.sigma48.dao.ReportDao(), null); // missionManager bisa null jika tidak ada update status
        isiLaporanTextArea.setWrapText(true); 
        setupAttachmentsListView();
    }

    public void initializeView(Report report, User currentUser, MainDashboardController mainController) {
        this.currentReport = report;
        this.currentUser = currentUser;
        this.mainDashboardController = mainController;
        populateReportDetails();
        setupPermissions();
    }

    private void populateReportDetails() {
        if (currentReport == null) return;

        titleLabel.setText("DETAIL LAPORAN: " + currentReport.getMissionId());
        reportIdLabel.setText("ID LAPORAN: " + currentReport.getReportId());
        agentNameLabel.setText("OLEH: " + currentReport.getUserId()); 
        dateTimeLabel.setText("WAKTU: " + currentReport.getWaktuLapor().format(formatter));
        locationLabel.setText("LOKASI: " + (currentReport.getLokasi() != null ? currentReport.getLokasi() : "N/A"));
        isiLaporanTextArea.setText(currentReport.getIsi());

        List<File> attachmentFiles = currentReport.getLampiran().stream()
                .map(File::new)
                .filter(File::exists)
                .collect(Collectors.toList());
        attachmentsListView.getItems().setAll(attachmentFiles);
    }

    private void setupPermissions() {
        boolean isAgentWhoReported = currentUser.getRole() == Role.AGEN_LAPANGAN && currentUser.getId().equals(currentReport.getUserId());
        isiLaporanTextArea.setEditable(isAgentWhoReported);
        simpanPerubahanButton.setVisible(isAgentWhoReported);
        simpanPerubahanButton.setManaged(isAgentWhoReported);
    }

    private void setupAttachmentsListView() {
        attachmentsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        attachmentsListView.setOnMouseClicked(this::handleAttachmentClick);
    }

    private void handleAttachmentClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            File selectedFile = attachmentsListView.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                openMediaViewer(selectedFile, attachmentsListView.getItems());
            }
        }
    }

    private void openMediaViewer(File selectedFile, List<File> allAttachments) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/MediaViewer.fxml"));
            Parent root = loader.load();

            MediaViewerController controller = loader.getController();
            controller.setupViewer(allAttachments, selectedFile);

            Stage mediaStage = new Stage();

            mediaStage.initStyle(StageStyle.UNDECORATED);
            mediaStage.initModality(Modality.APPLICATION_MODAL);
            mediaStage.initOwner(rootPane.getScene().getWindow());

            Scene scene = new Scene(root, 900, 650);
            scene.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
            
            mediaStage.setScene(scene);

            mediaStage.centerOnScreen();
            mediaStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Membuka Media", "Terjadi kesalahan saat mencoba membuka media player.");
        }
    }

    @FXML
    private void handleSimpanPerubahan() {
        currentReport.setIsi(isiLaporanTextArea.getText());
        if (reportManager.saveReport(currentReport)) { // Menggunakan saveReport dari DAO
            showSuccessDialog("Sukses", "Perubahan laporan berhasil disimpan.");
        } else {
            showErrorDialog("Gagal Menyimpan", "Gagal menyimpan perubahan ke file reports.json.");
        }
    }

    @FXML
    private void handleKembali() {
        if (mainDashboardController != null) {
            mainDashboardController.showKomandanOverview();
        }
    }

    private void showSuccessDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlertDialog(alert);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlertDialog(alert);
        alert.showAndWait();
    }
    
    private void styleAlertDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
    }
}
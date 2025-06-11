package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Mission;
import com.sigma48.model.Role;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import com.sigma48.ui.controller.base.BaseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DossierViewController extends BaseController {

    // --- FXML Components ---
    @FXML private Button backButton;
    @FXML private Label targetIdLabel;
    @FXML private ListView<File> evidenceListView;
    @FXML private Button addEvidenceButton;
    @FXML private Button deleteEvidenceButton;
    @FXML private ImageView targetPhotoImageView;
    @FXML private Label targetNameLabel;
    @FXML private Label targetStatusLabel;
    @FXML private Label targetTipeLabel;
    @FXML private Label targetLokasiLabel;
    @FXML private TextArea intelTextArea;
    @FXML private Label contentTitleLabel;
    @FXML private Label contentSubtitleLabel;
    @FXML private ScrollPane viewerScrollPane;
    @FXML private VBox imageViewerPane;
    @FXML private ImageView dossierImageView;
    @FXML private HBox imageNavBox;
    @FXML private Button prevImageButton;
    @FXML private Button nextImageButton;
    @FXML private Label imageCounterLabel;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomResetButton;

    private TargetManager targetManager;
    private Mission missionToReturnTo;
    private String returnView;
    private Target currentTarget;
    private User currentUser;
    private final ObservableList<File> evidenceFiles = FXCollections.observableArrayList();
    private List<File> imageFiles;
    private int currentImageIndex = 0;
    private double currentZoomFactor = 1.0;

    @FXML
    public void initialize() {
        // Inisialisasi manager dan user dari sumber yang benar
        this.targetManager = ServiceLocator.getTargetManager();
        this.currentUser = Main.authManager.getCurrentUser();

        evidenceListView.setItems(evidenceFiles);
        evidenceListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        evidenceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayEvidence(newVal);
            }
        });
        
        // Disable tombol hapus jika tidak ada item yang dipilih
        if (deleteEvidenceButton != null) {
            deleteEvidenceButton.disableProperty().bind(evidenceListView.getSelectionModel().selectedItemProperty().isNull());
        }

        viewerScrollPane.setFitToWidth(true);
        viewerScrollPane.setFitToHeight(true);

        // BARU: Tambahkan event handler untuk zoom dengan mouse wheel/pad
        viewerScrollPane.setOnScroll(event -> {
            if (dossierImageView.getImage() != null) {
                if (event.getDeltaY() > 0) {
                    handleZoomIn();
                } else if (event.getDeltaY() < 0) {
                    handleZoomOut();
                }
                event.consume(); // Konsumsi event agar tidak ada scrolling lain yang terjadi
            }
        });
    }

    public void loadDossierData(Target target, Mission missionToReturnTo, String returnView) {
        this.currentTarget = target;
        this.missionToReturnTo = missionToReturnTo;
        this.returnView = returnView;
        if (target == null) return;
        
        targetIdLabel.setText("TARGET: " + target.getNama().toUpperCase());
        targetNameLabel.setText(target.getNama().toUpperCase());
        targetStatusLabel.setText("ANCAMAN: " + target.getAncaman().getDisplayName().toUpperCase());
        targetTipeLabel.setText(target.getTipe() != null ? target.getTipe().getDisplayName() : "N/A");
        targetLokasiLabel.setText(target.getLokasi() != null ? target.getLokasi() : "N/A");
        intelTextArea.setText("ANALISIS AWAL:\n" + (target.getDeskripsi() != null ? target.getDeskripsi() : "-") + "\n\nCATATAN TAMBAHAN:\n-");

        evidenceFiles.clear();
        if (target.getEvidencePaths() != null) {
            target.getEvidencePaths().stream()
                .map(File::new)
                .filter(File::exists)
                .forEach(evidenceFiles::add);
        }

        loadProfileImage(target);
        
        imageFiles = evidenceFiles.stream()
            .filter(f -> f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$"))
            .collect(Collectors.toList());

        if (!evidenceFiles.isEmpty()) {
            evidenceListView.getSelectionModel().selectFirst();
        } else {
            showPlaceholder();
        }
        
        // Panggil metode untuk mengatur akses UI berdasarkan peran
        updateUIAccess();
    }

    private void displayEvidence(File file) {
        currentZoomFactor = 1.0;
        zoomResetButton.setText("100%");
        
        String fileName = file.getName().toLowerCase();
        contentTitleLabel.setText(file.getName().toUpperCase());

        if (fileName.matches(".*\\.(png|jpg|jpeg|gif|bmp)$")) {
            contentSubtitleLabel.setText("Visual Evidence / Image File");
            currentImageIndex = imageFiles.indexOf(file);
            showImageViewer(file);
        } else {
            showPlaceholder();
            contentTitleLabel.setText("Tipe File Tidak Didukung");
        }
    }
    
    @FXML
    private void handleBack() {
        if (mainDashboardController != null) {
            if (missionToReturnTo != null) {
                if ("agentDetail".equals(returnView)) {
                    mainDashboardController.showAgentMissionDetailView(missionToReturnTo);
                } else { 
                    mainDashboardController.showMissionPlanningView(missionToReturnTo);
                }
            } else {
                mainDashboardController.loadDefaultRoleDashboard(currentUser.getRole());
            }
        }
    }

    @FXML
    private void handleDeleteEvidence() {
        File selectedFile = evidenceListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setHeaderText("HAPUS BUKTI INI?");
        alert.setContentText("Anda yakin ingin menghapus file '" + selectedFile.getName() + "' secara permanen?");
        styleAlertDialog(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                currentTarget.getEvidencePaths().remove(selectedFile.getPath());
                targetManager.saveTarget(currentTarget);
                Files.deleteIfExists(selectedFile.toPath());
                loadDossierData(currentTarget, missionToReturnTo, returnView);
            } catch (IOException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.initStyle(StageStyle.UNDECORATED);
                errorAlert.setHeaderText("GAGAL MENGHAPUS FILE");
                errorAlert.setContentText("Terjadi error saat menghapus file bukti dari disk.");
                styleAlertDialog(errorAlert);
                errorAlert.showAndWait();
            }
        }
    }

    private void updateUIAccess() {
        // Visibilitas tombol hanya untuk Analis Intelijen
        boolean canEditEvidence = (currentUser != null && currentUser.getRole() == Role.ANALIS_INTELIJEN);
        
        addEvidenceButton.setVisible(canEditEvidence);
        addEvidenceButton.setManaged(canEditEvidence);
        
        deleteEvidenceButton.setVisible(canEditEvidence);
        deleteEvidenceButton.setManaged(canEditEvidence);
    }
    
    // Sisa metode lain (tidak berubah signifikan)
    private void showImageViewer(File imageFile) {
        imageViewerPane.setVisible(true);
        imageNavBox.setVisible(true);
        if (imageFile != null) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                dossierImageView.setImage(image);
                resetZoom(dossierImageView);
                updateImageNavUI();
            } catch (Exception e) {
                showPlaceholder();
            }
        } else {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        imageViewerPane.setVisible(true);
        dossierImageView.setImage(new Image(getClass().getResourceAsStream("/com/sigma48/images/placeholder.png")));
        imageNavBox.setVisible(false);
        contentTitleLabel.setText("TIDAK ADA BUKTI");
        contentSubtitleLabel.setText("Pilih atau tambahkan file bukti.");
    }
    
    @FXML private void handlePrevImage() {
        if (currentImageIndex > 0) displayEvidence(imageFiles.get(--currentImageIndex));
    }
    
    @FXML private void handleNextImage() {
        if (currentImageIndex < imageFiles.size() - 1) displayEvidence(imageFiles.get(++currentImageIndex));
    }

    // MODIFIKASI: Mengubah akses menjadi private karena dipanggil dari dalam
    @FXML private void handleZoomIn() {
        currentZoomFactor += 0.2;
        applyZoom();
    }

    // MODIFIKASI: Mengubah akses menjadi private karena dipanggil dari dalam
    @FXML private void handleZoomOut() {
        if (currentZoomFactor > 0.3) {
            currentZoomFactor -= 0.2;
            applyZoom();
        }
    }

    @FXML private void handleZoomReset() {
        resetZoom(dossierImageView);
    }
    
    @FXML private void handleSetProfilePhoto(MouseEvent event) {
        if (currentUser.getRole() != Role.ANALIS_INTELIJEN && currentUser.getRole() != Role.DIREKTUR_INTELIJEN) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil Baru");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(targetPhotoImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Path evidenceDir = Paths.get("data", "evidence", currentTarget.getId());
                Files.createDirectories(evidenceDir);
                Path destPath = evidenceDir.resolve(selectedFile.getName());
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                String newProfilePath = destPath.toString();
                currentTarget.setProfileImagePath(newProfilePath);
                
                if (!currentTarget.getEvidencePaths().contains(newProfilePath)) {
                    currentTarget.getEvidencePaths().add(newProfilePath);
                }
                if(targetManager.saveTarget(currentTarget)) {
                    loadProfileImage(currentTarget);
                    if (!evidenceFiles.contains(destPath.toFile())) {
                        evidenceFiles.add(destPath.toFile());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void handleAddEvidence() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Bukti");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(addEvidenceButton.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            Path evidenceDir = Paths.get("data", "evidence", currentTarget.getId());
            try {
                Files.createDirectories(evidenceDir);
                for (File file : selectedFiles) {
                    Path destPath = evidenceDir.resolve(file.getName());
                    Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    String pathString = destPath.toString();
                    if (!currentTarget.getEvidencePaths().contains(pathString)) {
                        currentTarget.getEvidencePaths().add(pathString);
                    }
                }
                targetManager.saveTarget(currentTarget);
                loadDossierData(currentTarget, missionToReturnTo, returnView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyZoom() {
        if(dossierImageView.getImage() == null) return;
        dossierImageView.setFitWidth(dossierImageView.getImage().getWidth() * currentZoomFactor);
        dossierImageView.setFitHeight(dossierImageView.getImage().getHeight() * currentZoomFactor);
        zoomResetButton.setText(String.format("%.0f%%", currentZoomFactor * 100));
    }
    
    private void resetZoom(ImageView imageView) {
        currentZoomFactor = 1.0;
        if(imageView != null && viewerScrollPane != null) {
            imageView.setFitWidth(viewerScrollPane.getWidth());
            imageView.setFitHeight(viewerScrollPane.getHeight());
        }
        if(zoomResetButton != null) {
            zoomResetButton.setText("100%");
        }
    }

    private void updateImageNavUI() {
        if (imageFiles == null || imageFiles.isEmpty()) {
            imageNavBox.setVisible(false);
            return;
        }
        imageNavBox.setVisible(true);
        imageCounterLabel.setText((currentImageIndex + 1) + " / " + imageFiles.size());
        prevImageButton.setDisable(currentImageIndex <= 0);
        nextImageButton.setDisable(currentImageIndex >= imageFiles.size() - 1);
    }
    
    private void loadProfileImage(Target target) {
        Image img = null;
        if (target.getProfileImagePath() != null && !target.getProfileImagePath().isEmpty()) {
            File profileFile = new File(target.getProfileImagePath());
            if(profileFile.exists()){
                img = new Image(profileFile.toURI().toString());
            }
        }
        
        if (img == null) {
            Optional<File> firstImage = evidenceFiles.stream()
                .filter(f -> f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg)$")).findFirst();
            if(firstImage.isPresent()) {
                img = new Image(firstImage.get().toURI().toString());
            }
        }
        targetPhotoImageView.setImage(img);
    }
}
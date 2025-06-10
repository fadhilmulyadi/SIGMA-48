package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Role;
import com.sigma48.model.Target;
import com.sigma48.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DossierViewController {

    // --- FXML Components (Variabel yang terhubung ke FXML) ---
    @FXML private GridPane dossierContainer;
    @FXML private Button backButton;
    @FXML private Label targetIdLabel;
    
    // Sidebar Kiri
    @FXML private ListView<File> evidenceListView;
    @FXML private Button addEvidenceButton;
    
    // Panel Detail Kanan
    @FXML private ImageView targetPhotoImageView;
    @FXML private Label targetNameLabel;
    @FXML private Label targetStatusLabel;
    @FXML private Label targetTipeLabel;
    @FXML private Label targetLokasiLabel;
    @FXML private TextArea intelTextArea;

    // Panel Konten Tengah
    @FXML private Label contentTitleLabel;
    @FXML private Label contentSubtitleLabel;
    @FXML private ScrollPane viewerScrollPane; // ScrollPane untuk menampung viewer

    // Viewer Gambar
    @FXML private VBox imageViewerPane;
    @FXML private ImageView dossierImageView;
    @FXML private HBox imageNavBox;
    @FXML private Button prevImageButton;
    @FXML private Button nextImageButton;
    @FXML private Label imageCounterLabel;

    // Viewer PDF
    @FXML private VBox pdfViewerPane;
    @FXML private ImageView pdfImageView;
    @FXML private HBox pdfNavBox;
    @FXML private Button prevPdfPageButton;
    @FXML private Button nextPdfPageButton;
    @FXML private Label pdfPageCounterLabel;
    
    // Tombol Zoom (Baru)
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomResetButton;

    // --- Class Members (Variabel internal) ---
    private MainDashboardController mainDashboardController;
    private TargetManager targetManager;
    private Target currentTarget;
    private User currentUser;

    private final ObservableList<File> evidenceFiles = FXCollections.observableArrayList();
    private List<File> imageFiles;
    private int currentImageIndex = 0;
    
    private PDDocument currentPdfDocument;
    private PDFRenderer pdfRenderer;
    private int currentPdfPage = 0;
    private int totalPdfPages = 0;
    private double currentZoomFactor = 1.0;

    // --- Inisialisasi & Setup ---
    public void setup(MainDashboardController mainController, TargetManager targetMgr) {
        this.mainDashboardController = mainController;
        this.targetManager = targetMgr;
        this.currentUser = Main.authManager.getCurrentUser();
    }
    
    @FXML
    public void initialize() {
        // Setup ListView bukti
        evidenceListView.setItems(evidenceFiles);
        evidenceListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Listener untuk menampilkan bukti saat dipilih
        evidenceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayEvidence(newVal);
            }
        });
        
        // Atur agar viewer bisa di-scroll jika kontennya besar
        viewerScrollPane.setFitToWidth(true);
        viewerScrollPane.setFitToHeight(true);
    }

    // --- Logika Utama ---
    public void loadDossierData(Target target) {
        this.currentTarget = target;
        if (target == null) return;

        cleanupResources(); // Bersihkan resource dari data sebelumnya
        
        // 1. Isi UI dengan data dari objek Target
        targetIdLabel.setText("TARGET: " + target.getNama().toUpperCase());
        targetNameLabel.setText(target.getNama().toUpperCase());
        targetStatusLabel.setText("ANCAMAN: " + target.getAncaman().getDisplayName().toUpperCase());
        targetTipeLabel.setText(target.getTipe() != null ? target.getTipe().getDisplayName() : "N/A");
        targetLokasiLabel.setText(target.getLokasi() != null ? target.getLokasi() : "N/A");
        intelTextArea.setText("ANALISIS AWAL:\n" + (target.getDeskripsi() != null ? target.getDeskripsi() : "-") + "\n\nCATATAN TAMBAHAN:\n-");

        // 2. Load daftar bukti
        evidenceFiles.clear();
        if (target.getEvidencePaths() != null) {
            target.getEvidencePaths().stream()
                .map(File::new)
                .filter(File::exists)
                .forEach(evidenceFiles::add);
        }

        // 3. Load foto profil
        loadProfileImage(target);
        
        // Filter file gambar untuk navigasi
        imageFiles = evidenceFiles.stream()
            .filter(f -> f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$"))
            .collect(Collectors.toList());

        // Tampilkan bukti pertama jika ada
        if (!evidenceFiles.isEmpty()) {
            evidenceListView.getSelectionModel().selectFirst();
        } else {
            showPlaceholder();
        }
        updateUIAccess();
    }

    private void displayEvidence(File file) {
        currentZoomFactor = 1.0;
        zoomResetButton.setText("100%");
        updateZoomButtonState();
        
        String fileName = file.getName().toLowerCase();
        contentTitleLabel.setText(file.getName().toUpperCase());

        if (fileName.matches(".*\\.(png|jpg|jpeg|gif|bmp)$")) {
            contentSubtitleLabel.setText("Visual Evidence / Image File");
            currentImageIndex = imageFiles.indexOf(file);
            showImageViewer(file);
        } else if (fileName.endsWith(".pdf")) {
            contentSubtitleLabel.setText("Document / PDF File");
            loadAndDisplayPdf(file);
        } else {
            showPlaceholder();
            contentTitleLabel.setText("Tipe File Tidak Didukung");
        }
    }

    private void showImageViewer(File imageFile) {
        pdfViewerPane.setVisible(false);
        pdfViewerPane.setManaged(false);
        imageViewerPane.setVisible(true);
        imageViewerPane.setManaged(true);

        pdfNavBox.setVisible(false);
        pdfNavBox.setManaged(false);
        imageNavBox.setVisible(true);
        imageNavBox.setManaged(true);

        if (imageFile != null) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                dossierImageView.setImage(image);
                dossierImageView.setPreserveRatio(true);
                resetZoom(dossierImageView);
                updateImageNavUI(); // Panggil update UI navigasi
            } catch (Exception e) {
                showPlaceholder();
            }
        } else {
            showPlaceholder();
        }
    }
    
    private void loadAndDisplayPdf(File pdfFile) {
        cleanupResources();
        try {
            currentPdfDocument = PDDocument.load(pdfFile);
            pdfRenderer = new PDFRenderer(currentPdfDocument);
            totalPdfPages = currentPdfDocument.getNumberOfPages();
            currentPdfPage = 0;
            
            imageViewerPane.setVisible(false);
            imageViewerPane.setManaged(false);
            pdfViewerPane.setVisible(true);
            pdfViewerPane.setManaged(true);

            imageNavBox.setVisible(false);
            imageNavBox.setManaged(false);
            pdfNavBox.setVisible(true);
            pdfNavBox.setManaged(true);
            
            renderPdfPage(currentPdfPage);
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder();
        }
    }

    private void renderPdfPage(int pageIndex) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= totalPdfPages) return;
        try {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            pdfImageView.setImage(fxImage);
            pdfImageView.setPreserveRatio(true);
            resetZoom(pdfImageView);
            
            updatePdfNavUI(); // Panggil update UI navigasi
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPlaceholder() {
        // Tampilkan viewer gambar sebagai placeholder
        pdfViewerPane.setVisible(false);
        pdfViewerPane.setManaged(false);
        imageViewerPane.setVisible(true);
        imageViewerPane.setManaged(true);
        dossierImageView.setImage(new Image(getClass().getResourceAsStream("/com/sigma48/images/placeholder.png")));
        
        // Sembunyikan SEMUA kontrol navigasi
        imageNavBox.setVisible(false);
        imageNavBox.setManaged(false);
        pdfNavBox.setVisible(false);
        pdfNavBox.setManaged(false);
        
        contentTitleLabel.setText("TIDAK ADA BUKTI");
        contentSubtitleLabel.setText("Pilih atau tambahkan file bukti.");
    }

    private void updateZoomButtonState() {
        zoomOutButton.setDisable(currentZoomFactor <= 1.0);
    }
    
    // --- Event Handlers ---

    @FXML private void handleBack() {
        cleanupResources();
        if (mainDashboardController != null) mainDashboardController.showTargetManagementView();
    }

    @FXML private void handlePrevImage() {
        if (currentImageIndex > 0) displayEvidence(imageFiles.get(--currentImageIndex));
    }
    
    @FXML private void handleNextImage() {
        if (currentImageIndex < imageFiles.size() - 1) displayEvidence(imageFiles.get(++currentImageIndex));
    }

    @FXML private void handlePrevPdfPage() {
        if (currentPdfPage > 0) renderPdfPage(--currentPdfPage);
    }
    
    @FXML private void handleNextPdfPage() {
        if (currentPdfPage < totalPdfPages - 1) renderPdfPage(++currentPdfPage);
    }

    @FXML private void handleZoomIn() {
        currentZoomFactor += 0.2;
        applyZoom();
    }

    @FXML private void handleZoomOut() {
        if (currentZoomFactor > 0.3) { // Batas zoom out
            currentZoomFactor -= 0.2;
            applyZoom();
        }
    }

    @FXML private void handleZoomReset() {
        ImageView activeView = pdfViewerPane.isVisible() ? pdfImageView : dossierImageView;
        resetZoom(activeView);
    }
    
    @FXML
    private void handleSetProfilePhoto(MouseEvent event) {
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
                
                // Jika file ini belum ada di daftar bukti, tambahkan
                if (!currentTarget.getEvidencePaths().contains(newProfilePath)) {
                    currentTarget.getEvidencePaths().add(newProfilePath);
                }
                
                if(targetManager.saveTarget(currentTarget)) {
                    loadProfileImage(currentTarget); // Muat ulang gambar profil
                    // Muat ulang daftar file jika ada file baru
                    if (!evidenceFiles.contains(destPath.toFile())) {
                        evidenceFiles.add(destPath.toFile());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddEvidence() {
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
                loadDossierData(currentTarget); // Muat ulang semua data
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // --- UI & Logic Helpers ---

    private void applyZoom() {
        ImageView activeView = pdfViewerPane.isVisible() ? pdfImageView : dossierImageView;
        if(activeView.getImage() == null) return;
        
        activeView.setFitWidth(activeView.getImage().getWidth() * currentZoomFactor);
        activeView.setFitHeight(activeView.getImage().getHeight() * currentZoomFactor);

        zoomResetButton.setText(String.format("%.0f%%", currentZoomFactor * 100));
        updateZoomButtonState();
    }
    
    private void resetZoom(ImageView imageView) {
        currentZoomFactor = 1.0;
        imageView.setFitWidth(viewerScrollPane.getWidth());
        imageView.setFitHeight(viewerScrollPane.getHeight());
        
        zoomResetButton.setText("100%");
        updateZoomButtonState();
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
    
    private void updatePdfNavUI() {
        if (totalPdfPages > 0) {
            pdfNavBox.setVisible(true);
            pdfPageCounterLabel.setText("Halaman " + (currentPdfPage + 1) + " / " + totalPdfPages);
            prevPdfPageButton.setDisable(currentPdfPage <= 0);
            nextPdfPageButton.setDisable(currentPdfPage >= totalPdfPages - 1);
        } else {
            pdfNavBox.setVisible(false);
        }
    }
    
    private void loadProfileImage(Target target) {
        Image img = null;
        if (target.getProfileImagePath() != null && !target.getProfileImagePath().isEmpty()) {
            File profileFile = new File(target.getProfileImagePath());
            if(profileFile.exists()){
                img = new Image(profileFile.toURI().toString());
            }
        }
        
        if (img == null) { // Jika path profil tidak ada atau file tidak ditemukan, cari gambar pertama
            Optional<File> firstImage = evidenceFiles.stream()
                .filter(f -> f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg)$")).findFirst();
            if(firstImage.isPresent()) {
                img = new Image(firstImage.get().toURI().toString());
            }
        }
        
        targetPhotoImageView.setImage(img); // Akan menampilkan null (kosong) jika img tetap null
    }

    private void updateUIAccess() {
        boolean canEdit = (currentUser != null && (currentUser.getRole() == Role.ANALIS_INTELIJEN || currentUser.getRole() == Role.DIREKTUR_INTELIJEN));
        addEvidenceButton.setManaged(canEdit);
        addEvidenceButton.setVisible(canEdit);
    }
    
    private void cleanupResources() {
        if (currentPdfDocument != null) {
            try {
                currentPdfDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentPdfDocument = null;
            pdfRenderer = null;
        }
    }
}
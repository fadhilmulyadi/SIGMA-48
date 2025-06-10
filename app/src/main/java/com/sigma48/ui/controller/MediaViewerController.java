package com.sigma48.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MediaViewerController {

    @FXML private BorderPane rootPane;
    @FXML private HBox headerBar;
    @FXML private StackPane viewerStackPane;
    @FXML private ImageView imageView;
    @FXML private MediaView mediaView;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label fileNameLabel;
    @FXML private Button closeButton;
    @FXML private HBox zoomControls;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomInButton;
    @FXML private Button resetZoomButton;
    @FXML private HBox mediaControls;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    private List<File> mediaFiles;
    private int currentIndex;
    private MediaPlayer mediaPlayer;
    private PDDocument currentPdfDocument;
    private PDFRenderer pdfRenderer;
    private int currentPdfPage;
    private int totalPdfPages;
    private double zoomFactor = 1.0;
    private final double ZOOM_AMOUNT = 0.2; // Zoom lebih terasa

    private static final List<String> IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    private static final List<String> MEDIA_EXT = Arrays.asList("mp4", "m4v", "m4a", "mp3", "wav");
    private static final List<String> PDF_EXT = List.of("pdf");

    @FXML
    public void initialize() {
        // Binding ini tetap dipertahankan agar gambar pas secara default
        imageView.fitWidthProperty().bind(viewerStackPane.widthProperty());
        imageView.fitHeightProperty().bind(viewerStackPane.heightProperty());
        mediaView.fitWidthProperty().bind(viewerStackPane.widthProperty());
        mediaView.fitHeightProperty().bind(viewerStackPane.heightProperty());
    }

    public void setupViewer(List<File> attachments, File selectedFile) {
        this.mediaFiles = attachments.stream().filter(f -> {
            String ext = getFileExtension(f);
            return IMAGE_EXT.contains(ext) || MEDIA_EXT.contains(ext) || PDF_EXT.contains(ext);
        }).collect(Collectors.toList());

        this.currentIndex = this.mediaFiles.indexOf(selectedFile);
        if (this.currentIndex == -1 && !this.mediaFiles.isEmpty()) {
            this.currentIndex = 0;
        }
        displayCurrentFile();
    }

    private void displayCurrentFile() {
        if (currentIndex < 0 || currentIndex >= mediaFiles.size()) {
            return;
        }
        stopAnyPlayback();
        File file = mediaFiles.get(currentIndex);
        String extension = getFileExtension(file);
        fileNameLabel.setText(file.getName().toUpperCase());

        // Reset semua view visibility
        imageView.setVisible(false);
        mediaView.setVisible(false);

        if (IMAGE_EXT.contains(extension)) {
            displayImage(file);
        } else if (MEDIA_EXT.contains(extension)) {
            displayMedia(file);
        } else if (PDF_EXT.contains(extension)) {
            displayPdf(file, 0);
        }
        updateControls();
    }


private void displayImage(File file) {
    try {
        System.out.println("=== DISPLAY IMAGE START ===");
        
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        imageView.setVisible(true);
        
        // Sembunyikan media controls DULU
        if (mediaControls != null) {
            mediaControls.setVisible(false);
            mediaControls.setManaged(false);
        }
        
        // Tampilkan zoom controls dengan DELAY
        Platform.runLater(() -> {
            if (zoomControls != null) {
                System.out.println("Setting zoom controls with Platform.runLater");
                
                zoomControls.setVisible(true);
                zoomControls.setManaged(true);
                zoomControls.toFront();
                
                // FORCE semua child buttons visible
                zoomControls.getChildren().forEach(child -> {
                    child.setVisible(true);
                    child.setManaged(true);
                    System.out.println("Child button: " + child + " set to visible");
                });
                
                // DEBUG styling
                System.out.println("Final zoomControls state:");
                System.out.println("  Visible: " + zoomControls.isVisible());
                System.out.println("  Managed: " + zoomControls.isManaged());
                System.out.println("  Width: " + zoomControls.getWidth());
                System.out.println("  Height: " + zoomControls.getHeight());
                System.out.println("  BoundsInParent: " + zoomControls.getBoundsInParent());
            }
        });
        
        resetZoom();
        System.out.println("=== DISPLAY IMAGE END ===");
        
    } catch (Exception e) {
        System.out.println("ERROR in displayImage: " + e.getMessage());
        e.printStackTrace();
    }
}
    // ... (sisa metode displayMedia dan displayPdf tidak berubah)
    private void displayMedia(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            mediaView.setVisible(true);
            mediaControls.setVisible(true);

            mediaPlayer.setOnEndOfMedia(this::handleNext);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void displayPdf(File file, int pageNumber) {
        try {
            if (currentPdfDocument == null || !new File(currentPdfDocument.getDocumentInformation().getProducer()).equals(file)) {
                 if (currentPdfDocument != null) currentPdfDocument.close();
                 currentPdfDocument = PDDocument.load(file);
                 pdfRenderer = new PDFRenderer(currentPdfDocument);
                 totalPdfPages = currentPdfDocument.getNumberOfPages();
            }
            if (pageNumber >= 0 && pageNumber < totalPdfPages) {
                currentPdfPage = pageNumber;
                BufferedImage bim = pdfRenderer.renderImageWithDPI(currentPdfPage, 150);
                Image image = SwingFXUtils.toFXImage(bim, null);
                imageView.setImage(image);
                imageView.setVisible(true);
                zoomControls.setVisible(true);
                resetZoom();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- PERBAIKAN LOGIKA ZOOM DI SINI ---
    @FXML
    private void handleZoomIn() {
        zoomFactor += ZOOM_AMOUNT;
        applyZoom();
    }

    @FXML
    private void handleZoomOut() {
        zoomFactor = Math.max(0.2, zoomFactor - ZOOM_AMOUNT); // Batasi zoom out
        applyZoom();
    }

    @FXML
    private void handleResetZoom() {
        resetZoom();
    }
    
    private void resetZoom() {
        zoomFactor = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        // Gunakan setScaleX dan setScaleY untuk zoom, bukan fitWidth/fitHeight
        imageView.setScaleX(zoomFactor);
        imageView.setScaleY(zoomFactor);
        resetZoomButton.setText(String.format("%.0f%%", zoomFactor * 100));
    }
    // --- AKHIR PERBAIKAN LOGIKA ZOOM ---
    
    // ... (sisa kode lainnya tidak berubah)
    private void stopAnyPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
         if (currentPdfDocument != null) {
            try {
                currentPdfDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentPdfDocument = null;
        }
    }

    @FXML
    private void handlePrev() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentFile();
        }
    }

    @FXML
    private void handleNext() {
        if (currentIndex < mediaFiles.size() - 1) {
            currentIndex++;
            displayCurrentFile();
        }
    }
    
    @FXML private void handlePlay() { if (mediaPlayer != null) mediaPlayer.play(); }
    @FXML private void handlePause() { if (mediaPlayer != null) mediaPlayer.pause(); }
    @FXML private void handleStop() { if (mediaPlayer != null) mediaPlayer.stop(); }

    @FXML
    private void handleClose() {
        stopAnyPlayback();
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    private void updateControls() {
        prevButton.setDisable(currentIndex <= 0);
        nextButton.setDisable(currentIndex >= mediaFiles.size() - 1);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) { 
            return ""; 
        }
        return name.substring(lastIndexOf + 1).toLowerCase();
    }
}
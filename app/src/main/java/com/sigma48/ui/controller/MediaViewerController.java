package com.sigma48.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MediaViewerController {

    @FXML private BorderPane rootPane;
    @FXML private ScrollPane viewerScrollPane;
    @FXML private ImageView imageView;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label fileNameLabel;
    @FXML private Button closeButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomInButton;
    @FXML private Button resetZoomButton;

    private List<File> imageFiles;
    private int currentIndex;
    private double currentZoomFactor = 1.0;
    private static final double ZOOM_AMOUNT = 0.2;

    private static final List<String> IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

    // BARU: Tambahkan method initialize untuk menangani event.
    @FXML
    public void initialize() {
        // BARU: Tambahkan event handler untuk zoom dengan mouse wheel/pad
        viewerScrollPane.setOnScroll(event -> {
            if (imageView.getImage() != null) {
                if (event.getDeltaY() > 0) {
                    handleZoomIn();
                } else if (event.getDeltaY() < 0) {
                    handleZoomOut();
                }
                event.consume(); // Konsumsi event agar tidak ada scrolling lain yang terjadi
            }
        });
    }

    public void setupViewer(List<File> attachments, File selectedFile) {
        // Filter hanya untuk file gambar
        this.imageFiles = attachments.stream().filter(f -> {
            String ext = getFileExtension(f);
            return IMAGE_EXT.contains(ext);
        }).collect(Collectors.toList());

        this.currentIndex = this.imageFiles.indexOf(selectedFile);
        if (this.currentIndex == -1 && !this.imageFiles.isEmpty()) {
            this.currentIndex = 0;
        }
        
        displayCurrentImage();
    }

    private void displayCurrentImage() {
        if (currentIndex < 0 || currentIndex >= imageFiles.size()) {
            return;
        }
        File file = imageFiles.get(currentIndex);
        fileNameLabel.setText(file.getName().toUpperCase());

        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            resetZoom(); // Panggil resetZoom untuk memastikan gambar pas di jendela
        } catch (Exception e) {
            e.printStackTrace();
            // Handle jika gambar gagal dimuat
            imageView.setImage(null);
            fileNameLabel.setText("GAGAL MEMUAT GAMBAR");
        }

        updateNavControls();
    }

    // --- LOGIKA ZOOM (DIADOPSI DARI DOSSIERVIEW) ---

    // MODIFIKASI: Mengubah akses menjadi private karena dipanggil dari dalam
    @FXML private void handleZoomIn() {
        currentZoomFactor += ZOOM_AMOUNT;
        applyZoom();
    }

    // MODIFIKASI: Mengubah akses menjadi private karena dipanggil dari dalam
    @FXML private void handleZoomOut() {
        // Batasi zoom out agar tidak terlalu kecil
        currentZoomFactor = Math.max(0.2, currentZoomFactor - ZOOM_AMOUNT);
        applyZoom();
    }

    @FXML private void handleResetZoom() {
        resetZoom();
    }

    private void applyZoom() {
        if (imageView.getImage() == null) return;
        // Mengatur ukuran gambar berdasarkan ukuran aslinya dikali faktor zoom
        imageView.setFitWidth(imageView.getImage().getWidth() * currentZoomFactor);
        imageView.setFitHeight(imageView.getImage().getHeight() * currentZoomFactor);
        resetZoomButton.setText(String.format("%.0f%%", currentZoomFactor * 100));
    }

    private void resetZoom() {
        currentZoomFactor = 1.0;
        // Membuat fitWidth/Height mengikuti ukuran ScrollPane agar gambar pas
        imageView.setFitWidth(viewerScrollPane.getWidth());
        imageView.setFitHeight(viewerScrollPane.getHeight());
        resetZoomButton.setText("100%");
    }
    
    // --- LOGIKA NAVIGASI ---

    @FXML private void handlePrev() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentImage();
        }
    }

    @FXML private void handleNext() {
        if (currentIndex < imageFiles.size() - 1) {
            currentIndex++;
            displayCurrentImage();
        }
    }
    
    @FXML private void handleClose() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    private void updateNavControls() {
        prevButton.setDisable(currentIndex <= 0);
        nextButton.setDisable(currentIndex >= imageFiles.size() - 1);
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
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

import com.sigma48.util.ImageViewZoomManager;

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
    private ImageViewZoomManager zoomManager;


    private static final List<String> IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

    @FXML
    public void initialize() {
        this.zoomManager = new ImageViewZoomManager(viewerScrollPane, imageView, zoomInButton, zoomOutButton, resetZoomButton);
 
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
            if (zoomManager != null) {
                zoomManager.resetZoom();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle jika gambar gagal dimuat
            imageView.setImage(null);
            fileNameLabel.setText("GAGAL MEMUAT GAMBAR");
        }

        updateNavControls();
    }
    
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
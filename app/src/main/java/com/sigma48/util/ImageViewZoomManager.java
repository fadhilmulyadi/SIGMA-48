package com.sigma48.util;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class ImageViewZoomManager {

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 10.0;
    
    private static final double[] ZOOM_LEVELS = {
        0.5, 0.67, 0.75, 0.8, 0.9, 1.0, 1.1, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0
    };
    
    private double currentZoomFactor = 1.0;

    private final ScrollPane scrollPane;
    private final ImageView imageView;
    private final Button zoomInButton;
    private final Button zoomOutButton;
    private final Button zoomResetButton;

    private double initialMouseX;
    private double initialMouseY;
    private double initialScrollPaneH;
    private double initialScrollPaneV;

    public ImageViewZoomManager(ScrollPane scrollPane, ImageView imageView, Button zoomInButton, Button zoomOutButton, Button zoomResetButton) {
        this.scrollPane = scrollPane;
        this.imageView = imageView;
        this.zoomInButton = zoomInButton;
        this.zoomOutButton = zoomOutButton;
        this.zoomResetButton = zoomResetButton;

        setupZoomHandlers();
        setupPanHandlers();
        updateButtonStates(); // Panggil saat pertama kali dibuat
    }

    private void setupZoomHandlers() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setOnScroll((ScrollEvent event) -> {
            if (imageView.getImage() != null) {
                // Simpan posisi untuk pivot point
                double mouseX = event.getX();
                double mouseY = event.getY();
                
                if (event.getDeltaY() > 0) {
                    // Zoom in - cari level zoom berikutnya yang lebih besar
                    double nextZoom = getNextZoomLevel(currentZoomFactor, true);
                    if (nextZoom > currentZoomFactor) {
                        applyZoomWithPivot(nextZoom, mouseX, mouseY);
                    }
                } else {
                    // Zoom out - cari level zoom sebelumnya yang lebih kecil
                    double prevZoom = getNextZoomLevel(currentZoomFactor, false);
                    if (prevZoom < currentZoomFactor) {
                        applyZoomWithPivot(prevZoom, mouseX, mouseY);
                    }
                }
                
                event.consume();
            }
        });

        zoomInButton.setOnAction(e -> handleZoomIn());
        zoomOutButton.setOnAction(e -> handleZoomOut());
        zoomResetButton.setOnAction(e -> handleZoomReset());
    }
    
    private void setupPanHandlers() {
        imageView.setOnMousePressed((MouseEvent event) -> {
            initialMouseX = event.getSceneX();
            initialMouseY = event.getSceneY();
            initialScrollPaneH = scrollPane.getHvalue();
            initialScrollPaneV = scrollPane.getVvalue();
        });

        imageView.setOnMouseDragged((MouseEvent event) -> {
            double deltaX = event.getSceneX() - initialMouseX;
            double deltaY = event.getSceneY() - initialMouseY;
            
            double contentWidth = imageView.getBoundsInLocal().getWidth();
            if (contentWidth > scrollPane.getViewportBounds().getWidth()) {
                double newHValue = initialScrollPaneH - (deltaX / contentWidth);
                scrollPane.setHvalue(Math.max(0, Math.min(1, newHValue)));
            }

            double contentHeight = imageView.getBoundsInLocal().getHeight();
            if (contentHeight > scrollPane.getViewportBounds().getHeight()) {
                double newVValue = initialScrollPaneV - (deltaY / contentHeight);
                scrollPane.setVvalue(Math.max(0, Math.min(1, newVValue)));
            }
        });
    }

    private void handleZoomIn() {
        double centerX = scrollPane.getViewportBounds().getWidth() / 2;
        double centerY = scrollPane.getViewportBounds().getHeight() / 2;
        
        double nextZoom = getNextZoomLevel(currentZoomFactor, true);
        if (nextZoom > currentZoomFactor) {
            applyZoomWithPivot(nextZoom, centerX, centerY);
        }
    }

    private void handleZoomOut() {
        double centerX = scrollPane.getViewportBounds().getWidth() / 2;
        double centerY = scrollPane.getViewportBounds().getHeight() / 2;
        
        double prevZoom = getNextZoomLevel(currentZoomFactor, false);
        if (prevZoom < currentZoomFactor) {
            applyZoomWithPivot(prevZoom, centerX, centerY);
        }
    }

    private void handleZoomReset() {
        resetZoom();
    }

    // Method untuk mencari zoom level berikutnya/sebelumnya
    private double getNextZoomLevel(double current, boolean zoomIn) {
        if (zoomIn) {
            // Cari level pertama yang lebih besar dari current
            for (double level : ZOOM_LEVELS) {
                if (level > current + 0.01) { // Toleransi kecil
                    return level;
                }
            }
            return current; // Tidak ada level yang lebih besar
        } else {
            // Cari level terbesar yang lebih kecil dari current
            double result = current;
            for (double level : ZOOM_LEVELS) {
                if (level < current - 0.01) { // Toleransi kecil
                    result = level;
                } else {
                    break;
                }
            }
            return result;
        }
    }

    private void applyZoomWithPivot(double newZoomFactor, double pivotX, double pivotY) {
        if (imageView.getImage() == null) return;

        // Simpan posisi scroll saat ini
        double oldHValue = scrollPane.getHvalue();
        double oldVValue = scrollPane.getVvalue();
        
        // Hitung posisi relatif pivot point
        double imageWidth = imageView.getFitWidth();
        double imageHeight = imageView.getFitHeight();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        
        // Posisi absolut pivot dalam koordinat image
        double pivotImageX = 0, pivotImageY = 0;
        if (imageWidth > viewportWidth) {
            pivotImageX = (oldHValue * (imageWidth - viewportWidth) + pivotX) / imageWidth;
        } else {
            pivotImageX = pivotX / imageWidth;
        }
        
        if (imageHeight > viewportHeight) {
            pivotImageY = (oldVValue * (imageHeight - viewportHeight) + pivotY) / imageHeight;  
        } else {
            pivotImageY = pivotY / imageHeight;
        }
        
        // Update zoom factor
        currentZoomFactor = newZoomFactor;
        
        // Terapkan zoom baru
        imageView.setFitWidth(imageView.getImage().getWidth() * currentZoomFactor);
        imageView.setFitHeight(imageView.getImage().getHeight() * currentZoomFactor);
        
        // Hitung posisi scroll baru untuk mempertahankan pivot point
        double newImageWidth = imageView.getFitWidth();
        double newImageHeight = imageView.getFitHeight();
        
        if (newImageWidth > viewportWidth) {
            double newHValue = (pivotImageX * newImageWidth - pivotX) / (newImageWidth - viewportWidth);
            scrollPane.setHvalue(Math.max(0, Math.min(1, newHValue)));
        } else {
            scrollPane.setHvalue(0.5);
        }
        
        if (newImageHeight > viewportHeight) {
            double newVValue = (pivotImageY * newImageHeight - pivotY) / (newImageHeight - viewportHeight);
            scrollPane.setVvalue(Math.max(0, Math.min(1, newVValue)));
        } else {
            scrollPane.setVvalue(0.5);
        }
        
        // Update UI
        if (zoomResetButton != null) {
            zoomResetButton.setText(String.format("%.0f%%", currentZoomFactor * 100));
        }
        updateButtonStates();
    }

    private void applyZoom() {
        if (imageView.getImage() == null) return;
        imageView.setFitWidth(imageView.getImage().getWidth() * currentZoomFactor);
        imageView.setFitHeight(imageView.getImage().getHeight() * currentZoomFactor);
        if (zoomResetButton != null) {
            zoomResetButton.setText(String.format("%.0f%%", currentZoomFactor * 100));
        }
        updateButtonStates(); // Panggil setiap kali zoom berubah
    }

    public void resetZoom() {
        currentZoomFactor = 1.0;
        if (imageView != null && scrollPane != null) {
            imageView.setFitWidth(scrollPane.getWidth() - 2);
            imageView.setFitHeight(scrollPane.getHeight() - 2);
            imageView.setPreserveRatio(true);
        }
        if (zoomResetButton != null) {
            zoomResetButton.setText("100%");
        }
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
        updateButtonStates(); // Panggil juga saat di-reset
    }

    private void updateButtonStates() {
        // Toleransi kecil untuk mengatasi masalah presisi floating point
        double tolerance = 1e-9; 

        if (zoomOutButton != null) {
            // Cek apakah ada level zoom yang lebih kecil
            boolean canZoomOut = false;
            for (double level : ZOOM_LEVELS) {
                if (level < currentZoomFactor - tolerance) {
                    canZoomOut = true;
                    break;
                }
            }
            zoomOutButton.setDisable(!canZoomOut);
        }
        
        if (zoomInButton != null) {
            // Cek apakah ada level zoom yang lebih besar
            boolean canZoomIn = false;
            for (double level : ZOOM_LEVELS) {
                if (level > currentZoomFactor + tolerance) {
                    canZoomIn = true;
                    break;
                }
            }
            zoomInButton.setDisable(!canZoomIn);
        }
    }
}
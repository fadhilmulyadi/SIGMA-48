package com.sigma48.ui.controller.base;

import com.sigma48.ui.controller.MainDashboardController;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public abstract class BaseController {

    protected MainDashboardController mainDashboardController;

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    protected void showStatus(Label statusLabel, String message, boolean isError) {
        if (statusLabel == null) return;
        
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.LIME);
        statusLabel.setManaged(true);
        statusLabel.setVisible(true);

        // Sembunyikan pesan setelah 4 detik
        PauseTransition visiblePause = new PauseTransition(Duration.seconds(4));
        visiblePause.setOnFinished(ev -> {
            statusLabel.setManaged(false);
            statusLabel.setVisible(false);
        });
        visiblePause.play();
    }

    protected void styleAlertDialog(Alert alert) {
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setGraphic(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/sigma48/css/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-dialog");
        
        // Atur style tombol OK atau konfirmasi
        dialogPane.lookupButton(dialogPane.getButtonTypes().get(0)).setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
    }
}
package com.sigma48.ui.controller.listitems;

import com.sigma48.model.MissionStatus;
import com.sigma48.ui.dto.MissionDisplayData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MissionListItemController {

    @FXML HBox missionItemRoot;
    @FXML Rectangle statusIndicatorBar;
    @FXML Label judulMisiLabel;
    @FXML Label targetMisiLabel;
    @FXML Label tujuanSingkatLabel;
    @FXML Label statusTextLabel;
    @FXML Label lastUpdateLabel;
    @FXML Label komandanMisiLabel;

    public void setData(MissionDisplayData data) {
        if (data == null) {
            judulMisiLabel.setText("");
            targetMisiLabel.setText("");
            tujuanSingkatLabel.setText("");
            statusTextLabel.setText("");
            lastUpdateLabel.setText("");
            komandanMisiLabel.setText("");
            missionItemRoot.getStyleClass().setAll("mission-list-item");
            statusIndicatorBar.getStyleClass().setAll("status-indicator-bar");
            return;
        }

        judulMisiLabel.setText(data.getJudul());
        targetMisiLabel.setText(data.getTargetName());
        tujuanSingkatLabel.setText("TUJUAN: " + data.getTujuanSingkat());
        statusTextLabel.setText(data.getStatusDisplayName().toUpperCase());
        lastUpdateLabel.setText(data.getTanggalUpdateFormatted());
        komandanMisiLabel.setText(data.getKomandanName());

        Color statusColor;
        switch (data.getStatusEnum()) {
            case DRAFT_ANALIS:
                statusColor = Color.web("#A0AEC0");
                break;
            case MENUNGGU_PERENCANAAN_KOMANDAN:
            case PLANNED:
                statusColor = Color.web("#0000ff"); 
                break;
            case ACTIVE:
                statusColor = Color.web("#00ff37");
                break;
            case COMPLETED:
                statusColor = Color.web("#40C4FF");
                break;
            case FAILED:
            case CANCELLED:
                statusColor = Color.web("#FF5252");
                break;
            default:
                statusColor = Color.web("#607080");
                break;
        }
        statusIndicatorBar.setFill(statusColor);
    }

    public HBox getRoot() {
        return missionItemRoot;
    }
}
package com.sigma48.ui.controller;

import com.sigma48.manager.TargetManager;
import com.sigma48.model.Target;
import com.sigma48.model.TargetType;
import com.sigma48.model.ThreatLevel;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

public class TargetFormDialogController {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField namaTargetField;
    @FXML private ComboBox<TargetType> tipeTargetComboBox;
    @FXML private TextField lokasiTargetField;
    @FXML private ComboBox<ThreatLevel> ancamanTargetComboBox;
    @FXML private Button saveButton;
    @FXML private Label statusDialogLabel;

    private TargetManager targetManager;
    private Target currentTarget;
    private Optional<Target> resultTarget = Optional.empty();

    public void setTargetManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public void setTarget(Target target) {
        this.currentTarget = (target == null) ? new Target() : target;
        if (target != null) {
            dialogTitleLabel.setText("EDIT TARGET: " + target.getNama());
            namaTargetField.setText(target.getNama());
            lokasiTargetField.setText(target.getLokasi());
            tipeTargetComboBox.setValue(target.getTipe());
            ancamanTargetComboBox.setValue(target.getAncaman());
        } else {
            dialogTitleLabel.setText("TAMBAH TARGET BARU");
        }
    }

    public Optional<Target> getResultTarget() {
        return resultTarget;
    }

    @FXML
    public void initialize() {
        tipeTargetComboBox.setItems(FXCollections.observableArrayList(TargetType.values()));
        ancamanTargetComboBox.setItems(FXCollections.observableArrayList(ThreatLevel.values()));
        statusDialogLabel.setVisible(false);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (namaTargetField.getText().trim().isEmpty() || tipeTargetComboBox.getValue() == null || ancamanTargetComboBox.getValue() == null) {
            showStatus("Nama, Tipe, dan Level Ancaman wajib diisi!", true);
            return;
        }

        currentTarget.setNama(namaTargetField.getText().trim());
        currentTarget.setTipe(tipeTargetComboBox.getValue());
        currentTarget.setLokasi(lokasiTargetField.getText().trim());
        currentTarget.setAncaman(ancamanTargetComboBox.getValue());

        if (targetManager.saveTarget(currentTarget)) {
            resultTarget = Optional.of(currentTarget);
            closeDialog();
        } else {
            showStatus("Gagal menyimpan target. Periksa konsol untuk error.", true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void showStatus(String message, boolean isError) {
        statusDialogLabel.setText(message);
        statusDialogLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusDialogLabel.setVisible(true);
    }

    private void closeDialog() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
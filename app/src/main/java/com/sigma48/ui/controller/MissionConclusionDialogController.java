package com.sigma48.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class MissionConclusionDialogController {

    @FXML
    private Label promptLabel;

    @FXML
    private TextArea notesTextArea;

    public void setup(boolean isSuccess) {
        if (isSuccess) {
            promptLabel.setText("CATATAN AKHIR PENYELESAIAN:");
        } else {
            promptLabel.setText("ALASAN KEGAGALAN MISI:");
        }
    }

    public String getNotes() {
        return notesTextArea.getText();
    }

    public TextArea getNotesTextArea() {
        return notesTextArea;
    }
}
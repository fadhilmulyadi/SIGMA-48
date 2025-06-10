package com.sigma48.ui.controller;

import com.sigma48.model.CoverIdentity;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CoverIdentityDialogController {

    @FXML
    private TextField coverNameField;

    @FXML
    private TextField coverRoleField;

    public void setIdentity(CoverIdentity identity) {
        if (identity != null) {
            coverNameField.setText(identity.getCoverName());
            coverRoleField.setText(identity.getCoverRole());
        }
    }

    public CoverIdentity getUpdatedIdentity() {
        String name = coverNameField.getText().trim();
        String role = coverRoleField.getText().trim();
        String passport = "DOC-S48-" + (System.currentTimeMillis() % 10000); 

        if (name.isEmpty()) {
            return null; // Anggap batal jika nama samaran kosong
        }
        return new CoverIdentity(name, passport, role);
    }
}
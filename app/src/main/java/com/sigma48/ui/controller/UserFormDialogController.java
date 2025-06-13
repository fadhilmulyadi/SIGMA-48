package com.sigma48.ui.controller;

import com.sigma48.manager.UserManager;
import com.sigma48.model.Agent;
import com.sigma48.model.Role;
import com.sigma48.model.User;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserFormDialogController {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Label spesialisasiLabel;
    @FXML private TextField spesialisasiField;
    @FXML private CheckBox isActiveCheckBox;
    @FXML private Button saveButton;
    @FXML private Label statusDialogLabel;
    @FXML private RowConstraints spesialisasiRow;

    private UserManager userManager;
    private User userToEdit;
    private boolean isEditMode = false;
    private Optional<User> resultUser = Optional.empty();

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        this.isEditMode = (user != null);
        setupForm();
    }

    public Optional<User> getResultUser() {
        return resultUser;
    }

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            toggleSpesialisasiField(newVal == Role.AGEN_LAPANGAN);
        });
        toggleSpesialisasiField(false);
    }

    private void setupForm() {
        if (isEditMode) {
            dialogTitleLabel.setText("Edit Pengguna: " + userToEdit.getUsername());
            usernameField.setText(userToEdit.getUsername());
            roleComboBox.setValue(userToEdit.getRole());
            isActiveCheckBox.setSelected(userToEdit.isActive());
            passwordField.setPromptText("Biarkan kosong jika tidak ingin mengubah");

            if (userToEdit instanceof Agent) {
                toggleSpesialisasiField(true);
                spesialisasiField.setText(String.join(", ", ((Agent) userToEdit).getSpesialisasi()));
            }
        } else {
            dialogTitleLabel.setText("Tambah Pengguna Baru");
        }
    }

    private void toggleSpesialisasiField(boolean visible) {
        spesialisasiLabel.setVisible(visible);
        spesialisasiField.setVisible(visible);
        spesialisasiRow.setPrefHeight(visible ? 30.0 : 0);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        Role role = roleComboBox.getValue();
        List<String> spesialisasi = Arrays.stream(spesialisasiField.getText().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        boolean isActive = isActiveCheckBox.isSelected();

        if (username.isEmpty() || role == null || (!isEditMode && password.isEmpty())) {
            showStatus("Username, password (saat membuat), dan peran wajib diisi.", true);
            return;
        }

        if (isEditMode) {
            // Update Logic
            userManager.updateUser(userToEdit.getId(), username, role, isActive, spesialisasi);
            resultUser = userManager.findUserById(userToEdit.getId());
        } else {
            // Create Logic
            resultUser = userManager.createUser(username, password, role, spesialisasi, isActive);
        }

        if (resultUser.isPresent()) {
            showStatus("Data berhasil disimpan!", false);
            closeDialog();
        } else {
            showStatus("Gagal menyimpan data. Username mungkin sudah ada.", true);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void showStatus(String message, boolean isError) {
        statusDialogLabel.setText(message);
        statusDialogLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusDialogLabel.setVisible(true);
        statusDialogLabel.setManaged(true);
    }

    private void closeDialog() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
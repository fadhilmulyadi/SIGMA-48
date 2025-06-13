package com.sigma48.ui.controller;

import com.sigma48.ServiceLocator;
import com.sigma48.manager.TargetManager;
import com.sigma48.model.Target;
import com.sigma48.model.TargetType;
import com.sigma48.model.ThreatLevel;
import com.sigma48.ui.controller.base.BaseController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import java.util.Optional;

public class TargetManagementViewController extends BaseController{

    @FXML private TableView<Target> targetTableView;
    @FXML private TableColumn<Target, String> namaColumn;
    @FXML private TableColumn<Target, TargetType> tipeColumn;
    @FXML private TableColumn<Target, String> lokasiColumn;
    @FXML private TableColumn<Target, ThreatLevel> ancamanColumn;
    @FXML private TableColumn<Target, Void> aksiColumn;
    @FXML private Button tambahTargetButton;
    @FXML private Label statusLabel;

    @FXML private VBox formPanel;
    @FXML private VBox listPanel;
    @FXML private Label formTitleLabel;
    @FXML private TextField namaTargetField;
    @FXML private ComboBox<TargetType> tipeTargetComboBox;
    @FXML private TextField lokasiTargetField;
    @FXML private ComboBox<ThreatLevel> ancamanTargetComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusFormLabel;
    @FXML private TextArea deskripsiTargetArea;


    private TargetManager targetManager;
    private ObservableList<Target> targetData = FXCollections.observableArrayList();
    private Target currentTargetToEdit;

    @FXML
    public void initialize() {
        this.targetManager = ServiceLocator.getTargetManager();
        loadTargets();
        setupTableColumns();
        targetTableView.setItems(targetData);
        targetTableView.setPlaceholder(new Label("Tidak ada data target. Klik 'Tambah Target Baru' untuk memulai."));
        
        // Inisialisasi ComboBox di form
        tipeTargetComboBox.setItems(FXCollections.observableArrayList(TargetType.values()));
        ancamanTargetComboBox.setItems(FXCollections.observableArrayList(ThreatLevel.values()));

        // Sembunyikan form pada awalnya
        formPanel.setVisible(false);
        formPanel.setManaged(false);
    }

    private void setupTableColumns() {
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        tipeColumn.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        lokasiColumn.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        ancamanColumn.setCellValueFactory(new PropertyValueFactory<>("ancaman"));

        aksiColumn.setCellFactory(param -> new TableCell<>() {
            private final Button dossierBtn = new Button("DOSSIER");
            private final Button editBtn = new Button("EDIT");
            private final Button deleteBtn = new Button("HAPUS");
            private final HBox pane = new HBox(10, dossierBtn, editBtn, deleteBtn);

            {
                dossierBtn.getStyleClass().addAll("action-button-xs");
                editBtn.getStyleClass().addAll("action-button-xs");
                deleteBtn.getStyleClass().addAll("action-button-xs", "cancel-button-xs");
                pane.setAlignment(Pos.CENTER);

                dossierBtn.setOnAction(event -> {
                    Target target = getTableView().getItems().get(getIndex());
                    if (mainDashboardController != null) {
                        mainDashboardController.showDossierView(target, null); 
                    }
                });
                
                editBtn.setOnAction(event -> {
                    Target target = getTableView().getItems().get(getIndex());
                    showFormForEdit(target);
                });

                deleteBtn.setOnAction(event -> {
                    Target target = getTableView().getItems().get(getIndex());
                    handleDeleteTarget(target);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadTargets() {
        targetData.clear();
        if (targetManager != null) {
            targetData.addAll(targetManager.getAllTargets());
        }
    }

    @FXML
    private void handleTambahTarget(ActionEvent event) {
        currentTargetToEdit = null;
        formTitleLabel.setText("TAMBAH TARGET BARU");
        clearFormFields();
        showFormPanel(true);
    }
    
    private void showFormForEdit(Target target) {
        currentTargetToEdit = target;
        formTitleLabel.setText("EDIT TARGET: " + target.getNama());
        populateFormFields(target);
        showFormPanel(true);
    }

    private void handleDeleteTarget(Target target) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.initStyle(StageStyle.UNDECORATED);

        alert.initOwner(targetTableView.getScene().getWindow());
        alert.setHeaderText("HAPUS TARGET: " + target.getNama().toUpperCase());
        alert.setContentText("AKSI INI BERSIFAT PERMANEN DAN TIDAK DAPAT DIKEMBALIKAN. LANJUTKAN?");

        alert.setGraphic(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = targetManager.deleteTarget(target.getId());
            if (success) {
                showStatus("Target '" + target.getNama() + "' berhasil dihapus.", false);
                loadTargets(); 
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.initStyle(StageStyle.UNDECORATED);
                errorAlert.initOwner(targetTableView.getScene().getWindow());
                errorAlert.setHeaderText("GAGAL MENGHAPUS");
                errorAlert.setContentText("Gagal menghapus target. Kemungkinan target ini masih terhubung dengan sebuah misi.");
                
                DialogPane errorDialogPane = errorAlert.getDialogPane();
                errorDialogPane.getStylesheets().add(
                    getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
                errorDialogPane.getStyleClass().add("custom-dialog");

                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    private void handleSaveTarget() {
        if (namaTargetField.getText().trim().isEmpty() || tipeTargetComboBox.getValue() == null || ancamanTargetComboBox.getValue() == null) {
            statusFormLabel.setText("Nama, Tipe, dan Level Ancaman wajib diisi!");
            statusFormLabel.setTextFill(Color.RED);
            statusFormLabel.setManaged(true);
            statusFormLabel.setVisible(true);
            return;
        }

        Target targetToSave;
        String successMessage;

        if (currentTargetToEdit != null) { // Mode Edit
            targetToSave = currentTargetToEdit;
            successMessage = "Data target '" + targetToSave.getNama() + "' berhasil diperbarui.";
        } else {
            targetToSave = new Target();
            successMessage = "Target baru berhasil disimpan.";
        }

        targetToSave.setNama(namaTargetField.getText().trim());
        targetToSave.setTipe(tipeTargetComboBox.getValue());
        targetToSave.setLokasi(lokasiTargetField.getText().trim());
        targetToSave.setAncaman(ancamanTargetComboBox.getValue());
        targetToSave.setDeskripsi(deskripsiTargetArea.getText().trim());

        if (targetManager.saveTarget(targetToSave)) {
            showStatus(successMessage, false);
            loadTargets();
            showFormPanel(false);
            clearFormFields();
        } else {
            showStatus("Gagal menyimpan data target.", true);
        }
    }

    @FXML
    private void handleCancel() {
        showFormPanel(false);
        clearFormFields();
    }
    
    // --- Helper Methods ---

    private void showFormPanel(boolean show) {
        formPanel.setVisible(show);
        formPanel.setManaged(show);
        listPanel.setVisible(!show);
        listPanel.setManaged(!show);
    }

    private void clearFormFields() {
        namaTargetField.clear();
        lokasiTargetField.clear();
        deskripsiTargetArea.clear();
        tipeTargetComboBox.getSelectionModel().clearSelection();
        ancamanTargetComboBox.getSelectionModel().clearSelection();
        statusFormLabel.setVisible(false);
    }
    
    private void populateFormFields(Target target) {
        namaTargetField.setText(target.getNama());
        lokasiTargetField.setText(target.getLokasi());
        deskripsiTargetArea.setText(target.getDeskripsi());
        tipeTargetComboBox.setValue(target.getTipe());
        ancamanTargetComboBox.setValue(target.getAncaman());
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.LIME);
    }
}
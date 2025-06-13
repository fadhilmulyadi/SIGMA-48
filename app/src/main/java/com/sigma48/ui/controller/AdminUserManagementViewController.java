package com.sigma48.ui.controller;

import com.sigma48.manager.UserManager;
import com.sigma48.model.Agent;
import com.sigma48.model.Role;
import com.sigma48.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUserManagementViewController {

    @FXML private VBox listPanel;
    @FXML private ScrollPane formScrollPane;
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, Role> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Button tambahUserButton;
    @FXML private Label statusLabel;
    @FXML private Label formTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordLabel;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Label spesialisasiLabel;
    @FXML private TextField spesialisasiField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusFormLabel;
    @FXML private Button toggleStatusInFormBtn;
    @FXML private Button resetPasswordInFormBtn;
    @FXML private Button deleteUserInFormBtn;
    @FXML private CheckBox isActiveCheckBox;
    @FXML private VBox otherActionsBox;
    @FXML private VBox dangerZoneBox;

    private UserManager userManager;
    private final ObservableList<User> userData = FXCollections.observableArrayList();
    private User currentUserToEdit;
    private MainDashboardController mainDashboardController;

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setManagers(UserManager userManager) {
        this.userManager = userManager;
        loadUsers();
    }
    
    @FXML
    public void initialize() {
        setupTableColumns();
        userTableView.setItems(userData);
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> toggleSpesialisasiField(newVal == Role.AGEN_LAPANGAN));
        showFormPanel(false);
    }
    
    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActive() ? "Aktif" : "Non-Aktif"));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button manageBtn = new Button("KELOLA");
            private final HBox pane = new HBox(manageBtn);
            {
                pane.setAlignment(Pos.CENTER);
                manageBtn.getStyleClass().add("action-button-xs");
                manageBtn.setOnAction(event -> showFormForEdit(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadUsers() {
        userData.clear();
        if (userManager != null) {
            userData.addAll(userManager.getAllUsers());
        }
    }

    private void showFormPanel(boolean show) {
        formScrollPane.setVisible(show);
        formScrollPane.setManaged(show);
        listPanel.setVisible(!show);
        listPanel.setManaged(!show);
        if (!show) {
            loadUsers();
        }
    }

    @FXML
    private void handleTambahUser() {
        currentUserToEdit = null;
        formTitleLabel.setText("TAMBAH PENGGUNA BARU");
        clearFormFields();
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        passwordLabel.setVisible(true);
        passwordLabel.setManaged(true);
        otherActionsBox.setVisible(false);
        otherActionsBox.setManaged(false);
        dangerZoneBox.setVisible(false);
        dangerZoneBox.setManaged(false);
        showFormPanel(true);
    }

    private void showFormForEdit(User user) {
        currentUserToEdit = user;
        formTitleLabel.setText("EDIT PENGGUNA: " + user.getUsername());
        populateFormFields(user);
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        passwordLabel.setVisible(false);
        passwordLabel.setManaged(false);
        otherActionsBox.setVisible(true);
        otherActionsBox.setManaged(true);
        dangerZoneBox.setVisible(true);
        dangerZoneBox.setManaged(true);
        if (user.isActive()) {
            toggleStatusInFormBtn.setText("NON-AKTIFKAN AKUN");
            toggleStatusInFormBtn.getStyleClass().setAll("button", "cancel-button");
        } else {
            toggleStatusInFormBtn.setText("AKTIFKAN AKUN");
            toggleStatusInFormBtn.getStyleClass().setAll("button");
        }
        boolean isMainAdmin = user.getUsername().equals("4899");
        toggleStatusInFormBtn.setDisable(isMainAdmin);
        resetPasswordInFormBtn.setDisable(isMainAdmin);
        deleteUserInFormBtn.setDisable(isMainAdmin);
        deleteUserInFormBtn.getStyleClass().setAll("button", "cancel-button");
        showFormPanel(true);
    }

    private void clearFormFields() {
        usernameField.clear();
        passwordField.clear();
        spesialisasiField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        
        if (statusFormLabel != null) {
            statusFormLabel.setVisible(false);
        }
    }

    private void showFormStatus(String message, boolean isError) {
        if (statusFormLabel != null) {
            statusFormLabel.setText(message);
            statusFormLabel.setTextFill(isError ? Color.RED : Color.LIME);
            statusFormLabel.setVisible(true);
            statusFormLabel.setManaged(true);
        }
    }
    
    @FXML
    private void handleSaveUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        Role role = roleComboBox.getValue();
        List<String> spesialisasi = Arrays.stream(spesialisasiField.getText().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if (username.isEmpty() || role == null || (currentUserToEdit == null && password.isEmpty())) {
            showFormStatus("Username, password (saat membuat), dan peran wajib diisi.", true);
            return;
        }
        boolean success;
        if (currentUserToEdit != null) {
            success = userManager.updateUser(currentUserToEdit.getId(), username, role, null, spesialisasi);
        } else {
            Optional<User> newUser = userManager.createUser(username, password, role, spesialisasi, true);
            success = newUser.isPresent();
        }
        if (success) {
            showFormStatus("Data pengguna berhasil disimpan.", false);
            loadUsers();
            if (currentUserToEdit == null) {
                showFormPanel(false);
            }
        } else {
            showFormStatus("Gagal menyimpan. Username mungkin sudah ada.", true);
        }
    }

    @FXML
    private void handleToggleStatusInForm() {
        if (currentUserToEdit == null) return;
        handleToggleStatus(currentUserToEdit);
    }

    @FXML
    private void handleDeleteUserInForm() {
        if (currentUserToEdit == null) return;
        handleDeleteUser(currentUserToEdit);
    }

    @FXML
    private void handleResetPasswordInForm() {
        if (currentUserToEdit == null) return;
        handleResetPassword(currentUserToEdit);
    }
    
    @FXML
    private void handleCancel() {
        showFormPanel(false);
    }

    private void handleToggleStatus(User user) {
        boolean newStatus = !user.isActive();
        String actionText = newStatus ? "mengaktifkan" : "menon-aktifkan";
        Alert alert = createCustomDialog(Alert.AlertType.CONFIRMATION, "KONFIRMASI STATUS", "Anda yakin ingin " + actionText + " pengguna '" + user.getUsername() + "'?");
        alert.showAndWait().filter(res -> res == ButtonType.OK).ifPresent(res -> {
            if (userManager.setUserStatus(user.getId(), newStatus)) {
                showFormStatus("Status berhasil diubah menjadi: " + (newStatus ? "Aktif" : "Non-Aktif"), false);
                loadUsers();
                userManager.findUserById(user.getId()).ifPresent(this::showFormForEdit);
            } else {
                showFormStatus("Gagal mengubah status.", true);
            }
        });
    }
    
    private void handleDeleteUser(User user) {
        Alert alert = createCustomDialog(Alert.AlertType.CONFIRMATION, "HAPUS PENGGUNA", "Aksi ini bersifat permanen. Lanjutkan menghapus '" + user.getUsername() + "'?");
        alert.showAndWait().filter(res -> res == ButtonType.OK).ifPresent(res -> {
            if (userManager.deleteUser(user.getId())) {
                showStatus("Pengguna '" + user.getUsername() + "' berhasil dihapus.", false);
                showFormPanel(false);
            } else {
                showFormStatus("Gagal menghapus pengguna.", true);
            }
        });
    }

    private void handleResetPassword(User user) {
        Dialog<String> dialog = new Dialog<>();
        setupCustomDialogPane(dialog, "RESET PASSWORD: " + user.getUsername().toUpperCase());
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Password baru");
        newPasswordField.getStyleClass().add("form-input-text");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.add(new Label("Password Baru:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> (dialogButton == ButtonType.OK) ? newPasswordField.getText() : null);
        dialog.showAndWait().ifPresent(password -> {
            if (password.trim().isEmpty()) {
                showFormStatus("Password baru tidak boleh kosong.", true);
                return;
            }
            if (userManager.resetUserPassword(user.getId(), password)) {
                showFormStatus("Password untuk '" + user.getUsername() + "' berhasil direset.", false);
            } else {
                showFormStatus("Gagal mereset password.", true);
            }
        });
    }
    
    private void populateFormFields(User user) {
        usernameField.setText(user.getUsername());
        roleComboBox.setValue(user.getRole());
        if (user instanceof Agent) {
            spesialisasiField.setText(String.join(", ", ((Agent) user).getSpesialisasi()));
        } else {
            spesialisasiField.clear();
        }
    }
    
    private void toggleSpesialisasiField(boolean visible) {
        spesialisasiLabel.setVisible(visible);
        spesialisasiLabel.setManaged(visible);
        spesialisasiField.setVisible(visible);
        spesialisasiField.setManaged(visible);
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.ORANGE);
    }

    private Alert createCustomDialog(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(formScrollPane.getScene().getWindow());
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setGraphic(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        return alert;
    }

    private void setupCustomDialogPane(Dialog<?> dialog, String header) {
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(formScrollPane.getScene().getWindow());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setHeaderText(header);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
    }
}
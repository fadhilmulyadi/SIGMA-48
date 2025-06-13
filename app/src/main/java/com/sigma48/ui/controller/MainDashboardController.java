package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.ServiceLocator;
import com.sigma48.model.*;
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.util.SoundUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.util.Optional;

public class MainDashboardController {

    @FXML private BorderPane mainDashboardPane;
    @FXML private StackPane contentAreaPane;
    @FXML private Label headerUserIdLabel;
    @FXML private Label headerLevelLabel;
    @FXML private Button settingsButton;
    @FXML private Button volumeButton;
    @FXML private Button logoutButton;
    @FXML private VBox navigationIconBar;
    @FXML private Label pageInfoLabel;
    @FXML private Label sectionInfoLabel;
    @FXML private Label itemInfoLabel;
    @FXML private Label lineInfoLabel;
    @FXML private Label colInfoLabel;
    @FXML private BorderPane contentPane;

    private User currentUser;
    private Stage primaryStage;

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();

        if (currentUser != null) {
            setupHeader();
            setupFooter();
            setupNavigationIcons(currentUser.getRole());
            loadDefaultRoleDashboard(currentUser.getRole());
        } else {
            contentAreaPane.getChildren().setAll(new Label("Sesi tidak valid. Silakan login kembali."));
        }
    }

    private void setupHeader() {
        headerUserIdLabel.setText(currentUser.getUsername());
        headerLevelLabel.setText("LEVEL " + getClearanceLevel(currentUser.getRole()));
        SoundUtils.playSound(getWelcomeSoundFileName(currentUser.getRole()));
    }

    private void setupFooter() {
        // Sudah Tidak Dipakai
        pageInfoLabel.setText("PAGE: 01");
        sectionInfoLabel.setText("SEC: 01");
        itemInfoLabel.setText("1/1");
        lineInfoLabel.setText("LINE: 01");
        colInfoLabel.setText("COL: 01");
    }

    private void setupNavigationIcons(Role role) {
        navigationIconBar.getChildren().clear();
        if (role != Role.AGEN_LAPANGAN && role != Role.ADMIN) {
            addNavIconButton("icons/home.png", "Dashboard Utama", () -> loadDefaultRoleDashboard(role));
        }

        if (role == Role.DIREKTUR_INTELIJEN || role == Role.ANALIS_INTELIJEN || role == Role.KOMANDAN_OPERASI) {
            addNavIconButton("icons/list.png", "Daftar Misi", () -> loadView("/com/sigma48/fxml/MissionListView.fxml", "ALL_MISSIONS"));
        }
        if (role == Role.DIREKTUR_INTELIJEN || role == Role.ANALIS_INTELIJEN) {
             addNavIconButton("icons/pen.png", "Buat Draft Misi", () -> loadView("/com/sigma48/fxml/MissionCreateForm.fxml", null));
        }
        if (role == Role.AGEN_LAPANGAN) {
            addNavIconButton("icons/list.png", "Misi Saya", () -> loadView("/com/sigma48/fxml/AgentMissionView.fxml", null));
        }
        if (role == Role.ADMIN) {
             addNavIconButton("icons/list.png", "Manajemen Pengguna", () -> loadDefaultRoleDashboard(role));
        }
    }

    private void addNavIconButton(String iconPath, String tooltipText, Runnable action) {
        Button button = new Button();
        try {
            // Menggunakan ImageView lagi
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/com/sigma48/images/" + iconPath)));
            iconView.setFitHeight(22);
            iconView.setFitWidth(22);
            button.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon: " + iconPath);
            // Fallback jika gambar tidak ditemukan
            button.setText(tooltipText.substring(0,1));
        }
        button.setTooltip(new Tooltip(tooltipText));
        button.setOnAction(event -> action.run());
        button.getStyleClass().add("nav-icon-button");
        navigationIconBar.getChildren().add(button);
    }

    // Metode helper untuk mendapatkan teks Level
    private String getClearanceLevel(Role role) {
        switch(role) {
            case DIREKTUR_INTELIJEN: return "07";
            case KOMANDAN_OPERASI: return "06";
            case ANALIS_INTELIJEN: return "05";
            case ADMIN: return "09";
            case AGEN_LAPANGAN: return "04";
            default: return "01";
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private String getWelcomeSoundFileName(Role role) {
        switch (role) {
            case DIREKTUR_INTELIJEN: return "welcome_direktur_intelijen.mp3";
            case ANALIS_INTELIJEN: return "welcome_analis_intelijen.mp3";
            case KOMANDAN_OPERASI: return "welcome_komandan_operasi.mp3";
            case AGEN_LAPANGAN: return "welcome_agen_lapangan.mp3";
            default: return "";
        }
    }

    public void loadDefaultRoleDashboard(Role role) {
        String fxmlPath = "";
        switch (role) {
            case DIREKTUR_INTELIJEN:
                fxmlPath = "/com/sigma48/fxml/DirekturOverview.fxml";
                break;
            case KOMANDAN_OPERASI:
                fxmlPath = "/com/sigma48/fxml/KomandanOverview.fxml";
                break;
            case ANALIS_INTELIJEN:
                fxmlPath = "/com/sigma48/fxml/AnalisOverview.fxml";
                break;
            case AGEN_LAPANGAN:
                fxmlPath = "/com/sigma48/fxml/AgentMissionView.fxml";
                break;
            case ADMIN:
                fxmlPath = "/com/sigma48/fxml/AdminUserManagementView.fxml";
                break;
            default:
                contentAreaPane.getChildren().setAll(new Label("Dashboard untuk peran ini belum ditentukan."));
                return;
        }
        
        if (!fxmlPath.isEmpty()) {
            loadView(fxmlPath, null);
        }
    }

    private void setupNavigationMenu(Role role) {
        navigationIconBar.getChildren().clear();
        Label menuTitle = new Label("MENU UTAMA");
        menuTitle.getStyleClass().add("menu-title");
        navigationIconBar.getChildren().add(menuTitle);

        // Tombol Home/Dashboard Utama (kembali ke overview peran masing-masing)
        addButtonToMenu("Dashboard Utama", () -> loadDefaultRoleDashboard(currentUser.getRole()));

        if (role == Role.DIREKTUR_INTELIJEN) {
            addButtonToMenu("Daftar Semua Misi", () -> loadView("/com/sigma48/fxml/MissionListView.fxml", "ALL_MISSIONS"));
            addButtonToMenu("Buat Draft Misi", () -> loadView("/com/sigma48/fxml/MissionCreateForm.fxml", null));
            addButtonToMenu("Manajemen Target", () -> showTargetManagementView());
        } else if (role == Role.ANALIS_INTELIJEN) {
            addButtonToMenu("Daftar Misi (Draft)", () -> loadView("/com/sigma48/fxml/MissionListView.fxml", MissionStatus.DRAFT_ANALIS.name()));
            addButtonToMenu("Buat Draft Misi", () -> loadView("/com/sigma48/fxml/MissionCreateForm.fxml", null));
            addButtonToMenu("Manajemen Target", () -> showTargetManagementView());
        } else if (role == Role.KOMANDAN_OPERASI) {
            addButtonToMenu("Misi Perlu Direncanakan", () -> loadView("/com/sigma48/fxml/MissionListView.fxml", "FOR_COMMANDER_PLANNING"));
            addButtonToMenu("Semua Misi Saya", () -> loadView("/com/sigma48/fxml/MissionListView.fxml", "FOR_COMMANDER_ALL_ASSIGNED"));
        } else if (role == Role.AGEN_LAPANGAN) {
            addButtonToMenu("Misi Saya", () -> loadView("/com/sigma48/fxml/AgentMissionsView.fxml", null));
        } else if (role == Role.ADMIN) {
             addButtonToMenu("Manajemen Pengguna", () -> System.out.println("ADMIN: Navigasi ke Manajemen Pengguna")); // Placeholder
        }
    }

    private void addButtonToMenu(String buttonText, Runnable action) {
        Button button = new Button(buttonText);
        button.setOnAction(event -> action.run());
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        navigationIconBar.getChildren().add(button);
    }

    public void loadView(String fxmlPath, Object context) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent viewRoot = loader.load();
            Object loadedController = loader.getController();

            if (loadedController instanceof BaseController) {
                ((BaseController) loadedController).setMainDashboardController(this);
            }

            if (loadedController instanceof MissionListViewController && context instanceof String) {
                ((MissionListViewController) loadedController).setListContext((String) context);
                ((MissionListViewController) loadedController).loadData();
            } else if (loadedController instanceof MissionCreateFormController) {
                ((MissionCreateFormController) loadedController).loadInitialData();
            } else if (loadedController instanceof AdminUserManagementViewController) {
                AdminUserManagementViewController controller = (AdminUserManagementViewController) loadedController;
                controller.setMainDashboardController(this);
                controller.setManagers(ServiceLocator.getUserManager());
            }


            contentAreaPane.getChildren().setAll(viewRoot);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error Kritis: Gagal memuat tampilan " + fxmlPath + ". Hubungi administrator.\nDetail: " + e.getMessage()));
        }
    }

    public void showMissionPlanningView(Mission missionToPlan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/MissionPlanningView.fxml"));
            Parent planningViewRoot = loader.load();
            MissionPlanningViewController controller = loader.getController();
            
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainDashboardController(this);
            }
            
            controller.setOnCancelAction(() -> loadView("/com/sigma48/fxml/MissionListView.fxml", "ALL_MISSIONS"));
            controller.loadMissionData(missionToPlan);
            contentAreaPane.getChildren().setAll(planningViewRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan Perencanaan Misi."));
        }
    }
    
    public void showReportSubmissionForm(String missionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/ReportSubmissionForm.fxml"));
            Parent reportFormRoot = loader.load();
            ReportSubmissionFormController controller = loader.getController();
            
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainDashboardController(this);
            }
            
            String missionJudul = ServiceLocator.getMissionManager().getMissionById(missionId).map(Mission::getJudul).orElse("ID: " + missionId);
            controller.setMissionToReport(missionId, missionJudul);
            contentAreaPane.getChildren().setAll(reportFormRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan Submission Form."));
        }
    }
    
    public void showEvaluationForm(Mission missionToEvaluate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/EvaluationForm.fxml"));
            Parent evalFormRoot = loader.load();
            EvaluationFormController controller = loader.getController();
            
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainDashboardController(this);
            }
            
            if (missionToEvaluate != null) {
                controller.setSelectedMission(missionToEvaluate);
            }
            controller.loadInitialData();
            contentAreaPane.getChildren().setAll(evalFormRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan Evaluation Form."));
        }
    }
    
    public void showTargetManagementView() {
        loadView("/com/sigma48/fxml/TargetManagementView.fxml", null);
    }

    public void showReportDetailView(Report report) {
        if (report == null) {
            System.err.println("Gagal membuka detail laporan: Objek laporan null.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/ReportDetailView.fxml"));
            Parent viewRoot = loader.load();
            ReportDetailViewController controller = loader.getController();
            
            // Inisialisasi controller dengan data yang diperlukan
            controller.initializeView(report, this.currentUser, this);

            contentAreaPane.getChildren().setAll(viewRoot);

        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error Kritis: Gagal memuat tampilan Detail Laporan."));
        }
    }

    public Optional<User> showUserFormDialog(User userToEdit, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/UserFormDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(userToEdit == null ? "Tambah Pengguna Baru" : "Edit Pengguna");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner != null ? owner : this.primaryStage);
            
            Scene scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
            dialogStage.setScene(scene);

            UserFormDialogController controller = loader.getController();
            controller.setUserToEdit(userToEdit);
            
            dialogStage.showAndWait();
            return controller.getResultUser();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void showDossierView(Target target, Mission missionToReturnTo) {
        showDossierView(target, missionToReturnTo, "planning"); 
    }

    public void showDossierView(Target target, Mission missionToReturnTo, String returnView) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/DossierView.fxml"));
            Parent viewRoot = loader.load();
            DossierViewController controller = loader.getController();

            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainDashboardController(this);
            }
            
            controller.loadDossierData(target, missionToReturnTo, returnView);
            contentAreaPane.getChildren().setAll(viewRoot);

        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error Kritis: Gagal memuat tampilan Dossier."));
        }
    }

    public void showAgentMissionDetailView(Mission mission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/AgentMissionDetailView.fxml"));
            Parent viewRoot = loader.load();
            AgentMissionDetailViewController controller = loader.getController();

            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainDashboardController(this);
            }
            
            controller.loadMissionDetails(mission);

            contentAreaPane.getChildren().setAll(viewRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat detail misi."));
        }
    }

    public Optional<String> showMissionConclusionDialog(Mission mission, boolean isSuccess) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.initOwner(mainDashboardPane.getScene().getWindow());
            dialog.initStyle(StageStyle.UNDECORATED);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/MissionConclusionDialog.fxml"));
            Parent content = loader.load();
            MissionConclusionDialogController controller = loader.getController();
            controller.setup(isSuccess);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(content);
            dialogPane.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");

            dialog.setTitle(isSuccess ? "MISI SELESAI" : "MISI GAGAL");
            dialogPane.setHeaderText(isSuccess ? "KONFIRMASI: MISI SELESAI" : "PERINGATAN: TANDAI MISI GAGAL");
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Node okButton = dialogPane.lookupButton(ButtonType.OK);
            if (isSuccess) {
                okButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
            } else {
                okButton.getStyleClass().add("button-danger"); 
                okButton.setStyle("-fx-background-color: #E53935; -fx-text-fill: white;");

                okButton.disableProperty().bind(
                    controller.getNotesTextArea().textProperty().isEmpty()
                );
            }

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.getNotes();
                }
                return null;
            });

            return dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void showEvaluationDetailView(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/EvaluationDetailView.fxml"));
            Parent viewRoot = loader.load();
            EvaluationDetailViewController controller = loader.getController();

            // Kirim MainDashboardController dan data evaluasi ke controller baru
            controller.setMainDashboardController(this);
            controller.loadEvaluationDetails(evaluation);

            // Tampilkan view di area konten utama
            contentAreaPane.getChildren().setAll(viewRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan Detail Evaluasi."));
        }
    }

    @FXML
    protected void handleLogoutButtonAction(ActionEvent event) {
        Main.authManager.logout();
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/LoginView.fxml"));
            Parent loginRoot = loader.load();
            
            LoginViewController loginController = loader.getController();
            loginController.setPrimaryStage(currentStage);

            Scene loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
            
            currentStage.setTitle("SIGMA-48");
            currentStage.setScene(loginScene);
            currentStage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper methods
    public void showKomandanOverview() {
        loadDefaultRoleDashboard(Role.KOMANDAN_OPERASI);
    }

    // Getter methods untuk backward compatibility jika diperlukan
    public User getCurrentUser() {
        return this.currentUser;
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }
}
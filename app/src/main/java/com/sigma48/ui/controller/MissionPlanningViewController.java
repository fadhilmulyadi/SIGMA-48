package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.manager.*;
import com.sigma48.model.*;
import com.sigma48.ui.dto.MissionDisplayData;
import com.sigma48.util.SoundUtils;
import com.sigma48.ui.controller.MissionPlanningViewController.AssignedAgentTableData;
import com.sigma48.ui.controller.base.BaseController;
import com.sigma48.ServiceLocator;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MissionPlanningViewController extends BaseController {

    @FXML ScrollPane scrollPane;
    @FXML Label missionTitleLabel;
    @FXML Label missionStatusLabel;
    @FXML Label missionIdText;
    @FXML Label tujuanText;
    @FXML Label deskripsiText;
    @FXML Label targetNameText;
    @FXML Label initialRiskAnalysisText;
    @FXML Label initialOperationTypeText;
    @FXML Label initialLocationText;

    @FXML ComboBox<User> komandanComboBox;
    @FXML Label komandanNameLabel;
    @FXML TextField jenisOperasiField;
    @FXML TextField lokasiField;
    @FXML TextArea analisisRisikoArea;
    @FXML TextArea strategiArea;
    @FXML TextArea protokolArea;
    @FXML private Button viewDossierButton;

    @FXML ComboBox<Agent> availableAgentsComboBox;
    @FXML Button addAgentButton;
    @FXML TableView<AssignedAgentTableData> assignedAgentsTableView;
    @FXML TableColumn<AssignedAgentTableData, String> agentNameColumn;
    @FXML TableColumn<AssignedAgentTableData, String> agentSpesialisasiColumn;
    @FXML TableColumn<AssignedAgentTableData, String> agentCoverNameColumn;
    @FXML TableColumn<AssignedAgentTableData, String> agentCoverRoleColumn;
    @FXML TableColumn<AssignedAgentTableData, Void> agentActionsColumn;

    @FXML TableView<ReportTableData> reportsTableView;
    @FXML TableColumn<ReportTableData, String> reportTimestampColumn;
    @FXML TableColumn<ReportTableData, String> reportAgentIdColumn;
    @FXML TableColumn<ReportTableData, String> reportIsiColumn;
    @FXML TableColumn<ReportTableData, Integer> reportLampiranCountColumn;
    @FXML TableColumn<ReportTableData, Void> reportAksiColumn;
    @FXML Button refreshReportsButton;
    
    @FXML TableView<EvaluationTableData> evaluationsTableView;
    @FXML TableColumn<EvaluationTableData, String> evalTimestampColumn;
    @FXML TableColumn<EvaluationTableData, String> evaluatorColumn;
    @FXML TableColumn<EvaluationTableData, String> evaluatedAgentColumn;
    @FXML TableColumn<EvaluationTableData, String> evalEffectivenessColumn;
    @FXML TableColumn<EvaluationTableData, String> evalNotesColumn;
    @FXML TableColumn<EvaluationTableData, Void> evalAksiColumn;
    @FXML Button refreshEvaluationsButton;
    @FXML Button addEvaluationButton;

    @FXML Label statusPlanLabel;
    @FXML Button cancelPlanButton;
    @FXML Button savePlanButton;
    @FXML Button finalizePlanButton;
    @FXML Button markCompletedButton;
    @FXML Button markFailedButton;
    @FXML private Button cancelMissionButton;

    private MissionManager missionManager;
    private TargetManager targetManager;
    private AgentManager agentManager;
    private UserManager userManager;
    private ReportManager reportManager;
    private EvaluationManager evaluationManager;
    
    private Runnable onCancelAction;
    private Mission currentMission;
    private User currentUser;
    private ObservableList<AssignedAgentTableData> assignedAgentsData = FXCollections.observableArrayList();
    private ObservableList<ReportTableData> reportsData = FXCollections.observableArrayList();
    private ObservableList<EvaluationTableData> evaluationsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
        this.missionManager = ServiceLocator.getMissionManager();
        this.targetManager = ServiceLocator.getTargetManager();
        this.agentManager = ServiceLocator.getAgentManager();
        this.userManager = ServiceLocator.getUserManager();
        this.reportManager = ServiceLocator.getReportManager();
        this.evaluationManager = ServiceLocator.getEvaluationManager();
        setupAssignedAgentsTable();
        setupReportsTable();
        setupEvaluationsTable();
        statusPlanLabel.setManaged(false);
        statusPlanLabel.setVisible(false);
        setButtonsInitialDisabledState(true);
    }
    
    private void setButtonsInitialDisabledState(boolean disabled) {
        savePlanButton.setDisable(disabled);
        finalizePlanButton.setDisable(disabled);
        markCompletedButton.setDisable(disabled);
        markFailedButton.setDisable(disabled);
        addEvaluationButton.setDisable(disabled);
        addAgentButton.setDisable(disabled);
    }

    public void loadMissionData(Mission mission) {
        this.currentMission = mission;
        if (currentMission.getTargetId() != null) {
            targetManager.getTargetById(currentMission.getTargetId())
                .ifPresent(target -> {
                    targetNameText.setText(defaultIfNull(target.getNama()) + " (TIPE: " + (target.getTipe() != null ? target.getTipe().getDisplayName() : "N/A") + ")");
                    viewDossierButton.setDisable(false);
                });
        } else {
            targetNameText.setText("N/A");
            viewDossierButton.setDisable(true);
        }

        missionTitleLabel.setText("DETAIL & PERENCANAAN MISI: " + defaultIfNull(currentMission.getJudul()));
        missionStatusLabel.setText("STATUS: " + (currentMission.getStatus() != null ? currentMission.getStatus().getDisplayName() : "N/A"));
        missionIdText.setText(defaultIfNull(currentMission.getId()));
        tujuanText.setText(defaultIfNull(currentMission.getTujuan()));
        deskripsiText.setText(defaultIfNull(currentMission.getDeskripsi()));
        
        initialRiskAnalysisText.setText(defaultIfNull(currentMission.getAnalisisRisiko()));
        initialOperationTypeText.setText(defaultIfNull(currentMission.getJenisOperasi()));
        initialLocationText.setText(defaultIfNull(currentMission.getLokasi()));

        if (currentMission.getTargetId() != null) {
            targetManager.getTargetById(currentMission.getTargetId())
                .ifPresent(target -> targetNameText.setText(defaultIfNull(target.getNama()) + " (TIPE: " + (target.getTipe() != null ? target.getTipe().getDisplayName() : "N/A") + ")"));
        } else {
            targetNameText.setText("N/A");
        }

        jenisOperasiField.setText(defaultIfNull(currentMission.getJenisOperasi()));
        lokasiField.setText(defaultIfNull(currentMission.getLokasi()));
        analisisRisikoArea.setText(defaultIfNull(currentMission.getAnalisisRisiko()));
        strategiArea.setText(defaultIfNull(currentMission.getStrategi()));
        protokolArea.setText(defaultIfNull(currentMission.getProtokol()));

        setupKomandanSelection();
        loadAvailableAgents();
        loadAssignedAgentsData();
        loadMissionReports();
        loadMissionEvaluations();
        updateAllButtonStates();
    }

    private void setupKomandanSelection() {
        if (currentUser == null) return;
        boolean isDirektur = currentUser.getRole() == Role.DIREKTUR_INTELIJEN;
        boolean isEditableByCurrentRole = (isDirektur && currentMission.getStatus() == MissionStatus.DRAFT_ANALIS) ||
                                           (isDirektur && currentMission.getStatus() == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN);

        komandanComboBox.setManaged(isEditableByCurrentRole);
        komandanComboBox.setVisible(isEditableByCurrentRole);
        komandanNameLabel.setManaged(!isEditableByCurrentRole);
        komandanNameLabel.setVisible(!isEditableByCurrentRole);

        if (isEditableByCurrentRole) {
            komandanComboBox.setDisable(false);
            loadKomandanOptions();
            if (currentMission.getKomandanId() != null && !currentMission.getKomandanId().isEmpty()) {
                userManager.findUserById(currentMission.getKomandanId()).ifPresent(komandanComboBox::setValue);
            } else {
                komandanComboBox.getSelectionModel().clearSelection();
                komandanComboBox.setPromptText("PILIH KOMANDAN OPERASI");
            }
        } else { 
            if (currentMission.getKomandanId() != null && !currentMission.getKomandanId().isEmpty()) {
                userManager.findUserById(currentMission.getKomandanId()).ifPresent(assignedCmdr -> 
                    komandanNameLabel.setText(defaultIfNull(assignedCmdr.getUsername())));
            } else {
                komandanNameLabel.setText("KOMANDAN BELUM DITUGASKAN");
            }
        }
    }

    private void loadKomandanOptions() {
        if (userManager == null) return;
        List<User> komandanList = userManager.getUsersByRole(Role.KOMANDAN_OPERASI);
        komandanComboBox.setItems(FXCollections.observableArrayList(komandanList));
        komandanComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getUsername(): "";
            }
            
            @Override
            public User fromString(String string) {
                return null; 
            }
        });
    }

    private void loadAvailableAgents() {
        if (agentManager == null) return;
        List<Agent> agents = agentManager.getAvailableAgents();
        availableAgentsComboBox.setItems(FXCollections.observableArrayList(agents));
        availableAgentsComboBox.setConverter(new StringConverter<Agent>() {
            @Override
            public String toString(Agent agent) {
                return agent != null ? agent.getUsername() + " (SPESIALISASI: " + (agent.getSpesialisasi() != null && !agent.getSpesialisasi().isEmpty() ? String.join(", ", agent.getSpesialisasi()) : "N/A") + ")" : "";
            }

            @Override
            public Agent fromString(String string) {
                return null;
            }
        });
        availableAgentsComboBox.setPromptText("PILIH AGEN...");
    }

    private void setupAssignedAgentsTable() {
        agentNameColumn.setCellValueFactory(new PropertyValueFactory<>("agentName"));
        agentSpesialisasiColumn.setCellValueFactory(new PropertyValueFactory<>("agentSpesialisasi"));
        agentCoverNameColumn.setCellValueFactory(new PropertyValueFactory<>("agentCoverName"));
        agentCoverRoleColumn.setCellValueFactory(new PropertyValueFactory<>("agentCoverRole"));
        
        Callback<TableColumn<AssignedAgentTableData, Void>, TableCell<AssignedAgentTableData, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btnEditCover = new Button("IDENTITAS");
                private final Button btnRemoveAgent = new Button("HAPUS");
                {
                    btnEditCover.getStyleClass().add("action-button-xs");
                    btnRemoveAgent.getStyleClass().addAll("action-button-xs", "cancel-button-xs");

                    btnEditCover.setOnAction(event -> {
                        AssignedAgentTableData data = getTableView().getItems().get(getIndex());
                        if (data != null) {
                            showCoverIdentityDialog(data.getCoverIdentity(), data.getAgent())
                                .ifPresent(updatedCi -> {
                                    data.setCoverIdentity(updatedCi);
                                    getTableView().refresh();
                                    showStatus(statusPlanLabel, "IDENTITAS SAMARAN UNTUK " + data.getAgentName() + " diperbarui.", false);
                                });
                        }
                    });
                    btnRemoveAgent.setOnAction(event -> {
                        AssignedAgentTableData data = getTableView().getItems().get(getIndex());
                        assignedAgentsData.remove(data);
                        showStatus(statusPlanLabel, "AGEN " + data.getAgentName() + " DIHAPUS DARI TIM.", false);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        boolean missionIsFinal = currentMission != null && 
                                                 (currentMission.getStatus() == MissionStatus.COMPLETED || 
                                                  currentMission.getStatus() == MissionStatus.FAILED || 
                                                  currentMission.getStatus() == MissionStatus.CANCELLED);
                        boolean canEdit = (currentUser.getRole() == Role.DIREKTUR_INTELIJEN || 
                                           (currentUser.getRole() == Role.KOMANDAN_OPERASI && currentUser.getId().equals(currentMission.getKomandanId()))) 
                                          && !missionIsFinal;
                        
                        btnEditCover.setDisable(!canEdit);
                        btnRemoveAgent.setDisable(!canEdit);

                        HBox buttonsBox = new HBox(5, btnEditCover, btnRemoveAgent);
                        setGraphic(buttonsBox);
                    }
                }
            };
        };
        agentActionsColumn.setCellFactory(cellFactory);
        assignedAgentsTableView.setItems(assignedAgentsData);
    }

    private void loadAssignedAgentsData() {
        assignedAgentsData.clear();
        if (currentMission != null && currentMission.getAssignedAgents() != null && userManager != null) {
            for (String agentId : currentMission.getAssignedAgents()) {
                userManager.findUserById(agentId).ifPresent(user -> {
                    if (user instanceof Agent) {
                        Agent agent = (Agent) user;
                        CoverIdentity ci = currentMission.getCoverIdentities() != null ? currentMission.getCoverIdentities().get(agentId) : null;
                        assignedAgentsData.add(new AssignedAgentTableData(agent, ci));
                    }
                });
            }
        }
    }

    private void setupReportsTable() {
        reportTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("waktuLapor"));
        reportAgentIdColumn.setCellValueFactory(new PropertyValueFactory<>("userIdPelapor"));
        reportIsiColumn.setCellValueFactory(new PropertyValueFactory<>("isiSingkat"));
        reportLampiranCountColumn.setCellValueFactory(new PropertyValueFactory<>("jumlahLampiran"));
        
        Callback<TableColumn<ReportTableData, Void>, TableCell<ReportTableData, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btnViewReport = new Button("Detail");
                {
                    btnViewReport.getStyleClass().add("action-button-xs");
                    btnViewReport.setOnAction(event -> {
                        ReportTableData data = getTableView().getItems().get(getIndex());
                        if (data != null && mainDashboardController != null) {
                            mainDashboardController.showReportDetailView(data.getOriginalReport());
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnViewReport);
                }
            };
        };
        reportAksiColumn.setCellFactory(cellFactory);
        reportsTableView.setItems(reportsData);
    }

    private void loadMissionReports() {
        reportsData.clear();
        if (currentMission != null && currentMission.getId() != null && reportManager != null) {
            List<Report> missionReports = reportManager.getReportsForMission(currentMission.getId());
            missionReports.forEach(report -> reportsData.add(new ReportTableData(report, this.userManager)));
        }
    }

    private void setupEvaluationsTable() {
        evalTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestampEvaluasi"));
        evaluatorColumn.setCellValueFactory(new PropertyValueFactory<>("evaluatorName"));
        evaluatedAgentColumn.setCellValueFactory(new PropertyValueFactory<>("agentName"));
        evalEffectivenessColumn.setCellValueFactory(new PropertyValueFactory<>("efektivitasOperasi"));
        evalNotesColumn.setCellValueFactory(new PropertyValueFactory<>("catatanSingkat"));
        
        Callback<TableColumn<EvaluationTableData, Void>, TableCell<EvaluationTableData, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btnViewEval = new Button("DETAIL");
                {
                    btnViewEval.getStyleClass().add("action-button-xs");
                    btnViewEval.setOnAction(event -> {
                        EvaluationTableData data = getTableView().getItems().get(getIndex());
                         if (data != null && mainDashboardController != null) {
                            mainDashboardController.showEvaluationDetailView(data.getOriginalEvaluation());
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnViewEval);
                }
            };
        };
        evalAksiColumn.setCellFactory(cellFactory);
        evaluationsTableView.setItems(evaluationsData);
    }

    private void loadMissionEvaluations() {
        evaluationsData.clear();
        if (currentMission != null && currentMission.getId() != null && evaluationManager != null) {
            List<Evaluation> missionEvals = evaluationManager.getEvaluationsForMission(currentMission.getId());
            missionEvals.forEach(eval -> evaluationsData.add(new EvaluationTableData(eval, this.userManager)));
        }
    }

    @FXML
    private void handleSavePlanButton(ActionEvent event) {
        if (currentMission == null || currentUser == null || missionManager == null) {
            super.showStatus(statusPlanLabel, "Sistem belum siap atau tidak ada misi dipilih.", true);
            return;
        }

        currentMission.setJenisOperasi(jenisOperasiField.getText());
        currentMission.setLokasi(lokasiField.getText());
        currentMission.setAnalisisRisiko(analisisRisikoArea.getText());
        currentMission.setStrategi(strategiArea.getText());
        currentMission.setProtokol(protokolArea.getText());

        boolean saveSuccess = false;
        String successMessage = "";

        if (currentUser.getRole() == Role.DIREKTUR_INTELIJEN) {
            User selectedKomandan = komandanComboBox.getValue();
            if (selectedKomandan != null) {
                currentMission.setKomandanId(selectedKomandan.getId());
            } else if (currentMission.getStatus() == MissionStatus.DRAFT_ANALIS) {
                super.showStatus(statusPlanLabel, "Pilih Komandan Operasi untuk menyetujui draft.", true);
                return;
            }

            if (currentMission.getStatus() == MissionStatus.DRAFT_ANALIS && currentMission.getKomandanId() != null) {
                saveSuccess = missionManager.approveDraftAndAssignCommander(
                    currentMission.getId(), 
                    currentMission.getKomandanId(), 
                    currentUser.getId()
                );
                if (saveSuccess) successMessage = "Draft misi disetujui & Komandan ditugaskan!";
            } else {
                updateAgentsAndCoverIdentitiesFromTableToCurrentMission();
                currentMission.setUpdatedAt(LocalDateTime.now());
                saveSuccess = missionManager.updateFullMissionDetails(currentMission);
                if (saveSuccess) successMessage = "Perubahan detail misi berhasil disimpan!";
            }
        } else if (currentUser.getRole() == Role.KOMANDAN_OPERASI) {
            if (!currentUser.getId().equals(currentMission.getKomandanId())) {
                super.showStatus(statusPlanLabel,"Anda bukan Komandan yang ditugaskan untuk misi ini.", true); return;
            }
            updateAgentsAndCoverIdentitiesFromTableToCurrentMission();
            currentMission.setUpdatedAt(LocalDateTime.now());
            saveSuccess = missionManager.updateFullMissionDetails(currentMission);
            if (saveSuccess) successMessage = "Rencana operasional & penugasan agen berhasil disimpan!";
        } else {
            super.showStatus(statusPlanLabel, "Peran Anda tidak memiliki wewenang untuk menyimpan.", true); return;
        }

        if (saveSuccess) {
            super.showStatus(statusPlanLabel, successMessage, false);
            missionManager.getMissionById(currentMission.getId()).ifPresent(this::loadMissionData);
        } else {
            super.showStatus(statusPlanLabel, "Gagal menyimpan perubahan pada misi.", true);
        }
    }
    
    private void updateAgentsAndCoverIdentitiesFromTableToCurrentMission() {
        if (currentMission == null) return;
        List<String> agentIds = new ArrayList<>();
        Map<String, CoverIdentity> covers = new HashMap<>();
        for (AssignedAgentTableData data : assignedAgentsData) {
            agentIds.add(data.getAgentId());
            if (data.getCoverIdentity() != null && data.getCoverIdentity().getCoverName() != null && !data.getCoverIdentity().getCoverName().isEmpty()) {
                covers.put(data.getAgentId(), data.getCoverIdentity());
            }
        }
        currentMission.setAssignedAgents(agentIds);
        currentMission.setCoverIdentities(covers);
    }

    @FXML
    private void handleAddAgentButton(ActionEvent event) {
        Agent selectedAgent = availableAgentsComboBox.getValue();
        if (selectedAgent == null || currentMission == null) {
            super.showStatus(statusPlanLabel, "Pilih agen dari daftar terlebih dahulu.", true); return;
        }
        boolean alreadyAssigned = assignedAgentsData.stream().anyMatch(data -> data.getAgentId().equals(selectedAgent.getId()));
        if (alreadyAssigned) {
            super.showStatus(statusPlanLabel, "Agen " + selectedAgent.getUsername() + " sudah ada dalam tim.", true); return;
        }

        Optional<CoverIdentity> coverIdentityOpt = showCoverIdentityDialog(null, selectedAgent);
        assignedAgentsData.add(new AssignedAgentTableData(selectedAgent, coverIdentityOpt.orElse(new CoverIdentity("Belum Diatur", "-", "-"))));
        availableAgentsComboBox.getSelectionModel().clearSelection();
        super.showStatus(statusPlanLabel,  "Agen " + selectedAgent.getUsername() + " ditambahkan ke tim. Atur identitas samaran jika perlu.", false);
    }
    
     private Optional<CoverIdentity> showCoverIdentityDialog(CoverIdentity existingCoverIdentity, Agent forAgent) {
        try {
            Dialog<CoverIdentity> dialog = new Dialog<>();
            dialog.setTitle("PENGATURAN IDENTITAS SAMARAN");
            dialog.setHeaderText("ATUR IDENTITAS SAMARAH UNTUK: " + forAgent.getUsername());

            dialog.initOwner(assignedAgentsTableView.getScene().getWindow());
            dialog.initStyle(StageStyle.UNDECORATED);
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/CoverIdentityDialog.fxml"));
            dialogPane.setContent(loader.load());

            CoverIdentityDialogController controller = loader.getController();
            if (existingCoverIdentity != null) {
                controller.setIdentity(existingCoverIdentity);
            }

            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");


            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.getUpdatedIdentity();
                }
                return null;
            });

            return dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            super.showStatus(statusPlanLabel, "Gagal memuat dialog identitas samaran.", true);
            return Optional.empty();
        }
    }

    @FXML
    private void handleFinalizePlanButton(ActionEvent event) {
        if (currentMission == null || currentUser == null || missionManager == null) {
             super.showStatus(statusPlanLabel, "Sistem belum siap atau tidak ada misi.", true); return;
        }
        if (!currentUser.getId().equals(currentMission.getKomandanId())) {
            super.showStatus(statusPlanLabel, "Hanya Komandan yang ditugaskan yang dapat memfinalkan rencana.", true); return;
        }
        boolean success = missionManager.finalizePlanningAndSetReadyForBriefing(currentMission.getId(), currentUser.getId());
        if (success) {
            super.showStatus(statusPlanLabel, "RENCANA MISI TELAH DIFINALKAN SIAP BRIEFING!", false);
            missionManager.getMissionById(currentMission.getId()).ifPresent(this::loadMissionData);
        } else {
            super.showStatus(statusPlanLabel, "Gagal memfinalkan rencana. Pastikan semua data inti (strategi, protokol, agen) sudah terisi.", true);
        }
    }
    
    @FXML
    private void handleMarkCompletedButton(ActionEvent event) {
        if (currentMission == null || currentUser == null || missionManager == null || mainDashboardController == null) return;

        Optional<String> notesOpt = mainDashboardController.showMissionConclusionDialog(currentMission, true);

        notesOpt.ifPresent(notes -> {
            String finalNotes = notes.isEmpty() ? "MISI BERHASIL DISELESAIKAN SESUAI TUJUAN." : notes;

            boolean success = missionManager.concludeMission(currentMission.getId(), MissionStatus.COMPLETED, finalNotes, currentUser.getId());
            if (success) {
                super.showStatus(statusPlanLabel, "MISI '" + currentMission.getJudul() + "' DITANDAI SELESAI.", false);
                missionManager.getMissionById(currentMission.getId()).ifPresent(this::loadMissionData);
            } else {
                super.showStatus(statusPlanLabel, "GAGAL MENANDAI MISI SELESAI.", true);
            }
        });
    }

    @FXML
    private void handleMarkFailedButton(ActionEvent event) {
        if (currentMission == null || currentUser == null || missionManager == null || mainDashboardController == null) return;

        Optional<String> reasonOpt = mainDashboardController.showMissionConclusionDialog(currentMission, false);

        reasonOpt.ifPresent(reason -> {
            boolean success = missionManager.concludeMission(currentMission.getId(), MissionStatus.FAILED, reason, currentUser.getId());
            if (success) {
                super.showStatus(statusPlanLabel, "MISI '" + currentMission.getJudul() + "' DITANDAI GAGAL.", false);
                missionManager.getMissionById(currentMission.getId()).ifPresent(this::loadMissionData);
            } else {
                super.showStatus(statusPlanLabel, "GAGAL MENANDAI MISI GAGAL", true);
            }
        });
    }

    
    @FXML
    private void handleAddEvaluationButton(ActionEvent event) {
        if (mainDashboardController != null && currentMission != null) {
            mainDashboardController.showEvaluationForm(currentMission);
        } else {
            super.showStatus(statusPlanLabel, "Tidak bisa membuka form evaluasi.", true);
        }
    }

    @FXML
    private void handleRefreshEvaluationsButton(ActionEvent event) {
         if (currentMission != null && evaluationManager != null) {
            loadMissionEvaluations();
            super.showStatus(statusPlanLabel, "Daftar evaluasi diperbarui.", false);
        } else {
            super.showStatus(statusPlanLabel, "Pilih misi atau EvaluationManager belum siap.", true);
        }
    }

    @FXML
    private void handleCancelPlanButton(ActionEvent event) {
        if (mainDashboardController != null) {
            mainDashboardController.loadView("/com/sigma48/fxml/MissionListView.fxml", "ALL_MISSIONS");
        } else {
            super.showStatus(statusPlanLabel, "Aksi dibatalkan.", false);
            if (currentMission != null) loadMissionData(currentMission);
        }
    }

    @FXML
    private void handleViewDossier() {
        if (currentMission != null && currentMission.getTargetId() != null && mainDashboardController != null) {
            targetManager.getTargetById(currentMission.getTargetId()).ifPresent(target -> {
                mainDashboardController.showDossierView(target, currentMission);
            });
        }
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    @FXML
    private void handleCancelMission(ActionEvent event) {
        if (currentMission == null || currentUser == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(scrollPane.getScene().getWindow()); 
        super.styleAlertDialog(confirmation);
        confirmation.setTitle("KONFIRMASI PEMBATALAN MISI");
        confirmation.setHeaderText("ANDA YAKIN INGIN MEMBATALKAN MISI INI: " + currentMission.getJudul() + "?");
        confirmation.setContentText("AKSI INI AKAN MENGUBAH MISI MENJADI STATUS DIBATALKAN.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = missionManager.updateMissionStatus(currentMission.getId(), MissionStatus.CANCELLED);
            if (success) {
                super.showStatus(statusPlanLabel, "MISI BERHASIL DIBATALKAN.", false);
                missionManager.getMissionById(currentMission.getId()).ifPresent(this::loadMissionData);
            } else {
                super.showStatus(statusPlanLabel, "GAGAL MEMBATALKAN MISI.", true);
            }
        }
    }

    private void updateAllButtonStates() {
        if (currentMission == null || currentUser == null) {
            savePlanButton.setDisable(true);
            finalizePlanButton.setDisable(true);
            markCompletedButton.setManaged(false); markCompletedButton.setVisible(false);
            markFailedButton.setManaged(false); markFailedButton.setVisible(false);
            addEvaluationButton.setManaged(false); addEvaluationButton.setVisible(false);
            addAgentButton.setDisable(true);
            assignedAgentsTableView.setEditable(false);
            return;
        }

        boolean isDirektur = currentUser.getRole() == Role.DIREKTUR_INTELIJEN;
        boolean isKomandanThisMission = currentUser.getRole() == Role.KOMANDAN_OPERASI &&
                                        currentUser.getId().equals(currentMission.getKomandanId());
        MissionStatus status = currentMission.getStatus();

        boolean canSavePlan = false;
        if (isDirektur) {
            canSavePlan = (status != MissionStatus.COMPLETED && status != MissionStatus.FAILED && status != MissionStatus.CANCELLED);
            savePlanButton.setText(status == MissionStatus.DRAFT_ANALIS ? "SETUJUI DAN TUGASKAN KO" : "SIMPAN PERUBAHAN DETAIL");
        } else if (isKomandanThisMission) {
            canSavePlan = (status == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN || status == MissionStatus.PLANNED);
            savePlanButton.setText("SIMPAN RENCANA DAN PENUGASAN");
        }
        savePlanButton.setDisable(!canSavePlan);

        boolean canFinalize = isKomandanThisMission &&
                            (status == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN || status == MissionStatus.PLANNED) &&
                            (currentMission.getStrategi() != null && !currentMission.getStrategi().isEmpty()) &&
                            (currentMission.getProtokol() != null && !currentMission.getProtokol().isEmpty()) &&
                            (currentMission.getAssignedAgents() != null && !currentMission.getAssignedAgents().isEmpty());
        finalizePlanButton.setDisable(!canFinalize);
        finalizePlanButton.setManaged(isKomandanThisMission || isDirektur);
        finalizePlanButton.setVisible(isKomandanThisMission || isDirektur);

        boolean canConcludeMission = isDirektur &&
                                    (status == MissionStatus.ACTIVE || status == MissionStatus.READY_FOR_BRIEFING || status == MissionStatus.PLANNED);
        markCompletedButton.setManaged(canConcludeMission); markCompletedButton.setVisible(canConcludeMission); markCompletedButton.setDisable(!canConcludeMission);
        markFailedButton.setManaged(canConcludeMission); markFailedButton.setVisible(canConcludeMission); markFailedButton.setDisable(!canConcludeMission);
        
        boolean canEvaluate = (isDirektur || isKomandanThisMission) &&
                            (status == MissionStatus.COMPLETED || status == MissionStatus.FAILED);
        addEvaluationButton.setManaged(canEvaluate); addEvaluationButton.setVisible(canEvaluate); addEvaluationButton.setDisable(!canEvaluate);

        boolean canAddAgent = (isDirektur || isKomandanThisMission) &&
                            (status == MissionStatus.DRAFT_ANALIS || status == MissionStatus.MENUNGGU_PERENCANAAN_KOMANDAN || status == MissionStatus.PLANNED);
        addAgentButton.setDisable(!canAddAgent);
        assignedAgentsTableView.setEditable(canAddAgent);

        if (status == MissionStatus.COMPLETED || status == MissionStatus.FAILED || status == MissionStatus.CANCELLED) {
            savePlanButton.setDisable(true);
            finalizePlanButton.setDisable(true);
            addAgentButton.setDisable(true);
            assignedAgentsTableView.setEditable(false);
            markCompletedButton.setDisable(true);
            markFailedButton.setDisable(true);
        }

        boolean canCancelByDirector = isDirektur && 
                                (status != MissionStatus.COMPLETED && 
                                status != MissionStatus.FAILED && 
                                status != MissionStatus.CANCELLED);

        boolean canCancelByCommander = isKomandanThisMission && 
                                        (status == MissionStatus.PLANNED || 
                                        status == MissionStatus.READY_FOR_BRIEFING || 
                                        status == MissionStatus.ACTIVE);

        boolean canCancel = canCancelByDirector || canCancelByCommander;

        cancelMissionButton.setManaged(canCancel);
        cancelMissionButton.setVisible(canCancel);
        cancelMissionButton.setDisable(!canCancel);
        }

    private String defaultIfNull(String value) {
        return value != null ? value : "";
    }

    public static class AssignedAgentTableData {
        private final Agent agent;
        private CoverIdentity coverIdentity;

        public AssignedAgentTableData(Agent agent, CoverIdentity ci) {
            this.agent = agent; this.coverIdentity = ci;
        }

        public String getAgentName() {
            return agent.getUsername();
        }

        public String getAgentId() {
            return agent.getId();
        }

        public String getAgentSpesialisasi() {
            return agent.getSpesialisasi() != null ? String.join(", ", agent.getSpesialisasi()) : "N/A";
        }

        public String getAgentCoverName() {
            return coverIdentity != null ? defaultIfNull(coverIdentity.getCoverName()) : "BELUM DIATUR";
        }

        public String getAgentCoverRole() {
            return coverIdentity != null ? defaultIfNull(coverIdentity.getCoverRole()) : "-";
        }

        public Agent getAgent() {
            return agent;
        }

        public CoverIdentity getCoverIdentity() {
            return coverIdentity;
        }

        public void setCoverIdentity(CoverIdentity ci) {
            this.coverIdentity = ci;
        }

        private static String defaultIfNull(String val) {
            return val == null ? "" : val;
        }
    }

    public static class ReportTableData {
        private final SimpleStringProperty reportId;
        private final SimpleStringProperty waktuLapor;
        private final SimpleStringProperty userIdPelapor;
        private final SimpleStringProperty isiSingkat;
        private final SimpleIntegerProperty jumlahLampiran;
        private final Report originalReport;
        public ReportTableData(Report report, UserManager userManager) {
            this.originalReport = report;
            this.reportId = new SimpleStringProperty(report.getReportId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            this.waktuLapor = new SimpleStringProperty(report.getWaktuLapor() != null ? report.getWaktuLapor().format(formatter) : "N/A");
            String pelapor = report.getUserId();
            if(userManager != null) pelapor = userManager.findUserById(report.getUserId()).map(User::getUsername).orElse(report.getUserId());
            this.userIdPelapor = new SimpleStringProperty(pelapor);
            String isi = report.getIsi();
            this.isiSingkat = new SimpleStringProperty(isi != null && isi.length() > 70 ? isi.substring(0, 70) + "..." : isi);
            this.jumlahLampiran = new SimpleIntegerProperty(report.getLampiran() != null ? report.getLampiran().size() : 0);
        }
        public String getReportId() { return reportId.get(); }
        public String getWaktuLapor() { return waktuLapor.get(); }
        public String getUserIdPelapor() { return userIdPelapor.get(); }
        public String getIsiSingkat() { return isiSingkat.get(); }
        public Integer getJumlahLampiran() { return jumlahLampiran.get(); }
        public Report getOriginalReport() { return originalReport; }
    }

    public static class EvaluationTableData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty timestampEvaluasi;
        private final SimpleStringProperty evaluatorName;
        private final SimpleStringProperty agentName;
        private final SimpleStringProperty efektivitasOperasi;
        private final SimpleStringProperty catatanSingkat;
        private final Evaluation originalEvaluation;
        public EvaluationTableData(Evaluation eval, UserManager userManager) {
            this.originalEvaluation = eval;
            this.id = new SimpleStringProperty(eval.getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            this.timestampEvaluasi = new SimpleStringProperty(eval.getTimestampEvaluasi()!=null ? eval.getTimestampEvaluasi().format(formatter) : "N/A");
            String evaluator = eval.getEvaluatorId();
            if(userManager!=null) evaluator = userManager.findUserById(eval.getEvaluatorId()).map(User::getUsername).orElse(eval.getEvaluatorId());
            this.evaluatorName = new SimpleStringProperty(evaluator);
            String agent = "MISI KESELURUHAN";
            if (eval.getAgentId() != null && !eval.getAgentId().isEmpty() && userManager != null) {
                agent = userManager.findUserById(eval.getAgentId()).map(User::getUsername).orElse(eval.getAgentId());
            } else if (eval.getAgentId() != null && !eval.getAgentId().isEmpty()) agent = eval.getAgentId();
            this.agentName = new SimpleStringProperty(agent);
            this.efektivitasOperasi = new SimpleStringProperty(eval.getEfektivitasOperasi()!=null ? eval.getEfektivitasOperasi().getDisplayName() : "N/A");
            String catatan = eval.getCatatanUmumEvaluator();
            this.catatanSingkat = new SimpleStringProperty(catatan != null && catatan.length() > 50 ? catatan.substring(0, 50) + "..." : catatan);
        }
        public String getId() { return id.get(); }
        public String getTimestampEvaluasi() { return timestampEvaluasi.get(); }
        public String getEvaluatorName() { return evaluatorName.get(); }
        public String getAgentName() { return agentName.get(); }
        public String getEfektivitasOperasi() { return efektivitasOperasi.get(); }
        public String getCatatanSingkat() { return catatanSingkat.get(); }
        public Evaluation getOriginalEvaluation() { return originalEvaluation; }
    }
}
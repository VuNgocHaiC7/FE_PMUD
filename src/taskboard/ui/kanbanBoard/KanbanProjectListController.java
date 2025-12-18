package taskboard.ui.kanbanBoard;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;

import java.io.IOException;
import java.util.List;

public class KanbanProjectListController {
    @FXML private TableView<ProjectDTO> tblProjects;
    @FXML private TableColumn<ProjectDTO, String> colName;
    @FXML private TableColumn<ProjectDTO, String> colPM;
    @FXML private TableColumn<ProjectDTO, String> colStatus;
    @FXML private TableColumn<ProjectDTO, String> colStart;
    @FXML private TableColumn<ProjectDTO, String> colEnd;
    
    // New UI elements
    @FXML private FlowPane projectCardsPane;
    @FXML private Label lblProjectCount;
    @FXML private VBox emptyState;
    
    private List<ProjectDTO> currentProjects;

    public void initialize() {
        try {
            System.out.println("🔄 Initializing KanbanProjectListController...");
            
            // Check if new UI elements are loaded
            System.out.println("FlowPane: " + (projectCardsPane != null ? "OK" : "NULL"));
            System.out.println("Project Count Label: " + (lblProjectCount != null ? "OK" : "NULL"));
            System.out.println("Empty State: " + (emptyState != null ? "OK" : "NULL"));
            
            setupColumns();

            // Setup Double-Click to open Kanban board (keep for compatibility)
            tblProjects.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tblProjects.getSelectionModel().getSelectedItem() != null) {
                    openKanbanBoard(tblProjects.getSelectionModel().getSelectedItem());
                }
            });

            loadData();
            
            System.out.println("✅ KanbanProjectListController initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi trong initialize: " + e.getMessage());
        }
    }

    private void setupColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPM.setCellValueFactory(new PropertyValueFactory<>("pmName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // Status column with badge style
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setMaxWidth(Double.MAX_VALUE);
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String displayText = item;
                    switch (item.toUpperCase()) {
                        case "PLANNING":
                            displayText = "LẬP KẾ HOẠCH";
                            badge.setStyle("-fx-background-color: #fef5e7; -fx-text-fill: #d68910; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "IN_PROGRESS":
                        case "ACTIVE":
                            displayText = "ĐANG HOẠT ĐỘNG";
                            badge.setStyle("-fx-background-color: #ebf8ff; -fx-text-fill: #2c5282; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "COMPLETED":
                            displayText = "HOÀN THÀNH";
                            badge.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "CLOSED":
                            displayText = "ĐÃ ĐÓNG";
                            badge.setStyle("-fx-background-color: #f0f4f8; -fx-text-fill: #4a5568; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        default:
                            badge.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #718096; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                    }
                    badge.setText(displayText);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }


    private void loadData() {
        new Thread(() -> {
            try {
                System.out.println("📊 Loading all projects...");
                
                // Lấy tất cả dự án
                List<ProjectDTO> projects = ProjectApi.getProjects();
                System.out.println("✅ Loaded " + projects.size() + " projects");
                
                // Cập nhật UI trên JavaFX thread
                Platform.runLater(() -> {
                    currentProjects = projects;
                    tblProjects.setItems(FXCollections.observableArrayList(currentProjects));
                    updateCardView();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Lỗi load data: " + e.getMessage());
                Platform.runLater(() -> {
                    currentProjects = List.of();
                    tblProjects.setItems(FXCollections.observableArrayList());
                    updateCardView();
                });
            }
        }).start();
    }
    
    private void updateCardView() {
        try {
            System.out.println("🔄 Updating card view...");
            
            if (projectCardsPane == null) {
                System.err.println("❌ projectCardsPane is NULL! Check FXML fx:id");
                return;
            }
            
            projectCardsPane.getChildren().clear();
            
            if (currentProjects == null || currentProjects.isEmpty()) {
                if (emptyState != null) {
                    emptyState.setVisible(true);
                    emptyState.setManaged(true);
                }
                if (lblProjectCount != null) {
                    lblProjectCount.setText("0 dự án");
                }
                System.out.println("ℹ️ No projects to display");
                return;
            }
            
            if (emptyState != null) {
                emptyState.setVisible(false);
                emptyState.setManaged(false);
            }
            
            if (lblProjectCount != null) {
                lblProjectCount.setText(currentProjects.size() + " dự án");
            }
            
            System.out.println("📦 Creating " + currentProjects.size() + " project cards...");
            for (ProjectDTO project : currentProjects) {
                VBox card = createProjectCard(project);
                projectCardsPane.getChildren().add(card);
            }
            System.out.println("✅ Card view updated successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error updating card view: " + e.getMessage());
        }
    }
    
    private VBox createProjectCard(ProjectDTO project) {
        try {
            VBox card = new VBox(12);
            card.getStyleClass().add("project-card");
            
            // Header: Project Name + Status Badge
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label nameLabel = new Label(project.getName() != null ? project.getName() : "Dự án");
            nameLabel.getStyleClass().add("card-project-name");
            nameLabel.setMaxWidth(200);
            nameLabel.setWrapText(true);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            
            String status = project.getStatus() != null ? project.getStatus() : "PLANNING";
            Label statusBadge = new Label(getStatusText(status));
            statusBadge.getStyleClass().addAll("status-badge", "badge-" + status.toLowerCase());
            
            header.getChildren().addAll(nameLabel, statusBadge);
        
        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #e2e8f0;");
        
        // PM Section
        VBox pmSection = new VBox(5);
        Label pmTitle = new Label("QUẢN LÝ DỰ ÁN");
        pmTitle.getStyleClass().add("card-section-title");
        
        HBox pmBox = new HBox(8);
        pmBox.setAlignment(Pos.CENTER_LEFT);
        Label pmIcon = new Label("👤");
        pmIcon.setStyle("-fx-font-size: 14px;");
        Label pmName = new Label(project.getPmName() != null ? project.getPmName() : "Chưa có PM");
        pmName.getStyleClass().add("card-pm-name");
        pmBox.getChildren().addAll(pmIcon, pmName);
        
        pmSection.getChildren().addAll(pmTitle, pmBox);
        
        // Dates Section
        HBox datesBox = new HBox(20);
        datesBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox startDateBox = new VBox(5);
        Label startTitle = new Label("BẮT ĐẦU");
        startTitle.getStyleClass().add("card-section-title");
        HBox startBox = new HBox(6);
        startBox.setAlignment(Pos.CENTER_LEFT);
        Label startIcon = new Label("📅");
        startIcon.getStyleClass().add("card-date-icon");
        String startDateText = (project.getStartDate() != null) ? project.getStartDate().toString() : "N/A";
        Label startDate = new Label(startDateText);
        startDate.getStyleClass().add("card-date");
        startBox.getChildren().addAll(startIcon, startDate);
        startDateBox.getChildren().addAll(startTitle, startBox);
        
        VBox endDateBox = new VBox(5);
        Label endTitle = new Label("KẾT THÚC");
        endTitle.getStyleClass().add("card-section-title");
        HBox endBox = new HBox(6);
        endBox.setAlignment(Pos.CENTER_LEFT);
        Label endIcon = new Label("🏁");
        endIcon.getStyleClass().add("card-date-icon");
        String endDateText = (project.getEndDate() != null) ? project.getEndDate().toString() : "N/A";
        Label endDate = new Label(endDateText);
        endDate.getStyleClass().add("card-date");
        endBox.getChildren().addAll(endIcon, endDate);
        endDateBox.getChildren().addAll(endTitle, endBox);
        
        datesBox.getChildren().addAll(startDateBox, endDateBox);
        
        // Add all sections to card
        card.getChildren().addAll(header, divider, pmSection, datesBox);
        
        // Click event to open Kanban board
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openKanbanBoard(project);
            }
        });
        
        // Hover effect (enhanced by CSS)
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-cursor: hand;"));
        
        return card;
        
        } catch (Exception e) {
            System.err.println("❌ Error creating card for project: " + project.getName());
            e.printStackTrace();
            // Return a simple error card
            VBox errorCard = new VBox(10);
            errorCard.setStyle("-fx-background-color: #fee2e2; -fx-padding: 20; -fx-border-radius: 8; -fx-background-radius: 8;");
            Label errorLabel = new Label("⚠️ Lỗi hiển thị: " + (project.getName() != null ? project.getName() : ""));
            errorCard.getChildren().add(errorLabel);
            return errorCard;
        }
    }
    
    private String getStatusText(String status) {
        if (status == null) return "N/A";
        switch (status.toUpperCase()) {
            case "PLANNING":
                return "LẬP KẾ HOẠCH";
            case "IN_PROGRESS":
            case "ACTIVE":
                return "ĐANG HOẠT ĐỘNG";
            case "COMPLETED":
                return "HOÀN THÀNH";
            case "CLOSED":
                return "ĐÃ ĐÓNG";
            default:
                return status;
        }
    }

    // Mở bảng Kanban cho project đã chọn
    private void openKanbanBoard(ProjectDTO project) {
        try {
            System.out.println("🔄 Opening Kanban Board for project: " + project.getName() + " (ID: " + project.getId() + ")");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/kanbanBoard/BoardView.fxml"));
            System.out.println("✅ FXML loaded successfully");
            
            Parent root = loader.load();
            System.out.println("✅ Root loaded successfully");
            
            BoardController controller = loader.getController();
            System.out.println("✅ Controller obtained: " + controller);
            
            controller.setProjectId(project.getId());
            System.out.println("✅ Project ID set");

            Stage stage = new Stage();
            stage.setTitle("Kanban Board - " + project.getName());
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();
            
            System.out.println("✅ Kanban Board opened successfully!");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ IOException: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi mở Kanban Board: " + e.getMessage());
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Exception: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi không xác định: " + e.getMessage());
            alert.show();
        }
    }
}

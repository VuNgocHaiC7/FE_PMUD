package taskboard.ui.dashboard;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import taskboard.api.DashboardApi;

import java.util.Map;

public class DashboardController {

    @FXML private Label lblTotalProjects;
    @FXML private Label lblTotalTasks;
    @FXML private Label lblCompletedTasks;
    
    @FXML private PieChart chartTasksByStatus;
    @FXML private PieChart chartProjectsByStatus;
    @FXML private PieChart chartTasksByPriority;
    
    @FXML private BarChart<String, Number> chartTasksByProject;
    @FXML private CategoryAxis projectAxis;
    @FXML private NumberAxis taskCountAxis;

    @FXML
    public void initialize() {
        System.out.println("=== DashboardController initialized ===");
        
        // Set default values immediately so UI is visible
        Platform.runLater(() -> {
            if (lblTotalProjects != null) lblTotalProjects.setText("0");
            if (lblTotalTasks != null) lblTotalTasks.setText("0");
            if (lblCompletedTasks != null) lblCompletedTasks.setText("0");
        });
        
        // Load data in background
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Load dữ liệu trong background thread để không block UI
        new Thread(() -> {
            try {
                Map<String, Object> stats = DashboardApi.getDashboardStats();
                
                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    updateStatistics(stats);
                    updateCharts(stats);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi load dashboard data: " + e.getMessage());
                
                Platform.runLater(() -> {
                    lblTotalProjects.setText("N/A");
                    lblTotalTasks.setText("N/A");
                    lblCompletedTasks.setText("N/A");
                });
            }
        }).start();
    }

    private void updateStatistics(Map<String, Object> stats) {
        // Cập nhật các số liệu thống kê
        int totalProjects = (int) stats.getOrDefault("totalProjects", 0);
        int totalTasks = (int) stats.getOrDefault("totalTasks", 0);
        
        lblTotalProjects.setText(String.valueOf(totalProjects));
        lblTotalTasks.setText(String.valueOf(totalTasks));
        
        // Lấy số tasks hoàn thành từ tasksByStatus
        @SuppressWarnings("unchecked")
        Map<String, Integer> tasksByStatus = (Map<String, Integer>) stats.get("tasksByStatus");
        if (tasksByStatus != null) {
            int completed = tasksByStatus.getOrDefault("Done", 0) + 
                           tasksByStatus.getOrDefault("DONE", 0);
            lblCompletedTasks.setText(String.valueOf(completed));
        } else {
            lblCompletedTasks.setText("0");
        }
    }

    private void updateCharts(Map<String, Object> stats) {
        updateTasksByStatusChart(stats);
        updateTasksByProjectChart(stats);
        updateProjectsByStatusChart(stats);
        updateTasksByPriorityChart(stats);
    }

    @SuppressWarnings("unchecked")
    private void updateTasksByStatusChart(Map<String, Object> stats) {
        Map<String, Integer> tasksByStatus = (Map<String, Integer>) stats.get("tasksByStatus");
        if (tasksByStatus == null || tasksByStatus.isEmpty()) {
            // Hiển thị message nếu không có data
            PieChart.Data noData = new PieChart.Data("Không có dữ liệu", 1);
            chartTasksByStatus.setData(FXCollections.observableArrayList(noData));
            return;
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        tasksByStatus.forEach((status, count) -> {
            String displayName = getStatusDisplayName(status);
            pieChartData.add(new PieChart.Data(displayName + " (" + count + ")", count));
        });
        
        chartTasksByStatus.setData(pieChartData);
        applyPieChartColors(chartTasksByStatus);
    }

    @SuppressWarnings("unchecked")
    private void updateTasksByProjectChart(Map<String, Object> stats) {
        Map<String, Integer> tasksByProject = (Map<String, Integer>) stats.get("tasksByProject");
        if (tasksByProject == null || tasksByProject.isEmpty()) {
            return;
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng Task");
        
        tasksByProject.forEach((projectName, count) -> {
            // Rút ngắn tên project nếu quá dài
            String displayName = projectName.length() > 20 ? 
                                projectName.substring(0, 17) + "..." : projectName;
            series.getData().add(new XYChart.Data<>(displayName, count));
        });
        
        chartTasksByProject.getData().clear();
        chartTasksByProject.getData().add(series);
        
        // Tùy chỉnh style cho BarChart
        chartTasksByProject.setStyle("-fx-bar-fill: #6366f1;");
    }

    @SuppressWarnings("unchecked")
    private void updateProjectsByStatusChart(Map<String, Object> stats) {
        Map<String, Integer> projectsByStatus = (Map<String, Integer>) stats.get("projectsByStatus");
        if (projectsByStatus == null || projectsByStatus.isEmpty()) {
            PieChart.Data noData = new PieChart.Data("Không có dữ liệu", 1);
            chartProjectsByStatus.setData(FXCollections.observableArrayList(noData));
            return;
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        projectsByStatus.forEach((status, count) -> {
            String displayName = getProjectStatusDisplayName(status);
            pieChartData.add(new PieChart.Data(displayName + " (" + count + ")", count));
        });
        
        chartProjectsByStatus.setData(pieChartData);
        applyPieChartColors(chartProjectsByStatus);
    }

    @SuppressWarnings("unchecked")
    private void updateTasksByPriorityChart(Map<String, Object> stats) {
        Map<String, Integer> tasksByPriority = (Map<String, Integer>) stats.get("tasksByPriority");
        if (tasksByPriority == null || tasksByPriority.isEmpty()) {
            PieChart.Data noData = new PieChart.Data("Không có dữ liệu", 1);
            chartTasksByPriority.setData(FXCollections.observableArrayList(noData));
            return;
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        tasksByPriority.forEach((priority, count) -> {
            String displayName = getPriorityDisplayName(priority);
            pieChartData.add(new PieChart.Data(displayName + " (" + count + ")", count));
        });
        
        chartTasksByPriority.setData(pieChartData);
        applyPieChartColors(chartTasksByPriority);
    }

    private void applyPieChartColors(PieChart chart) {
        // Áp dụng màu sắc cho các phần của PieChart
        chart.setLegendVisible(true);
    }

    private String getStatusDisplayName(String status) {
        switch (status.toUpperCase()) {
            case "TODO":
                return "Chờ làm";
            case "INPROGRESS":
            case "IN_PROGRESS":
            case "DOING":
                return "Đang làm";
            case "DONE":
            case "COMPLETED":
                return "Hoàn thành";
            case "BLOCKED":
                return "Bị chặn";
            default:
                return status;
        }
    }

    private String getProjectStatusDisplayName(String status) {
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "Đang hoạt động";
            case "COMPLETED":
                return "Hoàn thành";
            case "CLOSED":
                return "Đã đóng";
            case "PLANNING":
                return "Đang lên kế hoạch";
            case "IN_PROGRESS":
                return "Đang thực hiện";
            default:
                return status;
        }
    }

    private String getPriorityDisplayName(String priority) {
        switch (priority.toUpperCase()) {
            case "HIGH":
                return "Cao";
            case "MEDIUM":
                return "Trung bình";
            case "LOW":
                return "Thấp";
            default:
                return priority;
        }
    }
}

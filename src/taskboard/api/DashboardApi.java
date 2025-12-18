package taskboard.api;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class DashboardApi {
    
    // Lấy thống kê dashboard
    public static Map<String, Object> getDashboardStats() throws Exception {
        String responseJson = ApiClient.get("/dashboard/stats");
        return parseStats(responseJson);
    }
    
    // Parse JSON response thành Map
    private static Map<String, Object> parseStats(String jsonResponse) {
        Map<String, Object> stats = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            
            stats.put("totalProjects", obj.optInt("totalProjects", 0));
            stats.put("totalTasks", obj.optInt("totalTasks", 0));
            
            // Parse tasksByStatus
            if (obj.has("tasksByStatus")) {
                JSONObject tasksByStatus = obj.getJSONObject("tasksByStatus");
                Map<String, Integer> statusMap = new HashMap<>();
                for (String key : tasksByStatus.keySet()) {
                    statusMap.put(key, tasksByStatus.getInt(key));
                }
                stats.put("tasksByStatus", statusMap);
            }
            
            // Parse tasksByProject
            if (obj.has("tasksByProject")) {
                JSONObject tasksByProject = obj.getJSONObject("tasksByProject");
                Map<String, Integer> projectMap = new HashMap<>();
                for (String key : tasksByProject.keySet()) {
                    projectMap.put(key, tasksByProject.getInt(key));
                }
                stats.put("tasksByProject", projectMap);
            }
            
            // Parse projectsByStatus
            if (obj.has("projectsByStatus")) {
                JSONObject projectsByStatus = obj.getJSONObject("projectsByStatus");
                Map<String, Integer> projectStatusMap = new HashMap<>();
                for (String key : projectsByStatus.keySet()) {
                    projectStatusMap.put(key, projectsByStatus.getInt(key));
                }
                stats.put("projectsByStatus", projectStatusMap);
            }
            
            // Parse tasksByPriority
            if (obj.has("tasksByPriority")) {
                JSONObject tasksByPriority = obj.getJSONObject("tasksByPriority");
                Map<String, Integer> priorityMap = new HashMap<>();
                for (String key : tasksByPriority.keySet()) {
                    priorityMap.put(key, tasksByPriority.getInt(key));
                }
                stats.put("tasksByPriority", priorityMap);
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON dashboard stats: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }
}

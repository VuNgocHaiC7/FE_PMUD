package taskboard.api;

import taskboard.model.TaskDTO;
import taskboard.model.CommentDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskApi {
    // Chuyển sang false để kết nối server thật
    private static final boolean IS_MOCK = false;

    // --- MOCK DATA STORE - Chỉ dùng khi IS_MOCK = true ---
    private static List<TaskDTO> mockTasks = new ArrayList<>();
    private static List<CommentDTO> mockComments = new ArrayList<>();
    private static Long nextTaskId = 1L;
    private static Long nextCommentId = 1L;

    static {
        // Dữ liệu mẫu cho project ID = 1
        mockTasks.add(new TaskDTO(nextTaskId++, "Thiết kế giao diện", "Thiết kế UI/UX cho trang chủ", "ToDo", "Nguyễn Văn A", 1L));
        mockTasks.add(new TaskDTO(nextTaskId++, "Xây dựng API", "Tạo REST API cho module user", "InProgress", "Trần Thị B", 1L));
        mockTasks.add(new TaskDTO(nextTaskId++, "Viết unit test", "Test coverage cho service layer", "Done", "Lê Văn C", 1L));
        mockTasks.add(new TaskDTO(nextTaskId++, "Tích hợp thanh toán", "Kết nối payment gateway", "ToDo", "", 1L));
        mockTasks.add(new TaskDTO(nextTaskId++, "Fix bug authentication", "Sửa lỗi đăng nhập không hoạt động", "Blocked", "Nguyễn Văn A", 1L));
        
        // Dữ liệu mẫu cho project ID = 2
        mockTasks.add(new TaskDTO(nextTaskId++, "Phân tích yêu cầu", "Gặp khách hàng để thu thập yêu cầu", "Done", "Phạm Thị D", 2L));
        mockTasks.add(new TaskDTO(nextTaskId++, "Setup môi trường", "Cài đặt server và database", "InProgress", "Hoàng Quản Lý", 2L));
        
        // Mock comments cho task 1
        mockComments.add(new CommentDTO(nextCommentId++, 1L, "admin", "Cần hoàn thành trước ngày 20", LocalDateTime.now()));
        mockComments.add(new CommentDTO(nextCommentId++, 1L, "dev1", "Đã bắt đầu làm", LocalDateTime.now().plusHours(1)));
    }

    // === TASK ENDPOINTS ===

    // 1. POST /api/tasks - Tạo Task mới (Chỉ Admin)
    public static TaskDTO createTask(TaskDTO task) throws Exception {
        if (IS_MOCK) {
            task.setId(nextTaskId++);
            mockTasks.add(task);
            System.out.println("API: Created task " + task.getTitle());
            return task;
        } else {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", task.getTitle());
            jsonBody.put("description", task.getDescription());
            jsonBody.put("priority", task.getStatus());
            jsonBody.put("projectId", task.getProjectId());
            
            // Gửi danh sách assigneeIds thay vì một assigneeId
            if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
                jsonBody.put("assigneeIds", new JSONArray(task.getAssigneeIds()));
            }
            
            String responseJson = ApiClient.post("/tasks", jsonBody.toString());
            return parseTask(responseJson);
        }
    }

    // 2. PUT /api/tasks/{id} - Sửa thông tin Task: Tên, Mô tả, Deadline... (Chỉ Admin)
    public static void updateTask(TaskDTO task) throws Exception {
        if (IS_MOCK) {
            for (int i = 0; i < mockTasks.size(); i++) {
                if (mockTasks.get(i).getId().equals(task.getId())) {
                    mockTasks.set(i, task);
                    System.out.println("API: Updated task " + task.getTitle());
                    return;
                }
            }
        } else {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", task.getTitle());
            jsonBody.put("description", task.getDescription());
            jsonBody.put("priority", task.getStatus());
            
            // Gửi danh sách assigneeIds thay vì một assigneeId
            if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
                jsonBody.put("assigneeIds", new JSONArray(task.getAssigneeIds()));
            }
            
            ApiClient.put("/tasks/" + task.getId(), jsonBody.toString());
        }
    }

    // 3. DELETE /api/tasks/{id} - Xóa Task (Chỉ Admin)
    public static void deleteTask(Long taskId) throws Exception {
        if (IS_MOCK) {
            boolean removed = mockTasks.removeIf(task -> task.getId().equals(taskId));
            if (removed) {
                System.out.println("API: Deleted task " + taskId);
            }
        } else {
            ApiClient.delete("/tasks/" + taskId);
        }
    }

    // 4. GET /api/projects/{id}/tasks - Lấy danh sách Task (Admin & Member)
    public static List<TaskDTO> getTasksByProject(Long projectId) throws Exception {
        if (IS_MOCK) {
            System.out.println("API: Lấy tasks của project " + projectId);
            return mockTasks.stream()
                    .filter(task -> task.getProjectId().equals(projectId))
                    .collect(Collectors.toList());
        } else {
            String responseJson = ApiClient.get("/projects/" + projectId + "/tasks");
            return parseTaskList(responseJson);
        }
    }

    // 5. PUT /api/tasks/{id}/status - Đổi trạng thái (Kéo thả) (Admin & Member)
    public static void updateTaskStatus(Long taskId, String newStatus) throws Exception {
        if (IS_MOCK) {
            for (TaskDTO task : mockTasks) {
                if (task.getId().equals(taskId)) {
                    task.setStatus(newStatus);
                    System.out.println("API: Task " + taskId + " chuyển sang " + newStatus);
                    return;
                }
            }
        } else {
            String jsonBody = String.format("{\"status\": \"%s\"}", newStatus);
            ApiClient.put("/tasks/" + taskId + "/status", jsonBody);
        }
    }

    // 6. GET /api/tasks/{id} - Xem chi tiết Task (Admin & Member)
    public static TaskDTO getTaskById(Long taskId) throws Exception {
        if (IS_MOCK) {
            return mockTasks.stream()
                    .filter(task -> task.getId().equals(taskId))
                    .findFirst()
                    .orElse(null);
        } else {
            String responseJson = ApiClient.get("/tasks/" + taskId);
            return parseTask(responseJson);
        }
    }

    // === COMMENT ENDPOINTS ===

    // 7. GET /api/tasks/{id}/comments - Lấy danh sách comment của task
    public static List<CommentDTO> getComments(Long taskId) throws Exception {
        if (IS_MOCK) {
            return mockComments.stream()
                    .filter(comment -> comment.getTaskId().equals(taskId))
                    .collect(Collectors.toList());
        } else {
            String responseJson = ApiClient.get("/tasks/" + taskId + "/comments");
            return parseCommentList(responseJson);
        }
    }

    // 8. POST /api/tasks/{id}/comments - Gửi comment dạng văn bản
    public static CommentDTO addComment(Long taskId, String content) throws Exception {
        if (IS_MOCK) {
            CommentDTO comment = new CommentDTO(
                nextCommentId++,
                taskId,
                "CurrentUser", // Lấy từ AuthContext
                content,
                LocalDateTime.now()
            );
            mockComments.add(comment);
            System.out.println("API: Added comment to task " + taskId);
            return comment;
        } else {
            String jsonBody = String.format("{\"content\": \"%s\"}", content);
            String responseJson = ApiClient.post("/tasks/" + taskId + "/comments", jsonBody);
            return parseComment(responseJson);
        }
    }

    // === HELPER METHODS - Lấy Task theo status (cho Kanban Board) ===

    public static List<TaskDTO> getTasksByStatus(Long projectId, String status) throws Exception {
        return getTasksByProject(projectId).stream()
                .filter(task -> task.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // === JSON PARSING HELPERS ===

    private static List<TaskDTO> parseTaskList(String jsonResponse) {
        List<TaskDTO> tasks = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                tasks.add(parseTask(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON tasks: " + e.getMessage());
        }
        return tasks;
    }

    private static TaskDTO parseTask(String jsonResponse) {
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            return parseTask(obj);
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON task: " + e.getMessage());
            return null;
        }
    }

    private static TaskDTO parseTask(JSONObject obj) {
        // Parse assignee - Backend trả về object User hoặc array Users
        String assigneeName = "";
        Long assigneeId = null;
        List<Long> assigneeIds = new ArrayList<>();
        List<String> assigneeNames = new ArrayList<>();
        
        // Kiểm tra nếu backend trả về danh sách assignees (nhiều người)
        if (obj.has("assignees") && !obj.isNull("assignees")) {
            JSONArray assigneesArray = obj.optJSONArray("assignees");
            if (assigneesArray != null && assigneesArray.length() > 0) {
                for (int i = 0; i < assigneesArray.length(); i++) {
                    JSONObject assigneeObj = assigneesArray.getJSONObject(i);
                    String name = assigneeObj.optString("full_name", assigneeObj.optString("username", ""));
                    Long id = assigneeObj.optLong("id", 0L);
                    
                    if (id != 0L) {
                        assigneeIds.add(id);
                        assigneeNames.add(name);
                    }
                }
                
                // Lưu người đầu tiên vào assignee/assigneeId để tương thích với code cũ
                if (!assigneeNames.isEmpty()) {
                    assigneeName = assigneeNames.get(0);
                    assigneeId = assigneeIds.get(0);
                }
            }
        }
        // Fallback: kiểm tra assignee đơn lẻ (để tương thích với code cũ)
        else if (obj.has("assignee") && !obj.isNull("assignee")) {
            JSONObject assigneeObj = obj.optJSONObject("assignee");
            if (assigneeObj != null) {
                assigneeName = assigneeObj.optString("full_name", assigneeObj.optString("username", ""));
                assigneeId = assigneeObj.optLong("id", 0L);
                if (assigneeId == 0L) assigneeId = null;
                
                if (assigneeId != null) {
                    assigneeIds.add(assigneeId);
                    assigneeNames.add(assigneeName);
                }
            }
        }
        
        // Parse projectId - Backend có thể trả về object Project
        Long projectId = 0L;
        if (obj.has("project") && !obj.isNull("project")) {
            JSONObject projectObj = obj.optJSONObject("project");
            if (projectObj != null) {
                projectId = projectObj.getLong("id");
            }
        } else if (obj.has("projectId")) {
            projectId = obj.getLong("projectId");
        }
        
        TaskDTO task = new TaskDTO(
            obj.getLong("id"),
            obj.getString("title"),
            obj.optString("description", ""),
            obj.optString("status", "TODO"),
            assigneeName,
            assigneeId,
            projectId
        );
        
        // Set danh sách assignees
        task.setAssigneeIds(assigneeIds);
        task.setAssigneeNames(assigneeNames);
        
        return task;
    }

    private static List<CommentDTO> parseCommentList(String jsonResponse) {
        List<CommentDTO> comments = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                comments.add(parseComment(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON comments: " + e.getMessage());
        }
        return comments;
    }

    private static CommentDTO parseComment(String jsonResponse) {
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            return parseComment(obj);
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON comment: " + e.getMessage());
            return null;
        }
    }

    private static CommentDTO parseComment(JSONObject obj) {
        // Parse username từ user object hoặc trực tiếp
        String username = "";
        if (obj.has("username")) {
            username = obj.getString("username");
        } else if (obj.has("user") && !obj.isNull("user")) {
            JSONObject userObj = obj.optJSONObject("user");
            if (userObj != null) {
                username = userObj.optString("username", userObj.optString("full_name", ""));
            }
        }
        
        // Parse taskId từ task object hoặc trực tiếp
        Long taskId = 0L;
        if (obj.has("taskId")) {
            taskId = obj.getLong("taskId");
        } else if (obj.has("task") && !obj.isNull("task")) {
            JSONObject taskObj = obj.optJSONObject("task");
            if (taskObj != null) {
                taskId = taskObj.getLong("id");
            }
        }
        
        return new CommentDTO(
            obj.getLong("id"),
            taskId,
            username,
            obj.getString("content"),
            LocalDateTime.parse(obj.getString("createdAt"))
        );
    }
}

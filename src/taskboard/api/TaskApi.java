package taskboard.api;

import taskboard.model.TaskDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskApi {

    // --- MOCK DATA STORE ---
    private static List<TaskDTO> mockTasks = new ArrayList<>();
    private static Long nextId = 1L;

    static {
        // Dữ liệu mẫu cho project ID = 1 (sử dụng status: ToDo, InProgress, Done, Blocked)
        mockTasks.add(new TaskDTO(nextId++, "Thiết kế giao diện", "Thiết kế UI/UX cho trang chủ", "ToDo", "Nguyễn Văn A", 1L));
        mockTasks.add(new TaskDTO(nextId++, "Xây dựng API", "Tạo REST API cho module user", "InProgress", "Trần Thị B", 1L));
        mockTasks.add(new TaskDTO(nextId++, "Viết unit test", "Test coverage cho service layer", "Done", "Lê Văn C", 1L));
        mockTasks.add(new TaskDTO(nextId++, "Tích hợp thanh toán", "Kết nối payment gateway", "ToDo", "", 1L));
        mockTasks.add(new TaskDTO(nextId++, "Fix bug authentication", "Sửa lỗi đăng nhập không hoạt động", "Blocked", "Nguyễn Văn A", 1L));
        
        // Dữ liệu mẫu cho project ID = 2
        mockTasks.add(new TaskDTO(nextId++, "Phân tích yêu cầu", "Gặp khách hàng để thu thập yêu cầu", "Done", "Phạm Thị D", 2L));
        mockTasks.add(new TaskDTO(nextId++, "Setup môi trường", "Cài đặt server và database", "InProgress", "Hoàng Quản Lý", 2L));
    }

    // 1. Lấy danh sách Task theo Project
    public List<TaskDTO> getTasksByProject(Long projectId) {
        System.out.println("API: Lấy tasks của project " + projectId);
        return mockTasks.stream()
                .filter(task -> task.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }

    // 2. Lấy Task theo Status và Project
    public List<TaskDTO> getTasksByStatus(Long projectId, String status) {
        return mockTasks.stream()
                .filter(task -> task.getProjectId().equals(projectId) && 
                               task.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // 3. Tạo hoặc Cập nhật Task
    public boolean saveTask(TaskDTO task) {
        if (task.getId() == null || task.getId() == 0L) {
            // CREATE
            task.setId(nextId++);
            mockTasks.add(task);
            System.out.println("API: Created task " + task.getTitle());
        } else {
            // UPDATE
            for (int i = 0; i < mockTasks.size(); i++) {
                if (mockTasks.get(i).getId().equals(task.getId())) {
                    mockTasks.set(i, task);
                    System.out.println("API: Updated task " + task.getTitle());
                    break;
                }
            }
        }
        return true;
    }

    // 4. Xóa Task
    public boolean deleteTask(Long taskId) {
        boolean removed = mockTasks.removeIf(task -> task.getId().equals(taskId));
        if (removed) {
            System.out.println("API: Deleted task " + taskId);
        }
        return removed;
    }

    // 5. Cập nhật trạng thái Task (kéo thả giữa các cột)
    public boolean updateTaskStatus(Long taskId, String newStatus) {
        for (TaskDTO task : mockTasks) {
            if (task.getId().equals(taskId)) {
                task.setStatus(newStatus);
                System.out.println("API: Task " + taskId + " chuyển sang " + newStatus);
                return true;
            }
        }
        return false;
    }
}

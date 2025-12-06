package taskboard.api;

import taskboard.model.ProjectDTO;
import taskboard.model.UserDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectApi {

    // --- MOCK DATA STORE (Giả lập database) ---
    private static List<ProjectDTO> mockProjects = new ArrayList<>();
    private static List<UserDTO> mockMembers = new ArrayList<>(); // Member của 1 project cụ thể (Demo)

    static {
        // Tạo dữ liệu mẫu ban đầu
        mockProjects.add(new ProjectDTO(1L, "Website E-commerce", "Làm trang bán hàng", LocalDate.now(), LocalDate.now().plusDays(30), "IN_PROGRESS", "Admin Mock"));
        mockProjects.add(new ProjectDTO(2L, "Mobile App", "App quản lý kho", LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), "PLANNING", "Project Manager Mock"));
    }

    // 1. Lấy danh sách Project (kèm filter keyword, status)
    public List<ProjectDTO> getProjects(String keyword, String status) {
        // Nếu dùng Server thật: gọi ApiClient.get("/api/projects?...")
        
        // Mock Logic: Filter trên list
        return mockProjects.stream().filter(p -> {
            boolean matchKeyword = (keyword == null || keyword.isEmpty()) || 
                                   p.getName().toLowerCase().contains(keyword.toLowerCase());
            boolean matchStatus = (status == null || status.isEmpty() || status.equals("Tất cả")) || 
                                  p.getStatus().equalsIgnoreCase(status);
            return matchKeyword && matchStatus;
        }).collect(Collectors.toList());
    }

    // 2. Tạo hoặc Cập nhật Project
    public boolean saveProject(ProjectDTO project) {
        if (project.getId() == null) {
            // CREATE: Tạo ID mới và thêm vào list
            project.setId((long) (mockProjects.size() + 1));
            project.setPmName("Current User"); // Giả sử người tạo là PM
            mockProjects.add(project);
            System.out.println("API: Created project " + project.getName());
        } else {
            // UPDATE: Tìm và sửa
            for (int i = 0; i < mockProjects.size(); i++) {
                if (mockProjects.get(i).getId().equals(project.getId())) {
                    mockProjects.set(i, project);
                    System.out.println("API: Updated project " + project.getName());
                    break;
                }
            }
        }
        return true;
    }

    // 3. Lấy danh sách thành viên của Project
    public List<UserDTO> getProjectMembers(Long projectId) {
        // Mock: Trả về list member giả
        if (mockMembers.isEmpty()) {
            mockMembers.add(new UserDTO(1, "dev1", "Nguyễn Văn A", "dev1@example.com", "Member", "Active"));
            mockMembers.add(new UserDTO(2, "tester1", "Trần Thị B", "tester1@example.com", "Member", "Active"));
        }
        return new ArrayList<>(mockMembers);
    }

    // 4. Thêm thành viên vào Project
    public boolean addMember(Long projectId, UserDTO user) {
        // Mock: Thêm vào list giả
        mockMembers.add(user);
        System.out.println("API: Added " + user.getFullName() + " to project " + projectId);
        return true;
    }

    // 5. Xóa thành viên khỏi Project
    public boolean removeMember(Long projectId, int userId) {
        // Mock: Xóa khỏi list giả
        boolean removed = mockMembers.removeIf(user -> user.getId() == userId);
        if (removed) {
            System.out.println("API: Removed user " + userId + " from project " + projectId);
        }
        return removed;
    }

    // 6. Xóa Project
    public boolean deleteProject(Long projectId) {
        // Mock: Xóa khỏi list
        boolean removed = mockProjects.removeIf(project -> project.getId().equals(projectId));
        if (removed) {
            System.out.println("API: Deleted project " + projectId);
        }
        return removed;
    }

    // Helper: Lấy danh sách tất cả user trong hệ thống (để chọn add)
    public List<UserDTO> getAllSystemUsers() {
        List<UserDTO> users = new ArrayList<>();
        users.add(new UserDTO(3, "dev2", "Lê Văn C", "dev2@example.com", "Member", "Active"));
        users.add(new UserDTO(4, "ba1", "Phạm Thị D", "ba1@example.com", "Member", "Active"));
        users.add(new UserDTO(5, "pm2", "Hoàng Quản Lý", "pm2@example.com", "PM", "Active"));
        return users;
    }
}
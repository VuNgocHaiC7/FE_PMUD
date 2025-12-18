package taskboard.api;

import taskboard.model.ProjectDTO;
import taskboard.model.UserDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectApi {
    // Chuyển sang false để kết nối server thật
    private static final boolean IS_MOCK = false;

    // --- MOCK DATA STORE (Giả lập database) - Chỉ dùng khi IS_MOCK = true ---
    private static List<ProjectDTO> mockProjects = new ArrayList<>();
    private static List<UserDTO> mockMembers = new ArrayList<>();

    static {
        mockProjects.add(new ProjectDTO(1L, "Website E-commerce", "Làm trang bán hàng", LocalDate.now(), LocalDate.now().plusDays(30), "IN_PROGRESS", "Admin Mock"));
        mockProjects.add(new ProjectDTO(2L, "Mobile App", "App quản lý kho", LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), "PLANNING", "Project Manager Mock"));
    }

    // 1. GET /api/projects - Xem danh sách Project
    public static List<ProjectDTO> getProjects() throws Exception {
        if (IS_MOCK) {
            return new ArrayList<>(mockProjects);
        } else {
            String responseJson = ApiClient.get("/projects");
            return parseProjectList(responseJson);
        }
    }

    // 1b. GET /api/projects (với filter)
    public static List<ProjectDTO> getProjects(String keyword, String status) throws Exception {
        if (IS_MOCK) {
            return mockProjects.stream().filter(p -> {
                boolean matchKeyword = (keyword == null || keyword.isEmpty()) || 
                                       p.getName().toLowerCase().contains(keyword.toLowerCase());
                boolean matchStatus = (status == null || status.isEmpty() || status.equals("Tất cả")) || 
                                      p.getStatus().equalsIgnoreCase(status);
                return matchKeyword && matchStatus;
            }).collect(Collectors.toList());
        } else {
            String endpoint = "/projects";
            List<String> params = new ArrayList<>();
            
            // QUAN TRỌNG: Trim keyword trước khi encode
            if (keyword != null && !keyword.trim().isEmpty()) {
                String trimmedKeyword = keyword.trim();
                String encodedKeyword = URLEncoder.encode(trimmedKeyword, StandardCharsets.UTF_8);
                params.add("keyword=" + encodedKeyword);
                System.out.println(">>> ProjectApi: Keyword sau trim = '" + trimmedKeyword + "'");
            }
            if (status != null && !status.isEmpty() && !status.equals("Tất cả")) {
                params.add("status=" + status);
            }
            if (!params.isEmpty()) {
                endpoint += "?" + String.join("&", params);
            }
            
            System.out.println(">>> ProjectApi: Đang gọi GET " + endpoint);
            String responseJson = ApiClient.get(endpoint);
            System.out.println(">>> ProjectApi: Response nhận được: " + (responseJson != null ? responseJson.substring(0, Math.min(200, responseJson.length())) + "..." : "NULL"));
            
            List<ProjectDTO> result = parseProjectList(responseJson);
            System.out.println(">>> ProjectApi: Parse được " + result.size() + " projects");
            return result;
        }
    }

    // 2. POST /api/projects - Tạo Project mới
    public static ProjectDTO createProject(ProjectDTO project) throws Exception {
        if (IS_MOCK) {
            project.setId((long) (mockProjects.size() + 1));
            project.setPmName("Current User");
            mockProjects.add(project);
            System.out.println("API: Created project " + project.getName());
            return project;
        } else {
            // Convert LocalDate to LocalDateTime (add T00:00:00)
            String startDateTime = project.getStartDate() != null ? project.getStartDate() + "T00:00:00" : null;
            String endDateTime = project.getEndDate() != null ? project.getEndDate() + "T00:00:00" : null;
            
            String jsonBody = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"startDate\": \"%s\", \"endDate\": \"%s\", \"status\": \"%s\"}",
                project.getName(),
                project.getDescription(),
                startDateTime,
                endDateTime,
                project.getStatus()
            );
            
            System.out.println(">>> Creating project with JSON: " + jsonBody);
            String responseJson = ApiClient.post("/projects", jsonBody);
            return parseProject(responseJson);
        }
    }

    // 3. PUT /api/projects/{id} - Sửa tên, mô tả, ngày tháng của dự án (Chỉ Admin)
    public static void updateProject(ProjectDTO project) throws Exception {
        if (IS_MOCK) {
            for (int i = 0; i < mockProjects.size(); i++) {
                if (mockProjects.get(i).getId().equals(project.getId())) {
                    mockProjects.set(i, project);
                    System.out.println("API: Updated project " + project.getName());
                    return;
                }
            }
        } else {
            // Convert LocalDate to LocalDateTime (add T00:00:00)
            String startDateTime = project.getStartDate() != null ? project.getStartDate() + "T00:00:00" : null;
            String endDateTime = project.getEndDate() != null ? project.getEndDate() + "T00:00:00" : null;
            
            String jsonBody = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"startDate\": \"%s\", \"endDate\": \"%s\", \"status\": \"%s\"}",
                project.getName(),
                project.getDescription(),
                startDateTime,
                endDateTime,
                project.getStatus()
            );
            
            System.out.println(">>> Updating project with JSON: " + jsonBody);
            ApiClient.put("/projects/" + project.getId(), jsonBody);
        }
    }

    // 4. POST /api/projects/{id}/members - Thêm Member
    public static void addMember(Long projectId, int userId) throws Exception {
        if (IS_MOCK) {
            UserDTO user = new UserDTO(userId, "user" + userId, "User " + userId, "user@example.com", "MEMBER", "Active");
            mockMembers.add(user);
            System.out.println("API: Added user " + userId + " to project " + projectId);
        } else {
            String jsonBody = String.format("{\"userId\": %d}", userId);
            ApiClient.post("/projects/" + projectId + "/members", jsonBody);
        }
    }

    // 5. DELETE /api/projects/{id}/members/{userId} - Xóa một thành viên ra khỏi dự án
    public static void removeMember(Long projectId, int userId) throws Exception {
        if (IS_MOCK) {
            boolean removed = mockMembers.removeIf(user -> user.getId() == userId);
            if (removed) {
                System.out.println("API: Removed user " + userId + " from project " + projectId);
            }
        } else {
            ApiClient.delete("/projects/" + projectId + "/members/" + userId);
        }
    }

    // 6. Lấy danh sách thành viên của Project (BONUS)
    public static List<UserDTO> getProjectMembers(Long projectId) throws Exception {
        if (IS_MOCK) {
            if (mockMembers.isEmpty()) {
                mockMembers.add(new UserDTO(1, "dev1", "Nguyễn Văn A", "dev1@example.com", "MEMBER", "Active"));
                mockMembers.add(new UserDTO(2, "tester1", "Trần Thị B", "tester1@example.com", "MEMBER", "Active"));
            }
            return new ArrayList<>(mockMembers);
        } else {
            String responseJson = ApiClient.get("/projects/" + projectId + "/members");
            return parseUserList(responseJson);
        }
    }

    // 7. Xóa Project (BONUS)
    public static void deleteProject(Long projectId) throws Exception {
        if (IS_MOCK) {
            boolean removed = mockProjects.removeIf(project -> project.getId().equals(projectId));
            if (removed) {
                System.out.println("API: Deleted project " + projectId);
            }
        } else {
            ApiClient.delete("/projects/" + projectId);
        }
    }

    // Helper: Lấy danh sách tất cả user trong hệ thống (để chọn add)
    public static List<UserDTO> getAllSystemUsers() throws Exception {
        if (IS_MOCK) {
            List<UserDTO> users = new ArrayList<>();
            users.add(new UserDTO(3, "dev2", "Lê Văn C", "dev2@example.com", "MEMBER", "Active"));
            users.add(new UserDTO(4, "ba1", "Phạm Thị D", "ba1@example.com", "MEMBER", "Active"));
            return users;
        } else {
            return UserApi.getAllUsers();
        }
    }

    // Helper: Parse JSON array thành List<ProjectDTO>
    private static List<ProjectDTO> parseProjectList(String jsonResponse) {
        List<ProjectDTO> projects = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                projects.add(parseProject(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON projects: " + e.getMessage());
        }
        return projects;
    }

    // Helper: Parse JSONObject thành ProjectDTO
    private static ProjectDTO parseProject(String jsonResponse) {
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            return parseProject(obj);
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON project: " + e.getMessage());
            return null;
        }
    }

    private static ProjectDTO parseProject(JSONObject obj) {
        try {
            ProjectDTO project = new ProjectDTO();
            project.setId(obj.getLong("id"));
            project.setName(obj.getString("name"));
            project.setDescription(obj.optString("description", ""));
            
            // Parse dates - có thể là LocalDateTime hoặc LocalDate
            if (obj.has("startDate") && !obj.isNull("startDate")) {
                String startDateStr = obj.getString("startDate");
                // Nếu có 'T' thì là DateTime, lấy phần date
                project.setStartDate(startDateStr.contains("T") 
                    ? LocalDate.parse(startDateStr.substring(0, 10)) 
                    : LocalDate.parse(startDateStr));
            }
            
            if (obj.has("endDate") && !obj.isNull("endDate")) {
                String endDateStr = obj.getString("endDate");
                project.setEndDate(endDateStr.contains("T") 
                    ? LocalDate.parse(endDateStr.substring(0, 10)) 
                    : LocalDate.parse(endDateStr));
            }
            
            project.setStatus(obj.optString("status", "PLANNING"));
            
            // Parse PM - có thể là object hoặc string
            if (obj.has("pm") && !obj.isNull("pm")) {
                Object pmObj = obj.get("pm");
                if (pmObj instanceof JSONObject) {
                    JSONObject pm = (JSONObject) pmObj;
                    project.setPmName(pm.optString("fullName", pm.optString("username", "")));
                    project.setPmId(pm.getLong("id"));
                } else if (pmObj instanceof String) {
                    project.setPmName((String) pmObj);
                }
            }
            
            // Fallback cho pmName và pmId riêng biệt
            if (obj.has("pmName")) {
                project.setPmName(obj.getString("pmName"));
            }
            if (obj.has("pmId")) {
                project.setPmId(obj.getLong("pmId"));
            }
            
            System.out.println("  Parsed: " + project.getName() + " - Status: " + project.getStatus());
            return project;
        } catch (Exception e) {
            System.err.println("!!! Lỗi parse project: " + e.getMessage());
            System.err.println("JSON: " + obj.toString());
            e.printStackTrace();
            return null;
        }
    }

    // Helper: Parse JSON array thành List<UserDTO>
    private static List<UserDTO> parseUserList(String jsonResponse) {
        List<UserDTO> users = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                UserDTO user = new UserDTO(
                    obj.getInt("id"),
                    obj.getString("username"),
                    obj.optString("fullName", ""),
                    obj.optString("email", ""),
                    obj.optString("role", "MEMBER"),
                    obj.optString("status", "Active")
                );
                users.add(user);
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON users: " + e.getMessage());
        }
        return users;
    }
}
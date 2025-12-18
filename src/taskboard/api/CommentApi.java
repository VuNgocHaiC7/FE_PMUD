package taskboard.api;

import org.json.JSONArray;
import org.json.JSONObject;
import taskboard.model.CommentDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommentApi {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Load tất cả comments của một task
     * @param taskId ID của task
     * @return List<CommentDTO> danh sách bình luận
     */
    public static List<CommentDTO> getCommentsByTask(Long taskId) throws Exception {
        String response = ApiClient.get("/tasks/" + taskId + "/comments");
        JSONArray jsonArray = new JSONArray(response);
        List<CommentDTO> comments = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            comments.add(parseComment(obj));
        }
        return comments;
    }

    /**
     * Gửi comment mới cho task
     * @param taskId ID của task
     * @param content Nội dung bình luận
     * @return CommentDTO comment vừa tạo
     */
    public static CommentDTO sendComment(Long taskId, String content) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("content", content);
        
        String response = ApiClient.post("/tasks/" + taskId + "/comments", payload.toString());
        JSONObject jsonObject = new JSONObject(response);
        
        return parseComment(jsonObject);
    }
    
    private static CommentDTO parseComment(JSONObject obj) {
        CommentDTO comment = new CommentDTO();
        comment.setId(obj.getLong("id"));
        comment.setTaskId(obj.getLong("taskId"));
        comment.setContent(obj.getString("content"));
        comment.setUsername(obj.optString("username", ""));
        
        String createdAtStr = obj.optString("createdAt");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            comment.setCreatedAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER));
        }
        
        return comment;
    }
}

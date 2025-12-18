package taskboard.api;

import org.json.JSONArray;
import org.json.JSONObject;
import taskboard.model.NotificationDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationApi {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Lấy tất cả notifications chưa đọc
     */
    public static List<NotificationDTO> getUnreadNotifications() throws Exception {
        String response = ApiClient.get("/notifications/unread");
        return parseNotificationList(response);
    }

    /**
     * Lấy tất cả notifications
     */
    public static List<NotificationDTO> getAllNotifications() throws Exception {
        String response = ApiClient.get("/notifications");
        return parseNotificationList(response);
    }

    /**
     * Đếm số notifications chưa đọc
     */
    public static long getUnreadCount() throws Exception {
        String response = ApiClient.get("/notifications/count");
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getLong("count");
    }

    /**
     * Đánh dấu một notification là đã đọc
     */
    public static void markAsRead(Long notificationId) throws Exception {
        ApiClient.put("/notifications/" + notificationId + "/read", "");
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    public static void markAllAsRead() throws Exception {
        ApiClient.put("/notifications/read-all", "");
    }
    
    private static List<NotificationDTO> parseNotificationList(String response) {
        JSONArray jsonArray = new JSONArray(response);
        List<NotificationDTO> notifications = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            notifications.add(parseNotification(obj));
        }
        return notifications;
    }
    
    private static NotificationDTO parseNotification(JSONObject obj) {
        NotificationDTO notification = new NotificationDTO();
        notification.setId(obj.getLong("id"));
        notification.setType(obj.getString("type"));
        notification.setMessage(obj.getString("message"));
        
        // Kiểm tra cả "isRead" và "read" vì Jackson có thể serialize khác nhau
        if (obj.has("isRead")) {
            notification.setRead(obj.getBoolean("isRead"));
        } else if (obj.has("read")) {
            notification.setRead(obj.getBoolean("read"));
        } else {
            notification.setRead(false); // Mặc định là chưa đọc
        }
        
        if (obj.has("taskId") && !obj.isNull("taskId")) {
            notification.setTaskId(obj.getLong("taskId"));
        }
        
        if (obj.has("taskTitle") && !obj.isNull("taskTitle")) {
            notification.setTaskTitle(obj.getString("taskTitle"));
        }
        
        if (obj.has("actorUsername") && !obj.isNull("actorUsername")) {
            notification.setActorUsername(obj.getString("actorUsername"));
        }
        
        if (obj.has("actorFullName") && !obj.isNull("actorFullName")) {
            notification.setActorFullName(obj.getString("actorFullName"));
        }
        
        String createdAtStr = obj.optString("createdAt");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            notification.setCreatedAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER));
        }
        
        return notification;
    }
}

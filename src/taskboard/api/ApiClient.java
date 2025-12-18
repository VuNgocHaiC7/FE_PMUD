package taskboard.api;

import taskboard.auth.AuthContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    // >>> CẬP NHẬT ĐỊA CHỈ SERVER MỚI <<<
    // Lưu ý: Tôi lấy phần gốc là ".../tms/api", các phần sau sẽ là endpoint
    private static final String BASE_URL = "http://localhost:8080/api";
    
    // ... (Phần code còn lại giữ nguyên không đổi) ...
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static HttpRequest.Builder createBuilder(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        String token = AuthContext.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    public static String get(String endpoint) throws Exception {
        System.out.println("[ApiClient] GET: " + BASE_URL + endpoint);
        String response = sendRequest(createBuilder(endpoint).GET().build());
        System.out.println("[ApiClient] Response length: " + (response != null ? response.length() : 0) + " chars");
        return response;
    }

    public static String post(String endpoint, String jsonBody) throws Exception {
        return sendRequest(createBuilder(endpoint).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build());
    }

    public static String put(String endpoint, String jsonBody) throws Exception {
        return sendRequest(createBuilder(endpoint).PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build());
    }

    public static String delete(String endpoint) throws Exception {
        return sendRequest(createBuilder(endpoint).DELETE().build());
    }

    private static String sendRequest(HttpRequest request) throws Exception {
        System.out.println("[ApiClient] Sending request to: " + request.uri());
        System.out.println("[ApiClient] Method: " + request.method());
        
        // Log headers for debugging
        request.headers().map().forEach((key, values) -> 
            System.out.println("[ApiClient] Header: " + key + " = " + values)
        );
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("[ApiClient] Status Code: " + response.statusCode());
        System.out.println("[ApiClient] Response Body: " + response.body());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("[ApiClient] Success!");
            return response.body();
        } else if (response.statusCode() == 401) {
            System.err.println("[ApiClient] ERROR 401: Unauthorized");
            throw new RuntimeException("Lỗi 401: Unauthorized (Có thể token hết hạn hoặc sai thông tin)");
        } else if (response.statusCode() == 403) {
            System.err.println("[ApiClient] ERROR 403: Forbidden - " + response.body());
            throw new RuntimeException("Không có quyền thực hiện thao tác này: " + response.body());
        } else {
            // In ra body lỗi để dễ debug
            System.err.println("[ApiClient] ERROR " + response.statusCode() + ": " + response.body());
            throw new RuntimeException("Server Error (" + response.statusCode() + "): " + response.body());
        }
    }
}
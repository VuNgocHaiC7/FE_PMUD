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
    private static final String BASE_URL = "https://phyllocladioid-roastable-gertude.ngrok-free.dev/tms/api";
    
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
        return sendRequest(createBuilder(endpoint).GET().build());
    }

    public static String post(String endpoint, String jsonBody) throws Exception {
        return sendRequest(createBuilder(endpoint).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build());
    }

    public static String put(String endpoint, String jsonBody) throws Exception {
        return sendRequest(createBuilder(endpoint).PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build());
    }

    private static String sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else if (response.statusCode() == 401) {
            throw new RuntimeException("Lỗi 401: Unauthorized (Có thể token hết hạn hoặc sai thông tin)");
        } else {
            // In ra body lỗi để dễ debug
            throw new RuntimeException("Server Error (" + response.statusCode() + "): " + response.body());
        }
    }
}
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TestReviewPost {
    public static void main(String[] args) {
        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            String jsonRequest = "{\"libroId\":1,\"autor\":\"Prueba\",\"calificacion\":5,\"comentario\":\"Hola\"}";
            System.out.println("Sending: " + jsonRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/reviews"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

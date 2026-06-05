package co.edu.javeriana.proyecto.infrastructure.adapter.out.rest;

import co.edu.javeriana.proyecto.application.port.out.ReviewsGateway;
import co.edu.javeriana.proyecto.domain.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RestReviewsGateway implements ReviewsGateway {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public RestReviewsGateway(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
    }

    @Override
    public List<Review> obtenerReviewsPorLibro(Long libroId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + libroId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<Review>>(){}.getType());
            } else {
                System.err.println("Error obteniendo reviews. Status: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Excepción al conectar con el API de Reviews: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Review guardarReview(Review review) {
        try {
            String jsonRequest = gson.toJson(review);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return gson.fromJson(response.body(), Review.class);
            } else {
                throw new RuntimeException("Error guardando review. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Excepción al guardar review: " + e.getMessage(), e);
        }
    }
}

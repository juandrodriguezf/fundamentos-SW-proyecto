import co.edu.javeriana.proyecto.domain.Review;
import co.edu.javeriana.proyecto.infrastructure.adapter.out.rest.RestReviewsGateway;
import java.util.List;

public class TestGateway {
    public static void main(String[] args) {
        try {
            RestReviewsGateway gateway = new RestReviewsGateway("http://localhost:8081/api/reviews");
            Review r = new Review(null, 1L, "GatewayUser", 3, "Test comment");
            System.out.println("Saving review...");
            Review saved = gateway.guardarReview(r);
            System.out.println("Saved ID: " + saved.getId());
            
            System.out.println("Fetching reviews...");
            List<Review> list = gateway.obtenerReviewsPorLibro(1L);
            System.out.println("Found: " + list.size() + " reviews.");
            for (Review rev : list) {
                System.out.println("- " + rev.getComentario());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

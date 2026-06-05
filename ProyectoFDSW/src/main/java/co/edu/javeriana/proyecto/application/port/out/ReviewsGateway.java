package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Review;
import java.util.List;

public interface ReviewsGateway {
    List<Review> obtenerReviewsPorLibro(Long libroId);
    Review guardarReview(Review review);
}

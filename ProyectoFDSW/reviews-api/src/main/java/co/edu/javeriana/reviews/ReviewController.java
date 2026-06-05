package co.edu.javeriana.reviews;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/{libroId}")
    public List<Review> obtenerReviews(@PathVariable Long libroId) {
        return reviewRepository.findByLibroId(libroId);
    }

    @PostMapping
    public ResponseEntity<Review> crearReview(@RequestBody Review review) {
        if (review.getCalificacion() < 1 || review.getCalificacion() > 5) {
            return ResponseEntity.badRequest().build();
        }
        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }
}

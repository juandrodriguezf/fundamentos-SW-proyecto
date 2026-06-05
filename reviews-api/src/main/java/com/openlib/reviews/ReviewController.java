package com.openlib.reviews;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/{libroId}")
    public ResponseEntity<List<Review>> obtenerReviews(@PathVariable Long libroId) {
        List<Review> reviews = reviewRepository.findByLibroId(libroId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<Review> guardarReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewRepository.save(review);
            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.port.out.ReviewsGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.Review;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class DetalleLibroController {

    @FXML private ImageView imgPortada;
    @FXML private Label lblTitulo;
    @FXML private Label lblAutor;
    @FXML private Label lblPrecio;
    @FXML private Button btnComprar;
    
    @FXML private Label lblCategoria;
    @FXML private Label lblCalificacion;
    @FXML private Label lblReviewCount;
    @FXML private HBox tagsContainer;
    @FXML private Label lblStock;

    @FXML private ListView<Review> listReviews;
    @FXML private ComboBox<Integer> cmbCalificacion;
    @FXML private TextArea txtComentario;
    @FXML private Button btnEnviarReview;

    private final Libro libro;
    private final Usuario usuarioActual;
    private final ReviewsGateway reviewsGateway;
    private final Runnable onComprar;

    private final ObservableList<Review> reviewsObservable = FXCollections.observableArrayList();

    public DetalleLibroController(Libro libro, Usuario usuarioActual, ReviewsGateway reviewsGateway, Runnable onComprar) {
        this.libro = libro;
        this.usuarioActual = usuarioActual;
        this.reviewsGateway = reviewsGateway;
        this.onComprar = onComprar;
    }

    @FXML
    public void initialize() {
        lblTitulo.setText(libro.getTitulo());
        lblAutor.setText(libro.getAutor());
        lblPrecio.setText("$" + libro.getPrecio());
        
        if (lblCategoria != null) lblCategoria.setText(libro.getCategoria() != null ? libro.getCategoria().toUpperCase() : "GENERAL");
        if (lblCalificacion != null) {
            int estrellas = (int) Math.max(1, Math.round(libro.getCalificacionPromedio()));
            lblCalificacion.setText("⭐".repeat(estrellas));
        }
        if (lblReviewCount != null) lblReviewCount.setText("(Cargando reseñas...)");
        
        if (tagsContainer != null && libro.getEtiquetas() != null && !libro.getEtiquetas().trim().isEmpty()) {
            for (String tag : libro.getEtiquetas().split(",")) {
                Label lblTag = new Label(tag.trim());
                lblTag.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
                tagsContainer.getChildren().add(lblTag);
            }
        }

        try {
            Image img = BibliotecaController.getCachedImage(libro.getPortada(), 200, 300);
            if (img != null) {
                imgPortada.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar portada: " + e.getMessage());
        }

        if (libro.getStock() <= 0) {
            btnComprar.setText("❌ Agotado");
            btnComprar.setDisable(true);
            btnComprar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            btnComprar.setOnAction(e -> {
                if (onComprar != null) {
                    onComprar.run();
                }
            });
        }

        cmbCalificacion.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cmbCalificacion.getSelectionModel().selectLast(); // Default 5

        btnEnviarReview.setOnAction(e -> enviarReview());

        listReviews.setItems(reviewsObservable);
        listReviews.setCellFactory(param -> new ReviewCell());

        cargarReviews();
    }

    private void cargarReviews() {
        if (lblReviewCount != null) lblReviewCount.setText("(Cargando...)");

        // Placeholder de lista vacía
        listReviews.setPlaceholder(new Label("Aún no hay reseñas para este libro. ¡Sé el primero en opinar!"));

        Task<List<Review>> task = new Task<>() {
            @Override
            protected List<Review> call() throws Exception {
                return reviewsGateway.obtenerReviewsPorLibro(libro.getId());
            }
        };

        task.setOnSucceeded(e -> {
            List<Review> lista = task.getValue();
            reviewsObservable.setAll(lista);
            if (lblReviewCount != null) {
                int n = reviewsObservable.size();
                lblReviewCount.setText(n == 0 ? "(sin reseñas aún)" : "(" + n + (n == 1 ? " reseña)" : " reseñas)"));
            }
        });

        task.setOnFailed(e -> {
            if (lblReviewCount != null) lblReviewCount.setText("(no disponible)");
            listReviews.setPlaceholder(new Label("No se pudo conectar con el servidor de reseñas."));
        });

        new Thread(task).start();
    }

    private void enviarReview() {
        if (usuarioActual == null) {
            mostrarAlerta("Debes iniciar sesión para dejar una reseña.");
            return;
        }

        String comentario = txtComentario.getText().trim();
        if (comentario.isEmpty()) {
            mostrarAlerta("El comentario no puede estar vacío.");
            return;
        }

        Integer calificacion = cmbCalificacion.getValue();

        Review newReview = new Review(null, libro.getId(), usuarioActual.getNombre(), calificacion, comentario);

        btnEnviarReview.setDisable(true);

        Task<Review> task = new Task<>() {
            @Override
            protected Review call() throws Exception {
                return reviewsGateway.guardarReview(newReview);
            }
        };

        task.setOnSucceeded(e -> {
            Review r = task.getValue();
            if (r != null) {
                reviewsObservable.add(r);
                txtComentario.clear();
                cmbCalificacion.getSelectionModel().selectLast();
            } else {
                mostrarAlerta("No se pudo guardar la reseña.");
            }
            btnEnviarReview.setDisable(false);
        });

        task.setOnFailed(e -> {
            mostrarAlerta("Error al conectar con la API de Reviews.");
            btnEnviarReview.setDisable(false);
        });

        new Thread(task).start();
    }

    private void mostrarAlerta(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    private static class ReviewCell extends ListCell<Review> {
        @Override
        protected void updateItem(Review review, boolean empty) {
            super.updateItem(review, empty);
            if (empty || review == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox(5);
                Label lblAutor = new Label(review.getAutor() + " - " + "⭐".repeat(review.getCalificacion()));
                lblAutor.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                Label lblComentario = new Label(review.getComentario());
                lblComentario.setWrapText(true);
                lblComentario.setStyle("-fx-text-fill: #34495e;");
                box.getChildren().addAll(lblAutor, lblComentario);
                setGraphic(box);
            }
        }
    }
}

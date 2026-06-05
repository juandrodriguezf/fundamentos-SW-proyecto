package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.ObtenerBibliotecaPersonalUseCase;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BibliotecaPersonalController {

    @FXML private TextField txtBuscador;
    @FXML private FlowPane flowEstanteria;
    @FXML private VBox vboxVacio;
    @FXML private Button btnExplorar;
    @FXML private Button btnCerrar;

    private final Usuario usuario;
    private final ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase;
    private final BibliotecaController bibliotecaController;
    
    public BibliotecaPersonalController(Usuario usuario, ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase, BibliotecaController bibliotecaController) {
        this.usuario = usuario;
        this.obtenerBibliotecaPersonalUseCase = obtenerBibliotecaPersonalUseCase;
        this.bibliotecaController = bibliotecaController;
    }

    @FXML
    public void initialize() {
        btnCerrar.setOnAction(e -> cerrarVentana());
        btnExplorar.setOnAction(e -> cerrarVentana());
        
        txtBuscador.textProperty().addListener((obs, oldVal, newVal) -> cargarLibros(newVal));

        cargarLibros("");
    }

    private void cargarLibros(String filtro) {
        Task<List<Libro>> task = new Task<>() {
            @Override
            protected List<Libro> call() {
                return obtenerBibliotecaPersonalUseCase.ejecutar(usuario.getId(), filtro);
            }
        };

        task.setOnSucceeded(e -> {
            List<Libro> libros = task.getValue();
            flowEstanteria.getChildren().clear();
            
            if (libros.isEmpty()) {
                if (filtro == null || filtro.isEmpty()) {
                    vboxVacio.setVisible(true);
                    vboxVacio.setManaged(true);
                }
            } else {
                vboxVacio.setVisible(false);
                vboxVacio.setManaged(false);
                for (Libro libro : libros) {
                    flowEstanteria.getChildren().add(crearTarjetaLibro(libro));
                }
            }
        });

        new Thread(task).start();
    }

    private VBox crearTarjetaLibro(Libro libro) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(200);

        ImageView imgPortada = new ImageView();
        try {
            Image img = BibliotecaController.getCachedImage(libro.getPortada(), 120, 160);
            if (img != null) {
                imgPortada.setImage(img);
            }
        } catch (Exception e) {
            // Ignorar
        }
        
        Label lblTitulo = new Label(libro.getTitulo());
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblTitulo.setWrapText(true);
        lblTitulo.setAlignment(Pos.CENTER);
        
        Label lblAutor = new Label(libro.getAutor());
        lblAutor.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        Button btnLeer = new Button("Leer ahora");
        btnLeer.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
        btnLeer.setMaxWidth(Double.MAX_VALUE);
        btnLeer.setOnAction(e -> simularLectura(libro));
        
        Button btnDescargar = new Button("Descargar (PDF/EPUB)");
        btnDescargar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnDescargar.setMaxWidth(Double.MAX_VALUE);
        btnDescargar.setOnAction(e -> simularDescarga(libro));
        
        card.getChildren().addAll(imgPortada, lblTitulo, lblAutor, btnLeer, btnDescargar);
        return card;
    }

    private String obtenerRutaPdf(String titulo) {
        String filename;
        switch (titulo) {
            case "Clean Code": filename = "Codigo limpio - Robert Cecil Martin.pdf"; break;
            case "The Pragmatic Programmer": filename = "The Pragmatic Programmer Your Journey to Mastery, 20th Anniversary Edition by Andrew H.pdf"; break;
            case "Design Patterns": filename = "Erich Gamma, Richard Helm, Ralph Johnson, John M. Vlissides-Design Patterns_ Elements.pdf"; break;
            case "Refactoring": filename = "Martin Fowler - Refactoring - Improving the Design of Existing.pdf"; break;
            case "Domain-Driven Design": filename = "Domain Driven Design Tackling Complexity in the Heart of Software - Eric Evans.pdf"; break;
            case "Effective Java": filename = "Joshua Bloch - Effective Java (3rd) - 2018.pdf"; break;
            case "Head First Java": filename = "Head First Java, 2e - Kathy Sierra, Bert Bates.pdf"; break;
            case "Introduction to Algorithms": filename = "Cormen Introduction to Algorithms.pdf"; break;
            case "Cien Anos de Soledad": filename = "soledad - gabriel garcia marques.pdf"; break;
            case "El Principito": filename = "el principito.pdf"; break;
            case "1984": filename = "1984 - george orwell.pdf"; break;
            case "Sapiens": filename = "Sapiens. De animales a dioses Una breve historia de la humanidad (Yuval Noah Harari).pdf"; break;
            default: filename = titulo.toLowerCase() + ".pdf"; break;
        }
        return "/books/" + filename;
    }

    private void simularLectura(Libro libro) {
        try {
            String resourcePath = obtenerRutaPdf(libro.getTitulo());
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                java.io.File tempFile = java.io.File.createTempFile("libro_tmp_", ".pdf");
                tempFile.deleteOnExit();
                try (java.io.InputStream is = url.openStream();
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(tempFile);
                    return; 
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Visor Web - " + libro.getTitulo());
        alert.setHeaderText("Libro no disponible fisicamente");
        alert.setContentText("No se encontro el PDF para: " + libro.getTitulo() + "\nPor ahora solo es una simulacion.");
        alert.showAndWait();
    }

    private void simularDescarga(Libro libro) {
        String resourcePath = obtenerRutaPdf(libro.getTitulo());
        java.net.URL url = getClass().getResource(resourcePath);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Descargar " + libro.getTitulo());
        fileChooser.setInitialFileName(libro.getTitulo().replaceAll(" ", "_") + ".pdf");
        
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                if (url != null) {
                    try (java.io.InputStream is = url.openStream();
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("Contenido simulado descargado de forma segura.\n\nLibro: " + libro.getTitulo() + "\nAutor: " + libro.getAutor());
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Descarga Completada");
                alert.setHeaderText(null);
                alert.setContentText("El libro se ha descargado correctamente en:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Ocurrio un error al guardar el archivo.");
                alert.showAndWait();
            }
        }
    }

    private void cerrarVentana() {
        if (bibliotecaController != null) {
            bibliotecaController.cerrarOverlay();
        }
    }
}

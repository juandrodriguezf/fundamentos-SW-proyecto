package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.SubirLibroUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SubirLibroController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtRutaArchivo;
    @FXML private Button btnSeleccionarArchivo;
    @FXML private Button btnSubir;

    private final SubirLibroUseCase subirLibroUseCase;
    private final BibliotecaController bibliotecaController;
    private File archivoSeleccionado;

    public SubirLibroController(SubirLibroUseCase subirLibroUseCase, BibliotecaController bibliotecaController) {
        this.subirLibroUseCase = subirLibroUseCase;
        this.bibliotecaController = bibliotecaController;
    }

    @FXML
    public void initialize() {
        btnSeleccionarArchivo.setOnAction(e -> seleccionarArchivo());
        btnSubir.setOnAction(e -> subirLibro());
    }

    private void seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo (PDF/EPUB)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.epub"),
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Archivos EPUB", "*.epub")
        );
        Stage stage = (Stage) btnSeleccionarArchivo.getScene().getWindow();
        archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            txtRutaArchivo.setText(archivoSeleccionado.getAbsolutePath());
        }
    }

    private void subirLibro() {
        try {
            String titulo = txtTitulo.getText();
            String autor = txtAutor.getText();
            String categoria = txtCategoria.getText();
            String precioStr = txtPrecio.getText();
            String ruta = txtRutaArchivo.getText();

            if (titulo.isEmpty() || autor.isEmpty() || categoria.isEmpty() || precioStr.isEmpty() || ruta.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor llene todos los campos.");
                return;
            }

            double precio = Double.parseDouble(precioStr);

            subirLibroUseCase.subirLibro(titulo, autor, categoria, precio, ruta);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Libro subido correctamente. Está pendiente de aprobación.");
            cerrarVentana();
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "El precio debe ser un número válido.");
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error inesperado al subir el libro.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        if (bibliotecaController != null) {
            bibliotecaController.cerrarOverlay();
        }
    }
}

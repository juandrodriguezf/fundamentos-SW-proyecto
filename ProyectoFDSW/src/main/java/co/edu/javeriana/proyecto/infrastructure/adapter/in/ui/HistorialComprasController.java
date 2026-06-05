package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.GenerarFacturaPdfUseCase;
import co.edu.javeriana.proyecto.application.usecase.ObtenerHistorialOrdenesUseCase;
import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Orden;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialComprasController {

    @FXML private ListView<Orden> listOrdenes;
    @FXML private VBox vboxDetalle;
    @FXML private Label lblOrdenId;
    @FXML private Label lblFecha;
    @FXML private Label lblEstado;
    @FXML private Label lblTotal;
    @FXML private TableView<Compra> tablaProductos;
    @FXML private TableColumn<Compra, String> colTitulo;
    @FXML private TableColumn<Compra, String> colCantidad;
    @FXML private TableColumn<Compra, String> colPrecio;
    @FXML private TableColumn<Compra, String> colSubtotal;
    @FXML private Label lblMensaje;
    @FXML private Button btnDescargarFactura;
    @FXML private Button btnCerrar;

    private final Usuario usuario;
    private final ObtenerHistorialOrdenesUseCase obtenerHistorialOrdenesUseCase;
    private final GenerarFacturaPdfUseCase generarFacturaPdfUseCase;
    private final BibliotecaController bibliotecaController;

    private final ObservableList<Orden> ordenesObservable = FXCollections.observableArrayList();
    private final ObservableList<Compra> productosObservable = FXCollections.observableArrayList();

    public HistorialComprasController(Usuario usuario, ObtenerHistorialOrdenesUseCase obtenerHistorialOrdenesUseCase, GenerarFacturaPdfUseCase generarFacturaPdfUseCase, BibliotecaController bibliotecaController) {
        this.usuario = usuario;
        this.obtenerHistorialOrdenesUseCase = obtenerHistorialOrdenesUseCase;
        this.generarFacturaPdfUseCase = generarFacturaPdfUseCase;
        this.bibliotecaController = bibliotecaController;
    }

    @FXML
    public void initialize() {
        listOrdenes.setItems(ordenesObservable);
        listOrdenes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Orden orden, boolean empty) {
                super.updateItem(orden, empty);
                if (empty || orden == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    setText(orden.getFecha().format(formatter) + " - Orden #" + orden.getId() + " - $" + String.format("%.2f", orden.getTotal()));
                }
            }
        });

        listOrdenes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarDetalleOrden(newVal);
            }
        });

        colTitulo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLibro().getTitulo()));
        colCantidad.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCantidad())));
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getPrecioUnitario())));
        colSubtotal.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getSubtotal())));

        tablaProductos.setItems(productosObservable);

        btnDescargarFactura.setOnAction(e -> descargarFactura());
        btnCerrar.setOnAction(e -> cerrarVentana());

        cargarHistorial();
    }

    private void cargarHistorial() {
        Task<List<Orden>> task = new Task<>() {
            @Override
            protected List<Orden> call() {
                return obtenerHistorialOrdenesUseCase.ejecutar(usuario.getId());
            }
        };

        task.setOnSucceeded(e -> {
            ordenesObservable.setAll(task.getValue());
            if (ordenesObservable.isEmpty()) {
                lblMensaje.setText("No tienes compras registradas aun.");
                lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
            }
        });

        new Thread(task).start();
    }

    private void mostrarDetalleOrden(Orden orden) {
        vboxDetalle.setVisible(true);
        vboxDetalle.setManaged(true);
        lblMensaje.setText("");

        lblOrdenId.setText(orden.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        lblFecha.setText(orden.getFecha().format(formatter));
        lblEstado.setText(orden.getEstado());
        if ("Completado".equals(orden.getEstado())) {
            lblEstado.setStyle("-fx-text-fill: #2ecc71;");
        }
        lblTotal.setText(String.format("$%.2f", orden.getTotal()));

        productosObservable.setAll(orden.getItems());
    }

    private void descargarFactura() {
        Orden orden = listOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Factura PDF");
        fileChooser.setInitialFileName("Factura_" + orden.getId() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        Stage stage = (Stage) btnDescargarFactura.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            btnDescargarFactura.setDisable(true);
            lblMensaje.setText("Generando PDF...");
            lblMensaje.setStyle("-fx-text-fill: #f39c12;");

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return generarFacturaPdfUseCase.ejecutar(orden, usuario.getEmail(), file.getParent());
                }
            };

            task.setOnSucceeded(e -> {
                btnDescargarFactura.setDisable(false);
                String path = task.getValue();
                if (path != null) {
                    // Renombrar el archivo generado si el usuario cambio el nombre en el FileChooser
                    File generatedFile = new File(file.getParent() + File.separator + "Factura_" + orden.getId() + ".pdf");
                    if (generatedFile.exists() && !generatedFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                        generatedFile.renameTo(file);
                    }
                    lblMensaje.setText("Factura guardada con exito.");
                    lblMensaje.setStyle("-fx-text-fill: #2ecc71;");
                } else {
                    lblMensaje.setText("Error al generar PDF.");
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
                }
            });

            new Thread(task).start();
        }
    }

    private void cerrarVentana() {
        if (bibliotecaController != null) {
            bibliotecaController.cerrarOverlay();
        }
    }
}

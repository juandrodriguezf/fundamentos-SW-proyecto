package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.LimpiarCarritoUseCase;
import co.edu.javeriana.proyecto.application.usecase.RegistrarCompraUseCase;
import co.edu.javeriana.proyecto.application.usecase.VerCarritoUseCase;
import co.edu.javeriana.proyecto.domain.CarritoItem;
import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutController {

    // Progress labels
    @FXML private Label lblProgreso1;
    @FXML private Label lblProgreso2;
    @FXML private Label lblProgreso3;

    // Steps
    @FXML private VBox vboxPaso1;
    @FXML private VBox vboxPaso2;
    @FXML private VBox vboxPaso3;

    // Step 1: Address
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtPostal;
    @FXML private Button btnCancelar;
    @FXML private Button btnSiguiente1;

    // Step 2: Payment
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private VBox vboxDatosTarjeta;
    @FXML private TextField txtTarjetaNum;
    @FXML private TextField txtTarjetaVenc;
    @FXML private TextField txtTarjetaCvv;
    @FXML private Button btnAtras2;
    @FXML private Button btnSiguiente2;

    // Step 3: Review
    @FXML private ListView<CarritoItem> listResumen;
    @FXML private Label lblSubtotal;
    @FXML private Label lblImpuestos;
    @FXML private Label lblEnvio;
    @FXML private Label lblTotal;
    @FXML private Label lblMensajePago;
    @FXML private Button btnAtras3;
    @FXML private Button btnPagar;
    @FXML private Button btnVolver;

    private final Usuario usuario;
    private final VerCarritoUseCase verCarritoUseCase;
    private final LimpiarCarritoUseCase limpiarCarritoUseCase;
    private final RegistrarCompraUseCase registrarCompraUseCase;
    private final BibliotecaController bibliotecaController;

    private final ObservableList<CarritoItem> carritoObservable = FXCollections.observableArrayList();
    private double totalGlobal = 0.0;

    public CheckoutController(Usuario usuario, VerCarritoUseCase verCarritoUseCase, LimpiarCarritoUseCase limpiarCarritoUseCase, RegistrarCompraUseCase registrarCompraUseCase, BibliotecaController bibliotecaController) {
        this.usuario = usuario;
        this.verCarritoUseCase = verCarritoUseCase;
        this.limpiarCarritoUseCase = limpiarCarritoUseCase;
        this.registrarCompraUseCase = registrarCompraUseCase;
        this.bibliotecaController = bibliotecaController;
    }

    @FXML
    public void initialize() {
        cmbMetodoPago.setItems(FXCollections.observableArrayList("Tarjeta de Credito", "PSE", "PayPal (Sandbox)"));
        cmbMetodoPago.getSelectionModel().selectFirst();
        
        cmbMetodoPago.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTarjeta = "Tarjeta de Credito".equals(newVal);
            vboxDatosTarjeta.setVisible(isTarjeta);
            vboxDatosTarjeta.setManaged(isTarjeta);
        });

        listResumen.setItems(carritoObservable);
        listResumen.setCellFactory(param -> new ResumenCell());

        // Step 1 Navigation
        btnCancelar.setOnAction(e -> volverAlCatalogo());
        btnSiguiente1.setOnAction(e -> irAPaso2());

        // Step 2 Navigation
        btnAtras2.setOnAction(e -> irAPaso1());
        btnSiguiente2.setOnAction(e -> irAPaso3());

        // Step 3 Navigation
        btnAtras3.setOnAction(e -> irAPaso2());
        btnPagar.setOnAction(e -> procesarPago());
        btnVolver.setOnAction(e -> volverAlCatalogo());

        cargarResumen();
        actualizarProgreso(1);
    }

    private void irAPaso1() {
        vboxPaso1.setVisible(true); vboxPaso1.setManaged(true);
        vboxPaso2.setVisible(false); vboxPaso2.setManaged(false);
        vboxPaso3.setVisible(false); vboxPaso3.setManaged(false);
        actualizarProgreso(1);
    }

    private void irAPaso2() {
        // Validate fields simply
        if (txtNombre.getText().isEmpty() || txtDireccion.getText().isEmpty() || txtCiudad.getText().isEmpty()) {
            mostrarErrorAlerta("Por favor completa los datos de direccion y envio.");
            return;
        }
        vboxPaso1.setVisible(false); vboxPaso1.setManaged(false);
        vboxPaso2.setVisible(true); vboxPaso2.setManaged(true);
        vboxPaso3.setVisible(false); vboxPaso3.setManaged(false);
        actualizarProgreso(2);
    }

    private void irAPaso3() {
        if ("Tarjeta de Credito".equals(cmbMetodoPago.getValue())) {
            if (txtTarjetaNum.getText().isEmpty() || txtTarjetaVenc.getText().isEmpty() || txtTarjetaCvv.getText().isEmpty()) {
                mostrarErrorAlerta("Por favor completa los datos de la tarjeta.");
                return;
            }
        }
        vboxPaso1.setVisible(false); vboxPaso1.setManaged(false);
        vboxPaso2.setVisible(false); vboxPaso2.setManaged(false);
        vboxPaso3.setVisible(true); vboxPaso3.setManaged(true);
        actualizarProgreso(3);
    }

    private void actualizarProgreso(int paso) {
        lblProgreso1.setStyle("-fx-font-weight: " + (paso == 1 ? "bold" : "normal") + "; -fx-text-fill: " + (paso == 1 ? "#3498db" : "#7f8c8d") + ";");
        lblProgreso2.setStyle("-fx-font-weight: " + (paso == 2 ? "bold" : "normal") + "; -fx-text-fill: " + (paso == 2 ? "#3498db" : "#7f8c8d") + ";");
        lblProgreso3.setStyle("-fx-font-weight: " + (paso == 3 ? "bold" : "normal") + "; -fx-text-fill: " + (paso == 3 ? "#3498db" : "#7f8c8d") + ";");
    }

    private void mostrarErrorAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validacion");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void cargarResumen() {
        Task<List<CarritoItem>> task = new Task<>() {
            @Override
            protected List<CarritoItem> call() {
                return verCarritoUseCase.ejecutar(String.valueOf(usuario.getId()));
            }
        };

        task.setOnSucceeded(e -> {
            carritoObservable.setAll(task.getValue());
            calcularTotales();
        });

        new Thread(task).start();
    }

    private void calcularTotales() {
        double subtotal = carritoObservable.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        double impuestos = subtotal * 0.19; // 19% IVA
        double envio = 5.00;
        
        if (subtotal == 0) {
            envio = 0.0;
        }

        totalGlobal = subtotal + impuestos + envio;

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblImpuestos.setText(String.format("$%.2f", impuestos));
        lblEnvio.setText(String.format("$%.2f", envio));
        lblTotal.setText(String.format("$%.2f", totalGlobal));
        
        btnPagar.setDisable(subtotal == 0);
    }

    private void procesarPago() {
        if (carritoObservable.isEmpty()) {
            mostrarMensaje("Tu carrito está vacío.", Color.web("#e74c3c"));
            return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        btnPagar.setDisable(true);
        mostrarMensaje("Procesando pago con " + metodoPago + "...", Color.web("#3498db"));

        // Simular llamada a pasarela de pagos
        Task<Void> pagoTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000); // Simulación de red
                return null;
            }
        };

        pagoTask.setOnSucceeded(e -> {
            String ordenId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            mostrarMensaje("Pago Exitoso! Orden: " + ordenId + ". Recibo enviado a " + usuario.getEmail(), Color.web("#2ecc71"));
            
            // Registrar compras y limpiar carrito
            new Thread(() -> {
                // Guardar cada item como registro de compra
                List<Compra> compras = new ArrayList<>();
                for (CarritoItem item : carritoObservable) {
                    Compra compra = new Compra(null, usuario.getId(), item.getLibro().getId(),
                        item.getCantidad(), item.getLibro().getPrecio(), ordenId, LocalDateTime.now());
                    compras.add(compra);
                }
                registrarCompraUseCase.ejecutar(compras);
                limpiarCarritoUseCase.ejecutar(String.valueOf(usuario.getId()));
                Platform.runLater(() -> {
                    carritoObservable.clear();
                    calcularTotales();
                    btnPagar.setVisible(false);
                    btnPagar.setManaged(false);
                    btnAtras3.setVisible(false);
                    btnAtras3.setManaged(false);
                    btnVolver.setVisible(true);
                    btnVolver.setManaged(true);
                });
            }).start();
        });

        new Thread(pagoTask).start();
    }

    private void volverAlCatalogo() {
        if (bibliotecaController != null) {
            bibliotecaController.cerrarOverlay();
            bibliotecaController.setUsuario(usuario);
        } else {
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarMensaje(String msg, Color color) {
        lblMensajePago.setText(msg);
        lblMensajePago.setTextFill(color);
        lblMensajePago.setVisible(true);
        lblMensajePago.setManaged(true);
    }

    private class ResumenCell extends ListCell<CarritoItem> {
        @Override
        protected void updateItem(CarritoItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null); setGraphic(null);
            } else {
                HBox box = new HBox(10);
                
                VBox textInfo = new VBox();
                textInfo.setMinWidth(0);
                HBox.setHgrow(textInfo, Priority.ALWAYS);
                
                Label lblTitulo = new Label(item.getLibro().getTitulo());
                lblTitulo.setWrapText(true);
                lblTitulo.setMinHeight(Region.USE_PREF_SIZE);
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label lblDetalle = new Label(item.getCantidad() + "x $" + item.getLibro().getPrecio() + " = $" + item.getSubtotal());
                lblDetalle.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                
                textInfo.getChildren().addAll(lblTitulo, lblDetalle);
                box.getChildren().add(textInfo);
                setGraphic(box);
            }
        }
    }
}

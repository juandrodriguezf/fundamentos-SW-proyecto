package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.*;
import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.CarritoItem;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BibliotecaController {

    @FXML private ImageView imgFondo1;
    @FXML private ImageView imgFondo2;
    @FXML private StackPane paneFondoBase;
    @FXML private VBox vboxResultadosContainer;

    @FXML private TextField txtBusqueda;
    @FXML private ListView<Libro> listResultados;
    @FXML private ListView<Libro> listTendencias;
    @FXML private Label lblEstado;
    @FXML private Label lblFraseDia;
    
    @FXML private VBox vboxBusqueda;
    @FXML private VBox vboxCarrito;
    @FXML private Button btnToggleView;
    @FXML private Button btnContinuarProceso;
    @FXML private Button btnMisCompras;
    @FXML private Button btnMiBiblioteca;
    @FXML private Button btnPublicarLibro;
    @FXML private Button btnLogin;
    @FXML private Button btnLogout;
    @FXML private Button btnAdmin;
    @FXML private ListView<CarritoItem> listCarrito;
    @FXML private Label lblTotalCarrito;
    @FXML private StackPane overlayPlaceholder;

    private static final String ADMIN_EMAIL = "admin@openlib.com";

    // Filtros avanzados
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbOrdenamiento;
    @FXML private Slider sliderPrecioMax;
    @FXML private Label lblPrecioMax;
    @FXML private VBox vboxFiltros;

    // Recomendaciones
    @FXML private ListView<Libro> listRecomendaciones;
    @FXML private VBox vboxRecomendaciones;

    private final BuscarLibroUseCase buscarLibroUseCase;
    private final BuscarLibroAvanzadoUseCase buscarLibroAvanzadoUseCase;
    private final ObtenerTendenciasUseCase obtenerTendenciasUseCase;
    private final IncrementarClicsUseCase incrementarClicsUseCase;
    private final AgregarAlCarritoUseCase agregarAlCarritoUseCase;
    private final EliminarDelCarritoUseCase eliminarDelCarritoUseCase;
    private final VerCarritoUseCase verCarritoUseCase;
    private final LimpiarCarritoUseCase limpiarCarritoUseCase;
    private final RegistrarCompraUseCase registrarCompraUseCase;
    private final ObtenerRecomendacionesUseCase obtenerRecomendacionesUseCase;
    private final ObtenerHistorialOrdenesUseCase obtenerHistorialOrdenesUseCase;
    private final GenerarFacturaPdfUseCase generarFacturaPdfUseCase;
    private final ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUsuarioUseCase loginUsuarioUseCase;
    private final CambiarContrasenaUseCase cambiarContrasenaUseCase;
    private final SubirLibroUseCase subirLibroUseCase;
    private final UsuarioGateway usuarioGateway;
    private PauseTransition debounceTimer;

    private final ObservableList<Libro> resultadosObservable = FXCollections.observableArrayList();
    private final ObservableList<Libro> tendenciasObservable = FXCollections.observableArrayList();
    private final ObservableList<Libro> recomendacionesObservable = FXCollections.observableArrayList();
    
    // Command Invoker para patrón Command
    private final co.edu.javeriana.proyecto.application.command.CommandInvoker commandInvoker = new co.edu.javeriana.proyecto.application.command.CommandInvoker();
    private final ObservableList<CarritoItem> carritoObservable = FXCollections.observableArrayList();

    private String sessionId;
    private Usuario usuarioActual;
    
    private final String[] imagesArray = {"/images/hero_banner.png", "/images/hero_banner_2.png", "/images/hero_banner_3.png"};
    private int currentImageIndex = 0;

    public BibliotecaController(BuscarLibroUseCase buscarLibroUseCase, 
                                BuscarLibroAvanzadoUseCase buscarLibroAvanzadoUseCase,
                                ObtenerTendenciasUseCase obtenerTendenciasUseCase,
                                IncrementarClicsUseCase incrementarClicsUseCase,
                                AgregarAlCarritoUseCase agregarAlCarritoUseCase,
                                EliminarDelCarritoUseCase eliminarDelCarritoUseCase,
                                VerCarritoUseCase verCarritoUseCase,
                                LimpiarCarritoUseCase limpiarCarritoUseCase,
                                RegistrarCompraUseCase registrarCompraUseCase,
                                ObtenerRecomendacionesUseCase obtenerRecomendacionesUseCase,
                                ObtenerHistorialOrdenesUseCase obtenerHistorialOrdenesUseCase,
                                GenerarFacturaPdfUseCase generarFacturaPdfUseCase,
                                ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase,
                                RegistrarUsuarioUseCase registrarUsuarioUseCase,
                                LoginUsuarioUseCase loginUsuarioUseCase,
                                CambiarContrasenaUseCase cambiarContrasenaUseCase,
                                SubirLibroUseCase subirLibroUseCase,
                                UsuarioGateway usuarioGateway) {
        this.buscarLibroUseCase = buscarLibroUseCase;
        this.buscarLibroAvanzadoUseCase = buscarLibroAvanzadoUseCase;
        this.obtenerTendenciasUseCase = obtenerTendenciasUseCase;
        this.incrementarClicsUseCase = incrementarClicsUseCase;
        this.agregarAlCarritoUseCase = agregarAlCarritoUseCase;
        this.eliminarDelCarritoUseCase = eliminarDelCarritoUseCase;
        this.verCarritoUseCase = verCarritoUseCase;
        this.limpiarCarritoUseCase = limpiarCarritoUseCase;
        this.registrarCompraUseCase = registrarCompraUseCase;
        this.obtenerRecomendacionesUseCase = obtenerRecomendacionesUseCase;
        this.obtenerHistorialOrdenesUseCase = obtenerHistorialOrdenesUseCase;
        this.generarFacturaPdfUseCase = generarFacturaPdfUseCase;
        this.obtenerBibliotecaPersonalUseCase = obtenerBibliotecaPersonalUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUsuarioUseCase = loginUsuarioUseCase;
        this.cambiarContrasenaUseCase = cambiarContrasenaUseCase;
        this.subirLibroUseCase = subirLibroUseCase;
        this.usuarioGateway = usuarioGateway;
        this.sessionId = UUID.randomUUID().toString();
    }

    @FXML
    public void initialize() {
        listResultados.setItems(resultadosObservable);
        listTendencias.setItems(tendenciasObservable);
        listCarrito.setItems(carritoObservable);
        if (listRecomendaciones != null) {
            listRecomendaciones.setItems(recomendacionesObservable);
            listRecomendaciones.setCellFactory(param -> new LibroCell(true));
        }

        listResultados.setCellFactory(param -> new LibroCell(false));
        listTendencias.setCellFactory(param -> new LibroCell(true));
        listCarrito.setCellFactory(param -> new CarritoCell());

        listResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrarClic(newVal);
                abrirDetalleLibro(newVal);
                javafx.application.Platform.runLater(() -> listResultados.getSelectionModel().clearSelection());
            }
        });
        
        listTendencias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrarClic(newVal);
                abrirDetalleLibro(newVal);
                javafx.application.Platform.runLater(() -> listTendencias.getSelectionModel().clearSelection());
            }
        });
        
        if (listRecomendaciones != null) {
            listRecomendaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    registrarClic(newVal);
                    abrirDetalleLibro(newVal);
                    javafx.application.Platform.runLater(() -> listRecomendaciones.getSelectionModel().clearSelection());
                }
            });
        }

        btnToggleView.setOnAction(e -> toggleView());
        btnContinuarProceso.setOnAction(e -> continuarProceso());
        if (btnMisCompras != null) {
            btnMisCompras.setOnAction(e -> abrirMisComprasModal());
        }
        if (btnMiBiblioteca != null) {
            btnMiBiblioteca.setOnAction(e -> abrirMiBibliotecaModal());
        }
        if (btnPublicarLibro != null) {
            btnPublicarLibro.setOnAction(e -> publicarLibro());
        }
        
        if (btnLogin != null) {
            btnLogin.setOnAction(e -> abrirRegistroModal());
        }
        if (btnLogout != null) {
            btnLogout.setOnAction(e -> {
                co.edu.javeriana.proyecto.application.SessionManager.getInstance().logout();
                setUsuario(null);
            });
        }
        if (btnAdmin != null) {
            btnAdmin.setOnAction(e -> abrirPanelAdmin());
        }

        configurarFraseDelDia();
        configurarFondoYAnimaciones();
        configurarFiltros();
        cargarTendencias();
        cargarCarrito();
        cargarRecomendaciones();

        // Debounce de 300ms para búsqueda
        debounceTimer = new PauseTransition(Duration.millis(300));
        debounceTimer.setOnFinished(e -> ejecutarBusquedaAvanzada());
    }

    private void configurarFiltros() {
        // Cargar categorías desde BD
        Task<List<String>> catTask = new Task<>() {
            @Override protected List<String> call() { return buscarLibroAvanzadoUseCase.obtenerCategorias(); }
        };
        catTask.setOnSucceeded(e -> {
            if (cmbCategoria != null) {
                cmbCategoria.getItems().clear();
                cmbCategoria.getItems().add("Todas");
                cmbCategoria.getItems().addAll(catTask.getValue());
                cmbCategoria.getSelectionModel().selectFirst();
                cmbCategoria.setOnAction(ev -> dispararBusqueda());
            }
        });
        new Thread(catTask).start();

        // Configurar ordenamiento
        if (cmbOrdenamiento != null) {
            cmbOrdenamiento.setItems(FXCollections.observableArrayList(
                "Relevancia", "Precio: menor a mayor", "Precio: mayor a menor", "Mejor calificados", "Mas populares"
            ));
            cmbOrdenamiento.getSelectionModel().selectFirst();
            cmbOrdenamiento.setOnAction(e -> dispararBusqueda());
        }

        // Configurar slider de precio
        if (sliderPrecioMax != null) {
            sliderPrecioMax.setMin(0);
            sliderPrecioMax.setMax(100);
            sliderPrecioMax.setValue(100);
            sliderPrecioMax.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (lblPrecioMax != null) lblPrecioMax.setText(String.format("Max: $%.0f", newVal.doubleValue()));
                dispararBusqueda();
            });
        }
    }

    private void dispararBusqueda() {
        if (debounceTimer != null) {
            debounceTimer.playFromStart();
        }
    }

    private void ejecutarBusquedaAvanzada() {
        String texto = txtBusqueda.getText();
        String categoria = cmbCategoria != null ? cmbCategoria.getValue() : "Todas";
        double precioMax = sliderPrecioMax != null ? sliderPrecioMax.getValue() : 100;
        String ordenSeleccion = cmbOrdenamiento != null ? cmbOrdenamiento.getValue() : "Relevancia";

        String ordenamiento;
        switch (ordenSeleccion != null ? ordenSeleccion : "Relevancia") {
            case "Precio: menor a mayor": ordenamiento = "precio_asc"; break;
            case "Precio: mayor a menor": ordenamiento = "precio_desc"; break;
            case "Mejor calificados": ordenamiento = "mejor_calificados"; break;
            case "Mas populares": ordenamiento = "mas_populares"; break;
            default: ordenamiento = "relevancia"; break;
        }

        if ((texto == null || texto.trim().isEmpty()) && "Todas".equals(categoria) && precioMax >= 100) {
            resultadosObservable.clear();
            return;
        }

        lblEstado.setText("Buscando...");
        final String cat = categoria;
        final String ord = ordenamiento;
        final double pMax = precioMax >= 100 ? 0 : precioMax;
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() {
                return buscarLibroAvanzadoUseCase.ejecutar(texto, cat, 0, pMax, ord);
            }
        };
        task.setOnSucceeded(e -> {
            resultadosObservable.setAll(task.getValue());
            lblEstado.setText(task.getValue().size() + " resultado(s) encontrado(s).");
        });
        task.setOnFailed(e -> lblEstado.setText("Error en busqueda."));
        new Thread(task).start();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        
        if (usuario == null) {
            this.sessionId = UUID.randomUUID().toString();
            if (btnMisCompras != null) { btnMisCompras.setVisible(false); btnMisCompras.setManaged(false); }
            if (btnMiBiblioteca != null) { btnMiBiblioteca.setVisible(false); btnMiBiblioteca.setManaged(false); }
            if (btnLogin != null) { btnLogin.setVisible(true); btnLogin.setManaged(true); }
            if (btnLogout != null) { btnLogout.setVisible(false); btnLogout.setManaged(false); }
            if (btnAdmin != null) { btnAdmin.setVisible(false); btnAdmin.setManaged(false); }
            carritoObservable.clear();
            cargarCarrito();
            return;
        }

        String oldSessionId = this.sessionId;
        this.sessionId = String.valueOf(usuario.getId());
        
        if (btnMisCompras != null) {
            btnMisCompras.setVisible(true);
            btnMisCompras.setManaged(true);
        }
        if (btnMiBiblioteca != null) {
            btnMiBiblioteca.setVisible(true);
            btnMiBiblioteca.setManaged(true);
        }
        if (btnLogin != null) {
            btnLogin.setVisible(false);
            btnLogin.setManaged(false);
        }
        if (btnLogout != null) {
            btnLogout.setVisible(true);
            btnLogout.setManaged(true);
        }
        // Botón Admin: solo visible para el email admin hardcodeado
        boolean esAdmin = ADMIN_EMAIL.equals(usuario.getEmail());
        if (btnAdmin != null) {
            btnAdmin.setVisible(esAdmin);
            btnAdmin.setManaged(esAdmin);
        }
        
        if (!oldSessionId.equals(this.sessionId) && !carritoObservable.isEmpty()) {
            for (CarritoItem item : carritoObservable) {
                commandInvoker.executeCommand(new co.edu.javeriana.proyecto.application.command.AgregarAlCarritoCommand(
                    agregarAlCarritoUseCase, this.sessionId, item.getLibro().getId(), item.getCantidad()
                ));
            }
        }
        cargarCarrito();
        cargarRecomendaciones(); // Refrescar recomendaciones con historial del usuario
    }

    private void configurarFondoYAnimaciones() {
        try {
            imgFondo1.setImage(new Image(getClass().getResource(imagesArray[0]).toExternalForm()));
            
            imgFondo1.fitWidthProperty().bind(paneFondoBase.widthProperty());
            imgFondo1.fitHeightProperty().bind(paneFondoBase.heightProperty());
            imgFondo2.fitWidthProperty().bind(paneFondoBase.widthProperty());
            imgFondo2.fitHeightProperty().bind(paneFondoBase.heightProperty());

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(7), e -> rotarImagenFondo()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (Exception ex) {
            System.err.println("Error cargando imágenes de fondo: " + ex.getMessage());
        }

        GaussianBlur blurEffect = new GaussianBlur(0);
        paneFondoBase.setEffect(blurEffect);

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                vboxResultadosContainer.setVisible(true);
                vboxResultadosContainer.setManaged(true);
                if (vboxFiltros != null) { vboxFiltros.setVisible(true); vboxFiltros.setManaged(true); }
                blurEffect.setRadius(25); 
            } else {
                vboxResultadosContainer.setVisible(false);
                vboxResultadosContainer.setManaged(false);
                if (vboxFiltros != null) { vboxFiltros.setVisible(false); vboxFiltros.setManaged(false); }
                blurEffect.setRadius(txtBusqueda.isFocused() ? 10 : 0);
            }
            dispararBusqueda();
        });

        txtBusqueda.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (txtBusqueda.getText() == null || txtBusqueda.getText().trim().isEmpty()) {
                blurEffect.setRadius(isFocused ? 10 : 0);
            }
        });
    }

    private void rotarImagenFondo() {
        try {
            int nextIndex = (currentImageIndex + 1) % imagesArray.length;
            Image nextImage = new Image(getClass().getResource(imagesArray[nextIndex]).toExternalForm());
            
            imgFondo2.setImage(nextImage);
            
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), imgFondo2);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            fadeIn.setOnFinished(e -> {
                imgFondo1.setImage(nextImage);
                imgFondo2.setOpacity(0.0);
                currentImageIndex = nextIndex;
                configurarFraseDelDia(); // Cambiar frase al mismo tiempo
            });
            
            fadeIn.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configurarFraseDelDia() {
        if (lblFraseDia != null) {
            String[] frases = {
                "\"La pluma es la lengua del alma.\" – Miguel de Cervantes",
                "\"De los diversos instrumentos del hombre, el más asombroso es el libro. Es una extensión de la memoria y de la imaginación.\" – Jorge Luis Borges",
                "\"El que lee mucho y anda mucho, ve mucho y sabe mucho.\" – Miguel de Cervantes",
                "\"Estar a solas con un buen libro es ser capaz de comprenderte más a ti mismo.\" – Harold Bloom",
                "\"Aprender a leer es encender un fuego; cada sílaba que se deletrea es una chispa.\" – Victor Hugo",
                "\"Siempre imaginé que el Paraíso sería algún tipo de biblioteca.\" – Jorge Luis Borges",
                "\"Un libro debe ser el hacha que rompa el mar helado dentro de nosotros.\" – Franz Kafka",
                "\"Uno no es lo que es por lo que escribe, sino por lo que ha leído.\" – Jorge Luis Borges",
                "\"La vida es un cuento contado por un idiota, lleno de ruido y de furia, que no tiene ningún sentido.\" – William Shakespeare",
                "\"El mundo habrá acabado de joderse el día en que los hombres viajen en primera clase y la literatura en el vagón de carga.\" – Gabriel García Márquez"
            };
            int index = new java.util.Random().nextInt(frases.length);
            lblFraseDia.setText(frases[index]);
        }
    }

    private void toggleView() {
        if (vboxBusqueda.isVisible()) {
            vboxBusqueda.setVisible(false);
            vboxCarrito.setVisible(true);
            btnToggleView.setText("🏠 Volver al Catálogo");
            cargarCarrito();
        } else {
            vboxCarrito.setVisible(false);
            vboxBusqueda.setVisible(true);
            int totalItems = carritoObservable.stream().mapToInt(CarritoItem::getCantidad).sum();
            btnToggleView.setText("🛒 Ver Carrito (" + totalItems + ")");
        }
    }

    private void continuarProceso() {
        if (usuarioActual == null) {
            abrirRegistroModal();
        }
        
        // Después de cerrar el modal de login, si el usuario se logueó, abrir checkout
        if (usuarioActual != null) {
            abrirCheckoutModal();
        }
    }

    public void mostrarOverlay(javafx.scene.Parent root) {
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        btnCerrar.setOnAction(e -> cerrarOverlay());
        
        HBox topBar = new HBox(btnCerrar);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        VBox container = new VBox(10, topBar, root);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        
        overlayPlaceholder.getChildren().setAll(container);
        overlayPlaceholder.setVisible(true);
        overlayPlaceholder.setManaged(true);
    }

    public void cerrarOverlay() {
        overlayPlaceholder.setVisible(false);
        overlayPlaceholder.getChildren().clear();
        overlayPlaceholder.setManaged(false);
    }

    private void abrirRegistroModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/RegistroView.fxml"));
            RegistroController registroController = new RegistroController(
                registrarUsuarioUseCase,
                loginUsuarioUseCase,
                cambiarContrasenaUseCase,
                usuarioGateway,
                this
            );
            loader.setController(registroController);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirCheckoutModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/CheckoutView.fxml"));
            CheckoutController checkoutController = new CheckoutController(
                usuarioActual,
                verCarritoUseCase,
                limpiarCarritoUseCase,
                registrarCompraUseCase,
                this
            );
            loader.setController(checkoutController);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirMisComprasModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/HistorialComprasView.fxml"));
            HistorialComprasController historialController = new HistorialComprasController(
                usuarioActual,
                obtenerHistorialOrdenesUseCase,
                generarFacturaPdfUseCase,
                this
            );
            loader.setController(historialController);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirMiBibliotecaModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/BibliotecaPersonalView.fxml"));
            BibliotecaPersonalController bibliotecaPersonalController = new BibliotecaPersonalController(
                usuarioActual,
                obtenerBibliotecaPersonalUseCase,
                this
            );
            loader.setController(bibliotecaPersonalController);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void publicarLibro() {
        // Paso 1: Si no ha iniciado sesión, abrir login/registro primero
        if (usuarioActual == null) {
            abrirRegistroModal();
        }
        // Paso 2: Si ya inició sesión (o acaba de iniciarla), abrir el formulario
        if (usuarioActual != null) {
            abrirSubirLibroModal();
        }
    }

    private void abrirSubirLibroModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/SubirLibroView.fxml"));
            SubirLibroController controller = new SubirLibroController(subirLibroUseCase, this);
            loader.setController(controller);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private final co.edu.javeriana.proyecto.application.port.out.ReviewsGateway reviewsGateway = 
        new co.edu.javeriana.proyecto.infrastructure.adapter.out.rest.RestReviewsGateway("http://localhost:8081/api/reviews");

    // Dependencias para el panel de administración (inyectadas desde Main)
    private co.edu.javeriana.proyecto.application.usecase.ObtenerMetricasAdminUseCase metricsUC;
    private co.edu.javeriana.proyecto.application.usecase.GestionarUsuariosAdminUseCase gestionarUsuariosUC;
    private co.edu.javeriana.proyecto.application.usecase.GestionarLibrosAdminUseCase gestionarLibrosUC;
    private co.edu.javeriana.proyecto.application.usecase.GestionarCategoriasUseCase gestionarCategoriasUC;
    private co.edu.javeriana.proyecto.application.usecase.GestionarEtiquetasUseCase gestionarEtiquetasUC;
    private co.edu.javeriana.proyecto.application.usecase.ValidarCalidadDatosUseCase validarCalidadUC;

    public void setAdminUseCases(
            co.edu.javeriana.proyecto.application.usecase.ObtenerMetricasAdminUseCase metricsUC,
            co.edu.javeriana.proyecto.application.usecase.GestionarUsuariosAdminUseCase gestionarUsuariosUC,
            co.edu.javeriana.proyecto.application.usecase.GestionarLibrosAdminUseCase gestionarLibrosUC,
            co.edu.javeriana.proyecto.application.usecase.GestionarCategoriasUseCase gestionarCategoriasUC,
            co.edu.javeriana.proyecto.application.usecase.GestionarEtiquetasUseCase gestionarEtiquetasUC,
            co.edu.javeriana.proyecto.application.usecase.ValidarCalidadDatosUseCase validarCalidadUC) {
        this.metricsUC         = metricsUC;
        this.gestionarUsuariosUC  = gestionarUsuariosUC;
        this.gestionarLibrosUC    = gestionarLibrosUC;
        this.gestionarCategoriasUC= gestionarCategoriasUC;
        this.gestionarEtiquetasUC = gestionarEtiquetasUC;
        this.validarCalidadUC     = validarCalidadUC;
    }

    private void abrirPanelAdmin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/AdminView.fxml"));
            co.edu.javeriana.proyecto.infrastructure.adapter.in.ui.AdminController adminController =
                new co.edu.javeriana.proyecto.infrastructure.adapter.in.ui.AdminController(
                    metricsUC, gestionarUsuariosUC, gestionarLibrosUC,
                    gestionarCategoriasUC, gestionarEtiquetasUC, validarCalidadUC
                );
            loader.setController(adminController);
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirDetalleLibro(Libro libro) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/DetalleLibroView.fxml"));
            DetalleLibroController controller = new DetalleLibroController(
                libro, 
                usuarioActual, 
                reviewsGateway, 
                () -> {
                    agregarAlCarrito(libro);
                    cerrarOverlay();
                }
            );
            loader.setController(controller);
            
            javafx.scene.Parent root = loader.load();
            mostrarOverlay(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void buscarLibros(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            resultadosObservable.clear();
            return;
        }
        lblEstado.setText("Buscando...");
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() { return buscarLibroUseCase.ejecutar(filtro); }
        };
        task.setOnSucceeded(e -> {
            resultadosObservable.setAll(task.getValue());
            lblEstado.setText("Búsqueda completada.");
        });
        task.setOnFailed(e -> lblEstado.setText("Error en búsqueda."));
        new Thread(task).start();
    }

    private void cargarTendencias() {
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() { return obtenerTendenciasUseCase.ejecutar(5); }
        };
        task.setOnSucceeded(e -> tendenciasObservable.setAll(task.getValue()));
        new Thread(task).start();
    }

    public void cargarRecomendaciones() {
        Long userId = usuarioActual != null ? usuarioActual.getId() : null;
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() {
                return obtenerRecomendacionesUseCase.ejecutar(userId, 6);
            }
        };
        task.setOnSucceeded(e -> {
            recomendacionesObservable.setAll(task.getValue());
            if (vboxRecomendaciones != null) {
                boolean hasRecs = !recomendacionesObservable.isEmpty();
                vboxRecomendaciones.setVisible(hasRecs);
                vboxRecomendaciones.setManaged(hasRecs);
            }
        });
        new Thread(task).start();
    }

    private void registrarClic(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { incrementarClicsUseCase.ejecutar(libro.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarTendencias());
        new Thread(task).start();
    }

    private void agregarAlCarrito(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { 
                commandInvoker.executeCommand(new co.edu.javeriana.proyecto.application.command.AgregarAlCarritoCommand(
                    agregarAlCarritoUseCase, sessionId, libro.getId(), 1
                ));
                return null; 
            }
        };
        task.setOnSucceeded(e -> cargarCarrito());
        new Thread(task).start();
    }

    private void eliminarDelCarrito(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { 
                commandInvoker.executeCommand(new co.edu.javeriana.proyecto.application.command.EliminarDelCarritoCommand(
                    eliminarDelCarritoUseCase, sessionId, libro.getId()
                ));
                return null; 
            }
        };
        task.setOnSucceeded(e -> cargarCarrito());
        new Thread(task).start();
    }

    private void cargarCarrito() {
        Task<List<CarritoItem>> task = new Task<>() {
            @Override protected List<CarritoItem> call() { return verCarritoUseCase.ejecutar(sessionId); }
        };
        task.setOnSucceeded(e -> {
            carritoObservable.setAll(task.getValue());
            double total = carritoObservable.stream().mapToDouble(CarritoItem::getSubtotal).sum();
            lblTotalCarrito.setText(String.format("$%.2f", total));
            if (vboxBusqueda.isVisible()) {
                int totalItems = carritoObservable.stream().mapToInt(CarritoItem::getCantidad).sum();
                btnToggleView.setText("🛒 Ver Carrito (" + totalItems + ")");
            }
        });
        new Thread(task).start();
    }

    private class LibroCell extends ListCell<Libro> {
        private final boolean isDarkTheme;

        public LibroCell(boolean isDarkTheme) {
            this.isDarkTheme = isDarkTheme;
        }

        @Override
        protected void updateItem(Libro libro, boolean empty) {
            super.updateItem(libro, empty);
            if (empty || libro == null) {
                setText(null); setGraphic(null);
            } else {
                HBox box = new HBox(10);
                box.prefWidthProperty().bind(getListView().widthProperty().subtract(30));
                
                ImageView imgPortada = new ImageView();
                try {
                    Image cachedImg = getCachedImage(libro.getPortada(), 50, 75);
                    if (cachedImg != null) {
                        imgPortada.setImage(cachedImg);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo cargar la portada: " + e.getMessage());
                }

                VBox textInfo = new VBox();
                textInfo.setMinWidth(0); 
                HBox.setHgrow(textInfo, Priority.ALWAYS); 
                
                String colorPrincipal = isDarkTheme ? "#ecf0f1" : "#2c3e50";
                String colorSecundario = isDarkTheme ? "#bdc3c7" : "#7f8c8d";

                Label lblTitulo = new Label(libro.getTitulo());
                lblTitulo.setWrapText(true);
                lblTitulo.setMinHeight(Region.USE_PREF_SIZE); 
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorPrincipal + ";");
                
                Label lblAutor = new Label("Autor: " + libro.getAutor() + " | Precio: $" + libro.getPrecio());
                lblAutor.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorSecundario + ";");
                lblAutor.setWrapText(true);
                lblAutor.setMinHeight(Region.USE_PREF_SIZE);
                
                textInfo.getChildren().addAll(lblTitulo, lblAutor);

                Button btnAdd = new Button("🛒 Añadir");
                btnAdd.setMinWidth(Region.USE_PREF_SIZE); // EVITAR QUE SE CORTE
                
                if (libro.getStock() <= 0) {
                    btnAdd.setText("❌ Agotado");
                    btnAdd.setDisable(true);
                    btnAdd.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                    btnAdd.setOnAction(e -> {
                        agregarAlCarrito(libro);
                        e.consume(); // Prevent selection trigger
                    });
                }

                box.getChildren().addAll(imgPortada, textInfo, btnAdd);
                setGraphic(box);
            }
        }
    }

    private class CarritoCell extends ListCell<CarritoItem> {
        @Override
        protected void updateItem(CarritoItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null); setGraphic(null);
            } else {
                Libro libro = item.getLibro();
                HBox box = new HBox(10);
                box.prefWidthProperty().bind(getListView().widthProperty().subtract(30));
                
                ImageView imgPortada = new ImageView();
                try {
                    Image cachedImg = getCachedImage(libro.getPortada(), 40, 60);
                    if (cachedImg != null) {
                        imgPortada.setImage(cachedImg);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo cargar la portada: " + e.getMessage());
                }

                VBox textInfo = new VBox();
                textInfo.setMinWidth(0);
                HBox.setHgrow(textInfo, Priority.ALWAYS);
                
                Label lblTitulo = new Label(libro.getTitulo() + " (x" + item.getCantidad() + ")");
                lblTitulo.setWrapText(true);
                lblTitulo.setMinHeight(Region.USE_PREF_SIZE);
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                
                Label lblSubtotal = new Label("Subtotal: $" + item.getSubtotal());
                lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
                textInfo.getChildren().addAll(lblTitulo, lblSubtotal);

                Button btnRemove = new Button("🗑 Eliminar");
                btnRemove.setMinWidth(Region.USE_PREF_SIZE); // EVITAR QUE SE CORTE
                btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnRemove.setOnAction(e -> eliminarDelCarrito(libro));

                box.getChildren().addAll(imgPortada, textInfo, btnRemove);
                setGraphic(box);
            }
        }
    }

    private static final java.util.Map<String, Image> imageCache = new java.util.HashMap<>();
    
    public static Image getCachedImage(String url, double requestedWidth, double requestedHeight) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String key = url + "_" + requestedWidth + "_" + requestedHeight;
        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        try {
            Image img;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (java.io.InputStream in = connection.getInputStream()) {
                    img = new Image(in, requestedWidth, requestedHeight, true, true);
                }
            } else {
                var resource = BibliotecaController.class.getResource(url);
                if (resource != null) {
                    img = new Image(resource.toExternalForm(), requestedWidth, requestedHeight, true, true);
                } else {
                    img = new Image(url, requestedWidth, requestedHeight, true, true);
                }
            }
            imageCache.put(key, img);
            return img;
        } catch (Exception e) {
            System.err.println("Error loading image: " + url + " - " + e.getMessage());
            return null;
        }
    }
}

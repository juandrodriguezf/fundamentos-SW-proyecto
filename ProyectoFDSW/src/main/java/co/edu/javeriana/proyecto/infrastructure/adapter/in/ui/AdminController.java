package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.*;
import co.edu.javeriana.proyecto.domain.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class AdminController {

    // ──────────────────────────────────────────────
    // FXML Components
    // ──────────────────────────────────────────────
    @FXML private TabPane tabPane;

    // Dashboard
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblUsuariosActivos;
    @FXML private Label lblUsuariosBloqueados;
    @FXML private Label lblTotalLibros;
    @FXML private Label lblLibrosPendientes;
    @FXML private Label lblIngresos;
    @FXML private ListView<String> listTopLibros;
    @FXML private ListView<String> listCategoriasDistrib;

    // Curación
    @FXML private ListView<String> listLibrosProblemas;
    @FXML private ListView<String> listUsuariosBloqueados;
    @FXML private Button btnRecargarCuracion;

    // Usuarios
    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, Long>    colUsuId;
    @FXML private TableColumn<Usuario, String>  colUsuNombre;
    @FXML private TableColumn<Usuario, String>  colUsuEmail;
    @FXML private TableColumn<Usuario, Boolean> colUsuActivo;
    @FXML private TableColumn<Usuario, Integer> colUsuIntentos;
    @FXML private TextField txtBuscarUsuario;

    // Libros
    @FXML private TableView<Libro> tableLibros;
    @FXML private TableColumn<Libro, Long>   colLibId;
    @FXML private TableColumn<Libro, String> colLibTitulo;
    @FXML private TableColumn<Libro, String> colLibAutor;
    @FXML private TableColumn<Libro, String> colLibCategoria;
    @FXML private TableColumn<Libro, Double> colLibPrecio;
    @FXML private TableColumn<Libro, Integer>colLibStock;
    @FXML private TableColumn<Libro, String> colLibEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;

    // Categorías
    @FXML private TableView<Categoria> tableCategorias;
    @FXML private TableColumn<Categoria, Long>   colCatId;
    @FXML private TableColumn<Categoria, String> colCatNombre;
    @FXML private TableColumn<Categoria, String> colCatDesc;
    @FXML private TableColumn<Categoria, Integer>colCatTotal;
    @FXML private TextField txtNombreCategoria;
    @FXML private TextField txtDescCategoria;

    // Etiquetas
    @FXML private TableView<Etiqueta> tableEtiquetas;
    @FXML private TableColumn<Etiqueta, Long>    colEtqId;
    @FXML private TableColumn<Etiqueta, String>  colEtqNombre;
    @FXML private TableColumn<Etiqueta, Integer> colEtqTotal;
    @FXML private TextField txtNombreEtiqueta;

    // ──────────────────────────────────────────────
    // Use cases
    // ──────────────────────────────────────────────
    private final ObtenerMetricasAdminUseCase metricsUC;
    private final GestionarUsuariosAdminUseCase usuariosUC;
    private final GestionarLibrosAdminUseCase librosUC;
    private final GestionarCategoriasUseCase categoriasUC;
    private final GestionarEtiquetasUseCase etiquetasUC;
    private final ValidarCalidadDatosUseCase calidadUC;

    // Observable lists
    private final ObservableList<Usuario>  usuariosObs  = FXCollections.observableArrayList();
    private final ObservableList<Libro>    librosObs    = FXCollections.observableArrayList();
    private final ObservableList<Categoria>categoriasObs= FXCollections.observableArrayList();
    private final ObservableList<Etiqueta> etiquetasObs = FXCollections.observableArrayList();

    public AdminController(
            ObtenerMetricasAdminUseCase metricsUC,
            GestionarUsuariosAdminUseCase usuariosUC,
            GestionarLibrosAdminUseCase librosUC,
            GestionarCategoriasUseCase categoriasUC,
            GestionarEtiquetasUseCase etiquetasUC,
            ValidarCalidadDatosUseCase calidadUC) {
        this.metricsUC   = metricsUC;
        this.usuariosUC  = usuariosUC;
        this.librosUC    = librosUC;
        this.categoriasUC= categoriasUC;
        this.etiquetasUC = etiquetasUC;
        this.calidadUC   = calidadUC;
    }

    @FXML
    public void initialize() {
        configurarTablaUsuarios();
        configurarTablaLibros();
        configurarTablaCategorias();
        configurarTablaEtiquetas();

        // Cargar datos de la pestaña activa y escuchar cambios de tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                switch (newTab.getId() != null ? newTab.getId() : "") {
                    case "tabDashboard":  cargarDashboard(); break;
                    case "tabCuracion":   cargarCuracion(); break;
                    case "tabUsuarios":   cargarUsuarios(); break;
                    case "tabLibros":     cargarLibros(); break;
                    case "tabCategorias": cargarCategorias(); break;
                    case "tabEtiquetas":  cargarEtiquetas(); break;
                }
            }
        });

        // Cargar dashboard al inicio
        cargarDashboard();
    }

    // ──────────────────────────────────────────────
    // DASHBOARD
    // ──────────────────────────────────────────────
    private void cargarDashboard() {
        Task<MetricasAdmin> task = new Task<>() {
            @Override protected MetricasAdmin call() { return metricsUC.ejecutar(); }
        };
        task.setOnSucceeded(e -> {
            MetricasAdmin m = task.getValue();
            lblTotalUsuarios.setText(String.valueOf(m.getTotalUsuarios()));
            lblUsuariosActivos.setText(String.valueOf(m.getUsuariosActivos()));
            lblUsuariosBloqueados.setText(String.valueOf(m.getUsuariosBloqueados()));
            lblTotalLibros.setText(String.valueOf(m.getTotalLibros()));
            lblLibrosPendientes.setText(String.valueOf(m.getLibrosPendientes()));
            lblIngresos.setText(String.format("$%.2f", m.getIngresosTotales()));

            if (listTopLibros != null && m.getTopLibros() != null) {
                ObservableList<String> topItems = FXCollections.observableArrayList();
                int rank = 1;
                for (Libro l : m.getTopLibros()) {
                    topItems.add(rank++ + ". " + l.getTitulo() + " (" + l.getClics() + " clics)");
                }
                listTopLibros.setItems(topItems);
            }

            if (listCategoriasDistrib != null && m.getLibrosPorCategoria() != null) {
                ObservableList<String> catItems = FXCollections.observableArrayList();
                for (Map.Entry<String, Integer> entry : m.getLibrosPorCategoria().entrySet()) {
                    catItems.add(entry.getKey() + " → " + entry.getValue() + " libros");
                }
                listCategoriasDistrib.setItems(catItems);
            }
        });
        new Thread(task).start();
    }

    // ──────────────────────────────────────────────
    // CURACIÓN
    // ──────────────────────────────────────────────
    private void cargarCuracion() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                List<Libro>   problemas  = calidadUC.obtenerLibrosConProblemas();
                List<Usuario> bloqueados = calidadUC.obtenerUsuariosBloqueados();
                Platform.runLater(() -> {
                    ObservableList<String> pItems = FXCollections.observableArrayList();
                    for (Libro l : problemas) {
                        String problemaDesc = "";
                        if (l.getPortada() == null || l.getPortada().isEmpty()) problemaDesc += "sin portada ";
                        if (l.getIsbn() == null || l.getIsbn().isEmpty()) problemaDesc += "sin ISBN ";
                        if (l.getPrecio() == 0) problemaDesc += "precio=0 ";
                        pItems.add("[ID " + l.getId() + "] " + l.getTitulo() + " — ⚠ " + problemaDesc.trim());
                    }
                    listLibrosProblemas.setItems(pItems);

                    ObservableList<String> bItems = FXCollections.observableArrayList();
                    for (Usuario u : bloqueados) {
                        bItems.add("[ID " + u.getId() + "] " + u.getNombre() + " <" + u.getEmail() + "> — "
                                + u.getIntentosFallidos() + " intentos fallidos");
                    }
                    listUsuariosBloqueados.setItems(bItems);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void recargarCuracion() { cargarCuracion(); }

    // ──────────────────────────────────────────────
    // USUARIOS
    // ──────────────────────────────────────────────
    private void configurarTablaUsuarios() {
        colUsuId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsuActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colUsuIntentos.setCellValueFactory(new PropertyValueFactory<>("intentosFallidos"));
        tableUsuarios.setItems(usuariosObs);

        // Búsqueda en tiempo real
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((obs, old, val) -> filtrarUsuarios(val));
        }
    }

    private void cargarUsuarios() {
        Task<List<Usuario>> task = new Task<>() {
            @Override protected List<Usuario> call() { return usuariosUC.listarTodos(); }
        };
        task.setOnSucceeded(e -> usuariosObs.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void filtrarUsuarios(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarUsuarios();
            return;
        }
        String f = filtro.toLowerCase();
        usuariosObs.removeIf(u ->
            !u.getNombre().toLowerCase().contains(f) &&
            !u.getEmail().toLowerCase().contains(f)
        );
    }

    @FXML
    public void activarUsuario() {
        Usuario sel = tableUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un usuario."); return; }
        if ("admin@openlib.com".equals(sel.getEmail())) { mostrarAlerta("No puedes modificar al admin."); return; }
        Task<Void> task = new Task<>() {
            @Override protected Void call() { usuariosUC.activar(sel.getEmail()); return null; }
        };
        task.setOnSucceeded(e -> cargarUsuarios());
        new Thread(task).start();
    }

    @FXML
    public void desactivarUsuario() {
        Usuario sel = tableUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un usuario."); return; }
        if ("admin@openlib.com".equals(sel.getEmail())) { mostrarAlerta("No puedes bloquear al admin."); return; }
        Task<Void> task = new Task<>() {
            @Override protected Void call() { usuariosUC.desactivar(sel.getEmail()); return null; }
        };
        task.setOnSucceeded(e -> cargarUsuarios());
        new Thread(task).start();
    }

    @FXML
    public void eliminarUsuario() {
        Usuario sel = tableUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un usuario."); return; }
        if ("admin@openlib.com".equals(sel.getEmail())) { mostrarAlerta("No puedes eliminar al admin."); return; }
        if (!confirmar("¿Eliminar usuario " + sel.getNombre() + "?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() { usuariosUC.eliminar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarUsuarios());
        new Thread(task).start();
    }

    // ──────────────────────────────────────────────
    // LIBROS
    // ──────────────────────────────────────────────
    private void configurarTablaLibros() {
        colLibId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLibTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colLibAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colLibCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colLibPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colLibStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colLibEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tableLibros.setItems(librosObs);

        if (cmbFiltroEstado != null) {
            cmbFiltroEstado.setItems(FXCollections.observableArrayList("TODOS", "APROBADO", "PENDIENTE", "RECHAZADO"));
            cmbFiltroEstado.getSelectionModel().selectFirst();
            cmbFiltroEstado.setOnAction(e -> filtrarLibros());
        }
    }

    private void cargarLibros() {
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() { return librosUC.listarTodos(); }
        };
        task.setOnSucceeded(e -> { librosObs.setAll(task.getValue()); filtrarLibros(); });
        new Thread(task).start();
    }

    private void filtrarLibros() {
        if (cmbFiltroEstado == null) return;
        String filtro = cmbFiltroEstado.getValue();
        if (filtro == null || "TODOS".equals(filtro)) return;
        librosObs.removeIf(l -> !filtro.equals(l.getEstado()));
    }

    @FXML
    public void aprobarLibro() {
        Libro sel = tableLibros.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un libro."); return; }
        Task<Void> task = new Task<>() {
            @Override protected Void call() { librosUC.aprobar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarLibros());
        new Thread(task).start();
    }

    @FXML
    public void rechazarLibro() {
        Libro sel = tableLibros.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un libro."); return; }
        Task<Void> task = new Task<>() {
            @Override protected Void call() { librosUC.rechazar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarLibros());
        new Thread(task).start();
    }

    @FXML
    public void eliminarLibro() {
        Libro sel = tableLibros.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un libro."); return; }
        if (!confirmar("¿Eliminar el libro \"" + sel.getTitulo() + "\"?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() { librosUC.eliminar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarLibros());
        new Thread(task).start();
    }

    // ──────────────────────────────────────────────
    // CATEGORÍAS
    // ──────────────────────────────────────────────
    private void configurarTablaCategorias() {
        colCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCatTotal.setCellValueFactory(new PropertyValueFactory<>("totalLibros"));
        tableCategorias.setItems(categoriasObs);
    }

    private void cargarCategorias() {
        Task<List<Categoria>> task = new Task<>() {
            @Override protected List<Categoria> call() { return categoriasUC.listarTodas(); }
        };
        task.setOnSucceeded(e -> categoriasObs.setAll(task.getValue()));
        new Thread(task).start();
    }

    @FXML
    public void guardarCategoria() {
        String nombre = txtNombreCategoria != null ? txtNombreCategoria.getText().trim() : "";
        String desc   = txtDescCategoria   != null ? txtDescCategoria.getText().trim()   : "";
        if (nombre.isEmpty()) { mostrarAlerta("El nombre es obligatorio."); return; }

        Categoria sel = tableCategorias.getSelectionModel().getSelectedItem();
        Task<Void> task;
        if (sel != null && sel.getNombre().equalsIgnoreCase(nombre)) {
            // Edición
            sel.setNombre(nombre);
            sel.setDescripcion(desc);
            task = new Task<>() {
                @Override protected Void call() { categoriasUC.actualizar(sel); return null; }
            };
        } else {
            // Nueva
            task = new Task<>() {
                @Override protected Void call() { categoriasUC.crear(nombre, desc); return null; }
            };
        }
        task.setOnSucceeded(e -> {
            cargarCategorias();
            if (txtNombreCategoria != null) txtNombreCategoria.clear();
            if (txtDescCategoria   != null) txtDescCategoria.clear();
        });
        task.setOnFailed(e -> mostrarAlerta("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    public void eliminarCategoria() {
        Categoria sel = tableCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona una categoría."); return; }
        if (!confirmar("¿Eliminar la categoría \"" + sel.getNombre() + "\"?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() { categoriasUC.eliminar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarCategorias());
        new Thread(task).start();
    }

    @FXML
    public void seleccionarCategoria() {
        Categoria sel = tableCategorias.getSelectionModel().getSelectedItem();
        if (sel != null) {
            if (txtNombreCategoria != null) txtNombreCategoria.setText(sel.getNombre());
            if (txtDescCategoria   != null) txtDescCategoria.setText(sel.getDescripcion());
        }
    }

    // ──────────────────────────────────────────────
    // ETIQUETAS
    // ──────────────────────────────────────────────
    private void configurarTablaEtiquetas() {
        colEtqId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEtqNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEtqTotal.setCellValueFactory(new PropertyValueFactory<>("totalLibros"));
        tableEtiquetas.setItems(etiquetasObs);
    }

    private void cargarEtiquetas() {
        Task<List<Etiqueta>> task = new Task<>() {
            @Override protected List<Etiqueta> call() { return etiquetasUC.listarTodas(); }
        };
        task.setOnSucceeded(e -> etiquetasObs.setAll(task.getValue()));
        new Thread(task).start();
    }

    @FXML
    public void guardarEtiqueta() {
        String nombre = txtNombreEtiqueta != null ? txtNombreEtiqueta.getText().trim() : "";
        if (nombre.isEmpty()) { mostrarAlerta("El nombre es obligatorio."); return; }

        Etiqueta sel = tableEtiquetas.getSelectionModel().getSelectedItem();
        Task<Void> task;
        if (sel != null && sel.getNombre().equalsIgnoreCase(nombre)) {
            sel.setNombre(nombre);
            task = new Task<>() {
                @Override protected Void call() { etiquetasUC.actualizar(sel); return null; }
            };
        } else {
            task = new Task<>() {
                @Override protected Void call() { etiquetasUC.crear(nombre); return null; }
            };
        }
        task.setOnSucceeded(e -> {
            cargarEtiquetas();
            if (txtNombreEtiqueta != null) txtNombreEtiqueta.clear();
        });
        task.setOnFailed(e -> mostrarAlerta("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    public void eliminarEtiqueta() {
        Etiqueta sel = tableEtiquetas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona una etiqueta."); return; }
        if (!confirmar("¿Eliminar la etiqueta \"" + sel.getNombre() + "\"?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() { etiquetasUC.eliminar(sel.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarEtiquetas());
        new Thread(task).start();
    }

    @FXML
    public void seleccionarEtiqueta() {
        Etiqueta sel = tableEtiquetas.getSelectionModel().getSelectedItem();
        if (sel != null && txtNombreEtiqueta != null) txtNombreEtiqueta.setText(sel.getNombre());
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────
    private void mostrarAlerta(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private boolean confirmar(String msg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText(msg);
        return confirm.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
}

package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.application.usecase.CambiarContrasenaUseCase;
import co.edu.javeriana.proyecto.application.usecase.LoginUsuarioUseCase;
import co.edu.javeriana.proyecto.application.usecase.RegistrarUsuarioUseCase;
import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.proyecto.domain.exception.UsuarioBloqueadoException;
import co.edu.javeriana.proyecto.domain.exception.UsuarioYaExisteException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RegistroController {

    // ── Pestaña Login ────────────────────────────────────────────────────────
    @FXML private TextField txtEmailLogin;
    @FXML private PasswordField txtPasswordLogin;
    @FXML private Label lblEstadoRegistro;
    @FXML private Label lblMensajeLogin;
    @FXML private Button btnLogin;
    @FXML private ProgressIndicator progressLogin;
    @FXML private Hyperlink linkCambiarPassword;
    @FXML private TabPane tabPane;
    @FXML private Tab tabLogin;
    @FXML private Tab tabRegistro;

    // ── Pestaña Registro ─────────────────────────────────────────────────────
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblMensajeGlobal;
    @FXML private Button btnRegistrar;
    @FXML private ProgressIndicator progressIndicator;

    // ── Casos de uso ─────────────────────────────────────────────────────────
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUsuarioUseCase loginUsuarioUseCase;
    private final CambiarContrasenaUseCase cambiarContrasenaUseCase;
    private final UsuarioGateway usuarioGateway;
    private final BibliotecaController bibliotecaController;

    public RegistroController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                              LoginUsuarioUseCase loginUsuarioUseCase,
                              CambiarContrasenaUseCase cambiarContrasenaUseCase,
                              UsuarioGateway usuarioGateway,
                              BibliotecaController bibliotecaController) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUsuarioUseCase = loginUsuarioUseCase;
        this.cambiarContrasenaUseCase = cambiarContrasenaUseCase;
        this.usuarioGateway = usuarioGateway;
        this.bibliotecaController = bibliotecaController;
    }

    public RegistroController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                              LoginUsuarioUseCase loginUsuarioUseCase,
                              CambiarContrasenaUseCase cambiarContrasenaUseCase,
                              UsuarioGateway usuarioGateway) {
        this(registrarUsuarioUseCase, loginUsuarioUseCase, cambiarContrasenaUseCase, usuarioGateway, null);
    }

    @FXML
    public void initialize() {
        configurarTabLogin();
        configurarTabRegistro();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TAB LOGIN
    // ════════════════════════════════════════════════════════════════════════

    private void configurarTabLogin() {
        // Verificar en tiempo real si el email existe en el sistema
        txtEmailLogin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty() || !newVal.contains("@")) {
                lblEstadoRegistro.setVisible(false);
                lblEstadoRegistro.setManaged(false);
                return;
            }

            String emailTrimmed = newVal.trim();
            Task<Boolean> checkTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return usuarioGateway.existeEmail(emailTrimmed);
                }
            };

            checkTask.setOnSucceeded(e -> {
                // Solo actualizar si el texto sigue siendo el mismo
                if (!txtEmailLogin.getText().trim().equals(emailTrimmed)) return;
                boolean existe = checkTask.getValue();
                if (existe) {
                    lblEstadoRegistro.setText("✅ Usuario registrado");
                    lblEstadoRegistro.setTextFill(Color.web("#27ae60"));
                } else {
                    lblEstadoRegistro.setText("❌ Email no encontrado en el sistema");
                    lblEstadoRegistro.setTextFill(Color.web("#e74c3c"));
                }
                lblEstadoRegistro.setVisible(true);
                lblEstadoRegistro.setManaged(true);
            });

            new Thread(checkTask).start();
        });

        // Login con Enter en el campo de contraseña
        txtPasswordLogin.setOnAction(e -> iniciarSesion());
        btnLogin.setOnAction(e -> iniciarSesion());

        // Abrir modal de cambio de contraseña
        linkCambiarPassword.setOnAction(e -> abrirCambiarPassword());
    }

    private void iniciarSesion() {
        String email = txtEmailLogin.getText().trim();
        String password = txtPasswordLogin.getText();

        if (email.isEmpty() || !email.contains("@")) {
            mostrarMensajeLogin("Por favor ingresa un email válido.", Color.web("#e74c3c"));
            return;
        }
        if (password.isEmpty()) {
            mostrarMensajeLogin("Por favor ingresa tu contraseña.", Color.web("#e74c3c"));
            return;
        }

        bloquearLogin(true);
        mostrarMensajeLogin("Verificando credenciales...", Color.web("#3498db"));

        Task<Usuario> loginTask = new Task<>() {
            @Override
            protected Usuario call() throws Exception {
                return loginUsuarioUseCase.ejecutar(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            bloquearLogin(false);
            Usuario usuario = loginTask.getValue();
            if (bibliotecaController != null) {
                bibliotecaController.setUsuario(usuario);
            }
            mostrarMensajeLogin("✅ ¡Bienvenido, " + usuario.getNombre() + "! Sesión iniciada.", Color.web("#27ae60"));

            // Cerrar modal después de un momento
            new Thread(() -> {
                try {
                    Thread.sleep(1800);
                    Platform.runLater(() -> {
                        if (bibliotecaController != null) {
                            bibliotecaController.cerrarOverlay();
                        }
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        loginTask.setOnFailed(e -> {
            bloquearLogin(false);
            Throwable exception = loginTask.getException();
            if (exception instanceof UsuarioBloqueadoException) {
                mostrarMensajeLogin("🔒 " + exception.getMessage(), Color.web("#c0392b"));
                // Hacer el link de cambiar contraseña más visible
                linkCambiarPassword.setStyle(
                    "-fx-text-fill: #e67e22; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-border-color: transparent; -fx-underline: true;"
                );
            } else if (exception instanceof CredencialesInvalidasException) {
                mostrarMensajeLogin("⚠️ " + exception.getMessage(), Color.web("#e74c3c"));
            } else {
                mostrarMensajeLogin("Error al iniciar sesión. Intenta de nuevo.", Color.web("#e74c3c"));
                exception.printStackTrace();
            }
        });

        new Thread(loginTask).start();
    }

    private void abrirCambiarPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CambiarPasswordView.fxml"));
            CambiarPasswordController controller = new CambiarPasswordController(
                cambiarContrasenaUseCase, null, bibliotecaController
            );
            loader.setController(controller);
            Parent root = loader.load();

            if (bibliotecaController != null) {
                bibliotecaController.mostrarOverlay(root);
            }

            // Después de cambiar contraseña, limpiar campos
            txtPasswordLogin.clear();
            limpiarMensajeLogin();
        } catch (Exception ex) {
            mostrarMensajeLogin("Error al abrir cambio de contraseña.", Color.web("#e74c3c"));
            ex.printStackTrace();
        }
    }

    private void mostrarMensajeLogin(String msg, Color color) {
        lblMensajeLogin.setText(msg);
        lblMensajeLogin.setTextFill(color);
        lblMensajeLogin.setVisible(true);
        lblMensajeLogin.setManaged(true);
    }

    private void limpiarMensajeLogin() {
        lblMensajeLogin.setVisible(false);
        lblMensajeLogin.setManaged(false);
    }

    private void bloquearLogin(boolean bloqueado) {
        txtEmailLogin.setDisable(bloqueado);
        txtPasswordLogin.setDisable(bloqueado);
        btnLogin.setVisible(!bloqueado);
        btnLogin.setManaged(!bloqueado);
        progressLogin.setVisible(bloqueado);
        progressLogin.setManaged(bloqueado);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TAB REGISTRO
    // ════════════════════════════════════════════════════════════════════════

    private void configurarTabRegistro() {
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !newVal.contains("@")) {
                lblEmailError.setVisible(true);
                lblEmailError.setManaged(true);
                txtEmail.setStyle("-fx-border-color: #e74c3c; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            } else {
                lblEmailError.setVisible(false);
                lblEmailError.setManaged(false);
                txtEmail.setStyle("-fx-border-color: #2ecc71; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            }
        });

        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                lblPasswordError.setVisible(true);
                lblPasswordError.setManaged(true);
                txtPassword.setStyle("-fx-border-color: #e74c3c; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            } else {
                lblPasswordError.setVisible(false);
                lblPasswordError.setManaged(false);
                txtPassword.setStyle("-fx-border-color: #2ecc71; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            }
        });

        btnRegistrar.setOnAction(e -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (nombre.isEmpty() || email.isEmpty() || password.length() < 6 || !email.contains("@")) {
            mostrarMensajeGlobal("Por favor, corrige los errores antes de continuar.", Color.web("#e74c3c"));
            return;
        }

        bloquearRegistro(true);
        mostrarMensajeGlobal("Registrando...", Color.web("#3498db"));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                registrarUsuarioUseCase.ejecutar(email, password, nombre);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            bloquearRegistro(false);
            mostrarMensajeGlobal("¡Registro exitoso! Ya puedes iniciar sesión.", Color.web("#2ecc71"));
            // Ir a la pestaña de login y prellenar el email
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        txtEmailLogin.setText(txtEmail.getText().trim());
                        tabPane.getSelectionModel().select(tabLogin);
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        task.setOnFailed(e -> {
            bloquearRegistro(false);
            Throwable exception = task.getException();
            if (exception instanceof UsuarioYaExisteException) {
                mostrarMensajeGlobal(exception.getMessage(), Color.web("#e74c3c"));
            } else if (exception instanceof IllegalArgumentException) {
                mostrarMensajeGlobal(exception.getMessage(), Color.web("#e74c3c"));
            } else {
                mostrarMensajeGlobal("Error interno al registrar.", Color.web("#e74c3c"));
                exception.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    private void mostrarMensajeGlobal(String msg, Color color) {
        lblMensajeGlobal.setText(msg);
        lblMensajeGlobal.setTextFill(color);
        lblMensajeGlobal.setVisible(true);
        lblMensajeGlobal.setManaged(true);
    }

    private void bloquearRegistro(boolean bloqueado) {
        txtNombre.setDisable(bloqueado);
        txtEmail.setDisable(bloqueado);
        txtPassword.setDisable(bloqueado);
        btnRegistrar.setVisible(!bloqueado);
        btnRegistrar.setManaged(!bloqueado);
        progressIndicator.setVisible(bloqueado);
        progressIndicator.setManaged(bloqueado);
    }
}

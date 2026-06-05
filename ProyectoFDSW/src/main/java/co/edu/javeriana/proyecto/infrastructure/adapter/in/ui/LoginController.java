package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.CambiarContrasenaUseCase;
import co.edu.javeriana.proyecto.application.usecase.LoginUsuarioUseCase;
import co.edu.javeriana.proyecto.application.usecase.RegistrarUsuarioUseCase;
import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.proyecto.domain.exception.UsuarioBloqueadoException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblEstadoRegistro;
    @FXML private Label lblMensaje;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistrarse;
    @FXML private Hyperlink linkCambiarPassword;
    @FXML private ProgressIndicator progressIndicator;

    private final LoginUsuarioUseCase loginUsuarioUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final CambiarContrasenaUseCase cambiarContrasenaUseCase;
    private final UsuarioGateway usuarioGateway;

    // Dependencias para construir BibliotecaController después del login exitoso
    private final BibliotecaController bibliotecaController;
    private final String bibliotecaFxmlPath;

    public LoginController(LoginUsuarioUseCase loginUsuarioUseCase,
                           RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           CambiarContrasenaUseCase cambiarContrasenaUseCase,
                           UsuarioGateway usuarioGateway,
                           BibliotecaController bibliotecaController,
                           String bibliotecaFxmlPath) {
        this.loginUsuarioUseCase = loginUsuarioUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.cambiarContrasenaUseCase = cambiarContrasenaUseCase;
        this.usuarioGateway = usuarioGateway;
        this.bibliotecaController = bibliotecaController;
        this.bibliotecaFxmlPath = bibliotecaFxmlPath;
    }

    @FXML
    public void initialize() {
        // Detector en tiempo real del email: verifica si ya está registrado
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                lblEstadoRegistro.setVisible(false);
                lblEstadoRegistro.setManaged(false);
                return;
            }

            if (!newVal.contains("@")) {
                lblEstadoRegistro.setVisible(false);
                lblEstadoRegistro.setManaged(false);
                return;
            }

            // Verificar en BD de forma asíncrona para no bloquear la UI
            String emailIngresado = newVal.trim();
            Task<Boolean> verificarTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return usuarioGateway.existeEmail(emailIngresado);
                }
            };

            verificarTask.setOnSucceeded(e -> {
                boolean existe = verificarTask.getValue();
                // Solo actualizamos si el email sigue siendo el mismo
                if (txtEmail.getText().trim().equals(emailIngresado)) {
                    if (existe) {
                        lblEstadoRegistro.setText("✅ Usuario registrado");
                        lblEstadoRegistro.setTextFill(Color.web("#86efac"));
                    } else {
                        lblEstadoRegistro.setText("❌ Email no registrado");
                        lblEstadoRegistro.setTextFill(Color.web("#fca5a5"));
                    }
                    lblEstadoRegistro.setVisible(true);
                    lblEstadoRegistro.setManaged(true);
                }
            });

            verificarTask.setOnFailed(e -> {
                lblEstadoRegistro.setVisible(false);
                lblEstadoRegistro.setManaged(false);
            });

            new Thread(verificarTask).start();
        });

        // Acción del botón login
        btnLogin.setOnAction(e -> iniciarSesion());

        // Enter en el campo de contraseña también hace login
        txtPassword.setOnAction(e -> iniciarSesion());

        // Link para registrarse (abre el modal de registro)
        linkRegistrarse.setOnAction(e -> abrirRegistro());

        // Link para cambiar contraseña
        linkCambiarPassword.setOnAction(e -> abrirCambiarPassword());
    }

    private void iniciarSesion() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || !email.contains("@")) {
            mostrarMensaje("Por favor ingresa un email válido.", Color.web("#fca5a5"));
            return;
        }
        if (password.isEmpty()) {
            mostrarMensaje("Por favor ingresa tu contraseña.", Color.web("#fca5a5"));
            return;
        }

        bloquearUI(true);
        mostrarMensaje("Verificando credenciales...", Color.web("#93c5fd"));

        Task<Usuario> loginTask = new Task<>() {
            @Override
            protected Usuario call() throws Exception {
                return loginUsuarioUseCase.ejecutar(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            bloquearUI(false);
            Usuario usuario = loginTask.getValue();
            mostrarMensaje("✅ ¡Bienvenido, " + usuario.getNombre() + "!", Color.web("#86efac"));

            // Navegar a la pantalla principal después de breve pausa
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    Platform.runLater(() -> abrirBiblioteca(usuario));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        loginTask.setOnFailed(e -> {
            bloquearUI(false);
            Throwable exception = loginTask.getException();
            if (exception instanceof UsuarioBloqueadoException) {
                mostrarMensaje("🔒 " + exception.getMessage(), Color.web("#f87171"));
                // Resaltar el link de cambiar contraseña visualmente
                linkCambiarPassword.setStyle(
                    "-fx-text-fill: #f59e0b; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-border-color: transparent; -fx-font-family: 'Segoe UI'; " +
                    "-fx-effect: dropshadow(gaussian, #f59e0b, 8, 0, 0, 0);"
                );
            } else if (exception instanceof CredencialesInvalidasException) {
                mostrarMensaje("⚠️ " + exception.getMessage(), Color.web("#fca5a5"));
            } else {
                mostrarMensaje("Error al iniciar sesión. Intenta de nuevo.", Color.web("#fca5a5"));
                exception.printStackTrace();
            }
        });

        new Thread(loginTask).start();
    }

    private void abrirRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroView.fxml"));
            RegistroController registroController = new RegistroController(registrarUsuarioUseCase, loginUsuarioUseCase, cambiarContrasenaUseCase, usuarioGateway);
            loader.setController(registroController);
            Parent root = loader.load();

            Stage registroStage = new Stage();
            registroStage.initModality(Modality.APPLICATION_MODAL);
            registroStage.setTitle("OpenLib - Crear Cuenta");
            Scene scene = new Scene(root, 450, 500);
            registroStage.setScene(scene);
            registroStage.setResizable(false);
            registroStage.showAndWait();

            // Después de cerrar el registro, limpiar el campo email para que el usuario escriba su email
            txtEmail.clear();
            txtPassword.clear();
            lblEstadoRegistro.setVisible(false);
            lblEstadoRegistro.setManaged(false);
        } catch (Exception ex) {
            mostrarMensaje("Error al abrir el formulario de registro.", Color.web("#fca5a5"));
            ex.printStackTrace();
        }
    }

    private void abrirCambiarPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CambiarPasswordView.fxml"));
            CambiarPasswordController controller = new CambiarPasswordController(cambiarContrasenaUseCase, this, null);
            loader.setController(controller);
            Parent root = loader.load();

            Stage cambioStage = new Stage();
            cambioStage.initModality(Modality.APPLICATION_MODAL);
            cambioStage.setTitle("OpenLib - Cambiar Contraseña");
            Scene scene = new Scene(root, 450, 500);
            cambioStage.setScene(scene);
            cambioStage.setResizable(false);
            cambioStage.showAndWait();
        } catch (Exception ex) {
            mostrarMensaje("Error al abrir cambio de contraseña.", Color.web("#fca5a5"));
            ex.printStackTrace();
        }
    }

    private void abrirBiblioteca(Usuario usuarioLogueado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(bibliotecaFxmlPath));
            bibliotecaController.setUsuario(usuarioLogueado);
            loader.setController(bibliotecaController);
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 680);
            stage.setTitle("OpenLib Market - " + usuarioLogueado.getNombre());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.setMaximized(false);
        } catch (Exception ex) {
            mostrarMensaje("Error al cargar la pantalla principal.", Color.web("#fca5a5"));
            ex.printStackTrace();
        }
    }

    /** Permite que CambiarPasswordController ponga el email de vuelta en el campo */
    public void prellenarEmail(String email) {
        if (email != null && !email.isEmpty()) {
            txtEmail.setText(email);
        }
        txtPassword.clear();
        limpiarMensaje();
    }

    private void limpiarMensaje() {
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    private void mostrarMensaje(String mensaje, Color color) {
        lblMensaje.setText(mensaje);
        lblMensaje.setTextFill(color);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void bloquearUI(boolean bloqueado) {
        txtEmail.setDisable(bloqueado);
        txtPassword.setDisable(bloqueado);
        btnLogin.setVisible(!bloqueado);
        btnLogin.setManaged(!bloqueado);
        progressIndicator.setVisible(bloqueado);
        progressIndicator.setManaged(bloqueado);
        linkRegistrarse.setDisable(bloqueado);
        linkCambiarPassword.setDisable(bloqueado);
    }
}

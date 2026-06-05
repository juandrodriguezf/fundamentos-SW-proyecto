package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.CambiarContrasenaUseCase;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CambiarPasswordController {

    @FXML private TextField txtEmailCambio;
    @FXML private PasswordField txtNuevoPassword;
    @FXML private Label lblPasswordError;
    @FXML private Label lblMensajeCambio;
    @FXML private Button btnCambiarPassword;
    @FXML private Button btnVolver;
    @FXML private ProgressIndicator progressIndicatorCambio;

    private final CambiarContrasenaUseCase cambiarContrasenaUseCase;
    private final LoginController loginController;
    private final BibliotecaController bibliotecaController;

    public CambiarPasswordController(CambiarContrasenaUseCase cambiarContrasenaUseCase,
                                     LoginController loginController,
                                     BibliotecaController bibliotecaController) {
        this.cambiarContrasenaUseCase = cambiarContrasenaUseCase;
        this.loginController = loginController;
        this.bibliotecaController = bibliotecaController;
    }

    @FXML
    public void initialize() {
        // Validación en tiempo real de la nueva contraseña
        txtNuevoPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                lblPasswordError.setVisible(true);
                lblPasswordError.setManaged(true);
                txtNuevoPassword.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                        "-fx-padding: 12px 15px; -fx-font-size: 14px; -fx-background-radius: 10px; " +
                        "-fx-border-color: #ef4444; -fx-border-radius: 10px; -fx-prompt-text-fill: rgba(255,255,255,0.4);");
            } else {
                lblPasswordError.setVisible(false);
                lblPasswordError.setManaged(false);
                txtNuevoPassword.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                        "-fx-padding: 12px 15px; -fx-font-size: 14px; -fx-background-radius: 10px; " +
                        "-fx-border-color: #22c55e; -fx-border-radius: 10px; -fx-prompt-text-fill: rgba(255,255,255,0.4);");
            }
        });

        btnCambiarPassword.setOnAction(e -> cambiarPassword());
        btnVolver.setOnAction(e -> volverAlLogin());
    }

    private void cambiarPassword() {
        String email = txtEmailCambio.getText().trim();
        String nuevaPassword = txtNuevoPassword.getText();

        if (email.isEmpty() || !email.contains("@")) {
            mostrarMensaje("Por favor ingresa un email válido.", Color.web("#fca5a5"));
            return;
        }
        if (nuevaPassword.length() < 6) {
            mostrarMensaje("La contraseña debe tener al menos 6 caracteres.", Color.web("#fca5a5"));
            return;
        }

        bloquearUI(true);
        mostrarMensaje("Actualizando contraseña...", Color.web("#93c5fd"));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                cambiarContrasenaUseCase.ejecutar(email, nuevaPassword);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            bloquearUI(false);
            mostrarMensaje("✅ ¡Contraseña actualizada! Tu cuenta ha sido reactivada. Redirigiendo al login...",
                    Color.web("#86efac"));

            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                    Platform.runLater(this::volverAlLogin);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        task.setOnFailed(e -> {
            bloquearUI(false);
            Throwable exception = task.getException();
            if (exception instanceof CredencialesInvalidasException || exception instanceof IllegalArgumentException) {
                mostrarMensaje(exception.getMessage(), Color.web("#fca5a5"));
            } else {
                mostrarMensaje("Error al actualizar la contraseña. Intenta de nuevo.", Color.web("#fca5a5"));
                exception.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    private void volverAlLogin() {
        if (loginController != null) {
            loginController.prellenarEmail(txtEmailCambio.getText().trim());
        }
        if (bibliotecaController != null) {
            bibliotecaController.cerrarOverlay();
        }
    }

    private void mostrarMensaje(String mensaje, Color color) {
        lblMensajeCambio.setText(mensaje);
        lblMensajeCambio.setTextFill(color);
        lblMensajeCambio.setVisible(true);
        lblMensajeCambio.setManaged(true);
    }

    private void bloquearUI(boolean bloqueado) {
        txtEmailCambio.setDisable(bloqueado);
        txtNuevoPassword.setDisable(bloqueado);
        btnCambiarPassword.setDisable(bloqueado);
        btnCambiarPassword.setVisible(!bloqueado);
        btnCambiarPassword.setManaged(!bloqueado);
        progressIndicatorCambio.setVisible(bloqueado);
        progressIndicatorCambio.setManaged(bloqueado);
    }
}

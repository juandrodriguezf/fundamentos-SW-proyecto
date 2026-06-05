package co.edu.javeriana.proyecto.application.command;

import co.edu.javeriana.proyecto.application.usecase.AgregarAlCarritoUseCase;

public class AgregarAlCarritoCommand implements Command {
    private final AgregarAlCarritoUseCase useCase;
    private final String sessionId;
    private final Long libroId;
    private final int cantidad;

    public AgregarAlCarritoCommand(AgregarAlCarritoUseCase useCase, String sessionId, Long libroId, int cantidad) {
        this.useCase = useCase;
        this.sessionId = sessionId;
        this.libroId = libroId;
        this.cantidad = cantidad;
    }

    @Override
    public void execute() {
        useCase.ejecutar(sessionId, libroId, cantidad);
    }
}

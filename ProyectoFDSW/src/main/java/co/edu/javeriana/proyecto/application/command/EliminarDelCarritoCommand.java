package co.edu.javeriana.proyecto.application.command;

import co.edu.javeriana.proyecto.application.usecase.EliminarDelCarritoUseCase;

public class EliminarDelCarritoCommand implements Command {
    private final EliminarDelCarritoUseCase useCase;
    private final String sessionId;
    private final Long libroId;

    public EliminarDelCarritoCommand(EliminarDelCarritoUseCase useCase, String sessionId, Long libroId) {
        this.useCase = useCase;
        this.sessionId = sessionId;
        this.libroId = libroId;
    }

    @Override
    public void execute() {
        useCase.ejecutar(sessionId, libroId);
    }
}

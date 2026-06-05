package co.edu.javeriana.proyecto.application.command;

import java.util.Stack;

public class CommandInvoker {
    private final Stack<Command> history = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }
}

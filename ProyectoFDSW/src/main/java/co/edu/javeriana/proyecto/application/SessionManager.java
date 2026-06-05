package co.edu.javeriana.proyecto.application;

import co.edu.javeriana.proyecto.domain.Usuario;

public class SessionManager {
    private static SessionManager instance;
    private Usuario currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.currentUser = usuario;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }
}

package co.edu.javeriana.proyecto.domain;

public class Usuario {
    private Long id;
    private String email;
    private String passwordHash;
    private String nombre;
    private boolean activo;
    private int intentosFallidos;

    public Usuario() {}

    public Usuario(Long id, String email, String passwordHash, String nombre, boolean activo) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.activo = activo;
        this.intentosFallidos = 0;
    }

    public Usuario(Long id, String email, String passwordHash, String nombre, boolean activo, int intentosFallidos) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // --- Getters de solo lectura ---
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }

    // --- Métodos de Dominio Enriquecido ---

    public boolean puedeIntentarLogin() {
        return activo;
    }

    public void registrarIntentoFallido() {
        if (!activo) return;
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) {
            bloquear();
        }
    }

    public void resetearIntentos() {
        this.intentosFallidos = 0;
    }

    public void bloquear() {
        this.activo = false;
    }

    public void desbloquear() {
        this.activo = true;
        this.intentosFallidos = 0;
    }
}

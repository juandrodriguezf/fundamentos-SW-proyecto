package co.edu.javeriana.proyecto.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Orden {
    private String id;
    private Long usuarioId;
    private LocalDateTime fecha;
    private String estado;
    private List<Compra> items;

    public Orden(String id, Long usuarioId, LocalDateTime fecha, String estado, List<Compra> items) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.estado = estado;
        this.items = items;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Compra> getItems() { return items; }
    public void setItems(List<Compra> items) { this.items = items; }

    public double getTotal() {
        if (items == null || items.isEmpty()) return 0.0;
        double subtotal = items.stream().mapToDouble(Compra::getSubtotal).sum();
        double impuestos = subtotal * 0.19;
        double envio = 5.00;
        return subtotal + impuestos + envio;
    }
}

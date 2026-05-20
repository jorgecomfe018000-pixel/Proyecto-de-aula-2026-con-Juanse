package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String idNotificacion;

    @DBRef(lazy = true)
    private Estilista estilista;

    private TipoNotificacion tipo;

    private String titulo;
    private String mensaje;
    private Boolean leida = false;
    
    private LocalDateTime fechaCreacion;

    @DBRef(lazy = true)
    private Reserva reserva;

    public enum TipoNotificacion {
        NUEVA_RESERVA,
        RESERVA_CANCELADA,
        AUSENCIA_APROBADA,
        AUSENCIA_RECHAZADA,
        RECORDATORIO,
        VALORACION,
        SISTEMA
    }

    // Getters y Setters
    public String getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(String idNotificacion) { this.idNotificacion = idNotificacion; }

    public Estilista getEstilista() { return estilista; }
    public void setEstilista(Estilista estilista) { this.estilista = estilista; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public void setCliente(Cliente cliente) {
        // Assuming Cliente is a related entity, add logic to set it here
    }

    public void setFecha(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
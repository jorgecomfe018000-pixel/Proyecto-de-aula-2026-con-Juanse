package com.pa.spring.prueba1.pa_prueba1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Data;

import java.time.LocalDateTime;

@Document(collection = "reservas")
@Data
public class Reserva {

    @Id
    private String idReserva;

    private LocalDateTime fechaHoraReserva;
    private LocalDateTime fechaHoraTurno;

    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @DBRef(lazy = true)
    @JsonIgnore
    private Cliente cliente;

    @DBRef(lazy = true)
    @JsonIgnore
    private Estilista estilista;

    @DBRef(lazy = true)
    private ServicioBelleza servicioBelleza;

    @DBRef(lazy = true)
    @JsonBackReference
    private Turno turno;

    private String comentarios;

    public void setFecha(LocalDateTime fechaHoraReserva) {
        this.fechaHoraReserva = fechaHoraReserva;
    }

    public enum EstadoReserva {
        PENDIENTE,
        COMPLETADA,
        CANCELADA
    }
}
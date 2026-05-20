package com.pa.spring.prueba1.pa_prueba1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "turnos")
@Getter @Setter
@EqualsAndHashCode(of = "idTurno")
@ToString(of = {"idTurno", "fechaHora", "estado"})
public class Turno {

    @Id
    private String idTurno;

    private LocalDateTime fechaHora;

    private EstadoTurno estado = EstadoTurno.DISPONIBLE;

    @DBRef(lazy = true)
    @JsonIgnore
    private Estilista estilista;

    @DBRef(lazy = true)
    @JsonManagedReference
    private List<Reserva> reservas = new ArrayList<>();

    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setTurno(this);
    }

    public void setFecha(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setHora(String hora) {
        // Assuming hora is a string representation of time, convert and set it here
    }

    public void setDisponible(boolean disponible) {
        this.estado = disponible ? EstadoTurno.DISPONIBLE : EstadoTurno.NO_DISPONIBLE;
    }

    public enum EstadoTurno {
        DISPONIBLE,
        NO_DISPONIBLE
    }
}
package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "solicitudes_ausencia")
@Getter @Setter
public class SolicitudAusencia {

    @Id
    private String idSolicitud;

    @DBRef
    private Estilista estilista;

    private TipoAusencia tipoAusencia = TipoAusencia.DIA_COMPLETO;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    private String motivo;
    
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    private LocalDateTime fechaRespuesta;
    
    private String motivoRechazo;

    public enum TipoAusencia {
        DIA_COMPLETO,
        HORAS_ESPECIFICAS
    }

    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    public void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSolicitud.PENDIENTE;
        }
        if (tipoAusencia == null) {
            tipoAusencia = TipoAusencia.DIA_COMPLETO;
        }
    }
}

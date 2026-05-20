package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "peticiones")
@Data
public class Peticion {
    
    @Id
    private String idPeticion;
    
    @DBRef
    private Estilista estilista;
    
    private TipoPeticion tipo;
    
    private LocalDate fecha;
    
    private LocalTime horaInicio;
    
    private LocalTime horaFin;
    
    private String motivo;
    
    private EstadoPeticion estado = EstadoPeticion.PENDIENTE;
    
    private String respuestaAdmin;
    
    private LocalDate fechaCreacion = LocalDate.now();
    
    private LocalDate fechaRespuesta;
    
    public enum TipoPeticion {
        AUSENCIA,
        CAMBIO_HORARIO
    }
    
    public enum EstadoPeticion {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }
}

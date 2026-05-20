package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;

@Document(collection = "servicios_belleza")
@Data
public class ServicioBelleza {

    @Id
    private String id;

    @Indexed
    private String nombre; // Ej: Corte Dama, Uñas Acrílicas, Keratina

    private String categoria; 
    // Ej: Cabello, Uñas, Maquillaje, Tratamiento

    private double precio;

    private int duracion; // en minutos

    private String descripcion;

    private boolean activo = true;
}
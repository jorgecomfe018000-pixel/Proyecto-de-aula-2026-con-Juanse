package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "rate_limits")
public class RateLimit {
    @Id
    private String id; // identifier (correo/IP)

    private String clave;

    private int contador;

    @Indexed(expireAfterSeconds = 3600)
    private Date windowStart = new Date(); // TTL 1 hora

    public RateLimit() {}

    public RateLimit(String clave, int contador) {
        this.clave = clave;
        this.contador = contador;
        this.windowStart = new Date();
    }

    public String getId() { return id; }
    public String getClave() { return clave; }
    public int getContador() { return contador; }
    public void setContador(int contador) { this.contador = contador; }
}

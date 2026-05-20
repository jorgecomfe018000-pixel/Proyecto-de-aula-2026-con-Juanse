package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "codigos_verificacion")
public class CodigoVerificacion {

    @Id
    private String id;

    private String identificador; // correo o telefono

    private String codigo;

    private String tipoRecuperacion; // "correo" o "telefono"

    private int intentos = 0;

    @Indexed(expireAfterSeconds = 900)
    private Date creadoEn = new Date(); // TTL de 15 minutos

    public CodigoVerificacion() {}

    public CodigoVerificacion(String identificador, String codigo, String tipoRecuperacion) {
        this.identificador = identificador;
        this.codigo = codigo;
        this.tipoRecuperacion = tipoRecuperacion;
        this.creadoEn = new Date();
        this.intentos = 0;
    }

    public String getId() { return id; }
    public String getIdentificador() { return identificador; }
    public String getCodigo() { return codigo; }
    public String getTipoRecuperacion() { return tipoRecuperacion; }
    public Date getCreadoEn() { return creadoEn; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
}

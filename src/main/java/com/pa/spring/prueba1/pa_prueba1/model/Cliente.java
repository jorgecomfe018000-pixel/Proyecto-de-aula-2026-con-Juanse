package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Document(collection = "clientes")
@Getter @Setter
@EqualsAndHashCode(of = "idCliente")
@ToString(of = {"idCliente", "nombre", "correo"})
public class Cliente {

    @Id
    private String idCliente;

    private String nombre;

    @Indexed(unique = true, sparse = true)
    private String correo;

    private String clave;

    private String telefono;

    private boolean activo = true;

    private String rol = "ROLE_USER";
}
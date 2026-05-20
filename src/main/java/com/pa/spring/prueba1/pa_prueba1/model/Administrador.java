package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Document(collection = "administradores")
@Getter @Setter
@EqualsAndHashCode(of = "idAdministrador")
@ToString(of = {"idAdministrador", "nombre", "correo"})
public class Administrador {

    @Id
    private String idAdministrador;

    private String nombre;

    @Indexed(unique = true)
    private String correo;

    private String clave;
    
    // Campo para compatibilidad con datos antiguos (antes de migración)
    private String password;

    private String telefono;

    private boolean activo = true;

    private String rol = "ROLE_ADMIN";
    
    /**
     * Getter que intenta leer primero 'clave' y si es nulo, intenta con 'password'
     * Esto permite compatibilidad con documentos antiguos en MongoDB
     */
    public String getClaveOPassword() {
        if (this.clave != null && !this.clave.isEmpty()) {
            return this.clave;
        }
        return this.password;
    }
}

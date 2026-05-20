package com.pa.spring.prueba1.pa_prueba1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "estilistas")
@Getter @Setter
public class Estilista {

    @Id
    private String idEstilista;

    // ==================== INFORMACIÓN BÁSICA ====================

    private String nombre;

    private String apellido;

    @Indexed(unique = true)
    private String email;

    private String telefono;

    private String password;

    // ==================== INFORMACIÓN PERSONAL ====================

    @Indexed(unique = true, sparse = true)
    private String documento;

    private LocalDate fechaNacimiento;

    private String direccion;

    // ==================== INFORMACIÓN PROFESIONAL ====================

    private String especialidad; // Ej: Colorimetría, Uñas, Maquillaje, Keratina

    private Integer experienciaAnios;

    private LocalDate fechaIngreso;

    private String certificaciones;

    // ==================== CONFIGURACIÓN DE HORARIO ====================

    private DayOfWeek diaLibre;

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalTime horaInicioAlmuerzo;
    private LocalTime horaFinAlmuerzo;

    private Integer duracionTurno; // en minutos

    // ==================== PERFIL Y SEGURIDAD ====================

    private String fotoPerfil;

    private LocalDateTime ultimaSesion;

    private Boolean autenticacionDosPasos = false;

    private String rol = "ROLE_ESTILISTA";

    // ==================== PREFERENCIAS DE NOTIFICACIONES ====================

    private Boolean notifReservas = true;

    private Boolean notifCancelaciones = true;

    private Boolean notifRecordatorios = true;

    // ==================== ESTADO ====================

    private boolean activo = true;

    // ==================== CONSTRUCTOR ====================

    public Estilista() {
    }

    // ==================== MÉTODOS AUXILIARES ====================

    public String getNombreCompleto() {
        if (apellido != null && !apellido.isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto != null && nombreCompleto.contains(" ")) {
            String[] partes = nombreCompleto.split(" ", 2);
            this.nombre = partes[0];
            this.apellido = partes[1];
        } else {
            this.nombre = nombreCompleto;
            this.apellido = "";
        }
    }

    public boolean tieneFotoPerfil() {
        return fotoPerfil != null && !fotoPerfil.isEmpty();
    }

    public boolean estaDisponibleEnDia(DayOfWeek dia) {
        return activo && !dia.equals(diaLibre);
    }

    public boolean tieneHorarioCompleto() {
        return horaInicio != null && horaFin != null && duracionTurno != null;
    }
}
package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen
public class TurnoApiController {

    // Servicios inyectados para manejar la lógica de negocio
    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private EstilistaService estilistaService;

    // Endpoint de diagnóstico: obtener TODOS los turnos con detalles completos
    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTodosTurnos() {
        try {
            List<Turno> todos = turnoService.obtenerTodos();
            System.out.println("[DIAGNOSTICO] /api/turnos/todos -> count=" + (todos == null ? 0 : todos.size()));
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            if (todos != null && !todos.isEmpty()) {
                for (Turno turno : todos) {
                    Map<String, Object> turnoMap = new HashMap<>();
                    turnoMap.put("idTurno", turno.getIdTurno());
                    turnoMap.put("fechaHora", turno.getFechaHora() != null ? turno.getFechaHora().toString() : "NULL");
                    turnoMap.put("estado", turno.getEstado() != null ? turno.getEstado().toString() : "NULL");
                    turnoMap.put("estilista", turno.getEstilista() != null ? 
                        Map.of("id", turno.getEstilista().getIdEstilista(), "nombre", turno.getEstilista().getNombre()) : 
                        "NULL");
                    resultado.add(turnoMap);
                }
                System.out.println("[DIAGNOSTICO] Primer turno: " + resultado.get(0));
            }
            
            return ResponseEntity.ok(Map.of("count", todos == null ? 0 : todos.size(), "turnos", resultado));
        } catch (Exception e) {
            System.out.println("[DIAGNOSTICO] Error en /api/turnos/todos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage(), "stackTrace", e.getStackTrace()));
        }
    }

    // Método para obtener los turnos disponibles para un estilista específico
    @GetMapping("/disponibles/{estilistaId}")
    public ResponseEntity<?> obtenerTurnosDisponibles(@PathVariable String estilistaId) {
        try {
            // Obtener el estilista por su ID
            Estilista estilista = estilistaService.obtenerPorId(estilistaId);
            if (estilista == null) {
                // Si no se encuentra el estilista, se responde con un error
                System.out.println("Error: Estilista con ID " + estilistaId + " no encontrado");
                return ResponseEntity.badRequest().body(Map.of("error", "Estilista no encontrado"));
            }
            
            System.out.println("Obteniendo turnos disponibles para estilista: " + estilista.getNombre() + " (ID: " + estilistaId + ")");
            
            // Obtener los turnos disponibles para el estilista desde la base de datos
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorEstilista(estilistaId);
            
            System.out.println("Turnos disponibles encontrados en BD: " + turnosDisponibles.size());
            
            // Si no hay turnos disponibles en la base de datos, generar turnos para los próximos 14 días
            if (turnosDisponibles.isEmpty()) {
                LocalDate hoy = LocalDate.now();
                LocalDate finPeriodo = hoy.plusDays(14);
                
                System.out.println("Generando turnos para el periodo: " + hoy + " a " + finPeriodo);
                
                try {
                    turnosDisponibles = turnoService.generarTurnosDisponibles(estilista, hoy, finPeriodo);
                    System.out.println("Turnos generados: " + turnosDisponibles.size());
                } catch (Exception e) {
                    // Si ocurre un error al generar los turnos, se captura y se muestra el error
                    System.out.println("Error al generar turnos: " + e.getMessage());
                    e.printStackTrace();
                    // Continuar con la lista vacía en lugar de fallar
                }
            }
            
            // Filtrar solo los turnos futuros (a partir de ahora)
            LocalDateTime ahora = LocalDateTime.now();
            turnosDisponibles = turnosDisponibles.stream()
                .filter(turno -> turno.getFechaHora() != null && turno.getFechaHora().isAfter(ahora))
                .collect(Collectors.toList());
            
            System.out.println("Turnos futuros disponibles: " + turnosDisponibles.size());
            
            // Convertir los turnos a un formato más simple para retornar como JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                if (turno.getFechaHora() != null) {
                    Map<String, Object> turnoMap = new HashMap<>();
                    turnoMap.put("idTurno", turno.getIdTurno());
                    // Asegurar que la fecha se envía en formato ISO 8601 para evitar problemas de zona horaria
                    turnoMap.put("fechaHora", turno.getFechaHora().toString());
                    turnoMap.put("estado", turno.getEstado().toString());
                    turnoMap.put("estilistaId", turno.getEstilista().getIdEstilista());
                    turnosSimplificados.add(turnoMap);
                }
            }
            
            return ResponseEntity.ok(turnosSimplificados); // Responder con los turnos disponibles en formato JSON
        } catch (Exception e) {
            // Si ocurre un error en el proceso, se captura y se devuelve un error
            System.out.println("Error al obtener turnos disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    // Método para obtener los turnos disponibles para un estilista en una fecha específica
    @GetMapping("/disponibles/{estilistaId}/fecha/{fecha}")
    public ResponseEntity<?> obtenerTurnosDisponiblesPorFecha(
            @PathVariable String estilistaId,
            @PathVariable String fecha) {
        
        try {
            // Convertir la fecha recibida en formato String a LocalDate
            LocalDate fechaSeleccionada = LocalDate.parse(fecha);
            
            // Obtener los turnos disponibles para el estilista en la fecha seleccionada
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorEstilistaYFecha(estilistaId, fechaSeleccionada);
            
            // Convertir los turnos a un formato más simple para retornar como JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                Map<String, Object> turnoMap = new HashMap<>();
                turnoMap.put("idTurno", turno.getIdTurno());
                turnoMap.put("fechaHora", turno.getFechaHora().toString());
                turnoMap.put("estado", turno.getEstado().toString());
                turnoMap.put("estilistaId", turno.getEstilista().getIdEstilista());
                turnosSimplificados.add(turnoMap);
            }
            
            return ResponseEntity.ok(turnosSimplificados); // Responder con los turnos disponibles en formato JSON
        } catch (Exception e) {
            // Si ocurre un error en el proceso, se captura y se devuelve un error
            System.out.println("Error al obtener turnos por fecha: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    // Método para reservar un turno
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarTurno(@RequestBody Map<String, Object> datos) {
        try {
            // Obtener los datos de la reserva del cuerpo de la solicitud
            String turnoId = datos.get("turnoId").toString();
            
            System.out.println("Reservando turno: " + turnoId);
            
            // Verificar si el turno está disponible
            if (!turnoService.esTurnoDisponible(turnoId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El turno seleccionado ya no está disponible"));
            }
            
            // Marcar el turno como no disponible (reservado)
            Turno turno = turnoService.marcarTurnoNoDisponible(turnoId);
            
            if (turno == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo reservar el turno"));
            }
            
            // Preparar la respuesta con los detalles del turno reservado
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Turno reservado con éxito");
            
            Map<String, Object> turnoMap = new HashMap<>();
            turnoMap.put("idTurno", turno.getIdTurno());
            turnoMap.put("fechaHora", turno.getFechaHora().toString());
            turnoMap.put("estado", turno.getEstado().toString());
            
            respuesta.put("turno", turnoMap);
            
            return ResponseEntity.ok(respuesta); // Responder con éxito y los detalles del turno reservado
        } catch (Exception e) {
            // Si ocurre un error al procesar la reserva, se captura y se devuelve un error
            System.out.println("Error al reservar turno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la reserva: " + e.getMessage()));
        }
    }
}

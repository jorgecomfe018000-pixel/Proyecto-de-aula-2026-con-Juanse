package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos") // Define la ruta base para los servicios relacionados con turnos
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (para permitir solicitudes desde frontend en diferentes dominios)
public class TurnoController {

    // Servicio inyectado para la lógica relacionada con los turnos
    @Autowired
    private TurnoService turnoService;

    // Método para obtener la lista de turnos de un estilista específico
    @GetMapping("/estilista/{idEstilista}") // Endpoint que recibe el ID del estilista como parámetro
    public List<Turno> listarTurnosPorEstilista(@PathVariable String idEstilista) {
        // Llama al servicio para obtener los turnos asociados con el estilista dado
        return turnoService.obtenerTurnosPorEstilista(idEstilista);
    }

    // Método para crear un nuevo turno
    @PostMapping // Endpoint para crear un nuevo turno
    public Turno crearTurno(@RequestBody Turno turno) {
        // Llama al servicio para guardar el turno y lo retorna
        return turnoService.guardarTurno(turno);
    }

    // Método para eliminar un turno específico
    @DeleteMapping("/{id}") // Endpoint que recibe el ID del turno a eliminar
    public void eliminarTurno(@PathVariable String id) {
        // Llama al servicio para eliminar el turno con el ID proporcionado
        turnoService.eliminarTurno(id);
    }
}

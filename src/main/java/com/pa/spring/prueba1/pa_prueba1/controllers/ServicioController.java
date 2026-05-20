package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import com.pa.spring.prueba1.pa_prueba1.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para manejar los servicios de belleza (peluquería)
 */
@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public List<ServicioBelleza> listarServicios() {
        return servicioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ServicioBelleza obtenerServicio(@PathVariable String id) {
        return servicioService.obtenerPorId(id);
    }

    @PostMapping
    public ServicioBelleza crearServicio(@RequestBody ServicioBelleza servicio) {
        return servicioService.guardar(servicio);
    }

    @PutMapping("/{id}")
    public ServicioBelleza actualizarServicio(
            @PathVariable String id,
            @RequestBody ServicioBelleza servicioBelleza) {
        return servicioService.actualizar(id, servicioBelleza);
    }

    @DeleteMapping("/{id}")
    public void eliminarServicio(@PathVariable String id) {
        servicioService.eliminar(id);
    }
}
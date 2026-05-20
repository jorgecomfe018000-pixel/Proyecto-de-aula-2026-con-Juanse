package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import java.util.List;

// Interfaz que define los métodos necesarios para gestionar los servicios
public interface ServicioService {
    
    // Método para obtener todos los servicios registrados
    List<ServicioBelleza> obtenerTodos();
    
    // Método para obtener un servicio específico por su ID
    ServicioBelleza obtenerPorId(String id);
    
    // Método para guardar un nuevo servicio o actualizar uno existente
    ServicioBelleza guardar(ServicioBelleza servicioBelleza);
    
    // Método para actualizar los detalles de un servicio específico
    ServicioBelleza actualizar(String id, ServicioBelleza servicioBelleza);
    
    // Método para eliminar un servicio por su ID
    void eliminar(String id);
}

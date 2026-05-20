package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import com.pa.spring.prueba1.pa_prueba1.repository.ServicioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicioServiceImpl implements ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    private static final Logger logger = LoggerFactory.getLogger(ServicioServiceImpl.class);

    // ============================
    // OBTENER TODOS LOS SERVICIOS
    // ============================
    @Override
    public List<ServicioBelleza> obtenerTodos() {
        return servicioRepository.findAll();
    }

    // ============================
    // OBTENER SERVICIO POR ID
    // ============================
    @Override
    public ServicioBelleza obtenerPorId(String id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");

        return servicioRepository.findById(id)
                .orElse(null); // puedes cambiar a excepción si quieres comportamiento estricto
    }

    // ============================
    // GUARDAR SERVICIO
    // ============================
    @Override
    public ServicioBelleza guardar(ServicioBelleza servicioBelleza) {
        Objects.requireNonNull(servicioBelleza, "El servicio no puede ser nulo");

        // Permitir que MongoDB genere el ObjectId
        if (servicioBelleza.getId() == null || servicioBelleza.getId().trim().isEmpty()) {
            servicioBelleza.setId(null);
        }

        logger.debug("Guardando servicio de belleza: {}", servicioBelleza);

        ServicioBelleza saved = servicioRepository.save(servicioBelleza);

        logger.info("Servicio de belleza guardado con id={}", 
                saved != null ? saved.getId() : "null");

        return saved;
    }

    // ============================
    // ACTUALIZAR SERVICIO
    // ============================
    @Override
    public ServicioBelleza actualizar(String id, ServicioBelleza servicioBelleza) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Objects.requireNonNull(servicioBelleza, "El servicio no puede ser nulo");

        Optional<ServicioBelleza> servicioExistente = servicioRepository.findById(id);

        if (servicioExistente.isPresent()) {

            ServicioBelleza existente = servicioExistente.get();

            // Actualización de campos (modelo peluquería)
            existente.setNombre(servicioBelleza.getNombre());
            existente.setDescripcion(servicioBelleza.getDescripcion());
            existente.setPrecio(servicioBelleza.getPrecio());
            existente.setDuracion(servicioBelleza.getDuracion());
            existente.setCategoria(servicioBelleza.getCategoria());
            existente.setActivo(servicioBelleza.isActivo());

            return servicioRepository.save(existente);

        } else {
            return null; // o lanzar excepción si quieres API estricta
        }
    }

    // ============================
    // ELIMINAR SERVICIO
    // ============================
    @Override
    public void eliminar(String id) {
        if (id == null) return;

        String trimmedId = id.trim();
        if (trimmedId.isEmpty()) return;

        try {
            servicioRepository.deleteById(trimmedId);
            logger.info("Servicio eliminado con id={}", trimmedId);
        } catch (Exception e) {
            logger.error("Error eliminando servicio con id={}", trimmedId, e);
        }
    }
}

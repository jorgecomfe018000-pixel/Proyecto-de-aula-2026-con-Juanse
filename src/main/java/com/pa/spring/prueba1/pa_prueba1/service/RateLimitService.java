package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.RateLimit;
import com.pa.spring.prueba1.pa_prueba1.repository.RateLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RateLimitService {

    // Límite por identificador (correo o teléfono) en una ventana de 1 hora
    private static final int LIMITE_POR_HORA = 5;

    @Autowired
    private RateLimitRepository rateLimitRepository;

    public void checkAndIncrement(String clave) throws Exception {
        Optional<RateLimit> opt = rateLimitRepository.findByClave(clave);
        if (opt.isPresent()) {
            RateLimit r = opt.get();
            if (r.getContador() >= LIMITE_POR_HORA) {
                throw new Exception("Has excedido el número de intentos. Intenta más tarde.");
            }
            r.setContador(r.getContador() + 1);
            rateLimitRepository.save(r);
        } else {
            RateLimit nuevo = new RateLimit(clave, 1);
            rateLimitRepository.save(nuevo);
        }
    }
}

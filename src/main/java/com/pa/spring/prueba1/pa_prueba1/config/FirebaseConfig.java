package com.pa.spring.prueba1.pa_prueba1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Stub de configuración de Firebase.
 * Hemos eliminado la inicialización automática de Firebase para volver
 * al flujo clásico por SMTP y Twilio. Si más adelante quieres reactivar
 * Firebase, vuelve a introducir la lógica de inicialización aquí.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    public FirebaseConfig() {
        logger.info("Firebase support is disabled in this build; using SMTP/Twilio flow.");
    }
}

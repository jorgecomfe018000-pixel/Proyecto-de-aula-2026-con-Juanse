package com.pa.spring.prueba1.pa_prueba1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.core.env.Environment;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendRecoveryCode(String to, String code) {
        if (mailSender == null) {
            // Si no hay JavaMailSender configurado, simulamos el envío (útil para desarrollo/testing)
            logger.info("[EMAIL] (Sin SMTP) Código para {}: {}", to, code);
            System.out.println("[EMAIL] (Sin SMTP) Código para " + to + ": " + code);
            return;
        }

        // Intentar conexión TCP al host/puerto configurado para detectar fallos rápidos
        try {
            String host = env.getProperty("spring.mail.host");
            if (host == null || host.isEmpty()) host = env.getProperty("SPRING_MAIL_HOST", "localhost");
            String portStr = env.getProperty("spring.mail.port");
            if (portStr == null || portStr.isEmpty()) portStr = env.getProperty("SPRING_MAIL_PORT", "25");
            try {
                int port = Integer.parseInt(portStr);
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 2000);
                }
            } catch (NumberFormatException nfe) {
                // ignorar: usaremos el envío normal y si falla haremos fallback
            }
        } catch (Exception connCheckEx) {
            logger.warn("[EMAIL] Falló la comprobación de conectividad SMTP: {}", connCheckEx.getMessage());
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Código de recuperación");
            message.setText("Tu código de recuperación es: " + code + "\nSi no solicitaste este código, ignora este mensaje.");
            if (from != null && !from.isEmpty()) {
                message.setFrom(from);
            }
            mailSender.send(message);
            logger.info("[EMAIL] Código enviado a {}", to);
            System.out.println("[EMAIL] Código enviado a " + to + "");
        } catch (Exception e) {
            // Registrar error completo para facilitar diagnóstico
            logger.error("[EMAIL] Error al enviar correo a {}: {}", to, e.getMessage(), e);
            System.err.println("[EMAIL] Error al enviar correo a " + to + ": " + e.getMessage());
            // Fallback: imprimir código en consola para pruebas
            System.out.println("[EMAIL] (Fallback) Código para " + to + ": " + code);
        }
    }
}

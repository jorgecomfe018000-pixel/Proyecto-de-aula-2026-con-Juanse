package com.pa.spring.prueba1.pa_prueba1.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    /**
     * Envía un SMS con el código de recuperación.
     * Si no hay credenciales configuradas, hace fallback a imprimir en consola.
     */
    public void sendRecoveryCode(String telefono, String code) {
        String body = "Tu código de recuperación es: " + code + "\nSi no solicitaste este código, ignora este mensaje.";

        if (twilioAccountSid == null || twilioAccountSid.isBlank() ||
            twilioAuthToken == null || twilioAuthToken.isBlank() ||
            twilioFromNumber == null || twilioFromNumber.isBlank()) {
            System.out.println("[SMS] (Sin Twilio) Código para teléfono " + telefono + ": " + code);
            return;
        }

        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message message = Message.creator(
                    new PhoneNumber(telefono),
                    new PhoneNumber(twilioFromNumber),
                    body
            ).create();
            System.out.println("[SMS] Enviado a " + telefono + " SID=" + message.getSid());
        } catch (ApiException ex) {
            // Registrar y fallback a consola
            System.err.println("[SMS] Error al enviar SMS a " + telefono + ": " + ex.getMessage());
            System.out.println("[SMS] (Fallback) Código para teléfono " + telefono + ": " + code);
        } catch (Exception e) {
            System.err.println("[SMS] Error inesperado al enviar SMS: " + e.getMessage());
            System.out.println("[SMS] (Fallback) Código para teléfono " + telefono + ": " + code);
        }
    }
}

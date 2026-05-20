package com.pa.spring.prueba1.pa_prueba1.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object ex = request.getAttribute("jakarta.servlet.error.exception");
        if (ex == null) {
            ex = request.getAttribute("javax.servlet.error.exception");
        }
        Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
        Object requestUri = request.getAttribute("jakarta.servlet.error.request_uri");

        StringBuilder message = new StringBuilder();
        message.append("URI: ").append(requestUri).append(" | Status: ").append(statusCode).append(" | ");

        if (ex instanceof Throwable t) {
            message.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
            // Get root cause
            Throwable cause = t.getCause();
            while (cause != null) {
                message.append(" → Caused by: ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
                cause = cause.getCause();
            }
        } else if (ex != null) {
            message.append(ex.toString());
        } else {
            message.append("Se produjo un error inesperado.");
        }

        model.addAttribute("errorMessage", message.toString());
        return "error";
    }
}
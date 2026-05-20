# Configuración de Correo para Recuperación de Contraseña

## Resumen
La funcionalidad "Olvidé mi contraseña" ahora está integrada y envía un código de 6 dígitos por correo electrónico. Este documento te guía a través de la configuración necesaria para habilitar el envío real de correos.

## Opciones de Configuración

### Opción 1: Gmail (Recomendado para desarrollo/testing)

1. **Crear una contraseña de aplicación:**
   - Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
   - Asegúrate de tener 2FA habilitado
   - Selecciona "Correo" y "Dispositivo Windows"
   - Copia la contraseña de 16 caracteres generada

2. **Configurar en `application.properties`:**
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=tu-email@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   ```

3. **Actualizar remitente en `EmailService` (opcional):**
   - Si quieres que los correos provengan de una dirección específica, actualiza `spring.mail.username` en el archivo anterior.

### Opción 2: SendGrid (Producción)

1. **Crear cuenta y API Key:**
   - Registrarse en [sendgrid.com](https://sendgrid.com)
   - Generar una API Key desde Settings → API Keys
   - Copiar la API Key

2. **Configurar en `application.properties`:**
   ```properties
   spring.mail.host=smtp.sendgrid.net
   spring.mail.port=587
   spring.mail.username=apikey
   spring.mail.password=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

### Opción 3: MailHog (Desarrollo Local)

Para desarrollo sin envío real, usa MailHog (servidor SMTP mock local).

1. **Descargar e instalar MailHog:**
   - [Releases de MailHog](https://github.com/mailhog/MailHog/releases)
   - Ejecutar: `MailHog.exe` (en Windows)

2. **Configurar en `application.properties`:**
   ```properties
   spring.mail.host=localhost
   spring.mail.port=1025
   ```

3. **Ver correos capturados:**
   - Abre [http://localhost:8025](http://localhost:8025) en el navegador
   - Todos los correos enviados aparecerán en la interfaz web

## Archivos Modificados

### `pom.xml`
- Agregada dependencia: `spring-boot-starter-mail`

### `RecuperarPasswordService.java`
- Inyectados servicios: `EmailService` y `SmsService`
- Reemplazadas llamadas a `System.out.println` con:
  - `emailService.sendRecoveryCode(correo, codigo)` para correo
  - `smsService.sendRecoveryCode(telefono, codigo)` para SMS (placeholder)

### `EmailService.java` (Nuevo)
- Servicio que usa `JavaMailSender` para enviar correos
- Maneja excepciones y registra errores en consola
- El correo contiene: código de 6 dígitos y advertencia de seguridad

### `SmsService.java` (Nuevo)
- Placeholder para SMS (actualmente imprime en consola)
- Fácil de integrar con Twilio, MessageBird, etc.

### `application.properties`
- Agregados comentarios con ejemplos de configuración para 3 proveedores

## Flujo de Recuperación de Contraseña

1. Usuario hace clic en **"¿Olvidaste tu contraseña?"** en login
2. Ingresa su correo o teléfono
3. Sistema genera un código aleatorio de 6 dígitos
4. Código se envía por **correo** (o SMS placeholder) → acceso a consola para testing
5. Usuario recibe el código y lo ingresa en el formulario de verificación
6. Usuario establece una nueva contraseña (mínimo 6 caracteres)
7. Contraseña se encripta con **BCrypt** y se guarda en BD
8. Usuario puede iniciar sesión con la nueva contraseña

## Estructura de Clases

```
RecuperarPasswordService (Orquestador)
├── generarCodigoParaCorreo(String correo)
├── generarCodigoParaTelefono(String telefono)
├── verificarYResetearPassword(String id, String code, String pass, String tipo)
├── resetearPorCorreo(String correo, String nuevaPassword)
├── resetearPorTelefono(String telefono, String nuevaPassword)
└── (Inner) CodigoVerificacion
    ├── codigo (String)
    ├── tipoRecuperacion (String: "correo" o "telefono")
    └── tiempoCreacion (long) — expira en 15 minutos

EmailService
└── sendRecoveryCode(String to, String code) → JavaMailSender

SmsService
└── sendRecoveryCode(String telefono, String code) → Placeholder
```

## Recomendaciones para Producción

1. **Almacenamiento persistente de códigos:**
   - Migrar de `HashMap` en memoria a Redis o DB (MongoDB)
   - Permite recuperación tras reinicio y facilita expiración automática

2. **Rate Limiting:**
   - Limitar intentos por IP / por cuenta (ej: máximo 3 solicitudes por hora)
   - Previene abuso y fuerza bruta

3. **Auditoría y Logging:**
   - Registrar cada solicitud de recuperación (correo/teléfono, IP, timestamp)
   - Registrar intentos fallidos de verificación
   - Facilita detección de ataques

4. **SMS Real:**
   - Reemplazar `SmsService.java` con implementación real (Twilio, MessageBird, etc.)
   - Ejemplo Twilio: [twilio.com/docs/sms/quickstart/java](https://www.twilio.com/docs/sms/quickstart/java)

5. **Validación de Entrada:**
   - Validar formato de correo y teléfono antes de procesar
   - Usar regex o `javax.validation` para mejor robustez

6. **Testing:**
   - Tests unitarios para generación/expiración de códigos
   - Tests de integración para reset de contraseña (Cliente/Barbero/Admin)
   - Tests end-to-end con MailHog

## Pruebas Rápidas

### 1. Con MailHog (Recomendado)
```bash
# Terminal 1: Ejecutar MailHog
MailHog.exe

# Terminal 2: Iniciar aplicación Spring Boot
mvn spring-boot:run

# Navegador: http://localhost:8081/recuperar-password
# Ingresar correo → código se captura en http://localhost:8025
```

### 2. Sin Configuración SMTP
- Si `spring.mail.*` no está configurado, `EmailService` capturará la excepción
- El código seguirá almacenado en memoria
- Puedes leer el código de los logs de consola y usarlo manualmente

### 3. En Base de Datos
- Para verificar que el reset funcionó correctamente:
  ```javascript
  // MongoDB
  db.cliente.findOne({correo: "test@example.com"})
  // Buscar que 'clave' sea diferente y no sea plaintext
  ```

## Archivo de Configuración Completo (Ejemplo)

Ver `src/main/resources/application.properties` para todos los ejemplos de configuración.

---

**Última actualización:** 11 de noviembre de 2025  
**Responsable:** Implementación de recuperación de contraseña con Spring Mail

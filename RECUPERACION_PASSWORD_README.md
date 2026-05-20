# Sistema de Recuperación de Contraseña

## Descripción
Se ha implementado un sistema completo de recuperación de contraseña que permite a los usuarios (Clientes, Barberos y Administradores) recuperar su acceso mediante dos métodos:
- **Por Correo Electrónico**
- **Por Teléfono**

## Archivos Creados/Modificados

### 1. **Controlador: RecuperarPasswordController.java**
- **Ruta**: `src/main/java/com/pa/spring/prueba1/pa_prueba1/controllers/`
- **Funcionalidades**:
  - GET `/recuperar-password` - Muestra el formulario de recuperación
  - POST `/recuperar-password/por-correo` - Procesa recuperación por correo
  - POST `/recuperar-password/por-telefono` - Procesa recuperación por teléfono
  - POST `/recuperar-password/verificar-codigo` - Verifica código y resetea contraseña

### 2. **Servicio: RecuperarPasswordService.java**
- **Ruta**: `src/main/java/com/pa/spring/prueba1/pa_prueba1/service/`
- **Responsabilidades**:
  - Generar códigos de verificación (6 dígitos aleatorios)
  - Validar códigos con expiración (15 minutos)
  - Resetear contraseña en las tablas correspondientes
  - Soportar búsqueda por correo y teléfono

### 3. **Templates HTML**
- **recuperar-password.html**: Formulario con tabs para elegir método de recuperación
- **verificar-codigo.html**: Formulario para ingresar código y nueva contraseña

### 4. **Repositorios Actualizados**
- **ClienteRepository.java**: Agregados métodos `findByTelefono()`, `existsByTelefono()`
- **BarberoRepository.java**: Agregados métodos `findByTelefono()`, `existsByTelefono()`
- **AdministradorRepository.java**: Sin cambios necesarios (usa correo)

### 5. **Login.html**
- Agregado botón "¿Olvidaste tu contraseña?" con enlace a `/recuperar-password`

## Flujo de Funcionamiento

### Paso 1: Solicitar Recuperación
```
Usuario → Enlace "¿Olvidaste tu contraseña?" → GET /recuperar-password
```

### Paso 2: Elegir Método
- Por Correo: Ingresar correo electrónico
- Por Teléfono: Ingresar número de 10 dígitos

### Paso 3: Generar Código
```
Sistema genera código de 6 dígitos
Almacena código en memoria con expiración de 15 minutos
TODO: En producción, enviar por email o SMS
El código se imprime en consola para testing
```

### Paso 4: Verificar e Ingresar Contraseña
```
Usuario ingresa:
- Código de 6 dígitos
- Nueva contraseña (mínimo 6 caracteres)
- Confirmación de contraseña
```

### Paso 5: Resetear
```
Sistema verifica código
Sistema valida contraseñas coincidan
Sistema encripta contraseña con BCrypt
Sistema guarda en la tabla correspondiente (Cliente/Barbero/Admin)
Redirige a login con mensaje de éxito
```

## Códigos de Prueba

Para testing, los códigos se imprimen en la consola:
```
[RECUPERACIÓN] Código para correo usuario@example.com: 123456
[RECUPERACIÓN] Código para teléfono 3001234567: 654321
```

## Seguridad

✅ **Características de Seguridad Implementadas:**

1. **Códigos de Verificación**:
   - 6 dígitos aleatorios (1 millón de combinaciones)
   - Expiran en 15 minutos
   - Se almacenan en memoria (cambiar a BD/Redis en producción)

2. **Encriptación de Contraseñas**:
   - Usa BCryptPasswordEncoder
   - Contraseña mínima de 6 caracteres
   - Validación de coincidencia antes de guardar

3. **Validación**:
   - Verifica que la cuenta exista
   - Verifica que el código sea correcto
   - Verifica que el código no haya expirado
   - Valida que las contraseñas coincidan

4. **Manejo de Errores**:
   - Mensajes claros en caso de error
   - Redireccionamiento seguro
   - No expone información sensible

## TODOs para Producción

1. **Integración de Email**:
   ```java
   // Usar Spring Mail para enviar códigos por email
   emailService.enviarCodigoRecuperacion(correo, codigo);
   ```

2. **Integración de SMS**:
   ```java
   // Usar Twilio o similar para enviar códigos por SMS
   smsService.enviarCodigoRecuperacion(telefono, codigo);
   ```

3. **Almacenamiento Persistente de Códigos**:
   ```java
   // En lugar de HashMap, usar Redis o BD
   // Mayor seguridad y escalabilidad
   ```

4. **Logging y Auditoría**:
   ```java
   // Registrar intentos de recuperación
   // Detectar patrones de ataque
   ```

5. **Rate Limiting**:
   ```java
   // Limitar intentos por IP/usuario
   // Prevenir fuerza bruta
   ```

## Rutas Disponibles

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/recuperar-password` | Muestra formulario |
| POST | `/recuperar-password/por-correo` | Genera código por correo |
| POST | `/recuperar-password/por-telefono` | Genera código por teléfono |
| POST | `/recuperar-password/verificar-codigo` | Resetea contraseña |
| GET | `/login` | Volver a login |

## Ejemplo de Uso

### 1. Usuario olvida contraseña
```
1. Hace clic en "¿Olvidaste tu contraseña?"
2. Selecciona método (correo o teléfono)
3. Ingresa su correo o teléfono
4. Sistema genera código y lo envía
```

### 2. Usuario verifica código
```
1. Ingresa el código recibido (6 dígitos)
2. Ingresa nueva contraseña (mínimo 6 caracteres)
3. Confirma la contraseña
4. Sistema resetea y redirige a login
```

### 3. Usuario inicia sesión con nueva contraseña
```
1. Va a /login
2. Ingresa correo/usuario y nueva contraseña
3. Acceso otorgado
```

## Notas Importantes

⚠️ **Importante para desarrollo/testing:**

- Los códigos se imprimen en consola (Check Application Console)
- Los códigos expiran en 15 minutos
- Los intentos fallidos no cierran la sesión
- Las contraseñas deben coincidir exactamente
- Se soportan Clientes, Barberos y Administradores

## Mejoras Futuras

- [ ] Envío real de emails/SMS
- [ ] Almacenamiento de códigos en BD/Redis
- [ ] Rate limiting y detección de fraude
- [ ] 2FA (Two-Factor Authentication)
- [ ] Recovery codes de respaldo
- [ ] Historial de cambios de contraseña
- [ ] Notificación de cambio de contraseña

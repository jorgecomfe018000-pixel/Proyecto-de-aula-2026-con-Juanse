package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.model.CodigoVerificacion;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.CodigoVerificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Random;


@Service
public class RecuperarPasswordService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EstilistaRepository estilistaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private CodigoVerificacionRepository codigoRepository;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;


    private static final long TIEMPO_EXPIRACION_MS = 15 * 60 * 1000; // 15 minutos
    private static final int MAX_INTENTOS_VERIFICACION = 5;

    public String generarCodigoParaCorreo(String correo) throws Exception {
        // Validar que exista en al menos una tabla
        boolean existe = clienteRepository.existsByCorreo(correo) ||
                estilistaRepository.findByEmail(correo).isPresent() ||
                administradorRepository.existsByCorreo(correo);

        if (!existe) {
            throw new Exception("No existe cuenta registrada con este correo.");
        }

        // Rate limit por correo
        rateLimitService.checkAndIncrement("send:" + correo);

        // Flujo clásico: código de 6 dígitos

        // Flujo clásico: código de 6 dígitos
        String codigo = generarCodigo();
        CodigoVerificacion cv = new CodigoVerificacion(correo, codigo, "correo");
        codigoRepository.save(cv);

        // Enviar codigo por email
        emailService.sendRecoveryCode(correo, codigo);

        return codigo;
    }

    public String generarCodigoParaTelefono(String telefono) throws Exception {
        // Validar que exista en al menos una tabla
        boolean existeCliente = clienteRepository.findByTelefono(telefono).isPresent();
        boolean existeEstilista = estilistaRepository.findByTelefono(telefono).isPresent();

        if (!existeCliente && !existeEstilista) {
            throw new Exception("No existe cuenta registrada con este teléfono.");
        }

        // Rate limit por telefono
        rateLimitService.checkAndIncrement("send:" + telefono);

        String codigo = generarCodigo();
        CodigoVerificacion cv = new CodigoVerificacion(telefono, codigo, "telefono");
        codigoRepository.save(cv);

        // Enviar codigo por SMS (placeholder)
        smsService.sendRecoveryCode(telefono, codigo);

        return codigo;
    }

    @Transactional
    public void verificarYResetearPassword(String identificador, String codigo, String nuevaPassword, String tipo) throws Exception {
        // Validar contraseñas
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new Exception("La contraseña debe tener al menos 6 caracteres.");
        }

        Optional<CodigoVerificacion> opt = codigoRepository.findFirstByIdentificadorOrderByCreadoEnDesc(identificador);
        if (opt.isEmpty()) {
            throw new Exception("Código inválido o expirado.");
        }

        CodigoVerificacion codigoGuardado = opt.get();

        // Verificar expiración por seguridad (TTL también está configurado)
        Date ahora = new Date();
        if (ahora.getTime() - codigoGuardado.getCreadoEn().getTime() > TIEMPO_EXPIRACION_MS) {
            codigoRepository.deleteByIdentificador(identificador);
            throw new Exception("El código ha expirado. Por favor, intenta nuevamente.");
        }

        if (!codigo.equals(codigoGuardado.getCodigo())) {
            // Aumentar contador de intentos y bloquear si excede
            int intentos = codigoGuardado.getIntentos() + 1;
            codigoGuardado.setIntentos(intentos);
            codigoRepository.save(codigoGuardado);
            if (intentos >= MAX_INTENTOS_VERIFICACION) {
                codigoRepository.deleteByIdentificador(identificador);
                throw new Exception("Demasiados intentos fallidos. Se ha invalidado el código.");
            }
            throw new Exception("Código incorrecto.");
        }

        // Resetear contraseña
        if ("correo".equals(tipo)) {
            resetearPorCorreo(identificador, nuevaPassword);
        } else if ("telefono".equals(tipo)) {
            resetearPorTelefono(identificador, nuevaPassword);
        }

        // Limpiar código usado
        codigoRepository.deleteByIdentificador(identificador);
    }

    private void resetearPorCorreo(String correo, String nuevaPassword) throws Exception {
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);

        Optional<Cliente> cliente = clienteRepository.findByCorreo(correo);
        if (cliente.isPresent()) {
            Cliente c = cliente.orElseThrow(() -> new Exception("No se pudo encontrar la cuenta para resetear."));
            c.setClave(passwordEncriptada);
            clienteRepository.save(c);
            return;
        }

        Optional<Estilista> estilista = estilistaRepository.findByEmail(correo);
        if (estilista.isPresent()) {
            Estilista e = estilista.orElseThrow(() -> new Exception("No se pudo encontrar la cuenta para resetear."));
            e.setPassword(passwordEncriptada);
            estilistaRepository.save(e);
            return;
        }

        Optional<Administrador> admin = administradorRepository.findByCorreo(correo);
        if (admin.isPresent()) {
            Administrador a = admin.orElseThrow(() -> new Exception("No se pudo encontrar la cuenta para resetear."));
            a.setClave(passwordEncriptada);
            administradorRepository.save(a);
            return;
        }

        throw new Exception("No se pudo encontrar la cuenta para resetear.");
    }

    private void resetearPorTelefono(String telefono, String nuevaPassword) throws Exception {
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);

        Optional<Cliente> cliente = clienteRepository.findByTelefono(telefono);
        if (cliente.isPresent()) {
            Cliente c = cliente.orElseThrow(() -> new Exception("No se pudo encontrar la cuenta para resetear."));
            c.setClave(passwordEncriptada);
            clienteRepository.save(c);
            return;
        }

        Optional<Estilista> estilista = estilistaRepository.findByTelefono(telefono);
        if (estilista.isPresent()) {
            Estilista e = estilista.orElseThrow(() -> new Exception("No se pudo encontrar la cuenta para resetear."));
            e.setPassword(passwordEncriptada);
            estilistaRepository.save(e);
            return;
        }

        throw new Exception("No se pudo encontrar la cuenta para resetear.");
    }

    private String generarCodigo() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    // Antes soportábamos Firebase; ahora siempre usamos flujo por código (SMTP/Twilio)
    // Si quieres reactivar Firebase, reintroduce la comprobación aquí.
}

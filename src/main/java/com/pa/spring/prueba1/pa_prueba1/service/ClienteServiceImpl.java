package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente obtenerPorId(String id) {
        if (id == null) return null;
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        if (cliente.getRol() == null || cliente.getRol().isEmpty()) {
            cliente.setRol("ROLE_USER");
        }

        String idCliente = cliente.getIdCliente();
        boolean esNuevo = (idCliente == null || idCliente.trim().isEmpty());

        if (esNuevo) {
            cliente.setIdCliente(null);
            if (cliente.getClave() != null && !cliente.getClave().isEmpty()) {
                cliente.setClave(passwordEncoder.encode(cliente.getClave()));
            }
            return clienteRepository.save(cliente);
        } else {
            Optional<Cliente> optional = (idCliente != null) ? clienteRepository.findById(idCliente) : Optional.empty();
            if (optional.isPresent()) {
                Cliente existente = optional.get();
                existente.setNombre(cliente.getNombre());
                existente.setCorreo(cliente.getCorreo());
                existente.setTelefono(cliente.getTelefono());
                String clave = cliente.getClave();
                if (clave != null && !clave.isEmpty()
                        && !clave.startsWith("$2a$")
                        && !clave.startsWith("$2b$")) {
                    existente.setClave(passwordEncoder.encode(clave));
                }
                return clienteRepository.save(existente);
            }
            return null;
        }
    }

    @Override
    public Cliente actualizar(String id, Cliente cliente) {
        if (id == null) return null;
        Optional<Cliente> clienteExistente = clienteRepository.findById(id);
        if (clienteExistente.isPresent()) {
            Cliente actualizarCliente = clienteExistente.get();
            actualizarCliente.setNombre(cliente.getNombre());
            actualizarCliente.setCorreo(cliente.getCorreo());
            actualizarCliente.setTelefono(cliente.getTelefono());
            if (cliente.getClave() != null && !cliente.getClave().isEmpty()) {
                if (!cliente.getClave().startsWith("$2a$") && !cliente.getClave().startsWith("$2b$")) {
                    actualizarCliente.setClave(passwordEncoder.encode(cliente.getClave()));
                } else {
                    actualizarCliente.setClave(cliente.getClave());
                }
            }
            return clienteRepository.save(actualizarCliente);
        }
        return null;
    }

    @Override
    public void eliminar(String id) {
        if (id == null) return;
        clienteRepository.deleteById(id);
    }

    @Override
    public Cliente verificarCredenciales(String correo, String clave) {
        return clienteRepository.findByCorreo(correo)
                .filter(c -> passwordEncoder.matches(clave, c.getClave()))
                .orElse(null);
    }

    @Override
    public boolean existeCliente(String correo) {
        return clienteRepository.findByCorreo(correo).isPresent();
    }

    @Override
    public boolean tieneReservasRelacionadas(String idCliente) {
        if (idCliente == null) return false;
        return reservaRepository.existsByCliente_IdCliente(idCliente);
    }

    @Override
    public void inhabilitarCliente(String id) {
        if (id == null) throw new RuntimeException("ID nulo");
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public Cliente obtenerPorCorreo(String correo) {
        return clienteRepository.findByCorreo(correo).orElse(null);
    }
}
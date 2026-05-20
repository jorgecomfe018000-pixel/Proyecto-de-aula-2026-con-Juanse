package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import java.util.List;

public interface ClienteService {

    List<Cliente> obtenerTodos();

    Cliente obtenerPorId(String id);

    Cliente guardar(Cliente cliente);

    Cliente actualizar(String id, Cliente cliente);

    void eliminar(String id);

    Cliente verificarCredenciales(String correo, String clave);

    boolean existeCliente(String correo);

    boolean tieneReservasRelacionadas(String idCliente);

    void inhabilitarCliente(String id);

    Cliente obtenerPorCorreo(String correo);
}

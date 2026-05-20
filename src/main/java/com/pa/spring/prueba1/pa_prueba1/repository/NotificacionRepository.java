package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    
    // métodos antiguos basados en campo 'barbero' (migración)
    @Query(value = "{ 'barbero.$id': ?0 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByBarberoIdBarberoOrderByFechaCreacionDesc(String idBarbero);
    
    @Query(value = "{ 'barbero.$id': ?0, 'leida': ?1 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(String idBarbero, Boolean leida);
    
    @Query(value = "{ 'barbero.$id': ?0, 'tipo': ?1 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(String idBarbero, Notificacion.TipoNotificacion tipo);
    
    @Query(value = "{ 'barbero.$id': ?0, 'leida': ?1 }", count = true)
    long countByBarberoIdBarberoAndLeida(String idBarbero, Boolean leida);
    
    // nuevos métodos para estilista
    @Query(value = "{ 'estilista.$id': ?0 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByEstilistaIdEstilistaOrderByFechaCreacionDesc(String idEstilista);
    
    @Query(value = "{ 'estilista.$id': ?0, 'leida': ?1 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByEstilistaIdEstilistaAndLeidaOrderByFechaCreacionDesc(String idEstilista, Boolean leida);
    
    @Query(value = "{ 'estilista.$id': ?0, 'tipo': ?1 }", sort = "{ 'fechaCreacion': -1 }")
    List<Notificacion> findByEstilistaIdEstilistaAndTipoOrderByFechaCreacionDesc(String idEstilista, Notificacion.TipoNotificacion tipo);
    
    @Query(value = "{ 'estilista.$id': ?0, 'leida': ?1 }", count = true)
    long countByEstilistaIdEstilistaAndLeida(String idEstilista, Boolean leida);
}

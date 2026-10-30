package com.udb.hotel.repository;

import com.udb.hotel.model.EstadoReclamo;
import com.udb.hotel.model.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {

    @Query("""
            select c from Reclamo c
              join fetch c.huesped
              left join fetch c.reserva
             where (:estado is null or c.estado = :estado)
             order by c.fechaReclamo desc
            """)
    List<Reclamo> listar(@Param("estado") EstadoReclamo estado);

    @Query("""
            select c from Reclamo c
              join fetch c.huesped h
              left join fetch c.reserva
             where h.id = :huespedId
             order by c.fechaReclamo desc
            """)
    List<Reclamo> listarDeHuesped(@Param("huespedId") Integer huespedId);

    long countByEstado(EstadoReclamo estado);

    boolean existsByHuespedId(Integer huespedId);

    boolean existsByReservaId(Integer reservaId);
}

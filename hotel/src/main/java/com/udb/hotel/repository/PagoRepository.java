package com.udb.hotel.repository;

import com.udb.hotel.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    @Query("""
            select p from Pago p
              join fetch p.reserva r
              join fetch r.huesped
              join fetch r.hotel
             order by p.fechaPago desc
            """)
    List<Pago> listarTodos();

    /** Pagos aprobados y reembolsos registrados dentro del rango. */
    @Query("""
            select p from Pago p
              join fetch p.reserva r
              join fetch r.hotel
             where p.fechaPago >= :desde and p.fechaPago < :hasta
               and p.estado in (com.udb.hotel.model.EstadoPago.APROBADO, com.udb.hotel.model.EstadoPago.REEMBOLSADO)
            """)
    List<Pago> movimientosEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}

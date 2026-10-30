package com.udb.hotel.repository;

import com.udb.hotel.model.EstadoReserva;
import com.udb.hotel.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    Optional<Reserva> findByCodigo(String codigo);

    @Query("""
            select r from Reserva r
              join fetch r.huesped
              join fetch r.hotel ho
              join fetch r.usuario
             where (:estado is null or r.estadoReserva = :estado)
               and (:hotelId is null or ho.id = :hotelId)
             order by r.fechaReserva desc
            """)
    List<Reserva> listar(@Param("estado") EstadoReserva estado, @Param("hotelId") Integer hotelId);

    @Query("""
            select r from Reserva r
              join fetch r.huesped hu
              join fetch r.hotel
              join fetch r.usuario u
             where u.id = :usuarioId
                or (:huespedId is not null and hu.id = :huespedId)
             order by r.fechaReserva desc
            """)
    List<Reserva> listarDeUsuario(@Param("usuarioId") Integer usuarioId, @Param("huespedId") Integer huespedId);

    @Query("""
            select count(d) from DetalleReserva d join d.reserva r
             where d.habitacion.id = :habitacionId
               and r.estadoReserva in :estados
               and r.fechaEntrada < :salida
               and r.fechaSalida > :entrada
               and (:excluirId is null or r.id <> :excluirId)
            """)
    long contarCruces(@Param("habitacionId") Integer habitacionId,
                      @Param("entrada") LocalDate entrada,
                      @Param("salida") LocalDate salida,
                      @Param("estados") Collection<EstadoReserva> estados,
                      @Param("excluirId") Integer excluirId);

    @Query("select count(d) from DetalleReserva d where d.habitacion.id = :habitacionId")
    long contarDetallesDeHabitacion(@Param("habitacionId") Integer habitacionId);

    boolean existsByHuespedId(Integer huespedId);

    boolean existsByUsuarioId(Integer usuarioId);

    boolean existsByHotelId(Integer hotelId);

    @Query("""
            select distinct r from Reserva r
              join fetch r.hotel
              join fetch r.detalles
             where r.estadoReserva in :estados
               and r.fechaEntrada < :hasta
               and r.fechaSalida > :desde
            """)
    List<Reserva> ocupando(@Param("desde") LocalDate desde,
                           @Param("hasta") LocalDate hasta,
                           @Param("estados") Collection<EstadoReserva> estados);

    @Query("select r from Reserva r join fetch r.hotel where r.fechaReserva >= :desde and r.fechaReserva < :hasta")
    List<Reserva> creadasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}

package com.udb.hotel.repository;

import com.udb.hotel.model.EstadoReserva;
import com.udb.hotel.model.Habitacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    @Query("select h from Habitacion h join fetch h.hotel ho join fetch h.tipo order by ho.nombreHotel, h.numeroHabitacion")
    List<Habitacion> listarTodas();

    @Query("select h from Habitacion h join fetch h.hotel join fetch h.tipo where h.hotel.id = :hotelId order by h.numeroHabitacion")
    List<Habitacion> listarPorHotel(@Param("hotelId") Integer hotelId);

    Optional<Habitacion> findByHotelIdAndNumeroHabitacionIgnoreCase(Integer hotelId, String numero);

    boolean existsByHotelId(Integer hotelId);

    boolean existsByTipoId(Integer tipoId);

    /**
     * Habitaciones que cumplen los criterios y NO tienen un Detalle_Reserva de una reserva activa
     * que se cruce con el período. Dos períodos se cruzan si entradaA < salidaB y salidaA > entradaB
     * (la salida de una reserva puede ser la entrada de otra). Las habitaciones OCUPADAS hoy pueden
     * estar libres en fechas futuras, por eso solo se excluyen MANTENIMIENTO y FUERA_DE_SERVICIO.
     */
    @Query("""
            select h from Habitacion h
              join fetch h.hotel ho
              join fetch h.tipo t
             where lower(ho.estado.estadoHotel) = 'activo'
               and t.activo = true
               and h.estado in (com.udb.hotel.model.EstadoHabitacion.DISPONIBLE,
                                com.udb.hotel.model.EstadoHabitacion.OCUPADA)
               and t.cantidadPersonas >= :huespedes
               and (:ciudad is null or lower(ho.ciudad) like lower(concat('%', :ciudad, '%'))
                                    or lower(ho.nombreHotel) like lower(concat('%', :ciudad, '%')))
               and (:tipoId is null or t.id = :tipoId)
               and (:hotelId is null or ho.id = :hotelId)
               and not exists (
                    select 1 from DetalleReserva d join d.reserva r
                     where d.habitacion = h
                       and r.estadoReserva in :estadosActivos
                       and r.fechaEntrada < :salida
                       and r.fechaSalida > :entrada)
             order by ho.nombreHotel, h.precioBase, h.numeroHabitacion
            """)
    List<Habitacion> buscarDisponibles(@Param("ciudad") String ciudad,
                                       @Param("tipoId") Integer tipoId,
                                       @Param("hotelId") Integer hotelId,
                                       @Param("huespedes") int huespedes,
                                       @Param("entrada") LocalDate entrada,
                                       @Param("salida") LocalDate salida,
                                       @Param("estadosActivos") Collection<EstadoReserva> estadosActivos);

    /** Bloquea la fila de la habitación mientras se crea/modifica una reserva (evita reservas dobles). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Habitacion h join fetch h.hotel ho join fetch ho.estado join fetch h.tipo where h.id = :id")
    Optional<Habitacion> bloquearParaReserva(@Param("id") Integer id);
}

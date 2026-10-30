package com.udb.hotel.repository;

import com.udb.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    Optional<Hotel> findByNombreHotelIgnoreCase(String nombre);

    @Query("select h from Hotel h join fetch h.estado left join fetch h.empleado order by h.nombreHotel")
    List<Hotel> listarTodos();

    @Query("select distinct h.ciudad from Hotel h where lower(h.estado.estadoHotel) = 'activo' order by h.ciudad")
    List<String> ciudadesActivas();

    boolean existsByEmpleadoId(Integer empleadoId);

    boolean existsByEstadoId(Integer estadoId);
}

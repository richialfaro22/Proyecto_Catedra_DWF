package com.udb.hotel.repository;

import com.udb.hotel.model.EstadoHotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoHotelRepository extends JpaRepository<EstadoHotel, Integer> {

    Optional<EstadoHotel> findByEstadoHotelIgnoreCase(String estado);

    List<EstadoHotel> findAllByOrderByIdAsc();
}

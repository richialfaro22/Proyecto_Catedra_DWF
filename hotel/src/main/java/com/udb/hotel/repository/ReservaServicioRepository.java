package com.udb.hotel.repository;

import com.udb.hotel.model.ReservaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaServicioRepository extends JpaRepository<ReservaServicio, Integer> {

    boolean existsByServicioId(Integer servicioId);
}

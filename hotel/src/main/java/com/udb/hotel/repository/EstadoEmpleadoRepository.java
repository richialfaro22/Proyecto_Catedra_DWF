package com.udb.hotel.repository;

import com.udb.hotel.model.EstadoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoEmpleadoRepository extends JpaRepository<EstadoEmpleado, Integer> {

    Optional<EstadoEmpleado> findByTipoEstadoEmpleadoIgnoreCase(String tipo);

    List<EstadoEmpleado> findAllByOrderByIdAsc();
}

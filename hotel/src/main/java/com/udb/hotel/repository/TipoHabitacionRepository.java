package com.udb.hotel.repository;

import com.udb.hotel.model.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Integer> {

    Optional<TipoHabitacion> findByTipoHabitacionIgnoreCase(String nombre);

    List<TipoHabitacion> findAllByOrderByTipoHabitacionAsc();
}

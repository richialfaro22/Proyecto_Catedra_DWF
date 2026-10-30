package com.udb.hotel.repository;

import com.udb.hotel.model.ServicioAdicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioAdicionalRepository extends JpaRepository<ServicioAdicional, Integer> {

    Optional<ServicioAdicional> findByNombreServicioIgnoreCase(String nombre);

    List<ServicioAdicional> findAllByOrderByCategoriaAscNombreServicioAsc();

    List<ServicioAdicional> findByActivoTrueOrderByCategoriaAscNombreServicioAsc();
}

package com.udb.hotel.repository;

import com.udb.hotel.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    @Query("select e from Empleado e join fetch e.estado order by e.nombreEmpleado")
    List<Empleado> listarTodos();

    Optional<Empleado> findByEmailIgnoreCase(String email);

    boolean existsByEstadoId(Integer estadoId);
}

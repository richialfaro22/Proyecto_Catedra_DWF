package com.udb.hotel.repository;

import com.udb.hotel.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByAliasIgnoreCase(String alias);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("select u from Usuario u where lower(u.alias) = lower(:valor) or lower(u.email) = lower(:valor)")
    Optional<Usuario> buscarPorAliasOEmail(@Param("valor") String valor);

    @Query("select u from Usuario u left join fetch u.empleado left join fetch u.huesped order by u.alias")
    List<Usuario> listarTodos();

    Optional<Usuario> findFirstByHuespedId(Integer huespedId);

    boolean existsByEmpleadoId(Integer empleadoId);

    boolean existsByHuespedId(Integer huespedId);
}

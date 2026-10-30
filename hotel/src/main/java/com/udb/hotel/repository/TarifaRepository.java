package com.udb.hotel.repository;

import com.udb.hotel.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    @Query("select t from Tarifa t join fetch t.hotel h join fetch t.tipo order by h.nombreHotel, t.fechaInicio")
    List<Tarifa> listarTodas();

    @Query("""
            select t from Tarifa t
             where t.activa = true
               and t.hotel.id = :hotelId
               and t.tipo.id = :tipoId
               and t.fechaInicio < :hasta
               and t.fechaFin >= :desde
            """)
    List<Tarifa> vigentes(@Param("hotelId") Integer hotelId,
                          @Param("tipoId") Integer tipoId,
                          @Param("desde") LocalDate desde,
                          @Param("hasta") LocalDate hasta);

    @Query("""
            select count(t) from Tarifa t
             where t.activa = true
               and t.hotel.id = :hotelId
               and t.tipo.id = :tipoId
               and t.fechaInicio <= :fin
               and t.fechaFin >= :inicio
               and (:excluirId is null or t.id <> :excluirId)
            """)
    long contarTraslapes(@Param("hotelId") Integer hotelId,
                         @Param("tipoId") Integer tipoId,
                         @Param("inicio") LocalDate inicio,
                         @Param("fin") LocalDate fin,
                         @Param("excluirId") Integer excluirId);

    boolean existsByHotelId(Integer hotelId);

    boolean existsByTipoId(Integer tipoId);
}

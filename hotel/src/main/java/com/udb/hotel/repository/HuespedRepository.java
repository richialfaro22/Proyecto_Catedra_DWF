package com.udb.hotel.repository;

import com.udb.hotel.model.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Integer> {

    Optional<Huesped> findByDocumentoIgnoreCase(String documento);

    @Query("""
            select h from Huesped h
             where (:q is null
                    or lower(h.nombreHuesped) like lower(concat('%', :q, '%'))
                    or lower(h.documento) like lower(concat('%', :q, '%'))
                    or lower(h.email) like lower(concat('%', :q, '%')))
             order by h.nombreHuesped
            """)
    List<Huesped> buscar(@Param("q") String q);
}

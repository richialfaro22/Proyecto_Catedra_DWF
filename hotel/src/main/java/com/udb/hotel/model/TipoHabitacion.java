package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Tipo_Habitacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipohabitacion")
    private Integer id;

    @Column(name = "tipohabitacion", nullable = false, unique = true, length = 50)
    private String tipoHabitacion;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @Column(name = "servicios_incluidos", length = 300)
    private String serviciosIncluidos;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}

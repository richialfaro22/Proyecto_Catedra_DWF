package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Habitacion",
       uniqueConstraints = @UniqueConstraint(name = "uk_habitacion_hotel_numero",
                                             columnNames = {"id_hotel", "numerohabitacion"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Integer id;

    @Column(name = "numerohabitacion", nullable = false, length = 10)
    private String numeroHabitacion;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoHabitacion estado;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "numpiso", nullable = false)
    private Integer numPiso;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipohabitacion", nullable = false)
    private TipoHabitacion tipo;
}

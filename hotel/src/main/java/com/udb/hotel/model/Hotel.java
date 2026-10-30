package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Hotel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hotel")
    private Integer id;

    @Column(name = "nombre_hotel", nullable = false, unique = true, length = 150)
    private String nombreHotel;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estadohotel", nullable = false)
    private EstadoHotel estado;

    // ---- Ampliación Fase 2
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "estrellas", nullable = false)
    private Integer estrellas;

    @Column(name = "horas_cancelacion_gratis", nullable = false)
    private Integer horasCancelacionGratis;


    @Column(name = "porcentaje_penalizacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajePenalizacion;
}

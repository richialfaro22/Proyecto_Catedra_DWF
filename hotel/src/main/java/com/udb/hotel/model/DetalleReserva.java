package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Detalle_Reserva")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detallereserva")
    private Integer id;

    @Column(name = "PrecioReserva", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReserva;

    @Column(name = "cantidad_noches", nullable = false)
    private Integer cantidadNoches;

    @Column(name = "num_personas", nullable = false)
    private Integer numPersonas;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;
}

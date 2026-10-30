package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Reservas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer id;

    @Builder.Default
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estadoreserva", nullable = false, length = 30)
    private EstadoReserva estadoReserva;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_huesped", nullable = false)
    private Huesped huesped;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReserva> detalles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @OrderBy("fechaPago ASC")
    private List<Pago> pagos = new ArrayList<>();


    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaServicio> servicios = new ArrayList<>();

    @Column(name = "subtotal_habitaciones", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalHabitaciones;

    @Column(name = "subtotal_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalServicios;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    @Column(name = "cargo_cancelacion", precision = 12, scale = 2)
    private BigDecimal cargoCancelacion;

    public long getNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public Habitacion getHabitacionPrincipal() {
        return detalles.isEmpty() ? null : detalles.get(0).getHabitacion();
    }

    public int getNumPersonas() {
        return detalles.stream().mapToInt(DetalleReserva::getNumPersonas).sum();
    }
}

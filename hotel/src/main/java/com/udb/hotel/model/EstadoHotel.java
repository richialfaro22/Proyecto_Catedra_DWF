package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Estado_Hotel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoHotel {

    public static final String ACTIVO = "Activo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estadohotel")
    private Integer id;

    @Column(name = "estado_hotel", nullable = false, unique = true, length = 30)
    private String estadoHotel;

    public boolean permiteReservas() {
        return ACTIVO.equalsIgnoreCase(estadoHotel);
    }
}

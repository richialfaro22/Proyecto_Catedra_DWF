package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Estado_Empleado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estadoempleado")
    private Integer id;

    @Column(name = "tipo_estadoempleado", nullable = false, unique = true, length = 30)
    private String tipoEstadoEmpleado;
}

package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "Empleado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer id;

    @Column(name = "nombre_empleado", nullable = false, length = 150)
    private String nombreEmpleado;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "tipo_empleado", nullable = false, length = 50)
    private String tipoEmpleado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estadoempleado", nullable = false)
    private EstadoEmpleado estado;
}

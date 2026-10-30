package com.udb.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Servicio_Adicional")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer id;

    @Column(name = "nombre_servicio", nullable = false, unique = true, length = 80)
    private String nombreServicio;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private CategoriaServicio categoria;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobro", nullable = false, length = 20)
    private TipoCobro tipoCobro;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}

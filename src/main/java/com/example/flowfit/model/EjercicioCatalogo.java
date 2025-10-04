package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ejercicio_catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioCatalogo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(length = 255)
    private String imagen;
    
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
}
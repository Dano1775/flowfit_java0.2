package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "rutina_ejercicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutinaEjercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "rutina_id", nullable = false)
    private Integer rutinaId;
    
    @Column(name = "ejercicio_id", nullable = false)
    private Integer ejercicioId;
    
    @Column(name = "orden")
    private Integer orden;
    
    @Column(name = "series")
    private Integer series;
    
    @Column(name = "repeticiones")
    private Integer repeticiones;
    
    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;
    
    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;
    
    @Column(name = "peso_kg")
    private Double pesoKg;
    
    @Column(name = "notas", length = 500)
    private String notas;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", insertable = false, updatable = false)
    private Rutina rutina;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id", insertable = false, updatable = false)
    private EjercicioCatalogo ejercicio;
    
    // Métodos de utilidad
    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder();
        
        if (series != null && repeticiones != null) {
            desc.append(series).append(" x ").append(repeticiones);
        }
        
        if (duracionSegundos != null && duracionSegundos > 0) {
            desc.append(" (").append(duracionSegundos).append("s)");
        }
        
        if (pesoKg != null && pesoKg > 0) {
            desc.append(" - ").append(pesoKg).append(" kg");
        }
        
        return desc.toString();
    }
    
    public Integer getDuracionTotalSegundos() {
        int total = 0;
        
        if (duracionSegundos != null) {
            total += duracionSegundos;
        } else if (series != null && repeticiones != null) {
            // Estimación: 2 segundos por repetición
            total += series * repeticiones * 2;
        }
        
        if (descansoSegundos != null && series != null && series > 1) {
            total += descansoSegundos * (series - 1);
        }
        
        return total;
    }
}
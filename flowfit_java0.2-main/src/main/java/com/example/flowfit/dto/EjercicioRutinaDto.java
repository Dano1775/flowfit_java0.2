package com.example.flowfit.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para mostrar ejercicios de rutina con detalles del catálogo
 * Se usa para evitar problemas de lazy loading y simplificar la presentación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioRutinaDto {
    
    private Integer rutinaId;
    private Integer ejercicioId;
    private Integer orden;
    private Integer series;
    private Integer repeticiones;
    private Integer duracionSegundos;
    private Integer descansoSegundos;
    private Double pesoKg;
    private String notas;
    
    // Datos del ejercicio del catálogo
    private String ejercicioNombre;
    private String ejercicioDescripcion;
    private String ejercicioImagen;
    
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
    
    public boolean hasImage() {
        return ejercicioImagen != null && !ejercicioImagen.trim().isEmpty();
    }
}
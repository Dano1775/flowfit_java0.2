package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "rutina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rutina {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "entrenador_id")
    private Integer entrenadorId;
    
    // @Column(name = "fecha_creacion")
    // private LocalDate fechaCreacion;
    
    @Transient
    private LocalDate fechaCreacion;
    
    // Campos adicionales calculados
    @Transient
    private Integer duracionMinutos;
    
    @Transient
    private Integer caloriasEstimadas;
    
    @Transient
    private String dificultad;
    
    @Transient
    private String categoria;
    
    // Relación con ejercicios (opcional, para futuro)
    @OneToMany(mappedBy = "rutinaId", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<RutinaEjercicio> ejercicios;
    
    // Relación con entrenador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private Usuario entrenador;
    
    // Métodos de utilidad
    public boolean esRutinaGlobal() {
        return entrenadorId == null;
    }
    
    public boolean esRutinaPersonalizada() {
        return entrenadorId != null;
    }
    
    // Getters y setters adicionales para campos calculados
    public Integer getDuracionMinutos() {
        if (duracionMinutos == null) {
            // Calcular basado en ejercicios o usar valor por defecto
            return calcularDuracion();
        }
        return duracionMinutos;
    }
    
    public Integer getCaloriasEstimadas() {
        if (caloriasEstimadas == null) {
            return calcularCalorias();
        }
        return caloriasEstimadas;
    }
    
    public String getDificultad() {
        if (dificultad == null) {
            return calcularDificultad();
        }
        return dificultad;
    }
    
    private Integer calcularDuracion() {
        // Lógica básica - se puede mejorar con ejercicios reales
        if (nombre.toLowerCase().contains("hiit")) return 30;
        if (nombre.toLowerCase().contains("cardio")) return 35;
        if (nombre.toLowerCase().contains("fuerza")) return 45;
        if (nombre.toLowerCase().contains("yoga") || nombre.toLowerCase().contains("flexibilidad")) return 25;
        return 40; // valor por defecto
    }
    
    private Integer calcularCalorias() {
        Integer duracion = getDuracionMinutos();
        // Estimación aproximada: 8 calorías por minuto promedio
        return duracion * 8;
    }
    
    private String calcularDificultad() {
        if (nombre.toLowerCase().contains("principiante") || nombre.toLowerCase().contains("básico")) return "Principiante";
        if (nombre.toLowerCase().contains("avanzado") || nombre.toLowerCase().contains("intenso")) return "Avanzado";
        return "Intermedio";
    }
}
package com.example.flowfit.dto;

public class EjercicioRutinaSimpleDto {
    private Integer ejercicioId;
    private Integer sets;
    private Integer repeticiones;

    // Constructors
    public EjercicioRutinaSimpleDto() {}

    public EjercicioRutinaSimpleDto(Integer ejercicioId, Integer sets, Integer repeticiones) {
        this.ejercicioId = ejercicioId;
        this.sets = sets;
        this.repeticiones = repeticiones;
    }

    // Getters and Setters
    public Integer getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(Integer ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }
}
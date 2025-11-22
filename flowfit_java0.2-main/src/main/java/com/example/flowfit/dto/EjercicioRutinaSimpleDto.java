package com.example.flowfit.dto;

public class EjercicioRutinaSimpleDto {
    private Integer ejercicioId;
    private Integer sets;
    private Integer repeticiones;
    private Integer duracionSegundos;
    private Integer descansoSegundos;
    private String notas;

    // Constructors
    public EjercicioRutinaSimpleDto() {}

    public EjercicioRutinaSimpleDto(Integer ejercicioId, Integer sets, Integer repeticiones) {
        this.ejercicioId = ejercicioId;
        this.sets = sets;
        this.repeticiones = repeticiones;
    }

    public EjercicioRutinaSimpleDto(Integer ejercicioId, Integer sets, Integer repeticiones, 
                                    Integer duracionSegundos, Integer descansoSegundos, 
                                    String notas) {
        this.ejercicioId = ejercicioId;
        this.sets = sets;
        this.repeticiones = repeticiones;
        this.duracionSegundos = duracionSegundos;
        this.descansoSegundos = descansoSegundos;
        this.notas = notas;
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

    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public Integer getDescansoSegundos() {
        return descansoSegundos;
    }

    public void setDescansoSegundos(Integer descansoSegundos) {
        this.descansoSegundos = descansoSegundos;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
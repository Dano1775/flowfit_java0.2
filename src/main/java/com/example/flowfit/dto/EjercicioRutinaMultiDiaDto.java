package com.example.flowfit.dto;

import java.util.List;

public class EjercicioRutinaMultiDiaDto {
    private Integer ejercicioId;
    private Integer sets;
    private Integer repeticiones;
    private Integer duracionSegundos;
    private Integer descansoSegundos;
    private String notas;
    private List<Integer> diasOrdenes;

    public EjercicioRutinaMultiDiaDto() {
    }

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

    public List<Integer> getDiasOrdenes() {
        return diasOrdenes;
    }

    public void setDiasOrdenes(List<Integer> diasOrdenes) {
        this.diasOrdenes = diasOrdenes;
    }
}

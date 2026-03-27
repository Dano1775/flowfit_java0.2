package com.example.flowfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "rutina_ejercicio_dia")
@IdClass(RutinaEjercicioDia.RutinaEjercicioDiaId.class)
public class RutinaEjercicioDia {

    @Id
    @Column(name = "rutina_id", nullable = false)
    private Integer rutinaId;

    @Id
    @Column(name = "ejercicio_id", nullable = false)
    private Integer ejercicioId;

    @Id
    @Column(name = "dia_orden", nullable = false)
    private Integer diaOrden;

    public RutinaEjercicioDia() {
    }

    public RutinaEjercicioDia(Integer rutinaId, Integer ejercicioId, Integer diaOrden) {
        this.rutinaId = rutinaId;
        this.ejercicioId = ejercicioId;
        this.diaOrden = diaOrden;
    }

    public Integer getRutinaId() {
        return rutinaId;
    }

    public void setRutinaId(Integer rutinaId) {
        this.rutinaId = rutinaId;
    }

    public Integer getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(Integer ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public Integer getDiaOrden() {
        return diaOrden;
    }

    public void setDiaOrden(Integer diaOrden) {
        this.diaOrden = diaOrden;
    }

    public static class RutinaEjercicioDiaId implements Serializable {
        private Integer rutinaId;
        private Integer ejercicioId;
        private Integer diaOrden;

        public RutinaEjercicioDiaId() {
        }

        public RutinaEjercicioDiaId(Integer rutinaId, Integer ejercicioId, Integer diaOrden) {
            this.rutinaId = rutinaId;
            this.ejercicioId = ejercicioId;
            this.diaOrden = diaOrden;
        }

        public Integer getRutinaId() {
            return rutinaId;
        }

        public void setRutinaId(Integer rutinaId) {
            this.rutinaId = rutinaId;
        }

        public Integer getEjercicioId() {
            return ejercicioId;
        }

        public void setEjercicioId(Integer ejercicioId) {
            this.ejercicioId = ejercicioId;
        }

        public Integer getDiaOrden() {
            return diaOrden;
        }

        public void setDiaOrden(Integer diaOrden) {
            this.diaOrden = diaOrden;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RutinaEjercicioDiaId that = (RutinaEjercicioDiaId) o;
            if (rutinaId == null ? that.rutinaId != null : !rutinaId.equals(that.rutinaId)) {
                return false;
            }
            if (ejercicioId == null ? that.ejercicioId != null : !ejercicioId.equals(that.ejercicioId)) {
                return false;
            }
            return diaOrden == null ? that.diaOrden == null : diaOrden.equals(that.diaOrden);
        }

        @Override
        public int hashCode() {
            int result = rutinaId != null ? rutinaId.hashCode() : 0;
            result = 31 * result + (ejercicioId != null ? ejercicioId.hashCode() : 0);
            result = 31 * result + (diaOrden != null ? diaOrden.hashCode() : 0);
            return result;
        }
    }
}

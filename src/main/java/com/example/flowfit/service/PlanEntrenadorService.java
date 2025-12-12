package com.example.flowfit.service;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PlanEntrenadorService {

    @Autowired
    private PlanEntrenadorRepository planRepo;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    /**
     * Crear nuevo plan
     */
    @Transactional
    public PlanEntrenador crearPlan(PlanEntrenador plan) {
        return planRepo.save(plan);
    }

    /**
     * Actualizar plan existente
     */
    @Transactional
    public PlanEntrenador actualizarPlan(Integer planId, PlanEntrenador planActualizado) {
        PlanEntrenador plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        plan.setNombre(planActualizado.getNombre());
        plan.setDescripcion(planActualizado.getDescripcion());
        plan.setPrecioMensual(planActualizado.getPrecioMensual());
        plan.setDuracionDias(planActualizado.getDuracionDias());
        plan.setRutinasMes(planActualizado.getRutinasMes());
        plan.setSeguimientoSemanal(planActualizado.getSeguimientoSemanal());
        plan.setChatDirecto(planActualizado.getChatDirecto());
        plan.setVideollamadasMes(planActualizado.getVideollamadasMes());
        plan.setPlanNutricional(planActualizado.getPlanNutricional());
        plan.setEsPublico(planActualizado.getEsPublico());
        plan.setDestacado(planActualizado.getDestacado());

        return planRepo.save(plan);
    }

    /**
     * Activar/desactivar plan
     */
    @Transactional
    public void cambiarEstadoPlan(Integer planId, boolean activo) {
        PlanEntrenador plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        plan.setActivo(activo);
        planRepo.save(plan);
    }

    /**
     * Obtener planes activos de un entrenador
     */
    public List<PlanEntrenador> obtenerPlanesActivos(Integer entrenadorId) {
        return planRepo.findByEntrenadorIdAndActivoTrue(entrenadorId);
    }

    /**
     * Obtener planes públicos de un entrenador (para que los usuarios los vean)
     */
    public List<PlanEntrenador> obtenerPlanesPublicos(Integer entrenadorId) {
        return planRepo.findPlanesPublicosByEntrenador(entrenadorId);
    }

    /**
     * Obtener todos los planes de un entrenador
     */
    public List<PlanEntrenador> obtenerTodosPlanes(Integer entrenadorId) {
        return planRepo.findByEntrenadorId(entrenadorId);
    }

    /**
     * Obtener plan por ID
     */
    public PlanEntrenador obtenerPlan(Integer planId) {
        return planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
    }

    /**
     * Eliminar plan (solo si no tiene contrataciones activas)
     */
    @Transactional
    public void eliminarPlan(Integer planId) {
        Long clientesActivos = planRepo.contarClientesActivos(planId);

        if (clientesActivos > 0) {
            throw new RuntimeException("No puedes eliminar un plan con clientes activos");
        }

        planRepo.deleteById(planId);
    }

    /**
     * Obtener estadísticas de un plan
     */
    public Map<String, Object> obtenerEstadisticasPlan(Integer planId) {
        Map<String, Object> stats = new HashMap<>();

        Long clientesActivos = planRepo.contarClientesActivos(planId);
        stats.put("clientesActivos", clientesActivos);

        // Calcular ingresos mensuales estimados
        PlanEntrenador plan = obtenerPlan(planId);
        BigDecimal ingresosMensual = plan.getPrecioMensual()
                .multiply(BigDecimal.valueOf(clientesActivos))
                .multiply(BigDecimal.valueOf(0.90)); // 90% después de comisión 10%

        stats.put("ingresosMensualesEstimados", ingresosMensual);
        stats.put("precioBase", plan.getPrecioMensual());

        return stats;
    }

    /**
     * Obtener todos los planes públicos y activos (para marketplace de usuarios)
     */
    public List<PlanEntrenador> obtenerTodosPlanesPublicos() {
        return planRepo.findPlanesPublicosActivos();
    }

    /**
     * Reasignar todos los clientes de un plan a otro
     */
    @Transactional
    public int reasignarClientesDePlan(Integer planIdActual, Integer planIdNuevo) {
        // Verificar que ambos planes existan
        PlanEntrenador planActual = planRepo.findById(planIdActual)
                .orElseThrow(() -> new RuntimeException("Plan actual no encontrado"));

        PlanEntrenador planNuevo = planRepo.findById(planIdNuevo)
                .orElseThrow(() -> new RuntimeException("Plan nuevo no encontrado"));

        // Verificar que el plan nuevo esté activo
        if (!planNuevo.getActivo()) {
            throw new RuntimeException("El plan de destino debe estar activo");
        }

        // Obtener todas las contrataciones activas del plan actual
        List<ContratacionEntrenador> contrataciones = contratacionRepo.findByPlanIdAndEstadoNot(
                planIdActual,
                ContratacionEntrenador.EstadoContratacion.CANCELADA);

        // Reasignar cada contratación al nuevo plan
        int contadorReasignados = 0;
        for (ContratacionEntrenador contratacion : contrataciones) {
            // Solo cambiar el ID del plan, no la relación completa
            contratacion.setPlanBaseId(planIdNuevo);
            contratacion.setPlanBase(null); // Limpiar la relación para evitar conflictos
            contratacionRepo.save(contratacion);
            contadorReasignados++;
        }

        return contadorReasignados;
    }
}

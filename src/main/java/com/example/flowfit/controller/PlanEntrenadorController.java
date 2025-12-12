package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.service.PlanEntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

/**
 * Controlador para gestionar los planes de entrenamiento que ofrece un
 * entrenador
 */
@Controller
@RequestMapping("/entrenador")
public class PlanEntrenadorController {

    @Autowired
    private PlanEntrenadorService planService;

    /**
     * Vista de gestión de planes del entrenador
     */
    @GetMapping("/mis-planes")
    public String misPlanes(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
            return "redirect:/login";
        }

        System.out.println("🔍 DEBUG - Buscando planes del entrenador ID: " + usuario.getId());
        List<PlanEntrenador> planes = planService.obtenerTodosPlanes(usuario.getId());
        System.out.println("📊 DEBUG - Planes encontrados: " + planes.size());

        // Filtro adicional por seguridad (solo planes del entrenador actual)
        planes.removeIf(plan -> plan.getEntrenadorId() == null || !plan.getEntrenadorId().equals(usuario.getId()));
        System.out.println("✅ DEBUG - Planes después del filtro: " + planes.size());

        // Agregar estadísticas a cada plan
        for (PlanEntrenador plan : planes) {
            Map<String, Object> stats = planService.obtenerEstadisticasPlan(plan.getId());
            plan.setClientesActivos(((Long) stats.get("clientesActivos")).intValue());
        }

        model.addAttribute("planes", planes);
        model.addAttribute("entrenador", usuario);
        model.addAttribute("currentPage", "mis-planes");

        return "Entrenador/mis-planes";
    }

    /**
     * Vista de planes disponibles para usuarios (marketplace híbrido)
     */
    @GetMapping("/planes/explorar")
    public String explorarPlanes(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener todos los planes públicos y activos con entrenadores
        List<PlanEntrenador> planesDisponibles = planService.obtenerTodosPlanesPublicos();

        model.addAttribute("planes", planesDisponibles);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentPage", "explorar-planes");

        return "usuario/explorar-planes-nuevo";
    }

    /**
     * Crear nuevo plan (Sistema Híbrido)
     */
    @PostMapping("/planes/crear")
    public String crearPlan(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam BigDecimal precio,
            @RequestParam(required = false) BigDecimal rangoPrecioMin,
            @RequestParam(required = false) BigDecimal rangoPrecioMax,
            @RequestParam Integer duracionDias,
            @RequestParam(required = false) Integer rutinasMes,
            @RequestParam(defaultValue = "false") Boolean seguimientoSemanal,
            @RequestParam(defaultValue = "true") Boolean chatDirecto,
            @RequestParam(defaultValue = "0") Integer videollamadasMes,
            @RequestParam(defaultValue = "false") Boolean planNutricional,
            @RequestParam(defaultValue = "true") Boolean esPublico,
            @RequestParam(defaultValue = "true") Boolean permitePersonalizacion,
            @RequestParam(defaultValue = "false") Boolean destacado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                return "redirect:/login";
            }

            PlanEntrenador plan = new PlanEntrenador();
            plan.setEntrenadorId(usuario.getId());
            plan.setNombre(nombre);
            plan.setDescripcion(descripcion);
            plan.setPrecioMensual(precio);

            // Calcular rangos de precio si permite personalización
            if (permitePersonalizacion) {
                plan.setRangoPrecioMin(
                        rangoPrecioMin != null ? rangoPrecioMin : precio.multiply(new BigDecimal("0.70")));
                plan.setRangoPrecioMax(
                        rangoPrecioMax != null ? rangoPrecioMax : precio.multiply(new BigDecimal("1.30")));
            }

            plan.setDuracionDias(duracionDias);
            plan.setRutinasMes(rutinasMes);
            plan.setSeguimientoSemanal(seguimientoSemanal);
            plan.setChatDirecto(chatDirecto);
            plan.setVideollamadasMes(videollamadasMes);
            plan.setPlanNutricional(planNutricional);
            plan.setEsPublico(esPublico);
            plan.setPermitePersonalizacion(permitePersonalizacion);
            plan.setDestacado(destacado);

            planService.crearPlan(plan);

            redirectAttributes.addFlashAttribute("success", "Plan creado exitosamente" +
                    (permitePersonalizacion ? " (con opciones de personalización)" : " (precio fijo)"));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear plan: " + e.getMessage());
        }

        return "redirect:/entrenador/mis-planes";
    }

    /**
     * Actualizar plan existente
     */
    @PostMapping("/planes/actualizar/{planId}")
    public String actualizarPlan(
            @PathVariable Integer planId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam BigDecimal precio,
            @RequestParam Integer duracionDias,
            @RequestParam(required = false) Integer rutinasMes,
            @RequestParam(defaultValue = "false") Boolean seguimientoSemanal,
            @RequestParam(defaultValue = "true") Boolean chatDirecto,
            @RequestParam(defaultValue = "0") Integer videollamadasMes,
            @RequestParam(defaultValue = "false") Boolean planNutricional,
            @RequestParam(defaultValue = "true") Boolean esPublico,
            @RequestParam(defaultValue = "false") Boolean destacado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                return "redirect:/login";
            }

            PlanEntrenador planActualizado = new PlanEntrenador();
            planActualizado.setNombre(nombre);
            planActualizado.setDescripcion(descripcion);
            planActualizado.setPrecioMensual(precio);
            planActualizado.setDuracionDias(duracionDias);
            planActualizado.setRutinasMes(rutinasMes);
            planActualizado.setSeguimientoSemanal(seguimientoSemanal);
            planActualizado.setChatDirecto(chatDirecto);
            planActualizado.setVideollamadasMes(videollamadasMes);
            planActualizado.setPlanNutricional(planNutricional);
            planActualizado.setEsPublico(esPublico);
            planActualizado.setDestacado(destacado);

            planService.actualizarPlan(planId, planActualizado);

            redirectAttributes.addFlashAttribute("success", "Plan actualizado exitosamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar plan: " + e.getMessage());
        }

        return "redirect:/entrenador/mis-planes";
    }

    /**
     * Cambiar estado del plan (activar/desactivar)
     */
    @PostMapping("/planes/cambiar-estado/{planId}")
    public String cambiarEstadoPlan(
            @PathVariable Integer planId,
            @RequestParam Boolean activo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                return "redirect:/login";
            }

            planService.cambiarEstadoPlan(planId, activo);

            redirectAttributes.addFlashAttribute("success",
                    activo ? "Plan activado" : "Plan desactivado");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al cambiar estado: " + e.getMessage());
        }

        return "redirect:/entrenador/mis-planes";
    }

    /**
     * Reasignar clientes de un plan a otro antes de eliminarlo
     */
    @PostMapping("/planes/reasignar/{planIdActual}/{planIdNuevo}")
    public String reasignarClientesPlan(
            @PathVariable Integer planIdActual,
            @PathVariable Integer planIdNuevo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                return "redirect:/login";
            }

            // Reasignar clientes
            int clientesReasignados = planService.reasignarClientesDePlan(planIdActual, planIdNuevo);

            // Ahora eliminar el plan antiguo
            planService.eliminarPlan(planIdActual);

            redirectAttributes.addFlashAttribute("success",
                    "Plan eliminado exitosamente. " + clientesReasignados + " cliente(s) reasignado(s) al nuevo plan.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al reasignar clientes: " + e.getMessage());
        }

        return "redirect:/entrenador/mis-planes";
    }

    /**
     * Eliminar plan
     */
    @PostMapping("/planes/eliminar/{planId}")
    public String eliminarPlan(
            @PathVariable Integer planId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                return "redirect:/login";
            }

            // Verificar si tiene clientes activos
            Map<String, Object> stats = planService.obtenerEstadisticasPlan(planId);
            Long clientesActivos = (Long) stats.get("clientesActivos");

            if (clientesActivos > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "No puedes eliminar este plan porque tiene " + clientesActivos
                                + " cliente(s) activo(s). Reasígnalos primero.");
                return "redirect:/entrenador/mis-planes";
            }

            planService.eliminarPlan(planId);

            redirectAttributes.addFlashAttribute("success", "Plan eliminado exitosamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar plan: " + e.getMessage());
        }

        return "redirect:/entrenador/mis-planes";
    }
}

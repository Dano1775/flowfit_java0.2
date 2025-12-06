package com.example.flowfit.controller;

import com.example.flowfit.model.ContratacionEntrenador;
import com.example.flowfit.model.PagoContratacion;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.ContratacionEntrenadorRepository;
import com.example.flowfit.repository.PagoContratacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * Controlador para manejar las URLs de retorno de MercadoPago
 */
@Controller
@RequestMapping("/pagos")
public class PagosController {

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private PagoContratacionRepository pagoRepo;

    /**
     * Página de éxito - Pago aprobado
     */
    @GetMapping("/success")
    public String pagoExitoso(
            @RequestParam(required = false) Long contratacion_id,
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        System.out.println("=== PAGO EXITOSO ===");
        System.out.println("Contratación ID: " + contratacion_id);
        System.out.println("Payment ID: " + payment_id);
        System.out.println("Status: " + status);
        System.out.println("External Reference: " + external_reference);

        try {
            // Buscar contratación
            Long contratacionId = contratacion_id != null ? contratacion_id
                    : (external_reference != null ? Long.parseLong(external_reference) : null);

            if (contratacionId != null) {
                ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                        .orElse(null);

                if (contratacion != null) {
                    // Actualizar estado a ACTIVA
                    contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.ACTIVA);
                    contratacion.setFechaInicio(LocalDateTime.now());
                    contratacion.setFechaFin(LocalDateTime.now().plusDays(contratacion.getDuracionDiasAcordada()));
                    contratacionRepo.save(contratacion);

                    // Buscar o crear registro de pago
                    PagoContratacion pago = pagoRepo.findByContratacionId(contratacionId)
                            .orElse(new PagoContratacion());

                    pago.setContratacionId(contratacionId);
                    pago.setUsuarioId(usuario.getId());
                    pago.setMonto(contratacion.getPrecioAcordado());
                    pago.setMpPaymentId(payment_id);
                    pago.setMpStatus("approved");
                    pago.setEstadoPago(PagoContratacion.EstadoPago.APROBADO);
                    pago.setFechaPago(LocalDateTime.now());

                    // Activar ESCROW
                    pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.RETENIDO);
                    pago.setFechaLimiteDisputa(LocalDateTime.now().plusDays(7));

                    pagoRepo.save(pago);

                    model.addAttribute("contratacion", contratacion);
                    model.addAttribute("pago", pago);
                    model.addAttribute("mensaje", "¡Pago completado exitosamente!");

                    System.out.println("✅ Pago procesado correctamente - Contratación ACTIVA");
                    return "pagos/success";
                }
            }

            model.addAttribute("mensaje", "Pago procesado, pero no se encontró la contratación.");
            return "pagos/success";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "pagos/success";
        }
    }

    /**
     * Página de fallo - Pago rechazado
     */
    @GetMapping("/failure")
    public String pagoFallido(
            @RequestParam(required = false) Long contratacion_id,
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        System.out.println("=== PAGO FALLIDO ===");
        System.out.println("Contratación ID: " + contratacion_id);
        System.out.println("Payment ID: " + payment_id);
        System.out.println("Status: " + status);

        model.addAttribute("contratacionId", contratacion_id);
        model.addAttribute("mensaje", "El pago no pudo ser procesado.");

        return "pagos/failure";
    }

    /**
     * Página de pendiente - Pago en proceso
     */
    @GetMapping("/pending")
    public String pagoPendiente(
            @RequestParam(required = false) Long contratacion_id,
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        System.out.println("=== PAGO PENDIENTE ===");
        System.out.println("Contratación ID: " + contratacion_id);
        System.out.println("Payment ID: " + payment_id);
        System.out.println("Status: " + status);

        try {
            if (contratacion_id != null) {
                ContratacionEntrenador contratacion = contratacionRepo.findById(contratacion_id)
                        .orElse(null);

                if (contratacion != null) {
                    // Mantener en PENDIENTE_PAGO
                    model.addAttribute("contratacion", contratacion);
                }
            }

            model.addAttribute("contratacionId", contratacion_id);
            model.addAttribute("mensaje", "Tu pago está siendo procesado.");

            return "pagos/pending";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            return "pagos/pending";
        }
    }
}

package com.example.flowfit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flowfit.service.UsuarioGroupService;

@Controller
@RequestMapping("/admin")
public class UsuarioGroupController {

    private final UsuarioGroupService usuarioGroupService;

    public UsuarioGroupController(UsuarioGroupService usuarioGroupService) {
        this.usuarioGroupService = usuarioGroupService;
    }

    @PostMapping("/registro_masivo")
    public String subirArchivo(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            int registrados = usuarioGroupService.procesarArchivo(file);
            redirectAttributes.addFlashAttribute("mensaje", "\u2705 Se registraron " + registrados + " usuarios correctamente.");
            // Añadir nombre de archivo para confirmación en la UI
            redirectAttributes.addFlashAttribute("archivo", file != null ? file.getOriginalFilename() : "(sin nombre)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u274C Error al procesar el archivo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("archivo", file != null ? file.getOriginalFilename() : "(sin nombre)");
        }
        return "redirect:/admin/dashboard";
    }

    /**
     * Endpoint JSON para subir el CSV vía AJAX y recibir confirmación inmediata.
     */
    @PostMapping(value = "/registro_masivo_ajax", produces = "application/json")
    @ResponseBody
    public java.util.Map<String, Object> subirArchivoAjax(@RequestParam("file") MultipartFile file) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        try {
            int registrados = usuarioGroupService.procesarArchivo(file);
            resp.put("success", true);
            resp.put("mensaje", "Se registraron " + registrados + " usuarios correctamente.");
            resp.put("archivo", file != null ? file.getOriginalFilename() : "(sin nombre)");
            resp.put("registrados", registrados);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", "Error al procesar el archivo: " + e.getMessage());
            resp.put("archivo", file != null ? file.getOriginalFilename() : "(sin nombre)");
        }
        return resp;
    }
}

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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u274C Error al procesar el archivo: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}

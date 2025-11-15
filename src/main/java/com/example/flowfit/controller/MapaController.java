package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador explícito para la vista de mapa (ruta /usuario/mapa)
 * Añadido como fallback en caso de que el mapeo original no se resuelva.
 */
@Controller
public class MapaController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuario/mapa")
    public String mapa(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        usuario = usuarioService.findById(usuario.getId()).orElse(usuario);
        model.addAttribute("usuario", usuario);
        return "usuario/mapa";
    }
}

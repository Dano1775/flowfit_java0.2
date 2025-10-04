package com.example.flowfit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/editar-perfil")
    public String mostrarEditarPerfil(HttpSession session, Model model) {
        // Verificar si el usuario está logueado
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        return "editar-perfil";
    }

    @PostMapping("/editar-perfil")
    public String procesarEditarPerfil(
            @RequestParam("nombre") String nombre,
            @RequestParam("numero_documento") String numeroDocumento,
            @RequestParam("telefono") String telefono,
            @RequestParam("correo") String correo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Verificar si el usuario está logueado
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        try {
            Integer usuarioId = (Integer) session.getAttribute("usuarioId");
            Usuario usuario = usuarioService.buscarPorId(usuarioId);
            
            if (usuario == null) {
                return "redirect:/login";
            }

            // Actualizar los datos del usuario
            usuario.setNombre(nombre.trim());
            usuario.setNumeroDocumento(numeroDocumento.trim());
            usuario.setTelefono(telefono.trim());
            usuario.setCorreo(correo.trim());

            usuarioService.guardarUsuario(usuario);
            
            // Actualizar el nombre en la sesión
            session.setAttribute("nombre", usuario.getNombre());

            redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente");
            return "redirect:/editar-perfil?exito=1";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
            return "redirect:/editar-perfil?error=1";
        }
    }

    @GetMapping("/configuracion")
    public String mostrarConfiguracion(HttpSession session, Model model) {
        // Verificar si el usuario está logueado
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        return "configuracion";
    }
}
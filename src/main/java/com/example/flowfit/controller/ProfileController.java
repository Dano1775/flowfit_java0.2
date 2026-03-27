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

    @GetMapping({ "/editar-perfil", "/perfil/editar" })
    public String mostrarEditarPerfil(HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.buscarPorId(usuarioSesion.getId());
        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("esUsuario", "Usuario".equalsIgnoreCase(String.valueOf(usuario.getPerfilUsuario())));
        return "editar-perfil";
    }

    @PostMapping({ "/editar-perfil", "/perfil/editar" })
    public String procesarEditarPerfil(
            @RequestParam("nombre") String nombre,
            @RequestParam("numero_documento") String numeroDocumento,
            @RequestParam("telefono") String telefono,
            @RequestParam("correo") String correo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        try {
            Usuario usuario = usuarioService.buscarPorId(usuarioSesion.getId());

            if (usuario == null) {
                return "redirect:/login";
            }

            // Actualizar los datos del usuario
            usuario.setNombre(nombre.trim());
            usuario.setNumeroDocumento(numeroDocumento.trim());
            usuario.setTelefono(telefono.trim());
            usuario.setCorreo(correo.trim());

            usuarioService.guardarUsuario(usuario);

            // refrescar usuario en sesión para reflejar cambios
            session.setAttribute("usuario", usuario);

            redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente");
            return "redirect:/perfil/editar?exito=1";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
            return "redirect:/perfil/editar?error=1";
        }
    }

    @GetMapping("/configuracion")
    public String mostrarConfiguracion(HttpSession session, Model model) {
        // Verificar si el usuario está logueado
        if (session.getAttribute("id") == null) {
            return "redirect:/login";
        }

        return "configuracion";
    }
}
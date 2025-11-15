package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Mostrar página principal/índice
     */
    @GetMapping("/")
    public String index() {
        return "index"; // retorna templates/index.html
    }

    /**
     * Mostrar página de inicio de sesión
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "success", required = false) String success,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "login"; // retorna templates/login.html
    }

    /**
     * Procesar inicio de sesión - misma lógica que PHP login_controller.php
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam("correo") String correo,
                              @RequestParam("clave") String clave,
                              HttpSession session,
                              Model model) {
        
        // Authenticate user using service (same logic as PHP)
        UsuarioService.LoginResult result = usuarioService.login(correo.trim(), clave.trim());
        
        if (!result.isSuccess()) {
            // Login failed - return to login page with error
            model.addAttribute("error", result.getMessage());
            model.addAttribute("correo", correo); // Keep email in form
            return "login";
        }
        
        // Login successful - create session (same as PHP $_SESSION)
        Usuario usuario = result.getUsuario();
        session.setAttribute("id", usuario.getId());
        session.setAttribute("perfil", usuario.getPerfilUsuario().toString());
        session.setAttribute("nombre", usuario.getNombre());
        session.setAttribute("usuario", usuario); // Store full user object
        
        // Redirect based on user profile (same as PHP switch statement)
        String redirectUrl = usuarioService.getRedirectUrl(usuario.getPerfilUsuario());
        return "redirect:" + redirectUrl;
    }


    /**
     * Cerrar sesión - limpiar sesión
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?success=Sesión cerrada exitosamente";
    }
}
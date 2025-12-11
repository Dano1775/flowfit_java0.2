package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.dto.EjercicioRutinaDto;
import com.example.flowfit.dto.RutinaDetalleDto;
import com.example.flowfit.dto.RutinaAsignadaDto;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.RutinaService;
import com.example.flowfit.service.AsignacionEntrenadorService;
import com.example.flowfit.model.AsignacionEntrenador;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador para gestionar las páginas del usuario regular
 * Maneja dashboard, rutinas, ejercicios, progreso y perfil del usuario
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private AsignacionEntrenadorService asignacionService;

    @Autowired
    private com.example.flowfit.service.ProgresoService progresoService;

    @Autowired
    private com.example.flowfit.service.PdfService pdfService;

    /**
     * Dashboard principal del usuario
     * Muestra resumen de actividad, estadísticas personales y accesos rápidos
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        try {
            // Verificar sesión de usuario
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            // Verificar que es un usuario regular (no admin)
            if (usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/admin/dashboard";
            }

            // Cargar datos del usuario actualizado desde BD
            usuario = usuarioService.findById(usuario.getId()).orElse(usuario);

            model.addAttribute("usuario", usuario);

            // Cargar estadísticas del dashboard
            List<RutinaAsignada> rutinasAsignadas = rutinaService.obtenerRutinasAsignadas(usuario.getId());
            Double progresoGeneral = rutinaService.calcularProgresoGeneralUsuario(usuario.getId());

            // Estadísticas de rutinas
            long rutinasCompletadas = rutinasAsignadas.stream()
                    .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.COMPLETADA)
                    .count();
            long rutinasEnProgreso = rutinasAsignadas.stream()
                    .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.ACTIVA)
                    .count();
            long rutinasTotal = rutinasAsignadas.size();

            // Datos adicionales para el dashboard
            int diasActivosEsteMes = rutinaService.contarDiasActivosEsteMes(usuario.getId());
            int rachaActual = rutinaService.calcularRachaActual(usuario.getId());

            model.addAttribute("rutinasAsignadas", rutinasAsignadas);
            model.addAttribute("progresoGeneral", progresoGeneral);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            model.addAttribute("rutinasEnProgreso", rutinasEnProgreso);
            model.addAttribute("rutinasActivas", rutinasEnProgreso); // Alias para consistencia con template
            model.addAttribute("rutinasTotal", rutinasTotal);
            model.addAttribute("diasActivosEsteMes", diasActivosEsteMes);
            model.addAttribute("rachaActual", rachaActual);
            model.addAttribute("currentPage", "dashboard");

            return "usuario/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Página de rutinas del usuario
     * Muestra rutinas asignadas, completadas y disponibles
     */
    @GetMapping("/rutinas")
    public String rutinas(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                // TEMPORAL: Para testing del modal, usar usuario de prueba
                List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
                if (!todosUsuarios.isEmpty()) {
                    usuario = todosUsuarios.stream()
                            .filter(u -> u.getPerfilUsuario() == Usuario.PerfilUsuario.Usuario)
                            .findFirst()
                            .orElse(todosUsuarios.get(0));
                    session.setAttribute("usuario", usuario);
                    System.out.println("TESTING: Usuario temporal agregado a sesión: " + usuario.getNombre());
                } else {
                    return "redirect:/login";
                }
            } else if (usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            model.addAttribute("usuario", usuario);

            // Cargar rutinas del usuario
            List<RutinaAsignada> rutinasActivas = rutinaService.obtenerRutinasActivasDelUsuario(usuario.getId());
            List<RutinaAsignada> rutinasCompletadas = rutinaService
                    .obtenerRutinasCompletadasDelUsuario(usuario.getId());
            List<Rutina> rutinasDisponibles = rutinaService.obtenerRutinasGlobales();
            List<Rutina> rutinasPopulares = rutinaService.obtenerRutinasPopulares(6);

            // Obtener rutinas asignadas por el entrenador del usuario
            List<RutinaAsignada> rutinasAsignadasPorEntrenador = rutinaService.obtenerRutinasAsignadas(usuario.getId())
                    .stream()
                    .filter(ra -> ra.getRutina() != null && ra.getRutina().getEntrenadorId() != null)
                    .toList();

            // Estadísticas del usuario
            Double progresoGeneral = rutinaService.calcularProgresoGeneralUsuario(usuario.getId());

            // Procesar estadísticas de forma más simple y segura
            int totalRutinas = rutinasActivas.size() + rutinasCompletadas.size();
            int rutinasCompletadasCount = rutinasCompletadas.size();
            int rutinasActivasCount = rutinasActivas.size();
            int rutinasEntrenadorCount = rutinasAsignadasPorEntrenador.size();

            model.addAttribute("rutinasActivas", rutinasActivas);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            model.addAttribute("rutinasDisponibles", rutinasDisponibles);
            model.addAttribute("rutinasPopulares", rutinasPopulares);
            model.addAttribute("rutinasAsignadasPorEntrenador", rutinasAsignadasPorEntrenador);
            model.addAttribute("totalRutinas", totalRutinas);
            model.addAttribute("rutinasCompletadasCount", rutinasCompletadasCount);
            model.addAttribute("rutinasActivasCount", rutinasActivasCount);
            model.addAttribute("rutinasEntrenadorCount", rutinasEntrenadorCount);
            model.addAttribute("progresoGeneral", progresoGeneral);
            model.addAttribute("currentPage", "rutinas");

            return "usuario/rutinas";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar rutinas");
            return "usuario/dashboard";
        }
    }

    /*
     * Página de ejercicios disponibles para el usuario
     * Muestra catálogo de ejercicios con filtros y favoritos
     * TEMPORALMENTE DESHABILITADO
     */
    /*
     * @GetMapping("/ejercicios")
     * public String ejercicios(Model model, HttpSession session) {
     * try {
     * Usuario usuario = (Usuario) session.getAttribute("usuario");
     * if (usuario == null ||
     * usuario.getPerfilUsuario().toString().equals("Administrador")) {
     * return "redirect:/login";
     * }
     * 
     * // Cargar todos los ejercicios disponibles
     * List<EjercicioCatalogo> ejercicios = ejercicioService.getAllEjercicios();
     * 
     * model.addAttribute("usuario", usuario);
     * model.addAttribute("ejercicios", ejercicios);
     * 
     * return "usuario/ejercicios";
     * } catch (Exception e) {
     * e.printStackTrace();
     * model.addAttribute("error", "Error al cargar ejercicios");
     * return "usuario/dashboard";
     * }
     * }
     */

    /**
     * Página de progreso del usuario
     * Muestra estadísticas, gráficos y evolución del entrenamiento
     */
    @GetMapping("/progreso")
    public String progreso(Model model, HttpSession session) {
        try {
            // Obtener usuario de la sesión
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            // Validar que haya usuario en sesión y no sea administrador
            if (usuario == null) {
                return "redirect:/login";
            }

            if (usuario.getPerfilUsuario() != null &&
                    "Administrador".equals(usuario.getPerfilUsuario().name())) {
                return "redirect:/admin/dashboard";
            }

            model.addAttribute("usuario", usuario);

            // Cargar estadísticas de rutinas asignadas
            List<RutinaAsignada> rutinasAsignadas = rutinaService.obtenerRutinasAsignadas(usuario.getId());

            // Estadísticas de rutinas
            long rutinasCompletadas = rutinasAsignadas.stream()
                    .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.COMPLETADA)
                    .count();
            long rutinasEnProgreso = rutinasAsignadas.stream()
                    .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.ACTIVA)
                    .count();
            long rutinasTotal = rutinasAsignadas.size();

            // Calcular progreso general (porcentaje de rutinas completadas)
            double progresoGeneral = rutinasTotal > 0 ? ((double) rutinasCompletadas / rutinasTotal) * 100 : 0.0;

            // Obtener estadísticas reales del ProgresoService
            Map<String, Object> estadisticas = progresoService.getEstadisticasGenerales(usuario);
            int rachaActual = (Integer) estadisticas.getOrDefault("rachaActual", 0);
            // Calcular días activos del mes (no de la semana)
            java.time.LocalDate inicioMes = java.time.LocalDate.now().withDayOfMonth(1);
            int diasActivosEsteMes = progresoService.contarDiasEntrenadosEntre(usuario, inicioMes,
                    java.time.LocalDate.now());

            // DATOS PARA GRÁFICA: Últimos 7 días de rutinas completadas
            java.time.LocalDate hoy = java.time.LocalDate.now();
            java.time.LocalDate hace7Dias = hoy.minusDays(6); // Incluye hoy

            // Crear mapa de fechas con 0 por defecto (últimos 7 días)
            Map<java.time.LocalDate, Integer> rutinasPorFecha = new java.util.LinkedHashMap<>();
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate fecha = hace7Dias.plusDays(i);
                rutinasPorFecha.put(fecha, 0);
            }

            System.out.println("=== DEBUG PROGRESO SEMANAL (RUTINAS COMPLETADAS) ===");
            System.out.println("Usuario: " + usuario.getCorreo() + " (ID: " + usuario.getId() + ")");
            System.out.println("Rango: " + hace7Dias + " a " + hoy);

            // Obtener rutinas completadas por fecha desde rutina_asignada
            List<Object[]> rutinasCompletadasPorFecha = rutinaService.obtenerRutinasCompletadasPorFecha(
                    usuario.getId(), hace7Dias);

            System.out.println(
                    "Rutinas completadas encontradas: " + rutinasCompletadasPorFecha.size() + " días con actividad");

            // Poblar con datos reales
            for (Object[] row : rutinasCompletadasPorFecha) {
                try {
                    java.sql.Date sqlDate = (java.sql.Date) row[0];
                    java.time.LocalDate fecha = sqlDate.toLocalDate();
                    int cantidad = ((Number) row[1]).intValue();

                    if (rutinasPorFecha.containsKey(fecha)) {
                        rutinasPorFecha.put(fecha, cantidad);
                        System.out.println("  ✓ " + fecha + ": " + cantidad + " rutina(s) completada(s)");
                    }
                } catch (Exception e) {
                    System.out.println("  ✗ Error procesando fecha: " + e.getMessage());
                }
            }

            // Convertir mapa a listas para el frontend
            List<String> fechasGrafica = new java.util.ArrayList<>();
            List<Integer> rutinasGrafica = new java.util.ArrayList<>();

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            for (Map.Entry<java.time.LocalDate, Integer> entry : rutinasPorFecha.entrySet()) {
                fechasGrafica.add(entry.getKey().format(formatter));
                rutinasGrafica.add(entry.getValue());
            }

            System.out.println("Datos finales para gráfica:");
            System.out.println("  Fechas: " + fechasGrafica);
            System.out.println("  Rutinas completadas: " + rutinasGrafica);
            System.out.println("  Total días con rutinas: "
                    + rutinasGrafica.stream().filter(v -> v > 0).count());
            System.out.println("====================================================\n");

            // Agregar atributos al modelo
            model.addAttribute("rutinasAsignadas", rutinasAsignadas);
            model.addAttribute("progresoGeneral", Math.round(progresoGeneral * 10.0) / 10.0);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            model.addAttribute("rutinasEnProgreso", rutinasEnProgreso);
            model.addAttribute("rutinasTotal", rutinasTotal);
            model.addAttribute("diasActivosEsteMes", diasActivosEsteMes);
            model.addAttribute("rachaActual", rachaActual);

            // Pasar datos como arrays simples para JavaScript
            model.addAttribute("fechasArray", fechasGrafica);
            model.addAttribute("ejerciciosArray", rutinasGrafica); // Ahora son rutinas, no ejercicios
            model.addAttribute("currentPage", "progreso");

            return "usuario/progreso";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar progreso: " + e.getMessage());
            return "usuario/dashboard";
        }
    }

    /**
     * Descarga el reporte de progreso en formato PDF
     * Genera un documento con estadísticas, rutinas completadas y activas
     */
    @GetMapping("/progreso/descargar-pdf")
    public ResponseEntity<byte[]> descargarProgresoPdf(HttpSession session) {
        try {
            // Validar usuario en sesión
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (usuario.getPerfilUsuario() != null &&
                    "Administrador".equals(usuario.getPerfilUsuario().name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generar PDF
            byte[] pdfBytes = pdfService.generarReporteProgreso(usuario);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Nombre del archivo con fecha y nombre del usuario
            String filename = String.format("FlowFit_Progreso_%s_%s.pdf",
                    usuario.getNombre().replaceAll("\\s+", "_"),
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy")));

            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * Página de perfil del usuario
     * Permite ver y editar información personal, objetivos y preferencias
     * TEMPORALMENTE DESHABILITADO
     */
    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            // Cargar usuario actualizado
            usuario = usuarioService.findById(usuario.getId()).orElse(usuario);
            model.addAttribute("usuario", usuario);

            // Cargar estadísticas básicas para mostrar en el perfil
            List<RutinaAsignada> rutinasActivas = rutinaService.obtenerRutinasActivasDelUsuario(usuario.getId());
            List<RutinaAsignada> rutinasCompletadas = rutinaService
                    .obtenerRutinasCompletadasDelUsuario(usuario.getId());

            model.addAttribute("rutinasActivas", rutinasActivas);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            model.addAttribute("currentPage", "perfil");

            System.out.println(" Cargando página de perfil para usuario: " + usuario.getNombre() + " (ID: "
                    + usuario.getId() + ")");
            return "usuario/perfil";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar perfil");
            return "usuario/dashboard";
        }
    }

    /**
     * Página del mapa del usuario
     * NOTA: Esta funcionalidad ahora está en MapaController
     */
    /*
     * @GetMapping("/mapa")
     * public String mapa(Model model, HttpSession session) {
     * try {
     * Usuario usuario = (Usuario) session.getAttribute("usuario");
     * if (usuario == null ||
     * usuario.getPerfilUsuario().toString().equals("Administrador")) {
     * return "redirect:/login";
     * }
     * 
     * usuario = usuarioService.findById(usuario.getId()).orElse(usuario);
     * model.addAttribute("usuario", usuario);
     * return "usuario/mapa";
     * } catch (Exception e) {
     * e.printStackTrace();
     * model.addAttribute("error", "Error al cargar el mapa");
     * return "usuario/dashboard";
     * }
     * }
     */

    // ===== ENDPOINTS PARA ACCIONES DE RUTINAS =====

    /**
     * Asignar una rutina al usuario
     */
    @PostMapping("/rutinas/asignar")
    public String asignarRutina(@RequestParam Integer rutinaId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            rutinaService.asignarRutinaAUsuario(rutinaId, usuario.getId());
            redirectAttributes.addFlashAttribute("successMessage", "¡Rutina asignada correctamente!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar rutina: " + e.getMessage());
        }

        return "redirect:/usuario/rutinas";
    }

    /**
     * Marcar una rutina como completada
     */
    @PostMapping("/rutinas/completar")
    public String completarRutina(@RequestParam Integer rutinaAsignadaId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            // Obtener la rutina asignada
            Optional<RutinaAsignada> rutinaOpt = rutinaService.obtenerRutinasAsignadas(usuario.getId())
                    .stream()
                    .filter(r -> r.getId().equals(rutinaAsignadaId))
                    .findFirst();

            if (rutinaOpt.isPresent()) {
                RutinaAsignada rutinaAsignada = rutinaOpt.get();

                System.out.println("DEBUG - Completando rutina ID: " + rutinaAsignadaId);
                System.out.println("DEBUG - Rutina: " + rutinaAsignada.getRutina().getNombre());

                // Registrar progreso de ejercicios automáticamente
                if (rutinaAsignada.getRutina() != null && rutinaAsignada.getRutina().getEjercicios() != null) {
                    int ejerciciosRegistrados = 0;
                    for (com.example.flowfit.model.RutinaEjercicio ejercicioRutina : rutinaAsignada.getRutina()
                            .getEjercicios()) {
                        try {
                            progresoService.registrarProgreso(
                                    usuario,
                                    rutinaAsignadaId,
                                    ejercicioRutina.getEjercicioCatalogo().getId(),
                                    ejercicioRutina.getSeries() != null ? ejercicioRutina.getSeries() : 1,
                                    ejercicioRutina.getRepeticiones() != null ? ejercicioRutina.getRepeticiones() : 1,
                                    ejercicioRutina.getPesoKg(),
                                    "Completado automáticamente");
                            ejerciciosRegistrados++;
                        } catch (Exception ex) {
                            System.err.println("Error al registrar ejercicio: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                    System.out.println("DEBUG - Ejercicios registrados: " + ejerciciosRegistrados);
                }
            }

            // Marcar rutina como completada
            rutinaService.marcarRutinaComoCompletada(rutinaAsignadaId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Rutina completada! ¡Felicitaciones! Tu progreso ha sido registrado.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar rutina: " + e.getMessage());
        }

        return "redirect:/usuario/rutinas";
    }

    /**
     * Mostrar página de sesión de entrenamiento
     */
    @GetMapping("/sesion/{rutinaAsignadaId}")
    public String iniciarSesion(@PathVariable Integer rutinaAsignadaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            // Obtener rutina asignada con detalles
            Optional<RutinaAsignada> rutinaOpt = rutinaService.obtenerRutinasAsignadas(usuario.getId())
                    .stream()
                    .filter(r -> r.getId().equals(rutinaAsignadaId))
                    .findFirst();

            if (rutinaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Rutina no encontrada");
                return "redirect:/usuario/rutinas";
            }

            RutinaAsignada rutinaAsignada = rutinaOpt.get();
            List<com.example.flowfit.model.RutinaEjercicio> ejercicios = rutinaAsignada.getRutina().getEjercicios();

            model.addAttribute("rutinaAsignada", rutinaAsignada);
            model.addAttribute("ejercicios", ejercicios);

            return "usuario/sesion-entrenamiento";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar sesión: " + e.getMessage());
            return "redirect:/usuario/rutinas";
        }
    }

    /**
     * Completar sesión de entrenamiento con progreso detallado
     */
    @PostMapping("/sesion/completar")
    public String completarSesion(@RequestParam Integer rutinaAsignadaId,
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            int ejerciciosRegistrados = 0;
            int ejerciciosCompletados = 0;

            // Procesar cada ejercicio del formulario
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();

                // Buscar ejercicios con formato: ejercicios[0].ejercicioId
                if (key.startsWith("ejercicios[") && key.endsWith("].ejercicioId")) {
                    String indexStr = key.substring(11, key.indexOf("]"));
                    int index = Integer.parseInt(indexStr);

                    // Verificar si este ejercicio fue completado
                    String completadoKey = "ejercicios[" + index + "].completado";
                    boolean completado = "true".equals(allParams.get(completadoKey));

                    if (completado) {
                        Integer ejercicioId = Integer.parseInt(entry.getValue());
                        Integer series = Integer
                                .parseInt(allParams.getOrDefault("ejercicios[" + index + "].series", "0"));
                        Integer repeticiones = Integer
                                .parseInt(allParams.getOrDefault("ejercicios[" + index + "].repeticiones", "0"));
                        String pesoStr = allParams.get("ejercicios[" + index + "].peso");
                        Double peso = (pesoStr != null && !pesoStr.isEmpty()) ? Double.parseDouble(pesoStr) : null;
                        String comentarios = allParams.get("ejercicios[" + index + "].comentarios");

                        try {
                            progresoService.registrarProgreso(
                                    usuario,
                                    rutinaAsignadaId,
                                    ejercicioId,
                                    series,
                                    repeticiones,
                                    peso,
                                    comentarios);
                            ejerciciosRegistrados++;
                            ejerciciosCompletados++;
                        } catch (Exception ex) {
                            System.err.println("Error al registrar ejercicio " + ejercicioId + ": " + ex.getMessage());
                        }
                    }
                }
            }

            if (ejerciciosRegistrados > 0) {
                // Actualizar progreso de la rutina
                rutinaService.marcarRutinaComoCompletada(rutinaAsignadaId);

                redirectAttributes.addFlashAttribute("successMessage",
                        "¡Sesión completada! Se registraron " + ejerciciosRegistrados
                                + " ejercicios. ¡Excelente trabajo!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No se registraron ejercicios. Marca los ejercicios completados.");
                return "redirect:/usuario/sesion/" + rutinaAsignadaId;
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar sesión: " + e.getMessage());
        }

        return "redirect:/usuario/progreso";
    }

    /**
     * Actualizar progreso de una rutina
     */
    @PostMapping("/rutinas/progreso")
    public String actualizarProgreso(@RequestParam Integer rutinaAsignadaId,
            @RequestParam int progreso,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            rutinaService.actualizarProgresoRutina(rutinaAsignadaId, progreso);
            redirectAttributes.addFlashAttribute("successMessage", "Progreso actualizado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar progreso: " + e.getMessage());
        }

        return "redirect:/usuario/rutinas";
    }

    /**
     * Ver detalles de una rutina específica
     * Muestra todos los ejercicios incluidos en la rutina
     */
    @GetMapping("/rutinas/detalles")
    public String verDetallesRutina(@RequestParam Integer rutinaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            // Obtener la rutina con sus ejercicios
            Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
            if (rutina == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Rutina no encontrada");
                return "redirect:/usuario/rutinas";
            }

            // Obtener ejercicios de la rutina usando el método DTO que evita problemas de
            // JPA
            List<EjercicioRutinaDto> ejerciciosRutina = rutinaService.obtenerEjerciciosConDetallesDto(rutinaId);

            // Verificar si el usuario tiene esta rutina asignada
            List<RutinaAsignada> rutinasUsuario = rutinaService.obtenerRutinasDelUsuario(usuario.getId());
            RutinaAsignada rutinaAsignada = rutinasUsuario.stream()
                    .filter(ra -> ra.getRutinaId().equals(rutinaId))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("usuario", usuario);
            model.addAttribute("rutina", rutina);
            model.addAttribute("ejercicios", ejerciciosRutina);
            model.addAttribute("rutinaAsignada", rutinaAsignada);
            model.addAttribute("totalEjercicios", ejerciciosRutina.size());

            return "usuario/rutina-detalles";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar detalles: " + e.getMessage());
            return "redirect:/usuario/rutinas";
        }
    }

    /**
     * API endpoint para obtener detalles de rutina en formato JSON
     * Se usa para el modal overlay dinámico
     */
    @GetMapping("/rutinas/detalles-json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesRutinaJson(@RequestParam Integer id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
                if (!todosUsuarios.isEmpty()) {
                    usuario = todosUsuarios.stream()
                            .filter(u -> u.getPerfilUsuario() == Usuario.PerfilUsuario.Usuario)
                            .findFirst()
                            .orElse(todosUsuarios.get(0));
                } else {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "No hay usuarios disponibles");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            }

            // Obtener la rutina
            Rutina rutina = rutinaService.obtenerRutinaPorId(id);
            if (rutina == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Rutina no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Obtener ejercicios de la rutina
            List<EjercicioRutinaDto> ejerciciosRutina = rutinaService.obtenerEjerciciosConDetallesDto(id);

            // Verificar si el usuario tiene esta rutina asignada
            List<RutinaAsignada> rutinasUsuario = rutinaService.obtenerRutinasDelUsuario(usuario.getId());
            RutinaAsignada rutinaAsignada = rutinasUsuario.stream()
                    .filter(ra -> ra.getRutinaId().equals(id))
                    .findFirst()
                    .orElse(null);

            // Preparar respuesta JSON simplificada
            Map<String, Object> response = new HashMap<>();
            response.put("id", rutina.getId());
            response.put("nombre", rutina.getNombre());
            response.put("descripcion", rutina.getDescripcion());
            response.put("duracionMinutos", rutina.getDuracionMinutos());
            response.put("caloriasEstimadas", rutina.getCaloriasEstimadas());
            response.put("dificultad", rutina.getDificultad());
            response.put("categoria", rutina.getCategoria());
            response.put("entrenador", rutina.getEntrenadorId() != null ? "Entrenador asignado" : "Global");

            // Ejercicios simplificados
            List<Map<String, Object>> ejerciciosSimplificados = new java.util.ArrayList<>();
            for (EjercicioRutinaDto ejercicio : ejerciciosRutina) {
                Map<String, Object> ej = new HashMap<>();
                ej.put("nombre", ejercicio.getEjercicioNombre());
                ej.put("descripcion", ejercicio.getEjercicioDescripcion());
                ej.put("series", ejercicio.getSeries());
                ej.put("repeticiones", ejercicio.getRepeticiones());
                ej.put("duracion", ejercicio.getDuracionSegundos());
                ej.put("imagen", ejercicio.getEjercicioImagen());
                ejerciciosSimplificados.add(ej);
            }
            response.put("ejercicios", ejerciciosSimplificados);

            response.put("estaAsignada", rutinaAsignada != null);
            if (rutinaAsignada != null) {
                response.put("rutinaAsignadaId", rutinaAsignada.getId());
                response.put("progreso", rutinaAsignada.getProgreso());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * API endpoint para obtener detalles de rutina en formato JSON
     * Se usa para el modal overlay dinámico
     */
    /**
     * API REST endpoint para obtener detalles de una rutina con sus ejercicios
     * Usado por el modal de detalles en la interfaz de usuario
     */
    @GetMapping("/api/rutinas/{rutinaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesRutinaRest(
            @PathVariable Integer rutinaId,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            // Obtener rutina
            Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
            if (rutina == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Rutina no encontrada"));
            }

            // Obtener ejercicios
            List<EjercicioRutinaDto> ejercicios = rutinaService.obtenerEjerciciosConDetallesDto(rutinaId);

            // Verificar si está asignada
            RutinaAsignada rutinaAsignada = rutinaService.obtenerRutinasDelUsuario(usuario.getId())
                    .stream()
                    .filter(ra -> ra.getRutinaId().equals(rutinaId))
                    .findFirst()
                    .orElse(null);

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();

            // Rutina completa con todos los detalles
            Map<String, Object> rutinaData = new HashMap<>();
            rutinaData.put("id", rutina.getId());
            rutinaData.put("nombre", rutina.getNombre());
            rutinaData.put("descripcion", rutina.getDescripcion());
            rutinaData.put("duracionMinutos", rutina.getDuracionMinutos());
            rutinaData.put("dificultad", rutina.getDificultad());
            rutinaData.put("categoria", calcularCategoria(rutina));
            rutinaData.put("caloriasEstimadas", rutina.getCaloriasEstimadas());
            rutinaData.put("objetivo", calcularObjetivo(rutina));

            response.put("rutina", rutinaData);
            response.put("ejercicios", ejercicios);
            response.put("rutinaAsignada", rutinaAsignada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar detalles: " + e.getMessage()));
        }
    }

    @GetMapping("/rutinas/detalles-api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesRutinaApi(@RequestParam Integer rutinaId,
            HttpSession session) {
        try {
            System.out.println("=== API DETALLES RUTINA ===");
            System.out.println("RutinaId recibido: " + rutinaId);

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                System.out.println("ADVERTENCIA: Usuario no encontrado en sesión, usando usuario de prueba...");
                // Usar primer usuario disponible como fallback para testing
                List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
                if (!todosUsuarios.isEmpty()) {
                    // Buscar un usuario con perfil Usuario (no admin)
                    usuario = todosUsuarios.stream()
                            .filter(u -> u.getPerfilUsuario() == Usuario.PerfilUsuario.Usuario)
                            .findFirst()
                            .orElse(todosUsuarios.get(0));
                    System.out.println("Usando usuario de prueba: " + usuario.getNombre());
                } else {
                    System.out.println("ERROR: No hay usuarios en el sistema");
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "No hay usuarios disponibles");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            }

            System.out.println("Usuario encontrado: " + usuario.getNombre() + " (ID: " + usuario.getId() + ")");

            // Obtener la rutina con sus ejercicios
            Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
            if (rutina == null) {
                System.out.println("ERROR: Rutina no encontrada con ID: " + rutinaId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Rutina no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            System.out.println("Rutina encontrada: " + rutina.getNombre());

            // Obtener ejercicios de la rutina
            List<EjercicioRutinaDto> ejerciciosRutina = rutinaService.obtenerEjerciciosConDetallesDto(rutinaId);
            System.out.println("Ejercicios encontrados: " + ejerciciosRutina.size());

            // Verificar si el usuario tiene esta rutina asignada
            List<RutinaAsignada> rutinasUsuario = rutinaService.obtenerRutinasDelUsuario(usuario.getId());
            RutinaAsignada rutinaAsignada = rutinasUsuario.stream()
                    .filter(ra -> ra.getRutinaId().equals(rutinaId))
                    .findFirst()
                    .orElse(null);

            System.out.println("Rutina asignada: " + (rutinaAsignada != null ? "Sí" : "No"));

            // Crear DTO para rutina (evitar problemas de serialización con Hibernate)
            RutinaDetalleDto rutinaDto = new RutinaDetalleDto();
            rutinaDto.setId(rutina.getId());
            rutinaDto.setNombre(rutina.getNombre());
            rutinaDto.setDescripcion(rutina.getDescripcion());
            rutinaDto.setFechaCreacion(rutina.getFechaCreacion());
            // Solo obtener el nombre del entrenador si existe, evitando cargar el proxy
            rutinaDto.setEntrenadorNombre(rutina.getEntrenadorId() != null ? "Entrenador asignado" : "Sin entrenador");

            // Crear DTO para rutina asignada si existe
            RutinaAsignadaDto rutinaAsignadaDto = null;
            if (rutinaAsignada != null) {
                rutinaAsignadaDto = new RutinaAsignadaDto();
                rutinaAsignadaDto.setId(rutinaAsignada.getId());
                rutinaAsignadaDto.setUsuarioId(rutinaAsignada.getUsuarioId());
                rutinaAsignadaDto.setRutinaId(rutinaAsignada.getRutinaId());
                rutinaAsignadaDto.setFechaAsignacion(rutinaAsignada.getFechaAsignacion());
                rutinaAsignadaDto.setFechaCompletada(rutinaAsignada.getFechaCompletada());
                rutinaAsignadaDto.setProgreso(rutinaAsignada.getProgreso());
                rutinaAsignadaDto.setEstado(rutinaAsignada.getEstado().toString());
            }

            // Preparar respuesta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("rutina", rutinaDto);
            response.put("ejercicios", ejerciciosRutina);
            response.put("rutinaAsignada", rutinaAsignadaDto);
            response.put("totalEjercicios", ejerciciosRutina.size());
            response.put("tieneRutinaAsignada", rutinaAsignada != null);

            System.out.println("Respuesta preparada exitosamente");
            System.out.println("===========================");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERROR en API detalles rutina:");
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===== FUNCIONALIDADES PARA ASIGNACIÓN DE ENTRENADORES =====

    /**
     * Página para buscar y solicitar entrenadores
     */
    @GetMapping("/buscar-entrenador")
    public String buscarEntrenador(HttpSession session, Model model) {
        // Verificar que el usuario esté logueado
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Añadir usuario al modelo
        model.addAttribute("usuario", usuario);

        // Obtener entrenadores disponibles
        List<Usuario> entrenadores = asignacionService.getEntrenadoresDisponibles();
        model.addAttribute("entrenadores", entrenadores);

        // Verificar si el usuario ya tiene un entrenador
        AsignacionEntrenador entrenadorActual = asignacionService.getEntrenadorActual(usuario.getId());
        model.addAttribute("tieneEntrenador", entrenadorActual != null);
        if (entrenadorActual != null) {
            model.addAttribute("entrenadorActual", entrenadorActual);
        }

        // Obtener historial de solicitudes
        List<AsignacionEntrenador> historial = asignacionService.getHistorialSolicitudes(usuario.getId());
        model.addAttribute("historialSolicitudes", historial);
        model.addAttribute("currentPage", "entrenador");

        return "usuario/buscar-entrenador";
    }

    /**
     * Procesar solicitud de asignación a entrenador
     */
    @PostMapping("/solicitar-entrenador")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> solicitarEntrenador(
            @RequestParam Integer entrenadorId,
            @RequestParam(defaultValue = "") String mensaje,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean exito = asignacionService.crearSolicitudAsignacion(usuario.getId(), entrenadorId, mensaje);

            if (exito) {
                response.put("success", true);
                response.put("message", "Solicitud enviada exitosamente");
            } else {
                response.put("success", false);
                response.put("message",
                        "No se pudo enviar la solicitud. Puede que ya tengas una solicitud pendiente o un entrenador asignado.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Métodos auxiliares para calcular categoría y objetivo de rutinas
    private String calcularCategoria(Rutina rutina) {
        String nombre = rutina.getNombre().toLowerCase();
        if (nombre.contains("fuerza") || nombre.contains("pesas") || nombre.contains("hipertrofia")) {
            return "Fuerza";
        }
        if (nombre.contains("cardio") || nombre.contains("hiit") || nombre.contains("resistencia")) {
            return "Cardio";
        }
        if (nombre.contains("yoga") || nombre.contains("flexibilidad") || nombre.contains("estiramiento")) {
            return "Flexibilidad";
        }
        if (nombre.contains("funcional") || nombre.contains("crossfit")) {
            return "Funcional";
        }
        return "General";
    }

    private String calcularObjetivo(Rutina rutina) {
        String nombre = rutina.getNombre().toLowerCase();
        if (nombre.contains("pérdida") || nombre.contains("perdida") || nombre.contains("quemar")
                || nombre.contains("adelgazar")) {
            return "Pérdida de peso";
        }
        if (nombre.contains("masa") || nombre.contains("músculo") || nombre.contains("volumen")
                || nombre.contains("hipertrofia")) {
            return "Ganar músculo";
        }
        if (nombre.contains("tonificación") || nombre.contains("tonificar") || nombre.contains("definición")) {
            return "Tonificación";
        }
        if (nombre.contains("resistencia") || nombre.contains("endurance")) {
            return "Resistencia";
        }
        if (nombre.contains("salud") || nombre.contains("bienestar")) {
            return "Salud general";
        }
        return "Acondicionamiento físico";
    }
}
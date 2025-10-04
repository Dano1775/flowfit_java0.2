package com.example.flowfit.config;

import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.repository.EjercicioCatalogoRepository;
import com.example.flowfit.repository.UsuarioRepository;
import com.example.flowfit.repository.RutinaRepository;
import com.example.flowfit.repository.RutinaEjercicioRepository;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EjercicioCatalogoRepository ejercicioRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RutinaRepository rutinaRepository;
    
    @Autowired
    private RutinaEjercicioRepository rutinaEjercicioRepository;
    
    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DATA INITIALIZER ===");
        
        // Initialize exercises
        long exerciseCount = ejercicioRepository.count();
        System.out.println("Current exercise count: " + exerciseCount);
        if (exerciseCount == 0) {
            System.out.println("No exercises found, initializing...");
            initializeExercises();
        } else {
            System.out.println("Exercises already exist, skipping initialization.");
            // List existing exercises
            List<EjercicioCatalogo> existing = ejercicioRepository.findAll();
            System.out.println("Existing exercises:");
            for (EjercicioCatalogo ej : existing) {
                System.out.println("- " + ej.getNombre() + " (ID: " + ej.getId() + ")");
            }
        }
        
        // Initialize test users
        initializeTestUsers();
        
        // Initialize test routines
        initializeTestRoutines();
        
        // Initialize test assigned routines
        initializeAssignedRoutines();
        
        System.out.println("========================");
    }

    private void initializeExercises() {
        List<EjercicioCatalogo> ejercicios = Arrays.asList(
            createEjercicio("Sentadillas", 
                "Ponte de pie con los pies al ancho de los hombros. Baja el cuerpo como si fueras a sentarte en una silla invisible, manteniendo la espalda recta y las rodillas alineadas con los pies.", 
                "sentadillas.jpg"),
            createEjercicio("Flexiones de pecho", 
                "Colócate en posición de plancha con las manos ligeramente más anchas que los hombros. Baja el pecho hacia el suelo y empuja hacia arriba manteniendo el cuerpo recto.", 
                "flexiones_pecho.jpg"),
            createEjercicio("Abdominales crunch", 
                "Acuéstate boca arriba con las rodillas flexionadas. Contrae los abdominales llevando el torso hacia las rodillas, manteniendo la parte baja de la espalda en el suelo.", 
                "abdominales_crunch.jpg"),
            createEjercicio("Plancha frontal", 
                "Colócate en posición de plancha apoyado en los antebrazos y puntas de los pies. Mantén el cuerpo recto desde la cabeza hasta los talones activando el core.", 
                "plancha_frontal.jpg"),
            createEjercicio("Zancadas", 
                "De pie, da un paso largo hacia adelante flexionando ambas rodillas a 90 grados. El muslo de la pierna adelantada debe quedar paralelo al suelo. Regresa a la posición inicial.", 
                "zancadas.jpg"),
            createEjercicio("Mountain climbers", 
                "Adopta la posición de plancha alta. Lleva alternadamente cada rodilla hacia el pecho manteniendo las caderas estables y el core activado.", 
                "mountain_climbers.jpg"),
            createEjercicio("Burpees", 
                "Desde una posición de pie, baja en cuclillas, coloca las manos en el suelo, salta hacia atrás en plancha, haz una flexión, regresa saltando y salta hacia arriba con los brazos extendidos.", 
                "burpees.jpg"),
            createEjercicio("Puente de glúteos", 
                "Acuéstate boca arriba con las rodillas flexionadas y pies apoyados en el suelo. Eleva las caderas contrayendo glúteos y abdominales, formando una línea recta desde las rodillas hasta los hombros.", 
                "puente_gluteos.jpg"),
            createEjercicio("Saltos en tijera", 
                "Desde posición erguida, salta separando las piernas y elevando los brazos por encima de la cabeza. Regresa saltando a la posición inicial con pies juntos y brazos a los lados.", 
                "saltos_tijera.jpg"),
            createEjercicio("Curl de bíceps", 
                "De pie con una mancuerna en cada mano y brazos extendidos a los lados. Flexiona los codos llevando las mancuernas hacia los hombros, manteniendo los codos fijos al torso.", 
                "curl_biceps.jpg"),
            createEjercicio("Press militar", 
                "Sentado o de pie, sujeta dos mancuernas a la altura de los hombros. Presiona las mancuernas hacia arriba hasta extender completamente los brazos, luego baja controladamente.", 
                "press_militar.jpg"),
            createEjercicio("Extensión de tríceps", 
                "Sujeta una mancuerna con ambas manos por encima de la cabeza. Baja la mancuerna detrás de la cabeza flexionando solo los codos, luego extiende los brazos para regresar a la posición inicial.", 
                "extension_triceps.jpg"),
            createEjercicio("Jump Squats", 
                "Realiza una sentadilla profunda y al subir salta explosivamente hacia arriba con los brazos extendidos. Aterriza suavemente y repite el movimiento de forma continua.", 
                "jump_squats.jpg"),
            createEjercicio("Curl martillo", 
                "De pie con mancuernas en cada mano en posición neutra (palmas mirándose). Flexiona los codos manteniendo las muñecas rectas, trabajando diferente área del bíceps.", 
                "curl_martillo.jpg")
        );

        ejercicioRepository.saveAll(ejercicios);
        System.out.println("✅ Ejercicios inicializados correctamente: " + ejercicios.size() + " ejercicios cargados.");
    }

    private EjercicioCatalogo createEjercicio(String nombre, String descripcion, String imagen) {
        EjercicioCatalogo ejercicio = new EjercicioCatalogo();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setImagen(imagen);
        ejercicio.setCreadoPor(null); // Global exercise
        return ejercicio;
    }
    
    private void initializeTestUsers() {
        long userCount = usuarioRepository.count();
        System.out.println("Current user count: " + userCount);
        
        if (userCount == 0) {
            System.out.println("No users found, creating test users...");
            
            // Create test user
            Usuario testUser = new Usuario();
            testUser.setNumeroDocumento("12345678");
            testUser.setNombre("Juan Pérez");
            testUser.setCorreo("juan.perez@test.com");
            testUser.setClave("123456");
            testUser.setTelefono("3001234567");
            testUser.setPerfilUsuario(Usuario.PerfilUsuario.Usuario);
            testUser.setEstado("A");
            
            usuarioRepository.save(testUser);
            System.out.println("✅ Usuario de prueba creado: " + testUser.getNombre());
        } else {
            System.out.println("Users already exist, listing existing users:");
            List<Usuario> existingUsers = usuarioRepository.findAll();
            for (Usuario user : existingUsers) {
                System.out.println("- " + user.getNombre() + " (ID: " + user.getId() + ", Rol: " + user.getPerfilUsuario() + ")");
            }
        }
    }
    
    private void initializeTestRoutines() {
        long rutinaCount = rutinaRepository.count();
        System.out.println("Current routine count: " + rutinaCount);
        
        if (rutinaCount == 0) {
            System.out.println("No routines found, creating test routines...");
            
            // Create test routine 1
            Rutina rutina1 = new Rutina();
            rutina1.setNombre("Rutina Express 15");
            rutina1.setDescripcion("Rutina rápida y efectiva de 15 minutos para quemar grasa y tonificar todo el cuerpo. Perfecta para principiantes.");
            rutina1.setFechaCreacion(LocalDate.now());
            
            // Create test routine 2
            Rutina rutina2 = new Rutina();
            rutina2.setNombre("Yoga y Movilidad");
            rutina2.setDescripcion("Sesión de yoga suave enfocada en flexibilidad y relajación. Ideal para recuperación activa.");
            rutina2.setFechaCreacion(LocalDate.now());
            
            // Save routines
            rutinaRepository.saveAll(Arrays.asList(rutina1, rutina2));
            
            // Add exercises to routines
            List<EjercicioCatalogo> ejercicios = ejercicioRepository.findAll();
            if (ejercicios.size() >= 5) {
                // Add exercises to routine 1
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(0).getId(), 1, 3, 15, null, null);
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(1).getId(), 2, 2, 10, null, null);
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(2).getId(), 3, 3, 12, null, null);
                
                // Add exercises to routine 2  
                addExerciseToRoutine(rutina2.getId(), ejercicios.get(3).getId(), 1, null, null, 45, null);
                addExerciseToRoutine(rutina2.getId(), ejercicios.get(4).getId(), 2, null, null, 60, null);
            }
            
            System.out.println("✅ Rutinas de prueba creadas: " + Arrays.asList(rutina1.getNombre(), rutina2.getNombre()));
        } else {
            System.out.println("Routines already exist, listing existing routines:");
            List<Rutina> existingRoutines = rutinaRepository.findAll();
            for (Rutina rutina : existingRoutines) {
                System.out.println("- " + rutina.getNombre() + " (ID: " + rutina.getId() + ")");
            }
        }
    }
    
    private void addExerciseToRoutine(Integer rutinaId, Integer ejercicioId, Integer orden, 
                                    Integer series, Integer repeticiones, Integer duracionSegundos, String notas) {
        RutinaEjercicio rutinaEjercicio = new RutinaEjercicio();
        rutinaEjercicio.setRutinaId(rutinaId);
        rutinaEjercicio.setEjercicioId(ejercicioId);
        rutinaEjercicio.setOrden(orden);
        rutinaEjercicio.setSeries(series);
        rutinaEjercicio.setRepeticiones(repeticiones);
        rutinaEjercicio.setDuracionSegundos(duracionSegundos);
        rutinaEjercicio.setNotas(notas);
        
        rutinaEjercicioRepository.save(rutinaEjercicio);
    }
    
    private void initializeAssignedRoutines() {
        // Clear existing assignments and create fresh test data
        rutinaAsignadaRepository.deleteAll();
        System.out.println("Cleared existing assigned routines, creating fresh test data...");
        
        // Always create fresh test assignments
            System.out.println("No assigned routines found, creating test assignments...");
            
            // Find Juan Pérez (test user)
            Usuario testUser = usuarioRepository.findByCorreo("juan.perez@test.com").orElse(null);
            if (testUser != null) {
                // Assign some routines with different progress levels
                createAssignedRoutine(testUser.getId(), 1, LocalDate.now().minusDays(10), 100, RutinaAsignada.EstadoRutina.COMPLETADA);
                createAssignedRoutine(testUser.getId(), 2, LocalDate.now().minusDays(7), 75, RutinaAsignada.EstadoRutina.ACTIVA);
                createAssignedRoutine(testUser.getId(), 3, LocalDate.now().minusDays(5), 50, RutinaAsignada.EstadoRutina.ACTIVA);
                createAssignedRoutine(testUser.getId(), 4, LocalDate.now().minusDays(3), 25, RutinaAsignada.EstadoRutina.ACTIVA);
                createAssignedRoutine(testUser.getId(), 5, LocalDate.now().minusDays(15), 100, RutinaAsignada.EstadoRutina.COMPLETADA);
                
                System.out.println("✅ Test assigned routines created for user: " + testUser.getNombre());
            } else {
                System.out.println("❌ Test user not found, cannot assign routines");
            }
    }
    
    private void createAssignedRoutine(Integer usuarioId, Integer rutinaId, LocalDate fechaAsignacion, Integer progreso, RutinaAsignada.EstadoRutina estado) {
        RutinaAsignada rutina = new RutinaAsignada();
        rutina.setUsuarioId(usuarioId);
        rutina.setRutinaId(rutinaId);
        rutina.setFechaAsignacion(fechaAsignacion);
        rutina.setProgreso(progreso);
        rutina.setEstado(estado);
        
        if (estado == RutinaAsignada.EstadoRutina.COMPLETADA) {
            rutina.setFechaCompletada(fechaAsignacion.plusDays(1));
        }
        
        rutinaAsignadaRepository.save(rutina);
    }
}
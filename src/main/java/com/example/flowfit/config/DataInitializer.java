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
import java.util.Optional;

// @Component  // Comentado temporalmente para evitar problemas con la base de datos
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
        System.out.println("=== INICIALIZADOR DE DATOS ===");

        // Inicializar ejercicios
        long exerciseCount = ejercicioRepository.count();
        System.out.println("Cantidad actual de ejercicios: " + exerciseCount);
        if (exerciseCount == 0) {
            System.out.println("No se encontraron ejercicios, inicializando...");
            initializeExercises();
        } else {
            System.out.println("Los ejercicios ya existen, omitiendo inicialización.");
            // Listar ejercicios existentes
            List<EjercicioCatalogo> existing = ejercicioRepository.findAll();
            System.out.println("Ejercicios existentes:");
            for (EjercicioCatalogo ej : existing) {
                System.out.println("- " + ej.getNombre() + " (ID: " + ej.getId() + ")");
            }
        }

        // Inicializar usuarios de prueba
        initializeTestUsers();

        // Inicializar rutinas de prueba
        initializeTestRoutines();

        // Inicializar ejercicios de rutinas
        initializeRoutineExercises();

        // Inicializar rutinas asignadas de prueba
        initializeAssignedRoutines();

        // Corregir nombre de usuario si es necesario
        fixUserName();

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
                        "curl_martillo.jpg"));

        ejercicioRepository.saveAll(ejercicios);
        System.out.println("✅ Ejercicios inicializados correctamente: " + ejercicios.size() + " ejercicios cargados.");
    }

    private EjercicioCatalogo createEjercicio(String nombre, String descripcion, String imagen) {
        EjercicioCatalogo ejercicio = new EjercicioCatalogo();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setImagen(imagen);
        ejercicio.setCreadoPor(null); // Ejercicio global
        return ejercicio;
    }

    private void initializeTestUsers() {
        long userCount = usuarioRepository.count();
        System.out.println("Cantidad actual de usuarios: " + userCount);

        if (userCount == 0) {
            System.out.println("No se encontraron usuarios, creando usuarios de prueba...");

            // Crear usuario de prueba
            Usuario testUser = new Usuario();
            testUser.setNumeroDocumento("12345678");
            testUser.setNombre("Usuario Test");
            testUser.setCorreo("usuario@flowfit.com");
            testUser.setClave("123456");
            testUser.setTelefono("3001234567");
            testUser.setPerfilUsuario(Usuario.PerfilUsuario.Usuario);
            testUser.setEstado("A");

            usuarioRepository.save(testUser);
            System.out.println("✅ Usuario de prueba creado: " + testUser.getNombre());

            // Crear entrenador de prueba
            Usuario testEntrenador = new Usuario();
            testEntrenador.setNumeroDocumento("87654321");
            testEntrenador.setNombre("Entrenador FlowFit");
            testEntrenador.setCorreo("entrenador@flowfit.com");
            testEntrenador.setClave("123456");
            testEntrenador.setTelefono("3007654321");
            testEntrenador.setPerfilUsuario(Usuario.PerfilUsuario.Entrenador);
            testEntrenador.setEstado("A");

            usuarioRepository.save(testEntrenador);
            System.out.println("✅ Entrenador de prueba creado: " + testEntrenador.getNombre());
        } else {
            System.out.println("Los usuarios ya existen, listando usuarios existentes:");
            List<Usuario> existingUsers = usuarioRepository.findAll();
            for (Usuario user : existingUsers) {
                System.out.println(
                        "- " + user.getNombre() + " (ID: " + user.getId() + ", Rol: " + user.getPerfilUsuario() + ")");
            }
        }
    }

    private void initializeTestRoutines() {
        long rutinaCount = rutinaRepository.count();
        System.out.println("Cantidad actual de rutinas: " + rutinaCount);

        if (rutinaCount == 0) {
            System.out.println("No se encontraron rutinas, creando rutinas de prueba...");

            // Crear rutina de prueba 1
            Rutina rutina1 = new Rutina();
            rutina1.setNombre("Rutina Express 15");
            rutina1.setDescripcion(
                    "Rutina rápida y efectiva de 15 minutos para quemar grasa y tonificar todo el cuerpo. Perfecta para principiantes.");
            rutina1.setFechaCreacion(LocalDate.now());

            // Crear rutina de prueba 2
            Rutina rutina2 = new Rutina();
            rutina2.setNombre("Yoga y Movilidad");
            rutina2.setDescripcion(
                    "Sesión de yoga suave enfocada en flexibilidad y relajación. Ideal para recuperación activa.");
            rutina2.setFechaCreacion(LocalDate.now());

            // Guardar rutinas
            rutinaRepository.saveAll(Arrays.asList(rutina1, rutina2));

            // Agregar ejercicios a rutinas
            List<EjercicioCatalogo> ejercicios = ejercicioRepository.findAll();
            if (ejercicios.size() >= 5) {
                // Agregar ejercicios a rutina 1
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(0).getId(), 1, 3, 15, null, null);
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(1).getId(), 2, 2, 10, null, null);
                addExerciseToRoutine(rutina1.getId(), ejercicios.get(2).getId(), 3, 3, 12, null, null);

                // Agregar ejercicios a rutina 2
                addExerciseToRoutine(rutina2.getId(), ejercicios.get(3).getId(), 1, null, null, 45, null);
                addExerciseToRoutine(rutina2.getId(), ejercicios.get(4).getId(), 2, null, null, 60, null);
            }

            System.out
                    .println("✅ Rutinas de prueba creadas: " + Arrays.asList(rutina1.getNombre(), rutina2.getNombre()));
        } else {
            System.out.println("Las rutinas ya existen, listando rutinas existentes:");
            List<Rutina> existingRoutines = rutinaRepository.findAll();
            for (Rutina rutina : existingRoutines) {
                System.out.println("- " + rutina.getNombre() + " (ID: " + rutina.getId() + ")");
            }
        }
    }

    private void initializeRoutineExercises() {
        long exerciseRoutineCount = rutinaEjercicioRepository.count();
        System.out.println("Cantidad actual de asociaciones rutina-ejercicio: " + exerciseRoutineCount);

        if (exerciseRoutineCount == 0) {
            System.out.println("No se encontraron ejercicios de rutina, creando asociaciones...");

            // Obtener todas las rutinas y ejercicios
            List<Rutina> rutinas = rutinaRepository.findAll();
            List<EjercicioCatalogo> ejercicios = ejercicioRepository.findAll();

            if (!rutinas.isEmpty() && !ejercicios.isEmpty()) {
                // Rutina 1: Cardio Básico para Principiantes
                if (rutinas.size() >= 1 && ejercicios.size() >= 5) {
                    Integer rutina1Id = rutinas.get(0).getId();
                    addExerciseToRoutine(rutina1Id, ejercicios.get(11).getId(), 1, 3, 15, 60,
                            "Saltos suaves para calentar");
                    addExerciseToRoutine(rutina1Id, ejercicios.get(0).getId(), 2, 3, 12, 0,
                            "Mantén la técnica correcta");
                    addExerciseToRoutine(rutina1Id, ejercicios.get(7).getId(), 3, 2, 30, 45, "Mantén ritmo constante");
                    addExerciseToRoutine(rutina1Id, ejercicios.get(8).getId(), 4, 2, 0, 30, "Mantén posición estable");
                }

                // Rutina 2: Fuerza Total Completa
                if (rutinas.size() >= 2 && ejercicios.size() >= 8) {
                    Integer rutina2Id = rutinas.get(1).getId();
                    addExerciseToRoutine(rutina2Id, ejercicios.get(0).getId(), 1, 4, 15, 0, "Peso corporal");
                    addExerciseToRoutine(rutina2Id, ejercicios.get(5).getId(), 2, 3, 10, 0, "Forma perfecta");
                    addExerciseToRoutine(rutina2Id, ejercicios.get(9).getId(), 3, 3, 0, 45, "Core activo");
                    addExerciseToRoutine(rutina2Id, ejercicios.get(1).getId(), 4, 3, 12, 60, "Controla el movimiento");
                }

                // Rutina 3: HIIT Quema Grasa Intenso
                if (rutinas.size() >= 3 && ejercicios.size() >= 10) {
                    Integer rutina3Id = rutinas.get(2).getId();
                    addExerciseToRoutine(rutina3Id, ejercicios.get(8).getId(), 1, 4, 5, 30, "Máxima intensidad");
                    addExerciseToRoutine(rutina3Id, ejercicios.get(6).getId(), 2, 4, 20, 45, "Saltos explosivos");
                    addExerciseToRoutine(rutina3Id, ejercicios.get(7).getId(), 3, 4, 30, 30, "Ritmo alto");
                    addExerciseToRoutine(rutina3Id, ejercicios.get(11).getId(), 4, 3, 45, 60, "Sin parar");
                }

                // Rutina 4: Yoga y Movilidad Matutina
                if (rutinas.size() >= 4) {
                    Integer rutina4Id = rutinas.get(3).getId();
                    addExerciseToRoutine(rutina4Id, ejercicios.get(9).getId(), 1, 3, 0, 45, "Respiración profunda");
                    addExerciseToRoutine(rutina4Id, ejercicios.get(10).getId(), 2, 3, 15, 0, "Aprieta glúteos");
                    addExerciseToRoutine(rutina4Id, ejercicios.get(13).getId(), 3, 2, 10, 60, "Cada pierna");
                }

                System.out.println("✅ Ejercicios asociados a rutinas correctamente");
            } else {
                System.out.println("❌ No hay suficientes rutinas o ejercicios para crear asociaciones");
            }
        } else {
            System.out.println("Los ejercicios de rutina ya existen, omitiendo inicialización.");
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
        rutinaEjercicio.setDescansoSegundos(60); // Descanso por defecto
        rutinaEjercicio.setNotas(notas);

        rutinaEjercicioRepository.save(rutinaEjercicio);
    }

    private void initializeAssignedRoutines() {
        // Limpiar asignaciones existentes y crear datos de prueba nuevos
        rutinaAsignadaRepository.deleteAll();
        System.out.println("Rutinas asignadas existentes eliminadas, creando datos de prueba nuevos...");

        // Siempre crear asignaciones de prueba nuevas
        System.out.println("Creando asignaciones de prueba...");

        // Usar Usuario Test (ID: 4) como usuario de prueba
        Integer testUserId = 4;
        Optional<Usuario> testUserOpt = usuarioRepository.findById(testUserId);
        if (testUserOpt.isPresent()) {
            Usuario testUser = testUserOpt.get();
            // Asignar algunas rutinas con diferentes niveles de progreso para datos
            // realistas
            createAssignedRoutine(testUser.getId(), 1, LocalDate.now().minusDays(15), 100,
                    RutinaAsignada.EstadoRutina.COMPLETADA);
            createAssignedRoutine(testUser.getId(), 2, LocalDate.now().minusDays(10), 85,
                    RutinaAsignada.EstadoRutina.ACTIVA);
            createAssignedRoutine(testUser.getId(), 3, LocalDate.now().minusDays(7), 70,
                    RutinaAsignada.EstadoRutina.ACTIVA);
            createAssignedRoutine(testUser.getId(), 4, LocalDate.now().minusDays(5), 45,
                    RutinaAsignada.EstadoRutina.ACTIVA);
            createAssignedRoutine(testUser.getId(), 5, LocalDate.now().minusDays(2), 25,
                    RutinaAsignada.EstadoRutina.ACTIVA);

            System.out.println("✅ Rutinas asignadas de prueba creadas para usuario: " + testUser.getNombre());
        } else {
            System.out.println("❌ Usuario de prueba no encontrado, no se pueden asignar rutinas");
        }
    }

    private void createAssignedRoutine(Integer usuarioId, Integer rutinaId, LocalDate fechaAsignacion, Integer progreso,
            RutinaAsignada.EstadoRutina estado) {
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

    private void fixUserName() {
        System.out.println("Verificando y corrigiendo nombre de usuario...");

        // Buscar usuario por correo
        Optional<Usuario> userOpt = usuarioRepository.findByCorreo("usuario@flowfit.com");

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            System.out.println("Nombre de usuario actual: " + user.getNombre());

            // Si el nombre no es "Usuario Test", corregirlo
            if (!"Usuario Test".equals(user.getNombre())) {
                user.setNombre("Usuario Test");
                usuarioRepository.save(user);
                System.out.println("✅ Nombre de usuario corregido a: Usuario Test");
            } else {
                System.out.println("✅ El nombre de usuario ya es correcto: Usuario Test");
            }
        } else {
            System.out.println("❌ Usuario no encontrado con correo: usuario@flowfit.com");
        }
    }
}
package com.example.flowfit.config;

import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.repository.EjercicioCatalogoRepository;
import com.example.flowfit.repository.UsuarioRepository;
import com.example.flowfit.repository.RutinaRepository;
import com.example.flowfit.repository.RutinaEjercicioRepository;
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
}
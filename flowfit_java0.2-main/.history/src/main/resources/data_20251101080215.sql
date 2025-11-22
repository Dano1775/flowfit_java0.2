-- =========================================================
-- FlowFit - Esquema completo sincronizado con los modelos Java
-- Engine: InnoDB, Charset: utf8mb4
-- Cuidado: revisa credenciales y realiza backup antes de ejecutar
-- =========================================================

DROP DATABASE IF EXISTS flowfit_db;
CREATE DATABASE flowfit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE flowfit_db;

-- =============================================
-- TABLA: usuario
-- Campos basados en `Usuario.java` + mejoras útiles
-- =============================================
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_documento VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    perfil_usuario ENUM('Usuario','Administrador','Entrenador','Nutricionista') NOT NULL,
    estado CHAR(1) NOT NULL DEFAULT 'A', -- A=activo, I=inactivo, P=pendiente
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME NULL,
    intentos_fallidos INT DEFAULT 0,
    cuenta_bloqueada BOOLEAN DEFAULT FALSE,
    token_recuperacion VARCHAR(255) DEFAULT NULL,
    token_expiracion DATETIME DEFAULT NULL,
    INDEX idx_correo (correo),
    INDEX idx_perfil (perfil_usuario),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: ejercicio_catalogo
-- Campos ampliados para coincidir con el script y el modelo `EjercicioCatalogo`
-- =============================================
CREATE TABLE ejercicio_catalogo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    imagen VARCHAR(255),
    grupo_muscular ENUM('Pecho','Espalda','Piernas','Brazos','Hombros','Abdomen','Cardio','Full Body') DEFAULT 'Full Body',
    nivel_dificultad ENUM('Principiante','Intermedio','Avanzado') DEFAULT 'Principiante',
    calorias_por_minuto INT DEFAULT 5,
    creado_por INT DEFAULT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (creado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_grupo (grupo_muscular),
    INDEX idx_nivel (nivel_dificultad),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: rutina
-- Mantener columnas que en el código eran @Transient para poder consultarlas desde SQL
-- =============================================
CREATE TABLE rutina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    entrenador_id INT DEFAULT NULL,
    fecha_creacion DATE DEFAULT (CURRENT_DATE),
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activa BOOLEAN DEFAULT TRUE,
    tipo_rutina ENUM('Cardio','Fuerza','HIIT','Yoga','Mixta') DEFAULT 'Mixta',
    nivel_dificultad ENUM('Principiante','Intermedio','Avanzado') DEFAULT 'Principiante',
    duracion_estimada_minutos INT DEFAULT 30,
    calorias_estimadas INT DEFAULT 200,
    creado_por INT DEFAULT NULL,
    actualizado_por INT DEFAULT NULL,
    FOREIGN KEY (entrenador_id) REFERENCES usuario(id) ON DELETE SET NULL,
    FOREIGN KEY (creado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    FOREIGN KEY (actualizado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_entrenador (entrenador_id),
    INDEX idx_tipo (tipo_rutina),
    INDEX idx_nivel (nivel_dificultad),
    INDEX idx_activa (activa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: rutina_ejercicio (relación many-to-many con metadatos)
-- =============================================
CREATE TABLE rutina_ejercicio (
    rutina_id INT NOT NULL,
    ejercicio_id INT NOT NULL,
    orden INT NOT NULL DEFAULT 1,
    series INT DEFAULT 1,
    repeticiones INT DEFAULT 1,
    duracion_segundos INT DEFAULT NULL,
    descanso_segundos INT DEFAULT 60,
    peso_kg DOUBLE DEFAULT NULL,
    notas VARCHAR(500) DEFAULT NULL,
    sets INT DEFAULT NULL,
    PRIMARY KEY (rutina_id, ejercicio_id),
    FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE,
    FOREIGN KEY (ejercicio_id) REFERENCES ejercicio_catalogo(id) ON DELETE CASCADE,
    INDEX idx_rutina_orden (rutina_id, orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: rutina_asignada
-- Seguimiento de asignaciones de rutina a usuarios
-- =============================================
CREATE TABLE rutina_asignada (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rutina_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_asignacion DATE DEFAULT (CURRENT_DATE),
    fecha_completada DATE DEFAULT NULL,
    estado ENUM('ACTIVA','COMPLETADA','PAUSADA') DEFAULT 'ACTIVA',
    progreso INT DEFAULT 0,
    veces_completada INT DEFAULT 0,
    asignado_por INT DEFAULT NULL,
    notas TEXT DEFAULT NULL,
    ultima_actividad DATE DEFAULT NULL,
    FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (asignado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_rutina (rutina_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_asignacion (fecha_asignacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: registro_aprobaciones
-- =============================================
CREATE TABLE registro_aprobaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    admin_id INT NOT NULL,
    accion ENUM('Aprobado','Rechazado') NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    comentarios TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_fecha (fecha),
    INDEX idx_accion (accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLA: progreso_ejercicio
-- Guarda progreso por ejercicio dentro de una asignación
-- =============================================
CREATE TABLE progreso_ejercicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    rutina_asignada_id INT NOT NULL,
    ejercicio_id INT NOT NULL,
    fecha DATE DEFAULT (CURRENT_DATE),
    series_completadas INT DEFAULT 0,
    repeticiones_realizadas INT DEFAULT 0,
    peso_utilizado DOUBLE DEFAULT NULL,
    tiempo_segundos INT DEFAULT NULL,
    comentarios TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (rutina_asignada_id) REFERENCES rutina_asignada(id) ON DELETE CASCADE,
    FOREIGN KEY (ejercicio_id) REFERENCES ejercicio_catalogo(id) ON DELETE CASCADE,
    INDEX idx_usuario_fecha (usuario_id, fecha),
    INDEX idx_rutina_asignada (rutina_asignada_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- (Opcional) Tabla: notificacion
-- Para notificaciones internas del sistema
-- =============================================
CREATE TABLE notificacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo ENUM('RUTINA_ASIGNADA','APROBACION','RECORDATORIO','LOGRO') NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    datos_json JSON DEFAULT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_usuario_leida (usuario_id, leida),
    INDEX idx_fecha_notificacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- (Opcional) Tablas: logro y usuario_logro
-- Sistema de logros/insignias
-- =============================================
CREATE TABLE logro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    icono VARCHAR(255),
    condicion_tipo ENUM('RUTINAS_COMPLETADAS','DIAS_CONSECUTIVOS','CALORIAS_QUEMADAS') NOT NULL,
    condicion_valor INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_logro_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario_logro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    logro_id INT NOT NULL,
    fecha_obtenido DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (logro_id) REFERENCES logro(id) ON DELETE CASCADE,
    UNIQUE KEY uq_usuario_logro (usuario_id, logro_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- (Opcional) Historial de ediciones (si usas Auditing)
-- =============================================
CREATE TABLE historial_ediciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tabla_nombre VARCHAR(100) NOT NULL,
    registro_id INT NOT NULL,
    campo VARCHAR(100) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    usuario_id INT DEFAULT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_tabla_registro (tabla_nombre, registro_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- VISTAS ÚTILES
-- =============================================
CREATE VIEW vista_rutinas_completas AS
SELECT 
    r.id,
    r.nombre,
    r.descripcion,
    r.tipo_rutina,
    r.nivel_dificultad,
    r.duracion_estimada_minutos,
    r.calorias_estimadas,
    u.nombre AS entrenador_nombre,
    COUNT(re.ejercicio_id) AS total_ejercicios
FROM rutina r
LEFT JOIN usuario u ON r.entrenador_id = u.id
LEFT JOIN rutina_ejercicio re ON r.id = re.rutina_id
WHERE r.activa = TRUE
GROUP BY r.id, r.nombre, r.descripcion, r.tipo_rutina, r.nivel_dificultad, 
         r.duracion_estimada_minutos, r.calorias_estimadas, u.nombre;

CREATE VIEW vista_progreso_usuarios AS
SELECT 
    u.id AS usuario_id,
    u.nombre AS usuario_nombre,
    COUNT(ra.id) AS rutinas_asignadas,
    SUM(CASE WHEN ra.estado = 'COMPLETADA' THEN 1 ELSE 0 END) AS rutinas_completadas,
    SUM(CASE WHEN ra.estado = 'ACTIVA' THEN 1 ELSE 0 END) AS rutinas_activas,
    CASE WHEN COUNT(ra.id)=0 THEN 0
         ELSE ROUND((SUM(CASE WHEN ra.estado = 'COMPLETADA' THEN 1 ELSE 0 END) * 100.0 / COUNT(ra.id)), 2) END AS porcentaje_completadas
FROM usuario u
LEFT JOIN rutina_asignada ra ON u.id = ra.usuario_id
WHERE u.perfil_usuario = 'Usuario'
GROUP BY u.id, u.nombre;

-- =============================================
-- DATOS DE EJEMPLO (INSERTS)
-- =============================================

-- Usuarios
INSERT INTO usuario (numero_documento, nombre, telefono, correo, clave, perfil_usuario, estado) VALUES
('1001','Admin FlowFit','1111111111','admin@flowfit.com','$2a$10$EXAMPLEHASHADMIN','Administrador','A'),
('2001','Carlos Rodríguez','2222222222','entrenador@flowfit.com','$2a$10$EXAMPLEHASHENT','Entrenador','A'),
('3001','María González','3333333333','nutricionista@flowfit.com','$2a$10$EXAMPLEHASHNUT','Nutricionista','A'),
('4001','Juan Pérez','4444444444','usuario@flowfit.com','$2a$10$EXAMPLEHASHUSR','Usuario','A'),
('5001','Carlos Gómez','5551110001','carlos.gomez@flowfit.com','$2a$10$EXAMPLE','Entrenador','I'),
('5002','Laura Ruiz','5551110002','laura.ruiz@flowfit.com','$2a$10$EXAMPLE','Nutricionista','I'),
('6001','Sofia López','6661110001','sofia@flowfit.com','$2a$10$EXAMPLE','Usuario','A'),
('6002','Miguel Torres','6661110002','miguel@flowfit.com','$2a$10$EXAMPLE','Usuario','A'),
('6003','Carmen Vega','6661110003','carmen@flowfit.com','$2a$10$EXAMPLE','Usuario','A');

-- Ejercicios catálogo
INSERT INTO ejercicio_catalogo (nombre, descripcion, imagen, grupo_muscular, nivel_dificultad, calorias_por_minuto, creado_por) VALUES
('Abdominales Crunch','Acuéstate boca arriba con las rodillas dobladas. Levanta el torso contrayendo los abdominales.','abdominales_crunch.jpg','Abdomen','Principiante',4,200),
('Burpees','Desde una posición de pie...','burpees.jpg','Full Body','Intermedio',12,200),
('Curl de Bíceps','De pie con mancuernas...','curl_biceps.jpg','Brazos','Principiante',3,200),
('Curl Martillo','Similar al curl de bíceps...','curl_martillo.jpg','Brazos','Principiante',3,200),
('Extensión de Tríceps','Sujeta una mancuerna...','extension_triceps.jpg','Brazos','Principiante',4,200),
('Flexiones de Pecho','En posición de plancha...','flexiones_pecho.jpg','Pecho','Principiante',6,200),
('Jump Squats','Realiza una sentadilla profunda...','jump_squats.jpg','Piernas','Intermedio',8,200),
('Mountain Climbers','En posición de plancha...','mountain_climbers.jpg','Cardio','Intermedio',10,200),
('Plancha Frontal','Mantén el cuerpo recto...','plancha_frontal.jpg','Abdomen','Principiante',3,200),
('Press Militar','Empuja mancuernas desde los hombros...','press_militar.jpg','Hombros','Intermedio',5,200),
('Puente de Glúteos','Acostado boca arriba...','puente_gluteos.jpg','Piernas','Principiante',4,200),
('Saltos en Tijera','Desde posición erguida...','saltos_tijera.jpg','Cardio','Principiante',7,200),
('Sentadillas','Ponte de pie con pies al ancho...','sentadillas.jpg','Piernas','Principiante',5,200),
('Zancadas','Da un paso largo hacia adelante...','zancadas.jpg','Piernas','Principiante',6,200);

-- Rutinas
INSERT INTO rutina (nombre, descripcion, entrenador_id, tipo_rutina, nivel_dificultad, duracion_estimada_minutos, calorias_estimadas) VALUES
('Cardio Básico para Principiantes','Rutina perfecta para principiantes...', NULL, 'Cardio', 'Principiante', 25, 180),
('Fuerza Total Completa','Entrenamiento completo de fuerza...', 2001, 'Fuerza', 'Intermedio', 45, 320),
('HIIT Quema Grasa Intenso','Entrenamiento de alta intensidad...', NULL, 'HIIT', 'Intermedio', 20, 280),
('Yoga y Movilidad Matutina','Sesión de yoga suave...', NULL, 'Yoga', 'Principiante', 30, 120),
('Abs y Core Definición','Entrenamiento especializado para el core...', 2001, 'Fuerza', 'Intermedio', 20, 150),
('Piernas y Glúteos Power','Rutina enfocada en el tren inferior...', NULL, 'Fuerza', 'Principiante', 35, 250),
('Rutina Express 15 min','Entrenamiento rápido y efectivo...', NULL, 'Mixta', 'Intermedio', 15, 200),
('Full Body Principiante','Rutina completa para trabajar todo el cuerpo...', 2001, 'Mixta', 'Principiante', 40, 280);

-- Rutina_ejercicio (varios inserts, ejemplo)
-- Nota: los ids de ejercicios dependen del orden de los inserts anteriores
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(1, 12, 1, 3, 20, NULL, 30),
(1, 8, 2, 3, 15, NULL, 45),
(1, 7, 3, 3, 10, NULL, 60),
(1, 13, 4, 2, 15, NULL, 30),
(1, 9, 5, 2, NULL, 30, 45);

-- Asignaciones de ejemplo
INSERT INTO rutina_asignada (rutina_id, usuario_id, estado, asignado_por) VALUES
(1, 4, 'ACTIVA', 2001),
(4, 4, 'ACTIVA', NULL),
(8, 7, 'ACTIVA', 2001),
(1, 6, 'COMPLETADA', 2001),
(6, 9, 'ACTIVA', NULL);

-- Registro de aprobaciones
INSERT INTO registro_aprobaciones (usuario_id, admin_id, accion, comentarios) VALUES
(4, 1, 'Aprobado', 'Usuario registrado correctamente'),
(6, 1, 'Aprobado', 'Perfil completo y verificado'),
(7, 1, 'Aprobado', 'Documentación en orden');

-- =============================================
-- CONSULTAS DE VERIFICACIÓN (opcional al ejecutar)
-- =============================================
-- SELECT 'USUARIOS' as tabla, COUNT(*) as registros FROM usuario
-- UNION ALL
-- SELECT 'EJERCICIOS' as tabla, COUNT(*) as registros FROM ejercicio_catalogo
-- UNION ALL
-- SELECT 'RUTINAS' as tabla, COUNT(*) as registros FROM rutina
-- UNION ALL
-- SELECT 'RUTINA_EJERCICIO' as tabla, COUNT(*) as registros FROM rutina_ejercicio
-- UNION ALL
-- SELECT 'RUTINAS_ASIGNADAS' as tabla, COUNT(*) as registros FROM rutina_asignada;

-- =========================================================
-- FIN DEL SCRIPT
-- =========================================================
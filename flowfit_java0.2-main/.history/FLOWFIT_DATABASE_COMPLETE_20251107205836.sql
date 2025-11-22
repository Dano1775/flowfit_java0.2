-- =============================================
-- FLOWFIT DATABASE - SCRIPT COMPLETO
-- Incluye todas las mejoras y estructura necesaria
-- =============================================

-- Eliminar base de datos si existe y crear nueva
DROP DATABASE IF EXISTS flowfit_db;
CREATE DATABASE flowfit_db;
USE flowfit_db;

-- =============================================
-- TABLA DE USUARIOS
-- =============================================
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    perfil_usuario ENUM('Usuario', 'Administrador', 'Entrenador', 'Nutricionista') NOT NULL,
    estado CHAR(1) NOT NULL DEFAULT 'A',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_correo (correo),
    INDEX idx_perfil (perfil_usuario),
    INDEX idx_estado (estado)
);

-- =============================================
-- TABLA DE TOKENS DE RECUPERACIÓN DE CONTRASEÑA
-- (NUEVA - Para sistema de reset de password)
-- =============================================
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    usuario_id INT NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_token (token),
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_fecha_expiracion (fecha_expiracion),
    CONSTRAINT fk_password_reset_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tokens temporales para recuperación de contraseña (válidos 15 minutos)';

-- =============================================
-- TABLA DE EJERCICIOS CATÁLOGO
-- =============================================
CREATE TABLE ejercicio_catalogo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    imagen VARCHAR(255),
    grupo_muscular ENUM('Pecho', 'Espalda', 'Piernas', 'Brazos', 'Hombros', 'Abdomen', 'Cardio', 'Full Body') DEFAULT 'Full Body',
    nivel_dificultad ENUM('Principiante', 'Intermedio', 'Avanzado') DEFAULT 'Principiante',
    calorias_por_minuto INT DEFAULT 5,
    creado_por INT DEFAULT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (creado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_grupo (grupo_muscular),
    INDEX idx_nivel (nivel_dificultad)
);

-- =============================================
-- TABLA DE RUTINAS
-- =============================================
CREATE TABLE rutina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    entrenador_id INT,
    fecha_creacion DATE DEFAULT (CURRENT_DATE),
    activa BOOLEAN DEFAULT TRUE,
    tipo_rutina ENUM('Cardio', 'Fuerza', 'HIIT', 'Yoga', 'Mixta') DEFAULT 'Mixta',
    nivel_dificultad ENUM('Principiante', 'Intermedio', 'Avanzado') DEFAULT 'Principiante',
    duracion_estimada_minutos INT DEFAULT 30,
    calorias_estimadas INT DEFAULT 200,
    FOREIGN KEY (entrenador_id) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_entrenador (entrenador_id),
    INDEX idx_tipo (tipo_rutina),
    INDEX idx_nivel (nivel_dificultad)
);

-- =============================================
-- RELACIÓN RUTINA-EJERCICIO (MEJORADA)
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
    sets INT DEFAULT NULL, -- Para compatibilidad
    PRIMARY KEY (rutina_id, ejercicio_id),
    FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE,
    FOREIGN KEY (ejercicio_id) REFERENCES ejercicio_catalogo(id) ON DELETE CASCADE,
    INDEX idx_orden (rutina_id, orden)
);

-- =============================================
-- RUTINAS ASIGNADAS (MEJORADA)
-- =============================================
CREATE TABLE rutina_asignada (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rutina_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_asignacion DATE DEFAULT (CURRENT_DATE),
    fecha_completada DATE DEFAULT NULL,
    estado ENUM('ACTIVA', 'COMPLETADA', 'PAUSADA') DEFAULT 'ACTIVA',
    progreso INT DEFAULT 0,
    veces_completada INT DEFAULT 0,
    asignado_por INT DEFAULT NULL,
    notas TEXT DEFAULT NULL,
    FOREIGN KEY (rutina_id) REFERENCES rutina(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (asignado_por) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_rutina (rutina_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_asignacion (fecha_asignacion)
);

-- =============================================
-- REGISTRO DE APROBACIONES
-- =============================================
CREATE TABLE registro_aprobaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    admin_id INT NOT NULL,
    accion ENUM('Aprobado', 'Rechazado') NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    comentarios TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_fecha (fecha),
    INDEX idx_accion (accion)
);

-- =============================================
-- PROGRESO DE EJERCICIOS (NUEVA TABLA)
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
);

-- =============================================
-- TABLA DE BOLETINES INFORMATIVOS
-- =============================================
CREATE TABLE boletin_informativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asunto VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    tipo_destinatario ENUM('TODOS', 'USUARIOS', 'ENTRENADORES', 'NUTRICIONISTAS', 'ADMINISTRADORES', 'USUARIOS_ACTIVOS', 'USUARIOS_INACTIVOS') NOT NULL,
    estado_envio ENUM('PENDIENTE', 'ENVIANDO', 'COMPLETADO', 'FALLIDO') DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_envio DATETIME DEFAULT NULL,
    total_destinatarios INT DEFAULT 0,
    enviados_exitosos INT DEFAULT 0,
    enviados_fallidos INT DEFAULT 0,
    creado_por VARCHAR(100) NOT NULL,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_estado (estado_envio),
    INDEX idx_fecha_creacion (fecha_creacion),
    INDEX idx_tipo_destinatario (tipo_destinatario),
    INDEX idx_creado_por (creado_por)
);

-- =============================================
-- INSERTAR USUARIOS DE PRUEBA
-- =============================================
INSERT INTO usuario (numero_documento, nombre, telefono, correo, clave, perfil_usuario, estado) VALUES
-- Usuarios del sistema principales
('1001', 'Admin FlowFit', '1111111111', 'admin@flowfit.com', 'admin123', 'Administrador', 'A'),
('2001', 'Carlos Rodríguez', '2222222222', 'entrenador@flowfit.com', 'entrenador123', 'Entrenador', 'A'),
('3001', 'María González', '3333333333', 'nutricionista@flowfit.com', 'nutri123', 'Nutricionista', 'A'),
('4001', 'Juan Pérez', '4444444444', 'usuario@flowfit.com', 'usuario123', 'Usuario', 'A'),

-- Usuarios adicionales para pruebas
('5001', 'Carlos Gómez', '5551110001', 'carlos.gomez@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5002', 'Laura Ruiz', '5551110002', 'laura.ruiz@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5003', 'Pedro Díaz', '5551110003', 'pedro.diaz@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5004', 'Ana Torres', '5551110004', 'ana.torres@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5005', 'Diego Marín', '5551110005', 'diego.marin@flowfit.com', 'clave123', 'Entrenador', 'I'),

-- Más usuarios para pruebas del sistema
('6001', 'Sofia López', '6661110001', 'sofia@flowfit.com', 'usuario123', 'Usuario', 'A'),
('6002', 'Miguel Torres', '6661110002', 'miguel@flowfit.com', 'usuario123', 'Usuario', 'A'),
('6003', 'Carmen Vega', '6661110003', 'carmen@flowfit.com', 'usuario123', 'Usuario', 'A');

-- =============================================
-- INSERTAR EJERCICIOS COMPLETOS
-- =============================================
INSERT INTO ejercicio_catalogo (nombre, descripcion, imagen, grupo_muscular, nivel_dificultad, calorias_por_minuto) VALUES
('Abdominales Crunch', 'Acuéstate boca arriba con las rodillas dobladas. Levanta el torso contrayendo los abdominales.', 'abdominales_crunch.jpg', 'Abdomen', 'Principiante', 4),
('Burpees', 'Desde una posición de pie, baja a cuclillas, salta hacia atrás a plancha, haz una flexión, regresa y salta.', 'burpees.jpg', 'Full Body', 'Intermedio', 12),
('Curl de Bíceps', 'De pie con mancuernas, flexiona los codos llevando el peso hacia los hombros.', 'curl_biceps.jpg', 'Brazos', 'Principiante', 3),
('Curl Martillo', 'Similar al curl de bíceps pero manteniendo las muñecas en posición neutra.', 'curl_martillo.jpg', 'Brazos', 'Principiante', 3),
('Extensión de Tríceps', 'Sujeta una mancuerna con ambas manos por encima de la cabeza y baja controladamente.', 'extension_triceps.jpg', 'Brazos', 'Principiante', 4),
('Flexiones de Pecho', 'En posición de plancha, baja el pecho hacia el suelo y empuja hacia arriba.', 'flexiones_pecho.jpg', 'Pecho', 'Principiante', 6),
('Jump Squats', 'Realiza una sentadilla profunda y explota hacia arriba con un salto.', 'jump_squats.jpg', 'Piernas', 'Intermedio', 8),
('Mountain Climbers', 'En posición de plancha, alterna llevando las rodillas hacia el pecho rápidamente.', 'mountain_climbers.jpg', 'Cardio', 'Intermedio', 10),
('Plancha Frontal', 'Mantén el cuerpo recto apoyándote en antebrazos y puntas de los pies.', 'plancha_frontal.jpg', 'Abdomen', 'Principiante', 3),
('Press Militar', 'Sentado o de pie, empuja mancuernas desde los hombros hacia arriba.', 'press_militar.jpg', 'Hombros', 'Intermedio', 5),
('Puente de Glúteos', 'Acostado boca arriba, levanta la cadera contrayendo los glúteos.', 'puente_gluteos.jpg', 'Piernas', 'Principiante', 4),
('Saltos en Tijera', 'Desde posición erguida, salta abriendo piernas y brazos simultáneamente.', 'saltos_tijera.jpg', 'Cardio', 'Principiante', 7),
('Sentadillas', 'Ponte de pie con pies al ancho de hombros, baja como si fueras a sentarte y sube.', 'sentadillas.jpg', 'Piernas', 'Principiante', 5),
('Zancadas', 'Da un paso largo hacia adelante, baja la rodilla trasera casi al suelo y regresa.', 'zancadas.jpg', 'Piernas', 'Principiante', 6);

-- =============================================
-- INSERTAR RUTINAS BÁSICAS
-- =============================================
INSERT INTO rutina (nombre, descripcion, entrenador_id, tipo_rutina, nivel_dificultad, duracion_estimada_minutos, calorias_estimadas) VALUES 
('Cardio Básico para Principiantes', 'Rutina perfecta para principiantes que quieren mejorar su resistencia cardiovascular. Incluye ejercicios de bajo impacto ideales para empezar tu journey fitness.', NULL, 'Cardio', 'Principiante', 25, 180),
('Fuerza Total Completa', 'Entrenamiento completo de fuerza que trabaja todos los grupos musculares principales. Ideal para desarrollar masa muscular y tonificar el cuerpo de forma equilibrada.', 2, 'Fuerza', 'Intermedio', 45, 320),
('HIIT Quema Grasa Intenso', 'Entrenamiento de alta intensidad por intervalos. Perfecto para quemar grasa y mejorar la condición física en poco tiempo. Máxima eficiencia.', NULL, 'HIIT', 'Intermedio', 20, 280),
('Yoga y Movilidad Matutina', 'Sesión de yoga suave para empezar el día con energía. Combina estiramientos, respiración y relajación para preparar el cuerpo.', NULL, 'Yoga', 'Principiante', 30, 120),
('Abs y Core Definición', 'Entrenamiento especializado para fortalecer el core y definir los abdominales. Ejercicios progresivos y efectivos para un abdomen fuerte.', 2, 'Fuerza', 'Intermedio', 20, 150),
('Piernas y Glúteos Power', 'Rutina enfocada en el tren inferior. Perfecta para tonificar piernas y glúteos con ejercicios variados y efectivos que dan resultados visibles.', NULL, 'Fuerza', 'Principiante', 35, 250),
('Rutina Express 15 min', 'Entrenamiento rápido y efectivo para días ocupados. Combina cardio y fuerza en una sesión intensa pero corta.', NULL, 'Mixta', 'Intermedio', 15, 200),
('Full Body Principiante', 'Rutina completa para trabajar todo el cuerpo. Perfecta para quienes están comenzando su viaje fitness con ejercicios básicos pero efectivos.', 2, 'Mixta', 'Principiante', 40, 280);

-- =============================================
-- INSERTAR EJERCICIOS DE RUTINAS
-- =============================================

-- Rutina 1: Cardio Básico para Principiantes
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(1, 12, 1, 3, 20, 0, 30),  -- Saltos en Tijera
(1, 8, 2, 3, 15, 0, 45),   -- Mountain Climbers  
(1, 7, 3, 3, 10, 0, 60),   -- Jump Squats
(1, 13, 4, 2, 15, 0, 30),  -- Sentadillas
(1, 9, 5, 2, 0, 30, 45);   -- Plancha Frontal

-- Rutina 2: Fuerza Total Completa
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(2, 13, 1, 4, 15, 0, 90),  -- Sentadillas
(2, 6, 2, 3, 12, 0, 90),   -- Flexiones
(2, 11, 3, 3, 15, 0, 60),  -- Puente Glúteos
(2, 10, 4, 3, 12, 0, 90),  -- Press Militar
(2, 3, 5, 3, 12, 0, 60),   -- Curl Bíceps
(2, 5, 6, 3, 12, 0, 60);   -- Extensión Tríceps

-- Rutina 3: HIIT Quema Grasa Intenso
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(3, 2, 1, 4, 8, 0, 20),    -- Burpees
(3, 8, 2, 4, 20, 0, 20),   -- Mountain Climbers
(3, 7, 3, 4, 12, 0, 20),   -- Jump Squats
(3, 12, 4, 4, 25, 0, 20),  -- Saltos Tijera
(3, 6, 5, 3, 10, 0, 30);   -- Flexiones

-- Rutina 4: Yoga y Movilidad Matutina
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(4, 9, 1, 3, 0, 45, 30),   -- Plancha Frontal
(4, 11, 2, 3, 15, 0, 30),  -- Puente Glúteos
(4, 14, 3, 2, 10, 0, 45),  -- Zancadas
(4, 13, 4, 2, 12, 0, 30);  -- Sentadillas suaves

-- Rutina 5: Abs y Core Definición
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(5, 1, 1, 4, 20, 0, 45),   -- Abdominales Crunch
(5, 9, 2, 3, 0, 45, 60),   -- Plancha Frontal
(5, 8, 3, 3, 15, 0, 45),   -- Mountain Climbers
(5, 11, 4, 3, 20, 0, 30);  -- Puente Glúteos

-- Rutina 6: Piernas y Glúteos Power
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(6, 13, 1, 4, 20, 0, 90),  -- Sentadillas
(6, 14, 2, 3, 12, 0, 90),  -- Zancadas
(6, 11, 3, 4, 15, 0, 60),  -- Puente Glúteos
(6, 7, 4, 3, 10, 0, 75);   -- Jump Squats

-- Rutina 7: Rutina Express 15 min
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(7, 2, 1, 3, 5, 0, 15),    -- Burpees
(7, 13, 2, 3, 15, 0, 15),  -- Sentadillas
(7, 6, 3, 3, 10, 0, 15),   -- Flexiones
(7, 8, 4, 2, 20, 0, 15);   -- Mountain Climbers

-- Rutina 8: Full Body Principiante
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(8, 13, 1, 3, 12, 0, 60),  -- Sentadillas
(8, 6, 2, 3, 8, 0, 60),    -- Flexiones (modificadas)
(8, 1, 3, 3, 15, 0, 45),   -- Abdominales
(8, 14, 4, 2, 8, 0, 60),   -- Zancadas
(8, 9, 5, 2, 0, 20, 45),   -- Plancha
(8, 11, 6, 3, 12, 0, 45);  -- Puente Glúteos

-- =============================================
-- ASIGNAR ALGUNAS RUTINAS A USUARIOS DE PRUEBA
-- =============================================
INSERT INTO rutina_asignada (rutina_id, usuario_id, estado, asignado_por) VALUES
(1, 4, 'ACTIVA', 2),    -- Cardio Básico para Usuario Test
(4, 4, 'ACTIVA', NULL), -- Yoga Matutino para Usuario Test
(8, 6, 'ACTIVA', 2),    -- Full Body para Sofia
(1, 7, 'COMPLETADA', 2), -- Cardio completado por Miguel
(6, 8, 'ACTIVA', NULL); -- Piernas y Glúteos para Carmen

-- =============================================
-- INSERTAR REGISTROS DE APROBACIÓN
-- =============================================
INSERT INTO registro_aprobaciones (usuario_id, admin_id, accion, comentarios) VALUES
(4, 1, 'Aprobado', 'Usuario registrado correctamente'),
(6, 1, 'Aprobado', 'Perfil completo y verificado'),
(7, 1, 'Aprobado', 'Documentación en orden');

-- =============================================
-- INSERTAR BOLETINES DE PRUEBA
-- =============================================
INSERT INTO boletin_informativo (asunto, contenido, tipo_destinatario, estado_envio, total_destinatarios, enviados_exitosos, creado_por) VALUES
('Bienvenida a FlowFit', 'Estimado {{nombre}},\n\n¡Bienvenido a FlowFit! Estamos emocionados de acompañarte en tu journey fitness.\n\nTu cuenta ha sido activada exitosamente. Ahora puedes:\n- Acceder a rutinas personalizadas\n- Realizar seguimiento de tu progreso\n- Conectar con entrenadores certificados\n\n¡Empecemos a entrenar!\n\nSaludos,\nEl equipo FlowFit', 'USUARIOS', 'COMPLETADO', 3, 3, 'Admin FlowFit'),

('Nuevas Rutinas de HIIT Disponibles', 'Hola {{nombre}},\n\n¡Tenemos excelentes noticias! Hemos agregado nuevas rutinas de entrenamiento HIIT de alta intensidad.\n\nEstas rutinas están diseñadas para:\n- Quemar grasa de forma eficiente\n- Mejorar tu resistencia cardiovascular\n- Fortalecer todo tu cuerpo en sesiones cortas\n\nVisita tu panel de usuario para explorar las nuevas opciones.\n\n¡A entrenar!\nEquipo FlowFit', 'TODOS', 'PENDIENTE', 0, 0, 'Admin FlowFit'),

('Recordatorio: Tu Progreso Semanal', 'Hola {{nombre}},\n\nEs momento de revisar tu progreso de esta semana.\n\n¿Has completado tus rutinas asignadas?\n¿Necesitas ajustar algún ejercicio?\n\nRecuerda que la consistencia es clave para alcanzar tus objetivos fitness.\n\nTu entrenador está disponible para cualquier consulta.\n\nSigue así,\nFlowFit', 'USUARIOS_ACTIVOS', 'PENDIENTE', 0, 0, 'Carlos Rodríguez');

-- =============================================
-- VISTAS ÚTILES PARA CONSULTAS
-- =============================================

-- Vista de rutinas con información completa
CREATE VIEW vista_rutinas_completas AS
SELECT 
    r.id,
    r.nombre,
    r.descripcion,
    r.tipo_rutina,
    r.nivel_dificultad,
    r.duracion_estimada_minutos,
    r.calorias_estimadas,
    u.nombre as entrenador_nombre,
    COUNT(re.ejercicio_id) as total_ejercicios
FROM rutina r
LEFT JOIN usuario u ON r.entrenador_id = u.id
LEFT JOIN rutina_ejercicio re ON r.id = re.rutina_id
WHERE r.activa = TRUE
GROUP BY r.id, r.nombre, r.descripcion, r.tipo_rutina, r.nivel_dificultad, 
         r.duracion_estimada_minutos, r.calorias_estimadas, u.nombre;

-- Vista de progreso de usuarios
CREATE VIEW vista_progreso_usuarios AS
SELECT 
    u.id as usuario_id,
    u.nombre as usuario_nombre,
    COUNT(ra.id) as rutinas_asignadas,
    SUM(CASE WHEN ra.estado = 'COMPLETADA' THEN 1 ELSE 0 END) as rutinas_completadas,
    SUM(CASE WHEN ra.estado = 'ACTIVA' THEN 1 ELSE 0 END) as rutinas_activas,
    ROUND((SUM(CASE WHEN ra.estado = 'COMPLETADA' THEN 1 ELSE 0 END) * 100.0 / COUNT(ra.id)), 2) as porcentaje_completadas
FROM usuario u
LEFT JOIN rutina_asignada ra ON u.id = ra.usuario_id
WHERE u.perfil_usuario = 'Usuario'
GROUP BY u.id, u.nombre;

-- Vista de estadísticas de boletines
CREATE VIEW vista_estadisticas_boletines AS
SELECT 
    DATE(fecha_creacion) as fecha,
    COUNT(*) as total_boletines,
    SUM(CASE WHEN estado_envio = 'COMPLETADO' THEN 1 ELSE 0 END) as completados,
    SUM(CASE WHEN estado_envio = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes,
    SUM(CASE WHEN estado_envio = 'FALLIDO' THEN 1 ELSE 0 END) as fallidos,
    SUM(total_destinatarios) as total_destinatarios,
    SUM(enviados_exitosos) as total_exitosos,
    SUM(enviados_fallidos) as total_fallidos
FROM boletin_informativo
GROUP BY DATE(fecha_creacion)
ORDER BY fecha DESC;

-- =============================================
-- CONSULTAS DE VERIFICACIÓN
-- =============================================
SELECT 'USUARIOS' as tabla, COUNT(*) as registros FROM usuario
UNION ALL
SELECT 'PASSWORD_TOKENS' as tabla, COUNT(*) as registros FROM password_reset_token
UNION ALL
SELECT 'EJERCICIOS' as tabla, COUNT(*) as registros FROM ejercicio_catalogo
UNION ALL
SELECT 'RUTINAS' as tabla, COUNT(*) as registros FROM rutina
UNION ALL
SELECT 'RUTINA_EJERCICIO' as tabla, COUNT(*) as registros FROM rutina_ejercicio
UNION ALL
SELECT 'RUTINAS_ASIGNADAS' as tabla, COUNT(*) as registros FROM rutina_asignada
UNION ALL
SELECT 'BOLETINES' as tabla, COUNT(*) as registros FROM boletin_informativo;

-- Mostrar estructura final de la base de datos
SHOW TABLES;

-- Verificar usuarios creados
SELECT id, numero_documento, nombre, correo, perfil_usuario, estado FROM usuario;

-- Verificar boletines creados
SELECT id, asunto, tipo_destinatario, estado_envio, total_destinatarios, creado_por FROM boletin_informativo;

-- =============================================
-- FIN DEL SCRIPT
-- =============================================

-- =============================================
-- FLOWFIT DATABASE - SCRIPT COMPLETO CON CHAT Y CONTRATACIÓN
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
-- RELACIÓN RUTINA-EJERCICIO
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
    INDEX idx_orden (rutina_id, orden)
);

-- =============================================
-- RUTINAS ASIGNADAS
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
-- PROGRESO DE EJERCICIOS
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
-- SISTEMA DE CHAT Y CONTRATACIÓN
-- =============================================

-- 1. TABLA DE CONVERSACIONES (CHATS)
CREATE TABLE conversacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    entrenador_id INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_mensaje DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVA', 'ARCHIVADA', 'BLOQUEADA') DEFAULT 'ACTIVA',
    usuario_ultimo_leido_id BIGINT DEFAULT NULL,
    entrenador_ultimo_leido_id BIGINT DEFAULT NULL,
    mensajes_no_leidos_usuario INT DEFAULT 0,
    mensajes_no_leidos_entrenador INT DEFAULT 0,
    INDEX idx_usuario (usuario_id),
    INDEX idx_entrenador (entrenador_id),
    INDEX idx_fecha_ultimo_mensaje (fecha_ultimo_mensaje),
    INDEX idx_estado (estado),
    UNIQUE KEY unique_conversacion (usuario_id, entrenador_id),
    CONSTRAINT fk_conversacion_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_conversacion_entrenador 
        FOREIGN KEY (entrenador_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. TABLA DE MENSAJES
CREATE TABLE mensaje (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    remitente_id INT NOT NULL,
    contenido TEXT NOT NULL,
    tipo_mensaje ENUM('TEXTO', 'IMAGEN', 'ARCHIVO', 'PROPUESTA_PLAN', 'PAGO_GENERADO', 'SISTEMA') DEFAULT 'TEXTO',
    
    -- Campos para envío de archivos
    archivo_url VARCHAR(500) DEFAULT NULL COMMENT 'URL del archivo subido',
    archivo_nombre VARCHAR(255) DEFAULT NULL COMMENT 'Nombre original del archivo',
    archivo_tipo VARCHAR(100) DEFAULT NULL COMMENT 'MIME type del archivo',
    archivo_tamano BIGINT DEFAULT NULL COMMENT 'Tamaño del archivo en bytes',
    
    fecha_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    leido BOOLEAN DEFAULT FALSE,
    fecha_lectura DATETIME DEFAULT NULL,
    editado BOOLEAN DEFAULT FALSE,
    fecha_edicion DATETIME DEFAULT NULL,
    eliminado BOOLEAN DEFAULT FALSE,
    metadata JSON DEFAULT NULL,
    INDEX idx_conversacion (conversacion_id),
    INDEX idx_remitente (remitente_id),
    INDEX idx_fecha_envio (fecha_envio),
    INDEX idx_leido (leido),
    INDEX idx_tipo (tipo_mensaje),
    CONSTRAINT fk_mensaje_conversacion 
        FOREIGN KEY (conversacion_id) 
        REFERENCES conversacion(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_remitente 
        FOREIGN KEY (remitente_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. TABLA DE PLANES DEL ENTRENADOR (SISTEMA HÍBRIDO)
CREATE TABLE plan_entrenador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entrenador_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio_mensual DECIMAL(10,2) NOT NULL,
    rango_precio_min DECIMAL(10,2) DEFAULT NULL COMMENT 'Precio mínimo para personalización',
    rango_precio_max DECIMAL(10,2) DEFAULT NULL COMMENT 'Precio máximo para personalización',
    duracion_dias INT DEFAULT 30,
    rutinas_mes INT DEFAULT NULL,
    seguimiento_semanal BOOLEAN DEFAULT FALSE,
    chat_directo BOOLEAN DEFAULT TRUE,
    videollamadas_mes INT DEFAULT 0,
    plan_nutricional BOOLEAN DEFAULT FALSE,
    es_publico BOOLEAN DEFAULT TRUE,
    permite_personalizacion BOOLEAN DEFAULT TRUE COMMENT 'Permite negociación inteligente',
    destacado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_entrenador (entrenador_id),
    INDEX idx_es_publico (es_publico),
    INDEX idx_destacado (destacado),
    INDEX idx_activo (activo),
    INDEX idx_permite_personalizacion (permite_personalizacion),
    CONSTRAINT fk_plan_entrenador 
        FOREIGN KEY (entrenador_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TABLA DE CONTRATACIONES (SISTEMA HÍBRIDO CON NEGOCIACIÓN INTELIGENTE)
CREATE TABLE contratacion_entrenador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    entrenador_id INT NOT NULL,
    plan_base_id INT DEFAULT NULL,
    tipo_contratacion ENUM('PLAN_FIJO', 'PERSONALIZADO') DEFAULT 'PLAN_FIJO' COMMENT 'Compra directa o con negociación',
    estado ENUM(
        'PENDIENTE_APROBACION',
        'NEGOCIACION',
        'PROPUESTA_FINAL',
        'PENDIENTE_PAGO',
        'PAGO_PROCESANDO',
        'ACTIVA',
        'PAUSADA',
        'VENCIDA',
        'CANCELADA',
        'RECHAZADA'
    ) DEFAULT 'PENDIENTE_APROBACION',
    precio_acordado DECIMAL(10,2) NOT NULL,
    descuento_aplicado DECIMAL(10,2) DEFAULT 0.00,
    comision_plataforma_porcentaje DECIMAL(5,2) DEFAULT 10.00,
    monto_entrenador DECIMAL(10,2) DEFAULT NULL,
    monto_comision DECIMAL(10,2) DEFAULT NULL,
    duracion_dias_acordada INT NOT NULL,
    rutinas_mes_acordadas INT DEFAULT NULL,
    seguimiento_semanal_acordado BOOLEAN DEFAULT FALSE,
    chat_directo_acordado BOOLEAN DEFAULT TRUE,
    videollamadas_mes_acordadas INT DEFAULT 0,
    plan_nutricional_acordado BOOLEAN DEFAULT FALSE,
    servicios_adicionales TEXT DEFAULT NULL,
    version_negociacion INT DEFAULT 1,
    rondas_negociacion INT DEFAULT 0 COMMENT 'Contador de rondas (máximo 3)',
    porcentaje_variacion_permitido DECIMAL(5,2) DEFAULT 30.00 COMMENT 'Variación permitida según ronda',
    ultima_propuesta_de ENUM('USUARIO', 'ENTRENADOR') DEFAULT NULL,
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion DATETIME DEFAULT NULL,
    fecha_inicio DATETIME DEFAULT NULL,
    fecha_fin DATETIME DEFAULT NULL,
    fecha_cancelacion DATETIME DEFAULT NULL,
    nota_usuario TEXT DEFAULT NULL,
    nota_entrenador TEXT DEFAULT NULL,
    razon_cancelacion TEXT DEFAULT NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_entrenador (entrenador_id),
    INDEX idx_plan_base (plan_base_id),
    INDEX idx_tipo_contratacion (tipo_contratacion),
    INDEX idx_estado (estado),
    INDEX idx_fecha_solicitud (fecha_solicitud),
    INDEX idx_fecha_inicio (fecha_inicio),
    INDEX idx_fecha_fin (fecha_fin),
    CONSTRAINT fk_contratacion_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_contratacion_entrenador 
        FOREIGN KEY (entrenador_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_contratacion_plan_base 
        FOREIGN KEY (plan_base_id) 
        REFERENCES plan_entrenador(id) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. TABLA DE HISTORIAL DE NEGOCIACIÓN (CON SISTEMA INTELIGENTE)
CREATE TABLE historial_negociacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contratacion_id BIGINT NOT NULL,
    version INT NOT NULL,
    ronda_numero INT DEFAULT 1 COMMENT 'Número de ronda (1-3)',
    propuesto_por ENUM('USUARIO', 'ENTRENADOR') NOT NULL,
    precio_propuesto DECIMAL(10,2) NOT NULL,
    precio_base_referencia DECIMAL(10,2) NOT NULL COMMENT 'Precio base del plan',
    porcentaje_variacion DECIMAL(5,2) DEFAULT NULL COMMENT 'Variación respecto al precio base',
    duracion_propuesta INT NOT NULL,
    servicios_propuestos JSON DEFAULT NULL,
    estado_propuesta ENUM('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CONTRAOFERTA', 'PROPUESTA_FINAL') DEFAULT 'PENDIENTE',
    mensaje TEXT DEFAULT NULL,
    es_ultima_ronda BOOLEAN DEFAULT FALSE COMMENT 'Marca si es la última oportunidad',
    fecha_propuesta DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta DATETIME DEFAULT NULL,
    INDEX idx_contratacion (contratacion_id),
    INDEX idx_version (version),
    INDEX idx_ronda (ronda_numero),
    INDEX idx_estado (estado_propuesta),
    CONSTRAINT fk_historial_contratacion 
        FOREIGN KEY (contratacion_id) 
        REFERENCES contratacion_entrenador(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. TABLA DE PAGOS (MERCADOPAGO) CON SISTEMA ESCROW
CREATE TABLE pago_contratacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contratacion_id BIGINT NOT NULL,
    usuario_id INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'COP',
    estado_pago ENUM(
        'PENDIENTE',
        'PROCESANDO',
        'APROBADO',
        'RECHAZADO',
        'CANCELADO',
        'REEMBOLSADO',
        'EN_MEDIACION',
        'EXPIRADO'
    ) DEFAULT 'PENDIENTE',
    
    -- SISTEMA DE ESCROW (Retención de pagos)
    estado_escrow ENUM(
        'RETENIDO',              -- Dinero retenido esperando confirmaciones
        'ESPERANDO_USUARIO',     -- Esperando confirmación del usuario
        'ESPERANDO_ENTRENADOR',  -- Esperando confirmación del entrenador
        'DISPUTA',               -- Hay una disputa activa
        'LIBERADO',              -- Dinero liberado al entrenador
        'REEMBOLSADO'            -- Dinero devuelto al usuario
    ) DEFAULT 'RETENIDO',
    
    usuario_confirma_servicio BOOLEAN DEFAULT FALSE,
    entrenador_confirma_servicio BOOLEAN DEFAULT FALSE,
    fecha_confirmacion_usuario DATETIME DEFAULT NULL,
    fecha_confirmacion_entrenador DATETIME DEFAULT NULL,
    fecha_liberacion_fondos DATETIME DEFAULT NULL,
    fecha_limite_disputa DATETIME DEFAULT NULL,  -- 7 días después del fin del contrato
    
    -- Información de la disputa
    disputa_activa BOOLEAN DEFAULT FALSE,
    disputa_iniciada_por ENUM('USUARIO', 'ENTRENADOR', 'ADMIN') DEFAULT NULL,
    disputa_razon TEXT DEFAULT NULL,
    disputa_fecha DATETIME DEFAULT NULL,
    disputa_resuelta BOOLEAN DEFAULT FALSE,
    disputa_resolucion TEXT DEFAULT NULL,
    disputa_fecha_resolucion DATETIME DEFAULT NULL,
    
    -- MercadoPago
    mp_preference_id VARCHAR(255) DEFAULT NULL,
    mp_payment_id VARCHAR(255) DEFAULT NULL,
    mp_init_point TEXT DEFAULT NULL,
    mp_external_reference VARCHAR(255) DEFAULT NULL,
    mp_status VARCHAR(50) DEFAULT NULL,
    mp_status_detail VARCHAR(100) DEFAULT NULL,
    mp_payment_method VARCHAR(50) DEFAULT NULL,
    mp_payment_type VARCHAR(50) DEFAULT NULL,
    mp_webhook_data JSON DEFAULT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME DEFAULT NULL,
    fecha_pago DATETIME DEFAULT NULL,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_contratacion (contratacion_id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_estado_pago (estado_pago),
    INDEX idx_estado_escrow (estado_escrow),
    INDEX idx_disputa_activa (disputa_activa),
    INDEX idx_mp_preference_id (mp_preference_id),
    INDEX idx_mp_payment_id (mp_payment_id),
    INDEX idx_mp_external_reference (mp_external_reference),
    CONSTRAINT fk_pago_contratacion 
        FOREIGN KEY (contratacion_id) 
        REFERENCES contratacion_entrenador(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_pago_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. TABLA DE RENOVACIONES DE CONTRATO
CREATE TABLE historial_renovacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contratacion_id BIGINT NOT NULL,
    contratacion_anterior_id BIGINT DEFAULT NULL,
    fecha_renovacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    precio_renovacion DECIMAL(10,2) NOT NULL,
    duracion_dias INT NOT NULL,
    nota TEXT DEFAULT NULL,
    INDEX idx_contratacion (contratacion_id),
    INDEX idx_fecha_renovacion (fecha_renovacion),
    CONSTRAINT fk_renovacion_contratacion 
        FOREIGN KEY (contratacion_id) 
        REFERENCES contratacion_entrenador(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. TABLA DE CONFIGURACIÓN MERCADOPAGO
CREATE TABLE mercadopago_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entrenador_id INT DEFAULT NULL,
    access_token VARCHAR(500) NOT NULL,
    public_key VARCHAR(500) NOT NULL,
    modo ENUM('SANDBOX', 'PRODUCCION') DEFAULT 'SANDBOX',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_entrenador (entrenador_id),
    CONSTRAINT fk_mp_config_entrenador 
        FOREIGN KEY (entrenador_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. RELACIÓN CONVERSACIÓN-CONTRATACIÓN
CREATE TABLE conversacion_contratacion (
    conversacion_id BIGINT NOT NULL,
    contratacion_id BIGINT NOT NULL,
    fecha_vinculacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversacion_id, contratacion_id),
    CONSTRAINT fk_conv_contratacion_conversacion 
        FOREIGN KEY (conversacion_id) 
        REFERENCES conversacion(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_conv_contratacion_contratacion 
        FOREIGN KEY (contratacion_id) 
        REFERENCES contratacion_entrenador(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- INSERTAR USUARIOS DE PRUEBA
-- =============================================
INSERT INTO usuario (numero_documento, nombre, telefono, correo, clave, perfil_usuario, estado) VALUES
('1001', 'Admin FlowFit', '1111111111', 'admin@flowfit.com', 'admin123', 'Administrador', 'A'),
('2001', 'Carlos Rodríguez', '2222222222', 'entrenador@flowfit.com', 'entrenador123', 'Entrenador', 'A'),
('3001', 'María González', '3333333333', 'nutricionista@flowfit.com', 'nutri123', 'Nutricionista', 'A'),
('4001', 'Juan Pérez', '4444444444', 'usuario@flowfit.com', 'usuario123', 'Usuario', 'A'),
('5001', 'Carlos Gómez', '5551110001', 'carlos.gomez@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5002', 'Laura Ruiz', '5551110002', 'laura.ruiz@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5003', 'Pedro Díaz', '5551110003', 'pedro.diaz@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5004', 'Ana Torres', '5551110004', 'ana.torres@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5005', 'Diego Marín', '5551110005', 'diego.marin@flowfit.com', 'clave123', 'Entrenador', 'I'),
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
('Cardio Básico para Principiantes', 'Rutina perfecta para principiantes que quieren mejorar su resistencia cardiovascular.', NULL, 'Cardio', 'Principiante', 25, 180),
('Fuerza Total Completa', 'Entrenamiento completo de fuerza que trabaja todos los grupos musculares principales.', 2, 'Fuerza', 'Intermedio', 45, 320),
('HIIT Quema Grasa Intenso', 'Entrenamiento de alta intensidad por intervalos.', NULL, 'HIIT', 'Intermedio', 20, 280),
('Yoga y Movilidad Matutina', 'Sesión de yoga suave para empezar el día con energía.', NULL, 'Yoga', 'Principiante', 30, 120),
('Abs y Core Definición', 'Entrenamiento especializado para fortalecer el core.', 2, 'Fuerza', 'Intermedio', 20, 150),
('Piernas y Glúteos Power', 'Rutina enfocada en el tren inferior.', NULL, 'Fuerza', 'Principiante', 35, 250),
('Rutina Express 15 min', 'Entrenamiento rápido y efectivo para días ocupados.', NULL, 'Mixta', 'Intermedio', 15, 200),
('Full Body Principiante', 'Rutina completa para trabajar todo el cuerpo.', 2, 'Mixta', 'Principiante', 40, 280);

-- =============================================
-- INSERTAR EJERCICIOS DE RUTINAS
-- =============================================
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, orden, series, repeticiones, duracion_segundos, descanso_segundos) VALUES
(1, 12, 1, 3, 20, 0, 30), (1, 8, 2, 3, 15, 0, 45), (1, 7, 3, 3, 10, 0, 60), (1, 13, 4, 2, 15, 0, 30), (1, 9, 5, 2, 0, 30, 45),
(2, 13, 1, 4, 15, 0, 90), (2, 6, 2, 3, 12, 0, 90), (2, 11, 3, 3, 15, 0, 60), (2, 10, 4, 3, 12, 0, 90), (2, 3, 5, 3, 12, 0, 60), (2, 5, 6, 3, 12, 0, 60),
(3, 2, 1, 4, 8, 0, 20), (3, 8, 2, 4, 20, 0, 20), (3, 7, 3, 4, 12, 0, 20), (3, 12, 4, 4, 25, 0, 20), (3, 6, 5, 3, 10, 0, 30),
(4, 9, 1, 3, 0, 45, 30), (4, 11, 2, 3, 15, 0, 30), (4, 14, 3, 2, 10, 0, 45), (4, 13, 4, 2, 12, 0, 30),
(5, 1, 1, 4, 20, 0, 45), (5, 9, 2, 3, 0, 45, 60), (5, 8, 3, 3, 15, 0, 45), (5, 11, 4, 3, 20, 0, 30),
(6, 13, 1, 4, 20, 0, 90), (6, 14, 2, 3, 12, 0, 90), (6, 11, 3, 4, 15, 0, 60), (6, 7, 4, 3, 10, 0, 75),
(7, 2, 1, 3, 5, 0, 15), (7, 13, 2, 3, 15, 0, 15), (7, 6, 3, 3, 10, 0, 15), (7, 8, 4, 2, 20, 0, 15),
(8, 13, 1, 3, 12, 0, 60), (8, 6, 2, 3, 8, 0, 60), (8, 1, 3, 3, 15, 0, 45), (8, 14, 4, 2, 8, 0, 60), (8, 9, 5, 2, 0, 20, 45), (8, 11, 6, 3, 12, 0, 45);

-- =============================================
-- ASIGNAR RUTINAS A USUARIOS
-- =============================================
INSERT INTO rutina_asignada (rutina_id, usuario_id, estado, progreso, veces_completada, fecha_asignacion, fecha_completada, asignado_por) VALUES
(1, 4, 'ACTIVA', 65, 3, DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, 2),
(4, 4, 'ACTIVA', 40, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), NULL, NULL),
(8, 6, 'ACTIVA', 80, 5, DATE_SUB(NOW(), INTERVAL 30 DAY), NULL, 2),
(1, 7, 'COMPLETADA', 100, 8, DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 2),
(6, 8, 'ACTIVA', 50, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL),
(2, 4, 'COMPLETADA', 100, 6, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 2),
(5, 6, 'ACTIVA', 70, 4, DATE_SUB(NOW(), INTERVAL 18 DAY), NULL, 2);

-- =============================================
-- INSERTAR PROGRESO DE EJERCICIOS (DATOS DE EJEMPLO)
-- =============================================
-- Progreso de Juan Pérez (ID:4) - Última semana
INSERT INTO progreso_ejercicio (usuario_id, rutina_asignada_id, ejercicio_id, fecha, series_completadas, repeticiones_realizadas, peso_utilizado, comentarios) VALUES
-- Hace 7 días
(4, 1, 12, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 3, 60, NULL, 'Primera sesión, buen ritmo'),
(4, 1, 8, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 3, 45, NULL, 'Intensidad moderada'),
-- Hace 6 días
(4, 1, 7, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 3, 30, NULL, 'Sentí buen trabajo en piernas'),
(4, 1, 13, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 2, 30, 10.0, 'Progreso en fuerza'),
-- Hace 5 días
(4, 4, 9, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 2, 90, NULL, 'Mantuve postura'),
(4, 4, 11, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 3, 45, NULL, 'Activación de glúteos excelente'),
-- Hace 4 días
(4, 1, 6, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, 36, NULL, 'Mejorando técnica'),
(4, 1, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4, 80, NULL, 'Core más fuerte'),
-- Hace 3 días
(4, 1, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 4, 32, NULL, 'Cardio intenso, muy cansado'),
(4, 1, 8, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 4, 80, NULL, 'Aumenté repeticiones'),
-- Hace 2 días  
(4, 4, 13, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 45, 15.0, 'Subí peso'),
(4, 4, 14, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, 24, NULL, 'Piernas quemando'),
-- Ayer
(4, 1, 12, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, 75, NULL, 'Excelente sesión'),
(4, 1, 9, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, 135, NULL, 'Plancha más tiempo'),
-- Hoy
(4, 1, 6, CURDATE(), 3, 40, NULL, 'Sesión matutina'),
(4, 1, 11, CURDATE(), 3, 50, NULL, 'En progreso...'),

-- Progreso de Sofia López (ID:6) - Última semana
(6, 3, 2, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 4, 40, NULL, 'HIIT muy intenso'),
(6, 3, 8, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 4, 100, NULL, 'Cardio al máximo'),
(6, 3, 7, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 4, 48, NULL, 'Piernas fuertes'),
(6, 5, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4, 80, NULL, 'Abs definición'),
(6, 5, 9, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, 135, NULL, 'Core resistente'),
(6, 3, 6, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3, 36, NULL, 'Pecho trabajando'),
(6, 5, 11, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 60, NULL, 'Glúteos activados'),
(6, 3, 12, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 4, 100, NULL, 'Cardio completo'),
(6, 5, 1, CURDATE(), 4, 85, NULL, 'Mejorando core'),

-- Progreso de Miguel Torres (ID:7) - Última semana
(7, 4, 9, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 3, 120, NULL, 'Plancha estable'),
(7, 4, 13, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 2, 30, 20.0, 'Sentadillas con peso'),
(7, 4, 11, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3, 45, NULL, 'Puente glúteos'),
(7, 4, 14, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, 20, NULL, 'Zancadas controladas'),
(7, 4, 6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, 30, NULL, 'Flexiones mejoradas');

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
-- =============================================
-- PLANES DEL ENTRENADOR CARLOS (SISTEMA HÍBRIDO)
-- =============================================
INSERT INTO plan_entrenador (entrenador_id, nombre, descripcion, precio_mensual, rango_precio_min, rango_precio_max, duracion_dias, rutinas_mes, seguimiento_semanal, chat_directo, videollamadas_mes, plan_nutricional, es_publico, permite_personalizacion, destacado) VALUES
(2, 'Plan Básico', 'Plan perfecto para empezar tu transformación. Ideal para principiantes que buscan establecer hábitos saludables.', 599.00, 419.30, 778.70, 30, 4, FALSE, TRUE, 0, FALSE, TRUE, TRUE, FALSE),
(2, 'Plan Premium', 'Plan completo con seguimiento personalizado, rutinas adaptadas y nutrición incluida. Perfecto para resultados serios.', 1299.00, 909.30, 1688.70, 30, 8, TRUE, TRUE, 2, TRUE, TRUE, TRUE, TRUE),
(2, 'Plan Elite', 'Transformación total con atención personalizada VIP. Incluye todo lo necesario para alcanzar tus objetivos.', 2499.00, 1749.30, 3248.70, 30, 12, TRUE, TRUE, 4, TRUE, TRUE, TRUE, TRUE);

-- =============================================
-- CONVERSACIONES DE PRUEBA
-- =============================================
INSERT INTO conversacion (usuario_id, entrenador_id, fecha_ultimo_mensaje, estado) VALUES
(4, 2, NOW(), 'ACTIVA'), (6, 2, NOW(), 'ACTIVA'), (7, 2, NOW(), 'ACTIVA');

-- Mensajes de prueba
INSERT INTO mensaje (conversacion_id, remitente_id, contenido, tipo_mensaje, leido, fecha_lectura) VALUES
(1, 4, 'Hola Carlos! Me interesa empezar a entrenar contigo. ¿Qué planes tienes?', 'TEXTO', TRUE, NOW()),
(1, 2, 'Hola Juan! 😊 Tengo 3 opciones: Básico ($599), Premium ($1,299) y Personalizado ($2,499).', 'TEXTO', TRUE, NOW()),
(1, 4, 'Quiero perder peso y ganar masa muscular. ¿El Premium incluye nutrición?', 'TEXTO', TRUE, NOW()),
(1, 2, 'Perfecto! Sí, el Premium incluye plan nutricional y 2 videollamadas al mes.', 'TEXTO', FALSE, NULL);

-- Contrataciones de ejemplo (SISTEMA HÍBRIDO)
-- Contratación 1: Compra directa (sin negociación)
INSERT INTO contratacion_entrenador (usuario_id, entrenador_id, plan_base_id, tipo_contratacion, estado, precio_acordado, duracion_dias_acordada, rutinas_mes_acordadas, seguimiento_semanal_acordado, chat_directo_acordado, videollamadas_mes_acordadas, plan_nutricional_acordado, version_negociacion, rondas_negociacion, ultima_propuesta_de, nota_usuario) VALUES
(6, 2, 1, 'PLAN_FIJO', 'PENDIENTE_PAGO', 599.00, 30, 4, FALSE, TRUE, 0, FALSE, 1, 0, 'USUARIO', 'Compra directa del Plan Básico. ¡Listo para empezar!');

-- Contratación 2: Con negociación personalizada
INSERT INTO contratacion_entrenador (usuario_id, entrenador_id, plan_base_id, tipo_contratacion, estado, precio_acordado, duracion_dias_acordada, rutinas_mes_acordadas, seguimiento_semanal_acordado, chat_directo_acordado, videollamadas_mes_acordadas, plan_nutricional_acordado, version_negociacion, rondas_negociacion, porcentaje_variacion_permitido, ultima_propuesta_de, nota_usuario) VALUES
(7, 2, 2, 'PERSONALIZADO', 'NEGOCIACION', 1299.00, 30, 10, TRUE, TRUE, 2, TRUE, 1, 1, 30.00, 'USUARIO', 'Quiero el Premium pero con más rutinas mensuales.');

-- Historial de negociación (SISTEMA INTELIGENTE)
-- Ronda 1: Usuario propone personalización
INSERT INTO historial_negociacion (contratacion_id, version, ronda_numero, propuesto_por, precio_propuesto, precio_base_referencia, porcentaje_variacion, duracion_propuesta, mensaje, estado_propuesta, es_ultima_ronda, servicios_propuestos) VALUES
(2, 1, 1, 'USUARIO', 1299.00, 1299.00, 0.00, 30, 'Me interesa el Plan Premium. ¿Podríamos ajustar las rutinas a 10 en lugar de 8? Estoy dispuesto a pagar el mismo precio.', 'PENDIENTE', FALSE, JSON_OBJECT('rutinas_mes', 10, 'seguimiento_semanal', TRUE, 'videollamadas_mes', 2, 'plan_nutricional', TRUE, 'chat_directo', TRUE));

-- Historial de negociación
INSERT INTO historial_negociacion (contratacion_id, version, propuesto_por, precio_propuesto, duracion_propuesta, mensaje, estado_propuesta, servicios_propuestos) VALUES
(1, 1, 'USUARIO', 1299.00, 30, 'Me gustaría iniciar con el Plan Premium completo', 'PENDIENTE', JSON_OBJECT('rutinas_mes', NULL, 'seguimiento_semanal', TRUE, 'videollamadas_mes', 2, 'plan_nutricional', TRUE, 'chat_directo', TRUE));

-- =============================================
-- TRIGGERS
-- =============================================

DELIMITER //
CREATE TRIGGER after_mensaje_insert
AFTER INSERT ON mensaje
FOR EACH ROW
BEGIN
    DECLARE es_entrenador BOOLEAN;
    SELECT (NEW.remitente_id = c.entrenador_id) INTO es_entrenador FROM conversacion c WHERE c.id = NEW.conversacion_id;
    UPDATE conversacion SET fecha_ultimo_mensaje = NEW.fecha_envio WHERE id = NEW.conversacion_id;
    IF es_entrenador THEN
        UPDATE conversacion SET mensajes_no_leidos_usuario = mensajes_no_leidos_usuario + 1 WHERE id = NEW.conversacion_id;
    ELSE
        UPDATE conversacion SET mensajes_no_leidos_entrenador = mensajes_no_leidos_entrenador + 1 WHERE id = NEW.conversacion_id;
    END IF;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER before_contratacion_aprobacion
BEFORE UPDATE ON contratacion_entrenador
FOR EACH ROW
BEGIN
    IF NEW.estado = 'ACTIVA' AND OLD.estado != 'ACTIVA' THEN
        SET NEW.monto_comision = NEW.precio_acordado * (NEW.comision_plataforma_porcentaje / 100);
        SET NEW.monto_entrenador = NEW.precio_acordado - NEW.monto_comision;
        IF NEW.fecha_inicio IS NULL THEN SET NEW.fecha_inicio = NOW(); END IF;
        IF NEW.fecha_fin IS NULL THEN SET NEW.fecha_fin = DATE_ADD(NEW.fecha_inicio, INTERVAL NEW.duracion_dias_acordada DAY); END IF;
    END IF;
END//
DELIMITER ;

-- TRIGGER: Liberar fondos automáticamente si ambos confirman el servicio
DELIMITER //
CREATE TRIGGER after_pago_confirmacion
AFTER UPDATE ON pago_contratacion
FOR EACH ROW
BEGIN
    -- Si ambos confirmaron el servicio y no hay disputa, liberar fondos
    IF NEW.usuario_confirma_servicio = TRUE 
       AND NEW.entrenador_confirma_servicio = TRUE 
       AND NEW.disputa_activa = FALSE
       AND NEW.estado_escrow = 'RETENIDO' THEN
        
        UPDATE pago_contratacion 
        SET estado_escrow = 'LIBERADO',
            fecha_liberacion_fondos = NOW()
        WHERE id = NEW.id;
        
        -- Enviar notificación al entrenador (se manejará en el backend)
    END IF;
    
    -- Si se inicia una disputa, cambiar estado
    IF NEW.disputa_activa = TRUE AND OLD.disputa_activa = FALSE THEN
        UPDATE pago_contratacion 
        SET estado_escrow = 'DISPUTA'
        WHERE id = NEW.id;
    END IF;
END//
DELIMITER ;

-- TRIGGER: Auto-liberar fondos después de 7 días si no hay disputa
DELIMITER //
CREATE TRIGGER check_auto_release_funds
AFTER UPDATE ON pago_contratacion
FOR EACH ROW
BEGIN
    -- Si pasó la fecha límite de disputa y no hay disputa activa, liberar automáticamente
    IF NEW.fecha_limite_disputa < NOW() 
       AND NEW.disputa_activa = FALSE 
       AND NEW.estado_escrow = 'RETENIDO' THEN
        
        UPDATE pago_contratacion 
        SET estado_escrow = 'LIBERADO',
            fecha_liberacion_fondos = NOW(),
            usuario_confirma_servicio = TRUE,
            entrenador_confirma_servicio = TRUE
        WHERE id = NEW.id;
    END IF;
END//
DELIMITER ;

-- =============================================
-- FIN DEL SCRIPT
-- =============================================
SELECT '✅ BASE DE DATOS FLOWFIT CREADA EXITOSAMENTE CON SISTEMA DE CHAT Y CONTRATACIÓN' as resultado;
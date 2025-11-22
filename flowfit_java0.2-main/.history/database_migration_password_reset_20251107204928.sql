-- ===================================================================
-- FLOWFIT - SCRIPT DE MIGRACIÓN
-- Tabla: password_reset_token
-- Propósito: Almacenar tokens temporales para recuperación de contraseña
-- Fecha: 2024-11-07
-- ===================================================================

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    usuario_id INT NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Índices para mejorar rendimiento
    INDEX idx_token (token),
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_fecha_expiracion (fecha_expiracion),
    
    -- Llave foránea con la tabla usuario
    CONSTRAINT fk_password_reset_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) 
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentarios de la tabla
ALTER TABLE password_reset_token COMMENT = 'Tokens temporales para recuperación de contraseña (válidos 15 minutos)';

-- ===================================================================
-- VERIFICACIÓN
-- ===================================================================
-- Ejecuta esta consulta para verificar que la tabla se creó correctamente:
-- SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES 
-- WHERE TABLE_SCHEMA = 'flowfit' AND TABLE_NAME = 'password_reset_token';

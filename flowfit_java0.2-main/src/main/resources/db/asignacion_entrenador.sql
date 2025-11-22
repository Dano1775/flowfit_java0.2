-- =============================================
-- TABLA DE ASIGNACION ENTRENADOR
-- =============================================
USE flowfit_db;

CREATE TABLE asignacion_entrenador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    entrenador_id INT NOT NULL,
    estado ENUM('PENDIENTE', 'ACEPTADA', 'RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aceptacion DATETIME NULL,
    mensaje_solicitud TEXT NULL,
    mensaje_respuesta TEXT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (entrenador_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_usuario_entrenador (usuario_id, entrenador_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar algunas asignaciones de prueba
INSERT INTO asignacion_entrenador (usuario_id, entrenador_id, estado, mensaje_solicitud)
SELECT 
    u.id as usuario_id, 
    e.id as entrenador_id,
    'ACEPTADA' as estado,
    'Solicitud de entrenamiento personal' as mensaje_solicitud
FROM usuario u 
CROSS JOIN usuario e
WHERE u.perfil_usuario = 'Usuario' 
AND e.perfil_usuario = 'Entrenador'
AND e.estado = 'A'
LIMIT 3;
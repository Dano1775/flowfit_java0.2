-- Agregar columnas para archivos adjuntos en la tabla mensaje
ALTER TABLE mensaje
ADD COLUMN archivo_url VARCHAR(500) NULL AFTER tipo_mensaje,
ADD COLUMN archivo_nombre VARCHAR(255) NULL AFTER archivo_url,
ADD COLUMN archivo_tipo VARCHAR(100) NULL AFTER archivo_nombre,
ADD COLUMN archivo_tamano BIGINT NULL AFTER archivo_tipo;

-- Verificar que las columnas se agregaron correctamente
DESCRIBE mensaje;

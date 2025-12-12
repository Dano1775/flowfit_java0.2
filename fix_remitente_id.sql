-- Script para permitir NULL en remitente_id para mensajes del sistema
-- Ejecutar este script en tu base de datos flowfit_db

USE flowfit_db;

-- Eliminar la foreign key constraint
ALTER TABLE mensaje DROP FOREIGN KEY fk_mensaje_remitente;

-- Modificar la columna para permitir NULL
ALTER TABLE mensaje MODIFY COLUMN remitente_id INT NULL;

-- Volver a crear la foreign key constraint
ALTER TABLE mensaje ADD CONSTRAINT fk_mensaje_remitente 
    FOREIGN KEY (remitente_id) 
    REFERENCES usuario(id) 
    ON DELETE CASCADE;

-- Actualizar mensajes existentes con remitente_id = 0 a NULL
UPDATE mensaje SET remitente_id = NULL WHERE remitente_id = 0;

SELECT 'Script ejecutado exitosamente' as status;

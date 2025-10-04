-- Insertar rutinas básicas para FlowFit
USE flowfit_db;

-- Rutina 1: Cardio Básico
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('Cardio Básico', 'Rutina perfecta para principiantes que quieren mejorar su resistencia cardiovascular. Incluye ejercicios de bajo impacto ideales para empezar.', NULL, '2024-01-15');

-- Rutina 2: Fuerza Total
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('Fuerza Total', 'Entrenamiento completo de fuerza que trabaja todos los grupos musculares principales. Ideal para desarrollar masa muscular y tonificar el cuerpo.', NULL, '2024-01-16');

-- Rutina 3: HIIT Intenso
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('HIIT Intenso', 'Entrenamiento de alta intensidad por intervalos. Perfecto para quemar grasa y mejorar la condición física en poco tiempo.', NULL, '2024-01-17');

-- Rutina 4: Yoga Matutino
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('Yoga Matutino', 'Sesión de yoga suave para empezar el día con energía. Combina estiramientos, respiración y relajación.', NULL, '2024-01-18');

-- Rutina 5: Abs y Core
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('Abs y Core', 'Entrenamiento especializado para fortalecer el core y definir los abdominales. Ejercicios progresivos y efectivos.', NULL, '2024-01-19');

-- Rutina 6: Piernas y Glúteos
INSERT INTO rutina (nombre, descripcion, entrenador_id, fecha_creacion) VALUES 
('Piernas y Glúteos', 'Rutina enfocada en el tren inferior. Perfecta para tonificar piernas y glúteos con ejercicios variados y efectivos.', NULL, '2024-01-20');

-- Obtener los IDs de las rutinas insertadas (asumiendo que empiezan desde ID 1)
-- Agregar ejercicios a la Rutina 1: Cardio Básico
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(1, 1, 1, 0, 300, 0, 60, 1),  -- 5 min de calentamiento
(1, 8, 3, 30, 0, 0, 30, 2),   -- Mountain Climbers
(1, 12, 3, 20, 0, 0, 30, 3),  -- Saltos Tijera
(1, 7, 3, 10, 0, 0, 60, 4);   -- Jump Squats

-- Agregar ejercicios a la Rutina 2: Fuerza Total
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(2, 13, 4, 15, 0, 0, 90, 1),  -- Sentadillas
(2, 6, 3, 12, 0, 0, 90, 2),   -- Flexiones
(2, 11, 3, 12, 0, 0, 90, 3),  -- Puente Glúteos
(2, 10, 3, 12, 0, 5, 90, 4);  -- Press Militar

-- Agregar ejercicios a la Rutina 3: HIIT Intenso
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(3, 2, 4, 10, 0, 0, 30, 1),   -- Burpees
(3, 8, 4, 20, 0, 0, 30, 2),   -- Mountain Climbers
(3, 7, 4, 15, 0, 0, 30, 3),   -- Jump Squats
(3, 12, 4, 30, 0, 0, 30, 4);  -- Saltos Tijera

-- Agregar ejercicios a la Rutina 4: Yoga Matutino
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(4, 9, 3, 0, 60, 0, 30, 1),   -- Plancha Frontal
(4, 11, 3, 15, 0, 0, 30, 2),  -- Puente Glúteos
(4, 14, 2, 10, 0, 0, 60, 3);  -- Zancadas

-- Agregar ejercicios a la Rutina 5: Abs y Core
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(5, 1, 4, 20, 0, 0, 45, 1),   -- Abdominales Crunch
(5, 9, 3, 0, 45, 0, 60, 2),   -- Plancha Frontal
(5, 8, 3, 15, 0, 0, 45, 3);   -- Mountain Climbers

-- Agregar ejercicios a la Rutina 6: Piernas y Glúteos
INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones, duracion, peso, descanso, orden) VALUES
(6, 13, 4, 20, 0, 0, 90, 1),  -- Sentadillas
(6, 14, 3, 12, 0, 0, 90, 2),  -- Zancadas
(6, 11, 3, 15, 0, 0, 90, 3),  -- Puente Glúteos
(6, 7, 3, 10, 0, 0, 60, 4);   -- Jump Squats

-- Verificar que se insertaron correctamente
SELECT * FROM rutina;
SELECT * FROM rutina_ejercicio;
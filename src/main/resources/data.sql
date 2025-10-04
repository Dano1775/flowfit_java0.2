INSERT INTO usuario (numero_documento, nombre, telefono, correo, clave, perfil_usuario, estado) VALUES
('1001', 'Admin Test', '1111111111', 'admin@flowfit.com', 'admin123', 'Administrador', 'A'),
('2001', 'Entrenador Test', '2222222222', 'entrenador@flowfit.com', 'entrenador123', 'Entrenador', 'A'),
('3001', 'Nutricionista Test', '3333333333', 'nutricionista@flowfit.com', 'nutri123', 'Nutricionista', 'A'),
('4001', 'Usuario Test', '4444444444', 'usuario@flowfit.com', 'usuario123', 'Usuario', 'A'),
('5001', 'Carlos Gómez', '5551110001', 'carlos@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5002', 'Laura Ruiz', '5551110002', 'laura@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5003', 'Pedro Díaz', '5551110003', 'pedro@flowfit.com', 'clave123', 'Entrenador', 'I'),
('5004', 'Ana Torres', '5551110004', 'ana@flowfit.com', 'clave123', 'Nutricionista', 'I'),
('5005', 'Diego Marín', '5551110005', 'diego@flowfit.com', 'clave123', 'Entrenador', 'I');

INSERT IGNORE INTO ejercicio_catalogo (nombre, descripcion, imagen, creado_por) VALUES
('Sentadillas', 'Ponte de pie con los pies al ancho de los hombros. Baja el cuerpo como si fueras a sentarte en una silla invisible, manteniendo la espalda recta y las rodillas alineadas con los pies.', 'sentadillas.jpg', NULL),
('Flexiones de pecho', 'Colócate en posición de plancha con las manos ligeramente más anchas que los hombros. Baja el pecho hacia el suelo y empuja hacia arriba manteniendo el cuerpo recto.', 'flexiones_pecho.jpg', NULL),
('Abdominales crunch', 'Acuéstate boca arriba con las rodillas flexionadas. Contrae los abdominales llevando el torso hacia las rodillas, manteniendo la parte baja de la espalda en el suelo.', 'abdominales_crunch.jpg', NULL),
('Plancha frontal', 'Colócate en posición de plancha apoyado en los antebrazos y puntas de los pies. Mantén el cuerpo recto desde la cabeza hasta los talones activando el core.', 'plancha_frontal.jpg', NULL),
('Zancadas', 'De pie, da un paso largo hacia adelante flexionando ambas rodillas a 90 grados. El muslo de la pierna adelantada debe quedar paralelo al suelo. Regresa a la posición inicial.', 'zancadas.jpg', NULL),
('Mountain climbers', 'Adopta la posición de plancha alta. Lleva alternadamente cada rodilla hacia el pecho manteniendo las caderas estables y el core activado.', 'mountain_climbers.jpg', NULL),
('Burpees', 'Desde una posición de pie, baja en cuclillas, coloca las manos en el suelo, salta hacia atrás en plancha, haz una flexión, regresa saltando y salta hacia arriba con los brazos extendidos.', 'burpees.jpg', NULL),
('Puente de glúteos', 'Acuéstate boca arriba con las rodillas flexionadas y pies apoyados en el suelo. Eleva las caderas contrayendo glúteos y abdominales, formando una línea recta desde las rodillas hasta los hombros.', 'puente_gluteos.jpg', NULL),
('Saltos en tijera', 'Desde posición erguida, salta separando las piernas y elevando los brazos por encima de la cabeza. Regresa saltando a la posición inicial con pies juntos y brazos a los lados.', 'saltos_tijera.jpg', NULL),
('Curl de bíceps con mancuerna', 'De pie con una mancuerna en cada mano y brazos extendidos a los lados. Flexiona los codos llevando las mancuernas hacia los hombros, manteniendo los codos fijos al torso.', 'curl_biceps.jpg', NULL),
('Press militar', 'Sentado o de pie, sujeta dos mancuernas a la altura de los hombros. Presiona las mancuernas hacia arriba hasta extender completamente los brazos, luego baja controladamente.', 'press_militar.jpg', NULL),
('Extensión de tríceps', 'Sujeta una mancuerna con ambas manos por encima de la cabeza. Baja la mancuerna detrás de la cabeza flexionando solo los codos, luego extiende los brazos para regresar a la posición inicial.', 'extension_triceps.jpg', NULL),
('Jump Squats', 'Realiza una sentadilla profunda y al subir salta explosivamente hacia arriba con los brazos extendidos. Aterriza suavemente y repite el movimiento de forma continua.', 'jump_squats.jpg', NULL);
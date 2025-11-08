<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
    header("Location: ../../Inicio/inicio.html");
    exit;
}

include "../../models/conexion.php";
$entrenador_id = $_SESSION["id"];
$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Usuario")[0];

// Obtener usuarios asignados
$stmt = $conexion->prepare("
    SELECT DISTINCT u.*, 
           r.nombre as rutina_nombre,
           r.descripcion as rutina_descripcion,
           ar.fecha_asignacion
    FROM usuario u
    INNER JOIN asignacion_rutina ar ON u.id = ar.usuario_id
    INNER JOIN rutina r ON ar.rutina_id = r.id
    WHERE r.entrenador_id = ?
    ORDER BY ar.fecha_asignacion DESC
");
$stmt->execute([$entrenador_id]);
$usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Usuarios - FlowFit</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="entrenador.css">
</head>
<body class="rutina-bg">

<nav class="navbar navbar-dark bg-dark fixed-top shadow">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="entrenador.php">
            <img src="../assets/logo_flowfit.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
            <span class="fs-4 fw-bold text-blue">FlowFit</span>
        </a>
        <div class="profile-wrapper" id="profile-container">
            <img src="../assets/perfil_default.png" alt="Perfil" class="profile-img" id="profileIcon">
            <ul class="dropdown-menu-custom text-center px-2 py-3" id="profileMenu">
                <li class="dropdown-header">Hola, <?= htmlspecialchars($primerNombre) ?></li>
                <li><hr class="dropdown-divider my-2"></li>
                <li><a class="dropdown-item" href="../editarperfil/editar_perfil.php">Editar perfil</a></li>
                <li><a class="dropdown-item" href="rutina.php">Gestión de Rutinas</a></li>
                <li><a class="dropdown-item" href="ejercicios_entrenador.php">Ejercicios</a></li>
                <li><a class="dropdown-item" href="historial_asignaciones.php">Historial de asignaciones</a></li>
                <li><hr class="dropdown-divider my-2"></li>
                <li><a class="dropdown-item text-danger" href="../index/index.html">Cerrar sesión</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container mt-5 pt-5">
    <div class="row mb-4">
        <div class="col">
            <h2 class="text-blue">Mis Usuarios</h2>
            <p class="text-white">Aquí puedes ver todos los usuarios a los que has asignado rutinas.</p>
        </div>
    </div>

    <?php if (empty($usuarios)): ?>
        <div class="alert alert-info bg-dark border-blue text-blue">
            <i class="bi bi-info-circle-fill me-2"></i>
            Aún no has asignado rutinas a ningún usuario. 
            <a href="asignar_rutina.php" class="alert-link text-blue">Comienza asignando una rutina</a>.
        </div>
    <?php else: ?>
        <div class="row g-4">
            <?php foreach ($usuarios as $usuario): ?>
                <div class="col-md-6 col-lg-4">
                    <div class="card h-100">
                        <div class="card-header bg-dark text-white">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-person-circle me-2"></i>
                                <?= htmlspecialchars($usuario["nombre"]) ?>
                            </h5>
                        </div>
                        <div class="card-body">
                            <p class="card-text">
                                <strong>Email:</strong> <?= htmlspecialchars($usuario["email"]) ?><br>
                                <strong>Rutina asignada:</strong> <?= htmlspecialchars($usuario["rutina_nombre"]) ?><br>
                                <strong>Fecha de asignación:</strong> <?= date('d/m/Y', strtotime($usuario["fecha_asignacion"])) ?>
                            </p>
                            <div class="mt-3">
                                <h6 class="text-blue">Descripción de la rutina:</h6>
                                <p class="card-text"><?= nl2br(htmlspecialchars($usuario["rutina_descripcion"])) ?></p>
                            </div>
                        </div>
                        <div class="card-footer">
                            <a href="asignar_rutina.php?usuario_id=<?= $usuario["id"] ?>" class="btn btn-blue btn-sm">
                                <i class="bi bi-pencil-square me-1"></i>
                                Cambiar rutina
                            </a>
                            <a href="historial_asignaciones.php?usuario_id=<?= $usuario["id"] ?>" class="btn btn-outline-primary btn-sm">
                                <i class="bi bi-clock-history me-1"></i>
                                Ver historial
                            </a>
                        </div>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    <?php endif; ?>
</div>

<footer class="footer text-center mt-5">
    <p>© 2025 FlowFit. Todos los derechos reservados.</p>
    <p>
        Síguenos en 
        <a href="https://www.instagram.com/0flowfit0/" class="text-danger">Instagram</a> · 
        <a href="https://www.facebook.com/profile.php?id=61578485602344" class="text-danger">Facebook</a> · 
        <a href="https://x.com/Flowfit420" class="text-danger">X</a>
    </p>
</footer>

<script>
    const icon = document.getElementById('profileIcon');
    const menu = document.getElementById('profileMenu');

    icon?.addEventListener('click', () => {
        menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
    });

    document.addEventListener('click', function(event) {
        if (!icon.contains(event.target) && !menu.contains(event.target)) {
            menu.style.display = 'none';
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Usuario") {
  header("Location: ../Inicio/inicio.html");
  exit;
}

$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Usuario")[0];
$idUsuario = $_SESSION["id"];

require_once "../../models/conexion.php";

$stmt = $conexion->prepare("
  SELECT r.id, r.nombre, r.descripcion, ra.fecha_asignacion
  FROM rutina_asignada ra
  JOIN rutina r ON ra.rutina_id = r.id
  WHERE ra.usuario_id = ?
");
$stmt->execute([$idUsuario]);
$rutinas = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Rutinas asignadas - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="usuario.css">
</head>
<body class="rutina-bg d-flex flex-column">

<!-- Navbar -->
<nav class="navbar navbar-dark bg-dark fixed-top shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="Usuario.php">
      <img src="../assets/logo_flowfit_usuario.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold text-success">FlowFit</span>
    </a>
    <div class="profile-wrapper" id="profile-container">
      <img src="../assets/perfil_default.png" alt="Perfil" class="profile-img" id="profileIcon">
      <ul class="dropdown-menu-custom text-center px-2 py-3" id="profileMenu">
        <li class="dropdown-header">Hola, <?= htmlspecialchars($primerNombre) ?></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item" href="../editarperfil/editar_perfil.php">Editar perfil</a></li>
        <li><a class="dropdown-item" href="ver_rutinas_usuario.php">Mis rutinas</a></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item text-danger" href="../index/index.html">Cerrar sesión</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- Contenido principal -->
<main class="flex-grow-1">
  <div class="container mt-5 pt-5">
    <h2 class="text-success text-center mb-5">Tus rutinas asignadas, <?= htmlspecialchars($primerNombre) ?> </h2>
    <div class="row justify-content-center g-4">
      <?php if (count($rutinas) > 0): ?>
        <?php foreach ($rutinas as $rutina): ?>
          <div class="col-md-4">
            <div class="card-custom p-4 text-white h-100 d-flex flex-column justify-content-between">
              <div>
                <h4 class="fw-bold text-success"><?= htmlspecialchars($rutina['nombre']) ?></h4>
                <p><?= htmlspecialchars($rutina['descripcion']) ?></p>
                <p class="fecha-asignacion">Asignada el <?= date('d/m/Y', strtotime($rutina['fecha_asignacion'])) ?></p>
              </div>
              <a href="detalle_rutina_usuario.php?id=<?= $rutina['id'] ?>" class="btn btn-glass w-100 mt-3">Ver detalles</a>
            </div>
          </div>
        <?php endforeach; ?>
      <?php else: ?>
        <div class="col-12 text-center">
          <p class="text-white fs-5">No tienes rutinas asignadas por ahora.</p>
        </div>
      <?php endif; ?>
    </div>

    <!-- Botón para volver al panel principal -->
    <div class="text-center mt-5">
      <a href="Usuario.php" class="btn btn-glass">← Volver al panel principal</a>
    </div>
  </div>
</main>

<!-- Footer -->
<footer class="footer text-center mt-5">
  <p>© 2025 FlowFit. Todos los derechos reservados.</p>
  <p>
    Síguenos en 
    <a href="https://www.instagram.com/0flowfit0/" class="text-danger">Instagram</a> · 
    <a href="https://www.facebook.com/profile.php?id=61578485602344" class="text-danger">Facebook</a> · 
    <a href="https://x.com/Flowfit420" class="text-danger">X</a>
  </p>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

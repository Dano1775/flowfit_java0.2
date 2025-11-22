<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Usuario") {
  header("Location: ../Inicio/inicio.html");
  exit;
}

require_once "../../models/conexion.php";

$rutina_id = $_GET["id"] ?? null;
$idUsuario = $_SESSION["id"];

if (!$rutina_id) {
  echo "Rutina no encontrada.";
  exit;
}

// Validar que la rutina esté asignada a este usuario
$stmt = $conexion->prepare("
  SELECT r.id, r.nombre, r.descripcion
  FROM rutina_asignada ra
  JOIN rutina r ON ra.rutina_id = r.id
  WHERE ra.usuario_id = ? AND ra.rutina_id = ?
");
$stmt->execute([$idUsuario, $rutina_id]);
$rutina = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$rutina) {
  echo "No tienes acceso a esta rutina.";
  exit;
}

// Obtener ejercicios de la rutina
$stmt2 = $conexion->prepare("
  SELECT ec.nombre, ec.descripcion, ec.imagen, ec.creado_por, re.sets, re.repeticiones
  FROM rutina_ejercicio re
  JOIN ejercicio_catalogo ec ON re.ejercicio_id = ec.id
  WHERE re.rutina_id = ?
");
$stmt2->execute([$rutina_id]);
$ejercicios = $stmt2->fetchAll(PDO::FETCH_ASSOC);

$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Usuario")[0];
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Detalle de rutina - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="usuario.css">
</head>
<body class="rutina-bg">

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

<!-- Contenido -->
<div class="container mt-5 pt-5">
  <h2 class="text-success mb-4 text-center">Detalles de la rutina: <?= htmlspecialchars($rutina["nombre"]) ?></h2>
  <p class="text-light text-center mb-5"><?= htmlspecialchars($rutina["descripcion"]) ?></p>

  <div class="row g-4">
    <?php foreach ($ejercicios as $ej): ?>
      <div class="col-md-6 col-lg-4">
        <div class="card-custom p-4 text-white h-100 d-flex flex-column justify-content-between">
          <?php
            $src = "";
            if (!empty($ej['imagen'])) {
              $imagen_limpia = htmlspecialchars($ej["imagen"]);
              $src = ($ej["creado_por"] !== null)
                ? "../ejercicio_image_uploads/user_uploads/$imagen_limpia"
                : "../ejercicio_image_uploads/$imagen_limpia";
            }
          ?>
          <?php if (!empty($src)): ?>
            <img src="<?= $src ?>" alt="<?= htmlspecialchars($ej["nombre"]) ?>" class="img-fluid rounded mb-3" style="height: 200px; object-fit: contain; background-color: rgba(255,255,255,0.05);">
          <?php else: ?>
            <div class="bg-secondary rounded mb-3 d-flex align-items-center justify-content-center" style="height: 200px;">
              <span class="text-light">Sin imagen</span>
            </div>
          <?php endif; ?>

          <div>
            <h5 class="text-success"><?= htmlspecialchars($ej["nombre"]) ?></h5>
            <p><?= htmlspecialchars($ej["descripcion"]) ?></p>
            <p class="text-muted small">Sets: <?= $ej["sets"] ?> · Repeticiones: <?= $ej["repeticiones"] ?></p>
          </div>
        </div>
      </div>
    <?php endforeach; ?>
  </div>

  <div class="text-center mt-5">
    <a href="ver_rutinas_usuario.php" class="btn btn-glass">← Volver a mis rutinas</a>
  </div>
</div>

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

<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../Inicio/inicio.html");
  exit;
}
$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Usuario")[0];
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Rutinas - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="entrenador.css">
</head>
<body class="rutina-bg">

<!-- Navbar -->
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
        <li><a class="dropdown-item" href="editar_perfil.php">Editar perfil</a></li>
        <li><a class="dropdown-item" href="rutina.php">Gestión de Rutinas</a></li>
        <li><a class="dropdown-item" href="ejercicios_entrenador.php">Ejercicios</a></li>
        <li><a class="dropdown-item" href="historial_asignaciones.php">Historial de asignaciones</a></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item text-danger" href="../index/index.html">Cerrar sesión</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- Hero Section -->
<section class="hero-section d-flex align-items-center justify-content-center text-center">
  <div class="container py-5 mt-4">
    <h1 class="hero-title">Gestión de Rutinas</h1>
    <p class="lead mt-2">Aquí puedes crear, asignar y editar rutinas para tus usuarios.</p>
  </div>
</section>

<!-- Rutina Options -->
<section class="container my-5">
  <div class="row g-4 justify-content-center">

    <!-- Crear rutina -->
    <div class="col-md-4">
      <div class="card text-center p-4">
        <i class="bi bi-plus-circle-dotted fs-1 text-blue mb-3"></i>
        <h5>Crear Rutina</h5>
        <p>Diseña una rutina personalizada desde cero para tus usuarios.</p>
        <a href="crear_rutina.php" class="btn btn-blue mt-2">Ir a Crear Rutina</a>
      </div>
    </div>

    <!-- Ver / Editar rutina -->
    <div class="col-md-4">
      <div class="card text-center p-4">
        <i class="bi bi-pencil-square fs-1 text-blue mb-3"></i>
        <h5>Ver / Editar Rutinas</h5>
        <p>Consulta tus rutinas existentes, edítalas o elimínalas.</p>
        <a href="rutinas_entrenador.php" class="btn btn-blue mt-2">Ver Rutinas</a>
      </div>
    </div>

    <!-- Asignar rutina -->
    <div class="col-md-4">
      <div class="card text-center p-4">
        <i class="bi bi-person-check fs-1 text-primary mb-3"></i>
        <h5>Asignar Rutinas</h5>
        <p>Selecciona una rutina y asígnala a uno o más usuarios fácilmente.</p>
        <a href="asignar_rutina.php" class="btn btn-blue mt-2">Asignar Rutinas</a>
      </div>
    </div>

  </div>

  <!-- Botón para volver al panel -->
  <div class="text-center mt-4">
    <a href="entrenador.php" class="btn btn-outline-light px-4 py-2">
      ← Volver al panel
    </a>
  </div>
</section>

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
<!-- Bootstrap Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- Dropdown por clic -->
<script>
  const icon = document.getElementById('profileIcon');
  const menu = document.getElementById('profileMenu');

  icon?.addEventListener('click', () => {
    menu.classList.toggle('show');
  });

  document.addEventListener('click', (e) => {
    if (!icon.contains(e.target) && !menu.contains(e.target)) {
      menu.classList.remove('show');
    }
  });
</script>

</body>
</html>

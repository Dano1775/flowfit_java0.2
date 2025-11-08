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
  <title>Panel Entrenador - FlowFit</title>
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

<!-- Hero Section -->
<section class="hero-section d-flex align-items-center justify-content-center text-center">
  <div class="container py-5 mt-4">
    <h1 class="hero-title">Hola, <?= htmlspecialchars($primerNombre) ?></h1>
    <p class="lead mt-2">Gestiona tus rutinas, ejercicios personalizados y usuarios asignados.</p>
    <div class="d-flex justify-content-center gap-3 flex-wrap mt-3">
      <a href="#panel" class="btn btn-blue px-4">Ir al Panel</a>
      <a href="https://flowfit.com/manual-entrenador" class="btn btn-outline-light px-4">Guía de Uso</a>
    </div>
  </div>
</section>

<!-- Panel de acciones -->
<section class="container my-5" id="panel">
  <div class="row g-4 justify-content-center">

    <!-- Ejercicios -->
    <div class="col-md-4 col-sm-6">
      <div class="card text-center p-4">
        <i class="bi bi-bicycle fs-1 text-primary mb-3"></i>
        <h5>Ejercicios</h5>
        <p>Crea y administra ejercicios para tus rutinas.</p>
        <a href="ejercicios_entrenador.php" class="btn btn-blue mt-2">Ir a Ejercicios</a>
      </div>
    </div>

    <!-- Gestión de Rutinas -->
    <div class="col-md-4 col-sm-6">
      <div class="card text-center p-4">
        <i class="bi bi-clipboard-data fs-1 text-blue mb-3"></i>
        <h5>Gestión de Rutinas</h5>
        <p>Crea, edita y administra tus rutinas.</p>
        <a href="rutina.php" class="btn btn-blue mt-2">Ver Rutinas</a>
      </div>
    </div>

    <!-- Historial -->
    <div class="col-md-4 col-sm-6">
      <div class="card text-center p-4">
        <i class="bi bi-journal-text fs-1 text-info mb-3"></i>
        <h5>Historial</h5>
        <p>Consulta el historial de rutinas asignadas.</p>
        <a href="historial_asignaciones.php" class="btn btn-blue mt-2">Ver Historial</a>
      </div>
    </div>

  </div>
</section>

<!-- Footer -->
<footer class="footer text-center mt-5">
  <p>© 2025 FlowFit. Todos los derechos reservados.</p>
  <p>
    Síguenos en 
    <a href="https://www.instagram.com/0flowfit0/" class="text-blue">Instagram</a> · 
    <a href="https://www.facebook.com/profile.php?id=61578485602344" class="text-blue">Facebook</a> · 
    <a href="https://x.com/Flowfit420" class="text-blue">X</a>
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

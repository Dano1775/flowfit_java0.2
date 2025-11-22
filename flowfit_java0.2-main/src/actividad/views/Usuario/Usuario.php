<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Usuario") {
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
  <title>Panel Usuario - FlowFit</title>
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

<!-- Hero Section -->
<section class="hero-section d-flex align-items-center justify-content-center text-center">
  <div class="container py-5 mt-4">
    <h1 class="hero-title">Hola, <?= htmlspecialchars($primerNombre) ?></h1>
    <p class="lead mt-2">Consulta tus rutinas y mantén tu perfil actualizado.</p>
    <div class="d-flex justify-content-center gap-3 flex-wrap mt-3">
      <a href="#panel" class="btn btn-success px-4">Ir al Panel</a>
  </div>
</section>

<!-- Panel de acciones -->
<section class="container my-5" id="panel">
  <div class="row g-4 justify-content-center">

    <!-- Ver rutinas -->
    <div class="col-md-4 col-sm-6">
      <div class="card text-center p-4">
        <i class="bi bi-list-check fs-1 text-success mb-3"></i>
        <h5>Mis Rutinas</h5>
        <p>Consulta las rutinas asignadas por tu entrenador.</p>
        <a href="ver_rutinas_usuario.php" class="btn btn-success mt-2">Ver rutinas</a>
      </div>
    </div>

    <!-- Editar perfil -->
    <div class="col-md-4 col-sm-6">
      <div class="card text-center p-4">
        <i class="bi bi-person-circle fs-1 text-dark mb-3"></i>
        <h5>Mi Perfil</h5>
        <p>Actualiza tus datos personales y contacto.</p>
        <a href="../editarperfil/editar_perfil.php" class="btn btn-success mt-2">Editar perfil</a>
      </div>
    </div>

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

<!-- Scripts -->
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

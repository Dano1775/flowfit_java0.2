<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}
?>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Nuevo ejercicio - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="administrador.css">
</head>
<body class="rutina-bg d-flex flex-column min-vh-100">

<!-- NAVBAR -->
<nav class="navbar navbar-dark bg-dark shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="administrador.php">
      <img src="../assets/logo_flowfit_admin.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold text-danger">FlowFit Admin</span>
    </a>
  </div>
</nav>

<!-- Botón discreto para ir a edición de ejercicios -->
<div class="container mt-3 text-end">
  <a href="ejercicios_admin.php" class="btn-editar-ejercicios">
    <i class="bi bi-pencil-square"></i> Editar ejercicios
  </a>
</div>


<!-- FORMULARIO CREAR -->
<main class="container pt-3 mt-3 mb-5 pb-5">
  <h2 class="text-center text-danger mb-4">Crear ejercicio para la plataforma</h2>

  <form action="../../controllers/admin_controllers/crear_ejercicio_global_controller.php" method="POST" enctype="multipart/form-data" class="glass-table p-4 rounded shadow-lg" style="max-width: 600px; margin: auto;">
    <div class="mb-3">
      <label class="form-label text-white">Nombre del ejercicio</label>
      <input type="text" name="nombre" class="form-control" required>
    </div>
    <div class="mb-3">
      <label class="form-label text-white">Descripción</label>
      <textarea name="descripcion" class="form-control" rows="4" required></textarea>
    </div>
    <div class="mb-3">
      <label class="form-label text-white">Imagen</label>
      <input type="file" name="imagen" accept="image/*" class="form-control" required>
    </div>
    <div class="text-center mt-4">
      <button type="submit" class="btn btn-success">Crear ejercicio</button>
      <a href="administrador.php" class="btn btn-outline-light ms-2">← Volver</a>
    </div>
  </form>
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

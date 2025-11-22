<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../Inicio/inicio.html");
  exit;
}

$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Admin")[0];

require_once "../../models/usuario.php";
$admin = new administrador();
$pendientes = $admin->ConsultaPendientes();
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Solicitudes Pendientes - FlowFit Admin</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="administrador.css">
</head>
<body class="rutina-bg d-flex flex-column min-vh-100">

<!-- Navbar -->
<nav class="navbar navbar-dark bg-dark fixed-top shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="administrador.php">
      <img src="../assets/logo_flowfit_admin.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold" style="color:#ef4444;">FlowFit Admin</span>
    </a>
    <div class="profile-wrapper">
      <img src="../assets/perfil_default.png" alt="Perfil" class="profile-img">
      <ul class="dropdown-menu-custom text-center px-2 py-3">
        <li class="dropdown-header">Hola, <?= htmlspecialchars($primerNombre) ?></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item" href="../editarperfil/editar_perfil.php">Editar perfil</a></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item text-danger" href="../index/index.html">Cerrar sesión</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- Contenido -->
<main class="flex-grow-1 pt-5">
  <div class="container pt-5 mt-3 text-center">
    <h2 class="text-danger mb-4">Solicitudes Pendientes</h2>

    <!-- Botón para historial de aprobaciones/rechazos -->
    <div class="text-end mb-3">
      <a href="historial_aprobaciones.php" class="btn btn-outline-info btn-sm">
        <i class="bi bi-clock-history me-1"></i> Ver historial
      </a>
    </div>

    <?php if (empty($pendientes)): ?>
      <div class="py-5">
        <i class="bi bi-inbox-fill fs-1 text-secondary mb-3"></i>
        <p class="text-white fw-bold">No hay solicitudes pendientes por aprobar.</p>
      </div>
    <?php else: ?>
      <div class="table-responsive glass-table">
        <table class="table table-dark table-hover align-middle text-center">
          <thead class="table-success text-dark">
            <tr>
              <th>ID</th><th>Nombre</th><th>Correo</th><th>Teléfono</th><th>Perfil</th><th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($pendientes as $u): ?>
              <tr>
                <td><?= $u['id'] ?></td>
                <td><?= htmlspecialchars($u['nombre']) ?></td>
                <td><?= htmlspecialchars($u['correo']) ?></td>
                <td><?= htmlspecialchars($u['telefono']) ?></td>
                <td><?= htmlspecialchars($u['perfil_usuario']) ?></td>
                <td>
                  <a href="../../controllers/admin_controllers/aprobar_usuario.php?id=<?= $u['id'] ?>" class="btn btn-aprobar btn-sm me-1">
                    <i class="bi bi-check-lg"></i>
                  </a>
                  <a href="../../controllers/admin_controllers/rechazar_usuario.php?id=<?= $u['id'] ?>" class="btn btn-danger btn-sm me-1">
                    <i class="bi bi-x-lg"></i>
                  </a>
                  <a href="editar_usuario.php?id=<?= $u['id'] ?>" class="btn btn-warning btn-sm">
                    <i class="bi bi-pencil"></i>
                  </a>
                </td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    <?php endif; ?>

    <!-- Botón volver -->
    <div class="d-flex justify-content-center mt-4">
      <a href="administrador.php" class="btn btn-outline-light">← Volver al Dashboard</a>
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

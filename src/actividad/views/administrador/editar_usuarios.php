<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../Inicio/inicio.html");
  exit;
}

require_once "../../models/usuario.php";
$admin = new administrador();
$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Admin")[0];

// Filtro de búsqueda
$q = $_GET["q"] ?? "";
if ($q !== "") {
  $usuarios = $admin->BuscarPorFiltro($q);
} else {
  $usuarios = $admin->ConsultaGeneral();
}

// Usuario para editar
$usuarioEditar = null;
if (isset($_GET["editar"])) {
  $usuarioEditar = $admin->BuscarPorId($_GET["editar"]);
}
?>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Editar Usuarios - FlowFit Admin</title>
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

<!-- Contenido principal -->
<main class="flex-grow-1 pt-5">
  <div class="container pt-5 mt-4 text-center">
    <h2 class="text-danger mb-4">Usuarios Registrados</h2>

    <!-- Formulario de búsqueda -->
    <form method="get" action="editar_usuarios.php" class="mb-4 d-flex justify-content-center">
      <input type="text" name="q" class="form-control w-50" placeholder="Buscar por nombre, correo, teléfono o documento..." value="<?= htmlspecialchars($q) ?>">
      <button type="submit" class="btn btn-success ms-2">Buscar</button>
    </form>

    <?php if (empty($usuarios)): ?>
      <div class="py-5">
        <i class="bi bi-inbox-fill fs-1 text-secondary mb-3"></i>
        <p class="text-white fw-bold">No hay usuarios registrados.</p>
      </div>
    <?php else: ?>
      <div class="table-responsive glass-table mb-5">
        <table class="table table-dark table-hover align-middle text-center">
          <thead class="table-success text-dark">
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Teléfono</th>
              <th>Perfil</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($usuarios as $u): ?>
              <tr>
                <td><?= $u['id'] ?></td>
                <td><?= htmlspecialchars($u['nombre']) ?></td>
                <td><?= htmlspecialchars($u['correo']) ?></td>
                <td><?= htmlspecialchars($u['telefono']) ?></td>
                <td><?= htmlspecialchars($u['perfil_usuario']) ?></td>
                <td><?= htmlspecialchars($u['estado']) ?></td>
                <td>
                  <a href="editar_usuarios.php?editar=<?= $u['id'] ?>&q=<?= urlencode($q) ?>" class="btn btn-warning btn-sm me-1">
                    <i class="bi bi-pencil-square"></i>
                  </a>
                  <a href="../../controllers/admin_controllers/eliminar_usuario.php?id=<?= $u['id'] ?>" class="btn btn-danger btn-sm" onclick="return confirm('¿Seguro que deseas eliminar este usuario?');">
                    <i class="bi bi-trash3-fill"></i>
                  </a>
                </td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    <?php endif; ?>

    <!-- Formulario de edición -->
    <?php if ($usuarioEditar): ?>
      <h3 class="text-white mb-4">Editar Usuario: <?= htmlspecialchars($usuarioEditar["nombre"]) ?></h3>
      <form action="../../controllers/admin_controllers/editar_usuario_controller.php" method="POST" class="text-start glass-table p-4 rounded shadow-lg" style="max-width: 600px; margin: auto;">
        <input type="hidden" name="id" value="<?= $usuarioEditar['id'] ?>">

        <div class="mb-3">
          <label class="form-label text-white">Número de Documento</label>
          <input type="text" name="numero_documento" class="form-control" value="<?= htmlspecialchars($usuarioEditar['numero_documento']) ?>" required>
        </div>

        <div class="mb-3">
          <label class="form-label text-white">Nombre</label>
          <input type="text" name="nombre" class="form-control" value="<?= htmlspecialchars($usuarioEditar['nombre']) ?>" required>
        </div>

        <div class="mb-3">
          <label class="form-label text-white">Correo</label>
          <input type="email" name="correo" class="form-control" value="<?= htmlspecialchars($usuarioEditar['correo']) ?>" required>
        </div>

        <div class="mb-3">
          <label class="form-label text-white">Teléfono</label>
          <input type="text" name="telefono" class="form-control" value="<?= htmlspecialchars($usuarioEditar['telefono']) ?>">
        </div>

        <div class="mb-3">
          <label class="form-label text-white">Perfil</label>
          <select name="perfil_usuario" class="form-select">
            <option <?= $usuarioEditar['perfil_usuario'] === 'Usuario' ? 'selected' : '' ?>>Usuario</option>
            <option <?= $usuarioEditar['perfil_usuario'] === 'Entrenador' ? 'selected' : '' ?>>Entrenador</option>
            <option <?= $usuarioEditar['perfil_usuario'] === 'Nutricionista' ? 'selected' : '' ?>>Nutricionista</option>
            <option <?= $usuarioEditar['perfil_usuario'] === 'Administrador' ? 'selected' : '' ?>>Administrador</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label text-white">Estado</label>
          <select name="estado" class="form-select">
            <option value="A" <?= $usuarioEditar['estado'] === 'A' ? 'selected' : '' ?>>Aprobado</option>
            <option value="I" <?= $usuarioEditar['estado'] === 'I' ? 'selected' : '' ?>>Pendiente</option>
            <option value="R" <?= $usuarioEditar['estado'] === 'R' ? 'selected' : '' ?>>Rechazado</option>
          </select>
        </div>

        <div class="text-center mt-4">
          <button type="submit" class="btn btn-success px-4">Guardar Cambios</button>
          <a href="editar_usuarios.php?q=<?= urlencode($q) ?>" class="btn btn-outline-light ms-2">← Cancelar</a>
        </div>
      </form>
    <?php endif; ?>

    <div class="d-flex justify-content-center mt-5">
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
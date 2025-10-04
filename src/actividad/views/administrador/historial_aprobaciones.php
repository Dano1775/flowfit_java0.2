<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";

// Captura de filtros
$accion = $_GET['accion'] ?? '';
$fecha = $_GET['fecha'] ?? '';
$correo = $_GET['correo'] ?? '';

// Consulta base
$sql = "SELECT ra.*, u.nombre AS nombre_usuario, u.correo AS correo_usuario, a.nombre AS nombre_admin
        FROM registro_aprobaciones ra
        JOIN usuario u ON ra.usuario_id = u.id
        JOIN usuario a ON ra.admin_id = a.id
        WHERE 1=1";

// Filtros dinámicos
$parametros = [];

if (!empty($accion)) {
    $sql .= " AND ra.accion = ?";
    $parametros[] = $accion;
}

if (!empty($fecha)) {
    $sql .= " AND DATE(ra.fecha) = ?";
    $parametros[] = $fecha;
}

if (!empty($correo)) {
    $sql .= " AND u.correo LIKE ?";
    $parametros[] = "%$correo%";
}

$sql .= " ORDER BY ra.fecha DESC";

$stmt = $conexion->prepare($sql);
$stmt->execute($parametros);
$registros = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Historial de Aprobaciones - FlowFit Admin</title>
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
  </div>
</nav>

<main class="flex-grow-1 pt-5">
  <div class="container pt-5 mt-3 text-center">
    <h2 class="text-danger mb-4">Historial de Aprobaciones / Rechazos</h2>

    <!-- Formulario de filtros -->
    <form method="GET" class="row justify-content-center g-2 mb-4">
      <div class="col-md-3">
        <select name="accion" class="form-select">
          <option value="">-Todos-</option>
          <option value="Aprobado" <?= $accion === 'Aprobado' ? 'selected' : '' ?>>Aprobado</option>
          <option value="Rechazado" <?= $accion === 'Rechazado' ? 'selected' : '' ?>>Rechazado</option>
        </select>
      </div>
      <div class="col-md-3">
        <input type="date" name="fecha" class="form-control" value="<?= htmlspecialchars($fecha) ?>">
      </div>
      <div class="col-md-3">
        <input type="text" name="correo" class="form-control" placeholder="Buscar por correo" value="<?= htmlspecialchars($correo) ?>">
      </div>
      <div class="col-md-2">
        <button type="submit" class="btn btn-outline-light w-100">Filtrar</button>
      </div>
    </form>

    <?php if (empty($registros)): ?>
      <div class="py-5">
        <i class="bi bi-inbox-fill fs-1 text-secondary mb-3"></i>
        <p class="text-white fw-bold">No se encontraron registros.</p>
      </div>
    <?php else: ?>
      <div class="table-responsive glass-table">
        <table class="table table-dark table-hover align-middle text-center">
          <thead class="table-success text-dark">
            <tr>
              <th>ID</th>
              <th>Correo</th>
              <th>Nombre</th>
              <th>Acción</th>
              <th>Administrador</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($registros as $r): ?>
              <tr>
                <td><?= $r['usuario_id'] ?></td>
                <td><?= htmlspecialchars($r['correo_usuario']) ?></td>
                <td><?= htmlspecialchars($r['nombre_usuario']) ?></td>
                <td class="<?= $r['accion'] === 'Aprobado' ? 'text-success' : 'text-danger' ?>">
                  <?= $r['accion'] ?>
                </td>
                <td><?= htmlspecialchars($r['nombre_admin']) ?></td>
                <td><?= date("Y-m-d H:i", strtotime($r['fecha'])) ?></td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    <?php endif; ?>

    <!-- Botón volver -->
    <div class="d-flex justify-content-center mt-4">
      <a href="usuarios_pendientes.php" class="btn btn-outline-light">← Volver a pendientes</a>
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

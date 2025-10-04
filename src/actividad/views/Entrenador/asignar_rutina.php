<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}
include "../../models/conexion.php";
$entrenador_id = $_SESSION["id"];
$modo = isset($_GET["modo"]) && $_GET["modo"] === "eliminar" ? "eliminar" : "asignar";

$stmt = $conexion->prepare("
  SELECT ra.rutina_id, ra.usuario_id, ra.fecha_asignacion, u.nombre AS usuario_nombre, r.nombre AS rutina_nombre
  FROM rutina_asignada ra
  JOIN usuario u ON ra.usuario_id = u.id
  JOIN rutina r ON ra.rutina_id = r.id
  WHERE r.entrenador_id = ?
  ORDER BY ra.fecha_asignacion DESC
");
$stmt->execute([$entrenador_id]);
$asignaciones = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Asignar Rutina - FlowFit</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="entrenador.css">
  <style>
    :root {
      --accent-color: #3b82f6; /* azul */
    }
    .text-accent { color: var(--accent-color) !important; }
    .btn-accent { background-color: var(--accent-color); color: #fff; }
    .btn-accent:hover { background-color: #2563eb; color: #fff; }
    .btn-outline-accent { border: 1px solid var(--accent-color); color: var(--accent-color); }
    .btn-outline-accent:hover { background-color: var(--accent-color); color: #fff; }
    .glass-card {
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      color: #e0f7fa;
      box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
    }
  </style>
</head>
<body class="d-flex flex-column min-vh-100">

<nav class="navbar navbar-dark fixed-top shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="entrenador.php">
      <img src="../assets/logo_flowfit.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold text-accent">FlowFit</span>
    </a>
  </div>
</nav>

<div class="container main-wrapper mt-5 pt-5 text-white">
  <h2 class="text-center mb-4 fw-bold text-accent">Asignación de Rutinas</h2>

  <?php
    if (isset($_GET["success"]) && $_GET["success"] === "asignada") {
      echo "<div class='alert alert-success text-center'>Rutina asignada correctamente.</div>";
    }
    if (isset($_GET["success"]) && $_GET["success"] === "desasignada") {
      echo "<div class='alert alert-success text-center'>Asignación eliminada correctamente.</div>";
    }
    if (isset($_GET["error"]) && $_GET["error"] === "ya_asignada") {
      echo "<div class='alert alert-danger text-center'>Esta rutina ya fue asignada a este usuario. Elimina primero la asignación anterior para modificarla.</div>";
    }
  ?>

  <div class="text-center mb-4">
    <a href="asignar_rutina.php?modo=asignar" class="btn <?= $modo === 'asignar' ? 'btn-accent' : 'btn-outline-accent' ?> me-2">Asignar rutina</a>
    <a href="asignar_rutina.php?modo=eliminar" class="btn <?= $modo === 'eliminar' ? 'btn-danger' : 'btn-outline-danger' ?>">Eliminar rutina</a>
  </div>

  <!-- ASIGNAR -->
  <div id="asignarSection" style="display: <?= $modo === 'eliminar' ? 'none' : 'block' ?>;">
    <div class="card glass-card shadow mx-auto mb-4 p-4" style="max-width: 800px;">
      <form action="../../controllers/entrenador_controllers/asignar_rutina_controller.php" method="POST">
        <input type="hidden" name="accion" value="asignar">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">Seleccionar Rutina</label>
            <select name="rutina_id" class="form-select" required>
              <option value="" disabled selected>-- Selecciona una rutina --</option>
              <?php
              $stmt = $conexion->prepare("SELECT id, nombre FROM rutina WHERE entrenador_id = ?");
              $stmt->execute([$entrenador_id]);
              foreach ($stmt as $row) {
                echo "<option value='{$row["id"]}'>{$row["nombre"]}</option>";
              }
              ?>
            </select>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label fw-bold">Seleccionar Usuario</label>
            <select name="usuario_id" class="form-select" required>
              <option value="" disabled selected>-- Selecciona un usuario --</option>
              <?php
              $stmt2 = $conexion->query("SELECT id, nombre FROM usuario WHERE perfil_usuario = 'Usuario' AND estado = 'A'");
              foreach ($stmt2 as $row2) {
                echo "<option value='{$row2["id"]}'>{$row2["nombre"]}</option>";
              }
              ?>
            </select>
          </div>
        </div>
        <button type="submit" class="btn btn-accent w-100 mt-2">Asignar Rutina</button>
      </form>
    </div>
  </div>

  <!-- ELIMINAR -->
  <div id="eliminarSection" style="display: <?= $modo === 'eliminar' ? 'block' : 'none' ?>;">
    <h4 class="text-center mb-3 text-accent">Asignaciones Actuales</h4>
    <?php if (count($asignaciones) > 0): ?>
      <div class="table-responsive">
        <table class="table table-dark table-bordered table-hover align-middle text-center">
          <thead class="table-primary text-dark">
            <tr>
              <th>Usuario</th>
              <th>Rutina</th>
              <th>Fecha</th>
              <th>Acción</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($asignaciones as $asig): ?>
              <tr>
                <td><?= htmlspecialchars($asig["usuario_nombre"]) ?></td>
                <td><?= htmlspecialchars($asig["rutina_nombre"]) ?></td>
                <td><?= $asig["fecha_asignacion"] ?></td>
                <td>
                  <form action="../../controllers/entrenador_controllers/asignar_rutina_controller.php" method="POST">
                    <input type="hidden" name="accion" value="desasignar">
                    <input type="hidden" name="rutina_id" value="<?= $asig["rutina_id"] ?>">
                    <input type="hidden" name="usuario_id" value="<?= $asig["usuario_id"] ?>">
                    <input type="hidden" name="modo" value="eliminar">
                    <button class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar esta asignación?')">
                      <i class="bi bi-x-circle"></i> Eliminar
                    </button>
                  </form>
                </td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    <?php else: ?>
      <div class="alert alert-info text-center">No hay asignaciones registradas aún.</div>
    <?php endif; ?>
  </div>

  <div class="text-center mt-4">
    <a href="rutina.php" class="btn btn-outline-light">← Volver al panel</a>
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

</body>
</html>

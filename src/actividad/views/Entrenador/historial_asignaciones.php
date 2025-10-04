<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";

// Exportar a Excel si se solicita
if (isset($_GET["exportar"]) && $_GET["exportar"] == 1) {
  header("Content-Type: application/vnd.ms-excel");
  header("Content-Disposition: attachment; filename=historial_asignaciones.xls");
  echo "Usuario\tRutina\tFecha de Asignación\n";

  $entrenador_id = $_SESSION["id"];
  $stmt = $conexion->prepare("
    SELECT ra.fecha_asignacion, u.nombre AS usuario_nombre, r.nombre AS rutina_nombre
    FROM rutina_asignada ra
    JOIN usuario u ON ra.usuario_id = u.id
    JOIN rutina r ON ra.rutina_id = r.id
    WHERE r.entrenador_id = ?
    ORDER BY ra.fecha_asignacion DESC
  ");
  $stmt->execute([$entrenador_id]);
  $asignaciones = $stmt->fetchAll(PDO::FETCH_ASSOC);

  foreach ($asignaciones as $asig) {
    echo $asig["usuario_nombre"] . "\t" . $asig["rutina_nombre"] . "\t" . $asig["fecha_asignacion"] . "\n";
  }
  exit;
}

// Consultar datos para mostrar en la tabla
$entrenador_id = $_SESSION["id"];
$stmt = $conexion->prepare("
  SELECT ra.fecha_asignacion, u.nombre AS usuario_nombre, r.nombre AS rutina_nombre
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
  <title>Historial de Asignaciones - FlowFit</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="entrenador.css">
</head>
<body class="main-wrapper">

<nav class="navbar navbar-dark bg-dark fixed-top glass-header shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="entrenador.php">
      <img src="../assets/logo_flowfit.png" alt="FlowFit" height="40" class="me-2">
      <span class="fs-4 text-primary fw-bold">FlowFit</span>
    </a>
  </div>
</nav>

<div class="container main-content">
  <div class="glass-card px-4 py-5 shadow-lg">
    <h2 class="text-center mb-4 text-primary hero-title">Historial de Rutinas Asignadas</h2>

    <?php if (count($asignaciones) > 0): ?>
      <div class="table-responsive mx-auto" style="max-width: 900px;">
        <table class="table table-hover table-bordered align-middle text-center shadow-sm">
          <thead class="table-primary text-dark">
            <tr>
              <th>Usuario</th>
              <th>Rutina</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($asignaciones as $asig): ?>
              <tr>
                <td><?= htmlspecialchars($asig["usuario_nombre"]) ?></td>
                <td><?= htmlspecialchars($asig["rutina_nombre"]) ?></td>
                <td><?= htmlspecialchars($asig["fecha_asignacion"]) ?></td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>

      <div class="text-center mt-4">
        <a href="historial_asignaciones.php?exportar=1" class="btn btn-primary px-4">
          Exportar a Excel
        </a>
      </div>
    <?php else: ?>
      <div class="alert alert-info text-center mt-4">
        Aún no has asignado ninguna rutina.
      </div>
    <?php endif; ?>

    <div class="text-center mt-4">
      <a href="entrenador.php" class="btn btn-outline-secondary">← Volver al panel</a>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  const toggleBtn = document.getElementById("toggleModo");
  if (localStorage.getItem("modoOscuro") === "true") {
    document.body.classList.add("dark-mode");
  }
  toggleBtn.addEventListener("click", () => {
    document.body.classList.toggle("dark-mode");
    localStorage.setItem("modoOscuro", document.body.classList.contains("dark-mode"));
  });
</script>
</body>
</html>

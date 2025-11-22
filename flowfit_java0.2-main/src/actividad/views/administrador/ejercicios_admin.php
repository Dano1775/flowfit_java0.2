<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";

if ($_SERVER["REQUEST_METHOD"] === "POST") {
  // Eliminar
  if (isset($_POST["eliminar_id"])) {
    $stmt = $conexion->prepare("DELETE FROM ejercicio_catalogo WHERE id = ?");
    $stmt->execute([$_POST["eliminar_id"]]);
  }
  // Editar
  if (isset($_POST["editar_id"])) {
    $id          = $_POST["editar_id"];
    $nombre      = $_POST["editar_nombre"];
    $descripcion = $_POST["editar_descripcion"];

    if (!empty($_FILES["editar_imagen"]["name"])) {
      $img_nombre  = uniqid() . "_" . basename($_FILES["editar_imagen"]["name"]);
      $ruta_dest   = "../ejercicio_image_uploads/" . $img_nombre;
      move_uploaded_file($_FILES["editar_imagen"]["tmp_name"], $ruta_dest);

      $stmt = $conexion->prepare("
        UPDATE ejercicio_catalogo 
           SET nombre = ?, descripcion = ?, imagen = ?
         WHERE id = ?
      ");
      $stmt->execute([$nombre, $descripcion, $img_nombre, $id]);
    } else {
      $stmt = $conexion->prepare("
        UPDATE ejercicio_catalogo 
           SET nombre = ?, descripcion = ?
         WHERE id = ?
      ");
      $stmt->execute([$nombre, $descripcion, $id]);
    }
  }
}

// Sólo ejercicios “de plataforma”
$ejercicios = $conexion
  ->query("SELECT * FROM ejercicio_catalogo WHERE creado_por IS NULL")
  ->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Ejercicios – Administrador</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link href="administrador.css" rel="stylesheet">
</head>
<body class="d-flex flex-column min-vh-100">

  <!-- Navbar -->
  <nav class="navbar navbar-dark fixed-top shadow">
    <div class="container-fluid">
      <a class="navbar-brand d-flex align-items-center" href="administrador.php">
        <img src="../assets/logo_flowfit_admin.png" alt="FlowFit" height="40" class="me-2" style="border-radius:10px;">
        <span class="fs-4 fw-bold" style="color:#ef4444;">FlowFit Admin</span>
      </a>
    </div>
  </nav>

  <main class="flex-grow-1 container mt-5 pt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-danger">Ejercicios de la plataforma</h2>
      <a href="crear_ejercicio_global.php" class="btn-editar-ejercicios">+ Crear ejercicio</a>
    </div>

    <div class="row">
      <?php foreach ($ejercicios as $ej): ?>
      <div class="col-md-4 mb-4">
        <div class="card card-custom h-100">
          <img 
            src="../ejercicio_image_uploads/<?= htmlspecialchars($ej['imagen']) ?>" 
            class="card-img-top" 
            alt="<?= htmlspecialchars($ej['nombre']) ?>">
          <div class="card-body d-flex flex-column">
            <h5 class="card-title"><?= htmlspecialchars($ej['nombre']) ?></h5>
            <p class="card-text flex-grow-1"><?= nl2br(htmlspecialchars($ej['descripcion'])) ?></p>
            <div class="d-flex justify-content-end gap-2 mt-3">
              <!-- Botón editar -->
              <button 
                class="btn btn-sm btn-outline-light" 
                data-bs-toggle="modal" 
                data-bs-target="#editarModal<?= $ej['id'] ?>">
                <i class="bi bi-pencil-fill"></i>
              </button>
              <!-- Form eliminar -->
              <form method="POST" onsubmit="return confirm('¿Eliminar este ejercicio?')">
                <input type="hidden" name="eliminar_id" value="<?= $ej['id'] ?>">
                <button type="submit" class="btn btn-sm btn-outline-danger">
                  <i class="bi bi-trash3-fill"></i>
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal edición -->
      <div class="modal fade" id="editarModal<?= $ej['id'] ?>" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
          <form method="POST" class="modal-content" enctype="multipart/form-data">
            <div class="modal-header bg-danger text-white">
              <h5 class="modal-title">
                <i class="bi bi-pencil-square me-2"></i>Editar ejercicio
              </h5>
              <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <input type="hidden" name="editar_id" value="<?= $ej['id'] ?>">
              <div class="mb-3">
                <label class="form-label text-white">Nombre del ejercicio</label>
                <input 
                  type="text" 
                  name="editar_nombre" 
                  class="form-control" 
                  value="<?= htmlspecialchars($ej['nombre']) ?>" 
                  required>
              </div>
              <div class="mb-3">
                <label class="form-label text-white">Descripción detallada</label>
                <textarea 
                  name="editar_descripcion" 
                  class="form-control" 
                  rows="4" 
                  required><?= htmlspecialchars($ej['descripcion']) ?></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label text-white">Reemplazar imagen (opcional)</label>
                <input 
                  type="file" 
                  name="editar_imagen" 
                  accept="image/*" 
                  class="form-control">
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-success">
                <i class="bi bi-check-circle-fill me-1"></i>Guardar
              </button>
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle-fill me-1"></i>Cancelar
              </button>
            </div>
          </form>
        </div>
      </div>

      <?php endforeach; ?>
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

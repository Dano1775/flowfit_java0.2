<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";
$entrenador_id = $_SESSION["id"];

if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($_POST["eliminar_id"])) {
  $stmt = $conexion->prepare("DELETE FROM ejercicio_catalogo WHERE id = ? AND creado_por = ?");
  $stmt->execute([$_POST["eliminar_id"], $entrenador_id]);
}

if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($_POST["editar_id"])) {
  $editar_id = $_POST["editar_id"];
  $nombre = $_POST["editar_nombre"];
  $descripcion = $_POST["editar_descripcion"];

  if (isset($_FILES["editar_imagen"]) && $_FILES["editar_imagen"]["error"] === UPLOAD_ERR_OK) {
    $imagen_nombre = uniqid() . "_" . basename($_FILES["editar_imagen"]["name"]);
    $ruta_destino = "../ejercicio_image_uploads/user_uploads/" . $imagen_nombre;
    move_uploaded_file($_FILES["editar_imagen"]["tmp_name"], $ruta_destino);

    $stmt = $conexion->prepare("UPDATE ejercicio_catalogo SET nombre = ?, descripcion = ?, imagen = ? WHERE id = ? AND creado_por = ?");
    $stmt->execute([$nombre, $descripcion, $imagen_nombre, $editar_id, $entrenador_id]);
  } else {
    $stmt = $conexion->prepare("UPDATE ejercicio_catalogo SET nombre = ?, descripcion = ? WHERE id = ? AND creado_por = ?");
    $stmt->execute([$nombre, $descripcion, $editar_id, $entrenador_id]);
  }
}

$global = $conexion->query("SELECT * FROM ejercicio_catalogo WHERE creado_por IS NULL")->fetchAll(PDO::FETCH_ASSOC);
$stmt = $conexion->prepare("SELECT * FROM ejercicio_catalogo WHERE creado_por = ?");
$stmt->execute([$entrenador_id]);
$personales = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Ejercicios - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link href="entrenador.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-dark fixed-top shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="entrenador.php">
      <img src="../assets/logo_flowfit.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold text-blue">FlowFit</span>
    </a>
  </div>
</nav>

<div class="container main-wrapper">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="mb-0">Ejercicios disponibles</h2>
    <a href="entrenador.php" class="btn btn-outline-danger">← Volver</a>
  </div>

  <input type="text" id="buscador" class="form-control mb-4" placeholder="Buscar ejercicios...">

  <h5 class="collapsible-header text-success" onclick="toggleSeccion('plataforma')">Ejercicios de la plataforma</h5>
  <div class="row" id="plataforma">
    <?php foreach ($global as $ej): ?>
      <div class="col-md-4 ejercicio-card mb-4" data-nombre="<?= strtolower($ej["nombre"]) ?>">
        <div class="card">
          <img src="../ejercicio_image_uploads/<?= $ej["imagen"] ?>" class="card-img-top">
          <div class="card-body">
            <h5 class="card-title"><?= $ej["nombre"] ?></h5>
            <p class="card-text"><?= nl2br($ej["descripcion"]) ?></p>
          </div>
        </div>
      </div>
    <?php endforeach; ?>
  </div>

  <h5 class="collapsible-header text-info mt-4" onclick="toggleSeccion('personales')">Ejercicios creados por ti</h5>
  <div class="row" id="personales">
    <?php foreach ($personales as $ej): ?>
      <div class="col-md-4 ejercicio-card mb-4" data-nombre="<?= strtolower($ej["nombre"]) ?>">
        <div class="card">
          <img src="../ejercicio_image_uploads/user_uploads/<?= $ej["imagen"] ?>" class="card-img-top">
          <div class="card-body">
            <h5 class="card-title"><?= $ej["nombre"] ?></h5>
            <p class="card-text"><?= nl2br($ej["descripcion"]) ?></p>
            <div class="d-flex justify-content-between mt-3">
              <button class="btn btn-sm btn-blue" data-bs-toggle="modal" data-bs-target="#editarModal<?= $ej["id"] ?>">Editar</button>
              <form method="POST" onsubmit="return confirm('¿Eliminar este ejercicio?')">
                <input type="hidden" name="eliminar_id" value="<?= $ej["id"] ?>">
                <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal edición -->
      <div class="modal fade" id="editarModal<?= $ej["id"] ?>" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
          <form method="POST" class="modal-content" enctype="multipart/form-data">
            <div class="modal-header bg-blue text-white">
              <h5 class="modal-title">Editar ejercicio</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <input type="hidden" name="editar_id" value="<?= $ej["id"] ?>">
              <div class="mb-3">
                <label>Nombre:</label>
                <input type="text" name="editar_nombre" class="form-control" value="<?= $ej["nombre"] ?>" required>
              </div>
              <div class="mb-3">
                <label>Descripción:</label>
                <textarea name="editar_descripcion" class="form-control" rows="4" required><?= $ej["descripcion"] ?></textarea>
              </div>
              <div class="mb-3">
                <label>Reemplazar imagen (opcional):</label>
                <input type="file" name="editar_imagen" accept="image/*" class="form-control">
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-blue">Guardar</button>
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    <?php endforeach; ?>
  </div>
</div>

<button class="btn btn-blue btn-sm btn-flotante" data-bs-toggle="modal" data-bs-target="#modalCrearEjercicio">+ Crear ejercicio</button>

<!-- Modal Crear -->
<div class="modal fade" id="modalCrearEjercicio" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <form id="formCrearEjercicio" class="modal-content" enctype="multipart/form-data">
      <div class="modal-header bg-blue text-white">
        <h5 class="modal-title">Crear nuevo ejercicio</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label">Nombre del ejercicio</label>
          <input type="text" name="nombre" class="form-control" required>
        </div>
        <div class="mb-3">
          <label class="form-label">Descripción detallada</label>
          <textarea name="descripcion" class="form-control" rows="4" required></textarea>
        </div>
        <div class="mb-3">
          <label class="form-label">Imagen del ejercicio</label>
          <input type="file" name="imagen" accept="image/*" class="form-control" required>
        </div>
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn btn-blue">Guardar</button>
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
      </div>
    </form>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  const cards = document.querySelectorAll('.ejercicio-card');
  const buscador = document.getElementById('buscador');
  buscador.addEventListener('input', function () {
    const texto = this.value.toLowerCase();
    cards.forEach(card => {
      const nombre = card.dataset.nombre;
      card.style.display = nombre.includes(texto) ? 'block' : 'none';
    });
  });

  function toggleSeccion(id) {
    document.getElementById(id).classList.toggle("d-none");
  }

  document.getElementById("formCrearEjercicio").addEventListener("submit", function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    fetch("../../controllers/entrenador_controllers/crear_ejercicio_controller.php", {
      method: "POST",
      body: formData
    })
    .then(res => res.text())
    .then(() => {
      alert("Ejercicio creado correctamente");
      location.reload();
    })
    .catch(err => {
      alert("Error al guardar el ejercicio");
      console.error(err);
    });
  });
</script>
</body>
</html>

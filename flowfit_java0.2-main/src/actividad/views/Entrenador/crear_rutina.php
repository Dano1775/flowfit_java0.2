<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";
$entrenador_id = $_SESSION["id"];

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
  <title>Crear rutina - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="entrenador.css">
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
  <h2 class="mb-4 text-center text-blue">Crear nueva rutina</h2>

  <form action="../../controllers/entrenador_controllers/rutinas_controller.php" method="POST">
    <input type="hidden" name="accion" value="crear">
    <div class="mb-3">
      <label class="form-label">Nombre de la rutina:</label>
      <input type="text" name="nombre" class="form-control" required>
    </div>

    <div class="mb-4">
      <label class="form-label">Descripción:</label>
      <textarea name="descripcion" class="form-control" rows="3"></textarea>
    </div>

    <div class="text-center mb-5">
      <button type="button" class="btn btn-outline-blue btn-lg" data-bs-toggle="modal" data-bs-target="#modalEjercicios">
        Seleccionar ejercicios
      </button>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="modalEjercicios" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header bg-blue text-white">
            <h5 class="modal-title">Seleccionar ejercicios</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <input type="text" id="buscadorModal" class="form-control mb-4" placeholder="Buscar ejercicios...">

            <h5 class="collapsible-header text-success" onclick="toggleSeccion('modalPlataforma')">Ejercicios de la plataforma</h5>
            <div class="row" id="modalPlataforma">
              <?php foreach ($global as $ej): ?>
                <div class="col-md-4 ejercicio-card-modal mb-4" data-nombre="<?= strtolower($ej["nombre"]) ?>">
                  <div class="card h-100">
                    <img src="../ejercicio_image_uploads/<?= $ej["imagen"] ?>" class="card-img-top">
                    <div class="card-body">
                      <div class="form-check mb-2">
                        <input class="form-check-input ejercicio-check" type="checkbox" name="ejercicios[]" value="<?= $ej["id"] ?>" id="e<?= $ej["id"] ?>">
                        <label class="form-check-label fw-bold" for="e<?= $ej["id"] ?>"><?= $ej["nombre"] ?></label>
                      </div>
                      <p class="card-text"><?= nl2br($ej["descripcion"]) ?></p>
                      <div class="row">
                        <div class="col">
                          <input type="number" name="sets[<?= $ej["id"] ?>]" class="form-control" placeholder="Sets" min="1" disabled>
                        </div>
                        <div class="col">
                          <input type="number" name="reps[<?= $ej["id"] ?>]" class="form-control" placeholder="Reps" min="1" disabled>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              <?php endforeach; ?>
            </div>

            <h5 class="collapsible-header text-info mt-4" onclick="toggleSeccion('modalPersonales')">Ejercicios creados por ti</h5>
            <div class="row" id="modalPersonales">
              <?php foreach ($personales as $ej): ?>
                <div class="col-md-4 ejercicio-card-modal mb-4" data-nombre="<?= strtolower($ej["nombre"]) ?>">
                  <div class="card h-100">
                    <img src="../ejercicio_image_uploads/user_uploads/<?= $ej["imagen"] ?>" class="card-img-top">
                    <div class="card-body">
                      <div class="form-check mb-2">
                        <input class="form-check-input ejercicio-check" type="checkbox" name="ejercicios[]" value="<?= $ej["id"] ?>" id="p<?= $ej["id"] ?>">
                        <label class="form-check-label fw-bold" for="p<?= $ej["id"] ?>"><?= $ej["nombre"] ?></label>
                      </div>
                      <p class="card-text"><?= nl2br($ej["descripcion"]) ?></p>
                      <div class="row">
                        <div class="col">
                          <input type="number" name="sets[<?= $ej["id"] ?>]" class="form-control" placeholder="Sets" min="1" disabled>
                        </div>
                        <div class="col">
                          <input type="number" name="reps[<?= $ej["id"] ?>]" class="form-control" placeholder="Reps" min="1" disabled>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              <?php endforeach; ?>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-blue" data-bs-dismiss="modal">Listo</button>
          </div>
        </div>
      </div>
    </div>

    <div class="text-center mt-4">
      <button type="submit" class="btn btn-blue btn-lg">Guardar rutina</button>
      <a href="rutina.php" class="btn btn-secondary btn-lg">Cancelar</a>
    </div>
  </form>
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
  document.querySelectorAll('.ejercicio-check').forEach(check => {
    check.addEventListener('change', () => {
      const id = check.value;
      document.querySelector(`[name="sets[${id}]"]`).disabled = !check.checked;
      document.querySelector(`[name="reps[${id}]"]`).disabled = !check.checked;
    });
  });

  function toggleSeccion(id) {
    document.getElementById(id).classList.toggle("d-none");
  }

  const buscador = document.getElementById("buscadorModal");
  const tarjetas = document.querySelectorAll(".ejercicio-card-modal");
  buscador.addEventListener("input", function () {
    const texto = this.value.toLowerCase();
    tarjetas.forEach(card => {
      card.style.display = card.dataset.nombre.includes(texto) ? "block" : "none";
    });
  });
</script>
</body>
</html>

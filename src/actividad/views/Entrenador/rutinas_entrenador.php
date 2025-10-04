<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";
$entrenador_id = $_SESSION["id"];

$stmt = $conexion->prepare("SELECT * FROM rutina WHERE entrenador_id = ?");
$stmt->execute([$entrenador_id]);
$rutinas = $stmt->fetchAll(PDO::FETCH_ASSOC);

$catalogo = $conexion->query(
  "SELECT * FROM ejercicio_catalogo WHERE creado_por IS NULL OR creado_por = $entrenador_id"
)->fetchAll(PDO::FETCH_ASSOC);
?>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Mis Rutinas - FlowFit</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <link rel="stylesheet" href="entrenador.css">
</head>
<body class="d-flex flex-column min-vh-100">

<nav class="navbar navbar-dark fixed-top shadow">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="entrenador.php">
      <img src="../assets/logo_flowfit.png" alt="FlowFit" height="40" class="me-2" style="border-radius: 10px;">
      <span class="fs-4 fw-bold text-blue">FlowFit</span>
    </a>
    <div class="profile-wrapper">
      <img src="../assets/perfil_default.png" alt="Perfil" class="profile-img" id="profileIcon" onclick="toggleDropdown()">
      <ul class="dropdown-menu-custom text-center px-2 py-3" id="dropdownMenu">
        <li class="dropdown-header fw-semibold text-blue fs-6">
          Hola, <?= explode(' ', $_SESSION["nombre"] ?? 'Entrenador')[0] ?>
        </li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item" href="../editarperfil/editar_perfil.php">Editar perfil</a></li>
        <li><a class="dropdown-item" href="rutinas_entrenador.php">Mis Rutinas</a></li>
        <li><a class="dropdown-item" href="ejercicios_entrenador.php">Ejercicios</a></li>
        <li><a class="dropdown-item" href="asignar_rutina.php">Asignar Rutinas</a></li>
        <li><a class="dropdown-item" href="historial_asignaciones.php">Historial</a></li>
        <li><hr class="dropdown-divider my-2"></li>
        <li><a class="dropdown-item text-danger" href="../index/index.html">Cerrar sesión</a></li>
      </ul>
    </div>
  </div>
</nav>

<div class="container-fluid main-wrapper mt-5 pt-5 text-white">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2 class="fw-bold text-blue">Rutinas creadas</h2>
    <a href="rutina.php" class="btn btn-outline-light">← Volver</a>
  </div>

  <?php if (empty($rutinas)): ?>
    <div class="alert alert-info bg-dark border-blue text-blue text-center">
      <i class="bi bi-info-circle-fill me-2"></i>
      Aún no has creado rutinas. Usa el botón flotante para crear tu primera rutina.
    </div>
  <?php else: ?>
    <div class="row">
      <?php foreach ($rutinas as $rutina): ?>
        <?php
        $stmt2 = $conexion->prepare("
          SELECT ec.*, re.sets, re.repeticiones
          FROM rutina_ejercicio re
          JOIN ejercicio_catalogo ec ON re.ejercicio_id = ec.id
          WHERE re.rutina_id = ?
        ");
        $stmt2->execute([$rutina["id"]]);
        $ejercicios = $stmt2->fetchAll(PDO::FETCH_ASSOC);
        $ids_existentes = array_column($ejercicios, "id");
        ?>
        <div class="col-md-6 mb-4">
          <div class="card rutina-card p-3">
            <h5 class="fw-bold text-blue"><?= htmlspecialchars($rutina["nombre"]) ?></h5>
            <p class="card-text"><?= nl2br(htmlspecialchars($rutina["descripcion"])) ?></p>
            <ul class="list-group mb-3">
              <?php foreach ($ejercicios as $e): ?>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <?= htmlspecialchars($e["nombre"]) ?>
                  <span class="badge bg-blue rounded-pill"><?= $e["sets"] ?>×<?= $e["repeticiones"] ?></span>
                </li>
              <?php endforeach; ?>
            </ul>
            <div class="d-flex justify-content-between">
              <button class="btn btn-sm btn-outline-light" data-bs-toggle="modal" data-bs-target="#modalEditar<?= $rutina["id"] ?>">Editar</button>
              <a href="../../controllers/entrenador_controllers/rutinas_controller.php?accion=eliminar&id=<?= $rutina["id"] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar esta rutina?')">Eliminar</a>
            </div>
          </div>
        </div>

        <div class="modal fade" id="modalEditar<?= $rutina["id"] ?>" tabindex="-1" aria-hidden="true">
          <div class="modal-dialog modal-fullscreen-lg-down modal-xl modal-dialog-scrollable">
            <form action="../../controllers/entrenador_controllers/rutinas_controller.php" method="POST" class="modal-content">
              <div class="modal-header bg-blue text-white">
                <h5 class="modal-title">Editar rutina</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
              </div>
              <div class="modal-body">
                <input type="hidden" name="accion" value="editar">
                <input type="hidden" name="rutina_id" value="<?= $rutina["id"] ?>">

                <div class="mb-3">
                  <label class="form-label">Nombre</label>
                  <input type="text" name="nombre" class="form-control" value="<?= htmlspecialchars($rutina["nombre"]) ?>" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Descripción</label>
                  <textarea name="descripcion" class="form-control"><?= htmlspecialchars($rutina["descripcion"]) ?></textarea>
                </div>

                <h6 class="text-blue">Ejercicios actuales</h6>
                <?php foreach ($ejercicios as $i => $e): ?>
                  <div class="border rounded p-3 mb-3 bg-dark">
                    <input type="hidden" name="ejercicios[<?= $i ?>][id]" value="<?= $e["id"] ?>">
                    <div class="d-flex justify-content-between align-items-center">
                      <strong class="text-white"><?= htmlspecialchars($e["nombre"]) ?></strong>
                      <div class="form-check">
                        <input type="checkbox" class="form-check-input" name="eliminar_ejercicios[]" value="<?= $e["id"] ?>" id="del<?= $rutina["id"] ?>_<?= $e["id"] ?>">
                        <label for="del<?= $rutina["id"] ?>_<?= $e["id"] ?>" class="form-check-label text-danger">Eliminar</label>
                      </div>
                    </div>
                    <div class="row mt-2">
                      <div class="col">
                        <label class="form-label">Sets</label>
                        <input type="number" name="ejercicios[<?= $i ?>][sets]" value="<?= $e["sets"] ?>" class="form-control" required>
                      </div>
                      <div class="col">
                        <label class="form-label">Reps</label>
                        <input type="number" name="ejercicios[<?= $i ?>][reps]" value="<?= $e["repeticiones"] ?>" class="form-control" required>
                      </div>
                    </div>
                  </div>
                <?php endforeach; ?>

                <hr class="border-blue">

                <h6 class="text-blue">Añadir más ejercicios</h6>
                <input type="text" class="form-control mb-3" id="busqueda<?= $rutina["id"] ?>" placeholder="🔍 Buscar ejercicios...">

                <h6 class="text-blue">Ejercicios de FlowFit</h6>
                <div class="row" id="flowfit<?= $rutina["id"] ?>">
                  <?php
                  $hay_flowfit = false;
                  foreach ($catalogo as $e):
                    if ($e["creado_por"] !== null || in_array($e["id"], $ids_existentes)) continue;
                    $hay_flowfit = true;
                  ?>
                    <div class="col-md-4 mb-3 ejercicio-card" data-nombre="<?= strtolower($e["nombre"]) ?>">
                      <div class="card h-100">
                        <img src="../ejercicio_image_uploads/<?= $e["imagen"] ?>" class="card-img-top">
                        <div class="card-body">
                          <div class="form-check mb-2">
                            <input class="form-check-input ejercicio-check" type="checkbox" name="nuevos_ejercicios[]" value="<?= $e["id"] ?>" id="nuevo<?= $rutina["id"] ?>_<?= $e["id"] ?>">
                            <label class="form-check-label fw-bold" for="nuevo<?= $rutina["id"] ?>_<?= $e["id"] ?>"><?= htmlspecialchars($e["nombre"]) ?></label>
                          </div>
                          <div class="row mt-2">
                            <div class="col">
                              <input type="number" name="sets[<?= $e["id"] ?>]" placeholder="Sets" class="form-control" min="1" disabled>
                            </div>
                            <div class="col">
                              <input type="number" name="reps[<?= $e["id"] ?>]" placeholder="Reps" class="form-control" min="1" disabled>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  <?php endforeach; ?>
                  <?php if (!$hay_flowfit): ?>
                    <p class="text-muted fst-italic">No hay ejercicios de FlowFit disponibles.</p>
                  <?php endif; ?>
                </div>

                <h6 class="text-primary mt-4">Ejercicios creados por ti</h6>
                <div class="row" id="entrenador<?= $rutina["id"] ?>">
                  <?php
                  $hay_personales = false;
                  foreach ($catalogo as $e):
                    if ($e["creado_por"] !== $entrenador_id || in_array($e["id"], $ids_existentes)) continue;
                    $hay_personales = true;
                  ?>
                    <div class="col-md-4 mb-3 ejercicio-card" data-nombre="<?= strtolower($e["nombre"]) ?>">
                      <div class="card h-100">
                        <img src="../ejercicio_image_uploads/user_uploads/<?= $e["imagen"] ?>" class="card-img-top">
                        <div class="card-body">
                          <div class="form-check mb-2">
                            <input class="form-check-input ejercicio-check" type="checkbox" name="nuevos_ejercicios[]" value="<?= $e["id"] ?>" id="nuevoU<?= $rutina["id"] ?>_<?= $e["id"] ?>">
                            <label class="form-check-label fw-bold" for="nuevoU<?= $rutina["id"] ?>_<?= $e["id"] ?>"><?= htmlspecialchars($e["nombre"]) ?></label>
                          </div>
                          <div class="row mt-2">
                            <div class="col">
                              <input type="number" name="sets[<?= $e["id"] ?>]" placeholder="Sets" class="form-control" min="1" disabled>
                            </div>
                            <div class="col">
                              <input type="number" name="reps[<?= $e["id"] ?>]" placeholder="Reps" class="form-control" min="1" disabled>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  <?php endforeach; ?>
                  <?php if (!$hay_personales): ?>
                    <p class="text-muted fst-italic">No tienes ejercicios personales disponibles.</p>
                  <?php endif; ?>
                </div>
              </div>

              <div class="modal-footer">
                <button type="submit" class="btn btn-blue">Guardar cambios</button>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
              </div>
            </form>
          </div>
        </div>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>

<a href="crear_rutina.php" class="btn btn-blue btn-flotante">
  <i class="bi bi-plus-lg me-1"></i> Crear Rutina
</a>

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

<script>
  function toggleDropdown() {
    const dd = document.getElementById('dropdownMenu');
    dd.style.display = dd.style.display === 'block' ? 'none' : 'block';
  }

  document.addEventListener('click', e => {
    const dd = document.getElementById('dropdownMenu');
    if (!dd.contains(e.target) && e.target.id !== 'profileIcon') {
      dd.style.display = 'none';
    }
  });

  document.querySelectorAll("input[id^='busqueda']").forEach(input => {
    input.addEventListener("input", () => {
      const id = input.id.replace('busqueda','');
      const filtro = input.value.toLowerCase();
      document.querySelectorAll(`#flowfit${id} .ejercicio-card, #entrenador${id} .ejercicio-card`)
        .forEach(card => card.style.display = card.dataset.nombre.includes(filtro) ? 'block' : 'none');
    });
  });

  document.querySelectorAll('.ejercicio-check').forEach(check => {
    const id = check.value;
    const setInput = document.querySelector(`[name="sets[${id}]"]`);
    const repInput = document.querySelector(`[name="reps[${id}]"]`);
    setInput.disabled = true;
    repInput.disabled = true;
    check.addEventListener('change', () => {
      setInput.disabled = !check.checked;
      repInput.disabled = !check.checked;
    });
  });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

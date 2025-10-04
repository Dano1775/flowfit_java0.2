<?php
session_start();
if (!isset($_SESSION["id"])) {
  header("Location: ../Inicio/inicio.html");
  exit;
}

$primerNombre = explode(' ', $_SESSION["nombre"] ?? "Usuario")[0];
$usuarioId = $_SESSION["id"];

include_once "../../models/conexion.php";

$stmt = $conexion->prepare("SELECT * FROM usuario WHERE id = ?");
$stmt->execute([$usuarioId]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Editar Perfil - FlowFit</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="editarperfil.css">
</head>
<body class="rutina-bg">

<div class="container-xl px-4 mt-5">
  <?php if (isset($_GET["exito"]) && $_GET["exito"] == 1): ?>
    <div class="alert alert-success text-center fw-semibold">
      Datos actualizados correctamente.
    </div>
  <?php endif; ?>

  <div class="row">
    <!-- Panel lateral con imagen -->
    <div class="col-xl-4">
      <div class="card mb-4 text-center p-3">
        <div class="card-header">Foto de perfil</div>
        <div class="card-body">
          <img class="img-account-profile rounded-circle mb-3" src="../assets/perfil_default.png" alt="Foto de perfil" width="120">
          <div class="small text-muted mb-3">JPG o PNG menor a 5MB</div>
          <button class="btn btn-primary" type="button" disabled>Subir nueva foto</button>
        </div>
      </div>
    </div>

    <!-- Formulario de edición -->
    <div class="col-xl-8">
      <div class="card mb-4 p-3">
        <div class="card-header">Detalles de la cuenta</div>
        <div class="card-body">
          <form method="POST" action="../../controllers/procesar_edicion.php">
            <input type="hidden" name="id" value="<?= $usuario['id'] ?>">

            <div class="mb-3">
              <label for="nombre">Nombre completo</label>
              <input class="form-control" id="nombre" name="nombre" type="text" value="<?= htmlspecialchars($usuario['nombre']) ?>" required>
            </div>

            <div class="row gx-3 mb-3">
              <div class="col-md-6">
                <label for="numero_documento">Número de documento</label>
                <input class="form-control" id="numero_documento" name="numero_documento" type="text" value="<?= htmlspecialchars($usuario['numero_documento']) ?>" required>
              </div>
              <div class="col-md-6">
                <label for="telefono">Teléfono</label>
                <input class="form-control" id="telefono" name="telefono" type="text" value="<?= htmlspecialchars($usuario['telefono']) ?>" required>
              </div>
            </div>

            <div class="mb-3">
              <label for="correo">Correo electrónico</label>
              <input class="form-control" id="correo" name="correo" type="email" value="<?= htmlspecialchars($usuario['correo']) ?>" required>
            </div>

            <button class="btn btn-primary" type="submit">Guardar cambios</button>
            <a href="../<?= ucfirst(strtolower($_SESSION['perfil'])) ?>/<?= strtolower($_SESSION['perfil']) ?>.php" class="btn btn-outline-light ms-2">Cancelar</a>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

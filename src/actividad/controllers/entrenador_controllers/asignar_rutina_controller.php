<?php
session_start();
include "../../models/conexion.php";

if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($_POST["accion"])) {
  $accion     = $_POST["accion"];
  $rutina_id  = $_POST["rutina_id"];
  $usuario_id = $_POST["usuario_id"];

  // Por si viene del modo eliminar y queremos mantenerlo en esa vista
  $modo = isset($_POST["modo"]) && $_POST["modo"] === "eliminar" ? "modo=eliminar" : "modo=asignar";

  if ($accion === "asignar") {
    // Verificar si ya existe esa asignación
    $stmt = $conexion->prepare(
      "SELECT * FROM rutina_asignada WHERE rutina_id = ? AND usuario_id = ?"
    );
    $stmt->execute([$rutina_id, $usuario_id]);

    if ($stmt->rowCount() > 0) {
      header("Location: ../../views/entrenador/asignar_rutina.php?error=ya_asignada&$modo");
      exit;
    }

    // Insertar nueva asignación
    $insert = $conexion->prepare("
      INSERT INTO rutina_asignada (rutina_id, usuario_id, fecha_asignacion)
      VALUES (?, ?, ?)
    ");
    $insert->execute([
      $rutina_id,
      $usuario_id,
      date('Y-m-d')
    ]);

    header("Location: ../../views/entrenador/asignar_rutina.php?success=asignada&$modo");
    exit;

  } elseif ($accion === "desasignar") {
    // Eliminar asignación
    $del = $conexion->prepare("
      DELETE FROM rutina_asignada
      WHERE rutina_id = ? AND usuario_id = ?
    ");
    $del->execute([$rutina_id, $usuario_id]);

    header("Location: ../../views/entrenador/asignar_rutina.php?success=desasignada&modo=eliminar");
    exit;
  }
}

// Si no es una solicitud válida
header("HTTP/1.1 400 Bad Request");
exit;

<?php
//Si la sesion iniciada no es la de un entrenador, lleva para inicio
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";

// Verifica si es para editar o crear
if ($_SERVER["REQUEST_METHOD"] === "POST") {
  $accion = $_POST["accion"] ?? "crear";

  // CREAR RUTINA
  if ($accion === "crear") {
    $nombre = trim($_POST["nombre"]);
    $descripcion = trim($_POST["descripcion"]);
    $entrenador_id = $_SESSION["id"];
    $ejercicios = $_POST["ejercicios"] ?? [];
    $sets = $_POST["sets"] ?? [];
    $reps = $_POST["reps"] ?? [];

    if (empty($nombre) || empty($ejercicios)) {
      echo "<script>alert('Debes ingresar un nombre y al menos un ejercicio'); history.back();</script>";
      exit;
    }

    try {
      $conexion->beginTransaction();

      $stmt = $conexion->prepare("INSERT INTO rutina (nombre, descripcion, entrenador_id) VALUES (?, ?, ?)");
      $stmt->execute([$nombre, $descripcion, $entrenador_id]);
      $rutina_id = $conexion->lastInsertId();

      $stmt_ej = $conexion->prepare("INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, sets, repeticiones) VALUES (?, ?, ?, ?)");
      foreach ($ejercicios as $ej_id) {
        $num_sets = isset($sets[$ej_id]) ? (int)$sets[$ej_id] : 1;
        $num_reps = isset($reps[$ej_id]) ? (int)$reps[$ej_id] : 1;
        $stmt_ej->execute([$rutina_id, $ej_id, $num_sets, $num_reps]);
      }

      $conexion->commit();
      echo "<script>alert('Rutina creada con éxito'); window.location.href = '../../views/Entrenador/rutinas_entrenador.php';</script>";
      exit;

    } catch (PDOException $e) {
      $conexion->rollBack();
      echo "<script>alert('Error al crear rutina: " . $e->getMessage() . "'); history.back();</script>";
      exit;
    }
  }

  // EDITAR RUTINA
  if ($accion === "editar") {
    $rutina_id = $_POST["rutina_id"];
    $nombre = trim($_POST["nombre"]);
    $descripcion = trim($_POST["descripcion"]);
    $ejercicios_actuales = $_POST["ejercicios"] ?? [];
    $eliminar_ids = $_POST["eliminar_ejercicios"] ?? [];
    $nuevos = $_POST["nuevos_ejercicios"] ?? [];
    $nuevos_sets = $_POST["nuevos_sets"] ?? [];
    $nuevos_reps = $_POST["nuevos_reps"] ?? [];

    try {
      $conexion->beginTransaction();

      // Actualizar datos de rutina
      $stmt = $conexion->prepare("UPDATE rutina SET nombre = ?, descripcion = ? WHERE id = ?");
      $stmt->execute([$nombre, $descripcion, $rutina_id]);

      // Eliminar o actualizar ejercicios existentes
      foreach ($ejercicios_actuales as $ej) {
        $id = $ej["id"];
        $sets = $ej["sets"];
        $reps = $ej["reps"];

        if (in_array($id, $eliminar_ids)) {
          $stmt = $conexion->prepare("DELETE FROM rutina_ejercicio WHERE rutina_id = ? AND ejercicio_id = ?");
          $stmt->execute([$rutina_id, $id]);
        } else {
          $stmt = $conexion->prepare("UPDATE rutina_ejercicio SET sets = ?, repeticiones = ? WHERE rutina_id = ? AND ejercicio_id = ?");
          $stmt->execute([$sets, $reps, $rutina_id, $id]);
        }
      }

      // Agregar nuevos ejercicios
      foreach ($nuevos as $id) {
        $sets = isset($nuevos_sets[$id]) ? (int)$nuevos_sets[$id] : 1;
        $reps = isset($nuevos_reps[$id]) ? (int)$nuevos_reps[$id] : 1;

        $stmt = $conexion->prepare("INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, sets, repeticiones) VALUES (?, ?, ?, ?)");
        $stmt->execute([$rutina_id, $id, $sets, $reps]);
      }

      $conexion->commit();
      echo "<script>alert('Rutina actualizada con éxito'); window.location.href = '../../views/Entrenador/rutinas_entrenador.php';</script>";
      exit;

    } catch (PDOException $e) {
      $conexion->rollBack();
      echo "<script>alert('Error al editar rutina: " . $e->getMessage() . "'); history.back();</script>";
      exit;
    }
  }

} elseif ($_GET["accion"] === "eliminar" && isset($_GET["id"])) {

  // ELIMINAR RUTINA
  $id = $_GET["id"];
  try {
    $stmt = $conexion->prepare("DELETE FROM rutina WHERE id = ?");
    $stmt->execute([$id]);
    echo "<script>alert('Rutina eliminada con éxito'); window.location.href = '../../views/Entrenador/rutinas_entrenador.php';</script>";
    exit;
  } catch (PDOException $e) {
    echo "<script>alert('Error al eliminar rutina: " . $e->getMessage() . "'); history.back();</script>";
    exit;
  }

} else {
  echo "<script>alert('Acceso no permitido'); history.back();</script>";
  exit;
}

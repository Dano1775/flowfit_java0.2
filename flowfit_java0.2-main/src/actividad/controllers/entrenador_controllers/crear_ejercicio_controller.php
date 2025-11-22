<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

include "../../models/conexion.php";

$nombre = trim($_POST["nombre"]);
$descripcion = trim($_POST["descripcion"]);
$creado_por = $_SESSION["id"];

// Verificar si hay imagen
if (isset($_FILES["imagen"]) && $_FILES["imagen"]["error"] === UPLOAD_ERR_OK) {
  $imagen = $_FILES["imagen"]["name"];
  $tmp = $_FILES["imagen"]["tmp_name"];
  $rutaDestino = "../../views/ejercicio_image_uploads/user_uploads/" . $imagen;

  if (move_uploaded_file($tmp, $rutaDestino)) {
    $sql = "INSERT INTO ejercicio_catalogo (nombre, descripcion, imagen, creado_por) VALUES (?, ?, ?, ?)";
    $stmt = $conexion->prepare($sql);
    $stmt->execute([$nombre, $descripcion, $imagen, $creado_por]);

    echo "<script>alert('Ejercicio creado correctamente'); window.location.href = '../../views/Entrenador/ejercicios_entrenador.php';</script>";
    exit;
  } else {
    echo "<script>alert('Error al subir la imagen'); history.back();</script>";
    exit;
  }

} else {
  echo "<script>alert('Faltan datos o imagen'); history.back();</script>";
  exit;
}
?>

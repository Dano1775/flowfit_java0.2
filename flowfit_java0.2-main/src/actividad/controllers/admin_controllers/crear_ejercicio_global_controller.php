<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../../Inicio/inicio.html");
  exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
  include "../../models/conexion.php";

  $nombre = $_POST["nombre"] ?? '';
  $descripcion = $_POST["descripcion"] ?? '';
  $imagen_nombre = null;

  if (isset($_FILES["imagen"]) && $_FILES["imagen"]["error"] === UPLOAD_ERR_OK) {
    $nombreOriginal = basename($_FILES["imagen"]["name"]);
    $imagen_nombre = uniqid() . "_" . $nombreOriginal;
    $ruta_destino = "../../views/ejercicio_image_uploads/" . $imagen_nombre;

    if (!move_uploaded_file($_FILES["imagen"]["tmp_name"], $ruta_destino)) {
      echo "<script>alert('Error al guardar la imagen.'); window.location.href='../../views/administrador/crear_ejercicio.php';</script>";
      exit;
    }
  } else {
    echo "<script>alert('Debe seleccionar una imagen válida.'); window.location.href='../../views/administrador/crear_ejercicio.php';</script>";
    exit;
  }

  $sql = "INSERT INTO ejercicio_catalogo (nombre, descripcion, imagen, creado_por) VALUES (?, ?, ?, NULL)";
  $stmt = $conexion->prepare($sql);
  $stmt->execute([$nombre, $descripcion, $imagen_nombre]);

  echo "<script>alert('¡Ejercicio creado con éxito!'); window.location.href='../../views/administrador/crear_ejercicio_global.php';</script>";
  exit;
}
?>

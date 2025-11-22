<?php
session_start();
// Este oedazo asegura que solo un usuario logeado pueda editar su propio perfil
if (!isset($_SESSION["id"])) {
  header("Location: ../Inicio/inicio.html");
  exit;
}

include_once __DIR__ . "/../models/conexion.php";

// solo se puede acceder por metodo POST
if ($_SERVER["REQUEST_METHOD"] === "POST") {
  $id = $_POST["id"];
  $nombre = trim($_POST["nombre"]);
  $numero_documento = trim($_POST["numero_documento"]);
  $telefono = trim($_POST["telefono"]);
  $correo = trim($_POST["correo"]);

  $stmt = $conexion->prepare("UPDATE usuario SET nombre = ?, numero_documento = ?, telefono = ?, correo = ? WHERE id = ?");
  $resultado = $stmt->execute([$nombre, $numero_documento, $telefono, $correo, $id]);

  if ($resultado) {
    $_SESSION["mensaje_perfil"] = "Cambios guardados exitosamente.";
    header("Location: ../views/editarperfil/editar_perfil.php?exito=1");
  } else {
    $_SESSION["mensaje_perfil"] = "Hubo un error al guardar los cambios.";
    header("Location: ../views/editarperfil/editar_perfil.php?exito=0");
  }
  exit;
} else {
  header("Location: ../Inicio/inicio.html");
  exit;
}

<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
  header("Location: ../../views/Inicio/inicio.html");
  exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
  require_once "../../models/usuario.php";

  $id = $_POST["id"];
  $nombre = $_POST["nombre"];
  $correo = $_POST["correo"];
  $telefono = $_POST["telefono"];
  $perfil = $_POST["perfil_usuario"];
  $estado = $_POST["estado"];
  $documento = $_POST["numero_documento"];

  $admin = new administrador();
  $usuarioActual = $admin->BuscarPorId($id);

  if (!$usuarioActual) {
    echo "<script>alert('Usuario no encontrado'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
  }

  // Por si no portan la real seriedad
  if ($usuarioActual["perfil_usuario"] === "Administrador") {
    if (in_array($estado, ["I", "R"])) {
      echo "<script>alert('Mano que estas haciendo?'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
      exit;
    }

    if (strtolower(trim($perfil)) !== "administrador") {
      echo "<script>alert('Mano que estas haciendo?'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
      exit;
    }
  } 

  // Ejecutar actualización
  try {
    include "../../models/conexion.php";
    $sql = "UPDATE usuario SET numero_documento = ?, nombre = ?, correo = ?, telefono = ?, perfil_usuario = ?, estado = ? WHERE id = ?";
    $stmt = $conexion->prepare($sql);
    $stmt->execute([$documento, $nombre, $correo, $telefono, $perfil, $estado, $id]);

    echo "<script>alert('Usuario actualizado correctamente'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
  } catch (Exception $e) {
    echo "<script>alert('Error al actualizar: " . $e->getMessage() . "'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
  }
}
?>

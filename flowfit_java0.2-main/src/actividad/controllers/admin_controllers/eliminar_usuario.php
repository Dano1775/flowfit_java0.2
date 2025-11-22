<?php
session_start();
if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Administrador") {
    header("Location: ../../views/Inicio/inicio.html");
    exit;
}

if (!isset($_GET["id"])) {
    echo "<script>alert('ID no proporcionado'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
}

require_once "../../models/usuario.php";
$admin = new administrador();
$usuario = $admin->BuscarPorId($_GET["id"]);

if (!$usuario) {
    echo "<script>alert('Usuario no encontrado'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
}

// Por si se pasan de chistosos
if ($usuario["perfil_usuario"] === "Administrador") {
    echo "<script>alert('Mano ni lo intentes'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
    exit;
}

// Por si portan la real seriedad
if ($admin->Eliminar($usuario["id"])) {
    echo "<script>alert('Usuario eliminado correctamente'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
} else {
    echo "<script>alert('Error al eliminar el usuario'); window.location.href='../../views/administrador/editar_usuarios.php';</script>";
}

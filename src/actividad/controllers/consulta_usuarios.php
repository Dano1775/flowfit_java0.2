<?php
include "../models/usuario.php";

$login = new usuario();
$respuesta = $login->Login($_POST["correo"], $_POST["clave"]);

if ($respuesta instanceof Exception) {
    echo "<script>
            alert('Error del servidor, intente más tarde');
            location.href='../views/inicio.html';
          </script>";
    exit;
}

if (!$respuesta) {
    echo "<script>
            alert('Correo o contraseña incorrectos');
            location.href='../views/inicio.html';
          </script>";
    exit;
}

// Verifica si es profesional y está pendiente
if (
    ($respuesta["perfil_usuario"] === "Entrenador" || $respuesta["perfil_usuario"] === "Nutricionista") &&
    $respuesta["estado"] !== "A"
) {
    echo "<script>
            alert('Tu cuenta aún no ha sido aprobada por el administrador');
            location.href='../views/inicio.html';
          </script>";
    exit;
}

// Inicio de sesión exitoso
session_start();
$_SESSION["id"] = $respuesta["id"];
$_SESSION["perfil"] = $respuesta["perfil_usuario"];

switch ($_SESSION["perfil"]) {
    case "Administrador":
        header("Location: ../views/administrador.php");
        break;
    case "Entrenador":
        header("Location: ../views/entrenador.php");
        break;
    case "Nutricionista":
        header("Location: ../views/nutricionista.html");
        break;
    case "Usuario":
        header("Location: ../views/usuario.html");
        break;
    default:
        echo "<script>
                alert('Perfil no reconocido');
                location.href='../views/inicio.html';
              </script>";
}
?>

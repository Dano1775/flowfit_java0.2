<?php
// controllers/login_controller.php

// Incluimos el modelo usando ruta absoluta dinámica. DIR permite que la ruta funcione siempre, 
// sin importar desde donde se ejecute el script.
include __DIR__ . "/../models/usuario.php";

$login = new usuario();
$respuesta = $login->Login($_POST["correo"], $_POST["clave"]);

//Si hay un error en la base de datos, muestra el error 
if ($respuesta instanceof Exception) {
    echo "<script>
            alert('Error del servidor, intente más tarde');
            window.location.href = '../views/Inicio/inicio.html';
          </script>";
    exit;
}

// Si no encontró usuario o clave
if ($respuesta === null) {
    echo "<script>
            alert('Credenciales incorrectas o inexistentes');
            window.location.href = '../views/Inicio/inicio.html';
          </script>";
    exit;
}

// Validacion si el entrenador aun no ha sido aceptado
if ($respuesta['estado'] === 'I') {
    echo "<script>
            alert('Tu cuenta aún no ha sido aprobada por el administrador.');
            window.location.href = '../views/Inicio/inicio.html';
          </script>";
    exit;
}

if ($respuesta['estado'] === 'R') {
    echo "<script>
            alert('Tu solicitud fue rechazada por el administrador.');
            window.location.href = '../views/Inicio/inicio.html';
          </script>";
    exit;
}

// Login exitoso, PHP recordara la sesion desde la que se inicio. Si se abre otra sesion la antigua 
// al moverse de pagina mandara a inicio
session_start();
$_SESSION["id"] = $respuesta["id"];
$_SESSION["perfil"] = $respuesta["perfil_usuario"];
$_SESSION["nombre"] = $respuesta["nombre"];

// Redirige según perfil
switch ($_SESSION["perfil"]) {
    case "Administrador":
        header("Location: ../views/administrador/administrador.php");
        break;
    case "Entrenador":
        header("Location: ../views/Entrenador/entrenador.php");
        break;
    case "Nutricionista":
        header("Location: ../views/Nutricionista/Nutricionista.html");
        break;
    case "Usuario":
        header("Location: ../views/Usuario/Usuario.php");
        break;
    default:
        echo "<script>
                alert('Perfil no reconocido');
                window.location.href = '../views/Inicio/inicio.html';
              </script>";
        break;
}

<?php
include "../models/usuario.php";

if (
    isset($_POST["numero_documento"]) && 
    isset($_POST["nombre"]) && 
    isset($_POST["telefono"]) && 
    isset($_POST["correo"]) && 
    isset($_POST["clave"]) && 
    isset($_POST["perfil_usuario"])
) {
    $documento = $_POST["numero_documento"];
    $nombre = $_POST["nombre"];
    $telefono = $_POST["telefono"];
    $correo = $_POST["correo"];
    $clave = $_POST["clave"]; // texto plano, no vuelvo a meterle mano a codificaciones en la vida
    $perfil = $_POST["perfil_usuario"];

    $usuario = new usuario();
    $respuesta = $usuario->Registrar($documento, $nombre, $telefono, $correo, $clave, $perfil);

    if ($respuesta instanceof Exception) {
        if ($respuesta->getCode() == 23000) {
            echo "<script>
                alert('Ya existe un usuario con ese correo');
                window.location.href = '../views/registro/registro.html';
            </script>";
        } else {
            echo "<script>
                alert('Error del servidor: " . $respuesta->getMessage() . "');
                window.location.href = '../views/registro/registro.html';
            </script>";
        }
    } else {
        //  Se muestra primero el mensaje, luego redirección automática
        if ($perfil === "Usuario") {
            echo "<script>
                alert('¡Registro exitoso! Ya puedes iniciar sesión.');
                window.location.href = '../views/inicio/inicio.html';
            </script>";
        } else {
            echo "<script>
                alert('Tu solicitud de registro fue enviada correctamente. Espera a que un administrador apruebe tu cuenta.');
                window.location.href = '../views/inicio/inicio.html';
            </script>";
        }
    }
} else {
    echo "<script>
            alert('Faltan datos en el formulario');
            window.location.href = '../views/registro/registro.html';
    </script>";
}

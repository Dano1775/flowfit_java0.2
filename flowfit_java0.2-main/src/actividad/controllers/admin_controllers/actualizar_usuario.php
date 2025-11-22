<?php
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    include "../../models/usuario.php";
    $admin = new administrador();
    $admin->Actualizar(
        $_POST["id"],
        $_POST["numero_documento"] ?? '', 
        $_POST["nombre"],
        $_POST["telefono"],
        $_POST["correo"],
        $_POST["perfil_usuario"],
        $_POST["estado"]
    );
    header("Location: ../../views/administrador/usuarios_pendientes.php");
    exit;
}
?>

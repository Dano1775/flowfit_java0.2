<?php
session_start();
include "../../models/usuario.php";
include "../../models/conexion.php"; // Necesario para el registro manual en la tabla

if (isset($_GET["id"])) {
    $usuarioId = $_GET["id"];
    $adminId = $_SESSION["id"] ?? null;

    if ($adminId === null) {
        echo "<script>
                alert('Sesión de administrador no iniciada.');
                window.location.href = '../../views/Inicio/inicio.html';
              </script>";
        exit;
    }

    $admin = new administrador();
    $admin->Aprobar($usuarioId);

    // Registrar en la tabla registro_aprobaciones
    try {
        $sql = "INSERT INTO registro_aprobaciones (usuario_id, admin_id, accion) VALUES (?, ?, 'Aprobado')";
        $stmt = $conexion->prepare($sql);
        $stmt->execute([$usuarioId, $adminId]);
    } catch (Exception $e) {
        // Si falla el registro, no detenemos el flujo, pero lo mostramos en consola
        error_log("Error al registrar aprobación: " . $e->getMessage());
    }

    echo "<script>
            alert('Usuario aprobado exitosamente.');
            window.location.href = '../../views/administrador/usuarios_pendientes.php';
          </script>";
} else {
    echo "<script>
            alert('ID de usuario no válido.');
            window.location.href = '../../views/administrador/usuarios_pendientes.php';
          </script>";
}
?>

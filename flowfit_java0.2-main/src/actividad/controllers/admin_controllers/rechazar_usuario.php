<?php
session_start();
include "../../models/usuario.php";
include "../../models/conexion.php";

if (isset($_GET["id"])) {
    $usuarioId = $_GET["id"];
    $adminId = $_SESSION["id"] ?? null;

    if (!$adminId) {
        echo "<script>
                alert('Sesión de administrador no iniciada.');
                window.location.href = '../../views/Inicio/inicio.html';
              </script>";
        exit;
    }

    // Rechazar usuario
    $sql = "UPDATE usuario SET estado = 'R' WHERE id = ?";
    $stmt = $conexion->prepare($sql);
    $stmt->execute([$usuarioId]);

    // Registrar en historial
    try {
        $sql = "INSERT INTO registro_aprobaciones (usuario_id, admin_id, accion) VALUES (?, ?, 'Rechazado')";
        $stmt = $conexion->prepare($sql);
        $stmt->execute([$usuarioId, $adminId]);
    } catch (Exception $e) {
        error_log("Error al registrar rechazo: " . $e->getMessage());
    }

    echo "<script>
            alert('Usuario rechazado correctamente.');
            window.location.href = '../../views/administrador/usuarios_pendientes.php';
          </script>";
} else {
    echo "<script>
            alert('ID de usuario no válido.');
            window.location.href = '../../views/administrador/usuarios_pendientes.php';
          </script>";
}
?>

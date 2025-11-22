<?php
include "../../models/conexion.php";

session_start();

if (!isset($_SESSION["id"]) || $_SESSION["perfil"] !== "Entrenador") {
    header("Location: ../../Inicio/inicio.html");
    exit;
}

$entrenador_id = $_SESSION["id"];

// Obtener todos los usuarios asignados al entrenador
$stmt = $conexion->prepare("
    SELECT u.*, 
           r.nombre as rutina_nombre,
           r.descripcion as rutina_descripcion,
           ar.fecha_asignacion
    FROM usuario u
    INNER JOIN asignacion_rutina ar ON u.id = ar.usuario_id
    INNER JOIN rutina r ON ar.rutina_id = r.id
    WHERE r.entrenador_id = ?
    ORDER BY ar.fecha_asignacion DESC
");

$stmt->execute([$entrenador_id]);
$usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Devolver los resultados a la vista
if (isset($_GET["json"])) {
    header("Content-Type: application/json");
    echo json_encode($usuarios);
    exit;
}
?>
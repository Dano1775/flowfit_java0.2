<?php

class usuario {

    public function Login($correo, $clave) {
        try {
            include "conexion.php";
            $sql = "SELECT * FROM usuario WHERE correo = ?";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$correo]);
            $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($usuario && $usuario['clave'] === $clave) {
                return $usuario;
            } else {
                return null;
            }
        } catch (Exception $e) {
            return $e;
        }
    }

    public function Registrar($documento, $nombre, $telefono, $correo, $clave, $perfil_usuario) {
        try {
            include "conexion.php";

            $sqlCheck = "SELECT * FROM usuario WHERE correo = ?";
            $stmtCheck = $conexion->prepare($sqlCheck);
            $stmtCheck->execute([$correo]);
            $usuarioExistente = $stmtCheck->fetch(PDO::FETCH_ASSOC);

            if ($usuarioExistente && $usuarioExistente['estado'] === 'R') {
                $sqlDelete = "DELETE FROM usuario WHERE correo = ?";
                $stmtDelete = $conexion->prepare($sqlDelete);
                $stmtDelete->execute([$correo]);
            }

            $estado = ($perfil_usuario == 'Entrenador' || $perfil_usuario == 'Nutricionista') ? 'I' : 'A';

            $sql = "INSERT INTO usuario (numero_documento, nombre, telefono, correo, clave, perfil_usuario, estado) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$documento, $nombre, $telefono, $correo, $clave, $perfil_usuario, $estado]);

            return true;
        } catch (Exception $e) {
            return $e;
        }
    }

    public function ConsultaGeneral() {
        try {
            include "conexion.php";
            $sql = "SELECT * FROM usuario";
            $stmt = $conexion->prepare($sql);
            $stmt->execute();
            return $stmt->fetchAll(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            return $e;
        }
    }

    public function ConsultaPendientes() {
        try {
            include "conexion.php";
            $sql = "SELECT * FROM usuario WHERE estado = 'I' AND perfil_usuario IN ('Entrenador', 'Nutricionista')";
            $stmt = $conexion->prepare($sql);
            $stmt->execute();
            return $stmt->fetchAll(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            return $e;
        }
    }

    public function Aprobar($id) {
        try {
            include "conexion.php";
            $sql = "UPDATE usuario SET estado = 'A' WHERE id = ?";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$id]);
            return true;
        } catch (Exception $e) {
            return $e;
        }
    }

    public function Eliminar($id) {
        try {
            include "conexion.php";
            $sql = "DELETE FROM usuario WHERE id = ?";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$id]);
            return true;
        } catch (Exception $e) {
            return $e;
        }
    }

    public function BuscarPorId($id) {
        try {
            include "conexion.php";
            $sql = "SELECT * FROM usuario WHERE id = ?";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$id]);
            return $stmt->fetch(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            return $e;
        }
    }

    public function Actualizar($id, $documento, $nombre, $telefono, $correo, $perfil_usuario, $estado) {
        try {
            include "conexion.php";
            $sql = "UPDATE usuario SET numero_documento = ?, nombre = ?, telefono = ?, correo = ?, perfil_usuario = ?, estado = ? WHERE id = ?";
            $stmt = $conexion->prepare($sql);
            $stmt->execute([$documento, $nombre, $telefono, $correo, $perfil_usuario, $estado, $id]);
            return true;
        } catch (Exception $e) {
            return $e;
        }
    }

    public function BuscarPorFiltro($q) {
        try {
            include "conexion.php";
            // Like es like
            $sql = "SELECT * FROM usuario
                    WHERE nombre LIKE ? OR correo LIKE ? OR telefono LIKE ? OR numero_documento LIKE ?";
            $stmt = $conexion->prepare($sql);
            // % es para busquedas parciales en SQL
            $like = "%$q%";
            $stmt->execute([$like, $like, $like, $like]);
            return $stmt->fetchAll(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            return $e;
        }
    }
}


class administrador extends usuario {

}

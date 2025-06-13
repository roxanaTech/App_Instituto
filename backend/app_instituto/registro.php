<?php
header('Content-Type: application/json');

// 1. Configuración corregida de conexión
$host = "localhost";
$user = "root";
$password = "";
$database = "app_instituto";

// 2. Conexión mejorada con manejo de errores
try {
    $conexion = new mysqli($host, $user, $password, $database);
    
    if ($conexion->connect_error) {
        throw new Exception("Error de conexión: " . $conexion->connect_error);
    }
    
    // 3. Verificar método POST
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        throw new Exception("Método no permitido");
    }

    // 4. Recibir datos con verificación mejorada
    $nombre = isset($_POST['name']) ? trim($_POST['name']) : '';
    $apellido = isset($_POST['lastname']) ? trim($_POST['lastname']) : '';
    $ci = isset($_POST['cindent']) ? trim($_POST['cindent']) : '';
    $telefono = isset($_POST['telf']) ? trim($_POST['telf']) : '';
    $email = isset($_POST['email']) ? trim($_POST['email']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    $imagen = isset($_POST['image']) ? trim($_POST['image']) : '';

    // 5. Validación mejorada
    $camposFaltantes = [];
    if (empty($nombre)) $camposFaltantes[] = "nombre";
    if (empty($apellido)) $camposFaltantes[] = "apellido";
    if (empty($ci)) $camposFaltantes[] = "cédula";
    if (empty($telefono)) $camposFaltantes[] = "teléfono";
    if (empty($email)) $camposFaltantes[] = "email";
    if (empty($password)) $camposFaltantes[] = "contraseña";

    if (!empty($camposFaltantes)) {
        throw new Exception("Campos requeridos faltantes: " . implode(", ", $camposFaltantes));
    }
    


    // 6. Hash de contraseña
    $password_hash = password_hash($password, PASSWORD_BCRYPT);
    
    // 7. Limitar tamaño de imagen (opcional)
    if (strlen($imagen) > 500000) { // ~500KB
        $imagen = ''; 
    }
    $imageData = base64_decode($imagen, true); 
    if ($imageData === false) {
        throw new Exception("Formato de imagen base64 inválido");
    }
    file_put_contents("test_image.jpg", $imageData);
    $tempFileSize = filesize("test_image.jpg");
    error_log("Tamaño archivo temporal: " . $tempFileSize . " bytes");

    // 8. Consulta preparada segura
    $stmt = $conexion->prepare("INSERT INTO usuarios (nombre, apellido, ci, telefono, email, password, imagen) VALUES (?, ?, ?, ?, ?, ?, ?)");
    error_log("Imagen guardada: " . strlen($imagen));
    
    if (!$stmt) {
        throw new Exception("Error al preparar la consulta: " . $conexion->error);
    }
    
    $stmt->bind_param("sssssss", $nombre, $apellido, $ci, $telefono, $email, $password_hash, $imagen);
    
    if (!$stmt->execute()) {
        throw new Exception("Error al ejecutar la consulta: " . $stmt->error);
    }
    $id = $conexion->insert_id;
    $result = $conexion->query("SELECT LENGTH(imagen) as img_length FROM usuarios WHERE id = $id");
    if ($row = $result->fetch_assoc()) {
        error_log("Tamaño imagen en BD: " . $row['img_length'] . " bytes");
    }
    error_log("Longitud string imagen recibida: " . strlen($imagen));
    error_log("Primeros 50 chars de imagen: " . substr($imagen, 0, 50));

    echo json_encode([
        'success' => true,
        'message' => 'Usuario registrado exitosamente',
        'user_id' => $conexion->insert_id
    ]);
    var_dump($imagen);
    exit();

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage(),
        'error_details' => isset($conexion) ? $conexion->error : null
    ]);
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conexion)) $conexion->close();
}
?>
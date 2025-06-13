<?php
header('Content-Type: application/json');

// 1. Configuración de conexión a bd
$host = "localhost";
$user = "root";
$password = "";
$database = "app_instituto";

try {
    $conexion = new mysqli($host, $user, $password, $database);
    
    if ($conexion->connect_error) {
        throw new Exception("Error de conexión: " . $conexion->connect_error);
    }
    
    // 3. Verificar método POST
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        throw new Exception("Método no permitido");
    }

    // 4. Recibir datos con verificación
    $nombre = isset($_POST['name']) ? trim($_POST['name']) : '';
    $apellido = isset($_POST['lastname']) ? trim($_POST['lastname']) : '';
    $ci = isset($_POST['cindent']) ? trim($_POST['cindent']) : '';
    $telefono = isset($_POST['telf']) ? trim($_POST['telf']) : '';
    $email = isset($_POST['email']) ? trim($_POST['email']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    $imagen = isset($_POST['image']) ? trim($_POST['image']) : '';

    // 5. Recepción y validación de datos
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
    // Validación del formato de email
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    throw new Exception("Formato de email inválido");
    }
    // Código de error para duplicados
    if ($stmt->execute() === false) {
    if ($conexion->errno == 1062) { 
        $errorMessage = $conexion->error;
        if (strpos($errorMessage, 'ci') !== false) {
            throw new Exception("La cédula ya está registrada");
        } elseif (strpos($errorMessage, 'email') !== false) {
            throw new Exception("El email ya está registrado");
        }
    }
    throw new Exception("Error al ejecutar la consulta: " . $stmt->error);
    }
    // contraseña segura
    if (strlen($password) < 8 || !preg_match('/[A-Z]/', $password) || !preg_match('/[0-9]/', $password)) {
    throw new Exception("La contraseña debe tener al menos 8 caracteres, una mayúscula y un número");
    }
    // 6. Hash de contraseña
    $password_hash = password_hash($password, PASSWORD_BCRYPT);
    
    // 7. Manejo y validación de imagen
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

    // 8. Consulta preparada y almacenamiento seguro
    $stmt = $conexion->prepare("INSERT INTO usuarios (nombre, apellido, ci, telefono, email, password, imagen) VALUES (?, ?, ?, ?, ?, ?, ?)");
    error_log("Imagen guardada: " . strlen($imagen));
    
    if (!$stmt) {
        throw new Exception("Error al preparar la consulta: " . $conexion->error);
    }
    
    $stmt->bind_param("sssssss", $nombre, $apellido, $ci, $telefono, $email, $password_hash, $imagen);
    
    if (!$stmt->execute()) {
        throw new Exception("Error al ejecutar la consulta: " . $stmt->error);
    }
    // Registro de usuario y verificación de imagen almacenada
    $id = $conexion->insert_id;
    $result = $conexion->query("SELECT LENGTH(imagen) as img_length FROM usuarios WHERE id = $id");
    if ($row = $result->fetch_assoc()) {
        error_log("Tamaño imagen en BD: " . $row['img_length'] . " bytes");
    }
    error_log("Longitud string imagen recibida: " . strlen($imagen));
    error_log("Primeros 50 chars de imagen: " . substr($imagen, 0, 50));
    // Respuesta al cliente
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
} finally { // Cierra la conexión y libera recursos
    if (isset($stmt)) $stmt->close();
    if (isset($conexion)) $conexion->close();
}
?>
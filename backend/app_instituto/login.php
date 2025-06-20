<?php
#Define la respuesta como JSON.
header('Content-Type: application/json');
#Activa la visualización y el registro de errores en el archivo php_errors.log para facilitar la depuración.
ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', 'php_errors.log');

$host = "localhost";
$user = "root";
$password = "";
$database = "app_instituto";

try {
    error_log("=== INICIO DEL PROCESO DE LOGIN ===");
    #Establece conexión con MySQL usando mysqli.
    $conexion = new mysqli($host, $user, $password, $database);
    
    if ($conexion->connect_error) {
        throw new Exception("Error de conexión: " . $conexion->connect_error);
    }
    #Se asegura de que la solicitud sea de tipo POST
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        throw new Exception("Método no permitido");
    }
    #Recuperación y validación de datos
    $username = isset($_POST['username']) ? trim($_POST['username']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    
    error_log("Datos recibidos - Usuario: ".$username." | Contraseña: ".str_repeat('*', strlen($password)));
    
    if (empty($username) || empty($password)) {
        throw new Exception("Usuario y contraseña requeridos");
    }
    #Consulta preparada para evitar inyecciones SQL
    $stmt = $conexion->prepare("SELECT id, nombre, apellido, password, imagen FROM usuarios WHERE email = ? OR ci = ?");
    
    if (!$stmt) {
        throw new Exception("Error al preparar la consulta: " . $conexion->error);
    }
    
    $stmt->bind_param("ss", $username, $username);
    
    if (!$stmt->execute()) {
        throw new Exception("Error al ejecutar la consulta: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        throw new Exception("Usuario no encontrado");
    }
    
    $user = $result->fetch_assoc();
    error_log("Usuario encontrado: ID ".$user['id']." - ".$user['nombre']." ".$user['apellido']);
    
    // Log del estado de la imagen
    $imagenLength = !empty($user['imagen']) ? strlen($user['imagen']) : 0;
    error_log("Longitud de la imagen en BD: ".$imagenLength." bytes");
    error_log("Primeros 50 caracteres de la imagen: ".substr($user['imagen'] ?? '', 0, 50));

    #Verificación de credenciales
    if (!password_verify($password, $user['password'])) {
        throw new Exception("Contraseña incorrecta");
    }

    // Verificación y manejo de la imagen en Base64
    $imagenBase64 = null;
    if (!empty($user['imagen'])) {
        $imagenBase64 = $user['imagen'];
        
        // Verificación detallada de la imagen
        error_log("Iniciando verificación de imagen...");
        
        // 1. Verificar si es Base64 válido
        $decoded = base64_decode($imagenBase64, true);
        if ($decoded === false) {
            error_log("ERROR: Falló el decode Base64 (formato inválido)");
            $imagenBase64 = null;
        } else {
            error_log("Base64 decode exitoso - tamaño decodificado: ".strlen($decoded)." bytes");
            
            // 2. Verificar si es una imagen válida
            $tempFile = tempnam(sys_get_temp_dir(), 'imgcheck');
            file_put_contents($tempFile, $decoded);
            // 3. Decodifica la imagen desde Base64, verificando que sea válida.
            $imageInfo = @getimagesize($tempFile);
            if ($imageInfo === false) {
                error_log("ERROR: El archivo decodificado no es una imagen válida");
                $imagenBase64 = null;
            } else {
                error_log("Imagen válida detectada - Tipo: ".$imageInfo['mime']." - Dimensiones: ".$imageInfo[0]."x".$imageInfo[1]);
                
                // Guardar copia para inspección
                /*$debugFile = 'debug_image_'.time().'.jpg';
                file_put_contents($debugFile, $decoded);
                error_log("Imagen guardada para inspección: ".$debugFile);*/
            }
            
            unlink($tempFile);
        }
    }
    #Prepara la respuesta JSON con los datos del usuario.
    $response = [
        'success' => true,
        'message' => 'Inicio de sesión exitoso',
        'id' => $user['id'],
        'nombre' => $user['nombre'],
        'apellido' => $user['apellido'],
        'image' => $imagenBase64 
    ];
    
    error_log("Preparando respuesta - Longitud imagen a enviar: ".(!empty($imagenBase64) ? strlen($imagenBase64) : 0)." bytes");
    #Envía la respuesta en formato JSON.
    echo json_encode($response);
    error_log("=== PROCESO DE LOGIN COMPLETADO ===");

} catch (Exception $e) { // Manejo de errores
    error_log("ERROR: ".$e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
} finally { // Cierre de conexiones
    if (isset($stmt)) $stmt->close();
    if (isset($conexion)) $conexion->close();
}
?>
# App Instituto - Conexión Android con MySQL

Aplicación Android para gestión de instituto con autenticación de usuarios, conectada a una base de datos MySQL mediante XAMPP.

## Estructura del Proyecto
```
/app_instituto/
├── frontend/ # Código de la app Android (XML, Kotlin/Java)
├── backend/ # Archivos PHP (conexión a BD, APIs)
├── database/ # Scripts SQL para crear la BD
└── README.md
```

## Requisitos Previos

- XAMPP instalado
- Android Studio (para modificar la app)
- Emulador Android o dispositivo físico para pruebas

## Configuración Inicial

1. **Base de Datos**:
   - Iniciar XAMPP y activar Apache y MySQL
   - Crear una base de datos llamada `app_instituto`
   - Ejecutar el script SQL ubicado en `database/script.sql` para crear la tabla `usuarios`

2. **Backend**:
   - Copiar la carpeta `backend/app_instituto` a `C:\xampp\htdocs\`
   - Verificar que los archivos PHP sean accesibles via `http://localhost/app_instituto/`

3. **Frontend**:
   - Abrir el proyecto en Android Studio
   - Configurar las URLs de conexión en el código Kotlin/Java para apuntar a tu servidor local

## Uso de la Aplicación

1. **Registro**:
   - Los nuevos usuarios pueden registrarse mediante el formulario de registro
   - Los datos se almacenan en la base de datos MySQL

2. **Login**:
   - Los usuarios existentes pueden autenticarse
   - Las credenciales se verifican contra la base de datos

3. **Pantalla Principal**:
   - Tras autenticación exitosa, se muestra la pantalla principal

## Configuración de Red para Emulador

Para probar en el emulador Android:
- Usar la IP `10.0.2.2` para acceder al localhost de tu máquina
- Las URLs en la app deben ser del tipo: `http://10.0.2.2/app_instituto/login.php`

## Equipo de Desarrollo

- [Nombre 1] - Rol (ej: Desarrollo Backend)
- [Nombre 2] - Rol (ej: Desarrollo Android)
- [Nombre 3] - Rol (ej: Base de Datos)

## Equipo de Desarrollo

| Integrante         | Rol                          | Responsabilidades                          |
|--------------------|------------------------------|-------------------------------------------|
| [Ana Marca]        | Backend/BD                   | Conexión PHP-MySQL, APIs, estructura BD   |
| [Palermo Jimenez]  | Frontend Mobile              | UI/UX, formularios Login/Registro         |
| [Rosly Zapata]     | Gestión Documental           | Informes, manuales, documentación técnica |

## Equipo de Desarrollo

- Palermo Jimenez - Frontend Android
  - Diseño de interfaces (XML)
  - Lógica de formularios en Kotlin
  - Conexión con APIs
    
- Ana Marca - Backend y Base de Datos
  - Desarrollo de APIs PHP
  - Diseño de estructura MySQL
  - Configuración de XAMPP

- Rosly Zapata - Documentación
  - Redacción de informes
  - Manuales de usuario

## Mejoras Futuras

- Implementar más funcionalidades en la pantalla principal
- Implementar seguridad adicional (HTTPS, hash de contraseñas más seguro)

## Solución de Problemas

Si la conexión falla:
1. Verificar que XAMPP está corriendo
2. Confirmar que la base de datos y tabla existen
3. Revisar los permisos de la carpeta en htdocs
4. Verificar que las URLs en la app son correctas

## Licencia
MIT

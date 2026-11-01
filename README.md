# AprendeApp – Tercer Desafío Práctico DSM

Aplicación Android desarrollada en Kotlin + XML con organización MVC, consumo REST mediante Retrofit y persistencia local de sesión/favoritos.

## Requisitos
- Android Studio (recomendado Ladybug o superior).
- JDK 17 configurado en Android Studio.
- Conexión a internet.
- Cuenta gratuita en MockAPI.

## 1. Crear la API en MockAPI
1. Entra a https://mockapi.io/ y crea una cuenta.
2. Crea un proyecto nuevo llamado `AprendeApp`.
3. Dentro del proyecto crea un recurso llamado exactamente `recursos`.
4. Agrega estos campos (además del `id` automático):
   - `titulo` (string)
   - `descripcion` (string)
   - `tipo` (string)
   - `enlace` (string)
   - `imagen` (string)
   - `promedio` (number)
   - `cantidadCalificaciones` (number)
5. Copia la URL base que MockAPI muestra para el recurso. Debe terminar en `/` y ser similar a `https://xxxxxxxx.mockapi.io/api/v1/`.
6. Abre `app/src/main/java/com/aprendeapp/network/ApiClient.kt` y reemplaza `https://TU_ID.mockapi.io/api/v1/` por tu URL real. No borres la diagonal final.
7. En MockAPI agrega al menos cinco registros de prueba. Ejemplo:

```json
{
  "titulo": "Introducción a Kotlin",
  "descripcion": "Conceptos básicos del lenguaje Kotlin para Android.",
  "tipo": "Tutorial",
  "enlace": "https://kotlinlang.org/docs/home.html",
  "imagen": "",
  "promedio": 0,
  "cantidadCalificaciones": 0
}
```

Repite con tipos `Libro`, `Video`, `Artículo` y `Tutorial`.

## 2. Abrir en Android Studio
1. Descomprime `AprendeApp.zip`.
2. Abre Android Studio y selecciona **Open**.
3. Selecciona la carpeta `AprendeApp` (la que contiene `settings.gradle.kts`).
4. Espera la sincronización de Gradle. Si pregunta por JDK, selecciona Embedded JDK 17.
5. Conecta un teléfono con depuración USB o crea un emulador.
6. Ejecuta con el botón verde **Run**.

## 3. Probar la aplicación
- Registra un usuario Estudiante o Docente.
- La contraseña debe tener 12 caracteres como mínimo, mayúscula, minúscula, número y uno de estos símbolos: `!@#$%^&*`.
- Estudiante: consultar, buscar, filtrar, abrir, guardar favoritos y calificar.
- Docente: además puede crear, editar y eliminar recursos desde la API.
- Cierra sesión y vuelve a entrar para comprobar la persistencia local.

## 4. Estructura MVC
- `model/Resource.kt`: modelo de datos.
- `view/`: actividades, interfaz y adaptador de tarjetas.
- `controller/`: validación de autenticación y operaciones de recursos.
- `network/`: Retrofit y endpoints REST.
- `storage/`: sesión, cuentas locales y favoritos.

## 5. Endpoints utilizados
- `GET /recursos`: listar recursos.
- `POST /recursos`: crear recurso.
- `PUT /recursos/{id}`: editar recurso.
- `DELETE /recursos/{id}`: eliminar recurso.
- `PATCH /recursos/{id}`: actualizar promedio y cantidad de calificaciones.

## Consideraciones importantes para la defensa
- MockAPI almacena los recursos y el promedio global de calificaciones.
- Las cuentas y favoritos de esta versión se guardan localmente en el dispositivo; por tanto, no se comparten entre dispositivos.
- La contraseña se guarda como resumen SHA-256 local, no como texto plano. Para un sistema de producción se debe usar autenticación real en un backend con contraseñas protegidas mediante un algoritmo específico para contraseñas (por ejemplo Argon2 o BCrypt), HTTPS y autorización del lado del servidor.
- MockAPI no proporciona por sí solo un sistema de permisos por rol. La interfaz restringe las acciones docentes, pero para seguridad real el backend también tendría que validar los roles.
- El campo `imagen` queda disponible en el modelo/API; esta versión prioriza la lista funcional con texto y enlace y no descarga imágenes remotas.

## Commits sugeridos (mínimo 10, todos con verbo)
1. `crear estructura inicial del proyecto`
2. `agregar modelos de recursos`
3. `configurar cliente Retrofit`
4. `implementar registro de usuarios`
5. `implementar inicio y cierre de sesión`
6. `validar contraseñas seguras`
7. `mostrar lista dinámica de recursos`
8. `agregar búsqueda y filtros`
9. `implementar gestión de favoritos`
10. `implementar calificación de recursos`
11. `agregar operaciones CRUD para docentes`
12. `mejorar interfaz y mensajes de respuesta`

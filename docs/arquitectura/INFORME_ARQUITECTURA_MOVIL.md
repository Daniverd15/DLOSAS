# Informe de arquitectura movil

## 1) Proyecto retomado

Se retoma la aplicacion movil Android del curso de aplicaciones moviles, ubicada en este repositorio (`DLOSAS`), con base en Kotlin, Jetpack Compose, Firebase y Retrofit.

## 2) Partes y modulos identificados en la version inicial

- **Presentacion UI (Compose):** `LoginScreen`, `RegisterScreen`, `HomeScreen`, `ProfileScreen`, `TallerScreen`, `VehiculosScreen`, `HistorialScreen`, `AgregarVehiculoScreen`, `DetalleVehiculoScreen`.
- **Navegacion/Punto de entrada:** `MainActivity` con estado de pantalla manual (`enum Screen`).
- **Logica de autenticacion:** `AuthViewModel` con acceso directo a `FirebaseAuth` y `FirebaseFirestore`.
- **Datos de usuario:** `UserRepository` para perfil y verificacion de admin.
- **Servicios externos:** `ApiService` (OpenRouteService), Firebase Auth y Firestore.
- **Mapa y domicilio:** `MapaFragment` y `DomicilioFragment` (interoperabilidad Compose + Fragments).

## 3) Evidencia del repositorio usado

- Repositorio actual de trabajo y evolucion arquitectonica: `DLOSAS`.
- Evidencia de implementacion original: estructura de paquetes `com.example.proyecto`, archivos de UI y capa de acceso a Firebase.
- Evidencia de mejora actual: nueva separacion por capa de datos de autenticacion en `data/auth`.

## 4) Cambios de arquitectura implementados (comparativo)

### Arquitectura inicial (media)

- `AuthViewModel` concentraba validacion, reglas, acceso a Firebase y manejo de errores.
- `MainActivity` coordinaba navegacion y parte de la logica de autenticacion.
- Acoplamiento fuerte a infraestructura (Firebase) desde la capa de presentacion.

### Arquitectura mejorada

- Se introduce interfaz `AuthRepository` para desacoplar la capa de presentacion de Firebase.
- Se implementa `FirebaseAuthRepository` como adaptador de infraestructura.
- `AuthViewModel` queda enfocado en estado de UI y orquestacion de casos de uso.
- Se aplica **inyeccion de dependencias por constructor** (`AuthRepository` en `AuthViewModel`).
- Se refuerza la **separacion de responsabilidades**: UI -> ViewModel -> Repository -> Firebase.

## 5) Enfoques aplicados

- **Patron Repository:** encapsula acceso a datos y servicios de autenticacion.
- **Inyeccion de dependencias:** dependencia abstraida (`AuthRepository`) en lugar de `FirebaseAuth/FirebaseFirestore` directos.
- **Separacion de responsabilidades:** presentacion separada de infraestructura.
- **Orientacion a Clean Architecture (progresiva):**
  - Presentacion: `MainActivity`, pantallas Compose, `AuthViewModel`.
  - Datos: `FirebaseAuthRepository`.
  - Contratos/abstracciones: `AuthRepository`.

## 6) Resultado esperado de la mejora

- Mejor mantenibilidad y testabilidad del modulo de autenticacion.
- Menor impacto al cambiar proveedor de backend (ej. Firebase -> otro servicio).
- Flujo mas claro para evolucionar a capas `domain` y casos de uso en iteraciones futuras.

## 7) Evidencia de archivos modificados en esta mejora

- `app/src/main/java/com/example/proyecto/auth/AuthViewModel.kt`
- `app/src/main/java/com/example/proyecto/MainActivity.kt`
- `app/src/main/java/com/example/proyecto/data/auth/AuthRepository.kt`
- `app/src/main/java/com/example/proyecto/data/auth/FirebaseAuthRepository.kt`

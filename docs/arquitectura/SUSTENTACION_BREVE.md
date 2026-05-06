# Sustentacion breve - mejoras de arquitectura movil

## Guion sugerido (3 a 5 minutos)

### 1. Problema inicial

La app funcionaba, pero la arquitectura tenia acoplamiento alto: el `AuthViewModel` mezclaba logica de presentacion, reglas de negocio y acceso directo a Firebase.

### 2. Objetivo de mejora

Reducir acoplamiento y mejorar mantenibilidad aplicando:

- Patron Repository.
- Inyeccion de dependencias por constructor.
- Separacion de responsabilidades por capas.

### 3. Cambios implementados

- Se creo el contrato `AuthRepository`.
- Se implemento `FirebaseAuthRepository` para encapsular integracion con Firebase.
- Se simplifico `AuthViewModel` para enfocarlo en estado de UI y emision de eventos.
- `MainActivity` ahora invoca `vm.signIn()` sin orquestar acceso a repositorio de autenticacion.

### 4. Beneficios obtenidos

- Menor dependencia de Firebase desde presentacion.
- Logica mas reutilizable y facil de probar.
- Base preparada para migrar a Clean Architecture completa (casos de uso y capa domain).

### 5. Cierre

La comparacion entre arquitectura inicial (media) y arquitectura mejorada evidencia una evolucion real del diseno y una mejor base para escalar funcionalidad sin degradar mantenibilidad.

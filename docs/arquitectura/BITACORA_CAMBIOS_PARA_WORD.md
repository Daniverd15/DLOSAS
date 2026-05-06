# Bitacora de cambios de arquitectura movil

## Datos generales

- **Proyecto:** Aplicacion movil Android (`DLOSAS`)
- **Branch de trabajo:** `Arquitectura-David`
- **Objetivo:** comparar arquitectura inicial (media) vs arquitectura mejorada

## Cambio 1 - Refactor de autenticacion con Repository

### Que se cambio

- Se creo la interfaz `AuthRepository` en `app/src/main/java/com/example/proyecto/data/auth/AuthRepository.kt`.
- Se implemento `FirebaseAuthRepository` en `app/src/main/java/com/example/proyecto/data/auth/FirebaseAuthRepository.kt`.
- Se refactorizo `AuthViewModel` para usar `AuthRepository` en lugar de acceder directamente a Firebase.
- Se actualizo `MainActivity` para invocar `vm.signIn()` sin pasar logica externa de verificacion de admin.

### Por que se cambio

- La version inicial tenia alto acoplamiento entre presentacion y servicios Firebase.
- Se necesitaba mejorar mantenibilidad y claridad del flujo de autenticacion.
- Se busco aplicar separacion de responsabilidades e inyeccion de dependencias por constructor.

### Beneficio obtenido

- Menor dependencia de infraestructura desde la UI/ViewModel.
- Codigo mas facil de probar y extender.
- Base preparada para evolucionar a Clean Architecture completa.

### Evidencia (commit)

- `081742a` - *Refactor authentication flow to repository abstraction.*

## Cambio 2 - Documentacion de evolucion arquitectonica y C4 final

### Que se cambio

- Se agrego informe tecnico en `docs/arquitectura/INFORME_ARQUITECTURA_MOVIL.md`.
- Se agrego modelo C4 final en `docs/arquitectura/C4_MODELO.md`.
- Se agrego guion de sustentacion en `docs/arquitectura/SUSTENTACION_BREVE.md`.

### Por que se cambio

- La entrega exige evidencia explicita del proceso de mejora arquitectonica.
- Se requiere mostrar niveles de abstraccion (modelo C4) y justificar decisiones tecnicas.

### Beneficio obtenido

- Entrega mas trazable y evaluable.
- Facilita la sustentacion oral y la comparacion entre estado inicial y mejorado.

### Evidencia (commit)

- `5abffe1` - *Document architectural evolution and final C4 model.*

## Resumen comparativo (antes vs despues)

- **Antes:** `AuthViewModel` mezclaba estado UI, reglas y acceso directo a Firebase.
- **Despues:** UI y ViewModel consumen una abstraccion (`AuthRepository`) y la infraestructura queda en `FirebaseAuthRepository`.
- **Impacto:** mejor modularidad, menor acoplamiento y mayor capacidad de evolucion.

## Texto corto para incluir en Word (sugerido)

Durante esta iteracion se mejoro la arquitectura de la aplicacion movil separando la logica de autenticacion de la capa de presentacion. Se implemento el patron Repository y se aplico inyeccion de dependencias por constructor en el `AuthViewModel`. Esta decision reduce el acoplamiento con Firebase y mejora la mantenibilidad del codigo. Adicionalmente, se documento el modelo C4 final y la comparacion entre la arquitectura inicial y la arquitectura mejorada, dejando evidencia de la evolucion por medio de commits en la branch `Arquitectura-David`.

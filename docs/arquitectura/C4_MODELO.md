# Modelo C4 de la aplicacion movil

## Nivel 1 - Contexto del sistema

```mermaid
flowchart LR
    U[Usuario final] --> APP[App movil Android]
    A[Administrador] --> APP
    APP --> FA[Firebase Authentication]
    APP --> FS[Firebase Firestore]
    APP --> ORS[OpenRouteService API]
    APP --> GM[Google Maps Services]
```

## Nivel 2 - Contenedores

```mermaid
flowchart TB
    subgraph Mobile["Contenedor: App Android (Kotlin + Compose)"]
      UI[UI Compose y Fragments]
      VM[ViewModels]
      REPO[Repositorios de datos]
      NET[Cliente API Retrofit]
    end

    UI --> VM
    VM --> REPO
    REPO --> NET

    REPO --> FA[(Firebase Auth)]
    REPO --> FS[(Firebase Firestore)]
    NET --> ORS[(OpenRouteService)]
    UI --> GM[(Google Maps SDK)]
```

## Nivel 3 - Componentes (modulo autenticacion mejorado)

```mermaid
flowchart LR
    Main[MainActivity y pantallas Login/Register] --> AVM[AuthViewModel]
    AVM --> ARI[AuthRepository - interfaz]
    ARI --> FAR[FirebaseAuthRepository - implementacion]
    FAR --> FAuth[(FirebaseAuth)]
    FAR --> FStore[(Firestore)]
```

## Nivel 4 - Codigo (ejemplo de interaccion)

```mermaid
sequenceDiagram
    participant UI as LoginScreen
    participant VM as AuthViewModel
    participant R as AuthRepository
    participant FR as FirebaseAuthRepository
    participant FA as Firebase Auth
    participant FS as Firestore

    UI->>VM: signIn()
    VM->>R: signIn(email, password)
    R->>FR: signIn(email, password)
    FR->>FA: signInWithEmailAndPassword()
    FR->>FS: validar isBanned / isAdmin
    FR-->>VM: SignInResult(uid, isAdmin)
    VM-->>UI: AuthEvent.Success / Error
```

## Comparacion rapida: C4 antes vs despues

- **Antes:** UI/ViewModel dependian en gran medida de Firebase directo.
- **Despues:** se introduce una abstraccion (`AuthRepository`) y un adaptador de infraestructura (`FirebaseAuthRepository`).
- **Efecto:** disminuye acoplamiento y mejora capacidad de pruebas por sustitucion de implementaciones.

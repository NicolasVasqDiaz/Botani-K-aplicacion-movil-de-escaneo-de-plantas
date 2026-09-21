# Plan de Corrección y Estabilización - Iteración 1 (Botani-k)

Este plan aborda los errores bloqueantes identificados tras la integración del código generado por Claude, asegurando que el proyecto compile y siga las directrices del Contexto Maestro.

## User Review Required

> [!IMPORTANT]
> **Cambio de compileSdk a 37**: Las versiones más recientes de Navigation Compose y Lifecycle requieren API 37. Se actualizará el build.gradle.
> **Corrección de Namespaces**: El código de Claude usa `com.botanik.app`, pero el proyecto real usa `com.example.botani_k_aplicaciondeescaneodeplantas`. Se ajustarán todos los archivos.

## Proposed Changes

### [Infraestructura y Gradle]

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/build.gradle.kts)
- Actualizar `compileSdk` y `targetSdk` a 37.
- Agregar dependencias faltantes: Firebase Auth, Firestore, DataStore, Material Icons Extended.

#### [NEW] [google-services.json](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/google-services.json)
- Restaurar el archivo de configuración de Firebase.

### [Arquitectura y Hilt]

#### [NEW] [BotaniKApplication.kt](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/src/main/java/com/example/botani_k_aplicaciondeescaneodeplantas/BotaniKApplication.kt)
- Implementar la clase Application con `@HiltAndroidApp`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/src/main/AndroidManifest.xml)
- Registrar `BotaniKApplication`.
- Agregar permiso de `INTERNET`.

#### [NEW] [AppModule.kt](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/src/main/java/com/example/botani_k_aplicaciondeescaneodeplantas/di/AppModule.kt)
- Proveer instancias de Firebase y DataStore para inyección de dependencias.

### [Corrección de Código]

#### [MODIFY] [Todos los archivos .kt](file:///C:/Users/NICONEIL/AndroidStudioProjects/BotaniK_Aplicaciondeescaneodeplantas/app/src/main/java/com/example/botani_k_aplicaciondeescaneodeplantas/)
- Reemplazar imports erróneos de `com.botanik.app` por el namespace correcto del proyecto.

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew assembleDebug` para verificar compilación exitosa.

### Manual Verification
- Sincronizar Gradle en Android Studio y verificar que no existan errores de "Unresolved reference" en el editor.

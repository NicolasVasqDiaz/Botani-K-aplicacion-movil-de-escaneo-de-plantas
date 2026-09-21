# CONTEXTO MAESTRO — PROYECTO BOTANI-K

## Visión
Botani-k es una app móvil educativa que usa IA para identificar la flora endémica 
de Cajamarca (Perú), brindando información botánica verificable y consejos de 
cuidado personalizados, para fomentar conciencia ambiental en jóvenes y estudiantes.

## Stack técnico (FIJO, no cambiar sin aprobación)
- Kotlin + Jetpack Compose + Material 3
- Arquitectura: MVVM + Clean Architecture (simplificada)
- Navegación: Navigation Compose
- DI: Hilt
- Cámara: CameraX
- Red: Retrofit + OkHttp + Coil
- Asincronía: Coroutines + Flow
- Backend: Firebase (Auth + Firestore + Storage + Cloud Functions)
- Caché offline: Room
- APIs de IA: Pl@ntNet (primaria) + Gemini (fallback)

## Restricciones
- 100% gratuito (sin presupuesto)
- 1 desarrollador (asistido por IA)
- 10 semanas restantes (semana 6 de 16)
- 4-8 hrs/semana efectivas
- Solo español (MVP)
- Enfoque: flora endémica de Cajamarca

## Paleta de colores
- Verde hoja: #4A7C59
- Verde claro: #7BAE7F
- Crema fondo: #F5F0E6
- Blanco: #FFFFFF
- Texto: #2D3A2E
- Acento tierra: #A67C52

## Tipografía
- Títulos: Nunito
- Cuerpo: Inter / Roboto

## Estructura de paquetes
com.botanik.app/
├── data/{remote,local,repository}
├── domain/{model,repository,usecase}
├── ui/{scan,plantdetail,garden,profile,auth,theme}
├── di/
└── MainActivity.kt

## Alcance MVP (NO ampliar sin autorización)
✅ Login Google, Escaneo, Identificación IA, Ficha de planta, Mi Jardín, 
   Perfil, Modo offline, Feedback de usuario
❌ Red social, tienda, gamificación, multi-idioma, accesibilidad avanzada

## Flujo principal
App → Carga → Escaneo → Foto → "Bot pensando..." → Identificación → 
Ficha → Guardar en Mi Jardín → Volver a escanear

## Tono de marca
"Amigo jardinero científico": cálido, curioso, riguroso pero accesible.

## Nombre del asistente IA interno: "Bot" 🌱
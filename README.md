# Botani-k 🌱

App móvil educativa para identificar, aprender y cuidar la flora endémica
de Cajamarca (Perú). Combina captura de fotos, catálogo botánico y
almacenamiento en la nube para fomentar la conciencia ambiental en
jóvenes y estudiantes.

## 🎯 Características

- **Login con Google** — Autenticación segura vía Firebase Auth con sesión persistente
- **Catálogo botánico** — 20 plantas endémicas de Cajamarca con información verificable
- **Captura de fotos** — Cámara (CameraX) y galería (Photo Picker)
- **Almacenamiento en la nube** — Cloudinary para fotos de usuario
- **Mi Jardín** — Colección personal con persistencia offline (Room + Firestore)
- **Catálogo** — 40 especies endémicas de Cajamarca, disponible también sin conexión
- **Ficha de planta** — Nombre científico, familia, región, floración, cuidados y curiosidades
- **Modo offline** — Plantas guardadas y catálogo consultables sin conexión

## 🛠️ Stack Técnico

| Capa | Tecnología |
|---|---|
| UI | Kotlin + Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Clean Architecture |
| DI | Hilt 2.60.1 |
| Navegación | Navigation Compose |
| Asincronía | Coroutines + Flow |
| Cámara | CameraX 1.4.1 |
| Galería | Photo Picker (ActivityResultContracts) |
| Red | Retrofit 2.11.0 + OkHttp 4.12.0 + Gson |
| Imágenes | Coil 2.7.0 |
| Backend | Firebase Auth + Firestore |
| Caché offline | Room 2.7.1 |
| Almacenamiento de fotos | Cloudinary |

## 🏗️ Arquitectura

```
com.botanik.app/
├── data/
│   ├── remote/          # DataSources (Firebase, Cloudinary, Retrofit)
│   ├── local/           # Room (Entity, DAO, Database)
│   ├── mapper/          # Conversión Entity ↔ Domain
│   └── repository/      # Implementaciones de repositorios
├── domain/
│   ├── model/           # Entidades de negocio (CatalogPlant, GardenPlant, UserProfile)
│   ├── repository/      # Interfaces de repositorios
│   └── usecase/         # Casos de uso (SignInWithGoogle, EnsureUserProfile)
├── ui/
│   ├── auth/            # Login, AuthViewModel
│   ├── onboarding/      # Onboarding
│   ├── scan/            # Escaneo (CameraCapture, GalleryPicker, PhotoSourceBottomSheet)
│   ├── catalog/         # Catálogo (lista + búsqueda)
│   ├── plantdetail/     # Ficha de planta
│   ├── garden/          # Mi Jardín (grid + eliminar)
│   ├── profile/         # Perfil editable
│   ├── navigation/      # NavHost, rutas
│   └── theme/           # Colores, tipografía, tema
├── di/                  # Módulos Hilt (AppModule, NetworkModule, RepositoryModule)
└── util/                # ImageUtils (Uri → ByteArray)

seed/                    # Scripts Node.js para poblar Firestore (40 plantas)
```

### Flujo de datos

```
UI (Composable) → ViewModel → UseCase → Repository → DataSource (Firestore/Room/Cloudinary)
```

### Estrategia offline-first

Room es la fuente de verdad inmediata; la nube sincroniza en segundo plano.

| Contenido | Caché local | Nube | Fallback sin red |
|---|---|---|---|
| Catálogo (40 plantas) | `catalog_plants` | `catalog` | ✅ Se sirve la copia local |
| Mi Jardín | `garden_plants` | `users/{uid}/garden` | ✅ Merge de ambas fuentes |
| Cuenta local | `local_users` + DataStore | `users/{uid}` | ✅ Sesión local persistente |

## 🚀 Setup

### Requisitos

- Android Studio (versión reciente con soporte para AGP 9.x)
- JDK 17
- Node.js 18+ (solo para el seed del catálogo)
- Cuenta Firebase (proyecto `botani-k`)
- Cuenta Cloudinary (preset unsigned `botanik_unsigned`)

### Configuración

1. **Clonar el proyecto**

   ```bash
   git clone <url-del-repo>
   cd BotaniK_Aplicaciondeescaneodeplantas
   ```

2. **Crear `local.properties` en la raíz:**

   Android Studio lo genera solo al abrir el proyecto. Solo necesita el SDK path:

   ```properties
   sdk.dir=<ruta al Android SDK>
   ```

   > La configuración de Cloudinary **ya viene en `gradle.properties`** (que sí
   > se versiona), así que no hay que escribirla a mano. Esos valores no son
   > secretos: el preset `unsigned` está diseñado para apps cliente y ya viaja
   > dentro del APK.

3. **Colocar `google-services.json` en `app/`** (descargado desde Firebase Console).
   Este archivo **sí se versiona**: no contiene claves privadas, solo la
   configuración del cliente (API key restringida por package name + SHA-1).

4. **Registrar SHA-1 de debug en Firebase Console:**

   ```bash
   ./gradlew signingReport
   ```

5. **Sincronizar Gradle y compilar:**

   ```bash
   ./gradlew assembleDebug
   ```

6. **(Opcional) Seed del catálogo**

   ⚠️ **El seed solo hace falta si la colección `catalog` está vacía.** Los
   datos ya viven en Firestore, así que un clon limpio **funciona sin correrlo**.

   Para poblarla (o recargarla) necesitas tu propia Service Account key:

   ```bash
   cd seed
   npm install
   # Descarga tu key y colócala en seed/ (ver seed/README.md — NUNCA se sube al repo)
   npm run check       # verifica credenciales y estado del catálogo
   npm run seed        # 20 plantas (seed principal)
   npm run seed:extra  # +20 plantas (flora peruana, prioridad Cajamarca)
   ```

   Esto pobla la colección `catalog` con 40 plantas endémicas y nativas.

   > **Seguros para re-ejecutar:** ambos scripts usan `merge: true` y omiten los
   > campos vacíos (`imageUrl: ''`), por lo que **no sobrescriben** las URLs de
   > imagen que se hayan subido manualmente a Firestore.

7. **Imágenes del catálogo**

   El campo `imageUrl` de cada planta se llena desde Firebase Console (o con un
   script propio). Debe ser una **URL directa de imagen** (ej.
   `https://upload.wikimedia.org/.../Uchuva_2005.jpg`), no un enlace de búsqueda
   de Google: Coil no puede cargar páginas de redirección.

   > **Nota sobre SVG:** `placehold.co` sirve las imágenes como **SVG**, que Coil
   > no decodifica por defecto. Por eso `BotaniKApplication` implementa
   > `ImageLoaderFactory` y registra `SvgDecoder` de `coil-svg`: sin ese
   > registro los placeholders del catálogo aparecen como un cuadro vacío.

## 📱 Uso

1. Iniciar sesión (con Google, o creando una cuenta local con correo y contraseña)
2. Tocar "Escanear planta"
3. Tomar foto o elegir de galería
4. Seleccionar la especie del catálogo
5. Ver la ficha con información completa
6. Guardar en "Mi Jardín"
7. Revisar la colección personal en cualquier momento (incluso offline)

## 📋 Requerimientos Cubiertos

### Primera entrega

| ID | Requerimiento | Estado |
|---|---|---|
| RU-01 | Onboarding de bienvenida | ✅ |
| RU-08 | Editar nombre de perfil | ✅ |
| RU-09 | Cambiar foto de perfil | ✅ |
| RU-10 | Configurar notificaciones | ✅ |

### Segunda entrega

| ID | Requerimiento | Estado |
|---|---|---|
| RU-02 | Autenticación con Google (persistente) | ✅ |
| RU-02b | Crear cuenta local con correo y contraseña | ✅ |
| RU-03 | Escanear planta (cámara/galería + catálogo) | ✅ |
| RU-04 | Ver ficha de planta | ✅ |
| RU-05 | Guardar planta en Mi Jardín | ✅ |
| RU-06 | Ver Mi Jardín (loading/empty/error/success) | ✅ |
| RU-07 | Ver detalle de planta guardada (modo lectura, **offline**) | ✅ Fallback a caché Room |
| RU-07b | Consultar el catálogo sin conexión | ✅ Copia local del catálogo en Room |
| RU-11 | Eliminar planta con confirmación | ✅ |

## ⚠️ Limitaciones Conocidas

- **Eliminación de fotos en Cloudinary:** al eliminar una planta, la foto permanece en Cloudinary (eliminación firmada requiere API Secret server-side). Requiere Cloud Function en una fase futura.
- **Identificación por IA/ML:** fuera del alcance de la 2ª entrega por indicación del profesor.
- **Sin notificaciones push reales:** solo se persiste la preferencia del usuario.
- **Fotos del catálogo:** 18 de las 40 plantas usan un placeholder SVG generado (verde con el nombre científico) en lugar de una fotografía real. Reemplazarlas por URLs de Wikimedia Commons cuando haya imágenes verificadas de cada especie.

## 🧪 Testing

```bash
./gradlew test
```

## 📄 Licencia

Proyecto académico — Ingeniería de Sistemas Computacionales.

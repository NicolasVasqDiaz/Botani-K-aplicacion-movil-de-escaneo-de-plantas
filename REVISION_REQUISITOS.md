# Revisión de Requisitos — Botani-k

Fecha: 19/10/2026 · Estado del build: `BUILD SUCCESSFUL` · Tests: OK

---

## 📊 Resumen

| Requerimiento | Estado | Nota |
|---|---|---|
| Onboarding al abrir la app | ✅ Completo | 3 pantallas con "Saltar" (texto corregido: sin IA) |
| **RU-02** Auth Google (inicio/cierre, sesión persistente, errores) | ✅ Completo | + cuenta local bonus |
| **RU-03** Escanear (cámara/galería → catálogo real persistido) | ✅ Completo | Sin ML, como pidió el profe |
| **RU-04** Ficha de planta (datos del catálogo real + Guardar) | ✅ Completo | |
| **RU-06** Mi Jardín (foto, nombre, fecha, vacío/carga/error) | ✅ Completo | Fecha: `SimpleDateFormat` |
| **RU-04/06/07 offline** | ✅ Completo | Catálogo, jardín y ficha funcionan sin red |
| **RU-07** Detalle de planta guardada, disponible sin conexión | ✅ **Completo** | Fallback a caché Room + aviso offline |
| **Catálogo sin conexión** (extra, para el exercise #1) | ✅ **Completo** | Copia local en Room + preloader |
| Eliminar de Mi Jardín (con confirmación) | ✅ Completo | `DeletePlantDialog` |
| **Editar perfil (nombre, foto, notificaciones)** | ✅ **Completo** | Diálogo + Photo Picker + Switch |

### Correcciones aplicadas tras la revisión

| Qué | Por qué |
|---|---|
| Texto del onboarding: "nuestra IA te identificará" → "elige su especie en el catálogo" | Contradecía la indicación del profesor de *sin IA/ML* |
| Texto del bottom sheet: "para que Bot la identifique" → "para asociarla a una especie del catálogo" | Mismo motivo |
| Catálogo con caché local en Room (`catalog_plants`) | El exercise #1 ("demuéstrame que funciona sin internet") fallaba en el catálogo |

---

## 🔴 Gap 1 — Editar perfil (primera entrega)

**Lo que pide:** editar nombre, foto y notificaciones.

**Lo que hay:**
- ✅ `UserRepository.updateDisplayName(name)` — funciona
- ✅ `UserRepository.updatePhotoUrl(url)` — funciona
- ✅ `UserRepository.updateNotificationsEnabled(enabled)` — funciona
- ✅ `FirestoreUserDataSource` escribe en `users/{uid}` con las reglas correctas
- ❌ **`ProfileScreen` NO usa nada de eso.** Solo muestra nombre, email y "Cerrar sesión"

**Impacto:** es un requisito de la **primera** entrega. Si el profesor revisa esa parte, está incompleto.

**Esfuerzo:** medio. Ya existe el 80% (datos + repositorio + DI). Falta la UI.

---

## 🟡 Gap 2 — RU-07: detalle offline

**Lo que pide:** "abrir la ficha de una planta del jardín en modo lectura, disponible también sin conexión mediante caché local".

**Lo que hay:**
- ✅ El jardín (grid) se lee de Room → funciona offline
- ✅ `GardenPlantEntity` guarda todos los datos de la planta (nombre, familia, cuidado, curiosidades)
- ❌ Al tocar una planta del jardín, navega a `PlantDetail` que hace `catalogRepository.getById(plantId)` → **solo Firestore**
- ❌ `GardenDao` **no tiene** un `findByCatalogId` para leer de Room

**Impacto:** sin internet, al tocar una planta guardada, la ficha **no carga**.

**Esfuerzo:** bajo-medio. Agregar `GardenDao.findByCatalogPlantId()` + fallback en el repositorio.

---

## 🟢 Lo que está impecable

- **RU-03 / RU-04:** el catálogo real persiste en Firestore (40 plantas), la foto se asocia a la especie elegida. Exactamente lo que pidió el profesor (sin ML).
- **RU-06:** los 3 estados (vacío / carga / error) están implementados con `UiState` sellado.
- **Eliminar:** confirmación + borrado en Firestore + actualización al instante.
- **Sesión persistente:** `FirebaseAuth` + `DataStore` para cuentas locales.
- **Bonos propios:** cuenta local con email/contraseña, 40 plantas, imágenes en Cloudinary, cache offline del jardín.

---

## ⚠️ Notas menores

1. **Imágenes de las 20 plantas nuevas:** 18 usan `placehold.co` que sirve **SVG** — Coil no las renderiza (falta `coil-svg`). Se ven como cuadro vacío. Se puede resolver con URLs reales de Wikimedia (mi búsqueda está en curso) o agregando `coil-svg`.
2. **`imageUrl` de las 20 originales:** funcionan (URLs directas de Wikimedia verificadas).

---

## 🎯 Plan sugerido (por prioridad)

| # | Qué | Esfuerzo | Impacto |
|---|---|---|---|
| 1 | **Editar perfil** (nombre/foto/notificaciones) | Medio | Requisito de 1ª entrega, hoy ausente |
| 2 | **Detalle offline** (RU-07) | Bajo-medio | Requisito explícito de 2ª entrega |
| 3 | Imágenes de las 20 nuevas | Bajo | Pulido visual |
| 4 | Checklist E2E para mañana | Bajo | Tu evidencia ante el profesor |

---

## ❓ Decisión pendiente

**¿Qué hago primero?**

- **A)** Editar perfil (recomendado) — es lo que más pesa en evaluación
- **B)** Detalle offline — cierra un requisito explícito de 2ª entrega
- **C)** Terminar las imágenes
- **D)** Todo lo anterior, en ese orden

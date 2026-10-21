# Ejercicios de práctica — Botani-k (Botón + navegación)

**6 ejercicios resueltos paso a paso**, del mismo estilo que el que te pidieron:
un botón que lleva a otra pantalla. Sin diseño, sin datos, sin ViewModel.

Todos usan **el mismo patrón**, así que con el Ejercicio 1 ya sabes hacer los demás.

---

## 📖 El patrón (lee esto primero)

Cada ejercicio tiene **4 pasos**. Siempre los mismos:

```
1. Una RUTA nueva          → en el archivo donde viven todas las rutas
2. Una PANTALLA nueva      → un @Composable propio, en su propia carpeta
3. Un CALLBACK             → en la pantalla origen: onNavigateToX: () -> Unit
4. Conectar el CALLBACK    → en el NavHost: onNavigateToX = { navController.navigate(...) }
```

> **La regla de oro de tu arquitectura:** el Composable **nunca** llama al
> `navController`. Solo recibe lambdas. El NavHost es el único que sabe navegar.
> Por eso el paso 3 y el 4 siempre van juntos.

### Referencia rápida: las 4 piezas y dónde van

| Pieza | Archivo | Qué es |
|---|---|---|
| Ruta | `ui/navigation/BotaniKRoutes.kt` | `data object X : BotaniKRoute("x")` |
| Pantalla | `ui/<carpeta>/<Carpeta>Screen.kt` | `@Composable fun XScreen(...)` |
| Callback | `ui/<carpeta>/<Carpeta>Screen.kt` (firma) | `onNavigateToX: () -> Unit` |
| Conexión | `ui/theme/BotaniKApp.kt` | `onNavigateToX = { navController.navigate(...) }` |

---

# ✏️ Ejercicio 1 — Botón "Acerca de" en el perfil

> **Pedido:** *"En Mi Perfil, agregá un botón 'Acerca de' que abra una pantalla con el nombre de la app, la versión y un botón para volver."*

## Paso 1 — La ruta

`app/src/main/java/com/botanik/app/ui/navigation/BotaniKRoutes.kt`

Agregá dentro del `sealed class`, junto a las demás:

```kotlin
    data object About : BotaniKRoute("about")
```

Queda así:

```kotlin
sealed class BotaniKRoute(val route: String) {
    data object Onboarding : BotaniKRoute("onboarding")
    data object Login : BotaniKRoute("login")
    data object Register : BotaniKRoute("register")
    data object SignIn : BotaniKRoute("sign_in")
    data object About : BotaniKRoute("about")        // ← NUEVO
    data object ScanHome : BotaniKRoute("scan_home")
    // ...
}
```

## Paso 2 — La pantalla

**Archivo nuevo:** `app/src/main/java/com/botanik/app/ui/about/AboutScreen.kt`

```kotlin
package com.botanik.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.botanik.app.ui.theme.*

/**
 * Pantalla "Acerca de" con la información de la aplicación.
 *
 * Qué:    muestra el nombre de la app, la versión y los créditos.
 * Cómo:   es una pantalla estática: no necesita ViewModel ni estado.
 * Por qué: sirve como destino de navegación simple para probar el flujo
 *         de rutas y callbacks de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        },
        containerColor = BackgroundCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(96.dp).background(SurfaceWhite, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", style = MaterialTheme.typography.displayMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Botani-k",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu amigo jardinero científico",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutRow("Versión", "1.0")
                    AboutRow("Plantas del catálogo", "40 especies")
                    AboutRow("Hecho para", "Cajamarca, Perú")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Volver al perfil", color = TextPrimary)
            }
        }
    }
}

/** Fila etiqueta / valor dentro de la tarjeta de información. */
@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

## Paso 3 — El callback en el perfil

`app/src/main/java/com/botanik/app/ui/profile/ProfileScreen.kt`

**3a.** Agregá el parámetro a la firma:

```kotlin
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit,        // ← NUEVO
    viewModel: ProfileViewModel = hiltViewModel()
) {
```

**3b.** Agregá el botón. Ponlo justo **antes** del botón "Cerrar sesión":

```kotlin
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Acerca de", color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))
```

## Paso 4 — Conectar en el NavHost

`app/src/main/java/com/botanik/app/ui/theme/BotaniKApp.kt`

**4a.** Import:

```kotlin
import com.botanik.app.ui.about.AboutScreen
```

**4b.** Registra la pantalla (junto a las demás `composable(...)`):

```kotlin
                composable(BotaniKRoute.About.route) {
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
```

**4c.** Pasa el callback en la pantalla de perfil:

```kotlin
                composable(BotaniKRoute.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(BotaniKRoute.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAbout = {
                            navController.navigate(BotaniKRoute.About.route)
                        }
                    )
                }
```

## ✅ Verificar

```bash
./gradlew assembleDebug
```

En el emulador: **Mi Perfil → "Acerca de"** → debe abrir la pantalla → botón atrás → vuelves al perfil.

---

# ✏️ Ejercicio 2 — Botón "Ver mi jardín" desde la pantalla de escanear

> **Pedido:** *"En la pantalla donde escaneas, agregá un botón 'Ver mi jardín' que lleve a Mi Jardín."*

## Paso 1 — La ruta

**No hace falta.** `BotaniKRoute.Garden` ya existe.

## Paso 2 — La pantalla

**Tampoco hace falta.** `GardenScreen` ya existe.

> 💡 Este es el ejercicio que enseña algo importante: **no todas las navegaciones
> necesitan una pantalla nueva.** A veces solo el botón y el callback.

## Paso 3 — El callback en la pantalla de escanear

`app/src/main/java/com/botanik/app/ui/scan/ScanScreen.kt`

**3a.** Firma actual (simplificada):

```kotlin
@Composable
fun ScanScreen(
    onBrowseCatalog: () -> Unit,
    onPhotoReady: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
```

**3b.** Agrégale el callback:

```kotlin
@Composable
fun ScanScreen(
    onBrowseCatalog: () -> Unit,
    onGoToGarden: () -> Unit,              // ← NUEVO
    onPhotoReady: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
```

**3c.** El botón. Ponlo después del botón "Elegir especie del catálogo":

```kotlin
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGoToGarden,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver mi jardín", color = TextPrimary)
            }
```

## Paso 4 — Conectar en el NavHost

```kotlin
                composable(BotaniKRoute.ScanHome.route) {
                    ScanScreen(
                        onBrowseCatalog = {
                            navController.navigate(BotaniKRoute.Catalog.route)
                        },
                        onGoToGarden = {
                            navController.navigate(BotaniKRoute.Garden.route)
                        },
                        onPhotoReady = { photoUri ->
                            navController.navigate(BotaniKRoute.Catalog.createRoute(photoUri))
                        }
                    )
                }
```

## ✅ Verificar

**Escanear → "Ver mi jardín"** → abre Mi Jardín.

> ⚠️ **Ojo con un detalle:** la pantalla de escanear **no tiene `TopAppBar`**, así
> que el usuario no ve la flecha de "atrás". Si navegas a Mi Jardín desde ahí,
> el usuario puede quedarse "atrapado" salvo que use el botón de abajo.
> **Por eso el botón de la parte de abajo (BottomBar) es importante** — siempre
> hay una salida.

---

# ✏️ Ejercicio 3 — Botón "Ver todo el catálogo" en la ficha de planta

> **Pedido:** *"En la ficha de una planta, agregá un botón 'Ver todo el catálogo' que lleve al catálogo."*

## ⚠️ Ojo: la ficha se abre desde dos lugares

`PlantDetailScreen` se abre desde:
- El catálogo (con foto del usuario)
- Mi Jardín (sin foto)

Por eso el catálogo tiene un **parámetro** (`userPhotoUrl`). Si navegas al
catálogo desde la ficha, tienes que decidir si pasas la foto o no.

## Paso 1 — La ruta

Ya existe: `BotaniKRoute.Catalog`.

Pero ojo, `Catalog.route` es `"catalog?userPhotoUrl={userPhotoUrl}"` y su
función `createRoute()` es:

```kotlin
data object Catalog : BotaniKRoute("catalog?userPhotoUrl={userPhotoUrl}") {
    fun createRoute(userPhotoUrl: String? = null) =
        "catalog?userPhotoUrl=${userPhotoUrl.orEmpty()}"
    const val ARG_USER_PHOTO_URL = "userPhotoUrl"
}
```

## Paso 2 — La pantalla

No hace falta.

## Paso 3 — El callback en la ficha

`app/src/main/java/com/botanik/app/ui/plantdetail/PlantDetailScreen.kt`

**3a.** Firma actual:

```kotlin
@Composable
fun PlantDetailScreen(
    plantId: String,
    userPhotoUrl: String?,
    onBack: () -> Unit,
    onGoToGarden: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
```

**3b.** Agregá el callback:

```kotlin
    onGoToGarden: () -> Unit,
    onGoToCatalog: () -> Unit,              // ← NUEVO
```

**3c.** El botón. Ponlo junto al de "Guardar en Mi Jardín":

```kotlin
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoToCatalog,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver todo el catálogo", color = TextPrimary)
            }
```

## Paso 4 — Conectar en el NavHost

```kotlin
                    PlantDetailScreen(
                        plantId = plantId,
                        userPhotoUrl = userPhotoUrl?.ifEmpty { null },
                        onBack = { navController.popBackStack() },
                        onGoToGarden = {
                            navController.navigate(BotaniKRoute.Garden.route) {
                                popUpTo(BotaniKRoute.ScanHome.route)
                            }
                        },
                        onGoToCatalog = {
                            navController.navigate(BotaniKRoute.Catalog.route)
                        }
                    )
```

> 🔍 **Por qué `BotaniKRoute.Catalog.route` y no `createRoute(...)`?**
> Porque `createRoute(userPhotoUrl)` construye la ruta con el parámetro. Si
> llamas a `.route` directamente, el `NavHost` lo resuelve con el valor por
> defecto (`null`) → el catálogo abre sin foto del usuario. **Ambas formas
> sirven**; elige la que tenga sentido:
> - `.route` → vuelves al catálogo "limpio"
> - `.createRoute(userPhotoUrl)` → vuelves al catálogo **con** la foto que
>   acabas de tomar, para seguir explorando con esa foto de referencia

## ✅ Verificar

**Catálogo → planta → "Ver todo el catálogo"** → vuelve al catálogo.

---

# ✏️ Ejercicio 4 — Botón "Volver al inicio" en Mi Jardín

> **Pedido:** *"En Mi Jardín, agregá un botón 'Volver al inicio' que lleve a la pantalla de escanear."*

## Paso 1 y 2 — Ruta y pantalla

Ya existen: `BotaniKRoute.ScanHome` y `ScanScreen`.

## Paso 3 — El callback en Mi Jardín

`app/src/main/java/com/botanik/app/ui/garden/GardenScreen.kt`

**3a.** Firma actual (solo 2 parámetros — el borrado se maneja dentro con
`viewModel::onDeleteClick`):

```kotlin
@Composable
fun GardenScreen(
    onPlantClick: (String) -> Unit,
    onScanClick: () -> Unit,
    viewModel: GardenViewModel = hiltViewModel()
) {
```

Agregá el nuevo:

```kotlin
@Composable
fun GardenScreen(
    onPlantClick: (String) -> Unit,
    onScanClick: () -> Unit,
    onGoToHome: () -> Unit,               // ← NUEVO
    viewModel: GardenViewModel = hiltViewModel()
) {
```

**3b.** El botón. Ponlo dentro del `Scaffold`, antes del `when (val state = uiState.plants)`:

```kotlin
    Scaffold(
        containerColor = BackgroundCream,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // ── NUEVO: botón para volver al inicio ──
            OutlinedButton(
                onClick = onGoToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Volver al inicio", color = TextPrimary)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState.plants) {
                    // ...
                }
            }
        }
    }
```

> 📌 **Nota:** al meter un `Column` nuevo dentro del `Scaffold`, el `Box` que
> contiene el `when` debe quedar **dentro** del `Column`. Si te olvidas de
> cerrar bien las llaves, el compilador te avisa.

## Paso 4 — Conectar en el NavHost

```kotlin
                composable(BotaniKRoute.Garden.route) {
                    GardenScreen(
                        onPlantClick = { plantId ->
                            navController.navigate(BotaniKRoute.PlantDetail.createRoute(plantId))
                        },
                        onScanClick = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                launchSingleTop = true
                            }
                        },
                        onGoToHome = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
```

> 🔍 **`launchSingleTop = true`** evita que se apilen varias copias de la misma
> pantalla si el usuario toca el botón varias veces. Ya lo usas en `onScanClick`.

## ✅ Verificar

**Mi Jardín → "Volver al inicio"** → vuelve a Escanear.

---

# ✏️ Ejercicio 5 — Botón que abre una ficha de planta con ID fijo

> **Pedido:** *"Agregá un botón, donde quieras, que abra directamente la ficha de la Chuquiragua."*

Este es el que **enseña el patrón de argumentos**, que es lo más importante.

## Cómo funciona el patrón de argumentos

Las rutas con parámetros se declaran con `{param}` y se construyen con una
función `createRoute(...)`. Mirá cómo ya lo hace `PlantDetail`:

```kotlin
data object PlantDetail : BotaniKRoute("plant_detail/{plantId}?userPhotoUrl={userPhotoUrl}") {
    fun createRoute(plantId: String, userPhotoUrl: String? = null) =
        "plant_detail/$plantId?userPhotoUrl=${userPhotoUrl.orEmpty()}"
    const val ARG_PLANT_ID = "plantId"
    const val ARG_USER_PHOTO_URL = "userPhotoUrl"
}
```

Y en el NavHost se leen los argumentos:

```kotlin
composable(
    route = BotaniKRoute.PlantDetail.route,
    arguments = listOf(
        navArgument(BotaniKRoute.PlantDetail.ARG_PLANT_ID) {
            type = NavType.StringType
        },
        navArgument(BotaniKRoute.PlantDetail.ARG_USER_PHOTO_URL) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { backStackEntry ->
    val plantId = backStackEntry.arguments?.getString(BotaniKRoute.PlantDetail.ARG_PLANT_ID)
    // ...
}
```

## La solución

### La opción A — Reusar la ruta que ya existe (lo más simple)

En **Mi Jardín**, dentro del `GardenScreen`, agrega un botón que navegue a la
Chuquiragua:

```kotlin
            OutlinedButton(
                onClick = { onPlantClick("chuquiragua") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver la Chuquiragua", color = TextPrimary)
            }
```

¡Y ya está! Porque `onPlantClick` **ya recibe el id de la planta** y ya está
conectado al NavHost:

```kotlin
onPlantClick = { plantId ->
    navController.navigate(BotaniKRoute.PlantDetail.createRoute(plantId))
},
```

> 💡 **Este es el punto clave:** el callback `onPlantClick` ya existía y ya
> sabe cómo navegar a una planta. Solo le pasas un id distinto. **No tuviste
> que tocar ni la ruta ni el NavHost.**

### La opción B — Crear una ruta nueva con parámetro

Si quisieras una pantalla propia, por ejemplo "Ficha rápida":

**1. La ruta:**

```kotlin
    data object QuickPlant : BotaniKRoute("quick_plant/{plantId}") {
        fun createRoute(plantId: String) = "quick_plant/$plantId"
        const val ARG_PLANT_ID = "plantId"
    }
```

**2. La pantalla** (`ui/quickplant/QuickPlantScreen.kt`):

```kotlin
@Composable
fun QuickPlantScreen(
    plantId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha rápida", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        },
        containerColor = BackgroundCream
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Planta seleccionada",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plantId,
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

**3. El NavHost:**

```kotlin
                composable(
                    route = BotaniKRoute.QuickPlant.route,
                    arguments = listOf(
                        navArgument(BotaniKRoute.QuickPlant.ARG_PLANT_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getString(
                        BotaniKRoute.QuickPlant.ARG_PLANT_ID
                    )
                    if (plantId == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        QuickPlantScreen(
                            plantId = plantId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
```

**4. El botón que lo abre** (en `GardenScreen`, con su callback):

```kotlin
            OutlinedButton(
                onClick = { onGoToQuickPlant("chuquiragua") },
                ...
            ) {
                Text("Ver la Chuquiragua", color = TextPrimary)
            }
```

Y en la firma de `GardenScreen`:

```kotlin
    onGoToQuickPlant: (String) -> Unit,     // ← NUEVO (nota: recibe un String)
```

Y en el NavHost:

```kotlin
                        onGoToQuickPlant = { plantId ->
                            navController.navigate(BotaniKRoute.QuickPlant.createRoute(plantId))
                        },
```

> 🔍 **Fíjate en la diferencia:**
> - `onPlantClick: (String) -> Unit` → recibe un id
> - `onGoToHome: () -> Unit` → no recibe nada
>
> Elige la firma según lo que tu botón necesite pasar.

---

# ✏️ Ejercicio 6 (bonus) — Botón en la pantalla de Acerca de

> **Pedido:** *"En la pantalla Acerca de, agregá un botón 'Ver mis plantas' que lleve a Mi Jardín."*

Este es el más rápido porque **ya hiciste todo el trabajo** en los ejercicios
anteriores. Solo practicas el "path corto".

## La solución

**Paso 3 — el callback** en `AboutScreen`:

```kotlin
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onGoToGarden: () -> Unit          // ← NUEVO
) {
```

El botón:

```kotlin
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGoToGarden,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver mis plantas", color = TextPrimary)
            }
```

**Paso 4 — el NavHost:**

```kotlin
                composable(BotaniKRoute.About.route) {
                    AboutScreen(
                        onBack = { navController.popBackStack() },
                        onGoToGarden = {
                            navController.navigate(BotaniKRoute.Garden.route)
                        }
                    )
                }
```

**¡Listo!** Dos archivos tocados, 6 líneas.

---

# 📋 Tabla resumen

| # | Ejercicio | Ruta nueva | Pantalla nueva | Callback | NavHost |
|---|---|---|---|---|---|
| 1 | Acerca de | ✅ | ✅ | ✅ | ✅ |
| 2 | Ver mi jardín | ❌ ya existe | ❌ ya existe | ✅ | ✅ |
| 3 | Ver todo el catálogo | ❌ ya existe | ❌ ya existe | ✅ | ✅ |
| 4 | Volver al inicio | ❌ ya existe | ❌ ya existe | ✅ | ✅ |
| 5 | Ficha con ID fijo | ✅ (opción B) | ✅ (opción B) | ✅ | ✅ |
| 6 | Ver mis plantas | ❌ ya existe | ❌ ya existe | ✅ | ✅ |

> 🎓 **Dato clave:** el 60% de los ejercicios de navegación **no necesitan
> ruta ni pantalla nueva**. Solo el botón y el callback. El error típico del
> principiante es crear rutas de más.

---

# 🧪 Cómo practicar sin romper nada

1. **Copia** tu proyecto antes de empezar:
   ```bash
   git checkout -b practica-botones
   ```
   Así puedes experimentar y luego volver:
   ```bash
   git checkout main
   ```

2. **Haz UN ejercicio**, compila, prueba en el emulador, y recién ahí pasa al siguiente.

3. **Commitea cada uno por separado** para que tu historia de git quede limpia:
   ```bash
   git add -A
   git commit -m "feat: boton Acerca de con navegacion a nueva pantalla"
   ```

4. Cuando termines todos:
   ```bash
   git checkout main
   git branch -d practica-botones   # si no los quieres mantener
   ```

---

# 🚨 Errores típicos (y cómo se ven)

| Error | Síntoma | Solución |
|---|---|---|
| Olvidaste registrar en el NavHost | El botón no hace nada | Revisa el `composable(...)` |
| Olvidaste pasar el callback | Error de compilación: "falta el parámetro X" | Pásalo en la `composable(...)` |
| Nombre de la lambda distinto | Error de compilación | Revisa: `onGoToAbout` vs `onNavigateToAbout` |
| `popBackStack()` en vez de `navigate` | El botón "atrás" no funciona | `navigate` = ir; `popBackStack` = volver |
| Olvidaste el `import` | "Unresolved reference" | Agrega el import del Composable nuevo |
| Pantalla nueva sin `Scaffold` | Se ve rara, sin fondo ni botón atrás | Copia el esqueleto de `CatalogScreen` |

---

# ✅ Checklist final de cada ejercicio

- [ ] `./gradlew assembleDebug` compila sin errores
- [ ] El botón aparece en la pantalla correcta
- [ ] Al tocarlo se abre la pantalla correcta
- [ ] El botón atrás / el gesto del sistema vuelve a la pantalla anterior
- [ ] No se rompió ningún flujo anterior (prueba: login → escanear → catálogo → guardar → jardín)

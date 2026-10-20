# Ejemplo Completo — Agregar una Pantalla

Ejemplo real y completo: **pantalla de "Estadísticas de Mi Jardín"**.

Solo toca la capa UI (usa datos que ya existen). Es el ejemplo perfecto para
practicar en 15 minutos si el profe te pide "agrega una pantalla de...".

**5 archivos.** Sigue los pasos en orden.

---

## Paso 1 — La ruta

**Archivo:** `ui/navigation/BotaniKRoutes.kt`

Agregá la entrada dentro del `sealed class`:

```kotlin
sealed class BotaniKRoute(val route: String) {
    data object Onboarding : BotaniKRoute("onboarding")
    data object Login : BotaniKRoute("login")
    data object Register : BotaniKRoute("register")
    data object SignIn : BotaniKRoute("sign_in")
    data object Stats : BotaniKRoute("stats")        // ← NUEVO
    data object ScanHome : BotaniKRoute("scan_home")
    // ... las demás ...
}
```

> **¿Por qué aquí?** Un `sealed class` con todas las rutas en un solo archivo
> hace imposible "inventar" una ruta que no existe. Todas las pantallas de la
> app están aquí.

---

## Paso 2 — El estado de la pantalla

**Archivo nuevo:** `ui/stats/StatsUiState.kt`

```kotlin
package com.botanik.app.ui.stats

import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.common.UiState

/**
 * Estado de la pantalla de estadísticas de Mi Jardín.
 *
 * Qué:    los datos del jardín y las cifras derivadas de ellos.
 * Cómo:   el ViewModel publica el jardín como [UiState] y calcula las
 *         estadísticas a partir de esa lista.
 * Por qué: vive en el ViewModel (state hoisting) para que la pantalla solo
 *         dibuje y no calcule nada.
 */
data class StatsUiState(
    val plants: UiState<List<GardenPlant>> = UiState.Loading,
    val totalPlants: Int = 0,
    val distinctFamilies: Int = 0,
    val distinctRegions: Int = 0,
    val mostRecentPlant: GardenPlant? = null
)
```

---

## Paso 3 — El ViewModel

**Archivo nuevo:** `ui/stats/StatsViewModel.kt`

```kotlin
package com.botanik.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de estadísticas de Mi Jardín.
 *
 * Qué:    observa el jardín del usuario y calcula las cifras que se muestran.
 * Cómo:   se suscribe a [GardenRepository.getGarden] y, cada vez que llegan
 *         datos, deriva los totales y los publica en [uiState].
 * Por qué: el cálculo queda en una sola parte y la pantalla no hace nada
 *         más que pintar el resultado.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gardenRepository.getGarden().collect { state ->
                val plants = (state as? UiState.Success)?.data.orEmpty()
                _uiState.value = _uiState.value.copy(
                    plants = state,
                    totalPlants = plants.size,
                    distinctFamilies = plants.map { it.family }.distinct().size,
                    distinctRegions = plants.map { it.region }.distinct().size,
                    mostRecentPlant = plants.maxByOrNull { it.savedAt }
                )
            }
        }
    }
}
```

### 🔑 Las 3 piezas clave del patrón

| Pieza | Para qué sirve |
|---|---|
| `MutableStateFlow` + `StateFlow` | El estado observable que la UI lee |
| `viewModelScope.launch` | Corrutina ligada al ciclo de vida del ViewModel |
| `collect { }` | Se re-activa solo cuando el Repository emite datos nuevos |

> **No necesitas un UseCase aquí** porque no hay lógica de negocio: solo
> transformación de datos que ya tienes. Un UseCase se crea cuando hay una
> **decisión** (validar, encadenar dos repositorios, etc.).

---

## Paso 4 — La pantalla

**Archivo nuevo:** `ui/stats/StatsScreen.kt`

```kotlin
package com.botanik.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.botanik.app.ui.common.UiState
import com.botanik.app.ui.theme.*

/**
 * Pantalla de estadísticas de Mi Jardín.
 *
 * Qué:    resume el jardín del usuario: cuántas plantas guardó, cuántas
 *         familias y regiones abarca, y cuál fue su último hallazgo.
 * Cómo:   dibuja el estado que publica [StatsViewModel]; no calcula nada.
 */
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis estadísticas", fontWeight = FontWeight.Bold) },
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
        when (val state = uiState.plants) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is UiState.Empty -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Guarda tu primera planta para ver tus estadísticas 🌱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.7f)
                )
            }

            is UiState.Success -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Plantas guardadas",
                    value = uiState.totalPlants.toString(),
                    emoji = "🌿"
                )
                StatCard(
                    title = "Familias distintas",
                    value = uiState.distinctFamilies.toString(),
                    emoji = "🧬"
                )
                StatCard(
                    title = "Regiones andinas",
                    value = uiState.distinctRegions.toString(),
                    emoji = "⛰️"
                )

                uiState.mostRecentPlant?.let { plant ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu último hallazgo",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = plant.commonName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = plant.scientificName,
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = plant.region,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta reutilizable con un dato estadístico. */
@Composable
private fun StatCard(title: String, value: String, emoji: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### 📌 Detalles que hacen que se vea como el resto de la app

| Detalle | Por qué |
|---|---|
| `TopAppBar` con `containerColor = BackgroundCream` | Igual que todas las demás pantallas |
| `Surface` con `RoundedCornerShape(16.dp)` + `shadowElevation = 2.dp` | Mismo estilo de cards que el catálogo |
| Colores del tema (`PrimaryGreen`, `TextPrimary`, `SurfaceWhite`) | Consistencia visual |
| `when (val state = ...)` con los 4 casos de `UiState` | El patrón de estados que ya usás |

---

## Paso 5 — Registrar en el NavHost

**Archivo:** `ui/theme/BotaniKApp.kt`

Dentro del `NavHost { ... }`, agregá:

```kotlin
composable(BotaniKRoute.Stats.route) {
    StatsScreen(
        onBack = { navController.popBackStack() }
    )
}
```

Y el import:

```kotlin
import com.botanik.app.ui.stats.StatsScreen
```

---

## Paso 6 (opcional) — Cómo llegar desde otra pantalla

### En `Mi Jardín` (`ui/garden/GardenScreen.kt`)

Agregá el botón en la pantalla (dentro del `Scaffold`, junto al título):

```kotlin
IconButton(onClick = onGoToStats) {
    Icon(Icons.Rounded.Insights, contentDescription = "Ver estadísticas")
}
```

Y modificá la firma de `GardenScreen`:

```kotlin
fun GardenScreen(
    onPlantClick: (String) -> Unit,
    onDeleteClick: (GardenPlant) -> Unit,
    onGoToStats: () -> Unit,          // ← NUEVO
    viewModel: GardenViewModel = hiltViewModel()
)
```

### En el NavHost (`BotaniKApp.kt`)

```kotlin
composable(BotaniKRoute.Garden.route) {
    GardenScreen(
        onPlantClick = { plantId -> ... },
        onDeleteClick = { plant -> ... },
        onGoToStats = { navController.navigate(BotaniKRoute.Stats.route) }   // ← NUEVO
    )
}
```

> **Importante:** el Composable **no** importa `navController`. Solo recibe
> la lambda. El NavHost es el único que sabe navegar. Por eso `GardenScreen`
> no necesita cambiar sus imports de navegación.

---

## ✅ Verificar que funciona

```bash
./gradlew assembleDebug
```

En el emulador:
1. Mi Jardín → botón de estadísticas
2. Debe mostrar las 3 tarjetas con los números
3. "Tu último hallazgo" con la planta más reciente
4. El botón atrás debe volver a Mi Jardín

---

## 🧠 Resumen: el orden de las capas

```
Paso 1  Ruta          (BotaniKRoutes)      ← la puerta de entrada
Paso 2  Estado        (StatsUiState)       ← qué datos necesita la pantalla
Paso 3  ViewModel     (StatsViewModel)     ← de dónde salen y qué se calcula
Paso 4  Pantalla      (StatsScreen)        ← cómo se ve
Paso 5  NavHost       (BotaniKApp)         ← cómo se llega
Paso 6  Origen        (GardenScreen)       ← cómo se dispara
```

**Siempre en ese orden.** Si una pantalla nueva no compila, el error casi
siempre es: falta registrar la ruta en el NavHost, o el `hiltViewModel()` no
encuentra la clase del ViewModel.

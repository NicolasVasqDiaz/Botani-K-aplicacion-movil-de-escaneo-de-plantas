# Guía Rápida — Cómo agregar cosas a Botani-k

Referencia para el día de la presentación, cuando el profesor pida agregar
una pantalla, un botón, un campo o un filtro.

> **Reglas del proyecto (AGENTS.md) que no debes romper:**
> - Código en **inglés**, textos de UI en **español**.
> - **State hoisting**: el estado vive en el ViewModel, nunca en el Composable.
> - Separación estricta: `UI → ViewModel → UseCase → Repository → DataSource`.
> - Errores con `Result<Unit>` / `UiState` (sealed), nunca excepciones sueltas.
> - KDoc en los métodos públicos.

---

## 🧠 El modelo mental

Tu app tiene 5 capas. Cada una habla **solo** con la de abajo:

```
┌──────────────────────────────────────────────────┐
│ UI            CatalogScreen, GardenScreen, ...    │  dibuja
│               @Composable + callbacks (onX)       │
├──────────────────────────────────────────────────┤
│ ViewModel     CatalogViewModel, GardenViewModel   │  decide
│               StateFlow<UiState> + UiState         │
├──────────────────────────────────────────────────┤
│ UseCase       SaveToGardenUseCase, ...            │  coordina
│               (opcional, si hay lógica de negocio)│
├──────────────────────────────────────────────────┤
│ Repository    CatalogRepository, GardenRepository  │  decide de dónde
│               (interface) + Impl (implementación) │
├──────────────────────────────────────────────────┤
│ DataSource    FirestoreCatalogDataSource,         │  habla con
│               GardenDao (Room), Cloudinary...      │  la nube/DB
└──────────────────────────────────────────────────┘
```

**Regla de oro:** si tocás una capa, no saltás capas. Un Composable **nunca**
llama a un Repository directamente. Un ViewModel **nunca** llama a Firestore.

---

## 📗 Receta 1 — Agregar un BOTÓN (la más probable)

**Ejemplo:** "Agregá un botón que diga 'Ver mis Favoritos'".

### Paso 1 — El ViewModel (el estado y la acción)

`ui/catalog/CatalogViewModel.kt` o donde toque. Mirá cómo ya lo hacés:

```kotlin
// 1a. El estado que la pantalla necesita
data class CatalogUiState(
    val searchQuery: String = "",
    val plants: UiState<List<CatalogPlant>> = UiState.Loading,
    val showFavorites: Boolean = false          // ← nuevo
)

// 1b. La acción
fun onFavoritesClick() {
    _uiState.value = _uiState.value.copy(showFavorites = true)
}
```

> **Nunca** declares `var showFavorites by remember { mutableStateOf(false) }`
> dentro del Composable. Eso es *state hoisting* roto.

### Paso 2 — La pantalla dibuja y emite

```kotlin
Button(onClick = viewModel::onFavoritesClick) {
    Text("Ver mis Favoritos")
}
```

**Patrón:** el Composable **no decide**, solo **dibuja** y **emite intención**
(`onClick = viewModel::algo`).

### Paso 3 — Si la acción necesita datos de Firestore

```kotlin
// En el ViewModel
fun onFavoritesClick() {
    viewModelScope.launch {
        catalogRepository.getFavorites().collect { favorites ->
            _uiState.value = _uiState.value.copy(favorites = favorites)
        }
    }
}
```

Y en el Repository (interface + impl):

```kotlin
// domain/repository/CatalogRepository.kt
fun getFavorites(): Flow<UiState<List<CatalogPlant>>>

// data/repository/CatalogRepositoryImpl.kt
override fun getFavorites(): Flow<UiState<List<CatalogPlant>>> = flow {
    emit(UiState.Loading)
    try {
        val plants = dataSource.fetchFavorites()
        emit(if (plants.isEmpty()) UiState.Empty else UiState.Success(plants))
    } catch (e: Exception) {
        emit(UiState.Error("No pudimos cargar tus favoritos"))
    }
}
```

---

## 📗 Receta 2 — Agregar una PANTALLA completa

**Ejemplo:** "Agregá una pantalla de Estadísticas de mi jardín".

### Paso 1 — La ruta (en `ui/navigation/BotaniKRoutes.kt`)

```kotlin
sealed class BotaniKRoute(val route: String) {
    // ... las existentes ...
    data object Stats : BotaniKRoute("stats")
}
```

> Si la ruta necesita **argumentos**, copiá el patrón de `PlantDetail`:
> ```kotlin
> data object PlantDetail : BotaniKRoute("plant_detail/{plantId}") {
>     fun createRoute(plantId: String) = "plant_detail/$plantId"
>     const val ARG_PLANT_ID = "plantId"
> }
> ```

### Paso 2 — El Composable (en `ui/stats/StatsScreen.kt`)

```kotlin
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()   // ← hiltViewModel, no ViewModel()
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
        Column(modifier = Modifier.padding(padding)) {
            // dibujas usando uiState
        }
    }
}
```

**Copiá el esqueleto de `CatalogScreen`** — es el patrón canónico de tu app.

### Paso 3 — El ViewModel

```kotlin
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gardenRepository.getGarden().collect { state ->
                _uiState.value = _uiState.value.copy(plants = state)
            }
        }
    }
}
```

### Paso 4 — Registrar en el NavHost (`ui/theme/BotaniKApp.kt`)

```kotlin
composable(BotaniKRoute.Stats.route) {
    StatsScreen(
        onBack = { navController.popBackStack() }
    )
}
```

### Paso 5 — Si necesitas navegación DESDE otra pantalla

En la pantalla origen, agregá un callback y en el NavHost pasá la navegación:

```kotlin
// En CatalogScreen (la firma)
fun CatalogScreen(
    onBack: () -> Unit,
    onPlantSelected: (CatalogPlant) -> Unit,
    onGoToStats: () -> Unit,          // ← nuevo
    viewModel: CatalogViewModel = hiltViewModel()
)

// En el NavHost
CatalogScreen(
    onBack = { navController.popBackStack() },
    onPlantSelected = { ... },
    onGoToStats = { navController.navigate(BotaniKRoute.Stats.route) }
)
```

> **Patrón clave:** el Composable **nunca** llama a `navController`. Solo
> recibe lambdas. El NavHost es el único que sabe navegar.

---

## 📗 Receta 3 — Agregar un CAMPO a un dato (Nivel 2)

**Ejemplo:** "Agregá un campo 'notas de campo' a la planta guardada".

Este toca **todas** las capas. Seguí el orden:

| # | Archivo | Qué agregás |
|---|---|---|
| 1 | `domain/model/GardenPlant.kt` | `val fieldNotes: String = ""` |
| 2 | `data/local/GardenPlantEntity.kt` | `val fieldNotes: String` (columna en Room) |
| 3 | `data/mapper/GardenMapper.kt` | Mapeo en ambos sentidos (`toDomain` / `toEntity`) |
| 4 | `data/remote/FirestoreGardenDataSource.kt` | `"fieldNotes" to plant.fieldNotes` al guardar + al leer |
| 5 | `data/repository/GardenRepositoryImpl.kt` | Pasar el valor al construir la entidad |
| 6 | `ui/plantdetail/PlantDetailScreen.kt` | El campo de texto + el botón "Guardar" |

### ⚠️ Room: cambiás la entity → cambiás la versión

`data/local/BotaniKDatabase.kt`:

```kotlin
@Database(
    entities = [GardenPlantEntity::class, LocalUserEntity::class, CatalogPlantEntity::class],
    version = 4,                          // ← subí el número
    exportSchema = false
)
```

> Tu app usa `fallbackToDestructiveMigration(dropAllTables = true)`, así que al
> cambiar la versión **se borra la base local** (el jardín se vacía y se
> re-sincroniza de Firestore). Avisale al profesor si notaste que las plantas
> guardadas desaparecieron tras una actualización.

### ⚠️ Firestore: si el documento viejo no tiene el campo

`getString("fieldNotes")` devuelve `null` si no existe → usa `?: ""` (valor por
defecto) para no romper los documentos ya guardados.

---

## 📗 Receta 4 — Agregar un FILTRO o chip al catálogo

**Ejemplo:** "Agregá un filtro por región (Jalca / Valles / Sierra)".

### 1 — El enum (`ui/catalog/CatalogUiState.kt`)

```kotlin
enum class CatalogFilter(val label: String) {
    ALL("Todas"),
    ENDEMIC("Endémicas de Cajamarca"),
    JALCA("Flores de la Jalca"),
    VALLE("Valles secos"),      // ← nuevo
    TREES("Árboles nativos"),
    MEDICINAL("Medicinales")
}
```

### 2 — El ViewModel (`ui/catalog/CatalogViewModel.kt`)

En el `combine`, agregá la rama del `when (filter)`:

```kotlin
CatalogFilter.VALLE -> plantsState.data.filter { it.region.contains("Valles", ignoreCase = true) }
```

> El enum **ya** se dibuja solo: `FilterChipsRow` itera `CatalogFilter.entries`.
> No necesitás tocar la pantalla para que aparezca el chip.

---

## 🔥 Los 5 pedidos más probables y qué hacer en cada uno

| Lo que te pide | Dónde tocar | Dificultad |
|---|---|---|
| "Agregá un botón que…" | ViewModel + Screen (§1) | ⭐ Fácil |
| "Agregá una pantalla de…" | Ruta + Screen + VM + NavHost (§2) | ⭐⭐ Media |
| "Mostrá el total de plantas endémicas" | `CatalogViewModel` (ya tenés `plantCount` de ejemplo) | ⭐ Fácil |
| "Agregá un campo 'X' a la planta" | Las 6 capas (§3) | ⭐⭐⭐ Media-alta |
| "Que el filtro por Y funcione" | Enum + `when` del filtro (§4) | ⭐ Fácil |

> **Atajo:** el contador "N plantas encontradas" que ya hiciste es el ejemplo
> perfecto de §1. Decirle al profesor *"esto lo resolví así"* demuestra que
> entendiste el patrón de state hoisting.

---

## ✅ Checklist antes de decir "listo"

- [ ] `./gradlew assembleDebug` compila sin errores
- [ ] La UI **no** tiene `mutableStateOf` para estado de negocio
- [ ] Los textos nuevos están en **español**, el código en **inglés**
- [ ] El ViewModel tiene KDoc si el método es público
- [ ] Si tocaste `BotaniKDatabase`, subiste la `version`
- [ ] Probaste en el emulador (no solo compila)
- [ ] `git diff` para ver qué cambiaste

---

## 🚨 Errores comunes

| Error | Cómo evitarlo |
|---|---|
| `remember { mutableStateOf() }` para estado de negocio | Va en el ViewModel |
| `navController.navigate()` dentro de un Composable | Solo el NavHost navega |
| Olvidar `merge` en Firestore al guardar | `set(..., SetOptions.merge())` |
| Subir la versión de Room y perder el jardín | Avisa al profesor o espera |
| Olvidar el `?: ""` al leer un campo nuevo | Los documentos viejos no lo tienen |
| Nombrar un Composable en minúscula | `ComposableNaming`: `MiPantalla` |
| Meter lógica en el Composable | El Composable dibuja, el ViewModel decide |

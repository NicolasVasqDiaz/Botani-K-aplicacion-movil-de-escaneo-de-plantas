package com.botanik.app.ui.catalog

import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(CatalogFilter.ALL)

    /**
     * Estado del catálogo ya filtrado por texto (commonName/scientificName).
     * El repositorio lo resuelve con la copia local de Room cuando no hay
     * conexión, por lo que el catálogo sigue disponible sin red.
     */
    private val _plantsState: Flow<UiState<List<CatalogPlant>>> =
        _searchQuery.debounce(300).flatMapLatest { query ->
            catalogRepository.search(query)
        }

    val uiState: StateFlow<CatalogUiState> = combine(
        _searchQuery.debounce(300),
        _activeFilter,
        _plantsState,
        catalogRepository.isServingFromCache

    ) { query, filter, plantsState, fromCache ->
        val filtered = when (plantsState) {
            is UiState.Success -> {
                val byFilter = when (filter) {
                    CatalogFilter.ALL -> plantsState.data
                    CatalogFilter.ENDEMIC -> plantsState.data.filter { it.isEndemic }
                    CatalogFilter.JALCA -> plantsState.data.filter { it.region.contains("Jalca", ignoreCase = true) }
                    CatalogFilter.TREES -> plantsState.data.filter { it.family in setOf("Scrophulariaceae", "Rosaceae") }
                    CatalogFilter.MEDICINAL -> plantsState.data.filter { it.family == "Adoxaceae" }
                }
                if (byFilter.isEmpty()) UiState.Empty else UiState.Success(byFilter)
            }
            else -> plantsState

        }
        /*-- Cambio para conteo de plantas filtradas*/
        // Cantidad de plantas visibles para el contador de la pantalla.
        // Qué:          el número de elementos de la lista ya filtrada.
        // Cómo:         se desenvuelve el estado sellado UiState con `when`;
        //               solo en la rama Success existe la lista, por eso se
        //               accede a `.data.size`. En Loading/Empty/Error no hay
        //               datos, así que el conteo es 0.
        // Por qué:       se calcula sobre `filtered` (no sobre `plantsState`)
        //               porque `plantsState` es el dato crudo previo al filtro
        //               por categoría; así el número siempre coincide con lo
        //               que el usuario ve en pantalla.
        val plantCount = when (filtered) {
            is UiState.Success -> filtered.data.size
            else -> 0
        }

        /*--fin*/

        CatalogUiState(searchQuery = query,
            activeFilter = filter,
            plants = filtered,
            /*--se añade el valor del conteo de plantas*/
            plantCount = plantCount,
            isFromCache = fromCache)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogUiState())

    fun onSearchChange(query: String) { _searchQuery.value = query }
    fun onFilterChange(filter: CatalogFilter) { _activeFilter.value = filter }
}

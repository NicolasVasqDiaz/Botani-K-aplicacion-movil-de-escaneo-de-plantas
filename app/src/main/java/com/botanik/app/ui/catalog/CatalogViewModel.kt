package com.botanik.app.ui.catalog

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
    private val _selectedRegion = MutableStateFlow<String?>(null)
    private val _selectedFamily = MutableStateFlow<String?>(null)

    // Debounce SOLO en la búsqueda al repositorio, no en el query expuesto
    private val _plantsState: Flow<UiState<List<CatalogPlant>>> =
        _searchQuery.debounce(300).flatMapLatest { query ->
            catalogRepository.search(query)
        }

    val uiState: StateFlow<CatalogUiState> = combine(
        _searchQuery,               // ← SIN debounce: el campo se ve inmediato
        _activeFilter,
        _selectedRegion,
        _selectedFamily,
        _plantsState,               // ← CON debounce: la búsqueda real sí espera 300ms
        catalogRepository.isServingFromCache
    ) { values ->
        val query = values[0] as String
        val filter = values[1] as CatalogFilter
        val region = values[2] as String?
        val family = values[3] as String?
        val plantsState = values[4] as UiState<List<CatalogPlant>>
        val fromCache = values[5] as Boolean

        val byCategory = when (plantsState) {
            is UiState.Success -> {
                when (filter) {
                    CatalogFilter.ALL -> plantsState.data
                    CatalogFilter.ENDEMIC -> plantsState.data.filter { it.isEndemic }
                    CatalogFilter.JALCA -> plantsState.data.filter {
                        it.region.contains("Jalca", ignoreCase = true)
                    }
                    CatalogFilter.TREES -> plantsState.data.filter {
                        it.family in setOf("Scrophulariaceae", "Rosaceae")
                    }
                    CatalogFilter.MEDICINAL -> plantsState.data.filter { it.family == "Adoxaceae" }
                }
            }
            else -> emptyList()
        }

        val byRegion = if (region == null) byCategory else byCategory.filter {
            it.region.contains(region, ignoreCase = true)
        }

        val byFamily = if (family == null) byRegion else byRegion.filter { it.family == family }

        val filtered = when (plantsState) {
            is UiState.Success -> if (byFamily.isEmpty()) UiState.Empty else UiState.Success(byFamily)
            else -> plantsState
        }

        val plantCount = when (filtered) {
            is UiState.Success -> filtered.data.size
            else -> 0
        }

        val allData = (plantsState as? UiState.Success)?.data ?: emptyList()
        val regions = allData.map { extractRegion(it.region) }.filter { it.isNotBlank() }.distinct().sorted()
        val families = allData.map { it.family }.filter { it.isNotBlank() }.distinct().sorted()

        CatalogUiState(
            searchQuery = query,
            activeFilter = filter,
            plants = filtered,
            plantCount = plantCount,
            isFromCache = fromCache,
            selectedRegion = region,
            selectedFamily = family,
            availableRegions = regions,
            availableFamilies = families
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogUiState())

    private fun extractRegion(rawRegion: String): String {
        val r = rawRegion.lowercase()
        return when {
            r.contains("cajamarca") -> "Cajamarca"
            r.contains("jalca") -> "Jalca"
            r.contains("costa") || r.contains("lomas") -> "Costa"
            r.contains("amazon") || r.contains("selva") -> "Amazonía"
            r.contains("andes") || r.contains("interandino") -> "Andes"
            else -> ""
        }
    }

    fun onSearchChange(query: String) { _searchQuery.value = query }
    fun onFilterChange(filter: CatalogFilter) { _activeFilter.value = filter }
    fun onRegionSelected(region: String?) { _selectedRegion.value = region }
    fun onFamilySelected(family: String?) { _selectedFamily.value = family }
}
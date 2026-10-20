package com.botanik.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.ui.common.UiState
import com.botanik.app.ui.theme.*
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateField.count

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    userPhotoUrl: String? = null,
    onBack: () -> Unit,
    onPlantSelected: (CatalogPlant) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel()

) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elige la especie", fontWeight = FontWeight.Bold) },
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
            SearchField(query = uiState.searchQuery, onQueryChange = viewModel::onSearchChange)
            FilterChipsRow(active = uiState.activeFilter, onFilterChange = viewModel::onFilterChange)
            // Contador de plantas visibles: se dibuja entre los filtros y la
            // lista para que el usuario sepa cuántas plantas está viendo
            // (con la búsqueda y el filtro actuales) antes de elegir una.
            CatalogCount(count = uiState.plantCount)
            // Aviso de modo offline: el catálogo se está mostrando desde la
            // copia local descargada previamente en el dispositivo.
            if (uiState.isFromCache) {
                OfflineBanner()
            }
            when (val state = uiState.plants) {
                is UiState.Loading -> CatalogLoading()
                is UiState.Empty -> CatalogEmpty(onReset = { viewModel.onSearchChange("") })
                is UiState.Error -> CatalogEmpty(onReset = { viewModel.onSearchChange("") })
                is UiState.Success -> CatalogList(plants = state.data, onPlantSelected = onPlantSelected)
            }
        }
    }
}
/**
 * Contador de plantas endémicas mostradas en el catálogo.
 *
 * Qué:    muestra cuántos resultados tiene el usuario a la vista.
 * Cómo:   recibe el número ya calculado desde el ViewModel y arma el texto
 *         con singular/plural según el valor.
 * Por qué: el conteo NO se calcula aquí — vive en el ViewModel, respetando
 *         la regla de *state hoisting* del proyecto. Este Composable solo
 *         traduce el número a texto y lo dibuja.
 */
@Composable
private fun CatalogCount(count: Int) {
    val label = if (count == 1) "1 planta encontrada" else "$count plantas encontradas"
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = TextPrimary.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
/* funcion composable para mostrar el conteo de plantas*/
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
        placeholder = { Text("Buscar por nombre común o científico...", color = TextPrimary.copy(alpha = 0.4f)) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PrimaryGreen) },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            focusedBorderColor = SecondaryGreen,
            unfocusedBorderColor = SurfaceContainerLow
        )
    )
}

@Composable
private fun FilterChipsRow(active: CatalogFilter, onFilterChange: (CatalogFilter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(CatalogFilter.entries) { filter ->
            FilterChip(
                selected = filter == active,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = SurfaceWhite,
                    containerColor = SurfaceWhite,
                    labelColor = TextPrimary
                ),
                shape = RoundedCornerShape(50)
            )
        }
    }
}

@Composable
private fun CatalogList(plants: List<CatalogPlant>, onPlantSelected: (CatalogPlant) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(plants, key = { it.id }) { plant ->
            CatalogPlantCard(plant = plant, onClick = { onPlantSelected(plant) })
        }
    }
}

@Composable
private fun CatalogPlantCard(plant: CatalogPlant, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerLow)
                ) {
                    if (plant.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = plant.imageUrl,
                            contentDescription = plant.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(plant.commonName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        plant.scientificName,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = SecondaryGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "${plant.family} • ${plant.region}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextPrimary.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun CatalogEmpty(onReset: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(48.dp).background(SurfaceContainerLow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Spa, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("No encontramos esa planta 🌱",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary)
            Text(
                "Intenta con otro nombre común, familia taxonómica o verifica la ortografía.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onReset) { Text("Restablecer búsqueda", color = PrimaryGreen) }
        }
    }
}

/**
 * Aviso de que el catálogo se está mostrando desde la copia local.
 * Se muestra cuando no hay conexión y los datos provienen de la caché.
 */
@Composable
private fun OfflineBanner() {
    Surface(
        color = SurfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sin conexión. Catálogo guardado en tu dispositivo",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun CatalogLoading() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {}
        }
    }
}

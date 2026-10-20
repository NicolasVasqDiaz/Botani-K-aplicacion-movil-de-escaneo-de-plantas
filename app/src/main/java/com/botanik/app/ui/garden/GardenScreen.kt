package com.botanik.app.ui.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.common.UiState
import com.botanik.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GardenScreen(
    onPlantClick: (String) -> Unit,
    onScanClick: () -> Unit,
    viewModel: GardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        containerColor = BackgroundCream,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState.plants) {
                is UiState.Loading -> GardenLoading()
                is UiState.Empty -> GardenEmpty(onScanClick)
                is UiState.Error -> GardenError(message = state.message, onRetry = viewModel::retry)
                is UiState.Success -> GardenGrid(
                    plants = state.data,
                    onPlantClick = { onPlantClick(it.catalogPlantId) },
                    onDeleteClick = viewModel::onDeleteClick
                )
            }
        }
    }

    uiState.plantPendingDelete?.let { plant ->
        DeletePlantDialog(
            plant = plant,
            onCancel = viewModel::onDismissDelete,
            onConfirm = viewModel::onConfirmDelete
        )
    }
}

@Composable
private fun GardenEmpty(onScanClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Mi Jardín", style = MaterialTheme.typography.headlineLarge, color = PrimaryGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tu jardín está esperando su primera planta 🌱", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Sal al campo, escanea una especie nativa y agrégala a tu herbario escolar.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Rounded.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear ahora", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GardenLoading() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cargando tu jardín andino...", style = MaterialTheme.typography.titleMedium, color = PrimaryGreen)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(6) {
                Surface(shape = RoundedCornerShape(16.dp), color = SurfaceWhite.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth().height(160.dp)) {}
            }
        }
    }
}

@Composable
private fun GardenError(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = EarthAccent, modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("No pudimos cargar tu jardín", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(
            "Verifica tu conexión a internet o accede a tus plantas descargadas en la memoria local.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Rounded.Sync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GardenGrid(plants: List<GardenPlant>, onPlantClick: (GardenPlant) -> Unit, onDeleteClick: (GardenPlant) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(plants, key = { it.gardenPlantId }) { plant ->
            GardenPlantCard(plant = plant, onClick = { onPlantClick(plant) }, onDeleteClick = { onDeleteClick(plant) })
        }
    }
}

@Composable
private fun GardenPlantCard(plant: GardenPlant, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = SurfaceWhite, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(SecondaryGreen.copy(alpha = 0.2f))
            ) {
                if (plant.userPhotoUrl != null) {
                    AsyncImage(
                        model = plant.userPhotoUrl,
                        contentDescription = plant.commonName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(plant.commonName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                plant.scientificName,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = SecondaryGreen,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Guardada el ${SimpleDateFormat("d MMM", Locale("es")).format(Date(plant.savedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
    }
}

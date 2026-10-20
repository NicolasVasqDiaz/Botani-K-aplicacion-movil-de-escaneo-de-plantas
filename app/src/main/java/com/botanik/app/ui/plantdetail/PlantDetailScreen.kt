package com.botanik.app.ui.plantdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.ui.theme.*

@Composable
fun PlantDetailScreen(
    plantId: String,
    userPhotoUrl: String?,
    onBack: () -> Unit,
    onGoToGarden: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(plantId) {
        viewModel.loadPlantById(plantId, userPhotoUrl)
    }

    if (uiState.plant == null) return

    Box(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Aviso de modo offline: la ficha se está mostrando desde la
            // caché local de Mi Jardín porque no hay conexión.
            if (uiState.isFromCache) {
                Surface(
                    color = SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sin conexión. Mostrando la copia guardada en tu dispositivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // Header fotográfico 40%
            Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(SecondaryGreen.copy(alpha = 0.25f))) {
                if (uiState.userPhotoUrl != null) {
                    AsyncImage(
                        model = uiState.userPhotoUrl,
                        contentDescription = "Foto de campo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.plant?.imageUrl != null) {
                    AsyncImage(
                        model = uiState.plant!!.imageUrl,
                        contentDescription = uiState.plant!!.commonName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).size(40.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                IconButton(
                    onClick = { viewModel.toggleFeedbackSheet(true) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Feedback, contentDescription = "Reportar corrección", tint = Color.White)
                }
            }

            // Sheet superpuesto
            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .background(SurfaceWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(20.dp)
            ) {
                Text(uiState.plant!!.commonName, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    uiState.plant!!.scientificName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = SecondaryGreen
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailChip(uiState.plant!!.family)
                    DetailChip("Jalca de Cajamarca")
                    DetailChip(uiState.plant!!.floweringSeason)
                }

                Spacer(modifier = Modifier.height(20.dp))
                ExpandableSection(
                    title = "Cuidados y cultivo",
                    subtitle = "Riego, insolación y sustrato andino",
                    expanded = uiState.isCareExpanded,
                    onToggle = viewModel::toggleCareExpanded
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CareRow("Exposición solar", uiState.plant!!.care["light"] ?: "")
                        CareRow("Régimen hídrico", uiState.plant!!.care["water"] ?: "")
                        CareRow("Sustrato", uiState.plant!!.care["temperature"] ?: "")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                ExpandableSection(
                    title = "Historia y curiosidades",
                    subtitle = "Significado cultural y ecología",
                    expanded = uiState.isCultureExpanded,
                    onToggle = viewModel::toggleCultureExpanded
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.plant!!.funFacts.forEach { fact ->
                            Text("• $fact", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = SurfaceWhite, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EnergySavingsLeaf, contentDescription = null, tint = SecondaryGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Consejo de Bot", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(uiState.plant!!.funFacts.firstOrNull() ?: "", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SaveButton(isSaved = uiState.isSavedToGarden, onSave = { viewModel.saveToGarden(); onGoToGarden() })
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (uiState.showFeedbackSheet) {
        FeedbackBottomSheet(
            plantName = uiState.plant!!.scientificName,
            onDismiss = { viewModel.toggleFeedbackSheet(false) },
            onSubmit = viewModel::submitFeedback
        )
    }
}

@Composable
private fun SaveButton(isSaved: Boolean, onSave: () -> Unit) {
    if (isSaved) {
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen),
            colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = PrimaryGreen)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardada", fontWeight = FontWeight.Bold)
        }
    } else {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("💾 Guardar en Mi Jardín", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(50)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(color = SurfaceWhite, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
                IconButton(onClick = onToggle) {
                    Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = PrimaryGreen)
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) { content() }
            }
        }
    }
}

@Composable
private fun CareRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackBottomSheet(
    plantName: String,
    onDismiss: () -> Unit,
    onSubmit: (FeedbackIssueType) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceWhite) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Reportar corrección botánica", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "¿Detectaste una discrepancia en $plantName o su clasificación regional? Tu reporte ayuda a validar la base de datos de Cajamarca.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeedbackIssueType.entries.forEach { issue ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        .clickable { onSubmit(issue) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(issue.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Entendido", color = PrimaryGreen)
            }
        }
    }
}

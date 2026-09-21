package com.example.botani_k_aplicaciondeescaneodeplantas.ui.plantdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.ConservationStatus
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.Plant
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    onBack: () -> Unit,
    onGoToGarden: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha de Planta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = SurfaceWhite) {
                Button(
                    onClick = {
                        viewModel.saveToGarden()
                        onGoToGarden()
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("🌱 Guardar en Mi Jardín", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = BackgroundCream
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            uiState.plant != null -> {
                PlantDetailContent(
                    plant = uiState.plant!!,
                    isCareExpanded = uiState.isCareExpanded,
                    isCultureExpanded = uiState.isCultureExpanded,
                    feedbackSubmitted = uiState.feedbackSubmitted,
                    onToggleCare = viewModel::toggleCareExpanded,
                    onToggleCulture = viewModel::toggleCultureExpanded,
                    onFeedback = viewModel::submitFeedback,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun PlantDetailContent(
    plant: Plant,
    isCareExpanded: Boolean,
    isCultureExpanded: Boolean,
    feedbackSubmitted: FeedbackStatus?,
    onToggleCare: () -> Unit,
    onToggleCulture: () -> Unit,
    onFeedback: (FeedbackStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).background(SecondaryGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfidenceBadge(plant.aiConfidence)
                ConservationBadge(plant.conservationStatus)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(plant.commonName, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(
            plant.scientificName,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = TextPrimary.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(plant.family)
            InfoChip(plant.habitat)
            InfoChip(plant.floweringSeason)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ExpandableSection(title = "Cuidados & Cultivo", expanded = isCareExpanded, onToggle = onToggleCare) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CareRow("Riego", plant.care.watering)
                CareRow("Radiación Solar", plant.care.sunlight)
                CareRow("Sustrato/Drenaje", plant.care.soil)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExpandableSection(title = "Curiosidades & Valor Cultural", expanded = isCultureExpanded, onToggle = onToggleCulture) {
            Text(plant.culturalFacts, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = SecondaryGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Dato curioso de Bot 💡", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(plant.botFact, style = MaterialTheme.typography.bodyMedium, color = TextPrimary.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Text("¿Es correcto este resultado?", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onFeedback(FeedbackStatus.ACCURATE) }, enabled = feedbackSubmitted == null) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sí, es exacto")
                }
                OutlinedButton(onClick = { onFeedback(FeedbackStatus.DISCREPANCY) }, enabled = feedbackSubmitted == null) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Discrepancia")
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    Surface(color = SurfaceWhite, shape = RoundedCornerShape(50), shadowElevation = 2.dp) {
        Text(
            "${"%.1f".format(confidence * 100)}% Certeza IA",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryGreen
        )
    }
}

@Composable
private fun ConservationBadge(status: ConservationStatus) {
    val color = when (status) {
        ConservationStatus.CRITICALLY_ENDANGERED -> ConservationCritical
        ConservationStatus.ENDANGERED -> ConservationEndangered
        ConservationStatus.VULNERABLE -> ConservationVulnerable
        ConservationStatus.LEAST_CONCERN -> ConservationLeastConcern
        ConservationStatus.DATA_DEFICIENT -> ConservationDataDeficient
    }
    Surface(color = SurfaceWhite, shape = RoundedCornerShape(50), shadowElevation = 2.dp) {
        Text(
            "SERFOR: ${status.code}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) }
                IconButton(onClick = onToggle) {
                    Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = PrimaryGreen)
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) { content() }
            }
        }
    }
}

@Composable
private fun CareRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
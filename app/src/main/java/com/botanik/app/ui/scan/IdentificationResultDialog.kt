package com.botanik.app.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.botanik.app.domain.model.PlantIdentification
import com.botanik.app.ui.theme.PrimaryGreen

@Composable
fun IdentificationResultDialog(
    identifications: List<PlantIdentification>,
    onSelect: (PlantIdentification) -> Unit,
    onDismiss: () -> Unit
) {
    val topConfidence = identifications.firstOrNull()?.confidence ?: 0f
    val lowConfidence = topConfidence < 0.20f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "¿Cuál es tu planta?",
                    fontWeight = FontWeight.Bold
                )
                if (lowConfidence) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "⚠️ Confianza baja. Elige la opción más probable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA67C52)
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    "Bot identificó estas opciones:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                identifications.forEach { candidate ->
                    CandidateRow(
                        candidate = candidate,
                        onClick = { onSelect(candidate) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CandidateRow(
    candidate: PlantIdentification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (candidate.rank == 0) {
                PrimaryGreen.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    candidate.scientificName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryGreen
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Confianza: ${(candidate.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (candidate.rank == 0) {
                Text(
                    "★",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun CatalogFallbackDialog(
    scientificName: String,
    onSearchCatalog: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Especie no encontrada", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Bot identificó:")
                Spacer(Modifier.height(4.dp))
                Text(
                    scientificName,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(Modifier.height(8.dp))
                Text("Esta especie no está en nuestro catálogo actual.")
            }
        },
        confirmButton = {
            TextButton(onClick = onSearchCatalog) {
                Text("Buscar en catálogo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
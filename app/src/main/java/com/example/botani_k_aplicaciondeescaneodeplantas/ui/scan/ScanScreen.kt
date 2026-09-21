package com.example.botani_k_aplicaciondeescaneodeplantas.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.*

@Composable
fun ScanScreen(onPlantIdentified: (String) -> Unit, viewModel: ScanViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ScanUiState.Identified) {
            onPlantIdentified(state.plantId)
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¡Hola! Soy Bot 🌱 ¿Qué planta descubrimos hoy en Cajamarca?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth().height(360.dp).background(TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is ScanUiState.BotThinking -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Bot pensando...", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    else -> {
                        Text("Visor de cámara (CameraX)", color = TextPrimary.copy(alpha = 0.4f))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onScanCaptured() },
                enabled = uiState !is ScanUiState.BotThinking,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ESCANEAR PLANTA", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.onScanCaptured() },
                enabled = uiState !is ScanUiState.BotThinking,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Subir foto de excursión de campo", color = TextPrimary)
            }
        }
    }
}
package com.botanik.app.ui.scan

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
import com.botanik.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBrowseCatalog: () -> Unit,
    onPhotoReady: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ScanUiState.PhotoReady) {
            onPhotoReady(state.photoUri)
            viewModel.onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(16.dp)
        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Visor de cámara (CameraX)",
                    color = TextPrimary.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onScanClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ESCANEAR PLANTA", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBrowseCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Elegir especie del catálogo", color = TextPrimary)
            }
        }

        // Overlay: bottom sheet para elegir fuente
        if (uiState is ScanUiState.ShowingSourceSheet) {
            PhotoSourceBottomSheet(
                onEvent = { event ->
                    when (event) {
                        is PhotoSourceEvent.OnCameraClick -> viewModel.onSourceSelected(isCamera = true)
                        is PhotoSourceEvent.OnGalleryClick -> viewModel.onSourceSelected(isCamera = false)
                        is PhotoSourceEvent.OnDismiss -> viewModel.onDismiss()
                    }
                }
            )
        }

        // Overlay: cámara a pantalla completa
        if (uiState is ScanUiState.CapturingPhoto) {
            CameraCapture(
                onPhotoCaptured = viewModel::onPhotoCaptured,
                onCancel = viewModel::onDismiss
            )
        }

        // Overlay: galería a pantalla completa
        if (uiState is ScanUiState.PickingPhoto) {
            GalleryPicker(
                onPhotoPicked = viewModel::onPhotoPicked,
                onCancel = viewModel::onDismiss
            )
        }
    }
}

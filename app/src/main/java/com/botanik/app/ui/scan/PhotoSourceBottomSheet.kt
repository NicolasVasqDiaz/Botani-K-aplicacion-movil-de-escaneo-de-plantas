package com.botanik.app.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.botanik.app.ui.theme.*

sealed interface PhotoSourceEvent {
    data object OnCameraClick : PhotoSourceEvent
    data object OnGalleryClick : PhotoSourceEvent
    data object OnDismiss : PhotoSourceEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    onEvent: (PhotoSourceEvent) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(PhotoSourceEvent.OnDismiss) },
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Elige una opción", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Selecciona cómo capturar la planta para asociarla a una especie del catálogo",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            PhotoSourceOption(
                icon = Icons.Rounded.CameraAlt,
                iconBg = PrimaryGreen,
                cardBg = Color(0xFFF5F8F5),
                title = "Tomar foto",
                subtitle = "Usa la cámara para enfocar una hoja o flor fresca",
                onClick = { onEvent(PhotoSourceEvent.OnCameraClick) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PhotoSourceOption(
                icon = Icons.Rounded.PhotoLibrary,
                iconBg = SecondaryGreen,
                cardBg = Color(0xFFF2F7F2),
                title = "Elegir de galería",
                subtitle = "Sube una foto tomada en tu excursión de campo",
                onClick = { onEvent(PhotoSourceEvent.OnGalleryClick) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { onEvent(PhotoSourceEvent.OnDismiss) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cancelar", color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    cardBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}

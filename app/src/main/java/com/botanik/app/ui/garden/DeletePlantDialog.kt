package com.botanik.app.ui.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.theme.*

@Composable
fun DeletePlantDialog(
    plant: GardenPlant,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(28.dp),
        containerColor = SurfaceWhite,
        icon = {
            Box(modifier = Modifier.size(48.dp).background(DeleteRedContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = DeleteRed)
            }
        },
        title = {
            Text("¿Eliminar planta?", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Text(
                "Esta acción quitará ${plant.commonName} de tu jardín. No se puede deshacer y se borrarán tus notas de campo asociadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, modifier = Modifier.height(48.dp)) {
                Text("Cancelar", color = PrimaryGreen)
            }
        }
    )
}

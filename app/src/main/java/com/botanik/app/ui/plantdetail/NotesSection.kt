package com.botanik.app.ui.plantdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.botanik.app.domain.model.PlantNote
import com.botanik.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesSection(
    notes: List<PlantNote>,
    onAddNote: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Diario de la planta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Ej: Hoy la regué...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onAddNote(text)
                            text = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Añadir")
                }
            }

            if (notes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                notes.forEach { note ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = dateFormat.format(Date(note.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryGreen
                        )
                        Text(
                            text = note.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text("Aún no hay notas. ¡Agrega la primera!", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}
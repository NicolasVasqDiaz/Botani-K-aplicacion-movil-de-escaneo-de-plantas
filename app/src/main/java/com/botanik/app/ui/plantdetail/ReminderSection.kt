package com.botanik.app.ui.plantdetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.botanik.app.ui.theme.PrimaryGreen
import com.botanik.app.ui.theme.SecondaryGreen
import com.botanik.app.ui.theme.SurfaceWhite
import com.botanik.app.ui.theme.TextPrimary

@Composable
fun ReminderSection(
    enabled: Boolean,
    frequencyDays: Int,
    isUpdating: Boolean,
    onToggle: (Boolean) -> Unit,
    onFrequencyChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recordatorio de cuidado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (enabled) "Activo — recibirás avisos" else "Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryGreen
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = !isUpdating,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SurfaceWhite,
                        checkedTrackColor = PrimaryGreen
                    )
                )
            }

            if (enabled) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Frecuencia:",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrequencyChip("Diario", 1, frequencyDays, onFrequencyChange)
                    FrequencyChip("Cada 3 días", 3, frequencyDays, onFrequencyChange)
                    FrequencyChip("Semanal", 7, frequencyDays, onFrequencyChange)
                    FrequencyChip("Quincenal", 15, frequencyDays, onFrequencyChange)
                }
            }

            if (isUpdating) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun FrequencyChip(
    label: String,
    days: Int,
    currentDays: Int,
    onChange: (Int) -> Unit
) {
    FilterChip(
        selected = currentDays == days,
        onClick = { onChange(days) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryGreen,
            selectedLabelColor = SurfaceWhite,
            containerColor = SurfaceWhite,
            labelColor = TextPrimary
        ),
        shape = RoundedCornerShape(50)
    )
}
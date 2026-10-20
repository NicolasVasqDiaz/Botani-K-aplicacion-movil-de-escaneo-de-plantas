package com.botanik.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.botanik.app.ui.theme.*

/**
 * Pantalla de perfil del usuario.
 *
 * Qué:    muestra los datos de la cuenta y permite editar nombre, foto y
 *         la preferencia de notificaciones.
 * Cómo:   el estado vive en [ProfileViewModel] (state hoisting) y esta
 *         pantalla solo dibuja y emite intención de usuario.
 */
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Photo Picker nativo para elegir la foto de perfil.
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onErrorShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileAvatar(
                photoUrl = uiState.photoUrl,
                name = uiState.userName,
                isSaving = uiState.isSaving,
                onPickPhoto = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemovePhoto = viewModel::onRemovePhoto
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Nombre ────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nombre",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.userName ?: "Explorador Botani-k",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        uiState.userEmail?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(onClick = viewModel::onEditNameClick, enabled = !uiState.isSaving) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar nombre",
                            tint = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Notificaciones ─────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.notificationsEnabled) {
                                "Recibir avisos sobre tus plantas"
                            } else {
                                "Notificaciones desactivadas"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::onNotificationsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.signOut(onLogout) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Diálogo de edición de nombre ─────────────────────────────
    if (uiState.showEditNameDialog) {
        EditNameDialog(
            currentName = uiState.nameDraft,
            isSaving = uiState.isSaving,
            onNameChange = viewModel::onNameDraftChange,
            onConfirm = viewModel::onSaveName,
            onDismiss = viewModel::onDismissEditName
        )
    }
}

/** Avatar circular con la foto de perfil o las iniciales del usuario. */
@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    name: String?,
    isSaving: Boolean,
    onPickPhoto: (String) -> Unit,
    onRemovePhoto: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = name?.trim()?.firstOrNull()?.uppercase() ?: "B",
                    style = MaterialTheme.typography.displaySmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botón para elegir/eliminar la foto.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                IconButton(
                    onClick = onRemovePhoto,
                    enabled = !isSaving,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Quitar foto",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Diálogo para editar el nombre del usuario. */
@Composable
private fun EditNameDialog(
    currentName: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var localName by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = "Editar nombre",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Así te verá tu equipo en Botani-k",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = localName,
                    onValueChange = { localName = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onNameChange(localName)
                    onConfirm()
                },
                enabled = !isSaving && localName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                } else {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar", color = PrimaryGreen)
            }
        }
    )
}

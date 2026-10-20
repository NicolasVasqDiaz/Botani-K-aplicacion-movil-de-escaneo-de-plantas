package com.botanik.app.ui.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Composable sin UI propia: lanza el Photo Picker nativo de Android.
 * Al seleccionar una imagen, devuelve la URI vía [onPhotoPicked].
 * Si el usuario cancela, devuelve null.
 */
@Composable
fun GalleryPicker(
    onPhotoPicked: (Uri?) -> Unit,
    onCancel: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPhotoPicked(uri)
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

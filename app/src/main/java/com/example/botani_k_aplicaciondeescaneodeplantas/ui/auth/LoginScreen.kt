package com.example.botani_k_aplicaciondeescaneodeplantas.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.botani_k_aplicaciondeescaneodeplantas.R
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID_PLACEHOLDER = "TU_WEB_CLIENT_ID_DE_FIREBASE"

@Composable
fun LoginScreen(onAuthenticated: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun launchGoogleSignIn() {
        val serverClientId = context.getString(R.string.default_web_client_id)
        if (serverClientId == WEB_CLIENT_ID_PLACEHOLDER) {
            viewModel.onExternalError(
                "Falta configurar el ID de cliente web de Google en Firebase. " +
                    "Reemplaza default_web_client_id en strings.xml."
            )
            return
        }
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                viewModel.signInWithGoogle(googleIdTokenCredential.idToken, onAuthenticated)
            } catch (e: GetCredentialException) {
                viewModel.onExternalError(
                    "No se pudo obtener la cuenta de Google. Revisa tu conexión e inténtalo de nuevo."
                )
            } catch (e: GoogleIdTokenParsingException) {
                viewModel.onExternalError("Respuesta de Google no válida. Inténtalo de nuevo.")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(96.dp).background(SurfaceWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = "Bot, el asistente de Botani-k",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Bienvenido a Botani-k", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Text(text = "Tu amigo jardinero científico", style = MaterialTheme.typography.titleMedium, color = PrimaryGreen)

            Spacer(modifier = Modifier.height(32.dp))

            uiState.authError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(text = error, color = ErrorRed, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            OutlinedButton(
                onClick = { launchGoogleSignIn() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading,
                border = BorderStroke(1.dp, PrimaryGreen)
            ) {
                Text("Continuar con Google", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "O BIEN",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = { viewModel.signInAsGuest(onAuthenticated) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Explore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar como Invitado / Explorador escolar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ideal para salidas de campo y valles sin cobertura",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary.copy(alpha = 0.6f)
            )
        }
    }
}
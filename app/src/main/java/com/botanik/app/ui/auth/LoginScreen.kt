package com.botanik.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.botanik.app.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID = "143007902625-m1m35re0nkcpd8rvpbmrmea8er1inotb.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    suspend fun signInWithGoogle() {
        try {
            // Se utiliza GetSignInWithGoogleOption para botones explícitos de "Continuar con Google",
            // ya que despliega el selector/login de Google en el dispositivo.
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = WEB_CLIENT_ID
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = try {
                credentialManager.getCredential(context, request)
            } catch (e: NoCredentialException) {
                android.util.Log.i("BotaniKAuth", "NoCredentialException con GetSignInWithGoogleOption, intentando GetGoogleIdOption", e)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                credentialManager.getCredential(context, fallbackRequest)
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            viewModel.onSignInClick(googleIdTokenCredential.idToken)
        } catch (e: GetCredentialCancellationException) {
            android.util.Log.i("BotaniKAuth", "Inicio de sesión cancelado por el usuario")
            snackbarHostState.showSnackbar("Inicio de sesión cancelado")
        } catch (e: NoCredentialException) {
            android.util.Log.w("BotaniKAuth", "Sin credenciales de Google disponibles en el dispositivo", e)
            snackbarHostState.showSnackbar(
                "No hay cuentas de Google disponibles en este dispositivo. " +
                    "Agrega una cuenta en Ajustes → Cuentas o ingresa como Invitado."
            )
        } catch (e: GetCredentialException) {
            android.util.Log.e("BotaniKAuth", "Fallo Credential Manager: ${e.message}", e)
            snackbarHostState.showSnackbar("No pudimos conectar con Google. Revisa tu conexión e inténtalo de nuevo.")
        } catch (e: Exception) {
            android.util.Log.e("BotaniKAuth", "Error en inicio de sesión con Google: ${e.message}", e)
            snackbarHostState.showSnackbar("Ocurrió un error al iniciar sesión. Inténtalo de nuevo.")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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

                Text(
                    text = "Bienvenido a Botani-k",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Tu amigo jardinero científico",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { scope.launch { signInWithGoogle() } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading,
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Continuar con Google", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "O BIEN",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Button(
                    onClick = { onNavigateToRegister() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear cuenta", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { onNavigateToSignIn() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Ya tengo cuenta: iniciar sesión",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

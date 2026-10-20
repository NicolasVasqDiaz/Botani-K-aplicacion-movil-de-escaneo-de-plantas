package com.botanik.app.data.repository

import com.botanik.app.data.local.LocalSessionManager
import com.botanik.app.data.local.LocalUserDao
import com.botanik.app.data.local.LocalUserEntity
import com.botanik.app.data.remote.FirebaseAuthDataSource
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

/**
 * Implementación de [AuthRepository] delegando a [FirebaseAuthDataSource].
 * Traduce excepciones de Firebase a [AuthState.Error] con mensajes en español.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val localUserDao: LocalUserDao,
    private val localSessionManager: LocalSessionManager
) : AuthRepository {

    /**
     * Estado de autenticación unificado: combina la sesión de Firebase
     * (Google o anónima) con la sesión local (cuenta creada en la app).
     *
     * Qué:    una sola fuente de verdad para saber quién está conectado.
     * Cómo:   `combine` fusiona ambos flujos. Prioridad: cuenta local > Google
     *         > sesión anónima sin cuenta local. Para las cuentas locales se
     *         usa el UID anónimo de Firebase como `userId`, que es lo que
     *         permite operar sobre `users/{uid}` en Firestore.
     * Por qué: antes cada ViewModel consultaba un flujo distinto y se
     *         pisaban entre sí (condición de carrera) cuando ambos
     *         proveedores estaban activos.
     */
    override val authState: Flow<AuthState> = combine(
        authDataSource.authStateFlow(),
        localSessionManager.currentUserId,
        localSessionManager.currentFirebaseUid
    ) { firebaseUser, localUserId, localFirebaseUid ->
        when {
            // Sesión local: la cuenta creada dentro de la app.
            localUserId != null -> {
                val localUser = localUserDao.findById(localUserId)
                AuthState.Authenticated(
                    userId = localFirebaseUid ?: firebaseUser?.uid ?: localUserId,
                    email = localUser?.email,
                    displayName = localUser?.name ?: "Explorador Botani-k",
                    photoUrl = null
                )
            }

            // Google (usuario real de Firebase, no anónimo).
            firebaseUser != null && !firebaseUser.isAnonymous -> AuthState.Authenticated(
                userId = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName ?: "Usuario Botani-k",
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            // Sesión anónima de Firebase sin cuenta local asociada: la
            // cuenta local ya cerró sesión, así que el usuario está fuera.
            else -> AuthState.Unauthenticated
        }
    }

    /**
     * Inicia sesión con Google.
     *
     * Qué:    autentica al usuario con su cuenta de Google.
     * Cómo:   cierra primero la sesión local y cualquier sesión anónima
     *         previa de Firebase antes de aplicar la credencial de Google.
     * Por qué: si quedara una sesión local activa, el estado unificado
     *         seguiría mostrando la cuenta local en lugar de la de Google.
     *         Y si quedara una sesión anónima, Firebase podría vincular
     *         ambas identidades y el usuario de Google terminaría viendo
     *         los datos de la cuenta local.
     */
    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localSessionManager.clearSession()
            val current = authDataSource.currentUser()
            if (current != null && current.isAnonymous) {
                authDataSource.signOut()
            }
            authDataSource.signInWithGoogle(idToken).fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { e -> Result.failure(Exception(mapAuthError(e), e)) }
            )
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthError(e), e))
        }
    }

    override suspend fun signInAsGuest(): Result<Unit> = withContext(Dispatchers.IO) {
        authDataSource.signInAsGuest().fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e -> Result.failure(Exception(mapAuthError(e), e)) }
        )
    }

    /**
     * Cierra la sesión del usuario.
     *
     * Qué:    invalida la sesión local (Room + DataStore) y la de Firebase.
     * Cómo:   si el usuario es una cuenta local, se conserva la sesión
     *         anónima de Firebase para que el UID no cambie; si es una
     *         cuenta de Google, se cierra Firebase por completo.
     * Por qué: el UID anónimo es la clave bajo la que la cuenta local guarda
     *         su jardín en `users/{uid}`. Si se destruyera al cerrar sesión,
     *         al volver a entrar se generaría un UID nuevo y el jardín
     *         sincronizado en Firestore quedaría huérfano.
     */
    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentFirebaseUser = authDataSource.currentUser()
        val isLocalAccount = localSessionManager.getCurrentUserId() != null

        localSessionManager.clearSession()

        if (isLocalAccount && currentFirebaseUser != null && currentFirebaseUser.isAnonymous) {
            // Se mantiene la sesión anónima: el UID debe permanecer estable.
            return@withContext Result.success(Unit)
        }

        authDataSource.signOut().fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e -> Result.failure(Exception("No pudimos cerrar sesión. Intenta de nuevo", e)) }
        )
    }

    override suspend fun registerLocalUser(name: String, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            if (localUserDao.countByEmail(normalizedEmail) > 0) {
                return@withContext Result.failure(Exception("Ya existe una cuenta con este correo"))
            }
            val salt = generateSalt()
            val passwordHash = hashPassword(password, salt)
            val user = LocalUserEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                email = normalizedEmail,
                passwordHash = passwordHash,
                salt = salt,
                createdAt = System.currentTimeMillis()
            )
            localUserDao.insert(user)
            // Sesión anónima de Firebase: habilita a la cuenta local para
            // leer el catálogo y escribir su jardín en `users/{uid}`.
            val firebaseUid = ensureAnonymousSession()
            localSessionManager.saveSession(user.id, firebaseUid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos crear tu cuenta. Intenta de nuevo"))
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = localUserDao.findByEmail(email.trim().lowercase())
                ?: return@withContext Result.failure(Exception("No encontramos una cuenta con este correo"))
            val passwordHash = hashPassword(password, user.salt)
            if (passwordHash != user.passwordHash) {
                return@withContext Result.failure(Exception("Contraseña incorrecta"))
            }
            val firebaseUid = ensureAnonymousSession()
            localSessionManager.saveSession(user.id, firebaseUid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos iniciar sesión. Intenta de nuevo"))
        }
    }

    /**
     * Garantiza una sesión de Firebase activa y devuelve su UID.
     *
     * Si ya hay una sesión anónimo se reutiliza su UID: debe permanecer
     * estable, porque es la clave bajo la que la cuenta local guarda su
     * jardín en `users/{uid}`. Si la sesión activa es de Google, se cierra
     * primero para que la cuenta local no termine escribiendo sobre los
     * datos del usuario de Google.
     */
    private suspend fun ensureAnonymousSession(): String? {
        val current = authDataSource.currentUser()
        if (current != null && current.isAnonymous) return current.uid
        if (current != null) {
            // Sesión de Google activa: se cierra para no mezclar identidades.
            authDataSource.signOut()
        }
        return authDataSource.signInAsGuest().getOrNull()?.uid
    }

    override suspend fun getLocalUser(): LocalUserEntity? = withContext(Dispatchers.IO) {
        val userId = localSessionManager.getCurrentUserId() ?: return@withContext null
        localUserDao.findById(userId)
    }

    override suspend fun signOutLocal(): Result<Unit> = withContext(Dispatchers.IO) {
        localSessionManager.clearSession()
        Result.success(Unit)
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest((salt + password).toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /** Mapea excepciones de Firebase Auth a mensajes legibles en español. */
    private fun mapAuthError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Credenciales inválidas"
        is FirebaseNetworkException -> "Verifica tu conexión a internet"
        is FirebaseAuthInvalidUserException -> "Esta cuenta ha sido deshabilitada"
        else -> {
            val code = e.message ?: ""
            when {
                code.contains("SIGN_IN_CANCELLED", ignoreCase = true) -> "Inicio de sesión cancelado"
                code.contains("NETWORK", ignoreCase = true) -> "Verifica tu conexión a internet"
                code.contains("not allowed", ignoreCase = true) ||
                    code.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                    "El proveedor de Google no está habilitado en Firebase Console (Authentication → Sign-in method)"
                else -> "No pudimos iniciar sesión. Intenta de nuevo"
            }
        }
    }
}

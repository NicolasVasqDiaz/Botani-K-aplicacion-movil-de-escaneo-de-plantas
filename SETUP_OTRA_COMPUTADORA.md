# Setup en otra computadora — Botani-k

Guía para quien clone el repositorio y necesite que la app funcione (en
especial el **inicio de sesión con Google**).

> **El error típico:** al tocar "Continuar con Google" aparece el diálogo de
> Google Play Services que dice **"La sesión se cerró"** y no deja entrar.
>
> **La causa NO es el `serviceAccountKey.json`** (ese archivo solo lo usan los
> scripts de seed de Node, la app Android nunca lo lee).
>
> **La causa real:** cada computadora genera su propio `debug.keystore`, y por
> lo tanto su propio **SHA-1**. Google Play Services valida la firma de la
> app contra los SHA-1 registrados en Firebase. Si el de la máquina nueva no
> está registrado → el login falla.

---

## ⏱️ Resumen: 3 pasos, ~5 minutos

```
1. Sacar el SHA-1 de la maquina nueva
2. Registrarlo en Firebase Console
3. Esperar 5-10 min y compilar/instalar
```

---

## Paso 1 — Sacar el SHA-1

Desde la raíz del proyecto clonado:

```bash
node check-setup.js
```

O manualmente:

```bash
./gradlew signingReport
```

Busca el bloque `Variant: debug` y copia el valor `SHA1:`.

> En Windows: `gradlew.bat signingReport`

**Ejemplo:**
```
> Variant: debug
  SHA1: A6:82:9D:FA:0A:62:D2:FD:CE:5E:6F:D3:D2:FE:82:B6:33:FC:BC:B1
```

---

## Paso 2 — Registrarlo en Firebase Console

1. Abre [Firebase Console](https://console.firebase.google.com/) → proyecto **botani-k**
2. ⚙️ **Project settings** (icono de engranaje) → **Your apps**
3. Selecciona la app Android con package **`com.botanik.app`**
4. Pestaña **General** → botón **Add fingerprint**
5. Pega el SHA-1 del paso 1 → **Save**

> ⏱️ **Espera 5-10 minutos.** Firebase actualiza el cliente OAuth del proyecto
> y Google Play Services necesita refrescar su caché.

### Si sigue fallando

El cliente OAuth de Android se configura también en Google Cloud Console:

1. [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials)
   → selecciona el proyecto **botani-k**
2. En **OAuth 2.0 Client IDs** busca el cliente de tipo **Android**
   (el que empieza con `143007902625-` y termina en `apps.googleusercontent.com`)
3. **Add fingerprint** → pega el mismo SHA-1 → **Save**

---

## Paso 3 — Compilar e instalar

```bash
./gradlew clean assembleDebug
```

Instala el APK y prueba el login con Google.

> Si cambiaste el `debug.keystore` (ver "Opción nuclear" más abajo), el
> `clean` es **obligatorio**: el SHA-1 cambia la firma del APK.

---

## 🧩 El resto del setup (para que nada más falle)

### 1. `local.properties`

Android Studio lo genera solo al abrir el proyecto. Si lo creas a mano:

```properties
sdk.dir=C\:\\ruta\\a\\tu\\Android\\Sdk
```

> Los valores de Cloudinary **no** van aquí: ya vienen en `gradle.properties`
> (que sí se versiona). El build nunca falla por esto.

### 2. `google-services.json`

**Ya viene en el repo** (`app/google-services.json`). No hay que hacer nada.
No es un secreto: solo contiene la configuración del cliente.

### 3. Dependencias de Node (solo si vas a correr los seeds)

```bash
cd seed
npm install
```

Y la credencial de la Service Account → ver `seed/README.md`.
**No es necesaria para que la app funcione**; solo para repoblar Firestore.

---

## ⚡ Opción nuclear: que TODAS las máquinas usen el mismo keystore

Si quieres que el login funcione en cualquier compu **sin registrar nada nuevo**
en Firebase, copia el `debug.keystore` de una máquina a la otra.

**Windows:**
```cmd
copy /Y "C:\ruta\de\otra\maquina\.android\debug.keystore" "%USERPROFILE%\.android\debug.keystore"
```

**Linux / macOS:**
```bash
cp /ruta/de/otra/maquina/.android/debug.keystore ~/.android/debug.keystore
```

Luego **recompila con `clean`** (el APK debe cambiar de firma).

> ⚠️ Solo funciona si las dos máquinas compilan el mismo keystore. Si una usa
> release y otra debug, no aplica.

---

## 🐛 Diagnóstico rápido

Si algo falla, mira el logcat:

```bash
adb logcat | grep -iE "AuthApi|ApiException|DEVELOPER_ERROR|credential"
```

| Error | Causa | Solución |
|---|---|---|
| `ApiException: 10` + `DEVELOPER_ERROR` | **SHA-1 no registrado** | Paso 2 |
| "La sesión se cerró" (diálogo de Play Services) | **SHA-1 no registrado** | Paso 2 |
| `No hay cuentas de Google disponibles` | El dispositivo no tiene una cuenta Google | Ajustes → Cuentas → Agregar → Google |
| `Inicio de sesión cancelado` | El usuario cerró el diálogo | Normal, no es bug |
| `PERMISSION_DENIED` en Firestore | Reglas de Firestore | Revisar `README.md` |

---

## 📋 Checklist para presentar en una máquina nueva

```
□ Clonar el repo
□ Abrir en Android Studio (genera local.properties)
□ node check-setup.js  →  obtener el SHA-1
□ Registrar el SHA-1 en Firebase Console
□ Esperar 10 minutos
□ ./gradlew clean assembleDebug
□ Instalar y probar: login con Google → catálogo → guardar planta
□ (Opcional) node seed/… para repoblar el catálogo
```

# Scripts de seed — Botani-k

Estos scripts pueblan la colección `catalog` de Firestore con las plantas
endémicas y nativas de Cajamarca.

---

## ⚠️ Antes de nada: la credencial es un SECRETO

Los scripts necesitan una **Service Account key** de Firebase para escribir en
Firestore. Ese archivo contiene la **private key** de la cuenta de servicio y
da **acceso total a tu proyecto**.

> **NUNCA subas ese archivo a GitHub ni a ningún repositorio.**
> Si se filtra, cualquiera puede leer y modificar tu base de datos.

Por eso el `.gitignore` del proyecto ya ignora todos los nombres posibles de
esa credencial dentro de `seed/`.

---

## 1️⃣ Obtener tu credencial

1. Abre [Firebase Console](https://console.firebase.google.com/) → proyecto **botani-k**
2. ⚙️ **Project settings** → **Service accounts**
3. Pestaña **Firebase** → botón **Generate new private key**
4. Se descarga un `.json` (tipo `botani-k-xxxxx-1234abcd.json`)

> Cada miembro del equipo genera la suya. No se comparte por WhatsApp/ Drive:
> se comparte el **nombre del proyecto**, no la key.

---

## 2️⃣ Colocarla en esta carpeta

Copia el archivo descargado aquí (`seed/`) con cualquiera de estos nombres:

| Nombre aceptado | Estado |
|---|---|
| `botani-firebase.json` | ✅ ignorado por git |
| `serviceAccountKey.json` | ✅ ignorado por git |
| `.firebase-credentials.json` | ✅ ignorado por git |

El script busca los tres automáticamente.

### 🔒 Opción más segura: dejarla FUERA del repositorio

Si prefieres que el archivo ni siquiera esté en la carpeta del proyecto,
apúntalo con la variable de entorno `GOOGLE_APPLICATION_CREDENTIALS`:

**PowerShell**
```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\ruta\a\tu\botani-firebase.json"
node seed.js
```

**CMD**
```cmd
set GOOGLE_APPLICATION_CREDENTIALS=C:\ruta\a\tu\botani-firebase.json
node seed.js
```

**Linux / macOS**
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/ruta/a/tu/botani-firebase.json
node seed.js
```

Esto tiene **prioridad** sobre los archivos locales.

---

## 3️⃣ Instalar dependencias y ejecutar

```bash
cd seed
npm install
```

| Comando | Qué hace |
|---|---|
| `npm run seed` | Carga las 20 plantas principales |
| `npm run seed:extra` | Carga las 20 plantas adicionales (flora peruana) |
| `node seed.js` | Equivalente a `npm run seed` |

### ✅ Re-ejecutar es seguro

Ambos scripts usan `merge: true` y **omiten los campos vacíos**, así que:

- **NO** pisan los `imageUrl` que hayas subido manualmente a Firestore
- **NO** borran las plantas que ya estaban

Puedes correrlos las veces que quieras.

---

## 📋 Los dos catálogos

| Script | Plantas | Filtrosspeciales |
|---|---|---|
| `seed.js` | 20 (Chuquiragua, Amancaes, Cantuta, Quinua, Tarwi, Matico...) | `Scrophulariaceae`, `Rosaceae` (Árboles) · `Adoxaceae` (Medicinales) |
| `seed_cajamarca_extra.js` | 20 (Senecio de Cajamarca, Pacra-pacra, Canamuela, Cascapilla...) | 16 de 20 son endémicas de Cajamarca |

**Total: 40 plantas** en la colección `catalog`.

---

## ❓Si aparece este error

```
No se encontro la credencial de la Service Account de Firebase.
```

Significa que el script no encontró el archivo. Revisa:

1. ¿Copiaste el JSON dentro de la carpeta `seed/`?
2. ¿El nombre es uno de los tres aceptados?
3. ¿Definiste `GOOGLE_APPLICATION_CREDENTIALS` apuntando a un archivo que existe?

---

## 🔐 `app/google-services.json` sí se sube al repo

Para que no te confundas: ese archivo **no** es un secreto.

| Archivo | ¿Secreto? | ¿Se sube? |
|---|---|---|
| `app/google-services.json` | ❌ No | ✅ Sí |
| `seed/*firebase*.json` | ✅ **Sí** | ❌ **Nunca** |

`google-services.json` solo contiene la configuración del **cliente** (API key
restringida por package name + SHA-1 del certificado). La clave privada
**nunca** está ahí — por eso la app funciona sin conexión a internet.

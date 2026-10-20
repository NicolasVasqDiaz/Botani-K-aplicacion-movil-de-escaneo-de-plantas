# Evidencia End-to-End — Botani-k

**Fecha:** 2026-10-20
**Probador:** ______________________
**Dispositivo/Emulador:** ______________________
**Versión del APK:** 1.0 (versionCode 1)

---

## A. Checklist del flujo principal (14 puntos)

| # | Paso | Esperado | Resultado |
|---|---|---|---|
| 1 | Abrir la app (primera vez) | Splash → Onboarding (3 pantallas) | [PASS/FAIL] |
| 2 | Onboarding → "Comenzar" | Pantalla de Login | [PASS/FAIL] |
| 3 | Login con Google | Sesión activa → Escanea (Home) | [PASS/FAIL] |
| 4 | Tap "Escanear Planta" | Bottom sheet con 2 opciones (Cámara / Galería) | [PASS/FAIL] |
| 5 | Elegir "Tomar foto" | Abre cámara, pide permiso | [PASS/FAIL] |
| 6 | Tomar foto | Sube a Cloudinary → navega al Catálogo | [PASS/FAIL] |
| 7 | Verificar subida | Logcat muestra `https://res.cloudinary.com/kroz25nj/...` | [PASS/FAIL] |
| 8 | Catálogo | Lista de plantas + contador "40 plantas encontradas" | [PASS/FAIL] |
| 9 | Elegir una planta | Ficha con nombre, familia, región, floración, cuidados | [PASS/FAIL] |
| 10 | Tap "Guardar en Mi Jardín" | Botón cambia a "✓ Guardada" | [PASS/FAIL] |
| 11 | Ver "Mi Jardín" | Grid con planta + foto + fecha ("Guardada el 20 oct") | [PASS/FAIL] |
| 12 | Cerrar y reabrir la app | Sesión persiste → Home | [PASS/FAIL] |
| 13 | Eliminar planta | Confirmación → desaparece del grid al instante | [PASS/FAIL] |
| 14 | Cerrar sesión | Vuelve a Login | [PASS/FAIL] |

---

## B. Cuenta local (sin Google) — RU-02b

| # | Paso | Esperado | Resultado |
|---|---|---|---|
| 15 | Login → "Crear cuenta" | Formulario: nombre, correo, contraseña, confirmar | [PASS/FAIL] |
| 16 | Registrar con datos válidos | Entra a la app; sesión persiste en DataStore | [PASS/FAIL] |
| 17 | Contraseñas que no coinciden | Mensaje de error, no registra | [PASS/FAIL] |
| 18 | Correo duplicado | Mensaje "Ya existe una cuenta con este correo" | [PASS/FAIL] |
| 19 | Cerrar sesión → "Ya tengo cuenta: iniciar sesión" | Abre formulario de email + contraseña | [PASS/FAIL] |
| 20 | Iniciar sesión con la cuenta creada | Entra a la app con el mismo nombre | [PASS/FAIL] |

---

## C. Modo offline — RU-07

| # | Paso | Esperado | Resultado |
|---|---|---|---|
| 21 | Modo avión → "Mi Jardín" | Lista las plantas desde Room | [PASS/FAIL] |
| 22 | Modo avión → tocar una planta guardada | Ficha completa (datos, cuidados, curiosidades) | [PASS/FAIL] |
| 23 | Modo avión → ver la banda gris | "Sin conexión. Mostrando la copia guardada en tu dispositivo" | [PASS/FAIL] |
| 24 | Modo avión → Catálogo | 40 plantas desde la caché local | [PASS/FAIL] |
| 25 | Modo avión → buscar en el catálogo | Filtra sobre la copia local | [PASS/FAIL] |
| 26 | Restaurar red | La app sigue funcionando y sincroniza | [PASS/FAIL] |

---

## D. Editar perfil — RU-08 / RU-09 / RU-10

| # | Paso | Esperado | Resultado |
|---|---|---|---|
| 27 | Perfil → lápiz (nombre) | Diálogo con el nombre actual | [PASS/FAIL] |
| 28 | Escribir nuevo nombre y guardar | Se actualiza en pantalla + en Firestore | [PASS/FAIL] |
| 29 | Perfil → cámara (foto) | Abre Photo Picker | [PASS/FAIL] |
| 30 | Elegir imagen | Sube a Cloudinary y se ve como avatar | [PASS/FAIL] |
| 31 | Perfil → eliminar foto | Vuelve a la inicial del nombre | [PASS/FAIL] |
| 32 | Perfil → switch de notificaciones | Se apaga/enciende y persiste en Firestore | [PASS/FAIL] |

---

## E. Verificación de Firestore (consola)

- [ ] `catalog` tiene **40 documentos** (20 originales + 20 de `seed_cajamarca_extra.js`)
- [ ] `catalog` — cada documento tiene: commonName, scientificName, family, region, floweringSeason, care, funFacts, imageUrl, isEndemic
- [ ] `users` tiene al menos 1 documento por usuario probado
- [ ] `users/{uid}` tiene: uid, displayName, email, photoUrl, notificationsEnabled, createdAt
- [ ] `users/{uid}/garden` tiene al menos 1 documento con `userPhotoUrl` que empieza con `https://res.cloudinary.com/kroz25nj/`
- [ ] El `displayName` refleja el nombre editado desde el perfil

## F. Verificación de Room

```bash
adb shell "run-as com.botanik.app ls /data/data/com.botanik.app/databases/"
```

Esperado: `botanik_db` (+ journal). Dentro: tablas `garden_plants`, `local_users`, `catalog_plants`.

Resultado: ______________________

```bash
# Verificar que el catálogo quedó cacheado (40 filas)
adb shell "run-as com.botanik.app sqlite3 /data/data/com.botanik.app/databases/botanik_db 'SELECT COUNT(*) FROM catalog_plants;'"
```

Esperado: `40`

Resultado: ______________________

## G. Notas

- Si algún punto falla, documentar con el **mensaje de error exacto de Logcat**.
- Los resultados PASS/FAIL deben ser llenados por el probador.
- Puntos de riesgo verificados en emulador: 8, 11, 21-25, 28, 32.

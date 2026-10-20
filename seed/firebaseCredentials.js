/**
 * Resolución de la credencial de la Service Account de Firebase.
 *
 * La Service Account contiene la private key y da acceso total al proyecto.
 * NUNCA debe subirse al repositorio. Este módulo la busca en este orden:
 *
 *   1. Variable de entorno GOOGLE_APPLICATION_CREDENTIALS (o FIREBASE_SERVICE_ACCOUNT)
 *      → recomendado: permite dejar el archivo FUERA del repositorio.
 *   2. Archivos locales conocidos dentro de seed/ (todos ignorados por git):
 *      - botani-firebase.json
 *      - serviceAccountKey.json
 *      - .firebase-credentials.json
 *
 * Si no encuentra ninguna, lanza un error con instrucciones claras en vez de
 * un "Cannot find module" críptico.
 */

const fs = require('fs');
const path = require('path');

const LOCAL_CANDIDATES = [
  'botani-firebase.json',
  'serviceAccountKey.json',
  '.firebase-credentials.json',
];

/**
 * Devuelve la ruta absoluta del archivo de credenciales, o `null` si no existe.
 */
function resolveCredentialsPath() {
  // 1) Variable de entorno (prioridad máxima).
  const fromEnv =
    process.env.GOOGLE_APPLICATION_CREDENTIALS || process.env.FIREBASE_SERVICE_ACCOUNT;

  if (fromEnv) {
    const resolved = path.resolve(process.cwd(), fromEnv);
    if (fs.existsSync(resolved)) return resolved;
    throw new Error(
      `La variable de entorno apunta a un archivo que no existe:\n  ${resolved}`
    );
  }

  // 2) Archivos locales dentro de seed/ (ignorados por git).
  for (const file of LOCAL_CANDIDATES) {
    const candidate = path.join(__dirname, file);
    if (fs.existsSync(candidate)) return candidate;
  }

  return null;
}

/**
 * Carga la credencial de la Service Account.
 * @returns {object} El contenido del JSON de credenciales.
 */
function loadServiceAccount() {
  const credentialsPath = resolveCredentialsPath();

  if (!credentialsPath) {
    throw new Error(
      [
        '',
        '  No se encontro la credencial de la Service Account de Firebase.',
        '',
        '  Como obtenerla:',
        '    1. Firebase Console → Project settings → Service accounts',
        '    2. "Generate new private key" → descarga el .json',
        '    3. Copiala en la carpeta seed/ con alguno de estos nombres:',
        `         ${LOCAL_CANDIDATES.join('\n         ')}`,
        '    (los tres estan ignorados por git: no se suben al repositorio)',
        '',
        '  Alternativa (la mas segura): dejar el archivo FUERA del repo y',
        '  apontar la variable de entorno:',
        '',
        '    set GOOGLE_APPLICATION_CREDENTIALS=C:\\ruta\\a\\tu\\key.json',
        '',
        '  Detalle: seed/README.md',
        '',
      ].join('\n')
    );
  }

  try {
    return JSON.parse(fs.readFileSync(credentialsPath, 'utf8'));
  } catch (error) {
    throw new Error(
      `No se pudo leer la credencial (${credentialsPath}):\n  ${error.message}`
    );
  }
}

/**
 * Inicializa Firebase Admin con la credencial resuelta.
 * @returns {import('firebase-admin/app').App} La app de Firebase inicializada.
 */
function initializeFirebaseApp() {
  // Se carga de forma perezosa para no requerir firebase-admin al importar.
  const { initializeApp, cert } = require('firebase-admin/app');
  return initializeApp({ credential: cert(loadServiceAccount()) });
}

module.exports = {
  resolveCredentialsPath,
  loadServiceAccount,
  initializeFirebaseApp,
};

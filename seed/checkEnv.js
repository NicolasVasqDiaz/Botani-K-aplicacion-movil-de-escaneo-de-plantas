/**
 * Verificación del entorno de los scripts de seed.
 *
 * Comprueba:
 *   1. Que las dependencias de Node estén instaladas.
 *   2. Que exista la credencial de la Service Account (y de dónde se tomó).
 *   3. Que la credencial sea válida contra el proyecto de Firebase.
 *   4. Cuántas plantas hay en el catálogo y cuáles son.
 *
 * Uso: npm run check
 */

const { resolveCredentialsPath, loadServiceAccount } = require('./firebaseCredentials');

const OK = '[OK]';
const FAIL = '[!!]';
const INFO = '[--]';

function line(mark, text) {
  console.log(`${mark} ${text}`);
}

async function main() {
  console.log('\n=== Verificacion del entorno de seed - Botani-k ===\n');

  // 1) Dependencias
  try {
    require('firebase-admin/app');
    line(OK, 'Dependencias instaladas (firebase-admin).');
  } catch (e) {
    line(FAIL, 'Falta firebase-admin. Ejecuta: npm install');
    process.exit(1);
  }

  // 2) Credencial
  let credentialsPath;
  try {
    credentialsPath = resolveCredentialsPath();
  } catch (e) {
    line(FAIL, e.message);
    process.exit(1);
  }

  if (!credentialsPath) {
    line(FAIL, 'No se encontro la credencial de la Service Account.');
    console.log('\n  Ver seed/README.md para obtenerla.\n');
    process.exit(1);
  }

  const fromEnv = !!(
    process.env.GOOGLE_APPLICATION_CREDENTIALS ||
    process.env.FIREBASE_SERVICE_ACCOUNT
  );
  line(
    OK,
    `Credencial encontrada: ${credentialsPath}` +
      (fromEnv ? '  (via variable de entorno)' : '  (archivo local, ignorado por git)')
  );

  // 3) Validez contra Firebase
  let db;
  try {
    const { getFirestore } = require('firebase-admin/firestore');
    const { initializeFirebaseApp } = require('./firebaseCredentials');
    db = getFirestore(initializeFirebaseApp());
    await db.collection('catalog').limit(1).get();
    line(OK, 'Credencial valida: se pudo leer la coleccion "catalog".');
  } catch (e) {
    line(FAIL, `La credencial no pudo autenticarse: ${e.message}`);
    process.exit(1);
  }

  // 4) Estado del catálogo
  const snapshot = await db.collection('catalog').get();
  line(INFO, `El catalogo tiene ${snapshot.size} documentos.`);

  if (snapshot.size === 0) {
    line(FAIL, 'El catalogo esta vacio. Ejecuta: npm run seed && npm run seed:extra');
    process.exit(1);
  }

  const names = snapshot.docs
    .map((d) => d.data().commonName)
    .filter(Boolean)
    .sort();
  line(INFO, `Primeras 5: ${names.slice(0, 5).join(', ')}`);

  const withImage = snapshot.docs.filter((d) => (d.data().imageUrl || '').length > 0).length;
  line(INFO, `Con imageUrl: ${withImage} de ${snapshot.size}.`);

  console.log('\n=== Todo listo. Puedes correr los seeds. ===\n');
}

main().catch((e) => {
  console.error(`\n${FAIL} ${e.message}\n`);
  process.exit(1);
});

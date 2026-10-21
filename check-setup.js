#!/usr/bin/env node
/**
 * Verificación del setup de Botani-k en una máquina nueva.
 *
 * Comprueba lo que suele fallar al clonar el repo en otra computadora:
 *   1. Que exista local.properties (o al menos el SDK path).
 *   2. Que exista app/google-services.json.
 *   3. Que el proyecto compile.
 *   4. Muestra el SHA-1 del debug keystore, que hay que registrar en
 *      Firebase Console para que el login con Google funcione.
 *
 * Uso:  node check-setup.js
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const ROOT = __dirname;
const IS_WINDOWS = process.platform === 'win32';
const GRADLEW = IS_WINDOWS ? 'gradlew.bat' : './gradlew';

const OK = '[OK]  ';
const FAIL = '[!!]  ';
const WARN = '[--]  ';

function head(text) {
  console.log('\n=== ' + text + ' ===\n');
}

/** Devuelve el SHA-1 de la variante debug, o null si no se pudo obtener. */
function getDebugSha1() {
  try {
    const output = execSync(`${GRADLEW} signingReport --console=plain`, {
      cwd: ROOT,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
      maxBuffer: 1024 * 1024 * 16,
    });

    // Formato de Gradle:
    //   > Variant: debug
    //     SHA1: XX:XX:...
    const lines = output.split(/\r?\n/);
    for (let i = 0; i < lines.length; i++) {
      if (/Variant: debug\b/.test(lines[i])) {
        for (let j = i; j < Math.min(i + 8, lines.length); j++) {
          const match = lines[j].match(/SHA1:\s*([0-9A-Fa-f:]{40,})/);
          if (match) return match[1];
        }
      }
    }
  } catch (e) {
    // signingReport puede fallar si no hay SDK configurado; se reporta abajo.
  }
  return null;
}

function checkLocalProperties() {
  const file = path.join(ROOT, 'local.properties');
  if (!fs.existsSync(file)) {
    console.log(`${FAIL}local.properties NO existe.`);
    console.log('      Android Studio lo genera al abrir el proyecto.');
    console.log('      Si hay que crearlo a mano, debe tener: sdk.dir=<ruta al SDK>');
    return false;
  }
  const content = fs.readFileSync(file, 'utf8');
  if (!/sdk\.dir\s*=/.test(content)) {
    console.log(`${WARN}local.properties existe pero NO tiene sdk.dir.`);
    return false;
  }
  console.log(`${OK}local.properties OK (con sdk.dir).`);
  return true;
}

function checkGoogleServices() {
  const file = path.join(ROOT, 'app', 'google-services.json');
  if (!fs.existsSync(file)) {
    console.log(`${FAIL}app/google-services.json NO existe.`);
    console.log('      Descárgalo de Firebase Console → Project settings → Your apps.');
    return false;
  }
  try {
    const json = JSON.parse(fs.readFileSync(file, 'utf8'));
    const pkg = json.client?.[0]?.client_info?.android_client_info?.package_name;
    const proj = json.project_info?.project_id;
    if (pkg !== 'com.botanik.app') {
      console.log(`${FAIL}google-services.json es de otro paquete: ${pkg}`);
      console.log('      Debe ser com.botanik.app');
      return false;
    }
    console.log(`${OK}google-services.json OK (${proj} / ${pkg}).`);
    return true;
  } catch (e) {
    console.log(`${FAIL}google-services.json esta corrupto: ${e.message}`);
    return false;
  }
}

function checkBuild() {
  try {
    execSync(`${GRADLEW} assembleDebug --console=plain`, {
      cwd: ROOT,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
      maxBuffer: 1024 * 1024 * 32,
    });
    console.log(`${OK}El proyecto compila (assembleDebug).`);
    return true;
  } catch (e) {
    console.log(`${FAIL}El proyecto NO compila. Revisa el error de Gradle.`);
    return false;
  }
}

function main() {
  head('Verificacion del setup - Botani-k');

  checkLocalProperties();
  console.log('');
  checkGoogleServices();
  console.log('');

  head('Login con Google: SHA-1 del debug keystore');
  console.log('Cada computadora genera su propio debug.keystore, y por lo tanto');
  console.log('su propio SHA-1. Si el de esta maquina no esta registrado en');
  console.log('Firebase Console, el login con Google falla con "La sesion se cerro".');
  console.log('');

  const sha1 = getDebugSha1();
  if (sha1) {
    console.log(`${OK}SHA-1 de esta maquina:`);
    console.log('');
    console.log('    ' + sha1);
    console.log('');
    console.log('  Registralo en:');
    console.log('    Firebase Console -> proyecto botani-k -> Project settings');
    console.log('    -> Your apps -> app Android (com.botanik.app)');
    console.log('    -> General -> Add fingerprint -> pega el SHA-1 de arriba');
    console.log('');
    console.log('  Espera 5-10 minutos y luego recompila con:');
    console.log('    ' + GRADLEW + ' clean assembleDebug');
  } else {
    console.log(`${WARN}No se pudo obtener el SHA-1.`);
    console.log('      Ejecuta manualmente: ' + GRADLEW + ' signingReport');
  }

  console.log('');
  head('Compilacion');
  const builds = checkBuild();

  console.log('');
  head('Resumen');
  console.log('  Detalle completo en SETUP_OTRA_COMPUTADORA.md');
  console.log('');

  if (!builds) process.exit(1);
}

main();

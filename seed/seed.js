/**
 * Seed del catálogo de plantas endémicas de Cajamarca para Botani-k.
 * Compatible con firebase-admin v13+ (API moderna con submódulos).
 *
 * Uso:
 *   1. Descarga tu Service Account key y colócala en esta carpeta
 *      (ver README.md de esta carpeta: NUNCA se sube al repositorio)
 *   2. cd seed && npm install
 *   3. node seed.js
 */

const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { initializeFirebaseApp } = require('./firebaseCredentials');

const db = getFirestore(initializeFirebaseApp());
const catalogRef = db.collection('catalog');

// ─────────────────────────────────────────────────────────────
// 20 plantas (endémicas y nativas relevantes para Cajamarca)
// ─────────────────────────────────────────────────────────────
const catalogPlants = [
  {
    id: 'chuquiragua',
    commonName: 'Chuquiragua',
    scientificName: 'Chuquiraga jussieui',
    family: 'Asteraceae',
    region: 'Andes de Cajamarca, 3500–4500 msnm',
    floweringSeason: 'Todo el año, especialmente mayo–agosto',
    care: { light: 'Sol pleno', water: 'Escaso, resistente a sequía', temperature: '-5°C a 20°C' },
    funFacts: [
      'Conocida como "flor del andinista"',
      'Sus flores son símbolo de resistencia en los Andes',
      'Usada tradicionalmente en medicina para el mal de altura',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'orquidea-cajamarca',
    commonName: 'Orquídea de Cajamarca',
    scientificName: 'Telipogon peruvianus',
    family: 'Orchidaceae',
    region: 'Bosques nublados de Cajamarca, 2500–3200 msnm',
    floweringSeason: 'Marzo – junio',
    care: { light: 'Luz filtrada (sombra parcial)', water: 'Alta humedad, riego frecuente', temperature: '12°C a 22°C' },
    funFacts: [
      'Endémica del norte peruano',
      'Sus flores imitan a insectos para atraer polinizadores',
      'Amenazada por deforestación',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'begonia-bosque',
    commonName: 'Begonia de bosque nublado',
    scientificName: 'Begonia cajamarcensis',
    family: 'Begoniaceae',
    region: 'Bosques montanos de Cajamarca, 2000–3000 msnm',
    floweringSeason: 'Octubre – marzo',
    care: { light: 'Sombra parcial', water: 'Moderado, suelo húmedo', temperature: '15°C a 24°C' },
    funFacts: [
      'Descrita por primera vez en Cajamarca',
      'Sus hojas asimétricas son características',
      'Indicadora de bosques bien conservados',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'cactacea-cajamarca',
    commonName: 'Cactácea de Cajamarca',
    scientificName: 'Opuntia cajamarcensis',
    family: 'Cactaceae',
    region: 'Valles secos interandinos de Cajamarca, 2000–3000 msnm',
    floweringSeason: 'Septiembre – noviembre',
    care: { light: 'Sol pleno', water: 'Muy escaso', temperature: '5°C a 30°C' },
    funFacts: [
      'Produce tunas comestibles',
      'Usada para cercos vivos tradicionales',
      'Resistente a condiciones extremas',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'amancaes',
    commonName: 'Amancaes',
    scientificName: 'Ismene amancaes',
    family: 'Amaryllidaceae',
    region: 'Lomas y valles costeros e interandinos',
    floweringSeason: 'Junio – agosto',
    care: { light: 'Sol filtrado', water: 'Moderado', temperature: '10°C a 25°C' },
    funFacts: [
      'Flor símbolo de Lima',
      'Florece solo en invierno',
      'En peligro por pérdida de hábitat',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'cantuta',
    commonName: 'Cantuta',
    scientificName: 'Cantua buxifolia',
    family: 'Polemoniaceae',
    region: 'Andes del Perú, 2000–3500 msnm',
    floweringSeason: 'Abril – julio',
    care: { light: 'Sol pleno', water: 'Moderado', temperature: '5°C a 22°C' },
    funFacts: [
      'Flor nacional del Perú',
      'Considerada sagrada por los incas',
      'Atrae colibríes',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'molle',
    commonName: 'Molle',
    scientificName: 'Schinus molle',
    family: 'Anacardiaceae',
    region: 'Valles interandinos, 1000–3500 msnm',
    floweringSeason: 'Todo el año',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '0°C a 30°C' },
    funFacts: [
      'Usado para hacer chicha de molle',
      'Sus frutos rosados son decorativos',
      'Árbol sagrado en la cosmovisión andina',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'papa-andina-silvestre',
    commonName: 'Papa andina silvestre',
    scientificName: 'Solanum tuberosum',
    family: 'Solanaceae',
    region: 'Andes de Cajamarca, 2500–4000 msnm',
    floweringSeason: 'Diciembre – marzo',
    care: { light: 'Sol pleno', water: 'Moderado', temperature: '5°C a 20°C' },
    funFacts: [
      'Cajamarca es centro de origen de la papa',
      'Existen más de 4000 variedades nativas',
      'Base de la alimentación andina por milenios',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'quinua',
    commonName: 'Quinua',
    scientificName: 'Chenopodium quinoa',
    family: 'Amaranthaceae',
    region: 'Andes del Perú, 2500–4000 msnm',
    floweringSeason: 'Febrero – abril',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '0°C a 25°C' },
    funFacts: [
      'Considerada superalimento',
      'Resistente a suelos salinos',
      'Cultivada desde hace 7000 años',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'tarwi',
    commonName: 'Tarwi',
    scientificName: 'Lupinus mutabilis',
    family: 'Fabaceae',
    region: 'Andes de Cajamarca, 2000–4000 msnm',
    floweringSeason: 'Noviembre – febrero',
    care: { light: 'Sol pleno', water: 'Moderado', temperature: '3°C a 22°C' },
    funFacts: [
      'Altísimo contenido proteico',
      'Fija nitrógeno en el suelo',
      'Usado como abono verde',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'hierba-santa',
    commonName: 'Hierba santa',
    scientificName: 'Cestrum auriculatum',
    family: 'Solanaceae',
    region: 'Valles interandinos de Cajamarca, 1500–3000 msnm',
    floweringSeason: 'Todo el año',
    care: { light: 'Sol parcial', water: 'Moderado', temperature: '10°C a 25°C' },
    funFacts: [
      'Usada en medicina tradicional',
      'Arbusto aromático',
      'Frecuente en huertos familiares',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'matico',
    commonName: 'Matico',
    scientificName: 'Piper aduncum',
    family: 'Piperaceae',
    region: 'Bosques húmedos de Cajamarca, 500–2500 msnm',
    floweringSeason: 'Todo el año',
    care: { light: 'Sombra parcial', water: 'Alto', temperature: '15°C a 28°C' },
    funFacts: [
      'Cicatrizante natural',
      'Usado por comunidades amazónicas',
      'Arbusto de crecimiento rápido',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'sauco',
    commonName: 'Sauco',
    scientificName: 'Sambucus nigra',
    family: 'Adoxaceae',
    region: 'Andes del Perú, 2000–3500 msnm',
    floweringSeason: 'Septiembre – noviembre',
    care: { light: 'Sol parcial', water: 'Moderado', temperature: '0°C a 25°C' },
    funFacts: [
      'Sus flores se usan para infusiones',
      'Atrae aves polinizadoras',
      'Árbol medicinal tradicional',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'muna',
    commonName: 'Muña',
    scientificName: 'Minthostachys mollis',
    family: 'Lamiaceae',
    region: 'Andes de Cajamarca, 2500–3800 msnm',
    floweringSeason: 'Verano',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '3°C a 22°C' },
    funFacts: [
      'Aromática y digestiva',
      'Usada para ahuyentar insectos',
      'Infusión tradicional andina',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'chachacomo',
    commonName: 'Chachacomo',
    scientificName: 'Escallonia resinosa',
    family: 'Escalloniaceae',
    region: 'Andes del Perú, 2800–4000 msnm',
    floweringSeason: 'Diciembre – marzo',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '-5°C a 20°C' },
    funFacts: [
      'Madera dura muy apreciada',
      'Usado en cercos vivos',
      'Indicador de buena calidad de aire',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'aliso',
    commonName: 'Aliso',
    scientificName: 'Alnus acuminata',
    family: 'Betulaceae',
    region: 'Andes del Perú, 1500–3500 msnm',
    floweringSeason: 'Mayo – agosto',
    care: { light: 'Sol pleno', water: 'Alto', temperature: '5°C a 22°C' },
    funFacts: [
      'Fija nitrógeno en el suelo',
      'Ideal para reforestación',
      'Sus hojas sirven como abono',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'quishuar',
    commonName: 'Quishuar',
    scientificName: 'Buddleja incana',
    family: 'Scrophulariaceae',
    region: 'Andes de Cajamarca, 3000–4200 msnm',
    floweringSeason: 'Todo el año',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '-8°C a 18°C' },
    funFacts: [
      'Resiste heladas extremas',
      'Sus hojas tienen uso medicinal',
      'Árbol sagrado en época incaica',
    ],
    imageUrl: '',
    isEndemic: true,
  },
  {
    id: 'guinda',
    commonName: 'Guinda',
    scientificName: 'Prunus serotina',
    family: 'Rosaceae',
    region: 'Andes del Perú, 2000–3000 msnm',
    floweringSeason: 'Septiembre – octubre',
    care: { light: 'Sol pleno', water: 'Moderado', temperature: '5°C a 25°C' },
    funFacts: [
      'Sus frutos son comestibles',
      'Madera apreciada en ebanistería',
      'Atrae aves y polinizadores',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'zapote',
    commonName: 'Zapote',
    scientificName: 'Mespilus germanica',
    family: 'Rosaceae',
    region: 'Valles interandinos, 1500–2800 msnm',
    floweringSeason: 'Abril – junio',
    care: { light: 'Sol pleno', water: 'Moderado', temperature: '8°C a 25°C' },
    funFacts: [
      'Frutos dulces con textura única',
      'Árbol longevo',
      'Poco conocido fuera de los Andes',
    ],
    imageUrl: '',
    isEndemic: false,
  },
  {
    id: 'poro-poro',
    commonName: 'Poro poro',
    scientificName: 'Cochlospermum vitifolium',
    family: 'Bixaceae',
    region: 'Valles secos interandinos, 500–2000 msnm',
    floweringSeason: 'Marzo – mayo',
    care: { light: 'Sol pleno', water: 'Escaso', temperature: '15°C a 32°C' },
    funFacts: [
      'Flores amarillas espectaculares',
      'Usado como ornamental',
      'Su fibra se usa para textiles',
    ],
    imageUrl: '',
    isEndemic: false,
  },
];

// ─────────────────────────────────────────────────────────────
// Ejecución del seed
// ─────────────────────────────────────────────────────────────
async function seedCatalog() {
  console.log('🌱 Iniciando seed del catálogo Botani-k...\n');
  console.log(`📦 Total de plantas a cargar: ${catalogPlants.length}\n`);

  const batch = db.batch();
  const now = FieldValue.serverTimestamp();

  for (const plant of catalogPlants) {
    const { id, ...plantData } = plant;
    const docRef = catalogRef.doc(id);

    // Se omiten los campos declarados como cadena vacía (ej. imageUrl: '')
    // para no sobrescribir las URLs de imagen ya subidas manualmente.
    const dataToWrite = Object.fromEntries(
      Object.entries(plantData).filter(([, value]) => value !== '')
    );

    // merge: true -> NO sobrescribe campos que ya existen en el documento.
    // Sin esto, cada corrida del seed borraría los `imageUrl` ya subidos.
    batch.set(
      docRef,
      {
        ...dataToWrite,
        createdAt: now,
      },
      { merge: true }
    );

    console.log(`  ✓ ${plant.commonName.padEnd(30)} (${plant.scientificName})`);
  }

  try {
    await batch.commit();
    console.log(`\n✅ ${catalogPlants.length} plantas cargadas exitosamente.`);
    console.log(`🔗 Verifica en: https://console.firebase.google.com/project/botani-k/firestore`);
  } catch (error) {
    console.error('\n❌ Error al cargar el catálogo:', error);
    process.exit(1);
  }

  process.exit(0);
}

seedCatalog();
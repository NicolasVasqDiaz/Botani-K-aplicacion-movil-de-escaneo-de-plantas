/**
 * Seed adicional - 20 plantas endemicas/nativas del Peru (prioridad Cajamarca).
 * NO repite las 20 especies del primer seed (seed.js).
 *
 * Uso:
 *   1. Coloca serviceAccountKey.json en esta misma carpeta
 *   2. cd seed && npm install firebase-admin
 *   3. node seed_cajamarca_extra.js   (o: npm run seed:extra)
 *
 * Seguro para re-ejecutar: usa merge y omite los campos declarados como
 * cadena vacia, por lo que NO sobrescribe los `imageUrl` ya subidos
 * manualmente a Firestore.
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount),
});

const db = getFirestore();
const catalogRef = db.collection('catalog');

// -------------------------------------------------------------
// 20 especies nuevas (endemicas del Peru / Cajamarca)
// -------------------------------------------------------------
const catalogPlants = [
  {
    id: 'senecio-szyszylowiczii',
    commonName: 'Senecio de Cajamarca',
    scientificName: 'Senecio szyszylowiczii',
    family: 'Asteraceae',
    region: 'Cajamarca, 2400–3500 msnm',
    floweringSeason: 'Todo el año, con pico en época seca',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado, tolera sequía',
      temperature: '5°C a 22°C',
    },
    funFacts: [
      'Endémica del norte del Perú',
      'Tolera heladas ligeras',
      'Atrae polinizadores nativos',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Senecio+szyszylowiczii',
    isEndemic: true,
  },
  {
    id: 'laccopetalum-giganteum',
    commonName: 'Pacra-pacra',
    scientificName: 'Laccopetalum giganteum',
    family: 'Ranunculaceae',
    region: 'Jalcas del norte del Perú, 3500–4500 msnm',
    floweringSeason: 'Época húmeda (diciembre–marzo)',
    care: {
      light: 'Sol pleno',
      water: 'Moderado, suelo húmedo',
      temperature: '-2°C a 15°C',
    },
    funFacts: [
      'Género monotípico endémico del Perú',
      'Sus flores pueden alcanzar 15 cm de diámetro',
      'Amenazada por sobreextracción medicinal',
    ],
    imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/3/3c/Laccopgig1.jpg',
    isEndemic: true,
  },
  {
    id: 'calceolaria-caespitosa',
    commonName: 'Calceolaria cespitosa',
    scientificName: 'Calceolaria caespitosa',
    family: 'Calceolariaceae',
    region: 'Jalca de Cajamarca, 3200–4000 msnm',
    floweringSeason: 'Mayo – agosto',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Bajo, bien drenado',
      temperature: '0°C a 18°C',
    },
    funFacts: [
      'Forma cojines compactos en la jalca',
      'Endémica de Cajamarca',
      'Sus flores amarillas son muy vistosas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Calceolaria+caespitosa',
    isEndemic: true,
  },
  {
    id: 'jaltomata-cajamarca',
    commonName: 'Canamuela',
    scientificName: 'Jaltomata cajamarca',
    family: 'Solanaceae',
    region: 'Cajamarca, 2250–3050 msnm',
    floweringSeason: 'Abril – junio',
    care: {
      light: 'Sol pleno (heliófila)',
      water: 'Moderado',
      temperature: '8°C a 22°C',
    },
    funFacts: [
      'Endémica de Cajamarca',
      'Produce bayas anaranjadas comestibles',
      'Crece en bordes de caminos y laderas rocosas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Jaltomata+cajamarca',
    isEndemic: true,
  },
  {
    id: 'bomarea-peruviana',
    commonName: 'Bomarea peruana',
    scientificName: 'Bomarea peruviana',
    family: 'Alstroemeriaceae',
    region: 'Cajamarca y La Libertad, 2000–3500 msnm',
    floweringSeason: 'Todo el año, con pico en verano',
    care: {
      light: 'Semisombra',
      water: 'Moderado-alto',
      temperature: '8°C a 20°C',
    },
    funFacts: [
      'Endémica del Perú',
      'Sus flores tubulares atraen colibríes',
      'Trepadora o erguida según el hábitat',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Bomarea+peruviana',
    isEndemic: true,
  },
  {
    id: 'salvia-vargas-llosae',
    commonName: 'Salvia de Celendín',
    scientificName: 'Salvia vargas-llosae',
    family: 'Lamiaceae',
    region: 'Jalca de Celendín, Cajamarca, >3000 msnm',
    floweringSeason: 'Época húmeda (enero–abril)',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado',
      temperature: '5°C a 20°C',
    },
    funFacts: [
      'Endémica de la jalca de Celendín',
      'Descrita recientemente para la ciencia',
      'Sus flores atraen abejas y mariposas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Salvia+vargas-llosae',
    isEndemic: true,
  },
  {
    id: 'valeriana-cumbemayensis',
    commonName: 'Valeriana de Cumbemayo',
    scientificName: 'Valeriana cumbemayensis',
    family: 'Caprifoliaceae',
    region: 'Cumbemayo, Cajamarca, 3500–3600 msnm',
    floweringSeason: 'Con las lluvias (diciembre–marzo)',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado, suelo húmedo',
      temperature: '0°C a 15°C',
    },
    funFacts: [
      'Endémica de los alrededores de Cumbemayo',
      'Usada en medicina tradicional andina',
      'Prefiere suelos húmedos y bien drenados',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Valeriana+cumbemayensis',
    isEndemic: true,
  },
  {
    id: 'rauhia-multiflora',
    commonName: 'Rauhia',
    scientificName: 'Rauhia multiflora',
    family: 'Amaryllidaceae',
    region: 'Norte del Perú (Cajamarca, La Libertad), 1500–3000 msnm',
    floweringSeason: 'Finales de invierno – primavera',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Bajo, muy bien drenado',
      temperature: '5°C a 25°C',
    },
    funFacts: [
      'Género endémico del Perú',
      'Bulbosa de zonas secas y cálidas',
      'Entra en dormancia en invierno',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Rauhia+multiflora',
    isEndemic: true,
  },
  {
    id: 'castilleja-isidoroi',
    commonName: 'Castilleja de Isidoro',
    scientificName: 'Castilleja isidoroi',
    family: 'Orobanchaceae',
    region: 'Jalca de Amazonas, Cajamarca y La Libertad, 3300–4000 msnm',
    floweringSeason: 'Marzo – septiembre',
    care: {
      light: 'Sol pleno',
      water: 'Bajo-moderado',
      temperature: '-2°C a 15°C',
    },
    funFacts: [
      'Especie hemiparásita (obtiene nutrientes de otras plantas)',
      'Endémica de los Andes del norte del Perú',
      'Sus flores atraen mariposas y colibríes',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Castilleja+isidoroi',
    isEndemic: true,
  },
  {
    id: 'solanum-chiquidenum',
    commonName: 'Papita silvestre de Chiquiden',
    scientificName: 'Solanum chiquidenum',
    family: 'Solanaceae',
    region: 'Cajamarca, 2500–3200 msnm',
    floweringSeason: 'Febrero – mayo',
    care: {
      light: 'Sol pleno',
      water: 'Moderado',
      temperature: '5°C a 20°C',
    },
    funFacts: [
      'Pariente silvestre de la papa cultivada',
      'Endémica del norte del Perú',
      'Resistente a climas fríos de altura',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Solanum+chiquidenum',
    isEndemic: true,
  },
  {
    id: 'calceolaria-cumbemayensis',
    commonName: 'Calceolaria de Cumbemayo',
    scientificName: 'Calceolaria cumbemayensis',
    family: 'Calceolariaceae',
    region: 'Cumbemayo, Cajamarca, 3400–3600 msnm',
    floweringSeason: 'Junio – septiembre',
    care: {
      light: 'Semisombra',
      water: 'Moderado',
      temperature: '0°C a 18°C',
    },
    funFacts: [
      'Endémica de Cajamarca',
      'Sus flores tienen forma de bolsita',
      'Crece en roquedales y pastizales de altura',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Calceolaria+cumbemayensis',
    isEndemic: true,
  },
  {
    id: 'symplocos-sandemanii',
    commonName: 'Cascapilla',
    scientificName: 'Symplocos sandemanii',
    family: 'Symplocaceae',
    region: 'Bosques nublados de Cajamarca y Piura, 2000–3000 msnm',
    floweringSeason: 'Todo el año',
    care: {
      light: 'Semisombra a sombra',
      water: 'Alto (bosque nublado)',
      temperature: '10°C a 20°C',
    },
    funFacts: [
      'Árbol característico de los bosques nublados del norte peruano',
      'Sus hojas son coriáceas y perennes',
      'Importante para la captación de agua',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Symplocos+sandemanii',
    isEndemic: true,
  },
  {
    id: 'podocarpus-glomeratus',
    commonName: 'Intimpa / Romerillo',
    scientificName: 'Podocarpus glomeratus',
    family: 'Podocarpaceae',
    region: 'Bosques nublados andinos, 1800–3200 msnm',
    floweringSeason: 'Primavera (producción de conos)',
    care: {
      light: 'Semisombra a sombra',
      water: 'Alto',
      temperature: '8°C a 20°C',
    },
    funFacts: [
      'Conífera nativa de los Andes',
      'Especie relicta de bosques antiguos',
      'Su madera es muy apreciada y está amenazada',
    ],
    imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/b/be/Podocarpus_glomeratus.jpg',
    isEndemic: false,
  },
  {
    id: 'oreocallis-grandiflora',
    commonName: 'Chacpá / Alamacha',
    scientificName: 'Oreocallis grandiflora',
    family: 'Proteaceae',
    region: 'Andes del Perú, 2000–3500 msnm',
    floweringSeason: 'Todo el año, con pico en verano',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado',
      temperature: '5°C a 22°C',
    },
    funFacts: [
      'Sus flores rojas o rosadas son muy llamativas',
      'Atrae colibríes y otros polinizadores',
      'Usada como ornamental en jardines andinos',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Oreocallis+grandiflora',
    isEndemic: false,
  },
  {
    id: 'browallia-guzmangoa',
    commonName: 'Browallia de Guzmango',
    scientificName: 'Browallia guzmangoa',
    family: 'Solanaceae',
    region: 'Contumazá, Cajamarca, 2500–2600 msnm',
    floweringSeason: 'Verano',
    care: {
      light: 'Semisombra',
      water: 'Moderado',
      temperature: '12°C a 22°C',
    },
    funFacts: [
      'Endémica de la zona de Guzmango',
      'Flores azules o violetas',
      'Prefiere suelos bien drenados',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Browallia+guzmangoa',
    isEndemic: true,
  },
  {
    id: 'nasa-dillonii',
    commonName: 'Nasa de Cajamarca',
    scientificName: 'Nasa dillonii',
    family: 'Loasaceae',
    region: 'Cajamarca, 2500–3500 msnm',
    floweringSeason: 'Época húmeda',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado',
      temperature: '8°C a 20°C',
    },
    funFacts: [
      'Endémica del norte del Perú',
      'Hojas lobuladas y flores blancas o amarillas',
      'Crece en laderas rocosas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Nasa+dillonii',
    isEndemic: true,
  },
  {
    id: 'dyssodia-jelskii',
    commonName: 'Dyssodia de Jelski',
    scientificName: 'Dyssodia jelskii',
    family: 'Asteraceae',
    region: 'Cajamarca, 2500–3500 msnm',
    floweringSeason: 'Todo el año',
    care: {
      light: 'Sol pleno',
      water: 'Bajo',
      temperature: '5°C a 22°C',
    },
    funFacts: [
      'Endémica de Cajamarca',
      'Planta aromática',
      'Flores amarillas o anaranjadas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Dyssodia+jelskii',
    isEndemic: true,
  },
  {
    id: 'chamaecrista-glandulosa',
    commonName: 'Chamaecrista',
    scientificName: 'Chamaecrista glandulosa',
    family: 'Fabaceae',
    region: 'Cajamarca, 2000–3000 msnm',
    floweringSeason: 'Verano',
    care: {
      light: 'Sol pleno',
      water: 'Bajo-moderado',
      temperature: '10°C a 25°C',
    },
    funFacts: [
      'Endémica de Cajamarca',
      'Leguminosa fijadora de nitrógeno',
      'Flores amarillas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Chamaecrista+glandulosa',
    isEndemic: true,
  },
  {
    id: 'galactia-augusti',
    commonName: 'Galactia de Augusto',
    scientificName: 'Galactia augusti',
    family: 'Fabaceae',
    region: 'Cajamarca, 2000–3200 msnm',
    floweringSeason: 'Época húmeda',
    care: {
      light: 'Sol pleno a semisombra',
      water: 'Moderado',
      temperature: '8°C a 22°C',
    },
    funFacts: [
      'Endémica de Cajamarca',
      'Trepadora o rastrera',
      'Flores rosadas o moradas',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Galactia+augusti',
    isEndemic: true,
  },
  {
    id: 'ascidiogyne-sanchezvegae',
    commonName: 'Ascidiogyne',
    scientificName: 'Ascidiogyne sanchezvegae',
    family: 'Asteraceae',
    region: 'Jalca de Cajamarca, 3250–3800 msnm',
    floweringSeason: 'Época húmeda (diciembre–marzo)',
    care: {
      light: 'Sol pleno',
      water: 'Moderado',
      temperature: '0°C a 15°C',
    },
    funFacts: [
      'Endémica de la jalca de Cajamarca',
      'En peligro crítico por pérdida de hábitat',
      'Forma rosetas con flores sésiles',
    ],
    imageUrl: 'https://placehold.co/600x400/4A7C59/FFFFFF?text=Ascidiogyne+sanchezvegae',
    isEndemic: true,
  },
];

// -------------------------------------------------------------
// Ejecucion del seed
// -------------------------------------------------------------
async function seedCatalog() {
  console.log('Iniciando seed adicional de flora peruana (Cajamarca)...\n');
  console.log(`Total de plantas a cargar: ${catalogPlants.length}\n`);

  const batch = db.batch();
  const now = FieldValue.serverTimestamp();

  for (const plant of catalogPlants) {
    const { id, ...plantData } = plant;
    const docRef = catalogRef.doc(id);

    // Se omiten los campos declarados como cadena vacia (ej. imageUrl: '')
    // para no sobrescribir las URLs de imagen ya subidas manualmente.
    const dataToWrite = Object.fromEntries(
      Object.entries(plantData).filter(([, value]) => value !== '')
    );

    // merge: true -> NO sobrescribe campos que ya existen en el documento.
    batch.set(
      docRef,
      {
        ...dataToWrite,
        createdAt: now,
      },
      { merge: true }
    );

    console.log(`  OK ${plant.commonName.padEnd(30)} (${plant.scientificName})`);
  }

  try {
    await batch.commit();
    console.log(`\n${catalogPlants.length} plantas adicionales cargadas exitosamente.`);
    console.log('Verifica en: https://console.firebase.google.com/project/botani-k/firestore');
  } catch (error) {
    console.error('\nError al cargar el catalogo:', error);
    process.exit(1);
  }

  process.exit(0);
}

seedCatalog();

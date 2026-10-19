# PRE-G9B-CU — Plan candidato de corrección y mejora (Corrective and Usability Track)

| Campo | Valor |
|---|---|
| Carácter | Plan candidato de un track correctivo y de usabilidad. No es especificación normativa, no autoriza implementación y no altera fases históricas ni sus registros |
| Estado | `PLAN CANDIDATE — PENDING AUTHOR REVIEW`; `implementationAuthorized=false`; `selfApproved=false` |
| Identificador propuesto | `PRE-G9B-CU` (*Corrective and Usability*), con paquetes `CU0`–`CU10`. Familia nueva, sin colisión con `PRE-G9B-S/D/P/R` ni con ningún identificador G |
| Encargo | «GeoCeDG — Corrective and Usability Track», aportación autoral de 2026-10-19 (B01–B12, M01–M10), autorizada en esta primera ejecución solo para análisis y planificación |
| Base inspeccionada | `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`, árbol `75ebf03f089a5effa02daa4a68be360b7499fdd9`, árbol de trabajo limpio |
| Evidencia | [informe de caracterización](../validation/pre_g9b_cu_characterization_evidence_report.md) |
| Libro | `geocedg_book` en pausa. Solo se ha inspeccionado su estado; no se modifica |

## 0. Resumen ejecutivo

1. **B10 (P0) tiene causa establecida y es una no conformidad con el contrato
   ya aprobado, no una ampliación.** Cada punto de `e_2` recalcula entera una
   copia aislada de su construcción, incluida una copia de `o_1` cuyo índice
   métrico se reconstruye dos veces por muestra (≈ 43 000 evaluaciones
   internas). Además, cada recálculo publica una revisión semántica nueva aunque
   la geometría no cambie. `locus-v2-semantics.md` prohíbe ambas cosas. Medido:
   arrastrar el conductor cuesta ≈ 5,8 s por paso con `e_2` y ≈ 79 ms sin él.
2. **El cierre falso de `e_2` (B10.2), los huecos (B07) y la desaparición
   completa de loci comparten una causa.** El productor dependiente de Locus V2
   certifica `COMPLETE` con una única componente periódica sin ninguna prueba
   sobre la construcción dependiente. Contradice el ADR 0027 y
   `locus-v2-existence-components.md`. El renderizador, por su parte, une saltos
   y recurre a muestreo uniforme grueso. Además, un locus entero queda
   indefinido cuando su punto trazado no existe en la posición *actual* del
   conductor.
3. **B08 y B09 no son bloqueos por completitud global.** Las tres soluciones
   pedidas son tangencias (contacto de la curva de intersección con contornos
   aparentes y líneas de referencia), y el contrato público vigente nunca
   materializa tangencias. Resolverlas exige una decisión autoral. Se recomienda
   un punto estructural por correspondencia de conductor, exacto y dinámico.
   Durante la caracterización apareció además un defecto real e independiente:
   la tolerancia de residuo para objetivos recta/segmento no escala con el
   dibujo.
4. **B06 es de presentación.** La evaluación semántica es suave (defecto ∝ h²
   hasta 1e-14). Las facetas proceden de una teselación con profundidad mínima 2
   y criterio de un solo punto medio.
5. **B01 es una exclusión contractual de la matriz `P4`**, ya registrada como
   `DEFER-F3-UNLISTED-TYPES`. No es un fallo del tratamiento angular.
6. **Varios problemas de interfaz tienen causa establecida:**
   - B02: FlatLaf impone filas uniformes de 20 px en cada refresco de UI, que
     alternan con las de dos líneas (36/40 px).
   - B03: vaciado contractual en deshacer, rehacer y reconstrucción, y regla de
     partida de la activación dual.
   - B05: los objetos Locus V2 no aplican los valores por defecto de construcción.
   - M04: las rectas cuya cadena de referencia nace en un eje quedan excluidas.
   - M05.1: el modo de etiqueta del deslizador se pierde en el viaje de ida y
     vuelta por XML con el etiquetado «No New Objects».
7. **Secuencia recomendada:**
   - Camino crítico: `CU0` → `CU1` → `CU2` → `CU3` → `CU7` → `CU9` → `CU10`.
   - En paralelo con él: `CU4`, `CU5` → `CU6` y `CU8`.
   - Una sola campaña `FINAL` al cierre. El libro solo actualiza su base técnica
     después de la publicación etiquetada del track.

## A. Estado de partida

### A.1 Repositorio

| Elemento | Estado |
|---|---|
| Rama y commit | `main` en `48a0775e` (`fix(book-bridge): accept PRE/POST closed phases and schema-2 profiles`), idéntico a `origin/main`; 0 commits por delante o por detrás |
| Árbol de trabajo | limpio antes de esta ejecución. Esta ejecución añade solo este plan y el informe de evidencia, sin confirmar |
| Etiquetas | `geocedg-pre-g9b-r7-pass` → `372da89f` (anotada, publicada). `48a0775e` no tiene etiqueta |
| Worktrees locales ajenos | `.claude/worktrees/stoic-lovelace-7c499d` (`48a0775e`) y un worktree temporal de otra sesión (`257c854a`). No intervienen |
| Desfase documental | la cabecera del roadmap (v5.58) aún dice que `PRE-G9B-R7` está «aún sin publicar», aunque `372da89f` y su etiqueta están publicados. La corrección del puente `48a0775e` tampoco consta. Es un registro de publicación pendiente, propuesto en `CU0` |

### A.2 Fase vigente y autorización

Según el roadmap leído de disco:

- **Cerrada:** la última fase cerrada es `PRE-G9B-R7` — `PASS — AUTHOR APPROVED`.
- **Siguiente puerta:** «una decisión autoral separada de priorización y
  autorización del roadmap, sin fase productiva preseleccionada». G9B no la
  sigue automáticamente.
- **Diseñadas y no autorizadas:** G9B y G9C.
- **Bloqueada:** G9U2.
- **Planificación aprobada sin producto:** G10.
- **`PENDING — REVISED FUTURE SCOPE`:** G11, G12 y G15.
- **No iniciadas:** G13, G14 y G16.
- **Libro:** BOOK-P1, BOOK-FIGPOL, BOOK-B1 y BOOK-FIGPOL-PILOT cerradas y
  publicadas.

Este plan es un candidato para esa decisión de priorización. Ninguna parte
autoriza implementación: cada paquete requiere su propia autorización autoral
con commit base exacto.

### A.3 Funcionalidades afectadas, contratos y ADR

| Área | Fase propietaria y estado | Autoridad normativa vigente |
|---|---|---|
| Evaluación, revisión y composición de Locus V2 | G6/G6R `PASS`; G9U0 y R1–R6 `PASS — AUTHOR APPROVED` | `geocedg/specs/locus/locus-v2-semantics.md` (corte una vez por revisión; métrica reutilizable aguas abajo); ADR 0006 |
| Métricas y `LocusLength` | G7 `PASS — AUTHOR APPROVED`; POST-G9U1-A2 (política de recorrido) `PASS — AUTHOR APPROVED`; PRE-G9B-R2 (procedencia de extremos) `PASS — AUTHOR APPROVED` | `locus-v2-metrics.md`; ADR 0007, ADR 0028 |
| Existencia y componentes continuas | PRE-G9B-S1-R2 `PASS — AUTHOR APPROVED` | `locus-v2-existence-components.md`; ADR 0027; matriz R2 |
| Intersecciones y materialización | G8 `PASS — AUTHOR APPROVED`; G8B-R1 (opción B: admisibilidad local); G9U0-R4 | `locus-v2-intersections.md`, `locus-v2-extended-intersections.md`, `locus-v2-public-surface.md`, `locus-v2-certified-construction-model.md`; ADR 0008, 0009, 0013, 0017, 0021, 0028 |
| Interacción de puntos semánticos | G9U0-R6 `PASS — AUTHOR APPROVED` | `locus-v2-point-interaction.md`; ADR 0019 |
| Redefinición avanzada | POST-G9U1-A3 y A3-R1; PRE-G9B-R3 | ADR 0026 |
| Presentación de Locus V2 | G9U0-R2/R3 | `locus-v2-presentation.md`, `locus-v2-public-ui-exposure.md` |
| Inserción de documentos | PRE-G9B-R6-plus-F3, F3-R1 y F3-R2 `PASS — AUTHOR APPROVED — PUBLISHED` | `geocedg/specs/ui/document-insertion.md` 1.2 (matriz `P4`) |
| Memoria de autoría | F2 y F2-R1 `PASS — AUTHOR APPROVED — PUBLISHED` | `geocedg/specs/ui/authoring-memory.md` 1.1 |
| Indicación de orientación | F1 `PASS — AUTHOR APPROVED — PUBLISHED` | `geocedg/specs/presentation/orientation-cue.md` (rutas `A1`–`A17`, exclusiones `X1`–`X10`) |
| Tamaños de presentación y temas | PRE-G9B-S4 y P0 `PASS — AUTHOR APPROVED` | `application-presentation-sizing.md`, `application-presentation-theme.md` |
| Guía de usuario | PRE-G9B-R5-A `PASS — AUTHOR APPROVED` (estructura y navegación) | `geocedg/specs/operations/documentation-maintenance.md` §5 (`GUIDE_IMPACT`) y §10 (estructura) |
| Verificación | GOV-R y mantenimientos `PASS — AUTHOR APPROVED` | `tools/agent/verify.ps1`, `verification-registry.json`, esquemas; `verification-levels.md` (contrato vivo inicial) |

### A.4 Relación con deudas conocidas

| Incidencia | Deuda u observación existente | Estado |
|---|---|---|
| B01 | `DEFER-F3-UNLISTED-TYPES` (nombra textos estáticos y ángulos independientes); `OBS-F3-PROTECTED-LABELS` | abierta, no autorizada |
| B02 | `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` (orden, **no** espaciado); `OBS-D1-ROLLBACK-CONSPROT-GUI` | diferida o abierta. B02 es distinta |
| B03 | L-21 (limitaciones F2/F2-R1) | limitación aceptada |
| B05, M01 | `DEFER-F3-TEMPLATE-DEFAULTS`; `F4R-DEFAULTS-UNDO` (L-5); D-9 (valores físicos configurables); `OBS-D1-DEFAULTS-SAVE-RELEVANT` (L-17); raíz común con `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | abiertas o aceptadas |
| M04 | L-29 (rutas excluidas X1–X10 y Gráficos 2) | limitación aceptada |
| B06, B07, B10 | ninguna; solo la puerta genérica G16 | — |
| B08, B09, M09 | `A3-PAIR-SOURCE-COMPATIBLE-STRUCTURAL-REDEFINE` y afines (fail-closed) | limitación aceptada |
| B10.2 | `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` (cierre por igualdad de extremos en SplineV2) | limitación aceptada, distinta de B10.2 |
| B11, B12 | `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL`, `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION`, `OBS-R7-G9R4-LIVING-DOC-DRIFT` | abiertas o aceptadas |
| M02, M03, M05/M05.1, M06, M07, M08, M10 | ninguna (M03 enlaza con la limitación editorial L12 del piloto del libro) | — |

### A.5 Archivos del autor

Ver el [informe de evidencia](../validation/pre_g9b_cu_characterization_evidence_report.md) §2:

- `InterCilConoObliqueTest.ggb`: SHA-256 `88779d58…0398`, 25 203 bytes, GeoGebra Classic 5.4.932.1.
- `InterCilConoOblique.cedg`: SHA-256 `1ae6f512…456c`, 58 896 bytes, documento nativo.

Los originales permanecen intactos e ignorados en
`artifacts/author-input/Lista19oct2026/`. Ninguna incidencia se ha atribuido a
un defecto constructivo de los modelos.

## B. Matriz de incidencias

### B.1 Resumen

Clases: **C** = corrección de comportamiento contra un contrato vigente;
**A** = ampliación de un contrato vigente; **N** = funcionalidad nueva;
**D** = documentación.

| ID | Incidencia normalizada | Prioridad (inicial → propuesta) | Clase | Capa | Paquete |
|---|---|---|---|---|---|
| B01 | Insertar archivo rechaza ángulos libres (deslizadores) y textos estáticos | P1 → P1 | A | Desktop + especificación F3 | CU4 |
| B02 | Alto de filas del protocolo alterna entre 20 px uniformes y 36/40 px | P2 → P2 | C | Desktop (vista heredada) | CU5 |
| B03 | Memorias de Paralela y Perpendicular se vacían o dejan de aplicarse | P1 → P1 | A | Desktop | CU5 |
| B04 | Indicación visual del índice de cada solución de `Intersect(c, n)` | P2 → P2 | N | Desktop (presentación) | CU6 |
| B05 | Locus V2, SplineV2 y resultados ricos ignoran «No New Objects» | P2 → P2 | C | núcleo (valores por defecto) | CU4 |
| B06 | Rizado de curvas Locus V2 (teselación de presentación) | P1 → P2 | C | dibujo compartido | CU2 |
| B07 | Huecos en la transición y desaparición de loci | P1 → P1 | C | núcleo + dibujo | CU2 |
| B08 | Contactos de `e_1` con `n` y `c_1` no materializables | P1 → P1 | A | núcleo | CU3 |
| B09 | Contacto `o_1` ∩ `h_2` no materializable | P1 → P1 | A | núcleo | CU3 |
| B10.1 | `e_2` bloquea la aplicación | P0 → P0 | C | núcleo | CU1 |
| B10.2 | `e_2` dibujado cerrado | P0 → P0 | C | núcleo + dibujo | CU2 |
| B11 | Documentación completa de `LocusLength` | P2 → P2 | D | guía | CU9 |
| B12 | Revisión exhaustiva de la guía con matriz de cobertura | obligatoria | D | guía + pruebas de estructura | CU9 |
| M01 | Estilos por defecto persistentes; Locus → LocusV2; SplineV2 | — → P2 | C + A | Desktop + valores por defecto | CU4 |
| M02 | ESC cierra el menú compacto de estilos | — → P3 | N | Desktop | CU6 |
| M03 | Mayor libertad al desplazar etiquetas | — → P2 | A | núcleo heredado + dibujo | CU6 |
| M04 | Indicación de orientación en Paralela y Perpendicular | — → P2 | A | Desktop + especificación F1 | CU5 |
| M05 | Contenedor de controles fijo o vinculado al lienzo | — → P3 | N | frontend + persistencia | CU8 |
| M05.1 | El deslizador pierde «nombre + valor» | — → P2 | C | núcleo heredado (XML) | CU4 |
| M06 | Buscador por nombre en la vista algebraica | — → P3 | N | Desktop | CU6 |
| M07 | Clic en el lienzo inserta el nombre en el diálogo activo | — → P2 | A | Desktop | CU6 |
| M08 | `PointOnLocusV2`: punto sobre Locus V2 a partir de un punto próximo | — → P2 | N | núcleo | CU7 |
| M09 | Redefinir una intersección ordinaria como intersección Locus V2 | — → P3 | N | núcleo | CU7 |
| M10 | Información complementaria (Valor/Definición) en la vista algebraica | — → P3 | A | Desktop | CU6 |
| H01 | Un locus entero queda indefinido si el trazador no existe en la posición actual del conductor | nueva → P1 | C | núcleo | CU2 |
| H02 | Tolerancia de residuo absoluta para objetivos recta/segmento: raíces transversales perdidas a escala | nueva → P1 | C | núcleo | CU3 |
| H03 | Rotación de revisión en cada recálculo (también al arrastrar el conductor) | nueva → P1 | C | núcleo | CU1 |
| H04 | Margen escaso del presupuesto del índice métrico (≈ 21–23 k de 32 768 evaluaciones) | nueva → P2 | C | núcleo | CU1 |
| H05 | La memoria informa `HELD` para una referencia posterior al paso actual del protocolo | nueva → P3 | C | Desktop | CU5 |
| H06 | `Intersect(L, xAxis)` falla con un mensaje genérico de CAS | nueva → P3 | C | núcleo + mensajes | CU3 |
| H07 | Documentación desfasada (referencia matemática, API, roadmap, rutas de menú) | nueva → P2 | D | documentación | CU9 |
| H08 | La inserción renombra perdiendo subíndices (`α_{cyl}` → `α_{n}`) | nueva → P3 | C | Desktop | CU4 |
| H09 | «Aplicar estilos» incluye SplineV2 pero excluye LocusV2 | nueva → P3 | C | Desktop | CU4 |

### B.2 Fichas

Cada ficha resume: reproducción, causa (o hipótesis), dependencias, contrato,
complejidad y riesgo, prueba propuesta y criterio de aceptación. Las cifras
proceden del informe de evidencia.

#### B01 — Inserción de `.ggb` con ángulos libres

- **Reproducción:** `REJECTED_BEFORE_MUTATION` en documento vacío y en el modelo
  nativo. Mensaje: «these objects cannot be inserted: text4 (Text), α_C (Angle),
  α_{cyl} (Angle)». Ninguna de las siete variantes de bisección (grados,
  nombres, paréntesis, `slider`, `animation`, `angleStyle`) cambia el resultado.
- **Causa:** `GeoCeDGInsertionAdmission.classify` 241–248 = matriz `P4` (§2.2):
  solo ángulos de tres puntos y textos dependientes. La operación sigue siendo
  atómica y el mensaje es explícito. No hay error de reconstrucción.
- **Dependencias:** ninguna funcional. Comparte zona con el renombrado L1 (H08)
  y la verificación estricta, que compara valores pero no límites de deslizador.
- **Contrato:** ampliación de `P4` con filas nuevas y pruebas propias, según la
  regla de la especificación («A row is added only with its test»): ángulo libre
  con deslizador, animación y unidad; texto estático con posición absoluta o
  relativa; ángulos de otros algoritmos 2D si el autor lo decide (DC-1).
- **Complejidad / riesgo:** baja / medio. Riesgos: textos estáticos que nombran
  etiquetas renombradas; límites de deslizador que referencian objetos
  renombrados.
- **Prueba:** inserción del `.ggb` del autor en documento vacío y en su modelo;
  certificado del receptor; valores, límites, paso, animación y unidad de los
  ángulos; posición del texto; un solo paso de deshacer; guardar y reabrir.
- **Aceptación:** inserción completa y atómica, sin pérdida ni alteración del
  receptor. Los tipos aún no admitidos siguen rechazándose con mensaje explícito.

#### B02 — Espaciado inestable del protocolo

- **Reproducción:** con FlatLaf, tras cargar, las filas miden 36/40 px. Tras
  `updateComponentTreeUI`, pasan a 20 px uniformes, y la navegación las
  mantiene. Una edición o un deshacer vuelve a 36/40 px, y otro refresco de UI
  las devuelve a 20 px. La navegación por pasos, por sí sola, no altera los
  altos.
- **Causa:** `ConstructionTableDataD.updateAll()` fija altos por fila de «dos
  líneas»: tamaño×2 + 12, o +16 si hay `<sub>`. Ningún código fija el alto
  global, así que cada `updateUI` de FlatLaf (`Table.rowHeight = 20`) los borra.
  Hay refrescos de UI en el arranque, al abrir y al cambiar fuentes o temas.
  También se ha hallado un fallo upstream: el suelo de 33 px comprueba la
  columna «Name» (índice 1) en lugar de «ToolbarIcon».
- **Hipótesis sobre el disparador del autor:** una acción coincidente con la
  navegación (edición, deshacer, cambio de columnas) o algoritmos
  `ConstructionStep`. Se confirmará en la reproducción ventaneada de `CU0`.
- **Dependencias:** S4 (tamaño de fuente del protocolo) y temas P0.
- **Contrato:** corrección local, más una decisión menor sobre la altura
  uniforme deseada (DC-7).
- **Complejidad / riesgo:** baja / bajo. Cambio localizado en la vista heredada
  o en un gancho GeoCeDG.
- **Prueba:** altos de fila tras abrir, navegar, editar, deshacer, refrescar la
  UI, cambiar de tema y cambiar la fuente del protocolo. Captura en la ventana
  real.
- **Aceptación:** un único esquema de altos estable en todas esas transiciones,
  sin texto recortado. Distinto del orden del protocolo, que sigue en
  `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`.

#### B03 — Memoria de Paralela y Perpendicular

- **Reproducción:**
  - Al nivel del núcleo, la navegación por pasos y la creación en un paso
    intermedio conservan ambas memorias.
  - Deshacer, rehacer y la redefinición estructural de **cualquier** objeto
    vacían ambas (`RESET(clearView)`).
  - Cambiar de Paralela a Perpendicular devuelve la otra herramienta a `NORMAL`:
    la memoria se conserva, pero no se usa sin reactivar con Ctrl.
- **Causa:**
  - El vaciado es contractual: vigencia por instancia exacta y tabla de estados
    de `authoring-memory.md` §3.5, §4 y §7.2.
  - La «interferencia» es la regla de partida de la activación dual (§11.5).
  - Ambas ranuras son independientes en el código.
- **Hallazgo H05:** tras volver a un paso anterior a la creación de la
  referencia, la ranura sigue `HELD`.
- **Dependencias:** reproducción ventaneada del recorrido exacto del autor (`CU0`).
- **Contrato:** ampliación (DC-2). Opciones:
  1. Reasociación verificada tras reconstrucción: misma posición en el orden de
     construcción, mismo tipo, misma definición y misma etiqueta como
     corroboración, nunca como identidad.
  2. Comportamiento `MEMORY_ENABLED` persistente por herramienta durante la
     sesión.
  3. Indicador del estado de la ranura.
  4. Suspensión de referencias posteriores al paso actual (H05).
- **Complejidad / riesgo:** media / medio. Riesgo: reasociar un objeto distinto,
  algo que el contrato prohíbe hoy expresamente.
- **Prueba:** secuencias de alternancia de herramientas, deshacer, rehacer,
  redefinición, navegación por el protocolo e inserción en paso intermedio, con
  el arnés ventaneado.
- **Aceptación:** las dos memorias son independientes y se conservan en los
  casos que admita el contrato enmendado. Nunca queda una referencia obsoleta ni
  se reasocia una instancia que no corresponda.

#### B04 — Índices de soluciones de intersección

- **Hechos:** `Intersect(c, n)` crea las salidas en el orden que usa
  `Intersect(c, n, k)` en el estado actual, con continuidad activada o
  desactivada. El orden no es una identidad durable: puede cambiar por simetría,
  tangencia o heurística de continuidad.
- **Contrato:** funcionalidad de presentación nueva (Desktop) que lee el orden de
  salida del algoritmo. No cambia el núcleo.
- **Diseño orientativo:** insignias discretas «1», «2» junto a las soluciones al
  pasar el ratón o seleccionar el algoritmo, y vista previa en la herramienta
  Intersección. Texto que deje claro que es el orden actual del cálculo. No se
  aplica a resultados ricos Locus V2, que ya tienen marcadores por token.
- **Complejidad / riesgo:** media / bajo.
- **Prueba:** correspondencia insignia ↔ `Intersect(c, n, k)` en configuraciones
  dinámicas, en tangencia y con soluciones indefinidas.
- **Aceptación:** la insignia coincide siempre con el índice que usaría
  `Intersect(c, n, k)` en ese estado y desaparece cuando la solución no existe.

#### B05 — Etiquetado de Locus V2 y entidades GeoCeDG

- **Reproducción:** con `AlwaysOff`, `LocusV2`, `SplineV2` y el resultado
  métrico se crean con `labelVisible=true`. Los objetos ordinarios, el `Locus`
  clásico y los puntos semánticos siguen el estilo. El documento del autor lo
  muestra (`e_2` y `a_1` guardados con etiqueta visible).
- **Causa:** `GeoLocusV2` no invoca `setConstructionDefaults()`. No hay tipo
  por defecto `LOCUS_V2`. Cotas e `IsoABorder` fuerzan la ocultación por su
  cuenta.
- **Dependencias:** M01 (tipos por defecto de V2).
- **Contrato:** corrección. Precisar en `locus-v2-presentation.md` que la
  visibilidad inicial sigue el estilo de etiquetado al **crear** el objeto, nunca
  al cargarlo.
- **Complejidad / riesgo:** baja / bajo. No se tocan objetos existentes ni la
  carga de documentos.
- **Prueba:** matriz estilo × tipo GeoCeDG al crear; documentos antiguos
  inalterados al cargar.
- **Aceptación:** todas las entidades GeoCeDG respetan el estilo al crearse,
  salvo excepciones justificadas y registradas.

#### B06 — Rizado

- **Reproducción:** el defecto del punto medio es ∝ h² hasta 1e-14 (suave).
  Los polígonos dibujados tienen de 9 a 27 vértices, con giros de 38–54° y de
  157–180° en los repliegues de `w` y `e_1`. La desviación respecto a la curva
  densa queda dentro de 0,75 px, pero las facetas son visibles con trazo grueso
  (captura ×4).
- **Causa:** política adaptativa de `LocusRenderCache2D` (profundidad mínima 2,
  un solo punto medio, sin criterio angular).
- **Dependencias:** `CU1`, porque más muestras exigen evaluaciones baratas, y
  `CU2`, porque el mismo código decide fronteras y saltos.
- **Contrato:** enmienda técnica de `locus-v2-presentation.md` (DC-6). Solo
  presentación; las exportaciones heredan la política con su resolución.
- **Complejidad / riesgo:** baja / medio (coste de dibujo).
- **Prueba:** giro máximo entre segmentos consecutivos, desviación curva →
  polilínea e independencia de la semántica respecto al zoom.
- **Aceptación:** giro ≤ θ (propuesta: 10°) salvo en cúspides o repliegues
  establecidos; desviación ≤ 0,5 px; número de evaluaciones de dibujo acotado y
  registrado.

#### B07 y H01 — Huecos y desaparición de loci

- **Reproducción:**
  - Para `excCyl` entre 3,7 y 3,9, los loci `o`, `v`, `w` y `a_1` tienen
    subintervalos sin solución, pero siguen certificados `COMPLETE` con una
    componente `[-π, π)`.
  - El dibujo cae a 32 muestras uniformes y deja huecos de 5 a 22 px en cada
    frontera.
  - Con `excCyl` ≥ 4,0, las cuatro curvas desaparecen porque el trazador no
    existe en la posición actual de `N` (t = 0,646). Al mover `N` reaparecen.
- **Causa:**
  - `fallbackExistenceStructure` → `completeCompatibility`.
  - El dibujo recurre a muestreo uniforme sin localizar fronteras.
  - `isSemanticPublicationReady()` exige que el trazador esté definido en la
    posición actual del conductor.
- **Dependencias:** decisión arquitectónica DA-1; `CU1` (rendimiento).
- **Contrato:**
  - Conformidad con ADR 0027 y `locus-v2-existence-components.md` §9–§12.
  - Ampliación para no dejar sin dibujo, ni sin métrica, a los loci ordinarios
    fuera de la clase certificada (DA-1).
  - H01 es corrección de la definición L = ∪ F(Iⱼ \ Dⱼ).
- **Complejidad / riesgo:** alta / alto. Afecta a todos los consumidores:
  dibujo, métricas, intersecciones, `Point`/`Path` y DXF.
- **Prueba:** barrido de `excCyl` de 2,9 a 4,9 con el modelo del autor, a ambos
  lados de la transición de penetración a mordedura; posiciones del conductor
  dentro y fuera de la región válida; DXF y su anexo; guardar y reabrir; deshacer.
- **Aceptación:**
  - Ningún locus existente desaparece por la posición del conductor.
  - Cada componente se dibuja completa hasta su frontera localizada (≤ 1 px).
  - Ningún subtrayecto cruza una región sin solución.
  - Las componentes que deben estar separadas no se unen.
  - Las discontinuidades auténticas se conservan y se informan.

#### B08, B09, H02 y H06 — Materialización

- **Reproducción:**
  - Las tres soluciones pedidas son **tangencias** en t = 0 (doble cero del
    residuo). Clasificación `CONTACT_UNDETERMINED` o `TANGENT_ESTABLISHED`;
    identidad `NOT_ESTABLISHED`; no admisibles.
  - La raíz transversal de `e_1` ∩ `c_1` en la costura sí se materializa.
  - H02: raíces transversales de una circunferencia-locus contra recta o
    segmento: admisibles con r = 2; parcialmente con r = 20; ninguna con
    r = 200; desaparecen con r = 2000.
  - H06: `Intersect(L, xAxis)` falla con `CAS.GeneralErrorMessage`.
- **Causa:**
  - Contrato vigente: la materialización pública exige `TRANSVERSE_ESTABLISHED`.
    No interviene la completitud global, que ya no es veto (G8B-R1 opción B).
  - H02: umbral absoluto de residuo ≈ 4e-12 y corchete de parámetro de 1e-12
    para objetivos recta/segmento (`LocusIntersectionPolicy2D`,
    `LocusIntersectionTargets2D`).
  - H06: los ejes (`GeoAxis`) no son objetivos admitidos.
- **Dependencias:** `CU2`, porque las componentes alimentan el generador de
  candidatos; DA-3; DC-5.
- **Contrato:**
  - Tangencias: ampliación (DA-3).
  - H02: enmienda de la política normativa de tolerancias de
    `locus-v2-intersections.md` §8, con tolerancia relativa a la escala de la
    curva y del objetivo.
  - H06: admitir los ejes o dar un mensaje explícito.
- **Alternativa recomendada para los contactos** (DA-3 A): punto semántico por
  **correspondencia de conductor**. El parámetro canónico de un locus con
  conductor en una circunferencia es el parámetro de trayectoria de ese
  conductor, así que el punto del locus que corresponde a una posición
  construida del conductor es exacto y dinámico, sin búsqueda de raíces. En el
  modelo, la generatriz de contorno identifica esa posición.
- **Complejidad / riesgo:**
  - Punto estructural: media / bajo.
  - Materialización de tangencias: alta / alto (identidad ante desdoblamiento o
    desaparición).
  - H02: media / medio.
- **Prueba:**
  - Reconstruir `M_1` y `N_1` con procedencia semántica y comprobar que
    `LocusLength(o_1, ·, K_1)` reproduce 5,0265 (± tolerancia declarada) y sigue
    los cambios de `α_cone` y `rbCone`.
  - Barrido de escala de r = 2 a 2000 para las raíces transversales.
  - Persistencia, deshacer y copia.
- **Aceptación:**
  - El caso del autor queda resuelto sin `Point(o_1, text2, 0)` estático.
  - La admisibilidad transversal es invariante a la escala.
  - Ninguna identidad se apoya en coordenadas, proximidad u orden.

#### B10.1, H03 y H04 — Rendimiento de `e_2`

- **Reproducción:** ver tabla del informe. Lo esencial:
  - Arrastrar `E_1` cuesta ≈ 5,78 s por paso con `e_2` y 79 ms sin él.
  - Cada punto de `e_2` cuesta ≈ 43 000 evaluaciones internas.
  - La carga tarda 13 s; recrear `e_2`, 5,6 s.
- **Causa:**
  - Rotación de revisión (H03): `dependency-update=N` en la firma.
  - `updateConstruction` del corte por cada consulta puntual.
  - Copia anidada de `o_1` y su índice reconstruidos dos veces por muestra.
  - Sesión ignorada en la evaluación anidada.
  - Todo ello incumple `locus-v2-semantics.md`.
  - H04: el índice de `o_1` consume 21–23 k de 32 768 evaluaciones por llamada;
    un locus mayor agotaría el presupuesto y dejaría `d_2` indefinido.
- **Dependencias:** ninguna previa (cabeza del camino crítico).
- **Contrato:** conformidad. No hace falta decisión nueva salvo los umbrales de
  aceptación (DT-1).
- **Complejidad / riesgo:** alta / alto. Riesgo: devolver resultados obsoletos
  si la reutilización no respeta la vigencia de las dependencias.
- **Prueba:**
  - Contadores deterministas sobre el modelo del autor.
  - Igualdad de puntos de `e_2` (≤ 1e-12) frente a la referencia registrada en
    `CU0`.
  - Cambios de entradas internas (`rbCone`, `α_cone`) reflejados sin estado
    obsoleto.
  - Banco con y sin `e_2`.
- **Aceptación** (propuesta, DT-1):
  - Tras el calentamiento, 0 reconstrucciones del índice interno por evaluación
    externa.
  - Arrastrar solo el conductor no publica una revisión nueva de un locus cuyo
    contenido no cambia.
  - Una reconstrucción de corte por revisión.
  - Como referencia informativa, latencia de interacción con `e_2` ≤ 2× la
    latencia sin `e_2` (≤ 250 ms en la estación de referencia) y carga ≤ 3 s.

#### B10.2 — Cierre de `e_2`

- **Reproducción:** salto de 13,21 unidades en t = 0; costura ±π continua;
  componente periódica `COMPLETE`. El dibujo traza una cuerda horizontal a
  través del salto (captura).
- **Causa:**
  - La ruta de `LocusLength` sobre una rama periódica envuelve siempre la
    costura. Cuando el extremo `K_1`, ocurrencia del conductor, pasa por `N_1`,
    la longitud salta y ese hecho estructural no se publica.
  - El certificado de compatibilidad (B07).
  - El dibujo no tiene guarda de saltos.
- **Dependencias:** `CU1` y DA-1/DA-2.
- **Contrato:**
  - Las métricas publican la frontera estructural de envoltura: la posición del
    extremo fuente cuando el destino es una ocurrencia del mismo conductor.
  - El productor dependiente la consume como frontera de componente
    (precedencia estructural, R2-B01).
  - El dibujo nunca une a través de una discontinuidad establecida.
- **Complejidad / riesgo:** media / medio.
- **Prueba:** `e_2` publica componentes abiertas que terminan en t = 0⁺ y
  t = 0⁻; sin cuerda; longitud de cada tramo coherente con `o_1`.
- **Aceptación:** `e_2` se presenta como curva abierta y conserva la continuidad
  real en ±π.

#### B11 — Documentación de `LocusLength`

- **Hechos implementados:**
  - Formas: `LocusLength(L)`; `LocusLength(L, A, B)`, con política
    `forward`/`strict`/`zero-length` por defecto; y
    `LocusLength(L, A, B, dirección, frontera, misma posición)`.
  - Dirección: `forward` o `reverse`.
  - Frontera: `strict`, `stop-at-end` o `wrap-to-start`.
  - Misma posición: `zero-length` o `full-cycle`.
  - No existen `backward` ni `shortest`. La coincidencia distingue mayúsculas.
  - Un token inválido da `INVALID_QUERY`.
  - En una rama periódica de una sola componente, la ruta siempre envuelve la
    costura.
  - Las herramientas solo crean las formas por defecto.
- **Estado de la guía:** muestra solo marcadores de posición; ningún valor de
  token aparece en ninguna edición.
- **Contrato:** documentación de lo implementado, más corrección de documentos
  desfasados (H07).
- **Aceptación:** cada combinación implementada documentada con un ejemplo
  constructivo verificable (los valores de
  `PostG9U1A2ExplicitTraversalPolicyTest` sirven de base), con indicación de
  cuándo usar cada una. Nada diseñado y no implementado descrito como existente.

#### B12 — Revisión exhaustiva de la guía

- **Hechos:**
  - Guía bilingüe estructuralmente paralela (1 H1, 16 H2, 87 H3, 17 marcadores).
  - Densidad técnica alta: «semantic» 122 veces, «identity» 33, «admissib*» 28,
    «materializ*» 26, entre otras.
  - Rutas de menú incorrectas (submenús que son entradas planas; «Rotate around
    Point» por «Move around Point»).
  - Elementos heredados sin cubrir (Opciones, Edición, Ayuda).
  - Herramientas descritas con nombres que no coinciden con la aplicación.
  - Estructura fijada por pruebas: `PreG9BR5AGuideOutlineTest`,
    `PreG9BR5AGuideNavigationTest`, `PreG9BR6PlusGProductMetadataTest`,
    `PostP1BilingualUserGuideTest` y la comprobación DXF de `verify-g9x1`.
- **Estrategia:** sección E.
- **Aceptación:** matriz de cobertura completa y comprobada por una prueba
  (todas las acciones del perfil, herramientas, comandos y preferencias tienen
  ancla en la guía o exclusión justificada); ediciones ES y EN paralelas;
  ejemplos reproducibles; lenguaje de uso primero y detalle técnico al final.

#### M01 — Estilos por defecto persistentes

- **Hechos:**
  - «Guardar configuración» **no** guarda los objetos por defecto en GeoCeDG:
    `GeoGebraPortablePreferences` 242/286 solo lo hace si `!app.is3D()`, con un
    TODO upstream, y `AppGeoCeDG` es `App3D`.
  - La pestaña Predeterminados existe, heredada y sin filtrar, pero no tiene
    entradas Locus V2 ni SplineV2.
  - `SplineV2` y `LocusV2` caen en el valor por defecto de recta y nunca se
    aplica al crear (B05).
  - «Aplicar estilos» copia seis propiedades desde un documento (H09).
- **Contrato:**
  - Corrección: persistir los valores por defecto 2D en preferencias.
  - Ampliación (DC-3): tipos por defecto V2; herencia Locus → LocusV2;
    precedencia (preferencias personales < valores del documento < estilo del
    objeto).
  - Compatibilidad con documentos antiguos: sus valores por defecto prevalecen al
    abrirlos.
- **Complejidad / riesgo:** media / medio (formato del XML de valores por
  defecto; lectores antiguos).
- **Aceptación:** los estilos guardados se aplican a los objetos nuevos en
  sesiones posteriores. Ningún objeto existente cambia sin una operación
  explícita.

#### M02 — ESC en el menú compacto de estilos

- **Hechos:** las ventanas emergentes de la barra de estilo no son enfocables,
  así que Swing no instala ESC. ESC fuera del campo pasa a modo Mover. Al
  cerrarse, el menú **confirma** el valor del campo de grosor F4-A.
- **Contrato:** nuevo comportamiento (Desktop). Decisión menor: ESC cancela o
  confirma (DF-2).
- **Aceptación:** ESC cierra el menú sin cambiar de herramienta y sin otras
  cancelaciones. El valor queda según la decisión.

#### M03 — Libertad de colocación de etiquetas

- **Hechos:**
  - `GeoElement.MAX_LABEL_OFFSET = 80` px, recorte radial en `setLabelOffset`.
  - En segmentos, polígonos, rayos, vectores, cónicas y ángulos,
    `Drawable.addLabelOffset` **anula** el desplazamiento si la etiqueta sale de
    una banda, y la etiqueta salta a su posición por defecto.
  - Al cargar no se recorta.
  - Es la limitación editorial L12 del piloto del libro.
- **Contrato:** ampliación del comportamiento heredado (código upstream,
  cambio mínimo justificado). Decisión: nuevo límite y conducta fuera de la
  banda (DF-3).
- **Aceptación:** etiquetas desplazables hasta el nuevo límite sin saltos,
  asociadas al objeto, persistentes y sin efecto geométrico ni de exportación
  inesperado.

#### M04 — Orientación en Paralela y Perpendicular

- **Hechos:**
  - Las rutas `A6` (`AlgoLinePointLine`), `A7` y `A8` están admitidas, pero
    solo si la referencia también lo está.
  - En el `.ggb` de prueba, 27 de 27 rectas son admisibles.
  - En el modelo nativo, 22 de 41 están excluidas: todas son paralelas o
    perpendiculares cuya cadena de referencia nace en
    `g = OrthogonalLine(A, xAxis)`. Un eje está excluido (X10), y la exclusión se
    propaga (X8).
  - La indicación está desactivada por defecto y no existe en Gráficos 2.
- **Contrato:** ampliación (DC-4): admitir los ejes como entradas portadoras
  de orientación con su sentido del núcleo (+x, +y), sin mostrar indicación sobre
  los propios ejes.
- **Aceptación:** las 22 rectas excluidas del modelo muestran el sentido que
  define el núcleo. Las exclusiones restantes siguen fail-closed.

#### M05 y M05.1 — Controles

- **M05 (hechos):**
  - El núcleo tiene `Group` y la etiqueta XML `<group>`, sin interfaz Desktop.
  - Existe la posición absoluta en pantalla, pero no un contenedor visible ni el
    modo «vinculado al lienzo».
  - GeoCeDG reasigna Ctrl+Shift+D (fijar deslizador en upstream) a exportar DXF.
- **M05 (contrato):** funcionalidad nueva con persistencia documental: nuevo
  elemento serializado y compatibilidad con lectores antiguos. Requiere diseño
  (DA-6).
- **M05.1 (reproducción):**
  - Con estilo efectivo `AlwaysOff` («Automático» con la vista algebraica oculta,
    o «No New Objects»), un número escrito en la entrada y mostrado como
    deslizador con nombre + valor pasa a **solo valor** tras deshacer o recargar.
  - Un deslizador creado con `Slider()` lo conserva.
  - Con `AlwaysOn` se conserva.
- **M05.1 (causa):** `XMLBuilder` omite `<labelMode>` para números con
  definición en modo `NAME_VALUE`. Al recargar, el valor por defecto
  `LABEL_DEFAULT` se resuelve como VALUE bajo `AlwaysOff`.
- **M05.1 (contrato):** corrección (serializar siempre el modo elegido, o no
  resolverlo al cargar); compatible con lectores antiguos.
- **Aceptación de M05.1:** el formato elegido sobrevive a deshacer, rehacer,
  abrir, insertar y copiar.

#### M06 — Buscador en la vista algebraica

- **Hechos:** no existe filtro ni búsqueda (solo ordenaciones y alternancia de
  auxiliares).
- **Contrato:** funcionalidad nueva de navegación (Desktop) que opera sobre
  nombres visibles, sin identidad.
- **Decisión:** coincidencia parcial o exacta; filtrar o saltar; subíndices
  (DF-4).
- **Aceptación:** búsqueda con subíndices y caracteres especiales; selección y
  desplazamiento al objeto; respuesta interactiva con más de 1000 objetos.

#### M07 — Nombres desde el lienzo en los diálogos

- **Hechos:**
  - `InputDialogD` inserta « etiqueta » solo en diálogos no modales: Redefinir,
    Renombrar, Texto, Casilla, Crear herramienta.
  - Los diálogos de herramienta (Radio, Ángulo dado, Rotar, Dilatar, Segmento
    dado, Polígono regular) desactivan el oyente.
  - Todos los diálogos GeoCeDG son `JOptionPane` modales sin inserción.
- **Contrato:** ampliación (Desktop). Decidir qué diálogos y con qué reglas de
  posición del cursor y escape (DF-5).
- **Aceptación:** inserción en la posición del cursor sin cerrar el diálogo, sin
  perder el texto ni modificar la construcción. ESC cancela solo el diálogo.

#### M08 — `PointOnLocusV2`

- **Hechos:**
  - R6 ofrece una consulta transitoria por distancia con candidatos y selección
    explícita; tras seleccionar, la dirección semántica queda fija.
  - No existe comando ni variante dinámica: el ADR 0019 (decisión 10) lo
    descartó.
  - `Point(L, rama, parámetro)` existe con parámetro numérico.
  - La unicidad global exige cobertura completa.
- **Contrato:** nueva capacidad de núcleo. Requiere ADR que enmiende o supere
  la decisión 10 del ADR 0019 (DA-4).
- **Propuesta para discusión:**
  - Modo estático (crea una dirección fija si hay **un** candidato dentro de la
    bola de tolerancia; si no, rechazo explícito).
  - Modo dinámico (reresuelve por continuación en la misma rama y componente;
    indefinido con diagnóstico ante ambigüedad).
  - Distinción entre pertenencia, proximidad y proyección.
  - La tolerancia nunca es identidad.
- **Aceptación:** sin selección silenciosa de rama, sin ciclos en el DAG y con
  resultados explícitos en los casos ambiguos.

#### M09 — Redefinir hacia una intersección Locus V2

- **Hechos:**
  - Hoy, redefinir un punto de `AlgoIntersectLineConic` como
    `Intersect(R, token)` no permite RETAIN.
  - Solo hay reemplazo heredado con modo explícito, que retira la identidad.
  - `Intersect(L, obj)` se rechaza por cambio de familia.
  - El ADR 0026 prohíbe transportar continuidad por etiquetas, coordenadas o
    proximidad.
- **Contrato:** nueva ampliación de redefinición (DA-5). La igualdad de
  coordenadas puede ser condición de admisión, nunca prueba de identidad.
- **Aceptación:** operación atómica que conserva la construcción cuando no puede
  completarse y deja una identidad resultante explícita.

#### M10 — Información complementaria en la vista algebraica

- **Hechos:**
  - Ya existe información emergente con `getLongDescriptionHTML(true, true)`
    («tipo etiqueta: definición descripción»). No es complementaria al modo
    mostrado.
  - Modos Desktop: Valor, Descripción y Definición (`AlgebraSettings`).
- **Contrato:** ampliación Desktop del contenido de esa información emergente.
- **Aceptación:**
  - Si se muestra Definición o Descripción, la información emergente muestra el
    Valor.
  - Si se muestra Valor, muestra la Definición.
  - Valores largos truncados con criterio y textos localizados.
  - Objetos sin valor definido indicados explícitamente.

## C. Análisis de dependencias

### C.1 Causas comunes

| Grupo causal | Incidencias | Mecanismo común |
|---|---|---|
| K1 — certificado de existencia por compatibilidad | B07, B10.2, H01 y, a través de las componentes, B08/B09 | el productor dependiente afirma `COMPLETE` y periodicidad sin prueba, y la publicación depende de la posición actual del conductor |
| K2 — rotación de revisión y recálculo por consulta | B10.1, H03, H04 | firmas con contador; `updateConstruction` por punto; copia anidada y métrica reconstruidas |
| K3 — política de dibujo de Locus V2 | B06, B07 (parte de dibujo), B10.2 (cuerda) | profundidad mínima 2, retroceso uniforme, sin localización de fronteras ni guarda de saltos |
| K4 — puntos sobre Locus V2 en posiciones estructurales | B08, B09, M08, M09 | tangencias no materializables; sin punto por correspondencia de conductor; sin resolución por proximidad dinámica; sin redefinición hacia V2 |
| K5 — valores por defecto y etiquetado al crear o recargar | B05, M01, M05.1, H09 | las entidades V2 no aplican valores por defecto; el estilo `AlwaysOff` se aplica al recargar números; preferencias no persistidas en `App3D` |
| K6 — matriz de inserción | B01, H08 | filas `P4` conservadoras; renombrado L1 |
| K7 — estado de sesión y refresco Swing | B02, B03, H05 | refrescos de UI que borran estado de la vista; vigencia por instancia exacta y activación dual |

### C.2 Independencia y naturaleza

- **Independientes entre sí:** B04, M02, M03, M06, M07 y M10 (interacción);
  M04 (contrato F1); B02 y B03 (interfaz); B01 (inserción).
- **Correcciones contra contrato vigente:** B02, B05, B06, B07, B10.1, B10.2,
  M05.1, H01–H06, H08 y H09.
- **Ampliaciones de contrato:** B01, B03, B08/B09 (tangencias o punto
  estructural), M01 (tipos V2 y precedencia), M03, M04, M07, M10 y H02 (política
  de tolerancia).
- **Funcionalidades nuevas:** B04, M02, M05, M06, M08 y M09.

### C.3 Bloqueos

1. `CU1` debe preceder a `CU2`: localizar fronteras, detectar saltos y teselar
   con más muestras multiplicaría el coste actual de `e_2`.
2. `CU2` debe preceder a `CU3`: los candidatos de intersección se generan sobre
   las componentes certificadas, y DA-1 cambia esas componentes.
3. `CU3` debe preceder a `CU7`: M08/M09 reutilizan el punto estructural y la
   política de materialización que fije `CU3`.
4. La documentación final (`CU9`) va después de los cambios funcionales.
   Su auditoría inicial (matriz de cobertura base) se hace en `CU0`.
5. `CU10` requiere todos los paquetes autorizados cerrados o diferidos
   expresamente.

### C.4 Paralelismo

Tras `CU0` pueden avanzar en paralelo, porque tocan zonas de código disjuntas:

- carril núcleo Locus V2: `CU1` → `CU2` → `CU3` → `CU7`;
- carril valores por defecto e inserción: `CU4`. Comparte con `CU2` solo
  `GeoLocusV2` (constructor), así que conviene coordinar la fusión;
- carril interfaz: `CU5` → `CU6` (ambos tocan el controlador Desktop y la vista
  algebraica);
- carril controles: `CU8`, tras aprobar su diseño.

### C.5 Secuencia mínima

```text
CU0 ──► CU1 ──► CU2 ──► CU3 ──► CU7 ─┐
  ├──► CU4 ───────────────────────────┤
  ├──► CU5 ──► CU6 ──────────────────┼──► CU9 ──► CU10 ──► (libro: fase de base técnica)
  └──► CU8 (diseño ► implementación) ┘
```

Para reducir repeticiones de verificación: cada paquete ejecuta solo su `PHASE`
registrado (y, si su clase lo exige, `INTEGRATION`) sobre su propio commit, y
`FINAL` se ejecuta **una vez** sobre el candidato integrado del cierre. Si el
autor lo prefiere, `CU5` y `CU6` pueden entregarse como un único candidato con
una sola campaña `INTEGRATION`.

## D. Plan de ejecución recomendado

Clases de verificación (planificación, según el registro vivo):
`BOUNDED_PHASE` → `PHASE` registrado; `INTEGRATED_PHASE` → `PHASE` +
`INTEGRATION`; `GLOBAL_IMPACT` → `FINAL`. Cada paquete registra su selección
`PHASE` en `verification-registry.json`, y `INFRA_UNIT` cubre ese cambio. Los
tiempos son informativos; las puertas de rendimiento usan contadores
deterministas.

### CU0 — Entrada, línea base reproducible y paquete de decisiones

- **Objetivo:** convertir la evidencia de esta ejecución en base versionada y
  reproducible, y fijar las decisiones contractuales antes del código.
- **Alcance:**
  1. Copias byte a byte de los dos archivos del autor en
     `models/regression/pre-g9b-cu/original/`, con manifiesto (procedencia,
     SHA-256, derechos y propósito). Los originales no se tocan. Requiere
     autorización.
  2. Banco reproducible de composición anidada (con y sin `e_2`). Contadores
     deterministas y tiempos informativos, mediante un descriptor en
     `benchmarks/suites/` y el arnés de `tools/benchmark/`.
  3. Puntos de referencia de `e_2`, `o_1` y `w_1` registrados para comprobar
     después la igualdad semántica.
  4. Reproducción ventaneada de B02 y del recorrido gráfico de B03 (arnés con
     `GeoCeDG.main` y ajustes aislados), con comparación frente a Classic
     diagnóstico.
  5. Matriz de cobertura base de la guía (B12).
  6. Hoja de decisiones autorales (sección G) y, para las decisiones aceptadas,
     los borradores de enmienda normativa: ADR 0027 y especificación de
     existencia; presentación; tolerancias de intersección; `P4`; memoria de
     autoría; indicación de orientación.
  7. Registro estructurado de las incidencias B, M y H; registro en el roadmap
     del track como planificado y de la publicación de R7 y `48a0775e`.
- **Fuera de alcance:** cualquier cambio de producto.
- **Prerrequisitos:** aprobación de este plan y autorización de `CU0` con
  commit base.
- **Entregables:** fixtures y manifiesto; banco y resultados base (fuera de Git);
  informe ventaneado; hoja de decisiones; borradores normativos; registro de
  deudas.
- **Capa:** documentación, validación y banco.
- **Verificación:** `STATIC`; `INFRA_UNIT` si se registran selecciones.
- **Puerta:** decisiones autorales tomadas y línea base aceptada.
- **Siguiente:** habilita `CU1`, `CU4`, `CU5` y el diseño de `CU8`.

### CU1 — Locus V2: estabilidad de revisión y composición anidada (B10.1, H03, H04)

- **Objetivo:** que `e_2` y cualquier Locus V2 que dependa de métricas o de otros
  Locus V2 se evalúe y actualice sin bloquear la aplicación, conforme a
  `locus-v2-semantics.md`.
- **Alcance:**
  - Revisión semántica por contenido: sin contador forzado; mover solo el
    conductor no republica.
  - Reconstrucción del corte una vez por revisión y nunca por consulta puntual.
  - Copias anidadas con revisión estable mientras no cambien sus entradas; índice
    métrico interno reutilizado entre muestras externas.
  - Propagación de la sesión (memoización, ciclos y presupuesto) en la
    evaluación anidada.
  - Contadores muertos conectados y contadores anidados.
  - Revisión del margen del presupuesto métrico (H04) con fallo explícito.
- **Fuera de alcance:** certificado de existencia y dibujo (`CU2`), aplanado
  general del DAG y nuevas capacidades métricas.
- **Prerrequisitos:** `CU0` (banco y referencia).
- **Entregables:** cambio de núcleo GeoCeDG (`AlgoLocusV2`,
  `AlgoDependentPointLocusV2`, `ReconstructibleLocusEvaluator2D`, propietario
  métrico); pruebas focales y de regresión; informe de banco antes y después.
- **Capa:** núcleo compartido, código propio de GeoCeDG.
- **Verificación:** `INTEGRATED_PHASE`: `PHASE -Phase PRE-G9B-CU1` +
  `INTEGRATION`; smoke autoral de carga y arrastre con `e_2`.
- **Puerta:** criterios de B10.1 y conjunto de regresión Locus V2 G6–G9U0
  completo.
- **Siguiente:** `CU2`.

### CU2 — Locus V2: existencia, continuidad y presentación (B07, B10.2, H01, B06)

- **Objetivo:** que la topología publicada y dibujada sea fiel a la geometría:
  sin uniones artificiales, sin cierres no probados, sin desapariciones y sin
  facetas.
- **Alcance (etapa a, núcleo):**
  - Sustituir el certificado de compatibilidad según DA-1.
  - Publicación independiente de la definición del trazador en la posición
    actual del conductor (H01).
  - Hechos estructurales de envoltura publicados por las métricas y consumidos
    por el productor dependiente (DA-2).
  - Política de consumidores revisada: métricas, intersecciones, `Point`/`Path`
    y DXF con su anexo.
- **Alcance (etapa b, dibujo):**
  - Dibujo por componente hasta la frontera localizada, sin retroceso uniforme.
  - Ningún trazo a través de discontinuidades.
  - Cierre solo con continuidad periódica establecida.
  - Teselación con criterio angular y profundidad mínima (B06, DC-6).
- **Fuera de alcance:** materialización (`CU3`) y ampliar la clase de
  construcción certificada más allá de lo que fije DA-1.
- **Prerrequisitos:** `CU1`; DA-1, DA-2 y DC-6 decididas.
- **Entregables:** enmienda normativa aprobada; cambio de núcleo y de dibujo;
  pruebas de barrido de `excCyl`, de posición del conductor, de `e_2` abierto,
  DXF y persistencia; capturas deterministas.
- **Capa:** núcleo + dibujo compartido.
- **Verificación:** `INTEGRATED_PHASE` (incluye regresiones G9X1/S1 de DXF);
  smoke visual autoral con el modelo.
- **Puerta:** criterios de B06, B07, H01 y B10.2.
- **Siguiente:** `CU3`.

### CU3 — Locus V2: materialización y puntos estructurales (B08, B09, H02, H06)

- **Objetivo:** que el autor obtenga directamente, con identidad y propagación
  correctas, los puntos de contacto que hoy construye a mano, y que la
  admisibilidad de raíces transversales no dependa de la escala.
- **Alcance:**
  - Opción de DA-3: punto por correspondencia de conductor y, si se decide,
    clase de contacto tangente.
  - Política de tolerancia relativa a la escala (DC-5).
  - Ejes como objetivos o mensaje explícito (H06).
  - Mensaje de la herramienta que explique la tangencia y ofrezca la
    alternativa.
- **Fuera de alcance:** M08/M09 (`CU7`).
- **Prerrequisitos:** `CU2`; DA-3 y DC-5.
- **Capa:** núcleo + herramienta Desktop.
- **Verificación:** `INTEGRATED_PHASE`.
- **Puerta:** criterios de B08, B09 y H02 sobre el modelo del autor y el
  barrido de escala.
- **Siguiente:** `CU7`.

### CU4 — Valores por defecto, etiquetado e inserción (B05, M01, M05.1, B01, H08, H09)

- **Objetivo:** que las entidades GeoCeDG y los deslizadores respeten las
  preferencias de etiquetado y estilo del usuario, y que la inserción admita las
  construcciones angulares habituales.
- **Alcance:**
  - Valores por defecto y estilo de etiquetado al crear entidades V2 (B05).
  - Tipos por defecto V2 y persistencia de preferencias (M01, DC-3).
  - Serialización estable del modo de etiqueta (M05.1).
  - Filas nuevas de `P4` y verificación de límites de deslizador (B01, DC-1).
  - Renombrado que conserve subíndices (H08), si se decide.
  - Coherencia de «Aplicar estilos» (H09).
- **Fuera de alcance:** el resto de `DEFER-F3-*` (macros M2, scripts, 3D,
  exportación forense).
- **Prerrequisitos:** `CU0`; DC-1 y DC-3.
- **Capa:** núcleo heredado (cambio mínimo justificado en `ConstructionDefaults`
  y `XMLBuilder`), núcleo GeoCeDG y Desktop.
- **Verificación:** `INTEGRATED_PHASE`; viaje de ida y vuelta `.cedg`/`.ggb`.
- **Puerta:** criterios de B01, B05, M01 y M05.1.
- **Siguiente:** ninguno bloqueado; alimenta `CU9`.

### CU5 — Flujo de construcción en la interfaz (B02, B03, M04, H05)

- **Objetivo:** corregir las conductas de interfaz que interrumpen la autoría.
- **Alcance:**
  - Política de alto de filas estable del protocolo (B02, DC-7).
  - Enmienda de memoria y activación (B03, DC-2) y suspensión por paso (H05).
  - Ejes como entradas portadoras de orientación (M04, DC-4).
- **Prerrequisitos:** `CU0` (reproducción ventaneada); DC-2, DC-4 y DC-7.
- **Capa:** Desktop, con cambio mínimo en la vista heredada si hace falta.
- **Verificación:** `INTEGRATED_PHASE` si se toca código upstream; si no,
  `BOUNDED_PHASE`. Smoke ventaneado.
- **Siguiente:** `CU6`.

### CU6 — Mejoras de interacción y presentación (B04, M02, M03, M06, M07, M10)

- **Objetivo:** mejoras acotadas y verificables de interacción.
- **Alcance:**
  - Insignias de índice (B04).
  - ESC en el menú compacto (M02).
  - Nuevo límite de desplazamiento de etiquetas (M03).
  - Buscador algebraico (M06).
  - Inserción de nombres desde el lienzo en los diálogos decididos (M07).
  - Información complementaria en la vista algebraica (M10).
- **Prerrequisitos:** `CU5`; DF-1 a DF-6.
- **Capa:** Desktop; M03 toca código heredado del núcleo y del dibujo (cambio
  mínimo).
- **Verificación:** `INTEGRATED_PHASE`.

### CU7 — Puntos sobre Locus V2 por proximidad y redefinición (M08, M09)

- **Objetivo:** nuevas capacidades semánticas con contrato propio.
- **Etapas:**
  - `CU7-D`: diseño, ADR y especificación (DA-4, DA-5), con su propia aprobación.
  - `CU7-I`: implementación.
- **Prerrequisitos:** `CU3`.
- **Capa:** núcleo + herramientas.
- **Verificación:** `CU7-D` → `STATIC`; `CU7-I` → `INTEGRATED_PHASE`.

### CU8 — Contenedores de controles (M05)

- **Objetivo:** agrupar botones, casillas, deslizadores y campos de entrada en
  contenedores visibles, fijos en pantalla o vinculados al lienzo.
- **Etapas:**
  - `CU8-D`: diseño (persistencia, compatibilidad, edición).
  - `CU8-I`: implementación.
- **Alternativa:** diferir `CU8-I` a un track posterior si el diseño muestra un
  impacto de formato no deseable.
- **Capa:** frontend + modelo de documento (no geometría).
- **Verificación:** `CU8-D` → `STATIC`; `CU8-I` → `INTEGRATED_PHASE`, con su
  impacto global cubierto por el `FINAL` del cierre.

### CU9 — Documentación (B11, B12, H07)

- **Objetivo:** guía completa, correcta y accesible del producto resultante.
- **Alcance:** estrategia E completa; actualización coordinada de las pruebas
  que fijan la estructura de la guía; corrección de documentos desfasados (H07).
- **Prerrequisitos:** todos los paquetes funcionales autorizados.
- **Verificación:** `INTEGRATED_PHASE` (`PHASE -Phase PRE-G9B-R5-A` para la
  estructura de la guía + `INTEGRATION`, que ejecuta las pruebas Desktop que la
  fijan).

### CU10 — Cierre integrado del track

- **Objetivo:** candidato final integrado, verificado y aprobado.
- **Alcance:**
  - Congelar el candidato integrado.
  - Ejecutar `FINAL` una vez (`GLOBAL_IMPACT`).
  - Validación dinámica con los modelos del autor (barridos de B07, B10 con y
    sin `e_2`, materialización y continuidad de B08/B09, inserción de B01).
  - Smoke autoral, informe de rendimiento y cierre de deudas.
  - Informe de limitaciones restantes.
  - Versión del producto (propuesta 1.2.0, a decidir) y, si se autoriza,
    publicación con etiqueta anotada inmutable nueva (propuesta:
    `geocedg-pre-g9b-cu-pass`).
- **Siguiente:** fase editorial de actualización de la base técnica (sección F).

## E. Estrategia de documentación (B11, B12)

1. **Autoridad sin duplicación.** Las especificaciones siguen siendo la única
   autoridad técnica. La guía describe el uso observable y remite a ellas solo
   donde aporta. Los detalles internos (tokens, certificados, clases, fases) se
   retiran del texto de uso o se trasladan a un apartado avanzado claramente
   señalado o a la documentación de desarrollo.
2. **Inventario dirigido por el producto.** Una matriz de cobertura
   legible por máquina (propuesta:
   `geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json`) enumera:
   - las 133 acciones del perfil (`apps/geocedg/application-profile.yml`);
   - las herramientas de la barra y sus menús desplegables;
   - los comandos GeoCeDG y heredados expuestos;
   - las preferencias (incluidas «Disposición y presentación», unidades,
     orientación y pestañas heredadas);
   - las operaciones de documento.

   Cada entrada lleva su ancla de la guía o una exclusión justificada. Una
   prueba comprueba que ninguna entrada del perfil queda sin ancla o sin
   exclusión, y que las anclas existen en ES y EN.
3. **Estructura.** Primero uso: tareas y flujos con ejemplos reproducibles.
   Después referencia (comandos, sintaxis y opciones, incluido B11 completo). Al
   final, conceptos avanzados (Locus V2 semántico, existencia, métricas,
   intersecciones). Cada ejemplo procede de una construcción versionada y
   comprobada por prueba, como ya hace el ejemplo de §7.5/§9.3.
4. **Contrato estructural.** El esqueleto actual (1/16/87, 17 marcadores, anclas y
   literales fijados) se cambia de forma deliberada, con actualización
   coordinada de las pruebas que lo fijan y de `documentation-maintenance.md`
   §10 si cambian sus reglas.
5. **Paralelismo bilingüe.** ES y EN se mantienen estructuralmente idénticos
   (`PreG9BR5AGuideOutlineTest` y diagnóstico de estructura). La terminología se
   normaliza en un glosario breve dentro de la guía.
6. **Integración en la aplicación.** Se mantienen Ayuda → guía, la navegación por
   marcadores y el renderizador Markdown limitado. No se añaden imágenes ni
   listas anidadas mientras el renderizador no las soporte
   (`OBS-R5A-LATENT-NESTED-LIST-CONTINUATION`).
7. **Calendario.** Matriz base en `CU0`; actualización `GUIDE_IMPACT` en cada
   paquete funcional; consolidación y reescritura en `CU9`.

## F. Impacto editorial (`geocedg_book`)

- **Estado actual:** rama local `book/figpol-pilot-2026-10-18` = `origin/main` =
  `78fbc88` (BOOK-FIGPOL-PILOT publicado). Árbol limpio. La rama local `main`
  está 5 commits por detrás, sin contenido divergente. Las salidas generadas del
  piloto son ignoradas por diseño y se conservan. El puente informa `ALIGNED`
  con la base editorial aceptada `372da89f`. No hay discrepancia entre el piloto
  local y el publicado.
- **Durante el track:** sin cambios en el libro, sus fases, capítulos, figuras,
  modelos o bases.
- **Impacto previsto:**
  - Mínimo y favorable. B06, B07 y B10.2 condicionan la producción futura de
    figuras con curvas Locus V2: el piloto dejó «curves and Locus V2» (L8) sin
    ejercer.
  - M03 aborda la limitación L12 (colocación de etiquetas de segmentos).
  - Ningún cambio exige reorganizar el plan editorial.
- **Después del cierre aprobado y publicado de `CU10`:**
  1. Identificar el nuevo commit y la nueva etiqueta anotada; la etiqueta
     histórica no se mueve.
  2. Ejecutar `tools/book/book-worktree.ps1 -Action BaselineCandidate`.
  3. Una fase BOOK autorizada actualiza el registro vigente de
     `TECHNICAL_BASELINES.json`, conserva intactos los históricos y revisa el
     mapeo afectado.
  4. Comprobar `Alignment` = `ALIGNED`.
  5. Añadir referencias mínimas a funciones nuevas o corregidas solo donde
     tengan efecto material.
  6. No iniciar ninguna fase de redacción.

## G. Decisiones del autor

Solo se recogen las cuestiones que exigen intervención autoral. Para cada una se
indica la recomendación.

### G.1 Arquitectónicas

| ID | Decisión | Opciones | Recomendación |
|---|---|---|---|
| DA-1 | Evidencia de existencia de loci dependientes fuera de la clase certificada (B07, B10.2) | (A) nivel explícito «evaluado, no certificado»: escaneo acotado, localización de fronteras por bisección, detección de saltos, etiqueta visible; consumidores definidos uno a uno. (B) `NOT_ESTABLISHED` estricto y ampliación progresiva de la clase certificada. (C) mantener la compatibilidad añadiendo solo fronteras estructurales | **A**, junto con DA-2 y, a largo plazo, B. Con B sola, los loci ordinarios quedarían sin dibujo y sin `LocusLength` |
| DA-2 | Hechos estructurales de discontinuidad publicados por las métricas (envoltura de ruta) y guarda de saltos en el dibujo | adoptar o no | adoptar |
| DA-3 | Contactos tangentes (B08, B09) | (A) punto por correspondencia de conductor. (B) clase de materialización de contacto tangente. (C) ambas | **A** en `CU3`; B opcional y posterior |
| DA-4 | Contrato de `PointOnLocusV2` (M08) | una o dos operaciones; estática o dinámica; bola de tolerancia; política de ambigüedad. Enmienda del ADR 0019 | dos operaciones explícitas, sin selección silenciosa de rama |
| DA-5 | Identidad al redefinir hacia una intersección V2 (M09) | conservar la identidad con condiciones, o reemplazo explícito | diseño en `CU7-D` |
| DA-6 | Alcance de los contenedores (M05) | dentro del track (diseño + implementación) o solo diseño | solo diseño en este track, salvo preferencia autoral |

### G.2 Ampliaciones de contrato

| ID | Decisión | Recomendación |
|---|---|---|
| DC-1 | Filas `P4` nuevas (B01): ángulo libre con deslizador; texto estático; otros ángulos 2D | ángulo libre y texto estático; otros ángulos solo con prueba propia |
| DC-2 | Memoria de autoría (B03): reasociación verificada tras reconstrucción; activación persistente; indicador; suspensión por paso | activación persistente + indicador + suspensión por paso; reasociación solo con correspondencia verificada |
| DC-3 | Valores por defecto (B05, M01, M05.1): tipos V2; herencia Locus → LocusV2; precedencia; persistencia en preferencias | tipos propios V2 inicializados desde Locus; precedencia preferencias < documento < objeto |
| DC-4 | Orientación (M04): ejes como entradas portadoras | admitir |
| DC-5 | Tolerancias de intersección (H02): residuo relativo a la escala | admitir, con valores en la especificación |
| DC-6 | Teselación (B06): criterio angular y profundidad mínima | 10° y profundidad ≥ 4, a confirmar con el banco |
| DC-7 | Alto de filas del protocolo (B02) | altura uniforme estable, dos líneas o compacta según preferencia del autor |

### G.3 Alternativas funcionales

DF-1: forma de las insignias de B04 (al pasar el ratón o persistentes).
DF-2: ESC confirma o cancela en el menú compacto (M02).
DF-3: nuevo límite y conducta de las etiquetas (M03).
DF-4: conducta del buscador (M06).
DF-5: diálogos en los que se inserta el nombre (M07).
DF-6: contenido y formato de la información emergente (M10).

### G.4 Tolerancias y criterios de aceptación

| ID | Criterio |
|---|---|
| DT-1 | Puertas de B10.1 por contadores, y umbrales informativos de latencia y carga |
| DT-2 | Precisión de las fronteras dibujadas (B07): ≤ 1 px |
| DT-3 | Giro máximo y desviación de dibujo (B06): 10° y 0,5 px |
| DT-4 | Intervalo de escala del barrido de H02: radios de 2 a 2000 |
| DT-5 | Criterio de completitud de la matriz de cobertura (B12) |

### G.5 Autorizaciones

1. Aprobar el identificador `PRE-G9B-CU` y este plan.
2. Autorizar `CU0` con commit base exacto (recomendado: `48a0775e` o el commit
   que registre este plan).
3. Autorizar la incorporación de las copias de los archivos del autor al corpus
   de regresión.
4. Autorizar después cada paquete por separado.
5. Al cierre: versión, publicación y etiqueta, y fase editorial de base técnica.

### G.6 Cuestiones ya resueltas por contratos aprobados (no son decisiones nuevas)

- B10.1 es conformidad con `locus-v2-semantics.md`; solo DT-1 requiere decisión.
- Los productores dependientes ya deben fallar cerrados (ADR 0027). DA-1 decide
  solo cómo mantener la usabilidad.
- La admisibilidad local no exige completitud global (G8B-R1 opción B): B08/B09
  no están bloqueadas por completitud.
- Las tangencias no se materializan con el contrato vigente: solo hace falta
  decisión si se desea ampliarlo.
- El vaciado de la memoria al deshacer, rehacer o reconstruir es contractual
  (F2): solo hace falta decisión si se desea ampliarlo.
- La exclusión de ángulos libres y textos estáticos en la inserción es
  contractual (`P4`): solo hace falta decisión para ampliarla.

## Anexo. Correspondencia con el encargo

| Requisito del encargo | Sección |
|---|---|
| §1 Inspección de estado, autoridad y deudas | A.1–A.4 |
| §1.2 Tratamiento de archivos del autor | A.5; informe §2 |
| §2 B01–B12 | B.1–B.2 |
| §3 M01–M10 | B.1–B.2 |
| §4 Criterios arquitectónicos | capa de cada ficha; C; D |
| §5 Organización por grupos | C.4–C.5; D (grupo A = `CU1`–`CU3`; grupo B = `CU4`–`CU5`; grupo C = `CU6`; grupo D = `CU7`–`CU8`; grupo E = `CU9`; grupo F = `CU10`) |
| §6 Libro | F |
| §7 Validación y cierre | puertas de D; `CU10` |
| §8 A–G | A–G |

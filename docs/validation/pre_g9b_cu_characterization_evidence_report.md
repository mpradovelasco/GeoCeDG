# PRE-G9B-CU — Informe de caracterización y evidencia de reproducción

| Campo | Valor |
|---|---|
| Carácter | Evidencia de caracterización de la primera ejecución del encargo «GeoCeDG — Corrective and Usability Track» (aportación autoral de 2026-10-19). No es autoridad normativa, no autoriza implementación y no modifica el producto |
| Estado | `CHARACTERIZATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Plan asociado | [plan candidato PRE-G9B-CU](../architecture/pre_g9b_cu_corrective_usability_plan_candidate.md) |
| Base inspeccionada | `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`, árbol `75ebf03f089a5effa02daa4a68be360b7499fdd9`, árbol de trabajo limpio |
| Fecha | 2026-10-19 |
| Entorno de las sondas | Windows 11 Pro 10.0.26300, Intel i7-13700F, 31,8 GB; JVM de pruebas Desktop Java 17.0.10 (el producto empaquetado usa Temurin 25); Gradle `:desktop:desktop:test` con `--init-script` desde el scratchpad de la sesión |

## 1. Método y límites

Toda la reproducción se ha hecho con sondas JUnit temporales situadas fuera del
árbol versionado (scratchpad de la sesión) e inyectadas mediante un
`--init-script` de Gradle en el conjunto de pruebas Desktop. Las sondas usan el
host embebido `G9U1TestApp`, cargan **copias de trabajo** de los archivos del
autor y ejecutan las operaciones del núcleo en el hilo de eventos (el
propietario métrico de Locus V2 está confinado a su hilo creador; una sonda que
evalúa desde otro hilo recibe `IllegalStateException: Metric owner is confined
to its creating kernel thread`).

Límites declarados:

- Las sondas son evidencia de caracterización, no pruebas comprometidas. El
  paquete `CU0` del plan las convierte en fixtures, pruebas y un banco de
  rendimiento reproducibles dentro del repositorio.
- Los tiempos son informativos (una estación, JVM de pruebas Java 17). Las
  conclusiones de rendimiento se apoyan en contadores deterministas de trabajo
  (evaluaciones, reconstrucciones de índice, revisiones), que no dependen de la
  máquina.
- La navegación gráfica real del protocolo (B02) y de la memoria de autoría
  (B03) se ha ejercitado al nivel del núcleo y de la tabla Swing embebida, no en
  la ventana real con su barra de navegación: queda como reproducción ventaneada
  obligatoria de `CU0`.
- Ninguna sonda ha modificado archivos versionados, preferencias del autor ni
  el repositorio del libro.

## 2. Identidad de los archivos del autor

Los originales permanecen intactos en
`artifacts/author-input/Lista19oct2026/` (ruta ignorada por Git según
`.gitignore:7`). Solo se han abierto copias byte a byte.

| Archivo | Bytes | SHA-256 | Origen declarado en el XML | Observaciones |
|---|---:|---|---|---|
| `InterCilConoObliqueTest.ggb` | 25 203 | `88779d58bcbcb08449eec8ffe6f74982238c84020a347f4cf2011f20a2690398` | `app="classic"`, `version="5.4.932.1"`, formato 5.0 | Construcción ordinaria de 67 objetos; `labelingStyle` 2; dos deslizadores angulares libres `α_C` (`min="0°" max="90°"`) y `α_{cyl}` (`min="0" max="90°"`), texto libre `text4` (`"CeDG "`), ángulos `α` y `β` definidos por tres puntos |
| `InterCilConoOblique.cedg` | 58 896 | `1ae6f51293fb111b54510948cbcc6e8ba38ab63b658ba3f8a668858ae890456c` | `app="classic"`, `version="5.4.928.0"`, documento nativo GeoCeDG (`geocedgUnits` v2, `geocedgSpatial` v1) | 113 objetos tras cargar; siete Locus V2 (`o`, `v`, `w`, `a_1` con conductor `N` sobre la circunferencia `g_1`; `e_1`, `o_1`, `e_2` con conductor `E_1` sobre `p`); un resultado rico `u_1 = Intersect(o_1, h_2)`; `N_1 = Point(o_1, text2, v_1)` con `v_1 = 0`; `w_1 = LocusLength(o_1, N_1, K_1)`; `d_2 = Length(w_1)`; herramientas `pointJump` y `translationCoor`; `labelingStyle` 2 y, aun así, `a_1` y `e_2` guardados con `label="true"` |

La cadena de `e_2` es la siguiente:
`e_2 = LocusV2(R_1, E_1)`, `R_1 = translationCoor(J_1, O_1, k, Q_1, j_2)`,
`Q_1 = pointJump(L_1, g_2, d_2)`, `d_2 = Length(w_1)`,
`w_1 = LocusLength(o_1, N_1, K_1)` y `o_1 = LocusV2(K_1, E_1)`. Es decir, cada
punto de `e_2` necesita la longitud semántica de otro Locus V2 accionado por el
mismo conductor.

## 3. Resultados por incidencia

### B01 — Inserción del `.ggb`

- Apertura normal del `.ggb`: correcta (67 objetos; `α_C` y `α_{cyl}` son
  `ANGLE` libres definidos).
- Insertar archivo en un documento vacío y en una copia del `.cedg`:
  `REJECTED_BEFORE_MUTATION`, sin cambio alguno del receptor. Mensaje:
  «these objects cannot be inserted: text4 (Text), α_C (Angle), α_{cyl} (Angle)».
- Variantes de bisección: quitar el símbolo `°` de los límites, renombrar los
  ángulos, quitar los paréntesis de `Rotate(L, (-α_{cyl}), A)`, eliminar los
  elementos `<slider>` o `<animation>`, o cambiar `angleStyle`, **no** cambian el
  resultado. Eliminar `text4` deja solo los dos ángulos en el rechazo.
- Causa: `GeoCeDGInsertionAdmission.classify` (líneas 241–248) admite un
  `GeoAngle` solo si su algoritmo padre es `AlgoAnglePoints` y un `GeoText` solo
  si es dependiente. Coincide con la matriz `P4` normativa de
  `geocedg/specs/ui/document-insertion.md` §2.2 («angles from three points»,
  «dependent texts»; rechazo explícito de «static (free) texts»). La deuda
  `DEFER-F3-UNLISTED-TYPES`, abierta y no autorizada, ya nombra «static texts …
  independent angles».
- Conclusión: no es un fallo del tratamiento angular. Es una exclusión
  contractual del conjunto de inserción aprobado. Requiere una ampliación de
  `P4`.

### B02 — Protocolo de construcción

- Con la tabla Swing real del protocolo (`ConstructionProtocolViewD`) en el host
  embebido, creada antes y después de cargar el modelo, los altos de fila son
  **mixtos por diseño heredado**: 46 filas de 36 px y 66 filas de 40 px (fuente
  12). La regla upstream `updateAll()` asigna «dos líneas de texto» y añade
  4 px a las filas con índice o subíndice.
- `setConstructionStep`, `nextStep`, `previousStep`, `firstStep`, `lastStep`,
  `update()`, la creación de un objeto y el cambio de idioma **no** alteran esos
  altos en el host embebido.
- Con FlatLaf instalado (sonda `b02flat`, `Table.rowHeight = 20`), la
  alternancia sí se reproduce:

  | Paso | Altos de fila |
  |---|---|
  | tras cargar | 36/40 px |
  | tras `updateComponentTreeUI` | 20 px uniformes |
  | tras navegar por pasos | 20 px |
  | tras una edición | 36/40 px |
  | tras otro refresco de UI | 20 px |
  | tras deshacer | 36/40 px |

- Mecanismo: FlatLaf instala su `rowHeight` en cada `updateUI`, y `JTable` borra
  los altos por fila porque nunca se fijó un alto global. `updateAll()` los
  restituye en ediciones, deshacer, cambios de columna y cambios de ajustes.
- Conclusión: el mecanismo queda establecido. La navegación por sí sola no
  conmuta los altos; el disparador exacto que vio el autor se confirmará en la
  reproducción ventaneada. Fallo upstream asociado: el suelo de 33 px comprueba
  la columna «Name» (índice 1) en lugar de «ToolbarIcon».

### B03 — Memoria de Paralela y Perpendicular

Estados de las dos ranuras tras cada evento (sonda `b03`):

| Evento | PARALLEL | PERPENDICULAR | Evento del despachador |
|---|---|---|---|
| navegación del protocolo (`setConstructionStep`, siguiente, anterior, primero, último) | HELD | HELD | `ADD`/`REMOVE` |
| creación de un objeto con la construcción situada en un paso intermedio | HELD | HELD | `ADD` |
| cambio de número o deslizador; redefinición blanda del mismo tipo | HELD | HELD | `UPDATE` |
| redefinición estructural de **otro** objeto (`P=Point(xAxis)`) | **EMPTY** | **EMPTY** | `RESET(clearView)` |
| deshacer / rehacer | **EMPTY** | **EMPTY** | `RESET(clearView)` |
| borrar un objeto no relacionado; ocultar y mostrar la referencia; cambio de idioma; `updateConstruction` | conservada (ocultar → SUSPENDED → HELD) | conservada | — |

- La pérdida tras deshacer, rehacer o cualquier reconstrucción es el contrato
  aprobado (`geocedg/specs/ui/authoring-memory.md` §3.5, §4 y §7.2: vigencia por
  instancia exacta). Con ese contrato, la navegación por el protocolo, al nivel
  del núcleo, no vacía la memoria.
- La «interferencia» entre herramientas es la regla de activación dual
  (§11.5): seleccionar la otra herramienta es una *partida* que devuelve ambas a
  `NORMAL`. La ranura se conserva, pero no se usa hasta reactivar la herramienta
  con Ctrl. Lo prueba `PreG9BR6PlusF2R1DualActivationTest` (líneas 485–497).
- Hallazgo adicional: con la construcción situada en un paso anterior a la
  creación de la referencia, la ranura informa `HELD` para un objeto que todavía
  no existe en ese paso.

### B04 — Índices de `Intersect(c, n)`

Con una circunferencia y un segmento horizontal desplazado dinámicamente, en
modo continuo y no continuo, `Intersect(c, n)` crea las salidas en el mismo
orden que usan `Intersect(c, n, 1)` e `Intersect(c, n, 2)`. La correspondencia
índice–salida es, por tanto, una propiedad del estado actual del algoritmo y no
una identidad geométrica durable: puede invertirse por simetría, tangencia o
heurística de continuidad. En el modelo del autor, `Intersect(c_1, n)` guarda
la segunda salida sin etiqueta (`a1=""`).

### B05 — Etiquetas con «No New Objects»

Con `labelingStyle` `AlwaysOff`, un punto, una recta, una circunferencia y un
`Locus` clásico se crean sin etiqueta, mientras que `LocusV2`, `SplineV2` y el
resultado métrico de `LocusLength` se crean con `labelVisible=true`. Los puntos
semánticos siguen el estilo. Causa: los constructores de `GeoLocusV2` no
invocan `setConstructionDefaults()`, de modo que la visibilidad queda en el
valor por defecto de `GeoElement`. No existe tipo por defecto `LOCUS_V2`. Es la
misma raíz que la observación de capa `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST`,
corregida entonces solo para la capa.

### B06 — Rizado

| Locus | Defecto del punto medio en unidades de modelo (h = 1e-2 … 1e-7) | Vértices dibujados | Giro máximo entre segmentos | Desviación curva densa → polilínea |
|---|---|---:|---:|---:|
| `o` | 9,9e-5 … 1,1e-14 | 17 | 49,4° | 0,72 px |
| `v` | 1,0e-4 … 1,1e-14 | 17 | 54,1° | 0,74 px |
| `w` | 1,0e-4 … 1,1e-14 | 12 | 156,8° | 0,74 px |
| `a_1` | 1,1e-4 … 1,2e-14 | 19 | 38,4° | 0,69 px |
| `e_1` | 2,0e-4 … 2,0e-14 | 9 | 180° (curva que se repliega) | 0,66 px |
| `o_1` | 1,9e-4 … 2,4e-14 | 27 | 18,7° | 0,62 px |

El defecto escala exactamente como h², de modo que la evaluación semántica es
suave y no tiene ruido. El rizado es una teselación de presentación demasiado
pobre: `LocusRenderCache2D` usa profundidad mínima 2 y un único criterio de
cuerda de 0,75 px en el punto medio, sin criterio angular. A escala del autor
(23,4 px/unidad) eso deja polígonos de 9 a 27 vértices con giros de hasta 54° y
facetas visibles con trazo grueso. El repliegue de `e_1` y `w` agrava el
efecto. La captura ampliada ×4 muestra las facetas.

### B07 — Gaps y desaparición de curvas

| `excCyl` | Componentes certificadas (`o`, `v`, `w`, `a_1`) | Tramos válidos muestreados (evidencia) | Dibujo | Hueco entre extremo real y vértice dibujado |
|---|---|---|---|---|
| 2,9 / 3,5 | `COMPLETE`, una componente `[-π, π)` | uno | adaptativo, cerrado | 0 px |
| 3,7 | `COMPLETE`, una componente `[-π, π)` | `[-π, 1,0407] ∪ [1,5119, π]` | **retroceso a 32 muestras uniformes**, dos subtrayectos | 4,9–8,7 px |
| 3,75 | idem | `[-π, 0,9294] ∪ [1,6232, π]` | idem | 6,5–17,7 px |
| 3,8 | idem | `[-π, 0,8443] ∪ [1,7082, π]` | idem | 6,9–10,6 px |
| 3,85 | idem | `[-π, 0,7745] ∪ [1,7802, π]` | idem | 8,2–21,9 px |
| 3,9 | idem | `[-π, 0,7112] ∪ [1,8413, π]` | idem | 9,0–19,2 px |
| ≥ 4,0 | **las cuatro curvas indefinidas por completo** | — | nada | curva entera |

Causas establecidas:

1. `AlgoDependentPointLocusV2.fallbackExistenceStructure` (líneas 164–191)
   publica `completeCompatibility`: certifica `COMPLETE`, una componente y
   máximo establecido con evidencia «provider-endpoints/v1», sin prueba sobre la
   construcción dependiente. Esto contradice el ADR 0027 («Dependent producers
   fail closed as NOT_ESTABLISHED»), `locus-v2-existence-components.md` §9, §10
   paso 2 y §12, y las filas R2-D06/R2-D07. La única excepción implementada es la
   familia de raíz seleccionada Locus V2/SplineV2 del testigo `m`.
2. `LocusRenderCache2D.appendAdaptiveComponent` abandona el modo adaptativo
   ante la primera evaluación inválida y recurre a 32–512 muestras uniformes. Los
   subtrayectos terminan en nodos de esa malla, sin localizar la frontera.
3. Al pasar a `excCyl = 4,0`, el conductor actual `N` (t = 0,6456) cae en la
   región donde `W` no existe. `isSemanticPublicationReady()` exige que todas
   las dependencias durables, incluido el punto trazado en la posición actual,
   estén definidas, así que el locus completo pasa a indefinido. Al mover `N` a
   t ∈ {−2,5; −1; 0; 0,5; 2,5} reaparecen los cuatro loci. La existencia del
   locus depende de dónde esté el conductor, lo que contradice
   L = ∪ F(Iⱼ \ Dⱼ) (`AGENTS.md` §11.1).

### B08 y B09 — Intersecciones no materializables

| Par | Raíz rica | Clasificación | Admisible | Análisis del residuo h(t) (40 000 muestras) |
|---|---|---|---|---|
| `e_1` ∩ `n` (segmento) | (4,8277; 7,6731) en t = 0 (= `M_1`) | `CONTACT_UNDETERMINED`, identidad `NOT_ESTABLISHED` | no | h ≤ 0 con doble cero en t = 0; d²h ≈ −3,35: **tangencia** |
| `e_1` ∩ `Line(H, M)` | idem | idem | no | idem |
| `e_1` ∩ `c_1` | (4,8277; 7,6731) en t = 0 | `CONTACT_UNDETERMINED` | no | h ≤ 0, doble cero, d²h ≈ −2,06: **tangencia** |
| `e_1` ∩ `c_1` | (1,6084; 10,1310) en la costura periódica | `TRANSVERSE_ESTABLISHED` | **sí**; se materializa | cruce transversal |
| `o_1` ∩ `h_2` (`u_1` y nuevo) | (4,8277; −5,3675) en t = 0 (= `N_1`) | `TANGENT_ESTABLISHED`, identidad `NOT_ESTABLISHED` | no | h ≥ 0, doble cero, d²h ≈ +2,57: **tangencia** |

Las tres soluciones que el autor necesita son **contactos tangentes**. Es la
propiedad clásica: la proyección de la curva de intersección es tangente al
contorno aparente (generatriz de contorno `n`) en el punto de contorno, y en la
otra proyección la curva tiene allí tangente paralela a la línea de referencia
`h_2`. El contrato público de materialización exige `TRANSVERSE_ESTABLISHED` y
nunca admite tangencias. No se trata, pues, de un bloqueo por completitud
global. El recurso `Point(o_1, text2, v_1)` con `v_1 = 0` funciona porque el
contacto está en t = 0 en esta configuración, pero es un parámetro estático.

Hallazgo independiente (sonda `scale`): una circunferencia-locus de radio 1,5r
cortada transversalmente por una recta y por un segmento da:

| r | Recta: soluciones admisibles | Segmento: soluciones admisibles |
|---|---|---|
| 2 | 2 de 2 | 2 de 2 |
| 20 | 2 de 2 | 1 de 2 (aislamiento `NOT_ESTABLISHED`) |
| 200 | 0 de 2 | 0 de 1 |
| 2000 | 0 (ninguna raíz publicada) | 0 |

El umbral absoluto de residuo de los objetivos recta/segmento (≈ 4e-12 en
unidades de modelo) y el corchete de parámetro de 1e-12 no escalan con el
dibujo. A escalas moderadas, raíces transversales genuinas dejan de ser
materializables y, a escala grande, desaparecen.

`Intersect(L, xAxis)` e `Intersect(L, yAxis)` fallan con el mensaje genérico
`CAS.GeneralErrorMessage`, mientras que `Intersect(L, y=0)` funciona.

### B10.1 — Rendimiento con `e_2`

| Medida | Con `e_2` | Sin `e_2` |
|---|---:|---:|
| Carga del documento | 12,8–13,8 s; 87 % de las muestras de pila en el dibujo de `e_2` | — |
| Una evaluación puntual de `e_2` | 90–152 ms | — |
| Trabajo anidado por evaluación de `e_2` | +2 revisiones de la copia aislada de `o_1`, +2 índices métricos completos (≈ 43 000 evaluaciones internas) | — |
| Estado de la copia anidada tras cargar | revisión 90, 87 índices métricos construidos, ≈ 1,98 millones de evaluaciones | — |
| Barrido denso de 720 puntos de `e_2` | 87,8 s | — |
| Reconstrucción del dibujo de `e_2` (23 vértices) | 5,49 s | — |
| Arrastre de `E_1` (actualización + repintado) | **≈ 5,78 s por paso** (0,14 s + 5,63 s) | ≈ 79 ms por paso (73 ms + 5 ms) |
| Cambio de `excCyl` (no afecta a `e_2`) | ≈ 22 ms | ≈ 20 ms |
| Recrear `e_2 = LocusV2(R_1, E_1)` | 5,62 s | — |

Causas establecidas (sondas `lvA`, `lvB`, `perf` y lectura de código):

1. **Rotación de revisiones.** `AlgoDependentPointLocusV2.createCandidate`
   incluye `"|dependency-update=" + candidateRevision` en la firma del
   evaluador. Así `hasSameSemanticContent` nunca es cierto y cada recálculo
   publica una revisión nueva, aunque la geometría no cambie. Arrastrar `E_1`
   republica `o_1` y reconstruye su índice métrico (≈ 21 000 evaluaciones), y lo
   hace también sin `e_2`.
2. **Recálculo del corte por punto.** `ReconstructibleLocusEvaluator2D.evaluate`
   ejecuta `resetExternalState()`, que hace `isolatedConstruction.updateConstruction(false)`
   en cada consulta puntual. Ese recálculo reconstruye la copia anidada de `o_1`
   (XML de macro, revisión nueva) y su `LocusLength`, que vuelve a construir el
   índice métrico completo dos veces por muestra externa. La especificación
   normativa lo prohíbe: «Slice construction may occur at most once per locus
   definition/revision, never per point query» y, para métricas usadas como
   dependencia, «avoid recomputing the complete metric for every downstream
   evaluation while the upstream semantic revision is unchanged»
   (`locus-v2-semantics.md`).
3. La sesión de evaluación que recibe el evaluador se ignora: no hay
   memoización, detección de ciclos ni presupuesto en el trabajo anidado.
4. Cada actualización de un predecesor obliga a redibujar `e_2` con
   45 evaluaciones o más, lo que supone ≈ 5,6 s de bloqueo del hilo de eventos.

Es la patología que motivó Locus V2 (`InterCilConoOblique.ggb` heredado,
registrado en `locus-v2-semantics.md`), reproducida ahora dentro de la
implementación V2.

### B10.2 — Cierre falso de `e_2`

- El barrido denso de 720 puntos no tiene evaluaciones inválidas y muestra un
  **salto de 13,2058 unidades en t = 0**: e_2(−0,001) = (13,8104; 6,7250) y
  e_2(0,001) = (27,0399; 6,7250).
- En la costura ±π la función es continua (separación < 1e-6 px).
- El certificado declara una componente periódica `COMPLETE`, así que el
  renderizador cierra la curva y la subdivisión adaptativa traza una cuerda a
  través del salto. Es la línea horizontal visible bajo la «campana» de `e_2`.
- Causa: la ruta de `LocusLength(o_1, N_1, K_1)` sobre una rama periódica de una
  sola componente siempre envuelve la costura. Cuando `K_1`, ocurrencia del
  conductor, pasa por la posición de `N_1` (t = 0), la longitud salta de L_total
  a 0. Esa frontera estructural no se publica, el certificado de existencia es
  el de compatibilidad (B07) y el renderizador no tiene guarda de saltos.
- La curva correcta es un arco abierto entre t = 0⁺ y t = 0⁻ que pasa por ±π.

### M04 — Orientación en rectas paralelas y perpendiculares

Con el clasificador `GeoCeDGOrientationCue.isAdmitted`:

- `.ggb` de prueba: 27 de 27 rectas, segmentos y rayos admitidos. La cadena de
  referencias nace en el segmento `f`.
- Modelo nativo: 19 de 41 admitidos. Las 22 exclusiones son paralelas o
  perpendiculares (`g`, `h`, `i`, `k`, `l`, `m`, `a`, `b_2`, `s`, `c`, `d`, `e`,
  `h_1`, `k_1`, `p_1`, `r_1`, `t_1`, `b_1`, `f_2`, `g_2`, `h_2`, `j_2`) cuya
  referencia remonta a `g = OrthogonalLine(A, xAxis)`. Los ejes están excluidos
  (`X10`) y la exclusión se propaga (`X8`).

### M05.1 — Modo de etiqueta de los deslizadores

| Estilo efectivo | Número escrito `a=2` mostrado como deslizador (nombre + valor) | `b=Slider(0,10)` |
|---|---|---|
| Automático, con vista algebraica oculta (= `AlwaysOff`) | tras deshacer y tras recargar: **solo valor** | nombre + valor |
| `AlwaysOff` («No New Objects») | **solo valor** | nombre + valor |
| `AlwaysOn` | nombre + valor | nombre + valor |

Causa: `XMLBuilder` no escribe `<labelMode>` para números con definición en
modo nombre + valor. Al recargar, los valores por defecto de construcción
resuelven `LABEL_DEFAULT` como solo valor bajo `AlwaysOff`. Los dos documentos
del autor usan `labelingStyle` 2.

## 4. Capturas

Generadas en el scratchpad de la sesión con la vista del autor (1071 × 780,
escala 23,40 px/unidad):

| Captura | Muestra |
|---|---|
| `v1-default-with-e2.png` | la cuerda horizontal de cierre bajo `e_2` y la etiqueta visible de `e_2` |
| `v2-excCyl-3.85.png` | huecos de los loci rojos en la transición de penetración a mordedura |
| `v3-excCyl-4.0.png` | desaparición completa de `o`, `v`, `w` y `a_1` |
| `v5-zoom4-first-loci-viewport-policy.png` | facetas de los primeros loci con la política adaptativa actual |

`CU0` debe regenerarlas de forma determinista y conservarlas como evidencia no
autoritativa.

## 5. Estado de los repositorios relacionado

- GeoCeDG: `main` = `origin/main` = `48a0775e` (corrección del puente del
  libro, publicada sin etiqueta); la etiqueta `geocedg-pre-g9b-r7-pass` apunta a
  `372da89f`. Existen dos registros locales de worktree ajenos al trabajo:
  `.claude/worktrees/stoic-lovelace-7c499d` en `48a0775e` y un worktree temporal
  de otra sesión en `257c854a`. No afectan al plan.
- Libro: rama local `book/figpol-pilot-2026-10-18` = `origin/main` = `78fbc88`
  (BOOK-FIGPOL-PILOT publicado). Árbol limpio. La rama local `main` está 5 commits
  por detrás de `origin/main`, sin divergencia de contenido. Las salidas
  generadas del piloto (`figures/generated/figpol-pilot/`) y `artifacts/` son
  ignoradas por diseño y se conservan sin cambios. El puente
  `tools/book/book-worktree.ps1 -Action Status` (PowerShell 7) informa
  `Book technical alignment: ALIGNED` con la base editorial aceptada `372da89f`
  y la relación del origen técnico `UNPUBLISHED PRODUCT STATE`.

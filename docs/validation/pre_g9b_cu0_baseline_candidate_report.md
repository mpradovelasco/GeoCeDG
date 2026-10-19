# PRE-G9B-CU0 — Informe del candidato: línea base reproducible, caracterización complementaria y estado

| Campo | Valor |
|---|---|
| Carácter | Informe del paquete `CU0` del track `PRE-G9B-CU`. Evidencia, no autoridad normativa |
| Estado | `CU0 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `CU1`–`CU10` no autorizados |
| Encargo | Instrucción autoral «PRE-G9B-CU — Author Plan Approval, Architectural Refinements and CU0 Authorization» (2026-10-19), que autoriza exclusivamente `CU0` |
| Base | candidato de planificación local `444ad46b1c18a6101dae68a75a8d7a5f92ed83fe`, árbol `1aff549ccf3c0deff5581ee7942d566bd5cc4e19`, padre `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1` |
| Clase de verificación propuesta | `INTEGRATED_PHASE`: `STATIC`, `INFRA_UNIT` (cambia el registro), `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` sobre el commit candidato |
| Identidad del commit y resultados de verificación | `docs/validation/pre_g9b_cu0_verification_record.md`, en un commit documental descendiente (un commit no puede citar su propia identidad) |
| Entorno | Windows 11 Pro 10.0.26300; JVM de pruebas Desktop Java 17.0.10; escenario ventaneado con Temurin 25.0.4 (runtime del producto); PowerShell 7.6.6 |

## 1. Comprobación de la base (instrucción §2)

| Comprobación | Resultado |
|---|---|
| Identificadores completos | `HEAD` = `444ad46b1c18a6101dae68a75a8d7a5f92ed83fe`; árbol `1aff549ccf3c0deff5581ee7942d566bd5cc4e19` |
| Padre | `48a0775e4f2f122f5e2614c62b5773777752ccd1` (= `main` = `origin/main`; `git ls-remote` lo confirma) |
| Contenido exacto | `A docs/architecture/pre_g9b_cu_corrective_usability_plan_candidate.md` (1108 líneas) y `A docs/validation/pre_g9b_cu_characterization_evidence_report.md` (356 líneas); nada más |
| Árbol de trabajo | limpio antes de empezar |
| Modificaciones de producto | ninguna en el candidato de planificación |
| Publicación | la rama `phase/pre-g9b-cu-plan-candidate` no existe en el remoto |
| Autoridad vigente | `AGENTS.md`, prompts canónicos de gobierno y verificación, roadmap (cabecera y tracks `PRE-G9B`/`PRE-G9B-R`), especificaciones Locus V2 (semántica, existencia y componentes, modelo de construcción certificada, métricas, intersecciones, presentación, superficie pública), ADR 0017, 0026 y 0027, registro y esquemas de verificación |

## 2. Alcance ejecutado

| Punto autorizado (§5) | Entregable |
|---|---|
| 1. Copias verificadas de los modelos del autor | §3.1 |
| 2. Sondas convertidas en pruebas versionadas | §3.2 |
| 3. Banco reproducible de la composición anidada | §3.4 |
| 4. Contadores de evaluaciones, revisiones, reconstrucciones e integraciones | §3.5 |
| 5. Referencias geométricas antes/después | §3.3 |
| 6. Reproducción ventaneada de B02 y B03 con el runtime del producto | §4.1, §4.2 |
| 7. Matriz inicial de cobertura de la guía | §5 |
| 8. Precisiones de la decisión incorporadas al plan | [plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md) |
| 9. Enmiendas normativas candidatas para `CU1`–`CU3` | [enmiendas candidatas](../architecture/pre_g9b_cu0_candidate_normative_amendments.md) |
| 10. Conciliación del estado vivo | §7 |

Entregables D–I: [longitud orientada (D)](../architecture/pre_g9b_cu0_oriented_length_analysis.md),
[certificación híbrida (E)](../architecture/pre_g9b_cu0_hybrid_certification_proposal.md),
[materialización (F)](../architecture/pre_g9b_cu0_tangent_materialization_proposal.md),
[redefinición e interacción (G)](../architecture/pre_g9b_cu0_redefinition_and_canvas_picking_design.md),
[precisión y tolerancias (H)](../architecture/pre_g9b_cu0_precision_and_tolerance_study.md),
[hoja de decisiones (I)](pre_g9b_cu0_author_decision_sheet.md).

**No se ha hecho:** ningún cambio de código productivo, capacidad geométrica, algoritmo de
certificación, herramienta ni interfaz; ninguna especificación ni ADR modificados (solo
candidatos); ningún cambio en el libro; ninguna publicación, etiqueta ni cambio de `main`.

## 3. A — Línea base reproducible

### 3.1 Corpus

| Archivo versionado | Bytes | SHA-256 |
|---|---:|---|
| `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg` | 58 896 | `1ae6f51293fb111b54510948cbcc6e8ba38ab63b658ba3f8a668858ae890456c` |
| `models/regression/pre-g9b-cu/original/InterCilConoObliqueTest.ggb` | 25 203 | `88779d58bcbcb08449eec8ffe6f74982238c84020a347f4cf2011f20a2690398` |

Copias byte a byte de los originales de `artifacts/author-input/Lista19oct2026/`, que
siguen intactos e ignorados (hash comprobado antes y después de copiar). El
[manifiesto](../../models/regression/pre-g9b-cu/manifest.yml) valida contra
`geocedg/specs/operations/model-manifest.schema.json`; el
[registro de procedencia](../../models/regression/pre-g9b-cu/expected-results.yml) recoge
fechas de modificación, contenido y relación con cada incidencia. `.gitattributes` fija
ambos archivos como binarios.

### 3.2 Pruebas versionadas y selección `PRE-G9B-CU0`

| Clase | Pruebas | Contenido |
|---|---:|---|
| `PreG9BCu0CorpusTest` | 3 | identidad del corpus frente a manifiesto y línea base; cadena de `e_2` presente; la variante derivada sin `e_2` elimina solo ese paso |
| `PreG9BCu0LocusReferenceTest` | 4 | `preserve`: estado, puntos y muestras de seis loci, perfil de la longitud orientada, extremos de rama, contactos y curvaturas de contacto; fijaciones H03, B07, H01, B06 y B08/B09 |
| `PreG9BCu0CompositionCharacterizationTest` | 2 | `preserve`: puntos de `e_2` y continuidad en la costura; fijaciones B10.1 y B10.2 |
| `PreG9BCu0SyntheticCharacterizationTest` | 3 | fijaciones H02, H06, H10 y H11 con su verdad geométrica |
| `PreG9BCu0AuthoringCharacterizationTest` | 6 | fijaciones B01, B05, M05.1, M04 y H05; `preserve` del orden indexado de B04 |
| `PreG9BCu0GuideCoverageMatrixTest` | 3 | integridad de la matriz de cobertura de la guía |

Clases de apoyo: `PreG9BCu0Corpus`, `PreG9BCu0References` y `PreG9BCu0Baseline`.

**Regla de las fijaciones.** Un valor `preserve` es un hecho geométrico del producto que
las correcciones deben reproducir. Una fijación (`pins`) registra la firma de un defecto
caracterizado, nombra su paquete propietario en el mensaje («CHARACTERIZATION PIN …
reference evidence, not a normative expectation») y la invierte o sustituye ese paquete.

Las 21 pruebas pasan en dos ejecuciones de desarrollo y en la ejecución del productor
oficial del inventario (≈ 52 s de pruebas; 0 fallos, 0 omitidas). La variante sin `e_2` se deriva al vuelo
(se retiran el paso `e_2 = LocusV2(R_1, E_1)` y su registro de identidad, que nadie
referencia); el arnés comprueba que reproduce exactamente el modelo completo tras eliminar
`e_2` por el núcleo (diferencia máxima `0,0`).

La selección `pre-g9b-cu0.desktop` y la fase `PRE-G9B-CU0` quedan registradas (§8) para
que cada paquete compare antes y después con `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`.

### 3.3 Línea base geométrica

[`geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json`](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json)
(35 072 bytes, SHA-256 `4cf872df0454ea518a2cd60702f10c72803d1af3586e024afaec34c6ec4fba1d`):

- `preserve`: estado y 12 puntos de construcción; 6 loci en 24–25 parámetros; `e_2` en 29
  parámetros, incluidos `±10⁻⁶` y `±10⁻³` del origen; perfil de `d_2` en 8 posiciones
  del conductor; extremos de rama localizados por bisección para `excCyl` 2,9 / 3,7 /
  3,85 / 3,9; puntos y curvaturas de los tres contactos; raíz transversal de la costura;
  longitud total de `o_1` (`13,2364580874`) y su referencia independiente
  (`13,2364581958`, extrapolación de Richardson de sumas de cuerdas con 16 384 y 65 536
  muestras); distancia en la costura de `e_2`.
- `pins`: H03, B07, H01, B06, B08/B09, B10.1, B10.2, H02, H06 y H10/H11.
- Tolerancias: punto `10⁻⁹`; magnitudes derivadas de longitud `10⁻⁶` (holgura para la
  corrección métrica H12); extremo de rama `10⁻⁷` en parámetro.

Generada con el arnés (`-Scenario references`). Las ejecuciones 4, 5 y 6 coinciden en
todas las secciones comunes; solo cambian, de forma intencionada, `pins.B10.2` (la
ejecución 5 corrigió la detección de la cuerda de dibujo que cruza `t = 0`) y las dos
secciones que añadió la ejecución 6 (`pins.H10_H11` y
`preserve.o_1_total_length_independent`). El archivo versionado es idéntico byte a byte a
la salida de la ejecución 6. Fuente: `444ad46b` con fuentes de producto sin modificar.

### 3.4 Banco y escenarios reproducibles

`tools/benchmark/pre-g9b-cu/` (fuera de `source/` y del verificador):

```text
pwsh -File tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario references
pwsh -File tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario composition [-Dense]
pwsh -File tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario windowed -ProductRuntime
```

El ejecutor añade las fuentes del arnés a las pruebas Desktop mediante
`harness.init.gradle` para una sola ejecución, escribe la salida y `run-metadata.json`
(commit, árbol, estado del árbol de trabajo, hashes de las salidas) en una carpeta nueva
de `artifacts/benchmarks/pre-g9b-cu/` (ignorada) y recompila después sin el arnés, de modo
que no queda ninguna clase ni informe del arnés en el árbol de compilación. No asigna
veredictos.

### 3.5 Contadores (escenario `composition`, `-Dense`)

Salida `composition-bench.json` (SHA-256 `203df910…6f74`), JVM 17.0.10:

| Medida | Con `e_2` | Sin `e_2` |
|---|---:|---:|
| Carga del documento | 15,3 s | — |
| Copia anidada de `o_1` tras cargar y dibujar | revisión 266; 259 índices métricos; 5,9 millones de evaluaciones | — |
| Una evaluación puntual de `e_2` | 92–157 ms; +2 revisiones y +2 índices de la copia anidada; 31 000–55 000 evaluaciones internas | — |
| Arrastrar `E_1` (actualización + dibujo) | 144–219 ms + **5,68–5,70 s** | 70 ms + 5 ms |
| `excCyl` (no afecta a `e_2`) | 18–20 ms + 6 ms | 13 ms + 5 ms |
| `rbCone` | 152–164 ms + 5,61–5,66 s | 83–94 ms + 5 ms |
| Revisión y reconstrucción del índice de `o_1` al arrastrar el conductor | +1 / +1 (≈ 20 800 evaluaciones) | +1 / +1 |
| Barrido denso de 720 puntos de `e_2` | 89,5 s; 0 inválidos; salto máximo 13,2058 en `t = 0` | — |
| Recrear `e_2` | 5,78 s | — |

Los contadores anidados solo son accesibles por reflexión y se reinician cuando la
actualización del DAG reconstruye el evaluador (deltas negativos en la primera fila de
arrastre): `CU1` debe exponer contadores anidados públicos (enmienda A1.1).

## 4. B — Caracterización complementaria

### 4.1 B02 en el producto real

Escenario `windowed`: `GeoCeDG.main` con preferencias aisladas, FlatLaf Light
(`Table.rowHeight = 20`), Temurin 25.0.4; apertura del modelo del autor por la ruta de
apertura del producto en la ventana activa; eventos enviados a los componentes reales.

| Paso | Altos de fila (112 filas) |
|---|---|
| abrir el modelo y mostrar el protocolo | 20 px uniformes |
| barra de navegación: primero, siguiente, siguiente, anterior, último | 20 px |
| tabla: flecha arriba/abajo, inicio, fin, clic en una fila | 20 px |
| edición (crear un punto) | 36 px (47) y 40 px (66) |
| deshacer | 36/40 px |
| `updateComponentTreeUI` (refresco de interfaz, como un cambio de tema) | 20 px |
| navegación tras el refresco | 20 px |
| tamaño de fuente +2 y restaurado | 20 px |
| cambio de idioma y restauración | 36/40 px |
| navegación tras el cambio de idioma | 36/40 px |

Conclusión: al abrir el modelo el protocolo aparece con espaciado compacto de tabla y pasa
al espaciado de dos líneas con la primera edición, deshacer o cambio de idioma; cualquier
refresco de interfaz posterior lo devuelve a 20 px. La navegación por sí sola no cambia
el espaciado: el autor ve el cambio al alternar navegación y edición. El mecanismo es la
combinación de los altos por fila heredados (`updateAll`) con el alto de fila que FlatLaf
instala en cada refresco. Capturas `b02-01.png` a `b02-21.png`.

### 4.2 B03 en el producto real

Barra de herramientas real con activación por Ctrl, clics en el lienzo enviados al
controlador, barra de navegación del protocolo:

| Paso | Paralela | Perpendicular | Objetos |
|---|---|---|---:|
| Ctrl + Paralela; clic en `l`; clic en `P` | memoria activada; referencia guardada | normal | 11 |
| clic en `Q` | crea la paralela con la referencia recordada | — | 12 |
| navegación del protocolo (anterior, primero, último) | memoria y referencia **conservadas** | — | 12 |
| clic en `R` | crea la paralela | — | 13 |
| Ctrl + Perpendicular; clic en `m`; clic en `S` | pasa a normal (referencia conservada) | memoria activada; crea la perpendicular | 14 |
| Paralela sin Ctrl; clic en `R` | normal: no crea nada (espera recta y punto) | normal | 14 |
| Ctrl + Paralela; clic en `S` | la referencia anterior se aplica de nuevo | — | 15 |
| deshacer | referencia **vaciada** | referencia **vaciada** | 14 |

La barra de estado muestra «Parallel Line · Memory» o «Perpendicular Line · Memory»
mientras la memoria está activa. Conclusiones: la navegación no pierde la memoria; la
«interferencia» es la regla de partida de la activación dual (la referencia se conserva
pero no se aplica sin reactivar con Ctrl); deshacer vacía ambas referencias, según el
contrato vigente. Capturas `b03-22.png` a `b03-38.png`.

Cuatro ejecuciones del escenario produjeron un `windowed-b02-b03.json` idéntico byte a
byte (SHA-256 `402492c8…de06e`) y las mismas 38 capturas. Las tres primeras terminaron con
código 1 por el cierre de la JVM de pruebas, antes de separar el arnés ventaneado de la
extensión de ciclo de vida de las pruebas embebidas; la ejecución de referencia (la
cuarta, ya separada) termina con código 0.

### 4.3 Evidencia nueva

| Hallazgo | Evidencia | Paquete |
|---|---|---|
| **H10** `LocusLength` de `y = sin(2πkx)` en `[0,1]` devuelve 1,0 (la cuerda) para `k = 1, 2, 4`; longitudes reales 4,1883 / 8,1118 / 16,0647; resultado escalar admisible, sin diagnóstico | `pins.H10_H11` | `CU1` (P0) |
| **H11** longitud indefinida de una circunferencia Locus V2 de radio 8 o mayor (radio 7 medido) por agotamiento del presupuesto | `pins.H10_H11` | `CU1` (P1) |
| **H12** error relativo ≈ 6–8·10⁻⁹ frente a 10⁻⁹ declarado (circunferencias y `o_1`); la longitud de `o_1` es correcta dentro de `1,1·10⁻⁷` | `preserve.o_1_total_length_independent` | `CU1` (P2) |
| El salto de `w_1` es exactamente `L` y está solo en el origen `N_1`; continuidad en la costura | `preserve.oriented_length_profile`, `preserve.e_2` | análisis D |
| En la transición de penetración a mordedura, `o` y `v` comparten sus extremos, igual que `w` y `a_1` (la tangencia del rayo con la base une las dos ramas) | `preserve.branch_ends` | `CU2` |
| Los tres contactos son estructurales; el residuo es par en `t` (`h'(0) = 0` exacto por simetría) con `h'' ≈ −3,35`, `−2,06` y `+2,57` | `preserve.contacts` | `CU3` |

### 4.4 Estudios de código de `CU0`

Cuatro estudios de solo lectura, con referencias `archivo:línea`, alimentan D–I: modelo de
existencia y certificación, y canalización de intersecciones (base de E y F); inventario
de umbrales y presupuestos (base de H); redefinición, identidad y diálogos (base de G); y
cobertura de la guía (§5). Sus conclusiones esenciales y referencias están integradas en
esos documentos; los estudios completos se conservan fuera del árbol como material de
trabajo.

## 5. Matriz de cobertura de la guía

[`geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json`](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json):
267 entradas (133 acciones del perfil, 20 comandos, 18 herramientas, 27 preferencias,
52 menús, 17 operaciones de documento); 116 cubiertas, 119 parciales, 20 ausentes y 12
excluidas con razón. Anclas comunes a ambas ediciones: 104. Principales carencias: rutas
de menú inexistentes (submenús de exportación y de opciones GeoCeDG que son entradas
directas), nombres de herramientas que no coinciden con la aplicación (en las dos
ediciones), áreas heredadas sin cubrir (Edición, Opciones, Ayuda y pestañas de
Preferencias), la forma de seis argumentos de `LocusLength` sin tokens ni ejemplo, la forma
`Point(<Locus V2>, <Parámetro>)` sin documentar y la biblioteca GGT solo nombrada.
`PreG9BCu0GuideCoverageMatrixTest` comprueba su integridad, no su completitud (eso
corresponde a `CU9`). La guía no se ha modificado.

## 6. Plan, diseños y enmiendas

El [plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md) incorpora
todas las decisiones de la instrucción, añade H10–H12 a `CU1`, separa compromisos de
cierre y elementos diferibles y mantiene el libro en pausa. El plan candidato revisado se
conserva sin cambios como antecedente.

## 7. Conciliación del estado vivo

Roadmap (versión documental 5.59): cabecera actualizada con la publicación de
`PRE-G9B-R7` (avance rápido sin forzar `b9bdb1e1` → `372da89f` el 2026-10-15 y etiqueta
anotada `geocedg-pre-g9b-r7-pass` → `372da89f`), la corrección del puente del libro
publicada sin etiqueta como `main` `48a0775e` (2026-10-16), el estado del track
`PRE-G9B-CU`, la última fase ejecutada y la siguiente puerta; el track `PRE-G9B-R` marca
`R7` como publicado, igual que la fila de `PRE-G9B-R7` de la tabla de fases; nueva sección
«Track PRE-G9B-CU». El registro de estado del
[plan del mini-track `PRE-G9B-R6-plus`](../architecture/pre_g9b_r6_plus_minitrack_plan.md)
pasa la entrada de `PRE-G9B-R7` de «not yet published» a publicada, conservando sus
estados anteriores. Hechos comprobados con `git`
(`ls-remote`, etiqueta y ascendencia) y con la auditoría de publicación de R7. No se ha
modificado ningún registro de cierre.

## 8. Infraestructura de verificación modificada

| Archivo | Cambio |
|---|---|
| `geocedg/specs/operations/verification-registry.json` | fase `PRE-G9B-CU0` (`compile.desktop.semantic` + `junit.desktop.pre-g9b-cu0.semantic`) y sus dos nodos; huella del inventario |
| `geocedg/specs/operations/verification-junit-inventory.json` | selección `pre-g9b-cu0.desktop` (21 identidades); `discovery.desktop` 2159 → 2180 y `final.desktop` 2153 → 2174 (las 21 pruebas nuevas); `discovery.shared` sin cambio de recuento. Todo recalculado con el actualizador oficial a partir de ejecuciones reales del productor: dos descubrimientos en seco, `pre-g9b-cu0.desktop` (21 pruebas, 0 fallos) y `final.desktop` (2174 pruebas, 0 fallos, 1 omitida upstream preexistente, `CasValueExtractor.extractMockValues`) |
| `tools/agent/tests/verification-final-coverage.Tests.ps1` | recuentos fijados: 64 selecciones, 69 fases y 21 identidades de la selección nueva |
| `docs/upstream/modified-files.yml` | 9 archivos de prueba nuevos registrados (`change: added`) |

## 9. Limitaciones

1. Las pruebas de caracterización usan la JVM de pruebas Desktop (Java 17); el escenario
   ventaneado usa el runtime del producto (Temurin 25) y su `main`, pero no el ejecutable
   empaquetado.
2. B02 no se ha comparado con el perfil Classic diagnóstico en `CU0`.
3. B03 se ha reproducido con eventos enviados a los componentes, no con entrada nativa del
   sistema operativo; el foco entre ventanas no está cubierto.
4. Los contadores anidados de B10.1 se leen por reflexión (frágil a propósito: `CU1` los
   sustituirá por contadores públicos).
5. Las propuestas E, F y H son diseños: no se ha implementado ni probado ninguna
   certificación nueva.
6. H10–H12 se han medido con construcciones sintéticas; su efecto sobre los modelos del
   autor se ha comprobado solo para `o_1`.
7. La matriz de cobertura es un análisis estático; sus juicios editoriales requieren
   revisión autoral.
8. Los tiempos son informativos (una estación).

## 10. Estado de autorización

`CU0` autorizado y ejecutado; candidato técnico pendiente de revisión autoral; no se
declara `PASS — AUTHOR APPROVED`. `CU1`–`CU10` no autorizados. Sin publicación, etiqueta
ni cambio de `main`; libro sin cambios.

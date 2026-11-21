# GeoCeDG — Reparaciones posteriores a R1: huella del slice, propiedad de auxiliares, autoridad de la evaluación aislada y actualización equivalente

**CANDIDATO TÉCNICO — PENDIENTE DE REVISIÓN AUTORAL.** `selfApproved=false`. No es una
fase del roadmap ni una versión. Nada de este documento es autoridad geométrica: las
fuentes durables son el código, sus pruebas y las especificaciones que se citan.

## 0. Identidad y alcance

- Autorización: instrucción autoral del 2026-11-21, «Ejecución secuencial tras R1:
  reparaciones, modelo y uso condicionado de Locus V2». Autoriza las reparaciones A, F7,
  F6, B y A′, sus aclaraciones normativas estrechas, la incorporación del modelo y de su
  variante corregida, y el diseño completo del uso condicionado (bloque 3A, en
  [su documento](post_r1_locus_conditioned_use_design.md)). No autoriza publicar en `main`.
- Base: `main` `9ccefdcfe922d8c0e329bdeec30c12fc058de463` (árbol
  `18dbb9bc4216a536b5b2fc3d17321737001cdd36`), igual a `origin/main` al iniciar.
- Rama: `maintenance/post-r1-locus-repairs-and-conditioned-use`.
- Evidencia de partida: revisión R1 del diagnóstico de locus derivados de intersecciones
  (dos emisiones del 2026-11-21, fuera del repositorio). R1 midió los JAR de GeoCeDG 1.2.1,
  cuyo árbol `source/` es el de la base; lo que sigue se midió sobre el código modificado y
  no se le atribuye nada de aquellas ejecuciones.
- Registro de progreso y checkpoints: [registro](../validation/post_r1_locus_repairs_progress.md).

## 1. Notas de diseño

Cada nota: problema, contrato, capa, solución, alternativas descartadas y prueba
discriminante. Todas las piezas viven en el kernel Java compartido porque afectan a la
identidad, la invalidación del grafo de dependencias y la autoridad de evaluación.

### 1.1 A — huella del slice y localización del ledger

- **Problema.** El ledger de tokens de una intersección Locus V2 que sigue al conductor del
  slice entraba en la parte *estructura* de la huella con el parámetro canónico de cada
  raíz. Mover solo el conductor publicaba una revisión del locus consumidor por paso.
- **Contrato.** `locus-v2-semantics.md`, enmienda v1.2: los elementos que dependen del
  conductor entran con sus valores numéricos neutralizados y mover solo el conductor no
  publica revisión. `locus-v2-intersections.md` §6.1: el parámetro canónico es evidencia de
  localización de una revisión, no identidad durable.
- **Solución.** El ledger expone un texto de comparación «neutral respecto a la
  evaluación» (`exportEvaluationNeutralState`): igual que el estado persistido salvo el
  parámetro canónico de cada raíz y la distinción entre asignación reclamada vigente y
  reclamada durmiente, que el mismo enlace exacto reactiva. La huella lo usa solo para un
  resultado que sigue al conductor. Versión, marca de máximo, material de propietario, par,
  linaje y topología, número y tipo de entradas, encarnación, linajes, clave de
  continuación, contratos, selectores, estado no reclamado, cuarentenas e instantánea de
  copia entran como se persisten. Nada persistido cambia.
- **Alcance de activa/durmiente.** Incluido. Lo justifican los hechos del ledger: una
  asignación reclamada cuya raíz deja de existir pasa a durmiente sin cambiar ningún otro
  campo; una retirada elimina la entrada; una cuarentena usa otro código de estado; una
  asignación nueva añade una entrada y avanza la marca. Todas esas transiciones siguen
  cambiando el texto neutral. Queda una revisión por cruce del dominio solo cuando hay una
  asignación **no reclamada** (sin punto materializado): el ledger la descarta al salir y la
  asigna de nuevo, con otra encarnación, al volver.
- **Descartado.** Retirar el ledger de la huella (ocultaría asignaciones, retiradas y
  cambios de selector); excepciones por etiqueta, modelo o lista de valores; analizar el
  texto persistido desde la huella (acoplaría la huella al formato del ledger).
- **Prueba discriminante.** `PostR1SliceLedgerFingerprintTest` (6 casos; 4 fallan sin la
  neutralización): revisión, evaluador y función bit a bit con el conductor en movimiento;
  salida y retorno del dominio; slice construido con raíces durmientes; entrada no
  conductora y ciclo de vida de tokens siguen publicando; un resultado que no sigue al
  conductor conserva su ledger completo; reapertura.

### 1.2 F7 — propiedad de auxiliares y capacidad de guardar

- **Problema.** `Point(L, <texto>, u)` aceptaba el texto selector oculto de otro
  `Point(L, u)`. El dueño perdía la exclusividad que exige su rol de identidad persistido
  y el documento no podía volver a leerse (`MALFORMED_RECORD`).
- **Contrato.** `locus-v2-point-interaction.md` §12: las entradas dedicadas son padres
  dedicados, independientes y exclusivos del punto.
- **Solución.** `LocusDedicatedAuxiliary2D` decide el dueño de una entrada por el rol de
  identidad persistido del punto y por el lugar de la entrada en su padre, nunca por el
  contenido de un texto. `AlgebraProcessor` rechaza, antes de toda mutación y con la causa
  tipada `DEDICATED_AUXILIARY_OWNED`, una expresión que lea o redefina un auxiliar
  dedicado, salvo que redefina a su propio dueño. Es un único punto de admisión: cubre
  `Point`, cualquier otro comando o expresión que lo lea (`Length`, concatenación,
  `SetValue`) y la redefinición del propio auxiliar.
- **Decisión entre rechazo y estado independiente.** Rechazo tipado previo. Copiar el
  texto para el nuevo consumidor sustituiría una dependencia pedida por un valor
  congelado. Un texto ordinario con el mismo contenido no tiene dueño y sigue siendo
  compartible.
- **Documentos ya dañados.** Sin reparación automática: conservan el diagnóstico
  `MALFORMED_RECORD` sobre el registro del dueño; al retirar el consumidor ajeno el
  documento vuelve a ser coherente.
- **Descartado.** Guarda en el registro de dependencias de `GeoElement` (el algoritmo ya
  estaría a medio construir: rechazo no atómico); guarda solo en `CmdPoint` (no cubre
  otras vías); relajar la exclusividad del dueño.
- **Prueba discriminante.** `PostR1DedicatedAuxiliaryOwnershipTest` (5 casos; 4 fallan sin
  la guarda): rechazo sin cambios en el documento; otras vías de consumo y redefinición;
  formas públicas de dos y tres argumentos y texto propio; guardar, reabrir y
  deshacer/rehacer; documento ya dañado.
- **Observación fuera de alcance.** La definición que el producto presenta para el dueño
  nombra su selector (`Point(L, text1, u)`); la guarda la admite. En el arnés sin ventana,
  redefinir al dueño con esa forma y un parámetro literal devuelve «Please check your
  input» también sin la guarda: es comportamiento previo del contrato de redefinición, sin
  triaje en este trabajo.

### 1.3 F6 — autoridad efectiva de la evaluación aislada

- **Problema.** La copia aislada de un slice es una construcción de macro sin registro
  documental; todas sus relaciones se decidían bajo la política legacy aunque el documento
  vivo estuviera bajo `constructive-authority/v1`. Un locus que consume `Point(L, D)` se
  publicaba como fiel con todas sus evaluaciones `DEPENDENCY_UNDEFINED`.
- **Contrato.** `locus-v2-constructive-authority.md` §4: un documento tiene una autoridad.
- **Solución.** El evaluador captura como valor, antes de rellenar la copia, la autoridad
  efectiva de la construcción dueña del slice; la copia la conserva como contexto de
  evaluación (`captureEvaluationAuthority`, una sola vez, solo en construcciones de
  macro). `AuthorityPolicy.of` resuelve esa autoridad efectiva. No se inserta registro
  documental, nada se persiste, el evento de adopción sigue rechazando una macro y ninguna
  muestra lee el documento vivo. El dueño reconstruye el slice cuando cambia la autoridad
  de su documento; una copia bajo autoridad constructiva tiene su propia firma de
  evaluador y la firma legacy no cambia. Los slices anidados heredan por la misma captura.
- **Descartado.** Insertar un registro documental en la macro; leer el documento vivo en
  cada muestra; tipar como «no soportado» el caso que sí funciona vivo.
- **Límite.** La construcción de macro de una herramienta de usuario no es un slice y
  queda fuera.
- **Prueba discriminante.** `PostR1IsolatedEvaluationAuthorityTest` (5 casos): el locus
  sobre el punto estructural evalúa como el documento vivo sin modificarlo; firma y
  comportamiento legacy; adopción y deshacer reconstruyen el slice; reapertura; una
  relación fuera de clase sigue rechazada con su causa tipada.

### 1.4 B — actualización equivalente del mismo productor

- **Problema.** Una cascada que alcanza una intersección pública iniciaba una resolución
  aunque la fuente conservara su revisión y el objetivo no cambiara.
- **Contrato.** `locus-v2-intersections.md` §10 y §11: publicación atómica; sin índice ni
  caché compartidos de resultados. Aclaración estrecha añadida en §10: una actualización
  con entradas idénticas no es un recálculo.
- **Solución.** Antes de resolver, `AlgoLocusIntersectionV2` compara las entradas
  semánticas completas con las de su propia carga vigente: identidades durables de fuente,
  objetivo, par y propietario; revisión, objeto de definición y contrato del proveedor de
  la fuente; estado exacto del objetivo y de todo aquello de lo que se construye (como lo
  lee la huella); familia del objetivo; autoridad; versiones de linaje, topología y
  política; y estado completo del ledger, persistido y pendiente. Contadores de
  actualización o coordenadas sueltas nunca son la clave.
- **Condición de reutilización.** Solo después de que una resolución con esas mismas
  entradas haya reproducido el resultado anterior sin cambiar el ledger, y solo para un
  éxito finito vigente cuyas raíces tienen todas selección determinista establecida. Un
  fallo, un límite de trabajo, una primera publicación, una identidad ambigua o
  discontinua y toda transición del ledger se resuelven siempre de nuevo. Con ello el
  resultado con la reutilización activada es, por construcción, el que daría desactivada.
- **Descartado.** Caché compartida de resultados ricos (excluida por §11); clave por
  revisión de la fuente y coordenadas del objetivo; reutilizar tras la primera resolución
  (cambiaría el estado de identidad observable de una primera publicación).
- **Límites.** No cubre el productor locus–locus (`AlgoLocusLocusIntersectionV2`) ni los
  productores internos G8. Un resultado con contacto tangente o sin selección determinista
  no se reutiliza (en el modelo, `u_1`).
- **Prueba discriminante.** `PostR1EquivalentIntersectionUpdateTest` (4 casos): cascada
  equivalente sin resolución; un ulp del objetivo y una revisión de la fuente resuelven;
  transición del ledger y fallo nunca se conservan; misma trayectoria con la reutilización
  desactivada, con valores, tokens, estados, garantías, vigencia y documento idénticos.

### 1.5 A′ — conductores de loci anidados

- **Problema.** La posición del conductor de un locus anidado, y lo calculado desde ella,
  entraba en la parte *valores* de la huella del consumidor.
- **Contrato.** `locus-v2-semantics.md`, enmienda v1.2: el valor de una salida Locus V2 no
  depende de la posición de su conductor.
- **Solución.** `ReconstructionExactState2D.nestedDriverState` calcula, sobre el grafo de
  dependencias existente, el mayor conjunto de elementos que siguen al conductor de un
  locus anidado y cuyo uso dentro del slice termina siempre en la salida de un locus con
  ese mismo conductor. Esos valores entran en la huella como los que siguen al conductor
  del slice: sin estado exacto, con el lugar libre de todo punto sobre camino o región que
  no sea raíz conductora anidada, y con el ledger de una intersección entre ellos en su
  forma neutral. Un elemento se conserva como entrada, con todo aquello de lo que se
  calcula, cuando un algoritmo del slice lo lee y tiene una salida que sigue al conductor
  del slice (vía directa al punto trazado), que es la salida de un locus de otro conductor
  (entrada no conductora de una definición anidada) o que es cualquier otro elemento.
  Literales, opciones, selección y soporte no cambian. Huella `…/v3`, no persistida.
- **Descartado.** Excluir todo descendiente del conductor anidado; excluir por etiqueta.
- **Prueba discriminante.** `PostR1NestedLocusDriverFingerprintTest` (4 casos; 3 fallan
  sin el análisis): el conductor anidado no publica revisión y el slice reutilizado iguala
  a uno reconstruido desde el documento; una vía directa lo conserva como entrada; un
  parámetro de la definición anidada (`L_t`) y el soporte invalidan; un punto sobre un
  camino que se mueve con el conductor anidado conserva su parámetro propio.

## 2. Medición

Arnés versionado: `tools/benchmark/post-r1-locus/run-harness.ps1 -Scenario benchmark`
(`-CheckoutRoot` mide otro checkout con las mismas fuentes del arnés). Salidas en
`artifacts/benchmarks/post-r1-locus/` (ignorado por Git), con `run-metadata.json`.

Método: anfitrión embebido de pruebas Desktop, JVM de pruebas OpenJDK 17.0.10, Windows 11,
24 procesadores lógicos; una serie por escenario; calentamiento indicado; carga ajena no
controlada. *dag* es la actualización del grafo para un paso del conductor
(`pathChanged` + `updateRepaint`); *pintura* es, aparte, el dibujo de Gráficos 1 sobre una
imagen con el tamaño, origen y escala guardados en el modelo (1092×797), sin el compositor
de una ventana real. Las latencias son telemetría de este equipo, nunca umbral.

| Documento | Escenario | Pasos | dag, mediana (ms): antes → después | pintura, mediana (ms): antes → después | revisiones `w_2` / `a_3` / `trHole`: antes → después | evaluaciones anidadas `w_2` / `a_3`: antes → después | `t_2` resueltas / equivalentes (después) | `v_2` (después) |
|---|---|---|---|---|---|---|---|---|
| original | `E_1`, interior regular (2,20–2,40 rad) | 21 | 2368,6 → 58,3 | 54,4 → 5,8 | 21 / 21 / – → 0 / 0 / – | 1852495 / 1852495 → 5968 / 5968 | 0 / 21 | 21 / 0 |
| original | `N` (1 rad desde su posición guardada) | 21 | 1581,6 → 23,6 | 52,7 → 5,6 | 21 / 21 / – → 0 / 0 / – | 1852431 / 1852431 → 0 / 0 | 0 / 0 | 0 / 21 |
| original | `E_1`, salida y retorno del dominio | 6 | 2357,7 → 56,2 | 52,0 → 4,9 | 6 / 6 / – → 0 / 0 / – | 528684 / 528684 → 0 / 0 | 0 / 6 | 6 / 0 |
| original | `E_1`, junto al pliegue (2,72–2,80 rad) | 9 | 2348,4 → 55,9 | 52,0 → 5,3 | 6 / 6 / – → 0 / 0 / – | 529072 / 529072 → 0 / 0 | 0 / 9 | 9 / 0 |
| original | cambio real: `excCyl` ± 0,05 | 6 | 1578,8 → 1645,9 | – | 6 / 6 / – → 6 / 6 / – | 488856 / 488856 → 488856 / 488856 | 0 / 0 | 6 / 0 |
| transporte | `E_1`, interior regular (2,20–2,40 rad) | 21 | 2807,4 → 62,1 | 130,3 → 5,5 | 21 / 21 / 21 → 0 / 0 / 0 | 1852495 / 1852495 → 5968 / 5968 | 0 / 21 | 21 / 0 |
| transporte | `N` (1 rad desde su posición guardada) | 21 | 1590,1 → 31,7 | 52,9 → 5,6 | 21 / 21 / 0 → 0 / 0 / 0 | 1852431 / 1852431 → 0 / 0 | 0 / 0 | 0 / 21 |
| transporte | `E_1`, salida y retorno del dominio | 6 | 2806,5 → 61,2 | 131,0 → 5,4 | 6 / 6 / 6 → 0 / 0 / 0 | 528684 / 528684 → 0 / 0 | 0 / 6 | 6 / 0 |
| transporte | `E_1`, junto al pliegue (2,72–2,80 rad) | 9 | 2790,8 → 60,5 | 129,8 → 5,3 | 6 / 6 / 9 → 0 / 0 / 0 | 529072 / 529072 → 0 / 0 | 0 / 9 | 9 / 0 |
| transporte | cambio real: `excCyl` ± 0,05 | 6 | 2047,6 → 2073,4 | – | 6 / 6 / 6 → 6 / 6 / 6 | 488856 / 488856 → 488856 / 488856 | 0 / 0 | 6 / 0 |

«Antes»: checkout limpio de la base `9ccefdcfe` (`benchmark-before-9ccefdcf-01`). «Después»: rama de trabajo con las cinco reparaciones (`benchmark-after-03`). Mismo arnés, mismo equipo, misma sesión. La base no cuenta resoluciones ni actualizaciones equivalentes; por eso esas dos columnas solo tienen valor «después».

Lectura:

- Mover `E_1` dentro del dominio, al salir y volver y junto al pliegue ya no publica
  revisiones de `w_2`/`a_3` ni reconstruye sus slices, y `t_2` no se resuelve (21
  actualizaciones equivalentes de 21). `v_2`, cuyo objetivo sí se mueve con `E_1`, se
  resuelve en cada paso; `u_1`, de contacto tangente, también.
- Mover `N` ya no publica revisiones de `w_2`/`a_3`, y `v_2` no se resuelve.
- Un cambio real (`excCyl`) sigue publicando una revisión por locus afectado y
  reconstruyendo sus slices: es el coste de reconstruir los dos loci del agujero, ajeno a
  estas reparaciones. En esta serie cuesta entre un 1 % y un 4 % más que en la base; la
  diferencia incluye el análisis de conductores anidados de cada huella y no se ha
  separado de la variabilidad entre ejecuciones.
- El contorno transportado no añade revisiones al mover `E_1` ni `N`.

Límites: series cortas (no informan de memoria retenida ni de estabilidad general);
pintura fuera de pantalla, no latencia interactiva de una ventana real; un equipo.

## 3. Modelo corregido

Paquete: `models/regression/post-r1-locus-on-intersection/` (manifiesto, original
byte-exacto, receta, variante derivada, resultados esperados). El original no se modifica
y ninguna variante se promociona a curada, estable o aprobada por el autor.

**Transporte.** `N → (X(N), R(N)) → Y(N)`: `X(N)` es el punto de la intersección espacial
que la construcción representa por sus proyecciones `W` (alzado) y `R` (planta); `R` es la
raíz lejana de `Ray(P, N)` con la circunferencia base `p`, pie de la generatriz del cono
que pasa por `X(N)`. La receta única es `transport-commands.txt` (16 comandos públicos):
parámetro del pie sobre `p` por `atan2`, como número calculado con procedencia explícita;
`Point(e_2, u)`; `LocusLength` desde el origen existente `POrigen`; ángulo desarrollado
igual a esa longitud dividida por el radio de la esfera; verdadera magnitud del segmento
de generatriz tomada como la toma el desarrollo existente. No reutiliza `text3` ni ningún
auxiliar dedicado, no introduce asociaciones espaciales por coordenadas y no mueve `E_1`.

**Premisas de una sola curva cerrada** en la configuración guardada: el cilindro queda
dentro del cono para toda la vuelta de `N`; la raíz lejana existe para todo `N`; el
parámetro del pie cruza el origen del desarrollo, a lo sumo, como costura ordinaria; ninguna
generatriz es tangente al cilindro. La periodicidad de `N` no implica cierre ni continuidad
del desarrollo en otra configuración. La familia `Q` (raíz cercana) está en la prolongación
del cono más allá de su base y no es un segundo agujero.

**Comprobaciones** (`PostR1AuthorModelRegressionTest`, 3 casos, sobre el original
construido en memoria y sobre la variante versionada):

| Aspecto | Resultado |
|---|---|
| Geometría | Distancia máxima a la referencia matemática independiente en 72 parámetros: 1,5·10⁻⁸ (tolerancia 10⁻⁶, la de la longitud estimada que da el ángulo desarrollado) |
| Imagen conjugada | Ausente por construcción: distancia mínima a la curva conjugada 2,8 unidades |
| `E_1` y su reflejado | El contorno no lee `E_1`: misma función bit a bit en cuatro posiciones, incluidas las simétricas; cero revisiones |
| Arrastres | Cero revisiones al mover `N` y al mover `E_1`; una revisión con el cambio estructural `excCyl` |
| Guardado y reapertura | Guarda; reabre con las mismas identidades durables de `trHole`, `MP` y `POrigen` y la misma función bit a bit |
| Eventos | Con `excCyl = 3,8` el cilindro sale del cono en parte de la vuelta: evaluaciones `VALID` y `DEPENDENCY_UNDEFINED`, estados explícitos, sin excepción |
| Evaluación | Slice fiel; evaluación puntual válida en los 72 parámetros |
| Certificación | `NOT_ESTABLISHED`: sin componente certificada |
| Dibujo y consumo | Los de la evidencia exploratoria vigente; el uso condicionado es el bloque 3 |

Una imagen cerrada no equivale a `COMPLETE`. Mientras V no esté implementado, el contorno
transportado se dibuja como evidencia exploratoria y no admite `Point(L, u)`, longitud
entre posiciones ni intersecciones con soluciones.

**Oráculo independiente.** `geocedg/validation/post-r1-locus/`: script de la reconstrucción
matemática de R1 sin cambios, sus entradas reducidas y la referencia esencial, con hashes
y regeneración documentada. Con esas entradas reproduce byte a byte las tablas de R1.

## 4. Índice de afirmaciones

| Afirmación | Comando | Resultado |
|---|---|---|
| A, F7, F6, B, A′ | pruebas `org.geocedg.common.locus.PostR1*` (módulo compartido) | 24 casos en verde; ablaciones descritas en §1 |
| Modelo original y transporte | `org.geocedg.desktop.PostR1AuthorModelRegressionTest` | 3 casos en verde |
| Variante derivada | `run-harness.ps1 -Scenario variant` | guardada y reabierta; SHA-256 en el manifiesto |
| Latencias y contadores | `run-harness.ps1 -Scenario benchmark` | tabla de §2 |
| Referencia matemática | `node geocedg/validation/post-r1-locus/oracle/geometry.js …` | tablas idénticas a las de R1 |
| Verificación del candidato | perfiles `STATIC`, `PHASE` e `INTEGRATION` | en el [registro](../validation/post_r1_locus_repairs_progress.md) |

## 5. Límites y pendientes

1. Una asignación no reclamada sigue publicando una revisión al salir y al volver al
   dominio de incidencia (§1.1).
2. B no cubre intersecciones locus–locus, productores internos ni resultados con contacto
   tangente o identidad no determinista (§1.4).
3. F6 no cubre la construcción de macro de una herramienta de usuario (§1.3).
4. F7 protege la entrada algebraica; quien llame directamente a la operación de kernel
   `createSemanticPoint` sigue siendo responsable de no pasar auxiliares ajenos (§1.2).
5. La redefinición del dueño de un selector por su propia definición no se ha triado (§1.2).
6. La pintura se mide fuera de pantalla; la latencia de una ventana real no se ha medido.
7. El contorno transportado no está certificado; su presentación y su consumo dependen del
   bloque 3.

## 6. Impactos

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/architecture/post_r1_locus_repairs.md;
  docs/architecture/post_r1_locus_conditioned_use_design.md
```

Guía de usuario: sin cambios. Ningún comando, herramienta, menú, formato persistido o
exportado cambia; el nuevo rechazo `DEDICATED_AUXILIARY_OWNED` se explica en su propio
mensaje e indica la alternativa.

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, supported runtime/toolchain, Gradle, Conda,
packaging, download or environment contract changes. The reference oracle needs Node.js
only to be regenerated; no verification profile runs it.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE SELECTION ADDED
```

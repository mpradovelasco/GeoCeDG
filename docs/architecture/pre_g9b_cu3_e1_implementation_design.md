# PRE-G9B-CU3 — Diseño de implementación del incremento E1 bajo la autoridad constructiva

| Campo | Valor |
|---|---|
| Carácter | Diseño de implementación del incremento E1 (`AGENTS.md` §9). No es contrato: la autoridad normativa es [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) 1.0, la sección 12 del [modelo certificado](../../geocedg/specs/locus/locus-v2-certified-construction-model.md), la enmienda 1.4 del [contrato de intersecciones](../../geocedg/specs/locus/locus-v2-intersections.md) y el [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md). Ante cualquier diferencia prevalecen ellos |
| Autorización | [registro de aprobación](../validation/pre_g9b_cu3_constructive_authority_approval_record.md) §2 a §10 y §16 (presupuestos y continuación de E1, 2026-10-25) |
| Base | E0 en `1f81666ea90f4ca53a9d782b4f905cc839c61cf1`, sobre `3cd0f710`; rama local `phase/pre-g9b-cu3-implementation` |
| Diseños de `CU3` que se conservan | [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md), [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md), [normalización, admisibilidad y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md), [estrategia](pre_g9b_cu3_implementation_strategy.md), con las revisiones de D-AUT-11 bajo la política constructiva |
| `selfApproved` | `false` |

## 1. Alcance de E1

E1 entrega construcciones útiles con integridad consultable. Sus piezas, por orden de dependencia:

| Pieza | Contenido | Propuesta §6.2 | Filas de la matriz |
|---|---|---|---|
| E1.1 | integridad de las relaciones, registros de evidencia, propagación por premisas, requisitos de consumidor; política de autoridad; presupuesto `cu3-exact-evaluation-budget/v1`; torre exacta | (a), (b) | V-AUT-INT-1 a 3 |
| E1.2 | registro de política persistido, lectores anteriores que fallan cerrados, evento explícito de adopción con informe de impacto | (c) | V-AUT-ADOPT-1 a 7 |
| E1.3 | tabla de clase `constructive-class/v1`, programa constructivo del cono de una relación, semántica de los puntos sobre caminos, sustitutos de frontera, decisiones heredadas D1 a D5 | (b), (e′) | V-AUT-PROV-1 y 2, V-AUT-NUM-1 a 6 |
| E1.4 | evaluación exacta del cono en `Σ_C` (M3 de producto) y evaluación por intervalos de los valores ideales | (b), (d) | V-AUT-PROV-1, V-M3-* bajo AUT-7 |
| E1.5 | transferencia de los certificados de `CU2` por margen y por estructura | (d) | V-AUT-TRF-1 a 6 |
| E1.6 | `SUBSTITUTE_GENERIC` versión 1, `ENCLOSURE_COMPATIBLE` y recalificación de G8B-R1 en la adopción | (e) | V-AUT-RLX-1 a 7, V-AUT-ADOPT-8 |
| E1.7 | `Point(L, D)` e `Intersect(L, r, D)` bajo la autoridad constructiva | (f) | V-A-*, V-MAND-*, V-DYN-*, V-AUT-RLX-7 |
| E1.8 | piezas I-1, I-2, I-3 e I-6 de `CU3-A` que exige la puerta G-A (encierros del residuo, grados de evidencia, vía certificada H02, regla de conflicto, taxonomía de contactos, ejes H06) y compatibilidad | — | §3.4 a §3.9 de la matriz |

Al final de E1 se ejecutan las puertas G-AUT-1, G-AUT-3 a G-AUT-8 y G-A. El incremento E2 (certificado
de identidad constructiva) no empieza antes.

## 2. Capas y paquetes

- **Núcleo compartido, paquete nuevo** `org.geocedg.common.kernel.locus.constructive`: política,
  integridad, presupuesto, torre exacta, tabla de clase, programa del cono, evaluadores exacto y por
  intervalos, transferencia, `SUBSTITUTE_GENERIC`, adopción e informe de impacto.
- **Núcleo compartido, paquetes existentes de GeoCeDG**, solo con adiciones:
  - `locus/intersection`: una entrada de evaluación del programa de clase v3 con las constantes
    sustituidas por intervalos (§7), la procedencia de cada constante capturada y los accesores de
    solo lectura que la transferencia necesita; los contratos, certificados, huellas y claves de
    `CU2` no cambian;
  - `spatial/identity`: el registro de política dentro de la sección `geocedgSpatial` (§4);
  - `algos`: el algoritmo de `Point(L, D)` e `Intersect(L, r, D)`.
- **Clases heredadas**, con el mínimo necesario y su registro en `docs/upstream/modified-files.yml`:
  - `Construction.getConstructionXML`: escribir la sección también cuando solo contiene la política;
  - `CmdPoint` y `CmdIntersect`: despacho de las formas nuevas.
- **Escritorio:** filas del inspector (integridad, premisas, causa) y la acción explícita de adopción
  con su informe. Ninguna lógica geométrica.
- **Capas externas:** bancos de coste y contadores en `tools/benchmark/pre-g9b-cu/`, informes en
  `docs/validation/`. Los presupuestos son límites semánticos del núcleo; el banco solo los mide.

## 3. Integridad, política y presupuesto

- `RelationIntegrity` es un tipo propio (`STRICT`, `RELAXED`, `NOT_ESTABLISHED`, `CONTRADICTED`). La
  propagación aplica la regla de la especificación §6: la integridad del método, acotada por la peor
  premisa; una premisa contradicha anula la derivación; una refutación solo es rigurosa con todas sus
  premisas `STRICT`.
- `RelationIntegrityRecord` registra la relación, el método, la clase de evidencia, las premisas
  (relaciones de procedencia, certificados transferidos, hechos de existencia y sustitutos de
  frontera), la causa tipada y las versiones de la política. Es evidencia de revisión: no se
  persiste ni entra en tokens, selectores ni en el grafo.
- Un consumidor declara la integridad mínima que exige; si no se cumple, falla de forma explícita
  con causa tipada.
- `AuthorityPolicy` nombra la autoridad y las versiones de la tabla de clase, la política relajada,
  el certificador y la transferencia. Sin registro, el documento es
  `legacy-representation-authority/v1`.
- `ExactEvaluationBudget` fija `cu3-exact-evaluation-budget/v1` (100 000 operaciones, profundidad 8 y
  4 096 bits), con un contador determinista por evaluación. Su agotamiento lanza una excepción
  tipada que el consumidor convierte en `EXACT_EVALUATION_BUDGET_EXHAUSTED`, con sus contadores en el
  diagnóstico. El límite no se amplía nunca de forma automática.

## 4. Registro de política

**Lugar.** Dentro de la sección `geocedgSpatial` de la construcción, como el registro plano
`<authorityPolicy authority="constructive-authority/v1" classTable="constructive-class/v1"
relaxedAdmission="constructive-relaxed-admission/v1" certifier="constructive-identity-certificate/v1"
transfer="cu2-certificate-transfer/v1"/>`, delante de los registros de identidad.

**Por qué ahí.** El lector actual analiza esa sección de forma estricta: un registro de nombre
desconocido da `Unknown geocedgSpatial record` y el código `MALFORMED_RECORD`, aborta la carga y no
carga nada. Un elemento nuevo directamente bajo `<construction>` solo se anotaría en el registro de
errores y se ignoraría, de modo que un lector anterior leería el documento con otra autoridad, lo que
AUT-2 prohíbe.

**Escritura.**

- El registro de identidades guarda la política del documento y la escribe en `writeSpatialSection()`
  y en las vistas de redefinición (`writeSpatialSectionForRedefine`,
  `writeSpatialSectionForRetainedRedefines`), con los mismos bytes, para que el intercambio exacto de
  la sección en la reconstrucción siga funcionando.
- `Construction.getConstructionXML` escribe la sección cuando hay registros **o** una política
  constructiva. Un documento con la política heredada conserva sus bytes.
- Las secciones de cierre del portapapeles y de las macros (`writeClosureSection`,
  `writeMacroTemplateClosure`) no la contienen nunca: rige la política del destino.

**Lectura por propósito.**

| Propósito | Efecto del registro |
|---|---|
| `NATIVE_OR_UNDO_RESTORE`, `REDEFINE_REBUILD`, `ORDINARY_EDIT_REBUILD`, `ROLLBACK_RESTORE`, en una construcción que no es de macro | fija exactamente la política leída; sin registro, la política heredada |
| `CLIPBOARD_IMPORT`, construcción de macro | `MALFORMED_RECORD`: esos contenidos nunca la llevan |
| `GENERIC_MERGE` | la sección entera ya falla con `GENERIC_MERGE_FORBIDDEN` |

Los atributos se comprueban contra una lista cerrada; un valor distinto de las versiones de esta
compilación falla cerrado. El borrado del estado vivo del registro devuelve la política heredada, de
modo que Deshacer, Rehacer y reabrir restauran exactamente la del estado restaurado.

**Adopción.** `AUTHORITY_POLICY_ADOPTION`:

1. calcula el informe de impacto con la política heredada vigente y con la constructiva, sin
   publicar nada: las relaciones cuya integridad o admisión cambia (raíces de G8B-R1 recalificadas,
   resultados que la evidencia nueva contradice), con su causa;
2. fija la política en una sola operación y guarda un punto de Deshacer;
3. no cambia ninguna identidad, token, selector ni clave de linaje;
4. no tiene vuelta automática: volver a la política anterior es Deshacer.

En un documento vacío el informe está vacío, pero el evento sigue siendo explícito y queda
registrado. Crear `Point(L, D)` o `Intersect(L, r, D)` en un documento sin la política constructiva
falla cerrado con `CONSTRUCTIVE_AUTHORITY_REQUIRED`; el escritorio ofrece entonces la adopción con
su informe.

## 5. Tabla de clase y programa del cono

- **Cono de una relación.** Desde los objetos de la relación (el punto del corte del locus, el
  objetivo y el punto `D`), se recorren las dependencias hasta las entradas libres. Cada nodo
  registra su algoritmo, sus entradas por posición estructural, su rol de salida, el estado de
  selección heredado de las intersecciones y su identidad durable (`PersistentGeoId`) cuando la
  tiene. Nunca se identifica un nodo por su etiqueta.
- **Clasificación** por la clase del algoritmo, nunca por un valor:
  - en la clase: rectas, segmentos y rayos por dos puntos; paralelas y perpendiculares;
    intersecciones recta–recta y recta–circunferencia con la selección heredada; puntos medios;
    circunferencias por centro y punto o por centro y radio; puntos sobre caminos;
  - entradas libres (puntos y números; su valor binary64 es su definición) y ejes (constantes);
  - frontera declarada: cualquier otro algoritmo (por ejemplo `Rotate`), y los de las categorías D2 a
    D5 de la especificación §3.2.
- **Puntos sobre caminos.** Sobre una recta, el punto ideal es la proyección ortogonal de sus
  coordenadas persistidas sobre la recta ideal: es la composición exacta de `pointChanged` y
  `pathChanged` de `GeoLine`. Sobre un segmento o un rayo, además, la pertenencia a la parte limitada
  se decide con signo riguroso (D1); el recorte del algoritmo heredado nunca decide.
- **D1.** Las decisiones internas por tolerancia de los algoritmos de la clase (puntos iguales,
  paralelas, tangencia, caso especial de un punto ya incidente, pertenencia con tolerancia) se
  sustituyen por predicados de signo estricto; un caso no decidido da `NOT_ESTABLISHED`.

## 6. Evaluadores

- **Exacto (M3 en `Σ_C`).** Evalúa el cono en una torre de extensiones cuadráticas de los
  racionales, con las entradas libres y los sustitutos de frontera como racionales exactos de su
  valor binary64. Decide signos exactos con normas conjugadas. Reproduce la selección heredada de las
  intersecciones recta–circunferencia con el estado capturado; una raíz doble o un empate da
  `SELECTION_NOT_RECONSTRUCTED`. Cuenta operaciones, profundidad y bits con
  `cu3-exact-evaluation-budget/v1`. Fuera de la clase da `EXACT_EVALUATION_OUT_OF_CLASS`. Con un
  sustituto no constructible, su decisión vale para el sustituto y, por sí sola, nunca da `STRICT`.
- **Por intervalos (valores ideales).** Evalúa el mismo cono con aritmética de intervalos con
  redondeo hacia fuera. Admite además algoritmos de frontera cuya semántica ideal se puede encerrar
  con rigor: el giro por un ángulo, con el valor binary64 del ángulo como definición y los encierros
  de seno y coseno ensanchados más allá del error de un ulp que garantiza la plataforma, y los puntos
  sobre caminos de frontera. Un algoritmo sin encierro riguroso deja el valor sin encerrar, y quien lo
  necesita falla cerrado. Ninguna estimación de error ni ninguna caja ancha sustituye a un encierro.

## 7. Transferencia de `CU2`

- **Procedencia de las constantes.** La captura de clase v3 registra, para cada constante de un
  operando, del conductor o de un parámetro de paso, el objeto del que procede, por su identidad
  durable. Es un registro aditivo: el programa y su firma no cambian.
- **Encierro de transferencia.** Para cada constante, la envolvente de su valor capturado y del
  encierro riguroso de su valor ideal (§6).
- **Margen.** Una entrada nueva del modelo de cajas evalúa el programa de clase v3 con las
  constantes sustituidas por sus encierros de transferencia. Las tablas estructurales del programa,
  que dependen de igualdades exactas de bits, son premisas de estructura. Si todas las decisiones
  abiertas que la relación usa conservan su signo estricto, la decisión se transfiere
  (`TRANSFERRED_BY_MARGIN`).
- **Estructura.** La simetría de pliegue, la totalidad y la periodicidad estructurales se transfieren
  cuando todas sus premisas estructurales son `STRICT` bajo la autoridad constructiva.
- **N1 y N2.** La correspondencia estructural la da el propio programa capturado, con sus estados de
  selección y de etiqueta; el registro de transferencia cita la huella de la revisión consumidora,
  con un accesor de solo lectura nuevo.
- **Sin transferencia demostrada:** `EVIDENCE_INSUFFICIENT` con su causa
  (`CORRESPONDENCE_NOT_DEMONSTRATED`, `OPEN_MARGIN_INSUFFICIENT`, `CLOSED_DECISION_NOT_REDECIDED`,
  `STRUCTURAL_PREMISE_NOT_STRICT`, `BUDGET_EXHAUSTED`), y `P ∈ L` no se publica como `STRICT`. El
  certificado de origen no cambia en ningún caso.

## 8. `SUBSTITUTE_GENERIC` y recalificación de G8B-R1

- La versión 1 sigue literalmente la especificación §7.1: coordenadas de frontera, conjunto de
  perturbaciones de SplitMix64 con la condición de rango, cinco precondiciones, comprobación de
  ramas, celda operativa por revisión y retirada por un `NOT_AN_IDENTITY` del certificado de E2.
- `ENCLOSURE_COMPATIBLE` solo se aplica a raíces con un corchete de cambio de signo verificado contra
  la representación, con el umbral de G8B-R1 sobre el residuo constructivo.
- En un documento constructivo, cada raíz que G8B-R1 admite se recalifica: `STRICT` si su corchete se
  certifica contra el objetivo constructivo (encierros del residuo constructivo con signos opuestos
  estrictos y derivada sin el cero), `RELAXED` por `ENCLOSURE_COMPATIBLE`, `NOT_ESTABLISHED` en otro
  caso. En los documentos heredados, G8B-R1 no cambia.

## 9. `Point(L, D)` e `Intersect(L, r, D)`

- Un algoritmo nuevo del núcleo resuelve, en orden y con fallo cerrado, los cuatro hechos de la
  Etapa A bajo la autoridad constructiva: procedencia de `D` y su soporte por identidad durable;
  punto constructivo `D*` y correspondencia `t*(D)`, exacta o encerrada; pertenencia `P ∈ L` por la
  transferencia de §7; y, en la segunda forma, `P ∈ r`.
- `P ∈ r` es `RELAXED` por `SUBSTITUTE_GENERIC` cuando la relación depende de sustitutos no
  constructibles, y `STRICT` cuando la evaluación exacta la decide con premisas constructibles. Sin
  evidencia, el punto queda indefinido con su causa; `Point(L, D)` sigue definido.
- La posición materializada es una representación, con su error declarado. El punto se serializa
  como una orden ordinaria con su identidad durable; no se persiste ningún parámetro, certificado ni
  integridad.

## 10. Validación de E1

- Cada pieza llega con sus pruebas focalizadas y adversarias, en la selección de `CU3`, sobre
  construcciones sintéticas y sobre la copia del modelo del autor.
- La compatibilidad con un lector anterior (V-AUT-ADOPT-7) se prueba además con el producto de la
  base, construido aparte desde `git archive`, sobre un documento con la política.
- Los contadores de la evaluación exacta y de `SUBSTITUTE_GENERIC` se registran en el banco de la
  puerta G-AUT-8.

## 11. Condiciones de parada específicas

Además de las del registro de aprobación §10:

- si la transferencia por margen exigiera cambiar una decisión, un certificado, una huella o una clave
  de `CU2`;
- si el registro de política no pudiera hacer fallar cerrado al lector de la base;
- si un encierro de un valor ideal necesario no pudiera ser riguroso;
- si una pieza de `CU3-A` contradijera una revisión de D-AUT-11.

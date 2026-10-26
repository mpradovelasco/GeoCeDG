# PRE-G9B-CU3 — Registro de la implementación de T7-a: `cu3-certified-admission/v2`

| Campo | Valor |
|---|---|
| Carácter | Candidato técnico entregado para la revisión del autor. No es una aprobación ni un cierre; `CU3` no está `PASS — AUTHOR APPROVED` |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Decisión autoral sobre T7-a», de 2026-10-26 (§1) |
| Referencia | candidato T6 `3f0968385920e36b2b48191a4f6e736c4fc11c09` ([registro de T6](pre_g9b_cu3_enclosure_reuse_record.md)); [caracterización de T7-a](pre_g9b_cu3_t7a_characterization_record.md) |
| Puerta N | [registro de la Puerta N](pre_g9b_cu3_t7a_gate_n_record.md) y enmienda 1.5 de [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md), commit `22891c7b` |
| Candidato | `9f6f27f265725b27673f5c37880947ad6c4f2399` (árbol `abe10e3d4eda338be2b3c36373c4b09af21e7fdd`), rama local `phase/pre-g9b-cu3-implementation`, sin publicar |
| Evidencia | [`pre-g9b-cu3-t7a-implementation-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-t7a-implementation-evidence.json) |
| `selfApproved` | `false` |

## 1. Autorización

> DECISIÓN AUTORAL: T7-a APROBADA COMO DIRECCIÓN ARQUITECTÓNICA. IMPLEMENTACIÓN AUTORIZADA CON
> PUERTA NORMATIVA Y DE VALIDEZ BLOQUEANTE.

- **Alcance:** solo T7-a, es decir, anticipar la decisión exacta de existencia de una raíz doble
  en el certificado de conteo. No incluye T7-b, T5 ni otros cambios del algoritmo de
  certificación.
- **Orden:** primero la Puerta N (contrato normativo y validez), documentada antes de tocar el
  producto; después la Puerta I (implementación focal).
- **Decisiones normativas aprobadas:**
  - la v2 consulta la decisión exacta antes de buscar el signo del valor crítico;
  - se aceptan las certificaciones que la v1 dejaba en el límite de trabajo (los dos casos de
    `rbCone = 7,5`);
  - `MAXIMUM_EVALUATIONS = 4096` no cambia;
  - ningún umbral, tolerancia ni aproximación nuevos.
- **Compatibilidad:**
  - solo en el alcance constructivo; los documentos bajo `legacy-representation-authority/v1`
    no cambian;
  - no hay reasociación de identidades, ni tokens incompatibles, ni evidencia obsoleta;
  - no cambia ningún formato durable.
- **Verificación:** se congela un candidato y se ejecuta `INTEGRATED_PHASE`; `INFRA_UNIT` puede
  usar PowerShell 7.6.6 aislado.

## 2. Puerta N

El [registro de la Puerta N](pre_g9b_cu3_t7a_gate_n_record.md) contiene la demostración completa.
En resumen:

- **Validez y continuidad.** Una auditoría independiente de la cadena de encierros comprobó que
  un encierro no nulo de `ConstructiveResidualJetSource` en una caja certifica tres cosas para
  todo `t` de la caja:
  - que la fuente está definida;
  - que sus elecciones discretas son constantes, así que `h` es una sola función `C²`;
  - que los intervalos contienen `h`, `h'` y `h''`.

  Todas las decisiones se toman con signos certificados; ninguna depende de la monotonía.
- **Premisas.** Son la precisión de `StrictMath.cos` y `StrictMath.sin`, ensanchadas dos binary64,
  y la aritmética IEEE. Son las mismas de todos los certificados de la v1.
- **La regla.** Calcula un único encierro de `h` en el corchete del punto crítico, que es la
  primera caja de la búsqueda de signo de la v1, reutilizada.
  - Si el encierro no se obtiene, el resultado es el de la v1.
  - Si contiene el cero, consulta la decisión exacta antes de subdividir; solo una raíz doble
    termina la búsqueda.
  - En cualquier otro caso, la v1 continúa desde ese mismo encierro, con el mismo trabajo.
- **Fallos.** Se distinguen la invalidez geométrica, la falta de demostración de validez, el
  fallo numérico de una subcaja y el agotamiento del presupuesto.

La enmienda 1.5 de la especificación fija la política v2 solo bajo `constructive-authority/v1`.

## 3. Qué cambia en el producto

| Archivo | Cambio |
|---|---|
| `RootCountCertificate2D` | La regla de §2, solo para una fuente constructiva. El encierro del corchete se pasa a la búsqueda de signo de la v1, que no lo vuelve a evaluar |
| `CertifiedAdmissionPolicy2D` | `CONSTRUCTIVE_VERSION = cu3-certified-admission/v2` y `version(source)`. `VERSION` (v1) no cambia |
| `IntersectionExistenceEvidence2D` | La versión de la política es la v2 cuando la evidencia lleva la recalificación constructiva, y la v1 en otro caso |
| `CertifiedContactAnalysis2D`, `LocusIntersectionSolver2D` | Los diagnósticos de los certificados y de la vía certificada nombran la versión de su configuración |

El cambio neto en el producto son 88 líneas añadidas y 23 suprimidas en cinco archivos, incluida
la documentación de las clases. El
commit `00dc86f1` aplanó los finales de línea mixtos de `LocusIntersectionSolver2D.java`; el commit
`247f335c` los restauró desde el blob base, y el cambio neto en ese archivo es una línea.

**Lo que no cambia:**

- los límites (`MAXIMUM_EVALUATIONS`, profundidad, bisecciones, ancho de corchete);
- los presupuestos del certificado de identidad constructiva;
- las reglas de fallo cerrado, los grados, la regla de conflicto y la admisibilidad;
- los tokens, la persistencia y el comportamiento de los documentos heredados.

## 4. Pruebas adversarias

Las dos clases nuevas están en la selección `pre-g9b-cu3.shared`.

`PreG9BCu3EarlyExactDecisionTest` compara la v2 con el procedimiento de la v1 sobre el mismo
residuo de una tangencia regular. Las fuentes de prueba solo ensanchan intervalos hasta contener
el cero o rechazan cajas, así que todo encierro sigue siendo riguroso.

| Caso de la autorización | Prueba y resultado |
|---|---|
| Contacto doble exacto | `theExactDecisionComesBeforeTheSubdivisionWithTheSameEvidence`: la misma raíz doble, el mismo corchete, la misma evidencia y menos evaluaciones |
| Corchete sin contacto exacto, contacto fuera del corchete, signo discrepante de `h''` (contactos de orden superior: la configuración no los devuelve) | `whatDoesNotEstablishTheFormLeavesTheCertificateOfVersionOne`: certificado y trabajo idénticos a la v1, con y sin corchete ancho |
| Decisión exacta contradictoria con un valor crítico certificado | `aBracketWhoseEnclosureExcludesZeroIsNeverAskedForAnExactDecision`: `NONE`, como en la v1, sin ninguna consulta |
| Fallo de encierro en una subcaja y rama `INVALID` | `aNumericalFailureOfASubBoxOfTheBracketIsNotALackOfValidity`: la v1 da `INVALID`; la v2 da la raíz doble con contacto exacto, e `INVALID` como la v1 sin él |
| Validez no establecida | `aBoxOrABracketWithoutValidityIsRefusedAsInVersionOne`: la caja o el corchete sin validez se rechazan igual que en la v1, sin consulta |
| Presupuesto agotado | `theWorkLimitNoLongerHidesTheDoubleRootAndKeepsItsValue`: la v1 agota 4 097 evaluaciones y la v2 da la raíz doble dentro del límite. Sin contacto exacto, la v2 agota igual que la v1. Una fuente no constructiva nunca anticipa |
| Versión de la política | `thePolicyVersionFollowsTheConfiguration` |

`ConstructiveEarlyExactDecisionTest` trabaja sobre la réplica del modelo del autor:

| Caso de la autorización | Prueba y resultado |
|---|---|
| Los dos casos de `rbCone = 7,5`, tangencia regular | `theContactAtTheWorkLimitOfVersionOneIsTheDoubleRoot`: raíz doble de orden `TWO`, `EXISTENCE_EXACT`, evidencia y diagnóstico en la v2, no admisible para el consumo puntual |
| Cambios de configuración, Deshacer, Rehacer y reapertura | `theDecisionFollowsTheConfigurationThroughUndoRedoAndReopen`: ni la versión ni el certificado se persisten |
| Pliegues, costura y cambios de rama, revisión y configuración | `everyExactExistenceIsADoubleRootUnderVersionTwoInEveryConfiguration`: las cuatro intersecciones en base, `H +40`, `D_1 ×3`, `rbCone` 3 y 7,5, y restauración |
| Documentos heredados | `aLegacyDocumentKeepsVersionOne` |

**Prueba de mutación.** Al desactivar la anticipación fallan exactamente las seis pruebas que la
ejercitan. Las de respaldo, versión y documentos heredados siguen pasando.

**Regresiones.** Las selecciones `PHASE` de `CU3` y `CU2` pasan en ambos modos de la comparación
(§5). Las de `CU1`, `CU0` e `INTEGRATION`, en la campaña (§7).

## 5. Comparación con T6

### 5.1 Método

La herramienta versionada `run-cu3-t7a-characterization.ps1 -Candidate` (commit `b765f3e8`)
genera una copia sombra del certificado del candidato con la anticipación detrás de un
interruptor. Así, sobre el mismo producto y la misma fuente, `current` es la v1 (el procedimiento
de T6) y `t7a` es la v2.

### 5.2 Certificados locales (modo `compare`)

| Corpus | Llamadas | Misma decisión | Distinta | Evaluaciones v1 → v2 | Consultas exactas nuevas |
|---|---:|---:|---:|---:|---:|
| Selecciones compartidas de `CU3` y `CU2` | 69 | 69 | 0 | 33 826 → 7 947 | 0 |
| Selecciones de escritorio de `CU3` y `CU2` | 668 | 668 | 0 | 379 940 → 367 449 | 0 |
| Modelo del autor, 19 configuraciones | 122 | 120 | **2** | 88 800 → 5 238 | 2 (las agotadas, 31,8 ms) |

- **Las 2 diferencias** son las únicas llamadas en que la v1 agota su límite de trabajo:
  `o_1·h_2` en `rbCone = 7,5`, `t = 0`. La v2 da `ONE_DOUBLE` de orden `TWO`.
- **Ningún `INVALID` y ningún límite alcanzado en la v2.**
- **Consultas exactas nuevas.** La caracterización de la hipótesis hacía 14 consultas exactas
  donde el producto no consultaba. La implementación solo consulta cuando el encierro del corchete
  contiene el cero, y no hace ninguna salvo las dos agotadas.
- **Escritorio.** La mayoría de sus llamadas son de la configuración heredada, sin anticipación,
  y en ellas el trabajo es idéntico.
- **Pruebas en ambos modos:** 451 compartidas y 86 de escritorio pasan.

### 5.3 Soluciones publicadas

| Comparación | Objetos | Iguales tal cual | Iguales salvo identidades, recuentos, trabajo y nombre de la versión | Distintos |
|---|---:|---:|---:|---:|
| Producto T6 (`3f096838`, a través de `db752438`) frente a la v2 | 140 | 77 | 61 | 2 |
| Hipótesis caracterizada frente a la v2 | 140 | 77 | 63 | 0 |

- Las identidades `geo:<hash>` las genera cada carga de la copia del modelo.
- Los 61 objetos solo difieren en esas identidades y en el número de encierros de los
  diagnósticos (telemetría). Además, nombran `cu3-certified-admission/v2` donde T6 nombraba la v1.

**Las 2 diferencias** son `u_1` y `cu3X4` (`o_1·h_2`) en `rbCone = 7,5`:

| Campo publicado | T6 | v2 |
|---|---|---|
| Certificado | `NOT_CERTIFIED`, límite de trabajo | `ONE_DOUBLE` en el parámetro exacto `0` |
| Grado | `LOCALLY_VERIFIED` | `EXISTENCE_EXACT` |
| Garantía y método | `ESTIMATED_ERROR`, `SAFEGUARDED_DERIVATIVE` | `CERTIFIED_ERROR_BOUND`, `CERTIFIED_INTERVAL` |
| Multiplicidad y orden | no establecidos | 2, `TWO` |
| Diagnósticos | `CONTINUATION_AMBIGUOUS` | `CONTRADICTED_BY_CERTIFIED_EVIDENCE` (el candidato tangente se reclasifica como la raíz doble) y `CERTIFIED_EXISTENCE_ESTABLISHED` |
| Clase de contacto | `TANGENT_ESTABLISHED` | `TANGENT_ESTABLISHED` |
| Admisible | no | no |
| Recalificación constructiva | `NOT_ESTABLISHED` | `NOT_ESTABLISHED` |

**Por qué cada cambio observable está justificado:**

- **Existencia.** La evidencia sube de verificación local a existencia exacta por la regla aprobada
  (§1, decisión sobre el límite de trabajo).
- **Clasificación.** Se mantiene; la multiplicidad pasa a establecida.
- **Admisibilidad.** Ningún cambio.
- **Integridad.** El punto estructural `X3` es `STRICT` en ambos casos.
- **Causa.** El diagnóstico de reclasificación es el de la regla de conflicto (CU3.4).
- **Tokens.** El identificador de la solución es de revisión (`locus-root-local/v1`, identidad no
  establecida) en ambos casos. Su resumen incluye las identidades de cada ejecución; no se persiste
  y no se reasocia.

## 6. Rendimiento frente a T6

- **Condiciones.** Misma estación y mismo día. Sonda versionada de rendimiento con 21 arrastres
  por medida. Ahora registra también el primer arrastre y la serie completa.
- **Modos.**
  - `product`: el candidato tal como se construye, cinco ejecuciones.
  - `t7a`: la v2 a través de la sombra, dos ejecuciones.
  - `off`: la v1 a través de la sombra del candidato, que es el procedimiento de T6; cinco
    ejecuciones.
- Mediana / percentil 95 en ms:

| Modo | E3 `H` | E3 `B` | Smoke `H` | Smoke `B` | Adopción E3 (ms) |
|---|---|---|---|---|---:|
| `product` A | 44,7 / 56,8 | 44,3 / 51,2 | 67,5 / 99,6 | 66,6 / 67,9 | 2 262 |
| `product` B | 46,2 / 65,7 | 44,5 / 49,1 | 67,8 / 75,9 | 69,3 / 81,0 | 2 468 |
| `product` C | 45,8 / 56,7 | 44,4 / 53,2 | 66,8 / 81,8 | 69,1 / 76,2 | 2 304 |
| `product` D | 43,5 / 48,7 | 43,3 / 46,0 | 67,5 / 77,5 | 69,0 / 72,5 | 2 245 |
| `product` E | 43,0 / 49,9 | 43,1 / 46,6 | 67,9 / 78,4 | 68,4 / 73,5 | 2 236 |
| `t7a` A y B | 46,5 / 51,5–52,2 | 44,9–46,0 / 48,3–50,9 | 67,8–71,5 / 74,1–81,0 | 70,2–72,3 / 77,0 | 2 198–2 269 |
| `off` (v1) A–E | 102,6–121,4 / 109,6–165,8 | 104,9–121,7 / 109,3–159,1 | 125,6–135,2 / 130,9–153,3 | 130,8–138,4 / 135,5–164,4 | 2 323–3 031 |
| T6 (registro de T6) | ~100–102 | ~106 | ~125 | ~132 | — |

**Banco E3.** Los arrastres habituales de `H` y `B` quedan en 43–46 ms de mediana, frente a
103–121 ms de la v1: por debajo del objetivo orientativo de 100 ms y en todas las ejecuciones.

**Configuración del smoke.** Los arrastres quedan en 67–72 ms de mediana y 68–100 ms de
percentil 95 en las siete ejecuciones de la v2, frente a 126–138 ms de la v1.

**Primera actualización.** El primer arrastre tras la adopción y los puntos estructurales es el
arrastre en frío:

| Primer arrastre | E3 `H` | Smoke `H` | `B` |
|---|---|---|---|
| v2 | 388–413 ms | 1,54–1,70 s | como los demás |
| v1 | 473–780 ms | 1,72–1,81 s | como los demás |

La mayor parte de ese coste lo comparten las dos versiones: el trabajo de la primera revisión tras
la adopción. Por eso T7-a lo reduce poco. Entra en el percentil 95 de 21 arrastres solo como
máximo.

**Variabilidad del smoke.** En la caracterización, la ejecución A dio 132 y 116 ms de mediana y
un percentil 95 de unos 340–365 ms. No se reproduce:

- En siete ejecuciones de la v2 con la serie completa, todos los arrastres posteriores al primero
  están entre 63 y 82 ms, salvo uno aislado de 100 ms (`product` A).
- La implementación ya no consulta la decisión exacta donde la v1 no la consultaba (§5.2). La
  hipótesis caracterizada sí lo hacía, y eso puede adelantar intentos del certificado de identidad
  constructiva al arrastre.
- La causa de aquella ejecución no queda demostrada: no tenía la serie por arrastre. Se registra
  como no reproducida, no como explicada.

**Coste por consulta durante un arrastre** (`product` D frente a `off` E):

| Consulta | Cajas v2 / v1 | Encierros v2 / v1 (ms) | Consulta exacta v2 / v1 (ms) |
|---|---|---|---|
| `o_1·h_2` | 5 / 504 | 0,09 / 1,45 | 4,30 / 4,47 |
| `e_1·n` | 5 / 283 | 0,15 / 6,19 | 1,14 / 1,10 |
| `e_1·c_1` | 29 / 1 760 | 0,60 / 34,17 | 3,59 / 3,69 |
| `e_1·r` | 5 / 932 | 0,08 / 7,40 | 1,10 / 1,07 |

En el arrastre de `B`, `e_1·c_1` tiene 10 cajas en la v2 frente a 2 173 en la v1. La consulta
exacta cuesta lo mismo en ambas versiones, unos 10 ms por arrastre: es la memoria de decisiones
exactas con la comprobación de su celda. El resto del arrastre es propagación, recálculo del locus
y trabajo heredado.

**Memoria.** El montón retenido tras la recolección es de 39,9–40,1 MB frente a 40,8–41,0 MB en
E3, y de 65,0–65,4 MB frente a 67,1–67,2 MB en el smoke: menos, porque la memoria del corte de T6
guarda menos cajas. T7-a no añade ninguna memoria.

## 7. Verificación `INTEGRATED_PHASE` del candidato `9f6f27f2`

**Candidato congelado.** `9f6f27f265725b27673f5c37880947ad6c4f2399`, árbol
`abe10e3d4eda338be2b3c36373c4b09af21e7fdd`. El commit de inventario `9f6f27f2` añade las dos clases
a `pre-g9b-cu3.shared` y actualiza el inventario con el productor y el actualizador oficiales:

- `discovery.shared`: 6 560;
- `discovery.desktop`: 2 271, solo evidencia;
- `pre-g9b-cu3.shared`: 220, ejecutada;
- `final.shared`: 7 477, ejecutada;
- el pin `junit_inventory` del registro y el recuento de `verification-final-coverage.Tests.ps1`.

**Campaña.** Corrió sobre el árbol limpio. Consola en el directorio temporal y registros en
`artifacts/agent/pre-g9b-cu3-t7a-<perfil>-9f6f27f2`. Todos los resultados se volvieron a autenticar:

- hash del resultado recalculado;
- diario `COMPLETED`;
- identidad del candidato y de su árbol;
- firma de entorno `de567676…` (la misma de T6).

| Perfil | Ejecución | Resultado | Hojas | PowerShell | Minutos |
|---|---|---|---:|---|---:|
| `STATIC` | `verification-44a0828328d849059f973c1d8941792f` | `ACCEPTED / COMPLETE` | 3/3 | 7.6.9 | 0,1 |
| `INFRA_UNIT` | `verification-6342897440a547debf5691c31a3b9b1c` | `ACCEPTED / COMPLETE` | 22/22 | 7.6.6 aislado | 2,3 |
| `PHASE` `PRE-G9B-CU3` | `verification-a731357ab7074d369d0d650a74dadcca` | `ACCEPTED / COMPLETE` | 4/4 | 7.6.9 | 6,6 |
| `PHASE` `PRE-G9B-CU2` | `verification-5a637acd2be145fc8c19665e5bb7cfbf` | `ACCEPTED / COMPLETE` | 4/4 | 7.6.9 | 9,8 |
| `PHASE` `PRE-G9B-CU1` | `verification-f4aa2e72a95942fb9fff98b97ae60ac4` | `ACCEPTED / COMPLETE` | 4/4 | 7.6.9 | 5,9 |
| `PHASE` `PRE-G9B-CU0` | `verification-4ae9645951454f4899e15ccc05d43831` | `ACCEPTED / COMPLETE` | 2/2 | 7.6.9 | 2,6 |
| `INTEGRATION` | `verification-9e5c794a18fd4f24b809b0439b86caee` | `ACCEPTED / COMPLETE` | 19/19 | 7.6.9 | 52,9 |

- **Clase.** `INTEGRATED_PHASE` queda completa y aceptada técnicamente, sin aprobación autoral.
- **`INFRA_UNIT`.** Usó el entorno aislado de PowerShell 7.6.6, que la autorización admite
  mientras siga abierta `DEBT-VERIFIER-PWSH-769-TESTJSON`.
- **Diagnósticos.** Solo aparecen los de todas las ejecuciones: gobierno `FINDING` y consistencia
  histórica `UNAVAILABLE`. Los de estilo, `checkstyle` y documentación están limpios.

## 8. Asuntos que siguen separados

Ninguno queda resuelto por este candidato:

- la deuda `DEBT-VERIFIER-PWSH-769-TESTJSON`;
- la definición del presupuesto agregado de T1 a T3 y su respaldo;
- las decisiones arquitectónicas de `CU3-B`;
- la aceptación definitiva de `CU3-A`;
- la promoción de la autoridad constructiva a predeterminada.

T7-b y T5 no están autorizados. Las observaciones F1 a F6 de la auditoría de la Puerta N son
preexistentes, ajenas a T7-a, y no se corrigen.

## 9. Estado

- **Commits locales** sobre `bfee0dea`:
  - `22891c7b`: Puerta N y enmienda;
  - `86881f28`: espacio sobrante;
  - `00dc86f1`: producto y pruebas;
  - `b765f3e8`: herramientas;
  - `247f335c`: finales de línea;
  - `9f6f27f2`: inventario, candidato congelado;
  - el commit de este registro.
- **Publicación.** Nada publicado; `main`, las etiquetas y `geocedg_book` no cambian.
- **Salidas sin seguimiento.** Están en `artifacts/benchmarks/pre-g9b-cu/`:
  - `cu3-t7a-candidate-247f335c`;
  - `cu3-t7a-candidate-performance-247f335c`.

  El JSON de evidencia guarda sus SHA-256.
- **Pendiente del autor.** La revisión del candidato. `CU3` sigue sin `PASS — AUTHOR APPROVED`.

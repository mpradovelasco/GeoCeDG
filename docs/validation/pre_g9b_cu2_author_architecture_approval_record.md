# PRE-G9B-CU2 — Registro de la aprobación autoral de la arquitectura y de la autorización condicionada de implementación

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación de la arquitectura de `CU2` y de la autorización de su implementación; no aprueba ninguna implementación todavía no ejecutada |
| Instrucción | «GeoCeDG — PRE-G9B-CU2. Aprobación autoral de la arquitectura y autorización condicionada de implementación», decisión del autor de 21 de octubre de 2026 |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Base autorizada | `070af6f902cd0be291247e73efb5629ef2a39eba`, árbol `49a659d8b334aae3f69c4db000cc842204e9a85f` (rama publicada `phase/pre-g9b-cu2-preparation`) |
| Base del cierre aprobado de `CU1` | `27bf652f176e57ced3807b82afe14fc00b7586a9` |
| Rama de implementación | `phase/pre-g9b-cu2-implementation`, local, creada desde el SHA exacto |

```text
CU2 ARCHITECTURE            = AUTHOR APPROVED, with precision D-CU2-20-R
ADR 0038                    = ACCEPTED — AUTHOR APPROVED (architectural decision)
CU2 IMPLEMENTATION          = AUTHORIZED, conditioned (B07, B10.2, H01, B06 and the normative
                              and kernel capabilities needed to correct them)
CU2 PASS — AUTHOR APPROVED  = NOT DECLARED (needs a new author review of the technical
                              candidate, its verification and the corresponding smoke)
selfApproved                = false
```

## 1. Identidad y puerta de entrada

| Comprobación | Resultado |
|---|---|
| SHA, árbol y ascendencia | `070af6f9` existe; su árbol es `49a659d8…`; desciende de `cbb7ea8c`, `60d54644`, del cierre de `CU1` `27bf652f` y de `main` `48a0775e` |
| Rama publicada | `phase/pre-g9b-cu2-preparation` apunta en `origin` a `070af6f9` |
| Espacio de trabajo | limpio |
| `main` | local y remoto en `48a0775e`, sin divergencia; 73 etiquetas remotas |
| Autoridad leída | `AGENTS.md`, prompts canónicos de gobierno y verificación, hoja de ruta y prompt canónico de `CU2` |
| Selección de entrada `PRE-G9B-CU0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` sobre la base: `verification-b57e1dccad8f4570b10394be58e60f9d`, resultado `5639248aba0e84b35da89b91029a048c296ecc798a25ac78875abcfbb84acbe9`, `ACCEPTED / COMPLETE` 2/2, autenticado |
| Selección de entrada `PRE-G9B-CU1` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1` sobre la base: `verification-4dca68ed6f484b20a38f83fde3c7eaec`, resultado `f4f6e1c0a3f680ecc1f5162c4f2343d0b968d87de4dd96de06e28f653f34be68`, `ACCEPTED / COMPLETE` 4/4, autenticado |

Registros en `artifacts/agent/pre-g9b-cu2-entry-phase-pre-g9b-cu0-070af6f9` y
`artifacts/agent/pre-g9b-cu2-entry-phase-pre-g9b-cu1-070af6f9` (ignorados por Git).

## 2. Aprobación normativa

El autor aprueba las enmiendas A2.1-R a A2.6 de la
[propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md) y la arquitectura del
[ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), con la
precisión obligatoria D-CU2-20-R (§5). El ADR 0038 pasa a `ACCEPTED — AUTHOR APPROVED` como
decisión arquitectónica; ello no significa que `CU2` esté implementado o verificado. El paso T0
incorpora los textos aprobados a las especificaciones de destino y reconcilia sus versiones:

| Especificación | Versión | Sección |
|---|---|---|
| `locus-v2-existence-components.md` | 1.1 | §15 |
| `locus-v2-certified-construction-model.md` | 1.2 | §10 |
| `locus-v2-metrics.md` | 1.6 | §26 |
| `locus-v2-semantics.md` | 1.3 | «PRE-G9B-CU2 amendment (v1.3)» |
| `locus-v2-presentation.md` | 2 | «PRE-G9B-CU2 amendment (version 2)» |
| `dxf-curve-fidelity-and-approximation.md` | 1.2 | §10 |
| `latex-semantic-curve-export.md` (nueva, D-CU2-11) | 1.0 | §5 |
| notas de alcance de A2.5 | — | `locus-v2-intersections.md`, `locus-v2-extended-intersections.md`, `locus-v2-point-interaction.md` §17, `locus-v2-similarity-transformations.md`, `locus-v2-public-surface.md` §6.3 |

Se preserva la separación entre contrato normativo aprobado, implementación posterior,
verificación técnica y cierre autoral. Los contratos de `CU1` y los ADR aprobados no se modifican,
salvo la enmienda de conformidad de D-CU2-20-R en `locus-v2-semantics.md`.

## 3. Decisiones D-CU2-1 a D-CU2-11

| Decisión | Disposición autoral |
|---|---|
| D-CU2-1 | APPROVED. Certificación híbrida de existencia y continuidad, cuatro niveles de evidencia, completitud rigurosa, arcos y extremos laterales, autoridad única de cierre, identidad y linaje estructural |
| D-CU2-2 | APPROVED. Clase v3 del modelo de construcciones certificadas, con el perímetro limitado a las capacidades necesarias de `CU2` |
| D-CU2-3 | OPTION A. Certificación estructural de la topología de `e_2`, evento `ORIGIN_WRAP` y dos extremos laterales distintos, sin utilizar el valor de clase 3 como evidencia rigurosa |
| D-CU2-4 | APPROVED. Topología y teselación v2, alineación de consumidores, fidelidad declarada y reglas de exportación |
| D-CU2-5 | APPROVE RANGES. Las constantes definitivas se fijarán mediante bancos reproducibles y evidencia en el candidato técnico |
| D-CU2-6 | OPTION A. Chorros de segundo orden para certificar pliegues mediante las cinco pruebas establecidas, sin extender `CU2` a la materialización de tangencias |
| D-CU2-7 | OPTION A. Certificados explícitos de proveedor para productores estáticos y analíticos, únicamente cuando sus contratos realmente demuestren existencia y continuidad en todo el dominio declarado |
| D-CU2-8 | OPTION A. Definición válida y existencia global certificadamente vacía: `VALID`, `COMPLETE`, cero componentes |
| D-CU2-9 | OPTION A. Mantener el certificador especializado de SplineV2, corrigiendo currentness, independencia del conductor y tratamiento de la última celda |
| D-CU2-10 | APPROVE RANGES. Criterio de giro para el dibujo; las exportaciones mantienen sus criterios de aproximación en unidades del modelo y la topología del certificado |
| D-CU2-11 | OPTION A. Crear una especificación durable de la exportación LaTeX, incorporando las decisiones ya aprobadas y las reglas de topología de `CU2` |

La certificación local sigue siendo independiente de la completitud global conforme al ADR 0027.
La evidencia exploratoria no adquiere autoridad por muestreo, visualización ni proximidad
numérica.

## 4. Decisiones D-CU2-16 a D-CU2-19 y E-1 a E-3

| Decisión | Disposición autoral |
|---|---|
| D-CU2-16 | OPTION A. Certificado algebraico exacto de predicados de primer nivel (aritmética racional, polinomios, máximo común divisor, descomposición libre de cuadrados y sucesiones de Sturm). Distinguir constantes binary64 capturadas y sus valores racionales exactos, relaciones geométricas ideales no capturadas simbólicamente, raíces simples, múltiples y configuraciones degeneradas, y garantías algebraicas exactas, cotas rigurosas y resultados numéricos estimados. Cubrir el caso crítico de mordedura del modelo y las configuraciones sintéticas aprobadas |
| D-CU2-17 | OPTION A. Accesores de solo lectura del estado de selección en `AlgoIntersectLineConic` y `AlgoIntersectSingle`, con modificaciones upstream mínimas y registradas. La copia aislada reproduce la selección efectiva del algoritmo vivo o falla explícitamente. No se autoriza escribir la permutación ni reemplazar la selección por una heurística geométrica |
| D-CU2-18 | OPTION A. Tratamiento conservador de las salidas coincidentes: la raíz matemática y su valor límite pueden figurar como evidencia, nunca como salida operativa disponible; se mantienen las bandas `UNRESOLVED` y los diagnósticos tipados. La selección continua de raíces coincidentes en el corte y cualquier modificación del comportamiento heredado quedan fuera de `CU2`. Se admite la revisión provocada por una transición real del estado heredado de selección durante la primera inicialización, con causa tipada y prueba de que no se producen revisiones espurias reiteradas |
| D-CU2-19 | OPTION A. Componentes puntuales como entidades semánticas propias, declaradas en los resultados y en el inspector con su evidencia y su linaje, no dibujadas ni materializadas como objetos punto públicos. Longitud cero; su existencia no depende de que un parámetro binary64 represente exactamente su dirección. Todo consumidor que no las admita declara explícitamente la limitación |
| E-1 | APPROVED. La evidencia `EXPLORATORY` puede dibujarse con estilo diferenciado únicamente dentro de bandas `UNRESOLVED`, sin uniones a componentes certificadas ni exportación como geometría autoritativa |
| E-2 | APPROVED. Las operaciones que requieren continuidad y recorridos exigen componentes certificadas, aunque sean locales; la validez puntual queda limitada a las operaciones que solo requieren un punto aislado |
| E-3 | APPROVED. Identidad y linaje estructural de componentes; selectores persistidos resueltos solo por correspondencia semántica demostrada, que la contención única valida pero no crea; sin reescribir contenido persistido ni inferir asociaciones por etiquetas, índices, coordenadas o proximidad; fallo cerrado con diagnóstico tipado ante ambigüedad |

## 5. D-CU2-20-R — Exactitud de revisiones y fidelidad de reconstrucción

Disposición: OPTION A, ampliada y obligatoria. Se autoriza corregir dentro de `CU2`, con alcance
estrictamente limitado y sin reabrir el cierre de `CU1`, la huella de revisión introducida en
`CU1`, como conformidad con su contrato de instantánea semántica exacta. Requisitos inseparables:

1. **Exactitud de la huella.** `ReconstructionSliceFingerprint2D` no puede depender solo de una
   representación textual que reduzca binary64 distintos a la misma cadena. Huella versionada que
   distinga los cambios semánticamente relevantes de las entradas no conductoras, incluidos los
   de una unidad en la última cifra: definiciones y entradas autoritativas, binary64 exactos,
   estructura, estado de selección que afecte a la geometría, etiquetado que afecte a las salidas
   efectivas, y versión de la política y del contrato de reconstrucción. Nunca identidad de
   objetos Java, proximidad, índice circunstancial ni representación gráfica; no sustituye las
   identidades durables. Los movimientos exclusivos del conductor conservan la revisión mientras
   la instantánea no cambie; las transiciones legítimas del estado heredado se distinguen de los
   recálculos equivalentes.
2. **Fidelidad de reconstrucción.** Inspeccionar `Macro.buildMacroXML(...)` →
   `MacroKernel.loadXML(...)` en `ReconstructibleLocusEvaluator2D`. Antes de publicar un
   certificado o reutilizar un evaluador, establecer que el corte reproduce las entradas
   autoritativas con la fidelidad del contrato; nunca tratar una reconstrucción redondeada como
   idéntica. Identificar los valores que deben reproducirse exactamente, separar entradas
   autoritativas de resultados derivados, no modificar el formato general de persistencia,
   restaurar exactamente solo con corrección demostrada, no usar la copia como autoridad sobre el
   original, detectar discrepancias y fallar explícitamente, versionar el contrato y preservar
   determinismo y compatibilidad. Ubicación: núcleo compartido. Describir antes del código el
   ciclo de vida de captura, reconstrucción, validación y publicación.
3. **Persistencia y reapertura.** Distinguir la fidelidad de la copia interna y la del formato
   persistido; no atribuir a la persistencia un round-trip bit a bit que su contrato no establezca;
   tras reabrir, el certificado corresponde al estado recargado; caracterizar las pérdidas de
   precisión; detenerse y pedir autorización si corregirlas exige cambiar el formato.
4. **Orden.** Puerta temprana y bloqueante: caracterización focal y contadores en T1; corrección
   dentro de T3, antes de que los certificados de T4 puedan considerarse válidos o publicarse. No
   se activan productivamente certificados de `CU2` con instantáneas indistinguibles o
   reconstrucciones infieles.
5. **Validación.** Ampliar V-REV-EXACT-1 con: binary64 adyacentes al valor crítico; ida y vuelta;
   reconstrucción de un locus cuya entrada difiere en un ulp; comparación exacta entre entradas
   autoritativas y copia; contraste con un documento recién construido; conservación de revisiones
   con movimientos exclusivos del conductor; cambios del estado de selección; reproducibilidad;
   guardar y reabrir; Undo/Redo; propagación a loci anidados, métricas y certificados;
   invalidación de cachés y estados obsoletos; valores extremos y degeneraciones. Las pruebas
   distinguen fidelidad semántica exacta de igualdad textual.

Si la corrección exigiera cambiar serialización, comportamiento upstream, identidad durable o
contratos aprobados fuera de este alcance: detener y presentar una propuesta correctiva
independiente. Sin aproximaciones silenciosas ni tolerancias arbitrarias.

## 6. Autorización de la implementación T0–T13

Se autoriza ejecutar la [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) con
D-CU2-20-R y todas las decisiones anteriores, manteniendo la secuencia, la numeración, las
dependencias y las condiciones de parada T0–T13: T0, contratos antes del código, aceptación del
ADR 0038 y actualización del prompt canónico; T1–T3, instrumentación, bancos, estructura de
existencia, clase v3 y corrección de la huella y de la reconstrucción, con su validación focal
antes de depender de certificados nuevos; T4–T7, certificación en sombra; T8–T10, identidad,
consumidores y representación; T11, conmutación controlada sin que desaparezca la cadena
`o_1 → LocusLength → d_2 → e_2`; T12, conformidad y líneas base, con la corrección del manifiesto
(D-CU2-15); T13, congelación y verificación, sin declarar aprobación por el mero resultado de las
pruebas.

## 7. D-CU2-12 a D-CU2-15

| Decisión | Disposición autoral |
|---|---|
| D-CU2-12 | APPROVED: `INTEGRATED_PHASE`. Aceptación con `STATIC`, `INFRA_UNIT`, `PHASE PRE-G9B-CU2`, `PHASE PRE-G9B-CU1`, `PHASE PRE-G9B-CU0` e `INTEGRATION` sobre el candidato congelado, con identidad y evidencias propias. Si el perímetro de regresión se supera: detenerse y presentar `VERIFICATION_ESCALATION_REQUEST`, sin redefinir la clase |
| D-CU2-13 | APPROVED. Base exacta `070af6f902cd0be291247e73efb5629ef2a39eba`, árbol `49a659d8b334aae3f69c4db000cc842204e9a85f` |
| D-CU2-14 | APPROVED. Inversión de B07, H01, B06 y B10.2 con pruebas positivas, rebaseline trazable y preservación de las referencias históricas |
| D-CU2-15 | APPROVED. Corrección documental del manifiesto del cilindro oblicuo en T12, sin alterar el modelo ni sus hashes |

## 8. Garantías científicas y fronteras

Durante la implementación rigen las doce garantías científicas de la instrucción (§8): separación
de los niveles de evidencia; ninguna inferencia de existencia, continuidad, tangencia o
completitud desde un muestreo finito; cotas rigurosas en toda certificación numérica; el
certificado exacto solo en su clase; respeto de la semántica efectiva de las salidas de GeoGebra y
del estado de selección; ningún valor límite en lugar de una salida indefinida sin autorización;
identidades durables, linaje semántico y selección explícita; ninguna continuidad por proximidad
ni periodicidad solo por el conductor; parámetro singular interior distinto de componente
puntual; ninguna disponibilidad numérica inferida de la existencia; ninguna aproximación
silenciosa; modelo constructivo CeDG. Los parámetros críticos y los casos sintéticos exactos de la
matriz son pruebas obligatorias.

Quedan fuera de `CU2`: `CU3`–`CU10`; puntos estructurales públicos y materialización de
tangencias; cambios del motor de intersecciones; cambios no autorizados del comportamiento
heredado; escritura del estado de selección en clases upstream; selección continua artificial de
raíces coincidentes; cambios de identidad durable o serialización no contemplados; ampliaciones
algebraicas generales no necesarias para la clase v3; cambios de valores o políticas de
`LocusLength`; cambios de modelos originales; el libro; la promoción a `main`; etiquetas,
releases, binarios y publicación. Las únicas modificaciones upstream permitidas son los accesores
de solo lectura autorizados, registrados y justificados.

## 9. Condiciones de parada adicionales

Además de las de la estrategia: (1) no poder garantizar la exactitud de las instantáneas o la
fidelidad de reconstrucción con las modificaciones autorizadas; (2) un cambio de un ulp que no
invalide un estado semánticamente distinto; (3) que D-CU2-20-R exija alterar la persistencia o el
comportamiento upstream; (4) que la copia del corte no reproduzca el estado de selección necesario
para certificar el modelo del autor; (5) que la clase v3 no certifique un caso obligatorio del
corpus dentro de los presupuestos; (6) que la certificación crítica exija una afirmación no
demostrada; (7) que certificar `e_2` exija una estimación de longitud como evidencia rigurosa;
(8) una incompatibilidad entre normas aprobadas no reconciliable dentro del alcance; (9) que un
consumidor obligue a inferir identidad por proximidad, índice o coordenadas; (10) un `preserve`
de `CU0` fuera de tolerancia; (11) una verificación exigible que supere la clase aprobada;
(12) una corrección que requiera una capacidad reservada a `CU3`. Ante cualquiera: diagnóstico,
evidencia, alternativas mínimas y decisión autoral necesaria, sin debilitar pruebas ni ampliar el
alcance.

## 10. Mandato editorial E1

Se ratifica la vigencia del mandato E1 y del
[plan de transferencia científica](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md).
La implementación de `CU2` mantiene actualizadas las entradas de los métodos efectivamente
implementados, sin promover ningún método de `DISEÑO` a `IMPLEMENTACIÓN VERIFICADA` sin código,
pruebas y verificación. No se modifica `geocedg_book`; el expediente
`SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` sigue siendo obligatorio para `CU10`.

## 11. Entregables y siguiente puerta

El candidato de implementación presenta los trece entregables de la instrucción (§12): commit
técnico exacto, árbol y rama local; matriz de correspondencia entre decisiones e implementación;
especificaciones y ADR reconciliados; código y pruebas con su justificación de ubicación;
evidencia de D-CU2-20-R; certificación del modelo del autor; casos sintéticos y adversarios;
puertas GC-1 a GC-8; registro de inversión de fijaciones; resultados autenticados de `STATIC`,
`INFRA_UNIT`, las tres `PHASE` e `INTEGRATION`; limitaciones; actualización del registro E1; e
informe del candidato con el estado del workspace y del remoto. El candidato permanece en la rama
local; no se publica ni se fusiona con `main` sin autorización adicional; no se ejecuta el cierre
autoral ni se declara `PRE-G9B-CU2 = PASS — AUTHOR APPROVED`. La siguiente puerta es la revisión
autoral del candidato técnico y de sus evidencias, incluida la prueba manual.

## 12. Corrección registrada en T0

La caracterización previa al código (sondeo de solo lectura, 2026-10-21) corrigió la atribución
del hallazgo F-CU2-12 del cierre de coherencia: con el conductor fuera de la degeneración, cada
cambio de un ulp de `excCyl` publica una revisión y la copia del corte contiene el valor exacto,
porque los valores numéricos se serializan con `Double.toString`. La observación anterior se debió
a la regla de publicación H01 (el punto trazado estaba indefinido en la posición del conductor, el
locus no se republicaba y el sondeo evaluaba la definición anterior, F-CU2-3). D-CU2-20-R sigue
siendo necesaria: la huella no incluye el estado de selección ni el de etiquetado, un literal sin
texto original puede escribirse con menos cifras de las que exige un round-trip exacto, y nada
valida la fidelidad del corte. El
[análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §3.9 y §7 y el
[diseño](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §8 recogen la corrección.

# PRE-G9B-CU3 — Hoja de decisiones de `CU3-B` (R1–R6)

| Campo | Valor |
|---|---|
| Carácter | **Decisiones aprobadas por el autor el 2026-10-27**: R1 a R6 con la alternativa recomendada en cada una (R6: alternativa B, dos incrementos). Se autoriza `CU3-B1` (contactos en pliegues); `CU3-B2` no está autorizado. Enmiendas aplicadas: ADR 0039, decisión 3, y diseño de la Etapa B, §6.1 |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Estabilización de CU3-A, resolución presupuestaria, mantenimiento del verificador y preparación arquitectónica de CU3-B», de 2026-10-27, G4 |
| Base | [revisión arquitectónica de `CU3-B`](pre_g9b_cu3_b_identity_architecture_review.md) (P1–P3) sobre el candidato `CU3-A` `9f6f27f2` |
| Contratos | [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md), [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md), [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md), [diseño de la Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §6 |
| `selfApproved` | `false` |

## 1. Qué se decide

`CU3-B` es la materialización sin conductor de los contactos de clase 1 y 2: un punto público
`Intersect(L, r)` que designe un contacto. `CU3-A` ya clasifica estos contactos y los publica en el
resultado rico, sin token admisible. La revisión arquitectónica contiene el análisis completo; esta
hoja lo convierte en seis decisiones que se pueden responder directamente.

Comprobaciones técnicas hechas al preparar esta hoja:

- **Código.** El libro de tokens persiste las versiones 4 y 5 e importa de la 1 a la 5. Una versión
  desconocida aborta la carga. La regla de enlace del único token de una componente
  (`allowLegacySingletonBinding`) existe en `LocusIntersectionTokenLedger2D`.
- **Pliegues.** Cada pliegue certificado de `CU2` (`LocusCertifiedFold2D`) guarda el nombre de su
  simetría verificada y el encierro canónico de `t₀`. Las simetrías de `fold-symmetry/v1` son dos
  involuciones, cada una con dos puntos fijos (`0` y `π`; `π/2` y `−π/2`).
- **Etiqueta exacta.** Los puntos fijos posibles de una involución distan `π` entre sí. Por eso el
  encierro canónico de `t₀`, de anchura de unos pocos ulp, contiene exactamente uno, y la etiqueta
  exacta (involución, punto fijo) se decide rigurosamente desde datos ya certificados, sin nueva
  política.
- **T7-a.** Con `cu3-certified-admission/v2`, la tangencia regular `o_1·h_2` en `rbCone = 7,5` es una
  raíz doble certificada. La forma `ONE_DOUBLE` está disponible en todo el corpus del modelo del
  autor para las dos clases de contacto.

## 2. Decisiones

### R1 — Identidad única de un contacto en pliegue

| | |
|---|---|
| Problema | En un pliegue con simetría exacta `σ`, `F(σ(t)) = F(t)`. Las dos preimágenes `t₀ ± s` son un único punto geométrico con orientaciones de cruce opuestas, así que el motor actual publicaría dos selectores y dos tokens. ADR 0039, decisión 2 (D-CU3-11) exige una sola identidad |
| Evidencia | Revisión §3. `ConstructionSymmetry2D` verifica `σ` exactamente; la forma `ONE_DOUBLE` de E3 y de T7-a certifica la raíz doble en `t₀`; un certificado de conteo sobre una caja simétrica por `σ` certifica la pareja |
| Alternativas | (a) dos identidades y una relación «mismo punto»: contradice D-CU3-11. (b) fusionar los tokens existentes: es una reasociación. (c) **P1: contacto anclado en el pliegue**, una sola solución con la caja simétrica, el estado «contacto» o «pareja» como evidencia de la revisión y la misma identidad a través de la transición |
| Recomendación | (c) P1 |
| Consecuencias | Una solución por pliegue certificado en lugar de dos raíces. La transición pareja ↔ contacto conserva selector y token. Si no queda ninguna raíz (`NONE` certificado), el token queda latente con `CONTACT_LOST` y se reactiva con el mismo selector (ADR 0017, decisión 13). Sin certificado de pliegue, o con un emparejamiento ambiguo, no hay ancla ni elección silenciosa (`IDENTITY_DISCONTINUITY`) |
| Normativa | `locus-v2-intersections.md` (nueva sección de identidad de contactos bajo `constructive-authority/v1`); diseño de la Etapa B §6 |
| Pruebas | la pareja y el contacto conservan una sola identidad; `NONE` deja el token latente y el regreso lo reactiva; un pliegue no certificado no ancla nada; ninguna raíz de una caja certificada recibe germen transversal |
| Riesgos | Que una caja simétrica no aísle la pareja de otras raíces. Mitigación: exigir el certificado de conteo de la caja completa, sin excepción |
| **Decisión** | ¿Se adopta P1 como regla de identidad de los contactos en pliegue bajo `constructive-authority/v1`? |

### R2 — Diferenciación intrínseca de dos contactos de una componente

| | |
|---|---|
| Problema | Los contactos de `e_1·c_1` en `t = 0` y en la costura `t = π` comparten linaje, objetivo, orden, clase y lado. Con el germen aprobado, sus selectores base coinciden, y el rango de fase no puede desempatarlos en la costura sin heurística (ADR 0017, decisión 7) |
| Evidencia | Revisión §4. Ambos pliegues son de `t ↦ −t`, con puntos fijos `0` y `π`. La etiqueta se decide de forma exacta y rigurosa (§1) y no depende de coordenadas, valores binary64 del parámetro, orden de descubrimiento, proximidad, índices de construcción ni posición de la costura: `π` y `−π` son el mismo parámetro canónico |
| Alternativas | (a) una regla de rango para el punto fijo en la costura: es heurística. (b) anclar en los puntos estructurales `K` y `J`: no son intrínsecos a la consulta. (c) **P2: la etiqueta exacta (involución, punto fijo) como dimensión del selector, sin rango** |
| Recomendación | (c) P2 |
| Consecuencias | Los dos contactos de `e_1·c_1` tienen selectores distintos por construcción. Una redefinición del conductor, de su marco o de la componente es una barrera de identidad (ADR 0017, decisión 8). Los contactos regulares sin pliegue siguen ADR 0017 sin cambios |
| Normativa | Enmienda acotada de ADR 0017 (un tipo de selector nuevo sin rango para contactos anclados; las decisiones 3, 7, 8 y 13 se mantienen) |
| Pruebas | los contactos de la costura y de `t = 0` son distintos sin rango; la etiqueta es idéntica en `π` y en `−π`; la pérdida del certificado del pliegue es una barrera sin reasociación |
| Riesgos | Ninguno nuevo de identidad. El conjunto de etiquetas es finito y exacto |
| **Decisión** | ¿Se adopta la etiqueta exacta del pliegue como dimensión intrínseca del selector de un contacto anclado, sin rango de fase? |

### R3 — Composición del germen

| | |
|---|---|
| Problema | El germen tangente aprobado en la Etapa B §6.1 incluye la clase de evidencia y el orden de contacto. ADR 0039, decisión 5 excluye la evidencia de la revisión de todo token |
| Evidencia | Revisión §5. Con T7-a, el mismo contacto pasa de evidencia estimada a exacta (en `rbCone = 7,5`) sin cambiar de punto. Si la evidencia formara parte del germen, cambiaría la identidad |
| Alternativas | (a) mantener el germen aprobado: la identidad dependería de la evidencia. (b) **P3: un germen solo con hechos estructurales**: tipo de germen versionado, clave de la componente y objetivo, más la etiqueta del pliegue (contactos anclados) o el lado del contacto (tangencias regulares). Nunca evidencia, orden, grado, integridad, certificado, celda, intervalos, valores de parámetro ni configuración de decisión |
| Recomendación | (b) P3 |
| Consecuencias | Una transición de clase 1 a clase 2, o de evidencia estimada a exacta, conserva el token. Las clases 3 a 5 dejan el token latente sin crear otro. El cambio de signo del lado de una tangencia regular es una barrera |
| Normativa | Enmiendas de la Etapa B §6.1 y de ADR 0039, decisión 3 (preparadas aquí, no aplicadas). Nombres nuevos `pre-g9b-cu3/fold-contact-germ/v1` y `pre-g9b-cu3/regular-tangent-germ/v1`; `…/tangent-contact-germ/v1` queda retirado sin haberse implementado |
| Pruebas | el paso de clase 1 a 2 conserva el token; el paso de evidencia estimada a exacta conserva el token; ningún campo de evidencia aparece en el texto del germen |
| Riesgos | Ninguno sobre tokens existentes: el germen tangente aprobado nunca se acuñó |
| **Decisión** | ¿Se aprueban P3 y las enmiendas correspondientes de la Etapa B §6.1 y de ADR 0039, decisión 3? |

### R4 — Tokens transversales anteriores

| | |
|---|---|
| Problema | Una pareja que se certifica después de existir con identidades transversales (política heredada o una revisión sin certificado) tiene ya dos tokens |
| Evidencia | ADR 0040: una adopción que contradice un resultado le quita la admisibilidad con causa tipada y nunca lo reasocia. ADR 0017, decisión 13: un token reclamado puede quedar latente y reactivarse solo con el mismo selector completo. En `CU3-A`, la raíz de la costura ya pierde su admisibilidad tras la adopción y Deshacer la restaura |
| Alternativas | (a) fusionar o redirigir: es una reasociación. (b) heredar el único token de la componente: prohibido para contactos. (c) **dejan de ser actuales con una causa tipada propuesta (`SUPERSEDED_BY_CERTIFIED_FOLD_PAIR`), quedan latentes con su material intacto, sus puntos quedan indefinidos con causa, y solo se reactivan si el mismo selector transversal completo vuelve a ser actual. El token del contacto se acuña nuevo** |
| Recomendación | (c) |
| Consecuencias | Hay latencia: un punto materializado sobre un token transversal superado queda indefinido hasta que el usuario lo vuelva a designar sobre el contacto, sin migración automática. Deshacer la adopción o volver a una revisión sin pareja certificada reactiva los tokens transversales. Copiar y duplicar siguen la regla de cierre fresco |
| Normativa | `locus-v2-intersections.md` §10 (ciclo de vida de los tokens, causa nueva); ADR 0040 sin cambios |
| Pruebas | tokens transversales dorados sin cambios; un contacto nunca hereda el único token de la componente; Deshacer la adopción restaura los tokens superados; Deshacer y Rehacer conservan las reclamaciones latentes |
| Riesgos | Pérdida aparente de un punto del usuario. Mitigación: la causa tipada y el informe de impacto de la adopción lo nombran antes de aplicarla |
| **Decisión** | ¿Se aprueba la transición (c), con la causa `SUPERSEDED_BY_CERTIFIED_FOLD_PAIR` y sin migración automática de puntos? |

### R5 — Persistencia

| | |
|---|---|
| Problema | Los contactos necesitan entradas nuevas en el libro de tokens, y los lectores anteriores no deben interpretarlas mal |
| Evidencia | Versiones 1 a 5 importadas; una versión desconocida aborta la carga, como ya hace un lector anterior con el registro de política de un documento adoptado |
| Alternativas | (a) añadir entradas en la versión 5: un lector anterior las leería mal o las perdería. (b) **versión 6 del estado del libro, escrita solo cuando contiene una entrada de contacto, con un discriminador de tipo de entrada que hace fallar cerrado a un lector ante un tipo desconocido**. Los documentos sin contactos siguen en las versiones 4 y 5 |
| Recomendación | (b), con el discriminador desde el principio, para que un segundo incremento (R6) pueda añadir tangencias regulares sin una versión 7 |
| Consecuencias | Un documento con contactos no se abre en versiones anteriores de GeoCeDG (fallo cerrado con causa). La evidencia de la revisión nunca se persiste. La reapertura reconstruye el selector exacto. Copiar y duplicar dan identidades nuevas. Las reclamaciones latentes se persisten como las actuales |
| Normativa | Formato del libro de tokens (ADR 0017, persistencia); compatibilidad de `locus-v2-intersections.md` |
| Pruebas | ida y vuelta de la versión 6; un lector de la versión 5 rechaza la 6 con causa; un tipo de entrada desconocido falla cerrado; los documentos sin contactos conservan su versión byte a byte; Deshacer, Rehacer, copia y reapertura |
| Riesgos | Compatibilidad hacia atrás limitada para documentos con contactos; es la misma política ya aceptada para el registro de política |
| **Decisión** | ¿Se aprueba la versión 6 del libro de tokens con el discriminador de tipo de entrada y el fallo cerrado de los lectores anteriores? |

### R6 — Alcance del incremento `CU3-B`

| Criterio | A: pliegues y tangencias regulares juntos | B: pliegues primero, tangencias regulares después |
|---|---|---|
| Dependencias normativas | R1 a R5 a la vez, más las transiciones de una tangencia regular que se divide en dos raíces (`CONTACT_SPLIT`) | Primero R1 a R5 solo para el ancla de pliegue, que es exacta y finita. Las transiciones regulares se deciden después |
| Complejidad | alta: dos tipos de germen y dos regímenes de rango (sin rango y con rango) | menor: un tipo de germen sin rango |
| Riesgo de identidad | mayor: la tangencia regular no tiene punto fijo, se mueve con la configuración y depende de las barreras del lado | menor: la etiqueta del pliegue es invariante mientras el pliegue siga certificado |
| Verificación independiente | conjunta | cada incremento con sus propias filas y su campaña |
| Persistencia | versión 6 una vez | versión 6 una vez, con el discriminador (R5); el segundo incremento añade un tipo de entrada sin versión nueva |
| Beneficio funcional | los cuatro contactos de las intersecciones del modelo | tres intersecciones (`e_1·n`, `e_1·c_1` con la costura, `e_1·r`) en el primer incremento; `o_1·h_2` en el segundo |
| Coste de validación | una campaña grande | dos campañas menores |
| Smoke | uno | uno por incremento |
| Gates del roadmap | `CU3-B` en un paso | `CU3-B` en dos pasos; `CU3` global solo se cierra tras el segundo |

- **Recomendación: B.** El ancla de pliegue resuelve el problema de identidad más exigente con un
  hecho exacto y finito. La tangencia regular introduce transiciones (división en dos raíces y
  cambio de lado) que conviene verificar por separado. La tangencia regular `o_1·h_2` sigue
  disponible mientras tanto por su punto estructural `Intersect(L, r, D)` de `CU3-A`.
- **Decisión:** ¿se implementa `CU3-B` en dos incrementos (B) o en uno (A)?

## 3. Lo que esta hoja no hace

- No modifica el kernel, los tokens, los selectores, los gérmenes, la serialización ni los ADR
  aprobados.
- No autoriza implementar `CU3-B`; las enmiendas mencionadas quedan preparadas, no aplicadas.
- `CU3-B` no bloquea la aceptación de `CU3-A`; el cierre global de `CU3` sí la requiere.

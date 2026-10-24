# PRE-G9B-CU3 — Normalización de residuos, admisibilidad local frente a certificación (H-2) y ejes (H-3)

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de `CU3` para H02 y H06, y para la relación entre la admisibilidad local aprobada y la existencia certificada. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisado por la revisión científica focal de 2026-10-24; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1 (H-2 y H-3) y §4 (las tolerancias nunca dependen del zoom, el DPI, la cámara ni el tamaño de la vista); instrucción «PRE-G9B-CU3 — Revisión científica focal del candidato de preparación» (2026-10-24), punto 1 |
| Autoridad vigente | `locus-v2-intersections.md` v1.2 §5.1, §8 (en particular §8.1) y §9; aclaración G8B-R1 (ADR 0008, «G8B-R1 clarification — point admissibility versus completeness», y el [informe G8B-R1](../validation/g8b_r1_locus_v2_intersection_point_admissibility_report.md), §C y §D); `locus-v2-public-surface.md` §8 y §10; ADR 0013, 0017, 0026 y 0037 (clases de garantía métrica, precedente); `AGENTS.md` §1.8, §10 y §11.5–§11.6 |
| Antecedentes | [estudio H de `CU0`](pre_g9b_cu0_precision_and_tolerance_study.md) §4 y §5 (H-b, H-c); enmiendas candidatas A3.1 y A3.4 de `CU0`; decisiones H-2 y H-3 de la [hoja de `CU0`](../validation/pre_g9b_cu0_author_decision_sheet.md) |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §3.1 y §4 (H-CU3-1 a H-CU3-6); P7, P8 y P9 de la [caracterización](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json) |

## 1. H02: diagnóstico

Hay tres mecanismos, confirmados por P7:

1. **Escala unidad para rectas, segmentos, rayos, gráficas e implícitas.** El umbral efectivo es
   `4·10⁻¹²` en unidades del modelo, sea cual sea el tamaño de la construcción.
2. **Corchete absoluto `1e-12` en el parámetro.** Tras la bisección, el residuo vale
   aproximadamente `|dh/dt|·Δt`, que crece con la escala.
3. **Traslación.** Con la misma escala, el número de raíces admisibles cambia con la traslación
   (P7: radio 200, 0/2 en el origen y 2/2 a 10⁶), y a 10⁶ aparecen duplicados (H-CU3-4).

En todos esos casos, el sondeo **certifica la existencia de las dos raíces** por un cambio de signo
de envolventes que excluyen el cero (Etapa B §2a). Sus residuos divididos por la extensión `3r` de
la imagen están entre `0` y `7,6·10⁻¹⁷`. Lo que falla no es la precisión, sino el **criterio de
aceptación** basado en un residuo absoluto.

## 2. Admisibilidad local (G8B-R1) frente a existencia certificada

### 2.1 Lo que aprobó G8B-R1

El informe G8B-R1 (§C) y su aclaración en el ADR 0008 definen la **admisibilidad de un punto** como
una propiedad **local** de la solución, independiente de la completitud global. Una solución es
admisible si:

1. su token es único en el resultado rico actual;
2. el resultado es actual, de éxito, finito y soportado;
3. la solución fue publicada tras una evaluación semántica nueva, una comprobación de coordenadas
   finitas, una comprobación del **residuo normalizado** y una comprobación de pertenencia al
   objetivo;
4. su aislamiento local es `ESTABLISHED`;
5. su identidad es `NEW_TOPOLOGICAL_SOLUTION` o `CONTINUATION_ESTABLISHED`, con clave de
   continuación;
6. sus vínculos de linaje y de revisión concuerdan.

El ADR 0008 añade que «a small residual alone is insufficient» y que «tangency is not inferred
from sign changes alone».

### 2.2 Grados de evidencia de existencia

La revisión focal pide distinguir las soluciones **localmente verificadas y admisibles** bajo ese
contrato de las soluciones con **existencia rigurosamente certificada**. El diseño lo hace con un
atributo explícito de cada solución, ortogonal a su clase de contacto, a su orden y a la
completitud global:

| Grado | Evidencia | Naturaleza (principio 3 del registro E1) |
|---|---|---|
| `LOCALLY_VERIFIED` | las condiciones 1 a 6 de G8B-R1 con la evidencia binary64 actual: cambio de signo o muestra de los valores evaluados, residuo bajo el umbral versionado y aislamiento local | **verificación numérica bajo hipótesis**: suficiente para la admisibilidad aprobada, **no** un certificado de existencia |
| `EXISTENCE_CERTIFIED` | cambio de signo con envolventes que excluyen el cero en un corchete certificado válido (continuidad), y unicidad por `h'` que excluye el cero (Etapa B §2a) | aritmética de intervalos con cotas rigurosas |
| `EXISTENCE_STRUCTURAL` | igualdad por estructura (Etapa B §2c) | prueba estructural |
| `EXISTENCE_EXACT` | igualdad decidida por aritmética exacta en una configuración declarada (M3) | prueba algebraica exacta |

Los tres últimos certifican. El primero, no. Un grado no se deduce de otro (§4).

## 3. Propuesta H-2-R (D-CU3-6, revisada)

La versión anterior de esta preparación proponía sustituir la aceptación de G8B-R1 por la
aceptación certificada y dejar no admisibles las raíces de capacidades sin envolventes. **Eso
habría restringido una capacidad aprobada sin identificarlo**, y se retira. La propuesta revisada
tiene cuatro piezas.

### 3.1 Vía G8B-R1, sin cambios

La admisibilidad de G8B-R1 se conserva tal como está:

- los umbrales versionados de `g8b-initial-normalized/v1`;
- el grado `LOCALLY_VERIFIED`;
- las capacidades sin envolventes: el evaluador heredado, las fuentes fuera de la clase v3 y de
  SplineV2, y los pares.

Ninguna raíz admisible hoy deja de serlo por esta pieza.

### 3.2 Vía certificada, aditiva (H02)

Una raíz transversal `EXISTENCE_CERTIFIED` que **no** pasa la comprobación de residuo de la
condición 3 de G8B-R1 es admisible si su residuo normalizado es coherente con la escala
característica: `|residuo| ≤ τ_rel · L` (§3.5). Es una ampliación de la condición 3: la satisface
o bien el residuo versionado, o bien la existencia certificada con coherencia de escala.

Efectos:

- las raíces de H02 a gran escala y trasladadas pasan a ser admisibles;
- ninguna raíz admisible deja de serlo;
- en dibujos pequeños (`L < 1`) no cambia nada, porque la vía G8B-R1 se mantiene.

El corchete de la vía certificada se refina por el criterio de residuo o hasta unos pocos
`ulp(t)`, nunca por una anchura absoluta.

### 3.3 Regla de conflicto de evidencia (cambio normativo restrictivo, identificado)

> Cuando, en la misma revisión, una evidencia certificada contradice una clasificación o una
> admisibilidad obtenida por verificación local, prevalece la evidencia certificada: la solución
> contradicha se retira del conjunto finito o se reclasifica, y su token deja de ser admisible. Sin
> contradicción certificada, nada cambia.

Las contradicciones tipificadas son:

| Contradicción | Hallazgo | Efecto |
|---|---|---|
| clase 4 sin intersección certificada en una caja que contiene raíces verificadas localmente | H-CU3-3; parte de H-CU3-1 | las raíces se retiran; el resultado rico registra la ausencia local certificada (la completitud global no cambia) |
| punto crítico certificado (`h''` de signo fijo y un solo cambio de signo de `h'`) en una caja donde se publica una única raíz transversal, o más raíces de las que el certificado permite | H-CU3-1; H-CU3-2 | las raíces contradichas se reclasifican según la taxonomía (clase 3, 4 o 1–2) |
| unicidad certificada de dos candidatos cuyos corchetes se solapan | H-CU3-4 | se fusionan en una preimagen |

**Alcance.** Solo objetivos de un parámetro, y solo fuentes con envolventes (clase v3 o SplineV2).
Los pares y las capacidades sin envolventes no cambian: allí no se calcula ninguna evidencia
certificada que pueda contradecir nada.

**Disparador determinista e independiente de la escala.** El análisis certificado solo se ejecuta
sobre los candidatos que el resolvedor ya marca como no transversales por su indicador, sobre los
mínimos locales que encuentra su búsqueda ternaria y sobre las parejas de candidatos cuyos
corchetes están a una distancia relativa menor que la de su propia anchura. Una raíz transversal
genérica no lo dispara y no cambia.

**Naturaleza del cambio.** Es una enmienda normativa de la admisibilidad aprobada (A3.1-R) que
restringe solo resultados contradichos por una prueba. Se apoya en §5.1 de la especificación
(«uncertainty must not become a false transverse or no-root result») y en el ADR 0008
(«tangency is not inferred from sign changes alone»). Sus consecuencias de compatibilidad están
en §5.

**Alternativa sin restricción.** Conservar la admisibilidad de las soluciones contradichas y
marcarlas solo con un diagnóstico. Mantiene la compatibilidad completa, pero deja publicadas
raíces transversales que una prueba contradice. Contraría §5.1, y no se recomienda.

### 3.4 Diagnósticos de rechazo

`RESIDUAL_REJECTED`, `PAIR_RESIDUAL_REJECTED`, `MEMBERSHIP_REJECTED` y `NONFINITE_EVALUATION` están
declarados y hoy no se emiten. Pasan a emitirse en los rechazos que ya ocurren. Es observabilidad
pura y no cambia ninguna admisibilidad.

### 3.5 Escala característica `L` (solo para la vía certificada y los diagnósticos)

| Opción | Escala `L` | Valoración |
|---|---|---|
| **S1** (recomendada; A3.1 de `CU0`) | la mayor de la extensión de la imagen de la componente del locus y la del objetivo acotado (la del locus si el objetivo no está acotado), con un suelo absoluto solo para extensiones degeneradas | invariante por traslación y covariante con la escala; puede ser holgada para un detalle pequeño de una componente grande, lo que es aceptable porque aquí la escala no decide la existencia |
| S2 | escala local (`|F′|·Δt` o el radio de curvatura) | depende de la parametrización; más frágil |
| S3 | ninguna tolerancia, solo la anchura de las envolventes | rigurosa, pero sin control de coherencia |

Como la escala solo interviene en la vía certificada, adoptarla **no restringe** la vía G8B-R1.

### 3.6 Pares de loci

Los pares usan `max(1, ‖P₁‖, ‖P₂‖)`, la **distancia al origen**
(`LocusPairIntersectionSolver2D:593-596`). Eso contradice §8.1 de la especificación aprobada:

- **recomendación para `CU3`**: no cambiar la aceptación de pares, que está fuera del alcance
  mínimo, y registrar la no conformidad como limitación conocida con una enmienda futura;
- **alternativa**: corregirla en `CU3`. Afecta en las dos direcciones a la admisibilidad de los
  pares trasladados, y exigiría su propio análisis de compatibilidad.

## 4. Política de consumo de la evidencia

| Consumidor | Acepta | Regla |
|---|---|---|
| resultado rico e inspector | todos los grados y clases | muestra el grado, la clase, el orden y la evidencia tal cual; las clases 3 a 5 se conservan como resultado y diagnóstico |
| punto derivado por token, `Intersect(R, token)` | raíces transversales `LOCALLY_VERIFIED` o superiores (G8B-R1, sin cambios, más la vía certificada); contactos sin cambio de signo solo de las clases 1 y 2 (Etapa B §5) | el grado no forma parte del token: es evidencia de la revisión y se muestra en la procedencia del punto |
| segunda incidencia de la Etapa A | solo `EXISTENCE_STRUCTURAL` o `EXISTENCE_EXACT` | es una igualdad en un punto prescrito (Etapa B §2b) |
| materialización sin conductor de contactos (incremento `CU3-B`) | solo las clases 1 y 2 | Etapa B §5 y §6 |
| otras construcciones del DAG que usan el punto | el punto, como cualquier punto | su grado se propaga como procedencia: nunca se convierte en un certificado de los consumidores, y un certificado de `CU2` que capture el punto como constante independiente del conductor sigue certificando solo su configuración capturada |
| exportaciones (DXF, LaTeX) | el punto | declaran el grado donde el formato lo permite y nunca afirman exactitud que no tenga |

**Reglas contra la promoción silenciosa:**

1. El grado de una solución en una revisión es la evidencia más fuerte **calculada en esa
   revisión**. Nunca sube por una tolerancia, por la coincidencia de coordenadas, por una etiqueta
   del usuario, por la persistencia ni por un resultado de otra revisión.
2. `LOCALLY_VERIFIED` nunca se lee como `EXISTENCE_CERTIFIED`, ni `EXISTENCE_CERTIFIED` (que es
   existencial, §2a) como una igualdad en un punto prescrito.
3. Un cambio de grado entre revisiones no cambia la identidad. El inspector lo muestra, y un
   consumidor que exija un grado mínimo queda indefinido con su causa si ya no se cumple.
4. Como en las clases métricas de `CU1` (ADR 0037), el grado dice si un resultado puede alimentar
   un consumidor que exige rigor, y nunca se infiere de la calidad aparente del valor.

## 5. Compatibilidad, identidad y persistencia

**Hecho registrado.** El token `text1` guardado en el modelo del autor es un `locus-root/v3`. Su
selector incluye el contrato del adaptador de residuo:

```text
CIRCLE|g8b-circle-radial-distance-adapter/v1|MODEL_COORDINATE_DISTANCE|model-coordinate|
signed radial distance from captured GeoConic circle|max(1,captured-radius)
```

e incluye también el germen `g9u0-r4/current-transverse-root-germ/v1` con la orientación
`negative`. Es el token de una raíz transversal de un locus conducido por `E_1`
(`true-coordinate/geo:5ffebead…` es la identidad de `E_1`) frente a una circunferencia. La
identidad del locus que nombra (`geo:fcf47233…`) no aparece en el documento actual, y ningún
comando usa `text1`: es un token huérfano, conservado como texto. Su forma muestra qué entra en la
identidad de un token.

Consecuencias:

1. **El texto del contrato del adaptador no cambia.** La escala S1 vive en la identidad de la
   **política** de la vía certificada (nombre de trabajo `cu3-certified-admission/v1`), que entra
   en la identidad del resultado (§8 de la especificación) y no en la del token. Si el texto del
   adaptador cambiara, ningún token guardado volvería a coincidir.
2. **Cambios aditivos** (vía certificada, H02): las raíces antes rechazadas reciben tokens nuevos.
   No cambia ningún token existente.
3. **Cambios restrictivos identificados** (regla de conflicto, §3.3): un token guardado de una
   solución contradicha deja de resolverse, y su punto derivado queda indefinido con una causa
   tipada (`CONTRADICTED_BY_CERTIFIED_EVIDENCE`). Son la raíz de la costura de `e_1·c_1` en el
   modelo del autor (H-CU3-2), las raíces espurias de H-CU3-1, las tangencias en extremos
   clasificadas como transversales y los duplicados de H-CU3-4. `AGENTS.md` §11.6 exige una
   decisión de compatibilidad, una nota de migración y evidencia de regresión (D-CU3-7):
   - **(a)**, recomendada: la regla se aplica a todos los documentos, con un diagnóstico que
     explica la contradicción y ofrece la forma estructural de la Etapa A
     (`Intersect(e_1, c_1, J)`);
   - **(b)**: los documentos anteriores a `CU3` conservan su admisibilidad hasta una migración
     explícita del usuario, con la política persistida en el documento (ADR 0013).
4. **Precisión de G8B-R1 para raíces pares** (Etapa B §5): ningún token existente afectado.

## 6. H06: diagnóstico y propuesta H-3-R

**Causa** (P8). `GeoAxis` es un `GeoLine`, y la tabla de objetivos lo admite como recta
(`LocusIntersectionTargets2D:111-113`). El fallo está en la **identidad**:
`LocusV2PublicOperations.intersect` publica las participaciones del resultado en el registro de
identidad espacial, y `SpatialIdentityRegistry.validateAttachmentGeo` rechaza el eje con
`GEO_NOT_SERIALIZABLE` porque no tiene un vínculo estable de elemento ordinario. El procesador de
álgebra convierte esa excepción en `CAS.GeneralErrorMessage`. La causa es la misma para `xAxis` y
para `yAxis`; `y = 0` funciona porque es un elemento ordinario.

Alternativas para la decisión D-CU3-8:

| Opción | Contenido | Valoración |
|---|---|---|
| **A** (recomendada) | los ejes son **referencias reservadas del núcleo** con identidad simbólica estable (`xAxis`, `yAxis`, como ya los nombra el XML de comandos), admitidas por el registro de identidad como participantes no ordinarios; el adaptador los trata como rectas no acotadas | cumple H06 sin inferir identidad de etiquetas: la identidad reservada es un contrato del núcleo, no la etiqueta visible; toca el registro de identidad, una superficie aprobada (ADR 0026) |
| B | un mensaje explícito y tipado en lugar del error genérico, sin admitir los ejes | mínima, pero no cumple la decisión H-3, que recomendaba admitirlos |
| C | sustituir el eje en silencio por una recta ordinaria `y = 0` | **rechazada**: crea un objeto oculto, cambia la identidad e infiere equivalencia por coordenadas |

Con la opción A, el mensaje tipado de la B se emite igualmente para los objetivos que sigan sin
admitirse. Es un cambio aditivo: ningún documento existente contiene un resultado con un eje.

## 7. Pruebas exigidas (resumen; detalle en la matriz)

- **Inversión de `pins.H02`** por la vía certificada, en todo el barrido de escala y traslación,
  sin perder las raíces de la línea base.
- **Vía G8B-R1 intacta.** Las suites G8B, G8C1, G8C2 y G9U0-R4 pasan sin cambios, salvo las filas de
  conflicto identificadas.
- **Regla de conflicto.** Cada contradicción tipificada, con su prueba adversaria y su
  compatibilidad.
- **Política de consumo.** Ninguna promoción: el grado mínimo exigido por cada consumidor.
- **Diagnósticos de rechazo emitidos.**
- **Inversión de `pins.H06`**, con la persistencia de la identidad reservada.
- **Vista.** Invariancia ante el zoom, el DPI y el tamaño de la vista.

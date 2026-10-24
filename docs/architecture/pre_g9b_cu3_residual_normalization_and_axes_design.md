# PRE-G9B-CU3 — Normalización de residuos (H-2) y ejes como objetivos (H-3)

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de `CU3` para H02 y H06. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1 (H-2 y H-3) y §4 (las tolerancias nunca dependen del zoom, el DPI, la cámara ni el tamaño de la vista) |
| Autoridad vigente | `locus-v2-intersections.md` v1.2 §8 (en particular §8.1: «any characteristic geometric scale used by the relative check is translation-invariant… independent of viewport and coordinate-origin offset») y §9; `locus-v2-public-surface.md` §8; ADR 0013 (persistencia de políticas), ADR 0017 y ADR 0026; `AGENTS.md` §1.8, §10 y §11.5–§11.6 |
| Antecedentes | [estudio H de `CU0`](pre_g9b_cu0_precision_and_tolerance_study.md) §4 y §5 (H-b, H-c); enmiendas candidatas A3.1 y A3.4 de `CU0`; decisiones H-2 y H-3 de la [hoja de `CU0`](../validation/pre_g9b_cu0_author_decision_sheet.md) |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §3.1 y §4 (H-CU3-1 a H-CU3-6); P7, P8 y P9 de la [caracterización](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json) |

## 1. H02: diagnóstico

Hay tres mecanismos, confirmados por P7:

1. **Escala unidad para rectas, segmentos, rayos, gráficas e implícitas.** El umbral efectivo es
   `4·10⁻¹²` en unidades del modelo para cualquier tamaño de la construcción.
2. **Corchete absoluto `1e-12` en el parámetro.** Tras la bisección, el residuo vale
   aproximadamente `|dh/dt|·Δt`, que crece con la escala, así que una raíz regular se rechaza
   cuando `|dh/dt|` pasa de 4 a 8.
3. **Traslación.** Los valores binary64 de las coordenadas pierden precisión absoluta con la
   distancia al origen. Con la misma escala, el número de raíces admisibles cambia con la
   traslación (P7: radio 200, 0/2 en el origen y 2/2 a 10⁶), y a 10⁶ aparecen duplicados
   (H-CU3-4).

En todos esos casos, el sondeo **certifica la existencia de las dos raíces** por un cambio de signo
de envolventes que excluyen el cero, y sus residuos divididos por la extensión `3r` de la imagen
están entre `0` y `7,6·10⁻¹⁷`. El defecto no es de precisión, sino de **criterio de aceptación**:
el residuo absoluto se usa como prueba de existencia, cuando no lo es (Etapa B §2).

Los pares de loci usan `max(1, ‖P₁‖, ‖P₂‖)`, la **distancia al origen**
(`LocusPairIntersectionSolver2D:593-596`). Eso contradice §8.1 de la especificación de
intersecciones, ya aprobada: es una falta de conformidad, no una propuesta nueva.

## 2. Principio

- **La existencia de una raíz transversal se prueba con condiciones abiertas.** Si en un
  corchete `[a, b]` donde el programa está certificado válido las envolventes de `h(a)` y `h(b)`
  excluyen el cero con signos opuestos, existe una raíz (teorema del valor intermedio). Si
  además la envolvente de `h′` en `[a, b]` excluye el cero, es única y simple. Esa es la
  evidencia de aceptación.
- **El residuo normalizado es un control de consistencia**, no una prueba. Compara el valor
  evaluado en la raíz refinada con una escala geométrica característica, para detectar una
  evaluación incoherente con la declarada. Si lo supera, el candidato se rechaza y se emite el
  diagnóstico `RESIDUAL_REJECTED`, que hoy se declara y nunca se emite. No acepta nada por sí solo.
- **Ninguna tolerancia semántica lee la vista.** Ni el zoom, ni los píxeles, ni el DPI, ni la
  cámara, ni el tamaño de la vista. P9 lo comprueba en el estado actual, y la regla se conserva.

## 3. Propuesta H-2-R

### 3.1 Aceptación de raíces transversales

| Elemento | Regla candidata |
|---|---|
| Existencia | cambio de signo certificado con envolventes (§2), en lugar del residuo absoluto |
| Unicidad | `h′` que excluye el cero en el corchete; si no, el candidato pasa a la taxonomía de la Etapa B §3 |
| Corchete | refinado hasta que el control de consistencia se cumpla o hasta la anchura de unos pocos `ulp(t)`, con la parada por el residuo (`Δt ≤ τ·L / |h′|`) y no por un corchete absoluto |
| Deduplicación | dos candidatos son la misma preimagen **si y solo si** sus corchetes certificados se solapan y `h′` excluye el cero en la unión; nunca se fusionan dos preimágenes distintas por cercanía de coordenadas (§8.2) |
| Clase de contacto | la decide la evidencia (Etapa B §4.4), no `|n·t| ≤ 1e-10`, que queda como indicador de diagnóstico |

### 3.2 Escala característica

Alternativas para la decisión D-CU3-6:

| Opción | Escala `L` | Valoración |
|---|---|---|
| **S1** (recomendada; A3.1 de `CU0`) | la mayor de la extensión de la imagen de la componente del locus y la del objetivo acotado (la del locus si el objetivo no está acotado), con un suelo absoluto solo para extensiones degeneradas | invariante por traslación y covariante con la escala; puede ser holgada para un detalle pequeño de una componente grande, lo que es aceptable porque la escala ya no decide la existencia |
| S2 | escala local (`|F′|·Δt` o el radio de curvatura) | depende de la parametrización y del corchete; más frágil |
| S3 | ninguna tolerancia: solo la anchura de las envolventes | rigurosa, pero no detecta una evaluación incoherente cuando las envolventes no se pueden calcular (capacidades sin clase v3) |

**Combinación recomendada.** `τ_rel·L`, con `τ_rel` versionado; el valor inicial es el `2e-12`
vigente, pero ahora multiplica a una escala con sentido geométrico. Un suelo absoluto
`max(τ_abs, τ_rel·L)` solo para `L` degenerada, con `τ_abs` el `2e-12` vigente. En los pares, la
misma regla con la mayor de las extensiones de las dos componentes. Nunca la distancia al origen.

**Capacidades sin envolventes.** Para fuentes fuera de la clase v3 y que no sean SplineV2, y para
el evaluador heredado, la existencia no se puede certificar. En la decisión D-CU3-6 hay dos
alternativas:

- **(i)**, recomendada: esas raíces se publican con la evidencia numérica y **no son admisibles**
  para la materialización; en el inspector aparecen como `CONTACT_NOT_ESTABLISHED` o como
  candidatas;
- **(ii)**: conservan el criterio actual, con la escala S1, y se marcan como verificación numérica
  bajo hipótesis.

La (i) es coherente con §5.1. Puede reducir los puntos materializables en documentos que hoy los
tienen, así que exige la decisión de compatibilidad D-CU3-7.

## 4. Compatibilidad, identidad y persistencia

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

1. **La regla de escala no debe cambiar dentro del texto del contrato del adaptador.** Ese texto
   identifica la *cantidad* residual, no la tolerancia. La escala nueva pertenece a la identidad
   de la **política** (`g8b-initial-normalized/v1` pasa a una versión nueva, nombre de trabajo
   `cu3-certified-existence-normalized/v1`), y no a la del adaptador. Si el texto del adaptador
   cambiara, ningún token guardado antes de `CU3` volvería a coincidir. Recomendación: conservar
   el texto del adaptador y versionar solo la política, que participa en la identidad del
   resultado (§8) pero no en la del token.
2. **Reclasificar una tangencia mal clasificada como transversal** (H-CU3-2) hace que su token
   deje de resolverse: el germen transversal ya no existe. El punto derivado `Intersect(R, token)`
   quedaría indefinido con `IDENTITY_DISCONTINUITY`. Es la semántica correcta, el fallo cerrado de
   una raíz que nunca fue transversal, pero cambia el resultado de un documento antiguo. `AGENTS.md`
   §11.6 exige entonces una decisión de compatibilidad, una nota de migración y evidencia de
   regresión (D-CU3-7). Alternativas:
   - **(a)**, recomendada: el cambio se aplica a todos los documentos, con un diagnóstico que
     explica la reclasificación y ofrece la forma estructural de la Etapa A
     (`Intersect(e_1, c_1, J)`);
   - **(b)**: los documentos anteriores a `CU3` conservan la política anterior hasta una migración
     explícita del usuario, y la política entra en el documento como exige el ADR 0013.
3. **Raíces que pasan a ser admisibles** (H02 a gran escala). Las raíces antes rechazadas reciben
   tokens nuevos. No cambia ningún token existente: es un efecto aditivo.
4. **Raíces espurias que dejan de existir** (H-CU3-1). Un token persistido de una raíz espuria
   deja de resolverse y su punto queda indefinido. Mismo tratamiento que en el punto 2.

## 5. H06: diagnóstico y propuesta H-3-R

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
| **A** (recomendada) | los ejes son **referencias reservadas del núcleo** con identidad simbólica estable (`xAxis`, `yAxis`, como ya los nombra el XML de comandos), admitidas por el registro de identidad como participantes no ordinarios. El adaptador los trata como rectas no acotadas. El resultado y sus tokens usan esa identidad reservada | cumple H06 (el autor eligió admitir los ejes) sin inferir identidad de etiquetas: la identidad reservada es un contrato del núcleo, no la etiqueta visible; toca el registro de identidad, que es una superficie aprobada (ADR 0026) |
| B | un mensaje explícito y tipado en lugar del error genérico, sin admitir los ejes | mínima, pero no cumple la decisión H-3 que recomendaba admitirlos |
| C | sustituir el eje en silencio por una recta ordinaria `y = 0` | **rechazada**: crea un objeto oculto, cambia la identidad e infiere equivalencia por coordenadas |

Con la opción A, el mensaje tipado de la B se emite de todos modos para los objetivos que sigan
sin admitirse.

## 6. Pruebas exigidas (resumen; detalle en la matriz)

- **Inversión de `pins.H02`:** dos raíces admisibles en todo el barrido de escala y traslación,
  sin perder las de la línea base.
- **Inversión de `pins.H06`:** `Intersect(L, xAxis)` e `Intersect(L, yAxis)` creados y
  persistentes. Guardar, reabrir, Deshacer y Rehacer conservan la identidad reservada.
- **Diagnósticos de rechazo emitidos.**
- **Pares:** normalización sin el origen, con traslación de 10⁶.
- **Vista:** invariancia de todo resultado semántico ante el zoom, el DPI y el tamaño de la vista.
- **Compatibilidad:** documentos anteriores a `CU3` con tokens persistidos (incluido `text1`),
  con la opción elegida en D-CU3-7.

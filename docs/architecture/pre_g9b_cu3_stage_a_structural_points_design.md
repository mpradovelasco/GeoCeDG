# PRE-G9B-CU3 — Etapa A: puntos estructurales por correspondencia del conductor

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la Etapa A de `CU3`. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1–§4; precisión autoral «Decisión sobre la planificación de CU3» (2026-10-24): distinguir rigurosamente los cuatro hechos de la correspondencia, no presuponer que `K` pueda actuar como conductor y fallar cerrado cuando no exista una correspondencia demostrable |
| Autoridad vigente | `locus-v2-semantics.md` v1.3 (conductores y proveedores); `locus-v2-point-interaction.md` §7, §12, §16 y §17; `locus-v2-existence-components.md` v1.1 §15; `locus-v2-public-surface.md` §6, §8–§10; `locus-v2-intersections.md` §5 y §8; ADR 0027 y 0038; `AGENTS.md` §1, §4, §11.3–§11.5 |
| Antecedente | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md) §4 (F-1); enmienda candidata A3.3 de `CU0` |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §2, §3.3 y §5 |

## 1. Objetivo

Materializar un punto de un Locus V2 `L` **en la posición del conductor que corresponde a un
punto `D`**, sin buscar raíces. Opcionalmente, ese punto se establece como intersección con un
objetivo `r` cuando la segunda incidencia está probada. Es la Etapa A de DA-3: puntos
estructurales con las dos incidencias.

La propuesta F §4 de `CU0` definía `P = F(t(D))` «para un punto `D` cuya pertenencia al soporte
está establecida por procedencia». La precisión autoral de 2026-10-24 pide no dar eso por
supuesto. Este diseño separa cuatro hechos que la propuesta F fundía en uno.

## 2. Los cuatro hechos

Notación:

- `L = LocusV2(Q, E)` con conductor `E` sobre el soporte `S` (familia del proveedor: punto de
  circunferencia, de arco circular o de segmento);
- `F` es la evaluación del corte;
- `D` es el punto propuesto;
- `r` es el objetivo opcional.

### 2.1 Hecho 1 — pertenencia de `D` a sus objetos de origen

**Qué afirma.** `D` está sobre los objetos de los que procede, por la semántica de su algoritmo.
Para `K = Intersect(p, i, 2)`, `K ∈ p ∩ i` en la configuración ideal.

**Qué no afirma.**

- Que las coordenadas binary64 de `D` cumplan exactamente esas ecuaciones: no las cumplen.
- Cuál de las salidas es `D` cuando el algoritmo tiene varias: la salida de índice 2 es la que el
  estado de selección heredado asigne en cada revisión.
- Que `D` sea un punto sobre un camino: `K.isPointOnPath()` es `false`.

**Clases de procedencia candidatas** (decisión D-CU3-2):

| Clase | Ejemplo | Admitida |
|---|---|---|
| `PATH_POINT` | `D = Point(S)` o cualquier punto con `isPointOnPath()` y `getPath() == S` | sí |
| `INTERSECTION_OUTPUT` | `D` es salida de un algoritmo de intersección heredado (`AlgoIntersectLineConic`, `AlgoIntersectConics`, `AlgoIntersectLines`, `AlgoIntersectSingle` con su fuente) **una de cuyas entradas es exactamente `S`**, cuya semántica coloca todas sus salidas sobre esa entrada | sí, con una tabla cerrada y versionada de algoritmos |
| `DEFINING_POINT` | `D` es un punto definitorio del soporte (el inicio o el final de un segmento o de un arco) | sí, con parámetro exacto 0 o 1 |
| `LOCUS_STRUCTURAL` | salida estructural de GeoCeDG sobre un soporte que es a su vez un Locus V2 | no en `CU3` |
| cualquier otra | punto libre cercano, imagen por una transformación, punto con coordenadas coincidentes, intersección con un objeto **igual** a `S` pero distinto | **no**: fallo cerrado `PROVENANCE_NOT_ESTABLISHED` |

La tabla es una lista blanca: un algoritmo que no figura en ella no establece pertenencia aunque
su salida esté geométricamente sobre `S`.

### 2.2 Hecho 2 — pertenencia de `D` al soporte concreto del conductor de `L`

**Qué afirma.** El objeto de origen de `D` que lo coloca sobre un soporte es **el mismo objeto**
que el soporte del conductor de `L`.

**Cómo se establece.**

- El descriptor del generador de `L` registra la identidad durable del soporte
  (`SemanticGeneratorDescriptor1D`).
- La entrada del algoritmo de `D` debe ser ese objeto: la misma referencia viva y el mismo
  `PersistentGeoId`.
- En el corte aislado del evaluador, el soporte copiado se resuelve solo por `PersistentGeoId`
  (`ReconstructibleLocusEvaluator2D:199-218`). La correspondencia usa el soporte vivo y su
  identidad; nunca compara coordenadas entre el soporte vivo y la copia.

**Fallo cerrado.**

| Situación | Resultado |
|---|---|
| el origen es un objeto distinto aunque coincida geométricamente (otra `Circle[B, 5]`) | `DRIVER_SUPPORT_MISMATCH` |
| el origen es una copia del soporte (macro, copia del documento) sin el mismo identificador | `DRIVER_SUPPORT_MISMATCH` |
| `L` no tiene un conductor de punto sobre un camino (escalar o punto de rama) | `UNSUPPORTED_DRIVER_FAMILY` |
| el soporte de `L` cambió por redefinición | se reevalúa la correspondencia con el soporte nuevo; si `D` ya no procede de él, `DRIVER_SUPPORT_MISMATCH` |

**Modelo del autor.** El camino de `E_1` es el objeto `p` (misma referencia y mismo
identificador), y `p` es entrada del algoritmo de `K`. El hecho 2 se cumple.

### 2.3 Hecho 3 — existencia, unicidad y orientación de `D ↦ t(D)`

Los hechos 1 y 2 prueban que el punto ideal `D` está sobre `S`. De ahí se deduce la existencia
de un parámetro, porque la parametrización del soporte es sobreyectiva sobre `S`. La unicidad y
la orientación dependen de la familia:

| Familia | Dominio | Existencia y unicidad | Orientación |
|---|---|---|---|
| punto de circunferencia (`circle-point/v1`) | `[−π, π)` periódico | para todo punto de `S`, único módulo `2π`; canonicalización del proveedor en la costura | la del sistema propio de la cónica, la misma que usa el conductor |
| punto de arco circular | `[0, 1]` | único si `D` está en el arco; un punto de la circunferencia fuera del arco **no** tiene parámetro | la del arco (extensión con signo) |
| punto de segmento | `[0, 1]` | único si `D` está en el segmento; un punto de la recta soporte fuera del segmento no tiene parámetro | del inicio al final |

**Vías para calcular el valor** (decisión D-CU3-3):

| Vía | Contenido | Valoración |
|---|---|---|
| **(a)** proyección canónica | la fórmula del proveedor aplicada a las coordenadas de `D`: ángulo en el sistema propio, parámetro del arco o proyección sobre el segmento | da `t̃(D)` en binary64; error acotado por el condicionamiento (`|Δt| ≤ |ΔD|/ρ` en una circunferencia de radio `ρ`); es la recomendada para el **valor** |
| **(b)** colocar el conductor por el DAG | fijar el `PathParameter` del conductor como `AlgoPointOnPath` | numéricamente igual que (a), porque GeoGebra coloca el punto con la misma proyección. Además **movería el conductor del usuario**: rechazada. `Point(L, D)` evalúa la definición en `t̃(D)` sin tocar el conductor vivo |
| **(c)** ecuación del algoritmo de origen | obtener `t` de la solución paramétrica de la intersección, sin pasar por coordenadas | `AlgoIntersectLineConic` no expone parámetros; sería una capacidad nueva por familia sin ganancia de rigor salvo con aritmética exacta; no se recomienda |
| **(d)** rechazo | cuando no hay vía demostrable | obligatoria como resultado de fallo cerrado |

**Rigor.** La existencia, la unicidad y la orientación son **estructurales** (hechos 1 y 2 más la
geometría de la familia). El valor `t̃(D)` es una **aproximación binary64**. La identidad del
punto no depende de ese valor (§4), así que el redondeo afecta a la precisión y no a la
integridad, conforme al principio 8 del registro E1.

**Dominios acotados.** Si `t̃(D)` cae fuera de `[0, 1]`:

- dentro de su **cota certificada de error** (la proyección encerrada en un intervalo con
  redondeo hacia fuera corta el dominio), la procedencia prueba la pertenencia, que es una
  condición cerrada, y el valor se recorta como hecho de precisión;
- fuera de esa cota, el resultado es `OUT_OF_DRIVER_DOMAIN`.

Nunca se recorta por una tolerancia fija. Un `DEFINING_POINT` recibe su parámetro exacto 0 o 1.

**Costura.** El valor canónico de un punto en `±π` es `−π`. El punto es el mismo a ambos lados,
así que la costura no es ambigua para `P`. Solo importa para la continuación del lado (§5).

**Modelo del autor.** La vía (a), y un cálculo independiente del ángulo en el sistema propio,
dan `t(K) = 0` **exactamente**, porque `K.y − B.y = 0`
en binary64 y los vectores propios son `(1, 0)` y `(0, 1)`, y el conductor colocado en `0`
reproduce `K` bit a bit. Para `J`, `t(J) = π`, canónicamente `−π`.

### 2.4 Hecho 4 — evaluación de `F(t(D))` y segunda incidencia

**`P ∈ L`.** Se cumple por construcción **solo si** `t̃(D)` está en una componente continua
certificada o en una componente puntual certificada de `L` (consultas de `CU2`,
`locus-v2-existence-components.md` §15). Si no lo está, `P` queda indefinido con la causa tipada
de la consulta (`OUTSIDE_CERTIFIED_COMPONENT`, `AMBIGUOUS_COMPONENT`, `NOT_ESTABLISHED`).

Si la evaluación numérica falla con la existencia certificada, la posición es `UNAVAILABLE`, no
inexistente (V-POINT-4 de `CU2`).

**`P ∈ r`.** Es un hecho aparte, que solo afirma `Intersect(L, r, D)`. Se establece con la
evidencia de las clases 1 o 2 de la [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md)
§3, por los mecanismos de §6. **Nunca por un residuo bajo una tolerancia.** Si no se establece:

- `Intersect(L, r, D)` queda indefinido con `INCIDENCE_NOT_ESTABLISHED`;
- su evidencia numérica (la clase 3 o la 4) se muestra en el inspector;
- `Point(L, D)` sigue definido, porque no afirma la incidencia.

**Objetivo limitado.** Si `r` es un segmento, un rayo o un arco, además de la incidencia con su
soporte hace falta la pertenencia del punto a la parte limitada. Si falta, `NOT_ON_LIMITED_TARGET`.
Es el caso de desaparición observado con `D_1` ×3 (análisis §5.5).

## 3. Superficie pública candidata (F-1-R)

| Forma | Significado | Afirma |
|---|---|---|
| `Point(L, D)` | punto de `L` en la posición del conductor que corresponde a `D` | `P ∈ L` (hechos 1–4, primera parte) |
| `Intersect(L, r, D)` | el mismo punto como intersección de `L` con `r` | además, `P ∈ r` probado (hecho 4, segunda parte) |

**Colisión con una forma heredada.** GeoGebra ya tiene `Intersect(<Object>, <Object>, <Initial
Point>)`, cuyo tercer argumento es un punto de arranque de una iteración numérica: una semántica
de **proximidad**. En `Intersect(L, r, D)` con `L` Locus V2, `D` no es nunca un punto inicial.
Hoy el producto rechaza esa forma con un error de argumento (análisis §5.6, P10), así que no hay
reinterpretación silenciosa.

Alternativas para la decisión D-CU3-1:

| Opción | Formas | Valoración |
|---|---|---|
| **A** (recomendada) | `Point(L, D)` e `Intersect(L, r, D)`, tal como F-1 | continuidad con la propuesta aprobada en dirección; la ayuda y la sintaxis dicen expresamente que `D` es una correspondencia del conductor y no un punto inicial; para un operando Locus V2 nunca se recurre a la semántica heredada |
| B | `Point(L, D)` y un comando propio para la incidencia (nombre a decidir, por ejemplo `StructuralIntersect(L, r, D)`) | evita cualquier confusión con la forma heredada; añade un comando nuevo a la superficie |
| C | solo `Point(L, D)`; la incidencia con `r` como diagnóstico del inspector | superficie mínima; no cumple DA-3 («las dos incidencias» como objeto) |

## 4. Objeto semántico, identidad y DAG

- **Algoritmo nuevo** del núcleo compartido (nombre de trabajo
  `AlgoLocusDriverCorrespondencePoint2D`).
  - Entradas: `L`, `D` y, en la segunda forma, `r`.
  - Salida: un punto.
  - Registra `setInputOutput()` con esas dependencias.
  - Su `compute()` resuelve los hechos 1 a 4 en ese orden y para en el primero que falla, con su
    causa.
- **Identidad del punto:** la identidad durable de su propio `GeoElement`, más las identidades
  durables de `L`, `D` y `r` como entradas. **No** contiene token de raíz, parámetro,
  coordenadas, índice de salida ni proximidad.
- **No se persiste ningún parámetro.** Deshacer, Rehacer, guardar, reabrir y copiar reconstruyen
  el punto desde el DAG: la correspondencia se recalcula en cada revisión.
- **Dependencias:** cualquier cambio de `L` (revisión), de `D` o de `r` propaga por el grafo. No
  hay recálculo oculto fuera del DAG (`AGENTS.md` §1.7).
- **Permutación de salidas del origen.** Si el estado de selección heredado permuta las salidas
  del algoritmo de `D`, `D` es el objeto `K` con sus coordenadas actuales y `P` lo sigue: es un
  efecto heredado del objeto `K`, no del punto estructural. El punto no conserva coordenadas
  antiguas ni reasocia nada.

## 5. Vigencia, desaparición, degeneraciones y recuperación

| Situación | Estado del punto | Causa |
|---|---|---|
| `D` indefinido | indefinido | `DRIVER_POINT_UNDEFINED` |
| procedencia no admitida o soporte distinto | indefinido | `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH` |
| `t̃(D)` fuera del dominio | indefinido | `OUT_OF_DRIVER_DOMAIN` |
| `t̃(D)` fuera de las componentes certificadas | indefinido | causa de `CU2` |
| `L` con `Completeness = NOT_ESTABLISHED`, pero `t̃(D)` en una componente certificada | **definido** (admisibilidad local, ADR 0027) | — |
| `r` indefinido, o la incidencia no se establece en esta revisión | `Intersect` indefinido; `Point` definido | `TARGET_UNDEFINED`, `INCIDENCE_NOT_ESTABLISHED`, `NOT_ON_LIMITED_TARGET` |
| la condición vuelve a cumplirse | definido de nuevo, **con la misma identidad**, porque su definición no ha cambiado | — |

El modelo del autor lo ilustra (análisis §5.5): con `D_1` ×3, `Intersect(e_1, n, K)` quedaría
indefinido con `NOT_ON_LIMITED_TARGET`, mientras que `Intersect(e_1, c_1, K)` y `Point(e_1, K)`
seguirían definidos.

## 6. Mecanismos para la segunda incidencia

| Mecanismo | Contenido | Generalidad | Modelo del autor | Recomendación |
|---|---|---|---|---|
| **M1** identidad de términos tras la sustitución del conductor | el término del objetivo `r` es igual al término de un objeto del corte incidente con el punto trazado, con `E := D` | exacta y barata, pero estrecha | **no basta**: las incidencias dependen de `k ∥ g`, de una reflexión y de la simetría de `c_1` (análisis §2 y §5.3) | vía rápida cuando aplica |
| **M2** reglas sintéticas de incidencia con condiciones de no degeneración certificadas por intervalos | paralelas por un mismo punto, intersección de dos rectas distintas, rayo desde el centro, etc. | una base de reglas siempre incompleta: este modelo ya exige reflexiones y simetrías | parcial | **no se recomienda** |
| **M3** evaluación exacta del corte en el **punto exacto `D`** | los programas de clase v3 de regla y compás, con las constantes binary64 capturadas como racionales exactos, generan una torre finita de extensiones cuadráticas; con aritmética exacta (cotas constructivas de separación de raíces) se decide `h(D) = 0` | general para la clase v3 de regla y compás; una capacidad nueva del núcleo con presupuesto | el corte de `K` a `J_1` es de regla y compás sobre constantes racionales (`K` es racional porque `i` es horizontal en binary64); la evidencia P3 da `0E-79` en las dos ramas | **recomendada** como autoridad de la clase 2 |
| **M4** prover simbólico de GeoGebra (Botana, Gröbner) | «generalmente verdadero» con condiciones de no degeneración | depende de un CAS externo y no es determinista en tiempo | — | solo evidencia; nunca autoridad |
| **M5** residuo numérico | `|h| ≤ ε` | — | clase 3 en los tres casos | nunca autoridad (§2.4) |

**Alcance de M3** (siempre declarado):

- Prueba la incidencia para la **configuración exacta de las constantes capturadas**, sustituyendo
  el conductor por el punto exacto `D`, no por `t̃(D)`. `π` no es representable, pero `J` sí.
- No prueba nada sobre el valor escrito por el usuario (47° no es representable) ni sobre el
  cálculo redondeado.
- Una tangencia **estructural** (un teorema válido para cualquier valor de las constantes) se
  prueba. Una coincidencia **accidental** de valores escritos en general no se prueba, y queda
  honestamente en las clases 3 o 4.
- Las constantes no algebraicas (funciones trascendentes del conductor) salen de la clase y dan
  `INCIDENCE_NOT_ESTABLISHED`.

**Recomendación para la decisión D-CU3-4:** la autoridad de la segunda incidencia es M1 ∪ M3. M4
y M5 son evidencia. Sin M3, la Etapa A materializaría `Point(L, D)` con la clase 1 de `P ∈ L`,
pero `Intersect(L, r, D)` solo cuando M1 basta, y eso **no** cubre los tres casos obligatorios.
Por eso M3 es necesario para cumplir DA-3 sin rebajar la evidencia.

## 7. Los tres casos obligatorios

| Forma | Hechos 1–3 | `P ∈ L` | `P ∈ r` | Resultado previsto |
|---|---|---|---|---|
| `Intersect(e_1, n, K)` | se cumplen: salida de `AlgoIntersectLineConic(p, i)`; `p` es el soporte de `E_1`; `t(K) = 0` exacto | componente certificada `COMPLETE` | teorema de §5.3 del análisis; M3 | `M_1`, dinámico |
| `Intersect(e_1, c_1, K)` | igual | igual | igual | `M_1`, dinámico |
| `Intersect(o_1, h_2, K)` | igual | igual (`o_1`) | `K_1 ∈ b_1 = h_2`; M3 | `N_1`, dinámico |
| `Intersect(e_1, c_1, J)` e `Intersect(e_1, r, J)` (costura) | `J` es la salida 1; `t(J) = π` | igual | el mismo teorema con la generatriz `r`; M3 | el punto de la costura, dinámico, como contacto estructural (no transversal) |

**Dinámica.** Con `rbCone`, `α_cone`, `D_1` o `excCyl` cambiados, `K`, `t(K) = 0` y los contactos
siguen la construcción con la misma identidad (P4). `Point(o_1, text2, v_1)` con `v_1 = 0` deja
de ser necesario: el autor puede usar `Point(o_1, K)`.

**Desaparición.** Con `|HD_1| > |HM|`, `Intersect(e_1, n, K)` da `NOT_ON_LIMITED_TARGET` y
reaparece al volver. Si `i` dejara de cortar a `p` (no ocurre en este modelo, porque `i` pasa por
el centro), `K` quedaría indefinido y el punto, `DRIVER_POINT_UNDEFINED`.

## 8. Persistencia y compatibilidad

- Se serializa como `<command name="Point">` o `<command name="Intersect">` con los argumentos
  `L`, `D` (y `r`), más la identidad durable del punto. No hay ningún elemento XML nuevo de
  parámetro.
- **Lectores anteriores.** El producto actual rechaza las dos formas (P10). Abrir con un producto
  anterior un documento que las contenga fallará cerrado en esos comandos. Si la carga del
  documento completo se aborta o solo se pierden esos objetos, no se ha comprobado: se
  caracterizará en la implementación (V-A-COMPAT-2). La sintaxis nueva exige la nota de
  migración de `AGENTS.md` §11.6 (decisión D-CU3-12).
- No cambia ningún documento existente: la Etapa A solo añade formas nuevas.

## 9. Diagnósticos tipados (candidatos)

`DRIVER_POINT_UNDEFINED`, `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH`,
`UNSUPPORTED_DRIVER_FAMILY`, `OUT_OF_DRIVER_DOMAIN`, `OUTSIDE_CERTIFIED_COMPONENT` (con la causa
de `CU2`), `TARGET_UNDEFINED`, `INCIDENCE_NOT_ESTABLISHED`, `NOT_ON_LIMITED_TARGET`,
`EXACT_EVALUATION_BUDGET_EXHAUSTED` y `EXACT_EVALUATION_OUT_OF_CLASS`.

Cada uno es un fallo cerrado: ningún punto conserva coordenadas antiguas ni se reasocia.

## 10. Relación con otros paquetes

- `CU7` (M08, punto sobre Locus V2 por interacción) es otra familia: la Etapa A no crea puntos
  por arrastre ni por proximidad.
- Las componentes puntuales de `CU2` (D-CU2-19) pueden alojar un punto de la Etapa A si `t̃(D)`
  está en una de ellas. Su dibujo queda fuera de `CU3` salvo que la decisión D-CU3-9 lo incluya.

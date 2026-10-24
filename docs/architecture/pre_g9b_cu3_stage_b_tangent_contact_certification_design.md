# PRE-G9B-CU3 — Etapa B: contactos tangentes, contactos en pliegues y evidencia de existencia

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la Etapa B de `CU3`. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1, §2 y §3 |
| Autoridad vigente | `locus-v2-intersections.md` v1.2 §5.1, §6.2, §6.3 y §8; `locus-v2-public-surface.md` §10; `locus-v2-existence-components.md` v1.1 §15; `locus-v2-certified-construction-model.md` v1.2 §10; ADR 0008, 0009, 0017, 0027 y 0038; `AGENTS.md` §1.8 y §11.5 |
| Antecedentes | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md) §5 (`TANGENT_CERTIFIED_ISOLATED`, **no aprobada** tal como está formulada); `OBS-CU0-CU3-TANGENT-EXISTENCE` ([cierre de `CU0`](../validation/pre_g9b_cu0_closeout_record.md)); [plan consolidado](pre_g9b_cu_corrective_usability_plan.md) §2.1 y §4 |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md); [caracterización](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json) |
| Diseño hermano | [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md); [normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) |

## 1. Problema

Para un residuo con signo `h(t) = G(F(t))` de una rama regular, una tangencia aislada de orden
dos cumple `h(t₀) = 0`, `h'(t₀) = 0` y `h''(t₀) ≠ 0`. El motor actual verifica esas raíces, pero
nunca las materializa: el germen, el aislamiento local y la elegibilidad de R4 exigen
`TRANSVERSE_ESTABLISHED`, y la clase de contacto sale de una tangente por diferencias finitas
comparada con `1e-10` (análisis del estado inicial, §3).

La propuesta F-2 de `CU0` habría materializado `TANGENT_CERTIFIED_ISOLATED` con esta evidencia:
un punto crítico `t*` de `h` certificado (raíz simple de `h'`), el signo de `h''` certificado y
`|h(t*)| ≤ ε`. El autor no la aprobó porque **esa evidencia no prueba que exista una raíz de
`h`**. Este documento sustituye esa propuesta.

## 2. Principio: condiciones abiertas y condiciones cerradas

La distinción que gobierna todo el diseño es esta:

- **Condiciones abiertas.** Desigualdades estrictas: `h''(t) ≠ 0` en una caja, `h'` con signos
  opuestos en los extremos, `h(t*) > 0`, no paralelismo, transversalidad. Siguen siendo
  verdaderas ante perturbaciones pequeñas, así que una envolvente por intervalos con redondeo
  hacia fuera las certifica rigurosamente: si la envolvente excluye el cero, la desigualdad se
  cumple.
- **Condiciones cerradas.** Igualdades: `h(t*) = 0`, `P ∈ r`, una raíz doble, la igualdad de dos
  puntos. No son estables ante perturbaciones. Ningún cálculo en coma flotante, con intervalos o
  sin ellos, puede probar una igualdad de este tipo en general: una envolvente que contiene el
  cero es compatible con la igualdad, con dos raíces cercanas y con ninguna.

Una igualdad geométrica solo se establece de dos maneras:

1. **Por estructura.** La procedencia de la construcción, la identidad de términos del DAG o una
   simetría exacta la garantizan.
2. **Por aritmética exacta.** El valor se decide sin redondeo, con un alcance declarado.

Consecuencia directa: la materialización de una tangencia como intersección exige probar una
condición cerrada (`h(t*) = 0`) por una de esas dos vías. Los intervalos solo aportan las
condiciones abiertas que la acompañan: el aislamiento, el signo de `h''` y el orden exacto dos.

## 3. Taxonomía de la evidencia de contacto

Las cinco clases que pide el autor. Para cada candidato de contacto el diseño separa dos hechos:

- la **existencia y la identidad** del punto común, es decir, las dos incidencias;
- el **orden de contacto** (transversal, tangente de orden dos o superior), que es un atributo
  del punto, no su prueba de existencia.

| Clase | Nombre candidato | Evidencia de existencia | Orden de contacto | Materialización autoritativa |
|---|---|---|---|---|
| 1 | `CONTACT_STRUCTURAL` | las dos incidencias, por procedencia constructiva: la Etapa A, la identidad de términos o la pertenencia por procedencia (§4.1) | atributo aparte: estructural, certificado (§4.4) o estimado | **sí** |
| 2 | `TANGENCY_EXISTENCE_CERTIFIED` | `h(t*) = 0` probado por aritmética exacta o por simetría exacta más evaluación exacta, con el alcance declarado (§4.2) | orden dos certificado por `h''` (condición abierta) | **sí** |
| 3 | `CONTACT_NUMERICALLY_COMPATIBLE` | punto crítico `t*` certificado, signo de `h''` certificado y envolvente de `h(t*)` que **contiene** el cero | indecidible | **no** como intersección (§5) |
| 4 | `CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION` y `CRITICAL_POINT_CERTIFIED_TWO_ROOTS` | punto crítico certificado y envolvente de `h(t*)` que **excluye** el cero (condición abierta) | sin contacto: no hay raíz en la caja; o dos raíces transversales simples, certificables con identidades nuevas | **no** en el primer caso; en el segundo, solo las dos raíces transversales por la vía ordinaria |
| 5 | `CONTACT_NOT_ESTABLISHED` | ninguna de las anteriores: falta el aislamiento, falta el signo de `h''` o se agotó el presupuesto | — | **no** |

Reglas:

- La clase 3 nunca se presenta como intersección, y ninguna tolerancia, por pequeña que sea, la
  convierte en clase 2. Es la regla H de `CU0`: «un umbral no convierte una raíz no certificada en
  certificada».
- La clase 4 sí es un resultado riguroso: prueba la ausencia de raíces en la caja o la presencia
  de exactamente dos. Su valor es real, por ejemplo para decidir `CONTACT_LOST` o `CONTACT_SPLIT`
  en §6.
- Las clases son de un candidato local, no de la completitud global. Un resultado puede tener
  candidatos de clase 1 con `Completeness = NOT_ESTABLISHED` (§7).

## 4. Métodos científicos por clase

### 4.1 Clase 1: procedencia constructiva

La Etapa A define el mecanismo: el punto `P = F(t(D))` de un conductor `D` con correspondencia
demostrada. Cuando la segunda incidencia también se deduce de la construcción, el contacto es
estructural. Hay tres submecanismos, que el [diseño de la Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md)
§6 compara:

- **M1, identidad de términos tras la sustitución del conductor.** El término de construcción de
  un objeto del corte, con el conductor sustituido por `D`, es sintácticamente igual al término
  del objetivo `r`. Ejemplo del modelo: `p_1(E_1 := K) ≡ Line[K, g] ≡ m`. Es exacta y decidible
  sin aritmética.
- **M2, reglas de incidencia sintéticas** con condiciones de no degeneración certificadas por
  intervalos. No se recomienda (Etapa A §6).
- **M3, evaluación exacta del corte** en el cuerpo real algebraico generado por las constantes
  capturadas (§4.2), aplicada en la correspondencia exacta del conductor.

### 4.2 Clase 2: certificación exacta de la existencia

Métodos válidos, de menor a mayor generalidad:

1. **Simetría exacta más evaluación exacta.** Si un certificado exacto de simetría (SM-C-6,
   pliegues por simetría exacta de `CU2`) prueba que `h` es par respecto de `t₀`, entonces
   `h'(t₀) = 0` exactamente. Si además `h(t₀)` se decide igual a cero por evaluación exacta del
   corte en `t₀`, y `h''(t₀) ≠ 0` queda certificado por intervalos, existe en `t₀` una raíz del
   parámetro de multiplicidad exactamente dos. Que sea una tangencia o un contacto en un
   pliegue lo decide §4.4. Es el método natural de los tres casos del autor (§8), cuya simetría
   frontal hace `h` par en `t` (propuesta F §2).
2. **Estructura algebraica exacta de `h`** cuando `h` es un predicado de primer nivel
   (`FirstLevelPredicate2D` y la secuencia de Sturm con el `gcd(h, h')` de `CU2`, D-CU2-16). Una
   raíz múltiple exacta de la **configuración capturada** se decide sin redondeo. Su alcance es
   limitado: los residuos de los casos del autor no son de primer nivel, porque pasan por varias
   raíces cuadradas.
3. **Evaluación exacta del corte con cotas de separación.** La aritmética exacta de la geometría
   computacional (por ejemplo, cotas constructivas de separación de raíces del tipo BFMSS) decide
   el signo y la nulidad de expresiones con raíces cuadradas anidadas sobre racionales. Un
   programa de clase v3 de regla y compás sobre constantes binary64, tomadas como racionales
   exactos, genera una torre finita de extensiones cuadráticas, así que la nulidad de `h(t₀)` es
   decidible cuando `t₀` es exacto. Hace falta una capacidad nueva del núcleo; no existe hoy.

**Alcance que debe declararse siempre.** El objeto de la prueba es la **configuración exacta
definida por las constantes capturadas**: los valores binary64 de las entradas independientes,
tomados como racionales exactos. No es el valor escrito por el usuario (47° no es
representable) ni el cálculo redondeado. Es la misma regla que `CU2` aplicó a su certificado
exacto (D-CU2-16, «se certifican como su configuración capturada»).

Una tangencia **estructural** (un teorema del modelo válido para cualquier valor de las
constantes) es exacta en la configuración capturada. Una tangencia **accidental**
(la coincidencia de dos valores escritos) en general no lo es, y queda honestamente en las
clases 3 o 4.

**Métodos que no son de clase 2.**

- El residuo pequeño.
- La clasificación por diferencias finitas.
- El punto crítico certificado con `|h(t*)| ≤ ε` (la F-2 original).
- La estimación por Richardson o con más precisión flotante.
- El prover de GeoGebra («generalmente verdadero», con condiciones de no degeneración y CAS
  externo): es evidencia, no autoridad (Etapa A §6).

### 4.3 Clase 3 y clase 4: punto crítico certificado

Procedimiento, sobre una caja `T = [a, b]` con el programa de clase v3 certificado válido:

1. Envolver `h`, `h'` y `h''` en `T` componiendo la envolvente de segundo orden del punto trazado
   (`ConstructionBoxModel2D`, §10.7 del modelo certificado) con el residuo del objetivo. Esta
   composición es nueva: hoy no existe (análisis del estado inicial, §3.4).
2. Si la envolvente de `h''` en `T` excluye el cero, `h'` es estrictamente monótona.
3. Si `h'(a)` y `h'(b)` tienen signos opuestos certificados, existe un único `t* ∈ T` con
   `h'(t*) = 0`: es un punto crítico aislado (Krawczyk escalar o bisección certificada).
4. Envolver `h` en el intervalo de `t*`:
   - si la envolvente excluye el cero con el mismo signo que `h''`, la clase es 4 sin
     intersección;
   - si lo excluye con el signo contrario y los extremos de `T` tienen el signo de `h''`, la
     clase es 4 con dos raíces;
   - si contiene el cero, la clase es 3.

La clase 3 es un hecho cierto y útil, aunque no sea una intersección: el punto crítico `t*`
existe, es único en `T` y es una raíz **transversal** de `h'`, así que su identidad se puede
continuar (propuesta F §3).

### 4.4 Orden de contacto

El orden es un atributo aparte, y solo tiene sentido geométrico en una **rama regular**
(`F′(t₀) ≠ 0`, `AGENTS.md` §11.5):

- **uno** (transversal), cuando la existencia está probada y `h'(t₀) ≠ 0` está certificado;
- **dos** (tangente), cuando la existencia es de clase 1 o 2, la fuente es regular en `t₀`
  (la envolvente de `F′` excluye el vector cero, una condición abierta), `h'(t₀) = 0` está
  establecido (por estructura, por simetría exacta o por evaluación exacta) y `h''(t₀) ≠ 0`
  está certificado;
- **contacto en un pliegue** (`FOLD_CONTACT`), cuando `t₀` es un pliegue certificado de la
  fuente (`CU2`, SM-C-6, simetría exacta `F(t₀ + s) = F(t₀ − s)`). Entonces `h'(t₀) = 0` se debe
  a `F′(t₀) = 0` y no a una tangencia. El conjunto de puntos de la fuente termina en `F(t₀)` y
  llega al objetivo en la dirección `F″(t₀)`. Su ángulo con el objetivo es un dato geométrico
  (análisis del estado inicial §5.4b: 58,4° con `n` y 31,6° con `c_1`);
- **no establecido** en el resto de los casos, incluido un `F′(t₀)` cuya envolvente contiene el
  cero sin un pliegue certificado.

La clasificación vigente por `|n·t| ≤ 1e-10` con diferencias finitas queda como **indicador de
diagnóstico** y deja de decidir la clase (H-c del estudio H).

**Precisión sobre B08 y B09 (H-CU3-7).** Las «tangencias» de `e_1` con `n` y con `c_1` que
describe la propuesta F de `CU0` son contactos en el pliegue de `t = 0`. Solo `o_1·h_2` es una
tangencia. La existencia de los tres contactos y su materialización por la Etapa A no cambian;
cambian su nombre, su orden y su comportamiento ante perturbaciones (§6.3).

## 5. Materialización: alternativas F-2-R

| Opción | Contenido | Valoración |
|---|---|---|
| **A** | Intersecciones autoritativas solo de clases 1 y 2. Las clases 3, 4 y 5 se muestran en el resultado rico con su evidencia y sin punto | rigurosa; deja sin objeto dinámico al usuario cuando la existencia no puede probarse |
| **B** (recomendada) | A, más un **objeto distinto** y opcional: el «punto crítico certificado de `L` respecto de `r`» (`t*` de la clase 3 o 4). Es un punto de `L` cuya existencia, unicidad local y continuidad están probadas, pero que **no afirma la incidencia** con `r` y lleva la envolvente de `h(t*)` como evidencia | rigurosa; el usuario dispone de un punto dinámico en la posición del contacto aparente, con el estado honesto de su incidencia; requiere nombre y superficie propios, que decidirá el autor |
| **C** | Clase 3 materializada como intersección con un estado visible de «contacto dentro de ε» | **rechazada**: es la F-2 original y presenta como incidencia algo no probado; viola el principio del §2 y `AGENTS.md` §1.8 |

Recomendación: la opción **B**, con estas condiciones:

- El objeto de punto crítico tiene una familia y un nombre propios. Ni `Intersect` ni `Point(L, D)`
  lo sustituyen.
- Su ficha y sus exportaciones dicen expresamente que la incidencia no está establecida.
- Si en una revisión posterior la evidencia sube a la clase 1 o 2, no se convierte en silencio en
  una intersección: la intersección es otro objeto con su propia identidad.

Si el autor prefiere no ampliar la superficie pública en `CU3`, la opción A es coherente y el
punto crítico queda para un paquete posterior.

## 6. Identidad, germen tangente y ciclo de vida

### 6.1 Germen tangente

El germen transversal actual (`g9u0-r4/current-transverse-root-germ/v1/`) no se reinterpreta. Se
propone un germen nuevo, `…/tangent-contact-germ/v1`, con estos elementos:

- el linaje de la componente;
- la familia del objetivo y su orientación canónica (§5.1 y §8.3 de la especificación de
  intersecciones);
- el **lado del contacto**: el signo de `h''` respecto de la orientación canónica del objetivo;
- el orden establecido;
- la clase de evidencia de existencia (1 o 2).

No contiene coordenadas, parámetros ni índices. El selector R4 de un contacto tangente es «linaje
de la componente + germen tangente», con el mismo enriquecimiento por fase y rango de ADR 0017
cuando dos contactos compartan el selector base. El libro de tokens recibe una versión nueva del
formato de estado; los tokens transversales y sus versiones no cambian.

### 6.2 Continuación

En cada revisión se recalcula `t*` por continuación del punto crítico, que es una raíz simple de
`h'`. La continuación es evidencia de la revisión, no identidad (§8.2 de la especificación).

### 6.3 Transiciones

| Situación tras un cambio | Estado | Efecto |
|---|---|---|
| la existencia sigue siendo de clase 1 o 2 y el mismo germen | `CURRENT` | el punto se actualiza |
| clase 4 sin intersección (`h(t*)` con el signo de `h''`) | `CONTACT_LOST` | punto indefinido con diagnóstico; no se reasocia |
| clase 4 con dos raíces | `CONTACT_SPLIT` | punto indefinido; las dos raíces transversales reciben identidades nuevas por la vía ordinaria; ninguna hereda el token tangente |
| clase 3 (indecidible) | `CONTACT_NOT_CERTIFIED` | punto indefinido; la evidencia numérica se muestra; no es `LOST` ni `SPLIT` |
| `h''` deja de estar certificado lejos del cero, o falta el aislamiento | `IDENTITY_DISCONTINUITY` | punto indefinido |
| se recupera la clase 1 o 2 desde el mismo germen y el mismo linaje | `CURRENT` | reaparece con la misma identidad |

La fila de clase 3 corrige la propuesta F §5.3, que asignaba `CURRENT` a «`|h(t*)| ≤ ε`».

**En un pliegue.** Si `t₀` es un pliegue certificado, la clase 4 con dos raíces da dos
preimágenes `t₀ ± s` de **un único punto geométrico**, no dos intersecciones. Por eso:

- en un pliegue, `CONTACT_SPLIT` no crea dos puntos en la misma posición;
- el punto geométrico es uno, con un par de preimágenes emparejadas por el certificado de
  simetría;
- materializar uno de esos puntos exige elegir explícitamente la preimagen (la rama del lado)
  o tratar la pareja como una sola identidad;
- nunca se elige en silencio (`AGENTS.md` §11.4).

Cuál de las dos opciones adopta la implementación es parte de la decisión D-CU3-11.

### 6.4 Tangencia en un extremo incluido

En un extremo incluido no hay cambio de signo de `h'` dentro del dominio. La existencia exige la
misma condición cerrada, `h(t_end) = 0`, probada por las clases 1 o 2. Las condiciones abiertas
unilaterales (el signo de `h'` en el interior y el de `h''`) dan el orden y el aislamiento. Si
falta la prueba, la clase es 5. La Etapa A cubre los extremos estructurales.

## 7. Compatibilidad con Locus V2 y los certificados de `CU2`

- La materialización de un contacto exige que su parámetro esté en una componente continua
  certificada o en una componente puntual certificada (`locus-v2-existence-components.md` §15).
  Si no lo está, la clase es 5 con la causa tipada de la consulta de `CU2`.
- La admisibilidad local se mantiene independiente de la completitud global (ADR 0027):
  `Completeness = NOT_ESTABLISHED` no impide un contacto de clase 1 o 2 local, y un contacto no
  completa nada global.
- Integridad frente a precisión (principio 8 del registro E1, `CU2`): un contacto de clase 1 o 2
  conserva su existencia aunque la posición numérica falle (V-POINT-4 de `CU2`). La posición es
  `UNAVAILABLE`, no inexistente.
- Los certificados de `CU2` no se reabren. Las salidas coincidentes sin etiqueta (D-CU2-18) y las
  componentes puntuales (D-CU2-19) siguen con su contrato; su materialización en `CU3` depende de
  la decisión D-CU3-9 sobre C22, C23 y C40.

## 8. Los tres casos obligatorios

La evidencia medida y la deducción completa están en el
[análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §5 y en la
[Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md) §7.

En resumen, los tres contactos se reducen a una identidad del modelo, `J_1(K) = M_1`:

- `Intersect(e_1, n)` y `Intersect(e_1, c_1)` tienen su punto común en `M_1`;
- `Intersect(o_1, h_2)` lo tiene en `K_1(K)`, sobre `h_2 = Line[M_1, k]`.

Esa identidad es un teorema exacto del modelo para cualquier valor de las entradas, demostrado
en el análisis del estado inicial §5.3. El mismo teorema, con el conductor en `J` y la otra
generatriz frontal `r`, hace del punto de la costura de `e_1·c_1` un contacto estructural, no la
«raíz transversal» que publica el motor vigente (H-CU3-2; decisión D-CU3-10).

Por su orden de contacto (§4.4 y análisis §5.4b):

- `e_1·n`, `e_1·c_1` (en `t = 0`) y `e_1·c_1`, `e_1·r` (en la costura) son contactos en un
  pliegue;
- `o_1·h_2` es una tangencia regular de orden dos.
En la Etapa B:

- **Sin conductor** (`Intersect(L, r)`), la existencia de clase 2 exige la simetría exacta más la
  evaluación exacta (§4.2, método 1), o la evaluación exacta con cotas de separación (método 3).
  Las envolventes por intervalos de la caracterización dan la clase 3 en los tres casos: la
  envolvente de `h(t*)` contiene el cero. Es el comportamiento honesto esperado.
- **Con conductor** (`Intersect(L, r, K)`), la Etapa A los materializa como clase 1 o 2 por la
  correspondencia del conductor, sin búsqueda de raíces.

## 9. Hallazgos del motor vigente que la Etapa B corrige

La caracterización encontró cuatro comportamientos preexistentes que contradicen §5.1 de la
especificación de intersecciones o `AGENTS.md` §1.8 (análisis del estado inicial, §4):

- H-CU3-1: raíces del ruido de redondeo junto a tangencias, publicadas como transversales y a
  veces admisibles.
- H-CU3-2: tangencias en la costura y en un extremo incluido clasificadas como transversales y
  admisibles.
- H-CU3-3: tangencias declaradas donde la ausencia de intersección está certificada.
- H-CU3-4: duplicados de una misma preimagen.

Su causa común es aceptar y clasificar raíces con **valores binary64 sin envolver**: cambios de
signo y residuos de una función redondeada, que no prueban nada de la función geométrica cuando
su magnitud está en el nivel del ruido. La corrección que propone este diseño es la misma para
los cuatro:

- una raíz transversal se acepta solo con un cambio de signo **certificado** (envolventes que
  excluyen el cero en los extremos de su corchete) y la unicidad por `h′` que excluye el cero
  ([normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) §3);
- un candidato cuyo corchete no certifica la clase transversal pasa por la taxonomía de §3.

Qué está medido y qué es inferencia:

- **Medido en el sondeo.** La regla reclasifica los contactos de H-CU3-2, que son de clase 3 y
  no transversales, y certifica la ausencia de intersección en los casos de H-CU3-3 (clase 4).
- **Inferencia** de las anchuras de envolvente medidas, alrededor de `10⁻¹³` cerca del contacto
  frente a variaciones de `h` del orden de `10⁻¹⁵`:
  - las raíces espurias de H-CU3-1 no tendrían un cambio de signo certificado y no se aceptarían;
  - los duplicados de H-CU3-4 tendrían corchetes que se solapan.

La implementación debe confirmar esa inferencia con pruebas propias (matriz, V-B-NOISE-1 y
V-B-DUP-1). Si `CU3` incluye o no esas correcciones es la decisión D-CU3-13.

## 10. Fuera de alcance

- Contactos de orden mayor que dos (`h''(t₀) = 0`) y cúspides. Clase 5 con su diagnóstico.
- Tangencias de pares de loci, cuyo germen exige un determinante de signo certificado (ADR 0017,
  pares excluidos).
- La eliminación algebraica general de raíces múltiples de predicados que no son de primer nivel
  (cuestión abierta 2 de la preparación de `CU2`).
- Cualquier tolerancia configurable por el usuario (H-1, pospuesta).

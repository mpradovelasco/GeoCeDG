# PRE-G9B-CU3 — Etapa B: contactos tangentes, contactos en pliegues y evidencia de existencia

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la Etapa B de `CU3`. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisado por la revisión científica focal de 2026-10-24; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1–§3; instrucción «PRE-G9B-CU3 — Revisión científica focal del candidato de preparación» (2026-10-24), puntos 1, 3, 4, 5 y 6 |
| Autoridad vigente | `locus-v2-intersections.md` v1.2 §5.1, §6.2, §6.3 y §8; `locus-v2-public-surface.md` §10; aclaración G8B-R1 (ADR 0008 e [informe G8B-R1](../validation/g8b_r1_locus_v2_intersection_point_admissibility_report.md)); `locus-v2-existence-components.md` v1.1 §15; `locus-v2-certified-construction-model.md` v1.2 §10; ADR 0009, 0017, 0027 y 0038; `AGENTS.md` §1.8, §11.4 y §11.5 |
| Antecedentes | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md) §5 (`TANGENT_CERTIFIED_ISOLATED`, **no aprobada** tal como está formulada); `OBS-CU0-CU3-TANGENT-EXISTENCE` ([cierre de `CU0`](../validation/pre_g9b_cu0_closeout_record.md)); [plan consolidado](pre_g9b_cu_corrective_usability_plan.md) §2.1 y §4 |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md); [caracterización](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json) |
| Diseño hermano | [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md); [normalización, admisibilidad y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) |

## 1. Problema

Para un residuo con signo `h(t) = G(F(t))` de una rama regular, una tangencia aislada de orden
dos cumple `h(t₀) = 0`, `h'(t₀) = 0` y `h''(t₀) ≠ 0`. El motor actual verifica esas raíces, pero
nunca las materializa: el germen, el aislamiento local y la elegibilidad de R4 exigen
`TRANSVERSE_ESTABLISHED`, y la clase de contacto sale de una tangente por diferencias finitas
comparada con `1e-10` (análisis del estado inicial, §3).

La propuesta F-2 de `CU0` habría materializado `TANGENT_CERTIFIED_ISOLATED` con esta evidencia:

- un punto crítico `t*` de `h` certificado (raíz simple de `h'`);
- el signo de `h''` certificado;
- `|h(t*)| ≤ ε`.

El autor no la aprobó porque **esa evidencia no prueba que exista una raíz de `h`**. Este
documento sustituye esa propuesta.

## 2. Principio: qué certifican los intervalos y qué no

La revisión focal pide distinguir tres tipos de afirmación, que la formulación anterior de esta
preparación mezclaba (A3.0 de la propuesta normativa):

**(a) Existencia de alguna raíz en un intervalo. Certificable con intervalos.** Sea un corchete
`[a, b]` en el que el programa está certificado válido, de modo que `h` es continua. Si las
envolventes de `h(a)` y `h(b)` excluyen el cero con signos opuestos, existe al menos un `t ∈ [a, b]`
con `h(t) = 0`, por el teorema del valor intermedio. Es una afirmación **existencial**: prueba que
hay un punto de `L ∩ r`, aunque no lo localice exactamente. Si además la envolvente de `h'` en
`[a, b]` excluye el cero, la raíz es única y simple. Los métodos de Newton por intervalos, de
Krawczyk y de Miranda dan la misma clase de certificado. Así se certifica una raíz **transversal**.

**(b) Igualdad en un parámetro o en un punto prescrito. No certificable con intervalos.** Son
condiciones cerradas:

- `h(t₀) = 0` para un `t₀` dado;
- `P ∈ r` para un `P` dado (en particular, `F(t*(D)) ∈ r` en la Etapa A);
- la igualdad de dos puntos dados;
- `h'(t₀) = 0` o `F'(t₀) = 0` en un `t₀` dado.

Una envolvente que contiene el cero es compatible con la igualdad y con su negación. La existencia
de una raíz de **multiplicidad par**, que no cambia de signo, pertenece a este tipo: exige
`h(t*) = 0` en el punto crítico `t*`, y ningún cambio de signo la detecta.

**(c) Igualdad estructural frente a igualdad decidida por aritmética exacta.** Una igualdad del
tipo (b) se establece por una de dos vías, que son evidencias distintas y se registran por
separado:

- **estructural**: la procedencia de la construcción, la identidad de términos tras la sustitución
  del conductor, o una simetría exacta certificada;
- **por aritmética exacta**: la evaluación exacta M3 decide el valor en la configuración
  conjunta `Σ_B`, con su frontera declarada ([Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md)
  §6).

Consecuencia directa: una raíz transversal se certifica con (a). La materialización de una
tangencia o de un contacto en un pliegue como intersección exige (b), probado por (c). Los
intervalos aportan las condiciones abiertas que la acompañan: el aislamiento, el signo de `h''` y
la regularidad de la fuente.

## 3. Taxonomía de la evidencia de contacto

Las cinco clases que pide el autor, para candidatos con un punto crítico (sin cambio de signo
local). Para cada candidato el diseño separa dos hechos:

- la **existencia y la identidad** del punto común;
- el **orden de contacto** (§4.4), que es un atributo del punto, no su prueba de existencia.

| Clase | Nombre candidato | Evidencia de existencia | Materialización como intersección en `CU3` |
|---|---|---|---|
| 1 | `CONTACT_STRUCTURAL` | las dos incidencias, por estructura (§2c): la Etapa A con M1, o la procedencia | **sí** |
| 2 | `CONTACT_EXISTENCE_EXACT` | `h(t*) = 0` decidido por aritmética exacta (M3) en el punto exacto del conductor, con la configuración y la frontera declaradas (§4.2) | **sí** |
| 3 | `CONTACT_NUMERICALLY_COMPATIBLE` | punto crítico `t*` certificado, signo de `h''` certificado, y envolvente de `h(t*)` que **contiene** el cero | **no** (§5) |
| 4 | `CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION` o `CRITICAL_POINT_CERTIFIED_TWO_ROOTS` | punto crítico certificado y envolvente de `h(t*)` que **excluye** el cero | **no** sin intersección; con dos raíces, solo las dos raíces transversales, certificadas por §2a con identidades nuevas |
| 5 | `CONTACT_NOT_ESTABLISHED` | ninguna de las anteriores: falta el aislamiento, falta el signo de `h''`, fuera de clase o presupuesto agotado | **no** |

Reglas:

- **Sin promoción.** La clase 3 nunca se presenta como intersección, y ninguna tolerancia la
  convierte en clase 2. La clase de un candidato es la evidencia más fuerte calculada en su
  revisión; nunca sube por tolerancia, por coincidencia de coordenadas ni por persistencia (§9 y
  la política de consumo de la normalización, §4).
- **La clase 4 es rigurosa:** prueba la ausencia de raíces en la caja o exactamente dos.
- **Admisibilidad local.** Las clases son de un candidato local, no de la completitud global
  (ADR 0027 y G8B-R1).

## 4. Métodos científicos por clase

### 4.1 Clase 1: estructura

La Etapa A define el mecanismo: el punto `P = F(t*(D))` de un conductor `D` con correspondencia
demostrada. El contacto es de clase 1 cuando la segunda incidencia se deduce también de la
estructura:

- **M1, identidad de términos tras la sustitución del conductor.** El término de construcción de
  un objeto del corte, con el conductor sustituido por `D`, es sintácticamente igual al término de
  `r`, de modo que el punto trazado está sobre `r` por construcción. Ejemplo genérico: si el punto
  trazado se construye como `Intersect(X, Line[E, Y])` y el objetivo es `Line[D, Y]`, con `E := D`
  los términos coinciden. En el modelo del autor **no hay ninguna** (análisis §2): `p_1 = Line[E_1, k]`
  no es el término `m = Line[K, g]`, aunque las dos rectas sean paralelas por construcción. Esta
  preparación daba antes ese par como ejemplo de identidad de términos; era un error, que se
  corrige aquí.
- **Simetría exacta**, cuando un certificado de `CU2` (SM-C-6) prueba que el contacto está en el
  parámetro fijo de la simetría. Prueba `h'(t₀) = 0`, pero no `h(t₀) = 0` (§4.2).

### 4.2 Clase 2: aritmética exacta (M3)

**Con conductor (Etapa A).** M3 decide `h(D*) = 0` en la configuración conjunta `Σ_B`, sustituyendo
el punto exacto `D*` reconstruido desde su procedencia y su selección heredada (Etapa A §6.2 y
§6.3).

**Sin conductor (`Intersect(L, r)`).** Hace falta un punto exacto del conductor para el parámetro
crítico. Hay dos vías:

1. **Parámetro fijo de una simetría exacta certificada.** Si el certificado de `CU2` prueba que `h`
   es par respecto de `t₀` (pliegue por simetría exacta, SM-C-6), entonces `h'(t₀) = 0`
   exactamente. Si el punto del soporte del conductor en `t₀` es exacto (en una circunferencia,
   `t₀ ∈ {0, π}` da los puntos `c ± r·e₀`, racionales en `Σ_B`), M3 decide `h = 0` en ese punto.
   La envolvente de `h''(t₀)` que excluye el cero (condición abierta) da la multiplicidad exacta
   dos en el parámetro. Para los casos obligatorios, `t₀ = 0` y `t₀ = π` son los puntos `K*` y
   `J*`: es **la misma decisión** que la Etapa A.
2. **Estructura algebraica exacta de `h`** cuando `h` es un predicado de primer nivel: el
   certificado de Sturm con el `gcd(h, h')` de `CU2` (D-CU2-16) decide una raíz múltiple exacta
   en la configuración capturada. Su alcance es limitado: los residuos de los casos del autor no
   son de primer nivel.

Una eliminación algebraica general (resultantes de `h` y `h'` sobre la torre de `Σ_B`) queda como
alternativa R-2 de la puerta de M3, no como capacidad de `CU3`.

**Alcance que se declara siempre.** El objeto de la prueba es una configuración exacta (`Σ_B` o
la configuración capturada de `CU2`), no los valores escritos por el usuario (47° no es
representable) ni el cálculo redondeado.

**Igualdades accidentales.** Una tangencia que no es un teorema del modelo, sino la coincidencia
de valores escritos, **se demuestra si y solo si es exacta en la configuración declarada**. Por
ejemplo, la circunferencia de radio 1,5 trazada y la recta `y = 1,5` son exactamente tangentes en
`Σ_B`, porque los dos valores son representables: M3 lo demuestra (clase 2). Si la coincidencia
solo vale con valores no representables, M3 decide exactamente la desigualdad, que da la clase 4
con el signo exacto. Esto corrige la formulación anterior de esta preparación («una tangencia
accidental en general no es exacta» y «no se certifica nunca»).

**Lo que no es de clase 2:**

- el residuo pequeño;
- la clasificación por diferencias finitas;
- el punto crítico certificado con `|h(t*)| ≤ ε` (la F-2 original);
- la estimación con más precisión flotante (como la evidencia P3 de 80 cifras, que es una
  comprobación y no una prueba);
- el prover de GeoGebra, que es evidencia y no autoridad.

### 4.3 Clase 3 y clase 4: punto crítico certificado

Procedimiento, sobre una caja `T = [a, b]` con el programa de clase v3 certificado válido:

1. Envolver `h`, `h'` y `h''` en `T` componiendo la envolvente de segundo orden del punto trazado
   (`ConstructionBoxModel2D`, §10.7 del modelo certificado) con el residuo del objetivo. Esta
   composición es nueva.
2. Si la envolvente de `h''` en `T` excluye el cero, `h'` es estrictamente monótona.
3. Si `h'(a)` y `h'(b)` tienen signos opuestos certificados, existe un único `t* ∈ T` con
   `h'(t*) = 0`. Es una afirmación del tipo §2a aplicada a `h'`, así que el punto crítico **sí**
   se certifica con intervalos.
4. Envolver `h` en el corchete de `t*`:
   - si la envolvente excluye el cero con el signo de `h''`, la clase es 4 sin intersección;
   - si lo excluye con el signo contrario y los extremos de `T` tienen el signo de `h''`, la clase
     es 4 con dos raíces (dos afirmaciones §2a);
   - si contiene el cero, la clase es 3.

La clase 3 es un hecho cierto, aunque no sea una intersección: el punto crítico existe, es único
en `T` y se puede continuar. Se conserva como evidencia del resultado rico y como diagnóstico (§5).

### 4.4 Orden de contacto

El orden solo tiene sentido geométrico en una **rama regular** (`F′(t₀) ≠ 0`, `AGENTS.md` §11.5):

- **uno** (transversal): la existencia está probada y `h'(t₀) ≠ 0` está certificado;
- **dos** (tangente): la existencia es de clase 1 o 2; la fuente es regular en `t₀` (la envolvente
  de `F′` excluye el vector cero, una condición abierta); `h'(t₀) = 0` está establecido por
  estructura, por simetría exacta o por aritmética exacta; y `h''(t₀) ≠ 0` está certificado;
- **contacto en un pliegue** (`FOLD_CONTACT`): `t₀` es un pliegue certificado de la fuente
  (`CU2`, SM-C-6, simetría exacta `F(t₀ + s) = F(t₀ − s)`). Entonces `h'(t₀) = 0` se debe a
  `F′(t₀) = 0`, no a una tangencia: el conjunto de puntos de la fuente termina en `F(t₀)` y llega
  al objetivo en la dirección `F″(t₀)` (análisis §5.4b: 58,4° con `n` y 31,6° con `c_1`);
- **no establecido** en el resto de los casos, incluido un `F′(t₀)` cuya envolvente contiene el
  cero sin un pliegue certificado.

La clasificación vigente por `|n·t| ≤ 1e-10` con diferencias finitas queda como **indicador de
diagnóstico** y deja de decidir la clase (H-c del estudio H).

**Precisión sobre B08 y B09 (H-CU3-7).** Las «tangencias» de `e_1` con `n` y con `c_1` son
contactos en el pliegue de `t = 0`. Solo `o_1·h_2` es una tangencia. La existencia de los contactos
y su materialización por la Etapa A no cambian; cambian su nombre, su orden y su comportamiento
ante perturbaciones (§6.3).

## 5. Materialización: F-2-R

Por instrucción de la revisión focal, la recomendación para `CU3` es la **opción A**.

| Opción | Contenido | Valoración |
|---|---|---|
| **A** (recomendada para `CU3`) | solo se materializan como intersecciones las clases con existencia establecida: 1 y 2 para los contactos sin cambio de signo; las raíces transversales con existencia establecida por §2a o admisibles por G8B-R1 (normalización §3). Las clases 3, 4 y 5 se conservan en el resultado rico con su evidencia (punto crítico, corchete, envolventes y orden) y como diagnóstico en el inspector, sin punto | rigurosa; no amplía la superficie pública |
| B (opción futura, **no incorporada a `CU3`**) | A, más un objeto público independiente de «punto crítico certificado de `L` respecto de `r`»: un punto de `L` cuya existencia, unicidad local y continuidad están probadas, que no afirma la incidencia | útil para el usuario; exige una familia, un nombre, una ficha y exportaciones propias, y una autorización aparte |
| C | clase 3 materializada como intersección con un estado de «contacto dentro de ε» | **rechazada**: es la F-2 original |

**Relación con G8B-R1.** El informe G8B-R1 admite en principio un «established even/tangent root
with local evidence». La opción A **precisa** que, para una raíz de multiplicidad par, *established*
exige las clases 1 o 2: la evidencia local de G8B-R1 (residuo y aislamiento) no prueba la
existencia de una raíz sin cambio de signo. Es un cambio normativo identificado (A3.2-R), y solo
alcanza a los objetivos de un parámetro.

Su consecuencia de compatibilidad es nula en el producto actual. En las capacidades adaptativa y
polinómica, una raíz recibe aislamiento local `ESTABLISHED` (condición 4 de G8B-R1) solo si es
`TRANSVERSE_ESTABLISHED`:

- `ExtendedTargetIntersectionCapability2D:410-425`;
- `PolynomialTargetIntersectionCapability2D:243-248`.

La capacidad que solo usa el evaluador nunca la concede, y desde G9U0-R4 el germen público exige
además la clase transversal. Ningún documento contiene, por tanto, un token admisible de una raíz
tangente de un parámetro.

Los pares de loci, que pueden publicar `TANGENT_ESTABLISHED` con garantía exacta o certificada
(`LocusPairIntersectionSolver2D:400-421`), quedan fuera del alcance de `CU3` y no cambian.

## 6. Identidad, germen tangente y ciclo de vida

Esta sección solo es necesaria si se autoriza la materialización **sin conductor** de las clases
1 y 2 (incremento `CU3-B` de la estrategia). La Etapa A no usa tokens de raíz.

### 6.1 Germen tangente

El germen transversal actual (`g9u0-r4/current-transverse-root-germ/v1/`) no se reinterpreta. Se
propone un germen nuevo, `…/tangent-contact-germ/v1`, con estos elementos:

- el linaje de la componente;
- la familia del objetivo y su orientación canónica (§5.1 y §8.3 de la especificación de
  intersecciones);
- el **lado del contacto**: el signo de `h''` respecto de la orientación canónica del objetivo;
- el orden establecido (dos o `FOLD_CONTACT`);
- la clase de evidencia de existencia (1 o 2).

No contiene coordenadas, parámetros, índices ni configuración. El selector R4 de un contacto es
«linaje de la componente + germen tangente», con el enriquecimiento por fase y rango de ADR 0017
cuando dos contactos compartan el selector base. El libro de tokens recibe una versión nueva del
formato de estado; los tokens transversales y sus versiones no cambian.

### 6.2 Continuación

En cada revisión se recalcula `t*` por continuación del punto crítico, que es una raíz simple de
`h'`. La continuación es evidencia de la revisión, no identidad (§8.2 de la especificación).

### 6.3 Transiciones

| Situación tras un cambio | Estado | Efecto |
|---|---|---|
| la existencia sigue siendo de clase 1 o 2 con el mismo germen | `CURRENT` | el punto se actualiza |
| clase 4 sin intersección | `CONTACT_LOST` | punto indefinido con diagnóstico; no se reasocia |
| clase 4 con dos raíces, en una tangencia regular | `CONTACT_SPLIT` | punto indefinido; las dos raíces transversales reciben identidades nuevas; ninguna hereda el token |
| clase 3 | `CONTACT_NOT_CERTIFIED` | punto indefinido; la evidencia se muestra; no es `LOST` ni `SPLIT` |
| `h''` deja de estar certificado lejos del cero, o falta el aislamiento | `IDENTITY_DISCONTINUITY` | punto indefinido |
| se recupera la clase 1 o 2 desde el mismo germen y el mismo linaje | `CURRENT` | reaparece con la misma identidad |

**En un pliegue.** Si `t₀` es un pliegue certificado, la clase 4 con dos raíces da dos
preimágenes `t₀ ± s` de **un único punto geométrico**. En un pliegue, `CONTACT_SPLIT` no crea dos
puntos en la misma posición; la pareja de preimágenes, emparejada por el certificado de simetría,
tiene **una sola identidad**, y nunca se elige un lado en silencio (`AGENTS.md` §11.4;
decisión D-CU3-11).

### 6.4 Contacto en un extremo incluido

En un extremo incluido no hay cambio de signo de `h'` dentro del dominio. La existencia exige la
condición cerrada `h(t_end) = 0`, probada por las clases 1 o 2. Las condiciones abiertas
unilaterales dan el orden y el aislamiento. Si falta la prueba, la clase es 5.

## 7. Compatibilidad con Locus V2 y los certificados de `CU2`

- **Pertenencia.** La materialización de un contacto exige que su parámetro esté en el interior
  de una componente **continua** certificada.
- **Componentes puntuales.** Una componente puntual existe aunque ningún parámetro binary64
  alcance su parámetro exacto, y no crea ningún objeto de punto público
  (`locus-v2-existence-components.md` §15.4). `CU3` no materializa contactos en ellas ni introduce
  las capacidades de C22 y C23 (D-CU3-9). Un contacto cuyo corchete corta una componente puntual
  es de clase 5, con la causa `POINT_COMPONENT_NOT_MATERIALIZED`.
- **Completitud.** La admisibilidad local se mantiene independiente de la completitud global
  (ADR 0027 y G8B-R1).
- **Integridad frente a precisión** (principio 8 del registro E1). Un contacto de clase 1 o 2
  conserva su existencia aunque la posición numérica falle.
- **Salidas coincidentes.** Los certificados de `CU2` no se reabren, y las salidas coincidentes
  sin etiqueta siguen con su contrato (D-CU2-18).

## 8. Los tres casos obligatorios

Los tres contactos se reducen a una identidad del modelo, `J_1(K) = M_1` (análisis §5.3): un
teorema válido para cualquier valor de la frontera, que M3 debe decidir en `Σ_B`. El mismo teorema,
con el conductor en `J` y la generatriz `r`, hace del punto de la costura de `e_1·c_1` un contacto
estructural (H-CU3-2; D-CU3-10).

Por su orden de contacto:

- `e_1·n` y `e_1·c_1` (en `t = 0`), y `e_1·c_1` y `e_1·r` (en la costura), son contactos en un
  pliegue;
- `o_1·h_2` es una tangencia regular.

**Con conductor** (`Intersect(L, r, K)` y `Intersect(L, r, J)`), la Etapa A los materializa si M3
supera su puerta temprana (Etapa A §6.5).

**Sin conductor** (`Intersect(L, r)`):

- en el incremento mínimo, los cinco quedan en el resultado rico con su clase (3, salvo que se
  calcule la clase 2) y sin punto;
- con el incremento `CU3-B`, la vía 1 de §4.2 (pliegue certificado de `CU2` en `t₀ ∈ {0, π}`, más
  M3 en `K*` y `J*`) los eleva a la clase 2 y los materializa.

## 9. Hallazgos del motor vigente y regla de conflicto de evidencia

La caracterización encontró cuatro comportamientos preexistentes que contradicen §5.1 de la
especificación de intersecciones (análisis §4):

- **H-CU3-1:** raíces del ruido de redondeo junto a contactos, publicadas como transversales y a
  veces admisibles.
- **H-CU3-2:** contactos con cero doble clasificados como raíces transversales simples y
  admisibles.
- **H-CU3-3:** tangencias declaradas donde la ausencia de intersección está certificada.
- **H-CU3-4:** duplicados de una misma preimagen.

Esos resultados son **admisibles o publicables bajo la verificación local de G8B-R1**: valores
binary64, residuo normalizado y aislamiento local. La revisión focal pide no restringir esa
capacidad aprobada sin identificar el cambio normativo y sus consecuencias. El diseño revisado no
sustituye la aceptación de G8B-R1. Añade una **regla de conflicto**:

> Cuando, en la misma revisión, una evidencia certificada (§2a o §4.3) contradice una
> clasificación obtenida por verificación local, prevalece la evidencia certificada, y la
> solución contradicha se retira o se reclasifica. Sin contradicción certificada, la admisibilidad
> de G8B-R1 no cambia.

Las contradicciones posibles son:

- una clase 4 sin intersección en una caja que contiene una raíz verificada localmente;
- un punto crítico certificado (`h''` de signo fijo y `h'` con un solo cambio de signo) en una caja
  donde se publica una única raíz transversal, o más raíces de las que el certificado permite;
- la unicidad certificada de dos candidatos que se solapan, que son la misma preimagen.

El análisis certificado se dispara de forma determinista e independiente de la escala solo en
los candidatos que el resolvedor ya marca como no transversales o como próximos entre sí, de modo
que las raíces transversales genéricas no cambian. El mecanismo, la normativa (A3.1-R) y la
compatibilidad están en la
[normalización, admisibilidad y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) §3 y
§4, y la política de consumo, en su §4.

Qué está medido y qué es inferencia:

- **Medido en el sondeo.** La clase 3 de los contactos de H-CU3-2 (incompatible con una sola raíz
  transversal) y la clase 4 sin intersección de H-CU3-3.
- **Inferencia** de las anchuras de envolvente medidas: las raíces de H-CU3-1 caen dentro de la
  banda de indecisión de un punto crítico certificado, y los duplicados de H-CU3-4 tendrían
  corchetes que se solapan. La implementación debe confirmarlo (V-B-NOISE-1 y V-B-DUP-1).

## 10. Fuera de alcance

- El objeto público de punto crítico (opción B), que queda como opción futura.
- Contactos de orden mayor que dos (`h''(t₀) = 0`) y cúspides: clase 5 con su diagnóstico.
- Tangencias de pares de loci.
- La materialización en componentes puntuales (C22, C23) y el seguimiento de C40.
- La eliminación algebraica general de raíces múltiples (alternativa R-2 de la puerta de M3).
- Cualquier tolerancia configurable por el usuario (H-1, pospuesta).

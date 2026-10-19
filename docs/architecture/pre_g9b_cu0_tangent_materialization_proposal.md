# PRE-G9B-CU0 — Materialización de contactos tangentes y puntos estructurales (entregable F)

| Campo | Valor |
|---|---|
| Carácter | Propuesta técnica del paquete `CU0` para la decisión DA-3 (opción C por etapas). No es especificación normativa ni autoriza implementación |
| Estado | `CU0 DESIGN CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral de 2026-10-19, §3.3 |
| Autoridad vigente | `geocedg/specs/locus/locus-v2-intersections.md` (§6.2, §6.3, §8), `locus-v2-extended-intersections.md`, `locus-v2-public-surface.md` §10, `locus-v2-point-interaction.md`; ADR 0008, 0009, 0017, 0019 y 0026; `AGENTS.md` §11.5 |
| Evidencia | [línea base CU0](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json) (`preserve.contacts`, `pins.B08_B09`, `pins.H02`, `pins.H06`); [certificación híbrida](pre_g9b_cu0_hybrid_certification_proposal.md); [precisión y tolerancias](pre_g9b_cu0_precision_and_tolerance_study.md) |

## 1. Objetivo y casos obligatorios

El autor necesita materializar intersecciones tangentes geométricamente válidas, con
las dos incidencias establecidas (`P ∈ L` y `P ∈ r`), utilizables dinámicamente. No es
aceptable cerrar B08/B09 solo con una construcción alternativa que evite la
intersección. Casos obligatorios de diseño y validación:

```text
Intersect(e_1, n)      Intersect(e_1, c_1)      Intersect(o_1, h_2)
```

## 2. Hechos registrados en `CU0`

| Par | Punto de contacto | Clase actual | Admisible | `h(0)` | `h'(0)` | `h''(0)` |
|---|---|---|---|---:|---:|---:|
| `e_1` · `n` (segmento) | `(4,827722208; 7,673085543)` = `M_1` | `CONTACT_UNDETERMINED` | no | `4,2·10⁻¹⁶` | `0` | `−3,348` |
| `e_1` · `c_1` | `(4,827722208; 7,673085543)` = `M_1` | `CONTACT_UNDETERMINED` | no | `−1,8·10⁻¹⁵` | `0` | `−2,061` |
| `o_1` · `h_2` | `(4,827722208; −5,367469712)` = `N_1` | `TANGENT_ESTABLISHED` | no | `0` | `0` | `+2,569` |
| `e_1` · `c_1` (costura) | `(1,608439554; 10,130963488)` | `TRANSVERSE_ESTABLISHED` | **sí** | — | — | — |

`h` es el residuo con signo (distancia a la recta o diferencia radial a la
circunferencia) en el parámetro del conductor; las derivadas son diferencias centradas
con paso `10⁻³`. Que `h'(0)` sea exactamente cero no es casualidad: el modelo es simétrico
respecto del plano frontal que contiene el vértice, de modo que `h` es par en `t`.

Las tres tangencias son **estructurales**: para un cono de base horizontal, las
generatrices de contorno en alzado parten de los puntos de la base con tangente
perpendicular al plano de alzado, que son los extremos `J` y `K` de `p` en la dirección
de la línea de tierra, cualquiera que sea el vértice. En este modelo además la
proyección horizontal del vértice `I` está construida sobre la recta `i` que pasa por el
centro de la base (`I = Intersect(i, k)`), así que el plano frontal de la generatriz `VK`
contiene el centro de la esfera. Por eso, en la posición del conductor `E_1 = K`
(`t = 0`): `J_1` está sobre `n` (la generatriz de contorno), sobre `c_1` (contorno de la
esfera) y la tangente de `o_1` es paralela a la línea de referencia `h_2`.

Rechazo actual (estudio de `CU0`): la tangencia no se excluye con una sola comprobación.
Una raíz tangente nunca se marca localmente aislada, nunca recibe germen de raíz actual,
el selector R4 se define como «linaje de componente + germen transversal orientado», las
guardas de fusión y desdoblamiento la ignoran y la ruta de pares exige un jacobiano de
signo certificado. Además, la clasificación de contacto procede de una tangente por
diferencias finitas comparada con `10⁻¹⁰`, cuyo ruido puede ser del mismo orden: por eso
contactos equivalentes salen `TANGENT_ESTABLISHED` o `CONTACT_UNDETERMINED`.

La normativa no es uniforme: el informe G8B-R1 y `locus-v2-public-surface.md` §10 admiten
en principio un punto tangente con aislamiento e identidad establecidos, mientras que
`locus-v2-intersections.md` §6.2 y el ADR 0017 definen el selector sobre un germen
transversal y tratan la tangencia como barrera de identidad.

## 3. Fundamento matemático

Para un residuo escalar `h(t)` de una rama regular:

1. Una tangencia aislada regular satisface `h(t₀) = 0`, `h'(t₀) = 0`, `h''(t₀) ≠ 0`.
2. La existencia de un cero doble **exacto** no es una propiedad estable ante
   perturbaciones: ningún cálculo en coma flotante puede probar `h(t₀) = 0` en general.
   Sí puede probar, con intervalos:
   - que `h'` tiene **una única raíz simple** `t*` en un intervalo `I` (cambio de signo de
     `h'` y envolvente de `h''` que excluye el cero), es decir, un punto crítico aislado;
   - que `|h(t*)| ≤ ε` con `ε` la tolerancia de contacto declarada y normalizada a la
     escala (estudio H).
3. Con `h'' ≥ m > 0` en `I` se cumple `h(t) ≥ h(t*) + m (t − t*)²/2`. Por tanto, cerca de
   un punto crítico certificado solo caben tres situaciones exactas: ninguna raíz
   (`h(t*) > 0`), un contacto doble (`h(t*) = 0`) o dos raíces simples (`h(t*) < 0`). El
   punto crítico `t*` es en todos los casos un objeto bien definido y continuable: es una
   raíz **transversal** de `h'`.
4. Un contacto exacto solo se establece por **evidencia estructural**: la procedencia de
   la construcción garantiza las dos incidencias en una posición identificada del
   conductor.

Consecuencia: el estado honesto de un contacto numérico es «contacto dentro de `ε`,
aislado y de multiplicidad dos estimada», distinto de «contacto exacto probado». Ambos
pueden ser materializables si así se decide, pero deben distinguirse.

## 4. Etapa A — Puntos estructurales por correspondencia del conductor

### 4.1 Definición

Sea `L = LocusV2(T, D₀)` con conductor `D₀` sobre el soporte `c` y mapa canónico del
proveedor `D ↦ t(D)` (circunferencia: ángulo en el sistema propio de la cónica; segmento
o arco: parámetro de trayectoria). Para un punto `D` cuya pertenencia a `c` está
establecida por **procedencia** (punto sobre `c`, o salida de una intersección con `c`),
el punto estructural es

```text
P = F(t(D)),
```

exacto y dinámico, sin búsqueda de raíces. Hoy solo existe esta correspondencia dentro
del resolvedor métrico (ocurrencia de generador en el conductor vivo); no hay una forma
de punto. El parámetro canónico del evaluador **es** el parámetro de trayectoria del
conductor (`ReconstructibleLocusEvaluator2D.java:276-302`), así que la dirección sería
exacta.

### 4.2 Las dos incidencias

Cuando el punto estructural se usa para representar `P ∈ L ∩ r`:

1. `P ∈ L` se cumple por construcción.
2. `P ∈ r` se **verifica** con el adaptador de residuo del objetivo, con la tolerancia
   normalizada del estudio H, y se clasifica el contacto en ese parámetro:
   `TRANSVERSE` (cambio de signo certificable), `TANGENT_CONTACT` (punto crítico de `h` en
   `t(D)` dentro de tolerancia) o `NOT_INCIDENT` (residuo por encima de la tolerancia:
   el punto queda indefinido como intersección, con diagnóstico).
3. Si además la procedencia prueba la incidencia (por ejemplo, el punto se construye
   sobre `r` en el corte), el estado es `STRUCTURAL`.

### 4.3 Superficie pública candidata (decisión F-1)

| Forma | Significado |
|---|---|
| `Point(L, D)` | punto de `L` que corresponde a la posición `D` del conductor |
| `Intersect(L, r, D)` | intersección de `L` con `r` en la correspondencia `D`; definida solo si se verifica `P ∈ r` |

Para el modelo del autor, `K = Intersect(p, i, 2)` marca la posición del conductor de las
generatrices de contorno y es un punto de `p` por procedencia:

```text
Intersect(o_1, h_2, K)   → N_1, dinámico
Intersect(e_1, n, K)     → M_1, dinámico
Intersect(e_1, c_1, K)   → M_1, dinámico
```

Con `rbCone`, `α_cone` o `D_1` cambiados, `K` y los contactos se mueven con la
construcción. `Point(o_1, text2, v_1)` con `v_1 = 0` deja de ser necesario.

### 4.4 Identidad y ciclo de vida

- Identidad: (identidad del locus, identidad durable de `D`, objetivo). No hay token de
  raíz ni continuidad por proximidad.
- Si `t(D)` sale de las componentes certificadas del locus, o el objetivo deja de ser
  incidente, el punto queda indefinido con diagnóstico; reaparece cuando la condición
  vuelve a cumplirse, sin cambiar de identidad, porque su definición no ha cambiado.
- Deshacer, rehacer, guardar y reabrir reconstruyen el punto desde el DAG; no se
  persiste ningún parámetro.

## 5. Etapa B — Clase de contacto tangente en el motor de intersecciones

### 5.1 Niveles de evidencia de contacto

| Nivel | Evidencia | Materializable (propuesta) |
|---|---|---|
| `TANGENT_STRUCTURAL` | procedencia de la Etapa A | sí |
| `TANGENT_CERTIFIED_ISOLATED` | punto crítico único de `h` certificado en `I`, signo de `h''` certificado, `|h(t*)| ≤ ε` | sí, con estado «contacto dentro de `ε`» visible (decisión F-2) |
| `TANGENT_NUMERICAL` | clasificación actual por diferencias finitas | no; diagnóstico explicativo y oferta de la Etapa A |
| `CONTACT_UNDETERMINED` | sin evidencia suficiente | no |

### 5.2 Capacidades nuevas necesarias

1. Envolventes de segundo orden de `h` (chorros de segundo orden en la clase v3 del
   programa de construcción; derivadas polinómicas exactas para SplineV2).
2. Certificación de la raíz simple de `h'` (Newton por intervalos o Krawczyk escalar,
   que ya existen para la primera derivada).
3. Un **germen tangente** con orientación canónica: lado del contacto (signo de `h''`
   respecto de la orientación canónica del objetivo) y continuación del punto crítico.
   Requiere una versión nueva del selector y del linaje del libro de tokens
   (`.../tangent-contact-germ/v1`), sin reinterpretar los tokens transversales.
4. Reglas de vigencia y ciclo de vida (§5.3).
5. Clasificación de contacto basada en el punto crítico, no en el umbral `10⁻¹⁰`
   sobre una tangente aproximada.

### 5.3 Desaparición, desdoblamiento y continuidad

En cada revisión se recalcula `t*` por continuación de la raíz simple de `h'`:

| Situación tras un cambio de parámetros | Estado | Efecto |
|---|---|---|
| `|h(t*)| ≤ ε` | `CURRENT` | el punto se actualiza |
| `h(t*)` del lado «sin raíz» más allá de `ε` | `CONTACT_LOST` | punto indefinido; no se reasocia |
| `h(t*)` del lado «dos raíces» más allá de `ε` | `CONTACT_SPLIT` | punto indefinido; las dos raíces transversales nuevas reciben identidades nuevas; ninguna hereda el token tangente |
| `h''` deja de estar certificado lejos de cero | `IDENTITY_DISCONTINUITY` | punto indefinido |
| vuelve a cumplirse la primera fila desde el mismo germen | `CURRENT` | el punto reaparece con la misma identidad |

Nunca se elige en silencio otra raíz al desaparecer o desdoblarse una tangencia.

### 5.4 Tangencia en un extremo incluido del dominio

En un extremo incluido no hay cambio de signo de `h'` dentro del dominio. Se propone
admitirla solo con evidencia unilateral: `h(t_end)` dentro de `ε`, envolvente de `h'`
en el lado interior con el signo que corresponde a un contacto, y `h''` certificado; si
no, `CONTACT_UNDETERMINED`. La Etapa A cubre los extremos estructurales.

## 6. Qué se puede hacer con la arquitectura actual y qué es nuevo

| Pieza | Arquitectura actual | Nuevo |
|---|---|---|
| Punto estructural `Point(L, D)` | mapa `t(D)` del proveedor y evaluador exacto ya existen | familia de punto semántico, procedencia de pertenencia de `D`, mensajes |
| Verificación de `P ∈ r` | adaptadores de residuo existentes | tolerancia normalizada (H02) y clasificación por punto crítico |
| Clase tangente certificada | — | segundo orden, germen tangente, linaje y vigencia nuevos, enmiendas de `locus-v2-intersections.md` §6.2/§6.3 y del ADR 0017 |
| Ejes como objetivo (H06) | adaptador de recta | admitir `GeoAxis` como recta, o mensaje explícito |

## 7. Validación propuesta para `CU3`

1. Los tres casos obligatorios: puntos iguales a `preserve.contacts` (tolerancia de
   punto), estado `TANGENT_STRUCTURAL` con la Etapa A y, si se aprueba la Etapa B,
   `TANGENT_CERTIFIED_ISOLATED` en `Intersect(L, r)`.
2. Dinámica: variar `rbCone`, `α_cone`, `D_1` y `excCyl`; los contactos siguen la
   construcción y conservan identidad.
3. Casi-tangencia sintética con dos raíces cercanas y fallo de contacto: estados
   `CONTACT_SPLIT` y `CONTACT_LOST` sin reasociación.
4. La raíz transversal de la costura sigue materializable (fijación de la línea base).
5. Barrido de escala de H02 y ejes de H06.
6. Persistencia, deshacer, rehacer y copia.

Las decisiones F-1 (superficie pública de la Etapa A) y F-2 (materializabilidad del nivel
`TANGENT_CERTIFIED_ISOLATED`) están en la
[hoja de decisiones](../validation/pre_g9b_cu0_author_decision_sheet.md).

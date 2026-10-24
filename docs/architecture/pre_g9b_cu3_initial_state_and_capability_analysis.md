# PRE-G9B-CU3 — Estado inicial reproducible y análisis de capacidades

| Campo | Valor |
|---|---|
| Carácter | Análisis candidato de la preparación de `CU3` (entregables 1 y 2 de la instrucción). Evidencia y análisis, no autoridad normativa |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24) y su precisión «Decisión sobre la planificación de CU3» (2026-10-24) sobre la correspondencia entre la procedencia y el parámetro del conductor |
| Base | `main` = `e6c908f2203cd8feb5d4938013e16b212e0fe016`, árbol `fdb0e7027e19ee2b8fc9717f54d545e6ea50f6e1` (cierre de `CU2` publicado) |
| Evidencia reproducible | [`pre-g9b-cu3-preparation-characterization.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json): salida del sondeo [`run-cu3-probe.ps1`](../../tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1) sobre el commit `ec2b4dfa0bd77dad0686897cd559ea5003675165`, árbol `a8c32e657d6a5de76d9b42396108362b3b898240`. Dos ejecuciones con el árbol limpio dieron una salida idéntica byte a byte (SHA-256 `20af5cf471fc54622d63dec6ba69d9fe675587a48023a1faff654fd702e7d70f`) |
| Diseños | [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md) |

## 1. Método

La caracterización usa el sondeo `PreG9BCu3PreparationProbe`, que no es producto. Lo ejecuta
`tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1` con el init script de `CU0` sin cambios, y su
lanzador retira del árbol de compilación las clases del sondeo después de cada ejecución.

- Trabaja sobre copias del modelo del autor sin `e_2` (`PreG9BCu0Corpus.withoutE2`) y sobre
  construcciones sintéticas.
- No modifica código de producto, pruebas, el registro ni el inventario.
- Las envolventes componen la envolvente de segundo orden del punto trazado de clase v3
  (`ConstructionBoxModel2D.encloseTraced`, `locus-v2-certified-construction-model.md` §10.7) con
  el residuo del objetivo, en aritmética de intervalos binary64 con redondeo hacia fuera. Para
  llegar a ese método de visibilidad de paquete, el sondeo usa una clase de acceso propia
  (`PreG9BCu3EnclosureProbeAccess`) en el mismo paquete, que solo existe durante la ejecución.
- Los coeficientes binary64 del objetivo se toman como constantes exactas.
- Ninguna clasificación del sondeo es comportamiento del producto. La taxonomía que aplica es la
  candidata de la [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §3.

Escenarios: P1 a P10, según el plan de la preparación. P1 es además un análisis estático de las
definiciones del modelo (§2).

El sondeo se ajustó en cuatro commits de herramienta, `9e3a4735`, `7e483d0b`, `aa5f2ef0` y
`ec2b4dfa`:

- la normalización de identificadores de sesión, porque la primera pareja de ejecuciones
  difería solo en un `geo:<id>` aleatorio de un mensaje de excepción;
- el lector actual (P10);
- los chorros de la fuente (§5.4b).

Solo la salida del último commit se versiona.

## 2. P1 — Procedencia y términos del modelo del autor

Definiciones relevantes, leídas de `geogebra.xml` en una copia del modelo:

```text
g   = OrthogonalLine[A, xAxis]      h   = OrthogonalLine[A, g]      B   = Point[g]
i   = Line[B, h]                     j   = Line[A, C']               H   = Point[j]
k   = Line[H, g]                     I   = Intersect[i, k]           p   = Circle[B, rbCone]
J, K = Intersect[p, i]               l   = Line[J, g]                m   = Line[K, g]
L   = Intersect[l, h]                M   = Intersect[h, m]           n   = Segment[H, M]
r   = Segment[H, L]                  c_1 = Circle[H, D_1]            E_1 = Point[p]
n_1 = Ray[I, E_1]                    p_1 = Line[E_1, k]              F_1 = Intersect[h, p_1]
d_1 = Circle[I, E_1]                 H_1 = Intersect[d_1, i, 1]      r_1 = Line[H_1, k]
I_1 = Intersect[r_1, h]              q_1 = Ray[H, F_1]               s_1 = Ray[H, I_1]
G_1 = Intersect[c_1, s_1, 1]         t_1 = OrthogonalLine[G_1, k]    J_1 = Intersect[q_1, t_1]
b_1 = Line[J_1, k]                   K_1 = Intersect[b_1, n_1]       e_1 = LocusV2[J_1, E_1]
o_1 = LocusV2[K_1, E_1]              M_1 = Intersect[c_1, n]         h_2 = Line[M_1, k]
```

Lectura geométrica:

- `p` es la base del cono en planta, `H` el vértice en alzado e `I` el vértice en planta.
- `c_1` es el contorno en alzado de la esfera de centro el vértice y radio `|HD_1|`.
- `e_1` y `o_1` son el alzado y la planta de la curva del cono situada a distancia `|HD_1|` del
  vértice.
- `K` y `J` son los extremos del diámetro de `p` sobre `i`. Sus generatrices son frontales y sus
  alzados son las generatrices de contorno `n` y `r`.

**Conclusión de P1.** Ninguna de las tres incidencias pedidas es una **identidad sintáctica de
términos** tras la sustitución del conductor:

- `p_1(E_1 := K) = Line[K, k]` no es el término `m = Line[K, g]`, porque usa `k` en lugar de `g`,
  aunque las dos rectas sean paralelas por construcción.
- `b_1 = Line[J_1, k]` y `h_2 = Line[M_1, k]` comparten solo el segundo argumento.

Las tres incidencias son consecuencias de un **teorema geométrico del modelo** (§5.3).

## 3. Capacidades existentes y límites

### 3.1 Motor de intersecciones de Locus V2

Paquete `org.geocedg.common.kernel.locus.intersection` del núcleo compartido.

| Capacidad | Estado actual | Referencia |
|---|---|---|
| Resolvedor | elige una capacidad, verifica los candidatos y publica un resultado rico | `LocusIntersectionSolver2D:126-133`, `:239-351` |
| Capacidades | adaptativa (malla de 256 muestras, bisección por cambio de signo y búsqueda ternaria de mínimos), polinómica (grado ≤ 96), solo evaluador (`CONTACT_UNDETERMINED`) | `ExtendedTargetIntersectionCapability2D`, `PolynomialTargetIntersectionCapability2D`, `EvaluatorOnlyIntersectionCapability2D:260` |
| Clase de contacto | `|n·t| ≤ 1e-10` sobre una tangente por diferencias finitas, con paso `max(16·1e-12, 32·ulp(t), 1e-6·span)`; derivada exacta solo en fuentes polinómicas por tramos | `LocusIntersectionPolicy2D:21`; `IntersectionCapabilityContext2D:146-180` |
| Materialización | solo de las raíces con `TRANSVERSE_ESTABLISHED` y aislamiento `ESTABLISHED`; una raíz tangente es solución verificada del resultado rico, pero nunca recibe germen ni elegibilidad R4 | `PublicTargetIntersectionCapability2D:145-177`; `PublicIntersectionRootIdentityResolver2D:985-1017`; `ExtendedTargetIntersectionCapability2D:410-425` |
| Residuo | `2e-12 + 2e-12·max(1, S)`. `S = 1` en rectas, segmentos, rayos, gráficas e implícitas; `max(1, r)` en circunferencias; `max(1, semiejes)` en cónicas; `max(1, ‖P₁‖, ‖P₂‖)`, la **distancia al origen**, en pares | `LocusIntersectionPolicy2D:18-24`, `:286-294`; `LocusIntersectionTargets2D:284-285`, `:404-405`; `LocusPairIntersectionSolver2D:593-596` |
| Corchete y deduplicación | parada de la bisección por un corchete absoluto `1e-12` en el parámetro; deduplicación absoluta `4e-12` | `ExtendedTargetIntersectionCapability2D:305`, `:428-471`; `LocusIntersectionSolver2D:353-406` |
| Diagnósticos de rechazo | `RESIDUAL_REJECTED`, `PAIR_RESIDUAL_REJECTED`, `MEMBERSHIP_REJECTED` y `NONFINITE_EVALUATION` se declaran, pero nunca se emiten; un rechazo solo incrementa un contador | `IntersectionSemanticMetadata2D:155,161`; `LocusIntersectionSolver2D:275` |
| Identidad | selector R4 «linaje de componente + germen transversal orientado»; germen `g9u0-r4/current-transverse-root-germ/v1/`; libro de tokens con formatos de estado 1 a 5; persistencia `<locusIntersectionTokenLedger>` | `IntersectionRootDeterministicSelector2D:13-34`; `LocusIntersectionTokenLedger2D:35-45`; `GeoLocusIntersectionResult:246-253`, `:494` |
| Fusiones y desdoblamientos | fallo cerrado (`AMBIGUOUS_CONTINUATION` con `SPLIT_CANDIDATE` o `MERGE_CANDIDATE`) | `PublicIntersectionRootIdentityResolver2D:313-434` |

### 3.2 Envolventes, certificación exacta y Krawczyk

- **Intervalos.** `SplineOutwardInterval2D`, con redondeo hacia fuera de una unidad binary64.
- **Krawczyk escalar y 2D.** Solo para SplineV2 (`SplineImplicitIntervalCertification2D`,
  `SplinePairIntervalCertification2D`).
- **Clase v3** (`ConstructionBoxModel2D`, capturada con `captureCertifiedProgramV3`). Envuelve
  el punto trazado `C`, `C′` y `C″` (`encloseTraced`, `:366-380`) y los predicados de validez
  hasta el segundo orden (`enclose`, `:345-358`). El modelo certificado §10.7 limita el segundo
  orden al aislamiento de puntos estacionarios.
- **Certificación exacta de `CU2`.** Paquete `locus/exact` (Sturm, descomposición libre de
  cuadrados, `CircleDriverRootStructure2D`) y `FirstLevelPredicate2D`, para predicados de primer
  nivel sobre constantes binary64 capturadas como racionales exactos (D-CU2-16).
- **Hueco.** Ningún código compone un objetivo con `C(t)` para envolver `h`, `h′` o `h″`
  (§3.4). No hay aritmética exacta para expresiones con raíces cuadradas anidadas.

### 3.3 Correspondencia del conductor y procedencia

- **Conductores admitidos.** `LocusV2(Q, P)` admite un punto sobre un camino (`isPointOnPath`)
  sobre segmento, arco circular o circunferencia, o un punto semántico de rama
  (`LocusV2PublicOperations.PointDriver.resolve`, `:905-942`). Una salida de intersección
  como `K` **no** es conductor admisible.
- **Parámetro canónico.** En las familias de punto es el `PathParameter.t` del conductor, tal
  como lo fija la evaluación (`ReconstructibleLocusEvaluator2D.applyCoordinate`, `:763-789`).
  Para las familias escalar y de rama es el valor numérico.
- **Inversa `D ↦ t(D)`.** No existe en GeoCeDG: `StablePathDomainProvider2D:106-125` solo
  calcula t → punto. Las fórmulas de GeoGebra existen para la proyección de un punto sobre un
  camino:
  - circunferencia, el ángulo en el sistema propio de la cónica (`GeoConicND:450-460`);
  - arco, `(ángulo − inicio)/extensión` (`GeoConicPartParameters:254-300`);
  - segmento, la proyección recortada a `[0, 1]` (`GeoSegment:370-397`).
- **Pertenencia por procedencia.** GeoGebra registra para un punto de intersección una lista de
  incidencias del prover (`GeoPoint.getIncidenceList`, `:2370`), pero no un camino. El único
  control estructural de pertenencia en GeoCeDG es el de un conductor sobre su soporte
  (`CertifiedConstructionCapture2D.requirePathState`, `:324-335`). La única correspondencia viva
  entre un punto y el parámetro es la del resolvedor métrico, y solo para el punto trazado
  (`SemanticMetricEndpointResolver2D:250-302`).
- **Puntos sobre Locus V2.** `Point(L, u)` y `Point(L, "rama", u)` (`AlgoSemanticLocusPoint2D`)
  existen. `Point(L, D)` con `D` punto no existe (§5.5).

### 3.4 Componentes certificadas de `CU2`

`LocusExistenceStructure2D`:

- `getComponentContaining(t)` nunca devuelve una componente puntual;
- `getPointComponentEnclosing(t)`;
- `getCompleteness()`;
- `ContinuousComponent.contains(t)`, exacta;
- `isClosedPeriodic()` como autoridad única de cierre (ADR 0038).

Las consultas de procedencia fallan cerradas: `IntersectionEndpointProvenanceResolver2D.addressFromRawParameter`,
`:267-306`, y `LocusComponentSelectorResolver2D.resolve`. Son la base de la vigencia de un punto
estructural (Etapa A §5).

### 3.5 Límites de `CU2` que afectan a `CU3`

- Las componentes puntuales se declaran pero no se dibujan ni se materializan (D-CU2-19).
- F-CU2-1: el evaluador heredado sobrepasa el extremo exacto (ventana de tangencia), y nunca se
  publica como existencia.
- El paquete final de `CU2` §8 llama «trabajo de `CU3`» a C22, C23 y C40: materializar raíces en
  componentes puntuales y el seguimiento de raíces transversales. El registro de cierre §6 dice
  que el trabajo futuro «no son tareas de `CU3`». Esa tensión documental es la decisión D-CU3-9.
- `L-CU2-CLOSED-PERIODIC-SEAM`: se conserva la pertenencia heredada cerca de la costura (§5.4).

## 4. Hallazgos del motor vigente

H-CU3-1 a H-CU3-6 son comportamientos preexistentes en la base `e6c908f2`; H-CU3-7 es una precisión científica. La preparación no corrige nada.
Los tres primeros contradicen un contrato aprobado: `locus-v2-intersections.md` §5.1 dice que
«neither tangency nor multiplicity follows from a small residual alone» y que la incertidumbre no
debe convertirse en un falso transversal.

| Id | Hallazgo | Evidencia | Contrato |
|---|---|---|---|
| **H-CU3-1** | **Raíces espurias del ruido de redondeo** junto a una tangencia, publicadas como `TRANSVERSE_ESTABLISHED` y **algunas admisibles** (se puede crear un punto): un `h` redondeado con un valor de `±1e-15` en su extremo da dos raíces de la función binary64 a `√(2|h|/|h″|) ≈ 2–5·10⁻⁸` del contacto | modelo, P4: `rbCone` 3, 4, 6 y 7, `α` 35° (siete raíces para `e_1·c_1`, dos admisibles) y `D_1` ×0,5 (cuatro raíces para `o_1·h_2`, dos admisibles); sintéticos, P6: δ = −1e-13 y −1e-10 dan cuatro y tres raíces, con duplicados, frente a las dos que el sondeo certifica | viola §5.1 |
| **H-CU3-2** | **Contactos con un cero doble del residuo** (el pliegue de `e_1` en la costura, §5.4b, y una tangencia en un extremo incluido) **clasificados como raíces transversales simples y admisibles** | `e_1·c_1` y `e_1·r` en `t = ±π`: el sondeo certifica `h″ < 0` en `[π − 1e-2, π + 1e-2]`, el cambio de signo de `h′`, los signos de `h` en los extremos de la caja y que `h(t*)` contiene el cero. Eso deja tres posibilidades en la caja: ninguna raíz, una doble o dos simples. No contradice por sí solo la raíz publicada (comprobación final, Etapa B §9); el error de clasificación lo establece el teorema estructural (§5.3), que sitúa en `π` un cero doble; cuarto de arco con `x = 1` en su inicio: `h′(0) ≈ 0` y `h″ > 0`, y el motor publica `TRANSVERSE_ESTABLISHED`, `INCLUDED_ENDPOINT`, admisible | viola §5.1 |
| **H-CU3-3** | **Tangencias declaradas donde la ausencia de intersección está certificada** | δ = +1e-13 (recta y circunferencia): el sondeo certifica la clase 4 sin intersección (`h(t*)` ∈ `[−1,010e-13; −0,993e-13]`) y el motor publica `TANGENT_ESTABLISHED`, no admisible, porque el residuo `1e-13` es menor que la tolerancia `4e-12` | viola §5.1 y `AGENTS.md` §1.8 |
| **H-CU3-4** | **Duplicados de una misma preimagen** | traslación 10⁶, radios 2 y 20: cuatro soluciones transversales para dos raíces; δ = −1e-10: raíces a `4,2·10⁻¹²` y `4,8·10⁻¹²` en el parámetro | §8.2 (la deduplicación no debe fusionar preimágenes distintas, pero sí las repetidas) |
| **H-CU3-5** | **H02 confirmado**, también con traslaciones | `r` = 2, 20, 200, 2000 y 20 000 con traslaciones 0, 10³ y 10⁶: las raíces admisibles pasan de 2/2 a 0/0 según la escala y, a la misma escala, según la traslación; el sondeo certifica las dos raíces por cambio de signo en todos los casos, con residuos normalizados por `3r` entre 0 y `7,6·10⁻¹⁷` | §8.1 (escala invariante por traslación) |
| **H-CU3-6** | **Causa de H06** | `Intersect(L, xAxis)` falla con `CAS.GeneralErrorMessage`; la llamada directa a `LocusV2PublicOperations.intersect` lanza `SpatialIdentityException: GEO_NOT_SERIALIZABLE` («Participating geo has no stable ordinary-element attachment») desde `SpatialIdentityRegistry.validateAttachmentGeo`, al publicar las participaciones. La tabla de objetivos sí admite el eje como recta | identidad durable (ADR 0026, G9U0); no es un problema de residuo |
| **H-CU3-7** | **Los contactos de `e_1` son contactos en pliegues, no tangencias** (precisión científica, no un comportamiento del motor) | §5.4b: `F′ = 0` en `t = 0` y `t = π`; el arco llega a `n`, `r` y `c_1` a 58,4° y 31,6°; solo `o_1·h_2` es una tangencia | `AGENTS.md` §11.5 (rama regular) |

## 5. Los tres casos obligatorios

### 5.1 Los cuatro hechos de la correspondencia (P1, P2)

| Hecho | Resultado en el modelo |
|---|---|
| 1. Pertenencia de `K` a sus objetos de origen | `K` es la salida 2 (de `J, K`) de `AlgoIntersectLineConic` con entradas `p` e `i`. Su lista de incidencias es `i`, `p` y `m`. `K` **no** es un punto sobre un camino (`isPointOnPath = false`). La pertenencia de `K` a `p` es de la configuración ideal de la salida de un algoritmo heredado, con su redondeo y su estado de selección |
| 2. Pertenencia al soporte concreto del conductor | `E_1 = Point[p]` es un punto sobre el camino `p`. El camino de `E_1` **es el mismo objeto** `p` (identidad de referencia), con el mismo identificador persistente `geo:848974e6549ef5f2754a7efcb19c0068`. `p` está entre las entradas del algoritmo de `K` |
| 3. Correspondencia `K ↦ t(K)` | circunferencia con vectores propios `(1, 0)` y `(0, 1)`; `K.y − B.y = 0` exactamente. La proyección canónica de GeoGebra (vía a de la Etapa A) y un cálculo independiente del ángulo en el sistema propio dan `t(K) = 0` **exactamente**. El conductor colocado en `t = 0` reproduce `K` bit a bit. Por la costura, `t(J) = π` (canónicamente `−π`); el conductor en `π` no reproduce `J` bit a bit, porque `cos π` y `sin π` se redondean, y la diferencia es `6,4·10⁻¹⁵` en `J_1` |
| 4. `F(t(K))` y segunda incidencia | `t(K) = 0` está en una componente continua certificada de `e_1` y de `o_1`, con `Completeness = COMPLETE`. En `E_1 = K`: `J_1 = M_1` y `J_1.x = M_1.x` bit a bit, y `K_1` está a distancia `0` de `h_2`. El residuo de `J_1` respecto de `n` es `4,17·10⁻¹⁶` y respecto de `c_1` es `−1,78·10⁻¹⁵`: es el residuo del propio `M_1` heredado, que no está exactamente sobre `n` ni sobre `c_1` en binary64 |

### 5.2 Rama elegida por el documento

`H_1 = Intersect[d_1, i, 1]` elige en el documento la rama **reflejada**: `H_1 = 2I − K = (9,209…; −5,367…)`,
no `K`. Por eso `I_1 ≠ M` y `G_1 ≠ M_1` (`|G_1 − M_1| = 3,6155`): `G_1` es el simétrico de `M_1`
respecto de `k`. La igualdad `J_1 = M_1` no procede de una cadena de puntos iguales, sino de la
**reflexión respecto de `k`** y de la paralela horizontal `t_1`.

### 5.3 Teorema del modelo

**Proposición.** Sea `E_1 = K`. Supongamos las condiciones de no degeneración `|IK| > 0`,
`M_y ≠ H_y` y `|HD_1| ≤ |HM|`. Entonces `J_1 = M_1`, en las dos ramas de `H_1`. En consecuencia:

- `e_1(t(K)) ∈ n ∩ c_1`;
- `o_1(t(K)) = K_1 ∈ h_2`.

*Demostración.*

1. `k = Line[H, g]` es paralela a `g`, así que `p_1 = Line[K, k]` coincide con `m = Line[K, g]`.
   Por tanto `F_1 = h ∩ m = M` y `q_1 = Ray[H, M] ⊇ n`.
2. `d_1` tiene centro `I ∈ i` y pasa por `K ∈ i`, así que `d_1 ∩ i = {K, 2I − K}`.
3. Si `H_1 = K`, entonces `I_1 = M`, `s_1 = q_1` y `G_1 = c_1 ∩ q_1`.
4. Si `H_1 = 2I − K`: la reflexión `σ` respecto de `k` fija `H` e `I` (`I ∈ k`), conserva `h`
   (perpendicular a `k`) y lleva `K` a `2I − K`. Por tanto `I_1 = σ(M)` y `s_1 = σ(q_1)`. Como
   `c_1` tiene centro `H ∈ k`, es invariante por `σ`, así que `G_1 = σ(c_1 ∩ q_1)`.
5. En los dos casos, `G_1` tiene la ordenada de `c_1 ∩ q_1`, porque `σ` conserva las ordenadas.
6. `t_1` es la horizontal que pasa por `G_1`. Entonces `J_1 = q_1 ∩ t_1` es el punto del rayo `HM`
   con esa ordenada: es `c_1 ∩ q_1`, y el rayo no es horizontal porque `M_y ≠ H_y`.
7. Con `|HD_1| ≤ |HM|`, `c_1 ∩ q_1 = c_1 ∩ n = M_1`. Por tanto `J_1 = M_1`.
8. Finalmente, `b_1 = Line[J_1, k] = Line[M_1, k] = h_2`, y `K_1 ∈ b_1 = h_2`. ∎

Las mismas razones, con `E_1 = J` y la generatriz `r = Segment[H, L]`, dan
`e_1(t(J)) ∈ r ∩ c_1`: la raíz de la costura es el **mismo contacto en la otra generatriz frontal**.

Las condiciones de no degeneración `|IK| > 0` y `M_y ≠ H_y` son **abiertas**, certificables
por intervalos. `|HD_1| ≤ |HM|` es cerrada solo en el caso límite `|HD_1| = |HM|`, que no es el
del modelo.

**Evidencia numérica (P3).** Con las entradas capturadas tomadas como exactas (`H`, `B`, la altura
binary64 de `h`, `rbCone` y `D_1`) y 80 cifras significativas, `|J_1 − M_1|` vale `0E-79` en las
dos ramas. Esto comprueba la deducción; no la sustituye.

### 5.4 Clasificación por intervalos (P2, P5)

En los tres contactos de `t = 0`, la envolvente de `h″` excluye el cero en las cajas de
semiancho `1e-2`, `1e-4` y `1e-6`:

- `e_1·n`: `h″ ∈ [−3,40; −3,29]`;
- `e_1·c_1`: `h″ ∈ [−2,18; −1,94]`;
- `o_1·h_2`: `h″ ∈ [2,51; 2,63]`.

`h′` cambia de signo de forma certificada, así que el punto crítico `t*` es único y queda
encerrado en un corchete de anchura `2·10⁻¹⁴`. La envolvente de `h(t*)` **contiene el cero**:

- `e_1·n`: `[−7,5·10⁻¹⁴; 7,4·10⁻¹⁴]`;
- `e_1·c_1`: `[−9,7·10⁻¹⁴; 9,3·10⁻¹⁴]`;
- `o_1·h_2`: `[−6,3·10⁻¹⁴; 6,6·10⁻¹⁴]`.

Los tres son de **clase 3**: un contacto numéricamente compatible, que no es una intersección
probada. Con la evidencia de la F-2 original, los tres habrían sido «tangencias certificadas».
No lo son: son contactos **estructurales** por el teorema de §5.3. Además, dos de ellos no son
tangencias, sino contactos en un pliegue (§5.4b).

En la costura (`t = π`, caja periódica), `e_1·c_1` y `e_1·r` son también de clase 3:

- `e_1·c_1`: `h″ ∈ [−0,73; −0,60]` y `h(t*) ∈ [−2,7·10⁻¹³; 2,6·10⁻¹³]`;
- `e_1·r`: `h″ ∈ [1,07; 1,10]` y `h(t*) ∈ [−2,4·10⁻¹³; 2,4·10⁻¹³]`.

Por H-CU3-2, la «raíz transversal de la costura» que fijan la línea base de `CU0` y el criterio
de aceptación del plan («la raíz transversal de la costura se conserva») es en realidad un
contacto estructural en el pliegue de `t = π` (§5.4b), con un cero doble de `h` en el parámetro.
**El punto se conserva**, porque es geométricamente correcto. Lo que es un defecto es su
clasificación como raíz transversal simple (decisión D-CU3-10).

### 5.4b Orden de contacto: pliegue del trazado frente a tangencia (P2)

Las envolventes puntuales de `F′` y `F″` en los parámetros del contacto distinguen dos fenómenos
que la propuesta F de `CU0` llamaba igual, «tangencias»:

| Locus | `t` | `F′` (envolvente) | Dirección límite | Ángulo con el objetivo | Naturaleza |
|---|---|---|---|---|---|
| `e_1` | `0` | contiene el vector cero (`|F′| ≤ 2,7·10⁻¹⁵`) | `F″(0) ≈ (−2,569; 2,976)`, `|F″| ≈ 3,93` | `n`: 58,4°; `c_1`: 31,6° | **contacto en un pliegue**: el arco `e_1` llega a `M_1` y se recorre de vuelta |
| `e_1` | `π` | contiene el vector cero | `F″(π) ≈ (1,149; −0,549)` | `r`: 58,4°; `c_1`: 31,6° | contacto en un pliegue (punto de la costura) |
| `o_1` | `0` | `(0; 3,512)` | `F′(0)` | `h_2`: 0° | **tangencia regular** de orden dos |
| `o_1` | `π` | `(0; −1,999)` | `F′(π)` | recta vertical por el punto de la costura: 0° | tangencia regular |

**Interpretación.** La curva espacial (cono ∩ esfera) es simétrica respecto del plano frontal que
contiene el vértice, el eje y el centro de la esfera. La corta en dos puntos, con tangente
perpendicular a ese plano, es decir, paralela a la dirección de proyección del alzado. Por eso
el alzado `e_1` tiene `F′ = 0`: es el pliegue que `CU2` certificó por simetría exacta en `t = 0` y
en `t = π` (SM-C-6; matriz de `CU2` §1). El conjunto de puntos `e_1` es un arco con extremos en
`M_1` y en el punto de la costura, y llega a `n`, `r` y `c_1` con ángulos no nulos.

El cero doble de `h` en `e_1·n` y en `e_1·c_1` procede de `F′(0) = 0`, no de una tangencia. La
planta `o_1` no se pliega, y su tangente en `t = 0` es paralela a la línea de referencia `h_2`:
ese sí es un contacto tangente.

**Consecuencias:**

- **Hallazgo científico H-CU3-7.** Los contactos B08 y B09 de `e_1` son contactos en un pliegue
  del trazado, no tangencias. La multiplicidad dos en el parámetro no es un orden de contacto
  geométrico cuando la fuente es singular (`F′ = 0`). La precisión «rama regular» de
  `AGENTS.md` §11.5 es exactamente esa condición.
- En un pliegue certificado por simetría exacta (`F(t₀ + s) = F(t₀ − s)`), un objetivo
  ligeramente desplazado hacia el interior corta el arco en **un** punto con **dos** preimágenes
  `t₀ ± s`, no en dos puntos. Un «desdoblamiento» en el parámetro no crea dos intersecciones
  geométricas (`AGENTS.md` §11.4, puntos con varias preimágenes). La Etapa B §4.4 y §6.3 lo
  tratan.
- El cero de `F′` lo contiene la envolvente, pero eso no lo certifica (es una condición cerrada).
  La autoridad del pliegue es el certificado de simetría exacta de `CU2`.

### 5.5 Dinámica, desaparición y reaparición (P4)

En todos los barridos (`rbCone` de 3 a 7,5; `α_cone` 35°, 47° y 60°; `D_1` con radios ×0,5, ×1 y
×1,3; `excCyl` 2,9 y 3,7):

- `t(K) = 0` exactamente;
- `t(K)` está en una componente certificada `COMPLETE`;
- `|J_1(K) − M_1| ≤ 4,0·10⁻¹⁵`;
- los tres contactos del sondeo son de clase 3.

El motor vigente publica entre una y siete raíces según el caso, con raíces espurias admisibles
(H-CU3-1).

Con `D_1` ×3 (`|HD_1| > |HM|`), `M_1` deja de estar definido y `Intersect(e_1, n)` y
`Intersect(o_1, h_2)` no tienen solución, porque la esfera ya no alcanza el segmento `n`. Es el
caso de **desaparición** del contacto con un objetivo limitado. El contacto `e_1·c_1` en `t = 0`
persiste, porque `J_1(K)` sigue en `c_1`. Al restaurar `D_1`, todo vuelve.

### 5.6 Independencia de la vista (P9) y lector actual (P10)

- Un cambio de sistema de coordenadas de la vista (factor 37,5 y origen desplazado) no altera ni
  la envolvente ni la clasificación (salidas idénticas).
- El producto actual rechaza con un error de argumento `Point(e_1, K)`, `Intersect(e_1, n, K)`,
  `Intersect(o_1, h_2, K)` e `Intersect(e_1, c_1, K)`. No reinterpreta en silencio la forma
  heredada `Intersect(<Object>, <Object>, <Initial Point>)`. Un documento de `CU3` abierto con un
  producto anterior falla cerrado; que la carga del documento completo falle no se ha comprobado
  (Etapa A §8).

### 5.7 Configuración de la evaluación exacta (revisión focal, 2026-10-24)

**Comprobación racional exacta.** Se hizo con los valores binary64 de `B.x` y `K.x` que registra
la evidencia, convertidos a racionales exactos con `System.Numerics.BigInteger`:

- `K.x = 4,0619549279530975` es `fl(B.x + 5)`;
- difiere del racional exacto `B.x + 5`, con `B.x = −0,9380450720469029`, en `4,44·10⁻¹⁶`.

**Consecuencia.** El estado binary64 de `K`, y con él los de `m`, `M` y `n`, no conserva
exactamente la relación «`n` pasa por la vertical del punto exacto de contacto». Una
configuración que tomara como constantes los estados binary64 de esos objetos intermedios no
contendría la incidencia del teorema de §5.3. Por eso M3 recalcula exactamente, en la
configuración de decisión `Σ_L⁺`, los ancestros de `D` y de `r` que no son constantes de `Σ_L`,
entre ellos `K`, `m`, `M` y `n`, y conserva sin cambios las constantes de `Σ_L`
([Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md) §6.2, reglas 1 y 6; §5.8). Esta
consecuencia se precisó en la comprobación final de coherencia: la revisión focal pedía recalcular
el subgrafo conjunto desde una frontera común.

**P3 hizo una recalculación más profunda**, con la frontera `{H, B, altura de h, rbCone, D_1}`:

- recalculó `K = B + (rbCone, 0)` y `M` exactamente a partir de ella;
- recalculó también constantes de `Σ_L`: el radio de `c_1 = Circle[H, D_1]` es en P3 `|HD_1|`, y
  no el de la matriz capturada;
- por eso dio `0E-79`.

Es una evaluación con 80 cifras en una configuración del tipo `Σ_B`, no una prueba exacta, y solo
tiene valor diagnóstico (Etapa A §6.6). La validez del teorema en `Σ_L⁺` se apoya en §5.8 y la
decide la puerta G-M3.

### 5.8 Exactitud aritmética de las constantes capturadas del modelo (comprobación final, 2026-10-24)

Análisis de la aritmética del código heredado y de la captura de `CU2`, sin ejecutar ningún
sondeo. Sirve para decidir si las relaciones que usa el teorema de §5.3 se cumplen exactamente en
la configuración capturada `Σ_L` de `e_1` y `o_1`, que es la configuración autoritativa
([Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md) §2.3).

**Cómo captura `CU2` las constantes**
(`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/CertifiedConstructionCapture2D.java`):

- Una recta constante se captura por sus coeficientes binary64 `(x, y, z)` (`:1034-1047`).
- Una circunferencia constante se captura por su matriz, `(A2, A4, A5)` con centro `(−A4, −A5)`,
  más su semieje (`:1050-1071`). La clase v3 usa la matriz (`r² = A4² + A5² − A2`) para la
  intersección geométrica, y el semieje solo para la banda heredada
  (`ConstructionBoxModel2D.java:1273-1310`, D-CU2-T13-R7).
- El soporte del conductor se captura por sus semiejes, sus vectores propios y su centro
  (`:286-297`).

El objeto vivo `c_1` tiene, por tanto, dos representaciones del radio que pueden diferir en el
redondeo: el semieje, que es `M.distance(P)`, y `A2 = Σ m_i² − r²` redondeado
(`GeoQuadricND.setSphereNDMatrix`, `:321-345`). Su semántica en `Σ_L` es la de la matriz.

**Aritmética heredada**
(`source/shared/common/src/main/java/org/geogebra/common/kernel/`):

- `AlgoOrthoLinePointLine.compute` (`algos/…:129-140`): `cross(P, l.x, l.y, 0)` y después
  `getNormalizedCoefficients(…, 10E6, 1E-2)`, que solo escala por **potencias de dos** y solo
  cuando los tres coeficientes quedan fuera de los límites (`geos/GeoLine.java:1620-1650`).
- `AlgoLinePointLine.compute` (`algos/…:118-121`): `cross(P, l.y, −l.x, 0)`.
- Productos vectoriales `GeoVec3D.cross` (`geos/GeoVec3D.java:234-237` y `:396-411`), también en la
  intersección de dos rectas.
- Circunferencia por centro y punto: `setCircle` → `setCircleMatrix` → `setSphereNDMatrix`, que
  copia el centro.

**Derivación para el modelo**, con `xAxis = (0, 1, 0)` y coordenada homogénea `z = 1` en `A`, `B`
y `H`:

- `g = (−1, 0, A.x)`;
- `h = (0, −1, A.y)`;
- `k = (−H.z, 0, H.x)`, vertical; `H ∈ k` exactamente, porque los dos productos `H.z·H.x` son el
  mismo valor binary64;
- `i = (0, −B.z, B.y)`, horizontal;
- `I = cross(i, k) = (−B.z·H.x, −B.y·H.z, −B.z·H.z)`, que con `z = 1` es exactamente
  `(H.x, B.y)`;
- `c_1` con centro exactamente `H`;
- `p` con centro `B`, semieje 5 y vectores propios `(1, 0)` y `(0, 1)` (P2).

Todas son productos por 0 y por ±1, sumas con cero y copias de coordenadas, que son exactas en
IEEE 754.

**Consecuencia.** Las relaciones que usa el teorema se cumplen **exactamente** en `Σ_L`:

- `k` vertical por `H`;
- `h` e `i` horizontales;
- `I` sobre `i` y sobre `k`;
- el centro de `c_1` en `k`;
- el centro de `p` en `i`;
- `g ∥ k`.

Los valores métricos (los radios de `c_1` y de `p`) no hace falta que sean exactos. El teorema se
conserva, por tanto, en `Σ_L⁺`.

**Hipótesis a confirmar en la puerta G-M3** (no medidas aquí):

- la coordenada homogénea `z = 1` de los puntos libres y sobre caminos;
- que los estados capturados son los que da esta aritmética.

En un modelo con rectas oblicuas, el coeficiente `z` de una paralela por un punto se redondea,
y una relación análoga puede no sobrevivir. Entonces M3 refuta en `Σ_L⁺` y el fallo es cerrado
(Etapa A §6.6).

## 6. Clasificación núcleo frente a capa externa

Según la regla de colocación de `AGENTS.md` §4:

| Capacidad | Capa | Razón |
|---|---|---|
| `Point(L, D)` e `Intersect(L, r, D)` (Etapa A) | núcleo Java compartido | identidad, DAG, incidencia y serialización; varios clientes |
| Correspondencia `D ↦ t(D)` y su verificación de procedencia | núcleo | precondición de incidencia y de identidad |
| Taxonomía y germen tangente (Etapa B) | núcleo | identidad durable y admisibilidad |
| Envolventes de `h`, `h′` y `h″` | núcleo | evidencia de admisibilidad de un resultado del DAG |
| Evaluación exacta del corte (M3) | núcleo | certifica una incidencia; su resultado decide la admisibilidad |
| Normalización de residuos (H-2) | núcleo | política de un resultado semántico |
| Ejes como objetivos (H-3) | núcleo (registro de identidad y adaptador de objetivo) | identidad durable |
| Objeto de punto crítico (opción futura B de F-2-R; no incorporado a `CU3`) | núcleo | punto del DAG con identidad |
| Bancos, sondeos y comparación con referencias | capa externa (`tools/benchmark`, pruebas) | validación |
| Prover simbólico de GeoGebra como contraste | capa externa (evidencia) | depende de un CAS y no es determinista en tiempo; nunca es autoridad |
| Inspector, mensajes y textos de diagnóstico | aplicación de escritorio | presentación |
| Exportación del punto | adaptadores DXF y LaTeX existentes | consumen el punto como cualquier otro, sin lógica geométrica nueva |

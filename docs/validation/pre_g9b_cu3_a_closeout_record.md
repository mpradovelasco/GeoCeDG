# PRE-G9B-CU3 — Registro de cierre de `CU3-A`

| Campo | Valor |
|---|---|
| Carácter | Registro de cierre de `CU3-A`, efectivo por la decisión autoral de 2026-10-27 (§0). Es la única autoridad de ese estado. No cierra `CU3-B` ni `CU3` global ni publica nada |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Resolución de OBS-01, cierre de CU3-A, mantenimiento del verificador y autorización de CU3-B1» (2026-10-27); precisión autoral «OBS-CU3A-SMOKE-01 y cierre de CU3-A» (2026-10-27); decisión autoral «Cierre de CU3-A y continuación de CU3-B1» (2026-10-27) |
| Candidato | `f61f295447d75755e4c932a0f8f41533563550dd` (árbol `875e1cbf5dca2b6d0bbea8b72d0c24ec3aa306c0`), sin cambios de producto |
| Estado | `CU3-A` `PASS — AUTHOR APPROVED` sobre `f61f2954`; `OBS-CU3A-SMOKE-01` `CLOSED — AUTHOR ACCEPTED DISPOSITION`; `OBS-CU3A-SMOKE-02` `CLOSED — AUTHOR ACCEPTED EXPLANATION`; `OBS-CU3A-XMLEDIT-01` registrada aparte, no bloqueante (§5); `CU3` global sin `PASS — AUTHOR APPROVED`; nada publicado |
| Espejo | [`pre-g9b-cu3-a-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-a-closeout.json) |
| Rectificación | Este texto sustituye al de `e88c6b5b`, que se retira en tres puntos: registraba `CU3-A` como `PASS — AUTHOR APPROVED` antes de la precisión autoral, atribuía la indefinición a un paso de `K_1` por el origen de `n_1` y proponía el remedio `Point(o_1, K)` (§1.4, punto 3) |

## 0. Decisión autoral

Transcripción de la decisión autoral de 2026-10-27, «Cierre de CU3-A y continuación de CU3-B1»:

- **`OBS-CU3A-SMOKE-01` cerrada.**
  - La indefinición de `e_2` procede de una degeneración evitable del procedimiento constructivo original de `K_1`, no de un defecto del núcleo ni de una regresión de `CU3`.
  - Se aprueba la construcción alternativa por contragiro en planta, con su equivalencia geométrica, la preservación de identidades y el comportamiento dinámico verificados sobre una copia.
  - El modelo original de `C:\Temp` se conserva sin modificar, y la copia corregida queda identificada (§1.4, punto 5).
  - Se mantiene documentada la degeneración excepcional `rbCone = d` (§1.4, punto 1).
- **Observación aparte.** La omisión de objetos al cargar una copia cuyo XML se editó externamente se registra como observación no bloqueante (§5). No autoriza cambios de producto y no es un fallo de la redefinición interna.
- **`CU3-A` `PASS — AUTHOR APPROVED`** sobre `f61f295447d75755e4c932a0f8f41533563550dd`:
  - se apoya en las verificaciones integradas ya autenticadas y en el smoke autoral satisfactorio;
  - no se repiten verificaciones integradas por cambios solo documentales;
  - no implica el cierre global de `CU3`.
- **Continuación.**
  - Se ratifican R1–R6 y la autorización de `CU3-B1`; `CU3-B2` sigue sin autorización.
  - `CU3-B1` se entregará como candidato técnico congelado y verificado, pendiente de aceptación autoral.
  - La corrección del verificador PowerShell queda aceptada como mantenimiento independiente.
- **Publicación.** No se publican ramas, no se modifica `main` y no se crean etiquetas.

## 1. `OBS-CU3A-SMOKE-01`

### 1.1 Archivo y cadena

**Archivo del autor.**

- `C:\Temp\InterCilConoOblique.cedg`, el único modelo de esa carpeta. No se ha modificado.
- Es idéntico byte a byte al modelo versionado `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg`: SHA-256 `1AE6F51293FB111B54510948CBCC6E8BA38AB63B658BA3F8A668858AE890456C`, 58 896 bytes.
- Está bajo la política `legacy-representation-authority/v1`, con `rbCone = 5` (deslizador de 0,5 a 10).
- Todas las sondas trabajan sobre copias, con una sonda externa de escritorio.

**Cadena de dependencias.**

- `e_2 = LocusV2(R_1, E_1)`.
- El generador es `R_1 = translationCoor(J_1, O_1, k, Q_1, j_2)`; `Q_1` desplaza `L_1` la longitud `d_2` de `w_1 = LocusLength(o_1, N_1, K_1)`.
- `N_1 = Point(o_1, text2, v_1)` (`AlgoSemanticLocusPoint2D`) es el origen de esa longitud. Es el punto de `o_1` en el parámetro `v_1 = 0` del conductor, sobre la componente que designa el selector manual `text2`.
- `text2` es el selector principal `geocedg-locus-principal-selection/v1` de la componente `generator.main/component/[-π, π)/closed/open`: la curva cerrada completa de `o_1`.
- `o_1 = LocusV2(K_1, E_1)`, con `K_1 = Intersect(b_1, n_1)`.

### 1.2 Transición observada

| `rbCone` | `N_1` | causa de `N_1` | `R_1` | existencia certificada de `o_1` (`CU2`) |
|---|---|---|---|---|
| 5 a 7,57 | definido | `RESOLVED` (`RESOLVED_BY_DECOMPOSITION_EQUALITY`) | definido | una componente periódica cerrada, `COMPLETE` |
| 7,58 a 7,60 | indefinido | `PRINCIPAL_BRANCH_UNRESOLVED` | indefinido | bandas no resueltas `BUDGET_EXHAUSTED` en `t ≈ ±0,041…±0,083`; `NOT_ESTABLISHED` |
| 7,65 a 10 | indefinido | `PRINCIPAL_BRANCH_UNRESOLVED` | indefinido | bandas `DEGENERATE_CONFIGURATION` en `t ≈ ±0,33…±0,71` («Bounded-line membership is not decided»); `NOT_ESTABLISHED` |
| vuelta a 7,55 o menos | definido | `RESOLVED` | definido | componente única |

`e_2` conserva su definición semántica `VALID`, pero ninguna de sus 360 muestras es válida desde 7,58.

### 1.3 Mecanismo geométrico

**Hechos exactos del modelo.** Se siguen de la construcción, no de la medida:

- `g` es vertical por `A`; `h` es horizontal por `A`; `k = Line(H, g)` es vertical por `H`; `i = Line(B, h)` es horizontal por `B`. Por tanto `I = i ∩ k` cumple `x_I = x_H`.
- `E_1` está sobre `p = Circle(B, rbCone)`; `F_1 = h ∩ Line(E_1, k) = (x_E, y_A)`.
- `J_1` está en `q_1 = Ray(H, F_1)`, así que `J_1 = H + μ (F_1 − H)` con `μ = (y_J − y_H) / (y_A − y_H) ≥ 0`.
- `b_1 = Line(J_1, k)` es la vertical por `J_1`; `n_1 = Ray(I, E_1)`.

**Consecuencia.**

- `x_J − x_I = μ (x_E − x_I)`.
- Si `x_E ≠ x_I`, entonces `K_1 = I + μ (E_1 − I)`.
- Si `x_E = x_I`, `b_1` y la recta de `n_1` son la misma vertical `x = x_I`, y `K_1` queda indefinido: no hay intersección única.
- `x_E = x_I` equivale a `cos t = d / rbCone`, con `d = x_I − x_B = 7,573539944963338` (distancia horizontal entre el eje `g` y la vertical `k` del vértice). Ocurre en `t = ±t*`, `t* = arccos(d / rbCone)`, solo si `rbCone ≥ d`.

**Medida (sonda sobre copias).**

- `t*` vale 0,041289 con 7,58; 0,065870 con 7,59; 0,083470 con 7,60; y 0,327988 con 8. Coincide con las bandas no resueltas que `CU2` informa en cada valor.
- En `t = ±t*` exactos, `K_1` del núcleo es indefinido.
- A `10⁻¹¹` de `t*`, `K_1` está definido pero pierde hasta `1,6·10⁻⁴`: es el mal condicionamiento de una intersección casi coincidente.
- Lejos de `t*`, `K_1` coincide con `I + μ (E_1 − I)` hasta `10⁻¹⁴`.

**Lectura geométrica.**

- `n_1` es la proyección horizontal de la generatriz del cono por `E_1`; `b_1` es la línea de referencia de `J_1`.
- Si `rbCone > d`, la base del cono corta la vertical de la proyección `I` del vértice. Entonces dos generatrices son **de perfil**: su proyección horizontal es paralela a las líneas de referencia.
- En ellas, el método de la línea de referencia es indeterminado. El punto del espacio existe y la curva es continua; los límites laterales de `K_1` coinciden. Es una discontinuidad evitable **de la construcción**, no de la curva.
- `K_1` **no** pasa por el origen `I` de `n_1`, como afirmaba `e88c6b5b`: el parámetro de la semirrecta vale `μ ≈ 0,70`. La pertenencia no se decide porque ese parámetro es una forma `0/0`.

### 1.4 Respuesta a la precisión autoral

**1. Existencia del punto ideal.**

- El punto que designa `N_1` es el punto de la curva en el parámetro `t = 0` del conductor. Existe para todo `rbCone ≠ d`, porque `t = 0` nunca es parámetro de perfil (`x_E(0) − x_I = rbCone − d ≠ 0`).
- No desaparece por una condición geométrica.
- La única excepción es el valor aislado `rbCone = d`. Allí `E_1(0) = I`, y la propia construcción de `J_1` por giro degenera en `t = 0` (`d_1 = Circle(I, E_1)` de radio 0). `e_1` y la cadena corregida (punto 4) también son indefinidos en ese valor.

**2. Por qué falla el selector.**

- La construcción de `K_1` es indefinida en `±t*`, con evaluaciones válidas a ambos lados.
- Por el contrato aprobado, `locus-v2-existence-components.md`:
  - §15.2: una discontinuidad estructural con evaluaciones válidas a ambos lados es frontera, nunca punto interior;
  - §15.3: el cierre lo decide solo el certificado.
- Así que la componente periódica cerrada que designa `text2` **no existe** para la construcción escrita, y por §15.6 el selector no se resuelve.
- **Clasificación.** Es una cuestión de construcción del modelo (líneas de referencia con generatrices de perfil). La indefinición es legítima para la construcción tal como está escrita. No es un defecto del núcleo ni una regresión de `CU3`.
- **Limitación de certificación.** Solo afecta a la precisión del diagnóstico. `CU2` informa bandas no resueltas (`BUDGET_EXHAUSTED` cerca del umbral, `DEGENERATE_CONFIGURATION` después) en lugar de identificar la coincidencia aislada. Un certificador exacto tampoco daría la componente cerrada, así que el resultado de `N_1` no cambiaría.

**3. `Point(o_1, K)` no es la misma construcción semántica que `N_1`.**

- Designa el mismo punto **por un teorema del modelo**, no por la construcción:
  - `K` es la segunda salida de `Intersect(p, i)`, con `i` por el centro `B`;
  - `K − B = (rbCone, 0)` y `E_1(0) − B = (rbCone, 0)` exactamente, con integridad `STRICT`.
- Su componente es la que contiene `t(K)`, no la componente cerrada.
- `LocusLength` no lo admite como extremo: `LocusLength(o_1, N_2, K_1)` da `INVALID_QUERY` incluso con `rbCone = 5`, en las cuatro posiciones probadas. `AlgoLocusDriverCorrespondencePoint2D` no es una familia de extremos del resolvedor métrico.
- No puede sustituir a `N_1` en `w_1`. Sustituirlo sería una coincidencia numérica más un cambio de contrato. Se retira el remedio de `e88c6b5b`.

**4. Corrección rigurosa y mínima: `K_1` por contragiro en planta.**

Se usan solo uniones, paralelas e intersecciones de rectas (clase v1), con once objetos auxiliares ocultos:

```text
eK1 = Line(E_1, h)        EK1 = Intersect(k, eK1)
jK1 = Line(J_1, h)        lK1 = Line(H, I_1)        GJ1 = Intersect(lK1, jK1)
gK1 = Line(GJ1, k)        GK1 = Intersect(gK1, i)
mK1 = Line(H_1, EK1)      pK1 = Line(GK1, mK1)      KK1 = Intersect(k, pK1)
hK1 = Line(KK1, h)        K   = Intersect(b_1, hK1)
```

**Demostración.**

- `G_1 = H + μ (I_1 − H)` y `x_I = x_H`. Por tanto la homotecia de centro `I` y razón `μ` lleva `H_1` a `GK1` (los dos sobre `i`), y `EK1 = (x_I, y_E)` a `KK1`, porque `k` es invariante y `mK1 ∥ pK1`.
- Así `KK1 = (x_I, y_I + μ (y_E − y_I))` y `K = (x_J, y_I + μ (y_E − y_I)) = I + μ (E_1 − I)`.
- Es el mismo punto que `K_1` donde `K_1` está definido. Es una identidad exacta, no numérica.
- Todas las intersecciones son transversales para todo `t`. La única degeneración es `E_1 = I` (`rbCone = d`, `t = 0`), compartida con la cadena original.
- `GJ1` se obtiene de `J_1` y no de `G_1`. Así los ancestros identificados más próximos de `K` siguen siendo `{E_1, J_1}`, como en el registro de identidad de `K_1`.

**Significado y parametrización.** Mismo conductor `E_1`, mismo parámetro `t` y mismo punto. La traza es la misma curva más sus dos puntos evitables.

**Verificación.** Se construyó una cadena paralela en una copia bajo la política heredada, sin tocar ninguna identidad persistida: `K_2 → o_2 = LocusV2(K_2, E_1) → N_3 = Point(o_2, text2, v_1) → w_3 → d_3 → Q_2 → j_3 → R_2 → e_3`.

| `rbCone` | `o_2` (`CU2`) | `N_3` | `K_1` indefinido / `K_2` indefinido | máx. `|K_2 − K_1|` | máx. `|d_3 − d_2|`, `|R_2 − R_1|` | muestras válidas `e_2` / `e_3` |
|---|---|---|---|---|---|---|
| 5 | `COMPLETE`, 1 componente | `RESOLVED`, `|N_3 − N_1|` = 8,9·10⁻¹⁶ | 0 / 0 de 72 | 4,5·10⁻¹⁵ | 3,6·10⁻¹⁵ | 360 / 360 |
| 7,5 | `COMPLETE`, 1 | `RESOLVED`, 2,5·10⁻¹⁴ | 0 / 0 de 72 | 2,5·10⁻¹⁴ | 3,6·10⁻¹⁴ | 360 / 360 |
| 7,58 | `COMPLETE`, 1 | `RESOLVED` (`N_1` indefinido) | 2 / 0 de 74 | 1,4·10⁻¹³ | — (`R_1` indefinido) | 0 / 360 |
| 8 | `COMPLETE`, 1 | `RESOLVED` (`N_1` indefinido) | 2 / 0 de 74 | 9,4·10⁻¹⁴ | — | 0 / 360 |
| 10 | `COMPLETE`, 1 | `RESOLVED` (`N_1` indefinido) | 2 / 0 de 74 | 4,4·10⁻¹⁴ | — | 0 / 360 |

- El barrido de 5 a 10 y de vuelta, en pasos de 0,05, mantiene definidos `N_3`, `R_2` y `e_3` en todo el recorrido.
- La única excepción es `rbCone = d`, por el punto 1.

**5. Aplicación de la corrección.**

- **Producto.** No cambia. La corrección es del modelo del autor, y ninguna corrección de producto entra en el alcance autorizado (§1.5).
- **Vía en sitio, verificada en una copia bajo la política heredada.** Se crean los once objetos auxiliares, ocultos y auxiliares, y se redefine `K_1 = Intersect(b_1, hK1)` con la redefinición de la aplicación. Resultado:
  - no se pierde ningún objeto;
  - `N_1`, `w_1`, `d_2`, `R_1` y `e_2` siguen siendo los objetos originales; las identidades persistidas de `K_1` y `N_1` se conservan al guardar;
  - la política sigue siendo la heredada;
  - el estado del autor (`rbCone = 5`) no cambia (diferencia máxima `8,9·10⁻¹⁶`);
  - con `rbCone` = 5, 7,5, 7,58, 8 y 10: `N_1` `RESOLVED`, `o_1` `COMPLETE` con una componente, `R_1` definido y 360 de 360 muestras válidas de `e_2`;
  - el barrido de 5 a 10 y de vuelta, en pasos de 0,25, mantiene `N_1` y `R_1` definidos; las 41 actualizaciones tardan 6,7 s;
  - guardada y reabierta, la copia da los mismos resultados.
- **Copia corregida para uso posterior.** `C:\Temp\InterCilConoOblique-K1-contragiro.cedg`, 59 277 bytes, SHA-256 `E967D91EEA30FCB7BDB20ADEA58E7AD53487059571632122AC845B4DDA0A381E`. Se obtuvo así:
  - en una copia, la aplicación creó los once auxiliares, redefinió `K_1` y guardó (copia intermedia de la sonda, SHA-256 `DA41E8A7…`, con la disposición de ventana de la aplicación de pruebas);
  - su construcción se insertó en el archivo del autor, que conserva la cabecera de vista y la miniatura;
  - el paso de la navegación del protocolo se ajustó de 109 a 120, el número de pasos de la construcción corregida (§5);
  - la carga real de esa copia reproduce los resultados anteriores: ningún objeto ausente, estado del autor idéntico (`8,9·10⁻¹⁶`), `N_1` `RESOLVED`, `o_1` `COMPLETE` y 360 de 360 muestras válidas de `e_2` con `rbCone` = 5, 7,5, 7,58, 8 y 10.
- **Receta para reproducirla en otra sesión.** Crear los once auxiliares del punto 4 (ocultos) y redefinir `K_1 = Intersect(b_1, hK1)`.
- **Original.** `C:\Temp\InterCilConoOblique.cedg` no se ha modificado (SHA-256 `1AE6F512…`, sin cambios).

**6. Hallazgo lateral.** Es `OBS-CU3A-XMLEDIT-01` (§5); no forma parte de `OBS-CU3A-SMOKE-01`.

### 1.5 Disposición final

`OBS-CU3A-SMOKE-01` — `CLOSED — AUTHOR ACCEPTED DISPOSITION` (decisión autoral de 2026-10-27, §0). La disposición aceptada:

- La indefinición de `N_1` es **matemáticamente legítima** para la construcción escrita: dos generatrices de perfil cortan la componente cerrada desde `rbCone = d`.
- Es **corregible en el modelo** sin cambiar su significado ni su parametrización (§1.4, punto 4). La corrección en sitio está verificada en una copia (§1.4, punto 5).
- No hay defecto del producto ni regresión de `CU3`.
- Ninguna corrección de producto entra en el alcance autorizado. Cerrar componentes a través de discontinuidades evitables contradice §15.2 y §15.3 y exigiría un contrato normativo nuevo.

## 2. `OBS-CU3A-SMOKE-02`

`OBS-CU3A-SMOKE-02 — CLOSED — AUTHOR ACCEPTED EXPLANATION`.

La adopción explícita se comprobó en una copia limpia (registro de D1-A y del smoke, §4). No hay cambios de producto.

## 3. Hechos del cierre

| Hecho | Estado |
|---|---|
| Candidato técnicamente aceptado | `f61f2954`: `INTEGRATED_PHASE` `ACCEPTED / COMPLETE` (`INTEGRATION` `verification-ef34ade20c5d44a79a93926dce2c2ee5`), autenticada ([registro de D1-A y del smoke](pre_g9b_cu3_a_d1a_and_smoke_record.md) §6) |
| Smoke autoral comunicado | `AUTHOR SMOKE — FUNCTIONALLY SATISFACTORY WITH TWO OPEN OBSERVATIONS`, sobre `9f6f27f2`, que difiere de `f61f2954` solo en la contabilidad D1-A, con resultados publicados idénticos |
| Observaciones | `OBS-CU3A-SMOKE-01` `CLOSED — AUTHOR ACCEPTED DISPOSITION` (§1.5); `OBS-CU3A-SMOKE-02` `CLOSED — AUTHOR ACCEPTED EXPLANATION` (§2); `OBS-CU3A-XMLEDIT-01` abierta, no bloqueante (§5) |
| Aprobación autoral | decisión autoral de 2026-10-27, «Cierre de CU3-A y continuación de CU3-B1» (§0) |
| Publicación | ninguna: la rama remota sigue en `b1813da1`; `main`, las etiquetas y `geocedg_book` no cambian |

**Reautenticación de la campaña de `f61f2954`** (2026-10-27): `Read-VerificationJson` y `Get-VerificationResultHash`; diario `COMPLETED`; commit y árbol exactos. No se repitió ninguna verificación.

| Comprobación | `run_id` | Veredicto | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-80b6ae338d504831906be53ae008b1fb` | `ACCEPTED / COMPLETE` 3/3 | `cddb937d24885f19991fe63f89c1404c26487c6cda3029aacecdbe8832d4b8d8` |
| `INFRA_UNIT` (PowerShell 7.6.6 aislado) | `verification-b373cdfdf58b4fe1b38b475486afdefd` | `ACCEPTED / COMPLETE` 22/22 | `88b5088c305c42c78f0c4005c719c4b0eb7cfd1ff69a53697d276c6ad3265dcf` |
| `PHASE` `PRE-G9B-CU3` | `verification-e1bf5c61e39c45a382b7b756ff44223d` | `ACCEPTED / COMPLETE` 4/4 | `97ac69dbc08ef3a640b1b2aade7369047133425f240d1340a89cc9ddc4562310` |
| `PHASE` `PRE-G9B-CU2` | `verification-99063f59d1084745ac46f5d93b337911` | `ACCEPTED / COMPLETE` 4/4 | `5a55827e57e8368ab43821127f54df81ee84e5c7ae4d2db445a266b15e9513f8` |
| `PHASE` `PRE-G9B-CU1` | `verification-1a4dad5532a247079ed77d8f0891b1d1` | `ACCEPTED / COMPLETE` 4/4 | `527ce06947c17dc3564f23b1c06ba1fdf888ef59496a436446b158a57a0f401f` |
| `PHASE` `PRE-G9B-CU0` | `verification-6cf174d0b6e64b57a6f50213f64d4cf0` | `ACCEPTED / COMPLETE` 2/2 | `9c47f4e236ec6f67a4c815f5810016d943d034afb5a51a47d9fe2811214b1c95` |
| `INTEGRATION` | `verification-ef34ade20c5d44a79a93926dce2c2ee5` | `ACCEPTED / COMPLETE` 19/19 | `3a7668aaf39e095e09572884818cc193806c41f037564ee642bc26427d136efb` |

## 4. Condiciones de la aprobación

| Condición | Cumplimiento |
|---|---|
| 1. Causa de OBS-01 rigurosamente establecida | sí (§1.3 y §1.4), con disposición final aceptada por el autor (§1.5) |
| 2. Defecto corregido dentro del alcance | no hay defecto del producto; la corrección es del modelo y está aprobada y verificada en una copia (§1.4, puntos 4 y 5) |
| 3. Garantías geométricas y de identidad | sin cambios de producto desde la verificación; fallo cerrado tipado |
| 4. Verificación del candidato definitivo | `INTEGRATED_PHASE` de `f61f2954` `ACCEPTED / COMPLETE`, reautenticada (§3) |
| 5. Sin regresiones bloqueantes conocidas | ninguna; `OBS-CU3A-XMLEDIT-01` no es bloqueante |
| 6. Comprobación manual por cambios funcionales | no procede: el producto no cambia |

**Resultado: `CU3-A` `PASS — AUTHOR APPROVED`** sobre `f61f295447d75755e4c932a0f8f41533563550dd`, por la decisión autoral de 2026-10-27 (§0).

- No implica la aprobación de `CU3-B` ni la de `CU3` global.
- `CU3-B1` continúa como candidato técnico pendiente de aceptación autoral; `CU3-B2` no está autorizado.

## 5. `OBS-CU3A-XMLEDIT-01` — omisión de objetos al cargar un XML editado externamente

| Campo | Valor |
|---|---|
| Estado | abierta, **no bloqueante**; registrada por decisión autoral (§0); pendiente de una eventual caracterización completa |
| Alcance | solo copias cuyo `geogebra.xml` se editó fuera de la aplicación. La redefinición interna de la aplicación no está afectada (§1.4, punto 5) |
| Hecho | Tres copias editadas externamente cargaron sin error pero sin sus últimos objetos, `N_1` incluido: la sustitución de la orden de `K_1`, la misma con `u_1` retirado y la construcción insertada en el archivo del autor. `processXMLString` terminó sin excepción |
| Causa identificada | La cabecera conserva `consProtNavigationBar … consStep="109"`, el último paso de la construcción original. La construcción editada tiene más pasos (120 con los once auxiliares), y los objetos posteriores al paso 109 quedan sin construir en la carga. Con `consStep="120"`, la copia combinada carga completa (§1.4, punto 5) |
| Pendiente | decidir si basta con documentar ese comportamiento de la navegación por pasos o si la carga debería avisar cuando el paso persistido no es el último. No se autorizan cambios de producto |

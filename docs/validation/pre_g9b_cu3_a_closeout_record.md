# PRE-G9B-CU3 — Expediente de cierre de `CU3-A` (preparado, sin efecto)

| Campo | Valor |
|---|---|
| Carácter | Expediente de cierre de `CU3-A`, preparado y **sin efecto**. La precisión autoral de 2026-10-27 sobre `OBS-CU3A-SMOKE-01` lo deja en suspenso hasta la disposición final de esa observación. No cierra `CU3-B` ni `CU3` global ni publica nada |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Resolución de OBS-01, cierre de CU3-A, mantenimiento del verificador y autorización de CU3-B1» (2026-10-27) y precisión autoral «OBS-CU3A-SMOKE-01 y cierre de CU3-A» (2026-10-27) |
| Candidato | `f61f295447d75755e4c932a0f8f41533563550dd` (árbol `875e1cbf5dca2b6d0bbea8b72d0c24ec3aa306c0`), sin cambios de producto |
| Estado | `CU3-A` **sin** `PASS — AUTHOR APPROVED`; `OBS-CU3A-SMOKE-01` `CHARACTERIZED — FINAL DISPOSITION PENDING`; `OBS-CU3A-SMOKE-02` `CLOSED — AUTHOR ACCEPTED EXPLANATION`; nada publicado |
| Rectificación | Este texto sustituye al de `e88c6b5b`, que se retira en dos puntos: registraba `CU3-A` como `PASS — AUTHOR APPROVED` y atribuía la indefinición a un paso de `K_1` por el origen de `n_1`. También se retira el remedio `Point(o_1, K)` (§1.4, punto 3) |

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
- **Copia corregida.** `InterCilConoOblique-K1-corregido.cedg`, SHA-256 `DA41E8A7DED1D6417115C8625166D1B80F49B47810B07FBA1DCA4557B4C5566E`, guardada por la sonda fuera del árbol del repositorio.
- **Original.** `C:\Temp\InterCilConoOblique.cedg` no se ha modificado (SHA-256 sin cambios). Aplicar la corrección al modelo del autor es una decisión autoral.

**6. Hallazgo lateral, no aislado.**

- Una copia en la que se reescribió el XML persistido, sustituyendo solo la orden de `K_1` por la cadena anterior, supera el registro de identidad de `K_1`.
- Sin embargo, la carga omite en silencio los once objetos posteriores a `i_2` (`u_1`, `text2`, `v_1`, `N_1`, `w_1`, `d_2`, `Q_1`, `j_2`, `R_1`, `k_2` y `e_2`), sin error: `processXMLString` termina sin excepción.
- No es una vía admitida, porque el documento se editó fuera de la aplicación. Queda para una decisión autoral aparte; no forma parte de `OBS-CU3A-SMOKE-01`.

### 1.5 Disposición propuesta (pendiente del autor)

`OBS-CU3A-SMOKE-01` sigue `CHARACTERIZED — FINAL DISPOSITION PENDING`. Propuesta:

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
| Observaciones | `OBS-CU3A-SMOKE-01` `CHARACTERIZED — FINAL DISPOSITION PENDING` (§1); `OBS-CU3A-SMOKE-02` cerrada (§2) |
| Aprobación autoral | condicionada de 2026-10-27, **en suspenso** por la precisión autoral de la misma fecha |
| Publicación | ninguna: `main`, las etiquetas y `geocedg_book` no cambian |

## 4. Condiciones de la aprobación

| Condición | Cumplimiento |
|---|---|
| 1. Causa de OBS-01 rigurosamente establecida | establecida (§1.3 y §1.4); su disposición final es del autor |
| 2. Defecto corregido dentro del alcance | no hay defecto del producto; la corrección es del modelo (§1.4, puntos 4 y 5) y su aplicación es decisión autoral |
| 3. Garantías geométricas y de identidad | sin cambios de producto desde la verificación; fallo cerrado tipado |
| 4. Verificación del candidato definitivo | `INTEGRATED_PHASE` de `f61f2954` `ACCEPTED / COMPLETE` |
| 5. Sin regresiones bloqueantes conocidas | ninguna |
| 6. Comprobación manual por cambios funcionales | no procede: el producto no cambia |

**Resultado: no registrado.** `CU3-A` sigue sin `PASS — AUTHOR APPROVED` hasta que el autor resuelva la disposición final de `OBS-CU3A-SMOKE-01`. Esta aprobación, cuando llegue, no implicará la de `CU3-B` ni la de `CU3` global.

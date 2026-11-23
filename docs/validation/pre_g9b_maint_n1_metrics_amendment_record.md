# PRE-G9B-MAINT-N1 — registro de la enmienda candidata de métricas (§28.3 y §29)

| Campo | Valor |
|---|---|
| Carácter | Registro de la propuesta normativa. `selfApproved=false`. `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`: la decisión autoral sobre la enmienda se registra después, fuera de este documento |
| Autorización | Instrucción autoral del 2026-11-23, entregada en la conversación: «AUTORIZO iniciar exclusivamente `PRE-G9B-MAINT-N1`, como incremento normativo documental, conforme al expediente P0 aprobado y al roadmap vigente» |
| Decisiones que la motivan | D-B-1a (dirección normativa de B5a) y D-B-2 (solo B2-F1), aprobadas en la [resolución autoral de P0](pre_g9b_maint_p0_author_resolution_record.md) y transcritas en la [hoja F](pre_g9b_maint_p0_author_decision_sheet.md) §0 |
| Base | `main = origin/main = e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` (comprobados contra `origin` antes de crear la rama) |
| Rama | `phase/pre-g9b-maint-n1-metrics-amendment`, local y sin publicar |
| Commits | `84e05a59b`: paso de gobernanza (prompt canónico). El commit documental N1 contiene este registro y la enmienda; su identidad y la de su `STATIC` se informan fuera del commit |
| Prompt | [`pre-g9b-maint-n1-metrics-amendment.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-n1-metrics-amendment.prompt.md) |
| Clase | `CHANGE_ROUTE=ORDINARY`; `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`, congelada al inicio; `PRODUCT_PHASE_EFFECT=NONE`; nivel `STATIC` |
| Enmienda | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §28.3.1 (B5a) y §29.1 (B2-F1), ambas candidatas y sin vigor |

## 1. Alcance y exclusiones

La autorización pide preparar la enmienda acotada de `locus-v2-metrics.md` para:

1. B5a (§28.3): comprobación numérica conservadora de posibles discontinuidades alineadas con
   la cuerda, sin reclamar certificación de continuidad;
2. B2-F1 (§29): limpieza transaccional de autoridades ricas auxiliares sin consumidores tras
   completar una redefinición.

Exclusiones expresas, respetadas: B5a y B2 no se implementan; no hay B2-F2 ni barrido al cargar
documentos antiguos; §28.2 no se modifica (D-B-1b sigue pendiente); no se implementa B5b ni se
modifican las políticas de aislamiento de intersecciones; G9B no se abre. Tampoco se modifican
código, pruebas, selecciones, otras especificaciones, ADR, roadmap, libro, `main`, etiquetas ni
releases.

## 2. Paso de gobernanza: revisión del prompt candidato

El prompt candidato de P0
([`maint-n1-metrics-amendment.prompt-candidate.md`](../architecture/pre_g9b_maint_p0_candidate_prompts/maint-n1-metrics-amendment.prompt-candidate.md))
se revisó contra la resolución autoral y la autorización de N1, y se incorporó a
`.github/prompts/tasks/` en el commit `84e05a59b`, separado del commit documental. Hallazgos y
correcciones:

| # | Hallazgo en el candidato | Corrección en el prompt canónico |
|---|---|---|
| G-1 | El alcance permitido incluía §28.2, en contradicción con su propio objetivo y con D-B-1b pendiente | §28.2 pasa al alcance prohibido |
| G-2 | La base era la del expediente por defecto | Base exacta de la autorización, commit y árbol |
| G-3 | Faltaban exclusiones de la autorización (B5b, política de aislamiento, G9B, no implementar B5a ni B2) | Añadidas al objetivo y al alcance prohibido |
| G-4 | El umbral se llamaba `ρ`, símbolo que el contrato de uso condicionado §8.7 usa para la razón de una semejanza | Se llama `ρ_J` |
| G-5 | La parada «la prueba niega un número hoy correcto en los modelos del autor» no es medible en un incremento documental | Se mantiene como parada analítica y pasa a condición de entrada de `MAINT-B5a` (§6) |
| G-6 | No preveía la entrega en dos commits | Commit de gobernanza y commit documental |

Ambos prompts superan `Test-PromptContractDocument` con el perfil `task` (`execution_safe =
true`, sin campos ausentes, duplicados, desconocidos ni desordenados). El candidato queda como
evidencia histórica de P0; donde difiere, prevalece el prompt canónico.

## 3. Decisiones normativas propuestas al autor

### 3.1 B5a — §28.3.1

| Id | Propuesta |
|---|---|
| N1-B5a-1 | La admisión de §28.3 pasaría a `conditioned-length-scalar/v2` con una condición 8: una comprobación de escalado de cuerdas (`conditioned-route-jump-guard/v1`) sobre cada hoja aceptada de la estimación de la ruta. Las condiciones 1 a 7 no cambian |
| N1-B5a-2 | Carácter: detección numérica conservadora. No es prueba ni certificación de continuidad; una ruta admitida conserva H1–H4 como hipótesis. La tabla de §28.3.1 separa continuidad no certificada (estado normal), incumplimiento de H3 observado (sin número) e incumplimiento no detectado (sin cambio) |
| N1-B5a-3 | Procedimiento determinista por hoja: cuarto de cuerda máxima, bisecciones siguiendo la mitad de cuerda mayor, cociente `q_k = c(I_{k+1})/c(I_k)`, firma de salto si `q_k > ρ_J` en `m` niveles consecutivos; parada anticipada cuando ya no caben `m` niveles; desempates hacia el parámetro menor |
| N1-B5a-4 | Tres diagnósticos propios y distintos: `CHORD_SCALING_JUMP_SIGNATURE`, `CHORD_SCALING_INCONCLUSIVE`, `CHORD_SCALING_BUDGET_EXHAUSTED`; la ruta no admitida publica la misma estimación como resultado rico con `CONDITIONED_ROUTE_NOT_ADMISSIBLE`, como la condición 4. Una evaluación fallida en un punto de la comprobación es un fallo del resultado (§28.1) |
| N1-B5a-5 | Valores candidatos `ρ_J = 0,85`, `m = 3`, `L = 5`, `ε_res(λ) = max(τ_λ, 2^8·u·M_λ)`, `E_cap = 8 192` evaluaciones por ruta, con intervalos admisibles dentro de los cuales `MAINT-B5a` fija los valores finales mediante pruebas focales; fuera de ellos hace falta una enmienda nueva |
| N1-B5a-6 | Presupuesto: como máximo `L` evaluaciones por hoja (típicamente `L − m + 1 = 3`) y `E_cap` por ruta, contadas aparte del presupuesto del estimador; no son hojas, no cambian valor ni error y no se reutilizan entre revisiones. Agotar el tope es negativa con causa propia |
| N1-B5a-7 | Límites declarados (seis clases de incumplimientos de H3 no detectables por muestreo finito) y negativas conservadoras (singularidad de velocidad con `α > 1 + log2 ρ_J`, puntos estacionarios de orden muy alto, variación de velocidad no resuelta), nunca presentadas como salto probado |
| N1-B5a-8 | Sin cambios en el recorrido y su política, identidad de contenido de la evidencia, estructura de existencia, inventario de componentes, tokens, identidad semántica, certificados, valor y error de la estimación, H10, P2, §28.1, §28.2, `Intersect` ni serialización. No se aplica a ningún total. B5b y T-A siguen diferidas |

### 3.2 B2-F1 — §29.1

| Id | Propuesta |
|---|---|
| N1-B2-1 | Al adoptarse, sustituye el punto «Declared limit: redefinition» de §29; el resto de §29 no cambia |
| N1-B2-2 | Cuando una redefinición sustituye al número, su autoridad privada pasa a candidata de retirada de esa operación; nada se retira durante la transacción y las guardas de identidad no cambian |
| N1-B2-3 | Al completar con éxito la operación (en lotes, tras el conmutador final), fuera del arrendamiento de publicación y antes de almacenar el punto de Undo, se retira por la retirada ordinaria de §29 cada candidata que siga en la construcción sin lector; el punto de Undo registra el estado sin la autoridad y no hay punto intermedio |
| N1-B2-4 | Criterio: nombre privado del espacio reservado, entrada rica del número sustituido y ningún lector. Nunca se retiran un resultado rico con nombre ordinario, una autoridad con otro lector (incluido el número nuevo), un resultado con identidad y uso propios ni nada que la redefinición no haya hecho candidato |
| N1-B2-5 | Una redefinición fallida o revertida descarta sus candidatas y conserva el par íntegro; Undo restaura el número anterior con su autoridad y Redo el redefinido sin ella, cada uno en un paso; la redefinición nunca se trata como borrar y crear |
| N1-B2-6 | Sin barrido al cargar (B2-F2 no se implementa): abrir, leer y reconstruir estados de Undo/Redo no retiran nada; los huérfanos heredados quedan como limitación declarada, sin migración ni reparación silenciosa |

## 4. Análisis de los valores candidatos

Modelo: curva colineal `x(u)` con cuerda `|x(b) − x(a)|`, peor caso de un salto alineado
(todas las cuerdas son aditivas, como en la sonda P-1). Se aplica el procedimiento de §28.3.1 a
una hoja `[0, 1]` (cuartos de anchura `h_0 = 0,25`). Cálculo en el directorio temporal de la
sesión con Node.js; no forma parte del repositorio ni es evidencia de aceptación.

| Caso | Resultado (`ρ_J = 0,85`, `m = 3`) |
|---|---|
| Salto `J` a velocidad `s`, posiciones `0,1`, `1/3`, `0,618`, `0,9` | firma cuando `J ≳ 2,36·s·h_0 / 2^{L−m}`: `2,36` (`L = 3`), `1,18` (`L = 4`), `0,59` (`L = 5`), `0,30` (`L = 6`), igual en las cuatro posiciones |
| Velocidad divergente `|u − u*|^{−α}` con `u*` en `0, ¼, ½, ¾, 1` | primera firma en `α = 0,77` (cociente constante `0,8526`), conforme a `α* = 1 + log2 0,85 ≈ 0,766`; el pliegue `α = ½` da `0,707` |
| Punto estacionario de orden `k`, `u*` en pasos de `1/256` de la hoja | sin firma hasta `k = 26`; la primera, en `k = 27` (`u* ≈ 0,488`, cocientes `0,9996`, `0,9743`, `0,8509`), igual para `L = 3…6` |
| Mismo barrido con otros `m` (`L = m + 2`) | primera firma estacionaria en `k = 13` con `m = 2`, `k = 27` con `m = 3`, `k = 55` con `m = 4` |
| `u ↦ u³` y `u ↦ u⁸` en las 64 hojas de una partición uniforme de `[0, 1]` | ninguna firma con `m = 2, 3, 4` |
| Coste en hojas sin firma | `L − m + 1` evaluaciones: 1, 2, 3, 4 para `L = 3…6` |

Lectura: `L = 5` divide por cuatro el salto mínimo detectable respecto de `L = m` a cambio de
dos evaluaciones más por hoja sin firma; `m = 3` duplica, respecto de `m = 2`, el orden
estacionario a partir del cual aparecen firmas falsas (de 13 a 27) sin el nivel adicional que
exigiría `m = 4`; `ρ_J = 0,85` sitúa las
negativas conservadoras por velocidad divergente por encima de `α ≈ 0,77`, lejos del pliegue
`α = ½`. Ningún caso de referencia continuo del plan C §1.5 queda negado en el modelo, por lo
que no se cumple la condición de parada analítica del prompt. El modelo no sustituye la
medición sobre los modelos del autor, que es condición de entrada y criterio de aceptación de
`MAINT-B5a`.

## 5. Compatibilidad

- Sin XML nuevo ni carga útil persistida en ninguna de las dos partes.
- B5a: un documento de cualquier versión con `LocusLength(L, A, B)` sobre una ruta condicionada
  cuya comprobación observe una firma, sea inconclusa o agote el presupuesto mostraría un número
  indefinido donde 1.2.2 muestra uno; el resultado rico conserva la estimación con su
  diagnóstico. Lo declara la nota de versión de `MAINT-B5a`.
- B2-F1: la carga de documentos existentes no cambia. Un documento guardado tras una
  redefinición hecha con la regla nueva no contiene la autoridad que 1.2.2 habría dejado. Los
  huérfanos heredados se conservan.
- `GUIDE_IMPACT = NONE`: la norma no cambia la conducta del producto; la guía se actualiza con
  `MAINT-B5a`, si su conducta visible lo exige.
- Impacto de bootstrap: sin cambio; ningún prerrequisito de estación ni supuesto del bootstrap
  de Windows cambia.

## 6. Condiciones de entrada de `MAINT-B5a`

1. Decisión autoral que adopte §28.3.1 (con o sin precisiones) sobre el commit exacto de N1 o
   un descendiente que lo contenga, y autorización propia de `MAINT-B5a` con base exacta.
2. Prompt de `MAINT-B5a` revisado e incorporado a `.github/prompts/tasks/` en un paso de
   gobernanza autorizado, con `VERIFICATION_CLASS=INTEGRATED_PHASE` y nivel `STATIC` +
   `PHASE PRE-G9B-MAINT-B5A` + `INTEGRATION` (D-V), y el registro de la fase en el catálogo de
   verificación dentro del propio incremento.
3. Antes de cambiar código, una medición de base: rutas condicionadas admitidas hoy en los
   modelos del autor (`w_2`, `a_3`, `trHole`) y en las baterías post-R1, con su coste actual en
   evaluaciones, para comparar después con las evaluaciones de la comprobación.
4. Valores finales dentro de los intervalos de §28.3.1, fijados por pruebas focales y
   registrados en la política y el informe; un valor fuera exige enmienda nueva y detención.
5. Paradas: una ruta admitida hoy deja de admitirse sin firma de salto o por agotamiento del
   presupuesto; la comprobación exige tocar el recorrido o su identidad de contenido; un fallo de
   `INTEGRATION` atribuible al cambio.
6. Criterio de aceptación del autor (plan C §1.6) y validación enumerada al final de §28.3.1.
7. Fuera de alcance: B5b, T-A, §28.2, `Intersect` y su política de aislamiento.

## 7. Condiciones de entrada de `MAINT-B2`

1. Decisión autoral que adopte §29.1 y autorización propia de `MAINT-B2` con base exacta; su
   prompt, revisado e incorporado en un paso de gobernanza autorizado.
2. Antes de codificar, el informe fija el punto exacto de la retirada para redefinición simple,
   en lote y revertida, y mide el coste aislado de la redefinición en el arnés post-R1.
3. Paradas: la retirada exige relajar una guarda de identidad, tratar la redefinición como
   borrar y crear o barrer al cargar; una reversión pierde el par; un resultado con nombre del
   usuario queda afectado.

## 8. Qué no decide

No adopta la enmienda, no autoriza `MAINT-B5a`, `MAINT-B2`, `MAINT-B1a` ni ningún otro
incremento, no decide D-B-1b ni T-A, no promueve a `main` ni publica. La aceptación técnica de
`STATIC` sobre el commit N1 es independiente de la decisión autoral.

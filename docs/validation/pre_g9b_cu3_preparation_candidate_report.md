# PRE-G9B-CU3 — Informe del candidato de preparación (expediente único)

| Campo | Valor |
|---|---|
| Carácter | Expediente candidato de la preparación arquitectónica, científica y normativa de `CU3`. Evidencia y propuestas, no autoridad normativa |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisado por la revisión científica focal (§11) y por la comprobación final de coherencia científica (§12), ambas de 2026-10-24; `selfApproved=false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Decisión autoral posterior | no se registra en este artefacto: el [registro de la aprobación del diseño](pre_g9b_cu3_author_design_approval_record.md) de 2026-10-24, sobre el commit `80d72a50` que contiene esta versión del informe, es su única autoridad |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24): **preparación autorizada, implementación no autorizada**; precisión autoral «Decisión sobre la planificación de CU3» (2026-10-24): distinguir los cuatro hechos de la correspondencia entre la procedencia de `K` y el parámetro del conductor, con fallo cerrado, e incorporarlo al diseño y a la matriz sin una autorización intermedia; instrucciones «Revisión científica focal del candidato de preparación» (§11) y «Comprobación final de coherencia científica» (§12), de la misma fecha |
| Base exacta | `main` = `e6c908f2203cd8feb5d4938013e16b212e0fe016`, árbol `fdb0e7027e19ee2b8fc9717f54d545e6ea50f6e1` (§1) |
| Rama | `phase/pre-g9b-cu3-preparation`, creada desde la base exacta. Publicada por instrucciones autorales de 2026-10-24: push normal de la rama nueva en `213ea5aa89ad093cbfbd8f83673d1d638afe076e` y push normal por avance rápido a `cbd316332ebfeb30aa6325fc835cb338b2b92b6b` (revisión focal), sin etiquetas, sin bypass y sin solicitud de cambios; `main` remoto en `e6c908f2` y las 73 referencias de etiqueta remotas (37 etiquetas) sin cambios. La instrucción de la comprobación final manda publicar su commit descendiente en la misma rama por avance rápido normal, sin modificar `main` |
| Commits de herramienta (sin código productivo) | `9e3a47355d2ef99687b5c0708666f55f550f753c` (sondeo), `7e483d0b8a7a1f8238edf7d117f81df88ea7d5dc` (identificadores de sesión normalizados), `aa5f2ef097e2706fa4d32657c859fd11c13c7c78` (lector actual, P10), `ec2b4dfa0bd77dad0686897cd559ea5003675165` (chorros de la fuente) |
| Commits documentales | `213ea5aa89ad093cbfbd8f83673d1d638afe076e`, la preparación (`STATIC` `verification-f5833518af5f462ab53f6866048fec12`, `ACCEPTED / COMPLETE` 3/3, re-autenticado); `cbd316332ebfeb30aa6325fc835cb338b2b92b6b`, la revisión focal (`STATIC` `verification-6bf3cce08a594963bd12ace91638b75c`, `ACCEPTED / COMPLETE` 3/3, re-autenticado; §12.5); y su descendiente con la comprobación final de coherencia, que contiene esta versión del informe. Un commit no puede citar su propia identidad, y la verificación `STATIC` del descendiente se comunica con la entrega |
| Clase | candidato documental con sondeos de solo lectura: `STATIC` y las comprobaciones de §7. No hay producto que verificar con `PHASE`, `INTEGRATION` ni `FINAL` |

## 1. Base, publicación de `CU2` y protección del repositorio

| Elemento | Valor |
|---|---|
| Identidad de la base | `HEAD`, `main`, `origin/main` y `git ls-remote origin refs/heads/main` iguales a `e6c908f2` antes de crear la rama; árbol de trabajo limpio |
| Publicación anterior | el cierre de `CU2` llegó a `main` por avance rápido exacto y sin forzar, `48a0775e` → `111d9d46` → `e6c908f2`. Las auditorías ignoradas `artifacts/agent/pre-g9b-cu2-closeout-publication-audit/` y `…-coherence-publication-audit/` (SHA-256 `cd0bf72f…` y `6fd9a793…`) registran en los dos pushes la salida «Bypassed rule violations for refs/heads/main: Changes must be made through a pull request. Cannot change this locked branch». Es la primera vez que un registro versionado nombra `e6c908f2` como `main` |
| Reglas de protección (consulta de solo lectura, 2026-10-24) | protección clásica de `main`: solicitud de cambios con una aprobación, revisión del propietario del código, descarte de revisiones antiguas, resolución de conversaciones, rama bloqueada (`lock_branch`), sin forzar, sin borrado, sin comprobaciones de estado obligatorias y `enforce_admins = false`. No hay rulesets (`gh api …/rulesets` devuelve `[]`) |
| Consecuencia | integrar en `main` como en `CU2` volvería a usar el bypass administrativo. Conforme a la instrucción §6, **no se usa como procedimiento ordinario**: la preparación solo publica la rama de revisión, por pushes normales que el autor autoriza expresamente (cabecera y D-CU3-17), y nunca `main` |
| Etiquetas | 73 referencias de etiqueta en el remoto (37 etiquetas y 36 desreferencias de etiquetas anotadas), sin cambios |

## 2. Entregables

| Entregable (§5 de la instrucción) | Documento |
|---|---|
| 1. Caracterización reproducible del estado inicial | [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md) §1, §2, §4 y §5; [evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json); [sondeo](../../tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1) |
| 2. Capacidades existentes y límites de `CU2` | [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md) §3 y §6 |
| 3. Diseño separado de las Etapas A y B | [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md) (los cuatro hechos); [Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md) |
| 4. Propuestas normativas y ADR | [propuesta normativa](../architecture/pre_g9b_cu3_normative_proposal.md) (A3.0 a A3.5); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| 5. Decisiones autorales pendientes | [hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md) (D-CU3-1 a D-CU3-19 tras la revisión focal y la comprobación final de coherencia, con la recomendación final en §7) |
| 6. Matriz de validación científica y dinámica | [matriz](pre_g9b_cu3_validation_matrix.md) |
| 7. Estrategia mínima de implementación y plan de verificación | [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §1–§4 |
| 8. Riesgos, degeneraciones y fuera de alcance | [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §5 y §6 |
| 9. Trazabilidad científica para E1 y `CU10` | [registro E1](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md), versión 6 (§6: SM-B-11, SM-C-12, SM-D-14, SM-E-12 y SM-E-13; filas SM-C-4, SM-C-7 y SM-D-11) |

## 3. Conclusiones principales

1. **El principio que pide el autor tiene una formulación precisa** (A3.0, precisada en la
   revisión focal):
   - los intervalos certifican la **existencia de alguna raíz en un intervalo**, por continuidad y
     cambio de signo certificado, y las desigualdades estrictas;
   - no deciden una **igualdad en un parámetro o en un punto prescrito** (una incidencia dada,
     `h(t*) = 0`, una raíz doble);
   - esa igualdad se establece por estructura o por aritmética exacta, que son evidencias
     distintas y se registran por separado.

   La F-2 original falla en eso: en los tres casos obligatorios, la envolvente de `h(t*)` en el
   punto crítico certificado **contiene el cero** (clase 3), así que la F-2 los habría
   «certificado» sin prueba.
2. **Los tres casos se reducen a un teorema del modelo, `J_1(K) = M_1`**, demostrado para las dos
   ramas de `H_1` (análisis §5.3) y comprobado con 80 cifras. No hay ninguna identidad sintáctica
   de términos: `p_1` usa `k` donde `m` usa `g`, el documento elige la rama reflejada
   `H_1 = 2I − K` y la igualdad procede de la reflexión respecto de `k`. Por eso la segunda
   incidencia necesita la **evaluación exacta** (M3) para ser autoritativa (D-CU3-4). M3 decide en
   la configuración de decisión `Σ_L⁺`, que contiene sin cambios la configuración capturada de
   `L`, de modo que certifica una propiedad del objeto CeDG autoritativo (§12.1). La comprobación
   con 80 cifras es una recalculación profunda y solo diagnóstica; en `Σ_L⁺` el teorema se apoya en
   la exactitud aritmética de las constantes capturadas (análisis §5.8), que confirma G-M3.
3. **Los cuatro hechos de la correspondencia** (precisión autoral) se cumplen en el modelo y se
   diseñan con un fallo cerrado propio:
   - `K` es la salida 2 de `AlgoIntersectLineConic(p, i)` y no es un punto sobre un camino;
   - el soporte de `E_1` es el mismo objeto `p`, con el mismo identificador persistente;
   - `t(K) = 0` exactamente, y el conductor en `0` reproduce `K` bit a bit;
   - `t(K)` está en una componente certificada `COMPLETE`.

   `K` no se convierte en conductor y el conductor vivo no se mueve.
4. **Precisión científica sobre B08 y B09** (H-CU3-7). `e_1` tiene pliegues certificados en
   `t = 0` y `t = π`: sus contactos con `n` y `c_1` son contactos en el extremo de un trazado
   plegado, a 58,4° y 31,6°, no tangencias. Solo `o_1·h_2` es una tangencia regular. Cambian el
   orden de contacto y el comportamiento ante perturbaciones: en un pliegue, dos preimágenes son
   un solo punto.
5. **La «raíz transversal de la costura» de `e_1·c_1` es un contacto con cero doble** en el
   pliegue de `t = π`. Su punto se conserva; su clasificación como transversal es un defecto, y
   el criterio de aceptación del plan debe reformularse (D-CU3-10). Corregir la clase exige el
   certificado `N = 1` doble que solo da M3: la clase 3 que miden los intervalos no la contradice
   (§12.2).
6. **El motor vigente acepta y clasifica raíces con valores binary64 sin envolver**, y eso da
   cuatro defectos preexistentes que contradicen §5.1 de la especificación aprobada (H-CU3-1 a
   H-CU3-4). Tras la revisión focal, la admisibilidad local de G8B-R1 **se conserva**:
   - una vía certificada aditiva resuelve H02;
   - una regla de conflicto identificada es la única restricción y trata H-CU3-1 a H-CU3-4: una
     solución verificada localmente solo se retira o se reclasifica ante un certificado de conteo
     que contradice realmente su existencia, su multiplicidad o su clasificación, y un punto
     crítico aislado no es una contradicción (D-CU3-6, D-CU3-13, §12.2).
7. **H06 no es un problema de residuo:** es un fallo de identidad durable
   (`GEO_NOT_SERIALIZABLE` del registro de identidad espacial). La propuesta es tratar los ejes
   como referencias reservadas (D-CU3-8).
8. **Compatibilidad.** El texto del contrato del adaptador de residuo forma parte de los tokens
   persistidos. La escala nueva se versiona en la política, no en el adaptador. Las soluciones
   contradichas por un certificado de conteo en documentos antiguos exigen la decisión D-CU3-7.
9. **Ninguna tolerancia semántica depende de la vista**, ni hoy (P9) ni en el diseño.

## 4. Hallazgos

Detalle y evidencia en el [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md)
§4 y §5.4b. Todos son preexistentes en `e6c908f2` y ninguno se ha corregido.

| Id | Resumen | Disposición propuesta |
|---|---|---|
| H-CU3-1 | raíces espurias del ruido de redondeo junto a contactos, algunas admisibles | corrección en `CU3` (D-CU3-13) |
| H-CU3-2 | contactos con cero doble (pliegue en la costura, tangencia en un extremo incluido) clasificados como transversales y admisibles | corrección en `CU3` (D-CU3-10, D-CU3-13) |
| H-CU3-3 | tangencias declaradas donde la ausencia de intersección está certificada | corrección en `CU3` (D-CU3-13) |
| H-CU3-4 | duplicados de una misma preimagen | corrección en `CU3` (D-CU3-13) |
| H-CU3-5 | H02 confirmado, también con traslaciones | `CU3` (D-CU3-6) |
| H-CU3-6 | causa de H06: identidad de los ejes | `CU3` (D-CU3-8) |
| H-CU3-7 | los contactos de `e_1` son contactos en pliegues, no tangencias | precisión del plan y de las normas candidatas (D-CU3-18) |

## 5. Decisiones que requieren intervención autoral

La [hoja](pre_g9b_cu3_author_decision_sheet.md) las agrupa en una sola revisión.

| Bloque | Decisiones | Recomendación resumida |
|---|---|---|
| Etapa A | D-CU3-1 a D-CU3-4 | `Point(L, D)` e `Intersect(L, r, D)`, el mismo punto de `L` en su configuración capturada, que es la autoridad; tabla de procedencia cerrada con selección exacta; correspondencia exacta o encerrada, separada del valor numérico; segunda incidencia por M1 ∪ M3 en la configuración de decisión `Σ_L⁺`, con `Σ_B` solo diagnóstico y la puerta temprana G-M3 |
| Etapa B | D-CU3-5, D-CU3-10, D-CU3-11 y D-CU3-18 | opción A: intersecciones solo de las clases con existencia establecida, con la evidencia de las demás conservada; objeto de punto crítico como opción futura; costura reformulada; germen tangente solo en `CU3-B`; precisión de los pliegues |
| Admisibilidad, H-2 y H-3 | D-CU3-6, D-CU3-7, D-CU3-8 y D-CU3-13 | G8B-R1 conservada; grados de evidencia y política de consumo; vía certificada aditiva para H02; regla de conflicto identificada, solo con certificados de conteo; compatibilidad (a); ejes como referencias reservadas |
| Alcance | D-CU3-9, D-CU3-12 y D-CU3-19 | componentes puntuales fuera de la materialización (`CU2` §15.4), sin C22 ni C23; fallo cerrado de los lectores anteriores con nota de migración; una autorización con los incrementos `CU3-A` y `CU3-B` y sus puertas |
| Proceso | D-CU3-14 a D-CU3-17 | `INTEGRATED_PHASE`; prompt con la autorización de implementación; H-1 pospuesta; publicación de la rama solo por instrucción autoral |

**Recomendación final y alcance exacto de la implementación propuesta:** la hoja de decisiones,
§7, resumida en §12.4.

## 6. Qué no se ha hecho

- No se ha modificado código productivo, pruebas, el registro, el inventario, la serialización,
  las especificaciones vigentes ni los ADR aceptados. El ADR 0039 es **PROPOSED** y las enmiendas
  A3.0 a A3.5 son candidatas.
- No se han tocado los contratos ni los registros de `CU0`, `CU1` y `CU2`.
- No se ha creado ningún comando ni herramienta pública. El sondeo es una herramienta de
  `tools/benchmark`, que se ejecuta fuera de los conjuntos de fuentes.
- No se ha creado el prompt de tarea de `CU3` (D-CU3-15). No se han tocado `.github/prompts/**`,
  `ai-shell/prompts/**`, `AGENTS.md` ni `CLAUDE.md`.
- No se ha iniciado `CU4`, `CU5`, `CU8-D` ni otra fase.
- No se ha tocado `main` ni se han creado etiquetas. Las únicas publicaciones son las de la rama
  de revisión, autorizadas expresamente (cabecera).
- No se ha modificado el modelo original: el sondeo trabaja sobre copias temporales.
- `geocedg_book` no se ha tocado.

## 7. Comprobaciones previas al commit de la preparación (`213ea5aa`)

Las de la revisión focal están en §11.4.

| Comprobación | Resultado |
|---|---|
| `git diff --check` (los once archivos nuevos como `intent-to-add`) | código 0 con la política canónica (`blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol`) y código 0 con la política por defecto de Git |
| Finales de línea | los once archivos nuevos y las líneas nuevas, en LF; el plan consolidado y el registro E1 siguen en LF; el roadmap conserva sus 2 873 líneas CRLF y sus 1 471 LF (edición byte a byte con un script del directorio temporal) |
| Enlaces relativos de los documentos nuevos y modificados | todos resuelven (0 rotos) |
| Identificadores cruzados | 18 `D-CU3-n`, 60 filas `V-…`, 7 `H-CU3-n`, 56 `SM-…` y 4 `GC3-n` citados; todos definidos |
| Nombres retirados de cierre | ninguno |
| `geocedg_book` | `HEAD` `78fbc88e9e12346b89d6ded411eaae94de2284b9`, árbol de trabajo limpio |
| Determinismo del sondeo | dos ejecuciones sobre `ec2b4dfa` con el árbol limpio: salida idéntica, SHA-256 `20af5cf471fc54622d63dec6ba69d9fe675587a48023a1faff654fd702e7d70f`, códigos de salida 0 y 0; árbol de compilación limpio tras cada ejecución |
| Árbol de producto | ningún archivo de `source/` modificado (`product_sources_modified = false` en los metadatos de las ejecuciones) |

El resultado del `STATIC` del commit documental se comunica con la entrega, porque este informe
no puede citar su propio commit.

## 8. Limitaciones

1. El teorema del modelo se ha deducido a mano. La comprobación con 80 cifras usa una frontera
   profunda (`{H, B, altura de h, rbCone, D_1}`), una configuración del tipo `Σ_B` que solo es
   diagnóstica, y los barridos lo comprueban en binary64 en muestras finitas. Su validez en
   `Σ_L⁺` se apoya en la lectura de la aritmética heredada y de la captura (análisis §5.8), con
   hipótesis que no se han medido (`z = 1` y los estados capturados). La evaluación exacta M3 no
   existe todavía: la decide la puerta temprana G-M3 de la implementación.
2. Las envolventes del sondeo usan coeficientes binary64 de los objetivos como constantes
   exactas y una composición de intervalos propia del sondeo, no del producto.
3. El pliegue de `e_1` lo certifica `CU2` por simetría exacta. Que la envolvente de `F′`
   contenga el cero no lo certifica.
4. El efecto de la aceptación certificada sobre las raíces espurias y los duplicados es una
   inferencia de las anchuras de envolvente medidas. Las filas V-B-NOISE-1 y V-B-DUP-1 lo exigen
   como prueba.
5. No se ha comprobado si un lector anterior aborta la carga completa de un documento con la
   sintaxis nueva (V-A-COMPAT-2).
6. El token `text1` del modelo es huérfano (su locus no existe en el documento). Ilustra la
   identidad de los tokens, pero no es un caso de regresión de un punto vivo.

## 9. Cuestiones abiertas

1. **Coste de M3.** La profundidad de la torre y el tamaño de los enteros fijarán su
   presupuesto (GC3-1). Si el coste fuera prohibitivo en el arrastre, el certificado se
   calcularía una vez por revisión, no por movimiento del conductor (GC3-3).
2. **Objeto de punto crítico** (opción B de D-CU3-5). Queda como opción futura, fuera de `CU3`.
   Su nombre, su icono y su ficha serían decisiones de superficie de esa autorización.
3. **Familias de objetivos de la Etapa B** fuera del alcance inicial (cónicas, gráficas,
   implícitas y pares): diferibles según el plan §6.
4. **C40** (seguimiento de raíces transversales) cambia de base con la aceptación certificada y
   debería reevaluarse después (D-CU3-9).
5. **Principio candidato A3.0 para el libro:** necesita una decisión autoral para entrar en §3
   del registro E1.
6. **Hipótesis aritméticas del análisis §5.8** (`z = 1` y los estados capturados): las comprueba
   la puerta G-M3 sobre los estados reales de la captura.

## 10. Estado de autorización

```text
PRE-G9B-CU0 … CU2            = PASS — AUTHOR APPROVED (contratos no reabiertos)
PRE-G9B-CU3 preparación      = CANDIDATO DOCUMENTAL (213ea5aa, publicado para revisión)
PRE-G9B-CU3 revisión focal   = EJECUTADA (cbd31633, publicada para revisión)
PRE-G9B-CU3 orientación      = ACEPTADA PROVISIONALMENTE EN LA INSTRUCCIÓN AUTORAL DE 2026-10-24
                               (no es la aprobación del diseño)
PRE-G9B-CU3 comprobación     = EJECUTADA (descendiente de cbd31633) — PENDIENTE DE REVISIÓN AUTORAL
  final de coherencia
PRE-G9B-CU3 diseño           = NO APROBADO (ADR 0039 PROPOSED; A3.0–A3.5 candidatas)
PRE-G9B-CU3 implementación   = NO AUTORIZADA
CU4 … CU10, CU8-D, G9B       = NO AUTORIZADOS
main / tags / releases       = SIN CAMBIOS
libro                        = SIN CAMBIOS
selfApproved                 = false
```

La siguiente decisión autoral es la recomendación final de la
[hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md) §7 (resumen en §12.4): la aprobación
del diseño (D-CU3-1 a D-CU3-19) y, en su caso, la autorización de la implementación con su alcance
mínimo, sus incrementos y sus puertas, sobre una base exacta (D-CU3-14). No se presupone ninguna
de las dos.

## 11. Revisión científica focal (2026-10-24)

Encargo: instrucción autoral «PRE-G9B-CU3 — Revisión científica focal del candidato de
preparación», sobre el candidato publicado `213ea5aa`: revisión focal autorizada, implementación
todavía no autorizada. No se repite la caracterización general ni se rehacen las propuestas. Se
actualizan solo los documentos candidatos afectados:

- los diseños de las Etapas A y B y el de la normalización;
- el análisis, con la evidencia nueva de §5.7;
- la propuesta normativa;
- el ADR 0039 propuesto;
- la matriz;
- la estrategia;
- la hoja de decisiones;
- este informe;
- la redacción de las filas candidatas del registro E1 que enunciaban el principio.

El plan consolidado y el roadmap no se tocan. La comprobación final de coherencia (§12) precisa
los puntos 2 y 3 de §11.1 (la configuración de decisión de M3 es `Σ_L⁺` y `Σ_B` solo es
diagnóstico) y la regla de conflicto de los puntos 1 y 5.

### 11.1 Resolución de las seis precisiones

| Punto de la instrucción | Resolución | Dónde |
|---|---|---|
| 1. Admisibilidad local y certificación rigurosa | Se contrastó D-CU3-6 con el contrato aprobado G8B-R1 (informe G8B-R1 §C y §D; aclaración del ADR 0008). La versión anterior **restringía** sin identificarlo la admisibilidad aprobada (aceptación solo certificada, y capacidades sin envolventes no admisibles): se retira. La propuesta revisada: **conserva** G8B-R1 y sus umbrales; distingue los grados `LOCALLY_VERIFIED` (verificación numérica bajo hipótesis), `EXISTENCE_CERTIFIED`, `EXISTENCE_STRUCTURAL` y `EXISTENCE_EXACT`; **amplía** con una vía certificada aditiva para H02; y **restringe** solo mediante una regla de conflicto identificada, cuyas consecuencias de compatibilidad se enumeran. Una política de consumo fija el grado mínimo por consumidor y prohíbe la promoción silenciosa | normalización, admisibilidad y ejes §2–§5; A3.1-R; D-CU3-6, D-CU3-7, D-CU3-13 |
| 2. Correspondencia exacta del conductor | Se distinguen la correspondencia **exacta** `t*(D)` en una configuración declarada (`Σ_L` o `Σ_B`), su **encierro** certificado desde un encierro del punto exacto (nunca desde las coordenadas binary64) y su **valor numérico** `t̃(D)`, que solo sirve para la posición mostrada. Garantías y fallo cerrado en extremos, costuras, bordes de componente, transiciones, bandas sin resolver, huecos y componentes puntuales. Ninguna incidencia se infiere de una proyección | Etapa A §2.3 y §2.4; A3.3-R; D-CU3-3; V-A-7 a V-A-18 |
| 3. Evaluación exacta M3 | Se mantiene como dirección arquitectónica candidata del núcleo. Hallazgo nuevo: `K.x = fl(B.x + 5) ≠ B.x + 5` (análisis §5.7), así que M3 debe recalcular el subgrafo conjunto desde una **frontera de captura común**, porque una captura objeto a objeto refutaría el teorema. `D*` se reconstruye desde su procedencia con la forma exacta de la regla de selección heredada de la clase v3, y sus coordenadas binary64 solo sirven para comprobar la correspondencia con el algoritmo vivo. Se **corrige** la afirmación sobre las igualdades accidentales: se demuestran si y solo si son exactas en la configuración declarada. Se define la **puerta temprana y bloqueante** G-M3, y sus alternativas si falla, sin residuos tolerados | Etapa A §6; Etapa B §4.2; A3.5; D-CU3-4; matriz §2.1 y V-M3-1 a V-M3-8 |
| 4. Alcance del punto crítico | Se recomienda la **opción A** de D-CU3-5 para `CU3`. El objeto público de punto crítico queda como opción futura, no incorporada. Las clases 3 a 5 se conservan como resultado rico y como diagnóstico. La fila de raíces pares de G8B-R1 se precisa (clases 1 y 2), sin impacto en el producto actual | Etapa B §5; A3.2-R; D-CU3-5 |
| 5. Precisión del principio A3.0 | Se distinguen: (a) la existencia de alguna raíz en un intervalo por continuidad y cambio de signo certificado, que los intervalos sí certifican; (b) la igualdad en un parámetro o punto prescrito, que no certifican; y (c) la igualdad estructural frente a la decidida por aritmética exacta. Se retira la formulación que presentaba (a) como imposible con intervalos | Etapa B §2; A3.0; ADR 0039, decisión 1; registro E1, SM-C-12 |
| 6. Componentes puntuales | Se concilia D-CU3-9 con `CU2` §15.4: una componente puntual existe aunque ningún binary64 alcance su parámetro, y no crea ningún punto público. Sin direccionamiento estructural demostrado, queda fuera de la materialización de `CU3` con el fallo cerrado `POINT_COMPONENT_NOT_MATERIALIZED`. No se introducen C22 ni C23. Se corrige la Etapa A, que admitía la pertenencia a «una componente puntual certificada» | Etapa A §2.4; Etapa B §7; D-CU3-9; V-A-15 |

Correcciones adicionales encontradas en la revisión:

- la Etapa B daba `p_1(E_1 := K) ≡ Line[K, g] ≡ m` como ejemplo de identidad de términos, y no lo
  es (`p_1` usa `k`), como ya decía el análisis §2;
- la recomendación de alcance se reorganiza en dos incrementos de una sola autorización
  (D-CU3-19).

### 11.2 Evidencia nueva

Una comprobación racional exacta con `System.Numerics.BigInteger` sobre los valores binary64 de
la evidencia versionada da:

- `K.x = fl(B.x + 5)`;
- la diferencia con `B.x + 5` exacto es de `4,44·10⁻¹⁶` (análisis §5.7).

Explica por qué la evidencia P3 (`0E-79`) recalculó `K` y `M` desde la frontera, y por qué M3 lo
exige. No se ha ejecutado ningún sondeo nuevo, y la evidencia versionada
(`20af5cf4…`) no cambia.

### 11.3 Recomendación consolidada

Una sola decisión autoral: aprobar el diseño (D-CU3-1 a D-CU3-19) y autorizar la implementación
con dos incrementos:

- `CU3-A`, el alcance mínimo, que cierra los casos obligatorios, la costura, H02, H06 y H-CU3-1 a
  H-CU3-4;
- `CU3-B`, la materialización sin conductor de las clases 1 y 2.

Las puertas son G-M3 (temprana y bloqueante), G-A, G-B y G-final, más la comprobación manual del
autor. El detalle está en la [hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md) §7 y en
la [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §1 y §2.

### 11.4 Comprobaciones de la revisión focal

| Comprobación | Resultado |
|---|---|
| `git diff --check` (11 archivos modificados) | código 0 con la política canónica y código 0 con la política por defecto de Git |
| Finales de línea | los 11 archivos, en LF; ninguna línea CRLF introducida |
| Enlaces relativos | todos resuelven (0 rotos) |
| Identificadores cruzados | 19 `D-CU3-n`, 74 filas `V-…`, 7 `H-CU3-n`, 56 `SM-…`, 3 `G-M3-n` y 5 `GC3-n` citados; todos definidos |
| Árbol de producto, registro, inventario, prompts, `AGENTS.md`, `CLAUDE.md`, plan, roadmap y libro | sin cambios |

El `STATIC` del commit de la revisión focal se registra en §12.5.

## 12. Comprobación final de coherencia científica (2026-10-24)

Encargo: instrucción autoral «PRE-G9B-CU3 — Comprobación final de coherencia científica», sobre el
candidato publicado `cbd31633`: precisión focal autorizada, implementación todavía no autorizada.
El autor acepta provisionalmente la orientación general de D-CU3-1 a D-CU3-19, los incrementos
`CU3-A` y `CU3-B`, la conservación de G8B-R1 y las puertas, y pide resolver dos extremos antes de
la autorización definitiva: la coherencia entre `Σ_L` y `Σ_B`, y la regla de conflicto de
evidencias. El objetivo es que M3 certifique una propiedad del objeto CeDG autoritativo, y no
solo de una reconstrucción algebraica cercana.

No se repiten los sondeos ni la revisión completa. Se actualizan solo los documentos candidatos y
las filas afectados:

- la Etapa A (§1, §2.3, §2.4, §3, §5, §6 y §9);
- la Etapa B (§2, §4.2, §8 y §9);
- la normalización (§3.3, §4 y §5);
- el análisis (§5.7, la fila H-CU3-2 y el nuevo §5.8);
- la propuesta normativa (A3.1-R, A3.3-R y A3.5);
- el ADR 0039 propuesto;
- la matriz (filas nuevas V-CFG-1 a V-CFG-6 y V-CONF-1 a V-CONF-8, y las filas afectadas);
- la estrategia (I-3, I-4, T2, §3 y riesgos);
- la hoja de decisiones (D-CU3-3, D-CU3-4, D-CU3-7, D-CU3-10, D-CU3-13, D-CU3-14, D-CU3-17,
  §6 y §7; las demás decisiones conservan su texto);
- este informe;
- la redacción de SM-B-11 y SM-C-12 del registro E1.

El plan consolidado y el roadmap no se tocan.

### 12.1 Coherencia entre `Σ_L` y `Σ_B`

| Pregunta de la instrucción | Resolución | Dónde |
|---|---|---|
| Objeto geométrico exacto que materializa `Intersect(L, r, D)` | el mismo punto `P = F(t*(D))` que `Point(L, D)`, en la configuración capturada `Σ_L` de `L`. `Intersect` no materializa un punto de otra configuración: solo añade la incidencia `P ∈ r`, demostrada para ese punto | Etapa A §3; A3.3-R; ADR 0039, decisión 4 |
| Autoridad geométrica | `L` tal como lo certifica `CU2`: su configuración capturada `Σ_L`, con las constantes independientes del conductor en su estado binary64 tomadas como racionales exactos (D-CU2-16) | Etapa A §2.3 |
| Relación demostrable entre los puntos exactos | `Σ_L ⊂ Σ_L⁺` literalmente: mismas constantes, mismos valores exactos y mismo programa de clase v3. Una decisión en `Σ_L⁺` es, por tanto, una propiedad del punto autoritativo `P_L`, sin transferencia. El punto `P_B` de la recalculación profunda `Σ_B` es en general **otro punto**: el radio de `c_1` es el de la matriz capturada en `Σ_L` y el exacto `|HD_1|` en `Σ_B`. Una cota certificada de `|P_L − P_B|` es información de precisión, nunca evidencia de incidencia | Etapa A §6.2 y §6.6; matriz §1 |
| Condiciones de transferencia | una demostración en `Σ_B` vale para `P_L` solo si se demuestra exactamente que `Σ_B` y `Σ_L⁺` asignan el mismo valor exacto a cada objeto que la demostración usa; entonces coinciden en ese subgrafo. El diseño no se apoya en la transferencia: decide en `Σ_L⁺` | Etapa A §6.6; A3.5 |
| Configuraciones distintas sin correspondencia demostrada | `Intersect(L, r, D)` queda indefinido: `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION` si M3 decide `h ≠ 0` en `Σ_L⁺`, o la causa de fuera de clase o de presupuesto. Si `Σ_B` se calculó como diagnóstico y allí la incidencia se demuestra, el inspector añade `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`. `Point(L, D)` sigue definido. Ninguna coincidencia numérica cambia el resultado | Etapa A §5 y §6.6 |
| Conservación del contrato de `CU2` | ninguna constante ni ningún certificado de `Σ_L` se modifica ni se reinterpreta, porque `Σ_L⁺` solo añade objetos, con reglas de una sola semántica por objeto. Las constantes conservan su valor y su representación de clase v3, también como objetivo; el soporte, su semántica de conductor; los ancestros compartidos y la frontera, su estado binary64; el resto se recalcula exactamente; un conflicto de representación falla cerrado. Hacer de la recalculación profunda la configuración capturada sería la enmienda C3 del contrato de `CU2`, fuera de `CU3` | Etapa A §6.2, §6.6 y §6.7 |
| Comprobación adversaria | V-CFG-1: `c = Circle(O, (1, 1))`, constante capturada con `r² = fl(fl(√2)²) = 2,0000000000000004`, frente al objetivo `Circle(O, (1, −1))`, que `Σ_L⁺` recalcula con `r² = 2`. La incidencia se demuestra en `Σ_B` y se refuta en `Σ_L⁺`; el residuo binary64, del orden de `10⁻¹⁶`, no se acepta. V-CFG-2 a V-CFG-6: la misma constante como objetivo, la copia de término idéntico (M1), la coincidencia de redondeo, el modelo del autor y el conflicto de representación | matriz §3.3b; G-M3-1 |

**Solución compatible con el contrato vigente.** Existe para los casos obligatorios, sin bloqueo
matemático:

- el teorema `J_1(K) = M_1` solo usa incidencias, paralelismos y perpendicularidades entre
  constantes, no valores métricos;
- la lectura de la aritmética heredada y de la captura (análisis §5.8) muestra que esas
  relaciones se cumplen exactamente en los estados binary64 capturados: productos por 0 y por ±1,
  copias de coordenadas y normalización por potencias de dos, con la hipótesis `z = 1`, que
  comprueba G-M3;
- así M3 puede decidir en `Σ_L⁺` los cinco contactos para el punto autoritativo.

**Discrepancia general y alternativas.** En otras construcciones, el redondeo de una constante
capturada puede romper una relación que la recalculación profunda conserva, por ejemplo el
coeficiente `z` redondeado de una paralela oblicua por un punto. Entonces la incidencia se refuta
en `Σ_L⁺` con fallo cerrado y el diagnóstico de `Σ_B`. Demostrarla para el objeto CeDG exigiría
cambiar qué es ese objeto: la alternativa C3, que enmienda el convenio de captura de `CU2`, con su
propia autorización y fuera de `CU3`. Si G-M3 fallara en los casos obligatorios, el autor elegiría
entre R-1, R-2, R-3 y C3; nunca un residuo ni una demostración en `Σ_B`.

**Evidencia anterior reclasificada.** P3 (`0E-79`, 80 cifras) recalculó también constantes de
`Σ_L` (el radio de `c_1` es `|HD_1|`). Es una configuración del tipo `Σ_B` y queda como evidencia
diagnóstica, no como demostración en `Σ_L⁺` (análisis §5.7; matriz §1).

### 12.2 Regla de conflicto de evidencias

| Exigencia de la instrucción | Resolución | Dónde |
|---|---|---|
| retirar o reclasificar solo ante una contradicción real de la existencia, la multiplicidad o la clasificación | la regla actúa solo con un **certificado de conteo** de la misma revisión, calculado en `Σ_L⁺`. Las contradicciones son de existencia (`N = 0`, o un intervalo aislante disjunto de todo lo que el certificado localiza), de conteo (fusión en una preimagen), de multiplicidad (`N = 1` doble donde se publicó una raíz simple) y de clasificación al alza (`N = 1` simple donde se publicó una tangente) | Etapa B §9.2; A3.1-R; ADR 0039, decisión 5; D-CU3-13 |
| un punto crítico aislado no es una contradicción | no lo es, ni lo es una clase 3, ni un certificado que no cumple sus condiciones. Una contradicción solo de la completitud del conjunto rebaja la completitud a `NOT_ESTABLISHED` sin retirar la solución, conforme a la ortogonalidad de G8B-R1 | Etapa B §9.2 |
| número de raíces y localización, con extremos y continuidad | (C1) continuidad: una sola componente continua certificada y la caja de clase v3 `VALID`, sin extremos de componente, costuras que terminen una componente, transiciones, bandas `UNRESOLVED`, huecos ni componentes puntuales, salvo un extremo incluido decidido exactamente; (C2) cuatro formas: `N = 0`, `N = 1` simple, `N = 2` simples a ambos lados de un punto crítico localizado y `N = 1` doble en un punto exacto; (C3) cálculo en `Σ_L⁺` | Etapa B §9.1 |
| pruebas adversarias | V-CONF-1 (ausencia), V-CONF-2 (una raíz transversal que se conserva junto a un punto crítico), V-CONF-3 (dos raíces localizadas alrededor de un punto crítico), y V-CONF-4 a V-CONF-8 (clase 3 sola, raíz doble exacta, continuidad y extremos, reclasificación al alza, contradicción solo del conjunto) | matriz §3.5b |

**Corrección.** La preparación afirmaba que la clase 3 de la costura bastaba para contradecir la
raíz transversal publicada. No basta, porque deja `N ∈ {0, 1 doble, 2}`. Con intervalos se
corrigen H-CU3-3 (`N = 0`) y H-CU3-1 y H-CU3-4 en los casos sintéticos (`N = 2` localizado). La
corrección de H-CU3-1 en el modelo y la de H-CU3-2 (la costura y el extremo del cuarto de arco)
exigen el certificado `N = 1` doble que solo da M3. Sin M3, esas raíces conservan su estado de
G8B-R1 con la evidencia de clase 3 visible (Etapa B §9.3; análisis, fila H-CU3-2).

De ahí siguen D-CU3-7 (qué cambia en un documento guardado, con M3 y sin ella), D-CU3-10 (la
inversión del pin de clasificación de la costura depende del certificado de M3) y las filas
V-B-2, V-B-3, V-B-4, V-B-11 y V-B-NOISE-1.

### 12.3 Evidencia nueva

No se ha ejecutado ningún sondeo. Lectura del código de la captura y de la aritmética heredada
(análisis §5.8):

- `CertifiedConstructionCapture2D`: las rectas constantes se capturan por sus coeficientes; las
  circunferencias constantes, por su matriz `(A2, A4, A5)` y su semieje, con la comprobación del
  centro; el soporte del conductor, por sus semiejes, sus vectores propios y su centro;
- `ConstructionBoxModel2D`: la clase v3 usa `r² = A4² + A5² − A2` para la geometría;
- `AlgoLinePointLine`, `AlgoOrthoLinePointLine` (con `GeoLine.getNormalizedCoefficients`, que solo
  escala por potencias de dos), `AlgoIntersectLines` y `GeoVec3D.cross`: productos por 0 y por ±1
  y copias de coordenadas, exactos en IEEE 754;
- `GeoQuadricND.setSphereNDMatrix`: `A2 = Σ m_i² − r²` redondeado, de modo que una circunferencia
  tiene dos representaciones del radio que pueden diferir (V-CFG-6).

Comprobación aritmética del caso adversario: `fl(√2) = 1,4142135623730951` y
`fl(fl(√2)²) = 2,0000000000000004 = 2 + 2⁻⁵¹ ≠ 2` (patrón binary64 `0x4000000000000001`). La
evidencia versionada del sondeo (`20af5cf4…`) no cambia.

### 12.4 Recomendación final

Una sola decisión autoral, detallada en la [hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md)
§7:

- **aprobar el diseño** con D-CU3-1 a D-CU3-19 y las dos precisiones:
  - la autoridad geométrica es `L` en su configuración capturada; `Intersect(L, r, D)` materializa
    el punto de `Point(L, D)`; M3 decide en `Σ_L⁺`, y `Σ_B` solo es diagnóstico;
  - la regla de conflicto solo actúa con certificados de conteo;
- **aceptar** el ADR 0039 y las enmiendas A3.0 a A3.5 para su incorporación en T0;
- **autorizar la implementación** sobre la base exacta de D-CU3-14 (el commit de esta comprobación),
  con `CU3-A` y, tras G-A, `CU3-B`;
- **puertas:** G-M3 (temprana y bloqueante, en `Σ_L⁺`, con V-CFG-1 y las hipótesis del análisis
  §5.8), G-A, G-B y G-final, más la comprobación manual del autor;
- **exclusiones:** las de la hoja §7, que añade la enmienda C3 del convenio de captura de `CU2`.

No hay ningún bloqueo matemático, y ninguna intervención intermedia es necesaria antes de esa
decisión.

### 12.5 Publicación anterior y comprobaciones

| Comprobación | Resultado |
|---|---|
| `STATIC` de la revisión focal (`cbd31633`, árbol `c93aee335762932205ecd5d18bba817a71784991`) | `verification-6bf3cce08a594963bd12ace91638b75c`, `ACCEPTED / COMPLETE` 3/3, re-autenticado (`result_hash` `fd917c49dd14695629d831ccad701ff61e0603dad5a17bb9123ea3adcc11c216`); diagnósticos permanentes: hallazgo de gobierno y consistencia histórica no disponible, como en la base |
| Publicación de `cbd31633` | push normal por avance rápido desde `213ea5aa`, autorizado el 2026-10-24, sin `main`, sin etiquetas y sin bypass; comprobado de nuevo con `git ls-remote` antes de este commit |
| `git diff --check` (11 archivos modificados) | código 0 con la política canónica y código 0 con la política por defecto de Git |
| Finales de línea | los 11 archivos, en LF; ninguna línea CRLF introducida |
| Enlaces relativos | todos resuelven (0 rotos) |
| Identificadores cruzados | 19 `D-CU3-n`, 88 filas `V-…`, 7 `H-CU3-n`, 56 `SM-…`, 3 `G-M3-n` y 5 `GC3-n` citados; todos definidos |
| Árbol de producto, registro, inventario, prompts, `AGENTS.md`, `CLAUDE.md`, plan, roadmap y libro | sin cambios |

El `STATIC` del commit de esta comprobación y su publicación se comunican con la entrega.

# PRE-G9B-CU3 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Decisiones que requieren intervención autoral para aprobar el diseño de `CU3` y, en su caso, autorizar su implementación. No registra ninguna decisión tomada por el agente |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Expediente | [informe del candidato de preparación](pre_g9b_cu3_preparation_candidate_report.md) |
| Sustituye | las decisiones F-1, F-2, H-2 y H-3 de la [hoja de `CU0`](pre_g9b_cu0_author_decision_sheet.md) §4, que se conservan como antecedente; H-1 sigue en esa hoja |

Cada decisión indica su recomendación (**R**). Las decisiones menores de organización y
documentación no se elevan.

## 1. Etapa A — puntos estructurales

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-1** | Formas públicas (F-1-R) | **A**: `Point(L, D)` e `Intersect(L, r, D)`, con `D` documentado como correspondencia del conductor y nunca como punto inicial. B: `Point(L, D)` y un comando propio para la incidencia. C: solo `Point(L, D)` | **A** ([Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md) §3) |
| **D-CU3-2** | Clases de procedencia de `D` (hecho 1) | tabla cerrada y versionada: `PATH_POINT`, `INTERSECTION_OUTPUT` (lista blanca de algoritmos heredados con el soporte entre sus entradas) y `DEFINING_POINT`; ampliable solo por una decisión posterior | adoptar la tabla (Etapa A §2.1) |
| **D-CU3-3** | Correspondencia `D ↦ t(D)` (hecho 3) | existencia, unicidad y orientación estructurales; valor por la proyección canónica del proveedor (vía a), con su cota de error para los dominios acotados; vía (b), mover el conductor, rechazada; vía (c), parámetros del algoritmo de origen, no recomendada | adoptar (Etapa A §2.3) |
| **D-CU3-4** | Autoridad de la segunda incidencia (hecho 4) | **M1 ∪ M3** (identidad de términos; evaluación exacta del corte en el punto exacto `D`); M2 no; M4 y M5 solo como evidencia. Sin M3, `Intersect(L, r, D)` **no** cubre los tres casos obligatorios, porque en el modelo del autor no hay identidad de términos (análisis §2) | **M1 ∪ M3**, con M3 como capacidad nueva del núcleo con presupuesto versionado y banco (Etapa A §6) |

## 2. Etapa B — evidencia de contacto

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-5** | Materialización (F-2-R, sustituye a F-2) | A: solo las clases 1 y 2 son intersecciones. **B**: A más un objeto distinto de «punto crítico certificado» que no afirma la incidencia. C: la clase 3 como intersección con un estado visible (**rechazada**: es la F-2 original) | **B**; A si se prefiere no ampliar ahora la superficie pública ([Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §5) |
| **D-CU3-10** | Raíz de la costura de `e_1·c_1` | el plan exige «la raíz transversal de la costura se conserva», y la línea base la fija como `TRANSVERSE_ESTABLISHED` y admisible; la caracterización prueba que es un contacto con un cero doble en el pliegue de `t = π` (H-CU3-2). Alternativas: **reformular** el criterio («el punto de contacto de la costura se conserva y se materializa como contacto estructural, con su clase corregida»); o conservarlo como transversal (**contrario a §5.1**) | **reformular**; el punto `preserve` no cambia, y el pin de clasificación se invierte |
| **D-CU3-11** | Germen tangente, alcance de la Etapa B y pliegues | germen `…/tangent-contact-germ/v1` y libro de tokens con un formato de estado nuevo; alcance inicial: recta, segmento, rayo y circunferencia; en un pliegue certificado, las dos preimágenes de un objetivo perturbado son **una sola** intersección geométrica, con una identidad para la pareja (órbita de la simetría certificada) o con una elección explícita del lado | germen y alcance como se proponen; en los pliegues, **una identidad para la pareja**; resto de familias diferibles (plan §6) |
| **D-CU3-18** | Precisión científica sobre B08 y B09 (H-CU3-7) | los contactos de `e_1` con `n` y con `c_1` (y los de la costura) son contactos en un pliegue del trazado, no tangencias; solo `o_1·h_2` es una tangencia | adoptar la precisión en el plan vivo y en las especificaciones candidatas |

## 3. H-2 y H-3

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-6** | Normalización y aceptación (H-2-R, sustituye a H-2) | aceptación por cambio de signo certificado más unicidad; residuo normalizado por la escala **S1** como control de consistencia; deduplicación por corchetes; pares sin la distancia al origen. Para las capacidades sin envolventes: **(i)** no admisibles, con su evidencia visible; o (ii) aceptación numérica con S1, marcada como verificación bajo hipótesis | **S1 con (i)** ([normalización y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md) §3) |
| **D-CU3-7** | Compatibilidad con los documentos anteriores (`AGENTS.md` §11.6) | **(a)** la política nueva se aplica a todos los documentos; los tokens de raíces mal clasificadas o espurias fallan cerrados con un diagnóstico que ofrece la forma estructural. (b) Los documentos anteriores conservan la política anterior hasta una migración explícita, con la política persistida en el documento (ADR 0013) | **(a)**, con nota de migración y la evidencia V-COMPAT-1 y V-COMPAT-2 |
| **D-CU3-8** | Ejes (H-3-R, sustituye a H-3) | **A**: `xAxis` e `yAxis` como referencias reservadas con identidad estable en el registro de identidad (enmienda acotada del ADR 0010). B: solo un mensaje tipado. C: sustituir el eje por una recta ordinaria (**rechazada**) | **A** (la causa es `GEO_NOT_SERIALIZABLE`, análisis §4) |
| **D-CU3-13** | Corrección de los defectos preexistentes H-CU3-1 a H-CU3-4 | incluirlos en `CU3` como conformidad con `locus-v2-intersections.md` §5.1, porque la corrección es la misma aceptación certificada de D-CU3-6; o registrarlos para un paquete posterior | **incluirlos** |

## 4. Alcance y relación con `CU2`

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-9** | C22, C23 y C40 | el paquete final de `CU2` §8 los llama «trabajo de `CU3`»; el registro de cierre §6 dice que el trabajo futuro «no son tareas de `CU3`». (a) Fuera de `CU3`: un punto de la Etapa A puede quedar definido sobre una componente puntual certificada, pero no se dibujan ni se materializan raíces en ellas. (b) Incluir la materialización en componentes puntuales y el seguimiento de raíces transversales de C40 | **(a)**; C40 se reevalúa tras la aceptación certificada, que cambia su base |
| **D-CU3-12** | Sintaxis nueva frente a lectores anteriores | aceptar el fallo cerrado de los lectores anteriores (P10), con nota de migración y la caracterización V-A-COMPAT-2 de la carga | aceptar |

## 5. Verificación, base, prompt y publicación

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-14** | Clase de verificación y base | `INTEGRATED_PHASE` (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU3`, `CU2`, `CU1` y `CU0`, `INTEGRATION`); base exacta: el commit de este candidato de preparación, o su descendiente si el autor pide cambios; `FINAL` en `CU10` | adoptar |
| **D-CU3-15** | Prompt canónico de tarea | crear `.github/prompts/tasks/pre-g9b-cu3-…prompt.md` con la aprobación del diseño, como primer paso documental de la implementación (CLAUDE.md impide crearlo como efecto secundario de otra tarea); o no crearlo | crearlo **con la autorización de implementación**, no antes |
| **D-CU3-16** | Perfil de esfuerzo (H-1) | sigue pospuesto; no condiciona `CU3` | confirmar |
| **D-CU3-17** | Publicación de la rama de preparación | la protección de `main` exige una solicitud de cambios con una aprobación y la revisión del propietario del código, bloquea la rama (`lock_branch`) y no fuerza la regla para los administradores; no hay rulesets. Alternativas: un push normal de la rama nueva `phase/pre-g9b-cu3-preparation`, sin `main`, sin etiquetas y sin bypass; o mantenerla local | push normal de la rama de revisión **si el autor lo autoriza expresamente**; ninguna integración en `main` con esta preparación |

## 6. Alcance exacto de la implementación propuesta

Si se adoptan las recomendaciones, la autorización de implementación cubriría:

- las piezas I-1 a I-7 de la [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §1;
- los pasos T0 a T11, con sus condiciones de parada (§2);
- la incorporación de A3.0 a A3.5 a sus especificaciones en T0;
- el ADR 0039 aceptado;
- la inversión de `pins.B08_B09` (reformulado), `pins.H02` y `pins.H06`;
- los tres casos obligatorios y la costura materializados con identidad y dinámica.

Quedarían fuera las fases posteriores, el libro y el trabajo futuro de `CU2`.

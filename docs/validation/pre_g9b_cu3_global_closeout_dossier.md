# PRE-G9B-CU3 — Expediente de cierre global

| Campo | Valor |
|---|---|
| Carácter | Expediente de auditoría previo al cierre global de `CU3`, pedido por la decisión autoral de 2026-10-28 («Aprobación de CU3-B2 y preparación del cierre global», §4 y §5). No aprueba `CU3` global; prepara una única decisión autoral |
| Estado | `CU3-A`, `CU3-B1` y `CU3-B2` `PASS — AUTHOR APPROVED`; `CU3` global **sin** `PASS — AUTHOR APPROVED`; nada publicado; `selfApproved=false` |
| Rama | `phase/pre-g9b-cu3-implementation`, local; la rama remota sigue en `b1813da1`; `main` = `e6c908f2` |

## 1. Identidad del candidato global

| Elemento | Identidad |
|---|---|
| Producto (código) | `2d13ad4c9b61d6f443b691b8214a8b2361ad0451` (árbol `64dca8e5138050c0e1c5fc6b659dd33ffa430728`), el de `CU3-B2` aprobado |
| Descendientes documentales | `0e93ff5d` (registro del candidato `CU3-B2`), `ba246ffc` (cierre de `CU3-B2`), `8f5c4f45` (`DOC-CU3-CU9-STRUCTURAL-POINTS`) y el commit de este expediente. Ninguno toca código; `8f5c4f45` cambia la guía de usuario, que el producto empaqueta como recurso |
| Base aprobada | `main` = `e6c908f2` (`CU0` a `CU2` `PASS — AUTHOR APPROVED`) |

**Punto de decisión (§7, D-CU3G-2).** La guía empaquetada cambió después de la campaña integrada de
`2d13ad4c`. La guía se verificó con sus pruebas focales (113 pruebas, 0 fallos) y `STATIC`; ninguna
ejecución integrada cubre todavía el árbol con la guía nueva.

## 2. Matriz de requisitos y evidencias

| Requisito | Estado | Evidencia |
|---|---|---|
| `CU3-A` `PASS — AUTHOR APPROVED` | cumplido | [cierre de `CU3-A`](pre_g9b_cu3_a_closeout_record.md), `f61f2954`, 2026-10-27 |
| `CU3-B1` `PASS — AUTHOR APPROVED` | cumplido | [cierre de `CU3-B1`](pre_g9b_cu3_b1_closeout_record.md), `dd3a2676` + `aabfd0b9`, 2026-10-28 |
| `CU3-B2` `PASS — AUTHOR APPROVED` | cumplido | [cierre de `CU3-B2`](pre_g9b_cu3_b2_closeout_record.md), `2d13ad4c`, 2026-10-28 |
| Etapa A (`Point(L, D)`, `Intersect(L, r, D)` con la segunda incidencia decidida) | cumplido en su alcance | `CU3-A`; [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md); `locus-v2-public-surface.md` §21 |
| Etapa B (contactos certificados sin conductor) | cumplido en su alcance: pliegues (`CU3-B1`) y tangencias regulares de clase 1 o 2 (`CU3-B2`) | cierres de `CU3-B1` y `CU3-B2`; `locus-v2-intersections.md` 1.3 a 1.6 |
| Tres casos obligatorios (`e_1·n`, `e_1·c_1` en `0` y en la costura, `o_1·h_2`) | cumplido | con conductor: `CU3-A` (cinco contactos `STRICT`, [informe del candidato](pre_g9b_cu3_implementation_candidate_report.md) §4); sin conductor: anclas de pliegue de `e_1` (`CU3-B1`) y ancla regular de `o_1·h_2` (`CU3-B2`); costura reclasificada como contacto en el pliegue de `π` (D-CU3-10) |
| H02 (normalización de residuos) | cumplido con la limitación registrada | `pins.H02` invertido en E1 ([registro de verificación](pre_g9b_cu3_verification_record.md)); límite: traslación `10⁶` con `r ≤ 20` sigue perdiendo una raíz (informe del candidato, limitación 2) |
| H06 (ejes) | cumplido | `pins.H06` invertido en E1 (registro de verificación) |
| Incidencia decidida contra la autoridad del objetivo (G-M3-4) | cumplido bajo la autoridad constructiva | `locus-v2-intersections.md` 1.4 y 1.5; certificado de identidad constructiva; ADR 0040 |
| Contactos en pliegues | cumplido | `CU3-B1` |
| Tangencias regulares | cumplido para los certificados aceptados; producción actual limitada a `o_1·h_2` | `CU3-B2`; `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` |
| Identidad durable, latencia y barreras | cumplido | ADR 0017 (y nota de 2026-10-28), ADR 0039; pruebas de `CU3-B1` (b1x2 a b1x16) y `CU3-B2` (germen y selector) |
| Persistencia v6 | cumplido | discriminador `P`/`S`/`F`/`T`; rechazo cerrado de lectores anteriores; corrección K6 (reapertura idempotente) |
| Compatibilidad de documentos heredados | cumplido | política heredada sin anclas y libro v4 sin cambios (b1x12, k6x1, b2x1 heredado); corpus del autor idéntico fuera de los contactos |
| Adopción explícita de la autoridad constructiva | cumplido | `AuthorityPolicyAdoption`; diálogo en los smokes; Deshacer de la adopción (smokes de `CU3-B1` y `CU3-B2`, paso 6) |
| Verificaciones integradas | cumplido por incremento | `INTEGRATED_PHASE` autenticada de `f61f2954`, `aabfd0b9` y `2d13ad4c` (con el rechazo 18/19 y la reejecución 19/19 de este último) |
| Smokes autorales | cumplido | `CU3-A` funcional (2026-10-26/27), `CU3-B1` `AUTHOR SMOKE — PASS`, `CU3-B2` `AUTHOR SMOKE — PASS` |
| Rendimiento | aceptado por incremento | `CU3-A` tras T1 a T7-a; `CU3-B1` y `CU3-B2` equivalentes a su predecesor (registro del candidato `CU3-B2` §4) |
| `DOC-CU3-CU9-STRUCTURAL-POINTS` | resuelto | guía §8.6 y §16.2 en las dos ediciones; dos entradas `COVERED` en la matriz de cobertura (plan §2.29) |

## 3. Alcance funcional demostrado

- **Puntos estructurales** con conductor sobre los soportes admitidos (circunferencias y segmentos),
  con la segunda incidencia probada o refutada exactamente y causas tipadas de fallo.
- **Contactos sin conductor** en pliegues certificados de `CU2` y tangencias regulares de clase 1 o 2,
  con identidad durable, latencia, reactivación y persistencia v6.
- **Autoridad constructiva** adoptada explícitamente por documento; la política heredada no cambia.
- **No demostrado:** la transición dinámica tangencia ↔ pareja y la ausencia certificada en tangencias
  regulares (K1, K2); la barrera por redefinición del soporte del conductor, sin camino en el núcleo
  (K5).

## 4. Limitaciones, deudas y observaciones

| Id | Disposición |
|---|---|
| `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` | no bloqueante; después de `CU10` y del trabajo editorial del libro |
| `DEBT-CU3B1-LEDGER-V6-HIGHWATER-IDEMPOTENCE` | **resuelta** por la corrección K6 de `CU3-B2` (reapertura sin reservas, contador estable) |
| `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` | reclasificada como camino inexistente (K5); se reabre si `CU7` admite esa redefinición |
| `OBS-CU3B2-SILENT-DRIVER-REDEFINITION-REFUSAL` | rechazo silencioso de la redefinición del conductor; para `CU7` |
| `OBS-DESKTOP-OFF-EDT-TOOLBAR-MAXSIZE-RACE` | carrera previa de escritorio; para `CU5` o `CU6` |
| `OBS-CU3A-XMLEDIT-01` | abierta, no bloqueante ([cierre de `CU3-A`](pre_g9b_cu3_a_closeout_record.md) §5) |
| H02 en traslaciones grandes | limitación registrada; corregirla exige un suelo de posición prohibido |
| Clase constructiva | arcos y conductores que no son circunferencias ni segmentos: `UNSUPPORTED_DRIVER_FAMILY` |
| Objetivos limitados y cónicas no circulares en `CU3-B2` | sin ancla regular |
| Ayuda de sintaxis de la aplicación | `Point.Syntax` e `Intersect.Syntax` no listan aún las formas con conductor (la guía sí); para `CU9` |

## 5. Estado de la documentación

- Normativa vigente: `locus-v2-intersections.md` 1.6, `locus-v2-public-surface.md` §21,
  `locus-v2-point-interaction.md` §18, ADR 0017 (nota), ADR 0039 (enmiendas), ADR 0040.
- Guía de usuario: complemento breve de puntos estructurales; manual completo en `CU9`.
- Registros de cierre y espejos JSON de `CU3-A`, `CU3-B1` y `CU3-B2`; plan §2.29; roadmap.

## 6. Propuesta de aprobación y cierre

Se propone `CU3 — PASS — AUTHOR APPROVED` sobre el producto `2d13ad4c` con sus descendientes
documentales, con las limitaciones y deudas del §4, sin reabrir trabajos científicos verificados.

## 7. Decisiones autorales para el cierre

| Id | Decisión | Recomendación |
|---|---|---|
| D-CU3G-1 | aprobar `CU3` global en los términos del §6 | aprobar |
| D-CU3G-2 | verificación del árbol con la guía nueva | aceptar la verificación focal de la guía y `STATIC` para el cierre de `CU3`, y dejar la verificación integrada del árbol final a `CU10` (`FINAL`); alternativa: una `INTEGRATION` sobre el commit de cierre antes de aprobar |
| D-CU3G-3 | publicación | autorizar, si procede, la publicación de la rama y el avance de `main` por un paso aparte, con identidad exacta |
| D-CU3G-4 | próxima fase | elegir entre las elegibles del §8 |

## 8. Próxima fase elegible según el plan vigente

Según la secuencia del plan (§5), tras `CU3` la línea principal sigue con `CU7-D` (diseño de M08 y
M09) y después `CU7-I`. `CU4`, `CU5` y `CU8-D` son elegibles en paralelo desde `CU0`. Ninguna está
autorizada; `CU9` y `CU10` cierran el track.

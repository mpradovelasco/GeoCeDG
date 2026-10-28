# PRE-G9B-CU3 — Registro de cierre global

| Campo | Valor |
|---|---|
| Carácter | Registro del cierre global de `CU3`, efectivo por la decisión autoral de 2026-10-28 (§0). Es la única autoridad de ese estado |
| Autoridad | decisión autoral «Autorización autoral condicionada de cierre global» (2026-10-28) |
| Estado | `PRE-G9B-CU3` `PASS — AUTHOR APPROVED`; `CU4` a `CU10` no autorizados; nada publicado |
| Expediente | [expediente de cierre global](pre_g9b_cu3_global_closeout_dossier.md) (`fa7896f3`) |
| Espejo | [`pre-g9b-cu3-global-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-global-closeout.json) |

## 0. Decisión autoral y comprobación de sus condiciones

La autorización exige que D-CU3G-1, D-CU3G-3 y D-CU3G-4 se limiten a confirmar el alcance, reconocer
limitaciones y deudas ya aceptadas y fijar la siguiente fase según el roadmap, sin decisiones
normativas nuevas, incumplimientos de requisitos obligatorios ni ampliaciones de alcance.

| Decisión | Contenido del expediente | Aplicación |
|---|---|---|
| D-CU3G-1 | aprobar `CU3` global en los términos del §6 del expediente | aplicada: confirma el alcance implementado y las limitaciones y deudas del §4, ya aceptadas en los cierres de `CU3-A`, `CU3-B1` y `CU3-B2` |
| D-CU3G-2 | verificación del árbol con la guía nueva | aplicada por decisión expresa del autor: la `INTEGRATION` sobre el árbol documental se difiere a la verificación `FINAL` de `CU10` (§2) |
| D-CU3G-3 | «autorizar, si procede, la publicación de la rama y el avance de `main` por un paso aparte, con identidad exacta» | aplicada sin publicar: no contiene ninguna decisión normativa ni ampliación; la publicación **no** se autoriza y queda como decisión autoral aparte |
| D-CU3G-4 | elegir la próxima fase entre las elegibles | aplicada según el plan (§5 de la secuencia): la línea principal sigue con `CU7-D`; `CU4`, `CU5` y `CU8-D` son elegibles en paralelo; ninguna se autoriza ni se inicia |

Ninguna de las tres contiene una decisión normativa nueva, un incumplimiento de un requisito obligatorio ni
una ampliación de alcance: la condición de la autorización se cumple.

## 1. Identidades

| Elemento | Identidad |
|---|---|
| Producto aprobado (código) | `2d13ad4c9b61d6f443b691b8214a8b2361ad0451`, árbol `64dca8e5138050c0e1c5fc6b659dd33ffa430728` |
| Incrementos | `CU3-A` sobre `f61f2954`; `CU3-B1` sobre `dd3a2676` + `aabfd0b9`; `CU3-B2` sobre `2d13ad4c` |
| Descendientes documentales | `0e93ff5d`, `ba246ffc`, `8f5c4f45` (guía y matriz), `fa7896f3` (expediente) y el commit de este registro |

## 2. Trazabilidad de las verificaciones (D-CU3G-2)

| Árbol | Verificación ejecutada sobre él |
|---|---|
| `2d13ad4c` (producto) | `INTEGRATED_PHASE` completa y autenticada ([cierre de `CU3-B2`](pre_g9b_cu3_b2_closeout_record.md) §3), con la `INTEGRATION` `verification-8a6d1632…` rechazada 18/19 y la reejecución `verification-b5021e24…` aceptada 19/19 |
| `8f5c4f45` (guía y matriz) | pruebas focales de la guía, la matriz y la paridad bilingüe: 113 pruebas, 0 fallos (no es una ejecución del verificador) |
| `fa7896f3` (expediente) | `STATIC` `verification-7cc3beffc795467ab9755e34c5849d86` `ACCEPTED / COMPLETE` |
| commit de este registro | `STATIC` (se informa en el commit siguiente o en el informe final) |

Ninguna campaña integrada se ha ejecutado sobre los árboles documentales y no se les atribuye. La guía de
usuario, que el producto empaqueta, cambió en `8f5c4f45`: la comprobación de su integración y su
empaquetado en el árbol final queda como compromiso de `CU10` (`FINAL`).

## 3. Compromisos, deudas y observaciones que se conservan

| Id | Disposición |
|---|---|
| `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` | no bloqueante; después de `CU10` y del trabajo editorial del libro |
| `CU10-GUIDE-INTEGRATION-CHECK` | compromiso: verificar en `FINAL` de `CU10` la integración y el empaquetado de la guía cambiada en `8f5c4f45` |
| `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` | camino inexistente (K5); se reabre si `CU7` admite esa redefinición |
| `OBS-CU3B2-SILENT-DRIVER-REDEFINITION-REFUSAL` | para `CU7` |
| `OBS-DESKTOP-OFF-EDT-TOOLBAR-MAXSIZE-RACE` | para `CU5` o `CU6` |
| `OBS-CU3A-XMLEDIT-01` | abierta, no bloqueante |
| Ayuda de sintaxis de la aplicación sin las formas con conductor | para `CU9` |
| H02 en traslaciones grandes; clase constructiva sin arcos; objetivos limitados y cónicas no circulares en `CU3-B2` | limitaciones registradas |

`DEBT-CU3B1-LEDGER-V6-HIGHWATER-IDEMPOTENCE` quedó resuelta por la corrección K6 de `CU3-B2`.

## 4. Resultado

**`PRE-G9B-CU3` `PASS — AUTHOR APPROVED`**, sobre el producto `2d13ad4c` y sus descendientes documentales,
por la decisión autoral de 2026-10-28, con el alcance, las limitaciones y las deudas del expediente. No se
publica nada: la rama remota sigue en `b1813da1`, `main` en `e6c908f2` y no se crean etiquetas. La próxima
puerta elegible es `CU7-D`, no autorizada.

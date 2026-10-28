# PRE-G9B-CU3 — Informe breve de preparación de `CU3-B2`: tangencias regulares

| Campo | Valor |
|---|---|
| Carácter | Informe de preparación pedido por la decisión autoral de 2026-10-28. No autoriza implementar `CU3-B2` ni modificar código productivo |
| Base | `CU3-A` y `CU3-B1` `PASS — AUTHOR APPROVED` ([cierre de `CU3-B1`](../validation/pre_g9b_cu3_b1_closeout_record.md)); R1–R6 aprobadas ([hoja de decisiones](pre_g9b_cu3_b_decision_sheet.md)) |
| Estado | `CU3-B2` no autorizado; `selfApproved=false` |

## 1. Alcance

`Intersect(L, r)` sin conductor publicaría, con identidad durable y token admisible, los contactos certificados de clase 1 o 2 que **no** están en un pliegue: tangencias regulares de `L` con `r`, con `h'(t₀) = 0` y `h''(t₀) ≠ 0`, y sin simetría que las empareje.

- **Caso obligatorio:** `o_1·h_2` del modelo del autor (`u_1`), hoy `EXISTENCE_EXACT`, orden `TWO`, sin token admisible.
- **Fuera de alcance:**
  - los contactos en pliegues, que ya cubre `CU3-B1`;
  - los contactos de orden superior;
  - los extremos incluidos de un dominio;
  - los documentos bajo la política heredada.

## 2. Diferencia de fondo con `CU3-B1`

- En un pliegue, las dos raíces de una pareja son preimágenes **de un mismo punto**: una identidad es natural.
- En una tangencia regular, al separarse el objetivo aparecen **dos puntos distintos** del plano, o ninguno. Una sola identidad de contacto no puede continuar en las dos raíces sin elegir lado. La regla vigente lo prohíbe (sin proximidad ni elección silenciosa).
- Hace falta, por tanto, una regla de ciclo de vida nueva: `CONTACT_SPLIT` / `CONTACT_LOST`, ya nombrados en el diseño de la Etapa B.

## 3. Puertas previstas

| Puerta | Contenido | Condición de superación |
|---|---|---|
| G-B2-0: diseño | germen `pre-g9b-cu3/regular-tangent-germ/v1` (linaje de la componente, objetivo y **lado del contacto**, Etapa B §6.1); ancla exacta de la tangencia sin rango; ciclo de vida contacto → división (dos transversales nuevas, el token de contacto latente) → reunión (reactivación); entrada `regular-tangent` del libro (versión 6 o 7) | diseño aprobado sin contradicciones con ADR 0017, 0039 y 0040 |
| G-B2-1: certificación | prueba rigurosa de que el contacto es una tangencia regular aislada: decisión exacta de `h = 0` y `h' = 0` en el punto, signo de `h''`, y ninguna otra raíz en la caja | decisión exacta (clase 1 o 2) sin `binary64` ni tolerancias |
| G-B2-2: identidad del lado | definición exacta del «lado del contacto» (signo de `h''` respecto de la orientación canónica del objetivo), estable en las revisiones y bajo Deshacer | prueba de que el lado no depende de la vista ni del orden de las raíces |
| G-B2-3: matriz | filas como B1-1 a B1-18, adaptadas a la división en dos raíces distintas, más la reunión y el cambio de lado | matriz aprobada antes del código |
| G-B2-4: verificación | `INTEGRATED_PHASE`, rendimiento y smoke autoral | `ACCEPTED / COMPLETE` y `AUTHOR SMOKE — PASS` |

## 4. Dependencias pendientes

- **Decisiones exactas en tangencias regulares.** Las da hoy `T7-a` en los contactos estructurales del autor (`u_1`). No hay evidencia de que lleguen a contactos con objetivos libres. Hay que caracterizar su alcance antes del diseño.
- **Las dos cuestiones no bloqueantes de `CU3-B1`:**
  - la idempotencia del contador `nextIncarnation` en la versión 6, que `CU3-B2` heredaría si usa el mismo libro;
  - la barrera de redefinición del soporte del conductor (ADR 0017), que convendría demostrar antes de ampliar los selectores.
- **Rendimiento.** Una caja de certificación más por cada tangencia candidata; hay que medirlo en E3 y en el smoke.

## 5. Decisiones autorales necesarias para autorizar `CU3-B2`

1. Aceptar la regla de **división**: al pasar de tangencia a dos raíces, nacen dos identidades transversales nuevas y el token de contacto queda latente. La alternativa, heredarlo en una de ellas, contradice R4 y la prohibición de proximidad.
2. Fijar la **definición del lado** del contacto en el germen, o renunciar al lado y aceptar una sola identidad por tangencia y objetivo.
3. Decidir la **versión del libro**: entrada nueva en la versión 6 o versión 7.
4. Decidir si se **resuelven antes** las dos cuestiones no bloqueantes de `CU3-B1` o se aceptan como deuda heredada.
5. Autorizar, en su caso, la puerta G-B2-0 (solo diseño), con la clase de verificación `INTEGRATED_PHASE`.

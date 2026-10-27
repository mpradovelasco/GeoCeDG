# PRE-G9B-CU3 — Diseño de `CU3-B1`: contactos certificados en pliegues

| Campo | Valor |
|---|---|
| Carácter | Puerta normativa y arquitectónica de `CU3-B1` (instrucción autoral de 2026-10-27, §6.2). Diseño previo al código; no es una aprobación del resultado |
| Autoridad | decisiones R1–R6 aprobadas el 2026-10-27 ([hoja de decisiones](pre_g9b_cu3_b_decision_sheet.md)); [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md), decisión 3 enmendada; [diseño de la Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §6.1 enmendado; [revisión de identidad](pre_g9b_cu3_b_identity_architecture_review.md) P1–P3; ADR 0017 y ADR 0040 |
| Base | candidato `CU3-A` `f61f2954`, con su cierre preparado y en suspenso, sin `PASS — AUTHOR APPROVED`, hasta la disposición final de `OBS-CU3A-SMOKE-01` ([expediente de cierre](../validation/pre_g9b_cu3_a_closeout_record.md)) |
| Alcance | solo contactos en pliegues certificados. `CU3-B2` (tangencias regulares) no está autorizado |
| `selfApproved` | `false` |

## 1. Objeto y alcance

`Intersect(L, r)`, sin conductor, publica para cada pliegue certificado de una componente de `L` una
**solución anclada en el pliegue** con identidad durable y token admisible. Cubre:

- los contactos de `e_1` con `n`, `c_1` (en `t = 0` y en la costura) y `r` del modelo del autor;
- cualquier pliegue certificado de `CU2` bajo `constructive-authority/v1`.

Quedan fuera:

- la tangencia regular `o_1·h_2` y cualquier contacto sin pliegue (`CU3-B2`);
- los documentos bajo la política heredada.

## 2. Precondiciones de materialización

Una solución anclada en el pliegue existe en una revisión solo si se cumplen todas estas condiciones:

1. **Autoridad.** El documento está bajo `constructive-authority/v1`. Bajo la política heredada no
   hay contactos anclados: los resultados y el libro de tokens no cambian.
2. **Pliegue certificado.** La componente tiene un pliegue `LocusCertifiedFold2D` de `CU2` con una
   involución exacta `σ` de `fold-symmetry/v1` (`t ↦ −t` o `t ↦ π − t`) y su punto fijo `t₀`.
3. **Etiqueta exacta.** El encierro canónico de `t₀` contiene exactamente un punto fijo de `σ`.
   Los dos puntos fijos de una involución distan `π`, así que la etiqueta (involución, punto fijo)
   se decide de forma rigurosa, sin binary64 ni proximidad.
4. **Una forma certificada** sobre una caja `T` simétrica por `σ` y contenida en la componente:
   - **contacto:** forma `ONE_DOUBLE` en `t₀`, con la decisión exacta (E3 o T7-a);
   - **pareja:** forma `TWO_SIMPLE` con raíces emparejadas por `σ` (cada una en la imagen por `σ`
     del corchete de la otra);
   - **sin raíz:** forma `NONE`.
5. **Sin ambigüedad.** Ninguna otra raíz certificada de la caja, ni ninguna otra solución, reclama
   el mismo ancla.

Si falta cualquiera de las condiciones 2 a 5, no hay ancla:

- las raíces siguen las reglas transversales vigentes, o quedan sin identidad con la causa
  `IDENTITY_DISCONTINUITY`;
- nunca se elige un lado en silencio.

## 3. Germen, selector e identidad

**Germen** (ADR 0039, decisión 3 enmendada; P3):

`pre-g9b-cu3/fold-contact-germ/v1/` + `framed(linaje de la componente)` +
`framed(etiqueta del pliegue)`, con la etiqueta = `framed(involución)` +
`framed(punto fijo ∈ {0, π, π/2, −π/2})`.

- **Linaje.** Es `LocusComponentLineage2D.identityKey` de la componente, el mismo que usa el germen
  transversal.
- **Objetivo.** Lo aporta el contrato de objetivo del libro, como en los tokens transversales.
- **Lo que nunca contiene:** clase de evidencia, orden, grado, integridad, certificado, celda,
  intervalos, valores de parámetro ni configuración de decisión.

**Selector.**

- Es el selector R4 vigente con el germen anclado como germen base.
- No lleva rango de fase: dos anclas de una componente difieren por su etiqueta.
- Se reconstruye exactamente en cada instantánea.
- Las barreras de ADR 0017, decisión 8 (redefinición del conductor, de su marco o de la componente)
  se aplican sin cambios.

**Token.**

- Un token anclado se acuña nuevo; nunca hereda el único token de la componente
  (`allowLegacySingletonBinding` queda desactivado para estas entradas).
- El mismo selector completo reactiva el mismo token latente (ADR 0017, decisión 13).

## 4. Ciclo de vida

| Estado en la revisión | Publicación | Token anclado |
|---|---|---|
| contacto (`ONE_DOUBLE`) o pareja (`TWO_SIMPLE` emparejada) | **una** solución, con la caja simétrica como intervalo aislante y el representante canónico del ancla como parámetro semántico; el estado «contacto» o «pareja» es evidencia de la revisión | actual |
| `NONE` certificado en la caja | ninguna solución en el ancla | latente, causa `CONTACT_LOST` |
| pliegue no certificado, etiqueta indecisa o emparejamiento ambiguo | las raíces siguen las reglas transversales o quedan sin identidad | latente, causa `IDENTITY_DISCONTINUITY` |
| la componente o el conductor se redefinen | barrera de ADR 0017 | latente, sin reasociación |

- **Transición pareja ↔ contacto.** Conserva el selector y el token, porque el ancla no cambia (R1).
- **Tokens transversales previos.** Los de las dos raíces de una pareja certificada que se acuñaron
  sin ancla (bajo la política heredada, o en una revisión sin el certificado) dejan de ser actuales
  con la causa `SUPERSEDED_BY_CERTIFIED_FOLD_PAIR`:
  - no se fusionan ni se redirigen, y su material no cambia;
  - los puntos materializados sobre ellos quedan indefinidos con esa causa y su reclamación queda
    latente;
  - se reactivan solo cuando vuelve a ser actual su mismo selector transversal completo, por
    ejemplo al deshacer la adopción (R4).
- **Admisibilidad.** Una solución anclada es admisible para el consumo puntual
  (`Intersect(L, r, token)`) en las revisiones en que es actual con evidencia de clase 1 o 2.
  - La integridad y el grado se publican aparte, como evidencia.
  - Que la clase pase de 1 a 2 conserva el token.
  - Las clases 3 a 5 dejan el token latente sin crear otro.

## 5. Persistencia: versión 6 del libro de tokens

- **Cuándo se escribe.** El estado del libro pasa a la versión 6 solo cuando contiene al menos una
  entrada anclada. Sin ellas, se siguen escribiendo las versiones 4 y 5 byte a byte.
- **Contenido.** Cada entrada de la versión 6 lleva un discriminador explícito del tipo de entrada
  (`transverse` o `fold-contact`). Una entrada `fold-contact` contiene el germen anclado y el
  linaje persistido; nunca la evidencia de la revisión.
- **Lectores.**
  - Un lector de esta versión rechaza cerrado un tipo de entrada desconocido.
  - Un lector anterior rechaza cerrado la versión 6 (versión desconocida, como hoy).
- **Copia y duplicación.** Siguen la regla de cierre fresco: identidades nuevas, ninguna reutilizada.
- **Deshacer, Rehacer y reapertura.** Restauran el estado del libro, incluidas las reclamaciones
  latentes.

## 6. Degeneraciones y discontinuidades

| Caso | Tratamiento |
|---|---|
| el pliegue deja de estar certificado (por ejemplo, un cambio de simetría del programa) | barrera: `IDENTITY_DISCONTINUITY`, token latente |
| la caja simétrica contiene otra raíz que la pareja | el certificado de conteo no da `TWO_SIMPLE` emparejada: sin ancla |
| pliegue en la costura (`π` o `−π`) | un mismo punto fijo canónico; la etiqueta es la misma a ambos lados |
| contacto de orden superior (`h''(t₀) = 0`) | la decisión exacta no da contacto: sin `ONE_DOUBLE`; sin ancla salvo que el certificado dé pareja o `NONE` |
| agotamiento de un presupuesto | fallo cerrado, nunca una decisión |
| extremo incluido de un dominio | fuera de `CU3-B1` (no es un pliegue de la clase de `CU2`) |

## 7. Compatibilidad

- **Intersecciones transversales sin pliegue:** sin cambios. Lo fija una prueba de tokens dorados.
- **Contactos de `CU3-A`:** no cambian. Los puntos estructurales `Intersect(L, r, D)` siguen como
  están.
- **Documentos heredados y documentos adoptados sin contactos:** su libro no cambia de versión.
- **Coherencia con los ADR.** ADR 0017 (rango, cuarentena y barreras), ADR 0039 (decisiones 2 y 5;
  decisión 3 enmendada) y ADR 0040 (solo bajo la autoridad constructiva; la adopción no cambia
  ningún token y solo deja de ser actuales, con causa tipada, los transversales de parejas
  certificadas).

## 8. Matriz de validación de `CU3-B1`

| Fila | Caso (instrucción §6.4) | Prueba prevista |
|---|---|---|
| B1-1 | una pareja de preimágenes con una sola identidad | réplica: `e_1·c_1` con `α_cone` modificado para que el objetivo cruce junto al pliegue en `t = 0` |
| B1-2 | pareja → raíz doble | el mismo caso, moviendo el objetivo hasta el contacto: mismo token |
| B1-3 | transición inversa | vuelta a la pareja: mismo token |
| B1-4 | desaparición y reaparición | `NONE` certificado y vuelta: token latente y reactivado |
| B1-5 | dos pliegues de una componente | `e_1·c_1` en `t = 0` y en la costura: tokens distintos sin rango |
| B1-6 | costura periódica | etiqueta igual en `π` y `−π` |
| B1-7 | pérdida del certificado de simetría | barrera `IDENTITY_DISCONTINUITY`, sin reasociación |
| B1-8 | ambigüedad | caja con otra raíz: sin ancla, fallo cerrado |
| B1-9 | tokens transversales previos | tokens heredados: `SUPERSEDED_BY_CERTIFIED_FOLD_PAIR`, latentes y restaurados al deshacer la adopción |
| B1-10 | Deshacer y Rehacer | estado del libro y reclamaciones |
| B1-11 | guardado y reapertura | versión 6 de ida y vuelta, mismos tokens |
| B1-12 | documentos heredados | sin anclas; libro sin cambio de versión |
| B1-13 | rechazo de la versión 6 por lectores anteriores | prueba de lector de la versión 5 |
| B1-14 | copia y duplicación | identidades nuevas |
| B1-15 | integridad y admisibilidad | `Intersect(L, r, token)` admisible solo con clase 1 o 2 actual |
| B1-16 | revisiones y modificaciones del DAG | redefinición de la componente o del conductor: barrera |
| B1-17 | contactos de `CU3-A` | los cinco contactos obligatorios sin cambios |
| B1-18 | intersecciones sin contacto | tokens dorados transversales sin cambios |

## 9. Verificación prevista

- **Candidato:** congelar un candidato `CU3-B1`.
- **Pruebas focales:** las de la matriz.
- **Campaña:** `INTEGRATED_PHASE` (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU3` a `CU0`,
  `INTEGRATION`), con las selecciones de inventario actualizadas.
- **Contratos:** los de identidad y persistencia se verifican expresamente.
- **Rendimiento:** caracterizar los movimientos que atraviesan la transición pareja ↔ contacto.
- **Smoke:** guía focal sobre el candidato.

## 10. Estado de la puerta

- **Contradicciones:** ninguna normativa sin resolver. Las enmiendas de R1–R5 están aplicadas
  (`24d851cb`).
- **Puntos que se fijan en la implementación sin cambiar este diseño:**
  - el nombre exacto de la causa `CONTACT_LOST` para los tokens anclados;
  - la ubicación del discriminador en el formato de estado del libro.

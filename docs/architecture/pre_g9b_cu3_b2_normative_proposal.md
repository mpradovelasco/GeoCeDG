# PRE-G9B-CU3 — Enmiendas normativas candidatas de `CU3-B2` (G-B2-0)

| Campo | Valor |
|---|---|
| Carácter | Texto candidato para revisión autoral. Las especificaciones y los ADR vigentes **no** se modifican; las enmiendas se aplicarían al inicio de G-B2-2, solo si se aprueban |
| Diseño | [diseño de `CU3-B2`](pre_g9b_cu3_b2_regular_tangent_design.md); [hoja de decisiones](../validation/pre_g9b_cu3_b2_decision_sheet.md) |
| `selfApproved` | `false` |

## A-B2-1. Especificación de intersecciones, sección CU3.3 (versión 1.6 candidata)

Sustituye el párrafo del germen `…/tangent-contact-germ/v1` (ya retirado por la enmienda de
2026-10-27) por este texto, en inglés como la especificación:

> **Regular tangent contacts (increment `CU3-B2`, only under `constructive-authority/v1`).** A
> regular tangent contact is an isolated root `t₀` of the canonically oriented indicator
> `h = F_r ∘ L` with `h(t₀) = h'(t₀) = 0` decided by structural (class 1) or exact (class 2)
> evidence, `sign h''(t₀)` certified away from zero, a count certificate `ONE_DOUBLE` in its box and
> no fold certificate at `t₀`.
>
> The side of contact is `σ = sign h''(t₀)` with the canonical target orientation of §6.2. It is the
> sign of the relative curvature `κ_L − κ_r` along the canonical normal. It is invariant under
> regular reparameterization of `L` (including orientation reversal), under legitimate nonzero
> scalar changes of the target equation, under equivalent indicators and under any view state.
>
> The selector is the stable component lineage plus the germ
> `pre-g9b-cu3/regular-tangent-germ/v1` (component lineage, target family and canonical
> orientation, `σ`). It contains no coordinate, parameter, index, contact order or evidence class.
> A repeated base selector is enriched by the phase and rank of ADR 0017.
>
> Transitions: the same selector under the preconditions is `CURRENT`; a certified `NONE` in the box
> is `CONTACT_LOST`; two certified transverse roots in the box is `CONTACT_SPLIT`; class 3 is
> `CONTACT_NOT_CERTIFIED`; loss of the certified sign of `h''`, a change of canonical target
> orientation or a lineage change is `IDENTITY_DISCONTINUITY`. In every non-current state the
> contact token is retained only when claimed, and is never transferred.
>
> **`CONTACT_SPLIT`.** The contact token is not transferred to either transverse root. Each
> transverse root keeps or acquires its own identity under the transverse germ and ADR 0017 only:
> a claimed transverse token is reactivated only when its same complete selector again resolves
> uniquely with current admissibility. Merge and split are identity barriers; no token moves to a
> root because it occupies the same rank.

## A-B2-2. Especificación de intersecciones, persistencia del libro de tokens

> Ledger state version 6 is written when the current or copy-source snapshot contains at least one
> contact entry (`F` fold contact or `T` regular tangent contact). Every v6 entry carries its
> explicit kind discriminator (`P`, `S`, `F` or `T`). A reader rejects an unknown kind closed. Older
> readers reject version 6 closed. A `T` entry stores the germ and the persisted lineage, never
> revision evidence.

(Si el autor elige v7 en D-B2-3, este texto se sustituye por una versión 7 que admite `T`, con v6
limitada a `F`.)

## A-B2-3. ADR 0039, decisión 3 (nota de enmienda)

> The regular tangent germ named in the 2026-10-27 amendment is defined by A-B2-1. Its side is the
> certified sign of the relative curvature with the canonical target orientation. A regular tangent
> contact token is never transferred at a split.

## A-B2-4. ADR 0017 (nota de aplicación, sin cambiar sus decisiones)

> Regular tangent contacts of `CU3-B2` are a new selector family under decisions 6 to 9 and 13.
> A regular tangency is the merge/split event of decision 8 for the transverse family, and the
> contact family has its own selector; neither family inherits the other's token.

## A-B2-5. Diseño de la Etapa B, §6.3 (aclaración)

La fila «clase 4 con dos raíces, en una tangencia regular» pasa a remitir a A-B2-1. La fila «en un
pliegue» no cambia.

## A-B2-6. Diagnósticos (catálogo candidato)

| Código candidato | Cuándo |
|---|---|
| `REGULAR_CONTACT_ANCHORED` | tangencia regular con ancla actual |
| `REGULAR_CONTACT_LOST` | `NONE` certificado en la caja de una tangencia anclada |
| `REGULAR_CONTACT_SPLIT` | dos raíces transversales certificadas en la caja |
| `IDENTITY_DISCONTINUITY` (motivo `CONTACT_SIDE_NOT_CERTIFIED`, `TARGET_ORIENTATION_CHANGED` o `COMPONENT_LINEAGE_CHANGED`) | barreras del diseño §3.3 |

Cada código nuevo necesita sus etiquetas en `menu*.properties` (lección de `CU3-B1`).

## A-B2-7. Precisiones tras G-B2-1 (2026-10-28; candidatas, no promovidas)

Por las precisiones autorales P1 y P2 y el [expediente G-B2-1](../validation/pre_g9b_cu3_b2_g1_characterization_record.md):

- **A-B2-1, precondiciones.** Se añade, tras «no fold certificate at `t₀`»:

  > The source is valid on the box, `t₀` is interior to its domain and the enclosure of `L'` excludes
  > the zero vector; the target is regular at the contact (`∇F_r ≠ 0`) and, when bounded, the contact
  > belongs to its bounded part, decided without tolerances; `L` and `F_r` are `C²` on the box. A double
  > root of the residual at a singular source is never a regular tangent contact.

- **A-B2-1, transiciones.** «a certified `NONE` in the box is `CONTACT_LOST`» se mantiene, y se añade:

  > Any other absence or undecided state, including an unresolved constructive configuration, is
  > `CONTACT_NOT_CERTIFIED`.

- **A-B2-2, persistencia.** Se añade:

  > Evaluations performed while a document is being loaded never reserve incarnations under a policy
  > other than the persisted document authority. Reopening and saving an unedited document leaves the
  > ledger state unchanged.

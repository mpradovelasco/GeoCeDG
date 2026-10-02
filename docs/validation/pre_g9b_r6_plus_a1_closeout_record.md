# PRE-G9B-R6-plus-A-1 closeout record

```text
PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE        = PASS
```

```text
selfApproved              = false
authorApproved            = true    (A-1 only)
passClaimed               = true    (A-1 only)
implementationAuthorized  = false   (A-2, B, D0, D1, C, E1, E2, E3, F1, F2, G, PRE-G9B-R7, G9B)

candidateFrozen           = true
candidateMutated          = false
SERIALIZATION CHANGE      = NONE
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-02 on
`PRE-G9B-R6-plus-A-1` and the exact identities behind it. It changes no
product, test, build, packaging, verifier, registry, schema, specification or
ADR file, and it does not modify the frozen candidate. Besides this record and
its machine-readable mirror,
[`pre-g9b-r6-plus-a1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a1-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_a1_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. They are not amended. This
record is the sole authority for the author approval of `A-1`.

## Author disposition

```text
PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED
T_R6PLUS_A1         = 2a71133ad622e18e9963b04a466a92665a784722
AUTHOR_SMOKE        = PASS
selfApproved        = false
```

The author instruction of 2026-10-02, confirmed directly by the author, approves
the frozen candidate with author smoke `PASS` and authorizes exclusively the
documentary closeout, promotion and publication of `A-1`.

## Approved identities

```text
P_R6PLUS_A1_PROMPT = 069c7a03da92f2c30f015815a0aa278e7356b356
                     tree 707c0b557ed93de3aaf4fea7d76bb643fb20411a
                     published A-1 preparation closeout; implementation base

A_R6PLUS_A1_PROMPT = bfee814235c1a6979b7016df1e6a60c5ffd5fb92
                     authorized canonical-prompt amendment (first tracked edit)

T_R6PLUS_A1        = 2a71133ad622e18e9963b04a466a92665a784722
                     tree e9bf297f4307b8ab027c64b6e791870ac1d55e93
                     parent bfee814235c1a6979b7016df1e6a60c5ffd5fb92
                     approved technical candidate; not amended

branch             = phase/pre-g9b-r6-plus-a1-layer-workspace (local; never pushed)
```

The identity of this closeout commit, `P_R6PLUS_A1`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `INTEGRATED_PHASE` required the registered PHASE selection
plus `INTEGRATION` on the same exact commit and tree.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-4a7c3d61fab944fb81112edffcd08cf8` — `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-A-1` | `2a71133a…` / `e9bf297f…` | exit 0; `ACCEPTED / COMPLETE`; 5/5 acceptance leaves; plan `70e86626c15048c462de5a80f3846266f07130dec7528db754965c16a0ac6319`; result `ef40697dbb43b04e70fae382150cad5a749573b7d32883de18e085ed532af481` |
| `verification-94c28552e125490c98da11b292f57412` — `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/pre-g9b-r6-plus-a1-integration-2a71133a` | `2a71133a…` / `e9bf297f…` | exit 0; `ACCEPTED / COMPLETE`; 19/19 acceptance leaves; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; result `67f597dfde00d4de0a76f1ad1e80bfc9dae52fad8e35fea54f4557845b1ecea9`; standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) |

**Invalid historical attempt.** The first INTEGRATION on the same commit and
tree, `verification-3cdf3c5491804a229568bc54bea3688b`, ended
`REJECTED_VERIFICATION_CORE / UNTRUSTED`. Its only non-satisfied leaf was
`packaging.repository-safety` (`EVIDENCE_UNTRUSTED`): its `-LogDirectory` lay
outside the repository, and `tools/agent/checks/repository-safety.ps1` requires
the packaging task output root to be a contained non-Git repository path. Every
semantic leaf of that attempt was satisfied. It is an invalid attempt caused by
an incorrect log-root configuration, not a product failure; it stays historical
evidence and is not acceptance evidence. The accepted rerun used the same plan
hash on the unchanged candidate.

`git diff --check` over `069c7a03…..2a71133a…`: exit 0. `FINAL` is not required
for this class; no receipt is expected or claimed. This closeout commit's own
`STATIC` run is reported with the publication.

## Clarification of the frozen prompt

The frozen canonical prompt
(`.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md`,
blob `237c8f303249f72e6cfd95624e1895e8c5675a77`) writes both acceptance
commands with `-LogDirectory <fresh root outside artifacts/>`. That wording is
incorrect for `INTEGRATION`. The prompt is not modified; this clarification
governs future readings of it:

```text
PHASE and STATIC:     -LogDirectory may be a fresh root outside the repository
INTEGRATION and FINAL: -LogDirectory must be a fresh path inside the repository,
                      artifacts/agent/<fresh>, as packaging.repository-safety requires
console log of every run: outside artifacts/ (for example the session scratchpad)
```

## Serialization

`SERIALIZATION CHANGE = NONE`. The layer domain stays `0..9`. The working layer
and the hidden-layer set are `SESSION` state: no document element, no XML
attribute, no undo-XML, preferences, macro or clipboard content.

## Observations

Residual observation recorded at author smoke:

| ID | Observation | Disposition |
|---|---|---|
| `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | The preview shown before a PNG export can display objects on hidden layers, while the final PNG export omits them correctly. | Not blocking for `A-1`; primary owner `PRE-G9B-R6-plus-B`; revalidate in `A-2` when hidden layers become persistent; not corrected by widening the scope of `A-1`. |

Deferred observations already recorded in the candidate report §9, unchanged:

| ID | Owner |
|---|---|
| `OBS-A1-GRAPHICS2-GEOCEDG-MODES` (GeoCeDG modes, including 141, do not act in the upstream Graphics 2 controller; pre-existing) | later frontend phase |
| `OBS-A1-BACKGROUND-IMAGE-LAYER` (background images are not hidden with their layer) | `B` / author |
| `OBS-A1-3D-VIEW` (hidden layers do not apply to the 3D view) | author |
| `OBS-A1-NON-HIT-SELECTION` (selection that is not hit testing can reach objects on hidden layers) | author |
| `OBS-A1-RECOMPUTE-OUTPUTS` (outputs created by a later recompute take the working layer) | none required |
| `OBS-A1-URL-OPEN` (the API-only `loadXML(URL)` route does not reset the session) | none required |

The temporary export inconsistency documented by the candidate (SVG until `B`;
LaTeX and DXF until `C`) is unchanged.

## Status updates in this closeout

- Roadmap 4.40: `PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED` in the last
  closed and last executed phase entries, the next-gate entry, the track
  paragraph and the `A-1` row; the next operational subphase is
  `PRE-G9B-R6-plus-B`, `NOT AUTHORIZED`.
- Mini-track plan: the `A-1` state in the header, the §3 table and §14.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_A1_PROMPT 069c7a03da92f2c30f015815a0aa278e7356b356
  -> A_R6PLUS_A1_PROMPT bfee814235c1a6979b7016df1e6a60c5ffd5fb92
  -> T_R6PLUS_A1      2a71133ad622e18e9963b04a466a92665a784722
  -> P_R6PLUS_A1      (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_A1_PROMPT` immediately
before the push. Forbidden: merge commits, rebases, squashes, amends, force
pushes, tags, releases, binary publication and any further implementation.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = NOT AUTHORIZED   (next operational subphase)
PRE-G9B-R6-plus-D0, D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

This authorization ends with the publication of `A-1`. `B` needs its own
explicit author instruction naming its exact base. No tag or release is
authorized.

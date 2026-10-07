# PRE-G9B-R6-plus-E1-X1 implementation authorization record

```text
PRE-G9B-R6-plus-E1-X1 = AUTHORIZED FOR IMPLEMENTATION
SELECTED DESIGN       = CANDIDATE C — PRIMARY LAUNCHER EARLY CLASSIC DISPATCH
VERIFICATION_CLASS    = BOUNDED_PHASE (frozen)
```

```text
selfApproved              = false
authorApproved            = false   (no technical candidate is approved by this record)
passClaimed               = false
implementationAuthorized  = true    (E1-X1 implementation and technical verification only;
                                     E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B: false)
productPhaseEffect        = NONE    (this record)
```

This record preserves the explicit author instruction of 2026-10-07, typed in
the session ("Autorizo la siguiente tarea"), that authorizes the implementation
and technical verification of the characterized correction of
`OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` under
`PRE-G9B-R6-plus-E1-X1`. It changes no product, test, build, packaging,
verifier, registry, schema, specification, ADR or serialization file. Its
machine-readable mirror is
[`pre-g9b-r6-plus-e1-x1-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-authorization.json).

## Entry verification

```text
live remote main = local main = origin/main
                 = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b   (P_R6PLUS_E1)
                   tree cc613c0337d3d75fdf14e55d20db5cb896af2b52
characterization = fdc1ade6 → b6172af4f319d297aa5780c9f516b310254d3e90 (E1-X1)
                            → b3c78e6d6564b0400d1fea7cda55bb480778716c (E1-X1-R1)
                   linear, no merge; neither commit amended
implementation base = b3c78e6d6564b0400d1fea7cda55bb480778716c
                      tree dca7daaa27f9aca392c8c84a80db61b369fc3d68
implementation branch = phase/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher (local)
worktree = clean
```

`E1-L`, `E1-P` and `E1` stay `PASS — AUTHOR APPROVED — PUBLISHED` and are not
reopened. The characterization history is preserved: `b6172af4` recommended
Candidate A on the evidence then available; `b3c78e6d` demonstrated and
recommended Candidate C. Candidate A remains characterization history only and
is not implemented.

## Author decisions

| Question | Disposition |
|---|---|
| `DQ-E1X1-1` identifier | `PRE-G9B-R6-plus-E1-X1`; registered phase `PRE-G9B-R6-PLUS-E1-X1` |
| `DQ-E1X1-2` design | Candidate C — primary launcher early Classic dispatch; the packaged child is the running launcher named by `jpackage.app-path`; no second executable or `.cfg` |
| `DQ-E1X1-3` marker | `--classic-diagnostic`, under the positional rule frozen by the reconciled prompt (first argument only); internal launcher protocol, not a public command or document feature |
| `DQ-E1X1-5` streams | `stdout = Redirect.DISCARD`, `stderr = Redirect.DISCARD`; no default pipes, no `inheritIO`, no persistent log file |
| `DQ-E1X1-6` development JVM options | the running JVM is the authority; only the `--add-exports`, `--add-opens` and `--enable-native-access` families are propagated; agents, heap sizing, harness options, arbitrary `-D` properties and unrelated flags are not; focused tests for the filter |
| `DQ-E1X1-7` class | `BOUNDED_PHASE`: one registered `PHASE` plus the focused, process and packaged technical-smoke evidence of the contract; no `FINAL`; `INTEGRATION` only after a `VERIFICATION_ESCALATION_REQUEST` |
| `DQ-E1X1-8` documentation and closure | the minimum `geocedg/specs/ui/application-profile.md` wording for the internal dispatch; no ADR change; the observation becomes resolved only with the author's approval after author smoke |

The instruction additionally fixes: packaged failure is explicit when
`jpackage.app-path` is invalid or missing, with no fallback to `java.home` and
no `PATH` search; the development route prefers `javaw.exe` with the
established fallback and is never used in a packaged execution; preference
isolation (`classic-diagnostic.properties`, `laboratory.properties`, never
`preferences.properties`); no document transfer, construction mutation,
saved-state change, workspace or profile switch, or GeoCeDG semantic authority
in the child; the Laboratory admission rule (`.ggb`/`.ggt`, existing file,
rejection before launch, diagnostic input only); `PACKAGING PRODUCT CHANGE =
NONE`; the required focused, process and packaged-smoke evidence; the stop
conditions; and the publication boundary (local commits only).

`DQ-E1X1-4` (dispatch guarantees for a caller of the marker) is not stated
separately by the instruction. Its §6 requires that only the marker is removed
and the remaining Classic arguments are preserved. The implementation therefore
applies the prepared recommendation in its narrowest compatible form: the
dispatch never removes or alters a caller argument, and adds `--showSplash=false`
and the isolated `classic-diagnostic.properties` only when the caller supplied
no splash or settings argument. The product routes always supply both, so they
are unaffected. This reading is recorded for author review; it is not an
author decision.

The one non-reproduced full-application XML difference of the R1
characterization is preserved as an observation and is not treated as a product
defect unless implementation testing reproduces it.

## Effect

As the first tracked edit of the phase, the canonical
[`E1-X1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md)
is amended to `AUTHORIZED FOR IMPLEMENTATION` (prepared blob
`fb2c245e679fdd3fcd6082d5c6ecd722c561608f` at `b3c78e6d`). Where the prepared
prompt and this record differ, this record prevails.

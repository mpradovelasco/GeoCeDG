# PRE-G9B-S2 Text Properties and construction-font closeout report

- Status: **PASS — AUTHOR APPROVED**
- Entry commit: `f502ac8ead5e1b2bf6e70a8583fedfc0a26d9c4a`
- Entry tree: `a5bd55820e24a998e5cb4f9c4e39964202b7e0de`
- Verified implementation commit: `36b4ac68f868282c452e48d35b4972b2a71fba0a`
- Verified implementation tree: `3f2397e468569ca716992e0500751ef3b1b1ea12`
- Verification class: bounded `PHASE`, selector `PRE-G9B-S2`
- Author smoke: **PASS**
- Self approval: `false`

## Scope and result

S2 corrects only the inherited Desktop Text Properties lifecycle and the
construction-object base-font setting. It adds no geometric-kernel semantics,
zoom-dependent Text behavior, general presentation controls, toolbar sizing
architecture or serialization migration. Existing `.cedg` and `.ggb`
compatibility and the inherited construction-font persistence scope remain
unchanged.

The implementation uses the existing Text editor and Algebra Processor
redefine path. Apply commits the current draft and leaves Properties open. OK
commits and closes on success. Cancel discards only the draft after the latest
committed baseline. Selection change and normal window close discard an
unapplied Text draft; inherited tab/panel auto-apply remains unchanged and
targets the original Text. A Text-owned close no longer adds a duplicate Undo
checkpoint, and one committed redefine remains one logical Undo/Redo step.

Construction-object font choices now include 10 pt independently of the
existing menu-font choices. GeoCeDG keeps default GUI typography and toolbar
icons on GUI-font ownership while preserving explicit GUI preferences and the
Classic fallback.

## Validation matrix

| Contract | Evidence | Result |
|---|---|---|
| Apply/OK/Cancel and Apply-followed-by-Cancel/OK | focused Desktop lifecycle tests | PASS |
| Invalid OK, selection change, close and reopen | focused Desktop lifecycle tests | PASS |
| Redefine continuity and one-step Undo/Redo | real Desktop redefine test | PASS |
| 10 pt acceptance and retained normal values | shared and Desktop font tests | PASS |
| Existing document persistence | native XML save/reopen regression | PASS |
| Menu typography and toolbar icon isolation | real GeoCeDG host-menu regression | PASS |
| Retained Text and Desktop Properties behavior | canonical S2 shared/Desktop selections | PASS |

## Accepted automated evidence

- Focused shared selection: 11 tests, zero failures, errors or skips.
- Focused Desktop selection: 76 tests, zero failures, errors or skips.
- Final PHASE run `verification-61d364a0f05a442988912ec10b375704`:
  87 tests, zero failures, errors or skips; `ACCEPTED / COMPLETE`; zero
  diagnostics. Execution-plan hash
  `7084076e5b09c34bfa91b20d55129edb9e9584158b0997de6a00e799b28334f9`;
  result hash
  `99f3a68ffbfd63ef7e8aaa6a20a93c92952e8d90b1dac540d97e9dc0bd6e35f9`.
- `INFRA_UNIT` run `verification-a1b0bacffcf44f15bf1b951cfaf9fea2`:
  `ACCEPTED / COMPLETE`; zero diagnostics; result hash
  `9132ec298ef3138c9b679731494e37abd40e0ec954c99d93c77aa67efb9d5d59`.
- Affected shared/Desktop main and test checkstyle: PASS.
- `git diff --check`: PASS.

The exact implementation candidate was not changed after PHASE. These receipts
remain the technical acceptance authority and were not rerun for documentary
closeout.

## Author evidence and decision

The author reports the requested interactive S2 smoke as **PASS**, covering the
implemented Text Properties lifecycle and construction-font behavior. This is
human evidence, not an automated verifier result. No additional manual result
is inferred.

The author explicitly approves S2 and authorizes its documentary closeout and
ordinary fast-forward publication. This decision is not agent self-approval.

```text
PRE-G9B-S2 — PASS — AUTHOR APPROVED
selfApproved=false
```

## Boundaries and next gate

`GUIDE_IMPACT = UPDATED`: the living user guide now records the approved S2
behavior and links this closeout evidence.

`BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: S2 and its documentary closeout alter
no workstation prerequisite, supported toolchain, verification entrypoint,
packaging prerequisite or bootstrap-consumed assumption.

Verification-infrastructure impact remains the additive S2 registry/inventory
work already accepted by `INFRA_UNIT`; closeout changes no executable or
verification-authority file.

The next gate is:

```text
PRE-G9B-S3 — DESIGNED / NOT YET IMPLEMENTATION-AUTHORIZED
```

S4, D1, P1, G9B/G9C/G9U2, further G12 and productive G10 remain unauthorized.

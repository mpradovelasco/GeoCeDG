# PRE-G9B-R6-plus-G-R1 authorization record (corrective integration and D-1)

```text
RECORD_KIND                       = AUTHOR AUTHORIZATION, ENTRY GATE AND D-1 DISPOSITION
TASK                              = PRE-G9B-R6-plus-G-R1 — author-directed corrective integration,
                                    debt reconciliation and technical reverification
AUTHORIZATION_DATE                = 2026-10-15
APPROVED_PUBLISHED_BASE           = ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3
                                    tree fa748fd1b87ae16c9f0c248f8f77b94069cc2ea3
ORIGINAL TECHNICAL CANDIDATE T    = dd3937cfba7092877f48e7f927dd6213959292cf
                                    tree b45311d2b78d9c3625e379835b8d76031df9530a
                                    (branch phase/pre-g9b-r6-plus-g-implementation, unchanged)
SUCCESSOR BRANCH                  = phase/pre-g9b-r6-plus-g-r1 (local), from the last
                                    pre-transition commit f52cf5b31fc36b3dac007c7204b2df53ea80fcc6

G ORIGINAL CANDIDATE T            = VERIFIED — SUPERSEDED FOR FINAL REVIEW
D-1                               = ACCEPTED RECOVERY — AUTHOR DISPOSITION
G-R1 OPEN-ROUTE CORRECTION        = AUTHORIZED (SC-01, SC-02, SC-11)
TOOL-REPLACEMENT CORRECTION       = NOT AUTHORIZED
TOOL-REPLACEMENT DISPOSITION      = PENDING AUTHOR REVIEW
VERSION TARGET / VERSION_DATE     = 1.1.0 / 2026-10-15 (rebuilt as the last productive commit T')
VERIFICATION_CLASS                = INTEGRATED_PHASE
CLOSEOUT_MODE                     = AUTHOR_OPERATED
G AUTHOR SMOKE                    = PENDING
G AUTHOR APPROVAL                 = NOT YET GRANTED
PRE-G9B-R6-plus GLOBAL CLOSEOUT   = NOT AUTHORIZED
PRE-G9B-R7 / G9B                  = NOT AUTHORIZED
PUBLICATION / PUBLIC RELEASE      = NOT AUTHORIZED
selfApproved                      = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-R6-plus-G-R1" and
records its entry gate and the author disposition of the packaging recovery D-1. It is the
first tracked change of the successor lineage. It changes no decision of the
[G author design closeout record](pre_g9b_r6_plus_g_author_design_closeout_record.md) or
of the [G implementation authorization](pre_g9b_r6_plus_g_implementation_authorization_record.md),
whose scope it extends only as stated in §1. Machine-readable mirror:
[`pre-g9b-r6-plus-g-r1-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-r1-authorization.json).

## 1. Author authorization (essentials)

The author reviewed the technical candidate report of the original candidate T and
authorizes six operations as one integrated task:

| # | Operation | Authorized content |
|---|---|---|
| 1 | D-1 | acceptance and documentary recording of the packaging-verification recovery D-1 (§3) |
| 2 | Open-route correction | a bounded GeoCeDG-owned correction of the confirmed violations SC-01, SC-02 and SC-11: a successful full-document Open transaction applies the approved document-transition session reset (`ExportArea` session with its manual area, producer, overlay and ISO A link where the Open contract requires it; the transient paste notice of the previous document) only after successful commitment, exactly once, independently of transport; focused regression tests |
| 3 | Tool replacement | `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` kept separate: no corrective change, no automatic session reset, no working-layer change; characterization of G-R1 interactions; a proposed disposition and owner for the author |
| 4 | Successor candidate T' | a separate local lineage from the last pre-transition commit; the corrections at `1.0.0` without a version date; the approved version transition rebuilt as the last productive commit (`1.1.0`, `2026-10-15`, package-profile schema `2`, provenance schema `3`); the full `INTEGRATED_PHASE` campaign on T' |
| 5 | Debt decisions | one consolidated author decision sheet for the items still pending before the global closeout; the 134-entry register and its mirror preserved |
| 6 | S3 and ADR 0029 | status metadata only, from the accepted closeouts; no contract, decision or pinned historical text changed |

Excluded routes and changes: Insert File, Apply Styles, tool replacement, construction
merging, `evalXML` and other partial updates, embedded-host operations that are not
approved Open transactions, API transports whose session-reset semantics are undecided; no
unconditional reset in `commitLoadedDocument`, the generic clearing `setXML` or an
equivalent shared path; no inherited-source, kernel, serialization or global
verification-infrastructure change; no change to T or its evidence; no push, merge, tag,
release, publication or subsequent phase. `FINAL` is not authorized automatically.

## 2. Entry gate (2026-10-15)

| # | Check | Result |
|---|---|---|
| 1 | live remote `main` (`git ls-remote origin refs/heads/main`) | `ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3` (the approved published base) |
| 2 | lineage from local objects (commit, tree, parent) | `6e09eee99a9365ef1d770aa6e48ebab2e5938994` (tree `f4a7550b…`) → `ebd6accb1a417ca98339d7d2f9342c6140d630ae` (tree `65572e1d…`) → `5962569e308652686df2097dceb0ad0575ba39d8` (tree `4ed9ae7b…`) → `f52cf5b31fc36b3dac007c7204b2df53ea80fcc6` (tree `9014c7fe512a80b800292c903d3a899ab5bb57ac`) → T `dd3937cfba7092877f48e7f927dd6213959292cf` (tree `b45311d2…`); linear, first parent of `6e09eee9` is the base |
| 3 | `f52cf5b3` is the last pre-transition commit and the parent of T | yes; it keeps `1.0.0`, package-profile schema `1`, no version date |
| 4 | worktree at branch creation | clean |
| 5 | T unchanged | branch `phase/pre-g9b-r6-plus-g-implementation` still at `dd3937cf…` |

Entry gate: **PASS**.

## 3. D-1 — packaging-verification recovery (author disposition)

### 3.1 Facts (authenticated for this record)

The original campaign script skipped both `PACKAGING -VerifyPackagingArtifacts`
invocations: its build step returned the logged line together with the exit code, so the
gate `-eq 0` was false. The builds themselves succeeded. NC was then verified against its
unchanged set; INTERNAL, which the NC build had replaced, was rebuilt from T and verified.
The source commit and tree never changed and no product correction was needed.

| Set | Built from | Manifest SHA-256 | Record SHA-256 | ZIP / MSI / EXE SHA-256 (prefix) | `runtime/lib/modules` | Own artifact verification |
|---|---|---|---|---|---|---|
| original INTERNAL | T | `b3c3a52b33418681d90afd29f9fca6c0a15eec35a41fb5ee9e8d1be33d3dae66` | `60c1b660d06603df987119439c907f0e865dfec61153c51fef1ae3c5d7a59b49` | `99941358` / `f3277653` / `a7dc9a38` | `dd22478c…` | **none (skipped)** |
| NC | T | `d424f6ef6c92d6738361bd5dcc3d0bccafc77a54a91fb3cd39eaf1a9de7a3b00` | `8419368bd132560a1f0ed3060529cfb447dd00ac9c693b3e5c6541867256004a` | `d5cd38ed` / `817ac390` / `cd6f2699` | `25fd18d5…` | `verification-31d0335626c245d785d2a4cc59368eaa`, result `48331e7e8db816e117cf916020ee8ee14bae2d6454f79d1069d5bcca072cb1e9`, ACCEPTED / COMPLETE |
| recovery INTERNAL (second build) | T | `b65c2c757dc32fd7ec5820e0362f491939ba497fc0666931692dd7f5f25baec9` | `60c1b660…` (identical) | `17c78517` / `36781c1e` / `76805e8f` | `25fd18d5…` | `verification-8ffb3adc4fd041cbbf7f69f6c776b9b5`, result `d0cd4cff778cff9fa53597b934e8fd04f8add8eadcb8dd29e372e54cc1a36bce`, ACCEPTED / COMPLETE |

- `desktop.jar` is byte-identical in all three sets (`f01b6067…`); the two INTERNAL sets
  differ only in the JDK 25 `jlink` image `runtime/lib/modules` and the packages derived
  from it, a variation the approved
  [Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md)
  already covers ("does not promise byte-identical ZIP, MSI, or EXE binaries").
- Each verification result names T and its tree; its recorded result hash recomputes
  (`Get-VerificationResultHash`) and equals its run journal. The NC result carries the NC
  ZIP hash and the INTERNAL result the recovery ZIP hash.
- The 54 preserved campaign-evidence files (git-ignored
  `artifacts/agent/g-dd3937cf-campaign-evidence/`) match their `SHA256SUMS.evidence.txt`.

### 3.2 Disposition

`D-1 = ACCEPTED RECOVERY — AUTHOR DISPOSITION`. It accepts the evidence of the verified
**second** INTERNAL build; the original INTERNAL set was built but never verified, and is
not claimed as verified. The T campaign is not rerun. T's acceptance evidence is
historical and is not inherited by any successor candidate.

## 4. Execution plan

1. Entry gate and D-1 (this record).
2. The Open-route correction and its regression tests at `1.0.0`.
3. Tool-replacement interaction characterization; the G-R1 correction report.
4. The consolidated debt decision sheet; S3 and ADR 0029 status metadata.
5. The version transition rebuilt as the last productive commit, which is T'.
6. The full `INTEGRATED_PHASE` campaign on the exact T'.
7. Evidence consolidation and the author smoke handoff.

A genuine stop condition preserves the completed evidence and is reported. Technical
acceptance never implies author approval; this record originates no publication, release
or approval.

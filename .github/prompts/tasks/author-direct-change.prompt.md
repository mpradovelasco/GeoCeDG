# Author-direct change

<!-- geocedg-field: objective -->
## Objective

Apply only an explicitly author-authorized repository change through
`CHANGE_ROUTE = AUTHOR_DIRECT`. This route is outside `VERIFICATION_CLASS` and
must never be inferred, selected autonomously or used to escape a failure.

<!-- geocedg-field: implementation_base -->
## Implementation base

Record the exact base commit and tree named by the author. Stop if either moves.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Only the exact author-authorized paths and transformations are in scope.

<!-- geocedg-field: forbidden_scope -->
## Forbidden scope

No adjacent implementation, verification, publication or policy change is implied.

<!-- geocedg-field: authorization_boundary -->
## Authority and authorization

Read `AGENTS.md`, the current repository sources and
`geocedg/specs/operations/verification-levels.md` section 13. Record the exact
author instruction and freeze all of:

```text
CHANGE_ROUTE = AUTHOR_DIRECT
AUTHORIZED_SCOPE = <exact change scope>
AUTHORIZED_PATHS = <exact repository paths>
AUTHORIZED_REASON = <reason for the exception>
PRODUCT_PHASE_EFFECT = NONE
TECHNICAL_VERIFICATION_CLAIM = NONE
PREVIOUS_EVIDENCE_REINTERPRETED = false
```

If any field is absent, contradictory or broader than the author's instruction,
stop before editing. Authorization applies only to the named scope and paths.

## Scope detail

Make the smallest coherent diff within `AUTHORIZED_PATHS`. Do not add scripts,
schemas, validators, receipts, gates or test families. Do not change product,
kernel, productive UI, build, Gradle, toolchain, serialization, scientific
references or productive tests unless a future explicit author authorization
first expands the governing policy and names those exact paths.

Stop and request authorization before any scope expansion, executable-code
change, new validation machinery or product/scientific effect.

<!-- geocedg-field: required_checks -->
## Checks

Perform only trivial static checks needed to avoid syntactic or documentary
errors. Do not run DEV, PHASE, COMPOSED, FULL, READINESS, PREPARE or another
ordinary acceptance/closeout workflow. Static checks are not acceptance evidence
and support no technical verification claim. Do not reinterpret, relabel or
inherit previous evidence.

<!-- geocedg-field: publication_boundary -->
## Commit and publication boundary

Inspect the exact diff, create one clean candidate commit, and confirm the
worktree/index are clean. Report the candidate SHA, tree, exact changed paths,
diff summary, checks performed and the four fixed route declarations. Then stop
for explicit author approval naming that exact SHA.

Do not push, merge, tag, fast-forward a publication ref or otherwise promote the
candidate. Git promotion is author-operated. Do not continue into any later task
or use of `AUTHOR_DIRECT` until the author has approved the exact SHA and the new
authority is published.

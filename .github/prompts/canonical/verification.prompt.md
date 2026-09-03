# GeoCeDG canonical verification prompt

Status: canonical operational prompt

Use executable repository tools as the authority. Reports summarize their
saved evidence; they do not replace a command result.

## Entry points

- Default COMPOSED gate applicable to the current checkout: `tools/agent/verify.ps1`.
- Exhaustive supported test gate: `tools/agent/verify.ps1 -Level FULL`
  (`-FullTests` remains the legacy alias).
- Explicit inner-loop/capability scope: `-Level DEV -Module <shared|desktop>
  -TestFilter <filters>` or `-Level PHASE -Phase <supported-id>`.
- Operational structure only: `tools/agent/verify-operational.ps1`.
- Pinned upstream compile gate: `tools/agent/verify-baseline.ps1`.
- Informational benchmark suite: `tools/benchmark/run.ps1`.

Use `geocedg/specs/operations/verification-levels.md` for level/perimeter,
bootstrap-impact and infrastructure-review rules. Start with the narrowest
relevant command, then complete the required COMPOSED/FULL gate. DEV is not an
acceptance gate; static `-SkipBuild` evidence is incomplete. A required but
unrun/failed FULL gate cannot be waived by narrower success. Record applicable
interactive, packaging and external-runtime evidence separately.

COMPOSED/FULL use separate shared and Desktop test invocations. Current-run
receipt consumption is valid only through the explicit executable protocol;
every phase retains its own live assertions. `-IndependentBuilds` retains the
original orchestration for equivalence/diagnosis. Explicit `-KeepBuildOutputs`
retains generated outputs; it does not change test scope or freshness. Never
install tools silently, suppress a failure, edit generated evidence, or
translate an environment/permission failure into product code.

`verify.ps1` reports the current branch or detached HEAD, exact commit, and
latest included phase from the normative roadmap. The branch is diagnostic;
the roadmap and versioned checkout determine the applicable productive gates.
Historical G7 phase preconditions remain available only through the explicit
`-ReproduceCharacterization` and `-ReproduceImplementation` modes of their
focused verifiers.

For every command record its working directory, arguments, exit code, log or
artifact path, and whether evidence is static, fake-first, skipped, or from a
real runtime. Run `git diff --check` and report final `git status --short`.

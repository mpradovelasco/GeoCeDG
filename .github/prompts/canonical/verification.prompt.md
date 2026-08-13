# GeoCeDG canonical verification prompt

Status: canonical operational prompt

Use executable repository tools as the authority. Reports summarize their
saved evidence; they do not replace a command result.

## Entry points

- Full gate applicable to the current checkout: `tools/agent/verify.ps1`.
- Operational structure only: `tools/agent/verify-operational.ps1`.
- Pinned upstream compile gate: `tools/agent/verify-baseline.ps1`.
- Informational benchmark suite: `tools/benchmark/run.ps1`.

Start with the narrowest relevant command, then run `verify.ps1` before
completion unless the task documents why a gate is inapplicable. Never install
tools silently, suppress a failure, edit generated evidence, or translate an
environment/permission failure into product code.

`verify.ps1` reports the current branch or detached HEAD, exact commit, and
latest included phase from the normative roadmap. The branch is diagnostic;
the roadmap and versioned checkout determine the applicable productive gates.
Historical G7 phase preconditions remain available only through the explicit
`-ReproduceCharacterization` and `-ReproduceImplementation` modes of their
focused verifiers.

For every command record its working directory, arguments, exit code, log or
artifact path, and whether evidence is static, fake-first, skipped, or from a
real runtime. Run `git diff --check` and report final `git status --short`.

# CLAUDE.md - Claude Code session bootstrap for GeoCeDG

Scope: this file governs only how Claude Code starts and conducts a session in
this repository. It carries no geometric, architectural, licensing, or roadmap
authority, and it is not a source of truth for anything else. `AGENTS.md`
prevails over this file on any conflict.

## Before doing anything else

1. Read root `AGENTS.md` in full. It is the root authority for automated agents
   here and defines the CeDG principles, authority hierarchy, repository
   boundaries, placement rules, maturity states, licensing and upstream policy,
   the Locus V2 and spatial/projection contracts, validation authority,
   execution rules, and prohibited actions.
2. Read `.github/prompts/canonical/governance.prompt.md` and
   `.github/prompts/canonical/verification.prompt.md`. For product-to-book
   operations only, also read `.github/prompts/canonical/book/operations.prompt.md`;
   editorial authority lives in the external book repository and is never
   copied into this one.
3. Determine current phase and authorization status by reading
   `docs/roadmap/geocedg_roadmap.md` from disk. Never assume a phase, and never
   treat a prior session, a summary, or this file as authorization.
4. If the task corresponds to a prompt under `.github/prompts/tasks/` or
   `.github/prompts/reviews/`, load that prompt and follow its declared scope,
   required level, and stop conditions instead of improvising. Use
   `.github/prompts/tasks/task-template.prompt.md` as the shape of a task
   prompt. `ai-shell/prompts/*.md` are short interaction profiles that point
   back to the canonical prompts.
5. `FIRST_AGENT_TASK.md` is the historical G0 bootstrap mission, already closed
   per the roadmap. Read it as evidence of origin, never as an open task.

## Authority order

As defined in `AGENTS.md` section 2. Do not re-derive, reorder, or summarize it
here. Generated artifacts, reports, screenshots and previous agent output are
evidence, not source authority.

## Claude-Code-specific operating notes

These cover only tool-level facts `AGENTS.md` does not address.

- **The verifier is PowerShell-only.** `tools/agent/verify.*` resolves to
  `tools/agent/verify.ps1`; there is no POSIX variant, and CI runs on Windows.
  Invoke it and its siblings through the PowerShell tool, never the Bash tool.
  Bash remains fine for reading and searching.
- **Run the narrowest executable authority.** Follow the entry points and level
  rules in the canonical verification prompt and
  `geocedg/specs/operations/verification-levels.md`. Do not launch a FULL or
  acceptance-class run as a side effect of exploration: those runs are
  expensive and carry closeout semantics. Report exact commands, exit codes and
  log paths.
- **Read the governing file, not a memory of it.** `AGENTS.md`,
  `verification-levels.md` and the roadmap are large and are frequently the
  files a long session summarizes away. Re-read the relevant section from disk
  before relying on it; a compacted summary of an authority is not that
  authority.
- **Read before write, and edit exactly.** Inspect a file before modifying it
  and keep edits to the smallest coherent set. Do not reformat, re-encode, or
  normalize line endings in passing: `.gitattributes` pins byte-exact author
  evidence and generated-reference files, where a whitespace or EOL change
  destroys the evidence it protects.
- **No destructive or history-rewriting git operations** without explicit human
  instruction in the current session: no `push --force`, `reset --hard`,
  `checkout --`/`restore` over uncommitted work, `clean -fd`, branch or tag
  deletion, rebase of published history, or stash drop. Do not stage sweepingly
  (`git add -A`) when unrelated work is present; stage the files the task
  actually changed.
- **Keep scratch output out of the tree.** Temporary scripts, probes and
  intermediate results go to the session scratchpad, not into tracked source
  paths or `artifacts/`.

## Hard prohibitions specific to this file

- Never edit `AGENTS.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`, or
  `ai-shell/prompts/**` as a side effect of another task. Changing the
  governance layer is its own author-authorized task.
- Never edit this file to widen its own permissions, and never promote prior
  Claude Code output, plans, or reports to authoritative status.
- Never mark a gate, phase, specification, ADR, or roadmap item as
  approved / PASS / AUTHOR APPROVED. Technical PASS never implies author
  approval, and the two are independent facts.
- Never infer authorization from a previous session, a conversation summary, a
  clean working tree, a branch name, a tag, or this file's own content.
- Never introduce a second copy of geometric, architectural, or verification
  truth - in this file or anywhere else. Reference the durable source instead.

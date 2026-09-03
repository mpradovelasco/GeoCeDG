# Objective

State one concrete outcome.

# Authority and evidence hierarchy

Reference `AGENTS.md`, current code/build, accepted specifications/ADRs, and
task-specific evidence in order.

# Scope

List exact files, modules, and observable behavior included.

# Explicitly forbidden scope

List adjacent layers and behavior that must remain unchanged.

# Architectural placement

Name the owning layer and explain why it is authoritative.

# Required design/specification

Name the approved specification/ADR or require one before implementation.

# Geometric invariants and degeneracies

Reference governing definitions. State `not applicable` only for a genuinely
non-geometric task.

# Compatibility and serialization

State legacy, file-format, feature-flag, and migration constraints.

# Required tests and commands

Name focused checks and the relevant `tools/agent/verify*.ps1` entry point.
Distinguish DEV, PHASE, COMPOSED and FULL; name explicit DEV filters and the PHASE
regression perimeter. Identify verification-infrastructure impact and required
FULL validation under `geocedg/specs/operations/verification-levels.md`. A smaller
development selection never changes the acceptance gate.

# Required artifacts

List durable sources, generated evidence, and the completion report.
Include the bootstrap-impact outcome and rationale/affected paths required by
the verification-level contract, infrastructure-impact assessment, exact
required-level command/exit/log evidence, and existing `GUIDE_IMPACT`. Distinguish
technical verification from author approval; explicitly report incomplete gates.

# Stop conditions

List decisions or failures that require human review before continuing.

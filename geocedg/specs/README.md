# GeoCeDG specifications

This directory contains durable GeoCeDG contracts. A specification owns
behavioral meaning; prompts and verification scripts reference it and must not
restate its geometric rules.

Current contracts include the application profile, controlled legacy
integration, Windows packaging, neutral 2D export and the normative Locus V2
semantic contract under `locus/`. Approval of that contract closes G6A but does
not itself authorize or start G6B implementation.

Every specification must state its status, version, authority, scope,
invariants, compatibility policy, validation evidence, and stop conditions.
Use `templates/specification-template.md` as the starting structure.

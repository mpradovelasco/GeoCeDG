# GeoCeDG public release records

One immutable file per public release: `v<MAJOR>.<MINOR>.<PATCH>.release.json`.

A release record is a verifiable descriptor of a publication. It is not an
autonomous statement of technical success, it grants no approval, and it adds
no authority over the tag, the commit, the roadmap or the verification
contracts. Where it repeats an identity, Git is the authority and the book
bridge (`tools/book/book-worktree.ps1`) rejects a record that contradicts it.

Each record states:

- the identity of the release: annotated tag name and object, commit and tree;
- the version and version date, equal to `packaging/windows/package.yml` at
  the release commit;
- the distribution: profile, platform, release URL, publication time and the
  published assets with their sizes and SHA-256 digests;
- `publication_contrast`: when and how the public release was contrasted with
  GitHub, and the scope of that contrast;
- the author authorization, by reference to the record that documents it;
- the verification accepted on the release commit, the profiles not executed
  there, the profiles whose execution is not established there, and the state
  of the final receipt;
- evidence inherited from earlier commits, with their own identity;
- limits and exceptions, by reference to their source.

Rules:

- A record contains no single pass or approval flag; unknown properties are
  rejected.
- A record describes an ancestor commit: it is committed after the release,
  because it names the tag object.
- A local record that is coherent with Git is not by itself proof that the
  release is publicly available. The offline bridge checks coherence only; the
  contrast with GitHub is the one recorded in `publication_contrast`.
- A published record is not edited to change history. A later fact is recorded
  in a living document or in a successor disposition.
- This directory is outside the technical-authority fingerprint of the book
  bridge: recording a release does not change the fingerprint of any commit.

The schema is `tools/book/schemas/release-record.schema.json`. Publishing a
release remains a separate explicit author decision
(`geocedg/specs/packaging/windows-packaging.md`, "Release gate"); accepting a
release as the technical baseline of the book is an editorial decision recorded
in the book repository.

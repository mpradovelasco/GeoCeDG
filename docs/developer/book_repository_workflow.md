# External GeoCeDG book repository workflow

## Authority boundary

The GeoCeDG product and its future book use two independent Git repositories.
The division is deliberate:

| Repository | Owns | Does not own |
|---|---|---|
| GeoCeDG | Product/kernel source, specifications, ADRs, validation evidence, canonical models, user/developer documentation and the optional book-worktree entry point | Manuscript, editorial matrix, book bibliography, composed book figures or book build output |
| `mpradovelasco/geocedg_book` | Editorial matrix and roadmap, manuscript sources, bibliography, curated publication assets and reproducible book tooling | GeoCeDG product semantics, canonical product source or product validation authority |

`book` at the GeoCeDG root is only a local filesystem link to an external clone.
The root-scoped `/book` ignore rule prevents accidental discovery by GeoCeDG
Git. It is not a submodule, gitlink, vendored manuscript or machine-specific
path declaration. A normal GeoCeDG clone does not contain this link and remains
fully buildable and verifiable without it.

Book outputs are publication-workflow evidence only. They are not GeoCeDG
product validation evidence and cannot alter product maturity or phase status.

## Establish the optional link

Clone the book repository outside the GeoCeDG worktree, then create the local
link from the GeoCeDG root. Choose the external location locally; never record
its absolute path in either repository.

```powershell
$BookClonePath = Read-Host "External directory for geocedg_book"
git clone https://github.com/mpradovelasco/geocedg_book.git $BookClonePath
New-Item -ItemType SymbolicLink -Path .\book -Target $BookClonePath
```

A directory junction is also acceptable on Windows when it resolves to that
separate repository. A normal directory nested inside GeoCeDG is rejected.
Creating symbolic links may require Windows Developer Mode or appropriate
permissions.

## Inspect and invoke

From the GeoCeDG root, the default action validates the boundary and reports
the link, resolved root, origin, branch, commit and full porcelain status of
both repositories:

```powershell
.\tools\book\book-worktree.ps1
.\tools\book\book-worktree.ps1 -Action Status
```

The script rejects a tracked `book` path, a submodule, shared Git authority,
nested worktrees, a link to a repository subdirectory, a missing ignore rule or
an origin other than `mpradovelasco/geocedg_book`.

Future book-owned verification and build entry points can be invoked only by
an explicit action:

```powershell
.\tools\book\book-worktree.ps1 -Action Verify
.\tools\book\book-worktree.ps1 -Action Build
```

These actions delegate to `tools/verify.ps1` or `tools/build.ps1` in the book
repository only when the selected file exists. Optional arguments can be
passed with `-BookArguments`. The wrapper does not install LaTeX or any other
tool, and it never stages, commits, merges, tags, fetches, pulls or pushes.

Run Git commands separately and from the intended repository root. In
particular, never use a recursive `git add` from GeoCeDG to operate on book
content.

## Independent verification

The book entry point is deliberately outside `tools/agent`. It is not called by
`tools/agent/verify.ps1`, the bootstrap or normal CI. Missing or dirty book
state therefore cannot change GeoCeDG acceptance; book checks are always
explicit and opt-in.

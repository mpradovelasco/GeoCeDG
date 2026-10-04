# FreeHEP VectorGraphics vendored source: provenance record

- Recorded: 2026-10-02 by `PRE-G9B-R6-plus-B`, before any `org/freehep/**`
  file was modified (author gate of 2026-10-02)
- Machine-readable mirror:
  [`freehep-vectorgraphics-provenance.json`](../../geocedg/validation/pre-g9b-r6-plus/freehep-vectorgraphics-provenance.json)
- Character: factual provenance evidence. It is not legal advice, a license
  conclusion or a release approval.

## Snapshot

The GeoGebra baseline (`9b93256b7df401ff056c37b502d82df4d72b1522`) carries a
vendored copy of the FreeHEP VectorGraphics Java library under
`source/desktop/desktop/src/main/java/org/freehep/**` (`graphics2d`,
`graphicsio` with its `emf`, `font`, `pdf`, `raw` and `svg` families, `util`,
`xml`; 314 files, 304 of them Java). It is compiled into the Desktop module. GeoGebra uses it for the PDF,
SVG and EMF/EMF+ picture writers.

| Fact | Evidence |
|---|---|
| Origin | FreeHEP VectorGraphics, an open-source library of the FreeHEP Java Library; copyright holders recorded in the files as FreeHEP and, in six files, "CERN, Geneva, Switzerland and University of Santa Cruz, California, U.S.A." | file headers |
| Exact revision of `pdf/PDFGraphics2D.java` | the FreeHEP repository revision `36ad488` (2006-11-12, SVN identity `ed7cf0bfe62e`, "Copyright 2000-2006 FreeHEP"). The next FreeHEP revision of that file, `7bf7c53` (2007-01-04), changes the header to "2000-2007"; no revision lies between them | official repository history of the file |
| Release line | FreeHEP VectorGraphics 2.0, released on 2006-12-07; its release notes list `freehep-graphicsio-pdf 2.0` and the other 2.0 modules. Revision `36ad488` is the state of the file at that release | FreeHEP News page; Release Notes 2.0 |
| Later history before the GeoCeDG baseline | GeoGebra CVS revisions by `murkle` (Michael Borcherds) between 2008-05-04 and 2009-08-17, visible in the `@version $Id$` lines; GeoGebra changes such as `setPageSize` and the use of `org.geogebra.common.jre.util.ScientificFormat` in `PDFUtil` | file headers and comments |
| Entry into this repository | the GeoGebra Copybara import `e6a9568f2` (2025-01-10); no earlier git history exists here | `git log --follow` |
| GeoGebra-licensed files inside the tree | 30 files under `org/freehep/**` carry a GeoGebra GmbH EUPL-1.2 header | file headers |

## Terms demonstrated for the snapshot

| Statement | Source | Applies to the 2006 snapshot |
|---|---|---|
| "The VectorGraphics package is part of the FreeHEP Java Library, an 'Open Source' library distributed under the terms of the LGPL." | FreeHEP VectorGraphics Release Notes 2.0 | yes: the release-2.0 statement. The release notes do not state the LGPL version |
| The current repository `LGPL.txt` is the GNU Lesser General Public License, Version 2.1, February 1999 | official repository, current `master` | recorded as the text distributed by the project today; not proven to be the text shipped with release 2.0 |
| The current repository `LICENSE.txt` dual-licenses the software under the LGPL and the Apache License 2.0, with copyright CERN, SLAC and UC Santa Cruz 2000-2009 | official repository, current `master` | **not attributed** to the 2006 snapshot: no primary evidence connects Apache 2.0 to release 2.0 or to revision `36ad488` |

## GeoCeDG modifications

Every FreeHEP file GeoCeDG modifies is registered in
[`docs/upstream/modified-files.yml`](../upstream/modified-files.yml) as
modified third-party FreeHEP source. It keeps its copyright header and carries a
dated GeoCeDG modification notice at the change. `PRE-G9B-R6-plus-B` modifies
only `org/freehep/graphicsio/pdf/PDFGraphics2D.java`, with an opt-in exact page
extent; no other FreeHEP family is touched by `B`. `PRE-G9B-R6-plus-C` modifies
three files of the EMF family (below).

### EMF family (`PRE-G9B-R6-plus-C`, recorded 2026-10-04)

Recorded before any `org/freehep/graphicsio/emf/**` file was modified, under
the same gate, for the opt-in exact EMF frame of author decision `DQ-C9`.
`PRE-G9B-R6-plus-C` modifies only `emf/EMFOutputStream.java`,
`emf/EMFGraphics2D.java` and `emf/EMFPlusGraphics2D.java`, with an explicit
opt-in exact `rclFrame`; a writer that does not opt in produces the original
output. No other FreeHEP family and no default behavior change.

| Fact | Evidence |
|---|---|
| Release line | the vendored `emf` family has the `gdi` subpackage that FreeHEP introduced in revision `ab52a27` ("Moved emf tags to gdi", 2006-12-07, the date of the 2.0 release); `EMFGraphics2D.java` carries "Copyright 2000-2006 FreeHEP". It therefore belongs to the same FreeHEP VectorGraphics 2.0 line as `PDFGraphics2D.java` | official repository history of `EMFGraphics2D.java` and of the `emf` directory |
| `emf/EMFOutputStream.java` | its constructors, fields and header ("Copyright 2001-2006, FreeHEP.") match the FreeHEP revision `a285e1f` (2006-03-02, the last revision of that file in the official history); the vendored `close()` additionally passes a header type to `EMFHeader`, a later GeoGebra change | official repository history and the raw file at `a285e1f` |
| `emf/EMFGraphics2D.java` | 2.0 line, at or after `ab52a27`; the exact revision among `ab52a27` (2006-12-07) and the 2007 revisions was not determined; GeoGebra disabled the device query (`// GeoGebra: disabled`) | official repository history |
| `emf/EMFPlusGraphics2D.java`, the `emf/gdiplus` package and the header types of `emf/EMFHeader.java` | headers "Copyright 2006, FreeHEP"; **no revision of the official FreeHEP repository contains them** (the file has no history there and the `emf` directory at `ab52a27` has no `gdiplus`); they reached this tree through GeoGebra CVS (`$Id … 2009-08-17 murkle`). Their exact FreeHEP revision is **not establishable** from public primary sources | official repository history; file headers |
| Terms | the FreeHEP VectorGraphics release-2.0 statement recorded above (LGPL, version not stated) is the only statement attributed to the family; nothing else is inferred | as above |

The exact-revision limitation of the EMF+ files is recorded for the author's
review of `C`; it changes no license attribution of the snapshot.

## Open for release review

The package legal bundle (`LICENSES/manifest.json`, the component audit and the
source-access manifest) does not yet list FreeHEP as a separate component; the
D1 audit classified the source tree as GeoGebra-derived code. This record does
not change the package bundle. Whether the shipped legal texts and source
access need a FreeHEP entry is left to the professional release review
(`AGENTS.md` §7).

## Sources

- <https://github.com/freehep/freehep-vectorgraphics>
- <https://github.com/freehep/freehep-vectorgraphics/commits/master/freehep-graphicsio-pdf/src/main/java/org/freehep/graphicsio/pdf/PDFGraphics2D.java>
- <https://raw.githubusercontent.com/freehep/freehep-vectorgraphics/36ad488/freehep-graphicsio-pdf/src/main/java/org/freehep/graphicsio/pdf/PDFGraphics2D.java>
- <https://raw.githubusercontent.com/freehep/freehep-vectorgraphics/7bf7c53/freehep-graphicsio-pdf/src/main/java/org/freehep/graphicsio/pdf/PDFGraphics2D.java>
- <https://github.com/freehep/freehep-vectorgraphics/commits/master/freehep-graphicsio-emf/src/main/java/org/freehep/graphicsio/emf/EMFGraphics2D.java>
- <https://github.com/freehep/freehep-vectorgraphics/commits/master/freehep-graphicsio-emf/src/main/java/org/freehep/graphicsio/emf/EMFOutputStream.java>
- <https://github.com/freehep/freehep-vectorgraphics/commits/master/freehep-graphicsio-emf/src/main/java/org/freehep/graphicsio/emf/EMFHeader.java>
- <https://raw.githubusercontent.com/freehep/freehep-vectorgraphics/a285e1f/freehep-graphicsio-emf/src/main/java/org/freehep/graphicsio/emf/EMFOutputStream.java>
- <https://github.com/freehep/freehep-vectorgraphics/tree/ab52a27/freehep-graphicsio-emf/src/main/java/org/freehep/graphicsio/emf>
- <https://freehep.github.io/freehep-vectorgraphics/ReleaseNotes-2.0.html>
- <https://freehep.github.io/freehep-vectorgraphics/News.html>
- <https://raw.githubusercontent.com/freehep/freehep-vectorgraphics/master/LICENSE.txt>
- <https://raw.githubusercontent.com/freehep/freehep-vectorgraphics/master/LGPL.txt>

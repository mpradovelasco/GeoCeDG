# POST-E2-P2 closeout record

```text
POST-E2-P2 CHARACTERIZATION = PASS — AUTHOR APPROVED   (characterization and evidence;
                                                       no product correction)
POST-E2-P2 CORRECTION       = AUTHORIZED FOR IMPLEMENTATION (POST-E2-P2-R1, Option A)
POST-E2-P2 CORRECTION APPROVAL = PENDING
```

```text
selfApproved              = false
authorApproved            = true    (the P2 characterization only)
passClaimed               = true    (P2 characterization only)
implementationAuthorized  = POST-E2-P2-R1 bounded correction of the navigation-shortcut
                            validation (BOUNDED_PHASE, PHASE); its approval and
                            publication NOT AUTHORIZED; P3, P4, TD-PDF-1, TD-PDF-2,
                            E3, F1, F2, F3, G, G9B NOT AUTHORIZED
candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE
```

This record preserves the explicit author decision of 2026-10-09, given in writing
in the session ("Lo autorizo" confirming "GeoCeDG — POST-E2-P2 Author Approval,
Publication and Bounded Correction of Algebra Input Zoom Shortcuts"), with the exact
identities behind it. It is the author-decision record and the sole authority for
the approval of the `POST-E2-P2` characterization. Mirror:
[`post-e2-p2-closeout.json`](../../geocedg/validation/post-e2/post-e2-p2-closeout.json).
The [characterization report](post_e2_p2_characterization_report.md) and its
[evidence](../../geocedg/validation/post-e2/post-e2-p2-characterization-evidence.json)
are immutable historical evidence and are not amended.

## Entry verification

```text
live remote main = origin/main = local main
                 = b6747a1bf829051ff95a29c281558e00d3f82459 (R2 closeout)
branch           = phase/post-e2-p2-algebra-input-zoom, clean
history          = b6747a1b → 216964dc (characterization candidate); linear
```

## Author decision

```text
POST-E2-P2 candidate = 216964dc4ce8512e7b2be9587c8f93ca1d5a4355
                       tree 2ae1e51952c444abbadb2030dedc79142ad3f8eb
STATIC               = verification-f41470f4be54400ca5c0318dc47cefda, ACCEPTED / COMPLETE, 3/3
INFRA_UNIT           = verification-470baece3a484d2891e0d0877c00df65, ACCEPTED / COMPLETE, 22/22
PHASE                = verification-051515eb8e174681bd0a64ddacc6f66a, ACCEPTED / COMPLETE, 3/3
AUTHOR_APPROVAL      = POST-E2-P2 CHARACTERIZATION = PASS — AUTHOR APPROVED
```

Accepted classification:

```text
ROOT CAUSE           = GLOBAL MENU ACCELERATOR CONFLICT
TRIGGER              = TEXT-PRODUCING USER-ASSIGNED SHORTCUT
AFFECTED LAYER       = GEOCEDG DESKTOP
GEOMETRIC SEMANTICS  = UNCHANGED
UPSTREAM GEOGEBRA    = NOT RESPONSIBLE FOR THE GEOCEDG-SPECIFIC BINDING
```

- The author's preferences assign Shift+A to *Zoom Factor Out* and Shift+Z to
  *Zoom Factor In* (factor 10); the chords become Swing menu accelerators.
- An uppercase letter typed in Algebra Input reaches the window-level accelerator,
  which invokes the GeoCeDG navigation action and changes the view scale.
- Reproduced in a real windowed session; Shift+A alone reproduces it; the first
  argument is not itself responsible; configuration-dependent; fresh preferences and
  the author's file without the two bindings do not reproduce it; Classic does not;
  introduced by POST-G9U1-A7, not by E2 or R2; runtime stacks link the text-field
  event, the Swing accelerator and the navigation action.

## Selected corrective policy

```text
POST-E2-P2-R1 = Option A — reject keyboard shortcuts that can produce ordinary text
                (printable key with no modifier or with Shift only), owner
                GeoCeDGNavigationShortcutPreferences; stored inadmissible bindings
                are read as unassigned, non-destructively; the factor and any valid
                independent binding are preserved; Windows AltGr to be characterized
              = AUTHORIZED: IMPLEMENT AND VERIFY (BOUNDED_PHASE, PHASE);
                author approval and publication PENDING / NOT AUTHORIZED
```

Not authorized by this decision: changes to Algebra Input, the geometric kernel,
preview evaluation, viewport semantics, upstream text-field processing or the
global key dispatcher.

## Closeout verification

Documentary closeout (record, mirror, planning, mini-track and roadmap status);
`STATIC` runs on the closeout commit, which is reported outside this record with
the publication.

# CU7 R4 — integrated gesture and portable performance verification

Status: candidate design before test code, 2026-11-04. Source baseline is R3
00d65; the R4 continuation design remains authoritative. This adds verification
only, no production API or different geometric permission. Prepared externally
during the first compilation HOLD, this design is recorded in architecture
before the test implementation under the root's subsequent GO.

## Exact fixture and meaning

Use the independent MEET fixture unchanged: c=Circle((0,0),1), N=Point(c),
horizontal=Line(N,xAxis), vertical=Line(N,yAxis), Q=Intersect(horizontal,vertical),
L=LocusV2(Q,N). Desktop creation uses the facade's existing R2_H1 offer,
whose historical priority is preserved, with its explicit local-selection
permission and confirmed choice. The portable shared benchmark queries the
individual-forward API directly. Both construct normally from the admitted
complete address; neither supplies a manual address or converts a point role.
The first Desktop focal run exposed an incorrect new-fixture expectation of
the fallback route, before any gesture ran. Correcting that expectation does
not change admission, dispatch, permissions or the gesture assertions.

The request trajectory is the chord from the prior request near u=.11 to the
new request near u=1.91 with explicit radius .5. Its radial projection is
regular and its chord stays more than .62 from the origin. AWT input is integer
screen input: preserve and report the world target actually reconstructed from
the event, compare its semantic parameter to atan2 of those captured world
coordinates, and distinguish that from bit-exact command-level .11/1.91 tests.
Choose an explicit capture threshold and scale whose actual world radius is .5;
keep nonselected construction points hidden to avoid accidental hit candidates.

## Desktop permanent tests

New class org.geocedg.desktop.R4ContinuationGestureDesktopTest, three methods,
using existing real embedded Desktop/EDT/lifecycle and mouse helpers.

1. Create P from the admitted R2_H1 offer at .11, attach a normal DAG dependent, then
send press, exactly one large drag event, and release. Observe one P update and
one dependent update through the normal Kernel View notifications, retain IDs,
source/component/lift, and check the final address and forward coordinates.
Observe a new supported native transaction metrics record with no rollback DAG.
The source's one atomic publication boundary plus that observable single final
update is the snapshot-count argument; no nonexistent global snapshot counter
is claimed. Initialize and await native history; exactly one Undo restores the
initial address and identity, Redo restores the final address. Save/reopen a
.cedg copy and perform one large reverse drag on the restored point.
2. One request with an uncertifiable tail (near the circle's center, outside the
radius policy) retains the exact XML/address/dependent/identity/count/history.
A rejected proof emits no point DAG update and captures no new mutation record;
no intermediate certified link may have become a published point.
3. Throw a controlled exception from a point View.update callback after the
large chain has resolved and final publication has begun. The existing atomic
boundary restores the entire initial state and history, including the dependent;
no partial final gesture or new Undo remains.

The detailed final-link/source-change/cumulative-budget adversaries stay in the
kernel continuation tests. Desktop adds their integrated event/history boundary,
not a second geometric algorithm. AWT tests are not physical mouse evidence.

## Portable benchmark

New shared class org.geocedg.common.locus.R4ContinuationPerformanceTest. It uses
only R3 public APIs for all production calls, setup and native transaction
metrics. R4-only getters are read reflectively in this test: chart attempts,
accepted charts, request subdivisions, interval evaluations, correspondence
checks and similarity applications. An absent method is JSON null with an
explicit NOT_AVAILABLE state, never an invented zero. Invocation errors are
reported as real errors, not absence.

For each mode create an independent identical small document, P at .1, a normal
DAG dependent, and a context primed by the accepted request at .11. Time only the
public continuation call(s). Modes are one small request .11->.12, one large
request .11->1.91, and 24 requests at successive positions along the exact same
chord .11->1.91. Each mode has two warmups and nine measured samples. Setup,
reflection and report formatting stay outside the timed public-call interval.

Print all sample raw JSON and per-mode median/maximum summaries before asserting
R4 functional expectations. Thus R3 may fail the deliberately new large-event
expectation after preserving all three baseline modes. The root injects this
same test externally into the preserved R3 build; no R3 tracked source is edited.
Record source cohort and test content hash in that external execution receipt.

Report semantic evaluations, total proof budget units (getIntervalPasses),
optional R4 counters, resolution/DAG nanos, supported native capture/XML nanos,
and observed changes of the last transaction metrics record. That last count is
explicitly an observation per public call, not invented internal instrumentation.
Record P/dependent update notifications separately; normal updates and rollback
DAG calls have distinct meanings. Twenty-four external requests normally incur
24 publications/checkpoints, while one R4 chain publishes only its final state.
No performance threshold, p90, GUI latency or acceptance promotion is introduced.

## Selection and ownership

Append these two new exact filters only to R4.shared/R4.desktop and their
infrastructure assertions. Preserve R3 historical selections, XML/Undo contracts
and all diagnostic receipts. Expected identity counts remain provisional until
the root executes real producers and the canonical inventory updater.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED. GUIDE_IMPACT: no new behavior from these
tests; integrated R4 guides are owned by the root. VERIFICATION_INFRASTRUCTURE_IMPACT:
TEST_SELECTION. No Gradle, GUI, commits or canonical acceptance by this agent.

# PRE-G9B-D1 — next payload remediation plan candidate

Status: **PROPOSED, NOT AUTHORIZED**. This plan begins only after the author
disposes `D1-HD-05`; it does not begin P1 or COMMERCIAL-B.

## Objective

Close the six exact-byte blockers with the least behavioral change, rebuild the
same PROFILE NC closure, and present—not self-approve—a D1 deployability
candidate.

## Ordered work

1. **External evidence requests.** Seek exact artifact/source/build attestation
   for Giac 70501 (including statically incorporated GMP/MPFR), the complete
   math-cross-platform 3.6.3 fork grant, an OpenGeoProver redistribution grant,
   and byte-specific provenance/grants for the three TTFs. Do not disclose a
   package or contact any party without author authorization.
2. **Retain if evidence closes.** Add each exact grant/text/source identity to
   the existing manifests, regenerate no runtime bytes, rebuild, and run
   PACKAGING + STATIC.
3. **If evidence fails, design only.** Prepare per-component change proposals:
   pinned Giac rebuild/upgrade with CAS regression; documented
   math-cross-platform rebuild with numerical regression; OpenGeoProver
   exclusion/replacement with proving/fallback impact; font substitutions with
   glyph coverage/metrics/rendering baselines. Stop for author authorization.
4. **Execute only an authorized bounded alternative.** Change the declaration
   or build source—not generated package bytes—and apply the scientific/product
   gate implied by actual semantic impact.
5. **Rebuild closure.** Require 51 or explicitly dispositioned successor JARs,
   46 or explicitly dispositioned successor fonts, zero unresolved SBOM
   identities, exact legal manifest hashes, complete source access, and no
   excluded repository/reference material.
6. **Human closeout.** Present receipts plus D1-HD-01/02 adoption records for
   author/legal review. Do not set `selfApproved`, publish, tag, start P1, or
   declare D1 PASS.

## Automated controls already available

- Gradle component identity export joined to staged JARs by SHA-256;
- zero-`jsobject` closure assertion;
- external-artifact comparison with the immutable audit;
- 46-font SBOM/disposition correspondence;
- legal-bundle path/hash integrity;
- asset role/version/hash/provenance/license/trademark validation;
- app-image exclusion scan and MSI `.cedg` decompilation;
- canonical PACKAGING and STATIC profiles.

## Stop conditions

Stop before any change to CAS/algebra/numerical/prover/renderer behavior, any
substantial fork, any new runtime subsystem, or any license interpretation not
grounded in primary evidence. Such work needs an explicit author disposition
and a gate plan appropriate to the affected semantics.

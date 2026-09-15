# Unresolved payload license evidence

The following exact staged components do not yet have sufficient primary
evidence for redistribution. Generic project licensing is not treated as a
grant for bytes whose source correspondence is unproven.

| Exact payload | SHA-256 | Missing evidence | Smallest credible resolution |
|---|---|---|---|
| `javagiac-70501-natives-windows-amd64.jar` | `7dff713215cc645e8a843c11cc56bf9eadea1c15bf0cc59b99eec29ee154d33d` | Upstream attestation connecting the DLL to corresponding Giac/GMP/MPFR source and build | Obtain source/build attestation or authorize a pinned-source rebuild followed by algebra regression |
| `math-cross-platform-3.6.3.jar` | `f89cd36004b85e11d0c09118630106f87ddf0b9e3bfaef753b8c464a2fa8e933` | Complete fork grant: two GPL-2.0-with-Classpath-Exception files lack the referenced text and `Cloner.java` has no grant/provenance | Obtain the complete fork legal record or authorize a numerically validated rebuild/replacement |
| `OpenGeoProver-20120725.jar` | `3a75cb7ebf9abc6eff84cb85ae84d1d0fa9300da7d83ec6ae6983ddfd822faee` | A redistribution license from the copyright holders; recovered source says non-commercial use but provides no redistribution grant | Obtain written permission/license or separately authorize exclusion/replacement with prover regression |
| `jlm_cmmib10.ttf` | `5f160003632ae19f17d1fede8b86b67b258904e5d0a6907002dd2ef2d54f5a8a` | Exact upstream/derivation and grant | Obtain exact provenance/grant or authorize a metrics-tested renderer substitution |
| `jlm_cmssi10.ttf` | `1b7514b42f320f1b0821d78c9973d0bb115ff714830d5c88d368c962f6bf3e57` | Exact upstream/derivation and grant | Obtain exact provenance/grant or authorize a metrics-tested renderer substitution |
| `jlm_cmti10.ttf` | `59a8696017d3624ddd0e05397cf71c9855672b4466620ac550fcb9fe803e80cf` | Exact upstream/derivation and grant | Obtain exact provenance/grant or authorize a metrics-tested renderer substitution |

`jlm_special.ttf` is no longer unresolved: its exact bytes match the official
JLaTeXMath history and are covered by the recorded GPL-2.0-or-later license and
linking exception.

INTERNAL EVALUATION — NOT FOR REDISTRIBUTION

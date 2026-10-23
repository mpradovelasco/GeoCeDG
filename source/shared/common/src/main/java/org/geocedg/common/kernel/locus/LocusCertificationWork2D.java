/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Kinds of existence-certification and revision work counted by
 * {@link LocusInstrumentation2D} (PRE-G9B-CU2, gates GC-1 to GC-6 and
 * D-CU2-20-R). Counters decide the gates; wall-clock times only inform.
 *
 * <p>Slice reconstructions are the existing slice builds of
 * {@link LocusInstrumentation2D#getDependencySliceBuilds()}.</p>
 */
public enum LocusCertificationWork2D {
	/** One existence-certificate computation for a candidate revision. */
	CERTIFICATE_COMPUTATION,
	/** One existence box evaluated by a certifier for one branch. */
	EXISTENCE_BOX,
	/** One certified transition published by a certificate. */
	CERTIFIED_TRANSITION,
	/** One {@code UNRESOLVED} band published by a certificate. */
	UNRESOLVED_BAND,
	/**
	 * One run of valid cells reported as interior of a band of the inherited coincidence
	 * suppression, which a certificate closes at its band resolution (decision
	 * D-CU2-T13-R7).
	 */
	COINCIDENCE_BAND_CLOSURE,
	/** One exploratory traversal of an unresolved band. */
	EXPLORATORY_TRAVERSAL,
	/** One metric value computed or read while a certificate is computed. */
	METRIC_READ_DURING_CERTIFICATION,
	/** One reconstruction-slice fingerprint computation. */
	FINGERPRINT_COMPUTATION,
	/** One validation of a reconstructed slice against its live inputs. */
	FIDELITY_VALIDATION,
	/** One admitted restoration of a live input into a reconstructed slice. */
	FIDELITY_RESTORATION,
	/** One reconstructed slice found unfaithful to its live inputs. */
	FIDELITY_FAILURE,
	/**
	 * One settlement of the inherited selection of a reconstructed slice
	 * ({@code selection-settlement/v1}, D-CU2-T11-R1).
	 */
	SELECTION_SETTLEMENT,
	/** One auxiliary driver parameter computed twice by a selection settlement. */
	SELECTION_SETTLEMENT_PARAMETER,
	/** One selection settlement that did not reproduce the live selection state. */
	SELECTION_SETTLEMENT_FAILURE,
	/**
	 * One first initialization of a slice intersection completed by a point query and
	 * settled at the parameter of that query.
	 */
	SELECTION_DEPARTURE,
	/**
	 * One point query refused because the outputs of a departed intersection are not those
	 * of the selection rule of the revision.
	 */
	SELECTION_DEPARTURE_REFUSAL
}

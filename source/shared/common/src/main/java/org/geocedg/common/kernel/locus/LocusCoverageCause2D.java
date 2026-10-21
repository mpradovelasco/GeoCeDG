/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;

/**
 * Typed cause of one coverage assertion ({@code locus-v2-existence-components.md} section
 * 15.5). Certified nonexistence needs a predicate certified false or a dependency whose own
 * certificate establishes nonexistence; a computational failure that decides existence is
 * {@code UNRESOLVED}, never nonexistence.
 */
public enum LocusCoverageCause2D {
	/** Every validity predicate is certified true on the interval. */
	PREDICATES_CERTIFIED_TRUE(CoverageState.CERTIFIED_EXISTS),
	/** The provider's own contract asserts existence on the interval. */
	PROVIDER_CONTRACT(CoverageState.CERTIFIED_EXISTS),
	/** A validity predicate is certified false on the interval. */
	PREDICATE_CERTIFIED_FALSE(CoverageState.CERTIFIED_DOES_NOT_EXIST),
	/** A dependency's own certificate establishes nonexistence on the interval. */
	DEPENDENCY_CERTIFIED_NONEXISTENT(CoverageState.CERTIFIED_DOES_NOT_EXIST),
	/**
	 * Validity predicates that use no selection the slice copy does not reproduce are
	 * certified false on the whole canonical domain (normative precision D-CU2-T11-R1).
	 */
	SELECTION_INDEPENDENT_NONEXISTENCE(CoverageState.CERTIFIED_DOES_NOT_EXIST),
	/** The provider's own contract excludes the interval from its components. */
	PROVIDER_COMPLEMENT(CoverageState.CERTIFIED_DOES_NOT_EXIST),
	/** Existence holds at isolated parameters only, as point components. */
	ISOLATED_EVEN_ROOT(CoverageState.CERTIFIED_ISOLATED_POINTS),
	/** The provider's own contract asserts existence at an isolated parameter only. */
	PROVIDER_ISOLATED_POINT(CoverageState.CERTIFIED_ISOLATED_POINTS),
	/** The interval is the certified enclosure of one root that bounds a component. */
	CERTIFIED_BOUNDARY_ENCLOSURE(CoverageState.CERTIFIED_BOUNDARY),
	/** A versioned work budget was exhausted. */
	BUDGET_EXHAUSTED(CoverageState.UNRESOLVED),
	/** An operation of the slice is outside the certified construction class. */
	OPERATION_OUTSIDE_CLASS(CoverageState.UNRESOLVED),
	/** A degenerate configuration that the certificate cannot decide. */
	DEGENERATE_CONFIGURATION(CoverageState.UNRESOLVED),
	/** A dependency is undefined because its computation failed. */
	COMPUTATIONAL_DEPENDENCY_FAILURE(CoverageState.UNRESOLVED),
	/** Inherited behaviour that depends on a numerical tolerance. */
	INHERITED_TOLERANCE_BEHAVIOUR(CoverageState.UNRESOLVED),
	/** The slice does not reproduce the live selection state. */
	SELECTION_NOT_REPRODUCED(CoverageState.UNRESOLVED),
	/** An unlabelled output at coincident roots has a scattered effective definition. */
	UNLABELLED_COINCIDENT_OUTPUT(CoverageState.UNRESOLVED),
	/** The reconstructed slice is not faithful to its live inputs. */
	RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED(CoverageState.UNRESOLVED),
	/** An upstream locus does not establish the coverage this branch needs. */
	UPSTREAM_NOT_ESTABLISHED(CoverageState.UNRESOLVED),
	/** No certifier established the interval. */
	NOT_CERTIFIED(CoverageState.UNRESOLVED),
	/** A coverage assertion recorded before PRE-G9B-CU2, without a typed cause. */
	UNTYPED(null);

	private final CoverageState state;

	LocusCoverageCause2D(CoverageState state) {
		this.state = state;
	}

	/** @return whether this cause can justify a coverage of the given state */
	public boolean admits(CoverageState coverageState) {
		return state == null || state == coverageState;
	}
}

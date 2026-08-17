/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable resolution state and diagnostics for one published inert record. */
public final class SpatialRecordResolution {
	private final SpatialResolutionState state;
	private final List<SpatialIdentityDiagnostic> diagnostics;

	/** Creates immutable resolution evidence. */
	public SpatialRecordResolution(SpatialResolutionState state,
			List<SpatialIdentityDiagnostic> diagnostics) {
		this.state = Objects.requireNonNull(state);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	/** @return the singleton-shaped active resolution value */
	public static SpatialRecordResolution active() {
		return new SpatialRecordResolution(SpatialResolutionState.ACTIVE,
				Collections.<SpatialIdentityDiagnostic>emptyList());
	}

	private static List<SpatialIdentityDiagnostic> immutableDiagnostics(
			List<SpatialIdentityDiagnostic> diagnostics) {
		Objects.requireNonNull(diagnostics);
		ArrayList<SpatialIdentityDiagnostic> copy = new ArrayList<>();
		for (SpatialIdentityDiagnostic diagnostic : diagnostics) {
			copy.add(Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}

	public SpatialResolutionState getState() {
		return state;
	}

	public List<SpatialIdentityDiagnostic> getDiagnostics() {
		return diagnostics;
	}
}

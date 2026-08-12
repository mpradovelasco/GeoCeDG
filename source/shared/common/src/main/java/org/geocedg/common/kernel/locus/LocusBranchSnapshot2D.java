/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;

/** Immutable branch/domain publication prepared during a normal recompute. */
public final class LocusBranchSnapshot2D {
	private final DefinitionStatus definitionStatus;
	private final List<LocusBranch2D> branches;

	/** Creates one definition-level status plus its semantic branches. */
	public LocusBranchSnapshot2D(DefinitionStatus definitionStatus,
			List<LocusBranch2D> branches) {
		this.definitionStatus = Objects.requireNonNull(definitionStatus);
		Objects.requireNonNull(branches);
		ArrayList<LocusBranch2D> copy = new ArrayList<>();
		for (LocusBranch2D branch : branches) {
			copy.add(Objects.requireNonNull(branch));
		}
		this.branches = Collections.unmodifiableList(copy);
		if (definitionStatus == DefinitionStatus.VALID && this.branches.isEmpty()) {
			throw new IllegalArgumentException("A valid snapshot needs a branch");
		}
	}

	public DefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	public List<LocusBranch2D> getBranches() {
		return branches;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusBranchSnapshot2D)) {
			return false;
		}
		LocusBranchSnapshot2D snapshot = (LocusBranchSnapshot2D) other;
		return definitionStatus == snapshot.definitionStatus
				&& branches.equals(snapshot.branches);
	}

	@Override
	public int hashCode() {
		return Objects.hash(definitionStatus, branches);
	}
}

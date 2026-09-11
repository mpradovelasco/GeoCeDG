/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;

/** Typed, revision-bound constructor-occurrence provenance for SplineV2. */
public final class SplineConstructorOccurrenceResult2D {
	/** Closed result states; no state authorizes coordinate-based repair. */
	public enum Status {
		NO_ADDRESS,
		UNIQUE,
		MULTIPLE,
		UNRESOLVED_INVALID
	}

	/** One exact ordered-constructor occurrence and its current address. */
	public static final class Match {
		private final String occurrenceKey;
		private final int slot;
		private final LocusSemanticAddress2D address;

		Match(String occurrenceKey, int slot,
				LocusSemanticAddress2D address) {
			this.occurrenceKey = requireToken(occurrenceKey);
			if (slot < 0) {
				throw new IllegalArgumentException(
						"Constructor occurrence slot must be nonnegative");
			}
			this.slot = slot;
			this.address = Objects.requireNonNull(address);
		}

		public String getOccurrenceKey() {
			return occurrenceKey;
		}

		public int getSlot() {
			return slot;
		}

		public LocusSemanticAddress2D getAddress() {
			return address;
		}
	}

	private final Status status;
	private final long semanticRevision;
	private final List<Match> matches;
	private final String diagnostic;

	private SplineConstructorOccurrenceResult2D(Status status,
			long semanticRevision, List<Match> matches, String diagnostic) {
		this.status = Objects.requireNonNull(status);
		if (semanticRevision < 0) {
			throw new IllegalArgumentException(
					"Semantic revision cannot be negative");
		}
		this.semanticRevision = semanticRevision;
		ArrayList<Match> copy = new ArrayList<>();
		for (Match match : Objects.requireNonNull(matches)) {
			copy.add(Objects.requireNonNull(match));
		}
		this.matches = Collections.unmodifiableList(copy);
		this.diagnostic = requireText(diagnostic);
		if (status == Status.UNIQUE && copy.size() != 1
				|| status == Status.MULTIPLE && copy.size() < 2
				|| status != Status.UNIQUE && status != Status.MULTIPLE
						&& !copy.isEmpty()) {
			throw new IllegalArgumentException(
					"Occurrence result cardinality does not match its status");
		}
	}

	static SplineConstructorOccurrenceResult2D resolved(long revision,
			List<Match> matches) {
		return new SplineConstructorOccurrenceResult2D(
				matches.size() == 1 ? Status.UNIQUE : Status.MULTIPLE,
				revision, matches, matches.size() == 1
						? "Exactly one current constructor occurrence"
						: "Multiple current constructor occurrences");
	}

	static SplineConstructorOccurrenceResult2D noAddress(long revision,
			String diagnostic) {
		return new SplineConstructorOccurrenceResult2D(Status.NO_ADDRESS,
				revision, Collections.emptyList(), diagnostic);
	}

	static SplineConstructorOccurrenceResult2D unresolved(long revision,
			String diagnostic) {
		return new SplineConstructorOccurrenceResult2D(
				Status.UNRESOLVED_INVALID, revision, Collections.emptyList(),
				diagnostic);
	}

	public Status getStatus() {
		return status;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public List<Match> getMatches() {
		return matches;
	}

	public Match getUniqueMatch() {
		return status == Status.UNIQUE ? matches.get(0) : null;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	private static String requireToken(String value) {
		if (value == null || value.trim().isEmpty()
				|| !value.equals(value.trim())) {
			throw new IllegalArgumentException(
					"Occurrence key must be a canonical token");
		}
		return value;
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Occurrence diagnostic is required");
		}
		return value;
	}
}

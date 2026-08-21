/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

/**
 * Immutable fidelity classification for one revision-local source component.
 */
public final class SourceExportOutcome {

	/** Fidelity of one component relative to its authoritative source. */
	public enum Fidelity {
		EXACT,
		APPROXIMATE,
		UNSUPPORTED,
		INVALID
	}

	/** Orthogonal machine-readable explanation of an outcome. */
	public enum Reason {
		NONE,
		MISSING_DOMAIN,
		INVALID_DOMAIN,
		NON_FINITE,
		DISCONTINUITY_UNRESOLVED,
		TOLERANCE_NOT_ESTABLISHED,
		WORK_LIMIT,
		STALE_SOURCE_REVISION,
		UNSUPPORTED_FAMILY,
		UNDEFINED_SOURCE,
		NOT_2D,
		DEGENERATE_SOURCE,
		DUPLICATE_COMPONENT
	}

	/** Scope in which the reported source identifier remains stable. */
	public enum IdentityScope {
		PERSISTENT,
		CONSTRUCTION_REVISION
	}

	private final String sourceId;
	private final String sourceType;
	private final String label;
	private final long sourceRevision;
	private final boolean visible;
	private final IdentityScope identityScope;
	private final ComponentAddress componentAddress;
	private final Fidelity fidelity;
	private final Reason reason;
	private final String neutralEntityId;
	private final ApproximationEvidence approximationEvidence;
	private final String message;

	/**
	 * Creates a complete per-component outcome.
	 *
	 * @param sourceId source identifier in the declared identity scope
	 * @param sourceType machine-readable source family/type
	 * @param label optional source label
	 * @param sourceRevision captured non-negative source revision
	 * @param visible current source visibility carried for reporting
	 * @param identityScope stability scope of sourceId
	 * @param componentAddress revision-local component address
	 * @param fidelity exact, approximate, unsupported, or invalid
	 * @param reason orthogonal reason; NONE for emitted components
	 * @param neutralEntityId emitted neutral entity ID, otherwise null
	 * @param approximationEvidence evidence required for approximation
	 * @param message optional diagnostic detail
	 */
	public SourceExportOutcome(String sourceId, String sourceType, String label,
			long sourceRevision, boolean visible, IdentityScope identityScope,
			ComponentAddress componentAddress, Fidelity fidelity, Reason reason,
			String neutralEntityId,
			ApproximationEvidence approximationEvidence, String message) {
		this.sourceId = requireText(sourceId, "source id");
		this.sourceType = requireText(sourceType, "source type");
		this.label = optionalText(label, "label");
		if (sourceRevision < 0) {
			throw new IllegalArgumentException("source revision must be non-negative");
		}
		this.sourceRevision = sourceRevision;
		this.visible = visible;
		this.identityScope = require(identityScope, "identity scope");
		this.componentAddress = require(componentAddress, "component address");
		this.fidelity = require(fidelity, "fidelity");
		this.reason = require(reason, "reason");
		this.neutralEntityId = optionalText(neutralEntityId, "neutral entity id");
		this.approximationEvidence = approximationEvidence;
		this.message = optionalText(message, "message");
		validateState();
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getLabel() {
		return label;
	}

	public long getSourceRevision() {
		return sourceRevision;
	}

	public boolean isVisible() {
		return visible;
	}

	public IdentityScope getIdentityScope() {
		return identityScope;
	}

	public ComponentAddress getComponentAddress() {
		return componentAddress;
	}

	public Fidelity getFidelity() {
		return fidelity;
	}

	public Reason getReason() {
		return reason;
	}

	public String getNeutralEntityId() {
		return neutralEntityId;
	}

	public ApproximationEvidence getApproximationEvidence() {
		return approximationEvidence;
	}

	public String getMessage() {
		return message;
	}

	public boolean isEmitted() {
		return fidelity == Fidelity.EXACT || fidelity == Fidelity.APPROXIMATE;
	}

	private void validateState() {
		if (isEmitted()) {
			if (neutralEntityId == null) {
				throw new IllegalArgumentException(
						"An emitted outcome requires a neutral entity id");
			}
			if (reason != Reason.NONE) {
				throw new IllegalArgumentException(
						"An emitted outcome cannot carry a failure reason");
			}
		} else {
			if (neutralEntityId != null) {
				throw new IllegalArgumentException(
						"A non-emitted outcome cannot name a neutral entity");
			}
			if (reason == Reason.NONE) {
				throw new IllegalArgumentException(
						"A non-emitted outcome requires a reason");
			}
			if (message == null) {
				throw new IllegalArgumentException(
						"A non-emitted outcome requires diagnostic detail");
			}
		}
		if (fidelity == Fidelity.APPROXIMATE) {
			if (approximationEvidence == null
					|| !approximationEvidence.hasAchievedError()) {
				throw new IllegalArgumentException(
						"Approximation requires established error evidence");
			}
		} else if (fidelity == Fidelity.EXACT
				&& approximationEvidence != null) {
			throw new IllegalArgumentException(
					"Exact fidelity cannot carry approximation evidence");
		}
	}

	private static String optionalText(String value, String name) {
		if (value == null) {
			return null;
		}
		return requireText(value, name);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static <T> T require(T value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}

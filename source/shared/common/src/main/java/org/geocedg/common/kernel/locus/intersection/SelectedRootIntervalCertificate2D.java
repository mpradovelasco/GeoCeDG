/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Revision-local interval proof for one already allocated selected root.
 * The certificate refers to an exact token/selector and never allocates or
 * retargets root identity.
 */
public final class SelectedRootIntervalCertificate2D {
	private final String rootToken;
	private final IntersectionRootDeterministicSelector2D selector;
	private final long sourceSemanticRevision;
	private final LocusInterval2D driverInterval;
	private final LocusInterval2D rootParameterInterval;
	private final String method;
	private final String boundaryEvidence;
	private final int workCount;

	/** Creates one immutable interval-wide selected-root proof. */
	public SelectedRootIntervalCertificate2D(String rootToken,
			IntersectionRootDeterministicSelector2D selector,
			long sourceSemanticRevision, LocusInterval2D driverInterval,
			LocusInterval2D rootParameterInterval, String method,
			String boundaryEvidence, int workCount) {
		this.rootToken = requireText(rootToken, "Root token");
		this.selector = Objects.requireNonNull(selector);
		if (sourceSemanticRevision < 1 || workCount < 1) {
			throw new IllegalArgumentException(
					"Source revision and proof work must be positive");
		}
		this.sourceSemanticRevision = sourceSemanticRevision;
		this.driverInterval = Objects.requireNonNull(driverInterval);
		this.rootParameterInterval = Objects.requireNonNull(rootParameterInterval);
		this.method = requireText(method, "Interval proof method");
		this.boundaryEvidence = requireText(boundaryEvidence,
				"Boundary evidence");
		this.workCount = workCount;
	}

	/** @return exact retained root token certified by this interval evidence */
	public String getRootToken() {
		return rootToken;
	}

	/** @return existing deterministic selector; no new root identity is allocated */
	public IntersectionRootDeterministicSelector2D getSelector() {
		return selector;
	}

	/** @return exact semantic revision of the selected-root source */
	public long getSourceSemanticRevision() {
		return sourceSemanticRevision;
	}

	/** @return driver interval certified by this evidence */
	public LocusInterval2D getDriverInterval() {
		return driverInterval;
	}

	/** @return structural source-root chart used by the interval proof */
	public LocusInterval2D getRootParameterInterval() {
		return rootParameterInterval;
	}

	/** @return versioned interval-proof method */
	public String getMethod() {
		return method;
	}

	/** @return explicit boundary/maximality evidence */
	public String getBoundaryEvidence() {
		return boundaryEvidence;
	}

	/** @return deterministic bounded proof work */
	public int getWorkCount() {
		return workCount;
	}

	/** @return revision-local evidence key, not durable component identity */
	public String getEvidenceKey() {
		return "selected-root-interval/v1|root-token=" + rootToken
				+ "|selector=" + selector.toExternalForm()
				+ "|source-revision=" + sourceSemanticRevision + "|driver="
				+ driverInterval + "|root-domain=" + rootParameterInterval
				+ "|method=" + method + "|work=" + workCount;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()
				|| !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be canonical");
		}
		return value;
	}
}

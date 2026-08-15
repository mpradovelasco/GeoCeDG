/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;

/** Immutable query-local binding of one semantic locus revision. */
public final class LocusSourceBinding2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String providerId;
	private final String parameterDescriptor;
	private final boolean periodic;

	/** Captures one current semantic definition without retaining mutable state. */
	public LocusSourceBinding2D(LocusDefinition2D definition) {
		java.util.Objects.requireNonNull(definition);
		this.locusIdentity = definition.getLocusIdentity();
		this.semanticRevision = definition.getSemanticRevision();
		this.providerId = definition.getProvider().getProviderId();
		this.parameterDescriptor = definition.getProvider()
				.getParameterDescriptor();
		this.periodic = definition.getProvider().isPeriodic();
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public String getProviderId() {
		return providerId;
	}

	public String getParameterDescriptor() {
		return parameterDescriptor;
	}

	public boolean isPeriodic() {
		return periodic;
	}
}

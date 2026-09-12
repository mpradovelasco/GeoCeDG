/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Complete-or-fail-closed semantic impact of an offered legacy replacement. */
public final class SpatialRedefineImpactReport {
	/** Whether the kernel established the complete known CeDG downstream closure. */
	public enum Completeness {
		IMPACT_COMPLETE,
		IMPACT_NOT_ESTABLISHED
	}

	private final Completeness completeness;
	private final List<SpatialRedefineImpactEntry> entries;
	private final String detail;

	SpatialRedefineImpactReport(Completeness completeness,
			List<SpatialRedefineImpactEntry> entries, String detail) {
		this.completeness = java.util.Objects.requireNonNull(completeness);
		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
		this.detail = detail;
	}

	public Completeness getCompleteness() {
		return completeness;
	}

	public List<SpatialRedefineImpactEntry> getEntries() {
		return entries;
	}

	/** @return diagnostic detail when completeness was not established */
	public String getDetail() {
		return detail;
	}
}

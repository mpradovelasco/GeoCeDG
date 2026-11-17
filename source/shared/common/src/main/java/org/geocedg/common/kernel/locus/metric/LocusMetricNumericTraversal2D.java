/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.metric;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/** Immutable P2 numeric traversal evidence; supplies no geometric authority. */
public final class LocusMetricNumericTraversal2D {
	public static final String VERSION = "partial-conditioned-estimate/v1";
	private final LocusBranch2D branch;
	private final MetricCoverage numericCoverage;
	private final long revision;
	LocusMetricNumericTraversal2D(LocusBranch2D branch, MetricCoverage numericCoverage,
			long revision) {
		this.branch = branch;
		this.numericCoverage = numericCoverage;
		this.revision = revision;
	}
	public String getPolicyVersion() { return VERSION; }
	public String getBranchKey() { return branch.getBranchKey(); }
	public LocusInterval2D getRequestedDomain() { return branch.getDeclaredDriverDomain(); }
	public Orientation getOrientation() { return branch.getOrientation(); }
	public long getSemanticRevision() { return revision; }
	public MetricCoverage getNumericCoverage() { return numericCoverage; }
	public LocusExistenceStructure2D getGeometricExistence() {
		return branch.getExistenceStructure();
	}
	@Override public String toString() {
		return VERSION + " branch=" + getBranchKey() + " domain=" + getRequestedDomain()
				+ " orientation=" + getOrientation() + " numeric=" + numericCoverage
				+ " geometry=" + getGeometricExistence().getCompleteness() + " revision=" + revision;
	}
}

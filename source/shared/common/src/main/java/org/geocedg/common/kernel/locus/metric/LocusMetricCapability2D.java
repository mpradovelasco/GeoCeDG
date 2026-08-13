/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/** Explicit capability seam; no class-name or render-data inference is allowed. */
public interface LocusMetricCapability2D
		extends LocusMetricComponentEvaluator2D {
	/** @return version participating in every complete index key */
	String getCapabilityVersion();

	/** @return evaluator capability kind */
	MetricEvaluatorMethod2D getEvaluatorMethod();

	/** @return whether this capability supports the component and policy */
	boolean supports(LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy);

	/** @return immutable complete-component state */
	LocusMetricComponentState2D buildComponentState(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation);
}

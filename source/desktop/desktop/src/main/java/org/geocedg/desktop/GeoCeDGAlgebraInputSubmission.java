/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.parser.ParseException;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.main.AppD;

/** Explicit input-bar submission adapter; identity/transactions stay in G9A. */
public final class GeoCeDGAlgebraInputSubmission {

	private GeoCeDGAlgebraInputSubmission() {
	}

	/**
	 * Submit exactly once through the normal host command or explicit-edit seam.
	 * A parsed label locates the intended current target, never its durable ID.
	 *
	 * @param app product application
	 * @param input explicit user submission
	 * @param info normal input evaluation policy
	 * @param errors normal input error handler
	 * @param callback receives the ordinary command result
	 * @throws ParseException invalid syntax before any productive operation
	 */
	public static void submit(AppD app, String input, EvalInfo info,
			ErrorHandler errors, AsyncOperation<GeoElementND[]> callback)
			throws ParseException {
		AlgebraProcessor processor = app.getKernel().getAlgebraProcessor();
		ValidExpression expression = processor.getValidExpressionNoExceptionHandling(input);
		String label = expression.getLabel();
		GeoElement target = label == null ? null : app.getKernel().lookupLabel(label);
		if (target == null) {
			// Snapshot membership only to distinguish a new explicit query from an
			// existing result reference. This is not token or geometric identity.
			Set<GeoElement> existing = Collections.newSetFromMap(new IdentityHashMap<>());
			existing.addAll(app.getKernel().getConstruction().getGeoSetConstructionOrder());
			processor.processAlgebraCommandNoExceptionHandling(input, false,
					errors, info, result -> {
						if (result == null) {
							callback.callback(null);
							return;
						}
						try {
							callback.callback(result);
							for (GeoElementND output : result) {
								if (output instanceof GeoLocusIntersectionResult
										&& !existing.contains(output.toGeoElement())) {
									GeoCeDGEuclidianController controller =
											(GeoCeDGEuclidianController) app.getEuclidianView1()
													.getEuclidianController();
									controller.afterExplicitIntersectionCreation(
											(GeoLocusIntersectionResult) output);
								}
							}
						} finally {
							// One explicit input transaction includes its opt-in points.
							// Do not race two asynchronous Desktop history stores.
							app.storeUndoInfo();
						}
					});
			return;
		}
		if (!target.isAlgebraViewEditable()) {
			errors.showError(app.getLocalization().getInvalidInputError());
			return;
		}
		processor.changeGeoElementNoExceptionHandling(target, input,
				info.withLabelRedefinitionAllowedFor(label), true,
				result -> callback.callback(result == null ? null
						: new GeoElementND[] {result}), errors);
	}
}

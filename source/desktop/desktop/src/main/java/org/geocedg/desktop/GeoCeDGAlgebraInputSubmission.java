/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.parser.ParseException;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.main.error.ErrorLogger;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.debug.Log;
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
		if (app instanceof AppGeoCeDG geoCeDG && geoCeDG.isInsertionIsolated()) {
			// PRE-G9B-R6-plus-F3 P7: no input reaches an isolated drawing
			callback.callback(null);
			return;
		}
		AlgebraProcessor processor = app.getKernel().getAlgebraProcessor();
		ValidExpression expression = processor.getValidExpressionNoExceptionHandling(input);
		String label = expression.getLabel();
		GeoElement target = label == null ? null : app.getKernel().lookupLabel(label);
		if (target == null) {
			submitNew(app, input, info, errors, callback, false);
			return;
		}
		if (!target.isAlgebraViewEditable()
				&& !GeoCeDGDefinitionInspector.isSemanticRedefineEnabled(target)) {
			errors.showError(app.getLocalization().getInvalidInputError());
			return;
		}
		processor.changeGeoElementNoExceptionHandling(target, input,
				info.withLabelRedefinitionAllowedFor(label), true,
				result -> callback.callback(result == null ? null
						: new GeoElementND[] {result}), errors);
	}

	/**
	 * A new explicit query. PRE-G9B-CU3 (V-AUT-ADOPT-1): when a form of {@code CU3} is
	 * refused because the document is governed by the legacy policy, the refusal is held back
	 * until the processor has returned; the explicit adoption event is then offered once, and
	 * the input is submitted again after the adoption, or the typed refusal is shown. The
	 * adoption and the input submitted again are one explicit input transaction with one undo
	 * point, also when that input ends without a result.
	 *
	 * @param adopted whether this submission follows an accepted adoption
	 */
	private static void submitNew(AppD app, String input, EvalInfo info, ErrorHandler errors,
			AsyncOperation<GeoElementND[]> callback, boolean adopted) {
		AlgebraProcessor processor = app.getKernel().getAlgebraProcessor();
		AuthorityRefusal refusal = app instanceof AppGeoCeDG && !adopted
				? new AuthorityRefusal(errors) : null;
		boolean[] completed = {false};
		// Snapshot membership only to distinguish a new explicit query from an
		// existing result reference. This is not token or geometric identity.
		Set<GeoElement> existing = Collections.newSetFromMap(new IdentityHashMap<>());
		existing.addAll(app.getKernel().getConstruction().getGeoSetConstructionOrder());
		processor.processAlgebraCommandNoExceptionHandling(input, false,
				refusal == null ? errors : refusal, info, result -> {
					if (result == null && refusal != null && refusal.isHeld()) {
						// A processor that went on after a question of its own reports the
						// refusal only now.
						if (refusal.claim()) {
							offerAdoption(app, input, info, errors, callback, refusal);
						}
						return;
					}
					completed[0] = true;
					if (result == null) {
						if (adopted) {
							// The adoption stays, as the whole transaction.
							app.storeUndoInfo();
						}
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
		if (refusal != null) {
			refusal.markReturned();
			if (refusal.claim()) {
				offerAdoption(app, input, info, errors, callback, refusal);
			}
		} else if (adopted && !completed[0]) {
			// Desktop product submissions complete synchronously: an input that ended without
			// reporting, as after a declined question, still ends the transaction of the
			// adoption with its undo point.
			app.storeUndoInfo();
		}
	}

	private static void offerAdoption(AppD app, String input, EvalInfo info,
			ErrorHandler errors, AsyncOperation<GeoElementND[]> callback,
			AuthorityRefusal refusal) {
		if (((AppGeoCeDG) app).offerConstructiveAdoption()) {
			submitNew(app, input, info, errors, callback, true);
			return;
		}
		refusal.release();
		callback.callback(null);
	}

	/**
	 * Holds back the refusal {@value AuthorityPolicyAdoption#REQUIRED_CAUSE} of a form of
	 * {@code CU3}; every other error goes through unchanged.
	 */
	private static final class AuthorityRefusal implements ErrorLogger {
		private final ErrorHandler delegate;
		private Throwable heldCause;
		private String heldCommand;
		private String heldMessage;
		private boolean returned;
		private boolean claimed;

		private AuthorityRefusal(ErrorHandler delegate) {
			this.delegate = delegate;
		}

		private static boolean isRefusal(String message) {
			return message != null && message.startsWith(AuthorityPolicyAdoption.REQUIRED_CAUSE);
		}

		private boolean isHeld() {
			return heldMessage != null;
		}

		private void markReturned() {
			returned = true;
		}

		/** @return whether the held refusal is answered now: once, after the processor */
		private boolean claim() {
			if (!returned || !isHeld() || claimed) {
				return false;
			}
			claimed = true;
			return true;
		}

		/** Shows the typed refusal, as the processor would have. */
		private void release() {
			if (heldCause != null && delegate instanceof ErrorLogger) {
				((ErrorLogger) delegate).log(heldCause);
			}
			if (heldCommand != null) {
				delegate.showCommandError(heldCommand, heldMessage);
			} else {
				delegate.showError(heldMessage);
			}
		}

		@Override
		public void log(Throwable error) {
			if (isRefusal(error.getMessage())) {
				heldCause = error;
			} else if (delegate instanceof ErrorLogger) {
				((ErrorLogger) delegate).log(error);
			} else {
				Log.debug(error);
			}
		}

		@Override
		public void showError(String message) {
			if (isRefusal(message)) {
				heldMessage = message;
				return;
			}
			delegate.showError(message);
		}

		@Override
		public void showCommandError(String command, String message) {
			if (isRefusal(message)) {
				heldCommand = command;
				heldMessage = message;
				return;
			}
			delegate.showCommandError(command, message);
		}

		@Override
		public String getCurrentCommand() {
			return delegate.getCurrentCommand();
		}

		@Override
		public boolean onUndefinedVariables(String string,
				AsyncOperation<String[]> callback) {
			return delegate.onUndefinedVariables(string, callback);
		}

		@Override
		public void resetError() {
			delegate.resetError();
		}
	}
}

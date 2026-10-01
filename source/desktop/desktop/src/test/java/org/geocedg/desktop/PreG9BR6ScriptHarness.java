/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.parser.ParseException;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.main.AppD;

/**
 * PRE-G9B-R6 test host helpers. They select the real host entry points (the Desktop
 * Algebra-input submission adapter, click scripts through the event dispatcher and
 * nested {@code Execute}) and replace only the modal error presentation, which is
 * recorded instead of shown. They hold no command identity or geometric fact.
 */
final class PreG9BR6ScriptHarness {

	/** The Spanish UI language of the locale dimension. */
	static final Locale SPANISH = Locale.forLanguageTag("es");

	private PreG9BR6ScriptHarness() {
		// utility class
	}

	/**
	 * @param language UI language
	 * @return a GeoCeDG host whose language went through the host language lifecycle
	 */
	static AppGeoCeDG application(Locale language) {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLanguage(language);
		app.getCommandDictionary();
		return app;
	}

	/**
	 * Installs a replacement for the modal default error presentation.
	 *
	 * @param app host
	 * @param handler recording presentation
	 * @return restores the previous presentation
	 * @throws ReflectiveOperationException if the host field is unavailable
	 */
	static AutoCloseable install(AppD app, ErrorHandler handler)
			throws ReflectiveOperationException {
		Field field = AppD.class.getDeclaredField("defaultErrorHandler");
		field.setAccessible(true);
		Object previous = field.get(app);
		field.set(app, handler);
		return () -> field.set(app, previous);
	}

	/**
	 * Explicit Algebra Input submission with the flags of the Desktop input bar.
	 *
	 * @param app host
	 * @param input typed text
	 * @param errors input-bar error handler
	 * @return created outputs, or null when the submission failed
	 * @throws ParseException invalid syntax
	 */
	static GeoElementND[] submit(AppD app, String input, ErrorHandler errors)
			throws ParseException {
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();
		EvalInfo info = new EvalInfo(true, true).withSliders(true)
				.addDegree(app.getKernel().getAngleUnitUsesDegrees()).withSymbolic(true);
		GeoCeDGAlgebraInputSubmission.submit(app, input, info, errors, output::set);
		return output.get();
	}

	/**
	 * @param app host
	 * @return the construction XML without listeners
	 */
	static String constructionXml(AppD app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, false);
		return xml.toString();
	}

	/** Records presentation calls; optionally fails the presentation itself. */
	static final class Sink implements ErrorHandler {
		private final List<String> messages = new ArrayList<>();
		private Throwable failure;

		/**
		 * @param presentationFailure thrown by every later presentation call, or null
		 */
		void failWith(Throwable presentationFailure) {
			this.failure = presentationFailure;
		}

		List<String> messages() {
			return messages;
		}

		String joined() {
			return String.join("\n", messages);
		}

		void clear() {
			messages.clear();
		}

		@Override
		public void showError(String msg) {
			messages.add(String.valueOf(msg));
			presentationFailure();
		}

		@Override
		public void showCommandError(String command, String message) {
			messages.add(String.valueOf(message));
			presentationFailure();
		}

		private void presentationFailure() {
			if (failure instanceof Error) {
				throw (Error) failure;
			}
			if (failure instanceof RuntimeException) {
				throw (RuntimeException) failure;
			}
		}

		@Override
		public String getCurrentCommand() {
			return null;
		}

		@Override
		public boolean onUndefinedVariables(String string,
				AsyncOperation<String[]> callback) {
			return false;
		}

		@Override
		public void resetError() {
			// nothing to reset
		}
	}
}

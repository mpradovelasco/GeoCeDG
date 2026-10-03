/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.units.CanonicalBinary64;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;

/**
 * PRE-G9B-R6-plus-D1 Document Units dialog model (DQ-D1-1, DQ-D1-8). It maps the
 * document state to dialog fields and validates a request into exactly one target
 * state, or into one localized error key; it never changes the document. The factor
 * field uses the unit-system section 8.4 grammar with "." as the decimal separator; a
 * decimal comma is rejected with a hint and never converted.
 */
final class GeoCeDGDocumentUnits {

	private GeoCeDGDocumentUnits() {
	}

	/** Dialog fields; text fields are raw user input. */
	static final class Request {
		/** construction unit, or {@code null} for unspecified */
		final UnitToken construction;
		/** presentation unit, or {@code null} to follow the construction unit */
		final UnitToken presentation;
		final boolean usmDefined;
		final String factor;
		final String name;
		final String symbol;

		Request(UnitToken construction, UnitToken presentation, boolean usmDefined,
				String factor, String name, String symbol) {
			this.construction = construction;
			this.presentation = presentation;
			this.usmDefined = usmDefined;
			this.factor = factor == null ? "" : factor;
			this.name = name == null ? "" : name;
			this.symbol = symbol == null ? "" : symbol;
		}
	}

	/** Validation result: a target state, or an error key. */
	static final class Result {
		final UnitState state;
		final String errorKey;

		private Result(UnitState state, String errorKey) {
			this.state = state;
			this.errorKey = errorKey;
		}

		boolean isValid() {
			return state != null;
		}
	}

	static Request fromState(UnitState state) {
		UsmDefinition usm = state.getUsm();
		return new Request(state.getConstructionSelection(),
				state.getPresentationSelection(), usm != null,
				usm == null ? "" : usm.canonicalFactor(),
				usm == null || usm.getName() == null ? "" : usm.getName(),
				usm == null || usm.getSymbol() == null ? "" : usm.getSymbol());
	}

	static Result validate(Request request, UnitState current) {
		UsmDefinition usm = null;
		if (request.usmDefined) {
			if (request.factor.indexOf(',') >= 0) {
				return error("Units.Error.FactorComma");
			}
			Double factor = CanonicalBinary64.tryParse(request.factor);
			if (factor == null) {
				return error("Units.Error.Factor");
			}
			String name = request.name.isEmpty() ? null : request.name;
			String symbol = request.symbol.isEmpty() ? null : request.symbol;
			if ((name != null && !UsmDefinition.isValidLabel(name))
					|| (symbol != null && !UsmDefinition.isValidLabel(symbol))) {
				return error("Units.Error.Label");
			}
			usm = UsmDefinition.of(factor, name, symbol);
		}
		if (request.presentation != null && request.construction == null) {
			return error("Units.Error.PresentationWithoutConstruction");
		}
		if ((request.construction == UnitToken.USM
				|| request.presentation == UnitToken.USM) && usm == null) {
			// section 4.3: remove only while neither unit selects usm
			return error(current.getUsm() != null ? "Units.Error.UsmInUse"
					: "Units.Error.UsmUndefined");
		}
		return new Result(UnitState.of(request.construction, request.presentation, usm),
				null);
	}

	private static Result error(String key) {
		return new Result(null, key);
	}
}

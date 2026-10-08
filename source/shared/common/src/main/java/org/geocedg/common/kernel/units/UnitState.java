/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.util.Objects;

/**
 * Immutable document unit state (unit-system v1.0, section 5.1):
 * {@code {c : unit-token?, p : unit-token?, usm : NONE | DEFINED(k, name?, symbol?)}}.
 *
 * <p>It is document metadata, never geometry: no algorithm reads it while computing
 * (section 6.1). The methods {@link #effectiveConstructionUnit()},
 * {@link #effectivePresentationUnit()} and {@link #metresPerUnit(UnitToken)} are the
 * single effective-unit function of section 5.2; every consumer derives its units from
 * them. Every instance satisfies both state constraints: a presentation selection
 * requires a construction selection, and a selected {@code usm} requires a definition.
 *
 * <p>Unit-system v1.1 (PRE-G9B-R6-plus-E2 smoke follow-up B2) adds one document
 * presentation policy, the dimension unit suffix (section 14.2): shown, the historical
 * behavior of a document without the policy, or hidden. It changes no unit and no
 * effective-unit function.
 */
public final class UnitState {
	/** No unit metadata: {@code UNSPECIFIED_MODEL_UNIT}, nothing serialized. */
	public static final UnitState EMPTY = new UnitState(null, null, null, true);

	private final UnitToken construction;
	private final UnitToken presentation;
	private final UsmDefinition usm;
	private final boolean dimensionUnitSuffixShown;

	private UnitState(UnitToken construction, UnitToken presentation, UsmDefinition usm,
			boolean dimensionUnitSuffixShown) {
		this.construction = construction;
		this.presentation = presentation;
		this.usm = usm;
		this.dimensionUnitSuffixShown = dimensionUnitSuffixShown;
	}

	/**
	 * @param construction explicit construction-unit selection, or {@code null}
	 * @param presentation explicit presentation-unit selection, or {@code null}
	 * @param usm the usm definition, or {@code null} for {@code NONE}
	 * @return the state, with the dimension unit suffix shown
	 * @throws IllegalArgumentException when a state constraint of section 5.1 fails
	 */
	public static UnitState of(UnitToken construction, UnitToken presentation,
			UsmDefinition usm) {
		return of(construction, presentation, usm, true);
	}

	/**
	 * @param construction explicit construction-unit selection, or {@code null}
	 * @param presentation explicit presentation-unit selection, or {@code null}
	 * @param usm the usm definition, or {@code null} for {@code NONE}
	 * @param dimensionUnitSuffixShown dimension unit suffix policy (section 14.2)
	 * @return the state
	 * @throws IllegalArgumentException when a state constraint of section 5.1 fails
	 */
	public static UnitState of(UnitToken construction, UnitToken presentation,
			UsmDefinition usm, boolean dimensionUnitSuffixShown) {
		if (presentation != null && construction == null) {
			throw new IllegalArgumentException(
					"A presentation unit requires a construction unit");
		}
		if ((construction == UnitToken.USM || presentation == UnitToken.USM)
				&& usm == null) {
			throw new IllegalArgumentException("usm is selected but not defined");
		}
		if (construction == null && usm == null && dimensionUnitSuffixShown) {
			return EMPTY;
		}
		return new UnitState(construction, presentation, usm, dimensionUnitSuffixShown);
	}

	/**
	 * @return whether no unit metadata exists (nothing is serialized)
	 */
	public boolean isEmpty() {
		return construction == null && presentation == null && usm == null
				&& dimensionUnitSuffixShown;
	}

	/**
	 * @return whether a construction or presentation unit is selected or the usm is
	 *         defined, the unit metadata of version 1 (section 5.1)
	 */
	public boolean hasUnitMetadata() {
		return construction != null || presentation != null || usm != null;
	}

	/**
	 * Section 14.2 dimension unit suffix policy; {@code true} also for a document that
	 * carries no policy (the historical behavior).
	 *
	 * @return whether native dimension texts show the presentation-unit suffix
	 */
	public boolean isDimensionUnitSuffixShown() {
		return dimensionUnitSuffixShown;
	}

	/**
	 * Section 7.1a: set the dimension unit suffix policy; units are unchanged.
	 *
	 * @param shown whether native dimension texts show the suffix
	 * @return the new state
	 */
	public UnitState withDimensionUnitSuffixShown(boolean shown) {
		return of(construction, presentation, usm, shown);
	}

	/**
	 * @return explicit construction-unit selection, or {@code null}
	 */
	public UnitToken getConstructionSelection() {
		return construction;
	}

	/**
	 * @return explicit presentation-unit selection, or {@code null}
	 */
	public UnitToken getPresentationSelection() {
		return presentation;
	}

	/**
	 * @return the usm definition, or {@code null} when it is {@code NONE}
	 */
	public UsmDefinition getUsm() {
		return usm;
	}

	/**
	 * Section 5.2 {@code effC}.
	 *
	 * @return effective construction unit, or {@code null} for
	 *         {@code UNSPECIFIED_MODEL_UNIT}
	 */
	public UnitToken effectiveConstructionUnit() {
		return construction;
	}

	/**
	 * Section 5.2 {@code effP}.
	 *
	 * @return effective presentation unit, or {@code null} for {@code NONE}
	 */
	public UnitToken effectivePresentationUnit() {
		if (construction == null) {
			return null;
		}
		return presentation == null ? construction : presentation;
	}

	/**
	 * @return whether the effective construction unit is physical
	 */
	public boolean isPhysical() {
		return construction != null;
	}

	/**
	 * Section 5.2 {@code fb(u)}.
	 *
	 * @param unit a physical unit
	 * @return its binary64 metre factor in this state
	 * @throws IllegalStateException for usm when it is not defined
	 */
	public double metresPerUnit(UnitToken unit) {
		if (unit.isBuiltIn()) {
			return unit.builtInMetresPerUnit();
		}
		if (usm == null) {
			throw new IllegalStateException("usm is not defined");
		}
		return usm.getMetresPerUnit();
	}

	/**
	 * @return the effective construction metre factor, or {@code NaN} when unspecified
	 */
	public double effectiveConstructionMetresPerUnit() {
		return construction == null ? Double.NaN : metresPerUnit(construction);
	}

	/**
	 * @param unit a physical unit
	 * @return its presentation symbol in this state: the token, or the usm display
	 *         symbol
	 */
	public String symbolOf(UnitToken unit) {
		return unit == UnitToken.USM && usm != null ? usm.displaySymbol() : unit.token();
	}

	/**
	 * Section 5.3 {@code display(L) = L * fb(effC) / fb(effP)}.
	 *
	 * @param modelLength length in model units
	 * @return the quantity expressed in the effective presentation unit
	 */
	public UnitQuantity display(double modelLength) {
		UnitToken presentationUnit = effectivePresentationUnit();
		if (presentationUnit == null) {
			return UnitQuantity.UNSPECIFIED;
		}
		return UnitQuantity.of(modelLength * metresPerUnit(construction)
				/ metresPerUnit(presentationUnit));
	}

	/**
	 * Section 5.3 {@code physical(L) = L * fb(effC)}.
	 *
	 * @param modelLength length in model units
	 * @return the length in metres
	 */
	public UnitQuantity physical(double modelLength) {
		if (construction == null) {
			return UnitQuantity.UNSPECIFIED;
		}
		return UnitQuantity.of(modelLength * metresPerUnit(construction));
	}

	/**
	 * Section 5.4: equal binary64 effective construction factors, both physical; the
	 * unspecified state equals nothing, itself included.
	 *
	 * @param first a state
	 * @param second a state
	 * @return whether both states give model units the same physical meaning
	 */
	public static boolean samePhysicalMeaning(UnitState first, UnitState second) {
		return first.isPhysical() && second.isPhysical()
				&& first.effectiveConstructionMetresPerUnit()
						== second.effectiveConstructionMetresPerUnit();
	}

	/**
	 * Section 7.1: set the construction unit, or {@code null} for
	 * {@code UNSPECIFIED_MODEL_UNIT}, which removes the presentation selection and keeps
	 * the usm definition.
	 *
	 * @param unit construction unit or {@code null}
	 * @return the new state
	 * @throws IllegalArgumentException when usm is selected but not defined
	 */
	public UnitState withConstructionUnit(UnitToken unit) {
		return of(unit, unit == null ? null : presentation, usm, dimensionUnitSuffixShown);
	}

	/**
	 * Section 7.1: set or clear the explicit presentation unit.
	 *
	 * @param unit presentation unit, or {@code null} to follow the construction unit
	 * @return the new state
	 * @throws IllegalArgumentException when the construction unit is unspecified and a
	 *         unit is given, or when usm is selected but not defined
	 */
	public UnitState withPresentationUnit(UnitToken unit) {
		return of(construction, unit, usm, dimensionUnitSuffixShown);
	}

	/**
	 * Section 4.3: define the usm, or change its factor, name or symbol.
	 *
	 * @param definition the definition
	 * @return the new state
	 */
	public UnitState withUsm(UsmDefinition definition) {
		return of(construction, presentation, Objects.requireNonNull(definition),
				dimensionUnitSuffixShown);
	}

	/**
	 * Section 4.3: remove the usm definition.
	 *
	 * @return the new state
	 * @throws IllegalArgumentException while a stored unit selects usm
	 */
	public UnitState withoutUsm() {
		return of(construction, presentation, null, dimensionUnitSuffixShown);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UnitState)) {
			return false;
		}
		UnitState state = (UnitState) other;
		return construction == state.construction && presentation == state.presentation
				&& Objects.equals(usm, state.usm)
				&& dimensionUnitSuffixShown == state.dimensionUnitSuffixShown;
	}

	@Override
	public int hashCode() {
		return Objects.hash(construction, presentation, usm, dimensionUnitSuffixShown);
	}

	@Override
	public String toString() {
		return "UnitState[c=" + (construction == null ? "-" : construction.token())
				+ ", p=" + (presentation == null ? "-" : presentation.token()) + ", usm="
				+ (usm == null ? "NONE" : usm)
				+ (dimensionUnitSuffixShown ? "" : ", dimensionUnitSuffix=hidden") + "]";
	}
}

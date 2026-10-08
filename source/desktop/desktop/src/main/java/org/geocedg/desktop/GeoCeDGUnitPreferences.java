/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;

/**
 * PRE-G9B-R6-plus-D1 portable user defaults for new blank documents (unit-system v1.0,
 * section 10; DQ-D1-2, DQ-D1-6), and since unit-system v1.1 the dimension unit suffix
 * policy of new documents. They live in the GeoCeDG properties store, never in
 * the preferences XML or a document, are never {@code usm}, and reach a document only
 * through the new-document lifecycle. Reading never writes; an invalid stored value
 * behaves as unset.
 */
final class GeoCeDGUnitPreferences {
	static final String CONSTRUCTION_KEY = "geocedg.units.new-document-construction.v1";
	static final String PRESENTATION_KEY = "geocedg.units.new-document-presentation.v1";
	/** Unit-system v1.1 (E2 smoke follow-up B2): dimension unit suffix of new documents. */
	static final String DIMENSION_SUFFIX_KEY =
			"geocedg.units.new-document-dimension-unit-suffix.v1";
	static final String SUFFIX_SHOWN = "shown";
	static final String SUFFIX_HIDDEN = "hidden";

	/** Construction default: unspecified or a built-in unit. */
	enum ConstructionDefault {
		UNSPECIFIED("unspecified", null),
		MM("mm", UnitToken.MM),
		CM("cm", UnitToken.CM),
		M("m", UnitToken.M);

		final String value;
		final UnitToken unit;

		ConstructionDefault(String value, UnitToken unit) {
			this.value = value;
			this.unit = unit;
		}
	}

	/** Presentation default: none (follows the construction unit) or a built-in unit. */
	enum PresentationDefault {
		NONE("none", null),
		MM("mm", UnitToken.MM),
		CM("cm", UnitToken.CM),
		M("m", UnitToken.M);

		final String value;
		final UnitToken unit;

		PresentationDefault(String value, UnitToken unit) {
			this.value = value;
			this.unit = unit;
		}
	}

	private static GeoCeDGPresentationPreferences.Store storeOverride;

	private final GeoCeDGPresentationPreferences.Store store;

	GeoCeDGUnitPreferences() {
		this(storeOverride != null ? storeOverride : GeoCeDGPresentationPreferences.systemStore());
	}

	GeoCeDGUnitPreferences(GeoCeDGPresentationPreferences.Store store) {
		this.store = store;
	}

	/**
	 * Test seam: every instance created by the no-argument constructor uses the given
	 * store until the returned handle is closed, so tests never touch the real store.
	 */
	static AutoCloseable useStoreForTesting(GeoCeDGPresentationPreferences.Store store) {
		GeoCeDGPresentationPreferences.Store previous = storeOverride;
		storeOverride = store;
		return () -> storeOverride = previous;
	}

	ConstructionDefault construction() {
		String stored = store.load(CONSTRUCTION_KEY);
		for (ConstructionDefault candidate : ConstructionDefault.values()) {
			if (candidate.value.equals(stored)) {
				return candidate;
			}
		}
		return ConstructionDefault.UNSPECIFIED;
	}

	PresentationDefault presentation() {
		String stored = store.load(PRESENTATION_KEY);
		for (PresentationDefault candidate : PresentationDefault.values()) {
			if (candidate.value.equals(stored)) {
				return candidate;
			}
		}
		return PresentationDefault.NONE;
	}

	void setConstruction(ConstructionDefault value) {
		store.save(CONSTRUCTION_KEY, value.value);
	}

	void setPresentation(PresentationDefault value) {
		store.save(PRESENTATION_KEY, value.value);
	}

	/**
	 * @return whether new documents show the dimension unit suffix; hidden unless the
	 *         stored value is exactly {@code shown} (author decision, section 14.2)
	 */
	boolean dimensionUnitSuffixShown() {
		return SUFFIX_SHOWN.equals(store.load(DIMENSION_SUFFIX_KEY));
	}

	void setDimensionUnitSuffixShown(boolean shown) {
		store.save(DIMENSION_SUFFIX_KEY, shown ? SUFFIX_SHOWN : SUFFIX_HIDDEN);
	}

	/**
	 * @return the state of a new blank document: a physical construction default with
	 *         the presentation default, or no unit (an unspecified construction default
	 *         leaves any presentation default inert, DQ-D0-1), with the dimension unit
	 *         suffix policy of new documents (unit-system v1.1, section 14.2)
	 */
	UnitState newDocumentState() {
		ConstructionDefault construction = construction();
		boolean suffixShown = dimensionUnitSuffixShown();
		if (construction.unit == null) {
			return UnitState.of(null, null, null, suffixShown);
		}
		return UnitState.of(construction.unit, presentation().unit, null, suffixShown);
	}
}

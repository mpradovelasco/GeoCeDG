/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.Objects;

import org.geocedg.common.export.GeometryExportModel.Unit;
import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.units.CanonicalBinary64;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;

/**
 * PRE-G9B-R6-plus-C: immutable document and session inputs of one neutral
 * geometry export that are not construction geometry: the unit state
 * (unit-system section 15.2), the persistent hidden-layer set ({@code DQ-C2}) and
 * the consumed export area ({@code DQ-C13}). Every value enters the preflight
 * staleness fingerprint (section 15.3); none is ever written to the document.
 */
public final class GeometryExportContext {

	/** Resolves the current context; called at preflight and on every currentness check. */
	@FunctionalInterface
	public interface Source {
		/** @return current export context */
		GeometryExportContext resolve();
	}

	/** Unspecified unit, no hidden layer, no export area. */
	public static final GeometryExportContext UNSPECIFIED =
			new GeometryExportContext(UnitState.EMPTY, HiddenLayerSet.EMPTY, null);

	private final UnitState unitState;
	private final Unit unit;
	private final HiddenLayerSet hiddenLayers;
	private final GeometryExportArea area;

	private GeometryExportContext(UnitState unitState, HiddenLayerSet hiddenLayers,
			GeometryExportArea area) {
		this.unitState = Objects.requireNonNull(unitState, "Unit state is required");
		this.hiddenLayers = Objects.requireNonNull(hiddenLayers,
				"Hidden-layer set is required");
		this.area = area;
		unit = unitOf(unitState);
	}

	/**
	 * @param unitState document unit state
	 * @param hiddenLayers document hidden-layer set
	 * @param area consumed export area, or null when none was resolved
	 * @return immutable context
	 */
	public static GeometryExportContext of(UnitState unitState,
			HiddenLayerSet hiddenLayers, GeometryExportArea area) {
		return new GeometryExportContext(unitState, hiddenLayers, area);
	}

	/** @return document unit state; presentation selection included */
	public UnitState getUnitState() {
		return unitState;
	}

	/** @return neutral-model unit of the effective construction unit */
	public Unit getUnit() {
		return unit;
	}

	/** @return binary64 metres per model unit; NaN when unspecified */
	public double getMetresPerUnit() {
		return unitState.effectiveConstructionMetresPerUnit();
	}

	/** @return canonical binary64 metre factor text; null when unspecified */
	public String getCanonicalMetresPerUnit() {
		return unit == Unit.UNITLESS ? null
				: CanonicalBinary64.write(getMetresPerUnit());
	}

	/** @return the usm definition when the construction unit is usm, else null */
	public UsmDefinition getUsmDefinition() {
		return unit == Unit.USM ? unitState.getUsm() : null;
	}

	public HiddenLayerSet getHiddenLayers() {
		return hiddenLayers;
	}

	/**
	 * @param layer GeoCeDG layer number
	 * @return whether the layer is persistently hidden in the document
	 */
	public boolean isLayerHidden(int layer) {
		return hiddenLayers.contains(layer);
	}

	/** @return consumed export area, or null when none was resolved */
	public GeometryExportArea getArea() {
		return area;
	}

	/** @return whether an explicit closed participation boundary applies */
	public boolean hasAreaBoundary() {
		return area != null && area.isBoundary();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GeometryExportContext)) {
			return false;
		}
		GeometryExportContext context = (GeometryExportContext) other;
		return unitState.equals(context.unitState)
				&& hiddenLayers.equals(context.hiddenLayers)
				&& Objects.equals(area, context.area);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unitState, hiddenLayers, area);
	}

	@Override
	public String toString() {
		return "unit=" + unit + ", unitState=" + unitState + ", hiddenLayers="
				+ hiddenLayers + ", area=" + area;
	}

	private static Unit unitOf(UnitState state) {
		UnitToken token = state.effectiveConstructionUnit();
		if (token == null) {
			return Unit.UNITLESS;
		}
		switch (token) {
		case MM:
			return Unit.MM;
		case CM:
			return Unit.CM;
		case M:
			return Unit.M;
		case USM:
		default:
			return Unit.USM;
		}
	}
}

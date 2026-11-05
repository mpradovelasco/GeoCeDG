/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.arithmetic.SymbolicMode;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.plugin.Operation;

/** Strict P4 postconditions for the two CU4 rows, including non-algorithm inputs. */
final class GeoCeDGInsertionFidelity {

	private GeoCeDGInsertionFidelity() {
	}

	/** @return the first uncertified slot, or null if the slider was preserved */
	static String angleMismatch(GeoAngle from, GeoAngle to, Map<GeoElement, GeoElement> copies) {
		if (!to.isSliderable()) {
			return "interval";
		}
		if (!GeoCeDGInsertionAdmission.compatibleAngleUnits(
				from.getKernel().getAngleUnit(), to.getKernel().getAngleUnit())
				|| from.isKeepDegrees() != to.isKeepDegrees()) {
			return "unit";
		}
		if (from.getAngleStyle() != to.getAngleStyle() || from.getRawAngle() != to.getRawAngle()
				|| from.getValue() != to.getValue() || from.isRandom() != to.isRandom()
				|| !sameAngleDefinition(from, to, copies)) {
			return "value or definition";
		}
		if (!sameNumber(from.getIntervalMinObject(), to.getIntervalMinObject(), copies)) {
			return "minimum";
		}
		if (!sameNumber(from.getIntervalMaxObject(), to.getIntervalMaxObject(), copies)) {
			return "maximum";
		}
		if (from.isAutoStep() != to.isAutoStep() || !from.isAutoStep()
				&& !sameNumber(from.getAnimationStepObject(), to.getAnimationStepObject(), copies)) {
			return "step";
		}
		if (!sameSpeed(from, to, copies)) {
			return "speed";
		}
		if (from.getAnimationType() != to.getAnimationType()
				|| from.isAnimating() != to.isAnimating()) {
			return "animation state";
		}
		if (from.isAbsoluteScreenLocActive() != to.isAbsoluteScreenLocActive()
				|| !sameAnchor(from.getStartPoint(), to.getStartPoint(), copies)) {
			return "anchor";
		}
		if (from.getSliderWidth() != to.getSliderWidth()
				|| from.getSliderBlobSize() != to.getSliderBlobSize()
				|| from.isSliderHorizontal() != to.isSliderHorizontal()
				|| from.isLockedPosition() != to.isLockedPosition()
				|| from.isAVSliderOrCheckboxVisible() != to.isAVSliderOrCheckboxVisible()) {
			return "slider controls";
		}
		return samePresentation(from, to, copies) ? null : "presentation";
	}

	private static boolean sameAngleDefinition(GeoAngle from, GeoAngle to,
			Map<GeoElement, GeoElement> copies) {
		if (sameDefinition(from.getDefinition(), to.getDefinition(), copies)) {
			return true;
		}
		// A degree literal parses as a multiplication by the degree constant.
		// The clipboard may normalize this closed value to stored radians.
		return to.getDefinition() == null && !from.isKeepDegrees()
				&& from.getDefinition() != null
				&& from.getDefinition().unwrap() instanceof ExpressionNode degrees
				&& degrees.getOperation() == Operation.MULTIPLY
				&& degrees.getLeft().unwrap() instanceof MyDouble magnitude
				&& degrees.getRight().unwrap() instanceof MyDouble unit
				&& unit.isAngle() && unit.getDouble() == Kernel.PI_180
				&& magnitude.getDouble() * unit.getDouble() == from.getRawAngle();
	}

	static boolean sameText(GeoText from, GeoText to, Map<GeoElement, GeoElement> copies) {
		return to.isDefined()
				&& Objects.equals(from.getTextString(), to.getTextString())
				&& from.isLaTeX() == to.isLaTeX()
				&& from.isSymbolicMode() == to.isSymbolicMode()
				&& from.isSerifFont() == to.isSerifFont()
				&& from.getFontStyle() == to.getFontStyle()
				&& from.getFontSizeMultiplier() == to.getFontSizeMultiplier()
				&& from.getPrintDecimals() == to.getPrintDecimals()
				&& from.getPrintFigures() == to.getPrintFigures()
				&& from.isAbsoluteScreenLocActive() == to.isAbsoluteScreenLocActive()
				&& sameAnchor(from.getStartPoint(), to.getStartPoint(), copies)
				&& samePresentation(from, to, copies);
	}

	private static boolean samePresentation(GeoElement from, GeoElement to,
			Map<GeoElement, GeoElement> copies) {
		return from.isSetEuclidianVisible() == to.isSetEuclidianVisible()
				&& from.getLabelVisible() == to.getLabelVisible()
				&& from.getLabelMode() == to.getLabelMode()
				&& from.labelOffsetX == to.labelOffsetX && from.labelOffsetY == to.labelOffsetY
				&& Objects.equals(from.getRawCaption(), to.getRawCaption())
				&& Objects.equals(from.getStaticObjectColor(), to.getStaticObjectColor())
				&& Objects.equals(from.getBackgroundColor(), to.getBackgroundColor())
				&& from.getAlphaValue() == to.getAlphaValue()
				&& from.isLocked() == to.isLocked()
				&& sameReference(from.getShowObjectCondition(), to.getShowObjectCondition(), copies)
				&& sameReference(from.getColorFunction(), to.getColorFunction(), copies)
				&& sameReference(from.getDynamicCaption(), to.getDynamicCaption(), copies);
	}

	private static boolean sameReference(GeoElement from, GeoElement to,
			Map<GeoElement, GeoElement> copies) {
		return from == null ? to == null : to != null && copies.get(from) == to;
	}

	private static boolean sameAnchor(GeoPointND from, GeoPointND to,
			Map<GeoElement, GeoElement> copies) {
		if (from == null || to == null) {
			return from == to;
		}
		if (from.isGeoElement3D() || to.isGeoElement3D() || !from.isDefined()
				|| !to.isDefined() || from.isInfinite() || to.isInfinite()) {
			return false;
		}
		if (from.isLabelSet() || to.isLabelSet()) {
			return sameReference(from.toGeoElement(), to.toGeoElement(), copies);
		}
		return from.isAbsoluteStartPoint() == to.isAbsoluteStartPoint()
				&& from.getInhomX() == to.getInhomX() && from.getInhomY() == to.getInhomY()
				&& sameDefinition(from.toGeoElement().getDefinition(),
						to.toGeoElement().getDefinition(), copies);
	}

	private static boolean sameSpeed(GeoAngle from, GeoAngle to,
			Map<GeoElement, GeoElement> copies) {
		// The native writer omits a default speed of one, which the loader may
		// represent as a missing slot rather than an allocated anonymous number.
		NumberValue source = (NumberValue) from.getAnimationSpeedObject();
		NumberValue target = (NumberValue) to.getAnimationSpeedObject();
		return sameNumber(source == null ? new MyDouble(from.getKernel(), 1) : source,
				target == null ? new MyDouble(to.getKernel(), 1) : target, copies);
	}

	private static boolean sameNumber(NumberValue from, NumberValue to,
			Map<GeoElement, GeoElement> copies) {
		if (from == null || to == null) {
			return from == to;
		}
		if (!Double.isFinite(from.getDouble()) || from.getDouble() != to.getDouble()
				|| from.getAngleDim() != to.getAngleDim()) {
			return false;
		}
		GeoElement source = from instanceof GeoElement geo ? geo : null;
		GeoElement target = to instanceof GeoElement geo ? geo : null;
		if (source != null && source.isLabelSet() || target != null && target.isLabelSet()) {
			return source != null && sameReference(source, target, copies);
		}
		return sameDefinition(source == null ? null : source.getDefinition(),
				target == null ? null : target.getDefinition(), copies);
	}

	private static boolean sameDefinition(ExpressionNode from, ExpressionNode to,
			Map<GeoElement, GeoElement> copies) {
		if (from == null || to == null) {
			ExpressionNode explicit = from == null ? to : from;
			// Native reconstruction can normalize a literal to a value-only slot.
			return explicit == null || explicit.unwrap() instanceof MyDouble;
		}
		Set<GeoElement> expected = Collections.newSetFromMap(new IdentityHashMap<>());
		for (GeoElement original : from.getVariables(SymbolicMode.NONE)) {
			GeoElement mapped = copies.get(original);
			if (mapped == null) {
				return false;
			}
			expected.add(mapped);
		}
		if (!expected.equals(to.getVariables(SymbolicMode.NONE))) {
			return false;
		}
		// Serialize the remapped expression tree without changing either construction.
		return from.getCopy(to.getKernel()).traverse(value -> value instanceof GeoElement geo
						? copies.get(geo) : value)
				.toString(StringTemplate.xmlTemplate).equals(to.toString(StringTemplate.xmlTemplate));
	}
}

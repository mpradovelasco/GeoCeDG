/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MyStringBuffer;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoAngle.AngleStyle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.Operation;

/**
 * Certifies the closed construction family whose geometry is independent of
 * the document's angular display mode. The native insertion adapter owns
 * archive validation, consent, presentation checks and transaction recovery.
 */
public final class MixedAngleInsertionProof {
	private final Construction construction;
	private final List<Entry> entries;
	private final String constructionXml;

	private MixedAngleInsertionProof(Construction construction, List<Entry> entries,
			String constructionXml) {
		this.construction = construction;
		this.entries = List.copyOf(entries);
		this.constructionXml = constructionXml;
	}

	/**
	 * Capture the native construction and its typed values without evaluating
	 * expressions or applying a display policy.
	 *
	 * @param construction source or isolated source copy
	 * @return a certificate, or null if the whole source cannot be verified
	 */
	public static MixedAngleInsertionProof capture(Construction construction) {
		Kernel kernel = construction.getKernel();
		if (kernel.hasMacros() || kernel.wantAnimationStarted()
				|| !construction.getSpatialIdentityRegistry().isEmpty()
				|| construction.getSpatialIdentityRegistry().hasDocumentAuthorityRecord()) {
			return null;
		}
		String script = kernel.getLibraryJavaScript();
		if (script != null && !script.isBlank()
				&& !Kernel.defaultLibraryJavaScript.equals(script.strip())) {
			return null;
		}
		List<Entry> entries = new ArrayList<>();
		Set<GeoElement> geos = identitySet();
		geos.addAll(construction.getGeoSetWithCasCellsConstructionOrder());
		Set<AlgoElement> algorithms = identitySet();
		Set<GeoElement> anchors = identitySet();
		boolean containsAngle = false;
		for (GeoElement geo : construction.getGeoSetWithCasCellsConstructionOrder()) {
			Entry entry = entry(geo, geos);
			AlgoElement parent = geo.getParentAlgorithm();
			if (entry == null || construction.getSpatialIdentityRegistry().isParticipating(geo)
					|| construction.getConstructionElement(entries.size())
							!= (parent == null ? geo : parent)) {
				return null;
			}
			entries.add(entry);
			if (parent != null) {
				algorithms.add(parent);
			}
			if (geo instanceof GeoText) {
				anchors.addAll(entry.operands());
			}
			containsAngle |= geo instanceof GeoAngle;
		}
		if (!containsAngle || construction.steps() != entries.size()
				|| construction.getAlgoList().size() != algorithms.size()
				|| !algorithms.containsAll(construction.getAlgoList())
				|| entries.stream().anyMatch(item -> item.geo() instanceof GeoPoint
						&& item.geo().isIndependent() && !anchors.contains(item.geo()))) {
			return null;
		}
		XMLStringBuilder xml = new XMLStringBuilder();
		construction.getConstructionXML(xml, false);
		return new MixedAngleInsertionProof(construction, entries, xml.toString());
	}

	/**
	 * @param other certificate from the second native load or its recomputation
	 * @return whether native XML, typed values and dependencies are unchanged
	 */
	public boolean sameState(MixedAngleInsertionProof other) {
		if (other == null || entries.size() != other.entries.size()
				|| !constructionXml.equals(other.constructionXml)) {
			return false;
		}
		Map<GeoElement, GeoElement> correspondence = new IdentityHashMap<>();
		for (int i = 0; i < entries.size(); i++) {
			Entry source = entries.get(i);
			Entry copy = other.entries.get(i);
			if (construction == other.construction
					&& (source.geo() != copy.geo() || source.producer() != copy.producer())) {
				return false;
			}
			correspondence.put(source.geo(), copy.geo());
		}
		for (int i = 0; i < entries.size(); i++) {
			if (!entries.get(i).matches(other.entries.get(i), correspondence)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param copies source-to-copy references reported by native insertion after L1
	 * @return whether the copied objects preserve exact typed values and operands
	 */
	public boolean preservesCopy(Map<GeoElement, GeoElement> copies) {
		Set<GeoElement> outputs = identitySet();
		for (Entry original : entries) {
			GeoElement copy = copies.get(original.geo());
			if (copy == null || !outputs.add(copy)) {
				return false;
			}
		}
		for (Entry original : entries) {
			Entry copied = entry(copies.get(original.geo()), outputs);
			if (copied == null || !original.matches(copied, copies)) {
				return false;
			}
		}
		return true;
	}

	private static Entry entry(GeoElement geo, Set<GeoElement> geos) {
		if (!geo.isLabelSet() || !geo.isDefined() || geo.isGeoElement3D()
				|| geo.isAnimating() || geo.hasScripts()
				|| geo.getShowObjectCondition() != null || geo.getColorFunction() != null
				|| geo.getDynamicCaption() != null) {
			return null;
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent != null && (parent.getOutputLength() != 1 || parent.getOutput(0) != geo)) {
			return null;
		}
		if (geo.getClass() == GeoAngle.class) {
			AngleState value = angleState((GeoAngle) geo);
			return value == null ? null : new Entry(geo, List.of(), value);
		}
		if (geo.getClass() == GeoNumeric.class) {
			return trigEntry((GeoNumeric) geo, geos);
		}
		if (geo.getClass() == GeoPoint.class) {
			return pointEntry((GeoPoint) geo, geos);
		}
		if (geo.getClass() == GeoText.class && parent == null) {
			return textEntry((GeoText) geo, geos);
		}
		return null;
	}

	private static AngleState angleState(GeoAngle angle) {
		int animationType = angle.getAnimationType();
		double raw = angle.getRawAngle();
		if (!angle.isIndependent() || !angle.isSliderable() || !Double.isFinite(raw)
				|| !Double.isFinite(angle.getValue()) || angle.isRandom() || angle.isAutoStep()
				|| angle.isKeepDegrees() || angle.getDefinition() != null
						&& !isClosedDegreeLiteral(angle)
				|| animationType < GeoElementND.ANIMATION_OSCILLATING
				|| animationType > GeoElementND.ANIMATION_INCREASING_ONCE) {
			return null;
		}
		Literal minimum = literal(angle.getIntervalMinObject());
		Literal maximum = literal(angle.getIntervalMaxObject());
		Literal step = literal(angle.getAnimationStepObject());
		GeoElement speedObject = angle.getAnimationSpeedObject();
		Literal speed = speedObject instanceof NumberValue number ? literal(number) : null;
		if (minimum == null || maximum == null || step == null || speed == null
				|| minimum.value() >= maximum.value() || raw < minimum.value()
				|| raw > maximum.value() || step.value() <= 0
				|| speed.dimension() != 0 || speed.value() == 1) {
			return null;
		}
		return new AngleState(raw, angle.getValue(), angle.getAngleStyle(),
				minimum, maximum, step, speed, animationType);
	}

	private static boolean isClosedDegreeLiteral(GeoAngle angle) {
		return angle.getDefinition() != null
				&& angle.getDefinition().unwrap() instanceof ExpressionNode degrees
				&& degrees.getOperation() == Operation.MULTIPLY
				&& degrees.getLeft().unwrap() instanceof MyDouble magnitude
				&& magnitude.getAngleDim() == 0 && Double.isFinite(magnitude.getDouble())
				&& degrees.getRight().unwrap() instanceof MyDouble unit
				&& unit.isAngle() && unit.getDouble() == Kernel.PI_180
				&& bits(magnitude.getDouble() * unit.getDouble()) == bits(angle.getRawAngle());
	}

	private static Literal literal(NumberValue value) {
		if (value instanceof MyDouble) {
			return finiteLiteral(value);
		}
		if (!(value instanceof GeoNumeric number) || number.getClass() != GeoNumeric.class
				|| number.isLabelSet() || !number.isIndependent() || number.isRandom()) {
			return null;
		}
		ExpressionNode definition = number.getDefinition();
		if (definition != null && (!(definition.unwrap() instanceof MyDouble scalar)
				|| bits(scalar.getDouble()) != bits(number.getDouble())
				|| scalar.getAngleDim() != number.getAngleDim())) {
			return null;
		}
		return finiteLiteral(number);
	}

	private static Literal finiteLiteral(NumberValue value) {
		double number = value.getDouble();
		int dimension = value.getAngleDim();
		return Double.isFinite(number) && (dimension == 0 || dimension == 1)
				? new Literal(number, dimension) : null;
	}

	private static Entry trigEntry(GeoNumeric number, Set<GeoElement> geos) {
		if (!(number.getParentAlgorithm() instanceof AlgoDependentNumber algorithm)
				|| algorithm.getClass() != AlgoDependentNumber.class || number.isRandom()
				|| number.isDrawable() || number.getAngleDim() != 0
				|| !Double.isFinite(number.getDouble())) {
			return null;
		}
		ExpressionNode expression = algorithm.getExpression();
		Operation operation = expression.getOperation();
		GeoElement[] input = algorithm.getInput();
		if (operation != Operation.SIN && operation != Operation.COS
				|| !(expression.getLeft().unwrap() instanceof GeoAngle angle)
				|| !geos.contains(angle) || input.length != 1 || input[0] != angle) {
			return null;
		}
		return new Entry(number, List.of(angle), new TrigState(operation, number.getDouble()));
	}

	private static Entry pointEntry(GeoPoint point, Set<GeoElement> geos) {
		if (point.getToStringMode() != Kernel.COORD_CARTESIAN || point.isPointOnPath()
				|| point.isPointInRegion() || !Double.isFinite(point.getInhomX())
				|| !Double.isFinite(point.getInhomY())) {
			return null;
		}
		PointState value = new PointState(point.getInhomX(), point.getInhomY());
		if (point.isIndependent()) {
			return isLiteralPoint(point) ? new Entry(point, List.of(), value) : null;
		}
		if (!(point.getParentAlgorithm() instanceof AlgoDependentPoint algorithm)
				|| algorithm.getClass() != AlgoDependentPoint.class
				|| !(algorithm.getExpression().unwrap() instanceof MyVecNode vector)
				|| vector.getClass() != MyVecNode.class
				|| vector.getToStringMode() != Kernel.COORD_CARTESIAN
				|| !(vector.getX().unwrap() instanceof GeoNumeric x)
				|| !(vector.getY().unwrap() instanceof GeoNumeric y)
				|| !geos.contains(x) || !geos.contains(y)) {
			return null;
		}
		Entry cosine = trigEntry(x, geos);
		Entry sine = trigEntry(y, geos);
		GeoElement[] input = algorithm.getInput();
		// The algorithm's dependency set has no semantic order. The AST has x and y roles.
		if (cosine == null || sine == null
				|| ((TrigState) cosine.value()).operation() != Operation.COS
				|| ((TrigState) sine.value()).operation() != Operation.SIN
				|| cosine.operands().get(0) != sine.operands().get(0) || input.length != 2
				|| !(input[0] == x && input[1] == y || input[0] == y && input[1] == x)) {
			return null;
		}
		return new Entry(point, List.of(x, y), value);
	}

	private static boolean isLiteralPoint(GeoPoint point) {
		ExpressionNode definition = point.getDefinition();
		return definition == null || definition.unwrap() instanceof MyVecNode vector
				&& vector.getClass() == MyVecNode.class
				&& vector.getToStringMode() == Kernel.COORD_CARTESIAN
				&& vector.getX().unwrap() instanceof MyDouble x && x.getAngleDim() == 0
				&& vector.getY().unwrap() instanceof MyDouble y && y.getAngleDim() == 0
				&& bits(x.getDouble()) == bits(point.getInhomX())
				&& bits(y.getDouble()) == bits(point.getInhomY());
	}

	private static Entry textEntry(GeoText text, Set<GeoElement> geos) {
		if (text.getDefinition() != null
				&& !(text.getDefinition().unwrap() instanceof MyStringBuffer)) {
			return null;
		}
		if (text.getStartPoint() == null) {
			return new Entry(text, List.of(), new TextState(text.getTextString()));
		}
		if (text.getStartPoint() instanceof GeoPoint anchor && geos.contains(anchor)
				&& anchor.getClass() == GeoPoint.class && anchor.isIndependent()) {
			return new Entry(text, List.of(anchor), new TextState(text.getTextString()));
		}
		return null;
	}

	private static long bits(double value) {
		return Double.doubleToLongBits(value);
	}

	private static <T> Set<T> identitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	private sealed interface Value permits AngleState, TrigState, PointState, TextState {
	}

	private record Entry(GeoElement geo, AlgoElement producer, List<GeoElement> operands,
			Value value, Appearance appearance) {
		Entry(GeoElement geo, List<GeoElement> operands, Value value) {
			this(geo, geo.getParentAlgorithm(), operands, value, Appearance.of(geo));
		}

		boolean matches(Entry copy, Map<GeoElement, GeoElement> correspondence) {
			if (geo.getClass() != copy.geo.getClass() || !value.equals(copy.value)
					|| !Objects.equals(appearance, copy.appearance)
					|| operands.size() != copy.operands.size()) {
				return false;
			}
			for (int i = 0; i < operands.size(); i++) {
				if (correspondence.get(operands.get(i)) != copy.operands.get(i)) {
					return false;
				}
			}
			return true;
		}
	}

	private record Appearance(GColor color, GColor background, double alpha, int layer,
			boolean visible, boolean auxiliary, boolean locked, boolean selectable,
			Label label, Stroke stroke, PointMarker marker, boolean symbolic) {
		static Appearance of(GeoElement geo) {
			// Text uses the established native text fidelity in F3.
			if (geo instanceof GeoText) {
				return null;
			}
			// The certified scalar operands cannot draw a label. Native XML omits its
			// visibility and mode, and reconstruction may resolve a different default mode.
			boolean graphicalLabel = geo.getClass() != GeoNumeric.class;
			Label label = new Label(graphicalLabel && geo.getLabelVisible(),
					graphicalLabel ? geo.getLabelMode() : GeoElement.LABEL_DEFAULT, geo.getRawCaption(),
					geo.labelOffsetX, geo.labelOffsetY);
			Stroke stroke = new Stroke(geo.getLineThickness(), geo.getLineType(),
					geo.getLineTypeHidden(), geo.getLineOpacity());
			PointMarker marker = geo instanceof GeoPoint point
					? new PointMarker(point.getPointSize(), point.getPointStyle()) : null;
			return new Appearance(geo.getStaticObjectColor(), geo.getBackgroundColor(),
					geo.getAlphaValue(), geo.getLayer(), geo.isSetEuclidianVisible(),
					geo.isAuxiliaryObject(), geo.isLocked(), geo.isSelectionAllowed(null), label,
					stroke, marker, geo instanceof GeoNumeric number && number.isSymbolicMode());
		}
	}

	private record Label(boolean visible, int mode, String caption, int offsetX, int offsetY) {
	}

	private record Stroke(int thickness, int type, int hiddenType, int opacity) {
	}

	private record PointMarker(int size, int style) {
	}

	private record Literal(double value, int dimension) {
	}

	private record AngleState(double raw, double effective, AngleStyle style, Literal minimum,
			Literal maximum, Literal step, Literal speed, int animationType) implements Value {
	}

	private record TrigState(Operation operation, double value) implements Value {
	}

	private record PointState(double x, double y) implements Value {
	}

	private record TextState(String text) implements Value {
	}
}

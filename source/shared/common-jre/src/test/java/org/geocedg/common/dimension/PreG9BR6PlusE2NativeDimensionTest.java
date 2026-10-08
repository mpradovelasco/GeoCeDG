/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.dimension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.euclidian.draw.DrawDimensionText;
import org.geocedg.common.kernel.dimension.AlgoAlignedDimension;
import org.geocedg.common.kernel.dimension.AlgoLinearDimension;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.dimension.DimensionFigure2D;
import org.geocedg.common.kernel.dimension.DimensionPresentation;
import org.geocedg.common.kernel.units.UnitDocumentOperations;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E2 shared-kernel contract of the native dimensions
 * ({@code geocedg/specs/dimensions/native-dimensions.md}): signatures, output roles,
 * values, side authority, figure, degeneracies, presentation and unit independence,
 * persistence, and the unchanged upstream {@code Dimension} command.
 */
class PreG9BR6PlusE2NativeDimensionTest extends BaseUnitTest {

	private static final double EPS = 1E-12;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private GeoElement[] dim(String command) {
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoElementND[] result = getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				command, false, errors, false, null);
		assertNotNull(result, command + " " + errors.getErrors());
		GeoElement[] out = new GeoElement[result.length];
		for (int i = 0; i < result.length; i++) {
			out[i] = result[i].toGeoElement();
		}
		return out;
	}

	private static AlgoNativeDimension algo(GeoElement[] outputs) {
		return (AlgoNativeDimension) outputs[0].getParentAlgorithm();
	}

	private static double[] coords(GeoSegment segment) {
		return new double[] {segment.getStartPoint().getInhomX(),
				segment.getStartPoint().getInhomY(), segment.getEndPoint().getInhomX(),
				segment.getEndPoint().getInhomY()};
	}

	private String errorsOf(String command) {
		ErrorAccumulator errors = new ErrorAccumulator();
		getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(command, false,
				errors, false, null);
		return errors.getErrors();
	}

	// ---------------------------------------------------------------- contract

	@Test
	void outputRolesAndOrderAreTheCommandContract() {
		add("A=(0,0)");
		add("B=(4,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1.5)");
		assertEquals(5, out.length);
		assertTrue(out[AlgoNativeDimension.VALUE] instanceof GeoNumeric);
		assertTrue(out[AlgoNativeDimension.DIMENSION_LINE] instanceof GeoSegment);
		assertTrue(out[AlgoNativeDimension.EXTENSION_A] instanceof GeoSegment);
		assertTrue(out[AlgoNativeDimension.EXTENSION_B] instanceof GeoSegment);
		assertTrue(out[AlgoNativeDimension.TEXT] instanceof GeoText);
		AlgoNativeDimension algo = algo(out);
		assertTrue(algo instanceof AlgoAlignedDimension);
		assertSame(algo.getValue(), out[0]);
		assertSame(algo.getDimensionLine(), out[1]);
		assertSame(algo.getExtensionA(), out[2]);
		assertSame(algo.getExtensionB(), out[3]);
		assertSame(algo.getPresentationText(), out[4]);
		for (GeoElement geo : out) {
			assertTrue(geo.isLabelSet(), "every output is labelled");
			assertSame(algo, geo.getParentAlgorithm());
		}
		assertFalse(out[1].isLabelVisible());
		assertFalse(out[2].isLabelVisible());
		assertFalse(out[3].isLabelVisible());
		assertEquals(AlgoNativeDimension.ARROW_ENDING,
				((GeoSegment) out[1]).getStartStyle());
		assertEquals(AlgoNativeDimension.ARROW_ENDING, ((GeoSegment) out[1]).getEndStyle());
		assertEquals(Commands.AlignedDimension, algo.getClassName());
		assertEquals(3, algo.getInput().length, "omitted overshoot and gap stay omitted");
	}

	@Test
	void aSingleLabelNamesTheValueAndTheOtherOutputsTakeDefaultLabels() {
		add("A=(0,0)");
		add("B=(4,0)");
		GeoElement[] named = dim("d=AlignedDimension(A,B,1)");
		assertEquals("d", named[AlgoNativeDimension.VALUE].getLabelSimple());
		assertSame(named[0], lookup("d"));
		for (int i = 1; i < named.length; i++) {
			assertTrue(named[i].isLabelSet(), "default label " + i);
			assertFalse(named[i].getLabelSimple().startsWith("d_"), "no indexed labels");
		}
		GeoElement[] linear = dim("e=LinearDimension(A,B,xAxis,1)");
		assertSame(linear[AlgoNativeDimension.VALUE], lookup("e"));
	}

	@Test
	void signaturesAndOptionalForms() {
		add("A=(1,1)");
		add("B=(5,4)");
		add("f=Line(A,B)");
		add("v=Vector((1,0))");
		assertEquals(5, dim("AlignedDimension(A,B,2)").length);
		AlgoNativeDimension five = algo(dim("AlignedDimension(A,B,2,0.3,0.1)"));
		assertEquals(5, five.getInput().length);
		AlgoNativeDimension linear = algo(dim("LinearDimension(A,B,xAxis,2)"));
		assertTrue(linear instanceof AlgoLinearDimension);
		assertEquals(4, linear.getInput().length);
		assertEquals(6, algo(dim("LinearDimension(A,B,v,2,0.3,0.1)")).getInput().length);
		assertTrue(errorsOf("AlignedDimension(A,B)").length() > 0, "offset is mandatory");
		assertTrue(errorsOf("AlignedDimension(A,B,1,2)").length() > 0);
		assertTrue(errorsOf("AlignedDimension(A,B,1,2,3,4)").length() > 0);
		assertTrue(errorsOf("LinearDimension(A,B,f)").length() > 0);
		assertTrue(errorsOf("LinearDimension(A,B,A,1)").length() > 0,
				"a point is not a direction");
		assertTrue(errorsOf("LinearDimension(A,B,3,1)").length() > 0);
		assertTrue(errorsOf("AlignedDimension(A,f,1)").length() > 0);
	}

	@Test
	void legacyNamesAreNotRegisteredAndDimensionIsUnchanged() {
		for (String legacy : new String[] {"DirectDimension", "AxisDimension",
				"directDimension", "axisDimension"}) {
			assertNull(Commands.lookupInternal(legacy), legacy);
			assertNull(getApp().getLocalization().getReverseCommand(legacy), legacy);
		}
		assertEquals("AlignedDimension", Commands.lookupInternal("aligneddimension"));
		assertEquals("LinearDimension", Commands.lookupInternal("LINEARDIMENSION"));
		add("A=(0,0)");
		add("B=(1,0)");
		assertTrue(errorsOf("DirectDimension(A,B,1)").contains("DirectDimension"));
		// upstream Dimension: cardinality of lists, matrices, points and vectors
		assertEquals(3, ((GeoNumeric) add("Dimension({1,2,3})")).getDouble(), 0);
		assertEquals("{2, 3}", add("Dimension({{1,2,3},{4,5,6}})")
				.toValueString(StringTemplate.testTemplate));
		assertEquals(2, ((GeoNumeric) add("Dimension((1,2))")).getDouble(), 0);
		assertEquals(2, ((GeoNumeric) add("Dimension(Vector((3,4)))")).getDouble(), 0);
		assertEquals(0, ((GeoNumeric) add("Dimension({})")).getDouble(), 0);
		assertEquals("Dimension", Commands.lookupInternal("dimension"));
		assertEquals("Dimension", getApp().getLocalization().getCommand("Dimension"));
		assertEquals("Dimension( <Object> )",
				getApp().getLocalization().getCommandSyntax("Dimension"));
	}

	// ------------------------------------------------------------------ values

	@Test
	void alignedValueIsTheModelDistance() {
		add("A=(1,1)");
		add("B=(4,5)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		assertEquals(5, ((GeoNumeric) out[0]).getDouble(), EPS);
	}

	@Test
	void linearValueIsTheAbsoluteProjectionForEveryDirectionRoute() {
		add("A=(1,1)");
		add("B=(4,3)");
		add("P=(0,5)");
		add("f=Line(A,B)");
		add("fr=Line(B,A)");
		add("g=Line(P,f)");
		add("h=PerpendicularLine(P,f)");
		add("v=Vector((2,1))");
		add("k=Line(P,v)");
		add("r=Ray(A,B)");
		add("rv=Ray(P,v)");
		add("s=Segment(A,B)");
		double along = Math.sqrt(13);
		double alongV = (3 * 2 + 2 * 1) / Math.sqrt(5);
		Map<String, Double> expected = new LinkedHashMap<>();
		expected.put("f", along);
		expected.put("fr", along);
		expected.put("g", along);
		expected.put("h", 0d);
		expected.put("v", alongV);
		expected.put("k", alongV);
		expected.put("r", along);
		expected.put("rv", alongV);
		expected.put("s", along);
		expected.put("xAxis", 3d);
		expected.put("yAxis", 2d);
		for (Map.Entry<String, Double> e : expected.entrySet()) {
			GeoElement[] out = dim("LinearDimension(A,B," + e.getKey() + ",1)");
			assertEquals(e.getValue(), ((GeoNumeric) out[0]).getDouble(), 1E-12,
					e.getKey());
		}
	}

	@Test
	void directionSignNeverChangesTheValueButFixesTheSide() {
		add("A=(1,1)");
		add("B=(4,3)");
		add("f=Line(A,B)");
		add("fr=Line(B,A)");
		GeoElement[] forward = dim("LinearDimension(A,B,f,1)");
		GeoElement[] backward = dim("LinearDimension(A,B,fr,1)");
		assertEquals(((GeoNumeric) forward[0]).getDouble(),
				((GeoNumeric) backward[0]).getDouble(), EPS);
		double[] n = algo(forward).getMeasurementNormal();
		double[] nr = algo(backward).getMeasurementNormal();
		assertEquals(-n[0], nr[0], EPS);
		assertEquals(-n[1], nr[1], EPS);
	}

	@Test
	void axisDirectionsFollowGetDirectionNotGetDirectionInD3() {
		add("A=(1,1)");
		add("B=(4,3)");
		add("P=(0,5)");
		add("f=Line(A,B)");
		add("g=Line(P,f)");
		add("h=PerpendicularLine(P,f)");
		add("v=Vector((2,1))");
		add("k=Line(P,v)");
		// left normal of getDirection: g follows f (getDirectionInD3 would be opposite)
		assertArrayEquals(algo(dim("LinearDimension(A,B,f,1)")).getMeasurementNormal(),
				algo(dim("LinearDimension(A,B,g,1)")).getMeasurementNormal(), EPS);
		double[] nh = algo(dim("LinearDimension(A,B,h,1)")).getMeasurementNormal();
		// h direction = f direction rotated by +90 degrees: (-2,3)/sqrt(13); normal = (-3,-2)
		assertEquals(-3 / Math.sqrt(13), nh[0], EPS);
		assertEquals(-2 / Math.sqrt(13), nh[1], EPS);
		double[] nk = algo(dim("LinearDimension(A,B,k,1)")).getMeasurementNormal();
		assertEquals(-1 / Math.sqrt(5), nk[0], EPS);
		assertEquals(2 / Math.sqrt(5), nk[1], EPS);
		assertArrayEquals(new double[] {0, 1},
				algo(dim("LinearDimension(A,B,xAxis,1)")).getMeasurementNormal(), EPS);
		assertArrayEquals(new double[] {-1, 0},
				algo(dim("LinearDimension(A,B,yAxis,1)")).getMeasurementNormal(), EPS);
	}

	// ---------------------------------------------------------- side and figure

	@Test
	void sideIsOrderedInputsPlusSignOfOffset() {
		add("A=(0,0)");
		add("B=(4,0)");
		double[] above = coords((GeoSegment) dim("AlignedDimension(A,B,1.5)")[1]);
		assertArrayEquals(new double[] {0, 1.5, 4, 1.5}, above, EPS);
		double[] below = coords((GeoSegment) dim("AlignedDimension(A,B,-1.5)")[1]);
		assertArrayEquals(new double[] {0, -1.5, 4, -1.5}, below, EPS);
		// swapping A and B is a semantic input change: the left normal of B to A
		double[] swapped = coords((GeoSegment) dim("AlignedDimension(B,A,1.5)")[1]);
		assertArrayEquals(new double[] {4, -1.5, 0, -1.5}, swapped, EPS);
		add("C=(1,2)");
		double[] linear = coords((GeoSegment) dim("LinearDimension(A,C,xAxis,3)")[1]);
		assertArrayEquals(new double[] {0, 3, 1, 3}, linear, EPS);
		double[] vertical = coords((GeoSegment) dim("LinearDimension(A,C,yAxis,1)")[1]);
		assertArrayEquals(new double[] {-1, 0, -1, 2}, vertical, EPS);
	}

	@Test
	void draggingMeasuredGeometryNeverFlipsTheSide() {
		add("A=(0,0)");
		GeoPoint b = add("B=(4,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1.5)");
		GeoSegment line = (GeoSegment) out[1];
		for (int degrees = 0; degrees < 720; degrees++) {
			double t = Math.toRadians(degrees / 2.0);
			b.setCoords(4 * Math.cos(t), 4 * Math.sin(t), 1);
			b.updateCascade();
			double mx = (line.getStartPoint().getInhomX() + line.getEndPoint().getInhomX()) / 2;
			double my = (line.getStartPoint().getInhomY() + line.getEndPoint().getInhomY()) / 2;
			double cross = b.getInhomX() * my - b.getInhomY() * mx;
			assertTrue(cross > 0, "dimension line stays on the left of A to B at "
					+ degrees / 2.0);
			assertEquals(1.5, cross / 4, 1E-9);
		}
	}

	@Test
	void extensionLinesHonourGapAndOvershoot() {
		add("A=(0,0)");
		add("B=(4,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,2,0.3,0.5)");
		assertArrayEquals(new double[] {0, 0.5, 0, 2.3}, coords((GeoSegment) out[2]), EPS);
		assertArrayEquals(new double[] {4, 0.5, 4, 2.3}, coords((GeoSegment) out[3]), EPS);
		GeoElement[] below = dim("AlignedDimension(A,B,-2,0.3,0.5)");
		assertArrayEquals(new double[] {0, -0.5, 0, -2.3}, coords((GeoSegment) below[2]),
				EPS);
		add("C=(4,1)");
		// linear: the extension at C runs from C to the line y = 2 (t = 1)
		GeoElement[] linear = dim("LinearDimension(A,C,xAxis,2,0.25,0.2)");
		assertArrayEquals(new double[] {4, 1.2, 4, 2.25}, coords((GeoSegment) linear[3]),
				EPS);
		GeoText text = (GeoText) out[4];
		assertEquals(2, text.getStartPoint().getInhomX(), EPS);
		assertEquals(2, text.getStartPoint().getInhomY(), EPS);
		assertTrue(text.isAlwaysFixed());
	}

	@Test
	void noGuideLineAndNoLengthProportionalGeometry() {
		add("A=(0,0)");
		GeoPoint b = add("B=(4,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1,0.2,0.1)");
		double[] before = coords((GeoSegment) out[2]);
		b.setCoords(40, 0, 1);
		b.updateCascade();
		double[] after = coords((GeoSegment) out[2]);
		assertArrayEquals(before, after, EPS, "extension at A independent of |AB|");
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			assertFalse(geo.isGeoLine() && !geo.isGeoSegment() && geo.getParentAlgorithm()
					instanceof AlgoNativeDimension, "no infinite guide line");
		}
	}

	// ------------------------------------------------------------ degeneracies

	@Test
	void degeneracyContract() {
		add("A=(0,0)");
		add("B=(0,0)");
		GeoElement[] coincident = dim("AlignedDimension(A,B,1)");
		assertDefinedness(coincident, true, false, false, false, false, "A = B");
		assertEquals(0, ((GeoNumeric) coincident[0]).getDouble(), 0);

		add("f=Line((0,0),(1,0))");
		add("g=Line((0,1),(1,1))");
		add("U=Intersect(f,g)");
		add("p0=0/0");
		add("N=(p0,1)");
		add("C=(3,0)");
		assertDefinedness(dim("AlignedDimension(U,C,1)"), false, false, false, false, false,
				"infinite A");
		assertDefinedness(dim("AlignedDimension(N,C,1)"), false, false, false, false, false,
				"undefined A");

		add("inf=1/0");
		assertDefinedness(dim("AlignedDimension(B,C,inf)"), true, false, false, false,
				false, "non-finite offset");
		assertDefinedness(dim("AlignedDimension(B,C,1,inf,0)"), true, false, false, false,
				false, "non-finite overshoot");
		assertDefinedness(dim("AlignedDimension(B,C,1,0,inf)"), true, false, false, false,
				false, "non-finite gap");
		assertDefinedness(dim("AlignedDimension(B,C,1,-0.1,0)"), true, false, false, false,
				false, "negative overshoot");

		add("zv=Vector((0,0))");
		assertDefinedness(dim("LinearDimension(B,C,zv,1)"), false, false, false, false,
				false, "zero direction vector");
		GeoNumeric q = add("q=1");
		add("K=(q,1)");
		add("dl=Line(K,(5,1))");
		GeoElement[] undefinedDirection = dim("LinearDimension(B,C,dl,1)");
		assertTrue(undefinedDirection[0].isDefined());
		q.setUndefined();
		q.updateCascade();
		assertDefinedness(undefinedDirection, false, false, false, false, false,
				"undefined direction");

		add("D=(0,4)");
		GeoElement[] zero = dim("LinearDimension(B,D,xAxis,1)");
		assertDefinedness(zero, true, false, true, true, true, "projected value 0");
		assertEquals(0, ((GeoNumeric) zero[0]).getDouble(), 0);

		assertDefinedness(dim("AlignedDimension(B,C,0)"), true, true, false, false, true,
				"offset 0");
		assertDefinedness(dim("AlignedDimension(B,C,0.1,0,0.2)"), true, true, false, false,
				true, "offset within the gap");
	}

	private static void assertDefinedness(GeoElement[] out, boolean value, boolean line,
			boolean extA, boolean extB, boolean text, String context) {
		assertEquals(value, out[0].isDefined(), context + ": value");
		assertEquals(line, out[1].isDefined(), context + ": dimension line");
		assertEquals(extA, out[2].isDefined(), context + ": extension A");
		assertEquals(extB, out[3].isDefined(), context + ": extension B");
		assertEquals(text, out[4].isDefined(), context + ": text");
	}

	// ------------------------------------------------- presentation and units

	@Test
	void presentationFollowsUnitsWithoutChangingTheValue() {
		add("A=(0,0)");
		add("B=(12,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		GeoText text = (GeoText) out[4];
		GeoNumeric value = (GeoNumeric) out[0];
		assertEquals("12", text.getTextString(), "unspecified: bare value, no suffix");
		setUnits(UnitState.of(UnitToken.CM, UnitToken.MM, null));
		assertEquals("120 mm", text.getTextString());
		assertEquals(12, value.getDouble(), 0);
		setUnits(UnitState.of(UnitToken.CM, UnitToken.M, null));
		assertEquals("0.12 m", text.getTextString());
		assertEquals(12, value.getDouble(), 0);
		setUnits(UnitState.of(UnitToken.CM, null, null));
		assertEquals("12 cm", text.getTextString());
		setUnits(UnitState.of(UnitToken.MM, UnitToken.USM,
				UsmDefinition.of(0.0254, "inch", "in")));
		assertEquals("0.47 in", text.getTextString());
		setUnits(UnitState.of(UnitToken.MM, UnitToken.USM,
				UsmDefinition.of(0.0254, null, null)));
		assertTrue(text.getTextString().endsWith(" usm"), text.getTextString());
		setUnits(UnitState.EMPTY);
		assertEquals("12", text.getTextString());
		assertEquals(12, value.getDouble(), 0);
	}

	@Test
	void nonFinitePresentationShowsTheFailureMarkerOnly() {
		add("A=(0,0)");
		add("B=(1E300,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		setUnits(UnitState.of(UnitToken.M, UnitToken.USM,
				UsmDefinition.of(1E-300, "tiny", "t")));
		assertEquals(DimensionPresentation.FAILURE_MARKER + " t",
				((GeoText) out[4]).getTextString());
		assertTrue(out[0].isDefined(), "the value survives a presentation failure");
		assertEquals(1E300, ((GeoNumeric) out[0]).getDouble(), 0);
	}

	@Test
	void unitChangesRefreshOnlyTheTexts() {
		add("A=(0,0)");
		add("B=(3,4)");
		add("s=1.5");
		GeoElement[] aligned = dim("AlignedDimension(A,B,s)");
		GeoElement[] linear = dim("LinearDimension(A,B,xAxis,s,0.2,0.1)");
		Map<String, String> before = geometryFingerprint();
		RecordingView view = new RecordingView();
		getKernel().attach(view);
		try {
			UnitDocumentOperations.commit(getApp(), UnitState.of(UnitToken.CM, UnitToken.MM,
					null));
			UnitDocumentOperations.commit(getApp(), UnitState.of(UnitToken.CM, UnitToken.M,
					null));
			UnitDocumentOperations.commit(getApp(), UnitState.of(UnitToken.MM, null, null));
		} finally {
			getKernel().detach(view);
		}
		assertEquals(before, geometryFingerprint(), "no geometric change");
		for (GeoElement geo : view.updated) {
			assertTrue(geo == aligned[4] || geo == linear[4],
					"only dimension texts are updated, not " + geo.getLabelSimple());
		}
		assertTrue(view.updated.contains(aligned[4]));
		assertEquals("5 mm", ((GeoText) aligned[4]).getTextString());
		assertEquals("3 mm", ((GeoText) linear[4]).getTextString());
	}

	private Map<String, String> geometryFingerprint() {
		Map<String, String> fingerprint = new LinkedHashMap<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			if (!geo.isGeoText()) {
				fingerprint.put(geo.getLabelSimple(),
						geo.toValueString(StringTemplate.maxPrecision) + "|"
								+ geo.getDefinition(StringTemplate.xmlTemplate));
			}
		}
		return fingerprint;
	}

	private void setUnits(UnitState state) {
		getConstruction().getUnitSystem().replace(state);
	}

	@Test
	void presentationFormatter() {
		UnitState cmMm = UnitState.of(UnitToken.CM, UnitToken.MM, null);
		assertEquals("120 mm", DimensionPresentation.format(12, cmMm, getKernel(),
				StringTemplate.defaultTemplate));
		assertEquals("12", DimensionPresentation.format(12, UnitState.EMPTY, getKernel(),
				StringTemplate.defaultTemplate));
	}

	// --------------------------------------------------------- dynamic and DAG

	@Test
	void geometricInputsRecomputeGeometryAndValue() {
		add("A=(0,0)");
		GeoPoint b = add("B=(4,0)");
		GeoNumeric s = add("s=1");
		final GeoNumeric o = add("o=0");
		final GeoNumeric g = add("g=0");
		add("d=Vector((1,0))");
		GeoElement[] out = dim("LinearDimension(A,B,d,s,o,g)");
		b.setCoords(6, 2, 1);
		b.updateCascade();
		assertEquals(6, ((GeoNumeric) out[0]).getDouble(), EPS);
		add("d=Vector((0,1))");
		assertEquals(2, ((GeoNumeric) out[0]).getDouble(), EPS);
		s.setValue(-2);
		s.updateCascade();
		assertEquals(2, ((GeoNumeric) out[0]).getDouble(), EPS);
		assertEquals(2, ((GeoSegment) out[1]).getStartPoint().getInhomX(), EPS);
		o.setValue(0.5);
		o.updateCascade();
		g.setValue(0.25);
		g.updateCascade();
		assertArrayEquals(new double[] {0.25, 0, 2.5, 0}, coords((GeoSegment) out[2]), EPS);
		assertEquals("2", ((GeoText) out[4]).getTextString());
	}

	@Test
	void figureFunctionIsPure() {
		DimensionFigure2D figure = DimensionFigure2D.aligned(0, 0, 4, 0, 1, 0.5, 0.25);
		assertEquals(4, figure.value, 0);
		assertArrayEquals(new double[] {0, 1, 4, 1}, figure.line, 0);
		assertArrayEquals(new double[] {0, 0.25, 0, 1.5}, figure.extensionA, 0);
		assertArrayEquals(new double[] {2, 1, 1, 0}, figure.frame, 0);
		assertEquals(1.5, DimensionFigure2D.offsetThrough(0, 0, 0, 1, 7, 1.5), 0);
	}

	// ------------------------------------------------------------- persistence

	@Test
	void saveReopenReproducesTheDimensionsExactly() {
		add("A=(0,0)");
		add("B=(3,4)");
		add("s=1.5");
		dim("AlignedDimension(A,B,s)");
		dim("LinearDimension(A,B,xAxis,s,0.2,0.1)");
		setUnits(UnitState.of(UnitToken.CM, UnitToken.MM, null));
		String xml = getApp().getXML();
		assertTrue(xml.contains("<command name=\"AlignedDimension\">"));
		assertTrue(xml.contains("<command name=\"LinearDimension\">"));
		assertFalse(xml.contains("120 mm") || xml.contains("50 mm"),
				"the presentation string is never serialized");
		for (String element : elementTypes(xml)) {
			assertTrue(List.of("point", "numeric", "segment", "text", "axis")
					.contains(element), "existing element type only: " + element);
		}
		Map<String, String> before = geometryFingerprint();
		reload();
		assertEquals(before, geometryFingerprint());
		assertEquals(xml, getApp().getXML(), "byte-identical round trip");
		GeoElement text = getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoElement::isGeoText).findFirst().orElse(null);
		assertNotNull(text);
		assertEquals("50 mm", ((GeoText) text).getTextString());
		assertTrue(text.getParentAlgorithm() instanceof AlgoAlignedDimension);
	}

	private static List<String> elementTypes(String xml) {
		List<String> types = new ArrayList<>();
		java.util.regex.Matcher m = java.util.regex.Pattern
				.compile("<element type=\"([^\"]+)\"").matcher(xml);
		while (m.find()) {
			types.add(m.group(1));
		}
		return types;
	}

	@Test
	void undoAndRedoRestoreGeometryTextAndUnits() {
		activateUndo();
		add("A=(0,0)");
		add("B=(4,0)");
		add("s=1");
		getApp().storeUndoInfo();
		dim("AlignedDimension(A,B,s)");
		getApp().storeUndoInfo();
		GeoNumeric s = (GeoNumeric) lookup("s");
		s.setValue(2);
		s.updateCascade();
		getApp().storeUndoInfo();
		UnitDocumentOperations.commit(getApp(), UnitState.of(UnitToken.CM, UnitToken.MM,
				null));
		assertEquals("40 mm", textOf().getTextString());
		getKernel().undo();
		assertEquals("4", textOf().getTextString());
		assertEquals(2, lineY(), EPS);
		getKernel().undo();
		assertEquals(1, lineY(), EPS);
		getKernel().undo();
		assertNull(textOf());
		getKernel().redo();
		getKernel().redo();
		getKernel().redo();
		assertEquals("40 mm", textOf().getTextString());
		assertEquals(2, lineY(), EPS);
	}

	private GeoText textOf() {
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			if (geo.isGeoText() && geo.getParentAlgorithm() instanceof AlgoNativeDimension) {
				return (GeoText) geo;
			}
		}
		return null;
	}

	private double lineY() {
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(textOf());
		return algo.getDimensionLine().getStartPoint().getInhomY();
	}

	@Test
	void redefineChangesOnlyTheCommandForm() {
		add("A=(0,0)");
		add("B=(4,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		String label = out[0].getLabelSimple();
		editGeoElement(out[0], "AlignedDimension(A,B,2,0.5,0.25)");
		GeoElement redefined = lookup(label);
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(redefined);
		assertNotNull(algo);
		assertEquals(5, algo.getInput().length);
		assertEquals(2, algo.getDimensionLine().getStartPoint().getInhomY(), EPS);
	}

	// ---------------------------------------------------------- reading angle

	@Test
	void readableAngleRule() {
		double[][] cases = {{0, 0}, {30, 30}, {44, 44}, {45, 45}, {46, 46}, {89, 89},
				{90, 90}, {91, -89}, {135, -45}, {179, -1}, {180, 0}, {-90, 90}, {-91, 89},
				{270, 90}, {-179, 1}};
		for (double[] c : cases) {
			assertEquals(c[1], DrawDimensionText.readableAngle(c[0]), 1E-12, "phi=" + c[0]);
			assertEquals(DrawDimensionText.readableAngle(c[0]),
					DrawDimensionText.readableAngle(c[0] + 180), 1E-12,
					"order independence at " + c[0]);
		}
	}

	@Test
	void classicConfigurationComputesTheSameValue() {
		AppCommon classic = AppCommonFactory.create(new org.geogebra.common.main.settings
				.config.AppConfigDefault());
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("A=(0,0)", false);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("B=(3,4)", false);
		GeoElementND[] out = classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("AlignedDimension(A,B,1)", false);
		assertEquals(5, ((GeoNumeric) out[0]).getDouble(), EPS);
		assertTrue(App.class.isInstance(classic));
	}

	/** Records updated geos. */
	private static final class RecordingView implements View {
		final List<GeoElement> updated = new ArrayList<>();

		@Override
		public void add(GeoElement geo) {
			// not recorded
		}

		@Override
		public void remove(GeoElement geo) {
			// not recorded
		}

		@Override
		public void rename(GeoElement geo) {
			// not recorded
		}

		@Override
		public void update(GeoElement geo) {
			updated.add(geo);
		}

		@Override
		public void updateVisualStyle(GeoElement geo, GProperty prop) {
			updated.add(geo);
		}

		@Override
		public void updateHighlight(GeoElementND geo) {
			// presentation only
		}

		@Override
		public void updateAuxiliaryObject(GeoElement geo) {
			updated.add(geo);
		}

		@Override
		public void repaintView() {
			// not recorded
		}

		@Override
		public boolean suggestRepaint() {
			return false;
		}

		@Override
		public void reset() {
			// not recorded
		}

		@Override
		public void clearView() {
			// not recorded
		}

		@Override
		public void setMode(int mode, org.geogebra.common.kernel.ModeSetter m) {
			// not recorded
		}

		@Override
		public int getViewID() {
			return 999_002;
		}

		@Override
		public boolean hasFocus() {
			return false;
		}

		@Override
		public void startBatchUpdate() {
			// not recorded
		}

		@Override
		public void endBatchUpdate() {
			// not recorded
		}

		@Override
		public void updatePreviewFromInputBar(GeoElement[] geos) {
			// not recorded
		}
	}
}

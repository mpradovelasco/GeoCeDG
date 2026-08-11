/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.MyPoint;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.PathMoverLocus;
import org.geogebra.common.kernel.PathNormalizer;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLocusND;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Read-only characterization of the pinned legacy Locus implementation.
 *
 * <p>These tests deliberately describe existing behavior. They are not a
 * contract for Locus V2 and introduce no productive V2 implementation.</p>
 */
class LegacyLocusCharacterizationTest extends BaseUnitTest {

	@Test
	void viewChangesLegacySamplingAndSampleDerivedPerimeter() {
		add("ZoomIn(-5,-5,5,5)");
		add("A=Point(Circle((0,0),2))");
		add("B=A+(1,0)");
		GeoLocus locus = add("legacy=Locus(B,A)");
		int nearSamples = locus.getPoints().size();
		double nearPerimeter = chordLength(locus.getPoints());

		add("ZoomIn(-100,-100,100,100)");
		int farSamples = locus.getPoints().size();
		double farPerimeter = chordLength(locus.getPoints());

		assertNotEquals(nearSamples, farSamples);
		assertNotEquals(nearPerimeter, farPerimeter);
		assertTrue(nearSamples > farSamples);
		metric("zoom-samples-near", nearSamples);
		metric("zoom-samples-far", farSamples);
		metric("zoom-chord-perimeter-near", nearPerimeter);
		metric("zoom-chord-perimeter-far", farPerimeter);
	}

	@Test
	void nativeAndNormalizedPathParametersAreNotUniversalSemanticIdentity() {
		GeoPoint point = add("A=Point(Circle((0,0),2))");
		Path circle = point.getPath();
		assertEquals(-Math.PI, circle.getMinParameter(), 1e-12);
		assertEquals(Math.PI, circle.getMaxParameter(), 1e-12);
		assertEquals(0.5, PathNormalizer.toNormalizedPathParameter(0,
				circle.getMinParameter(), circle.getMaxParameter()), 1e-12);

		final Path segment = add("Segment((0,0),(1,0))");
		final Path ray = add("Ray((0,0),(1,0))");
		final Path line = add("Line((0,0),(1,0))");
		final Path ellipse = add("Ellipse((-2,0),(2,0),3)");
		final Path parabola = add("Parabola((0,1),xAxis)");
		final Path hyperbola = add("Hyperbola((-2,0),(2,0),1)");
		assertEquals(0, segment.getMinParameter());
		assertEquals(1, segment.getMaxParameter());
		assertEquals(0, ray.getMinParameter());
		assertEquals(Double.POSITIVE_INFINITY, ray.getMaxParameter());
		assertEquals(Double.NEGATIVE_INFINITY, line.getMinParameter());
		assertEquals(Double.POSITIVE_INFINITY, line.getMaxParameter());
		assertEquals(-Math.PI, ellipse.getMinParameter(), 1e-12);
		assertEquals(Math.PI, ellipse.getMaxParameter(), 1e-12);
		assertEquals(Double.NEGATIVE_INFINITY, parabola.getMinParameter());
		assertEquals(Double.POSITIVE_INFINITY, parabola.getMaxParameter());
		assertEquals(-1, hyperbola.getMinParameter());
		assertEquals(3, hyperbola.getMaxParameter());

		add("ZoomIn(-5,-5,5,5)");
		GeoFunction function = add("f(x)=sin(x)");
		double nearMin = function.getMinParameter();
		double nearMax = function.getMaxParameter();
		add("ZoomIn(-100,-100,100,100)");
		double farMin = function.getMinParameter();
		double farMax = function.getMaxParameter();
		assertNotEquals(nearMin, farMin);
		assertNotEquals(nearMax, farMax);
		metric("function-domain-near-min", nearMin);
		metric("function-domain-near-max", nearMax);
		metric("function-domain-far-min", farMin);
		metric("function-domain-far-max", farMax);
	}

	@Test
	@Timeout(90)
	void dependencySliceCostIsCharacterizedAtControlledDepths()
			throws ReflectiveOperationException {
		for (int depth : new int[] {10, 50, 200}) {
			String prefix = "C" + depth;
			add(prefix + "P=Point(Circle((0,0),1))");
			add(prefix + "N0=x(" + prefix + "P)");
			for (int level = 1; level <= depth; level++) {
				add(prefix + "N" + level + "=sin(" + prefix + "N"
						+ (level - 1) + ")+0.001" + prefix + "N"
						+ (level - 1));
			}
			add(prefix + "Q=(" + prefix + "N" + depth + ",y("
					+ prefix + "P))");
			long start = System.nanoTime();
			GeoLocus locus = add(prefix + "L=Locus(" + prefix + "Q,"
					+ prefix + "P)");
			double elapsed = nanosToMillis(System.nanoTime() - start);
			AlgoLocusND<?> algorithm = (AlgoLocusND<?>) locus.getParentAlgorithm();
			Construction slice = readField(algorithm, "macroCons");
			assertTrue(locus.isDefined());
			assertTrue(slice.getAlgoList().size() >= depth);
			metric("legacy-chain-" + depth + "-create-ms", elapsed);
			metric("legacy-chain-" + depth + "-samples", locus.getPoints().size());
			metric("legacy-chain-" + depth + "-slice-algos",
					slice.getAlgoList().size());
		}
	}

	@Test
	void legacyHyperbolaEncodesBranchesAsMoveToSamples() {
		add("HDriver=Point(x^2-y^2=1)");
		add("HDependent=HDriver+(1,0)");
		GeoLocus locus = add("HLegacy=Locus(HDependent,HDriver)");
		long moveToCount = locus.getPoints().stream()
				.filter(point -> !point.getLineTo()).count();
		assertTrue(moveToCount >= 2);
		metric("legacy-hyperbola-samples", locus.getPoints().size());
		metric("legacy-hyperbola-moveto", moveToCount);
	}

	@Test
	@Timeout(90)
	void nestedLegacyLociConsumeSampledPathsAndKeepIndependentSlices()
			throws ReflectiveOperationException {
		add("ZoomIn(-5,-5,5,5)");
		long start = System.nanoTime();
		add("A=Point(Circle((0,0),1))");
		add("B=A+(1,0)");
		GeoLocus level1 = add("L1=Locus(B,A)");
		final long level1Nanos = System.nanoTime() - start;

		start = System.nanoTime();
		final GeoPoint level2Driver = add("C=Point(L1)");
		add("D=C+(0,1)");
		GeoLocus level2 = add("L2=Locus(D,C)");
		final long level2Nanos = System.nanoTime() - start;

		start = System.nanoTime();
		GeoPoint level3Driver = add("E=Point(L2)");
		add("F=E+(-1,0)");
		GeoLocus level3 = add("L3=Locus(F,E)");
		final long level3Nanos = System.nanoTime() - start;

		AlgoLocusND<?> algo1 = (AlgoLocusND<?>) level1.getParentAlgorithm();
		AlgoLocusND<?> algo2 = (AlgoLocusND<?>) level2.getParentAlgorithm();
		AlgoLocusND<?> algo3 = (AlgoLocusND<?>) level3.getParentAlgorithm();

		assertSame(level1, readField(algo2, "path"));
		assertSame(level2, readField(algo3, "path"));
		assertTrue(readField(algo2, "pathMover") instanceof PathMoverLocus);
		assertTrue(readField(algo3, "pathMover") instanceof PathMoverLocus);
		assertSame(level1, level2Driver.getPath());
		assertSame(level2, level3Driver.getPath());

		final Construction slice1 = readField(algo1, "macroCons");
		Construction slice2 = readField(algo2, "macroCons");
		Construction slice3 = readField(algo3, "macroCons");
		assertFalse(containsLocusAlgorithm(slice2));
		assertFalse(containsLocusAlgorithm(slice3));
		assertTrue(originalSliceSize(algo1) > 0);
		assertTrue(originalSliceSize(algo2) > 0);
		assertTrue(originalSliceSize(algo3) > 0);

		metric("nested-1-samples", level1.getPoints().size());
		metric("nested-2-samples", level2.getPoints().size());
		metric("nested-3-samples", level3.getPoints().size());
		metric("nested-1-create-ms", nanosToMillis(level1Nanos));
		metric("nested-2-create-ms", nanosToMillis(level2Nanos));
		metric("nested-3-create-ms", nanosToMillis(level3Nanos));
		metric("nested-1-slice-algos", slice1.getAlgoList().size());
		metric("nested-2-slice-algos", slice2.getAlgoList().size());
		metric("nested-3-slice-algos", slice3.getAlgoList().size());
		metric("nested-1-original-elements", originalSliceSize(algo1));
		metric("nested-2-original-elements", originalSliceSize(algo2));
		metric("nested-3-original-elements", originalSliceSize(algo3));
		metric("nested-2-slice-types", sliceTypes(slice2));
		metric("nested-3-slice-types", sliceTypes(slice3));
	}

	@Test
	@Timeout(90)
	void nestedLegacyRecomputeCostIsMeasuredSeparatelyByDepth() {
		double depth1 = buildAndMeasureChain("N1", 1);
		double depth2 = buildAndMeasureChain("N2", 2);
		double depth3 = buildAndMeasureChain("N3", 3);
		double depth5 = buildAndMeasureChain("N5", 5);

		assertTrue(depth1 >= 0);
		assertTrue(depth2 >= 0);
		assertTrue(depth3 >= 0);
		assertTrue(depth5 >= 0);
		metric("legacy-recompute-depth-1-median-ms", depth1);
		metric("legacy-recompute-depth-2-median-ms", depth2);
		metric("legacy-recompute-depth-3-median-ms", depth3);
		metric("legacy-recompute-depth-5-median-ms", depth5);
	}

	private double buildAndMeasureChain(String prefix, int depth) {
		GeoNumeric source = add(prefix + "a=1");
		add(prefix + "P=Point(Circle((0,0),1))");
		add(prefix + "Q=" + prefix + "P+(" + prefix + "a,0)");
		GeoLocus previous = add(prefix + "L1=Locus(" + prefix + "Q,"
				+ prefix + "P)");
		for (int level = 2; level <= depth; level++) {
			String driver = prefix + "P" + level;
			String dependent = prefix + "Q" + level;
			String locus = prefix + "L" + level;
			add(driver + "=Point(" + previous.getLabelSimple() + ")");
			add(dependent + "=" + driver + "+(0," + prefix + "a/"
					+ level + ")");
			previous = add(locus + "=Locus(" + dependent + "," + driver + ")");
		}

		for (int warmup = 0; warmup < 3; warmup++) {
			source.setValue(1 + warmup * 0.01);
			source.updateRepaint();
		}
		double[] durations = new double[9];
		for (int iteration = 0; iteration < durations.length; iteration++) {
			long start = System.nanoTime();
			source.setValue(1 + iteration * 0.005);
			source.updateRepaint();
			durations[iteration] = nanosToMillis(System.nanoTime() - start);
		}
		java.util.Arrays.sort(durations);
		metric("legacy-depth-" + depth + "-outer-samples",
				previous.getPoints().size());
		return durations[durations.length / 2];
	}

	private static boolean containsLocusAlgorithm(Construction construction) {
		return construction.getAlgoList().stream()
				.anyMatch(AlgoLocusND.class::isInstance);
	}

	private static String sliceTypes(Construction construction) {
		return construction.getAlgoList().stream()
				.map(AlgoElement::getClassName)
				.map(Object::toString)
				.collect(Collectors.joining(","));
	}

	private static int originalSliceSize(AlgoLocusND<?> algorithm)
			throws ReflectiveOperationException {
		Set<?> elements = readField(algorithm, "locusConsOrigElements");
		return elements.size();
	}

	@SuppressWarnings("unchecked")
	private static <T> T readField(Object target, String name)
			throws ReflectiveOperationException {
		Field field = AlgoLocusND.class.getDeclaredField(name);
		field.setAccessible(true);
		return (T) field.get(target);
	}

	private static double chordLength(List<MyPoint> points) {
		double result = 0;
		for (int i = 1; i < points.size(); i++) {
			result += points.get(i - 1).distance(points.get(i));
		}
		return result;
	}

	private static double nanosToMillis(long nanos) {
		return nanos / 1_000_000.0;
	}

	private static void metric(String name, Object value) {
		System.out.println("G6A_METRIC " + name + "=" + value);
	}
}

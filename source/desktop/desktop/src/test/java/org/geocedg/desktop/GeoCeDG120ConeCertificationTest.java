/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Saved-file oracle: the real development needs complete evidence, not a style change. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120ConeCertificationTest {
	private static final String SHA = "4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01";
	@TempDir Path directory;

	@Test void originalConeHasCertifiedOpenDevelopmentWithDistinctLateralEnds() throws Exception {
		Path original = Path.of(System.getProperty("geocedg.120.cone.fixture",
				"C:/Temp/InterCilConoOblique_2.cedg"));
		byte[] bytes = Files.readAllBytes(original);
		assertEquals(SHA, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
		Path copy = directory.resolve("cone-certification.cedg");
		Files.write(copy, bytes);
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(copy.toFile()));
		awaitQuiescent(app);
		onEdt(() -> {
			for (String label : List.of("o_1", "e_2", "s_2")) {
				GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class,
						app.getKernel().lookupLabel(label));
				assertNotNull(locus.getSemanticDefinition(), label);
				var evaluator = assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
						locus.getSemanticDefinition().getEvaluatorCapability());
				var program = evaluator.captureCertifiedProgramV3();
				System.out.println("R9_CONE_PROGRAM " + label + " refusal="
						+ evaluator.getProgramV3Refusal() + " steps="
						+ program.map(value -> value.getSteps().size()).orElse(-1));
				if (program.isEmpty()) { dumpUnidentifiedOrigins(evaluator); }
				var result = assertInstanceOf(AlgoDependentPointLocusV2.class,
						locus.getParentAlgorithm()).getHybridExistenceCertificate();
				assertNotNull(result, label);
				var structure = result.getStructure();
				for (var coverage : structure.getCoverage()) {
					System.out.println("R9_CONE_COVERAGE " + label + " " + coverage.getInterval()
							+ " " + coverage.getState() + " " + coverage.getCause()
							+ " " + coverage.getEvidence());
				}
				for (var component : structure.getContinuousValidComponents()) {
					System.out.println("R9_CONE_COMPONENT " + label + " " + component.getStart()
							+ " -> " + component.getEnd() + " " + component.getMaximality()
							+ " " + component.getBoundaryEvidence());
				}
				if (!label.equals("o_1")) {
					evaluator.getMetricStructuresV3().forEach((node, metric) -> System.out.println(
							"R9_CONE_METRIC_STRUCTURE " + label + " node=" + node + " " + metric.getSignature()));
					if (structure.getCompleteness() != LocusExistenceStructure2D.Completeness.COMPLETE) {
						dumpSeam(locus);
					}
				}
				assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE,
						structure.getCompleteness(), label);
				assertFalse(structure.getContinuousValidComponents().isEmpty(), label);
				if (!label.equals("o_1")) {
					assertFalse(structure.isClosedPeriodic(), label + " must not bridge its seam");
					assertTrue(structure.getContinuousValidComponents().stream().allMatch(component ->
							component.getNature().certifies()
							&& component.getLowerBoundary().supportsOneSidedLimit()
							&& component.getUpperBoundary().supportsOneSidedLimit()), label);
					assertEquals(1, structure.getContinuousValidComponents().size(), label);
					var component = structure.getContinuousValidComponents().get(0);
					assertTrue(component.isFullLength(), label);
					var proofIndex = ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
							.getRouteSourceIndex(component.getStart());
					if (label.equals("s_2")) {
						assertNotNull(proofIndex, "s_2 needs a proof of regularity");
						assertTrue(proofIndex.isCertified());
						var speed = proofIndex.speedBounds(structure.getCanonicalDomain().getLower(),
								structure.getCanonicalDomain().getUpper());
						assertNotNull(speed);
						assertTrue(speed[0] > 0, "Existence alone does not prove regularity");
					}
					if (proofIndex != null && proofIndex.isCertified()) {
						var speed = proofIndex.speedBounds(structure.getCanonicalDomain().getLower(),
								structure.getCanonicalDomain().getUpper());
						System.out.println("R9_CONE_SPEED " + label + " "
								+ java.util.Arrays.toString(speed) + " precision="
								+ proofIndex.getPrecisionStatus() + " boxes=" + proofIndex.getEvaluatedBoxes());
					}
					for (var boundary : List.of(component.getLowerBoundary(), component.getUpperBoundary())) {
						assertEquals(LocusBoundary2D.Kind.STRUCTURAL_EVENT, boundary.getKind(), label);
						assertEquals("ORIGIN_WRAP", boundary.getSource().getPredicate(), label);
						assertTrue(boundary.getNature().certifies(), label);
					}
					// Hybrid publishes these STRUCTURAL_EVENT ends only after outputJumps
					// separates the image enclosures. A scalar ORIGIN_WRAP alone produces
					// UNRESOLVED_BAND_EDGE, which the assertions above reject.
					assertEquals(LocusExistenceStructure2D.Maximality.ESTABLISHED,
							component.getMaximality(), label);
					var definition = locus.getSemanticDefinition();
					var branch = definition.getBranches().get(0);
					try (var session = LocusEvaluationSession2D.openServiceSession(false, 32)) {
						var start = component.getLowerBoundary().isIncluded()
								? evaluator.evaluate(definition, branch, component.getStart(), session)
								: evaluator.evaluateOneSidedLimit(definition, branch, component, false, session);
						var end = component.getUpperBoundary().isIncluded()
								? evaluator.evaluate(definition, branch, component.getEnd(), session)
								: evaluator.evaluateOneSidedLimit(definition, branch, component, true, session);
						assertTrue(start.isValid(), label + ": " + start.getDiagnostic());
						assertTrue(end.isValid(), label + ": " + end.getDiagnostic());
						assertTrue(Math.hypot(start.getPoint().getX() - end.getPoint().getX(),
								start.getPoint().getY() - end.getPoint().getY()) > 1E-6, label);
						System.out.println("R9_CONE_LATERAL " + label + " " + start.getPoint()
								+ " " + end.getPoint() + " " + component.getBoundaryEvidence());
					}
				}
			}
		});
		assertEquals(SHA, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(original))));
	}

	@Test void coneCertificationTracksParametersCutsAndReopenWithoutStaleEvidence() throws Exception {
		Path original = Path.of(System.getProperty("geocedg.120.cone.fixture",
				"C:/Temp/InterCilConoOblique_2.cedg"));
		byte[] bytes = Files.readAllBytes(original);
		assertEquals(SHA, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
		Path copy = directory.resolve("cone-dynamic.cedg");
		Files.write(copy, bytes);
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(copy.toFile()));
		awaitQuiescent(app);
		var identity = onEdt(() -> ((GeoLocusV2) app.getKernel().lookupLabel("s_2"))
				.getPersistentLocusId());
		onEdt(() -> {
			assertCertifiedDevelopment(app, "initial");
			GeoPoint h = (GeoPoint) app.getKernel().lookupLabel("H");
			double initialParameter = h.getPathParameter().t;
			double initialX = h.inhomX, initialY = h.inhomY;
			for (double parameter : new double[] {initialParameter + .25, initialParameter}) {
				long oldRevision = ((GeoLocusV2) app.getKernel().lookupLabel("s_2")).getSemanticRevision();
				h.updatePathParameter(parameter);
				h.updateCascade();
				assertEquals(parameter, h.getPathParameter().t, 1e-12);
				assertTrue(((GeoLocusV2) app.getKernel().lookupLabel("s_2"))
						.getSemanticRevision() > oldRevision, "H must invalidate the semantic revision");
				if (parameter != initialParameter) {
					assertTrue(Math.hypot(h.inhomX - initialX, h.inhomY - initialY) > 1e-6);
				} else {
					assertEquals(initialX, h.inhomX, 1e-12);
					assertEquals(initialY, h.inhomY, 1e-12);
				}
				assertCertifiedDevelopment(app, "H=" + parameter);
			}
			GeoNumeric origin = (GeoNumeric) app.getKernel().lookupLabel("v_1");
			for (double parameter : new double[] {.5, -Math.PI + .1, Math.PI - .1, 0}) {
				origin.setValue(parameter);
				origin.updateCascade();
				assertCertifiedDevelopment(app, "v_1=" + parameter);
			}
			origin.setUndefined();
			origin.updateCascade();
			var lost = ((GeoLocusV2) app.getKernel().lookupLabel("s_2")).getSemanticDefinition();
			assertTrue(lost == null || lost.getBranches().stream().noneMatch(branch ->
					branch.getExistenceStructure().getCompleteness()
							== LocusExistenceStructure2D.Completeness.COMPLETE),
					"An undefined metric origin must withdraw the old certificate");
			origin.setValue(.5);
			origin.updateCascade();
			assertCertifiedDevelopment(app, "recovered origin");
			GeoLocusV2 cut = assertInstanceOf(GeoLocusV2.class,
					must(app, "DevelopmentCut120=LocusV2(V_1,E_1,\"constructive-cuts/v1\",K)"));
			var structure = cut.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
			assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE, structure.getCompleteness());
			assertFalse(structure.isClosedPeriodic());
			assertEquals(2, structure.getContinuousValidComponents().size(),
					"The chosen K direction and the shifted metric origin are different cuts");
			assertTrue(structure.getContinuousValidComponents().stream().anyMatch(component ->
					component.getLowerBoundary().getKind() == LocusBoundary2D.Kind.DECLARED_CONSTRUCTIVE_CUT));
			assertEquals(identity, ((GeoLocusV2) app.getKernel().lookupLabel("s_2")).getPersistentLocusId());
		});
		var cutKeys = onEdt(() -> LocusComponentLineage2D.keys(((GeoLocusV2)
				app.getKernel().lookupLabel("DevelopmentCut120")).getSemanticDefinition()
				.getBranches().get(0).getExistenceStructure()));
		var cutIdentity = onEdt(() -> ((GeoLocusV2) app.getKernel()
				.lookupLabel("DevelopmentCut120")).getPersistentLocusId());
		var savedImage = onEdt(() -> assertCertifiedDevelopment(app, "before save"));
		var file = onEdt(() -> save(app, directory, "cone-certified-dynamic"));
		AppGeoCeDG reopened = onEdt(() -> PreG9BCu0Corpus.open(file));
		awaitQuiescent(reopened);
		onEdt(() -> {
			assertArrayEquals(savedImage, assertCertifiedDevelopment(reopened, "reopened"), 1e-8,
					"Reopen must preserve the two lateral images and an interior point");
			assertEquals(identity, ((GeoLocusV2) reopened.getKernel().lookupLabel("s_2"))
					.getPersistentLocusId());
			GeoLocusV2 cut = (GeoLocusV2) reopened.getKernel().lookupLabel("DevelopmentCut120");
			assertEquals(cutIdentity, cut.getPersistentLocusId());
			assertEquals(cutKeys, LocusComponentLineage2D.keys(cut.getSemanticDefinition()
					.getBranches().get(0).getExistenceStructure()));
		});
		assertEquals(SHA, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(original))));
	}

	private static double[] assertCertifiedDevelopment(AppGeoCeDG app, String state) {
		GeoLocusV2 locus = (GeoLocusV2) app.getKernel().lookupLabel("s_2");
		assertTrue(locus.isDefined(), state);
		var definition = locus.getSemanticDefinition();
		assertNotNull(definition, state);
		var branch = definition.getBranches().get(0);
		var structure = branch.getExistenceStructure();
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE,
				structure.getCompleteness(), state + ": " + structure.getSemanticSignature());
		assertFalse(structure.isClosedPeriodic(), state);
		assertEquals(1, structure.getContinuousValidComponents().size(), state);
		var component = structure.getContinuousValidComponents().get(0);
		assertEquals(LocusExistenceStructure2D.Maximality.ESTABLISHED, component.getMaximality(), state);
		assertTrue(component.isFullLength(), state);
		for (var boundary : List.of(component.getLowerBoundary(), component.getUpperBoundary())) {
			assertEquals(LocusBoundary2D.Kind.STRUCTURAL_EVENT, boundary.getKind(), state);
			assertEquals("ORIGIN_WRAP", boundary.getSource().getPredicate(), state);
			assertTrue(boundary.getNature().certifies(), state);
		}
		var origin = AlgoSemanticLocusPoint2D.requireSemanticParent(
				(GeoPoint) app.getKernel().lookupLabel("POrigen")).getCurrentSemanticAddress();
		assertNotNull(origin, state);
		assertEquals(origin.getCanonicalParameter(), component.getStart(), state);
		var index = ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getRouteSourceIndex(component.getStart());
		assertNotNull(index, state);
		assertTrue(index.isCertified(), state);
		var speed = index.speedBounds(structure.getCanonicalDomain().getLower(),
				structure.getCanonicalDomain().getUpper());
		assertNotNull(speed, state);
		assertTrue(speed[0] > 0, state + ": speed=" + java.util.Arrays.toString(speed));
		double[] image;
		try (var session = LocusEvaluationSession2D.openServiceSession(false, 32)) {
			var start = definition.evaluateComponentEnd(branch.getBranchKey(), component.getStart(), false, session);
			var end = definition.evaluateComponentEnd(branch.getBranchKey(), component.getEnd(), true, session);
			assertTrue(start.isValid(), state + ": " + start.getDiagnostic());
			assertTrue(end.isValid(), state + ": " + end.getDiagnostic());
			assertTrue(Math.hypot(start.getPoint().getX() - end.getPoint().getX(),
					start.getPoint().getY() - end.getPoint().getY()) > 1e-6, state);
			var interior = definition.evaluate(branch.getBranchKey(), component.getStart() + .5, session);
			assertTrue(interior.isValid(), state + ": " + interior.getDiagnostic());
			image = new double[] {start.getPoint().getX(), start.getPoint().getY(),
					end.getPoint().getX(), end.getPoint().getY(),
					interior.getPoint().getX(), interior.getPoint().getY()};
		}
		var render = new LocusRenderCache2D().getOrBuild(locus,
				LocusRenderPolicy2D.adaptive(1, 800, 600, 30, 30, 64, .75, 12));
		assertEquals(LocusRenderData2D.Fidelity.COMPLETE, render.getFidelity(),
				state + ": " + render.getMarks());
		assertFalse(render.getVertices().isEmpty(), state);
		assertTrue(render.getExploratoryVertices().isEmpty(), state);
		assertEquals(1, render.getVertices().stream().filter(LocusRenderData2D.Vertex::startsSubpath).count(),
				state + ": a single open component must not gain gaps or a closing segment");
		var first = render.getVertices().get(0).getPoint();
		var last = render.getVertices().get(render.getVertices().size() - 1).getPoint();
		assertArrayEquals(new double[] {image[0], image[1], image[2], image[3]},
				new double[] {first.getX(), first.getY(), last.getX(), last.getY()}, 1e-8,
				state + ": the render must finish at the excluded lateral image, without closing to its start");
		System.out.println("R9_CONE_DYNAMIC " + state + " origin=" + component.getStart()
				+ " speed=" + java.util.Arrays.toString(speed) + " vertices=" + render.getVertices().size());
		return image;
	}

	private static void dumpUnidentifiedOrigins(ReconstructibleLocusEvaluator2D evaluator) {
		try {
			var field = ReconstructibleLocusEvaluator2D.class.getDeclaredField("programV3Origins");
			field.setAccessible(true);
			for (Object origin : (List<?>) field.get(evaluator)) {
				var anchorMethod = origin.getClass().getDeclaredMethod("getAnchor");
				var pathMethod = origin.getClass().getDeclaredMethod("getMacroPath");
				var operationMethod = origin.getClass().getDeclaredMethod("getOperation");
				anchorMethod.setAccessible(true); pathMethod.setAccessible(true);
				operationMethod.setAccessible(true);
				GeoElement anchor = (GeoElement) anchorMethod.invoke(origin);
				if (anchor != null && anchor.getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(anchor) != null) { continue; }
				System.out.println("R9_CONE_UNIDENTIFIED_ORIGIN " + operationMethod.invoke(origin)
						+ " path=" + pathMethod.invoke(origin) + " anchor="
						+ (anchor == null ? "null" : anchor.getLabelSimple() + ":"
								+ anchor.getClass().getSimpleName() + ":"
								+ (anchor.getParentAlgorithm() == null ? "independent"
										: anchor.getParentAlgorithm().getClass().getSimpleName())));
			}
		} catch (ReflectiveOperationException failure) {
			throw new AssertionError("The saved diagnostic must identify its actual proof origins", failure);
		}
	}

	private static void dumpSeam(GeoLocusV2 locus) {
		try {
			var interval = Class.forName("org.geocedg.common.kernel.locus.intersection.SplineOutwardInterval2D");
			var constructor = interval.getDeclaredConstructor(double.class, double.class);
			constructor.setAccessible(true);
			var method = ConstructionBoxModel2D.class.getDeclaredMethod("evaluateCertifying", interval, boolean.class);
			method.setAccessible(true);
			var outcome = (ConstructionBoxOutcome2D) method.invoke(
					ConstructionBoxModel2D.capture(locus.getSemanticDefinition()),
					constructor.newInstance(Math.PI, Math.nextUp(Math.PI)), true);
			var detail = ConstructionBoxOutcome2D.class.getDeclaredField("detail");
			detail.setAccessible(true);
			System.out.println("R9_CONE_SEAM " + outcome.getKind() + " step=" + outcome.getStep()
					+ " " + outcome.getUnresolved() + " " + detail.get(outcome));
		} catch (ReflectiveOperationException failure) {
			throw new AssertionError("The seam diagnostic must inspect the current proof model", failure);
		}
	}
}

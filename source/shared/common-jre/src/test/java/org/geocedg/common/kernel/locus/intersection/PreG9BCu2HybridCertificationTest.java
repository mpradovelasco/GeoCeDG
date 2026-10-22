/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D;
import org.geocedg.common.kernel.locus.exact.ExactBudget1D;
import org.geocedg.common.kernel.locus.exact.ExactBudgetExhaustedException;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.exact.RealRootStructure1D;
import org.geocedg.common.kernel.locus.exact.TrigonometricPolynomial2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot.RootKind;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.ExactEvidence;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.UndoRedoMode;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T4: the hybrid existence certifier ({@code locus-existence/hybrid/v1};
 * {@code locus-v2-existence-components.md} section 15; {@code
 * locus-v2-certified-construction-model.md} sections 10.5 to 10.9) on synthetic
 * constructions, through the certificate of each revision (published since step T11). Matrix
 * rows V-TOT-1, V-PER-1 (synthetic part), V-CRIT-1 to V-CRIT-6 and V-CRIT-10, V-TR-2, V-POINT-1,
 * V-POINT-2, V-POINT-4 and V-POINT-6 (certificate parts), V-COINC-1, V-COINC-2, V-COINC-4,
 * V-COINC-5 and V-COINC-7 (certificate parts), V-LVL-1, V-LVL-3 (structure part), V-COMPL-1,
 * V-V3-3 and V-V3-4 (existence parts), and gates GC-4 and GC-8. Corrective descendant T13-R4
 * (decision D-CU2-T13-R4): V-DET-1 on the synthetic constructions and V-INV-2.
 */
class PreG9BCu2HybridCertificationTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	/** Isolated root of the synthetic external tangency (matrix section 1). */
	private static final double EXTERNAL = Math.atan2(-4, -3);
	/** Transition bracket width of the default policy: {@code 2^-40} periods. */
	private static final double DELTA = Math.scalb(2 * Math.PI, -40);
	/**
	 * V-INV-2: factor that makes every constant of matrix section 1 a multiple of 125, so
	 * that scales from {@code 10^-3} to {@code 10^3} are exact in binary64.
	 */
	private static final BigDecimal UNIT = BigDecimal.valueOf(125);
	private static final MathContext REFERENCE = new MathContext(60, RoundingMode.HALF_EVEN);
	/** Pi to 60 digits, for the enclosure of the seam. */
	private static final BigDecimal PI = new BigDecimal(
			"3.14159265358979323846264338327950288419716939937510582097494");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-TOT-1: a program of structurally total steps is certified on the whole circle without
	 * any box.
	 */
	@Test
	void structuralTotalityDecidesWithoutAnyBox() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("M=Midpoint(N,(3,4))");
		add("lm=LocusV2(M,N)");
		add("T=Translate(N,Vector((1,2)))");
		add("lt=LocusV2(T,N)");
		for (String label : List.of("lm", "lt")) {
			long boxes = work(label, LocusCertificationWork2D.EXISTENCE_BOX);
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(HybridExistenceCertifier2D.CONTRACT, structure.getCertifierContract());
			assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label);
			assertTrue(structure.isClosedPeriodic(), label);
			assertEquals(LocusEvidenceNature2D.EXACT,
					structure.getContinuousValidComponents().get(0).getNature());
			assertEquals(0, result.getExistenceBoxes(), label);
			assertEquals(boxes, work(label, LocusCertificationWork2D.EXISTENCE_BOX), label);
		}
	}

	/**
	 * V-CRIT-1, V-CRIT-4, V-COINC-1 and V-PER-1 (synthetic part): the exact critical
	 * configuration is one closed periodic component with a singular parameter of
	 * multiplicity 2 at {@code atan2(4, 3)}, with neither a transition nor a band; the boxes
	 * that wrap the seam certify the closure.
	 */
	@Test
	void theCriticalConfigurationIsOneClosedComponentWithASingularParameter() {
		critical("40");
		for (String label : List.of("lq", "lr")) {
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label);
			assertTrue(structure.isClosedPeriodic(), label);
			assertEquals(1, structure.getContinuousValidComponents().size(), label);
			assertEquals(0, result.getCertifiedTransitions(), label);
			assertEquals(0, result.getUnresolvedBands(), label);
			for (Coverage coverage : structure.getCoverage()) {
				assertEquals(CoverageState.CERTIFIED_EXISTS, coverage.getState(), label);
			}
			LocusSingularParameter2D singular = structure.getSingularParameters().get(0);
			assertEquals(1, structure.getSingularParameters().size(), label);
			assertEquals(2, singular.getMultiplicity());
			assertEquals(LocusEvidenceNature2D.EXACT, singular.getNature());
			assertEnclosed(CRITICAL, singular.getParameterLower(),
					singular.getParameterUpper());
			assertExactWithinBudget(result);
		}
		// V-CRIT-6, live part: Q = R at the singular parameter.
		moveDriver((GeoPoint) lookup("N"), CRITICAL);
		assertEquals(((GeoPoint) lookup("Q")).getInhomX(), ((GeoPoint) lookup("R"))
				.getInhomX(), 1e-6);
	}

	/**
	 * V-CRIT-2, V-COINC-4 and V-COINC-5 (labelled outputs): below the critical radius there is
	 * no real root and the component is closed; above it two simple roots bound one arc that
	 * crosses the seam, with closed certified ends whose enclosures contain the exact roots,
	 * and the gap between them is certified nonexistence.
	 */
	@Test
	void theNeighboursOfTheCriticalRadiusArePenetrationAndBite() {
		for (int k : new int[] {10, 30, 47}) {
			critical("40-2^(-" + k + ")");
			for (String label : List.of("lq", "lr")) {
				Result result = certificate(label);
				LocusExistenceStructure2D structure = result.getStructure();
				assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label + k);
				assertTrue(structure.isClosedPeriodic(), label + k);
				assertTrue(structure.getSingularParameters().isEmpty(), label + k);
				assertTrue(result.getRoots().isEmpty(), label + k);
			}
			getKernel().clearConstruction(true);
			critical("40+2^(-" + k + ")");
			for (String label : List.of("lq", "lr")) {
				Result result = certificate(label);
				LocusExistenceStructure2D structure = result.getStructure();
				assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label + k);
				assertFalse(structure.isClosedPeriodic(), label + k);
				assertEquals(2, result.getCertifiedTransitions(), label + k);
				ContinuousComponent arc = structure.getContinuousValidComponents().get(0);
				assertEquals(1, structure.getContinuousValidComponents().size());
				assertTrue(arc.crossesSeam(), label + k);
				assertEquals(Maximality.ESTABLISHED, arc.getMaximality());
				LocusBoundary2D start = arc.getLowerBoundary();
				LocusBoundary2D end = arc.getUpperBoundary();
				for (LocusBoundary2D boundary : List.of(start, end)) {
					assertEquals(LocusBoundary2D.Kind.CERTIFIED_TRANSITION, boundary.getKind());
					assertTrue(boundary.isIncluded(), "a discriminant root is a closed end");
					assertEquals(LocusEvidenceNature2D.EXACT, boundary.getNature());
					assertTrue(boundary.getEnclosureUpper() - boundary.getEnclosureLower()
							<= DELTA);
				}
				// The arc ends just before the gap and starts just after it.
				assertTrue(end.getEnclosureUpper() < CRITICAL);
				assertTrue(CRITICAL < start.getEnclosureLower());
				assertEquals(end.getEnclosureLower(), arc.getEnd());
				assertEquals(start.getEnclosureUpper(), arc.getStart());
				Coverage gap = coverageAt(structure, CRITICAL);
				assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, gap.getState());
				assertEquals(LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE, gap.getCause());
				assertTrue(gap.getEvidence().endsWith("DISCRIMINANT_NEGATIVE"));
				assertExactWithinBudget(result);
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-CRIT-5, V-POINT-1, V-POINT-4 and V-POINT-6 (certificate parts): the external tangency
	 * is a point component, never an interval: an enclosure of {@code atan2(-4, -3)}, closed,
	 * maximal between certified nonexistence, with the certified position (15, 20); the cover
	 * is complete, and no binary64 parameter selects the point.
	 */
	@Test
	void anExternalTangencyIsAPointComponent() {
		tangency("(83,19)", "Ray");
		Result result = certificate("lq");
		LocusExistenceStructure2D structure = result.getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertEquals(1, structure.getPointComponents().size());
		LocusPointComponent2D point = structure.getPointComponents().get(0);
		assertEnclosed(EXTERNAL, point.getParameterLower(), point.getParameterUpper());
		assertTrue(point.getParameterUpper() - point.getParameterLower() <= DELTA);
		assertEquals(Maximality.ESTABLISHED, point.getMaximality());
		assertEquals("DISCRIMINANT", point.getSource().getPredicate());
		assertEquals(LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE,
				point.getPositionState());
		double[] position = point.getPositionEnclosure();
		assertEnclosed(15, position[0], position[1]);
		assertEnclosed(20, position[2], position[3]);
		assertEquals(0, point.getLength());
		assertNull(structure.getComponentContaining(EXTERNAL));
		assertNull(structure.getComponentContaining(Math.nextUp(EXTERNAL)));
		assertEquals(CoverageState.CERTIFIED_ISOLATED_POINTS,
				coverageAt(structure, EXTERNAL).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, EXTERNAL + 0.01).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, EXTERNAL - 0.01).getState());
	}

	/**
	 * V-POINT-2 and V-POINT-6: two point components, one at the seam, with distinct sources and
	 * certified nonexistence between them; neither joins the other across the seam.
	 */
	@Test
	void twoPointComponentsAreSeparatedOneAtTheSeam() {
		tangency("(15,70)", "Line");
		LocusExistenceStructure2D structure = certificate("lq").getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		List<LocusPointComponent2D> points = structure.getPointComponents();
		assertEquals(2, points.size());
		LocusPointComponent2D seam = points.get(0);
		assertEquals(-Math.nextUp(Math.PI), seam.getParameterLower());
		assertEquals(-Math.PI, seam.getParameterUpper());
		assertEnclosed(EXTERNAL, points.get(1).getParameterLower(),
				points.get(1).getParameterUpper());
		assertNotEquals(seam.getSource(), points.get(1).getSource());
		for (LocusPointComponent2D point : points) {
			assertEquals(Maximality.ESTABLISHED, point.getMaximality());
		}
		double[] position = seam.getPositionEnclosure();
		assertEnclosed(-25, position[0], position[1]);
		assertEnclosed(0, position[2], position[3]);
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, 0.5 * (EXTERNAL - Math.PI)).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, 3).getState());
	}

	/**
	 * V-COINC-2, V-COINC-5 and V-COINC-7 (certificate parts) and V-LVL-3 (structure part): an
	 * unlabelled second output has a typed unresolved band around the coincident root that
	 * contains every undefined sample of the live construction and of the copy; its locus is
	 * not established, with a local component on both sides of the band, which a local
	 * consumer accepts away from the band.
	 */
	@Test
	void anUnlabelledSecondOutputLeavesLocalComponentsAroundABand() {
		for (String radius : List.of("40", "40+2^(-30)")) {
			critical(radius);
			Result result = certificate("lr2");
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness(), radius);
			Coverage band = coverageAt(structure, CRITICAL - 0.01);
			assertEquals(CoverageState.UNRESOLVED, band.getState(), radius);
			assertEquals(LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT, band.getCause());
			assertNotEquals(CoverageState.CERTIFIED_EXISTS,
					coverageAt(structure, CRITICAL).getState(), radius);
			ContinuousComponent arc = structure.getContinuousValidComponents().get(0);
			assertEquals(1, structure.getContinuousValidComponents().size(), radius);
			assertEquals(Maximality.LOCAL_CERTIFIED_SUBINTERVAL, arc.getMaximality());
			assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
					arc.getLowerBoundary().getKind());
			assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
					arc.getUpperBoundary().getKind());
			assertSame(arc, structure.getComponentContaining(CRITICAL + 0.2));
			assertSame(arc, structure.getComponentContaining(CRITICAL - 0.2));
			assertNull(structure.getComponentContaining(CRITICAL));
			for (CertifiedRoot root : result.getRoots()) {
				assertTrue(root.isInsideBand(), root.toString());
			}
			assertNotNull(result.getExploration());
			assertEquals(LocusEvidenceNature2D.EXPLORATORY,
					result.getExploration().getNature());
			LocusDefinition2D definition = locus("lr2").getSemanticDefinition();
			GeoPoint driver = (GeoPoint) lookup("N");
			GeoPoint live = (GeoPoint) lookup("R2");
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (int sample = -100; sample <= 100; sample++) {
					double parameter = CRITICAL + sample * 1e-10;
					moveDriver(driver, parameter);
					boolean copy = definition.evaluate(BRANCH, parameter, session).isValid();
					CoverageState state = coverageAt(structure, parameter).getState();
					if (live.isDefined() != copy) {
						// The certificate states nothing where the live point and the copy differ.
						assertEquals(CoverageState.UNRESOLVED, state, "t=" + parameter);
					} else if (!copy) {
						assertNotEquals(CoverageState.CERTIFIED_EXISTS, state, "t=" + parameter);
					}
				}
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-TR-2: end inclusion follows the predicate: closed at a ray parameter, a segment
	 * parameter and a discriminant root, open at a vanishing divisor, whose inherited
	 * finiteness test leaves an unresolved band around the root.
	 */
	@Test
	void endInclusionFollowsThePredicate() {
		add("cc=Circle((0,0),5)");
		add("d=Circle((3,0),3)");
		add("A=Point(d)");
		moveDriver((GeoPoint) lookup("A"), 0);
		add("B=(-10,0)");
		add("r=Ray(A,B)");
		add("s=Segment(A,B)");
		add("Y=Intersect(cc,r,1)");
		add("Ys=Intersect(cc,s,1)");
		getKernel().updateConstruction(false);
		add("ly=LocusV2(Y,A)");
		add("lys=LocusV2(Ys,A)");
		getKernel().updateConstruction(false);
		assertClosedTransitions(certificate("ly"), "RAY_PARAMETER-member-0");
		assertClosedTransitions(certificate("lys"), "SEGMENT_PARAMETER-member-0");
		getKernel().clearConstruction(true);
		critical("40+2^(-10)");
		assertClosedTransitions(certificate("lq"), "DISCRIMINANT");
		getKernel().clearConstruction(true);
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("g=Line(N,(5,0))");
		add("S=Intersect(g,Line((0,-10),(1,-10)))");
		add("ls=LocusV2(S,N)");
		Result divisor = certificate("ls");
		assertEquals(Completeness.NOT_ESTABLISHED, divisor.getStructure().getCompleteness());
		List<CertifiedRoot> roots = divisor.getRoots();
		assertEquals(2, roots.size(), roots.toString());
		for (CertifiedRoot root : roots) {
			assertEquals(RootKind.OPEN_EVENT, root.getKind());
			assertFalse(root.isIncluded(), "a vanishing divisor is an open end");
			assertTrue(root.isInsideBand());
			assertEquals(LocusEvidenceNature2D.EXACT, root.getNature());
		}
		assertEnclosed(0, roots.get(1).getLower(), roots.get(1).getUpper());
		assertEquals(CoverageState.UNRESOLVED,
				coverageAt(divisor.getStructure(), 0).getState());
		for (ContinuousComponent component : divisor.getStructure()
				.getContinuousValidComponents()) {
			assertEquals(Maximality.LOCAL_CERTIFIED_SUBINTERVAL, component.getMaximality());
		}
	}

	/**
	 * V-COMPL-1: a box budget exhausted before the domain is covered never gives a complete
	 * certificate, even though every explored box is valid; the uncovered region is explicit.
	 */
	@Test
	void anExhaustedBudgetIsNeverComplete() {
		critical("39");
		LocusDefinition2D definition = locus("lq").getSemanticDefinition();
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(definition);
		Result complete = HybridExistenceCertifier2D.certify(model,
				definition.getBranch(BRANCH).getDeclaredDriverDomain(), true, BRANCH, null,
				Policy.DEFAULT, null);
		assertEquals(Completeness.COMPLETE, complete.getStructure().getCompleteness());
		Result exhausted = HybridExistenceCertifier2D.certify(model,
				definition.getBranch(BRANCH).getDeclaredDriverDomain(), true, BRANCH, null,
				Policy.DEFAULT.withBoxBudget(16), null);
		LocusExistenceStructure2D structure = exhausted.getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertEquals(16, exhausted.getExistenceBoxes());
		boolean uncovered = false;
		for (Coverage coverage : structure.getCoverage()) {
			assertNotEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, coverage.getState());
			uncovered |= coverage.getCause() == LocusCoverageCause2D.BUDGET_EXHAUSTED;
		}
		assertTrue(uncovered);
		assertNotEquals(complete.getPolicySignature(), exhausted.getPolicySignature());
	}

	/**
	 * V-LVL-1, V-V3-3 and V-PER-2: a conic-conic intersection, a reference-point intersection
	 * and a random value that depends on the driver are outside class v3: the certificate is
	 * not established, has no component, claims no closure, and only exploratory evidence
	 * describes the locus.
	 */
	@Test
	void outOfClassLociHaveOnlyExploratoryEvidence() {
		add("e1=Ellipse((0,0),(4,0),5)");
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("k=Circle(N,5)");
		add("W=Intersect(e1,k,1)");
		add("lw=LocusV2(W,N)");
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("M=Point(g1)");
		add("t=Ray(P,M)");
		add("Rz=Intersect(p,t,(10,10))");
		add("lz=LocusV2(Rz,M)");
		add("X=N+(RandomUniform(0,1)*x(N),0)");
		add("lx=LocusV2(X,N)");
		getKernel().updateConstruction(false);
		for (String label : List.of("lw", "lz", "lx")) {
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness(), label);
			assertTrue(structure.getContinuousValidComponents().isEmpty(), label);
			assertFalse(structure.isClosedPeriodic(), label);
			assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
					structure.getCoverage().get(0).getCause(), label);
			assertNotNull(result.getExploration(), label);
			assertTrue(result.getExploration().getWork() > 0, label);
			assertFalse(result.getExploration().getNature().certifies());
			// Since step T11 the certificate is the published structure: never a fallback.
			LocusExistenceStructure2D published = locus(label).getSemanticDefinition()
					.getBranch(BRANCH).getExistenceStructure();
			assertEquals(HybridExistenceCertifier2D.CONTRACT,
					published.getCertifierContract(), label);
			assertEquals(structure.getCoverage().get(0).getEvidence(),
					published.getCoverage().get(0).getEvidence(), label);
			assertSame(result.getExploration(), locus(label).getExploratoryEvidence(BRANCH),
					label);
		}
	}

	/**
	 * V-V3-4, existence part: a slice above the versioned step limit is not established, and
	 * the refusal is typed.
	 */
	@Test
	void theStepLimitRefusalIsTyped() {
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("v=Vector((0.001,0))");
		String previous = "N";
		for (int index = 1; index <= CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3 + 1;
				index++) {
			add("T" + index + "=Translate(" + previous + ",v)");
			previous = "T" + index;
		}
		add("outside=LocusV2(" + previous + ",N)");
		LocusExistenceStructure2D structure = certificate("outside").getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		Coverage coverage = structure.getCoverage().get(0);
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS, coverage.getCause());
		assertTrue(coverage.getEvidence().endsWith("step-limit-exceeded"),
				coverage.getEvidence());
	}

	/**
	 * V-CRIT-3: with exact rational data the certificate separates roots {@code 2^-40} and
	 * {@code 2^-100} apart into disjoint enclosures, or reports its exhausted budget; it never
	 * merges them into one root.
	 */
	@Test
	void exactCertificatesSeparateArbitrarilyCloseRoots() {
		for (int k : new int[] {80, 200}) {
			ExactRational2D radius = ExactRational2D.of(40).add(ExactRational2D.of(
					BigInteger.ONE, BigInteger.ONE.shiftLeft(k)));
			TrigonometricPolynomial2D discriminant = criticalDiscriminant(radius);
			try {
				CircleDriverRootStructure2D structure = CircleDriverRootStructure2D.classify(
						discriminant, ExactRational2D.ofBinary64(DELTA / 4),
						ExactBudget1D.DEFAULT);
				List<CircleDriverRootStructure2D.Root> roots = structure.roots();
				assertEquals(2, roots.size(), "k=" + k);
				for (CircleDriverRootStructure2D.Root root : roots) {
					assertEquals(1, root.multiplicity());
				}
				assertTrue(roots.get(0).uUpper().compareTo(roots.get(1).uLower()) < 0,
						"disjoint isolating intervals for k=" + k);
			} catch (ExactBudgetExhaustedException exhausted) {
				// A typed exhaustion decides nothing and is acceptable.
				assertNotNull(exhausted.getMessage());
			}
		}
	}

	/**
	 * V-CRIT-10: certificates, exact evidence and roots are identical bit for bit on a new
	 * construction with the same state. Since step T8 a boundary source names its operation
	 * by the durable identity of its geo, which a new construction allocates anew; those
	 * identities are the only part compared up to renaming.
	 */
	@Test
	void certificatesAreDeterministic() {
		List<String> first = signatures();
		getKernel().clearConstruction(true);
		assertEquals(withoutDurableIdentities(first),
				withoutDurableIdentities(signatures()));
		assertTrue(first.stream().anyMatch(text -> text.contains("geo:")),
				"the sources name durable operation identities");
	}

	private static List<String> withoutDurableIdentities(List<String> signatures) {
		List<String> result = new ArrayList<>();
		for (String signature : signatures) {
			result.add(signature.replaceAll("geo:[0-9a-f]{32}", "geo:*"));
		}
		return result;
	}

	/**
	 * V-DET-1 (synthetic constructions of the matrix; corrective descendant T13-R4): two runs,
	 * each on a new document loaded from the same saved construction and taken through the
	 * same changes, then undone, redone and reopened, give results identical bit for bit at
	 * every step: the semantic revision, the slice fingerprint and the published branch of
	 * every locus (coverage, components with their ends, brackets and sources, singular
	 * parameters, point components, folds and the branch lineage), its roots, exact evidence,
	 * stationary points and box count, its lineage keys and transitions, the events of its
	 * slice (settlement outcome, fidelity report with its departures, captured selection
	 * rule), every image of a locus, every point of a locus and the structure of every metric
	 * with its events. The saved document carries the durable identities, so nothing is
	 * normalized. The constructions are the critical radius with labelled and unlabelled
	 * outputs, started at the double root in its first initialization and taken off it, and
	 * with similarity images; its penetration taken to a bite and back (split and merge) with a
	 * point of the locus selected by its parameter; a copy rebuilt with the driver in a gap and
	 * settled; the external tangency taken from an empty cover to a point, an arc and the point
	 * again (changes of dimension), two point components, the band of an unlabelled output, a
	 * fold, an origin wrap, the ends of a ray, a segment and a vanishing divisor, a structurally
	 * total locus, a locus outside the class and the bite started with the driver in its gap. A
	 * random value is not deterministic by definition and is left out.
	 */
	@Test
	void twoRunsOfTheSyntheticConstructionsGiveIdenticalResults() {
		Map<String, Runnable> builders = new LinkedHashMap<>();
		Map<String, List<Change>> changes = new LinkedHashMap<>();
		builders.put("critical", () -> critical("40"));
		builders.put("split and merge", () -> {
			add("rr=40-2^(-10)");
			critical("rr");
			// A point of the locus selected by its parameter (component selectors).
			add("v=0.3");
			add("A=Point(lq,\"" + BRANCH + "\",v)");
		});
		changes.put("split and merge", List.of(Change.radius(40 + Math.scalb(1.0, -10)),
				Change.radius(40 + Math.scalb(1.0, -4)), Change.radius(40 - Math.scalb(1.0, -10))));
		builders.put("first initialization at the double root", () -> {
			critical("40");
			moveDriver((GeoPoint) lookup("N"), CRITICAL);
		});
		changes.put("first initialization at the double root",
				List.of(Change.driver(CRITICAL + 0.1), Change.driver(CRITICAL + 0.2)));
		builders.put("settlement in a gap", () -> {
			add("rr=0");
			add("C=(0,rr)");
			add("k=Circle(C,5)");
			add("g1=Circle((0,0),20)");
			add("N=Point(g1)");
			add("m=Line(N,N+(1,0))");
			moveDriver((GeoPoint) lookup("N"), 0);
			getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
					(GeoLine) lookup("m"), (GeoConic) lookup("k"));
			moveDriver((GeoPoint) lookup("N"), 1e-4);
			add("ls=LocusV2(S,N)");
		});
		changes.put("settlement in a gap", List.of(Change.driver(Math.PI / 2),
				Change.radius(0.5), Change.radius(0.6)));
		builders.put("similarity images", () -> {
			add("rr=40");
			critical("rr");
			add("img=Translate(lq,(1,0))");
			add("dil=Dilate(lr,2,(0,0))");
		});
		changes.put("similarity images", List.of(Change.radius(40 + Math.scalb(1.0, -10))));
		builders.put("change of dimension", () -> {
			add("rr=40-2^(-30)");
			add("p=Circle((0,0),25)");
			add("P=(-25,50)");
			add("g1=Circle((83,19),rr)");
			add("N=Point(g1)");
			moveDriver((GeoPoint) lookup("N"), EXTERNAL + Math.PI);
			add("t=Ray(P,N)");
			getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
					(GeoLine) lookup("t"), (GeoConic) lookup("p"));
			add("lq=LocusV2(Q,N)");
			getKernel().updateConstruction(false);
		});
		changes.put("change of dimension", List.of(Change.radius(40.0),
				Change.radius(40 + Math.scalb(1.0, -30)), Change.radius(40.0)));
		builders.put("point components", () -> tangency("(15,70)", "Line"));
		builders.put("band", () -> critical("40+2^(-30)"));
		builders.put("fold", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("X=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
			add("lx=LocusV2(X,N)");
		});
		builders.put("origin wrap", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("K=N+(1,0)");
			add("o=LocusV2(K,N)");
			add("v=0");
			add("A=Point(o,\"" + BRANCH + "\",v)");
			add("w=LocusLength(o,A,K)");
			add("d=Length(o,A,K)");
			add("R=N-d*(1,0)");
			add("e=LocusV2(R,N)");
		});
		builders.put("ray, segment and divisor ends", () -> {
			add("cc=Circle((0,0),5)");
			add("d=Circle((3,0),3)");
			add("A=Point(d)");
			moveDriver((GeoPoint) lookup("A"), 0);
			add("B=(-10,0)");
			add("r=Ray(A,B)");
			add("s=Segment(A,B)");
			add("Y=Intersect(cc,r,1)");
			add("Ys=Intersect(cc,s,1)");
			add("ly=LocusV2(Y,A)");
			add("lys=LocusV2(Ys,A)");
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("g=Line(N,(5,0))");
			add("S=Intersect(g,Line((0,-10),(1,-10)))");
			add("ls=LocusV2(S,N)");
		});
		builders.put("totality and outside the class", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("M=Midpoint(N,(3,4))");
			add("lm=LocusV2(M,N)");
			add("e1=Ellipse((0,0),(4,0),5)");
			add("k=Circle(N,5)");
			add("W=Intersect(e1,k,1)");
			add("lw=LocusV2(W,N)");
		});
		builders.put("started in the gap", () -> {
			critical("40+2^(-10)");
			moveDriver((GeoPoint) lookup("N"), CRITICAL);
		});
		StringBuilder compared = new StringBuilder();
		for (Map.Entry<String, Runnable> builder : builders.entrySet()) {
			builder.getValue().run();
			String document = getApp().getXML();
			getKernel().clearConstruction(true);
			List<Change> steps = changes.getOrDefault(builder.getKey(), List.of());
			String first = run(document, steps);
			assertEquals(first, run(document, steps), builder.getKey());
			compared.append(first);
		}
		for (String part : List.of("geo:", "|coverage=", "|component=", "|singular=",
				"|point=", "|fold=", "TRANSITION[", "OPEN_EVENT[", "|coefficients=",
				"ORIGIN_WRAP", "OPERATION_OUTSIDE_CLASS", "UNLABELLED_COINCIDENT_OUTPUT",
				"SPLIT[", "MERGED[", "DIMENSION_CHANGED[", "settlement=settled",
				"2LINE_CIRCLE[0x1.0p0", " image revision=", "A point=", "undo 0", "redo 0",
				"reopened")) {
			assertTrue(compared.indexOf(part) >= 0, "the runs compare " + part);
		}
		for (String event : List.of("SELECTION_SETTLEMENT=[1-9]", "SELECTION_DEPARTURE=[1-9]",
				"CERTIFICATE_COMPUTATION=[1-9]")) {
			assertTrue(Pattern.compile(event).matcher(compared).find(),
					"the runs compare the event " + event);
		}
	}

	/**
	 * One run of V-DET-1: a new document loaded from the saved construction, then each change
	 * in turn, every change undone and redone, and the document reopened; the results after
	 * loading and after each operation.
	 */
	private static String run(String document, List<Change> changes) {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.setXML(document, true);
		app.setUndoRedoMode(UndoRedoMode.GUI);
		app.setUndoActive(true);
		app.storeUndoInfo();
		StringBuilder text = new StringBuilder(results(app, "loaded"));
		for (Change change : changes) {
			change.action.accept(app);
			app.storeUndoInfo();
			text.append(results(app, change.name));
		}
		for (int step = 0; step < changes.size(); step++) {
			app.getKernel().undo();
			text.append(results(app, "undo " + step));
		}
		for (int step = 0; step < changes.size(); step++) {
			app.getKernel().redo();
			text.append(results(app, "redo " + step));
		}
		app.setXML(app.getXML(), true);
		return text.append(results(app, "reopened")).toString();
	}

	/** A named change of a run of V-DET-1. */
	private static final class Change {
		private final String name;
		private final Consumer<AppCommon> action;

		private Change(String name, Consumer<AppCommon> action) {
			this.name = name;
			this.action = action;
		}

		/** The free radius {@code rr} set to the value. */
		static Change radius(double value) {
			return new Change("rr=" + Double.toHexString(value), app -> {
				GeoNumeric number = (GeoNumeric) app.getKernel().lookupLabel("rr");
				number.setValue(value);
				number.updateCascade();
				app.getKernel().updateConstruction(false);
			});
		}

		/** The driver {@code N} moved to the parameter. */
		static Change driver(double parameter) {
			return new Change("N at " + Double.toHexString(parameter), app -> moveDriver(
					(GeoPoint) app.getKernel().lookupLabel("N"), parameter));
		}
	}

	/** @return every result of the document that a second run must reproduce bit for bit */
	private static String results(AppCommon app, String step) {
		StringBuilder text = new StringBuilder("step ").append(step).append('\n');
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoDependentPointLocusV2) {
				AlgoDependentPointLocusV2 algorithm =
						(AlgoDependentPointLocusV2) geo.getParentAlgorithm();
				GeoLocusV2 locus = (GeoLocusV2) geo;
				LocusBranch2D branch = locus.getSemanticDefinition() == null ? null
						: locus.getSemanticDefinition().getBranch(BRANCH);
				text.append(geo.getLabelSimple()).append(" revision=")
						.append(locus.getSemanticRevision()).append(" fingerprint=")
						.append(ReconstructionSliceFingerprint2D.compute(
								algorithm.getReconstructionSliceGeos(), List.of(
										sliceRoot(algorithm, "state"),
										sliceRoot(algorithm, "coordinate"))))
						.append("\n branch=").append(branch == null ? "none"
								: branch.getSemanticSignature());
				Result result = algorithm.getHybridExistenceCertificate();
				if (result != null) {
					text.append("\n roots=").append(result.getRoots().stream()
							.map(CertifiedRoot::getSignature).collect(Collectors.joining(";")))
							.append("\n exact=").append(result.getExactEvidence().stream()
									.map(ExactEvidence::getSignature)
									.collect(Collectors.joining(";")))
							.append("\n stationary=").append(result.getStationaryPoints().stream()
									.map(HybridExistenceCertifier2D.CertifiedStationaryPoint
											::getSignature)
									.collect(Collectors.joining(";")))
							.append("\n boxes=").append(result.getExistenceBoxes())
							.append("\n keys=").append(LocusComponentLineage2D.keys(
									result.getStructure()))
							.append("\n lineage=").append(algorithm.getHybridLineage());
				}
				if (locus.getSemanticDefinition() != null
						&& locus.getSemanticDefinition().getEvaluatorCapability()
								instanceof ReconstructibleLocusEvaluator2D) {
					// The events of the slice: settlement, departures and the captured rule.
					ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D)
							locus.getSemanticDefinition().getEvaluatorCapability();
					text.append("\n settlement=").append(evaluator.getSettlementOutcome()
							.getToken()).append(" fidelity=").append(evaluator.getFidelityReport())
							.append("\n rule=").append(evaluator.captureCertifiedProgramV3()
									.map(CertifiedConstructionProgram2D::getSelectionSignature)
									.orElse("refused " + evaluator.getProgramV3Refusal()));
				}
				// The work of certification counts its events: settlements, departures.
				text.append("\n work=");
				for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
					text.append(kind.name()).append('=').append(locus.getInstrumentation()
							.getCertificationWork(kind)).append(' ');
				}
				text.append('\n');
			} else if (geo instanceof GeoLocusV2) {
				// An image of a locus, by a similarity or another construction on it.
				GeoLocusV2 image = (GeoLocusV2) geo;
				text.append(geo.getLabelSimple()).append(" image revision=")
						.append(image.getSemanticRevision());
				if (image.getSemanticDefinition() != null) {
					for (LocusBranch2D branch : image.getSemanticDefinition().getBranches()) {
						text.append("\n branch=").append(branch.getSemanticSignature());
					}
				}
				text.append('\n');
			} else if (geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
				GeoPoint point = (GeoPoint) geo;
				text.append(geo.getLabelSimple()).append(" point=").append(point.isDefined()
						? Double.toHexString(point.getInhomX()) + ","
								+ Double.toHexString(point.getInhomY()) : "undefined")
						.append('\n');
			} else if (geo.getParentAlgorithm() instanceof AlgoLocusBetweenMetricV2) {
				text.append(geo.getLabelSimple()).append(" metric=").append(
						((AlgoLocusBetweenMetricV2) geo.getParentAlgorithm())
								.getFunctionStructure().getSignature()).append('\n');
			}
		}
		return text.toString();
	}

	/** @return the driver state or coordinate that the slice of a locus starts from */
	private static GeoElement sliceRoot(AlgoDependentPointLocusV2 algorithm, String name) {
		try {
			java.lang.reflect.Field field = AlgoDependentPointLocusV2.class
					.getDeclaredField(name);
			field.setAccessible(true);
			return (GeoElement) field.get(algorithm);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * V-INV-2 (corrective descendant T13-R4): with every constant a multiple of 125, the
	 * critical radius, its bite and penetration at {@code 40 +- 2^-10}, the external
	 * tangency and the line of the two point components of matrix section 1 are
	 * translated by {@code 10^3} and scaled from {@code 10^-3} to {@code 10^3} as a whole,
	 * exactly in binary64: the typed inputs and the captured constants are the exact
	 * transformed constants, bit for bit. The structure is the same: completeness, coverage
	 * states and causes, components with their maximality, ends and boundary sources
	 * (operation, predicate and side), singular parameters and point components; the durable
	 * operation identities of different constructions are compared through the labels of their
	 * geos. The brackets contain the transformed references, proved with 60-digit arithmetic
	 * on the captured constants: {@code atan2(4, 3)} and {@code atan2(-4, -3)} by the monotony
	 * of the cosine, the seam by 60 digits of pi, the simple roots of the bite by a sign change
	 * of the discriminant; the position of each point component encloses the transformed
	 * point (15, 20) or, at the seam, (-25, 0), and every certified piece agrees with the
	 * discriminant at its samples.
	 */
	@Test
	void translationsAndScalesOfTheWholeConstructionKeepTheStructure() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<Transform> transforms = new ArrayList<>();
		transforms.add(Transform.of(UNIT, "1", "1000", "1000"));
		transforms.add(Transform.of(UNIT, "1", "-1000", "1000"));
		for (String scale : List.of("0.001", "0.01", "0.1", "10", "100", "1000")) {
			transforms.add(Transform.of(UNIT, scale, "0", "0"));
		}
		transforms.add(Transform.of(UNIT, "0.001", "1000", "1000"));
		transforms.add(Transform.of(UNIT, "1000", "-1000", "1000"));
		List<String> problems = new ArrayList<>();
		for (Transform transform : transforms) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				assertExact(context, configuration, transform, inputs());
				double[] constants = captured();
				assertCapturedExact(context, configuration, transform, constants);
				for (String label : configuration.loci) {
					assertEquals(Completeness.COMPLETE, structure(label).getCompleteness(),
							context + " " + label);
					assertEquals(reference.get(configuration).get(label), descriptor(label),
							context + " " + label);
					references(context + " " + label, label, configuration, constants,
							transform, problems);
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * V-INV-2, captured constants: in the units of matrix section 1, scaled by {@code 10^-3}
	 * or translated by {@code (1000.1, -999.7)}, the constants are rounded when they are
	 * captured, and the exact certificate classifies them as captured. The test reads the
	 * captured constants back, builds their discriminant with exact rationals and classifies
	 * its roots; the certificate has exactly the transitions, singular parameters and point
	 * components of that classification, each transition bracket shows a sign change of the
	 * 60-digit discriminant of the captured constants, and every certified piece agrees with
	 * that discriminant at its samples. Independently of any classification, the 60-digit
	 * discriminant changes sign at 4096 samples only where the certificate changes state, and
	 * shows a double root of the right side in every singular parameter and point component.
	 * The bite and the penetration, whose margin is far above the rounding, keep their
	 * structure.
	 */
	@Test
	void theExactCertificateClassifiesTheTransformedConstantsAsCaptured() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<String> problems = new ArrayList<>();
		int changes = 0;
		for (Transform transform : List.of(Transform.of(BigDecimal.ONE, "0.001", "0", "0"),
				Transform.of(BigDecimal.ONE, "1", "1000.1", "-999.7"))) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				double[] constants = captured();
				List<CircleDriverRootStructure2D.Root> roots = classify(constants);
				for (String label : configuration.loci) {
					LocusExistenceStructure2D structure = structure(label);
					assertEquals(Completeness.COMPLETE, structure.getCompleteness(),
							context + " " + label);
					long ends = structure.getContinuousValidComponents().stream()
							.flatMap(component -> java.util.stream.Stream.of(
									component.getLowerBoundary(), component.getUpperBoundary()))
							.filter(end -> end.getKind()
									== LocusBoundary2D.Kind.CERTIFIED_TRANSITION)
							.count();
					assertEquals(count(roots, RealRootStructure1D.RootKind.TRANSITION), ends,
							context + " " + label + roots);
					assertEquals(count(roots, RealRootStructure1D.RootKind.SINGULAR_INTERIOR),
							structure.getSingularParameters().size(), context + " " + label);
					assertEquals(count(roots, RealRootStructure1D.RootKind.ISOLATED_POINT),
							structure.getPointComponents().size(), context + " " + label);
					references(context + " " + label, label, configuration, constants, null,
							problems);
					changes += signChangesAreCertified(context + " " + label, label,
							configuration, constants, problems);
					if (configuration == Configuration.BITE
							|| configuration == Configuration.PENETRATION) {
						assertEquals(reference.get(configuration).get(label), descriptor(label),
								context + " " + label);
					}
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
		assertTrue(changes > 0, "sign changes of the discriminant found: " + changes);
	}

	/**
	 * V-INV-2, extreme cases: translated by {@code 10^6} or scaled by {@code 10^6}, exactly
	 * in binary64, a certificate may fail closed ({@code L-CU1-TRANSLATION-RESOLUTION}); a
	 * complete one has the structure of the untransformed configuration, and whatever a
	 * certificate states, complete or not, agrees with the 60-digit reference: every
	 * certified piece at its samples, every transition bracket by a sign change, and every
	 * singular parameter and point component by its enclosure of the transformed reference.
	 */
	@Test
	void transformationsOfOrderTenToTheSixFailClosedOrKeepTheStructure() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<String> problems = new ArrayList<>();
		for (Transform transform : List.of(Transform.of(UNIT, "1", "1000000", "1000000"),
				Transform.of(UNIT, "1000000", "0", "0"))) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				assertExact(context, configuration, transform, inputs());
				double[] constants = captured();
				assertCapturedExact(context, configuration, transform, constants);
				for (String label : configuration.loci) {
					if (structure(label).getCompleteness() == Completeness.COMPLETE) {
						assertEquals(reference.get(configuration).get(label), descriptor(label),
								context + " " + label);
					}
					references(context + " " + label, label, configuration, constants,
							transform, problems);
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * @return descriptors of the untransformed configurations (every constant a multiple of
	 *         125), which agree with those in the units of matrix section 1
	 */
	private Map<Configuration, Map<String, String>> referenceDescriptors() {
		Map<Configuration, Map<String, String>> descriptors = new LinkedHashMap<>();
		List<String> problems = new ArrayList<>();
		for (Configuration configuration : Configuration.values()) {
			Map<String, String> loci = new LinkedHashMap<>();
			for (BigDecimal unit : List.of(UNIT, BigDecimal.ONE)) {
				Transform identity = Transform.of(unit, "1", "0", "0");
				build(configuration, identity);
				assertExact(configuration.name(), configuration, identity, inputs());
				double[] constants = captured();
				assertCapturedExact(configuration.name(), configuration, identity, constants);
				for (String label : configuration.loci) {
					assertEquals(Completeness.COMPLETE, structure(label).getCompleteness(),
							configuration + " " + label);
					String descriptor = descriptor(label);
					assertEquals(loci.getOrDefault(label, descriptor), descriptor,
							configuration + " " + label + " in both units");
					loci.put(label, descriptor);
					references(configuration + " " + label, label, configuration, constants,
							identity, problems);
				}
				getKernel().clearConstruction(true);
			}
			descriptors.put(configuration, loci);
		}
		assertEquals(List.of(), problems);
		return descriptors;
	}

	/**
	 * The synthetic configuration with the driver at its starting parameter and the labelled
	 * outputs traced by its loci, every constant transformed. At the tangencies the second
	 * output is undefined when it is created, so only {@code lq} traces it there.
	 */
	private void build(Configuration configuration, Transform transform) {
		BigDecimal zero = BigDecimal.ZERO;
		add("p=Circle((" + plain(transform.x(zero)) + "," + plain(transform.y(zero)) + "),"
				+ plain(transform.length(BigDecimal.valueOf(25))) + ")");
		add("P=(" + plain(transform.x(BigDecimal.valueOf(-25))) + ","
				+ plain(transform.y(BigDecimal.valueOf(50))) + ")");
		add("g1=Circle((" + plain(transform.x(configuration.centreX)) + ","
				+ plain(transform.y(configuration.centreY)) + "),"
				+ plain(transform.length(configuration.radius)) + ")");
		add("N=Point(g1)");
		moveDriver((GeoPoint) lookup("N"), configuration.driver);
		add("t=" + configuration.line + "(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		for (String label : configuration.loci) {
			add(label + "=LocusV2(" + label.substring(1).toUpperCase() + ",N)");
		}
		getKernel().updateConstruction(false);
	}

	/**
	 * @return typed inputs: centre and radius of {@code p}, the point {@code P}, centre and
	 *         radius of {@code g1}
	 */
	private double[] inputs() {
		GeoElement[] circle = lookup("p").getParentAlgorithm().getInput();
		GeoElement[] driverCircle = lookup("g1").getParentAlgorithm().getInput();
		GeoPoint point = (GeoPoint) lookup("P");
		return new double[] {((GeoPoint) circle[0]).getInhomX(),
			((GeoPoint) circle[0]).getInhomY(), ((NumberValue) circle[1]).getDouble(),
			point.getInhomX(), point.getInhomY(), ((GeoPoint) driverCircle[0]).getInhomX(),
			((GeoPoint) driverCircle[0]).getInhomY(), ((NumberValue) driverCircle[1]).getDouble()};
	}

	/**
	 * @return the constants that the class-v3 program of {@code lq} captured, which the
	 *         certificate decides ({@code locus-v2-certified-construction-model.md} section
	 *         10.6): the driver circle by its half axes, eigenvectors and midpoint (0 to 7),
	 *         the point P of the ray or line (8, 9) and the circle p by its matrix entries A2,
	 *         A4 and A5 (10 to 12); the conic derives them from the typed inputs with rounding
	 */
	private double[] captured() {
		CertifiedConstructionProgram2D program = ((ReconstructibleLocusEvaluator2D) locus("lq")
				.getSemanticDefinition().getEvaluatorCapability()).captureCertifiedProgramV3()
				.orElseThrow();
		double[] constants = new double[13];
		for (int index = 0; index < 8; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		int found = 0;
		for (CertifiedConstructionProgram2D.Step step : program.getSteps()) {
			List<CertifiedConstructionProgram2D.Operand> operands = step.getOperands();
			if ((step.getOperation() == CertifiedConstructionProgram2D.Operation.RAY
					|| step.getOperation() == CertifiedConstructionProgram2D.Operation.JOIN)
					&& operands.get(0).getKind() == CertifiedConstructionProgram2D.OperandKind
							.POINT) {
				constants[8] = operands.get(0).getValue(3);
				constants[9] = operands.get(0).getValue(4);
				found++;
			} else if (step.getOperation()
					== CertifiedConstructionProgram2D.Operation.LINE_CIRCLE
					&& operands.get(1).getKind() == CertifiedConstructionProgram2D.OperandKind
							.CIRCLE) {
				for (int index = 0; index < 3; index++) {
					constants[10 + index] = operands.get(1).getValue(index);
				}
				found++;
			}
		}
		assertEquals(2, found, program.getSignature());
		return constants;
	}

	/**
	 * The captured constants are the exact transformed constants, bit for bit: the half axes
	 * are the radius of {@code g1}, the eigenvectors the axes, the midpoint its centre, then
	 * the point P and the matrix entries {@code A2 = cx^2 + cy^2 - r^2}, {@code A4 = -cx} and
	 * {@code A5 = -cy} of {@code p}.
	 */
	private static void assertCapturedExact(String context, Configuration configuration,
			Transform transform, double[] constants) {
		BigDecimal zero = BigDecimal.ZERO;
		BigDecimal radius = transform.length(configuration.radius);
		BigDecimal a4 = transform.x(zero).negate();
		BigDecimal a5 = transform.y(zero).negate();
		BigDecimal circle = transform.length(BigDecimal.valueOf(25));
		BigDecimal[] exact = {radius, radius, BigDecimal.ONE, zero, zero, BigDecimal.ONE,
			transform.x(configuration.centreX), transform.y(configuration.centreY),
			transform.x(BigDecimal.valueOf(-25)), transform.y(BigDecimal.valueOf(50)),
			a4.multiply(a4).add(a5.multiply(a5)).subtract(circle.multiply(circle)), a4, a5};
		for (int index = 0; index < exact.length; index++) {
			assertEquals(0, new BigDecimal(constants[index]).compareTo(exact[index]),
					context + " captured constant " + index + " " + constants[index]);
		}
	}

	/** The typed inputs are the exact transformed constants, bit for bit. */
	private static void assertExact(String context, Configuration configuration,
			Transform transform, double[] constants) {
		BigDecimal[] exact = {transform.x(BigDecimal.ZERO), transform.y(BigDecimal.ZERO),
			transform.length(BigDecimal.valueOf(25)), transform.x(BigDecimal.valueOf(-25)),
			transform.y(BigDecimal.valueOf(50)), transform.x(configuration.centreX),
			transform.y(configuration.centreY), transform.length(configuration.radius)};
		for (int index = 0; index < exact.length; index++) {
			assertEquals(0, new BigDecimal(constants[index]).compareTo(exact[index]),
					context + " constant " + index + " " + constants[index]);
		}
	}

	/**
	 * @return the structure of a certificate: completeness, coverage states and causes,
	 *         components with their ends and boundary sources, singular parameters, point
	 *         components and folds, without parameter values; durable operation identities
	 *         are replaced by the labels of their geos
	 */
	private String descriptor(String label) {
		Map<String, String> names = new LinkedHashMap<>();
		SpatialIdentityRegistry registry = getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (GeoElement geo : getKernel().getConstruction().getGeoSetConstructionOrder()) {
			PersistentGeoId id = registry.getPersistentGeoId(geo);
			if (id != null && geo.isLabelSet()) {
				names.put(id.toExternalForm(), geo.getLabelSimple());
			}
		}
		LocusExistenceStructure2D structure = structure(label);
		StringBuilder text = new StringBuilder(structure.getCompleteness().name())
				.append("|closed=").append(structure.isClosedPeriodic());
		String previous = null;
		for (Coverage coverage : structure.getCoverage()) {
			// Adjacent pieces of one state and cause differ only in the proof that decided
			// them, which is not structure.
			String piece = "|coverage=" + coverage.getState() + "/" + coverage.getCause();
			if (!piece.equals(previous)) {
				text.append(piece);
			}
			previous = piece;
		}
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			text.append("|component=arc:").append(component.isArc()).append(",seam:")
					.append(component.crossesSeam()).append(',')
					.append(component.getMaximality()).append(',')
					.append(component.getNature());
			for (LocusBoundary2D end : List.of(component.getLowerBoundary(),
					component.getUpperBoundary())) {
				text.append(",end=").append(end.getKind()).append(':')
						.append(end.isIncluded()).append(':').append(end.getNature())
						.append(':').append(source(end.getSource(), names));
			}
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			text.append("|singular=").append(singular.getMultiplicity()).append(':')
					.append(singular.getNature()).append(':')
					.append(source(singular.getSource(), names));
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			text.append("|point=").append(point.getMaximality()).append(':')
					.append(point.getPositionState()).append(':')
					.append(source(point.getSource(), names));
		}
		return text.append("|folds=").append(structure.getCertifiedFolds().size()).toString();
	}

	private static String source(LocusBoundarySource2D source, Map<String, String> names) {
		if (source == null) {
			return "none";
		}
		String origin = source.getOrigin();
		for (Map.Entry<String, String> name : names.entrySet()) {
			origin = origin.replace(name.getKey(), name.getValue());
		}
		return origin + "/" + source.getPredicate() + "/" + source.getSide();
	}

	/**
	 * Records every statement of a certificate that the 60-digit reference of the captured
	 * constants contradicts: a certified piece at one of its samples, a transition bracket
	 * without a sign change of the discriminant and, for an exact transformation, a singular
	 * parameter or point component that does not enclose the transformed reference: the
	 * double root at {@code atan2(4, 3)}, or the point (15, 20) at {@code atan2(-4, -3)} or,
	 * for the line of the point pair, the point (-25, 0) at the seam.
	 *
	 * @param transform the exact transformation, or {@code null} for rounded constants
	 */
	private void references(String context, String label, Configuration configuration,
			double[] constants, Transform transform, List<String> problems) {
		LocusExistenceStructure2D structure = structure(label);
		for (Coverage coverage : structure.getCoverage()) {
			CoverageState state = coverage.getState();
			if (state != CoverageState.CERTIFIED_EXISTS
					&& state != CoverageState.CERTIFIED_DOES_NOT_EXIST) {
				continue;
			}
			LocusInterval2D interval = coverage.getInterval();
			for (int sample = 1; sample <= 5; sample++) {
				double t = interval.getLower()
						+ (interval.getUpper() - interval.getLower()) * sample / 6;
				BigDecimal[] value = discriminantAt(constants, t);
				// A line has both roots; a ray only those ahead of P.
				boolean exists = value[0].signum() >= 0
						&& (!"Ray".equals(configuration.line) || value[1].signum() > 0);
				if (exists != (state == CoverageState.CERTIFIED_EXISTS)) {
					problems.add(context + ": " + state + " at t=" + t + " where D=" + value[0]
							+ " and the ray test is " + value[1]);
				}
			}
		}
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			for (LocusBoundary2D end : List.of(component.getLowerBoundary(),
					component.getUpperBoundary())) {
				if (end.getKind() == LocusBoundary2D.Kind.CERTIFIED_TRANSITION
						&& discriminantAt(constants, end.getEnclosureLower())[0].signum()
								* discriminantAt(constants, end.getEnclosureUpper())[0]
										.signum() >= 0) {
					problems.add(context + ": no sign change of the discriminant across "
							+ end);
				}
			}
		}
		if (transform == null) {
			return;
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			if (!enclosesCosine(singular.getParameterLower(), singular.getParameterUpper(),
					true)) {
				problems.add(context + ": the singular parameter does not enclose atan2(4, 3) "
						+ singular);
			}
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			// The seam point of the line is the exact -pi, inside [-nextUp(pi), -pi].
			boolean seam = "Line".equals(configuration.line)
					&& new BigDecimal(point.getParameterLower()).compareTo(PI.negate()) <= 0
					&& PI.negate().compareTo(new BigDecimal(point.getParameterUpper())) <= 0;
			if (!seam && !enclosesCosine(point.getParameterLower(), point.getParameterUpper(),
					false)) {
				problems.add(context + ": the point component does not enclose atan2(-4, -3) "
						+ point);
			}
			double[] position = point.getPositionEnclosure();
			BigDecimal x = transform.x(BigDecimal.valueOf(seam ? -25 : 15));
			BigDecimal y = transform.y(BigDecimal.valueOf(seam ? 0 : 20));
			if (position == null || new BigDecimal(position[0]).compareTo(x) > 0
					|| x.compareTo(new BigDecimal(position[1])) > 0
					|| new BigDecimal(position[2]).compareTo(y) > 0
					|| y.compareTo(new BigDecimal(position[3])) > 0) {
				problems.add(context + ": the position does not enclose (" + x + ", " + y + ") "
						+ point);
			}
		}
	}

	/**
	 * Records what the 60-digit discriminant of the captured constants contradicts,
	 * independently of any root classification: sampled at 4096 parameters, it changes sign
	 * with the roots ahead of P (on a ray) only where the certificate changes state or
	 * certifies a boundary; and at both ends of the enclosure of every singular parameter (a
	 * double root inside an arc) it is not negative with a slope from negative to positive, and
	 * of every point component (a double root inside nonexistence) not positive with a slope
	 * from positive to negative.
	 *
	 * @return number of sign changes found at the samples
	 */
	private int signChangesAreCertified(String context, String label,
			Configuration configuration, double[] constants, List<String> problems) {
		LocusExistenceStructure2D structure = structure(label);
		boolean ray = "Ray".equals(configuration.line);
		int samples = 4096;
		int changes = 0;
		double previous = -Math.PI;
		BigDecimal[] before = discriminantAt(constants, previous);
		for (int index = 1; index < samples; index++) {
			double t = -Math.PI + 2 * Math.PI * index / samples;
			BigDecimal[] value = discriminantAt(constants, t);
			if (before[0].signum() * value[0].signum() < 0
					&& (!ray || before[1].signum() > 0 && value[1].signum() > 0)) {
				changes++;
				if (!changesIn(structure, previous, t)) {
					problems.add(context + ": the discriminant changes sign in (" + previous
							+ ", " + t + ") where the certificate states no change");
				}
			}
			previous = t;
			before = value;
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			if (!doubleRoot(constants, singular.getParameterLower(),
					singular.getParameterUpper(), 1)) {
				problems.add(context + ": no double root of the discriminant with an arc on both"
						+ " sides in " + singular);
			}
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			if (!doubleRoot(constants, point.getParameterLower(), point.getParameterUpper(),
					-1)) {
				problems.add(context + ": no double root of the discriminant with nonexistence"
						+ " on both sides in " + point);
			}
		}
		return changes;
	}

	/** @return whether the certificate changes state, or certifies a boundary, in the range */
	private static boolean changesIn(LocusExistenceStructure2D structure, double lower,
			double upper) {
		for (Coverage coverage : structure.getCoverage()) {
			LocusInterval2D interval = coverage.getInterval();
			boolean meets = interval.getLower() <= upper && lower <= interval.getUpper();
			if (meets && coverage.getState() != CoverageState.CERTIFIED_EXISTS
					&& coverage.getState() != CoverageState.CERTIFIED_DOES_NOT_EXIST) {
				return true;
			}
		}
		return coverageAt(structure, lower).getState()
				!= coverageAt(structure, upper).getState();
	}

	/**
	 * @return whether the 60-digit discriminant has, at both binary64 ends, the sign of
	 *         {@code side} or zero, and a slope of sign {@code -side} at the lower end and
	 *         {@code side} at the upper one: a double root with that sign on both sides
	 */
	private static boolean doubleRoot(double[] constants, double lower, double upper,
			int side) {
		BigDecimal[] low = slopeAt(constants, lower);
		BigDecimal[] high = slopeAt(constants, upper);
		return low[0].signum() * side >= 0 && high[0].signum() * side >= 0
				&& low[1].signum() * side <= 0 && high[1].signum() * side >= 0;
	}

	/**
	 * @return the 60-digit discriminant of the line from P through the driver at {@code t}
	 *         against {@code p}, and its derivative in {@code t}, from the captured constants
	 */
	private static BigDecimal[] slopeAt(double[] constants, double t) {
		BigDecimal parameter = new BigDecimal(t);
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		BigDecimal cosine = series(parameter, true, REFERENCE);
		BigDecimal sine = series(parameter, false, REFERENCE);
		BigDecimal along = k[0].multiply(cosine, REFERENCE);
		BigDecimal across = k[1].multiply(sine, REFERENCE);
		BigDecimal alongSlope = k[0].multiply(sine, REFERENCE).negate();
		BigDecimal acrossSlope = k[1].multiply(cosine, REFERENCE);
		BigDecimal dx = along.multiply(k[2], REFERENCE).add(across.multiply(k[4], REFERENCE),
				REFERENCE).add(k[6], REFERENCE).subtract(k[8], REFERENCE);
		BigDecimal dy = along.multiply(k[3], REFERENCE).add(across.multiply(k[5], REFERENCE),
				REFERENCE).add(k[7], REFERENCE).subtract(k[9], REFERENCE);
		BigDecimal dxSlope = alongSlope.multiply(k[2], REFERENCE)
				.add(acrossSlope.multiply(k[4], REFERENCE), REFERENCE);
		BigDecimal dySlope = alongSlope.multiply(k[3], REFERENCE)
				.add(acrossSlope.multiply(k[5], REFERENCE), REFERENCE);
		BigDecimal wx = k[11].negate().subtract(k[8]);
		BigDecimal wy = k[12].negate().subtract(k[9]);
		BigDecimal squared = k[11].multiply(k[11]).add(k[12].multiply(k[12])).subtract(k[10]);
		BigDecimal cross = dx.multiply(wy, REFERENCE).subtract(dy.multiply(wx, REFERENCE),
				REFERENCE);
		BigDecimal crossSlope = dxSlope.multiply(wy, REFERENCE)
				.subtract(dySlope.multiply(wx, REFERENCE), REFERENCE);
		BigDecimal discriminant = squared.multiply(dx.multiply(dx, REFERENCE)
				.add(dy.multiply(dy, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(cross, REFERENCE), REFERENCE);
		BigDecimal slope = squared.multiply(dx.multiply(dxSlope, REFERENCE)
				.add(dy.multiply(dySlope, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(crossSlope, REFERENCE), REFERENCE)
				.multiply(BigDecimal.valueOf(2), REFERENCE);
		return new BigDecimal[] {discriminant, slope};
	}

	/**
	 * @return whether {@code [lower, upper]} encloses {@code atan2(4, 3)}, where the cosine
	 *         decreases through 3/5, or {@code atan2(-4, -3)}, where it increases through
	 *         -3/5, proved with the 60-digit cosine of the binary64 ends
	 */
	private static boolean enclosesCosine(double lower, double upper, boolean critical) {
		BigDecimal low = series(new BigDecimal(lower), true, REFERENCE);
		BigDecimal high = series(new BigDecimal(upper), true, REFERENCE);
		if (critical) {
			BigDecimal value = new BigDecimal("0.6");
			return 0 < lower && upper < Math.PI / 2 && low.compareTo(value) >= 0
					&& value.compareTo(high) >= 0;
		}
		BigDecimal value = new BigDecimal("-0.6");
		return -Math.PI < lower && upper < -Math.PI / 2 && low.compareTo(value) <= 0
				&& value.compareTo(high) <= 0;
	}

	/**
	 * @return the 60-digit discriminant of the line from P through the driver at {@code t}
	 *         against {@code p}, and the direction test of the ray (positive when the ray
	 *         points to the circle), from the captured constants
	 */
	private static BigDecimal[] discriminantAt(double[] constants, double t) {
		BigDecimal parameter = new BigDecimal(t);
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		// The driver as GeoConicND.pathChangedWithoutCheckEllipse places it.
		BigDecimal along = k[0].multiply(series(parameter, true, REFERENCE), REFERENCE);
		BigDecimal across = k[1].multiply(series(parameter, false, REFERENCE), REFERENCE);
		BigDecimal nx = along.multiply(k[2], REFERENCE).add(across.multiply(k[4], REFERENCE),
				REFERENCE).add(k[6], REFERENCE);
		BigDecimal ny = along.multiply(k[3], REFERENCE).add(across.multiply(k[5], REFERENCE),
				REFERENCE).add(k[7], REFERENCE);
		BigDecimal dx = nx.subtract(k[8], REFERENCE);
		BigDecimal dy = ny.subtract(k[9], REFERENCE);
		// The circle x^2 + y^2 + 2 A4 x + 2 A5 y + A2 = 0: centre (-A4, -A5).
		BigDecimal wx = k[11].negate().subtract(k[8]);
		BigDecimal wy = k[12].negate().subtract(k[9]);
		BigDecimal squared = k[11].multiply(k[11]).add(k[12].multiply(k[12])).subtract(k[10]);
		BigDecimal cross = dx.multiply(wy, REFERENCE).subtract(dy.multiply(wx, REFERENCE),
				REFERENCE);
		BigDecimal discriminant = squared.multiply(dx.multiply(dx, REFERENCE)
				.add(dy.multiply(dy, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(cross, REFERENCE), REFERENCE);
		return new BigDecimal[] {discriminant,
			dx.multiply(wx, REFERENCE).add(dy.multiply(wy, REFERENCE), REFERENCE)};
	}

	/**
	 * @return roots of the discriminant of the captured constants, built by the test with
	 *         exact rationals and classified with their multiplicities
	 */
	private static List<CircleDriverRootStructure2D.Root> classify(double[] constants) {
		ExactRational2D[] k = new ExactRational2D[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = ExactRational2D.ofBinary64(constants[index]);
		}
		TrigonometricPolynomial2D nx = TrigonometricPolynomial2D.constant(k[6])
				.add(TrigonometricPolynomial2D.cosine().scale(k[0].multiply(k[2])))
				.add(TrigonometricPolynomial2D.sine().scale(k[1].multiply(k[4])));
		TrigonometricPolynomial2D ny = TrigonometricPolynomial2D.constant(k[7])
				.add(TrigonometricPolynomial2D.cosine().scale(k[0].multiply(k[3])))
				.add(TrigonometricPolynomial2D.sine().scale(k[1].multiply(k[5])));
		TrigonometricPolynomial2D px = TrigonometricPolynomial2D.constant(k[8]);
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.constant(k[9]);
		TrigonometricPolynomial2D a = py.subtract(ny);
		TrigonometricPolynomial2D b = nx.subtract(px);
		TrigonometricPolynomial2D c = px.multiply(ny).subtract(py.multiply(nx));
		TrigonometricPolynomial2D distance = a.scale(k[11].negate()).add(b.scale(
				k[12].negate())).add(c);
		ExactRational2D squared = k[11].multiply(k[11]).add(k[12].multiply(k[12]))
				.subtract(k[10]);
		TrigonometricPolynomial2D discriminant = a.multiply(a).add(b.multiply(b))
				.scale(squared).subtract(distance.multiply(distance));
		return CircleDriverRootStructure2D.classify(discriminant,
				ExactRational2D.ofBinary64(DELTA / 4), ExactBudget1D.DEFAULT).roots();
	}

	private static long count(List<CircleDriverRootStructure2D.Root> roots,
			RealRootStructure1D.RootKind kind) {
		return roots.stream().filter(root -> root.kind() == kind).count();
	}

	private static String plain(BigDecimal value) {
		return value.stripTrailingZeros().toPlainString();
	}

	/**
	 * The synthetic configurations of matrix section 1 that V-INV-2 transforms: rays through
	 * the driver, and the line of the two point components.
	 */
	private enum Configuration {
		CRITICAL("35", "-45", "40", 2.5, "Ray", "lq", "lr"),
		BITE("35", "-45", "40.0009765625", 2.5, "Ray", "lq", "lr"),
		PENETRATION("35", "-45", "39.9990234375", 2.5, "Ray", "lq", "lr"),
		TANGENCY("83", "19", "40", EXTERNAL, "Ray", "lq"),
		POINT_PAIR("15", "70", "40", EXTERNAL, "Line", "lq");

		private final BigDecimal centreX;
		private final BigDecimal centreY;
		private final BigDecimal radius;
		private final double driver;
		private final String line;
		private final List<String> loci;

		Configuration(String centreX, String centreY, String radius, double driver,
				String line, String... loci) {
			this.centreX = new BigDecimal(centreX);
			this.centreY = new BigDecimal(centreY);
			this.radius = new BigDecimal(radius);
			this.driver = driver;
			this.line = line;
			this.loci = List.of(loci);
		}
	}

	/** A uniform scale and a translation of the whole construction, with exact data. */
	private static final class Transform {
		private final BigDecimal unit;
		private final BigDecimal scale;
		private final BigDecimal translationX;
		private final BigDecimal translationY;

		private Transform(BigDecimal unit, BigDecimal scale, BigDecimal translationX,
				BigDecimal translationY) {
			this.unit = unit;
			this.scale = scale;
			this.translationX = translationX;
			this.translationY = translationY;
		}

		static Transform of(BigDecimal unit, String scale, String translationX,
				String translationY) {
			return new Transform(unit, new BigDecimal(scale), new BigDecimal(translationX),
					new BigDecimal(translationY));
		}

		BigDecimal x(BigDecimal value) {
			return length(value).add(translationX);
		}

		BigDecimal y(BigDecimal value) {
			return length(value).add(translationY);
		}

		BigDecimal length(BigDecimal value) {
			return value.multiply(unit).multiply(scale);
		}

		@Override
		public String toString() {
			return "unit " + unit + ", scale " + scale + ", translation (" + translationX + ", "
					+ translationY + ")";
		}
	}

	/**
	 * GC-2 and GC-3: the certificate is computed once per revision, when the revision is
	 * published (step T11), and shared; reading it, moving the driver or recomputing does
	 * not recompute or change it; a new revision recomputes it.
	 */
	@Test
	void theShadowCertificateIsComputedOncePerRevision() {
		add("rr=40+2^(-10)");
		critical("rr");
		long certificates = work("lq", LocusCertificationWork2D.CERTIFICATE_COMPUTATION);
		long boxes = work("lq", LocusCertificationWork2D.EXISTENCE_BOX);
		Result result = certificate("lq");
		assertTrue(result.getExistenceBoxes() > 0);
		assertTrue(boxes >= result.getExistenceBoxes(), "computed when published");
		assertEquals(boxes, work("lq", LocusCertificationWork2D.EXISTENCE_BOX));
		assertEquals(certificates,
				work("lq", LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		assertSame(result, certificate("lq"));
		getKernel().updateConstruction(false);
		assertSame(result, certificate("lq"), "an equivalent recompute keeps it");
		moveDriver((GeoPoint) lookup("N"), 2.5);
		moveDriver((GeoPoint) lookup("N"), CRITICAL);
		assertSame(result, certificate("lq"));
		assertEquals(boxes, work("lq", LocusCertificationWork2D.EXISTENCE_BOX));
		GeoNumeric radius = (GeoNumeric) lookup("rr");
		radius.setValue(39);
		radius.updateCascade();
		Result changed = certificate("lq");
		assertNotEquals(result.getStructure().getSemanticSignature(),
				changed.getStructure().getSemanticSignature());
		assertTrue(changed.getStructure().isClosedPeriodic());
	}

	/**
	 * Structure of step T4: an enclosed root keeps the binary64 end of its component closed at
	 * the safe inner bound while stating the inclusion of the exact root; the bracket is
	 * certified boundary coverage, compatible with completeness; a singular parameter lies in
	 * certified existence.
	 */
	@Test
	void enclosedRootsKeepTheStructureConsistent() {
		final LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		LocusBoundarySource2D source = new LocusBoundarySource2D("step3", "DISCRIMINANT",
				"lower");
		LocusBoundary2D closed = LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25 + 1e-12);
		LocusBoundary2D open = LocusBoundary2D.enclosed(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("step5", "DIVISOR", "upper"), false,
				LocusEvidenceNature2D.RIGOROUS_INTERVAL, 0.75 - 1e-12, 0.75);
		assertTrue(closed.isIncluded() && closed.isBinaryEndIncluded());
		assertFalse(open.isIncluded());
		assertTrue(open.isBinaryEndIncluded());
		assertTrue(closed.getSignature().contains(":enclosure["));
		assertNotEquals(closed, LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25 + 2e-12));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.DOMAIN_END, source, true, LocusEvidenceNature2D.EXACT,
				0.25, 0.3));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXPLORATORY, 0.25, 0.3));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25));
		double start = 0.25 + 1e-12;
		double end = 0.75 - 1e-12;
		assertThrows(IllegalArgumentException.class, () -> ContinuousComponent.interval(
				new LocusInterval2D(start, end, false, true), closed, open,
				Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "c0"));
		ContinuousComponent component = ContinuousComponent.interval(
				new LocusInterval2D(start, end, true, true), closed, open,
				Maximality.ESTABLISHED, LocusEvidenceNature2D.RIGOROUS_INTERVAL, "c0");
		List<Coverage> coverage = List.of(
				coverageItem(0, 0.25, true, true, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE),
				coverageItem(0.25, start, false, false, CoverageState.CERTIFIED_BOUNDARY,
						LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE),
				coverageItem(start, end, true, true, CoverageState.CERTIFIED_EXISTS,
						LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE),
				coverageItem(end, 0.75, false, false, CoverageState.CERTIFIED_BOUNDARY,
						LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE),
				coverageItem(0.75, 1, true, true, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE));
		LocusSingularParameter2D inside = new LocusSingularParameter2D(0.5, 0.5 + 1e-12,
				new LocusBoundarySource2D("step3", "DISCRIMINANT", "root-1"), 2,
				LocusEvidenceNature2D.EXACT);
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(BRANCH, domain,
				LocusExistenceStructure2D.DomainTopology.BOUNDED, 0, coverage,
				List.of(component), List.of(), List.of(inside), Completeness.COMPLETE,
				HybridExistenceCertifier2D.CONTRACT);
		assertEquals(List.of(inside), structure.getSingularParameters());
		assertTrue(structure.getSemanticSignature().contains("|singular="));
		LocusSingularParameter2D outside = new LocusSingularParameter2D(0.9, 0.9 + 1e-12,
				inside.getSource(), 2, LocusEvidenceNature2D.EXACT);
		assertThrows(IllegalArgumentException.class, () -> new LocusExistenceStructure2D(
				BRANCH, domain, LocusExistenceStructure2D.DomainTopology.BOUNDED, 0, coverage,
				List.of(component), List.of(), List.of(outside), Completeness.COMPLETE,
				HybridExistenceCertifier2D.CONTRACT));
		assertThrows(IllegalArgumentException.class, () -> new LocusSingularParameter2D(0.5,
				0.6, inside.getSource(), 3, LocusEvidenceNature2D.EXACT));
		assertFalse(LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE.admits(
				CoverageState.CERTIFIED_EXISTS));
	}

	/**
	 * The exact first-level polynomials have the sign of the box predicates they stand for:
	 * the discriminant of a line through the driver, and the divisor of a meet carried
	 * through every transform of class v1.
	 */
	@Test
	void firstLevelPolynomialsHaveTheSignOfTheBoxPredicates() {
		critical("40+2^(-10)");
		add("c=Circle((0,0),2)");
		add("M=Point(c)");
		add("S=Intersect(Line(M,(5,0)),Line((0,-10),(1,-10)))");
		add("S1=Rotate(S,20deg,(1,2))");
		add("S2=Mirror(S1,y=0.2x+2)");
		add("S3=Dilate(S2,1.1,(1,2))");
		add("S4=Mirror(S3,(1,1))");
		add("ls=LocusV2(S4,M)");
		int compared = 0;
		for (String label : List.of("lq", "ls")) {
			ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
					locus(label).getSemanticDefinition());
			CertifiedConstructionProgram2D program = model.getProgram();
			for (int step = 1; step <= program.getSteps().size(); step++) {
				for (ConstructionValidityPredicate2D predicate : List.of(
						ConstructionValidityPredicate2D.DISCRIMINANT,
						ConstructionValidityPredicate2D.DIVISOR)) {
					ConstructionBoxModel2D.Target target = new ConstructionBoxModel2D.Target(
							step, predicate, ConstructionBoxModel2D.STEP_PREDICATE);
					TrigonometricPolynomial2D form = FirstLevelPredicate2D.build(program,
							target).orElse(null);
					if (form == null) {
						continue;
					}
					for (int sample = -50; sample < 50; sample++) {
						double t = (sample + 0.37) * Math.PI / 50;
						ConstructionBoxModel2D.PredicateEnclosure box = model.enclose(
								SplineOutwardInterval2D.point(t), target, true);
						if (box == null || box.value.containsZero()) {
							continue;
						}
						int exact = form.evaluate(ExactRational2D.ofBinary64(Math.cos(t)),
								ExactRational2D.ofBinary64(Math.sin(t))).signum();
						String context = label + " step" + step + " " + predicate + " t=" + t;
						if (predicate.isEquality()) {
							// A normalized homogeneous divisor may change sign: only zeros count.
							assertNotEquals(0, exact, context);
						} else {
							assertEquals(box.value.lower > 0 ? 1 : -1, exact, context);
						}
						compared++;
					}
				}
			}
		}
		assertTrue(compared > 300, "compared " + compared);
	}

	/**
	 * The centred form of a certifying evaluation is sound: on boxes of every width, the
	 * discriminant enclosures it decides with contain a 60-digit reference, and a box it
	 * certifies outside the band of the unlabelled output is outside it at every sample.
	 */
	@Test
	void theCentredFormEnclosesTheReference() {
		critical("40+2^(-10)");
		for (String label : List.of("lq", "lr2")) {
			ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
					locus(label).getSemanticDefinition());
			Random random = new Random(20261021);
			for (int trial = 0; trial < 400; trial++) {
				double width = Math.scalb(1.0, -2 - random.nextInt(40));
				double lower = trial % 2 == 0 ? CRITICAL - 0.08 + 0.16 * random.nextDouble()
						: -Math.PI + 2 * Math.PI * random.nextDouble();
				SplineOutwardInterval2D box = new SplineOutwardInterval2D(lower,
						lower + width);
				ConstructionBoxOutcome2D outcome = model.evaluateCertifying(box, true);
				for (ConstructionBoxOutcome2D.PredicateEvaluation evaluation
						: outcome.getEvaluations()) {
					if (evaluation.getPredicate() != ConstructionValidityPredicate2D.DISCRIMINANT
							|| evaluation.getRoot() != ConstructionBoxModel2D.STEP_PREDICATE) {
						continue;
					}
					for (double fraction : new double[] {0, 0.25, 0.5, 0.75, 1}) {
						BigDecimal[] reference = referenceLine(lower + width * fraction);
						BigDecimal value = reference[3];
						assertTrue(new BigDecimal(evaluation.getValueLower()).compareTo(value)
								<= 0 && value.compareTo(new BigDecimal(
										evaluation.getValueUpper())) <= 0,
								label + " box=" + lower + "+" + width + " D=" + value);
						if ("lr2".equals(label)
								&& outcome.kind == ConstructionBoxOutcome2D.Kind.VALID) {
							BigDecimal scale = reference[0].pow(2).max(reference[1].pow(2))
									.max(reference[2].pow(2));
							assertTrue(value.compareTo(scale.multiply(new BigDecimal(
									ConstructionBoxModel2D.COINCIDENT_WINDOW_FACTOR))) > 0);
						}
					}
				}
			}
		}
	}

	/**
	 * @return 60-digit line {@code (a, b, c)} through P = (-25, 50) and the driver on the
	 *         circle of centre (35, -45) and radius {@code 40 + 2^-10}, and its discriminant
	 *         against the circle of centre (0, 0) and radius 25
	 */
	private static BigDecimal[] referenceLine(double parameter) {
		MathContext context = new MathContext(60, RoundingMode.HALF_EVEN);
		BigDecimal t = new BigDecimal(parameter);
		BigDecimal radius = new BigDecimal(40 + Math.scalb(1.0, -10));
		BigDecimal nx = new BigDecimal(35).add(radius.multiply(series(t, true, context),
				context));
		BigDecimal ny = new BigDecimal(-45).add(radius.multiply(series(t, false, context),
				context));
		BigDecimal a = new BigDecimal(50).subtract(ny);
		BigDecimal b = nx.add(new BigDecimal(25));
		BigDecimal c = new BigDecimal(-25).multiply(ny, context)
				.subtract(new BigDecimal(50).multiply(nx, context));
		BigDecimal discriminant = a.multiply(a, context).add(b.multiply(b, context))
				.multiply(new BigDecimal(625), context).subtract(c.multiply(c, context));
		return new BigDecimal[] {a, b, c, discriminant};
	}

	private static BigDecimal series(BigDecimal x, boolean cosine, MathContext context) {
		BigDecimal square = x.multiply(x, context);
		BigDecimal term = cosine ? BigDecimal.ONE : x;
		BigDecimal sum = term;
		BigDecimal limit = new BigDecimal("1e-70");
		for (int k = cosine ? 2 : 3; term.abs().compareTo(limit) > 0; k += 2) {
			term = term.multiply(square, context).negate()
					.divide(BigDecimal.valueOf((long) k * (k - 1)), context);
			sum = sum.add(term, context);
		}
		return sum;
	}

	private static Coverage coverageItem(double lower, double upper, boolean lowerClosed,
			boolean upperClosed, CoverageState state, LocusCoverageCause2D cause) {
		return new Coverage(new LocusInterval2D(lower, upper, lowerClosed, upperClosed), state,
				cause, LocusEvidenceNature2D.EXACT, "test");
	}

	private List<String> signatures() {
		List<String> signatures = new ArrayList<>();
		for (String radius : List.of("40", "40+2^(-30)")) {
			critical(radius);
			for (String label : List.of("lq", "lr2")) {
				Result result = certificate(label);
				signatures.add(result.getStructure().getSemanticSignature());
				signatures.add(result.getExactEvidence().stream()
						.map(ExactEvidence::getSignature).collect(Collectors.joining(";")));
				signatures.add(result.getRoots().stream().map(CertifiedRoot::getSignature)
						.collect(Collectors.joining(";")));
				signatures.add(result.getExistenceBoxes() + "");
			}
			getKernel().clearConstruction(true);
		}
		tangency("(83,19)", "Ray");
		signatures.add(certificate("lq").getStructure().getSemanticSignature());
		getKernel().clearConstruction(true);
		return signatures;
	}

	private void assertClosedTransitions(Result result, String predicate) {
		LocusExistenceStructure2D structure = result.getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness(), predicate);
		assertEquals(2, result.getCertifiedTransitions(), predicate);
		ContinuousComponent component = structure.getContinuousValidComponents().get(0);
		for (LocusBoundary2D boundary : List.of(component.getLowerBoundary(),
				component.getUpperBoundary())) {
			assertEquals(LocusBoundary2D.Kind.CERTIFIED_TRANSITION, boundary.getKind(),
					predicate);
			assertEquals(predicate, boundary.getSource().getPredicate());
			assertTrue(boundary.isIncluded(), predicate + " is a closed end");
			assertTrue(boundary.hasEnclosure());
			assertTrue(boundary.getEnclosureUpper() - boundary.getEnclosureLower() <= DELTA);
		}
	}

	private static void assertExactWithinBudget(Result result) {
		assertFalse(result.getExactEvidence().isEmpty());
		for (ExactEvidence evidence : result.getExactEvidence()) {
			// GC-8: the exact certificate stays within its bit and bisection budgets.
			assertTrue(evidence.getMaximumCoefficientBits()
					<= ExactBudget1D.DEFAULT.maximumCoefficientBits(), evidence.toString());
			assertTrue(evidence.getMaximumRootBisections()
					<= ExactBudget1D.DEFAULT.maximumBisectionsPerRoot(), evidence.toString());
		}
		// GC-4: the boxes stay within the versioned budget.
		assertTrue(result.getExistenceBoxes() <= Policy.DEFAULT.getBoxBudget());
	}

	private static void assertEnclosed(double expected, double lower, double upper) {
		assertTrue(lower <= expected && expected <= upper,
				expected + " not in [" + lower + ", " + upper + "]");
	}

	private static Coverage coverageAt(LocusExistenceStructure2D structure,
			double parameter) {
		for (Coverage coverage : structure.getCoverage()) {
			if (coverage.getInterval().contains(parameter, 0)) {
				return coverage;
			}
		}
		throw new AssertionError("No coverage at " + parameter);
	}

	/**
	 * The discriminant of the ray from P = (-25, 50) through the driver on the circle of
	 * centre (35, -45) and the given radius, against the circle of centre (0, 0) and radius
	 * 25, with exact rational data.
	 */
	private static TrigonometricPolynomial2D criticalDiscriminant(ExactRational2D radius) {
		TrigonometricPolynomial2D nx = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(35)).add(TrigonometricPolynomial2D.cosine().scale(radius));
		TrigonometricPolynomial2D ny = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(-45)).add(TrigonometricPolynomial2D.sine().scale(radius));
		TrigonometricPolynomial2D px = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(-25));
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(50));
		TrigonometricPolynomial2D a = py.subtract(ny);
		TrigonometricPolynomial2D b = nx.subtract(px);
		TrigonometricPolynomial2D c = px.multiply(ny).subtract(py.multiply(nx));
		return a.multiply(a).add(b.multiply(b)).scale(ExactRational2D.of(625))
				.subtract(c.multiply(c));
	}

	/** Exact synthetic critical configuration with labelled and unlabelled outputs. */
	private void critical(String radius) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45)," + radius + ")");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		add("lr=LocusV2(R,N)");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		// Created by the command, the intersections settle their selection on recompute.
		getKernel().updateConstruction(false);
	}

	/**
	 * Synthetic tangency of the line from P = (-25, 50) through the driver with the circle of
	 * centre (0, 0) and radius 25; the driver starts at the tangency, where the traced point
	 * is defined, so the locus is published (H01 is step T11).
	 */
	private void tangency(String center, String line) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle(" + center + ",40)");
		add("N=Point(g1)");
		moveDriver((GeoPoint) lookup("N"), EXTERNAL);
		add("t=" + line + "(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
	}

	private static void moveDriver(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private Result certificate(String label) {
		Result result = ((AlgoDependentPointLocusV2) locus(label).getParentAlgorithm())
				.getHybridExistenceCertificate();
		assertNotNull(result, label);
		return result;
	}

	private LocusExistenceStructure2D structure(String label) {
		return certificate(label).getStructure();
	}

	private long work(String label, LocusCertificationWork2D kind) {
		return locus(label).getInstrumentation().getCertificationWork(kind);
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}
}

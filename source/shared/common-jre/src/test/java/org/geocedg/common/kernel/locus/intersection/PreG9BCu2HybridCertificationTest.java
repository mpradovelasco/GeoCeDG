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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
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
import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D;
import org.geocedg.common.kernel.locus.exact.ExactBudget1D;
import org.geocedg.common.kernel.locus.exact.ExactBudgetExhaustedException;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.exact.TrigonometricPolynomial2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot.RootKind;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.ExactEvidence;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T4: the hybrid existence certifier ({@code locus-existence/hybrid/v1};
 * {@code locus-v2-existence-components.md} section 15; {@code
 * locus-v2-certified-construction-model.md} sections 10.5 to 10.9) on synthetic
 * constructions, through the certificate of each revision (published since step T11). Matrix
 * rows V-TOT-1, V-PER-1 (synthetic part), V-CRIT-1 to V-CRIT-6 and V-CRIT-10, V-TR-2, V-POINT-1,
 * V-POINT-2, V-POINT-4 and V-POINT-6 (certificate parts), V-COINC-1, V-COINC-2, V-COINC-4,
 * V-COINC-5 and V-COINC-7 (certificate parts), V-LVL-1, V-LVL-3 (structure part), V-COMPL-1,
 * V-V3-3 and V-V3-4 (existence parts), and gates GC-4 and GC-8.
 */
class PreG9BCu2HybridCertificationTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	/** Isolated root of the synthetic external tangency (matrix section 1). */
	private static final double EXTERNAL = Math.atan2(-4, -3);
	/** Transition bracket width of the default policy: {@code 2^-40} periods. */
	private static final double DELTA = Math.scalb(2 * Math.PI, -40);

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

	private long work(String label, LocusCertificationWork2D kind) {
		return locus(label).getInstrumentation().getCertificationWork(kind);
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}
}

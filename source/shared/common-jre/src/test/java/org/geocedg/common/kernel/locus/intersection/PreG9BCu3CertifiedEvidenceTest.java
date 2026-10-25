/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 pieces I-1 to I-3 under {@code legacy-representation-authority/v1}: the residual
 * enclosures of {@code h}, {@code h'} and {@code h''}, the existence grades, the certified route
 * H02, the conflict rule with its root-count certificates and the contact evidence classes of
 * {@code cu3-certified-admission/v1} ({@code locus-v2-intersections.md} CU3.1 to CU3.4), on the
 * synthetic constructions of the preparation: the circle of radius 1.5 traced by
 * {@code Dilate(P, 1.5, O)} with {@code P} on the unit circle, against near-tangent lines and
 * circles, the H02 scale and translation sweep and the quarter arc. Validation matrix rows
 * V-B-3 to V-B-6, V-B-11 (without M3), V-B-DUP-1, V-CONF-1 to V-CONF-4 and V-CONF-6 to V-CONF-8,
 * V-EVID-2, V-EVID-3 and V-EVID-6, V-H2-1 to V-H2-5. Each adversarial check compares with the
 * same capability without residual enclosures, which is the behaviour of the base.
 */
final class PreG9BCu3CertifiedEvidenceTest extends BaseUnitTest {

	/** The same public capability without any residual enclosure: the base behaviour. */
	private static final IntersectionDecisionConfiguration2D BASE = (definition, target) -> null;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void anAbsentIntersectionIsWithdrawnByACertifiedFixedSign() {
		for (String target : new String[] {"g=Line((0,1.5000000000001),(1,1.5000000000001))",
				"g=Circle((3.0000000000001,0),1.5)"}) {
			reset();
			circleLocus(0, 1);
			add(target);
			assertEquals(1, internal("g", BASE).getFiniteSolutions().size(),
					target + ": the base declares a tangency (H-CU3-3)");
			LocusIntersectionResult2D rich = rich(command("g"));

			assertTrue(rich.getFiniteSolutions().isEmpty(), target + ": V-B-3 and V-CONF-1");
			IntersectionDiagnostic2D withdrawal = diagnostic(rich,
					DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE);
			assertTrue(withdrawal.getMessage().contains(
					ContactEvidenceClass.CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION.name()),
					withdrawal.getMessage());
			assertTrue(withdrawal.getMessage().contains("NONE on"), withdrawal.getMessage());
		}
		reset();
		circleLocus(0, 1);
		String above = Double.toString(Math.nextUp(1.5));
		add("g=Line((0," + above + "),(1," + above + "))");
		List<LocusIntersectionSolution2D> base = internal("g", BASE).getFiniteSolutions();
		LocusIntersectionResult2D rich = rich(command("g"));
		assertEquals(parameters(base), parameters(rich.getFiniteSolutions()),
				"V-CONF-1: one ulp above, h(t*) contains zero and nothing is withdrawn");
		assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE,
				rich.getFiniteSolutions().get(0).getRevisionEvidence().getExistenceEvidence()
						.getContactEvidence());
	}

	@Test
	void twoRootsAroundACertifiedCriticalPointAreExactlyTheSolutions() {
		String[][] cases = {{"g=Line((0,1.4999999999999),(1,1.4999999999999))", "4"},
				{"g=Line((0,1.4999999999),(1,1.4999999999))", "3"},
				{"g=Circle((2.9999999999999,0),1.5)", "4"},
				{"g=Circle((2.9999999999,0),1.5)", "3"}};
		for (String[] row : cases) {
			reset();
			circleLocus(0, 1);
			add(row[0]);
			assertEquals(Integer.parseInt(row[1]), internal("g", BASE).getFiniteSolutions()
					.size(), row[0] + ": the base publishes noise and duplicated roots");
			GeoLocusIntersectionResult result = command("g");
			LocusIntersectionResult2D rich = rich(result);

			assertEquals(2, rich.getFiniteSolutions().size(), row[0] + ": V-B-4 and V-CONF-3");
			assertEquals(2, admissible(result), row[0]);
			for (LocusIntersectionSolution2D solution : rich.getFiniteSolutions()) {
				IntersectionExistenceEvidence2D evidence =
						solution.getRevisionEvidence().getExistenceEvidence();
				assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
						solution.getClassification().getContactClass());
				assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED, evidence.getGrade(), row[0]);
				assertTrue(evidence.getCertificate().startsWith("TWO_SIMPLE"),
						evidence.getCertificate());
				assertEquals(CapturedResidualJetSource2D.CONFIGURATION,
						evidence.getConfiguration());
			}
			assertNotNull(diagnostic(rich, DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE));
		}
	}

	@Test
	void aContactOfClassThreeChangesNothing() {
		String below = Double.toString(Math.nextDown(1.5));
		for (String target : new String[] {"g=Line((0,1.5),(1,1.5))",
				"g=Line((0," + below + "),(1," + below + "))", "g=Circle((3,0),1.5)"}) {
			reset();
			circleLocus(0, 1);
			add(target);
			List<LocusIntersectionSolution2D> base = internal("g", BASE).getFiniteSolutions();
			GeoLocusIntersectionResult result = command("g");
			LocusIntersectionResult2D rich = rich(result);

			assertEquals(parameters(base), parameters(rich.getFiniteSolutions()),
					target + ": V-B-5 and V-CONF-4, no solution withdrawn or reclassified");
			assertNull(find(rich, DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE), target);
			IntersectionDiagnostic2D classified = diagnostic(rich,
					DiagnosticCode.CONTACT_EVIDENCE_CLASSIFIED);
			assertTrue(classified.getMessage().contains(
					ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE.name()));
			for (LocusIntersectionSolution2D solution : rich.getFiniteSolutions()) {
				if (solution.getClassification().getContactClass()
						== ContactClass.TANGENT_ESTABLISHED) {
					assertFalse(result.isPointAdmissible(solution.getIdentity().getRootToken()),
							"a class-3 contact is never an intersection");
					assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE, solution
							.getRevisionEvidence().getExistenceEvidence().getContactEvidence());
				}
			}
		}
	}

	@Test
	void aTransverseRootNextToACriticalPointKeepsItsState() {
		circleLocus(0, 1);
		add("g=Line((0,0.75),(1,0.75))");
		List<LocusIntersectionSolution2D> base = internal("g", BASE).getFiniteSolutions();
		GeoLocusIntersectionResult result = command("g");
		LocusIntersectionResult2D rich = rich(result);

		assertEquals(parameters(base), parameters(rich.getFiniteSolutions()), "V-CONF-2");
		assertEquals(2, admissible(result));
		for (LocusIntersectionSolution2D solution : rich.getFiniteSolutions()) {
			IntersectionExistenceEvidence2D evidence =
					solution.getRevisionEvidence().getExistenceEvidence();
			assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED, evidence.getGrade(), "V-EVID-2");
			assertTrue(evidence.getCertificate().startsWith("ONE_SIMPLE"));
			assertFalse(evidence.isCertifiedRoute());
		}
		assertNull(find(rich, DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE),
				"V-EVID-6: a generic transverse root does not trigger the conflict rule");
		assertNull(find(rich, DiagnosticCode.CONTACT_EVIDENCE_CLASSIFIED));
	}

	@Test
	void aTangentCandidateAtACertifiedSimpleRootIsReclassified() {
		circleLocus(0, 1);
		add("g=Line((0,0.75),(1,0.75))");
		IntersectionCapabilityContext2D context = context("g");
		ResidualJetSource2D source = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				context.getDefinition(), context.getTarget());
		double root = Math.PI / 6;
		IntersectionCandidate2D tangent = candidate(context, root, ContactClass.TANGENT_ESTABLISHED,
				LocalIsolationStatus.NOT_ESTABLISHED);

		CertifiedContactAnalysis2D.Outcome outcome = CertifiedContactAnalysis2D.apply(context,
				source, List.of(tangent), false);

		assertEquals(1, outcome.getCandidates().size());
		IntersectionCandidate2D reclassified = outcome.getCandidates().get(0);
		assertEquals(ContactClass.TRANSVERSE_ESTABLISHED, reclassified.getContactClass(),
				"V-CONF-7");
		assertEquals(LocalIsolationStatus.ESTABLISHED, reclassified.getLocalIsolationStatus());
		assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED,
				reclassified.getExistenceEvidence().getGrade());
		assertEquals(root, reclassified.getSemanticParameter(), 1E-12);
		assertNotNull(diagnostic(outcome.getDiagnostics(),
				DiagnosticCode.CERTIFIED_EXISTENCE_ESTABLISHED));
	}

	@Test
	void anEnclosureCompatibleRootIsRelaxedAndAContactNever() {
		circleLocus(0, 1);
		add("g=Line((0,0.75),(1,0.75))");
		IntersectionCapabilityContext2D context = context("g");
		ResidualJetSource2D captured = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				context.getDefinition(), context.getTarget());
		ResidualJetSource2D uncertifiable = new ResidualJetSource2D() {
			@Override
			public String getConfiguration() {
				return "test/constructive-without-derivative";
			}

			@Override
			public boolean isConstructive() {
				return true;
			}

			@Override
			public ResidualJetEnclosure2D enclose(double lower, double upper) {
				ResidualJetEnclosure2D jet = captured.enclose(lower, upper);
				return jet == null ? null : new ResidualJetEnclosure2D(jet.getValue(),
						new double[] {-1, 1}, jet.getSecond());
			}

			@Override
			public double[] normalizedResidual(double parameter) {
				return captured.normalizedResidual(parameter);
			}
		};
		double root = Math.PI / 6;
		List<IntersectionCandidate2D> candidates = List.of(
				candidate(context, root, ContactClass.TRANSVERSE_ESTABLISHED,
						LocalIsolationStatus.ESTABLISHED),
				candidate(context, Math.PI / 2, ContactClass.TANGENT_ESTABLISHED,
						LocalIsolationStatus.NOT_ESTABLISHED),
				candidate(context, 0.6, ContactClass.TRANSVERSE_ESTABLISHED,
						LocalIsolationStatus.ESTABLISHED));

		for (IntersectionCandidate2D candidate : CertifiedContactAnalysis2D.apply(context,
				uncertifiable, candidates, false).getCandidates()) {
			IntersectionExistenceEvidence2D evidence = candidate.getExistenceEvidence();
			double parameter = candidate.getSemanticParameter();
			if (parameter == root) {
				assertEquals(ConstructiveAdmission.RELAXED, evidence.getConstructiveAdmission(),
						"ENCLOSURE_COMPATIBLE without certified existence");
				assertEquals(ExistenceGrade.LOCALLY_VERIFIED, evidence.getGrade());
			} else {
				assertEquals(ConstructiveAdmission.NOT_ESTABLISHED,
						evidence.getConstructiveAdmission(), parameter + ": a contact or a"
								+ " residual enclosure without zero is never relaxed");
			}
		}
	}

	@Test
	void aClassThreeBoxOfACompleteSetOnlyLowersItsCompleteness() {
		circleLocus(0, 1);
		add("g=Line((0,1.5),(1,1.5))");
		IntersectionCapabilityContext2D context = context("g");
		ResidualJetSource2D source = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				context.getDefinition(), context.getTarget());
		IntersectionCandidate2D tangent = candidate(context, Math.PI / 2,
				ContactClass.TANGENT_ESTABLISHED, LocalIsolationStatus.NOT_ESTABLISHED);

		CertifiedContactAnalysis2D.Outcome outcome = CertifiedContactAnalysis2D.apply(context,
				source, List.of(tangent), true);

		assertTrue(outcome.isCompletenessContradicted(), "V-CONF-8");
		assertEquals(1, outcome.getCandidates().size(), "the solution is kept");
		assertEquals(tangent.getSemanticParameter(),
				outcome.getCandidates().get(0).getSemanticParameter(), 0);
	}

	@Test
	void anIncludedEndpointTangencyKeepsItsStateWithoutAnExactDecision() {
		add("a=CircularArc((0,0),(1,0),(0,1))");
		add("P=Point(a)");
		add("Q=Dilate(P,1,(0,0))");
		add("L=LocusV2(Q,P)");
		add("g=Line((1,0),(1,1))");
		List<LocusIntersectionSolution2D> base = internal("g", BASE).getFiniteSolutions();
		GeoLocusIntersectionResult result = command("g");
		LocusIntersectionResult2D rich = rich(result);

		assertEquals(parameters(base), parameters(rich.getFiniteSolutions()),
				"V-B-11 without M3 and V-CONF-6");
		LocusIntersectionSolution2D endpoint = rich.getFiniteSolutions().get(0);
		assertEquals(ExistenceGrade.LOCALLY_VERIFIED,
				endpoint.getRevisionEvidence().getExistenceEvidence().getGrade(),
				"the endpoint has no certified sign change");
		assertTrue(result.isPointAdmissible(endpoint.getIdentity().getRootToken()),
				"its G8B-R1 state is kept (H-CU3-2 needs the exact decision of E3)");
		ResidualJetSource2D source = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				((GeoLocusV2) lookup("L")).getSemanticDefinition(), capture("g"));
		RootCountCertificate2D outside = RootCountCertificate2D.certify(source, -0.01, 0.01);
		assertFalse(outside.isCertified(), "a box across the end of the domain certifies nothing");
	}

	@Test
	void theCertifiedRouteAdmitsTheTransverseRootsOfEveryScale() {
		for (double radius : new double[] {2, 20, 200, 2000, 20000}) {
			for (String kind : new String[] {"line", "segment"}) {
				reset();
				circleLocus(0, radius);
				add(target(kind, 0, radius));
				LocusIntersectionResult2D base = internal("g", BASE);
				GeoLocusIntersectionResult result = command("g");

				assertEquals(2, admissible(result), "V-H2-1: " + kind + " radius " + radius);
				if (radius >= 2000) {
					assertEquals(0, base.getFiniteSolutions().size(),
							"the base loses both roots at radius " + radius);
				}
				for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
					IntersectionExistenceEvidence2D evidence =
							solution.getRevisionEvidence().getExistenceEvidence();
					assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED, evidence.getGrade());
					if (evidence.isCertifiedRoute()) {
						assertTrue(evidence.getCharacteristicScale() >= 3 * radius * (1 - 1E-12),
								"L is the extent of the image or of the segment");
						assertTrue(Math.abs(solution.getRevisionEvidence().getResidualEvidence()
								.getNormalizedResidual()) <= CertifiedAdmissionPolicy2D
										.RELATIVE_RESIDUAL * evidence.getCharacteristicScale());
					}
				}
			}
		}
		reset();
		circleLocus(0, 20000);
		add(target("line", 0, 20000));
		boolean route = false;
		for (LocusIntersectionSolution2D solution : rich(command("g")).getFiniteSolutions()) {
			route |= solution.getRevisionEvidence().getExistenceEvidence().isCertifiedRoute();
		}
		assertTrue(route, "at radius 20000 the G8B-R1 threshold alone is not met");
	}

	@Test
	void translationsGiveTheResultOfTheOrigin() {
		for (double translation : new double[] {1E3, 1E6}) {
			for (double radius : new double[] {2, 20, 200, 2000, 20000}) {
				reset();
				circleLocus(translation, radius);
				add(target("line", translation, radius));
				GeoLocusIntersectionResult result = command("g");

				assertEquals(2, rich(result).getFiniteSolutions().size(), "V-B-DUP-1 and"
						+ " V-H2-2: translation " + translation + " radius " + radius);
				assertEquals(2, admissible(result));
			}
		}
		reset();
		circleLocus(1E6, 2);
		add(target("line", 1E6, 2));
		assertEquals(4, internal("g", BASE).getFiniteSolutions().size(),
				"the base publishes duplicated preimages (H-CU3-4)");
	}

	@Test
	void smallDrawingsKeepTheG8bR1Route() {
		for (String kind : new String[] {"line", "segment"}) {
			reset();
			circleLocus(0, 1E-3);
			add(target(kind, 0, 1E-3));
			List<LocusIntersectionSolution2D> base = internal("g", BASE).getFiniteSolutions();
			GeoLocusIntersectionResult result = command("g");

			assertEquals(parameters(base), parameters(rich(result).getFiniteSolutions()),
					"V-H2-3: " + kind);
			assertEquals(2, admissible(result));
			for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
				assertFalse(solution.getRevisionEvidence().getExistenceEvidence()
						.isCertifiedRoute(), "the G8B-R1 threshold admits them");
			}
		}
	}

	@Test
	void theScaleOfTheTargetEquationChangesNothing() {
		circleLocus(0, 2);
		add("g=Line((0,1),(1,1))");
		GeoLocusIntersectionResult unit = command("g");
		add("k:1000y=1000");
		GeoLocusIntersectionResult scaled = add("S=Intersect(L,k)");

		List<LocusIntersectionSolution2D> first = rich(unit).getFiniteSolutions();
		List<LocusIntersectionSolution2D> second = rich(scaled).getFiniteSolutions();
		assertEquals(first.size(), second.size(), "V-H2-5");
		assertEquals(admissible(unit), admissible(scaled));
		for (int index = 0; index < first.size(); index++) {
			assertEquals(first.get(index).getRevisionEvidence().getSemanticParameter(),
					second.get(index).getRevisionEvidence().getSemanticParameter(), 4E-12);
			assertEquals(first.get(index).getRevisionEvidence().getExistenceEvidence().getGrade(),
					second.get(index).getRevisionEvidence().getExistenceEvidence().getGrade());
		}
	}

	@Test
	void rejectionsEmitTheirDeclaredDiagnostics() {
		circleLocus(0, 1);
		add("s=Segment((-0.5,0.75),(2,0.75))");
		LocusIntersectionResult2D rich = rich(command("s"));
		assertEquals(1, rich.getFiniteSolutions().size());
		assertTrue(diagnostic(rich, DiagnosticCode.MEMBERSHIP_REJECTED).getMessage()
				.startsWith("1 candidate"), "V-H2-4: the root off the segment");

		add("g=Line((0,0.75),(1,0.75))");
		AlgoLocusIntersectionV2 algorithm = new AlgoLocusIntersectionV2(getConstruction(),
				(GeoLocusV2) lookup("L"), lookup("g"), "cu3-pair", "cu3-lineage", "cu3-target",
				"cu3-topology", new WrongCandidate(), new GeoElement[0]);
		LocusIntersectionResult2D rejected = algorithm.getResult().getIntersectionResult();
		assertTrue(rejected.getFiniteSolutions().isEmpty());
		assertNotNull(diagnostic(rejected, DiagnosticCode.RESIDUAL_REJECTED),
				"V-H2-4: a candidate off the target");
	}

	@Test
	void gradesAreRevisionEvidenceThatNeverEntersAToken() {
		circleLocus(0, 1);
		add("g=Line((0,0.75),(1,0.75))");
		add("a=CircularArc((0,0),(1,0),(0,1))");
		add("P2=Point(a)");
		add("Q2=Dilate(P2,1,(0,0))");
		add("L2=LocusV2(Q2,P2)");
		add("k=Line((1,0),(1,1))");
		command("g");
		add("E=Intersect(L2,k)");
		List<String> tokens = tokens("Rg");
		List<ExistenceGrade> grades = grades("Rg");

		reload();

		assertEquals(tokens, tokens("Rg"), "V-EVID-3: the same tokens after reopening");
		assertEquals(grades, grades("Rg"), "the grades are recomputed, never persisted");
		assertEquals(List.of(ExistenceGrade.LOCALLY_VERIFIED), grades("E"),
				"no promotion through persistence");
		for (String token : tokens) {
			assertFalse(token.contains("EXISTENCE") || token.contains("cu3-certified"), token);
		}
		assertFalse(getApp().getXML().contains("EXISTENCE_CERTIFIED"));
	}

	// ---------------------------------------------------------------- construction

	private void reset() {
		getKernel().clearConstruction(true);
	}

	private void circleLocus(double translation, double radius) {
		add("O=(" + translation + "," + translation + ")");
		add("c=Circle(O," + radius + ")");
		add("P=Point(c)");
		add("Q=Dilate(P,1.5,O)");
		add("L=LocusV2(Q,P)");
	}

	/** The H02 targets of the CU0 sweep, translated with the circle. */
	private static String target(String kind, double translation, double radius) {
		if ("line".equals(kind)) {
			double y = translation + 0.5 * radius;
			return "g=Line((" + translation + "," + y + "),(" + (translation + 1) + "," + y
					+ "))";
		}
		return "g=Segment((" + (translation - 2 * radius) + "," + (translation + 0.4 * radius)
				+ "),(" + (translation + 2 * radius) + "," + (translation + 0.6 * radius)
				+ "))";
	}

	private GeoLocusIntersectionResult command(String target) {
		return add("R" + target + "=Intersect(L," + target + ")");
	}

	private LocusIntersectionResult2D internal(String target,
			IntersectionDecisionConfiguration2D configuration) {
		AlgoLocusIntersectionV2 algorithm = new AlgoLocusIntersectionV2(getConstruction(),
				(GeoLocusV2) lookup("L"), lookup(target), "cu3-pair", "cu3-lineage",
				"cu3-target", "cu3-topology",
				new PublicTargetIntersectionCapability2D(configuration), new GeoElement[0]);
		return algorithm.getResult().getIntersectionResult();
	}

	private LocusIntersectionTarget2D capture(String target) {
		return LocusIntersectionTargets2D.capture(lookup(target), "cu3-target", 1);
	}

	private IntersectionCapabilityContext2D context(String target) {
		LocusDefinition2D definition = ((GeoLocusV2) lookup("L")).getSemanticDefinition();
		LocusIntersectionTarget2D captured = capture(target);
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor(),
				captured.getResidualContract());
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D("cu3-pair", "cu3-lineage",
				definition.getLocusIdentity(), Math.max(1, definition.getSemanticRevision()),
				"cu3-target", 1, "cu3-topology", policy);
		return new IntersectionCapabilityContext2D(query, definition, captured,
				LocusEvaluationSession2D.memoizing(64),
				new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
	}

	private static IntersectionCandidate2D candidate(IntersectionCapabilityContext2D context,
			double parameter, ContactClass contact, LocalIsolationStatus isolation) {
		String branch = context.getDefinition().getBranches().get(0).getBranchKey();
		return new IntersectionCandidate2D(branch,
				IntersectionCapabilityContext2D.componentKey(branch, 0), parameter,
				OptionalDouble.empty(), new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12), isolation, Optional.empty(), contact,
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.SAFEGUARDED_DERIVATIVE, NumericGuarantee.ESTIMATED_ERROR,
				LineageEventKind.APPEARED, Collections.emptyList(), Collections.emptyList());
	}

	// ---------------------------------------------------------------- reading

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		assertNotNull(result);
		return result.getIntersectionResult();
	}

	private static int admissible(GeoLocusIntersectionResult result) {
		int count = 0;
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			count += result.isPointAdmissible(solution.getIdentity().getRootToken()) ? 1 : 0;
		}
		return count;
	}

	private static List<String> parameters(List<LocusIntersectionSolution2D> solutions) {
		List<String> rows = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : solutions) {
			rows.add(solution.getRevisionEvidence().getSemanticParameter() + " "
					+ solution.getClassification().getContactClass() + " "
					+ solution.getRevisionEvidence().getLocalIsolationStatus());
		}
		return rows;
	}

	private List<String> tokens(String label) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich((GeoLocusIntersectionResult)
				lookup(label)).getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	private List<ExistenceGrade> grades(String label) {
		List<ExistenceGrade> grades = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich((GeoLocusIntersectionResult)
				lookup(label)).getFiniteSolutions()) {
			grades.add(solution.getRevisionEvidence().getExistenceEvidence().getGrade());
		}
		return grades;
	}

	private static IntersectionDiagnostic2D find(LocusIntersectionResult2D result,
			DiagnosticCode code) {
		for (IntersectionDiagnostic2D diagnostic : result.getDiagnostics()) {
			if (diagnostic.getCode() == code) {
				return diagnostic;
			}
		}
		return null;
	}

	private static IntersectionDiagnostic2D diagnostic(LocusIntersectionResult2D result,
			DiagnosticCode code) {
		return diagnostic(result.getDiagnostics(), code);
	}

	private static IntersectionDiagnostic2D diagnostic(List<IntersectionDiagnostic2D> list,
			DiagnosticCode code) {
		for (IntersectionDiagnostic2D diagnostic : list) {
			if (diagnostic.getCode() == code) {
				return diagnostic;
			}
		}
		throw new AssertionError("no " + code + " in " + list);
	}

	/** A capability that offers one transverse candidate at a parameter off the target. */
	private static final class WrongCandidate implements LocusIntersectionCapability2D {
		@Override
		public String getCapabilityId() {
			return "cu3-test-wrong-candidate";
		}

		@Override
		public boolean supports(IntersectionCapabilityContext2D context) {
			return true;
		}

		@Override
		public IntersectionCandidateSet2D isolate(IntersectionCapabilityContext2D context) {
			IntersectionCandidate2D wrong = candidate(context, 0,
					ContactClass.TRANSVERSE_ESTABLISHED, LocalIsolationStatus.ESTABLISHED);
			String component = wrong.getComponentKey();
			return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
					CompletenessMethod.NOT_ESTABLISHED, GeometryKind.FINITE,
					SupportLevel.VERIFIED_UNCERTIFIED, NumericGuarantee.ESTIMATED_ERROR,
					List.of(component), List.of(wrong), Collections.emptyList(),
					Collections.emptyList());
		}
	}
}

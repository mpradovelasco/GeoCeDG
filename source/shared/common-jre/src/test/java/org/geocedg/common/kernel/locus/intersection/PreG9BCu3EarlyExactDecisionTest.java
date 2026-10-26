/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 T7-a: the early exact decision of {@code cu3-certified-admission/v2}
 * ({@code locus-v2-intersections.md}, constructive admission policy v2 amendment; Gate N record),
 * against the procedure of version 1 on the same residual. The residual is that of the circle of
 * radius 1.5 traced by {@code Dilate(P, 1.5, O)} and the line {@code y = 1.5}, tangent at
 * {@code t = pi/2}, behind a source that declares itself constructive or not, injects exact
 * contacts and transforms enclosures only towards less information: an interval widened to
 * contain zero or a refused box, so that every enclosure stays rigorous. The early decision
 * establishes the one double root with fewer enclosures and the same evidence; it also does so
 * where version 1 reaches its work limit or loses a sub-box of the bracket to a numerical
 * failure; anything that does not establish the form (no contact, a contact outside the bracket,
 * a disagreeing exact sign of {@code h''}, a bracket whose enclosure excludes zero, a box or a
 * bracket without validity) leaves exactly the certificate of version 1, with the same work; a
 * source that is not constructive never decides early; and the policy version follows the
 * configuration.
 */
final class PreG9BCu3EarlyExactDecisionTest extends BaseUnitTest {

	private static final double CONTACT = Math.PI / 2;
	private static final double LOWER = CONTACT - 0.01;
	private static final double UPPER = CONTACT + 0.01;
	/** Half-width of the band in which {@code h'} at a point is left undecided. */
	private static final double BAND = 1E-4;

	private IntersectionCapabilityContext2D context;
	private ResidualJetSource2D captured;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@BeforeEach
	void tangentCircle() {
		add("O=(0,0)");
		add("c=Circle(O,1)");
		add("P=Point(c)");
		add("Q=Dilate(P,1.5,O)");
		add("L=LocusV2(Q,P)");
		add("g=Line((0,1.5),(1,1.5))");
		add("g2=Line((0,1.55),(1,1.55))");
		context = context("g");
		captured = captured(context);
	}

	@Test
	void theExactDecisionComesBeforeTheSubdivisionWithTheSameEvidence() {
		ExactContactEvidence2D exact = exact(-1);
		Probe legacy = new Probe(captured, false, exact, Probe::wideBracket);
		Probe constructive = new Probe(captured, true, exact, Probe::wideBracket);

		RootCountCertificate2D first = RootCountCertificate2D.certify(legacy, LOWER, UPPER);
		RootCountCertificate2D second = RootCountCertificate2D.certify(constructive, LOWER, UPPER);

		assertEquals(RootCountCertificate2D.Form.ONE_DOUBLE, first.getForm(), first.describe());
		assertEquals(RootCountCertificate2D.Form.ONE_DOUBLE, second.getForm(), second.describe());
		assertSame(exact, second.getExact());
		assertEquals(bounds(first.getCritical()), bounds(second.getCritical()), "the same bracket");
		assertEquals(bounds(first.getRoots().get(0)), bounds(second.getRoots().get(0)));
		assertEquals(first.getContact(), second.getContact());
		assertEquals(first.getReason(), second.getReason());
		assertTrue(second.getEvaluations() < first.getEvaluations(), "fewer enclosures: "
				+ second.getEvaluations() + " against " + first.getEvaluations());
		assertEquals(1, constructive.consultations);
	}

	@Test
	void whatDoesNotEstablishTheFormLeavesTheCertificateOfVersionOne() {
		List<ExactContactEvidence2D> cases = new ArrayList<>();
		cases.add(null);
		cases.add(new ExactContactEvidence2D("0", 0, 0,
				ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, ContactOrder.TWO, -1,
				"outside the bracket"));
		cases.add(exact(1));
		for (ExactContactEvidence2D exact : cases) {
			for (boolean wide : new boolean[] {false, true}) {
				Filter filter = wide ? Probe::wideBracket : Probe::none;
				RootCountCertificate2D first = RootCountCertificate2D.certify(
						new Probe(captured, false, exact, filter), LOWER, UPPER);
				RootCountCertificate2D second = RootCountCertificate2D.certify(
						new Probe(captured, true, exact, filter), LOWER, UPPER);

				assertEquals(first.describe(), second.describe(), exact + ", wide " + wide
						+ ": the same certificate and the same work");
				assertEquals(RootCountCertificate2D.Form.NOT_CERTIFIED, second.getForm());
			}
		}
	}

	@Test
	void aBracketWhoseEnclosureExcludesZeroIsNeverAskedForAnExactDecision() {
		ResidualJetSource2D above = captured(context("g2"));
		ExactContactEvidence2D contradictory = exact(-1);
		Probe legacy = new Probe(above, false, contradictory, Probe::none);
		Probe constructive = new Probe(above, true, contradictory, Probe::none);

		RootCountCertificate2D first = RootCountCertificate2D.certify(legacy, LOWER, UPPER);
		RootCountCertificate2D second = RootCountCertificate2D.certify(constructive, LOWER, UPPER);

		assertEquals(RootCountCertificate2D.Form.NONE, second.getForm(), second.describe());
		assertEquals(first.describe(), second.describe());
		assertEquals(0, constructive.consultations,
				"a certified critical value is never contradicted by an injected decision");
	}

	@Test
	void aNumericalFailureOfASubBoxOfTheBracketIsNotALackOfValidity() {
		double[] bracket = bracket();
		Filter failing = (lower, upper, jet) -> lower < upper && properlyInside(lower, upper,
				bracket) ? null : Probe.wideBracket(lower, upper, jet);

		RootCountCertificate2D first = RootCountCertificate2D.certify(
				new Probe(captured, false, exact(-1), failing), LOWER, UPPER);
		RootCountCertificate2D second = RootCountCertificate2D.certify(
				new Probe(captured, true, exact(-1), failing), LOWER, UPPER);
		RootCountCertificate2D without = RootCountCertificate2D.certify(
				new Probe(captured, true, null, failing), LOWER, UPPER);

		assertEquals(RootCountCertificate2D.Form.NOT_CERTIFIED, first.getForm());
		assertEquals("the source is not certified valid on the box", first.getReason(),
				"version 1 gives up on the sub-box");
		assertEquals(RootCountCertificate2D.Form.ONE_DOUBLE, second.getForm(), second.describe());
		assertEquals(first.describe(), RootCountCertificate2D.certify(
				new Probe(captured, false, null, failing), LOWER, UPPER).describe());
		assertEquals(first.describe(), without.describe(),
				"without an exact contact version 2 gives up as version 1");
	}

	@Test
	void aBoxOrABracketWithoutValidityIsRefusedAsInVersionOne() {
		double[] bracket = bracket();
		Filter noBracket = (lower, upper, jet) -> lower < upper && bracket[0] <= lower
				&& upper <= bracket[1] ? null : Probe.wideBracket(lower, upper, jet);
		Filter noBox = (lower, upper, jet) -> lower == LOWER && upper == UPPER ? null : jet;
		for (Filter filter : List.of(noBracket, noBox)) {
			Probe constructive = new Probe(captured, true, exact(-1), filter);
			RootCountCertificate2D first = RootCountCertificate2D.certify(
					new Probe(captured, false, exact(-1), filter), LOWER, UPPER);
			RootCountCertificate2D second = RootCountCertificate2D.certify(constructive, LOWER,
					UPPER);

			assertEquals("the source is not certified valid on the box", second.getReason(),
					second.describe());
			assertEquals(first.describe(), second.describe());
			assertEquals(0, constructive.consultations, "no exact decision without validity");
		}
	}

	@Test
	void theWorkLimitNoLongerHidesTheDoubleRootAndKeepsItsValue() {
		double[] bracket = bracket();
		double coarse = (bracket[1] - bracket[0]) / 5000;
		Filter wide = (lower, upper, jet) -> {
			ResidualJetEnclosure2D widened = Probe.wideBracket(lower, upper, jet);
			if (widened == null || !(upper - lower > coarse) || !properlyInside(lower, upper,
					bracket)) {
				return widened;
			}
			return new ResidualJetEnclosure2D(hull(widened.getValue()), widened.getFirst(),
					widened.getSecond());
		};
		Probe legacy = new Probe(captured, false, exact(-1), wide);
		Probe constructive = new Probe(captured, true, exact(-1), wide);

		RootCountCertificate2D first = RootCountCertificate2D.certify(legacy, LOWER, UPPER);
		RootCountCertificate2D second = RootCountCertificate2D.certify(constructive, LOWER, UPPER);
		RootCountCertificate2D without = RootCountCertificate2D.certify(
				new Probe(captured, true, null, wide), LOWER, UPPER);

		assertEquals("the work limit of the certificate is reached", first.getReason(),
				first.describe());
		assertEquals(CertifiedAdmissionPolicy2D.MAXIMUM_EVALUATIONS + 1, first.getEvaluations(),
				"the limit is unchanged");
		assertEquals(0, legacy.consultations, "a source that is not constructive never decides"
				+ " early");
		assertEquals(RootCountCertificate2D.Form.ONE_DOUBLE, second.getForm(), second.describe());
		assertTrue(second.getEvaluations() <= CertifiedAdmissionPolicy2D.MAXIMUM_EVALUATIONS);
		assertEquals(first.describe(), without.describe(),
				"without an exact contact version 2 reaches the same limit");
	}

	@Test
	void thePolicyVersionFollowsTheConfiguration() {
		ExactContactEvidence2D exact = exact(-1);
		IntersectionCandidate2D tangent = candidate(CONTACT);
		for (boolean constructive : new boolean[] {false, true}) {
			Probe source = new Probe(captured, constructive, exact, Probe::none);
			String version = constructive ? CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION
					: CertifiedAdmissionPolicy2D.VERSION;
			assertEquals(version, CertifiedAdmissionPolicy2D.version(source));

			CertifiedContactAnalysis2D.Outcome outcome = CertifiedContactAnalysis2D.apply(context,
					source, List.of(tangent), false);

			assertEquals(1, outcome.getCandidates().size(), outcome.getDiagnostics().toString());
			IntersectionCandidate2D contact = outcome.getCandidates().get(0);
			assertEquals(version, contact.getExistenceEvidence().getPolicyVersion());
			assertEquals(OptionalInt.of(2), contact.getEstablishedMultiplicity());
			boolean named = false;
			for (IntersectionDiagnostic2D diagnostic : contact.getDiagnostics()) {
				named |= diagnostic.getMessage().startsWith("Root-count certificate of "
						+ version + ":");
			}
			assertTrue(named, contact.getDiagnostics().toString());
		}
		assertEquals(CertifiedAdmissionPolicy2D.VERSION,
				IntersectionExistenceEvidence2D.locallyVerified().getPolicyVersion(),
				"evidence that no configuration computed names version 1");
	}

	// ---------------------------------------------------------------- helpers

	/** A transformation of one enclosure towards less information. */
	private interface Filter {
		ResidualJetEnclosure2D apply(double lower, double upper, ResidualJetEnclosure2D jet);
	}

	/** The residual of a source, declared constructive or not, with injected exact contacts. */
	private static final class Probe implements ResidualJetSource2D {
		private final ResidualJetSource2D delegate;
		private final boolean constructive;
		private final ExactContactEvidence2D exact;
		private final Filter filter;
		int consultations;

		Probe(ResidualJetSource2D delegate, boolean constructive, ExactContactEvidence2D exact,
				Filter filter) {
			this.delegate = delegate;
			this.constructive = constructive;
			this.exact = exact;
			this.filter = filter;
		}

		@Override
		public String getConfiguration() {
			return "test/early-exact-decision";
		}

		@Override
		public boolean isConstructive() {
			return constructive;
		}

		@Override
		public ResidualJetEnclosure2D enclose(double lower, double upper) {
			ResidualJetEnclosure2D jet = delegate.enclose(lower, upper);
			return jet == null ? null : filter.apply(lower, upper, jet);
		}

		@Override
		public double[] normalizedResidual(double parameter) {
			return delegate.normalizedResidual(parameter);
		}

		@Override
		public List<ExactContactEvidence2D> exactContacts(double lower, double upper) {
			consultations++;
			return exact != null && lower <= exact.getLower() && exact.getUpper() <= upper
					? List.of(exact) : Collections.emptyList();
		}

		static ResidualJetEnclosure2D none(double lower, double upper,
				ResidualJetEnclosure2D jet) {
			return jet;
		}

		/**
		 * {@code h'} at a point within {@link #BAND} of the contact left undecided, so that the
		 * bracket of the critical point stays about that wide.
		 */
		static ResidualJetEnclosure2D wideBracket(double lower, double upper,
				ResidualJetEnclosure2D jet) {
			if (lower != upper || Math.abs(lower - CONTACT) >= BAND) {
				return jet;
			}
			return new ResidualJetEnclosure2D(jet.getValue(), hull(jet.getFirst()),
					jet.getSecond());
		}
	}

	/** @return the bracket of the critical point that version 1 finds with the wide band */
	private double[] bracket() {
		RootCountCertificate2D reference = RootCountCertificate2D.certify(
				new Probe(captured, false, null, Probe::wideBracket), LOWER, UPPER);
		double[] bracket = reference.getCritical();
		assertNotNull(bracket, reference.describe());
		assertTrue(bracket[1] - bracket[0] > BAND / 2, "a wide bracket: " + reference.describe());
		return bracket;
	}

	private static boolean properlyInside(double lower, double upper, double[] bracket) {
		return bracket[0] <= lower && upper <= bracket[1]
				&& (bracket[0] < lower || upper < bracket[1]);
	}

	private static double[] hull(double[] bounds) {
		return new double[] {Math.min(bounds[0], 0), Math.max(bounds[1], 0)};
	}

	private static List<Double> bounds(double[] interval) {
		return List.of(interval[0], interval[1]);
	}

	private static ExactContactEvidence2D exact(int curvature) {
		return new ExactContactEvidence2D("pi/2", Math.PI / 2, Math.nextUp(Math.PI / 2),
				ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, ContactOrder.TWO, curvature,
				"injected exact contact");
	}

	private static ResidualJetSource2D captured(IntersectionCapabilityContext2D context) {
		ResidualJetSource2D source = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				context.getDefinition(), context.getTarget());
		assertNotNull(source);
		return source;
	}

	private IntersectionCapabilityContext2D context(String target) {
		LocusDefinition2D definition = ((GeoLocusV2) lookup("L")).getSemanticDefinition();
		LocusIntersectionTarget2D target2D = LocusIntersectionTargets2D.capture(lookup(target),
				"cu3-target", 1);
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor(),
				target2D.getResidualContract());
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D("cu3-pair", "cu3-lineage",
				definition.getLocusIdentity(), Math.max(1, definition.getSemanticRevision()),
				"cu3-target", 1, "cu3-topology", policy);
		return new IntersectionCapabilityContext2D(query, definition, target2D,
				LocusEvaluationSession2D.memoizing(64),
				new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
	}

	private IntersectionCandidate2D candidate(double parameter) {
		String branch = context.getDefinition().getBranches().get(0).getBranchKey();
		return new IntersectionCandidate2D(branch,
				IntersectionCapabilityContext2D.componentKey(branch, 0), parameter,
				OptionalDouble.empty(), new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12), LocalIsolationStatus.NOT_ESTABLISHED, Optional.empty(),
				ContactClass.TANGENT_ESTABLISHED, MultiplicityStatus.NOT_ESTABLISHED,
				OptionalInt.empty(), SolverMethod.SAFEGUARDED_DERIVATIVE,
				NumericGuarantee.ESTIMATED_ERROR, LineageEventKind.APPEARED, new ArrayList<>(),
				Collections.emptyList());
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
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
 * PRE-G9B-CU3 increment E3: the fourth form of the root-count certificate, one double root at
 * an exact point (Stage B design section 9.1), and its contradictions in the conflict rule
 * (section 9.2), on the circle of radius 1.5 traced by {@code Dilate(P, 1.5, O)} with
 * {@code P} on the unit circle and the line {@code y = 1.5}, tangent at {@code t = pi/2}, where
 * the interval enclosures alone give contact class 3. The exact decisions come from a source
 * that wraps the captured one and declares one exact contact; the constructive configuration
 * that produces them is tested on the replica of the author's model. Validation matrix rows
 * V-CONF-4 and V-CONF-5 in their rule part: without the exact decision nothing changes; with it
 * the noise is withdrawn, the contact is the one double root and a transverse classification
 * that meets it is reclassified; an exact sign of {@code h''} that disagrees with its enclosure,
 * or an exact parameter outside the bracket of the critical point, certifies nothing.
 */
final class PreG9BCu3ExactDoubleRootTest extends BaseUnitTest {

	private static final double CONTACT = Math.PI / 2;

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
		context = context("g");
		captured = IntersectionDecisionConfiguration2D.CAPTURED.residualJets(
				context.getDefinition(), context.getTarget());
		assertNotNull(captured);
	}

	@Test
	void withoutAnExactDecisionTheContactStaysClassThree() {
		RootCountCertificate2D certificate = RootCountCertificate2D.certify(captured,
				CONTACT - 0.01, CONTACT + 0.01);

		assertEquals(RootCountCertificate2D.Form.NOT_CERTIFIED, certificate.getForm());
		assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE,
				certificate.getContact(), "V-CONF-4: class 3 certifies no count");
		assertNull(certificate.getExact());
	}

	@Test
	void anExactDecisionGivesOneDoubleRootAtTheExactParameter() {
		ExactContactEvidence2D exact = exact(-1);
		RootCountCertificate2D certificate = RootCountCertificate2D.certify(
				withExact(exact), CONTACT - 0.01, CONTACT + 0.01);

		assertEquals(RootCountCertificate2D.Form.ONE_DOUBLE, certificate.getForm(),
				certificate.describe());
		assertTrue(certificate.isCertified());
		assertSame(exact, certificate.getExact());
		assertEquals(ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, certificate.getContact());
		assertEquals(1, certificate.getRoots().size());
		assertEquals(exact.getLower(), certificate.getRoots().get(0)[0], 0);
		assertEquals(exact.getUpper(), certificate.getRoots().get(0)[1], 0);
	}

	@Test
	void anExactCurvatureThatDisagreesWithItsEnclosureCertifiesNothing() {
		RootCountCertificate2D certificate = RootCountCertificate2D.certify(
				withExact(exact(1)), CONTACT - 0.01, CONTACT + 0.01);

		assertEquals(RootCountCertificate2D.Form.NOT_CERTIFIED, certificate.getForm());
		assertTrue(certificate.getReason().contains("disagrees"), certificate.describe());
	}

	@Test
	void anExactParameterOutsideTheCriticalBracketIsNotUsed() {
		ExactContactEvidence2D elsewhere = new ExactContactEvidence2D("0", 0, 0,
				ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, ContactOrder.TWO, -1, "injected");
		RootCountCertificate2D certificate = RootCountCertificate2D.certify(
				withExact(elsewhere), CONTACT - 0.01, CONTACT + 0.01);

		assertEquals(RootCountCertificate2D.Form.NOT_CERTIFIED, certificate.getForm());
		assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE,
				certificate.getContact());
	}

	@Test
	void theDoubleRootWithdrawsTheNoiseAndReclassifiesTheContact() {
		IntersectionCandidate2D tangent = candidate(CONTACT, ContactClass.TANGENT_ESTABLISHED,
				LocalIsolationStatus.NOT_ESTABLISHED);
		IntersectionCandidate2D left = candidate(CONTACT - 3E-8,
				ContactClass.TRANSVERSE_ESTABLISHED, LocalIsolationStatus.ESTABLISHED);
		IntersectionCandidate2D right = candidate(CONTACT + 3E-8,
				ContactClass.TRANSVERSE_ESTABLISHED, LocalIsolationStatus.ESTABLISHED);

		CertifiedContactAnalysis2D.Outcome base = CertifiedContactAnalysis2D.apply(context,
				captured, List.of(left, tangent, right), true);
		CertifiedContactAnalysis2D.Outcome outcome = CertifiedContactAnalysis2D.apply(context,
				withExact(exact(-1)), List.of(left, tangent, right), true);

		assertEquals(3, base.getCandidates().size(), "without the exact decision nothing changes");
		assertEquals(1, outcome.getCandidates().size(), "V-CONF-5: one double root");
		IntersectionCandidate2D contact = outcome.getCandidates().get(0);
		assertDoubleRoot(contact, ContactOrder.TWO);
		assertEquals(CONTACT, contact.getSemanticParameter(), 0,
				"the parameter of the contact it replaces");
		assertEquals(3, count(outcome.getDiagnostics(),
				DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE), outcome.getDiagnostics()
						.toString());
		assertEquals(1, count(outcome.getDiagnostics(),
				DiagnosticCode.CERTIFIED_EXISTENCE_ESTABLISHED));
	}

	@Test
	void aTransverseClassificationThatMeetsTheDoubleRootIsReclassified() {
		IntersectionCandidate2D transverse = candidate(CONTACT,
				ContactClass.TRANSVERSE_ESTABLISHED, LocalIsolationStatus.ESTABLISHED);
		IntersectionCandidate2D undetermined = candidate(CONTACT + 1E-9,
				ContactClass.CONTACT_UNDETERMINED, LocalIsolationStatus.NOT_ESTABLISHED);

		CertifiedContactAnalysis2D.Outcome outcome = CertifiedContactAnalysis2D.apply(context,
				withExact(exact(-1)), List.of(transverse, undetermined), false);

		assertEquals(1, outcome.getCandidates().size(), outcome.getDiagnostics().toString());
		assertDoubleRoot(outcome.getCandidates().get(0), ContactOrder.TWO);
		boolean reclassified = false;
		for (IntersectionDiagnostic2D diagnostic : outcome.getDiagnostics()) {
			reclassified |= diagnostic.getCode()
					== DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE
					&& diagnostic.getMessage().startsWith("Candidate TRANSVERSE_ESTABLISHED")
					&& diagnostic.getMessage().contains("reclassified as the one double root");
		}
		assertTrue(reclassified, "the multiplicity contradiction of the transverse candidate: "
				+ outcome.getDiagnostics());
	}

	// ---------------------------------------------------------------- helpers

	private static ExactContactEvidence2D exact(int curvature) {
		return new ExactContactEvidence2D("pi/2", Math.PI / 2, Math.nextUp(Math.PI / 2),
				ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, ContactOrder.TWO, curvature,
				"injected exact contact");
	}

	/** The captured residual, declared constructive with one exact contact. */
	private ResidualJetSource2D withExact(ExactContactEvidence2D exact) {
		ResidualJetSource2D delegate = captured;
		return new ResidualJetSource2D() {
			@Override
			public String getConfiguration() {
				return "test/exact-contact";
			}

			@Override
			public boolean isConstructive() {
				return true;
			}

			@Override
			public ResidualJetEnclosure2D enclose(double lower, double upper) {
				return delegate.enclose(lower, upper);
			}

			@Override
			public double[] normalizedResidual(double parameter) {
				return delegate.normalizedResidual(parameter);
			}

			@Override
			public List<ExactContactEvidence2D> exactContacts(double lower, double upper) {
				return lower <= exact.getLower() && exact.getUpper() <= upper
						? List.of(exact) : Collections.emptyList();
			}
		};
	}

	private static void assertDoubleRoot(IntersectionCandidate2D contact, ContactOrder order) {
		assertEquals(ContactClass.TANGENT_ESTABLISHED, contact.getContactClass());
		assertEquals(MultiplicityStatus.ESTABLISHED, contact.getMultiplicityStatus());
		assertEquals(OptionalInt.of(2), contact.getEstablishedMultiplicity());
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED, contact.getLocalIsolationStatus(),
				"a contact gets no transverse germ");
		IntersectionExistenceEvidence2D evidence = contact.getExistenceEvidence();
		assertEquals(ExistenceGrade.EXISTENCE_EXACT, evidence.getGrade());
		assertEquals(ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, evidence.getContactEvidence());
		assertEquals(order, evidence.getContactOrder());
	}

	private static int count(List<IntersectionDiagnostic2D> diagnostics, DiagnosticCode code) {
		int count = 0;
		for (IntersectionDiagnostic2D diagnostic : diagnostics) {
			count += diagnostic.getCode() == code ? 1 : 0;
		}
		return count;
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

	private IntersectionCandidate2D candidate(double parameter, ContactClass contact,
			LocalIsolationStatus isolation) {
		String branch = context.getDefinition().getBranches().get(0).getBranchKey();
		return new IntersectionCandidate2D(branch,
				IntersectionCapabilityContext2D.componentKey(branch, 0), parameter,
				OptionalDouble.empty(), new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12), isolation, Optional.empty(), contact,
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.SAFEGUARDED_DERIVATIVE, NumericGuarantee.ESTIMATED_ERROR,
				LineageEventKind.APPEARED, new ArrayList<>(), Collections.emptyList());
	}
}

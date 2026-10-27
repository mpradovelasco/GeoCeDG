/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 {@code CU3-B1}, transition between a pair and a double root with certified
 * existence (validation matrix rows B1-2 and B1-3; CU3-B1 design section 4). On the folded
 * segment traced by {@code (x(N), 3)}, the pair at {@code x = 1.9999} and the double root at
 * {@code x = 2}, whose exact decision is declared by the source as in
 * {@code PreG9BCu3ExactDoubleRootTest}, are both anchored at the fold {@code (t->-t, 0)}: the two
 * forms carry the same anchor, hence the same germ, the same selector and the same token. Without
 * the exact decision the double root anchors nothing and a claimed token stays latent
 * ({@code PreG9BCu3FoldContactTest} b1x2to4).
 */
final class PreG9BCu3FoldAnchorTransitionTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void aPairAndACertifiedDoubleRootCarryTheSameAnchor() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("L=LocusV2(X0,N)");
		add("A=(1.9999,0)");
		add("g=Line(A,Line((0,0),(0,1)))");
		IntersectionCapabilityContext2D pairContext = context();
		double root = Math.acos(1.9999 / 2);
		List<IntersectionCandidate2D> pair = CertifiedContactAnalysis2D.apply(pairContext,
				constructive(pairContext, null), List.of(candidate(pairContext, -root,
						ContactClass.TRANSVERSE_ESTABLISHED), candidate(pairContext, root,
								ContactClass.TRANSVERSE_ESTABLISHED)), false).getCandidates();

		add("A=(2,0)");
		IntersectionCapabilityContext2D doubleContext = context();
		ExactContactEvidence2D exact = new ExactContactEvidence2D("0", 0, 0,
				ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, ContactOrder.FOLD_CONTACT, 1,
				"declared exact contact at the fold");
		List<IntersectionCandidate2D> contact = CertifiedContactAnalysis2D.apply(doubleContext,
				constructive(doubleContext, exact), List.of(candidate(doubleContext, 0,
						ContactClass.CONTACT_UNDETERMINED)), false).getCandidates();

		assertEquals(1, pair.size(), "the pair publishes one candidate");
		assertEquals(1, contact.size(), "the double root publishes one candidate");
		assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED,
				pair.get(0).getExistenceEvidence().getGrade());
		assertEquals(ExistenceGrade.EXISTENCE_EXACT,
				contact.get(0).getExistenceEvidence().getGrade());
		Optional<FoldContactAnchor2D> pairAnchor = FoldContactAnchor2D.fromMarker(
				pair.get(0).getContinuationKey());
		Optional<FoldContactAnchor2D> contactAnchor = FoldContactAnchor2D.fromMarker(
				contact.get(0).getContinuationKey());
		assertTrue(pairAnchor.isPresent() && contactAnchor.isPresent());
		assertEquals(pairAnchor.get().label(), contactAnchor.get().label(),
				"B1-2/B1-3: one anchor for both forms");
		assertEquals("0", contactAnchor.get().getFixedPoint());
		String lineage = "lineage";
		assertEquals(IntersectionRootDeterministicSelector2D.parse(
				IntersectionRootDeterministicSelector2D.of(lineage,
						pairAnchor.get().germ(lineage)).toExternalForm()),
				IntersectionRootDeterministicSelector2D.of(lineage,
						contactAnchor.get().germ(lineage)),
				"the same selector, so the ledger reuses the same token");
	}

	private IntersectionCapabilityContext2D context() {
		LocusDefinition2D definition = ((GeoLocusV2) lookup("L")).getSemanticDefinition();
		LocusIntersectionTarget2D target = LocusIntersectionTargets2D.capture(lookup("g"),
				"cu3b1-target", 1);
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor(),
				target.getResidualContract());
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D("cu3b1-pair",
				"cu3b1-lineage", definition.getLocusIdentity(),
				Math.max(1, definition.getSemanticRevision()), "cu3b1-target", 1,
				"cu3b1-topology", policy);
		return new IntersectionCapabilityContext2D(query, definition, target,
				LocusEvaluationSession2D.memoizing(64),
				new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
	}

	/** The captured residual, declared constructive, with an optional exact contact. */
	private static ResidualJetSource2D constructive(IntersectionCapabilityContext2D context,
			ExactContactEvidence2D exact) {
		ResidualJetSource2D delegate = IntersectionDecisionConfiguration2D.CAPTURED
				.residualJets(context.getDefinition(), context.getTarget());
		assertNotNull(delegate);
		return new ResidualJetSource2D() {
			@Override
			public String getConfiguration() {
				return "test/fold-anchor";
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
				return exact != null && lower <= exact.getLower() && exact.getUpper() <= upper
						? List.of(exact) : Collections.emptyList();
			}
		};
	}

	private static IntersectionCandidate2D candidate(IntersectionCapabilityContext2D context,
			double parameter, ContactClass contact) {
		String branch = context.getDefinition().getBranches().get(0).getBranchKey();
		return new IntersectionCandidate2D(branch,
				IntersectionCapabilityContext2D.componentKey(branch, 0), parameter,
				OptionalDouble.empty(), new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12), contact == ContactClass.TRANSVERSE_ESTABLISHED
								? LocalIsolationStatus.ESTABLISHED
								: LocalIsolationStatus.NOT_ESTABLISHED, Optional.empty(),
				contact, MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.SAFEGUARDED_DERIVATIVE, NumericGuarantee.ESTIMATED_ERROR,
				LineageEventKind.APPEARED, new ArrayList<>(), Collections.emptyList());
	}
}

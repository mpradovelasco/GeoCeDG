/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Diagnostic only: no changed admission, no mock evaluator, baseline-compatible APIs. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SGeneralLocusDiagnosticTest {

	@BeforeAll
	static void initialize() {
		G9U1SemanticPointInteractionTest.initializeDesktop();
	}

	@Test
	void generalCircleIsHitHasLocalCandidateAndExplicitForwardWorks() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = circle(app);
		var view = app.getEuclidianView1();
		var drawable = new DrawLocusV2(view, source);
		assertTrue(drawable.hit(view.toScreenCoordX(1), view.toScreenCoordY(0), 4));
		var result = query(source, LocusPointInteractionPolicy2D.initial(0.1));
		trace(source, result);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus());
		assertFalse(result.getSearchCoverage().establishesCompleteRequestedScope());
		assertEquals(1, result.getCandidates().size());
		assertTrue(result.getCandidates().get(0).getLocalEvidence().isEstablished());
		assertNull(result.getUniqueCandidate());
		String before = app.getXML();
		assertNull(new GeoCeDGPointInteraction(app).create(source, 1.02, 0, 0.1,
				unused -> { throw new AssertionError("Unresolved is not offerable"); }));
		assertEquals(before, app.getXML());
		String branch = source.getSemanticDefinition().getBranches().get(0).getBranchKey();
		GeoPoint forward = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
				"F=Point(L,\"" + branch + "\",0)");
		assertTrue(forward.isDefined());
		assertEquals(1, forward.getInhomX(), 1E-10);
		assertEquals(0, forward.getInhomY(), 1E-10);
	}

	@Test
	void exhaustedBudgetIsNotCertifiedAbsence() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = circle(app);
		var policy = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		var result = query(source, policy);
		trace(source, result);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus());
		assertTrue(result.getDiagnostic().contains("budget exhausted"));
		assertNotEquals(LocusPointInteractionStatus2D.NO_ADMISSIBLE_PREIMAGE,
				result.getStatus());
	}

	private static GeoLocusV2 circle(AppGeoCeDG app) {
		G9U1SemanticPointInteractionTest.eval(app, "k=Circle((0,0),1)");
		G9U1SemanticPointInteractionTest.eval(app, "K=Point(k)");
		G9U1SemanticPointInteractionTest.eval(app, "R=K+(0,0)");
		GeoLocusV2 source = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
				"L=LocusV2(R,K)");
		source.setEuclidianVisible(true);
		source.updateRepaint();
		return source;
	}

	private static LocusPointInteractionResult2D query(GeoLocusV2 source,
			LocusPointInteractionPolicy2D policy) {
		return new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, 1.02, 0, policy));
	}

	static void trace(GeoLocusV2 source, LocusPointInteractionResult2D result) {
		var definition = source.getSemanticDefinition();
		var domain = definition.getProvider().getDeclaredDomain();
		System.out.println("CU7-R1 provider=" + definition.getProvider().getProviderId()
				+ " evaluator=" + definition.getEvaluatorCapability().getClass().getName()
				+ " defined=" + source.isDefined() + " status="
				+ definition.getDefinitionStatus() + " domain=" + domain.getLower()
				+ "," + domain.getUpper() + " result=" + result.getStatus()
				+ " coverage=" + result.getSearchCoverage()
				+ " diagnostic=" + result.getDiagnostic());
		for (var branch : definition.getBranches()) {
			System.out.println("branch=" + branch.getBranchKey() + " components="
					+ branch.getExistenceStructure().getCompleteness());
			for (var component : branch.getExistenceStructure().getContinuousValidComponents()) {
				System.out.println("component evidence=" + component.getEvidenceKey()
						+ " lower=" + component.getInterval().getLower()
						+ " upper=" + component.getInterval().getUpper());
			}
		}
		for (var candidate : result.getCandidates()) {
			var address = candidate.getAddress();
			System.out.println("candidate branch=" + address.getBranchKey()
					+ " component=" + address.getComponentLineageKey() + " u="
					+ address.getCanonicalParameter() + " distance=" + candidate.getWorldDistance()
					+ " cell=" + candidate.getIntervalLower() + "," + candidate.getIntervalUpper()
					+ " guarantee=" + candidate.getNumericGuarantee() + " evidence="
					+ candidate.getLocalEvidence().getStatus() + " " + candidate.getDiagnostic());
		}
		System.out.println("work evaluations=" + result.getInstrumentation().getSemanticEvaluations()
				+ " refinements=" + result.getInstrumentation().getRefinementIterations());
	}
}

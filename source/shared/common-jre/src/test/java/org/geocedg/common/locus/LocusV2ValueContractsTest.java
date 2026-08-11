/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.locus.LocusValidationTolerance2D;
import org.geocedg.common.kernel.locus.StablePathDomainProvider2D;
import org.junit.jupiter.api.Test;

class LocusV2ValueContractsTest {
	private static final double EPS_DOMAIN = 1E-14;

	@Test
	void semanticAxesRemainIndependentlyTyped() {
		assertEquals(Arrays.asList(DefinitionStatus.VALID,
				DefinitionStatus.EMPTY_DOMAIN, DefinitionStatus.DRIVER_INVALID,
				DefinitionStatus.UNSUPPORTED),
				Arrays.asList(DefinitionStatus.values()));
		assertTrue(EnumSet.allOf(BranchProperty.class)
				.contains(BranchProperty.COLLAPSED_IMAGE));
		assertTrue(EnumSet.allOf(EvaluationStatus.class)
				.contains(EvaluationStatus.UNSUPPORTED_NONDETERMINISM));
		assertTrue(EnumSet.allOf(Regularity.class).contains(Regularity.UNKNOWN));
		assertTrue(EnumSet.allOf(LineageTransition.class)
				.contains(LineageTransition.MERGED));

		LocusQuality2D quality = LocusQuality2D.analyticDoubleSemantic();
		assertEquals(ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				quality.getConstructionFidelity());
		assertEquals(EvaluationMethod.ANALYTIC_EVALUATION,
				quality.getEvaluationMethod());
		assertEquals(RepresentationRole.SEMANTIC_RESULT,
				quality.getRepresentationRole());
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				quality.getNumericGuarantee());
		assertEquals(Determinism.UNSUPPORTED_NONDETERMINISM,
				Determinism.valueOf("UNSUPPORTED_NONDETERMINISM"));
	}

	@Test
	void explicitNumericProviderOwnsDomainAndCanonicalParameter() {
		ExplicitNumericDomainProvider2D finite = numeric(-2, 3, false);
		assertEquals("explicit-numeric-domain/v1", finite.getProviderId());
		assertEquals(-2, finite.canonicalize(-2), 0);
		assertTrue(finite.contains(3));
		assertFalse(finite.contains(3.1));

		ExplicitNumericDomainProvider2D periodic =
				new ExplicitNumericDomainProvider2D("angle-radians/v1",
						new LocusInterval2D(-Math.PI, Math.PI, true, false),
						Orientation.INCREASING, true, EPS_DOMAIN);
		assertEquals(-Math.PI, periodic.canonicalize(Math.PI), 0);
		assertEquals(periodic.canonicalize(-Math.PI),
				periodic.canonicalize(3 * Math.PI), 0);
		assertFalse(periodic.getSemanticSignature().contains("PathParameter"));
	}

	@Test
	void approvedPathProvidersExposeOnlyReviewedMappings() {
		StablePathDomainProvider2D segment = StablePathDomainProvider2D.segment(
				"segment-driver/v1", new LocusPoint2D(2, 3),
				new LocusPoint2D(6, 11), EPS_DOMAIN);
		assertEquals(new LocusPoint2D(4, 7), segment.evaluateDriverPoint(0.5));
		assertEquals(new LocusInterval2D(0, 1, true, true),
				segment.getDeclaredDomain());

		StablePathDomainProvider2D circle = StablePathDomainProvider2D.circle(
				"circle-angle-radians/v1", new LocusPoint2D(1, -1), 2,
				EPS_DOMAIN);
		assertEquals(new LocusPoint2D(3, -1), circle.evaluateDriverPoint(0));
		assertEquals(circle.evaluateDriverPoint(-Math.PI),
				circle.evaluateDriverPoint(Math.PI));

		StablePathDomainProvider2D ellipse = StablePathDomainProvider2D.ellipse(
				"ellipse-angle-radians/v1", new LocusPoint2D(1, 2),
				new LocusPoint2D(3, 0), new LocusPoint2D(0, 2), EPS_DOMAIN);
		assertEquals(new LocusPoint2D(4, 2), ellipse.evaluateDriverPoint(0));
		assertThrows(IllegalArgumentException.class,
				() -> StablePathDomainProvider2D.ellipse("degenerate/v1",
						new LocusPoint2D(0, 0), new LocusPoint2D(1, 0),
						new LocusPoint2D(2, 0), EPS_DOMAIN));
	}

	@Test
	void branchIdentityIsNotAValidComponentIdentity() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false);
		LocusBranch2D branch = new LocusBranch2D("fixture.sheet.main",
				provider.getDeclaredDomain(), Arrays.asList(
						new LocusInterval2D(-1, -0.25, true, true),
						new LocusInterval2D(0.25, 1, true, true)),
				Orientation.INCREASING, "topology-fixture/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());

		assertEquals("fixture.sheet.main", branch.getBranchKey());
		assertEquals(2, branch.getValidDomainComponents().size());
		assertFalse(branch.containsValidParameter(0, provider));
		assertTrue(branch.containsValidParameter(-0.5, provider));
		assertEquals(LineageTransition.UNCHANGED,
				branch.getLineage().getTransition());

		LocusLineage2D split = new LocusLineage2D(LineageTransition.SPLIT,
				Collections.singletonList("fixture.sheet.main"),
				Arrays.asList("fixture.sheet.upper", "fixture.sheet.lower"));
		assertEquals(2, split.getChildKeys().size());
	}

	@Test
	void validationEnvelopeIsLocalScaleBasedAndUncertified() {
		double unitScale = LocusValidationTolerance2D.evaluationEnvelope(1);
		assertEquals(Math.max(1E-12, 64 * Math.ulp(1.0)), unitScale, 0);
		assertEquals(unitScale,
				LocusValidationTolerance2D.evaluationEnvelope(0.25), 0);
		assertEquals(Math.max(1E-6, 64 * Math.ulp(1E6)),
				LocusValidationTolerance2D.evaluationEnvelope(1E6), 0);
		assertThrows(IllegalArgumentException.class,
				() -> LocusValidationTolerance2D.evaluationEnvelope(Double.NaN));
	}

	private static ExplicitNumericDomainProvider2D numeric(double lower,
			double upper, boolean periodic) {
		return new ExplicitNumericDomainProvider2D("fixture-parameter/v1",
				new LocusInterval2D(lower, upper, true, !periodic),
				Orientation.INCREASING, periodic, EPS_DOMAIN);
	}
}

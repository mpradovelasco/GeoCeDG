/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.LocalPairIsolationEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * Increment I4 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 6.5, point 8, and 8.6): the intersection of two
 * loci on conditioned regions. Each branch-frame side records its own origin, and the reuse
 * guard is required on each of them. Criterion 7e, on the real kernel.
 */
final class PostR1ConditionedPairIntersectionTest extends G9U0PublicSurfaceTestBase {

	/**
	 * Two loci outside the certified class. The first is {@code y = exp(x)} with a band at
	 * {@code x = 0.5}; the second is the horizontal line of height {@code h} with a band at
	 * {@code x = 0.3}. They meet at {@code x = ln h}, which is the parameter of the root on
	 * both.
	 */
	private GeoLocusIntersectionResult pair(double height) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),exp(x(P))+0*sqrt(abs(x(P)-0.5)-0.01))");
		GeoLocusV2 first = add("L=LocusV2(Q,P)");
		assertNotNull(first);
		add("h=" + height);
		add("s2=Segment((0,0),(1,0))");
		add("P2=Point(s2)");
		add("Q2=(x(P2)+0*sqrt(abs(x(P2)-0.3)-0.01),h+0*exp(x(P2)))");
		GeoLocusV2 second = add("L2=LocusV2(Q2,P2)");
		assertNotNull(second);
		assertEquals(2, first.getConditionedEvidence(BRANCH).getRegions().size());
		assertEquals(2, second.getConditionedEvidence(BRANCH).getRegions().size());
		GeoLocusIntersectionResult rich = add("v=Intersect(L,L2)");
		assertNotNull(rich);
		return rich;
	}

	private void height(double value) {
		GeoNumeric height = (GeoNumeric) lookup("h");
		height.setValue(value);
		height.updateCascade();
	}

	private static List<LocusIntersectionSolution2D> roots(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getFiniteSolutions();
	}

	private static LocusIntersectionSolution2D only(GeoLocusIntersectionResult rich) {
		assertEquals(1, roots(rich).size(), String.valueOf(
				rich.getIntersectionResult().getDiagnostics()));
		return roots(rich).get(0);
	}

	private static boolean has(LocusIntersectionSolution2D root, DiagnosticCode code) {
		for (IntersectionDiagnostic2D diagnostic : root.getDiagnostics()) {
			if (diagnostic.getCode() == code) {
				return true;
			}
		}
		return false;
	}

	private static List<String[]> entries(String state) {
		List<String[]> entries = new ArrayList<>();
		String[] fields = state.split("\\|", -1);
		if ("-".equals(fields[2])) {
			return entries;
		}
		String[] parts = fields[2].split("~", -1);
		for (int index = 5; index < parts.length; index++) {
			entries.add(parts[index].split(",", -1));
		}
		return entries;
	}

	private static String unhex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int index = 0; index < bytes.length; index++) {
			bytes[index] = (byte) Integer.parseInt(hex.substring(2 * index, 2 * index + 2),
					16);
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/** The two origins of the only pair entry of a ledger state. */
	private static double[] origins(String state) {
		List<String[]> entries = entries(state);
		assertEquals(1, entries.size(), state);
		String[] entry = entries.get(0);
		assertEquals("Q", entry[0]);
		assertEquals(11, entry.length);
		String selector = unhex(entry[7]);
		assertEquals(2, selector.split("conditioned-use/branch-frame/v1/", -1).length - 1,
				selector);
		assertFalse(selector.contains("conditioned-region"), selector);
		assertFalse(selector.contains("component-"), selector);
		String[] sides = unhex(entry[10]).split(";", -1);
		assertEquals(2, sides.length);
		double[] origins = new double[2];
		for (int side = 0; side < 2; side++) {
			origins[side] = Double.longBitsToDouble(Long.parseUnsignedLong(
					sides[side].split("/", -1)[0], 16));
		}
		return origins;
	}

	private boolean defined(String label) {
		GeoElement element = lookup(label);
		return element != null && element.isDefined();
	}

	/**
	 * Step C3 of the corrections after block 3B: the local isolation of a pair root on
	 * conditioned regions is numerical evidence of a regular Jacobian on a sampled
	 * rectangle. It is established, it is never certified, and it has a coverage value of
	 * its own, which a certified rectangle cannot carry and which it cannot borrow.
	 */
	@Test
	void theIsolationOfAConditionedPairRootIsEstablishedAndNeverCertified() {
		GeoLocusIntersectionResult rich = pair(1.2);
		LocalPairIsolationEvidence2D isolation = only(rich).getPairEvidence().orElseThrow()
				.getLocalIsolation();
		assertEquals(LocalIsolationStatus.ESTABLISHED, isolation.getStatus());
		assertEquals(PairIsolationMethod.SAFEGUARDED_REGULAR_JACOBIAN, isolation.getMethod());
		assertEquals(PairCoverageStatus.SAMPLED_RECTANGLE_UNDER_HYPOTHESES,
				isolation.getCoverage());
		assertEquals(PairUniquenessStatus.REGULAR_JACOBIAN_UNIQUE, isolation.getUniqueness());
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				isolation.getNumericGuarantee());
		assertFalse(isolation.isCertified());
		// Nothing is upgraded by it: the roots are never known to be all the roots.
		assertEquals(Completeness.NOT_ESTABLISHED, rich.getIntersectionResult()
				.getCompletenessEvidence().getCompleteness());
		assertTrue(has(only(rich), DiagnosticCode.CONDITIONED_REGION_ROOT));

		// The two vocabularies cannot be mixed in either direction.
		assertThrows(IllegalArgumentException.class, () -> new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED,
				PairIsolationMethod.SAFEGUARDED_REGULAR_JACOBIAN,
				PairCoverageStatus.EXHAUSTIVE_RECTANGLE,
				PairUniquenessStatus.REGULAR_JACOBIAN_UNIQUE,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, "a Jacobian is not exhaustive"));
		assertThrows(IllegalArgumentException.class, () -> new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED, PairIsolationMethod.CERTIFIED_RECTANGLE,
				PairCoverageStatus.SAMPLED_RECTANGLE_UNDER_HYPOTHESES,
				PairUniquenessStatus.CERTIFIED_UNIQUE,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, "a certificate is not sampled"));
		assertThrows(IllegalArgumentException.class, () -> new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED,
				PairIsolationMethod.SAFEGUARDED_REGULAR_JACOBIAN,
				PairCoverageStatus.SAMPLED_RECTANGLE_UNDER_HYPOTHESES,
				PairUniquenessStatus.REGULAR_JACOBIAN_UNIQUE,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, "a Jacobian certifies no bound"));
		LocalPairIsolationEvidence2D certified = new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED, PairIsolationMethod.CERTIFIED_RECTANGLE,
				PairCoverageStatus.EXHAUSTIVE_RECTANGLE, PairUniquenessStatus.CERTIFIED_UNIQUE,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, "a certified rectangle");
		assertTrue(certified.isCertified());
	}

	@Test
	void theGuardIsRequiredOnEachBranchFrameSide() {
		// Criterion 7e.
		GeoLocusIntersectionResult rich = pair(1.2);
		LocusIntersectionSolution2D root = only(rich);
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
				root.getIdentity().getIdentityStatus(), root.getDiagnostics().toString());
		assertTrue(has(root, DiagnosticCode.CONDITIONED_REGION_ROOT));
		assertEquals(Completeness.NOT_ESTABLISHED,
				rich.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		String token = root.getIdentity().getRootToken();
		String allocated = rich.getTokenLedgerState();
		assertTrue(allocated.startsWith("7|"), allocated);
		double[] origins = origins(allocated);
		assertEquals(Math.log(1.2), origins[0], 1e-6);
		assertEquals(Math.log(1.2), origins[1], 1e-6);
		add("R=Intersect(v,\"" + token + "\")");
		assertTrue(defined("R"));

		// Both sides inside the regions of their origins: the token stays current.
		height(1.3);
		root = only(rich);
		assertEquals(token, root.getIdentity().getRootToken());
		assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
				root.getIdentity().getIdentityStatus());
		assertTrue(has(root, DiagnosticCode.CONDITIONED_EVIDENCE_USED));
		assertTrue(defined("R"));
		assertEquals(origins[0], origins(rich.getTokenLedgerState())[0], 0);
		assertEquals(origins[1], origins(rich.getTokenLedgerState())[1], 0);

		// x = ln 1.45 = 0.37: the same region of the first source, another region of the
		// second. The guard fails on one side and withholds the token.
		height(1.45);
		root = only(rich);
		assertEquals(IdentityStatus.NOT_ESTABLISHED, root.getIdentity().getIdentityStatus());
		assertNotEquals(token, root.getIdentity().getRootToken());
		assertTrue(has(root, DiagnosticCode.BRANCH_FRAME_TOKEN_WITHHELD));
		assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(
				root.getIdentity().getRootToken()).isPresent());
		assertFalse(defined("R"));
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		assertEquals(allocated.split("\\|")[1], rich.getTokenLedgerState().split("\\|")[1]);

		// x = ln 1.9 = 0.64: another region of both sources.
		height(1.9);
		assertEquals(IdentityStatus.NOT_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertFalse(defined("R"));
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		assertEquals(origins[0], origins(rich.getTokenLedgerState())[0], 0);

		// Back in the regions of both origins.
		height(1.25);
		assertEquals(token, only(rich).getIdentity().getRootToken());
		assertTrue(defined("R"));
	}

	@Test
	void thePairBindingSurvivesSaveAndReopenInEveryState() {
		GeoLocusIntersectionResult rich = pair(1.2);
		String token = only(rich).getIdentity().getRootToken();
		add("R=Intersect(v,\"" + token + "\")");
		boolean[] expected = {true, false, false, true};
		double[] heights = {1.2, 1.45, 1.9, 1.3};
		for (int step = 0; step < heights.length; step++) {
			height(heights[step]);
			assertEquals(expected[step], defined("R"), "step " + step);
			String ledger = rich.getTokenLedgerState();
			String document = getApp().getXML();
			AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
			reopened.setXML(document, true);
			GeoLocusIntersectionResult loaded = (GeoLocusIntersectionResult) reopened
					.getKernel().lookupLabel("v");
			assertEquals(ledger, loaded.getTokenLedgerState(), "reopen " + step);
			assertEquals(expected[step], reopened.getKernel().lookupLabel("R").isDefined(),
					"reopen " + step);
			assertEquals(expected[step], token.equals(only(loaded).getIdentity()
					.getRootToken()), "reopen " + step);
			assertEquals(document, reopened.getXML(), "reopen " + step);
		}
	}

	@Test
	void underTheConstructiveAuthorityThePairRootIsPublishedAndNotAdmitted() {
		// Criterion 1a for the roots of two loci.
		GeoLocusIntersectionResult rich = pair(1.2);
		AuthorityPolicyAdoption.adopt(getConstruction());
		rich = (GeoLocusIntersectionResult) lookup("v");
		LocusIntersectionSolution2D root = only(rich);
		assertEquals(ConstructiveAdmission.NOT_ESTABLISHED, root.getRevisionEvidence()
				.getExistenceEvidence().getConstructiveAdmission());
		assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(
				root.getIdentity().getRootToken()).isPresent());
	}
}

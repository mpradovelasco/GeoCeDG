/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D;
import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.SemanticCurveExportAdapter2D;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.SourceResult;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * Increment I4 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 6.5, 6.6 and 8.5): the intersection of a locus with
 * a target on conditioned regions, the branch-frame selector, its ledger binding with an
 * origin and the reuse guard. Criteria 1a (root part), 1b, 2a to 2d and 7a to 7d, on the
 * real kernel.
 */
final class PostR1ConditionedIntersectionTest extends G9U0PublicSurfaceTestBase {
	private static final String SELECTOR_KIND = "conditioned-use/branch-frame-root/v1/";

	/** A locus outside the certified class: one conditioned region, the whole domain. */
	private GeoLocusV2 outsideClass(String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P)," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	/** Two regions, A below 0.49 and B above 0.51, separated by an absence. */
	private GeoLocusV2 twoRegions() {
		GeoLocusV2 locus = outsideClass("exp(x(P))+0*sqrt(abs(x(P)-0.5)-0.01)");
		assertEquals(2, locus.getConditionedEvidence(BRANCH).getRegions().size());
		return locus;
	}

	/**
	 * One region whose two holes, of half width 1e-4 at 0.3 and at 0.6, no node of the
	 * traversal meets: only a report reveals them.
	 */
	private GeoLocusV2 hiddenHoles() {
		GeoLocusV2 locus = outsideClass("exp(x(P))+0*sqrt((x(P)-0.3)^2-1E-8)"
				+ "+0*sqrt((x(P)-0.6)^2-1E-8)");
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		return locus;
	}

	/** The horizontal line of height {@code h} meets {@code y = exp(x)} at {@code ln h}. */
	private GeoLocusIntersectionResult horizontal(double height) {
		add("h=" + height);
		add("g=Line((0,h),(1,h))");
		GeoLocusIntersectionResult rich = add("v=Intersect(L,g)");
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

	private static boolean has(LocusIntersectionResult2D result, DiagnosticCode code) {
		for (IntersectionDiagnostic2D diagnostic : result.getDiagnostics()) {
			if (diagnostic.getCode() == code) {
				return true;
			}
		}
		return false;
	}

	private static boolean durable(LocusIntersectionSolution2D root) {
		IdentityStatus status = root.getIdentity().getIdentityStatus();
		return status == IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
				|| status == IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
				|| status == IdentityStatus.CONTINUATION_ESTABLISHED;
	}

	/** The entries of the current snapshot of a ledger state, as their decoded fields. */
	private static List<String[]> entries(String state) {
		List<String[]> entries = new ArrayList<>();
		if (state == null) {
			return entries;
		}
		String[] fields = state.split("\\|", -1);
		if ("-".equals(fields[2])) {
			return entries;
		}
		String[] parts = fields[2].split("~", -1);
		for (int index = 5; index < parts.length; index++) {
			entries.add(parts[index].split(",", -1));
		}
		assertEquals(Integer.parseInt(parts[4]), entries.size());
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

	/** The origin of the only branch-frame entry of a ledger state. */
	private static double origin(String state) {
		List<String[]> entries = entries(state);
		assertEquals(1, entries.size(), state);
		String[] entry = entries.get(0);
		assertEquals("B", entry[0]);
		assertEquals(12, entry.length);
		assertTrue(unhex(entry[10]).startsWith(SELECTOR_KIND), unhex(entry[10]));
		String[] origin = unhex(entry[11]).split("/", -1);
		assertEquals(3, origin.length);
		return Double.longBitsToDouble(Long.parseUnsignedLong(origin[0], 16));
	}

	private boolean defined(String label) {
		GeoElement element = lookup(label);
		return element != null && element.isDefined();
	}

	/**
	 * Publishes, for the locus {@code y = exp(x)}, a certificate of the component
	 * {@code [0, 0.4]} only: the rest of the domain stays unresolved, and the producer
	 * publishes its conditioned region there.
	 */
	private GeoLocusV2 partlyCertified() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		LocusDefinition2D published = locus.getSemanticDefinition();
		LocusBranch2D branch = published.getBranch(BRANCH);
		LocusInterval2D domain = branch.getDeclaredDriverDomain();
		LocusInterval2D certified = new LocusInterval2D(domain.getLower(), 0.4, true, true);
		LocusBoundary2D lower = new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
				LocusBoundarySource2D.declaredLower(), true, LocusEvidenceNature2D.EXACT);
		LocusBoundary2D upper = new LocusBoundary2D(
				LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
				new LocusBoundarySource2D("band", "unresolved", "upper"), true,
				LocusEvidenceNature2D.NONE);
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(BRANCH, domain,
				branch.getExistenceStructure().getTopology(), 0,
				List.of(new Coverage(certified, CoverageState.CERTIFIED_EXISTS,
						LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE,
						LocusEvidenceNature2D.EXACT, "fixture-exists"),
						new Coverage(new LocusInterval2D(0.4, domain.getUpper(), false,
								domain.isUpperClosed()), CoverageState.UNRESOLVED,
								LocusCoverageCause2D.NOT_CERTIFIED, LocusEvidenceNature2D.NONE,
								"fixture-unresolved")),
				List.of(ContinuousComponent.interval(certified, lower, upper,
						Maximality.LOCAL_CERTIFIED_SUBINTERVAL, LocusEvidenceNature2D.EXACT,
						"fixture/0")),
				Collections.emptyList(),
				LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,
				"fixture-partial-certificate/v1");
		locus.publishSemanticDefinition(new LocusDefinition2D(published.getLocusIdentity(),
				published.getSemanticRevision() + 1, published.getDefinitionStatus(),
				published.getProvider(), List.of(new LocusBranch2D(branch.getBranchKey(),
						domain, structure, branch.getOrientation(), branch.getProvenance(),
						branch.getLineage(), branch.getProperties(), branch.getQuality())),
				published.getEvaluatorCapability(), published.getDeterminism(),
				"fixture-partial|" + structure.getCertifierContract(),
				locus.getInstrumentation()));
		List<LocusInterval2D> regions = locus.getConditionedEvidence(BRANCH).getRegions();
		assertEquals(1, regions.size());
		assertTrue(regions.get(0).getLower() > 0.4 && regions.get(0).getLower() < 0.41);
		return locus;
	}

	@Test
	void aRootThatPassesBetweenACertifiedComponentAndASpanDoesNotCarryItsToken() {
		// Criterion 2c, second part, and section 6.5, point 7.
		partlyCertified();
		GeoLocusIntersectionResult rich = horizontal(1.2);
		LocusIntersectionSolution2D onComponent = only(rich);
		String first = onComponent.getIdentity().getRootToken();
		assertTrue(durable(onComponent));
		assertFalse(has(onComponent, DiagnosticCode.CONDITIONED_REGION_ROOT));
		// A root of a certified component keeps the selector and the ledger of the base.
		String certified = rich.getTokenLedgerState();
		assertTrue(certified.startsWith("4|"), certified);
		assertTrue(unhex(entries(certified).get(0)[9]).startsWith(
				"g9u0-r4/deterministic-current-root/v1/"));
		add("R1=Intersect(v,\"" + first + "\")");
		assertTrue(defined("R1"));

		// The root passes to the uncertified part of its branch: another selector.
		height(1.8);
		LocusIntersectionSolution2D onSpan = only(rich);
		String second = onSpan.getIdentity().getRootToken();
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
				onSpan.getIdentity().getIdentityStatus());
		assertNotEquals(first, second);
		assertTrue(has(onSpan, DiagnosticCode.CONDITIONED_REGION_ROOT));
		assertFalse(defined("R1"));
		add("R2=Intersect(v,\"" + second + "\")");
		assertTrue(defined("R2"));
		List<String[]> both = entries(rich.getTokenLedgerState());
		assertEquals(2, both.size());
		String[] branchFrame = "B".equals(both.get(0)[0]) ? both.get(0) : both.get(1);
		String[] component = "B".equals(both.get(0)[0]) ? both.get(1) : both.get(0);
		assertEquals("S", component[0]);
		// The entry of the certified root keeps its text in the new ledger version.
		assertEquals(String.join(",", entries(certified).get(0)).replaceFirst("^c,", "d,")
				.replaceFirst("^a,", "d,"), String.join(",", java.util.Arrays.copyOfRange(
						component, 1, component.length)).replaceFirst("^c,", "d,"));
		final String originText = branchFrame[11];

		// The state returns: the earlier token is current again, and the branch-frame
		// binding keeps its origin through the passage.
		height(1.25);
		assertEquals(first, only(rich).getIdentity().getRootToken());
		assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertTrue(defined("R1"));
		assertFalse(defined("R2"));
		height(1.9);
		assertEquals(second, only(rich).getIdentity().getRootToken());
		assertTrue(defined("R2"));
		assertFalse(defined("R1"));
		for (String[] entry : entries(rich.getTokenLedgerState())) {
			if ("B".equals(entry[0])) {
				assertEquals(originText, entry[11]);
			}
		}
	}

	private static SourceResult export(GeoLocusV2 locus) {
		GeometryExportRequest request = GeometryExportRequest.builder(0.01)
				.allowedGuarantees(EnumSet.of(ApproximationEvidence.Guarantee
						.ESTIMATED_ERROR)).build();
		return new SemanticCurveExportAdapter2D().adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "i4:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
	}

	@Test
	void aSemanticExportOmitsConditionedRegionsAndDeclaresThem() {
		// Section 10, point 2: the omission is declared with its regions and its bands.
		SourceResult conditioned = export(twoRegions());
		assertEquals(1, conditioned.getComponents().size());
		Component declared = conditioned.getComponents().get(0);
		assertFalse(declared.isEmitted());
		assertEquals(SourceExportOutcome.Reason.EXISTENCE_NOT_ESTABLISHED,
				declared.getOutcome().getReason());
		String message = declared.getOutcome().getMessage();
		assertEquals(2, message.split("CONDITIONED_REGION", -1).length - 1, message);
		assertTrue(message.contains("ABSENT_BAND"), message);
		assertTrue(message.contains("TRANSITION_BAND"), message);
		assertEquals(message, declared.getDiagnostic().getMessage());
	}

	@Test
	void aSemanticExportKeepsTheCertifiedComponentsBesideTheDeclaration() {
		SourceResult mixed = export(partlyCertified());
		int emitted = 0;
		int declared = 0;
		for (Component component : mixed.getComponents()) {
			if (component.isEmitted()) {
				emitted++;
			} else {
				declared++;
				assertEquals(SourceExportOutcome.Reason.EXISTENCE_NOT_ESTABLISHED,
						component.getOutcome().getReason());
				assertEquals(1, component.getOutcome().getMessage()
						.split("CONDITIONED_REGION", -1).length - 1);
			}
		}
		// The certified component is exported as before; the region is declared.
		assertEquals(1, emitted);
		assertEquals(1, declared);
	}

	@Test
	void aRootAtTheSeamOfAPeriodicRegionIsOneRoot() {
		// A closed curve outside the certified class: one region, the whole period. The ray
		// meets it at the seam and at the opposite parameter.
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("kE=Circle(E,0.5)");
		add("K=Point(kE)");
		add("QF=(1,0)");
		add("M=Midpoint(K,QF)");
		GeoLocusV2 locus = add("L=LocusV2(M,E)");
		assertNotNull(locus);
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoLocusIntersectionResult rich = add("v=Intersect(L,r)");
		getKernel().updateConstruction(false);
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		assertEquals(2, roots(rich).size());
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertTrue(durable(root), root.getDiagnostics().toString());
			tokens.add(root.getIdentity().getRootToken());
		}
		assertEquals(-Math.PI, roots(rich).get(0).getRevisionEvidence()
				.getSemanticParameter(), 1e-9);
		assertNotEquals(tokens.get(0), tokens.get(1));
		// The curve changes and both roots leave the seam inside the same region: each
		// keeps the token of its selector.
		org.geogebra.common.kernel.geos.GeoPoint onPath =
				(org.geogebra.common.kernel.geos.GeoPoint) lookup("K");
		org.geogebra.common.kernel.geos.GeoPoint centre =
				(org.geogebra.common.kernel.geos.GeoPoint) lookup("E");
		double parameter = onPath.getPathParameter().getT();
		onPath.setCoords(centre.getInhomX() + 0.5 * Math.cos(parameter + 0.7),
				centre.getInhomY() + 0.5 * Math.sin(parameter + 0.7), 1.0);
		onPath.updateCascade();
		assertEquals(2, roots(rich).size());
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
					root.getIdentity().getIdentityStatus());
			assertTrue(tokens.contains(root.getIdentity().getRootToken()));
		}
		assertEquals(2, entries(rich.getTokenLedgerState()).size());
	}

	@Test
	void aRootOnAConditionedRegionIsPublishedDeclaredAndNeverComplete() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		GeoLocusIntersectionResult rich = horizontal(1.5);
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		LocusIntersectionSolution2D root = only(rich);
		assertEquals(Math.log(1.5), root.getRevisionEvidence().getSemanticParameter(), 1e-9);
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(SupportLevel.VERIFIED_UNCERTIFIED, result.getSupportLevel());
		// The roots found on a conditioned source are never known to be all its roots.
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertTrue(has(root, DiagnosticCode.CONDITIONED_REGION_ROOT));
		String declared = root.getDiagnostics().stream().filter(diagnostic ->
				diagnostic.getCode() == DiagnosticCode.CONDITIONED_REGION_ROOT).findFirst()
				.get().toString();
		// The result records the content identity that it read (section 7, point 6).
		assertTrue(declared.contains(evidence.getContentIdentity()), declared);
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
				root.getIdentity().getIdentityStatus());
		// The selector has its own versioned kind and names the branch and the germ only.
		String state = rich.getTokenLedgerState();
		assertTrue(state.startsWith("7|"), state);
		String selector = unhex(entries(state).get(0)[10]);
		assertTrue(selector.startsWith(SELECTOR_KIND), selector);
		assertTrue(selector.contains("conditioned-use/branch-frame/v1/"), selector);
		assertFalse(selector.contains("conditioned-region"), selector);
		assertFalse(selector.contains("component"), selector);
		assertEquals(Math.log(1.5), origin(state), 1e-9);
		GeoElement point = add("R=Intersect(v,\"" + root.getIdentity().getRootToken() + "\")");
		assertTrue(point.isDefined());

		// No root: unresolved, never empty.
		height(5);
		result = rich.getIntersectionResult();
		assertTrue(result.getFiniteSolutions().isEmpty());
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertFalse(defined("R"));
	}

	@Test
	void twoRootsOfOppositeGermOnOneRegionReceiveTwoTokens() {
		// Criterion 2a. The curve falls to its minimum at ln 2.2 and rises again.
		GeoLocusV2 locus = outsideClass("exp(x(P))-2.2*x(P)");
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		getApp().setUndoActive(true);
		GeoLocusIntersectionResult rich = horizontal(0.5);
		assertEquals(2, roots(rich).size());
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
					root.getIdentity().getIdentityStatus());
			tokens.add(root.getIdentity().getRootToken());
		}
		assertNotEquals(tokens.get(0), tokens.get(1));
		assertEquals(2, entries(rich.getTokenLedgerState()).size());
		assertNotEquals(unhex(entries(rich.getTokenLedgerState()).get(0)[10]),
				unhex(entries(rich.getTokenLedgerState()).get(1)[10]));
		add("R1=Intersect(v,\"" + tokens.get(0) + "\")");
		add("R2=Intersect(v,\"" + tokens.get(1) + "\")");
		assertTrue(defined("R1") && defined("R2"));
		getApp().storeUndoInfo();
		String first = rich.getTokenLedgerState();

		height(0.49);
		rich = (GeoLocusIntersectionResult) lookup("v");
		assertEquals(2, roots(rich).size());
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
					root.getIdentity().getIdentityStatus());
			assertTrue(tokens.contains(root.getIdentity().getRootToken()));
		}
		getApp().storeUndoInfo();
		String second = rich.getTokenLedgerState();

		// Undo and redo restore the bindings; save and reopen read them back exactly.
		getKernel().undo();
		assertEquals(first, ((GeoLocusIntersectionResult) lookup("v")).getTokenLedgerState());
		assertTrue(defined("R1") && defined("R2"));
		getKernel().redo();
		assertEquals(second, ((GeoLocusIntersectionResult) lookup("v")).getTokenLedgerState());
		assertTrue(defined("R1") && defined("R2"));
		String document = getApp().getXML();
		assertFalse(document.contains("conditioned-region"));
		AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reopened.setXML(document, true);
		GeoLocusIntersectionResult loaded = (GeoLocusIntersectionResult) reopened.getKernel()
				.lookupLabel("v");
		assertEquals(second, loaded.getTokenLedgerState());
		List<String> reloaded = new ArrayList<>();
		for (LocusIntersectionSolution2D root : roots(loaded)) {
			reloaded.add(root.getIdentity().getRootToken());
		}
		assertEquals(new java.util.TreeSet<>(tokens), new java.util.TreeSet<>(reloaded));
		assertTrue(reopened.getKernel().lookupLabel("R1").isDefined());
		assertTrue(reopened.getKernel().lookupLabel("R2").isDefined());
		assertEquals(document, reopened.getXML());
	}

	@Test
	void rootsThatShareABranchFrameSelectorKeepRevisionLocalHandles() {
		// Criterion 2c, first part: no rank on a conditioned region.
		GeoLocusV2 locus = outsideClass("sin(12*x(P))+0*exp(x(P))");
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		GeoLocusIntersectionResult rich = horizontal(0.5);
		assertEquals(4, roots(rich).size());
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
					root.getIdentity().getIdentityStatus());
			assertTrue(root.getIdentity().getRootToken().startsWith("locus-root-local/"));
			assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(
					root.getIdentity().getRootToken()).isPresent());
		}
		assertTrue(entries(rich.getTokenLedgerState()).isEmpty());
		// At this height one root rises and two fall: the rising one is the only root of
		// its selector and receives its token; the two falling ones stay ambiguous.
		height(-0.2);
		assertEquals(3, roots(rich).size());
		String token = null;
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertTrue(root.getRevisionEvidence().getCurrentRootGerm().isPresent());
			if (durable(root)) {
				assertEquals(null, token);
				token = root.getIdentity().getRootToken();
				assertEquals((2 * Math.PI - Math.asin(0.2)) / 12,
						root.getRevisionEvidence().getSemanticParameter(), 1e-6);
			} else {
				assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
						root.getIdentity().getIdentityStatus());
			}
		}
		assertNotNull(token);
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		// A published root whose germ is not established leaves no selector of its branch
		// known to be unique: every token of the frame is withheld, and none is retired.
		height(-0.3);
		assertEquals(3, roots(rich).size());
		boolean unselected = false;
		for (LocusIntersectionSolution2D root : roots(rich)) {
			unselected |= !root.getRevisionEvidence().getCurrentRootGerm().isPresent();
			assertFalse(durable(root));
		}
		assertTrue(unselected);
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		assertEquals("w", entries(rich.getTokenLedgerState()).get(0)[1]);
		height(-0.1);
		int held = 0;
		for (LocusIntersectionSolution2D root : roots(rich)) {
			held += token.equals(root.getIdentity().getRootToken()) ? 1 : 0;
		}
		assertEquals(1, held);
	}

	@Test
	void theTokenIsWithheldWhenTheRootOfItsSelectorAppearsInAnotherRegion() {
		// Criteria 7a and 7b.
		twoRegions();
		GeoLocusIntersectionResult rich = horizontal(1.3);
		String token = only(rich).getIdentity().getRootToken();
		add("R=Intersect(v,\"" + token + "\")");
		assertTrue(defined("R"));
		String allocated = rich.getTokenLedgerState();
		double origin = origin(allocated);
		assertEquals(Math.log(1.3), origin, 1e-9);

		// 7b: the root moves inside the region of the origin and keeps its token.
		for (double value : new double[] {1.35, 1.5, 1.6, 1.05}) {
			height(value);
			LocusIntersectionSolution2D root = only(rich);
			assertEquals(token, root.getIdentity().getRootToken());
			assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
					root.getIdentity().getIdentityStatus());
			assertTrue(has(root, DiagnosticCode.CONDITIONED_EVIDENCE_USED));
			assertTrue(defined("R"));
			// The origin is never updated by a later resolution.
			assertEquals(origin, origin(rich.getTokenLedgerState()), 0);
		}

		// 7a, in one update: the root of A disappears and one of the same germ appears on B.
		height(2.0);
		LocusIntersectionSolution2D other = only(rich);
		assertEquals(Math.log(2.0), other.getRevisionEvidence().getSemanticParameter(), 1e-9);
		assertEquals(IdentityStatus.NOT_ESTABLISHED, other.getIdentity().getIdentityStatus());
		assertNotEquals(token, other.getIdentity().getRootToken());
		assertTrue(other.getIdentity().getRootToken().startsWith("locus-root-local/"));
		assertTrue(has(other, DiagnosticCode.BRANCH_FRAME_TOKEN_WITHHELD));
		assertTrue(has(rich.getIntersectionResult(),
				DiagnosticCode.BRANCH_FRAME_TOKEN_WITHHELD));
		assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(
				other.getIdentity().getRootToken()).isPresent());
		assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(token)
				.isPresent());
		assertFalse(defined("R"));
		// One entry, with its selector, its origin and its token: nothing was allocated.
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		assertEquals(origin, origin(rich.getTokenLedgerState()), 0);
		assertEquals(allocated.split("\\|")[1], rich.getTokenLedgerState().split("\\|")[1]);
		height(2.3);
		assertEquals(IdentityStatus.NOT_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertFalse(defined("R"));

		// The target returns to A: the point is defined at the root on A.
		height(1.3);
		assertEquals(token, only(rich).getIdentity().getRootToken());
		assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertTrue(defined("R"));

		// 7a, through a state without roots (the line passes through the band).
		height(1.65);
		assertTrue(roots(rich).isEmpty());
		assertFalse(defined("R"));
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		height(2.0);
		assertEquals(IdentityStatus.NOT_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertFalse(defined("R"));
		assertEquals(1, entries(rich.getTokenLedgerState()).size());
		assertEquals(origin, origin(rich.getTokenLedgerState()), 0);
		height(1.2);
		assertEquals(token, only(rich).getIdentity().getRootToken());
		assertTrue(defined("R"));
	}

	@Test
	void anUnclaimedBindingIsWithheldAndNotReplaced() {
		// Section 6.5, point 6, without a dependent point: the binding is kept while its
		// selector has a current root, and no other token is allocated for it.
		twoRegions();
		GeoLocusIntersectionResult rich = horizontal(1.3);
		String token = only(rich).getIdentity().getRootToken();
		String high = rich.getTokenLedgerState().split("\\|")[1];
		for (int repeat = 0; repeat < 3; repeat++) {
			height(2.0 + 0.1 * repeat);
			assertEquals(IdentityStatus.NOT_ESTABLISHED,
					only(rich).getIdentity().getIdentityStatus());
			assertEquals(1, entries(rich.getTokenLedgerState()).size());
			assertEquals("w", entries(rich.getTokenLedgerState()).get(0)[1]);
			assertEquals(high, rich.getTokenLedgerState().split("\\|")[1]);
		}
		height(1.4);
		assertEquals(token, only(rich).getIdentity().getRootToken());
	}

	@Test
	void historiesRestoreTheBindingAndTheDefinednessOfThePoint() {
		// Criterion 7d: undo and redo, and save and reopen, in every state of 7a.
		twoRegions();
		getApp().setUndoActive(true);
		GeoLocusIntersectionResult rich = horizontal(1.3);
		String token = only(rich).getIdentity().getRootToken();
		add("R=Intersect(v,\"" + token + "\")");
		getApp().storeUndoInfo();
		List<String> ledgers = new ArrayList<>();
		List<Boolean> definedness = new ArrayList<>();
		List<String> documents = new ArrayList<>();
		double[] heights = {1.3, 2.0, 1.65, 2.1, 1.4};
		boolean[] expected = {true, false, false, false, true};
		for (int step = 0; step < heights.length; step++) {
			height(heights[step]);
			getApp().storeUndoInfo();
			rich = (GeoLocusIntersectionResult) lookup("v");
			ledgers.add(rich.getTokenLedgerState());
			definedness.add(defined("R"));
			documents.add(getApp().getXML());
			assertEquals(expected[step], defined("R"), "step " + step);
			assertEquals(1, entries(rich.getTokenLedgerState()).size());
		}
		for (int step = heights.length - 2; step >= 0; step--) {
			getKernel().undo();
			assertEquals(ledgers.get(step),
					((GeoLocusIntersectionResult) lookup("v")).getTokenLedgerState(),
					"undo to " + step);
			assertEquals(definedness.get(step), defined("R"), "undo to " + step);
		}
		for (int step = 1; step < heights.length; step++) {
			getKernel().redo();
			assertEquals(ledgers.get(step),
					((GeoLocusIntersectionResult) lookup("v")).getTokenLedgerState(),
					"redo to " + step);
			assertEquals(definedness.get(step), defined("R"), "redo to " + step);
		}
		for (int step = 0; step < heights.length; step++) {
			AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
			reopened.setXML(documents.get(step), true);
			GeoLocusIntersectionResult loaded = (GeoLocusIntersectionResult) reopened
					.getKernel().lookupLabel("v");
			assertEquals(ledgers.get(step), loaded.getTokenLedgerState(), "reopen " + step);
			assertEquals(definedness.get(step),
					reopened.getKernel().lookupLabel("R").isDefined(), "reopen " + step);
			for (LocusIntersectionSolution2D root : roots(loaded)) {
				// In no history does a root hold a token allocated to a root of another
				// region.
				assertEquals(expected[step], token.equals(root.getIdentity().getRootToken()));
			}
			assertEquals(documents.get(step), reopened.getXML(), "reopen " + step);
		}
	}

	@Test
	void aDirectHistoryRecordsItsOwnOriginWhichIsWithheldInTheOtherRegion() {
		// Criterion 7d, second part: a document built directly in the second state.
		twoRegions();
		GeoLocusIntersectionResult rich = horizontal(2.0);
		LocusIntersectionSolution2D root = only(rich);
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
				root.getIdentity().getIdentityStatus());
		String token = root.getIdentity().getRootToken();
		assertEquals(Math.log(2.0), origin(rich.getTokenLedgerState()), 1e-9);
		add("R=Intersect(v,\"" + token + "\")");
		assertTrue(defined("R"));
		height(1.3);
		assertEquals(IdentityStatus.NOT_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
		assertFalse(defined("R"));
		assertEquals(Math.log(2.0), origin(rich.getTokenLedgerState()), 1e-9);
		height(2.2);
		assertEquals(token, only(rich).getIdentity().getRootToken());
		assertTrue(defined("R"));
	}

	/**
	 * The scenario of criterion 7c: a token allocated at ln 1.2, a root carried to ln 1.5,
	 * and the two hidden holes confirmed in an order.
	 *
	 * @return for each step, whether the root held the token; a root never holds another
	 */
	private List<Boolean> scenario(int initialDepth, double firstReport,
			double secondReport) {
		GeoLocusV2 locus = hiddenHoles();
		if (initialDepth != 5) {
			((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
					.useConditionedTraversalPolicy(new LocusConditionedTraversalPolicy2D(
							"conditioned-traversal/test-depth-" + initialDepth, initialDepth,
							20, 1e-3, 0.35, 1e-3, 8192));
			assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		}
		GeoLocusIntersectionResult rich = horizontal(1.2);
		String token = only(rich).getIdentity().getRootToken();
		String selector = unhex(entries(rich.getTokenLedgerState()).get(0)[10]);
		add("R=Intersect(v,\"" + token + "\")");
		List<Boolean> held = new ArrayList<>();
		Runnable observe = () -> {
			LocusIntersectionSolution2D root = only(rich);
			boolean holds = token.equals(root.getIdentity().getRootToken());
			// Each root holds the single token of its selector or none.
			assertEquals(holds, durable(root));
			assertEquals(holds, defined("R"));
			// No second entry of the selector exists in any history.
			assertEquals(1, entries(rich.getTokenLedgerState()).size());
			assertEquals(selector, unhex(entries(rich.getTokenLedgerState()).get(0)[10]));
			assertEquals(Math.log(1.2), origin(rich.getTokenLedgerState()), 1e-9);
			held.add(holds);
		};
		observe.run();
		height(1.5);
		observe.run();
		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, firstReport));
		observe.run();
		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, secondReport));
		observe.run();
		height(2.0);
		observe.run();
		height(1.25);
		observe.run();
		return held;
	}

	@Test
	void aBandConfirmedBetweenTheOriginAndTheRootWithholdsTheToken() {
		// Criterion 7c: the origin is at 0.18 and the root at 0.41; the hole at 0.3 lies
		// between them and the hole at 0.6 does not.
		assertEquals(List.of(true, true, false, false, false, true), scenario(5, 0.3, 0.6));
	}

	@Test
	void theOrderOfTheConfirmationsNeverGivesAnotherToken() {
		assertEquals(List.of(true, true, true, false, false, true), scenario(5, 0.6, 0.3));
	}

	@Test
	void anotherPolicyDepthGivesTheSameSelectorsAndTheSameTokens() {
		// Criteria 2b and 7c.
		assertEquals(List.of(true, true, false, false, false, true), scenario(3, 0.3, 0.6));
	}

	@Test
	void aDeeperPolicyGivesTheSameSelectorsAndTheSameTokens() {
		assertEquals(List.of(true, true, true, false, false, true), scenario(7, 0.6, 0.3));
	}

	@Test
	void aNewTraversalOfTheSameStateKeepsEveryBinding() {
		// Criterion 2b: the same construction traversed with another depth, in place.
		GeoLocusV2 locus = twoRegions();
		GeoLocusIntersectionResult rich = horizontal(1.3);
		String token = only(rich).getIdentity().getRootToken();
		String state = rich.getTokenLedgerState();
		String content = locus.getConditionedEvidence(BRANCH).getContentIdentity();
		((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.useConditionedTraversalPolicy(new LocusConditionedTraversalPolicy2D(
						"conditioned-traversal/test-depth-7", 7, 20, 1e-3, 0.35, 1e-3, 8192));
		assertNotEquals(content, locus.getConditionedEvidence(BRANCH).getContentIdentity());
		height(1.3);
		assertEquals(token, only(rich).getIdentity().getRootToken());
		assertEquals(state.replace(",a,", ",c,"),
				rich.getTokenLedgerState().replace(",a,", ",c,"));
		height(2.0);
		assertEquals(IdentityStatus.NOT_ESTABLISHED,
				only(rich).getIdentity().getIdentityStatus());
	}

	@Test
	void underTheConstructiveAuthorityTheRootIsPublishedAndNotAdmitted() {
		// Criteria 1a (root part) and 1b: no production path builds RELAXED from
		// conditioned evidence.
		outsideClass("exp(x(P))");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusIntersectionResult rich = horizontal(1.5);
		LocusIntersectionSolution2D root = only(rich);
		assertTrue(has(root, DiagnosticCode.CONDITIONED_REGION_ROOT));
		assertEquals(ConstructiveAdmission.NOT_ESTABLISHED, root.getRevisionEvidence()
				.getExistenceEvidence().getConstructiveAdmission());
		assertFalse(rich.getIntersectionResult().findPointAdmissibleSolution(
				root.getIdentity().getRootToken()).isPresent());
		GeoElement point = add("R=Intersect(v,\"" + root.getIdentity().getRootToken()
				+ "\")");
		assertTrue(point == null || !point.isDefined());
	}

	@Test
	void theRootsOfACertifiedComponentKeepTheirSelectorsAndTheirLedgerText() {
		// Criterion 2d: a source without conditioned regions is searched as before.
		GeoLocusV2 locus = certifiedParabola();
		LocusDefinition2D definition = locus.getSemanticDefinition();

		assertSame(definition, definition.withConditionedSearch(
				locus::getConditionedEvidence));
		GeoLocusIntersectionResult rich = intersect(locus, "y=1");
		assertEquals(2, roots(rich).size());
		String state = rich.getTokenLedgerState();
		assertTrue(state.startsWith("4|"), state);
		for (String[] entry : entries(state)) {
			assertEquals(10, entry.length);
			assertTrue(unhex(entry[9]).startsWith("g9u0-r4/deterministic-current-root/v1/"),
					unhex(entry[9]));
			assertFalse(unhex(entry[9]).contains("conditioned-use"));
		}
		for (LocusIntersectionSolution2D root : roots(rich)) {
			assertFalse(has(root, DiagnosticCode.CONDITIONED_REGION_ROOT));
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactEntry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactReport;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3 decisions 4 to 6: an explicit redefine that changes the durable
 * dependency frontier of an ordinary participant is a typed durable-contract
 * change. It executes only through an operation the kernel declares available
 * and the user selects explicitly; replacement is never inferred.
 */
class PreG9bR3DurableContractRedefineTest extends PreG9bR3RedefineTestBase {
	private static final String FRONTIER_CHANGE =
			"Candidate changes the durable dependency frontier of the participant";

	@Test
	void explicitDependencySubstitutionIsATypedDurableContractChange() {
		contractFixture();
		final String beforeXml = getApp().getXML();
		final int reservations = registry().getReservedIdentityCount();

		Outcome cancelled = redefine("s", "a+1", assessment -> null);

		SpatialRedefineAssessment assessment = cancelled.assessment();
		assertNotNull(assessment);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				assessment.getStatus());
		assertEquals(FRONTIER_CHANGE, assessment.getDetail());
		assertTrue(assessment.isIdentityPreservingUpdateAvailable());
		assertTrue(assessment.isExplicitReplacementAvailable());
		assertNotNull(assessment.getProposal());
		SpatialRedefineImpactReport impact = assessment.getImpactReport();
		assertNotNull(impact);
		assertEquals(SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE,
				impact.getCompleteness());
		Set<SpatialIdentityId> affected = impact.getEntries().stream()
				.map(SpatialRedefineImpactEntry::getParticipantId)
				.collect(Collectors.toSet());
		assertTrue(affected.contains(id("s")));
		assertTrue(affected.contains(id("L")));
		assertFalse(affected.contains(id("a")));
		assertFalse(affected.contains(id("La")));
		assertTrue(cancelled.succeeded(), cancelled.errors());
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(reservations, registry().getReservedIdentityCount());
	}

	@Test
	void defaultIntentNeverExecutesAContractChangeNorInfersReplacement() {
		contractFixture();
		PersistentGeoId original = id("s");
		String beforeXml = getApp().getXML();
		int records = registry().getRecords().size();

		Outcome retain = redefine("s", "a+1",
				always(SpatialRedefineExecutionMode.ADVANCED_RETAIN));
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				retain.status());
		assertFalse(retain.succeeded());

		// The historical no-handler path selects ADVANCED_RETAIN by default.
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoElement target = requireLookup("s");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(target,
				"a+1", EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), target,
						true), false, null, errors);
		assertFalse(errors.getErrors().isBlank());

		assertEquals(beforeXml, getApp().getXML());
		assertEquals(original, id("s"));
		assertEquals(records, registry().getRecords().size());
		assertEquals(0, registry().getReservedIdentityCount());
	}

	@Test
	void anAcceptedIdentityPreservingUpdateKeepsIdentityAndPersistsTheNewFrontier()
			throws Exception {
		activateUndo();
		contractFixture();
		getApp().storeUndoInfo();
		GeoIdentityRecord before = record("s");
		final PersistentGeoId locus = id("L");
		final String beforeXml = getApp().getXML();

		Outcome update = redefine("s", "a+1", always(
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE));

		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				update.status());
		assertTrue(update.succeeded(), update.errors());
		GeoIdentityRecord after = record("s");
		assertEquals(before.getId(), after.getId());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				after.getSchemaVersion());
		assertEquals(ids("a"), after.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1, after.getTopologyRevision());
		assertFalse(requireLookup("s").isIndependent());
		assertEquals(3, ((GeoNumeric) requireLookup("s")).getDouble(), 1E-12);
		assertEquals(locus, id("L"));
		assertTrue(requireLookup("L").isDefined());
		assertEquals(0, registry().getReservedIdentityCount());
		String afterXml = getApp().getXML();

		getKernel().undo();
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		assertTrue(requireLookup("s").isIndependent());
		getKernel().redo();
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(before.getId(), id("s"));

		assertEquals(spatialGeoRecords(afterXml),
				spatialGeoRecords(openInFreshApp(afterXml).getXML()));
		getApp().getXMLio().processXMLString(afterXml, true, false, false);
		assertEquals(ids("a"), record("s").getDependencies());
		assertEquals(after.getDefinitionRevision(), record("s").getDefinitionRevision());
	}

	@Test
	void anUnauthorizedOrStaleContractUpdateChangesNothing() {
		contractFixture();
		String beforeXml = getApp().getXML();
		final GeoIdentityRecord before = record("s");
		SpatialRedefineExecutionMode update =
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE;

		// A compatible retain never authorizes the contract operations.
		Outcome compatible = redefine("s", "5", always(update));
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				compatible.status());
		assertFalse(compatible.succeeded());
		assertEquals(beforeXml, getApp().getXML());

		// A durable-contract change without a describable proposal offers nothing.
		Outcome family = redefine("s", "(1,1)", always(update));
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				family.status());
		assertFalse(family.assessment().isIdentityPreservingUpdateAvailable());
		assertFalse(family.assessment().isExplicitReplacementAvailable());
		assertEquals(beforeXml, getApp().getXML());

		// A choice made against a construction that changed meanwhile is discarded.
		Outcome stale = redefine("s", "a+1", assessment -> {
			add("Concurrent=9");
			return update;
		});
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				stale.status());
		assertFalse(stale.assessment().isCurrent());
		assertEquals(before, record("s"));
		assertTrue(requireLookup("s").isIndependent());
		assertEquals(0, registry().getReservedIdentityCount());
	}

	@Test
	void explicitReplacementCreatesANewIdentityWithTheAnnouncedImpact()
			throws Exception {
		activateUndo();
		contractFixture();
		getApp().storeUndoInfo();
		PersistentGeoId original = id("s");
		final PersistentGeoId locus = id("L");
		final PersistentGeoId unaffected = id("La");
		final String beforeXml = getApp().getXML();

		Outcome replacement = redefine("s", "a+1",
				always(SpatialRedefineExecutionMode.LEGACY_REPLACEMENT));

		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				replacement.status());
		assertTrue(replacement.succeeded(), replacement.errors());
		Set<SpatialIdentityId> announced = replacement.assessment().getImpactReport()
				.getEntries().stream().map(SpatialRedefineImpactEntry::getParticipantId)
				.collect(Collectors.toSet());
		PersistentGeoId fresh = id("s");
		assertNotEquals(original, fresh);
		assertNull(registry().getGeo(original));
		GeoIdentityRecord freshRecord = record(fresh);
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				freshRecord.getSchemaVersion());
		assertEquals(ids("a"), freshRecord.getDependencies());
		assertEquals(0, freshRecord.getDefinitionRevision());
		assertEquals(0, freshRecord.getTopologyRevision());
		for (SpatialIdentityId retired : List.of(original, locus)) {
			assertTrue(announced.contains(retired));
			assertNull(registry().getGeo((PersistentGeoId) retired));
		}
		assertEquals(unaffected, id("La"));
		assertEquals(0, registry().getReservedIdentityCount());

		getKernel().undo();
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(original, id("s"));
		assertEquals(locus, id("L"));
	}

	@Test
	void cartesianCoincidenceOfOldAndNewDependenciesIsNoEvidence() {
		contractFixture();
		// With a=-1 the assignment keeps the value of s, but its frontier changes.
		assertTrue(redefine("a", "-1", PreG9bR3RedefineTestBase::retainWhenAvailable)
				.succeeded());
		assertEquals(0, ((GeoNumeric) requireLookup("s")).getDouble(), 1E-12);
		Outcome coincident = redefine("s", "a+1", assessment -> null);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				coincident.status());

		// A different value over an unchanged frontier is a compatible change.
		Outcome moved = redefine("s", "1.5",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				moved.status());
		assertTrue(moved.succeeded(), moved.errors());
		assertTrue(record("s").getDependencies().isEmpty());
	}

	@Test
	void substitutingACoincidentDurableDependencyIsStillAContractChange() {
		contractFixture();
		add("t=s+1");
		add("Qt=(t,1)");
		add("Dt={false,{-2,2,true,true}}");
		assertNotNull(add("Lt=LocusV2(Qt,t,Dt)"));
		assertEquals(ids("s"), record("t").getDependencies());
		// With a=0 the substituted dependency coincides with s numerically.
		assertTrue(redefine("a", "0", PreG9bR3RedefineTestBase::retainWhenAvailable)
				.succeeded());
		final String beforeXml = getApp().getXML();

		Outcome substitution = redefine("t", "a+1", assessment -> null);

		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				substitution.status());
		assertTrue(substitution.assessment().isIdentityPreservingUpdateAvailable());
		assertTrue(substitution.assessment().isExplicitReplacementAvailable());
		assertEquals(beforeXml, getApp().getXML());
		PersistentGeoId identity = id("t");
		Outcome update = redefine("t", "a+1", always(
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE));
		assertTrue(update.succeeded(), update.errors());
		assertEquals(identity, id("t"));
		assertEquals(ids("a"), record("t").getDependencies());
	}

	@Test
	void anAcceptedContractUpdateMigratesAHistoricalRecord() throws Exception {
		loadMidpointFixture();
		GeoIdentityRecord before = record("E");
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
				before.getSchemaVersion());

		Outcome update = redefine("E", "Midpoint(D,Center(c))", always(
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE));

		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				update.status());
		assertTrue(update.succeeded(), update.errors());
		GeoIdentityRecord after = record("E");
		assertEquals(before.getId(), after.getId());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				after.getSchemaVersion());
		assertEquals(ids("C", "c"), after.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1, after.getTopologyRevision());
		for (String untouched : List.of("C", "c", "a")) {
			assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
					record(untouched).getSchemaVersion(), untouched);
		}
		String mixed = getApp().getXML();
		assertEquals(spatialGeoRecords(mixed),
				spatialGeoRecords(openInFreshApp(mixed).getXML()));
	}

	@Test
	void p08ReplacementIntentPublishesAFreshIdentityOnlyForAContractChange() {
		createLine();
		add("a=2");
		final PersistentGeoId original = id("s");
		String identityFreeXml = getApp().getXML();
		// With an identity-free `a` a compatible retain exists: intent is refused.
		ErrorAccumulator refused = new ErrorAccumulator();
		GeoElement state = requireLookup("s");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(state,
				"a+1", EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), state,
						true).withSpatialReplacementOperation(), false, null, refused);
		assertFalse(refused.getErrors().isBlank());
		assertEquals(identityFreeXml, getApp().getXML());

		add("Qa=(a,a^2)");
		add("Da={false,{-2,2,true,true}}");
		add("La=LocusV2(Qa,a,Da)");
		assertNotNull(registry().getPersistentGeoId(requireLookup("a")));
		ErrorAccumulator accepted = new ErrorAccumulator();
		state = requireLookup("s");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(state,
				"a+1", EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), state,
						true).withSpatialReplacementOperation(), false, null, accepted);
		assertTrue(accepted.getErrors().isBlank(), accepted.getErrors());
		PersistentGeoId replacement = id("s");
		assertNotEquals(original, replacement);
		assertNull(registry().getGeo(original));
		assertEquals(ids("a"), record(replacement).getDependencies());
		assertEquals(0, registry().getReservedIdentityCount());
	}

	/** Two loci whose independent drivers {@code s} and {@code a} are durable. */
	private void contractFixture() {
		createLine();
		add("a=2");
		add("Qa=(a,a^2)");
		add("Da={false,{-2,2,true,true}}");
		assertNotNull(add("La=LocusV2(Qa,a,Da)"));
		assertNotNull(registry().getPersistentGeoId(requireLookup("a")));
		assertTrue(record("s").getDependencies().isEmpty());
	}
}

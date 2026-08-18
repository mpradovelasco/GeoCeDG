/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geocedg.common.kernel.spatial.runtime.AlgoProjectionDefinedSpatialPoint;
import org.geocedg.common.kernel.spatial.runtime.AlgoProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.geogebra3D.euclidian3D.EuclidianController3D;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.io.ConsElementXMLHandler;
import org.geogebra.common.jre.headless.EuclidianController3DNoGui;
import org.geogebra.common.jre.headless.EuclidianView3DNoGui;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.Region;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoCasCell;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.PolygonFactory;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.matrix.Coords;
import org.junit.jupiter.api.Test;

class G9A3SpatialRedefineHostTest extends G9A3SpatialRedefineTestSupport {

	@Test
	void redef03IndependentUndefinedNumericUsesExplicitAuthorityAndExactRollback() {
		GeoNumeric target = add("R03=1");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		edit(target, "R03=?");

		GeoElement actual = lookup("R03");
		assertFalse(actual.isDefined());
		assertEquals(original.getId(), registry().getPersistentGeoId(actual));
		assertEquals(1, registry().getGeoRecord(original.getId())
				.getDefinitionRevision());
		String undefinedXml = getApp().getXML();
		SpatialRedefineContext stale = registry().captureRedefineContext(actual);
		edit(actual, "R03=2");
		String newerXml = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().rollbackSpatialRedefinePreparation(stale));
		assertEquals(newerXml, getApp().getXML());
		assertNotEquals(undefinedXml, newerXml);
	}

	@Test
	void redef04InPlaceSetRetainsIdentityAndAdvancesDefinitionOnce() {
		GeoNumeric target = add("R04=1");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		edit(target, "R04=2");

		assertSame(target, lookup("R04"));
		assertEquals(original.getId(), registry().getPersistentGeoId(target));
		assertEquals(1, registry().getGeoRecord(original.getId())
				.getDefinitionRevision());
		assertEquals(0, registry().getGeoRecord(original.getId())
				.getTopologyRevision());
	}

	@Test
	void redef05SoftRedefineMatchesTheCompatibleLifecycleContract() {
		add("R05P=(0,0)");
		GeoElement target = add("R05l=Line(R05P,(1,0))");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		edit(target, "R05l=Line(R05P,(0,1))");

		assertSame(target, lookup("R05l"));
		assertEquals(original.getId(), registry().getPersistentGeoId(target));
		assertEquals(1, registry().getGeoRecord(original.getId())
				.getDefinitionRevision());
		assertEquals(0, registry().getGeoRecord(original.getId())
				.getTopologyRevision());
	}

	@Test
	void redef06NoChildNewInstanceReceivesOnlyTheExplicitTargetIdentity() {
		add("R06P=(0,0)");
		add("R06Q=(1,0)");
		add("R06R=(0,1)");
		add("R06g=Line(R06Q,R06R)");
		GeoElement target = add("R06l=Line(R06P,R06Q)");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		edit(target, "R06l=PerpendicularLine(R06P,R06g)");

		GeoElement actual = lookup("R06l");
		assertNotSame(target, actual);
		assertEquals(original.getId(), registry().getPersistentGeoId(actual));
		assertNull(registry().getPersistentGeoId(target));
	}

	@Test
	void redef07CircularNumericEitherCommitsExplicitlyOrRejectsAtomically() {
		GeoNumeric target = add("R07=1");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));
		String before = getApp().getXML();
		CapturingErrorHandler errors = redefine(target, "R07=R07+1");

		GeoElement actual = lookup("R07");
		if (errors.hasError()) {
			assertEquals(before, getApp().getXML());
			assertSame(target, registry().getGeo(original.getId()));
		} else {
			assertEquals(original.getId(), registry().getPersistentGeoId(actual));
			assertEquals(1, registry().getGeoRecord(original.getId())
					.getDefinitionRevision());
		}
	}

	@Test
	void redef08CircularPointVectorAndBooleanShareTargetAndRollbackPolicy() {
		SpatialRedefineSignature signature = signature(PROVIDER, "GENERIC", "VALUE", 1);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));
		List<String[]> cases = List.of(
				new String[] {"R08P=(1,1)", "R08P=R08P+(1,0)", "R08P"},
				new String[] {"R08v=(1,0)", "R08v=R08v+(0,1)", "R08v"},
				new String[] {"R08b=true", "R08b=!R08b", "R08b"});
		for (String[] route : cases) {
			GeoElement target = add(route[0]);
			GeoIdentityRecord original = register(target, signature);
			String before = getApp().getXML();
			CapturingErrorHandler errors = redefine(target, route[1]);
			if (errors.hasError()) {
				assertEquals(before, getApp().getXML());
				assertSame(target, registry().getGeo(original.getId()));
			} else {
				assertEquals(original.getId(),
						registry().getPersistentGeoId(lookup(route[2])));
				assertEquals(1, registry().getGeoRecord(original.getId())
						.getDefinitionRevision());
			}
		}
	}

	@Test
	void redef09CollectedRetainedBatchCommitsOrRollsBackAsAWhole() throws Exception {
		add("R09P=(0,0)");
		add("R09Q=(1,0)");
		add("R09S=(2,0)");
		add("R09T=(2,1)");
		add("R09h=Line(R09S,R09T)");
		GeoElement first = add("R09m=Line(R09P,R09S)");
		GeoElement second = add("R09n=Line(R09Q,R09T)");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		GeoIdentityRecord firstRecord = register(first, signature);
		GeoIdentityRecord secondRecord = register(second, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		getConstruction().startCollectingRedefineCalls();
		edit(first, "R09m=PerpendicularLine(R09P,R09h)");
		edit(second, "R09n=PerpendicularLine(R09Q,R09h)");
		getConstruction().processCollectedRedefineCalls();

		assertEquals(firstRecord.getId(),
				registry().getPersistentGeoId(lookup("R09m")));
		assertEquals(secondRecord.getId(),
				registry().getPersistentGeoId(lookup("R09n")));
		assertEquals(1, registry().getGeoRecord(firstRecord.getId())
				.getDefinitionRevision());
		assertEquals(1, registry().getGeoRecord(secondRecord.getId())
				.getDefinitionRevision());

		String beforeRepeatedTarget = getApp().getXML();
		getConstruction().startCollectingRedefineCalls();
		edit(lookup("R09m"), "R09m=Line(R09P,R09S)");
		CapturingErrorHandler repeatedTarget = redefine(lookup("R09m"),
				"R09m=PerpendicularLine(R09P,R09h)");
		assertTrue(repeatedTarget.hasError());
		getConstruction().stopCollectingRedefineCalls();
		assertEquals(beforeRepeatedTarget, getApp().getXML());
		assertEquals(firstRecord.getId(),
				registry().getPersistentGeoId(lookup("R09m")));

		GeoElement overflow = add("R09x=Line(R09P,R09S)");
		GeoIdentityRecord overflowRecord = register(overflow, signature,
				Long.MAX_VALUE);
		String committed = getApp().getXML();
		getConstruction().startCollectingRedefineCalls();
		edit(lookup("R09m"), "R09m=Line(R09P,R09Q)");
		edit(overflow, "R09x=PerpendicularLine(R09P,R09h)");
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().processCollectedRedefineCalls());
		assertEquals(committed, getApp().getXML());
		assertEquals(Long.MAX_VALUE, registry().getGeoRecord(overflowRecord.getId())
				.getDefinitionRevision());

		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		long systemPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L);
		long pointPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L);
		getConstruction().startCollectingRedefineCalls();
		edit(graph.supportEnd, "G9A2HE=(2,0,0)");
		assertEquals(1, graph.supportEnd.getInhomX(), 0);
		assertEquals(systemPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(pointPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));
		edit(graph.one, "G9A2One=0.25");
		assertEquals(1, graph.one.getDouble(), 0);
		assertEquals(systemPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(pointPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));
		getConstruction().processCollectedRedefineCalls();
		assertEquals(systemPublications + 1, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(pointPublications + 1, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));
		assertEquals(2, ((GeoPointND) lookup("G9A2HE")).getInhomX(), 0);
		assertEquals(0.25, ((GeoNumeric) lookup("G9A2One")).getDouble(), 0);

		final String beforeMixedFailure = getApp().getXML();
		final int mixedSteps = getConstruction().steps();
		long mixedSystemPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L);
		long mixedPointPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L);
		getConstruction().startCollectingRedefineCalls();
		edit(lookup("G9A2HE"), "G9A2HE=(3,0,0)");
		assertEquals(2, ((GeoPointND) lookup("G9A2HE")).getInhomX(), 0);
		edit(lookup("R09x"), "R09x=Line(R09P,R09Q)");
		assertEquals(mixedSystemPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(mixedPointPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().processCollectedRedefineCalls());
		assertEquals(beforeMixedFailure, getApp().getXML());
		assertEquals(mixedSteps, getConstruction().steps());
		assertEquals(mixedSystemPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(mixedPointPublications, getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));
	}

	@Test
	void redef10FullXmlRebuildRestoresIdentityRevisionAndChildren() {
		add("R10P=(0,0)");
		add("R10Q=(1,0)");
		add("R10R=(0,1)");
		add("R10g=Line(R10Q,R10R)");
		GeoElement target = add("R10l=Line(R10P,R10Q)");
		add("R10X=Point(R10l)");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));

		edit(target, "R10l=PerpendicularLine(R10P,R10g)");

		GeoElement rebuilt = lookup("R10l");
		assertNotSame(target, rebuilt);
		assertEquals(original.getId(), registry().getPersistentGeoId(rebuilt));
		assertEquals(1, registry().getGeoRecord(original.getId())
				.getDefinitionRevision());
		assertNotNull(lookup("R10X"));
		assertTrue(getApp().getXML().contains(original.getId().toExternalForm()));

		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		G9A2SpatialSemanticRuntimeTest.SiblingGraph sibling =
				G9A2SpatialSemanticRuntimeTest.SiblingGraph.create(graph);
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(sibling.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate siblingCertificate = getConstruction()
				.getSpatialSemanticRuntime()
				.getSpatialPointCertificate(sibling.objectId);
		assertEquals(SpatialCertificateStatus.VALID,
				siblingCertificate.getSemanticCertificate().getStatus(),
				() -> "sibling definition=" + siblingCertificate
						.getSemanticCertificate().getDefinitionStatus()
						+ ", embeddedSystem=" + siblingCertificate
								.getSemanticCertificate().getProjectionSystemStatus()
						+ ", currentness=" + siblingCertificate
								.getSemanticCertificate().getCurrentnessStatus()
						+ ", rank=" + siblingCertificate.getSemanticCertificate()
								.getRank()
						+ ", method=" + siblingCertificate.getSemanticCertificate()
								.getArithmeticMethod()
						+ ", runtimeDiagnostics=" + getConstruction()
								.getSpatialSemanticRuntime().getDiagnostics());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(graph.systemId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(sibling.systemId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(sibling.objectId).isCurrentRevision());
		AlgoProjectionSystemCertificate targetSystemAlgorithm = getConstruction()
				.getSpatialSemanticRuntime().getSystemAlgorithm(graph.systemId);
		AlgoProjectionSystemCertificate siblingSystemAlgorithm = getConstruction()
				.getSpatialSemanticRuntime().getSystemAlgorithm(sibling.systemId);
		AlgoProjectionDefinedSpatialPoint targetPointAlgorithm = getConstruction()
				.getSpatialSemanticRuntime().getPointAlgorithm(graph.objectId);
		AlgoProjectionDefinedSpatialPoint siblingPointAlgorithm = getConstruction()
				.getSpatialSemanticRuntime().getPointAlgorithm(sibling.objectId);
		final PersistentGeoId supportId = registry().getPersistentGeoId(
				graph.supportEnd);
		final String unrelatedSystemToken = getConstruction()
				.getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(sibling.systemId)
				.getValueSnapshotToken();
		final String unrelatedPointToken = getConstruction()
				.getSpatialSemanticRuntime()
				.getSpatialPointCertificate(sibling.objectId)
				.getValueSnapshotToken();
		final long targetSystemPublications = getConstruction()
				.getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L);
		final long targetPointPublications = getConstruction()
				.getSpatialSemanticRuntime()
				.getInstrumentation().snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L);
		final long unrelatedSystemPublications = getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(sibling.systemId, 0L);
		final long unrelatedPointPublications = getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(sibling.objectId, 0L);
		final long systems = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getProjectionSystemEvaluations();
		final long systemPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getProjectionSystemCertificatePublications();
		final long reconstructions = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getReconstructionAttempts();
		final long pointPublications = getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getCertificatePublications();
		CapturingErrorHandler errors = redefine(graph.supportEnd,
				"G9A2HE=(2,0,0)");

		assertFalse(errors.hasError(), errors::describe);
		assertNotSame(targetSystemAlgorithm, getConstruction().getSpatialSemanticRuntime()
				.getSystemAlgorithm(graph.systemId));
		assertSame(siblingSystemAlgorithm, getConstruction().getSpatialSemanticRuntime()
				.getSystemAlgorithm(sibling.systemId));
		assertNotSame(targetPointAlgorithm, getConstruction().getSpatialSemanticRuntime()
				.getPointAlgorithm(graph.objectId));
		assertSame(siblingPointAlgorithm, getConstruction().getSpatialSemanticRuntime()
				.getPointAlgorithm(sibling.objectId));
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(sibling.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(sibling.objectId)
						.getSemanticCertificate().getStatus());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(graph.systemId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(sibling.systemId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId).isCurrentRevision());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(sibling.objectId).isCurrentRevision());
		assertEquals(systems + 2, getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getProjectionSystemEvaluations());
		assertEquals(systemPublications + 2,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.getProjectionSystemCertificatePublications());
		assertEquals(reconstructions + 1,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.getReconstructionAttempts());
		assertEquals(pointPublications + 1,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.getCertificatePublications());
		assertEquals(targetSystemPublications + 1,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.snapshotAuthoritativePublicationCounts()
						.getOrDefault(graph.systemId, 0L));
		assertEquals(targetPointPublications + 1,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.snapshotAuthoritativePublicationCounts()
						.getOrDefault(graph.objectId, 0L));
		assertEquals(unrelatedSystemPublications,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.snapshotAuthoritativePublicationCounts()
						.getOrDefault(sibling.systemId, 0L));
		assertEquals(unrelatedPointPublications,
				getConstruction().getSpatialSemanticRuntime().getInstrumentation()
						.snapshotAuthoritativePublicationCounts()
						.getOrDefault(sibling.objectId, 0L));
		assertEquals(unrelatedSystemToken,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(sibling.systemId)
						.getValueSnapshotToken());
		assertEquals(unrelatedPointToken,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(sibling.objectId)
						.getValueSnapshotToken());
		assertEquals(1, registry().getGeoRecord(supportId).getDefinitionRevision());
	}

	@Test
	void redef11AlgebraProcessorCapturesTheExplicitTargetBeforeParsing() {
		GeoNumeric target = add("R11=1");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));
		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), target, true);

		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(target, "R11=7",
				info, false, null, errors);

		assertFalse(errors.hasError(), errors::describe);
		assertEquals(original.getId(),
				registry().getPersistentGeoId(lookup("R11")));
		assertEquals(1, registry().getGeoRecord(original.getId())
				.getDefinitionRevision());

		String staleProvider = "g9a3.redef11.stale-provider";
		SpatialRedefineSignature staleSignature = signature(staleProvider,
				"NUMERIC", "VALUE", 1);
		GeoNumeric staleTarget = add("R11S=2");
		GeoIdentityRecord staleOriginal = register(staleTarget, staleSignature);
		GeoNumeric providerPublished = add("R11Published=3");
		PersistentGeoId providerPublishedId = registry().allocatePersistentGeoId();
		GeoIdentityRecord providerPublishedRecord = new GeoIdentityRecord(
				providerPublishedId, staleSignature.getProvider(),
				staleSignature.getFamily(), staleSignature.getSchemaId(),
				staleSignature.getSchemaVersion(), staleSignature.getAuthority(),
				staleSignature.getBindingRole(),
				staleSignature.getStableOutputRole(),
				staleSignature.getOutputCardinality(), 0, 0);
		registry().registerRedefineProvider(new CompatibleProvider(staleProvider,
				staleSignature, SpatialRedefineDecision.FRESH) {
			@Override
			public SpatialRedefineDecision inspect(SpatialRedefineContext context,
					SpatialRedefineProposal proposal) {
				registry().registerParticipation(providerPublished,
						providerPublishedRecord);
				return SpatialRedefineDecision.FRESH;
			}
		});

		CapturingErrorHandler staleLease = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(staleTarget,
				"R11S=4", EvalInfoFactory.getEvalInfoForRedefinition(
						getKernel(), staleTarget, true)
						.withSpatialReplacementOperation(),
				false, null, staleLease);

		assertTrue(staleLease.hasError());
		assertEquals(staleOriginal.getId(),
				registry().getPersistentGeoId(lookup("R11S")));
		assertEquals(2, lookup("R11S").evaluateDouble(), 0);
		assertEquals(0, registry().getGeoRecord(staleOriginal.getId())
				.getDefinitionRevision());
		assertEquals(providerPublishedRecord.getId(),
				registry().getPersistentGeoId(providerPublished));
		assertEquals(0, registry().getReservedIdentityCount());
	}

	@Test
	void redef12ParametricProcessorUsesProviderAuthorityNotExpressionEquality() {
		GeoElement target = add("R12l=Line((0,0),(1,0))");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));
		String oldValue = target.toValueString(StringTemplate.xmlTemplate);

		edit(target, "R12l: X = (0,0) + t * (0,1)");

		GeoElement actual = lookup("R12l");
		assertNotEquals(oldValue,
				actual.toValueString(StringTemplate.xmlTemplate));
		assertEquals(original.getId(), registry().getPersistentGeoId(actual));
	}

	@Test
	void redef13AlgoDispatcherPathAndRegionRoutesAreAtomic() {
		SpatialRedefineSignature signature = signature(PROVIDER, "POINT", "POINT", 1);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER, signature,
				SpatialRedefineDecision.RETAIN));
		GeoElement pathTarget = add("R13P=(1,1)");
		GeoIdentityRecord pathRecord = register(pathTarget, signature);
		Path path = (Path) add("R13g:y=0");

		GeoPointND attachedPath = getKernel().getAlgoDispatcher().attach(
				(GeoPointND) pathTarget, path, getApp().getActiveEuclidianView(),
				new Coords(1, 0, 1));

		assertNotNull(attachedPath);
		assertEquals(pathRecord.getId(),
				registry().getPersistentGeoId(lookup("R13P")));
		GeoPointND detachedPath = getKernel().getAlgoDispatcher().detach(
				attachedPath, getApp().getActiveEuclidianView());
		assertNotNull(detachedPath);
		assertEquals(pathRecord.getId(),
				registry().getPersistentGeoId(lookup("R13P")));

		GeoElement regionTarget = add("R13Q=(2,2)");
		GeoIdentityRecord regionRecord = register(regionTarget, signature);
		Region region = (Region) add("R13d=Circle((0,0),4)");
		GeoPointND attachedRegion = getKernel().getAlgoDispatcher().attach(
				(GeoPointND) regionTarget, region, getApp().getActiveEuclidianView(),
				new Coords(2, 2, 1));
		assertNotNull(attachedRegion);
		assertEquals(regionRecord.getId(),
				registry().getPersistentGeoId(lookup("R13Q")));
		GeoPointND detachedRegion = getKernel().getAlgoDispatcher().detach(
				attachedRegion, getApp().getActiveEuclidianView());
		assertNotNull(detachedRegion);
		assertEquals(regionRecord.getId(),
				registry().getPersistentGeoId(lookup("R13Q")));

		String throwingProvider = "g9a3.redef13.throwing";
		GeoElement rejected = add("R13Z=(3,3)");
		GeoIdentityRecord rejectedRecord = register(rejected,
				signature(throwingProvider, "POINT", "POINT", 1));
		registry().registerRedefineProvider(new ThrowingProvider(throwingProvider));
		String beforeFailure = getApp().getXML();
		int stepsBeforeFailure = getConstruction().steps();
		assertNull(getKernel().getAlgoDispatcher().attach((GeoPointND) rejected,
				path, getApp().getActiveEuclidianView(), new Coords(3, 0, 1)));
		assertEquals(beforeFailure, getApp().getXML());
		assertEquals(stepsBeforeFailure, getConstruction().steps());
		assertEquals(rejectedRecord.getId(),
				registry().getPersistentGeoId(lookup("R13Z")));
	}

	@Test
	void redef14ThreeDMoveReleaseCapturesBeforeHelperAndRestoresOnFailure()
			throws Exception {
		String provider = "g9a3.redef14.throwing";
		GeoElement point = add("R14=(1,2)");
		final GeoIdentityRecord original = register(point,
				signature(provider, "POINT", "POINT", 1));
		registry().registerRedefineProvider(new ThrowingProvider(provider));
		EuclidianController3DNoGui controller = new EuclidianController3DNoGui(
				getApp(), getKernel());
		EuclidianView3DNoGui view = new EuclidianView3DNoGui(controller,
				getApp().getSettings().getEuclidian(3));
		controller.setView(view);
		controller.setMode(EuclidianConstants.MODE_MOVE, ModeSetter.TOOLBAR);
		String before = getApp().getXML();
		int steps = getConstruction().steps();
		Method release = EuclidianController3D.class.getDeclaredMethod(
				"movePointForRelease", GeoPointND.class, boolean.class);
		release.setAccessible(true);

		InvocationTargetException releaseFailure = assertThrows(
				InvocationTargetException.class,
				() -> release.invoke(controller, (GeoPointND) point, false));
		AssertionError loggedFailure = assertInstanceOf(AssertionError.class,
				releaseFailure.getCause());
		assertInstanceOf(IllegalStateException.class, loggedFailure.getCause());

		assertEquals(before, getApp().getXML());
		assertEquals(steps, getConstruction().steps());
		assertEquals(original.getId(),
				registry().getPersistentGeoId(lookup("R14")));
	}

	@Test
	void redef15LabelAndXmlDiscoveryCannotSupplyRedefineAuthority()
			throws Exception {
		GeoNumeric target = add("R15=5");
		GeoIdentityRecord original = register(target,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		GeoNumeric lookalike = new GeoNumeric(getConstruction(), 5);
		lookalike.setLabelSimple("R15");
		String before = getApp().getXML();

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replaceWithoutSpatialRedefineAuthority(
						target, lookalike));

		assertEquals(before, getApp().getXML());
		assertSame(target, registry().getGeo(original.getId()));
		assertNull(registry().getPersistentGeoId(lookalike));

		GeoElement text = add("R15Text=\"authority\"");
		((GeoText) text).setStartPoint((GeoPointND) add("R15TextPoint=(0,0)"));
		GeoIdentityRecord textRecord = register(text,
				signature(PROVIDER, "TEXT", "VALUE", 1));
		String beforeBoundingBox = getApp().getXML();
		ConsElementXMLHandler handler = new ConsElementXMLHandler(null, getApp());
		Field geoField = ConsElementXMLHandler.class.getDeclaredField("geo");
		geoField.setAccessible(true);
		geoField.set(handler, text);
		Method boundingBox = ConsElementXMLHandler.class.getDeclaredMethod(
				"handleBoundingBox", Map.class);
		boundingBox.setAccessible(true);
		InvocationTargetException failure = assertThrows(
				InvocationTargetException.class,
				() -> boundingBox.invoke(handler,
						Map.of("width", "120", "height", "30")));
		AssertionError logged = assertInstanceOf(AssertionError.class,
				failure.getCause());
		assertInstanceOf(SpatialIdentityException.class, logged.getCause());
		assertEquals(beforeBoundingBox, getApp().getXML());
		assertSame(text, registry().getGeo(textRecord.getId()));
	}

	@Test
	void redef16ParticipatingGeoCasCellRouteIsUnsupportedAndAtomic() {
		GeoNumeric target = add("R16Cas=1");
		GeoIdentityRecord original = register(target,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		ParticipatingCasCell casCell = new ParticipatingCasCell(target);
		String before = getApp().getXML();

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().changeCasCell(casCell, casCell.getXML()));

		assertEquals(before, getApp().getXML());
		assertSame(target, registry().getGeo(original.getId()));
		assertFalse(getConstruction().isUpdateConstructionRunning());
	}

	@Test
	void redef17PolygonFactoryConservativelyRejectsParticipatingRewrite() {
		GeoPointND[] points = {
				(GeoPointND) add("R17A=(0,0)"),
				(GeoPointND) add("R17B=(1,0)"),
				(GeoPointND) add("R17C=(0,1)")
		};
		GeoIdentityRecord original = register((GeoElement) points[1],
				signature(PROVIDER, "POINT", "POINT", 1));
		String before = getApp().getXML();

		assertNull(new PolygonFactory(getKernel()).rigidPolygon(null, points));

		assertEquals(before, getApp().getXML());
		assertEquals(original.getId(), registry().getPersistentGeoId(
				(GeoElement) points[1]));
	}

	private CapturingErrorHandler redefine(GeoElement target, String definition) {
		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(target, definition,
				EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), target, true),
				false, null, errors);
		return errors;
	}

	private static final class ThrowingProvider implements SpatialRedefineProvider {
		private final String provider;

		private ThrowingProvider(String provider) {
			this.provider = provider;
		}

		@Override
		public String getProviderId() {
			return provider;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(
				SpatialRedefineContext context, GeoElement candidate) {
			throw new IllegalStateException("deliberate G9A3 host-route failure");
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return SpatialRedefineDecision.RETAIN;
		}
	}

	private final class ParticipatingCasCell extends GeoCasCell {
		private final GeoElement twin;

		private ParticipatingCasCell(GeoElement twin) {
			super(G9A3SpatialRedefineHostTest.this.getConstruction());
			this.twin = twin;
		}

		@Override
		public GeoElement getTwinGeo() {
			return twin;
		}
	}
}

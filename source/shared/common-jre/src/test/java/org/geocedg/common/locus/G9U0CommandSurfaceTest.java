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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2DomainDescriptor;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.selector.CommandFilter;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Exact C14 command/public-surface scenarios authorized by G9U0. */
class G9U0CommandSurfaceTest extends G9U0PublicSurfaceTestBase {

	@Test
	void c01LegacyLocusCommandRemainsDistinct() {
		assertNotEquals(Commands.Locus, Commands.LocusV2);
		CommandFilter filter = new AppConfigGeoCeDG().createCommandFilter();
		assertTrue(filter.isCommandAllowed(Commands.Locus));
	}

	@Test
	void c02ExplicitTypedV2CommandCreatesNativeGeo() {
		GeoLocusV2 locus = createParabola();
		assertEquals(Commands.LocusV2, locus.getParentAlgorithm().getClassName());
		assertFalse(locus.isPath());
	}

	@Test
	void c03FeatureOffAlgebraCommandIsFiltered() {
		CommandFilter filter = new AppConfigGeoCeDG().createCommandFilter();
		assertFalse(filter.isCommandAllowed(Commands.LocusV2));
		assertFalse(filter.isCommandAllowed(Commands.LocusLength));
		assertTrue(filter.isCommandAllowed(Commands.Intersect));
	}

	@Test
	void c04FeatureOffPreservationContextCanResolveNativeCommands() {
		RuntimeFeatureService service = new RuntimeFeatureService(false);
		assertFalse(service.isCommandVisible(Commands.LocusV2));
		service.bindPreservationContext(() -> true);
		assertTrue(service.isCommandVisible(Commands.LocusV2));
		assertFalse(service.isLocusV2CreationEnabled());
	}

	@Test
	void c05ArgumentAndProviderMatrixRejectsMalformedDomain() {
		GeoList malformed = add("badD={false,{-1,1,true}}");
		assertThrows(IllegalArgumentException.class,
				() -> LocusV2DomainDescriptor.parse(malformed));

		int constructionSteps = getConstruction().steps();
		int constructionGeos = getConstruction().getGeoSetConstructionOrder().size();
		int identityRecords = getConstruction().getSpatialIdentityRegistry()
				.getRecords().size();
		int reservations = getConstruction().getSpatialIdentityRegistry()
				.getReservedIdentityCount();
		assertThrows(AssertionError.class, () -> add(
				"Bad=LocusV2((1,1),0,{false,{-1,1,true,true}})"));
		assertEquals(constructionSteps, getConstruction().steps());
		assertEquals(constructionGeos,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(identityRecords, getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
		assertEquals(reservations, getConstruction()
				.getSpatialIdentityRegistry().getReservedIdentityCount());
		assertTrue(lookup("Bad") == null);
	}

	@Test
	void c06ScalarCommandCarriesExplicitSemanticDomain() {
		GeoLocusV2 locus = createParabola();
		assertEquals(-2, locus.getSemanticDefinition().getProvider()
				.getDeclaredDomain().getLower(), 0);
		assertEquals(2, locus.getSemanticDefinition().getProvider()
				.getDeclaredDomain().getUpper(), 0);
	}

	@Test
	void c07PointSupportProviderUsesConstrainedPoint() {
		add("A=(0,0)");
		add("B=(2,0)");
		add("g=Segment(A,B)");
		GeoPoint state = add("S=Point(g)");
		add("Q=(x(S),x(S)^2)");
		GeoLocusV2 locus = add("L=LocusV2(Q,S)");
		assertNotNull(locus);
		assertSame(state, locus.getParentAlgorithm().getInput(1));
	}

	@Test
	void c08PublicParentExposesNormalDagDependencies() {
		GeoLocusV2 locus = createParabola();
		var registry = getConstruction().getSpatialIdentityRegistry();
		Object identity = registry.getPersistentGeoId(locus);
		long commits = registry.getInstrumentation().getRedefineCommits();
		final long retainDecisions = registry.getInstrumentation()
				.getRedefineRetainDecisions();
		assertEquals(3, locus.getParentAlgorithm().getInputLength());
		assertSame(requireLookup("Q"), locus.getParentAlgorithm().getInput(0));
		assertSame(requireLookup("s"), locus.getParentAlgorithm().getInput(1));
		assertSame(requireLookup("D"), locus.getParentAlgorithm().getInput(2));

		editGeoElement(locus, "LocusV2(Q,s,D)");
		GeoLocusV2 retained = (GeoLocusV2) requireLookup("L");
		assertEquals(identity, registry.getPersistentGeoId(retained));
		assertEquals(commits + 1,
				registry.getInstrumentation().getRedefineCommits());
		assertEquals(retainDecisions + 1,
				registry.getInstrumentation().getRedefineRetainDecisions());
		assertEquals(0, registry.getReservedIdentityCount());
		assertSame(requireLookup("Q"), retained.getParentAlgorithm().getInput(0));
		assertSame(requireLookup("s"), retained.getParentAlgorithm().getInput(1));
		assertSame(requireLookup("D"), retained.getParentAlgorithm().getInput(2));
	}

	@Test
	void c09EvaluatorTransactionRestoresDriverState() {
		GeoLocusV2 locus = createParabola();
		GeoNumeric driver = (GeoNumeric) requireLookup("s");
		driver.setValue(0.375);
		driver.updateCascade();
		GeoLocusMetricResult metric = totalMetric(locus);
		assertTrue(metric.isDefined());
		assertEquals(0.375, driver.getDouble(), 0);
	}

	@Test
	void c10SemanticCommandDoesNotReadRenderOrViewportState() {
		GeoLocusV2 locus = createParabola();
		locus.getInstrumentation().reset();
		totalMetric(locus);
		assertEquals(0, locus.getInstrumentation().getRenderEvaluations());
		assertEquals(0, locus.getInstrumentation().getWholeLocusRegenerations());
	}

	@Test
	void c11PublicCommandHasReconstructibleCommandIdentity() {
		GeoLocusV2 locus = createParabola();
		assertEquals(Commands.LocusV2, locus.getParentAlgorithm().getClassName());
		assertTrue(getApp().getXML().contains("name=\"LocusV2\""));
	}

	@Test
	void c12DisplayLabelIsNotDurableLocusIdentity() {
		GeoLocusV2 locus = createParabola();
		String identity = locus.getLocusIdentity();
		locus.setLabel("RenamedLocus");
		assertEquals(identity, locus.getLocusIdentity());
		assertNotEquals(locus.getLabelSimple(), identity);
	}

	@Test
	void c13ArchiveUuidIsNotSemanticIdentityMaterial() {
		GeoLocusV2 locus = createParabola();
		String identity = locus.getLocusIdentity();
		assertTrue(identity.startsWith("geo:"));
		assertFalse(identity.contains("construction"));
		assertFalse(identity.contains(locus.getLabelSimple()));
	}

	@Test
	void c14BranchTopologyUpdatePreservesDurableOutputIdentity() {
		GeoLocusV2 locus = createDisconnectedLine();
		Object persistent = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(locus);
		long revision = locus.getSemanticRevision();
		((GeoNumeric) requireLookup("s")).setValue(1.5);
		requireLookup("s").updateCascade();
		assertTrue(locus.getSemanticRevision() > revision);
		assertEquals(persistent, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(locus));
	}
}

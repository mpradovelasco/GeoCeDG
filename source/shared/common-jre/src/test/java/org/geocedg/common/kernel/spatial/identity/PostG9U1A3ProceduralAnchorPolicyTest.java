/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Focused A4/P3 captured-anchor disappearance policy evidence for A3. */
class PostG9U1A3ProceduralAnchorPolicyTest extends BaseUnitTest {
	private static final String ROLE = "VALUE";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void ordinaryUnregisteredNeighborsRemainTransactionLocalP3Evidence() {
		GeoNumeric left = add("Left=1");
		GeoNumeric target = add("Target=2");
		GeoNumeric right = add("Right=3");
		GeoNumeric tail = add("Tail=4");
		GeoIdentityRecord targetRecord = register(target);
		SpatialProceduralPositionSnapshot position = registry()
				.captureRedefineContext(target).getProceduralPosition();

		add("NewUnrelated=5");
		assertTrue(getConstruction().moveInConstructionList(target,
				tail.getConstructionIndex()));
		assertTrue(right.getConstructionIndex() < target.getConstructionIndex());

		position.enforce(registry(), Map.of(ROLE, target), targetRecord.getId());
		assertTrue(left.getConstructionIndex() < target.getConstructionIndex());
		assertTrue(target.getConstructionIndex() < right.getConstructionIndex());
		assertTrue(right.getConstructionIndex() < tail.getConstructionIndex());
	}

	@Test
	void capturedOuterIntervalSurvivesAdjacentRemovalButNoAnchorFailsClosed() {
		GeoNumeric leftOuter = add("LeftOuter=1");
		GeoNumeric leftInner = add("LeftInner=2");
		GeoNumeric target = add("Target=3");
		GeoNumeric rightInner = add("RightInner=4");
		GeoNumeric rightOuter = add("RightOuter=5");
		register(leftOuter);
		register(leftInner);
		GeoIdentityRecord targetRecord = register(target);
		register(rightInner);
		register(rightOuter);

		SpatialRedefineContext context = registry()
				.captureRedefineContext(target);
		SpatialProceduralPositionSnapshot position = context
				.getProceduralPosition();
		leftInner.remove();
		rightInner.remove();
		position.enforce(registry(), Map.of(ROLE, target), targetRecord.getId());
		assertTrue(leftOuter.getConstructionIndex()
				< target.getConstructionIndex());
		assertTrue(target.getConstructionIndex()
				< rightOuter.getConstructionIndex());

		rightOuter.remove();
		position.enforce(registry(), Map.of(ROLE, target), targetRecord.getId());
		assertTrue(leftOuter.getConstructionIndex()
				< target.getConstructionIndex());

		leftOuter.remove();
		assertThrows(SpatialIdentityException.class,
				() -> position.enforce(registry(), Map.of(ROLE, target),
						targetRecord.getId()));
	}

	@Test
	void incompleteImpactCannotMasqueradeAsComplete() {
		final SpatialRedefineImpactReport incomplete =
				new SpatialRedefineImpactReport(
						SpatialRedefineImpactReport.Completeness
								.IMPACT_NOT_ESTABLISHED,
						List.of(), "unresolved durable closure");
		final SpatialRedefineImpactReport complete =
				new SpatialRedefineImpactReport(
						SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE,
						List.of(), null);

		assertFalse(SpatialIdentityRegistry
				.isLegacyReplacementOfferable(incomplete));
		assertTrue(SpatialIdentityRegistry
				.isLegacyReplacementOfferable(complete));
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private GeoIdentityRecord register(GeoNumeric geo) {
		SpatialRedefineSignature signature = new SpatialRedefineSignature(
				ConstructionGeoRedefineProvider.PROVIDER_ID,
				ConstructionGeoRedefineProvider.familyFor(geo),
				ConstructionGeoRedefineProvider.SCHEMA_ID,
				ConstructionGeoRedefineProvider.SCHEMA_VERSION,
				EditAuthorityMode.CONSTRUCTION_DEFINED,
				ProjectionBindingRole.NOT_APPLICABLE,
				ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE, 1);
		GeoIdentityRecord record = new GeoIdentityRecord(
				registry().allocatePersistentGeoId(), signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), 0, 0);
		registry().registerParticipation(geo, record);
		return record;
	}
}

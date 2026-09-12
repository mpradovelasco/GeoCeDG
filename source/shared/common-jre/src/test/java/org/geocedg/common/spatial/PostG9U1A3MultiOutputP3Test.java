/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Complete stable-role group anchors and P3 publication evidence for A3. */
class PostG9U1A3MultiOutputP3Test extends G9A3SpatialRedefineTestSupport {
	@Test
	void completeRoleMapAnchorsMultiOutputProducerWithoutUsingOrdinal() throws Exception {
		GeoNumeric before = add("BeforeAnchor=1");
		register(before, signature("post-g9u1-a3.before", "NUMERIC",
				"VALUE", 1));
		OutputPair old = intersectPair("OldCircle", "OldLine");
		ConstructionElement oldProducer = old.left.getParentAlgorithm();
		int oldSlot = oldProducer.getConstructionIndex();
		old.left.setLabel("OldLeft");
		old.right.setLabel("OldRight");
		String providerId = "post-g9u1-a3.multi-output";
		GeoIdentityRecord left = register(old.left,
				signature(providerId, "POINT", "LEFT", 2));
		final GeoIdentityRecord right = register(old.right,
				signature(providerId, "POINT", "RIGHT", 2));
		GeoNumeric middle = add("MiddleAnchor=2");
		final GeoNumeric candidateRadius = add("CandidateRadius=3");
		add("CandidateCircle=Circle((0,0),CandidateRadius)");
		add("CandidateLine:y=0");
		OutputPair candidate = intersectExisting("CandidateCircle",
				"CandidateLine");
		ConstructionElement candidateProducer = candidate.left.getParentAlgorithm();
		int requiredPredecessorBound = candidateProducer.getMinConstructionIndex();
		int requiredDependentBound = oldProducer.getMaxConstructionIndex();
		int assessedDestination = Math.max(requiredPredecessorBound,
				Math.min(oldSlot, requiredDependentBound));
		assertTrue(before.getConstructionIndex() < oldSlot);
		assertTrue(oldSlot < middle.getConstructionIndex());
		assertTrue(oldSlot < requiredPredecessorBound);
		assertEquals(requiredPredecessorBound, assessedDestination);
		GeoNumeric after = add("AfterAnchor=4");
		register(after, signature("post-g9u1-a3.after", "NUMERIC",
				"VALUE", 1));
		List<ConstructionElement> unaffected = new ArrayList<>();
		for (int index = 0; index < getConstruction().steps(); index++) {
			ConstructionElement element = getConstruction()
					.getConstructionElement(index);
			if (element != oldProducer && element != candidateProducer) {
				unaffected.add(element);
			}
		}
		GroupProvider provider = new GroupProvider(providerId, candidate.left,
				candidate.right, "LEFT", "RIGHT",
				SpatialRedefineDecision.RETAIN) {
			@Override
			public boolean requiresProceduralPositionPreservation(
					SpatialRedefineContext context,
					SpatialRedefineProposal proposal,
					SpatialRedefineDecision decision) {
				return decision == SpatialRedefineDecision.RETAIN;
			}
		};
		registry().registerRedefineProvider(provider);
		SpatialRedefineContext context = registry()
				.captureRedefineContext(old.right);
		SpatialRedefineTransaction transaction = registry().prepareRedefine(
				context, candidate.right,
				List.of(candidate.right, candidate.left), false);
		assertEquals(left.getId(), transaction.getDecidedId("LEFT"));
		assertEquals(right.getId(), transaction.getDecidedId("RIGHT"));

		EvalInfo info = new EvalInfo(true).withSpatialRedefineContext(context)
				.withSpatialRedefineTransaction(transaction);
		getConstruction().replace(old.right, candidate.right, info);
		getConstruction().completeSpatialRedefineOperation(context);

		GeoElement actualLeft = registry().getGeo(left.getId());
		GeoElement actualRight = registry().getGeo(right.getId());
		assertSame(actualLeft.getParentAlgorithm(),
				actualRight.getParentAlgorithm());
		ConstructionElement actualProducer = actualLeft.getParentAlgorithm();
		int producerIndex = actualProducer.getConstructionIndex();
		int applicableDestination = Math.max(actualProducer.getMinConstructionIndex(),
				Math.min(oldSlot, actualProducer.getMaxConstructionIndex()));
		assertEquals(applicableDestination, producerIndex);
		assertTrue(producerIndex != oldSlot);
		assertTrue(candidateRadius.getConstructionIndex() < producerIndex);
		int previousUnaffectedIndex = -1;
		for (ConstructionElement element : unaffected) {
			assertTrue(previousUnaffectedIndex < element.getConstructionIndex());
			previousUnaffectedIndex = element.getConstructionIndex();
		}
		assertEquals("LEFT", registry().getGeoRecord(left.getId())
				.getStableOutputRole());
		assertEquals("RIGHT", registry().getGeoRecord(right.getId())
				.getStableOutputRole());
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ReservedKernelReference;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 piece I-6 (finding H06; {@code locus-v2-intersections.md} CU3.5; decision D-CU3-8,
 * option A): the kernel axes are unbounded line targets of a Locus V2 intersection, named in its
 * durable relation by their reserved identities, never by a label and never by a hidden ordinary
 * line. Validation matrix rows V-H3-1 to V-H3-4: creation, persistence through saving, reopening,
 * Undo, Redo and copying, a typed message for a target that stays unsupported, and no hidden
 * object.
 */
final class PreG9BCu3ReservedAxisTargetTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void theAxesAreUnboundedLineTargets() {
		circleLocus();
		final int elements = getConstruction().steps();
		GeoLocusIntersectionResult onX = add("X=Intersect(L,xAxis)");
		GeoLocusIntersectionResult onY = add("Y=Intersect(L,yAxis)");
		GeoLocusIntersectionResult explicit = add("Z=Intersect(L,y=0)");

		assertNotNull(onX, "V-H3-1: Intersect(L, xAxis)");
		assertNotNull(onY, "V-H3-1: Intersect(L, yAxis)");
		assertEquals(2, admissible(onX));
		assertEquals(2, admissible(onY));
		assertEquals(parameters(explicit), parameters(onX),
				"the axis is the line y = 0 as a target");
		for (LocusIntersectionSolution2D solution : rich(onY).getFiniteSolutions()) {
			assertEquals(0, solution.getEvaluatedPoint().getX(), 1E-9);
			assertEquals(3, Math.abs(solution.getEvaluatedPoint().getY()), 1E-9);
		}
		Construction construction = getConstruction();
		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		assertNull(registry.getPersistentGeoId(construction.getXAxis()),
				"the axis never becomes an ordinary participant");
		assertEquals(ReservedKernelReference.X_AXIS,
				registry.getDurableReferenceId(construction.getXAxis()));
		assertTrue(record(onX).getDependencies().contains(ReservedKernelReference.X_AXIS));
		assertTrue(record(onY).getDependencies().contains(ReservedKernelReference.Y_AXIS));
		assertEquals(elements + 4, construction.steps(),
				"V-H3-4: the three results and the line y = 0, no hidden line for an axis");
		assertSame(construction.getXAxis(),
				((AlgoLocusIntersectionV2) onX.getParentAlgorithm()).getTarget());
	}

	@Test
	void theReservedIdentityPersistsThroughSavingUndoAndRedo() {
		activateUndo();
		circleLocus();
		getApp().storeUndoInfo();
		add("X=Intersect(L,xAxis)");
		getApp().storeUndoInfo();
		List<String> tokens = tokens("X");
		String spatial = getConstruction().getSpatialIdentityRegistry().writeSpatialSection();
		assertTrue(spatial.contains(ReservedKernelReference.X_AXIS.toExternalForm()), spatial);

		reload();

		assertEquals(tokens, tokens("X"), "V-H3-2: reopening keeps the exact tokens");
		assertEquals(2, admissible((GeoLocusIntersectionResult) lookup("X")));
		getApp().getUndoManager().undo();
		assertNull(lookup("X"), "Undo removes the result");
		getApp().getUndoManager().redo();
		assertEquals(tokens, tokens("X"), "Redo restores the same tokens");
		assertEquals(2, admissible((GeoLocusIntersectionResult) lookup("X")));
	}

	@Test
	void aCopyNamesTheAxisOfItsDestination() {
		circleLocus();
		add("X=Intersect(L,xAxis)");
		List<GeoElement> selection = List.of(lookup("X"));
		String clipboard = InternalClipboard.getTextToSave(getApp(), selection, text -> text);
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(List.of(clipboard.substring(0, separator).trim()
				.split(" ")));
		assertFalse(labels.contains("xAxis"), "the axis is never copied");

		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator + 1));

		GeoLocusIntersectionResult copy = (GeoLocusIntersectionResult) lookup("X_{1}");
		assertNotNull(copy, "V-H3-2: the copy is pasted");
		assertSame(getConstruction().getXAxis(),
				((AlgoLocusIntersectionV2) copy.getParentAlgorithm()).getTarget());
		assertTrue(record(copy).getDependencies().contains(ReservedKernelReference.X_AXIS));
		assertEquals(2, admissible(copy));
	}

	@Test
	void aTargetThatStaysUnsupportedHasATypedDiagnostic() {
		circleLocus();
		add("q=Polygon((0,0),(1,0),(0,1))");
		GeoLocusIntersectionResult result = add("W=Intersect(L,q)");

		assertNotNull(result, "V-H3-3: a typed result, never a generic error");
		LocusIntersectionResult2D rich = rich(result);
		assertEquals(ComputationStatus.UNSUPPORTED, rich.getComputationStatus());
		assertFalse(rich.getDiagnostics().isEmpty());
	}

	@Test
	void aReservedTokenIsNeverTheIdentityOfARecord() {
		assertTrue(ReservedKernelReference.isReservedToken(
				ReservedKernelReference.X_AXIS.getRawToken()));
		assertTrue(ReservedKernelReference.isReserved(new PersistentGeoId(
				ReservedKernelReference.Y_AXIS.getRawToken())));
		circleLocus();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(geo);
			assertFalse(id != null && ReservedKernelReference.isReserved(id), geo.toString());
		}
	}

	// ---------------------------------------------------------------- helpers

	/** The circle of radius 3 traced by {@code Dilate(P, 1.5, O)} with P on a circle of radius 2. */
	private void circleLocus() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("Q=Dilate(P,1.5,O)");
		add("L=LocusV2(Q,P)");
	}

	private GeoIdentityRecord record(GeoElement geo) {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		return (GeoIdentityRecord) registry.getRecord(registry.getPersistentGeoId(geo));
	}

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		assertNotNull(result);
		return result.getIntersectionResult();
	}

	private static int admissible(GeoLocusIntersectionResult result) {
		int count = 0;
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			count += result.isPointAdmissible(solution.getIdentity().getRootToken()) ? 1 : 0;
		}
		return count;
	}

	private static List<Double> parameters(GeoLocusIntersectionResult result) {
		List<Double> parameters = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			parameters.add(solution.getRevisionEvidence().getSemanticParameter());
		}
		return parameters;
	}

	private List<String> tokens(String label) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich((GeoLocusIntersectionResult)
				lookup(label)).getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}
}

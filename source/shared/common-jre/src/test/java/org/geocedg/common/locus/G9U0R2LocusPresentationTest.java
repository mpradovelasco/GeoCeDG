/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricValue2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphicsCommon;
import org.geogebra.common.awt.GPaint;
import org.geogebra.common.awt.GPathIterator;
import org.geogebra.common.awt.GShape;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.properties.factory.GeoElementPropertiesFactory;
import org.geogebra.common.properties.impl.objects.CaptionProperty;
import org.geogebra.common.properties.impl.objects.CaptionStyleProperty;
import org.geogebra.common.properties.impl.objects.ElementColorProperty;
import org.geogebra.common.properties.impl.objects.LabelProperty;
import org.geogebra.common.properties.impl.objects.LineOpacityProperty;
import org.geogebra.common.properties.impl.objects.LineStyleProperty;
import org.geogebra.common.properties.impl.objects.ShowObjectProperty;
import org.geogebra.common.properties.impl.objects.ThicknessProperty;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

class G9U0R2LocusPresentationTest extends G9U0PublicSurfaceTestBase {
	private static final int COPY_LINE_OPACITY = 102;

	// R2-L01
	@Test
	void ordinaryColorsRepaintWithoutSemanticMutation() throws Exception {
		GeoLocusV2 locus = createParabola();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult intersection = intersect(locus, "y=1");
		SemanticWitness witness = SemanticWitness.capture(locus, metric,
				intersection);

		for (GColor color : List.of(GColor.RED, GColor.BLUE, GColor.GREEN)) {
			new ElementColorProperty(getApp().getLocalization(), locus)
					.setValue(color);
			RecordingGraphics graphics = draw(locus, 80);
			assertEquals(color, locus.getObjectColor());
			assertTrue(graphics.paints.contains(color));
			witness.assertUnchanged(locus, metric, intersection);
		}
		LineOpacityProperty.forLine(getApp().getLocalization(), locus)
				.setValue(40);
		RecordingGraphics translucent = draw(locus, 80);
		assertEquals(COPY_LINE_OPACITY, locus.getLineOpacity());
		assertTrue(translucent.paints
				.contains(GColor.GREEN.deriveWithAlpha(COPY_LINE_OPACITY)));
		witness.assertUnchanged(locus, metric, intersection);
	}

	// R2-L02
	@Test
	void minDefaultAndLargeThicknessAreScaleIndependentPresentation()
			throws Exception {
		GeoLocusV2 locus = createParabola();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult intersection = intersect(locus, "y=1");
		SemanticWitness witness = SemanticWitness.capture(locus, metric,
				intersection);
		ThicknessProperty property = new ThicknessProperty(
				getApp().getLocalization(), locus);
		int defaultThickness = locus.getLineThickness();
		List<Integer> thicknesses = List.of(property.getMin(), defaultThickness,
				property.getMax());
		List<Double> firstScaleWidths = new ArrayList<>();

		for (int thickness : thicknesses) {
			property.setValue(thickness);
			double referenceWidth = maximumStrokeWidth(draw(locus, 40));
			firstScaleWidths.add(referenceWidth);
			assertEquals(referenceWidth, maximumStrokeWidth(draw(locus, 80)),
					1e-9);
			assertEquals(referenceWidth, maximumStrokeWidth(draw(locus, 160)),
					1e-9);
			assertEquals(1, minimumPathMoveCount(draw(locus, 80)));
			witness.assertUnchanged(locus, metric, intersection);
		}

		assertTrue(firstScaleWidths.get(0) <= firstScaleWidths.get(1));
		assertTrue(firstScaleWidths.get(1) <= firstScaleWidths.get(2));
		assertTrue(firstScaleWidths.get(0) < firstScaleWidths.get(2));
	}

	// R2-L03
	@Test
	void everyOrdinaryLineTypePersistsWithoutChangingSubpaths()
			throws Exception {
		GeoLocusV2 locus = canonicalReopen(createParabola());
		PersistentGeoId identity = locus.getPersistentLocusId();
		long revision = locus.getSemanticRevision();

		for (int lineType : EuclidianStyleConstants.lineStyleList) {
			new LineStyleProperty(getApp().getLocalization(), locus)
					.setValue(lineType);
			RecordingGraphics graphics = draw(locus, 80);
			assertEquals(1, minimumPathMoveCount(graphics));
			if (lineType == EuclidianStyleConstants.LINE_TYPE_FULL) {
				assertTrue(graphics.strokes.stream()
						.allMatch(stroke -> stroke.getDashArray() == null));
			} else {
				assertTrue(graphics.strokes.stream()
						.anyMatch(stroke -> stroke.getDashArray() != null));
			}

			String xml = getApp().getXML();
			getApp().getXMLio().processXMLString(xml, true, false, false);
			locus = (GeoLocusV2) requireLookup("L");
			assertEquals(lineType, locus.getLineType());
			assertEquals(identity, locus.getPersistentLocusId());
			assertEquals(revision, locus.getSemanticRevision());
			assertEquals(1, minimumPathMoveCount(draw(locus, 80)));
		}
	}

	// R2-L04
	@Test
	void propertiesExposeLineControlsForSingleAndCompatibleMultiselection()
			throws Exception {
		GeoLocusV2 locus = createParabola();
		final GeoElement line = add("ordinaryLine:y=0");
		GeoElementPropertiesFactory factory = new GeoElementPropertiesFactory();

		assertTrue(locus.showLineProperties());
		assertTrue(locus.hasLineOpacity());
		assertFalse(locus.isPath());
		assertFalse(locus.isFillable());
		assertNotNull(factory.createLineStyleProperty(getApp().getLocalization(),
				Collections.singletonList(locus)));
		assertNotNull(factory.createThicknessProperty(getApp().getLocalization(),
				Collections.singletonList(locus)));
		assertNotNull(LineOpacityProperty.forLine(getApp().getLocalization(),
				locus));
		List<GeoElement> compatibleSelection = List.of(locus, line);
		assertNotNull(factory.createLineStyleProperty(getApp().getLocalization(),
				compatibleSelection));
		assertNotNull(factory.createThicknessProperty(getApp().getLocalization(),
				compatibleSelection));

		getApp().getSelectionManager().addSelectedGeo(locus);
		getApp().getSelectionManager().addSelectedGeo(line);
		assertEquals(2, getApp().getSelectionManager().getSelectedGeos().size());
		assertFalse(locus.isPath());
	}

	// R2-L05
	@Test
	void completePresentationPersistsAcrossTwoNativeXmlReopens()
			throws Exception {
		GeoLocusV2 locus = canonicalReopen(createParabola());
		PersistentGeoId identity = locus.getPersistentLocusId();
		long revision = locus.getSemanticRevision();
		Class<?> parentType = locus.getParentAlgorithm().getClass();
		String generator = generatorSignature(locus);
		int lineType = alternateLineType(locus);
		applyCopyPresentation(locus, lineType);
		locus.setEuclidianVisible(false);
		locus.setCaption("R2 curve");
		locus.setLabelMode(GeoElementND.LABEL_CAPTION);
		locus.setLabelVisible(true);

		for (int reopen = 0; reopen < 2; reopen++) {
			String xml = getApp().getXML();
			assertTrue(xml.contains("<lineStyle thickness=\"8\""));
			assertTrue(xml.contains("opacity=\"102\""));
			getApp().getXMLio().processXMLString(xml, true, false, false);
			locus = (GeoLocusV2) requireLookup("L");
			assertEquals(identity, locus.getPersistentLocusId());
			assertEquals(revision, locus.getSemanticRevision());
			assertEquals(parentType, locus.getParentAlgorithm().getClass());
			assertEquals(generator, generatorSignature(locus));
			assertCopiedPresentation(locus, lineType);
			assertFalse(locus.isEuclidianVisible());
			assertTrue(locus.isLabelVisible());
			assertEquals(GeoElementND.LABEL_CAPTION, locus.getLabelMode());
			assertEquals("R2 curve",
					locus.getCaption(StringTemplate.defaultTemplate));
		}
	}

	// R2-L06
	@Test
	void clipboardCopyPreservesStyleAndRemapsOwnedIdsInBothScopes() {
		GeoLocusV2 source = createParabola();
		PersistentGeoId sourceId = source.getPersistentLocusId();
		final String sourceProvider = source.getSemanticDefinition().getProvider()
				.getSemanticSignature();
		final List<String> sourceBranches = branchSignatures(source);
		final String sourceGenerator = generatorSignature(source);
		final List<PersistentGeoId> sourceOwnedIds =
				ownedGeoIds(getConstruction());
		int lineType = alternateLineType(source);
		applyCopyPresentation(source, lineType);
		final PresentationState sourceStyle = PresentationState.capture(source);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(source), text -> text);

		paste(getApp(), clipboard);
		GeoLocusV2 withinConstruction = copiedLocus(getConstruction(), sourceId);
		assertNotEquals(sourceId, withinConstruction.getPersistentLocusId());
		assertCopiedPresentation(withinConstruction, lineType);
		assertEquals(GeoElementND.LABEL_NAME_VALUE,
				withinConstruction.getLabelMode());
		assertTrue(withinConstruction.isEuclidianVisible());
		assertFalse(withinConstruction.isPath());
		assertEquals(sourceId, recordFor(withinConstruction).getCopySourceId());
		assertOwnedIdsRemapped(getConstruction(), sourceOwnedIds);

		AppCommon target = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		paste(target, clipboard);
		GeoLocusV2 acrossConstructions = copiedLocus(
				target.getKernel().getConstruction(), sourceId);
		assertNotEquals(sourceId, acrossConstructions.getPersistentLocusId());
		assertCopiedPresentation(acrossConstructions, lineType);
		assertEquals(GeoElementND.LABEL_NAME_VALUE,
				acrossConstructions.getLabelMode());
		assertTrue(acrossConstructions.isEuclidianVisible());
		assertFalse(acrossConstructions.isPath());
		assertEquals(sourceId,
				recordFor(acrossConstructions).getCopySourceId());
		assertOwnedIdsRemapped(target.getKernel().getConstruction(),
				sourceOwnedIds);

		sourceStyle.assertEquals(source);
		assertEquals(sourceId, source.getPersistentLocusId());
		assertEquals(sourceProvider, source.getSemanticDefinition().getProvider()
				.getSemanticSignature());
		assertEquals(sourceBranches, branchSignatures(source));
		assertEquals(sourceGenerator, generatorSignature(source));
	}

	// R2-L07
	@Test
	void everyHostUndoablePresentationPropertyRestoresWithoutNewIdentity()
			throws Exception {
		GeoLocusV2 locus = canonicalReopen(createParabola());
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult intersection = intersect(locus, "y=1");
		final DurableSemanticWitness witness = DurableSemanticWitness.capture(locus,
				metric, intersection);
		getKernel().setUndoActive(true);
		getKernel().initUndoInfo();
		getApp().storeUndoInfo();
		List<PresentationState> states = new ArrayList<>();
		states.add(PresentationState.capture(locus));

		new ElementColorProperty(getApp().getLocalization(), locus)
				.setValue(GColor.RED);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		new ThicknessProperty(getApp().getLocalization(), locus).setValue(9);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		new LineStyleProperty(getApp().getLocalization(), locus)
				.setValue(alternateLineType(locus));
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		LineOpacityProperty.forLine(getApp().getLocalization(), locus)
				.setValue(40);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(false);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(true);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));
		new LabelProperty(getApp().getLocalization(), locus)
				.setValue(GeoElementND.LABEL_CAPTION_VALUE);
		getApp().storeUndoInfo();
		states.add(PresentationState.capture(locus));

		for (int index = states.size() - 2; index >= 0; index--) {
			getKernel().undo();
			locus = (GeoLocusV2) requireLookup("L");
			metric = (GeoLocusMetricResult) requireLookup("M");
			intersection = (GeoLocusIntersectionResult) requireLookup("R");
			states.get(index).assertEquals(locus);
			witness.assertEquivalent(locus, metric, intersection);
		}
		for (int index = 1; index < states.size(); index++) {
			getKernel().redo();
			locus = (GeoLocusV2) requireLookup("L");
			metric = (GeoLocusMetricResult) requireLookup("M");
			intersection = (GeoLocusIntersectionResult) requireLookup("R");
			states.get(index).assertEquals(locus);
			witness.assertEquivalent(locus, metric, intersection);
		}
	}

	// R2-L08
	@Test
	void everyPresentationMutationPreservesTheCompleteSemanticWitness()
			throws Exception {
		GeoLocusV2 locus = createParabola();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult intersection = intersect(locus, "y=1");
		SemanticWitness witness = SemanticWitness.capture(locus, metric,
				intersection);

		new ElementColorProperty(getApp().getLocalization(), locus)
				.setValue(GColor.RED);
		witness.assertUnchanged(locus, metric, intersection);
		new ThicknessProperty(getApp().getLocalization(), locus).setValue(8);
		witness.assertUnchanged(locus, metric, intersection);
		new LineStyleProperty(getApp().getLocalization(), locus)
				.setValue(alternateLineType(locus));
		witness.assertUnchanged(locus, metric, intersection);
		LineOpacityProperty.forLine(getApp().getLocalization(), locus)
				.setValue(40);
		witness.assertUnchanged(locus, metric, intersection);
		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(false);
		witness.assertUnchanged(locus, metric, intersection);
		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(true);
		new LabelProperty(getApp().getLocalization(), locus)
				.setValue(GeoElementND.LABEL_NAME_VALUE);
		witness.assertUnchanged(locus, metric, intersection);
		new CaptionStyleProperty(getApp().getLocalization(), locus)
				.setValue(GeoElementND.LABEL_CAPTION);
		witness.assertUnchanged(locus, metric, intersection);
		new CaptionProperty(getApp().getLocalization(), locus)
				.setValue("R2 semantic-neutral caption");
		witness.assertUnchanged(locus, metric, intersection);
		locus.setSelected(true);
		locus.setHighlighted(true);
		witness.assertUnchanged(locus, metric, intersection);
		assertEquals(GeoElement.class,
				GeoLocusV2.class.getMethod("updateRepaint").getDeclaringClass());
		assertEquals(GeoElement.class, GeoLocusV2.class
				.getMethod("updateRepaint", boolean.class).getDeclaringClass());
		witness.assertDirectUpdateCascades(locus, metric, intersection);
	}

	// R2-L13
	@Test
	void visibilityPersistsAndParticipatesInOrdinaryUndoRedo() throws Exception {
		GeoLocusV2 locus = canonicalReopen(createParabola());
		final PersistentGeoId identity = locus.getPersistentLocusId();
		final long revision = locus.getSemanticRevision();
		assertTrue(draw(locus, 80).shapeDraws > 0);

		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(false);
		assertEquals(0, draw(locus, 80).shapeDraws);
		String hiddenXml = getApp().getXML();
		getApp().getXMLio().processXMLString(hiddenXml, true, false, false);
		locus = (GeoLocusV2) requireLookup("L");
		assertFalse(locus.isEuclidianVisible());
		assertEquals(0, draw(locus, 80).shapeDraws);

		getKernel().setUndoActive(true);
		getKernel().initUndoInfo();
		getApp().storeUndoInfo();
		new ShowObjectProperty(getApp().getLocalization(), locus).setValue(true);
		getApp().storeUndoInfo();
		assertTrue(draw(locus, 80).shapeDraws > 0);
		getKernel().undo();
		locus = (GeoLocusV2) requireLookup("L");
		assertFalse(locus.isEuclidianVisible());
		assertEquals(0, draw(locus, 80).shapeDraws);
		getKernel().redo();
		locus = (GeoLocusV2) requireLookup("L");
		assertTrue(locus.isEuclidianVisible());
		assertTrue(draw(locus, 80).shapeDraws > 0);
		assertEquals(identity, locus.getPersistentLocusId());
		assertEquals(revision, locus.getSemanticRevision());
	}

	// R2-L14
	@Test
	void everySupportedLabelModeDrawsAndPersistsWithStyleAndCopy()
			throws Exception {
		GeoLocusV2 locus = canonicalReopen(createParabola());
		final long revision = locus.getSemanticRevision();
		locus.setCaption("R2 curve label");
		int[] modes = {GeoElementND.LABEL_NAME, GeoElementND.LABEL_NAME_VALUE,
				GeoElementND.LABEL_VALUE, GeoElementND.LABEL_CAPTION,
				GeoElementND.LABEL_CAPTION_VALUE};
		for (int mode : modes) {
			new LabelProperty(getApp().getLocalization(), locus).setValue(mode);
			assertFalse(draw(locus, 80).strings.isEmpty());
		}
		new LabelProperty(getApp().getLocalization(), locus)
				.setValue(GeoElementND.LABEL_HIDDEN);
		assertTrue(draw(locus, 80).strings.isEmpty());

		int lineType = alternateLineType(locus);
		applyCopyPresentation(locus, lineType);
		locus.setCaption("R2 curve label");
		locus.setLabelMode(GeoElementND.LABEL_CAPTION_VALUE);
		locus.setLabelVisible(true);
		GeoLocusV2 copy = (GeoLocusV2) locus.copyInternal(getConstruction());
		assertCopiedPresentation(copy, lineType);
		assertEquals(GeoElementND.LABEL_CAPTION_VALUE, copy.getLabelMode());
		assertTrue(copy.getLabelVisible());
		assertFalse(copy.isPath());

		String xml = getApp().getXML();
		getApp().getXMLio().processXMLString(xml, true, false, false);
		locus = (GeoLocusV2) requireLookup("L");
		assertCopiedPresentation(locus, lineType);
		assertEquals(GeoElementND.LABEL_CAPTION_VALUE, locus.getLabelMode());
		assertTrue(locus.isLabelVisible());
		assertFalse(draw(locus, 80).strings.isEmpty());
		assertEquals(revision, locus.getSemanticRevision());
	}

	// R2-L15
	@Test
	void transientSelectionHandlesDeselectionMultiselectCrossingAndGap() {
		GeoLocusV2 locus = createDisconnectedLine();
		final GeoElement crossing = add("crossing:x=1.5");
		final long revision = locus.getSemanticRevision();
		final String persistentStyle = locus.getStyleXML();
		DrawLocusV2 drawable = drawable(locus, 80);
		assertTrue(drawable.hit(520, 300, 5));
		assertFalse(drawable.hit(400, 300, 5));
		RecordingGraphics ordinary = new RecordingGraphics();
		drawable.draw(ordinary);

		getApp().getSelectionManager().addSelectedGeo(locus);
		locus.setHighlighted(true);
		RecordingGraphics hovered = new RecordingGraphics();
		drawable.draw(hovered);
		assertTrue(hovered.shapeDraws > ordinary.shapeDraws);
		assertTrue(getApp().getSelectionManager().containsSelectedGeo(locus));

		getApp().getSelectionManager().removeSelectedGeo(locus);
		locus.setHighlighted(false);
		assertFalse(getApp().getSelectionManager().containsSelectedGeo(locus));
		RecordingGraphics deselected = new RecordingGraphics();
		drawable.draw(deselected);
		assertEquals(ordinary.shapeDraws, deselected.shapeDraws);

		getApp().getSelectionManager().addSelectedGeo(locus);
		getApp().getSelectionManager().addSelectedGeo(crossing);
		assertEquals(2, getApp().getSelectionManager().getSelectedGeos().size());
		assertEquals(persistentStyle, locus.getStyleXML());
		assertEquals(revision, locus.getSemanticRevision());
	}

	private GeoLocusV2 canonicalReopen(GeoLocusV2 locus) throws Exception {
		String label = locus.getLabelSimple();
		getApp().getXMLio().processXMLString(getApp().getXML(), true, false, false);
		return (GeoLocusV2) requireLookup(label);
	}

	private DrawLocusV2 drawable(GeoLocusV2 locus, double scale) {
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(400, 300, scale, scale);
		return new DrawLocusV2(view, locus);
	}

	private RecordingGraphics draw(GeoLocusV2 locus, double scale) {
		RecordingGraphics graphics = new RecordingGraphics();
		drawable(locus, scale).draw(graphics);
		return graphics;
	}

	private static int alternateLineType(GeoLocusV2 locus) {
		return EuclidianStyleConstants.lineStyleList.stream()
				.filter(type -> type != locus.getLineType()).findFirst()
				.orElseThrow();
	}

	private static double maximumStrokeWidth(RecordingGraphics graphics) {
		return graphics.strokes.stream().mapToDouble(GBasicStroke::getLineWidth)
				.max().orElseThrow();
	}

	private static int minimumPathMoveCount(RecordingGraphics graphics) {
		return graphics.pathMoveCounts.stream().mapToInt(Integer::intValue)
				.min().orElseThrow();
	}

	private static int pathMoveCount(GShape shape) {
		int moves = 0;
		GPathIterator iterator = shape.getPathIterator(null);
		double[] coordinates = new double[6];
		while (!iterator.isDone()) {
			if (iterator.currentSegment(coordinates) == GPathIterator.SEG_MOVETO) {
				moves++;
			}
			iterator.next();
		}
		return moves;
	}

	private static String generatorSignature(GeoLocusV2 locus) {
		return ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getGeneratorDescriptor().getSemanticSignature();
	}

	private static List<String> branchSignatures(GeoLocusV2 locus) {
		return locus.getSemanticDefinition().getBranches().stream()
				.map(LocusBranch2D::getSemanticSignature).toList();
	}

	private static void applyCopyPresentation(GeoLocusV2 locus, int lineType) {
		locus.setObjColor(GColor.GREEN);
		locus.setLineThickness(8);
		locus.setLineType(lineType);
		locus.setLineOpacity(COPY_LINE_OPACITY);
		locus.setLabelMode(GeoElementND.LABEL_NAME_VALUE);
		locus.setLabelVisible(true);
	}

	private static void assertCopiedPresentation(GeoLocusV2 locus,
			int lineType) {
		assertEquals(GColor.GREEN, locus.getObjectColor());
		assertEquals(8, locus.getLineThickness());
		assertEquals(lineType, locus.getLineType());
		assertEquals(COPY_LINE_OPACITY, locus.getLineOpacity());
		assertTrue(locus.getLabelVisible());
	}

	private static GeoIdentityRecord recordFor(GeoLocusV2 locus) {
		return locus.getConstruction().getSpatialIdentityRegistry()
				.getGeoRecord(locus.getPersistentLocusId());
	}

	private static GeoLocusV2 copiedLocus(Construction construction,
			PersistentGeoId sourceId) {
		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		return registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> sourceId.equals(record.getCopySourceId()))
				.map(record -> registry.getGeo(record.getId()))
				.filter(GeoLocusV2.class::isInstance).map(GeoLocusV2.class::cast)
				.findFirst().orElseThrow();
	}

	private static List<PersistentGeoId> ownedGeoIds(
			Construction construction) {
		return construction.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast).map(GeoIdentityRecord::getId)
				.toList();
	}

	private static void assertOwnedIdsRemapped(Construction construction,
			List<PersistentGeoId> sourceIds) {
		List<GeoIdentityRecord> copies = construction.getSpatialIdentityRegistry()
				.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> record.getCopySourceId() != null).toList();
		assertFalse(copies.isEmpty());
		assertTrue(copies.stream().allMatch(record ->
				sourceIds.contains(record.getCopySourceId())
						&& !sourceIds.contains(record.getId())));
	}

	private static void paste(AppCommon app, String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(app, labels,
				clipboard.substring(separator));
	}

	private static final class RecordingGraphics extends GGraphicsCommon {
		private final List<GPaint> paints = new ArrayList<>();
		private final List<GBasicStroke> strokes = new ArrayList<>();
		private final List<Integer> pathMoveCounts = new ArrayList<>();
		private final List<String> strings = new ArrayList<>();
		private int shapeDraws;

		@Override
		public void setPaint(GPaint paint) {
			paints.add(paint);
		}

		@Override
		public void setStroke(GBasicStroke stroke) {
			strokes.add(stroke);
		}

		@Override
		public void draw(GShape shape) {
			super.draw(shape);
			shapeDraws++;
			pathMoveCounts.add(pathMoveCount(shape));
		}

		@Override
		public void drawString(String text, int x, int y) {
			strings.add(text);
		}

		@Override
		public void drawString(String text, double x, double y) {
			strings.add(text);
		}
	}

	private record PresentationState(GColor color, int thickness, int lineType,
			int lineOpacity, boolean visible, boolean labelVisible, int labelMode,
			String caption) {

		static PresentationState capture(GeoLocusV2 locus) {
			return new PresentationState(locus.getObjectColor(),
					locus.getLineThickness(), locus.getLineType(),
					locus.getLineOpacity(),
					locus.isEuclidianVisible(), locus.isLabelVisible(),
					locus.getLabelMode(),
					locus.getCaption(StringTemplate.defaultTemplate));
		}

		void assertEquals(GeoLocusV2 locus) {
			org.junit.jupiter.api.Assertions.assertEquals(color,
					locus.getObjectColor());
			org.junit.jupiter.api.Assertions.assertEquals(thickness,
					locus.getLineThickness());
			org.junit.jupiter.api.Assertions.assertEquals(lineType,
					locus.getLineType());
			org.junit.jupiter.api.Assertions.assertEquals(lineOpacity,
					locus.getLineOpacity());
			org.junit.jupiter.api.Assertions.assertEquals(visible,
					locus.isEuclidianVisible());
			org.junit.jupiter.api.Assertions.assertEquals(labelVisible,
					locus.isLabelVisible());
			org.junit.jupiter.api.Assertions.assertEquals(labelMode,
					locus.getLabelMode());
			org.junit.jupiter.api.Assertions.assertEquals(caption,
					locus.getCaption(StringTemplate.defaultTemplate));
		}
	}

	private record DurableSemanticWitness(PersistentGeoId locusIdentity,
			PersistentGeoId metricIdentity, PersistentGeoId intersectionIdentity,
			long revision, String providerSignature, String generatorSignature,
			List<String> branchSignatures, MetricValue2D metricValue,
			String tokenLedger, List<PersistentGeoId> dependencies,
			long definitionRevision, long topologyRevision) {

		static DurableSemanticWitness capture(GeoLocusV2 locus,
				GeoLocusMetricResult metric,
				GeoLocusIntersectionResult intersection) {
			SpatialIdentityRegistry registry = locus.getConstruction()
					.getSpatialIdentityRegistry();
			GeoIdentityRecord record = recordFor(locus);
			PersistentGeoId metricId = registry.getPersistentGeoId(metric);
			PersistentGeoId intersectionId = registry
					.getPersistentGeoId(intersection);
			assertNotNull(metricId);
			assertNotNull(intersectionId);
			return new DurableSemanticWitness(locus.getPersistentLocusId(),
					metricId, intersectionId, locus.getSemanticRevision(),
					locus.getSemanticDefinition().getProvider()
							.getSemanticSignature(),
					G9U0R2LocusPresentationTest.generatorSignature(locus),
					G9U0R2LocusPresentationTest.branchSignatures(locus),
					metric.getMetricResult().getMetricValue(),
					intersection.getTokenLedgerState(), record.getDependencies(),
					record.getDefinitionRevision(), record.getTopologyRevision());
		}

		void assertEquivalent(GeoLocusV2 locus, GeoLocusMetricResult metric,
				GeoLocusIntersectionResult intersection) {
			SpatialIdentityRegistry registry = locus.getConstruction()
					.getSpatialIdentityRegistry();
			final GeoIdentityRecord record = recordFor(locus);
			assertEquals(locusIdentity, locus.getPersistentLocusId());
			assertEquals(metricIdentity, registry.getPersistentGeoId(metric));
			assertEquals(intersectionIdentity,
					registry.getPersistentGeoId(intersection));
			assertEquals(revision, locus.getSemanticRevision());
			assertEquals(providerSignature, locus.getSemanticDefinition()
					.getProvider().getSemanticSignature());
			assertEquals(generatorSignature,
					G9U0R2LocusPresentationTest.generatorSignature(locus));
			assertEquals(branchSignatures,
					G9U0R2LocusPresentationTest.branchSignatures(locus));
			assertEquals(metricValue,
					metric.getMetricResult().getMetricValue());
			assertEquals(tokenLedger, intersection.getTokenLedgerState());
			assertEquals(dependencies, record.getDependencies());
			assertEquals(definitionRevision, record.getDefinitionRevision());
			assertEquals(topologyRevision, record.getTopologyRevision());
		}
	}

	private record SemanticWitness(PersistentGeoId identity,
			LocusDefinition2D definition, long revision,
			String providerSignature, String generatorSignature,
			List<String> branchSignatures, LocusMetricResult2D metric,
			LocusIntersectionResult2D intersection, String tokenLedger,
			GeoIdentityRecord identityRecord,
			PersistentGeoId metricIdentity,
			PersistentGeoId intersectionIdentity,
			long revisionPublications, long dependencyUpdates,
			long metricIndexHits) {

		static SemanticWitness capture(GeoLocusV2 locus,
				GeoLocusMetricResult metric,
				GeoLocusIntersectionResult intersection) {
			SpatialIdentityRegistry registry = locus.getConstruction()
					.getSpatialIdentityRegistry();
			PersistentGeoId metricId = registry.getPersistentGeoId(metric);
			PersistentGeoId intersectionId = registry
					.getPersistentGeoId(intersection);
			assertNotNull(metricId);
			assertNotNull(intersectionId);
			return new SemanticWitness(locus.getPersistentLocusId(),
					locus.getSemanticDefinition(), locus.getSemanticRevision(),
					locus.getSemanticDefinition().getProvider()
							.getSemanticSignature(),
					G9U0R2LocusPresentationTest.generatorSignature(locus),
					G9U0R2LocusPresentationTest.branchSignatures(locus),
					metric.getMetricResult(),
					intersection.getIntersectionResult(),
					intersection.getTokenLedgerState(), recordFor(locus), metricId,
					intersectionId,
					locus.getInstrumentation().getRevisionPublications(),
					locus.getInstrumentation().getDependencyUpdates(),
					locus.getMetricInstrumentation().snapshot().getIndexHits());
		}

		void assertUnchanged(GeoLocusV2 locus,
				GeoLocusMetricResult metricGeo,
				GeoLocusIntersectionResult intersectionGeo) {
			final SpatialIdentityRegistry registry = locus.getConstruction()
					.getSpatialIdentityRegistry();
			assertEquals(identity, locus.getPersistentLocusId());
			assertSame(definition, locus.getSemanticDefinition());
			assertEquals(revision, locus.getSemanticRevision());
			assertEquals(providerSignature, locus.getSemanticDefinition()
					.getProvider().getSemanticSignature());
			assertEquals(generatorSignature,
					G9U0R2LocusPresentationTest.generatorSignature(locus));
			assertEquals(branchSignatures,
					G9U0R2LocusPresentationTest.branchSignatures(locus));
			assertSame(metric, metricGeo.getMetricResult());
			assertSame(intersection, intersectionGeo.getIntersectionResult());
			assertEquals(tokenLedger, intersectionGeo.getTokenLedgerState());
			assertSame(identityRecord, recordFor(locus));
			assertEquals(metricIdentity, registry.getPersistentGeoId(metricGeo));
			assertEquals(intersectionIdentity,
					registry.getPersistentGeoId(intersectionGeo));
			assertEquals(revisionPublications,
					locus.getInstrumentation().getRevisionPublications());
			assertEquals(dependencyUpdates,
					locus.getInstrumentation().getDependencyUpdates());
			assertEquals(metricIndexHits, locus.getMetricInstrumentation()
					.snapshot().getIndexHits());
		}

		void assertDirectUpdateCascades(GeoLocusV2 locus,
				GeoLocusMetricResult metricGeo,
				GeoLocusIntersectionResult intersectionGeo) {
			long indexHitsBefore = locus.getMetricInstrumentation().snapshot()
					.getIndexHits();
			LocusMetricResult2D metricBefore = metricGeo.getMetricResult();
			LocusIntersectionResult2D intersectionBefore =
					intersectionGeo.getIntersectionResult();
			locus.updateRepaint();
			assertEquals(identity, locus.getPersistentLocusId());
			assertSame(definition, locus.getSemanticDefinition());
			assertEquals(revision, locus.getSemanticRevision());
			assertTrue(locus.getMetricInstrumentation().snapshot().getIndexHits()
					> indexHitsBefore);
			assertNotSame(metricBefore, metricGeo.getMetricResult());
			assertNotSame(intersectionBefore,
					intersectionGeo.getIntersectionResult());
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Ordinary tool dispatch to R5, without adding transform interfaces to the locus. */
class G9U1SimilarityToolsTest {

	@Test
	void allFiveOrdinaryTransformToolsConsumeSemanticSource() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoLocusV2 source = (GeoLocusV2) eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoElement axis = eval(app, "axis:y=x");
		GeoElement vector = eval(app, "v=(1,2)");
		int[] modes = {EuclidianConstants.MODE_TRANSLATE_BY_VECTOR,
				EuclidianConstants.MODE_MIRROR_AT_LINE, EuclidianConstants.MODE_MIRROR_AT_POINT,
				EuclidianConstants.MODE_ROTATE_BY_ANGLE, EuclidianConstants.MODE_DILATE_FROM_POINT};
		GeoElement[] arguments = {vector, axis, center, center, center};
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenReturn("-2");
			for (int i = 0; i < modes.length; i++) {
				GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
				assertTrue(tools.handles(modes[i], List.of(source)));
				assertNull(tools.click(modes[i], List.of(source), false));
				GeoLocusV2 image = tools.click(modes[i], List.of(arguments[i]), false);
				assertNotNull(image);
				assertTrue(image.isDefined());
				assertTrue(image.getAllPredecessors().contains(source));
				assertTrue(image.getAllPredecessors().contains(arguments[i]));
				assertEquals(modes[i] == EuclidianConstants.MODE_DILATE_FROM_POINT ? 8 : 4,
						((GeoNumeric) eval(app, "Length(" + image.getLabelSimple() + ")"))
								.getDouble(), 1E-8);
			}
		}
	}

	@Test
	void predefinedAxesFailClosedWithLocalizedOrdinaryLineGuidance() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoLocusV2 source = (GeoLocusV2) eval(app, "S=SplineV2({(-2,1),(0,1),(2,1)},3)");
		for (Locale locale : List.of(Locale.ENGLISH, new Locale("es"))) {
			app.setLanguage(locale);
			for (GeoElement axis : List.of(app.getKernel().getXAxis(),
					app.getKernel().getYAxis())) {
				final String xml = app.getXML();
				GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
				try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
					assertNull(tools.click(EuclidianConstants.MODE_MIRROR_AT_LINE,
							List.of(source, axis), false));
					dialog.verify(() -> JOptionPane.showMessageDialog(app.getMainComponent(),
							GeoCeDGProfile.getText("Similarity.OrdinaryAxisRequired",
									locale.getLanguage()),
							app.getToolName(EuclidianConstants.MODE_MIRROR_AT_LINE),
							JOptionPane.INFORMATION_MESSAGE));
				}
				assertEquals(xml, app.getXML());
				assertFalse(tools.isSelecting());
				assertNull(app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(axis));
			}
		}
	}

	@Test
	void numericCancelAndSelectionPreviewCreateNoObjects() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
		String xml = app.getXML();
		assertNull(tools.click(EuclidianConstants.MODE_DILATE_FROM_POINT,
				List.of(source, center), true));
		assertEquals(xml, app.getXML());
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenReturn(null);
			assertNull(tools.click(EuclidianConstants.MODE_DILATE_FROM_POINT,
					List.of(source, center), false));
		}
		assertEquals(xml, app.getXML());
	}

	@Test
	void dynamicNumericFactorRetainsKernelDependencyAndCollapsedRecovery() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoNumeric factor = (GeoNumeric) eval(app, "k=-2");
		GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
		GeoLocusV2 image;
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenReturn("k");
			image = tools.click(EuclidianConstants.MODE_DILATE_FROM_POINT,
					List.of(source, center), false);
		}
		assertNotNull(image);
		GeoNumeric length = (GeoNumeric) eval(app, "M=Length(" + image.getLabelSimple() + ")");
		for (double value : new double[] {0, 0.25, -1}) {
			factor.setValue(value);
			factor.updateCascade();
			assertTrue(image.isDefined());
			assertEquals(4 * Math.abs(value), length.getDouble(), 1E-8);
		}
	}

	@Test
	void invalidNumericInputRollsBackWithoutLeakingHelperObjects() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
		String xml = app.getXML();
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenReturn("0/0");
			assertNull(tools.click(EuclidianConstants.MODE_DILATE_FROM_POINT,
					List.of(source, center), false));
		}
		assertEquals(xml, app.getXML());
		assertFalse(tools.isSelecting());
	}

	@Test
	void staleOperandAfterNumericDialogCannotBeRetargeted() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
		String[] currentXml = {null};
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenAnswer(call -> {
				center.remove();
				eval(app, "O=(1,1)");
				currentXml[0] = app.getXML();
				return "2";
			});
			assertNull(tools.click(EuclidianConstants.MODE_DILATE_FROM_POINT,
					List.of(source, center), false));
		}
		assertEquals(currentXml[0], app.getXML());
		assertFalse(tools.isSelecting());
	}

	@Test
	void ordinaryControllerRoutesTwoClicksToOneSemanticTransform() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoElement center = eval(app, "O=(0,0)");
		GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController)
				app.getEuclidianView1().getEuclidianController();
		app.setMode(EuclidianConstants.MODE_DILATE_FROM_POINT);
		int before = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		Hits sourceHit = new Hits();
		sourceHit.add(source);
		Hits centerHit = new Hits();
		centerHit.add(center);
		int[] undoTransactions = {0};
		assertFalse(controller.processMode(sourceHit, false, false, changed -> {
			if (changed) {
				undoTransactions[0]++;
			}
		}));
		assertEquals(before, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any())).thenReturn("2");
			assertTrue(controller.processMode(centerHit, false, false, changed -> {
				if (changed) {
					undoTransactions[0]++;
				}
			}));
		}
		assertEquals(1, undoTransactions[0]);
		assertEquals(2, app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoLocusV2.class::isInstance).count());
	}

	@Test
	void ordinaryGeometryAndUnsupportedModesStayInInheritedController() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGSimilarityTools tools = new GeoCeDGSimilarityTools(app);
		GeoElement point = eval(app, "A=(0,0)");
		GeoElement source = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		assertFalse(tools.handles(EuclidianConstants.MODE_DILATE_FROM_POINT, List.of(point)));
		assertFalse(tools.handles(EuclidianConstants.MODE_MIRROR_AT_CIRCLE, List.of(source)));
		assertFalse(tools.handles(EuclidianConstants.MODE_MOVE, List.of(source)));
	}
}

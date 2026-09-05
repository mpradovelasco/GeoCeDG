/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.dialog.UpdateTabs;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Existing editability is presented coherently; no new redefine policy is installed. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1DefinitionAffordanceTest {

	@Test
	void propertiesShowsReadOnlySemanticDefinitionsWithoutChangingConstruction() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"u=0", "Q=(u,0)",
				"D={false,{-2,2,true,true}}", "L=LocusV2(Q,u,D)",
				"S=SplineV2({(-2,0),(0,0),(2,0)},3)", "T=Dilate(S,-2)",
				"M=LocusLength(S)", "axis:x=0", "R=Intersect(S,axis)"}) {
			eval(app, command);
		}
		JPanel panel = namePanel(app);
		for (String label : new String[] {"L", "S", "T", "M", "R"}) {
			GeoElement geo = lookup(app, label);
			String before = app.getXML();
			assertTrue(GeoCeDGDefinitionInspector.isReadOnly(geo), label);
			update(panel, geo);
			JTextField definition = definitionField(panel);
			assertTrue(SwingUtilities.isDescendingFrom(definition, panel), label);
			assertFalse(definition.isEditable(), label);
			assertEquals(GeoCeDGDefinitionInspector.definition(geo), definition.getText(), label);
			assertNotNull(definition.getToolTipText(), label);
			assertEquals(before, app.getXML(), label);
		}
	}

	@Test
	void readOnlyPropertiesRejectsEnterAndFocusLossWithoutAnyRedefine() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement spline = eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		JPanel panel = namePanel(app);
		update(panel, spline);
		JTextField definition = definitionField(panel);
		String xml = app.getXML();
		Object id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(spline);
		definition.setText("5"); // A synthetic event must not bypass the disabled editor.
		((ActionListener) panel).actionPerformed(new ActionEvent(definition, 0, "Enter"));
		((FocusListener) panel).focusLost(new FocusEvent(definition, FocusEvent.FOCUS_LOST));
		assertEquals(xml, app.getXML());
		assertSame(spline, lookup(app, "S"));
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(spline));
	}

	@Test
	void ordinaryAndSemanticScalarPointChildrenRetainTheirExistingHostEditFlags()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"k=1", "A=(1,2)", "B=(k,3)",
				"S=SplineV2({(-2,0),(0,0),(2,0)},3)", "M=Length(S)",
				"P=Point(S,\"spline-v2/main\",0.25)"}) {
			eval(app, command);
		}
		GeoPoint owned = new GeoCeDGPointInteraction(app).create(
				(GeoLocusV2) lookup(app, "S"), 1, 0.01, 0.1, result -> null);
		assertNotNull(owned);
		JPanel panel = namePanel(app);
		for (GeoElement geo : new GeoElement[] {lookup(app, "k"), lookup(app, "A"),
				lookup(app, "B"), lookup(app, "M"), lookup(app, "P"), owned}) {
			assertTrue(geo.isAlgebraViewEditable());
			assertFalse(GeoCeDGDefinitionInspector.isReadOnly(geo));
			String before = app.getXML();
			update(panel, geo);
			assertTrue(definitionField(panel).isEditable());
			assertEquals(before, app.getXML());
		}
	}

	@Test
	void numericPropertiesUsesExistingAtomicCompatibleRedefine() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoNumeric number = (GeoNumeric) eval(app, "k=1");
		Object id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(number);
		JPanel panel = namePanel(app);
		update(panel, number);
		JTextField definition = definitionField(panel);
		definition.setText("0.25");
		((ActionListener) panel).actionPerformed(new ActionEvent(definition, 0, "Enter"));
		assertEquals(0.25, ((GeoNumeric) lookup(app, "k")).getDouble());
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(lookup(app, "k")));
	}

	@Test
	void inspectorShowsTypedBranchComponentAndSemanticAddressInBothLanguages() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoLocusV2 spline = (GeoLocusV2) eval(app,
				"S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoPoint point = new GeoCeDGPointInteraction(app).create(spline, 1, 0,
				0.1, result -> null);
		assertNotNull(point);
		AlgoSemanticLocusPoint2D parent =
				(AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		LocusSemanticAddress2D address = parent.getCurrentSemanticAddress();
		assertNotNull(address);
		String branchKey = spline.getSemanticDefinition().getBranches().get(0)
				.getBranchKey();
		String componentKey = LocusComponentLineage2D.create(branchKey,
				spline.getSemanticDefinition().getBranches().get(0)
						.getValidDomainComponents().get(0));
		String xml = app.getXML();

		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			String language = locale.getLanguage();
			String locusDetails = GeoCeDGDefinitionInspector.semanticDetails(app, spline);
			assertTrue(locusDetails.contains(GeoCeDGProfile.getText(
					"Definition.SemanticStructure", language)));
			assertTrue(locusDetails.contains(GeoCeDGProfile.getText(
					"Definition.Branch", language) + ": " + branchKey));
			assertTrue(locusDetails.contains(GeoCeDGProfile.getText(
					"Definition.Component", language) + ": " + componentKey));

			String pointDetails = GeoCeDGDefinitionInspector.semanticDetails(app, point);
			assertTrue(pointDetails.contains(GeoCeDGProfile.getText(
					"Definition.SemanticAddress", language)));
			assertTrue(pointDetails.contains(GeoCeDGProfile.getText(
					"Definition.Branch", language) + ": " + address.getBranchKey()));
			assertTrue(pointDetails.contains(GeoCeDGProfile.getText(
					"Definition.Component", language) + ": "
					+ address.getComponentLineageKey()));
			assertTrue(pointDetails.contains(GeoCeDGProfile.getText(
					"Definition.Parameter", language) + ": "
					+ Double.toString(address.getCanonicalParameter())));
		}
		assertEquals(xml, app.getXML());
	}

	@Test
	void inspectorDistinguishesCurrentAddressFromRetainedDormantAddressAndRecovery() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint anchor = (GeoPoint) eval(app, "A=(-2,0)");
		GeoLocusV2 spline = (GeoLocusV2) eval(app,
				"S=SplineV2({A,(0,0),(2,0)},3)");
		GeoPoint point = new GeoCeDGPointInteraction(app).create(spline, 1, 0,
				0.1, result -> null);
		assertNotNull(point);
		AlgoSemanticLocusPoint2D parent =
				(AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		assertNotNull(parent.getCurrentSemanticAddress());
		assertTrue(GeoCeDGDefinitionInspector.semanticDetails(app, point).contains(
				GeoCeDGProfile.getText("Definition.AddressCurrent", "en")));

		anchor.setUndefined();
		anchor.updateRepaint();
		assertFalse(point.isDefined());
		assertNull(parent.getCurrentSemanticAddress());
		assertNotNull(parent.getSemanticAddress());
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			String language = locale.getLanguage();
			String details = GeoCeDGDefinitionInspector.semanticDetails(app, point);
			assertTrue(details.contains(GeoCeDGProfile.getText(
					"Definition.AddressStatus", language)));
			assertTrue(details.contains(GeoCeDGProfile.getText(
					"Definition.AddressRetainedDormant", language)));
			assertFalse(details.contains(GeoCeDGProfile.getText(
					"Definition.AddressCurrent", language)));
		}

		anchor.setCoords(-2, 0, 1);
		anchor.updateRepaint();
		assertTrue(point.isDefined());
		assertNotNull(parent.getCurrentSemanticAddress());
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			String language = locale.getLanguage();
			String details = GeoCeDGDefinitionInspector.semanticDetails(app, point);
			assertTrue(details.contains(GeoCeDGProfile.getText(
					"Definition.AddressCurrent", language)));
			assertFalse(details.contains(GeoCeDGProfile.getText(
					"Definition.AddressRetainedDormant", language)));
		}
	}

	@Test
	void readOnlyExplanationHasBothProductLanguagesAndClassicEditorIsUnchanged()
			throws Exception {
		AppGeoCeDG product = G9U1TestApp.create();
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			product.setLocale(locale);
			assertEquals(GeoCeDGProfile.getText("Definition.ReadOnly", locale.getLanguage()),
					GeoCeDGDefinitionInspector.readOnlyExplanation(product));
		}
		AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel(), true);
		G9U1TestApp.withoutWindowDispatcher(classic);
		GeoElement free = classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("A=(1,2)", false)[0].toGeoElement();
		JPanel panel = namePanel(classic);
		update(panel, free);
		assertTrue(definitionField(panel).isEditable());
	}

	@Test
	void ordinaryNoneditableImageKeepsHostPropertiesInsteadOfSemanticDefinition() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoImage image = new GeoImage(app.getKernel().getConstruction());
		assertFalse(image.isAlgebraViewEditable());
		assertFalse(GeoCeDGDefinitionInspector.isReadOnly(image));
		JPanel panel = namePanel(app);
		update(panel, image);
		assertFalse(SwingUtilities.isDescendingFrom(definitionField(panel), panel));
	}

	private static JPanel namePanel(AppD app) throws Exception {
		Class<?> type = Class.forName("org.geogebra.desktop.gui.dialog.NamePanelD");
		Constructor<?> constructor = type.getDeclaredConstructor(AppD.class, UpdateTabs.class);
		constructor.setAccessible(true);
		return (JPanel) constructor.newInstance(app, (UpdateTabs) geos -> { });
	}

	private static void update(JPanel panel, GeoElement geo) throws Exception {
		Method update = panel.getClass().getDeclaredMethod("updatePanel", Object[].class);
		update.setAccessible(true);
		assertSame(panel, update.invoke(panel, (Object) new Object[] {geo}));
	}

	private static JTextField definitionField(JPanel panel) throws Exception {
		Field field = panel.getClass().getDeclaredField("tfDefinition");
		field.setAccessible(true);
		return (JTextField) field.get(panel);
	}
}

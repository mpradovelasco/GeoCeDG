/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.document.controls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayoutException.Code;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class ControlLayoutXmlTest extends BaseUnitTest {
	@Test
	void completeSnapshotReclaimsFourControlsWithoutWritingTheirPresentationInFragments()
			throws Exception {
		GeoElement slider = control("s=Slider(0,10,1)", 70, 120);
		GeoElement button = control("b=Button()", 170, 150);
		GeoElement checkbox = control("q=Checkbox()", 270, 180);
		((GeoBoolean) checkbox).setCheckboxFixed(false);
		GeoElement field = control("i=InputBox(s)", 370, 210);
		GeoNumeric canvas = (GeoNumeric) add("m=Slider(0,s,1)");
		canvas.setAbsoluteScreenLocActive(false);
		canvas.setRealWorldLoc(3.25, -2);
		canvas.setSliderWidth(4, true);
		GeoNumeric angle = (GeoNumeric) control("t=Slider(0,6,0.1,1,100,true)", 40, 80);
		angle.removeViews3D();
		angle.setVisibleInViewForPlane(false);
		angle.setSliderFixed(false);
		var fixed = layout().addPanel(Anchor.FIXED, 5, 10, 300, 250, true);
		var world = layout().addPanel(Anchor.WORLD, 0.25, -1.5, 200, 160, false);
		var first = layout().addSlot(fixed.id(), slider, 2, 4);
		layout().addSlot(fixed.id(), button, 6, 8);
		layout().addSlot(world.id(), checkbox, 10, 12);
		var last = layout().addSlot(world.id(), field, -8, 26);
		var canvasSlot = layout().addSlot(world.id(), canvas, 20, 50, 125);
		var angleSlot = layout().addSlot(fixed.id(), angle, 12, 44);
		var fragment = new XMLStringBuilder();
		slider.getXML(false, fragment);
		assertFalse(fragment.toString().contains(ControlLayoutXml.SLOT_ATTRIBUTE));

		String saved = construction().getCurrentUndoXML(true).toString();
		assertTrue(saved.contains("geocedgControlLayout=\"2|"));
		assertRejected(saved.replace(",S,20.0,50.0,125.0", ",S,20.0,50.0"), Code.OWNER);
		assertRejected(saved.replace(",S,20.0,50.0,125.0", ",S,20.0,50.0,0.0"), Code.GRAMMAR);
		assertRejected(saved.replace(",A,12.0,44.0", ",S,12.0,44.0"), Code.OWNER);
		assertRejected(saved.replace("geocedgControlLayout=\"2|",
				"geocedgControlLayout=\"1|").replace(",S,20.0,50.0,125.0", ",S,20.0,50.0"),
				Code.OWNER);
		construction().processXML(saved, false, null);
		assertEquals(fixed, layout().panel(fixed.id()));
		assertEquals(world, layout().panel(world.id()));
		assertEquals(first.id(), layout().slot(lookup("s")).id());
		assertEquals(last.id(), layout().slot(lookup("i")).id());
		GeoNumeric restored = (GeoNumeric) lookup("m");
		assertEquals(canvasSlot.id(), layout().slot(restored).id());
		assertEquals(125, layout().slot(restored).sliderWidth());
		assertEquals(3.25, restored.getRealWorldLocX());
		assertEquals(4, restored.getSliderWidth());
		assertFalse(restored.isAbsoluteScreenLocActive());
		assertSame(lookup("s"), restored.getIntervalMaxObject());
		assertTrue(lookup("t").isGeoAngle());
		assertEquals(angleSlot.id(), layout().slot(lookup("t")).id());
		assertNotSame(slider, lookup("s"));
		assertNull(layout().slot(slider));
		assertSame(lookup("s"), ((GeoInputBox) lookup("i")).getLinkedGeo());
		assertEquals(70, ((AbsoluteScreenLocateable) lookup("s")).getAbsoluteScreenLocX());
		assertEquals(210, ((AbsoluteScreenLocateable) lookup("i")).getAbsoluteScreenLocY());
		assertEquals(3, layout().slots(fixed.id()).size());
		assertEquals(3, layout().slots(world.id()).size());
	}

	@Test
	void rejectingMalformedInputPreservesLayoutAndRejectsContextBeforeClearing() throws Exception {
		GeoElement slider = control("s=Slider(0,10,1)", 70, 120);
		var panel = layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true);
		var slot = layout().addSlot(panel.id(), slider, 4, 6);
		String saved = construction().getCurrentUndoXML(true).toString();
		assertEquals(Code.CONTEXT, assertThrows(ControlLayoutException.class,
				() -> getApp().getXMLio().processXMLString(saved, true, false)).code());
		assertSame(slider, lookup("s"));
		assertEquals(Code.CONTEXT, assertThrows(ControlLayoutException.class,
				() -> getApp().getXMLio().processXMLString(saved, false, false)).code());
		assertSame(slider, lookup("s"));

		assertRejected(saved.replace("geocedgControlLayout=\"1|",
				"geocedgControlLayout=\"3|"), Code.VERSION);
		assertRejected(saved.replace(",S,4.0,6.0", ",S,NaN,6.0"), Code.GRAMMAR);
		assertRejected(saved.replace(",S,4.0,6.0", ",S,1e999,6.0"), Code.GRAMMAR);
		assertRejected(saved.replace(",S,4.0,6.0", ",B,4.0,6.0"), Code.OWNER);
		assertRejected(saved.replaceAll(" geocedgControlSlot=\"[^\"]*\"", ""), Code.ORPHAN);
		assertRejected(saved.replace("label=\"s\" geocedgControlSlot",
				"label=\"s\" label=\"s\" geocedgControlSlot"), Code.GRAMMAR);
		assertRejected(saved.replace("geocedgControlLayout=\"1|",
				"geocedgControlLayout=\"" + "a".repeat(ControlLayoutXml.MAX_BYTES) + "|"), Code.LIMIT);
		assertEquals(panel, layout().panel(panel.id()));
		assertEquals(slot.id(), layout().slot(lookup("s")).id());
	}

	@Test
	void renameKeepsOwnerAndDeleteRetiresSlotEvenWhenLabelIsReused() {
		GeoElement button = control("b=Button()", 70, 120);
		var panel = layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true);
		var first = layout().addSlot(panel.id(), button, 0, 0);
		button.rename("renamed");
		assertSame(button, lookup("renamed"));
		assertEquals(first, layout().slot(button));
		button.remove();
		GeoElement replacement = control("renamed=Button()", 70, 120);
		assertNull(layout().slot(replacement));
		assertTrue(layout().slots(panel.id()).isEmpty());
		var second = layout().addSlot(panel.id(), replacement, 0, 0);
		assertNotEquals(first.id(), second.id());
	}

	@Test
	void capacityAndPositionEligibilityAreEnforcedWithoutConvertingControls() {
		GeoElement slider = control("s=Slider(0,10,1)", 70, 120);
		var panel = layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true);
		for (int i = 1; i < ControlLayout.MAX_PANELS; i++) {
			layout().addPanel(Anchor.WORLD, i, i, 300, 250, false);
		}
		assertEquals(Code.LIMIT, assertThrows(ControlLayoutException.class,
				() -> layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true)).code());
		assertEquals(Code.GRAMMAR, assertThrows(ControlLayoutException.class,
				() -> layout().addSlot(panel.id(), slider, Double.NaN, 0)).code());
		var angle = (GeoNumeric) control("t=Slider(0,6,0.1,1,100,true)", 50, 120);
		angle.removeViews3D();
		angle.setSliderFixed(true);
		assertFalse(layout().isEligible(angle));
		angle.setSliderFixed(false);
		assertTrue(layout().isEligible(angle));
		slider.addView(org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		assertFalse(layout().isEligible(slider));
		assertEquals(Code.OWNER, assertThrows(ControlLayoutException.class,
				() -> layout().addSlot(panel.id(), slider, 0, 0)).code());
	}

	@Test
	void appearanceRoundTripPreservesCompositionOrderAndReadsLegacyDefaults() throws Exception {
		GeoElement slider = control("s=Slider(0,10,1)", 70, 120);
		var fixed = layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true);
		layout().addPanel(Anchor.WORLD, 2, 3, 300, 250, true);
		var slot = layout().addSlot(fixed.id(), slider, 4, 6);
		String legacy = snapshot().replaceAll(" geocedgControlAppearance=\"[^\"]*\"", "");
		construction().processXML(legacy, false, null);
		var canonical = layout().panels();
		assertTrue(canonical.get(0).id().compareTo(canonical.get(1).id()) < 0);
		assertEquals("", canonical.get(0).name());
		assertEquals(ControlLayout.DEFAULT_RGB, canonical.get(0).rgb());
		assertEquals(ControlLayout.DEFAULT_OPACITY, canonical.get(0).opacity());
		assertFalse(snapshot().contains(ControlLayoutXml.APPEARANCE_ATTRIBUTE));

		var back = canonical.get(1);
		var front = canonical.get(0);
		String reverse = "1|" + back.id() + ",e1edf7,65,;" + front.id() + ",e1edf7,65,";
		construction().processXML(withAppearance(legacy, reverse), false, null);
		assertEquals(List.of(back, front), layout().panels());
		assertTrue(snapshot().contains("geocedgControlAppearance=\"" + reverse + "\""));
		assertEquals(slot.id(), layout().slot(lookup("s")).id());

		String title = "Árbol <céDG>,; 🎨";
		var styled = back.withStyle(title, 0x041215, ControlLayout.MIN_OPACITY);
		layout().updatePanel(styled);
		var newest = layout().addPanel(Anchor.FIXED, 2, 3, 300, 250, true,
				title, 0xffffff, ControlLayout.MAX_OPACITY);
		String saved = snapshot();
		assertTrue(saved.contains(styled.id() + ",041215,25,"
				+ HexFormat.of().formatHex(title.getBytes(StandardCharsets.UTF_8))));
		construction().processXML(saved, false, null);
		assertEquals(List.of(styled, front, newest), layout().panels());
		assertEquals(slot.id(), layout().slot(lookup("s")).id());
		assertSame(lookup("s"), layout().slot(lookup("s")).control());
	}

	@Test
	void appearanceRejectsInvalidNamesGrammarAndCoverageWithoutPublishing() throws Exception {
		GeoElement button = control("b=Button()", 70, 120);
		String longest = "🎨".repeat(ControlLayout.MAX_NAME_CODE_POINTS);
		var panel = layout().addPanel(Anchor.FIXED, 0, 0, 300, 250, true,
				longest, 0x041215, ControlLayout.MIN_OPACITY);
		var slot = layout().addSlot(panel.id(), button, 4, 6);
		String saved = snapshot();
		String wire = "1|" + panel.id() + ",041215,25,"
				+ HexFormat.of().formatHex(longest.getBytes(StandardCharsets.UTF_8));
		assertTrue(saved.contains(wire));
		assertEquals(Code.LIMIT, assertThrows(ControlLayoutException.class,
				() -> panel.withStyle(longest + "a", 0x041215, 25)).code());
		assertEquals(Code.GRAMMAR, assertThrows(ControlLayoutException.class,
				() -> panel.withStyle("bad" + (char) 0xd800, 0x041215, 25)).code());
		assertEquals(Code.GRAMMAR, assertThrows(ControlLayoutException.class,
				() -> panel.withStyle("bad" + (char) 0xfffe, 0x041215, 25)).code());

		assertRejected(saved.replaceAll(" geocedgControlLayout=\"[^\"]*\"", ""), Code.ORPHAN);
		assertSame(button, lookup("b"));
		assertRejected(saved.replace(wire, "2" + wire.substring(1)), Code.VERSION);
		assertRejected(saved.replace(wire, wire + ";" + wire.substring(2)), Code.DUPLICATE);
		assertRejected(saved.replace(wire, "1|00000000-0000-0000-0000-000000000000,041215,25,"),
				Code.ORPHAN);
		assertRejected(saved.replace(wire, "1|" + panel.id() + ",x41215,25,"), Code.GRAMMAR);
		assertRejected(saved.replace(wire, "1|" + panel.id() + ",041215,24,"), Code.GRAMMAR);
		assertRejected(saved.replace(wire, "1|" + panel.id() + ",041215,25,ff"), Code.GRAMMAR);
		assertRejected(saved.replace(wire, "1|" + panel.id() + ",041215,25,0a"), Code.GRAMMAR);
		assertRejected(saved.replace(wire, "1|" + panel.id() + ",041215,25," + "ff".repeat(257)),
				Code.LIMIT);
		assertEquals(List.of(panel), layout().panels());
		assertEquals(slot.id(), layout().slot(lookup("b")).id());
	}

	private static String withAppearance(String xml, String appearance) {
		return xml.replace("<construction ", "<construction geocedgControlAppearance=\""
				+ appearance + "\" ");
	}

	private String snapshot() {
		return construction().getCurrentUndoXML(true).toString();
	}

	private void assertRejected(String xml, Code code) {
		assertEquals(code, assertThrows(ControlLayoutException.class,
				() -> construction().processXML(xml, false, null)).code());
	}

	private GeoElement control(String expression, int x, int y) {
		GeoElement control = add(expression);
		AbsoluteScreenLocateable location = (AbsoluteScreenLocateable) control;
		location.setAbsoluteScreenLocActive(true);
		location.setAbsoluteScreenLoc(x, y);
		control.setEuclidianVisible(true);
		control.update();
		return control;
	}

	private Construction construction() {
		return getApp().getKernel().getConstruction();
	}

	private ControlLayout layout() {
		return construction().getControlLayout();
	}
}

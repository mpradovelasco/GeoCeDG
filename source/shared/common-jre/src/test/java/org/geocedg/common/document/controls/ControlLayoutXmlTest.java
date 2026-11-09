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

import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayoutException.Code;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
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
		var fixed = layout().addPanel(Anchor.FIXED, 5, 10, 300, 250, true);
		var world = layout().addPanel(Anchor.WORLD, 0.25, -1.5, 200, 160, false);
		var first = layout().addSlot(fixed.id(), slider, 2, 4);
		layout().addSlot(fixed.id(), button, 6, 8);
		layout().addSlot(world.id(), checkbox, 10, 12);
		var last = layout().addSlot(world.id(), field, -8, 26);
		var fragment = new XMLStringBuilder();
		slider.getXML(false, fragment);
		assertFalse(fragment.toString().contains(ControlLayoutXml.SLOT_ATTRIBUTE));

		String saved = construction().getCurrentUndoXML(true).toString();
		assertTrue(saved.contains("geocedgControlLayout=\"1|"));
		construction().processXML(saved, false, null);
		assertEquals(fixed, layout().panel(fixed.id()));
		assertEquals(world, layout().panel(world.id()));
		assertEquals(first.id(), layout().slot(lookup("s")).id());
		assertEquals(last.id(), layout().slot(lookup("i")).id());
		assertNotSame(slider, lookup("s"));
		assertNull(layout().slot(slider));
		assertSame(lookup("s"), ((GeoInputBox) lookup("i")).getLinkedGeo());
		assertEquals(70, ((AbsoluteScreenLocateable) lookup("s")).getAbsoluteScreenLocX());
		assertEquals(210, ((AbsoluteScreenLocateable) lookup("i")).getAbsoluteScreenLocY());
		assertEquals(layout().slots(fixed.id()).size(), 2);
		assertEquals(layout().slots(world.id()).size(), 2);
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
				"geocedgControlLayout=\"2|"), Code.VERSION);
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
		slider.addView(org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		assertFalse(layout().isEligible(slider));
		assertEquals(Code.OWNER, assertThrows(ControlLayoutException.class,
				() -> layout().addSlot(panel.id(), slider, 0, 0)).code());
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

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.desktop.euclidian.EuclidianViewJPanelD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** M07 lexical insertion and instance-currentness guards, through Swing Documents. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu6ArgumentDialogTest {

	@Test
	void selectedNumberReplacesOnlyTheSelectedTextAndKeepsItsSubscript() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "a_1=2");
			var point = eval(app, "A=(1,2)");
			var references = references(app);
			assertTrue(references.canPick(number));
			assertFalse(references.canPick(point));

			JTextField field = field(references, "1 + old * 3");
			field.select(4, 7);
			String xml = app.getXML();
			assertTrue(references.insert(field, references.offer(number)));
			assertEquals("1 +  a_1  * 3", field.getText());
			assertNull(references.firstStale(field.getText()));
			assertEquals(xml, app.getXML());
		});
	}

	@Test
	void staleSameLabelStaysBlockedUntilAnExplicitNewPick() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var old = eval(app, "k=2");
			var references = references(app);
			JTextField field = field(references, "1+");
			field.setCaretPosition(field.getText().length());
			assertTrue(references.insert(field, references.offer(old)));
			old.remove();
			var replacement = eval(app, "k=5");

			var stale = references.firstStale(field.getText());
			assertNotNull(stale);
			field.select(stale.start, stale.end);
			field.replaceSelection("k");
			assertNotNull(references.firstStale(field.getText()),
					"Replacing text with the same spelling is not a new graphical selection");
			field.select(stale.start, stale.end);
			assertTrue(references.insert(field, references.offer(replacement)));
			assertNull(references.firstStale(field.getText()));
		});
	}

	@Test
	void unrelatedEditingKeepsWitnessesAndInvalidationSurvivesLabelRestoration() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "k=2");
			var references = references(app);
			JTextField field = field(references, "");
			assertTrue(references.insert(field, references.offer(number)));
			field.setCaretPosition(0);
			field.replaceSelection("2+");
			assertNull(references.firstStale(field.getText()));

			references.invalidate(number);
			var stale = references.firstStale(field.getText());
			assertNotNull(stale);
			assertEquals("k", field.getText().substring(stale.start, stale.end));
			field.select(stale.start, stale.end);
			field.replaceSelection("7");
			assertNull(references.firstStale(field.getText()));
		});
	}

	@Test
	void subsequentConstructionStepsCannotSupplyAnArgument() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "k=2");
			var construction = app.getKernel().getConstruction();
			var references = references(app);
			JTextField field = field(references, "");
			assertTrue(references.insert(field, references.offer(number)));

			construction.setStep(-1);
			assertFalse(references.canPick(number));
			assertNotNull(references.firstStale(field.getText()));
			construction.setStep(number.getConstructionIndex());
			assertTrue(references.canPick(number));
		});
	}

	@Test
	void invalidatedChooserOfferCannotBeInsertedEvenIfTheObjectStillExists() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "k=2");
			var references = references(app);
			JTextField field = field(references, "4");
			var pending = references.offer(number);
			references.invalidateAll();
			assertFalse(references.insert(field, pending));
			assertEquals("4", field.getText());
		});
	}

	@Test
	void redefineAcceptsOtherObjectTypesAndRejectsItsOwnerAndDescendants() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var owner = eval(app, "A=(1,2)");
			var descendant = eval(app, "B=A+(1,1)");
			var other = eval(app, "C=(4,5)");
			var references = new GeoCeDGArgumentDialog.References(owner.getConstruction(),
					candidate -> GeoCeDGArgumentDialog.redefineReference(owner, candidate));
			assertFalse(references.canPick(owner));
			assertFalse(references.canPick(descendant));
			assertTrue(references.canPick(other));

			JTextField field = field(references, "(2,3)");
			field.selectAll();
			assertTrue(references.insert(field, references.offer(other)));
			assertEquals(" C ", field.getText());
			assertNull(references.firstStale(field.getText()));
		});
	}

	@Test
	void redefineDoesNotOfferAnObjectShadowedByTheFunctionsLocalVariable() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "k=3");
			var owner = eval(app, "f(k)=k^2");
			assertFalse(GeoCeDGArgumentDialog.redefineReference(owner, number));
		});
	}

	@Test
	void inputBoxComboStillAcceptsAnUndefinedEditableObject() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			var number = eval(app, "k=3");
			number.setUndefined();
			var references = new GeoCeDGArgumentDialog.References(number.getConstruction(),
					GeoInputBox::isGeoLinkable);
			assertTrue(references.canChoose(number));
			assertFalse(references.canPick(number));
		});
	}

	@Test
	void canvasCaptureUsesViewportRatherThanBoundingBoxOfDrawing() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			eval(app, "A=(2,1)");
			JPanel frame = new JPanel(null);
			frame.setSize(800, 600);
			JPanel shield = new JPanel();
			frame.add(shield);
			shield.setBounds(0, 0, 800, 600);
			JPanel canvas = new EuclidianViewJPanelD(app.getEuclidianView1(), false) {
				@Override
				public boolean isShowing() {
					return true;
				}
			};
			frame.add(canvas);
			canvas.setBounds(180, 120, 600, 450);
			Point local = new Point(310, 260);
			assertFalse(canvas.getVisibleRect().contains(local));
			MouseEvent click = new MouseEvent(shield, MouseEvent.MOUSE_RELEASED, 0, 0,
					490, 380, 1, false, MouseEvent.BUTTON1);
			assertEquals(local, GeoCeDGArgumentDialog.localPoint(click, canvas));
		});
	}

	private static GeoCeDGArgumentDialog.References references(AppGeoCeDG app) {
		return new GeoCeDGArgumentDialog.References(app.getKernel().getConstruction());
	}

	private static JTextField field(GeoCeDGArgumentDialog.References references, String initial) {
		JTextField field = new JTextField(initial);
		((AbstractDocument) field.getDocument()).setDocumentFilter(references);
		return field;
	}
}

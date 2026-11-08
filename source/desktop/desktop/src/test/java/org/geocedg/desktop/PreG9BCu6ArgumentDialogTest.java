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

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

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

	private static GeoCeDGArgumentDialog.References references(AppGeoCeDG app) {
		return new GeoCeDGArgumentDialog.References(app.getKernel().getConstruction());
	}

	private static JTextField field(GeoCeDGArgumentDialog.References references, String initial) {
		JTextField field = new JTextField(initial);
		((AbstractDocument) field.getDocument()).setDocumentFilter(references);
		return field;
	}
}

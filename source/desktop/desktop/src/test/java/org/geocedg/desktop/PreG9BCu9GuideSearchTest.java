/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.junit.jupiter.api.Test;

class PreG9BCu9GuideSearchTest {

	private static final String SPANISH = """
			<!-- geocedg-guide-section: guide -->
			# Guía

			<!-- geocedg-guide-section: internal-marker -->
			## 1. Medición

			Texto auxiliar.

			### 1.1 Recorridos

			Usa `LocusLength(C, P, Q)` para el cálculo de la extensio\u0301n.

			| Función | Formato |
			| --- | --- |
			| Exportación | DXF |
			""";

	private static final String ENGLISH = """
			<!-- geocedg-guide-section: guide -->
			# Guide

			<!-- geocedg-guide-section: internal-marker -->
			## 1. Measurement

			Supporting text.

			### 1.1 Traversal

			Use `LocusLength(C, P, Q)` to measure the extension.
			""";

	@Test
	void searchFindsDisplayedHeadingsProseCodeAndTableCells() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			GeoCeDGGuideSearchIndex index = fixture.load(SPANISH, "es");
			assertMatch(fixture, index, " MEDICION ", "internal-marker", "Medición");
			assertMatch(fixture, index, "EXTENSIÓN", "internal-marker/1.1", "extensio\u0301n");
			assertMatch(fixture, index, "locuslength(c, p, q)", "internal-marker/1.1",
					"LocusLength(C, P, Q)");
			assertMatch(fixture, index, "exportacion", "internal-marker/1.1", "Exportación");
			assertTrue(index.find("internal-marker").matches().isEmpty());
			assertTrue(index.find("`LocusLength").matches().isEmpty());
			assertTrue(index.find("Medición Texto auxiliar Recorridos").matches().isEmpty(),
					"a search does not cross section boundaries");
			return null;
		});
	}

	@Test
	void searchesBothPackagedEditions() throws Exception {
		String spanish = GeoCeDGActionRegistry.readUserGuide("es");
		String english = GeoCeDGActionRegistry.readUserGuide("en");
		onEdt(() -> {
			Fixture fixture = new Fixture();
			GeoCeDGGuideSearchIndex index = fixture.load(spanish, "es");
			assertFalse(index.find("EXPORTACION").matches().isEmpty());
			assertFalse(index.find("LocusLength").matches().isEmpty());
			index = fixture.load(english, "en");
			assertFalse(index.find("DXF export").matches().isEmpty());
			assertFalse(index.find("LocusLength").matches().isEmpty());
			return null;
		});
	}

	@Test
	void commonQueriesHaveABoundedResultListAndReportTruncation() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			String prefix = "<!-- geocedg-guide-section: guide -->\n# Guide\n\n";
			GeoCeDGGuideSearchIndex index = fixture.load(prefix + "DXF ".repeat(200), "en");
			assertEquals(GeoCeDGGuideSearchIndex.RESULT_LIMIT, index.find("DXF").matches().size());
			assertFalse(index.find("DXF").truncated());
			index = fixture.load(prefix + "DXF ".repeat(201), "en");
			assertEquals(GeoCeDGGuideSearchIndex.RESULT_LIMIT, index.find("DXF").matches().size());
			assertTrue(index.find("DXF").truncated());
			assertTrue(index.find(" \t ").matches().isEmpty());
			return null;
		});
	}

	@Test
	void keyboardNavigationHighlightsTheHitSelectsItsSectionAndClears() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			String longEdition = SPANISH.replace("Texto auxiliar.", "Texto auxiliar.\n\n".repeat(60));
			fixture.load(longEdition, "es");
			String text = fixture.view.getDocument().getText(0, fixture.view.getDocument().getLength());
			JTextField query = fixture.query();
			JList<?> results = fixture.results();
			query.setText("locuslength");
			activate(fixture.root, JComponent.WHEN_IN_FOCUSED_WINDOW, KeyEvent.VK_F,
					InputEvent.CTRL_DOWN_MASK);
			assertEquals("locuslength", query.getSelectedText());
			activate(query, JComponent.WHEN_FOCUSED, KeyEvent.VK_DOWN, 0);
			assertEquals(0, results.getSelectedIndex());
			activate(results, JComponent.WHEN_FOCUSED, KeyEvent.VK_ENTER, 0);
			assertEquals("internal-marker/1.1", fixture.navigator.getSelectedAnchor());
			assertTrue(fixture.scroll.getViewport().getViewPosition().y > 0);
			assertEquals(1, fixture.view.getHighlighter().getHighlights().length);
			assertTrue(GeoCeDGGuideSearch.dispatchKey(escape(results, KeyEvent.KEY_PRESSED)));
			assertEquals("locuslength", query.getText());
			assertTrue(GeoCeDGGuideSearch.dispatchKey(escape(results, KeyEvent.KEY_RELEASED)));
			assertEquals("", query.getText());
			assertEquals(0, results.getModel().getSize());
			assertEquals(0, fixture.view.getHighlighter().getHighlights().length);
			assertFalse(GeoCeDGGuideSearch.dispatchKey(escape(query, KeyEvent.KEY_PRESSED)),
					"an empty guide search leaves normal dialog shortcuts in place");
			assertEquals(text, fixture.view.getDocument().getText(0, text.length()));
			assertFalse(fixture.view.isEditable());
			return null;
		});
	}

	@Test
	void aWideResultHasRoomForItsTextAndScrollbar() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load(SPANISH, "es");
			fixture.query().setText("LocusLength");
			JComponent panel = fixture.search.getPanel();
			panel.setSize(300, panel.getPreferredSize().height);
			panel.doLayout();
			JScrollPane resultsScroll = find(panel, JScrollPane.class);
			resultsScroll.doLayout();
			assertTrue(resultsScroll.getHorizontalScrollBar().isVisible());
			assertTrue(resultsScroll.getViewport().getExtentSize().height
					>= fixture.results().getCellBounds(0, 0).height);
			return null;
		});
	}

	@Test
	void editionReloadClearsSearchAndUsesTheNewLanguageAndText() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load(SPANISH, "es");
			fixture.query().setText("medicion");
			assertEquals(1, fixture.results().getModel().getSize());
			assertEquals("Resultados de búsqueda", fixture.results().getAccessibleContext()
					.getAccessibleName());
			fixture.load(ENGLISH, "en");
			assertEquals("", fixture.query().getText());
			assertEquals(0, fixture.results().getModel().getSize());
			assertEquals("Search results", fixture.results().getAccessibleContext().getAccessibleName());
			fixture.query().setText("medicion");
			assertEquals(0, fixture.results().getModel().getSize());
			assertTrue(containsLabel(fixture.search.getPanel(), "No matches"));
			fixture.query().setText("measurement");
			assertEquals(1, fixture.results().getModel().getSize());
			assertTrue(containsLabel(fixture.search.getPanel(), "1 match"));
			return null;
		});
	}

	private static void assertMatch(Fixture fixture, GeoCeDGGuideSearchIndex index,
			String query, String anchor, String original) throws BadLocationException {
		GeoCeDGGuideSearchIndex.Results results = index.find(query);
		assertEquals(1, results.matches().size(), query);
		GeoCeDGGuideSearchIndex.Match match = results.matches().get(0);
		assertEquals(anchor, match.anchor(), query);
		assertEquals(original, fixture.view.getDocument().getText(match.start(),
				match.end() - match.start()), query);
		assertTrue(match.context().contains(original), match.context());
	}

	private static void activate(JComponent component, int condition, int key, int modifiers) {
		Object action = component.getInputMap(condition).get(KeyStroke.getKeyStroke(key, modifiers));
		assertNotNull(action);
		component.getActionMap().get(action).actionPerformed(
				new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
	}

	private static KeyEvent escape(Component source, int eventType) {
		return new KeyEvent(source, eventType, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
	}

	private static boolean containsLabel(Container container, String text) {
		for (Component child : container.getComponents()) {
			if (child instanceof JLabel label && label.isVisible() && text.equals(label.getText())) {
				return true;
			}
			if (child instanceof Container nested && containsLabel(nested, text)) {
				return true;
			}
		}
		return false;
	}

	private static <T extends Component> T find(Container container, Class<T> type) {
		for (Component child : container.getComponents()) {
			if (type.isInstance(child)) {
				return type.cast(child);
			}
			if (child instanceof Container nested) {
				T match = find(nested, type);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}

	private static void onEdt(Callable<Void> action) throws Exception {
		FutureTask<Void> task = new FutureTask<>(action);
		SwingUtilities.invokeAndWait(task);
		task.get();
	}

	private static final class Fixture {
		private final JEditorPane view = GeoCeDGGuideWindow.createView(null);
		private final JScrollPane scroll = new JScrollPane(view);
		private final GeoCeDGGuideNavigator navigator = new GeoCeDGGuideNavigator(view, scroll);
		private final GeoCeDGGuideSearch search = new GeoCeDGGuideSearch(view, navigator);
		private final JRootPane root = new JRootPane();

		Fixture() {
			root.setContentPane(search.getPanel());
			search.installShortcuts(root);
		}

		GeoCeDGGuideSearchIndex load(String markdown, String language) {
			GeoCeDGGuideRenderer.Rendering rendering = GeoCeDGGuideRenderer.render(markdown);
			view.setText(rendering.html());
			scroll.setSize(640, 420);
			for (int pass = 0; pass < 2; pass++) {
				scroll.doLayout();
				int width = scroll.getViewport().getExtentSize().width;
				view.setSize(width, Math.max(1, view.getHeight()));
				view.setSize(width, view.getPreferredSize().height);
				scroll.getViewport().doLayout();
			}
			navigator.load(rendering.outline());
			String title = GeoCeDGGuideRenderer.title(markdown);
			search.load(rendering.outline(), title, language);
			return new GeoCeDGGuideSearchIndex(view.getDocument(), rendering.outline(), navigator, title);
		}

		JTextField query() {
			return find(search.getPanel(), JTextField.class);
		}

		JList<?> results() {
			return find(search.getPanel(), JList.class);
		}
	}
}

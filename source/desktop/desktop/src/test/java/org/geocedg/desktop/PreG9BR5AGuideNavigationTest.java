/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R5-A focal contracts for the guide navigation tree and the
 * document-to-tree synchronization.
 *
 * <p>The navigator is exercised on the real rendered editions inside a laid-out
 * scroll pane, which needs no display. Selecting an entry positions its named
 * anchor at the top of the viewport; scrolling selects the last entry whose anchor
 * is at or above the reference line; a tree-driven navigation never reselects
 * through its own scroll event. Everything runs on the event dispatch thread.
 */
class PreG9BR5AGuideNavigationTest {

	private static final int WIDTH = 640;
	private static final int HEIGHT = 420;

	/** A laid-out document, scroll pane and navigator for one edition. */
	private static final class Fixture {
		final JEditorPane view = GeoCeDGGuideWindow.createView(
				new Font("SansSerif", Font.PLAIN, 12));
		final JScrollPane scroll = new JScrollPane(view);
		final GeoCeDGGuideNavigator navigator = new GeoCeDGGuideNavigator(view, scroll);
		final int height;
		List<GeoCeDGGuideOutline.Entry> outline;

		Fixture() {
			this(HEIGHT);
		}

		Fixture(int height) {
			this.height = height;
		}

		void load(String language) throws Exception {
			GeoCeDGGuideRenderer.Rendering rendering = GeoCeDGGuideRenderer.render(
					GeoCeDGActionRegistry.readUserGuide(language));
			outline = rendering.outline();
			view.setText(rendering.html());
			layOut();
			navigator.load(outline);
		}

		void layOut() {
			// Without a window the scroll pane is laid out explicitly; the second
			// pass re-wraps the text once the vertical scroll bar is present.
			scroll.setSize(WIDTH, height);
			for (int pass = 0; pass < 3; pass++) {
				scroll.doLayout();
				int width = scroll.getViewport().getExtentSize().width;
				view.setSize(width, Math.max(1, view.getHeight()));
				view.setSize(width, view.getPreferredSize().height);
				scroll.getViewport().doLayout();
			}
		}

		JViewport viewport() {
			return scroll.getViewport();
		}

		int viewY() {
			return viewport().getViewPosition().y;
		}

		int bottom() {
			return Math.max(0, view.getHeight() - viewport().getExtentSize().height);
		}

		double anchorY(String anchor) throws Exception {
			return view.modelToView2D(navigator.anchorOffset(anchor)).getY();
		}

		void scrollTo(int y) {
			viewport().setViewPosition(new Point(0, y));
		}

		void select(String anchor) {
			navigator.getTree().setSelectionPath(path(navigator.getTree(), anchor));
		}

		String selected() {
			return navigator.getSelectedAnchor();
		}

		String textAt(String anchor, int length) throws Exception {
			return view.getDocument().getText(navigator.anchorOffset(anchor), length);
		}

		/** The rule, evaluated independently of the navigator. */
		String expectedForViewport() throws Exception {
			double reference = viewY() + GeoCeDGGuideNavigator.REFERENCE_OFFSET;
			String current = outline.get(0).anchor();
			for (GeoCeDGGuideOutline.Entry entry : outline) {
				if (anchorY(entry.anchor()) <= reference) {
					current = entry.anchor();
				}
			}
			return current;
		}
	}

	private interface EdtAction {
		void run() throws Exception;
	}

	// --------------------------------------------------------------- the tree

	@Test
	void theTreeIsPopulatedSolelyFromTheDerivedOutline() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			JTree tree = fixture.navigator.getTree();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
			assertFalse(tree.isRootVisible(), "the Swing root carries no identity");
			assertEquals(1, root.getChildCount(), "the guide title is the only top entry");
			TreeNode title = root.getChildAt(0);
			assertEquals(16, title.getChildCount());
			// Pre-order traversal of the tree is exactly the outline in document order.
			List<String> anchors = new ArrayList<>();
			List<String> labels = new ArrayList<>();
			Enumeration<TreeNode> nodes = root.preorderEnumeration();
			nodes.nextElement();
			while (nodes.hasMoreElements()) {
				GeoCeDGGuideNavigator.OutlineNode node =
						(GeoCeDGGuideNavigator.OutlineNode) nodes.nextElement();
				anchors.add(node.getEntry().anchor());
				labels.add(node.toString());
			}
			assertEquals(fixture.outline.stream().map(GeoCeDGGuideOutline.Entry::anchor)
					.toList(), anchors);
			assertEquals(fixture.outline.stream().map(GeoCeDGGuideOutline.Entry::label)
					.toList(), labels);
			assertEquals("GeoCeDG user guide", labels.get(0));
			// Every entry is visible: the tree opens fully expanded.
			assertEquals(fixture.outline.size(), tree.getRowCount());
			// A newly shown guide starts at its top with the title selected.
			assertEquals("about-this-guide", fixture.selected());
		});
	}

	@Test
	void bothEditionsGiveTheSameTreeStructureWithLocalizedLabels() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			List<String> englishAnchors = treeAnchors(fixture.navigator.getTree());
			List<String> englishLabels = treeLabels(fixture.navigator.getTree());
			fixture.select("spline-v2/7.2");
			// Changing the language reloads the same navigator with the other edition.
			fixture.load("es");
			assertEquals(englishAnchors, treeAnchors(fixture.navigator.getTree()));
			List<String> spanishLabels = treeLabels(fixture.navigator.getTree());
			assertNotEquals(englishLabels, spanishLabels);
			assertEquals("Guía de usuario de GeoCeDG", spanishLabels.get(0));
			assertTrue(spanishLabels.contains("7.2 Crear una Spline V2"));
			assertFalse(spanishLabels.contains("7.2 Creating a Spline V2"));
			assertEquals("about-this-guide", fixture.selected(),
					"a reloaded edition starts at its top");
		});
	}

	// ------------------------------------------------------ tree -> document

	@Test
	void selectingAnEntryScrollsItsHeadingToTheTopOfTheViewport() throws Exception {
		for (String language : new String[] {"en", "es"}) {
			onEdt(() -> {
				Fixture fixture = new Fixture();
				fixture.load(language);
				for (String anchor : new String[] {"what-is-geocedg", "spline-v2",
						"spline-v2/7.2", "dxf-export/11.4", "known-limitations",
						"lengths-and-measurements/9.3", "about-this-guide"}) {
					fixture.select(anchor);
					int expected = (int) Math.max(0, Math.min(fixture.bottom(),
							Math.round(fixture.anchorY(anchor))));
					assertEquals(expected, fixture.viewY(), language + " " + anchor);
					assertEquals(anchor, fixture.selected(), language + " " + anchor);
					// The anchor sits on the heading text of exactly that entry.
					String label = entryLabel(fixture, anchor);
					assertEquals(label, fixture.textAt(anchor, label.length()), anchor);
				}
			});
		}
	}

	@Test
	void treeDrivenNavigationDoesNotOscillate() throws Exception {
		onEdt(() -> {
			// A tall viewport guarantees that the last entry cannot reach the top.
			Fixture fixture = new Fixture(1800);
			fixture.load("en");
			AtomicInteger selections = new AtomicInteger();
			fixture.navigator.getTree().addTreeSelectionListener(event ->
					selections.incrementAndGet());
			String last = "command-and-workflow-reference/16.4";
			assertTrue(fixture.anchorY(last) > fixture.bottom()
					+ GeoCeDGGuideNavigator.REFERENCE_OFFSET);
			fixture.select(last);
			// The viewport clamps to the end, where the rule alone names an
			// earlier entry; the chosen entry must still stay selected.
			assertEquals(fixture.bottom(), fixture.viewY());
			assertNotEquals(last, fixture.expectedForViewport());
			assertEquals(last, fixture.selected());
			assertEquals(1, selections.get(), "the navigation reselected");
			// Further change events at the same position keep the chosen entry.
			for (ChangeListener listener : fixture.viewport().getChangeListeners()) {
				listener.stateChanged(new ChangeEvent(fixture.viewport()));
			}
			fixture.scrollTo(fixture.bottom());
			assertEquals(last, fixture.selected());
			assertEquals(1, selections.get());
			// Once the user scrolls, the rule applies again.
			fixture.scrollTo(fixture.bottom() - 1);
			assertEquals(fixture.expectedForViewport(), fixture.selected());
		});
	}

	@Test
	void repeatedProgrammaticNavigationIsDeterministic() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			assertTrue(fixture.navigator.navigateTo("transformations/10.3"));
			int first = fixture.viewY();
			assertTrue(fixture.navigator.navigateTo("documents"));
			int other = fixture.viewY();
			assertTrue(fixture.navigator.navigateTo("transformations/10.3"));
			assertEquals(first, fixture.viewY());
			assertTrue(fixture.navigator.navigateTo("transformations/10.3"));
			assertEquals(first, fixture.viewY());
			assertEquals("transformations/10.3", fixture.selected());
			assertTrue(other < first);
			assertFalse(fixture.navigator.navigateTo("no-such-section"));
			assertFalse(fixture.navigator.navigateTo(null));
			assertEquals(first, fixture.viewY(), "an unknown anchor must not scroll");
		});
	}

	@Test
	void clickingTheSelectedEntryReturnsToItsHeading() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			JTree tree = fixture.navigator.getTree();
			tree.setSize(new Dimension(240, tree.getPreferredSize().height));
			String anchor = "basic-geometry/5.3";
			fixture.select(anchor);
			int heading = fixture.viewY();
			// Reading on inside the same section keeps the same selection.
			fixture.scrollTo(heading + 3);
			assertEquals(anchor, fixture.selected());
			Rectangle row = tree.getPathBounds(tree.getSelectionPath());
			assertNotNull(row);
			tree.dispatchEvent(new MouseEvent(tree, MouseEvent.MOUSE_CLICKED,
					System.currentTimeMillis(), 0, row.x + 4, row.y + row.height / 2, 1,
					false, MouseEvent.BUTTON1));
			assertEquals(heading, fixture.viewY());
			assertEquals(anchor, fixture.selected());
		});
	}

	// ------------------------------------------------------ document -> tree

	@Test
	void scrollingSelectsTheCurrentSectionAtExactBoundaries() throws Exception {
		for (String language : new String[] {"en", "es"}) {
			onEdt(() -> {
				Fixture fixture = new Fixture();
				fixture.load(language);
				int checked = 0;
				for (int index = 1; index < fixture.outline.size(); index++) {
					String anchor = fixture.outline.get(index).anchor();
					// At this position the anchor lies exactly on the reference line
					// (inclusive); one pixel less leaves it just below.
					int boundary = (int) Math.ceil(fixture.anchorY(anchor))
							- GeoCeDGGuideNavigator.REFERENCE_OFFSET;
					if (boundary < 1 || boundary > fixture.bottom()) {
						continue;
					}
					fixture.scrollTo(boundary);
					assertEquals(anchor, fixture.selected(), language + " at " + anchor);
					fixture.scrollTo(boundary - 1);
					assertEquals(fixture.outline.get(index - 1).anchor(), fixture.selected(),
							language + " just above " + anchor);
					checked++;
				}
				assertTrue(checked > 80, language + " checked only " + checked);
			});
		}
	}

	@Test
	void theTopAndTheBottomFollowTheSameRule() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("es");
			fixture.scrollTo(fixture.bottom());
			assertEquals(fixture.expectedForViewport(), fixture.selected());
			fixture.scrollTo(0);
			assertEquals("about-this-guide", fixture.selected());
			fixture.scrollTo(1);
			assertEquals("about-this-guide", fixture.selected());
		});
	}

	@Test
	void everyScrollPositionSelectsTheEntryOfTheIndependentRule() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			for (int y = 0; y <= fixture.bottom(); y += 97) {
				fixture.scrollTo(y);
				assertEquals(fixture.expectedForViewport(), fixture.selected(), "y=" + y);
			}
		});
	}

	// --------------------------------------------------------------- boundaries

	@Test
	void navigationChangesPresentationOnly() throws Exception {
		onEdt(() -> {
			Fixture fixture = new Fixture();
			fixture.load("en");
			String before = fixture.view.getDocument().getText(0,
					fixture.view.getDocument().getLength());
			fixture.select("locus-v2/6.5");
			fixture.scrollTo(fixture.viewY() + 500);
			assertEquals(before, fixture.view.getDocument().getText(0,
					fixture.view.getDocument().getLength()));
			assertFalse(fixture.view.isEditable());
			assertEquals(0, fixture.view.getHyperlinkListeners().length,
					"navigation must not listen to links");
		});
	}

	@Test
	void noGuideSourceIntroducesAHyperlinkListener() throws Exception {
		for (String name : new String[] {"GeoCeDGGuideWindow", "GeoCeDGGuideNavigator",
				"GeoCeDGGuideRenderer", "GeoCeDGGuideOutline"}) {
			String source = Files.readString(
					Path.of("src/main/java/org/geocedg/desktop/" + name + ".java"));
			assertFalse(source.contains("HyperlinkListener"), name);
			assertFalse(source.contains("scrollToReference"), name);
			assertFalse(source.contains("Desktop.browse"), name);
		}
	}

	@Test
	void theWindowShowsTheTreeBesideTheDocumentAndFollowsTheLanguage()
			throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"the window contract needs a display");
		GeoCeDGGuideWindow window = new GeoCeDGGuideWindow(null,
				new Font("SansSerif", Font.PLAIN, 12));
		try {
			onEdt(() -> {
				window.show(GeoCeDGGuideRenderer.render(
						GeoCeDGActionRegistry.readUserGuide("en")), "GeoCeDG user guide");
				JTree tree = window.getNavigator().getTree();
				assertTrue(SwingUtilities.isDescendingFrom(tree, window.getDialog()));
				assertTrue(SwingUtilities.isDescendingFrom(window.getView(),
						window.getDialog()));
				assertTrue(window.getDialog().getWidth() <= GeoCeDGGuideWindow.PREFERRED_WIDTH);
				assertEquals("GeoCeDG user guide", treeLabels(tree).get(0));
			});
			onEdt(() -> {
				window.getDialog().validate();
				assertTrue(window.getNavigator().navigateTo("spline-v2/7.2"));
				assertEquals("spline-v2/7.2", window.getNavigator().getSelectedAnchor());
			});
			onEdt(() -> {
				window.show(GeoCeDGGuideRenderer.render(
						GeoCeDGActionRegistry.readUserGuide("es")), "Guía");
				JTree tree = window.getNavigator().getTree();
				assertEquals("Guía de usuario de GeoCeDG", treeLabels(tree).get(0));
				assertEquals("about-this-guide", window.getNavigator().getSelectedAnchor());
			});
		} finally {
			onEdt(() -> {
				if (window.getDialog() != null) {
					window.getDialog().dispose();
				}
			});
		}
	}

	// ------------------------------------------------------------------ helpers

	private static void onEdt(EdtAction action) throws Exception {
		Exception[] failure = new Exception[1];
		Error[] error = new Error[1];
		SwingUtilities.invokeAndWait(() -> {
			try {
				action.run();
			} catch (Exception exception) {
				failure[0] = exception;
			} catch (Error assertion) {
				error[0] = assertion;
			}
		});
		// Drain whatever the step queued, such as caret visibility updates.
		SwingUtilities.invokeAndWait(() -> { });
		if (error[0] != null) {
			throw error[0];
		}
		if (failure[0] != null) {
			throw failure[0];
		}
	}

	private static TreePath path(JTree tree, String anchor) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		Enumeration<TreeNode> nodes = root.preorderEnumeration();
		while (nodes.hasMoreElements()) {
			TreeNode node = nodes.nextElement();
			if (node instanceof GeoCeDGGuideNavigator.OutlineNode
					&& ((GeoCeDGGuideNavigator.OutlineNode) node).getEntry().anchor()
							.equals(anchor)) {
				return new TreePath(((DefaultMutableTreeNode) node).getPath());
			}
		}
		throw new AssertionError("no tree node for " + anchor);
	}

	private static List<String> treeAnchors(JTree tree) {
		List<String> anchors = new ArrayList<>();
		for (GeoCeDGGuideNavigator.OutlineNode node : outlineNodes(tree)) {
			anchors.add(node.getEntry().anchor());
		}
		return anchors;
	}

	private static List<String> treeLabels(JTree tree) {
		List<String> labels = new ArrayList<>();
		for (GeoCeDGGuideNavigator.OutlineNode node : outlineNodes(tree)) {
			labels.add(node.toString());
		}
		return labels;
	}

	private static List<GeoCeDGGuideNavigator.OutlineNode> outlineNodes(JTree tree) {
		List<GeoCeDGGuideNavigator.OutlineNode> result = new ArrayList<>();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		Enumeration<TreeNode> nodes = root.preorderEnumeration();
		while (nodes.hasMoreElements()) {
			TreeNode node = nodes.nextElement();
			if (node instanceof GeoCeDGGuideNavigator.OutlineNode) {
				result.add((GeoCeDGGuideNavigator.OutlineNode) node);
			}
		}
		return result;
	}

	private static String entryLabel(Fixture fixture, String anchor) {
		return fixture.outline.stream().filter(entry -> entry.anchor().equals(anchor))
				.findFirst().orElseThrow().label();
	}
}

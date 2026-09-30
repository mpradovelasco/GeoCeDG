/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Couples the guide navigation tree with the rendered guide document.
 *
 * <p>The tree is built only from the renderer-derived {@link GeoCeDGGuideOutline}.
 * Selecting an entry scrolls the document programmatically to that entry's named
 * anchor; no hyperlink is followed and no listener reacts to links. Scrolling the
 * document selects the entry whose anchor is the last one at or above the
 * reference line, {@link #REFERENCE_OFFSET} pixels below the top of the viewport;
 * above the first anchor the first entry is current.
 *
 * <p>Two guards keep this free of feedback loops. A selection made by the
 * synchronization never navigates, and after a tree-driven navigation the
 * selection is kept while the viewport stays at the position that navigation set.
 * A selected entry near the end of the guide therefore stays selected even when
 * the document cannot scroll its heading to the top.
 *
 * <p>This is presentation state only. It changes no Markdown, no construction,
 * no preference, no geometry, no dependency graph and no serialization.
 */
final class GeoCeDGGuideNavigator {

	/** Reading tolerance below the viewport top, in pixels; not content derived. */
	static final int REFERENCE_OFFSET = 8;

	private final JEditorPane view;
	private final JScrollPane documentScroll;
	private final JTree tree;
	private final Map<String, OutlineNode> nodes = new HashMap<>();
	private List<String> anchors = List.of();
	private int[] offsets = new int[0];
	private boolean synchronizing;
	private int navigatedViewY = -1;

	/** Tree node carrying one entry; its text is the localized label only. */
	static final class OutlineNode extends DefaultMutableTreeNode {

		private static final long serialVersionUID = 1L;
		private final transient GeoCeDGGuideOutline.Entry entry;

		OutlineNode(GeoCeDGGuideOutline.Entry entry) {
			super(entry.label());
			this.entry = entry;
		}

		/** @return the outline entry shown by this node */
		GeoCeDGGuideOutline.Entry getEntry() {
			return entry;
		}
	}

	/**
	 * @param view read-only guide document
	 * @param documentScroll scroll pane whose viewport shows {@code view}
	 */
	GeoCeDGGuideNavigator(JEditorPane view, JScrollPane documentScroll) {
		this.view = view;
		this.documentScroll = documentScroll;
		// The hidden root is a Swing container only; it carries no identity.
		tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(event -> {
			if (!synchronizing) {
				navigateTo(getSelectedAnchor());
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				// Clicking the entry that is already selected returns to its heading.
				TreePath path = tree.getPathForLocation(event.getX(), event.getY());
				if (path != null && path.equals(tree.getSelectionPath())) {
					navigateTo(getSelectedAnchor());
				}
			}
		});
		documentScroll.getViewport().addChangeListener(event -> onViewportChange());
	}

	/**
	 * Replaces the tree with the outline of the document now shown by the view.
	 * Call it after the view text has been set; the first entry is selected
	 * because a newly shown guide starts at its top.
	 *
	 * @param outline entries in document order
	 */
	void load(List<GeoCeDGGuideOutline.Entry> outline) {
		synchronizing = true;
		try {
			nodes.clear();
			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			for (GeoCeDGGuideOutline.Entry entry : outline) {
				OutlineNode node = new OutlineNode(entry);
				OutlineNode parent = entry.parentAnchor() == null ? null
						: nodes.get(entry.parentAnchor());
				(parent == null ? root : parent).add(node);
				nodes.put(entry.anchor(), node);
			}
			tree.setModel(new DefaultTreeModel(root));
			for (int row = 0; row < tree.getRowCount(); row++) {
				tree.expandRow(row);
			}
			resolveAnchorOffsets(outline);
			navigatedViewY = -1;
			select(anchors.isEmpty() ? null : anchors.get(0));
		} finally {
			synchronizing = false;
		}
	}

	private void resolveAnchorOffsets(List<GeoCeDGGuideOutline.Entry> outline) {
		Map<String, Integer> found = new HashMap<>();
		Document document = view.getDocument();
		if (document instanceof HTMLDocument) {
			HTMLDocument.Iterator iterator = ((HTMLDocument) document).getIterator(HTML.Tag.A);
			for (; iterator.isValid(); iterator.next()) {
				AttributeSet attributes = iterator.getAttributes();
				Object name = attributes == null ? null
						: attributes.getAttribute(HTML.Attribute.NAME);
				if (name != null) {
					found.putIfAbsent(name.toString(), iterator.getStartOffset());
				}
			}
		}
		List<String> resolved = new ArrayList<>();
		List<Integer> positions = new ArrayList<>();
		for (GeoCeDGGuideOutline.Entry entry : outline) {
			Integer offset = found.get(entry.anchor());
			if (offset != null) {
				resolved.add(entry.anchor());
				positions.add(offset);
			}
		}
		anchors = List.copyOf(resolved);
		offsets = positions.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Scrolls the document so that the anchor of an entry is at the top of the
	 * viewport, as far as the document height allows, and selects that entry.
	 *
	 * @param anchor derived navigation identity
	 * @return whether the document was positioned
	 */
	boolean navigateTo(String anchor) {
		int index = anchor == null ? -1 : anchors.indexOf(anchor);
		double y = index < 0 ? Double.NaN : documentY(offsets[index]);
		if (Double.isNaN(y)) {
			return false;
		}
		JViewport viewport = documentScroll.getViewport();
		int bottom = Math.max(0, view.getHeight() - viewport.getExtentSize().height);
		int target = (int) Math.max(0, Math.min(bottom, Math.round(y)));
		// Recorded first: the change event of this move must not reselect.
		navigatedViewY = target;
		viewport.setViewPosition(new Point(0, target));
		synchronizing = true;
		try {
			select(anchor);
		} finally {
			synchronizing = false;
		}
		return true;
	}

	private void onViewportChange() {
		if (synchronizing) {
			return;
		}
		int y = documentScroll.getViewport().getViewPosition().y;
		if (navigatedViewY >= 0) {
			if (y == navigatedViewY) {
				return;
			}
			navigatedViewY = -1;
		}
		synchronizeTree();
	}

	/** Selects the entry that is current for the viewport, without navigating. */
	void synchronizeTree() {
		String current = getCurrentAnchor();
		if (current == null || current.equals(getSelectedAnchor())) {
			return;
		}
		synchronizing = true;
		try {
			select(current);
		} finally {
			synchronizing = false;
		}
	}

	/**
	 * @return anchor of the entry that is current for the viewport position, or
	 *         {@code null} when no anchor can be positioned
	 */
	String getCurrentAnchor() {
		if (offsets.length == 0 || Double.isNaN(documentY(offsets[0]))) {
			return null;
		}
		double reference = documentScroll.getViewport().getViewPosition().y
				+ REFERENCE_OFFSET;
		// Anchor positions grow with document order, so the last anchor at or
		// above the reference line is found by bisection.
		int low = 0;
		int high = offsets.length - 1;
		int current = 0;
		while (low <= high) {
			int middle = (low + high) >>> 1;
			if (documentY(offsets[middle]) <= reference) {
				current = middle;
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		return anchors.get(current);
	}

	private double documentY(int offset) {
		try {
			Rectangle2D bounds = view.modelToView2D(offset);
			return bounds == null ? Double.NaN : bounds.getY();
		} catch (BadLocationException exception) {
			return Double.NaN;
		}
	}

	private void select(String anchor) {
		OutlineNode node = anchor == null ? null : nodes.get(anchor);
		if (node == null) {
			tree.clearSelection();
			return;
		}
		TreePath path = new TreePath(node.getPath());
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}

	/** @return anchor of the selected entry, or {@code null} */
	String getSelectedAnchor() {
		Object selected = tree.getLastSelectedPathComponent();
		return selected instanceof OutlineNode
				? ((OutlineNode) selected).getEntry().anchor() : null;
	}

	/**
	 * @param anchor derived navigation identity
	 * @return the model offset of its named anchor in the document, or -1
	 */
	int anchorOffset(String anchor) {
		int index = anchors.indexOf(anchor);
		return index < 0 ? -1 : offsets[index];
	}

	/** @return the navigation tree */
	JTree getTree() {
		return tree;
	}
}

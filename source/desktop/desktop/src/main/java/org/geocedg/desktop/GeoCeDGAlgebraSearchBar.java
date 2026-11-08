/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.geogebra.desktop.gui.view.algebra.AlgebraHelperBar;
import org.geogebra.desktop.gui.view.algebra.GeoMutableTreeNode;

import com.formdev.flatlaf.FlatClientProperties;

/** Literal name search over the current Algebra tree, including collapsed categories. */
final class GeoCeDGAlgebraSearchBar extends AlgebraHelperBar {
	private static final long serialVersionUID = 1L;
	private static final String SUBSCRIPTS = "₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎ₐₑₒₓₔₕₖₗₘₙₚₛₜ";
	private static final String NORMAL_SUBSCRIPTS = "0123456789+-=()aeoxəhklmnpst";
	private static final Pattern BRACED_INDEX = Pattern.compile("_\\{([^{}]+)}");

	private final GeoCeDGAlgebraView view;
	private final AppGeoCeDG product;
	private final JTextField query = new JTextField(9);
	private final JButton previous = new JButton("‹");
	private final JButton next = new JButton("›");
	private final JLabel position = new JLabel();
	private List<TreePath> matches = List.of();
	private TreePath current;
	private boolean refreshPending;

	GeoCeDGAlgebraSearchBar(GeoCeDGAlgebraView view, AppGeoCeDG product) {
		super(view, product);
		this.view = view;
		this.product = product;
		query.setName("geocedg.algebra.search");
		query.addActionListener(event -> navigate(1));
		previous.addActionListener(event -> navigate(-1));
		next.addActionListener(event -> navigate(1));
		query.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				queryChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				queryChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				queryChanged();
			}
		});
		TreeModelListener changes = new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent event) {
				// Algebra renames remove/reinsert the node. Value and style repaints
				// leave the set of names intact, even during an animation.
			}

			@Override
			public void treeNodesInserted(TreeModelEvent event) {
				invalidateMatches();
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent event) {
				invalidateMatches();
			}

			@Override
			public void treeStructureChanged(TreeModelEvent event) {
				invalidateMatches();
			}
		};
		view.getModel().addTreeModelListener(changes);
		view.addPropertyChangeListener("model", event -> {
			if (event.getOldValue() instanceof TreeModel oldModel) {
				oldModel.removeTreeModelListener(changes);
			}
			if (event.getNewValue() instanceof TreeModel newModel) {
				newModel.addTreeModelListener(changes);
			}
			invalidateMatches();
		});
		addSearch();
		updateSearchLabels();
		showPosition();
	}

	private void addSearch() {
		addSeparator();
		query.setFont(product.getAlgebraFont());
		previous.setFont(product.getAlgebraFont());
		next.setFont(product.getAlgebraFont());
		position.setFont(product.getAlgebraFont());
		add(query);
		add(previous);
		add(next);
		add(position);
	}

	@Override
	public void update() {
		super.update();
		addSearch();
	}

	@Override
	public void updateLabels() {
		super.updateLabels();
		if (product != null) {
			updateSearchLabels();
		}
	}

	private void updateSearchLabels() {
		label(query, "Algebra.Search.Name");
		query.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, query.getToolTipText());
		label(previous, "Algebra.Search.Previous");
		label(next, "Algebra.Search.Next");
	}

	private void label(JComponent component, String key) {
		String text = product.layerText(key);
		component.setToolTipText(text);
		component.getAccessibleContext().setAccessibleName(text);
	}

	private void queryChanged() {
		current = null;
		invalidateMatches();
	}

	private void invalidateMatches() {
		if (!refreshPending) {
			refreshPending = true;
			SwingUtilities.invokeLater(this::refreshMatches);
		}
	}

	private void refreshMatches() {
		if (!refreshPending) {
			return;
		}
		refreshPending = false;
		matches = find(view.getModel().getRoot(), query.getText());
		if (current != null && !matches.contains(current)) {
			current = null;
		}
		showPosition();
	}

	void navigate(int direction) {
		refreshMatches();
		if (matches.isEmpty()) {
			return;
		}
		int index = current == null ? -1 : matches.indexOf(current);
		int target = index < 0 ? (direction < 0 ? matches.size() - 1 : 0)
				: Math.floorMod(index + direction, matches.size());
		TreePath path = matches.get(target);
		GeoMutableTreeNode node = (GeoMutableTreeNode) path.getLastPathComponent();
		if (node.getRoot() != view.getModel().getRoot()
				|| !path.equals(new TreePath(node.getPath()))) {
			invalidateMatches();
			return;
		}
		current = path;
		view.setSelectionPath(path);
		view.scrollPathToVisible(path);
		product.getSelectionManager().setSelectedGeos(List.of(node.getGeo()));
		showPosition();
	}

	private void showPosition() {
		previous.setEnabled(!matches.isEmpty());
		next.setEnabled(!matches.isEmpty());
		position.setText(query.getText().isBlank() ? ""
				: (current == null ? 0 : matches.indexOf(current) + 1) + "/" + matches.size());
	}

	static List<TreePath> find(Object root, String query) {
		String needle = normalize(query.strip());
		if (needle.isEmpty() || !(root instanceof DefaultMutableTreeNode tree)) {
			return List.of();
		}
		List<TreePath> exact = new ArrayList<>();
		List<TreePath> partial = new ArrayList<>();
		Enumeration<?> nodes = tree.preorderEnumeration();
		while (nodes.hasMoreElements()) {
			if (nodes.nextElement() instanceof GeoMutableTreeNode node) {
				String label = node.getGeo().getLabelSimple();
				if (label == null) {
					continue;
				}
				String name = normalize(label);
				if (name.equals(needle)) {
					exact.add(new TreePath(node.getPath()));
				} else if (name.contains(needle)) {
					partial.add(new TreePath(node.getPath()));
				}
			}
		}
		exact.addAll(partial);
		return exact;
	}

	private static String normalize(String text) {
		String input = BRACED_INDEX.matcher(Normalizer.normalize(text, Normalizer.Form.NFC)
				.toLowerCase(Locale.ROOT)).replaceAll("_$1");
		StringBuilder plain = new StringBuilder(input.length());
		boolean inSubscript = false;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			int subscript = SUBSCRIPTS.indexOf(c);
			if (subscript >= 0) {
				if (!inSubscript && (i == 0 || input.charAt(i - 1) != '_')) {
					plain.append('_');
				}
				plain.append(NORMAL_SUBSCRIPTS.charAt(subscript));
			} else {
				plain.append(c);
			}
			inSubscript = subscript >= 0;
		}
		return plain.toString();
	}
}

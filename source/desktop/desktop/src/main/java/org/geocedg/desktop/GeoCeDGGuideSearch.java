/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/** Search controls for the read-only guide window. */
final class GeoCeDGGuideSearch {
	private static final Highlighter.HighlightPainter MATCH_PAINTER =
			new DefaultHighlighter.DefaultHighlightPainter(new Color(0xF6C448));
	private final JEditorPane view;
	private final GeoCeDGGuideNavigator navigator;
	private final JPanel panel = new JPanel(new BorderLayout(6, 4));
	private final JLabel label = new JLabel();
	private final JTextField query = new JTextField();
	private final JButton clear = new JButton();
	private final JLabel status = new JLabel();
	private final JList<GeoCeDGGuideSearchIndex.Match> results = new JList<>();
	private final JScrollPane resultsScroll = new JScrollPane(results);
	private GeoCeDGGuideSearchIndex index;
	private boolean spanish;
	private Object highlight;

	GeoCeDGGuideSearch(JEditorPane view, GeoCeDGGuideNavigator navigator) {
		this.view = view;
		this.navigator = navigator;
		JPanel controls = new JPanel(new BorderLayout(6, 0));
		label.setLabelFor(query);
		query.setToolTipText("Ctrl+F");
		controls.add(label, BorderLayout.WEST);
		controls.add(query, BorderLayout.CENTER);
		controls.add(clear, BorderLayout.EAST);
		panel.add(controls, BorderLayout.NORTH);
		panel.add(status, BorderLayout.CENTER);
		panel.add(resultsScroll, BorderLayout.SOUTH);
		status.setVisible(false);
		resultsScroll.setVisible(false);
		results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultListCellRenderer renderer = new DefaultListCellRenderer();
		renderer.putClientProperty("html.disable", Boolean.TRUE);
		results.setCellRenderer(renderer);
		query.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				refresh();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				refresh();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				refresh();
			}
		});
		query.addActionListener(event -> focusResults());
		clear.addActionListener(event -> clearSearch());
		bind(query, JComponent.WHEN_FOCUSED, KeyEvent.VK_DOWN, 0,
				"focusGuideResults", this::focusResults);
		bind(results, JComponent.WHEN_FOCUSED, KeyEvent.VK_ENTER, 0,
				"openGuideResult", this::openResult);
		results.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				int row = results.locationToIndex(event.getPoint());
				if (SwingUtilities.isLeftMouseButton(event) && row >= 0
						&& results.getCellBounds(row, row).contains(event.getPoint())) {
					results.setSelectedIndex(row);
					openResult();
				}
			}
		});
	}

	void installShortcuts(JRootPane root) {
		root.putClientProperty(GeoCeDGGuideSearch.class, this);
		bind(root, JComponent.WHEN_IN_FOCUSED_WINDOW, KeyEvent.VK_F,
				InputEvent.CTRL_DOWN_MASK, "focusGuideSearch", () -> {
					query.requestFocusInWindow();
					query.selectAll();
				});
	}

	/** Handles Escape before Desktop's global shortcut can hide the guide dialog. */
	static boolean dispatchKey(KeyEvent event) {
		if (event.getKeyCode() != KeyEvent.VK_ESCAPE) {
			return false;
		}
		JRootPane root = SwingUtilities.getRootPane(event.getComponent());
		if (root == null || !(root.getClientProperty(GeoCeDGGuideSearch.class)
				instanceof GeoCeDGGuideSearch search) || search.query.getText().isEmpty()) {
			return false;
		}
		// Keep the query until release so both key events stay with the guide.
		if (event.getID() == KeyEvent.KEY_RELEASED) {
			search.clearSearch();
		}
		return true;
	}

	private static void bind(JComponent component, int condition, int key, int modifiers,
			String action, Runnable run) {
		component.getInputMap(condition).put(KeyStroke.getKeyStroke(key, modifiers), action);
		component.getActionMap().put(action, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				run.run();
			}
		});
	}

	void load(List<GeoCeDGGuideOutline.Entry> outline, String title, String language) {
		spanish = "es".equals(language);
		label.setText(spanish ? "Buscar en la guía" : "Search the guide");
		clear.setText(spanish ? "Limpiar" : "Clear");
		results.getAccessibleContext().setAccessibleName(
				spanish ? "Resultados de búsqueda" : "Search results");
		navigator.getTree().getAccessibleContext().setAccessibleName(
				spanish ? "Secciones de la guía" : "Guide sections");
		index = new GeoCeDGGuideSearchIndex(view.getDocument(), outline, navigator, title);
		query.setText("");
		refresh();
	}

	JPanel getPanel() {
		return panel;
	}

	private void refresh() {
		removeHighlight();
		boolean searching = !query.getText().isBlank();
		DefaultListModel<GeoCeDGGuideSearchIndex.Match> model = new DefaultListModel<>();
		if (searching && index != null) {
			GeoCeDGGuideSearchIndex.Results found = index.find(query.getText());
			model.addAll(found.matches());
			status.setText(statusText(model.size(), found.truncated()));
		}
		results.setModel(model);
		results.setVisibleRowCount(Math.min(4, model.size()));
		if (!model.isEmpty()) {
			Dimension viewport = results.getPreferredScrollableViewportSize();
			Insets insets = resultsScroll.getInsets();
			int horizontalBar = resultsScroll.getHorizontalScrollBar().getPreferredSize().height;
			resultsScroll.setPreferredSize(new Dimension(viewport.width,
					viewport.height + insets.top + insets.bottom + horizontalBar));
		}
		resultsScroll.setVisible(!model.isEmpty());
		status.setVisible(searching);
		clear.setEnabled(!query.getText().isEmpty());
	}

	private String statusText(int count, boolean truncated) {
		if (count == 0) {
			return spanish ? "Sin coincidencias" : "No matches";
		}
		if (truncated) {
			return spanish ? "Primeras " + count + " coincidencias" : "First " + count + " matches";
		}
		if (count == 1) {
			return spanish ? "1 coincidencia" : "1 match";
		}
		return count + (spanish ? " coincidencias" : " matches");
	}

	private void focusResults() {
		if (results.getModel().getSize() > 0) {
			results.setSelectedIndex(0);
			results.ensureIndexIsVisible(0);
			results.requestFocusInWindow();
		}
	}

	private void clearSearch() {
		query.setText("");
		query.requestFocusInWindow();
	}

	private void openResult() {
		GeoCeDGGuideSearchIndex.Match match = results.getSelectedValue();
		if (match == null || !navigator.navigateToMatch(match.anchor(), match.start())) {
			return;
		}
		removeHighlight();
		try {
			highlight = view.getHighlighter().addHighlight(match.start(), match.end(), MATCH_PAINTER);
		} catch (BadLocationException exception) {
			throw new IllegalStateException("Guide match is outside the current document", exception);
		}
	}

	private void removeHighlight() {
		if (highlight != null) {
			view.getHighlighter().removeHighlight(highlight);
			highlight = null;
		}
	}
}

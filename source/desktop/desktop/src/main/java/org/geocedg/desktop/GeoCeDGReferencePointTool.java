/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.AppD;

/** Desktop selection only; all mathematical admission and publication belong to H1. */
final class GeoCeDGReferencePointTool {
	private final AppD app;
	private GeoLocusV2 source;
	private M08SnapshotPlacement2D.Selection selection;
	private LocusPointInteractionPolicy2D policy = LocusPointInteractionPolicy2D.initial(.1);
	private Object document;
	private Object view;
	private long generation;
	private JPopupMenu popup;
	private JWindow strip;
	private JDialog settings;
	private JTextField status;
	private boolean acceptingMenu;
	private String lastFailure;

	GeoCeDGReferencePointTool(AppD app) { this.app = app; }

	private String t(String en, String es) {
		return "es".equals(app.getLocale().getLanguage()) ? es : en;
	}

	void activate() {
		reset();
		view = app.getActiveEuclidianView();
		Object current = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		if (current != document) {
			policy = LocusPointInteractionPolicy2D.initial(.1);
			document = current;
		}
		if (java.awt.GraphicsEnvironment.isHeadless() || app.getMainComponent() == null
				|| !app.getMainComponent().isShowing()) { return; }
		strip = new JWindow(javax.swing.SwingUtilities.getWindowAncestor(app.getMainComponent()));
		JPanel panel = new JPanel(new BorderLayout(6, 3));
		status = new JTextField(36);
		status.setEditable(false);
		JButton options = new JButton(t("Policy…", "Política…"));
		options.addActionListener(event -> editPolicy());
		panel.add(status, BorderLayout.CENTER);
		JPanel scope = new JPanel(new GridLayout(0, 1));
		scope.add(new JLabel(t("Fixed scope: all supported branches/components",
				"Alcance fijo: todas las ramas/componentes admitidas")));
		scope.add(new JLabel(t("Global coverage not established", "Cobertura global no establecida")));
		panel.add(scope, BorderLayout.SOUTH);
		panel.add(options, BorderLayout.EAST);
		panel.registerKeyboardAction(event -> activate(), KeyStroke.getKeyStroke("ESCAPE"),
				javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
		strip.setContentPane(panel);
		updateStatus(t("Select L", "Seleccione L"));
		strip.pack();
		java.awt.Point origin = app.getMainComponent().getLocationOnScreen();
		strip.setLocation(origin.x + 12, origin.y + Math.max(0, app.getMainComponent().getHeight() - strip.getHeight() - 16));
		strip.setAutoRequestFocus(false);
		strip.setVisible(true);
	}

	void reset() {
		generation++;
		source = null;
		if (selection != null) { selection.close(); selection = null; }
		if (popup != null) { popup.setVisible(false); popup = null; }
		if (settings != null) { settings.dispose(); settings = null; }
		if (strip != null) { strip.dispose(); strip = null; }
		app.getSelectionManager().clearSelectedGeos();
	}

	private void updateStatus(String message) {
		if (status != null) {
			status.setText(message + " • r=" + policy.getMaximumWorldDistance()
					+ " world");
			status.setCaretPosition(0);
			status.setToolTipText(status.getText());
		}
	}

	void click(List<GeoElement> hits, Component anchor, int x, int y) {
		if (document != app.getKernel().getConstruction().getSpatialIdentityRegistry()) { activate(); }
		if (selection != null) { return; }
		List<GeoElement> loci = hits.stream().filter(geo -> geo instanceof GeoLocusV2).toList();
		if (source == null || !loci.isEmpty() && hits.stream().noneMatch(geo -> geo instanceof GeoPoint)) {
			choose(loci, anchor, x, y, geo -> {
				source = (GeoLocusV2) geo;
				app.getSelectionManager().clearSelectedGeos();
				app.getSelectionManager().addSelectedGeo(source);
				updateStatus(t("Select existing Q", "Seleccione Q existente"));
			});
			return;
		}
		choose(hits.stream().filter(geo -> geo instanceof GeoPoint).toList(), anchor, x, y,
				geo -> query((GeoPoint) geo, anchor, x, y));
	}

	private <T> void choose(List<T> values, Component anchor, int x, int y, Consumer<T> accept) {
		if (values.isEmpty()) { return; }
		if (values.size() == 1) { accept.accept(values.get(0)); return; }
		showChoices(values, anchor, x, y, Object::toString, accept,
				t("Choose construction object", "Elija objeto de construcción"));
	}

	void query(GeoPoint q, Component anchor, int x, int y) {
		try {
			selection = M08SnapshotPlacement2D.queryFromReference(source, q, policy);
			var result = selection.getResult();
			if (result.getPermission() != M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED) {
				refuse(result.getFailure().name() + " / " + result.getLocalFailure());
				return;
			}
			showChoices(result.getChoices(), anchor, x, y, choice -> {
				var address = choice.getAddress();
				return address.getBranchKey() + " / " + address.getComponentLineageKey()
						+ " / u=" + address.getCanonicalParameter();
			}, this::commit, t("Verified local address; global uniqueness not established",
					"Dirección local verificada; unicidad global no establecida"));
		} catch (M08SnapshotPlacement2D.Rejected failure) { refuse(failure.getFailure().name()); }
	}

	GeoPoint commit(M08SnapshotPlacement2D.Choice choice) {
		if (app.getActiveEuclidianView() != view) { refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name()); return null; }
		try {
			GeoPoint point = selection.commit(null, choice);
			app.storeUndoInfo();
			app.getKernel().notifyRepaint();
			activate();
			return point;
		} catch (M08SnapshotPlacement2D.Rejected failure) {
			refuse(failure.getFailure().name());
			return null;
		}
	}

	private String describeFailure(String code) {
		return switch (code) {
		case "INVALID_SOURCE" -> t("Invalid source", "Fuente inválida");
		case "INVALID_REFERENCE" -> t("Invalid reference point", "Punto de referencia inválido");
		case "BUDGET_EXHAUSTED" -> t("Work budget exhausted", "Presupuesto de trabajo agotado");
		case "STALE_SELECTION" -> t("Selection is stale; select L and Q again", "Selección caducada; seleccione L y Q de nuevo");
		case "FOREIGN_CHOICE" -> t("Choice belongs to another query", "Dirección ajena a la consulta");
		case "CONSUMED_SELECTION" -> t("Selection already consumed", "Selección ya consumida");
		case "COMPONENT_NOT_ESTABLISHED" -> t("Component not established", "Componente no establecida");
		case "UNSAFE_SEARCH" -> t("Unsafe search barrier", "Barrera de seguridad de búsqueda");
		case "AUTHORITY_NOT_ESTABLISHED" -> t("Source authority not established", "Autoridad de la fuente no establecida");
		case "FORWARD_MODEL_MISMATCH" -> t("Forward model mismatch", "Discrepancia del modelo forward");
		case "LOCAL_EVIDENCE_NOT_ESTABLISHED", "NO_VERIFIED_ADDRESS" -> t("No address has sufficient local evidence", "Ninguna dirección tiene evidencia local suficiente");
		default -> t("Operation rejected", "Operación rechazada");
		};
	}

	private void refuse(String reason) {
		generation++;
		lastFailure = reason;
		if (selection != null) { selection.close(); selection = null; }
		if (popup != null) { popup.setVisible(false); }
		source = null;
		app.getSelectionManager().clearSelectedGeos();
		updateStatus(t("No point created: ", "No se creó ningún punto: ") + describeFailure(reason.split(" / ")[0]) + " [" + reason + "]");
	}

	M08SnapshotPlacement2D.Selection pendingSelection() { return selection; }
	JPopupMenu contextMenu() { return popup; }
	String lastFailure() { return lastFailure; }
	void setPolicy(LocusPointInteractionPolicy2D value) {
		policy = java.util.Objects.requireNonNull(value);
		activate();
	}

	private <T> void showChoices(List<T> values, Component anchor, int x, int y,
			java.util.function.Function<T, String> describe, Consumer<T> accept, String title) {
		long ticket = generation;
		popup = new JPopupMenu();
		JPanel content = new JPanel(new BorderLayout(4, 4));
		JTextField heading = new JTextField(title, 42);
		heading.setEditable(false);
		content.add(heading, BorderLayout.NORTH);
		JList<String> list = new JList<>(values.stream().map(describe).toArray(String[]::new));
		list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(Math.min(5, values.size()));
		// Bound the viewport, not the semantic text: retain horizontal scrolling.
		list.getAccessibleContext().setAccessibleName(title);
		JScrollPane addresses = new JScrollPane(list);
		addresses.setPreferredSize(new java.awt.Dimension(
				42 * list.getFontMetrics(list.getFont()).charWidth('0') + 24,
				Math.min(5, values.size()) * list.getFontMetrics(list.getFont()).getHeight()
						+ addresses.getHorizontalScrollBar().getPreferredSize().height + 8));
		content.add(addresses, BorderLayout.CENTER);
		JButton create = new JButton(t("Confirm", "Confirmar"));
		create.setEnabled(values.size() == 1);
		list.addListSelectionListener(event -> create.setEnabled(list.getSelectedIndex() >= 0));
		create.addActionListener(event -> {
			if (ticket != generation) { return; }
			int index = values.size() == 1 ? 0 : list.getSelectedIndex();
			if (index < 0) { return; }
			generation++;
			acceptingMenu = true;
			try {
				popup.setVisible(false);
				accept.accept(values.get(index)); // transient handle, never a persisted ordinal
			} finally { acceptingMenu = false; }
		});
		JButton cancel = new JButton(t("Cancel", "Cancelar"));
		cancel.addActionListener(event -> activate());
		JPanel buttons = new JPanel(); buttons.add(create); buttons.add(cancel);
		content.add(buttons, BorderLayout.SOUTH);
		content.registerKeyboardAction(event -> { if (create.isEnabled()) { create.doClick(); } },
				KeyStroke.getKeyStroke("ENTER"), javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		content.registerKeyboardAction(event -> activate(), KeyStroke.getKeyStroke("ESCAPE"),
				javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
		popup.add(content);
		popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
			public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent event) { }
			public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent event) {
				if (!acceptingMenu && ticket == generation) { activate(); }
			}
			public void popupMenuCanceled(javax.swing.event.PopupMenuEvent event) {
				if (ticket == generation) { activate(); }
			}
		});
		if (anchor != null && anchor.isShowing()) { popup.show(anchor, x, y); list.requestFocusInWindow(); }
	}

	private void editPolicy() {
		reset();
		settings = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(app.getMainComponent()));
		settings.setTitle(t("Reference point policy (session)", "Política de referencia (sesión)"));
		JPanel fields = new JPanel(new GridLayout(0, 2, 5, 3));
		var budget = policy.getWorkBudget();
		String[] labels = {t("World radius", "Radio world"), t("Equal-distance tolerance", "Tolerancia de empate en distancia"),
				t("Parameter tolerance", "Tolerancia parámetro"), t("Semantic misses", "Misses semánticos"),
				t("Subdivisions / interval passes", "Subdivisiones / pases intervalares"),
				t("Refinement iterations", "Iteraciones refinamiento"), t("Candidates", "Candidatos")};
		String[] initial = {"" + policy.getMaximumWorldDistance(), "" + policy.getEqualDistanceTolerance(),
				"" + policy.getParameterTolerance(), "" + budget.getMaximumSemanticEvaluations(),
				"" + budget.getMaximumSubdivisions(), "" + budget.getMaximumRefinementIterations(),
				"" + budget.getMaximumCandidates()};
		JTextField[] inputs = new JTextField[labels.length];
		for (int i = 0; i < labels.length; i++) {
			inputs[i] = new JTextField(initial[i], 14);
			inputs[i].setToolTipText(i < 3 ? t("Finite positive number; decimal point", "Número finito positivo; punto decimal")
					: t("Positive integer in existing policy units", "Entero positivo en las unidades de la política vigente"));
			JLabel label = new JLabel(labels[i]); label.setLabelFor(inputs[i]);
			inputs[i].getAccessibleContext().setAccessibleName(labels[i]);
			fields.add(label); fields.add(inputs[i]);
		}
		JButton apply = new JButton(t("Apply", "Aplicar"));
		JLabel error = new JLabel(t("Fixed scope: all supported branches/components", "Alcance fijo: todas las ramas/componentes admitidas"));
		apply.addActionListener(event -> {
			try {
				policy = new LocusPointInteractionPolicy2D(Double.parseDouble(inputs[0].getText()),
						Double.parseDouble(inputs[1].getText()), Double.parseDouble(inputs[2].getText()),
						new LocusPointInteractionWorkBudget2D(Integer.parseInt(inputs[3].getText()),
								Integer.parseInt(inputs[4].getText()), Integer.parseInt(inputs[5].getText()),
								Integer.parseInt(inputs[6].getText())));
				activate();
			} catch (IllegalArgumentException invalid) { error.setText(t("Invalid policy", "Política inválida")); }
		});
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.add(error, BorderLayout.NORTH); panel.add(fields, BorderLayout.CENTER); panel.add(apply, BorderLayout.SOUTH);
		panel.registerKeyboardAction(event -> activate(), KeyStroke.getKeyStroke("ESCAPE"),
				javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
		settings.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		settings.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override public void windowClosing(java.awt.event.WindowEvent event) { activate(); }
		});
		settings.setContentPane(panel); settings.pack(); settings.setLocationRelativeTo(app.getMainComponent());
		settings.setVisible(true);
	}
}

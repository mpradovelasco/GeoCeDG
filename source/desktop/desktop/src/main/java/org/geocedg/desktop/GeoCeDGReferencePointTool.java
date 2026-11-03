/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.AppD;

/** Desktop selection; semantic admission and native point-on-path evaluation stay kernel-owned. */
final class GeoCeDGReferencePointTool {
	private final AppD app;
	private GeoElement source;
	private GeoCeDGReferencePlacement selection;
	private GeoCeDGNativePointPlacement nativeSelection;
	private LocusPointInteractionPolicy2D policy = LocusPointInteractionPolicy2D.initial(.1);
	private Object document;
	private Object view;
	private long generation;
	private JPanel panel;
	private JPanel choicesHost;
	private JPanel choices;
	private JTextField status;
	private JLabel coverage;
	private JTextField[] inputs;
	private boolean fillingPolicy;
	private boolean policyDirty;
	private String lastFailure;
	private boolean worldPlacement;
	private boolean clickPosition;
	private boolean draggable = true;
	private boolean fromReference;

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
			clickPosition = false;
			draggable = true;
			fromReference = false;
			document = current;
		}
		buildPanel();
		if (app.getGuiManager() instanceof GuiManagerGeoCeDG gui) {
			gui.setReferencePointPanel(panel);
		}
		updateStatus(app.getMode() != EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT
				&& !fromReference ? t("Click to create a point", "Pulse para crear un punto")
				: clickPosition ? t("Click object to choose a position", "Pulse el objeto para elegir una posición")
				: t("Select object", "Seleccione objeto"));
	}

	void reset() {
		clearSelection();
		worldPlacement = false;
		source = null;
		if (app.getGuiManager() instanceof GuiManagerGeoCeDG gui && panel != null) {
			gui.removeReferencePointPanel(panel);
		}
		panel = null;
		status = null;
		coverage = null;
		inputs = null;
		policyDirty = false;
		app.getSelectionManager().clearSelectedGeos();
	}

	private void clearSelection() {
		generation++;
		if (selection != null) { selection.close(); selection = null; }
		if (nativeSelection != null) { nativeSelection.close(); nativeSelection = null; }
		clearChoices();
	}

	private void clearChoices() {
		choices = null;
		if (choicesHost != null) {
			choicesHost.removeAll();
			choicesHost.revalidate();
			choicesHost.repaint();
		}
	}

	private void updateStatus(String message) {
		if (status != null) {
			status.setText(message);
			status.setCaretPosition(0);
			status.setToolTipText(message);
		}
	}

	void click(List<GeoElement> hits, Component anchor, int x, int y) {
		if (document != app.getKernel().getConstruction().getSpatialIdentityRegistry()) { activate(); }
		if (!applyPolicy() || selection != null || nativeSelection != null || choices != null) { return; }
		List<GeoElement> loci = hits.stream().filter(geo -> geo instanceof GeoLocusV2
				|| GeoCeDGNativePointPlacement.supports(geo)).toList();
		if (clickPosition && app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT) {
			var requestView = app.getActiveEuclidianView();
			if (requestView == null) { refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name()); return; }
			double worldX = requestView.toRealWorldCoordX(x);
			double worldY = requestView.toRealWorldCoordY(y);
			choose(loci, geo -> {
				if (app.getActiveEuclidianView() != requestView) {
					refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name());
					return;
				}
				queryWorld(geo, worldX, worldY);
			});
			return;
		}
		if (source == null || !loci.isEmpty() && hits.stream().noneMatch(geo -> geo instanceof GeoPoint)) {
			choose(loci, geo -> {
				source = geo;
				app.getSelectionManager().clearSelectedGeos();
				app.getSelectionManager().addSelectedGeo(source);
				updateStatus(t("Select existing Q", "Seleccione Q existente"));
			});
			return;
		}
		choose(hits.stream().filter(geo -> geo instanceof GeoPoint).toList(),
				geo -> query((GeoPoint) geo, anchor, x, y));
	}

	private <T> void choose(List<T> values, Consumer<T> accept) {
		if (values.isEmpty()) { return; }
		if (values.size() == 1) { accept.accept(values.get(0)); return; }
		showChoices(values, Object::toString, accept,
				t("Choose construction object", "Elija objeto de construcción"));
	}

	void query(GeoPoint q, Component anchor, int x, int y) {
		try {
			if (source instanceof GeoLocusV2 locus) {
				selection = new GeoCeDGReferencePlacement(locus, q, policy);
				showSelection();
			} else {
				nativeSelection = new GeoCeDGNativePointPlacement(source, q,
						q.getInhomX(), q.getInhomY(), policy.getMaximumWorldDistance());
				showNativeSelection();
			}
		} catch (M08SnapshotPlacement2D.Rejected failure) { refuse(failure.getFailure().name()); }
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); }
		catch (IllegalArgumentException failure) { refuse(failure.getMessage()); }
	}

	/** A canvas request uses the same world policy and confirmation as an existing Q. */
	void queryWorld(GeoElement target, double x, double y) {
		if (worldPlacement && !applyPolicy()) { return; }
		activate();
		worldPlacement = true;
		source = target;
		app.getSelectionManager().addSelectedGeo(source);
		try {
			if (source instanceof GeoLocusV2 locus) {
				selection = new GeoCeDGReferencePlacement(locus, x, y, policy);
				showSelection();
			} else {
				nativeSelection = new GeoCeDGNativePointPlacement(source, null, x, y,
						policy.getMaximumWorldDistance());
				showNativeSelection();
			}
		} catch (M08SnapshotPlacement2D.Rejected failure) { refuse(failure.getFailure().name()); }
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); }
		catch (IllegalArgumentException failure) { refuse(failure.getMessage()); }
	}

	boolean isWorldPlacementActive() { return worldPlacement; }
	boolean hasPendingWorldSelection() { return worldPlacement && (selection != null || nativeSelection != null); }
	boolean isReferenceRequestActive() {
		return app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT
				|| fromReference && (app.getMode() == EuclidianConstants.MODE_POINT
						|| app.getMode() == EuclidianConstants.MODE_POINT_ON_OBJECT);
	}
	boolean isPanelActive() { return panel != null; }
	boolean isDraggingAllowed() { return draggable; }

	/** Finish an invalidated world offer before handing a new press to any authoring route. */
	boolean prepareCanvasRequest() {
		if (!worldPlacement) { return true; }
		if (!applyPolicy()) { return false; }
		activate();
		return true;
	}

	private void restart() {
		if (app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT
				|| app.getMode() == EuclidianConstants.MODE_POINT
				|| app.getMode() == EuclidianConstants.MODE_POINT_ON_OBJECT) {
			activate();
		} else {
			reset();
		}
	}

	private void showSelection() {
		if (selection.getRoute() == GeoCeDGReferencePlacement.Route.REJECTED) {
			refuse(selection.getFailure());
			return;
		}
		boolean historical = selection.getRoute() == GeoCeDGReferencePlacement.Route.R1_R6;
		boolean individual = selection.getRoute() == GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION;
		String title = historical ? "R1/R6: " + selection.getHistoricalResult().getStatus()
				: individual ? t("Forward address; numerical proximity is uncertified",
						"Dirección forward; proximidad numérica no certificada")
				: t("Verified local address; global uniqueness not established",
						"Dirección local verificada; unicidad global no establecida");
		coverage.setText(historical ? "R1/R6: " + selection.getHistoricalResult().getSearchCoverage()
				: individual ? t("Individual forward address; global coverage not established",
						"Dirección forward individual; cobertura global no establecida")
				: t("R2/H1: global coverage not established", "R2/H1: cobertura global no establecida"));
		updateStatus(t("Confirm a semantic address", "Confirme una dirección semántica"));
		showChoices(selection.getChoices(), choice -> {
			var address = choice.getAddress();
			return address.getBranchKey() + " / " + address.getComponentLineageKey()
					+ " / u=" + address.getCanonicalParameter();
		}, this::commit, title);
	}

	GeoPoint commit(GeoCeDGReferencePlacement.Choice choice) {
		if (app.getActiveEuclidianView() != view) { refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name()); return null; }
		try {
			GeoPoint point = selection.commit(choice);
			if (point == null) { restart(); return null; }
			point.setFixed(!draggable);
			app.storeUndoInfo();
			app.getKernel().notifyRepaint();
			restart();
			return point;
		} catch (M08SnapshotPlacement2D.Rejected failure) {
			refuse(failure.getFailure().name());
			return null;
		}
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); return null; }
	}

	private void showNativeSelection() {
		updateStatus(t("Confirm native point on object", "Confirme un punto nativo sobre el objeto"));
		showChoices(List.of(nativeSelection), ignored -> source.getLabelSimple(), ignored -> {
			if (app.getActiveEuclidianView() != view) { refuse("STALE_SELECTION"); return; }
			try {
				nativeSelection.commit(draggable);
				app.storeUndoInfo();
				app.getKernel().notifyRepaint();
				restart();
			} catch (IllegalStateException failure) { refuse(failure.getMessage()); }
		}, t("Native object placement; numerical selection", "Colocación nativa; selección numérica"));
	}

	private String describeFailure(String code) {
		return switch (code) {
		case "R1_FORWARD_REJECTED" -> t("R1 forward publication rejected", "Publicación forward R1 rechazada");
		case "CERTIFIED_ABSENCE" -> t("Certified absence within the R1 query policy", "Ausencia certificada bajo la política R1");
		case "DEGENERATE_SOURCE" -> t("Degenerate source", "Fuente degenerada");
		case "UNSUPPORTED_CAPABILITY" -> t("Unsupported capability", "Capacidad no admitida");
		case "UNSUPPORTED_CAPTURE" -> t("Source outside the supported reconstructible v3 scope",
				"Fuente fuera del perímetro reconstructible v3 admitido");
		case "LOCAL_EVIDENCE_INSUFFICIENT" -> t("Insufficient safe local evidence", "Evidencia local segura insuficiente");
		case "INVALID_SOURCE" -> t("Invalid source", "Fuente inválida");
		case "INVALID_REFERENCE" -> t("Invalid reference point", "Punto de referencia inválido");
		case "BUDGET_EXHAUSTED" -> t("Work budget exhausted", "Presupuesto de trabajo agotado");
		case "STALE_SELECTION" -> t("Selection is stale; make a new request", "Selección caducada; realice una petición nueva");
		case "FOREIGN_CHOICE" -> t("Choice belongs to another query", "Dirección ajena a la consulta");
		case "CONSUMED_SELECTION" -> t("Selection already consumed", "Selección ya consumida");
		case "COMPONENT_NOT_ESTABLISHED" -> t("Component not established", "Componente no establecida");
		case "UNSAFE_SEARCH", "UNSAFE_DISCOVERY" -> t("Unsafe search barrier", "Barrera de seguridad de búsqueda");
		case "AUTHORITY_NOT_ESTABLISHED" -> t("Source authority not established", "Autoridad de la fuente no establecida");
		case "FORWARD_MODEL_MISMATCH" -> t("Forward model mismatch", "Discrepancia del modelo forward");
		case "INVALID_FORWARD" -> t("Forward reconstruction could not be validated",
				"No se pudo validar la reconstrucción forward");
		case "NO_ADDRESS_DISCOVERED" -> t("No admissible address found in this query",
				"No se encontró una dirección admisible en esta consulta");
		case "NATIVE_PLACEMENT_REJECTED" -> t("Native placement within the query radius could not be validated",
				"No se pudo validar la colocación nativa dentro del radio de la consulta");
		case "LOCAL_EVIDENCE_NOT_ESTABLISHED", "NO_VERIFIED_ADDRESS" -> t("No address has sufficient local evidence", "Ninguna dirección tiene evidencia local suficiente");
		default -> t("Operation rejected", "Operación rechazada");
		};
	}


	private void refuse(String reason) {
		lastFailure = reason;
		clearSelection();
		source = null;
		app.getSelectionManager().clearSelectedGeos();
		updateStatus(t("No point created: ", "No se creó ningún punto: ") + describeFailure(reason.split(" / ")[0]) + " [" + reason + "]");
	}

	GeoCeDGReferencePlacement pendingSelection() { return selection; }
	JPanel contextMenu() { return choices; }
	String lastFailure() { return lastFailure; }

	void setPolicy(LocusPointInteractionPolicy2D value) {
		policy = java.util.Objects.requireNonNull(value);
		clearSelection();
		fillPolicy();
		if (coverage != null) { coverage.setText(""); }
		updateStatus(t("Policy applied; select ", "Política aplicada; seleccione ")
				+ (worldPlacement ? t("L again", "L de nuevo")
						: source == null ? "L" : t("Q again", "Q de nuevo")));
	}

	private <T> void showChoices(List<T> values,
			java.util.function.Function<T, String> describe, Consumer<T> accept, String title) {
		long ticket = generation;
		JPanel content = new JPanel(new BorderLayout(4, 3));
		JTextField heading = new JTextField(title, 24);
		heading.setEditable(false);
		heading.setFocusable(false);
		heading.setCaretPosition(0);
		heading.setToolTipText(title);
		content.add(heading, BorderLayout.NORTH);
		JList<String> list = new JList<>(values.stream().map(describe).toArray(String[]::new));
		list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(Math.min(3, values.size()));
		list.getAccessibleContext().setAccessibleName(title);
		JScrollPane addresses = new JScrollPane(list);
		addresses.setPreferredSize(new java.awt.Dimension(
				32 * list.getFontMetrics(list.getFont()).charWidth('0') + 24,
				Math.min(3, values.size()) * list.getFontMetrics(list.getFont()).getHeight()
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
			clearChoices();
			accept.accept(values.get(index)); // exact ephemeral handle, never persisted as an ordinal
		});
		JButton cancel = new JButton(t("Cancel", "Cancelar"));
		cancel.addActionListener(event -> restart());
		JPanel buttons = new JPanel(); buttons.add(create); buttons.add(cancel);
		content.add(buttons, BorderLayout.EAST);
		content.registerKeyboardAction(event -> { if (create.isEnabled()) { create.doClick(); } },
				KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		choices = content;
		choicesHost.add(content, BorderLayout.CENTER);
		choicesHost.revalidate();
		choicesHost.repaint();
	}

	private void buildPanel() {
		panel = new JPanel(new BorderLayout(4, 3));
		panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 4, 3, 4));
		status = new JTextField(18);
		status.setEditable(false);
		status.setFocusable(false);
		status.getAccessibleContext().setAccessibleName(t("Reference point instruction", "Instrucción de punto desde referencia"));
		inputs = new JTextField[7];
		String[] labels = {t("r world", "r world"), t("Equal-distance tolerance", "Tolerancia de empate en distancia"),
				t("Parameter tolerance", "Tolerancia parámetro"), t("Semantic misses", "Misses semánticos"),
				t("Subdivisions / interval passes", "Subdivisiones / pases intervalares"),
				t("Refinement iterations", "Iteraciones refinamiento"), t("Candidates", "Candidatos")};
		JPanel fields = new JPanel(new GridLayout(0, 2, 4, 2));
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = new JTextField(i == 0 ? 6 : 10);
			inputs[i].setName("m08-policy-" + i);
			inputs[i].setToolTipText(i < 3 ? t("Finite positive number; decimal point", "Número finito positivo; punto decimal")
					: t("Positive integer in existing policy units", "Entero positivo en las unidades de la política vigente"));
			JLabel label = new JLabel(labels[i]); label.setLabelFor(inputs[i]);
			inputs[i].getAccessibleContext().setAccessibleName(labels[i]);
			if (i > 0) { fields.add(label); fields.add(inputs[i]); }
			inputs[i].getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent event) { policyEdited(); }
				public void removeUpdate(DocumentEvent event) { policyEdited(); }
				public void changedUpdate(DocumentEvent event) { policyEdited(); }
			});
			inputs[i].addActionListener(event -> applyPolicy());
		}
		fillPolicy();
		JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 4, 0));
		JCheckBox free = new JCheckBox(t("Allow dragging", "Permitir arrastre"), draggable);
		free.setName("m08-allow-dragging");
		free.setToolTipText(t("New points on supported objects; fixed points still follow their source",
				"Puntos nuevos sobre objetos admitidos; los fijos siguen los cambios de su fuente"));
		free.addActionListener(event -> draggable = free.isSelected());
		actions.add(free);
		if (app.getMode() == EuclidianConstants.MODE_POINT
				|| app.getMode() == EuclidianConstants.MODE_POINT_ON_OBJECT) {
			JToggleButton reference = new JToggleButton(t("From reference Q", "Desde referencia Q"), fromReference);
			reference.setName("m08-from-reference");
			reference.addActionListener(event -> {
				fromReference = reference.isSelected();
				clearSelection(); source = null;
				app.getSelectionManager().clearSelectedGeos();
				updateStatus(fromReference ? t("Select object, then Q", "Seleccione objeto y después Q")
						: t("Click to create a point", "Pulse para crear un punto"));
			});
			actions.add(reference);
		}
		if (app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT) {
			JToggleButton origin = new JToggleButton(t("Click position", "Posición de clic"), clickPosition);
			origin.setName("m08-click-position");
			origin.addActionListener(event -> {
				clickPosition = origin.isSelected();
				worldPlacement = false;
				clearSelection();
				source = null;
				app.getSelectionManager().clearSelectedGeos();
				coverage.setText("");
				updateStatus(clickPosition ? t("Click L to choose a position", "Pulse L para elegir una posición")
						: t("Select L", "Seleccione L"));
			});
			actions.add(origin);
		}
		JLabel radius = new JLabel(labels[0]); radius.setLabelFor(inputs[0]);
		actions.add(radius); actions.add(inputs[0]);
		JToggleButton options = new JToggleButton(t("Policy", "Política"));
		actions.add(options);
		JButton restart = new JButton(t("Restart", "Reiniciar"));
		restart.addActionListener(event -> restart());
		actions.add(restart);
		JPanel header = new JPanel(new BorderLayout(4, 0));
		header.add(status, BorderLayout.CENTER); header.add(actions, BorderLayout.EAST);
		panel.add(header, BorderLayout.NORTH);
		JPanel technical = new JPanel(new BorderLayout(4, 2));
		JLabel scope = new JLabel(t("Scope: all supported branches/components", "Alcance: todas las ramas/componentes admitidas"));
		technical.add(scope, BorderLayout.NORTH); technical.add(fields, BorderLayout.CENTER);
		coverage = new JLabel("");
		JButton apply = new JButton(t("Apply", "Aplicar"));
		apply.addActionListener(event -> applyPolicy());
		JPanel footer = new JPanel(new BorderLayout(4, 0));
		footer.add(coverage, BorderLayout.CENTER); footer.add(apply, BorderLayout.EAST);
		technical.add(footer, BorderLayout.SOUTH);
		technical.setVisible(false);
		options.addActionListener(event -> {
			technical.setVisible(options.isSelected());
			panel.revalidate(); panel.repaint();
		});
		panel.add(technical, BorderLayout.SOUTH);
		choicesHost = new JPanel(new BorderLayout());
		panel.add(choicesHost, BorderLayout.CENTER);
		panel.registerKeyboardAction(event -> restart(), KeyStroke.getKeyStroke("ESCAPE"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void policyEdited() {
		if (fillingPolicy) { return; }
		policyDirty = true;
		clearSelection();
		coverage.setText("");
		updateStatus(t("Policy edited; previous choice cancelled. Select ", "Política editada; elección anterior cancelada. Seleccione ")
				+ (worldPlacement ? "L" : source == null ? "L" : "Q"));
	}

	private void fillPolicy() {
		if (inputs == null) { return; }
		fillingPolicy = true;
		try {
			var budget = policy.getWorkBudget();
			String[] values = {"" + policy.getMaximumWorldDistance(), "" + policy.getEqualDistanceTolerance(),
					"" + policy.getParameterTolerance(), "" + budget.getMaximumSemanticEvaluations(),
					"" + budget.getMaximumSubdivisions(), "" + budget.getMaximumRefinementIterations(),
					"" + budget.getMaximumCandidates()};
			for (int i = 0; i < values.length; i++) { inputs[i].setText(values[i]); }
			policyDirty = false;
		} finally { fillingPolicy = false; }
	}

	private boolean applyPolicy() {
		if (!policyDirty) { return true; }
		try {
			setPolicy(new LocusPointInteractionPolicy2D(Double.parseDouble(inputs[0].getText()),
					Double.parseDouble(inputs[1].getText()), Double.parseDouble(inputs[2].getText()),
					new LocusPointInteractionWorkBudget2D(Integer.parseInt(inputs[3].getText()),
							Integer.parseInt(inputs[4].getText()), Integer.parseInt(inputs[5].getText()),
							Integer.parseInt(inputs[6].getText()))));
			return true;
		} catch (IllegalArgumentException invalid) {
			updateStatus(t("Invalid policy; correct the fields before selecting Q", "Política inválida; corrija los campos antes de seleccionar Q"));
			return false;
		}
	}
}

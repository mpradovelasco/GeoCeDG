/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
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
	/** A screen-derived radius describes one canvas offer, not subsequent Q requests. */
	private LocusPointInteractionPolicy2D canvasPreviousPolicy;
	private Object document;
	private Object view;
	private long generation;
	private long contextVersion;
	private GeoCeDGPointRequest pendingRequest;
	private JPanel panel;
	private JPanel choicesHost;
	private JPanel choices;
	private JTextField status;
	private JLabel coverage;
	private JLabel originRule;
	private JToggleButton originClick;
	private JToggleButton originReference;
	private JTextField[] inputs;
	private boolean fillingPolicy;
	private boolean policyDirty;
	private String lastFailure;
	private boolean worldPlacement;
	private boolean clickPosition;
	private boolean draggable = true;
	private boolean fromReference;

	GeoCeDGReferencePointTool(AppD app) { this.app = app; }

	/** Explicit object-context entry; the ordinary Point click remains native. */
	void startFromReference(GeoElement object) {
		if (!(object instanceof GeoLocusV2) && !GeoCeDGNativePointPlacement.supports(object)) {
			throw new IllegalArgumentException("UNSUPPORTED_CAPABILITY");
		}
		activate(true);
		source = object;
		app.getSelectionManager().addSelectedGeo(source);
		updateStatus(t("Select existing Q in Graphics or Algebra",
				"Seleccione Q existente en Gráfica o Álgebra"));
	}

	private String t(String en, String es) {
		return t(app.getLocale(), en, es);
	}

	private static String t(Locale locale, String en, String es) {
		return "es".equals(locale.getLanguage()) ? es : en;
	}

	void activate() {
		activate(false);
	}

	private void activate(boolean referenceRequested) {
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
		if (referenceRequested) { fromReference = true; }
		buildPanel();
		if (app.getGuiManager() instanceof GuiManagerGeoCeDG gui) {
			gui.setReferencePointPanel(panel);
		}
		updateStatus(isReferenceOriginSelected() ? t("Select object", "Seleccione objeto")
				: isAliasMode() ? t("Click object to choose a position", "Pulse el objeto para elegir una posición")
				: t("Click to create a point", "Pulse para crear un punto"));
	}

	/** Tool 145 is an alias of the Point panel whose origin preference starts at reference Q. */
	private boolean isAliasMode() {
		return app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT;
	}

	private boolean isPointMode() {
		return app.getMode() == EuclidianConstants.MODE_POINT
				|| app.getMode() == EuclidianConstants.MODE_POINT_ON_OBJECT;
	}

	/**
	 * The origin shown by the selector. Point and Point on object keep one session preference
	 * (click by default); tool 145 keeps its own (reference Q by default). Each origin runs the
	 * route that its tool already had; the selector only chooses between them.
	 */
	boolean isReferenceOriginSelected() {
		return isAliasMode() ? !clickPosition : fromReference;
	}

	void reset() {
		clearSelection();
		restoreReferencePolicy();
		worldPlacement = false;
		source = null;
		if (app.getGuiManager() instanceof GuiManagerGeoCeDG gui && panel != null) {
			gui.removeReferencePointPanel(panel);
		}
		panel = null;
		status = null;
		coverage = null;
		originRule = null;
		originClick = null;
		originReference = null;
		inputs = null;
		policyDirty = false;
		app.getSelectionManager().clearSelectedGeos();
	}

	private void restoreReferencePolicy() {
		if (canvasPreviousPolicy != null) {
			policy = canvasPreviousPolicy;
			canvasPreviousPolicy = null;
		}
	}

	private void clearSelection() {
		generation++;
		contextVersion++;
		pendingRequest = null;
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
			status.setToolTipText(GeoCeDGTooltips.wrap(message));
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
			var request = bindContext(GeoCeDGPointRequest.capture(app, requestView,
					worldX, worldY, policy, loci));
			choose(loci, geo -> {
				try { queryWorld(request.select(geo)); }
				catch (GeoCeDGPointRequest.StaleContext failure) {
					refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name());
				}
				catch (IllegalArgumentException failure) { refuse(failure.getMessage()); }
			});
			return;
		}
		if (source == null || !loci.isEmpty() && hits.stream().noneMatch(geo -> geo instanceof GeoPoint)) {
			choose(loci, geo -> {
				source = geo;
				app.getSelectionManager().clearSelectedGeos();
				app.getSelectionManager().addSelectedGeo(source);
				updateStatus(t("Select existing Q in Graphics or Algebra",
						"Seleccione Q existente en Gráfica o Álgebra"));
			});
			return;
		}
		choose(hits.stream().filter(geo -> geo instanceof GeoPoint).toList(),
				geo -> query((GeoPoint) geo, anchor, x, y));
	}

	/** Algebra supplies an object selection, never a screen position or parameter. */
	boolean selectFromAlgebra(GeoElement geo, Component anchor) {
		if (!isReferenceRequestActive() || panel == null
				|| clickPosition && app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT
				|| geo == null) {
			return false;
		}
		if (!(geo instanceof GeoPoint) && !(geo instanceof GeoLocusV2)
				&& !GeoCeDGNativePointPlacement.supports(geo)) { return false; }
		// Match canvas selection: a second click/double click belongs to the
		// pending reference gesture, never to inherited Algebra redefinition.
		if (selection != null || nativeSelection != null || choices != null) { return true; }
		click(List.of(geo), anchor, 0, 0);
		return true;
	}

	private <T> void choose(List<T> values, Consumer<T> accept) {
		if (values.isEmpty()) { return; }
		if (values.size() == 1) { accept.accept(values.get(0)); return; }
		showChoices(values, values.stream().map(Object::toString).toList(), null, accept,
				t("Choose construction object", "Elija objeto de construcción"));
	}

	void query(GeoPoint q, Component anchor, int x, int y) {
		try {
			if (q == null) { throw new IllegalArgumentException("INVALID_REFERENCE"); }
			if (app.getActiveEuclidianView() != view) { throw new GeoCeDGPointRequest.StaleContext(); }
			pendingRequest = bindContext(GeoCeDGPointRequest.forSource(app, source,
					q.getInhomX(), q.getInhomY(), policy).withReference(q));
			if (source instanceof GeoLocusV2 locus) {
				selection = new GeoCeDGReferencePlacement(locus, pendingRequest);
				showSelection();
			} else {
				nativeSelection = new GeoCeDGNativePointPlacement(source, pendingRequest);
				showNativeSelection();
			}
		} catch (M08SnapshotPlacement2D.Rejected failure) { refuse(failure.getFailure().name()); }
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); }
		catch (GeoCeDGPointRequest.StaleContext failure) { refuse(failure.getMessage()); }
		catch (IllegalArgumentException failure) { refuse(failure.getMessage()); }
	}

	/** A canvas request uses the same world policy and confirmation as an existing Q. */
	void queryWorld(GeoElement target, double x, double y) {
		if (!applyPolicy()) { return; }
		activate();
		try {
			queryWorld(bindContext(GeoCeDGPointRequest.forSource(app, target, x, y, policy)));
		} catch (M08SnapshotPlacement2D.Rejected failure) { refuse(failure.getFailure().name()); }
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); }
		catch (GeoCeDGPointRequest.StaleContext failure) { refuse(failure.getMessage()); }
		catch (IllegalArgumentException failure) { refuse(failure.getMessage()); }
	}

	/** A canvas handoff retains the original query and all of its charged work. */
	void offerWorld(GeoCeDGPointRequest request, GeoCeDGReferencePlacement offer) {
		request.requireCurrent();
		if (offer.getRequest() != request || panel == null || app.getActiveEuclidianView() != view
				|| document != app.getKernel().getConstruction().getSpatialIdentityRegistry()
				|| selection != null || nativeSelection != null) {
			throw new GeoCeDGPointRequest.StaleContext();
		}
		worldPlacement = true;
		source = request.getSource();
		canvasPreviousPolicy = policy;
		policy = request.getPolicy();
		fillPolicy();
		pendingRequest = bindContext(request);
		selection = offer;
		app.getSelectionManager().addSelectedGeo(source);
		showSelection();
	}

	private void queryWorld(GeoCeDGPointRequest request) {
		request.requireCurrent();
		if (request.getSource() instanceof GeoLocusV2 locus) {
			var offer = new GeoCeDGReferencePlacement(locus, request);
			try { offerWorld(request, offer); }
			catch (RuntimeException failure) { offer.close(); throw failure; }
		} else {
			worldPlacement = true;
			source = request.getSource();
			canvasPreviousPolicy = policy;
			policy = request.getPolicy();
			fillPolicy();
			pendingRequest = bindContext(request);
			nativeSelection = new GeoCeDGNativePointPlacement(source, pendingRequest);
			app.getSelectionManager().addSelectedGeo(source);
			showNativeSelection();
		}
	}

	GeoCeDGPointRequest bindContext(GeoCeDGPointRequest request) {
		long ticket = contextVersion;
		return request.withContext(() -> ticket == contextVersion);
	}

	LocusPointInteractionPolicy2D canvasPolicy(double radius) {
		return new LocusPointInteractionPolicy2D(radius, policy.getEqualDistanceTolerance(),
				policy.getParameterTolerance(), policy.getWorkBudget());
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
		boolean pointMode = app.getMode() == EuclidianConstants.MODE_POINT
				|| app.getMode() == EuclidianConstants.MODE_POINT_ON_OBJECT
				|| app.getMode() == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT;
		if (pointMode && (panel == null || view != app.getActiveEuclidianView()
				|| document != app.getKernel().getConstruction().getSpatialIdentityRegistry())) {
			activate();
		}
		if (pointMode && !applyPolicy()) { return false; }
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
		boolean several = selection.getChoices().size() > 1;
		String title = several
				? t("Several possible positions were found. Select one.",
						"Se han encontrado varias posiciones posibles. Seleccione una.")
				: t("Position found on the curve", "Posición encontrada sobre la curva");
		var presentation = routePresentation(app.getLocale(), selection);
		coverage.setText(presentation.coverage());
		coverage.setToolTipText(GeoCeDGTooltips.wrap(presentation.guarantee()));
		updateStatus(several ? t("Select a branch and confirm", "Seleccione una rama y confirme")
				: t("Confirm the position", "Confirme la posición"));
		var addresses = selection.getChoices().stream()
				.map(GeoCeDGReferencePlacement.Choice::getAddress).toList();
		showChoices(selection.getChoices(),
				GeoCeDGAddressPresentation.labels(app.getLocale(),
						source instanceof GeoLocusV2 locus ? locus : null, addresses),
				addresses.stream().map(GeoCeDGAddressPresentation::technical).toList(),
				this::commit, title);
	}

	/**
	 * Usual coverage text and diagnostic guarantee of an offered route.
	 *
	 * @param coverage the short text shown with the offer
	 * @param guarantee the route and its guarantee, kept as diagnostic detail (tooltip)
	 */
	record RoutePresentation(String coverage, String guarantee) { }

	/** The one presentation of a route, shared by the Point panel and the endpoint assistance. */
	static RoutePresentation routePresentation(Locale locale, GeoCeDGReferencePlacement selection) {
		var route = selection.getRoute();
		boolean historical = route == GeoCeDGReferencePlacement.Route.R1_R6;
		boolean individual = route == GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION;
		boolean conditioned = route == GeoCeDGReferencePlacement.Route.CONDITIONED_PARAMETER_ADDRESS;
		// The route and its guarantee are diagnostic detail: kept, but not the usual label.
		String guarantee = historical ? "R1/R6: " + selection.getHistoricalResult().getStatus()
						+ " / " + selection.getHistoricalResult().getSearchCoverage()
				: individual ? t(locale, "Individual forward address; numerical proximity is uncertified; "
						+ "global coverage not established",
						"Dirección forward individual; proximidad numérica no certificada; "
						+ "cobertura global no establecida")
				: conditioned ? t(locale, "Address by parameter on a conditioned region: existence and "
						+ "continuity of the curve are not certified; the positions found are not "
						+ "stated to be all",
						"Dirección por parámetro sobre una región condicionada: la existencia y la "
						+ "continuidad de la curva no están certificadas; no se afirma que las "
						+ "posiciones halladas sean todas")
				: t(locale, "R2/H1 verified local address; global uniqueness not established",
						"Dirección local verificada R2/H1; unicidad global no establecida");
		String coverage = historical ? t(locale, "Positions found by the search", "Posiciones halladas por la búsqueda")
				: individual ? t(locale, "Position checked by direct evaluation", "Posición comprobada por evaluación directa")
				: conditioned ? t(locale, "Position on a conditioned stretch (not certified)",
						"Posición sobre un tramo condicionado (no certificado)")
				: t(locale, "Position verified locally", "Posición verificada localmente");
		return new RoutePresentation(coverage, guarantee);
	}

	GeoPoint commit(GeoCeDGReferencePlacement.Choice choice) {
		if (app.getActiveEuclidianView() != view) { refuse(M08SnapshotPlacement2D.Failure.STALE_SELECTION.name()); return null; }
		try {
			if (pendingRequest == null) { throw new GeoCeDGPointRequest.StaleContext(); }
			boolean allowDragging = draggable;
			GeoPoint point = selection.commit(choice, pendingRequest::requireCurrent);
			if (point == null) { restart(); return null; }
			point.setFixed(!allowDragging);
			app.storeUndoInfo();
			app.getKernel().notifyRepaint();
			restart();
			return point;
		} catch (M08SnapshotPlacement2D.Rejected failure) {
			refuse(failure.getFailure().name());
			return null;
		}
		catch (GeoCeDGReferencePlacement.Rejected failure) { refuse(failure.getMessage()); return null; }
		catch (GeoCeDGPointRequest.StaleContext failure) { refuse(failure.getMessage()); return null; }
	}

	private void showNativeSelection() {
		updateStatus(t("Confirm native point on object", "Confirme un punto nativo sobre el objeto"));
		showChoices(List.of(nativeSelection), List.of(source.getLabelSimple()), null, ignored -> {
			if (app.getActiveEuclidianView() != view) { refuse("STALE_SELECTION"); return; }
			try {
				nativeSelection.commit(draggable);
				app.storeUndoInfo();
				app.getKernel().notifyRepaint();
				restart();
			} catch (IllegalStateException failure) { refuse(failure.getMessage()); }
		}, t("Native point on object; Q is an initial reference",
				"Punto nativo sobre objeto; Q es referencia inicial"));
	}

	/** @return the user text of a typed rejection; the code itself stays in the tooltip */
	String describeFailure(String code) {
		return switch (code) {
		case "R1_FORWARD_REJECTED" -> t("R1 forward publication rejected", "Publicación forward R1 rechazada");
		case "CERTIFIED_ABSENCE" -> t("Certified absence within the R1 query policy", "Ausencia certificada bajo la política R1");
		case "DEGENERATE_SOURCE" -> t("Degenerate source", "Fuente degenerada");
		case "UNSUPPORTED_CAPABILITY" -> t("Unsupported capability", "Capacidad no admitida");
		case "UNSUPPORTED_CAPTURE" -> t("Source outside the supported reconstructible v3 scope",
				"Fuente fuera del perímetro reconstructible v3 admitido");
		case "UNSUPPORTED_HOST_STATE" -> t("Current document state does not permit exact restoration",
				"El estado actual del documento no permite una restauración exacta");
		case "SOURCE_FIDELITY_RESOURCE_EXHAUSTED" -> t(
				"Native restoration verification work budget reached; document unchanged",
				"Se alcanzó el presupuesto para verificar la restauración nativa; documento sin cambios");
		case "LOCAL_EVIDENCE_INSUFFICIENT" -> t("Insufficient safe local evidence", "Evidencia local segura insuficiente");
		case "GLOBAL_UNIQUENESS_NOT_ESTABLISHED" -> t(
				"No authorized address selection could be established; global metric uniqueness is not claimed",
				"No se pudo establecer una selección autorizada; no se afirma unicidad métrica global");
		case "INVALID_SOURCE" -> t("Invalid source", "Fuente inválida");
		case "INVALID_REFERENCE" -> t("Invalid reference point", "Punto de referencia inválido");
		case "BUDGET_EXHAUSTED" -> t(
				"the search for the point on this curve could not be completed within the work limit."
						+ " Click again closer to the curve, or use Point(curve, parameter)",
				"no se pudo completar la búsqueda del punto en esta curva dentro del límite de trabajo."
						+ " Pulse de nuevo más cerca de la curva o use Point(curva, parámetro)");
		case "STALE_SELECTION" -> t("Selection is stale; make a new request", "Selección caducada; realice una petición nueva");
		case "FOREIGN_CHOICE" -> t("Choice belongs to another query", "Dirección ajena a la consulta");
		case "CONSUMED_SELECTION" -> t("Selection already consumed", "Selección ya consumida");
		// F8 is not implemented: the rejection points to the explicit address alternatives.
		case "COMPONENT_NOT_ESTABLISHED" -> t(
				"the components of this curve are not established here, so the position cannot be"
						+ " confirmed on a component. Use Point(curve, parameter) or the tool ",
				"las componentes de esta curva no están establecidas aquí, así que la posición no"
						+ " puede confirmarse sobre una componente. Use Point(curva, parámetro) o la herramienta ")
				+ app.getLocalization().getMenu("LocusV2.Point.Tool");
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
		// The typed code stays available for diagnosis as the tooltip; it is not the message.
		updateStatus(t("No point created: ", "No se creó ningún punto: ")
				+ describeFailure(reason.split(" / ")[0]));
		if (status != null) {
			status.setToolTipText(GeoCeDGTooltips.lines(status.getText(), "[" + reason + "]"));
		}
	}

	GeoCeDGReferencePlacement pendingSelection() { return selection; }
	JPanel contextMenu() { return choices; }
	String lastFailure() { return lastFailure; }

	void setPolicy(LocusPointInteractionPolicy2D value) {
		policy = java.util.Objects.requireNonNull(value);
		canvasPreviousPolicy = null;
		clearSelection();
		fillPolicy();
		if (coverage != null) { coverage.setText(""); }
		updateStatus(t("Policy applied; select ", "Política aplicada; seleccione ")
				+ (worldPlacement ? t("L again", "L de nuevo")
						: source == null ? "L" : t("Q again", "Q de nuevo")));
	}

	/**
	 * @param labels the usual short text of each value
	 * @param details the technical text of each value, shown as its tooltip, or {@code null}
	 */
	private <T> void showChoices(List<T> values, List<String> labels, List<String> details,
			Consumer<T> accept, String title) {
		long ticket = generation;
		JPanel content = new JPanel(new BorderLayout(4, 3));
		JTextField heading = new JTextField(title, 24);
		heading.setEditable(false);
		heading.setFocusable(false);
		heading.setCaretPosition(0);
		heading.setToolTipText(GeoCeDGTooltips.wrap(title));
		content.add(heading, BorderLayout.NORTH);
		JList<String> list = new JList<>(labels.toArray(String[]::new)) {
			private static final long serialVersionUID = 1L;
			@Override public String getToolTipText(java.awt.event.MouseEvent event) {
				int row = locationToIndex(event.getPoint());
				return details != null && row >= 0 && row < details.size()
						&& getCellBounds(row, row).contains(event.getPoint()) ? details.get(row) : null;
			}
		};
		if (details != null) {
			javax.swing.ToolTipManager.sharedInstance().registerComponent(list);
		}
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
		free.setToolTipText(GeoCeDGTooltips.lines(
				t("Applies to new points on supported objects.",
						"Se aplica a los puntos nuevos sobre objetos admitidos."),
				t("A fixed point still follows its source.",
						"Un punto fijo sigue los cambios de su fuente.")));
		free.addActionListener(event -> draggable = free.isSelected());
		actions.add(free);
		JLabel radius = new JLabel(labels[0]); radius.setLabelFor(inputs[0]);
		actions.add(radius); actions.add(inputs[0]);
		JToggleButton options = new JToggleButton(t("Policy", "Política"));
		actions.add(options);
		JButton restart = new JButton(t("Restart", "Reiniciar"));
		restart.addActionListener(event -> restart());
		actions.add(restart);
		JPanel header = new JPanel(new BorderLayout(4, 0));
		header.add(status, BorderLayout.CENTER); header.add(actions, BorderLayout.EAST);
		if (isPointMode() || isAliasMode()) { header.add(buildOriginSelector(), BorderLayout.SOUTH); }
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

	/**
	 * One origin selector for Point, Point on object and tool 145. The two segments are
	 * complementary: pressing the selected one selects the other origin. Below them, the panel
	 * states where the search radius comes from and when an offer needs Confirm (U2-3).
	 */
	private JPanel buildOriginSelector() {
		JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 4, 0));
		JLabel label = new JLabel(t("Origin:", "Origen:"));
		JToggleButton click = new JToggleButton(t("Click", "Clic"));
		click.setName("m08-click-position");
		JToggleButton reference = new JToggleButton(t("Reference Q", "Referencia Q"));
		reference.setName("m08-from-reference");
		click.getAccessibleContext().setAccessibleName(t("Origin: click", "Origen: clic"));
		reference.getAccessibleContext().setAccessibleName(t("Origin: reference Q", "Origen: referencia Q"));
		originRule = new JLabel();
		originRule.setName("m08-origin-rule");
		click.addActionListener(event -> chooseOrigin(!click.isSelected()));
		reference.addActionListener(event -> chooseOrigin(reference.isSelected()));
		originClick = click;
		originReference = reference;
		showOrigin();
		row.add(label); row.add(click); row.add(reference); row.add(originRule);
		return row;
	}

	private void chooseOrigin(boolean reference) {
		if (isAliasMode()) { clickPosition = !reference; } else { fromReference = reference; }
		boolean restored = canvasPreviousPolicy != null;
		restoreReferencePolicy();
		worldPlacement = false;
		clearSelection();
		source = null;
		// Show the policy that applies again; edits that were not applied yet are kept.
		if (restored) { fillPolicy(); }
		app.getSelectionManager().clearSelectedGeos();
		if (coverage != null) { coverage.setText(""); }
		showOrigin();
		updateStatus(reference ? t("Select object", "Seleccione objeto")
				: isAliasMode() ? t("Click object to choose a position", "Pulse el objeto para elegir una posición")
				: t("Click to create a point", "Pulse para crear un punto"));
	}

	private void showOrigin() {
		boolean reference = isReferenceOriginSelected();
		originClick.setSelected(!reference);
		originReference.setSelected(reference);
		String rule;
		String[] detail;
		// The tooltip separates purpose, search radius and confirmation (U2-R1-1).
		if (reference) {
			rule = t("Radius r world around Q · every offer needs Confirm",
					"Radio r world alrededor de Q · toda oferta requiere Confirmar");
			detail = new String[] {
					t("Select the object, then an existing point Q.",
							"Seleccione el objeto y después un punto Q existente."),
					t("Radius: r world around Q on Locus V2 and Spline V2;"
							+ " native objects use the projection of Q.",
							"Radio: r world alrededor de Q sobre Locus V2 y Spline V2;"
							+ " los objetos nativos usan la proyección de Q."),
					t("Confirm: every offer waits for Confirm.",
							"Confirmar: toda oferta espera a Confirmar."),
					t("The new point does not depend on Q.", "El punto nuevo no depende de Q.")};
		} else if (isAliasMode()) {
			rule = t("Radius r world around the click · every offer needs Confirm",
					"Radio r world alrededor del clic · toda oferta requiere Confirmar");
			detail = new String[] {
					t("Click the object at the position you want.",
							"Pulse el objeto en la posición deseada."),
					t("Radius: r world, in construction units, around the click.",
							"Radio: r world, en unidades de la construcción, alrededor del clic."),
					t("Confirm: every offer waits for Confirm.",
							"Confirmar: toda oferta espera a Confirmar.")};
		} else {
			rule = t("Radius from the view · positions found by the search are created at once;"
					+ " other offers need Confirm",
					"Radio de la vista · lo hallado por la búsqueda se crea al instante;"
					+ " el resto requiere Confirmar");
			detail = new String[] {
					t("Click a Locus V2 or Spline V2 at the position you want.",
							"Pulse un Locus V2 o un Spline V2 en la posición deseada."),
					t("Radius: from the view (pointer size and zoom).",
							"Radio: de la vista (tamaño del puntero y zoom)."),
					t("Confirm: a position found by the search is created at once"
							+ " (choose one if there are several); other offers wait for Confirm.",
							"Confirmar: lo hallado por la búsqueda se crea al instante"
							+ " (elija una posición si hay varias); el resto espera a Confirmar."),
					t("On native objects and empty space the ordinary tool applies.",
							"Sobre objetos nativos y espacio vacío actúa la herramienta ordinaria.")};
		}
		originRule.setText(rule);
		originRule.setToolTipText(GeoCeDGTooltips.lines(detail));
	}

	/** @return the radius and confirmation rule of the selected origin, or {@code null} */
	String originRule() { return originRule == null || panel == null ? null : originRule.getText(); }

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

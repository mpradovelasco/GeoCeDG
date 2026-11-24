/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.awt.Component;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.geocedg.common.kernel.algos.AlgoProvenLocusEndpoint2D;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.AppD;

/**
 * One explicit authoring action after a metric endpoint has no semantic address. The new point
 * keeps the locus position the user selects in the list; nothing is selected for the user, and
 * the endpoint given to the length is never changed or replaced.
 */
final class GeoCeDGMetricEndpointAssistance {

	/** Search radius of the placement policy, in construction units. */
	private static final double RADIUS = .1;

	/** Shows one option pane until it has a value; tests drive the real pane without a window. */
	interface Modal {
		/** @return the value of the pane once it is closed */
		Object show(JOptionPane pane, Component parent, String title);
	}

	/** The product modal window. */
	static final Modal WINDOW = (pane, parent, title) -> {
		JDialog dialog = pane.createDialog(parent, title);
		try {
			dialog.setVisible(true);
		} finally {
			dialog.dispose();
		}
		return pane.getValue();
	};

	private static volatile Modal modal = WINDOW;

	private final AppD app;

	GeoCeDGMetricEndpointAssistance(AppD app) {
		this.app = app;
	}

	/**
	 * Test seam: the dialogs of the assistance are shown through the given modal.
	 * @return the modal that was in use
	 */
	static Modal useModal(Modal replacement) {
		Modal previous = modal;
		modal = replacement == null ? WINDOW : replacement;
		return previous;
	}

	boolean isApplicable(GeoLocusV2 source, GeoPoint reference) {
		return reference.isDefined()
				&& new SemanticMetricEndpointResolver2D().resolve(source, reference).getStatus()
						== SemanticMetricEndpointResolver2D.Status.NO_ADDRESS;
	}

	GeoPoint offer(GeoLocusV2 source, GeoPoint reference) {
		var construction = source.getConstruction();
		var proposal = AlgoProvenLocusEndpoint2D.propose(source, reference);
		String title = text("Construct a metric endpoint", "Construir un extremo métrico");
		String place = text("Choose a position on the locus", "Elegir una posición en el lugar");
		String prove = text("Create equivalent point", "Crear punto equivalente");
		String cancel = text("Cancel", "Cancelar");
		String[] routes = proposal == null ? new String[] {place, cancel}
				: new String[] {prove, place, cancel};
		String a = name(reference);
		String h = name(source);
		String description = text(a + " has no semantic address on " + h
						+ ": it cannot be an endpoint of the length.",
				a + " no tiene dirección semántica sobre " + h
						+ ": no puede ser extremo de la longitud.")
				+ "\n" + text("You can create a new point on " + h + " at a position next to " + a
						+ "; " + a + " does not change.",
				"Puede crear un punto nuevo sobre " + h + " en una posición junto a " + a
						+ "; " + a + " no cambia.");
		if (proposal != null) {
			description += "\n" + text("Equivalent construction with driver witness ",
					"Construcción equivalente con el testigo del conductor ")
					+ proposal.witness().getLabelSimple();
		}
		Object chosen = modal.show(new JOptionPane(description, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, routes, cancel), app.getMainComponent(), title);
		if ((!place.equals(chosen) && !prove.equals(chosen))
				|| app.getKernel().getConstruction() != construction) {
			return null;
		}
		try {
			if (proposal != null && prove.equals(chosen)) {
				return LocusV2PublicOperations.createProvenEndpoint(construction, null,
						source, reference, proposal.witness(), proposal.vector(),
						new GeoText(construction, proposal.recipe()));
			}
			return place(source, reference, title);
		} catch (IllegalArgumentException | IllegalStateException unavailable) {
			inform(text("No point created: ", "No se creó ningún punto: ")
					+ unavailable.getMessage(), title, JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

	/**
	 * Same placement policy, address presentation, route guarantee and drag choice as the Point
	 * panel. The positions are offered without a selection; the point is created only for the
	 * position the user selects and confirms. The host stores the single Undo step.
	 */
	private GeoPoint place(GeoLocusV2 source, GeoPoint reference, String title) {
		String a = name(reference);
		String h = name(source);
		String radius = String.format(app.getLocale(), "%.1f", RADIUS);
		try (var selection = new GeoCeDGReferencePlacement(source, reference,
				LocusPointInteractionPolicy2D.initial(RADIUS))) {
			var choices = selection.getChoices();
			if (choices.isEmpty()) {
				inform(text("No position of " + h + " was found next to " + a + " (search radius "
								+ radius + "). No point was created.",
						"No se encontró ninguna posición de " + h + " junto a " + a
								+ " (radio de búsqueda " + radius + "). No se creó ningún punto.")
						+ " [" + selection.getFailure() + "]", title, JOptionPane.INFORMATION_MESSAGE);
				return null;
			}
			var addresses = choices.stream().map(GeoCeDGReferencePlacement.Choice::getAddress).toList();
			var labels = GeoCeDGAddressPresentation.labels(app.getLocale(), source, addresses);
			var details = addresses.stream().map(GeoCeDGAddressPresentation::technical).toList();
			JList<String> list = new JList<>(labels.toArray(String[]::new)) {
				private static final long serialVersionUID = 1L;
				@Override public String getToolTipText(java.awt.event.MouseEvent event) {
					int row = locationToIndex(event.getPoint());
					return row >= 0 && getCellBounds(row, row).contains(event.getPoint())
							? details.get(row) : null;
				}
			};
			ToolTipManager.sharedInstance().registerComponent(list);
			list.setName("m08-endpoint-positions");
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setVisibleRowCount(Math.min(4, labels.size()));
			String heading = text("Positions of " + h + " next to " + a + " (search radius " + radius
							+ "):",
					"Posiciones de " + h + " junto a " + a + " (radio de búsqueda " + radius + "):");
			list.getAccessibleContext().setAccessibleName(heading);
			var presentation = GeoCeDGReferencePointTool.routePresentation(app.getLocale(), selection);
			JLabel guarantee = new JLabel(presentation.coverage());
			guarantee.setName("m08-endpoint-guarantee");
			guarantee.setToolTipText(GeoCeDGTooltips.wrap(presentation.guarantee()));
			JCheckBox free = new JCheckBox(text("Allow dragging", "Permitir arrastre"), true);
			free.setName("m08-allow-dragging");
			free.setToolTipText(GeoCeDGTooltips.lines(
					text("Applies to the new point.", "Se aplica al punto nuevo."),
					text("A fixed point still follows its curve.",
							"Un punto fijo sigue los cambios de su curva.")));
			JButton create = new JButton(text("Create point", "Crear punto"));
			create.setName("m08-endpoint-create");
			// Nothing is selected for the user, even when there is only one position.
			create.setEnabled(false);
			list.addListSelectionListener(event -> create.setEnabled(list.getSelectedIndex() >= 0));
			JButton cancel = new JButton(text("Cancel", "Cancelar"));
			cancel.setName("m08-endpoint-cancel");
			for (JButton button : List.of(create, cancel)) {
				button.addActionListener(event -> {
					var pane = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, button);
					if (pane != null) { pane.setValue(button); }
				});
			}
			Object[] message = {heading, new JScrollPane(list),
					text("Select a position, then Create point.",
							"Seleccione una posición y pulse Crear punto."),
					guarantee, free,
					text("The new point does not replace " + a + " in the length:"
									+ " measure again with the new point.",
							"El punto nuevo no sustituye a " + a + " en la longitud:"
									+ " vuelva a medir con el punto nuevo.")};
			Object answer = modal.show(new JOptionPane(message, JOptionPane.QUESTION_MESSAGE,
					JOptionPane.DEFAULT_OPTION, null, new Object[] {create, cancel}, null),
					app.getMainComponent(), title);
			int index = list.getSelectedIndex();
			if (answer != create || index < 0
					|| app.getKernel().getConstruction() != source.getConstruction()) {
				return null;
			}
			GeoPoint point = selection.commit(choices.get(index));
			if (point != null) { point.setFixed(!free.isSelected()); }
			return point;
		}
	}

	private void inform(String message, String title, int type) {
		modal.show(new JOptionPane(message, type), app.getMainComponent(), title);
	}

	private String name(GeoElement geo) {
		String label = geo.getLabelSimple();
		return label == null || label.isEmpty() ? text("the point", "el punto") : label;
	}

	private String text(String english, String spanish) {
		return "es".equals(app.getLocale().getLanguage()) ? spanish : english;
	}
}

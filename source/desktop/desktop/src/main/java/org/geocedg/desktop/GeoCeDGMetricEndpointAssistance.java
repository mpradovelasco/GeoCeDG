/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;

import org.geocedg.common.kernel.algos.AlgoProvenLocusEndpoint2D;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.AppD;

/** One explicit authoring action after a metric endpoint has no semantic address. */
final class GeoCeDGMetricEndpointAssistance {

	private final AppD app;

	GeoCeDGMetricEndpointAssistance(AppD app) {
		this.app = app;
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
		String description = text("The selected point has no admitted metric address.",
				"El punto seleccionado carece de una dirección métrica admisible.");
		if (proposal != null) {
			description += "\n" + text("Equivalent construction with driver witness ",
					"Construcción equivalente con el testigo del conductor ")
					+ proposal.witness().getLabelSimple();
		}
		int route = JOptionPane.showOptionDialog(app.getMainComponent(), description, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, routes, cancel);
		if (route < 0 || route == routes.length - 1
				|| app.getKernel().getConstruction() != construction) {
			return null;
		}
		try {
			if (proposal != null && route == 0) {
				return LocusV2PublicOperations.createProvenEndpoint(construction, null,
						source, reference, proposal.witness(), proposal.vector(),
						new GeoText(construction, proposal.recipe()));
			}
			return place(source, reference, title);
		} catch (IllegalArgumentException | IllegalStateException unavailable) {
			JOptionPane.showMessageDialog(app.getMainComponent(), unavailable.getMessage(),
					title, JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

	/**
	 * Same placement policy, address presentation, route guarantee and drag choice as the Point
	 * panel. The host stores the single Undo step of the created point.
	 */
	private GeoPoint place(GeoLocusV2 source, GeoPoint reference, String title) {
		try (var selection = new GeoCeDGReferencePlacement(source, reference,
				LocusPointInteractionPolicy2D.initial(.1))) {
			var choices = selection.getChoices();
			if (choices.isEmpty()) {
				JOptionPane.showMessageDialog(app.getMainComponent(),
						text("No position is available under the placement policy.",
								"La política de colocación no ofrece ninguna posición.")
								+ " " + selection.getFailure(), title,
						JOptionPane.INFORMATION_MESSAGE);
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
			// A single offer is preselected; several offers have no implicit choice.
			if (labels.size() == 1) { list.setSelectedIndex(0); }
			String heading = labels.size() > 1
					? text("Several possible positions were found. Select one.",
							"Se han encontrado varias posiciones posibles. Seleccione una.")
					: text("Position found on the curve", "Posición encontrada sobre la curva");
			list.getAccessibleContext().setAccessibleName(heading);
			var presentation = GeoCeDGReferencePointTool.routePresentation(app.getLocale(), selection);
			JLabel guarantee = new JLabel(presentation.coverage());
			guarantee.setName("m08-endpoint-guarantee");
			guarantee.setToolTipText(presentation.guarantee());
			JCheckBox free = new JCheckBox(text("Allow dragging", "Permitir arrastre"), true);
			free.setName("m08-allow-dragging");
			free.setToolTipText(text("New points on supported objects; fixed points still follow their source",
					"Puntos nuevos sobre objetos admitidos; los fijos siguen los cambios de su fuente"));
			Object[] message = {
					text("The new point retains the selected locus position, independent of the reference.",
							"El nuevo punto conserva la posición elegida en el lugar y es independiente de la referencia."),
					heading, new JScrollPane(list), guarantee, free};
			int answer = JOptionPane.showConfirmDialog(app.getMainComponent(), message, title,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			int index = list.getSelectedIndex();
			if (answer != JOptionPane.OK_OPTION || index < 0
					|| app.getKernel().getConstruction() != source.getConstruction()) {
				return null;
			}
			GeoPoint point = selection.commit(choices.get(index));
			if (point != null) { point.setFixed(!free.isSelected()); }
			return point;
		}
	}

	private String text(String english, String spanish) {
		return "es".equals(app.getLocale().getLanguage()) ? spanish : english;
	}
}

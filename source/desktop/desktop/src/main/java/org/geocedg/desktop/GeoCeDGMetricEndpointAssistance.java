/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import javax.swing.JOptionPane;

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
			String[] options = new String[choices.size()];
			for (int i = 0; i < choices.size(); i++) {
				var address = choices.get(i).getAddress();
				options[i] = (i + 1) + ". " + address.getBranchKey() + "   t = "
						+ address.getCanonicalParameter();
			}
			Object selected = JOptionPane.showInputDialog(app.getMainComponent(),
					text("The new point retains the selected locus position, independent of the reference.",
							"El nuevo punto conserva la posición elegida en el lugar y es independiente de la referencia."),
					title, JOptionPane.QUESTION_MESSAGE, null, options, null);
			for (int i = 0; i < options.length; i++) {
				if (options[i].equals(selected) && app.getKernel().getConstruction() == source.getConstruction()) {
					return selection.commit(choices.get(i));
				}
			}
			return null;
		}
	}

	private String text(String english, String spanish) {
		return "es".equals(app.getLocale().getLanguage()) ? spanish : english;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** Read-only constructive definition, independent of global Algebra presentation. */
public final class GeoCeDGDefinitionInspector {

	private GeoCeDGDefinitionInspector() {
	}

	/**
	 * @param geo inspected object
	 * @return its existing editable-form definition
	 */
	public static String definition(GeoElement geo) {
		return geo == null ? "" : geo.getDefinitionForInputBar();
	}

	/**
	 * @param geo inspected object
	 * @return a noneditable semantic definition, not a new edit policy
	 */
	public static boolean isReadOnly(GeoElement geo) {
		return (geo instanceof GeoLocusV2 || geo instanceof GeoLocusMetricResult
				|| geo instanceof GeoLocusIntersectionResult) && !geo.isAlgebraViewEditable();
	}

	/**
	 * @param app product app
	 * @return explanation shared by Properties and inspection
	 */
	public static String readOnlyExplanation(AppD app) {
		return GeoCeDGProfile.getText("Definition.ReadOnly", app.getLocale().getLanguage());
	}

	static String semanticDetails(AppD app, GeoElement geo) {
		StringBuilder details = new StringBuilder();
		if (geo instanceof GeoLocusV2) {
			appendLocusDetails(app, (GeoLocusV2) geo, details);
		}
		if (geo != null && geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
			AlgoSemanticLocusPoint2D parent =
					(AlgoSemanticLocusPoint2D) geo.getParentAlgorithm();
			LocusSemanticAddress2D address = parent.getCurrentSemanticAddress();
			boolean current = address != null;
			if (address == null) {
				address = parent.getSemanticAddress();
			}
			appendAddressDetails(app, address, current, details);
		}
		return details.toString();
	}

	static String inspectionText(AppD app, GeoElement geo) {
		StringBuilder content = new StringBuilder(definition(geo));
		appendSection(content, semanticDetails(app, geo));
		if (isReadOnly(geo)) {
			appendSection(content, readOnlyExplanation(app));
		}
		return content.toString();
	}

	/** @param app product application @param geo inspected object */
	public static void show(AppD app, GeoElement geo) {
		if (geo == null) {
			return;
		}
		String title = app.getLocalization().getMenu("Definition");
		JTextArea text = new JTextArea(inspectionText(app, geo), 12, 56);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setFont(app.getPlainFont());
		text.getAccessibleContext().setAccessibleName(title);
		text.setCaretPosition(0);
		JOptionPane.showMessageDialog(app.getMainComponent(), new JScrollPane(text),
				title, JOptionPane.INFORMATION_MESSAGE);
	}

	private static void appendLocusDetails(AppD app, GeoLocusV2 locus,
			StringBuilder details) {
		LocusDefinition2D semanticDefinition = locus.getSemanticDefinition();
		if (semanticDefinition == null) {
			return;
		}
		appendSemanticHeader(app, details);
		appendField(app, details, "Definition.Provider",
				semanticDefinition.getProvider().getProviderId(), "");
		for (LocusBranch2D branch : semanticDefinition.getBranches()) {
			appendField(app, details, "Definition.Branch", branch.getBranchKey(), "");
			for (LocusInterval2D component : branch.getValidDomainComponents()) {
				String lineage = LocusComponentLineage2D.create(branch.getBranchKey(),
						component);
				appendField(app, details, "Definition.Component",
						lineage + " " + component, "  ");
			}
		}
	}

	private static void appendAddressDetails(AppD app, LocusSemanticAddress2D address,
			boolean current, StringBuilder details) {
		if (address == null) {
			return;
		}
		appendSemanticHeader(app, details);
		details.append(text(app, "Definition.SemanticAddress")).append('\n');
		appendField(app, details, "Definition.AddressStatus",
				text(app, current ? "Definition.AddressCurrent"
						: "Definition.AddressRetainedDormant"), "  ");
		appendField(app, details, "Definition.Source",
				address.getSourceLocusId().toExternalForm(), "  ");
		appendField(app, details, "Definition.Provider", address.getProviderVersion(), "  ");
		appendField(app, details, "Definition.Branch", address.getBranchKey(), "  ");
		appendField(app, details, "Definition.Component",
				address.getComponentLineageKey(), "  ");
		appendField(app, details, "Definition.Parameter",
				Double.toString(address.getCanonicalParameter()), "  ");
		appendField(app, details, "Definition.PeriodicLift",
				Long.toString(address.getPeriodicLift()), "  ");
		appendField(app, details, "Definition.Seam", address.getSeamSide().name(), "  ");
	}

	private static void appendSemanticHeader(AppD app, StringBuilder details) {
		if (details.length() == 0) {
			details.append(text(app, "Definition.SemanticStructure")).append('\n');
		}
	}

	private static void appendField(AppD app, StringBuilder target, String key,
			String value, String indentation) {
		target.append(indentation).append(text(app, key)).append(": ")
				.append(value).append('\n');
	}

	private static String text(AppD app, String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	private static void appendSection(StringBuilder target, String section) {
		if (section == null || section.isBlank()) {
			return;
		}
		if (target.length() > 0) {
			target.append("\n\n");
		}
		target.append(section.stripTrailing());
	}
}

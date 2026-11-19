/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.List;
import java.util.Locale;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;

/**
 * User presentation of a semantic address: branch and section ordinals with the parameter.
 * The ordinals are display positions in the current definition only. They are never read
 * back, compared, persisted or used to select: the choice object keeps the exact address,
 * and the complete technical identifier stays available for inspection.
 */
final class GeoCeDGAddressPresentation {

	/** A middle dot between the parts of a label. */
	private static final String SEPARATOR = " " + (char) 0xB7 + " ";

	private GeoCeDGAddressPresentation() {
	}

	/**
	 * @param source the locus of the address, or {@code null} when it is not at hand
	 * @return a short label of the form "Branch 1, Section 2, u = -1.5808" with dot separators
	 */
	static String label(Locale locale, GeoLocusV2 source, LocusSemanticAddress2D address) {
		return label(locale, source, address, 4);
	}

	/**
	 * Labels of the members of one choice. The parameter is printed with as many decimals as
	 * the members need to read differently; members that still coincide are told apart only
	 * by their position in the list, which is why the list keeps the exact addresses.
	 */
	static List<String> labels(Locale locale, GeoLocusV2 source,
			List<LocusSemanticAddress2D> addresses) {
		List<String> labels = List.of();
		for (int decimals = 4; decimals <= 16; decimals += 4) {
			int digits = decimals;
			labels = addresses.stream().map(address -> label(locale, source, address, digits))
					.toList();
			if (labels.stream().distinct().count() == labels.size()) {
				break;
			}
		}
		return labels;
	}

	private static String label(Locale locale, GeoLocusV2 source,
			LocusSemanticAddress2D address, int decimals) {
		boolean spanish = "es".equals(locale.getLanguage());
		LocusDefinition2D definition = source == null ? null : source.getSemanticDefinition();
		LocusBranch2D branch = definition == null ? null
				: definition.getBranch(address.getBranchKey());
		StringBuilder label = new StringBuilder();
		if (branch != null) {
			int position = 0;
			for (LocusBranch2D candidate : definition.getBranches()) {
				position++;
				if (candidate == branch) {
					label.append(spanish ? "Rama " : "Branch ").append(position).append(SEPARATOR);
				}
			}
			var structure = branch.getExistenceStructure();
			var resolution = LocusComponentSelectorResolver2D.resolve(
					address.getComponentLineageKey(), structure, List.of(branch.getLineage()),
					address.getCanonicalParameter());
			int section = resolution.getStatus().isResolved() ? structure
					.getContinuousValidComponents().indexOf(resolution.getComponent()) + 1 : 0;
			if (section > 0) {
				label.append(spanish ? "Tramo " : "Section ").append(section).append(SEPARATOR);
			}
		}
		return label.append("u = ").append(String.format(locale, "%." + decimals + "f",
				address.getCanonicalParameter())).toString();
	}

	/** @return the complete technical identifier, for a tooltip or a diagnostic view */
	static String technical(LocusSemanticAddress2D address) {
		return address.getBranchKey() + " / " + address.getComponentLineageKey()
				+ " / u=" + address.getCanonicalParameter();
	}
}

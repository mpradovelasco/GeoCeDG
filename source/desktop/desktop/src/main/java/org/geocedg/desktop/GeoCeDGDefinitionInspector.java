/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusParameterAddressState2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedHypothesis2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedState2D;
import org.geocedg.common.kernel.locus.constructive.RelationIntegrityRecord;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geogebra.common.euclidian.DrawableND;
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
		return (geo instanceof GeoLocusMetricResult
				|| geo instanceof GeoLocusIntersectionResult) && !geo.isAlgebraViewEditable();
	}

	/**
	 * @param geo candidate selected through a Classic edit surface
	 * @return whether the approved A3 preflight governs its existing redefine path
	 */
	public static boolean isSemanticRedefineEnabled(GeoElement geo) {
		return geo instanceof GeoLocusV2;
	}

	/**
	 * @param app product app
	 * @return explanation shared by Properties and inspection
	 */
	public static String readOnlyExplanation(AppD app) {
		return GeoCeDGProfile.getText("Definition.ReadOnly", app.getLocale().getLanguage());
	}

	/**
	 * Grade of an object under ADR 0042, in ordinary words, for the properties of the
	 * object (contract {@code post-r1-conditioned-use.md}, section 9, point 3).
	 *
	 * @param app product app
	 * @param geo inspected object
	 * @return the localized statement that the object is conditioned, or that it is a result
	 *         on a conditioned region; {@code null} for every other object
	 */
	public static String conditionedGrade(AppD app, GeoElement geo) {
		if (geo instanceof GeoLocusV2 && LocusConditionedState2D.of((GeoLocusV2) geo) != null) {
			return text(app, "Definition.Conditioned");
		}
		if (geo != null && geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
			LocusSemanticAddress2D address = ((AlgoSemanticLocusPoint2D) geo
					.getParentAlgorithm()).getCurrentSemanticAddress();
			if (address != null && address.isParameterAddress()) {
				return text(app, "Definition.ConditionedResult");
			}
		}
		return null;
	}

	/**
	 * @param app product app
	 * @param geo inspected object
	 * @return explanation of a read-only definition, with the conditioned grade of the
	 *         object when it has one
	 */
	public static String readOnlyExplanation(AppD app, GeoElement geo) {
		String grade = conditionedGrade(app, geo);
		return grade == null ? readOnlyExplanation(app)
				: grade + ". " + readOnlyExplanation(app);
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
			if (current && address.isParameterAddress()) {
				// A result on a conditioned region states its grade the same way.
				details.append("  ").append(text(app, "Definition.ConditionedResult"))
						.append('\n');
				LocusConditionedCell2D cell = parent.getContainingConditionedCell();
				if (cell != null) {
					appendField(app, details, "Definition.ConditionedCell", text(app,
							"Definition.ConditionedKind." + cell.getKind().name()), "  ");
				}
				appendField(app, details, "Definition.Integrity",
						String.valueOf(parent.getParameterIncidenceIntegrity()), "  ");
			}
		}
		if (geo != null
				&& geo.getParentAlgorithm() instanceof AlgoLocusDriverCorrespondencePoint2D) {
			appendStructuralPointDetails(app,
					((AlgoLocusDriverCorrespondencePoint2D) geo.getParentAlgorithm())
							.getResolution(), details);
		}
		return details.toString();
	}

	/**
	 * PRE-G9B-CU3 ({@code locus-v2-constructive-authority.md} sections 6 and 7.3): the
	 * integrity of a structural point, the worst of the relations its definition relies on,
	 * with its premises that are not established, the evidence of {@code P in L} and of
	 * {@code P in r}, the cause of an undefined point and, apart from the integrity, the
	 * declared error of its position.
	 */
	private static void appendStructuralPointDetails(AppD app,
			StructuralPointResolution resolution, StringBuilder details) {
		if (resolution == null) {
			return;
		}
		appendSemanticHeader(app, details);
		details.append(text(app, "Definition.StructuralPoint")).append('\n');
		appendField(app, details, "Definition.Integrity", resolution.getIntegrity().name(),
				"  ");
		if (!resolution.isDefined()) {
			appendField(app, details, "Definition.Cause", resolution.getCause() + ": "
					+ resolution.getMessage(), "  ");
		}
		appendRelation(app, details, "Definition.Membership", resolution.getMembership());
		appendRelation(app, details, "Definition.Incidence", resolution.getIncidence());
		if (resolution.getTransfer() != null) {
			appendField(app, details, "Definition.Transfer",
					resolution.getTransfer().toString(), "  ");
		}
		double[] error = resolution.getDeclaredError();
		if (error != null) {
			appendField(app, details, "Definition.DeclaredError",
					"(" + error[0] + ", " + error[1] + ")", "  ");
		}
	}

	private static void appendRelation(AppD app, StringBuilder details, String key,
			RelationIntegrityRecord relation) {
		if (relation == null) {
			return;
		}
		appendField(app, details, key, relation.getIntegrity() + " " + relation.getEvidence(),
				"  ");
		for (String premise : relation.getUnestablishedPremises()) {
			appendField(app, details, "Definition.UnestablishedPremise", premise, "    ");
		}
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
			LocusExistenceStructure2D structure = branch.getExistenceStructure();
			for (LocusExistenceStructure2D.ContinuousComponent component
					: structure.getContinuousValidComponents()) {
				// PRE-G9B-CU2: the identity key of the component (E-3), the one its semantic
				// addresses and intersection roots cite; an arc across the seam, or an open
				// arc of full length, is shown with its ends and its closure.
				String lineage = LocusComponentLineage2D.identityKey(structure, component);
				String shape = component.followsCanonicalOrder() ? ""
						: " arc[" + component.getStart() + ", " + component.getEnd()
								+ ", length " + component.getLength()
								+ (component.crossesSeam() ? ", crosses seam" : "")
								+ (component.isClosedPeriodic() ? ", closed" : ", open") + "]";
				appendField(app, details, "Definition.Component",
						lineage + " " + component.getInterval() + shape, "  ");
			}
			// Point components are declared, never drawn (D-CU2-19).
			for (LocusPointComponent2D point : structure.getPointComponents()) {
				appendField(app, details, "Definition.Component",
						LocusComponentLineage2D.of(structure, point) + " point["
								+ point.getParameterLower() + ", "
								+ point.getParameterUpper() + "] source "
								+ point.getSource().getSignature() + " position "
								+ point.getPositionState(), "  ");
			}
			details.append("  existence completeness: ")
					.append(branch.getExistenceStructure().getCompleteness())
					.append('\n');
		}
		appendConditionedState(app, locus, details);
		appendDrawingFidelity(app, locus, details);
	}

	/**
	 * ADR 0042 (contract {@code post-r1-conditioned-use.md}, section 9, point 3): the grade
	 * of a conditioned locus in ordinary words, with its regions and bands by kind, the
	 * policy version and the hypotheses that every conditioned stretch leaves open.
	 */
	private static void appendConditionedState(AppD app, GeoLocusV2 locus,
			StringBuilder details) {
		LocusConditionedState2D state = LocusConditionedState2D.of(locus);
		if (state == null) {
			return;
		}
		details.append(text(app, "Definition.Conditioned")).append('\n');
		for (LocusConditionedState2D.Branch branch : state.getBranches()) {
			appendField(app, details, "Definition.Branch", branch.getBranchKey(), "  ");
			appendField(app, details, "Definition.ConditionedPolicy",
					branch.getPolicyVersion(), "    ");
			appendField(app, details, "Definition.ConditionedRegions",
					Integer.toString(branch.getRegions().size()), "    ");
			for (LocusInterval2D region : branch.getRegions()) {
				appendField(app, details, "Definition.ConditionedRegion", region.toString(),
						"      ");
			}
			int bands = 0;
			for (int count : branch.getBands().values()) {
				bands += count;
			}
			appendField(app, details, "Definition.ConditionedBands", Integer.toString(bands),
					"    ");
			for (Map.Entry<LocusConditionedCellKind2D, Integer> band
					: branch.getBands().entrySet()) {
				details.append("      ").append(band.getValue()).append(" x ")
						.append(text(app, "Definition.ConditionedKind."
								+ band.getKey().name())).append('\n');
			}
		}
		details.append("  ").append(text(app, "Definition.ConditionedHypotheses"))
				.append('\n');
		for (LocusConditionedHypothesis2D hypothesis : state.getHypotheses()) {
			details.append("    ").append(text(app, "Definition.ConditionedHypothesis."
					+ hypothesis.name())).append('\n');
		}
		details.append("  ").append(text(app, "Definition.ConditionedNotComplete"))
				.append('\n');
	}

	/**
	 * PRE-G9B-CU2 (consumption design section 5.6): the declared fidelity of the drawing in
	 * the active view, per component, with its typed marks.
	 */
	private static void appendDrawingFidelity(AppD app, GeoLocusV2 locus,
			StringBuilder details) {
		DrawableND drawable = app.getActiveEuclidianView().getDrawableFor(locus);
		LocusRenderData2D drawing = drawable instanceof DrawLocusV2
				? ((DrawLocusV2) drawable).getRenderCache().getLastBuild() : null;
		if (drawing == null) {
			return;
		}
		details.append("  drawing fidelity: ").append(drawing.getFidelity()).append(" (")
				.append(drawing.getPolicy().getVersion()).append(")\n");
		for (LocusRenderData2D.ComponentFidelity component
				: drawing.getComponentFidelities()) {
			details.append("    ").append(component).append('\n');
		}
		for (LocusRenderData2D.Mark mark : drawing.getMarks()) {
			details.append("    ").append(mark).append('\n');
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
				address.isParameterAddress() ? LocusParameterAddressState2D.KIND
						: address.getComponentLineageKey(), "  ");
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

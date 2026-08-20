/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionOverlapEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocalPairIsolationEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.EstablishedMetricErrorAmount2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricErrorAmount2D;
import org.geocedg.common.kernel.locus.metric.MetricValue2D;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.AppD;

/** Keyboard-accessible, text-first dialogs for experimental Locus V2 actions. */
final class GeoCeDGLocusV2Dialogs {

	private final AppD app;

	GeoCeDGLocusV2Dialogs(AppD app) {
		this.app = app;
	}

	GeoPoint createSemanticPoint(GeoLocusV2 source) {
		if (!RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction())) {
			showMessage("LocusV2.FeatureDisabled",
					"LocusV2.Point.Tool", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		String branchLabel = menu("LocusV2.BranchKey");
		String parameterLabel = menu("LocusV2.Parameter");
		JTextField branch = new JTextField(24);
		JTextField parameter = new JTextField(24);
		branch.getAccessibleContext().setAccessibleName(branchLabel);
		parameter.getAccessibleContext().setAccessibleName(parameterLabel);
		JLabel branchPrompt = new JLabel(branchLabel);
		JLabel parameterPrompt = new JLabel(parameterLabel);
		branchPrompt.setLabelFor(branch);
		parameterPrompt.setLabelFor(parameter);
		JPanel fields = new JPanel(new GridLayout(2, 2, 8, 6));
		fields.add(branchPrompt);
		fields.add(branch);
		fields.add(parameterPrompt);
		fields.add(parameter);

		while (JOptionPane.showConfirmDialog(app.getMainComponent(), fields,
				menu("LocusV2.Point.Tool"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			String branchKey = branch.getText().trim();
			GeoNumberValue parameterInput = app.getKernel()
					.getAlgebraProcessor().evaluateToNumeric(
							parameter.getText().trim(), true);
			if (branchKey.isEmpty() || parameterInput == null
					|| !parameterInput.toGeoElement().isDefined()
					|| !Double.isFinite(parameterInput.getDouble())) {
				showMessage("LocusV2.InvalidPosition",
						"LocusV2.Point.Tool", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			GeoText branchInput = new GeoText(
					app.getKernel().getConstruction(), branchKey);
			branchInput.setAuxiliaryObject(true);
			try {
				return LocusV2PublicOperations.createSemanticPoint(
						app.getKernel().getConstruction(), null, source,
						branchInput, parameterInput);
			} catch (IllegalArgumentException exception) {
				showMessage("LocusV2.InvalidPosition",
						"LocusV2.Point.Tool", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	/** Inspects selected/current rich results and optionally creates one token point. */
	GeoPoint inspectRichResult() {
		if (!RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction())) {
			showMessage("LocusV2.FeatureDisabled",
					"LocusV2.Results.Inspect", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		GeoElement rich = chooseRichResult();
		if (rich instanceof GeoLocusMetricResult) {
			showMetric((GeoLocusMetricResult) rich);
			return null;
		}
		if (rich instanceof GeoLocusIntersectionResult) {
			return showIntersection((GeoLocusIntersectionResult) rich);
		}
		return null;
	}

	/** Opens the rich metric inspector for a just-created query. */
	void inspectMetric(GeoLocusMetricResult rich) {
		showMetric(rich);
	}

	private GeoElement chooseRichResult() {
		Set<GeoElement> candidates = new LinkedHashSet<>();
		for (GeoElement selected : app.getSelectionManager().getSelectedGeos()) {
			if (isRichResult(selected)) {
				candidates.add(selected);
			}
		}
		if (candidates.isEmpty()) {
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				if (isRichResult(geo)) {
					candidates.add(geo);
				}
			}
		}
		if (candidates.isEmpty()) {
			showMessage("LocusV2.Results.None",
					"LocusV2.Results.Inspect", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		List<ResultChoice> choices = new ArrayList<>();
		for (GeoElement candidate : candidates) {
			choices.add(new ResultChoice(candidate));
		}
		if (choices.size() == 1) {
			return choices.get(0).geo;
		}
		Object chosen = JOptionPane.showInputDialog(app.getMainComponent(),
				menu("LocusV2.Results.Select"),
				menu("LocusV2.Results.Inspect"),
				JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0));
		return chosen instanceof ResultChoice ? ((ResultChoice) chosen).geo : null;
	}

	private void showMetric(GeoLocusMetricResult rich) {
		StringBuilder text = new StringBuilder();
		if (rich.isDefined()) {
			LocusMetricResult2D value = rich.getMetricResult();
			appendField(text, "LocusV2.Results.Field.Value",
					formatMetricValue(value.getMetricValue()));
			appendField(text, "LocusV2.Results.Field.Status",
					localizeEnum(value.getComputationStatus()));
			appendField(text, "LocusV2.Results.Field.Coverage",
					localizeEnum(value.getCoverage()));
			appendField(text, "LocusV2.Results.Field.ScalarAdmissible",
					localizeBoolean(value.isScalarAdmissible()));
			appendField(text, "LocusV2.Results.Field.Rectifiability",
					localizeEnum(value.getRectifiability()));
			appendField(text, "LocusV2.Results.Field.Traversal",
					value.getTraversalOutcome().map(this::localizeEnum)
							.orElseGet(this::notApplicable));
			appendField(text, "LocusV2.Results.Field.ConstructionFidelity",
					localizeEnum(value.getConstructionFidelity()));
			appendField(text, "LocusV2.Results.Field.EvaluatorMethod",
					localizeEnum(value.getEvaluatorMethod()));
			appendField(text, "LocusV2.Results.Field.Method",
					localizeEnum(value.getMetricMethod()));
			appendField(text, "LocusV2.Results.Field.RepresentationRole",
					localizeEnum(value.getRepresentationRole()));
			appendField(text, "LocusV2.Results.Field.Unit",
					localizeEnum(value.getUnit()));
			appendField(text, "LocusV2.Results.Field.Guarantee",
					value.getErrorEvidence().getNumericGuarantee()
							.map(this::localizeEnum)
							.orElseGet(this::notApplicable));
			appendField(text, "LocusV2.Results.Field.ErrorScope",
					localizeEnum(value.getErrorEvidence().getScope()));
			appendField(text, "LocusV2.Results.Field.AbsoluteError",
					formatErrorAmount(
							value.getErrorEvidence().getAbsoluteEvidence()));
			appendField(text, "LocusV2.Results.Field.RelativeError",
					formatErrorAmount(
							value.getErrorEvidence().getRelativeEvidence()));
			appendMetricDiagnostics(text, value.getDiagnostics());
		} else {
			text.append(menu("LocusV2.Results.Undefined"));
		}
		showReadOnlyText(text.toString(), "LocusV2.Results.Inspect");
	}

	private GeoPoint showIntersection(GeoLocusIntersectionResult rich) {
		StringBuilder summary = new StringBuilder();
		List<TokenChoice> admissible = new ArrayList<>();
		if (rich.isDefined()) {
			LocusIntersectionResult2D value = rich.getIntersectionResult();
			appendField(summary, "LocusV2.Results.Field.Status",
					localizeEnum(value.getComputationStatus()));
			appendField(summary, "LocusV2.Results.Field.Completeness",
					localizeEnum(value.getCompletenessEvidence()
							.getCompleteness()));
			appendField(summary, "LocusV2.Results.Field.CompletenessMethod",
					localizeEnum(value.getCompletenessEvidence().getMethod()));
			appendField(summary, "LocusV2.Results.Field.Geometry",
					localizeEnum(value.getGeometryKind()));
			appendField(summary, "LocusV2.Results.Field.Currentness",
					localizeEnum(value.getCurrentness()));
			appendField(summary, "LocusV2.Results.Field.Support",
					localizeEnum(value.getSupportLevel()));
			appendField(summary, "LocusV2.Results.Field.Guarantee",
					localizeEnum(value.getNumericGuarantee()));
			appendField(summary, "LocusV2.Results.Field.FiniteSolutions",
					value.getFiniteSolutions().size());
			appendField(summary, "LocusV2.Results.Field.Overlaps",
					value.getOverlapEvidence().size());
			appendIntersectionDiagnostics(summary,
					value.getCompletenessEvidence().getDiagnostics());
			int solutionNumber = 0;
			for (LocusIntersectionSolution2D solution
					: value.getFiniteSolutions()) {
				solutionNumber++;
				String token = solution.getIdentity().getRootToken();
				boolean pointAdmissible = rich.isPointAdmissible(token);
				appendField(summary, "LocusV2.Results.Field.Solution",
						solutionNumber);
				appendField(summary, "LocusV2.Results.Field.PointAdmissible",
						localizeBoolean(pointAdmissible));
				appendField(summary, "LocusV2.Results.Field.Contact",
						localizeEnum(solution.getClassification()
								.getContactClass()));
				appendField(summary, "LocusV2.Results.Field.Multiplicity",
						solution.getClassification().getEstablishedMultiplicity()
								.isPresent()
								? solution.getClassification()
										.getEstablishedMultiplicity().getAsInt()
								: localizeEnum(solution.getClassification()
										.getMultiplicityStatus()));
				appendField(summary, "LocusV2.Results.Field.DomainLocation",
						localizeEnum(solution.getClassification()
								.getDomainLocation()));
				appendField(summary, "LocusV2.Results.Field.Regularity",
						localizeEnum(solution.getClassification()
								.getSourceRegularity()));
				appendField(summary, "LocusV2.Results.Field.Continuation",
						localizeEnum(solution.getIdentity().getIdentityStatus()));
				appendField(summary, "LocusV2.Results.Field.LineageEvent",
						localizeEnum(solution.getLineage().getEventKind()));
				appendField(summary,
						"LocusV2.Results.Field.ContinuationEstablished",
						localizeBoolean(solution.getLineage()
								.isContinuationEstablished()));
				appendField(summary, "LocusV2.Results.Field.Isolation",
						localizeEnum(solution.getRevisionEvidence()
								.getLocalIsolationStatus()));
				appendField(summary, "LocusV2.Results.Field.SolverMethod",
						localizeEnum(solution.getRevisionEvidence()
								.getSolverMethod()));
				appendField(summary, "LocusV2.Results.Field.Guarantee",
						localizeEnum(solution.getRevisionEvidence()
								.getNumericGuarantee()));
				appendField(summary, "LocusV2.Results.Field.Token",
						pointAdmissible ? token
								: menu("LocusV2.Results.NoAdmissibleToken"));
				if (solution.getPairEvidence().isPresent()) {
					LocalPairIsolationEvidence2D pair = solution
							.getPairEvidence().get().getLocalIsolation();
					appendField(summary, "LocusV2.Results.Field.PairIsolation",
							localizeEnum(pair.getStatus()));
					appendField(summary, "LocusV2.Results.Field.PairMethod",
							localizeEnum(pair.getMethod()));
					appendField(summary, "LocusV2.Results.Field.PairCoverage",
							localizeEnum(pair.getCoverage()));
					appendField(summary, "LocusV2.Results.Field.PairUniqueness",
							localizeEnum(pair.getUniqueness()));
				}
				appendIntersectionDiagnostics(summary,
						solution.getDiagnostics());
				if (pointAdmissible) {
					admissible.add(new TokenChoice(token,
							localizeEnum(solution.getClassification()
									.getContactClass())));
				}
			}
			int overlapNumber = 0;
			for (IntersectionOverlapEvidence2D overlap
					: value.getOverlapEvidence()) {
				overlapNumber++;
				appendField(summary, "LocusV2.Results.Field.Overlap",
						overlapNumber);
				appendField(summary, "LocusV2.Results.Field.OverlapStatus",
						localizeEnum(overlap.getStatus()));
				appendField(summary, "LocusV2.Results.Field.OverlapRelation",
						localizeEnum(overlap.getRelationKind()));
				appendField(summary, "LocusV2.Results.Field.Guarantee",
						localizeEnum(overlap.getNumericGuarantee()));
			}
			appendWorkSummary(summary, value.getWork());
			appendIntersectionDiagnostics(summary, value.getDiagnostics());
		} else {
			summary.append(menu("LocusV2.Results.Undefined"));
		}
		if (admissible.isEmpty()) {
			summary.append("\n\n")
					.append(menu("LocusV2.Results.NoAdmissibleToken"));
			showReadOnlyText(summary.toString(), "LocusV2.Results.Inspect");
			return null;
		}

		JTextArea inspector = createTextArea(summary.toString());
		JComboBox<TokenChoice> tokens = new JComboBox<>(
				admissible.toArray(new TokenChoice[0]));
		String tokenLabel = menu("LocusV2.Results.ExactToken");
		tokens.getAccessibleContext().setAccessibleName(tokenLabel);
		JLabel prompt = new JLabel(tokenLabel);
		prompt.setLabelFor(tokens);
		JPanel chooser = new JPanel(new BorderLayout(0, 8));
		chooser.add(new JScrollPane(inspector), BorderLayout.CENTER);
		JPanel tokenRow = new JPanel(new BorderLayout(8, 0));
		tokenRow.add(prompt, BorderLayout.WEST);
		tokenRow.add(tokens, BorderLayout.CENTER);
		chooser.add(tokenRow, BorderLayout.SOUTH);
		int decision = JOptionPane.showConfirmDialog(app.getMainComponent(),
				chooser, menu("LocusV2.Results.Inspect"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (decision != JOptionPane.OK_OPTION) {
			return null;
		}
		TokenChoice chosen = (TokenChoice) tokens.getSelectedItem();
		if (chosen == null) {
			return null;
		}
		Construction construction = app.getKernel().getConstruction();
		GeoText tokenInput = new GeoText(construction, chosen.token);
		tokenInput.setAuxiliaryObject(true);
		try {
			return LocusV2PublicOperations.selectIntersectionPoint(construction,
					null, rich, tokenInput);
		} catch (IllegalArgumentException exception) {
			showMessage("LocusV2.InvalidToken", "LocusV2.Results.Inspect",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private void showReadOnlyText(String text, String titleKey) {
		JTextArea area = createTextArea(text);
		JScrollPane scroll = new JScrollPane(area);
		JOptionPane.showMessageDialog(app.getMainComponent(), scroll,
				menu(titleKey), JOptionPane.INFORMATION_MESSAGE);
	}

	private JTextArea createTextArea(String text) {
		JTextArea area = new JTextArea(text, 18, 72);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setCaretPosition(0);
		area.getAccessibleContext().setAccessibleName(
				menu("LocusV2.Results.Inspect"));
		return area;
	}

	private void showMessage(String messageKey, String titleKey,
			int messageType) {
		JOptionPane.showMessageDialog(app.getMainComponent(), menu(messageKey),
				menu(titleKey), messageType);
	}

	private String menu(String key) {
		return app.getLocalization().getMenu(key);
	}

	private void appendField(StringBuilder text, String key, Object value) {
		if (text.length() > 0) {
			text.append('\n');
		}
		text.append(menu(key)).append(": ")
				.append(value == null ? notApplicable() : value);
	}

	private void appendIntersectionDiagnostics(StringBuilder text,
			List<IntersectionDiagnostic2D> diagnostics) {
		if (!diagnostics.isEmpty()) {
			text.append('\n').append(menu("LocusV2.Results.Field.Diagnostics"));
			diagnostics.forEach(diagnostic -> text.append("\n- ")
					.append(localizeEnum(diagnostic.getCode())));
		}
	}

	private void appendMetricDiagnostics(StringBuilder text,
			List<MetricDiagnostic2D> diagnostics) {
		if (!diagnostics.isEmpty()) {
			text.append('\n').append(menu("LocusV2.Results.Field.Diagnostics"));
			diagnostics.forEach(diagnostic -> text.append("\n- ")
					.append(localizeEnum(diagnostic.getCode())));
		}
	}

	private void appendWorkSummary(StringBuilder text,
			LocusIntersectionInstrumentationSnapshot2D work) {
		appendField(text, "LocusV2.Results.Field.SemanticEvaluations",
				work.getSemanticEvaluations());
		appendField(text, "LocusV2.Results.Field.CandidateIntervals",
				work.getCandidateIntervals());
		appendField(text, "LocusV2.Results.Field.IsolationSubdivisions",
				work.getIsolationSubdivisions());
		appendField(text, "LocusV2.Results.Field.MaximumIsolationDepth",
				work.getMaximumIsolationDepth());
		appendField(text, "LocusV2.Results.Field.RefinementIterations",
				work.getRefinementIterations());
		appendField(text, "LocusV2.Results.Field.ContinuationComparisons",
				work.getContinuationComparisons()
						+ work.getPairContinuationComparisons());
		appendField(text, "LocusV2.Results.Field.UnresolvedCandidates",
				work.getUnresolvedCandidates());
	}

	private Object formatMetricValue(MetricValue2D value) {
		return value.getFiniteValue().isPresent()
				? value.getFiniteValue().getAsDouble()
				: localizeEnum(value.getKind());
	}

	private Object formatErrorAmount(MetricErrorAmount2D amount) {
		return amount instanceof EstablishedMetricErrorAmount2D
				? ((EstablishedMetricErrorAmount2D) amount)
						.getNonNegativeFiniteAmount()
				: localizeEnum(amount.getKind());
	}

	private String localizeBoolean(boolean value) {
		return menu(value ? "LocusV2.Results.Value.True"
				: "LocusV2.Results.Value.False");
	}

	private String localizeEnum(Enum<?> value) {
		String key = "LocusV2.Results.Value." + value.name();
		String localized = menu(key);
		return key.equals(localized)
				? menu("LocusV2.Results.Value.UNAVAILABLE") : localized;
	}

	private String notApplicable() {
		return menu("LocusV2.Results.Value.NOT_APPLICABLE");
	}

	private static boolean isRichResult(GeoElement geo) {
		return geo instanceof GeoLocusMetricResult
				|| geo instanceof GeoLocusIntersectionResult;
	}

	private final class ResultChoice {
		private final GeoElement geo;

		private ResultChoice(GeoElement geo) {
			this.geo = geo;
		}

		@Override
		public String toString() {
			String label = geo.isLabelSet() ? geo.getLabelSimple()
					: menu("LocusV2.Results.Unlabelled");
			return label + " \u2014 " + geo.translatedTypeString();
		}
	}

	private static final class TokenChoice {
		private final String token;
		private final String classification;

		private TokenChoice(String token, String classification) {
			this.token = token;
			this.classification = classification;
		}

		@Override
		public String toString() {
			return classification + " \u2014 " + token;
		}
	}
}

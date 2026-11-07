/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.desktop.GeoCeDGInspectionResult.Phase;
import org.geocedg.desktop.GeoCeDGInspectionResult.Status;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/** Plain-text, localized projection of one verified worker report. */
record GeoCeDGExpertReviewModel(String summary, String details) {
	private static final Pattern UNSAFE_CONTROLS = Pattern.compile("[\\p{Cc}\\p{Cf}]");
	private static final Pattern LAYER = Pattern.compile("^layer=(\\d+),");
	private static final Pattern VIEWS = Pattern.compile(",view1=(true|false),view2=(true|false),");

	static void validateReport(JSONObject report, boolean available, String source, String receiver)
			throws JSONException, IOException {
		Map<Phase, JSONObject> stages = new EnumMap<>(Phase.class);
		var observations = report.getJSONArray("stages");
		for (int index = 0; index < observations.length(); index++) {
			var stage = observations.getJSONObject(index);
			Phase phase = phase(stage, "phase");
			if (stages.put(phase, stage) != null) {
				throw new IOException("Duplicate worker stage " + phase);
			}
			String expected = switch (phase) {
			case SOURCE -> source;
			case RECEIVER -> receiver;
			default -> null;
			};
			if (expected != null && !expected.equals(stage.getString("sha256"))) {
				throw new IOException("Worker input hash mismatch at " + phase);
			}
		}
		if (available) {
			for (Phase phase : Phase.values()) {
				if (!stages.containsKey(phase) || !stages.get(phase).getBoolean("nativeEvaluated")) {
					throw new IOException("Offer lacks a native observation at " + phase);
				}
			}
		}
	}

	static GeoCeDGExpertReviewModel project(AppGeoCeDG app, GeoCeDGExpertWorker.Result result)
			throws IOException {
		try {
			return project(app, result.report(), result.available(), result.reason());
		} catch (JSONException failure) {
			throw new IOException("Invalid differential evidence", failure);
		}
	}

	static GeoCeDGExpertReviewModel project(AppGeoCeDG app, JSONObject report,
			boolean available, String reason) throws JSONException {
		StringBuilder additions = new StringBuilder();
		StringBuilder changes = new StringBuilder();
		StringBuilder omissions = new StringBuilder();
		StringBuilder uncertainties = new StringBuilder();
		StringBuilder assessments = new StringBuilder();
		StringBuilder resources = new StringBuilder();
		StringBuilder diagnostics = new StringBuilder();
		int added = 0;
		int omitted = 0;
		int uncertain = 0;
		boolean trialObserved = false;
		boolean technicalFailure = false;
		Set<String> modified = new HashSet<>();
		Map<Phase, Map<String, String>> labels = new EnumMap<>(Phase.class);
		var stages = report.getJSONArray("stages");
		for (int i = 0; i < stages.length(); i++) {
			JSONObject stage = stages.getJSONObject(i);
			Phase phase = phase(stage, "phase");
			trialObserved |= phase == Phase.TRIAL;
			var objects = stage.getJSONArray("objects");
			Map<String, String> stageLabels = new HashMap<>();
			for (int j = 0; j < objects.length(); j++) {
				JSONObject object = objects.getJSONObject(j);
				String ref = object.getString("ref");
				String label = object.isNull("label") ? ref : object.getString("label");
				stageLabels.put(ref, plain(label) + " (" + plain(object.getString("geoClass")) + ")");
			}
			labels.put(phase, stageLabels);
			var entries = stage.getJSONArray("entries");
			for (int j = 0; j < entries.length(); j++) {
				JSONObject entry = entries.getJSONObject(j);
				line(resources, phaseName(app, phase) + " · " + plain(entry.getString("name"))
						+ " · " + plain(entry.getString("disposition")));
			}
		}
		var comparisons = report.getJSONArray("comparisons");
		Set<String> comparisonPairs = new HashSet<>();
		for (int i = 0; i < comparisons.length(); i++) {
			JSONObject comparison = comparisons.getJSONObject(i);
			Phase before = phase(comparison, "before");
			Phase after = phase(comparison, "after");
			String pair = before.name() + ">" + after.name();
			if (!comparisonPairs.add(pair)) {
				throw new JSONException("Duplicate worker comparison " + pair);
			}
			String scope = phaseName(app, before) + " → " + phaseName(app, after);
			Status comparisonStatus = status(comparison);
			technicalFailure |= failsIntegrity(comparisonStatus);
			line(assessments, scope + " · " + statusName(app, comparisonStatus));
			var dimensions = comparison.getJSONArray("assessments");
			for (int j = 0; j < dimensions.length(); j++) {
				JSONObject dimension = dimensions.getJSONObject(j);
				Status status = status(dimension);
				technicalFailure |= failsIntegrity(status);
				line(assessments, "  " + plain(dimension.getString("dimension")) + " · "
						+ statusName(app, status) + " · " + plain(dimension.getString("coverage")));
				if (status == Status.NOT_ESTABLISHED) {
					line(uncertainties, scope + " · " + plain(dimension.getString("dimension"))
							+ " · " + plain(dimension.getString("coverage")));
					uncertain++;
				}
			}
			var afterObjects = comparison.getJSONArray("additions");
			if (before == Phase.RECEIVER && after == Phase.TRIAL) {
				for (int j = 0; j < afterObjects.length(); j++) {
					JSONObject addition = afterObjects.getJSONObject(j);
					line(additions, objectName(labels, Phase.SOURCE, addition, "sourceRef")
							+ " → " + objectName(labels, after, addition, "afterRef")
							+ " · " + plain(addition.getString("basis")));
					added++;
				}
			}
			var unmatched = comparison.getJSONArray("unmatchedAfter");
			for (int j = 0; j < unmatched.length(); j++) {
				line(uncertainties, scope + " · " + labels.getOrDefault(after, Map.of())
						.getOrDefault(unmatched.getString(j), plain(unmatched.getString(j)))
						+ " · " + statusName(app, Status.NOT_ESTABLISHED));
				uncertain++;
			}
			var objects = comparison.getJSONArray("objects");
			for (int j = 0; j < objects.length(); j++) {
				JSONObject object = objects.getJSONObject(j);
				String names = objectName(labels, before, object, "beforeRef") + " → "
						+ objectName(labels, after, object, "afterRef");
				if ("OMISSION_PROVEN".equals(object.getString("outcome"))) {
					line(omissions, scope + " · " + names);
					omitted++;
				}
				var fields = object.getJSONArray("observations");
				for (int k = 0; k < fields.length(); k++) {
					JSONObject field = fields.getJSONObject(k);
					String property = field.getString("property");
					Status state = status(field);
					technicalFailure |= failsIntegrity(state);
					String prefix = scope + " · " + names + " · " + propertyName(app, property);
					switch (state) {
					case VERIFIED_CHANGED -> {
						String oldValue = value(field, "before");
						String newValue = value(field, "after");
						if ("PRESENTATION_FIELDS".equals(property)) {
							describePresentation(app, changes, scope + " · " + names,
									oldValue, newValue);
						}
						line(changes, prefix + " · " + oldValue + " → " + newValue);
						modified.add(after.name() + "/" + object.optString("afterRef"));
					}
					case NOT_ESTABLISHED -> {
						line(uncertainties, prefix + " · " + value(field, "before") + " → "
								+ value(field, "after"));
						uncertain++;
					}
					case INVALID, TECHNICAL_FAILURE ->
						line(diagnostics, prefix + " · " + statusName(app, state));
					default -> {
						// Established unchanged fields and observations never run are
						// summarized in the coverage rows.
					}
					}
				}
			}
		}
		if (available && (!comparisonPairs.contains("SOURCE>TRIAL")
				|| !comparisonPairs.contains("RECEIVER>TRIAL")
				|| !comparisonPairs.contains("TRIAL>REOPENED") || added == 0 || omitted != 0)) {
			throw new JSONException("An expert offer requires all differential stages and no omissions");
		}
		var findings = report.getJSONArray("diagnostics");
		for (int i = 0; i < findings.length(); i++) {
			JSONObject finding = findings.getJSONObject(i);
			Status state = status(finding);
			technicalFailure |= failsIntegrity(state);
			line(diagnostics, phaseName(app, phase(finding, "phase")) + " · "
					+ statusName(app, state) + " · " + plain(finding.getString("code"))
					+ " · " + value(finding, "detail"));
		}
		if (available && technicalFailure) {
			throw new JSONException("Offer has blocking diagnostics");
		}
		JSONObject admission = report.getJSONObject("admission");
		line(assessments, app.layerText("Insert.Expert.Certified") + " · "
				+ value(admission, "state") + " · "
				+ value(admission, "reason") + " · " + value(admission, "detail"));
		String decision = app.layerText(available ? "Insert.Expert.Available" : "Insert.Expert.Unavailable");
		String scope = app.layerText("Insert.Expert.Scope");
		String counts = trialObserved ? app.layerText("Insert.Expert.Counts",
				Integer.toString(added), Integer.toString(modified.size()), Integer.toString(omitted),
				Integer.toString(uncertain)) : app.layerText("Insert.Expert.TrialMissing");
		String explanation = reasonName(app, reason);
		String summary = decision + "\n\n" + explanation + "\n\n" + counts + "\n\n" + scope;
		String details = decision + "\n" + explanation + "\n\n"
				+ section(app, "Added", additions) + section(app, "Changed", changes)
				+ section(app, "Omitted", omissions) + section(app, "Uncertain", uncertainties)
				+ section(app, "Assessments", assessments) + section(app, "Resources", resources)
				+ section(app, "Diagnostics", diagnostics);
		return new GeoCeDGExpertReviewModel(summary, details);
	}

	private static boolean failsIntegrity(Status status) {
		return status == Status.INVALID || status == Status.TECHNICAL_FAILURE;
	}

	private static String reasonName(AppGeoCeDG app, String reason) {
		return switch (reason) {
		case "NATIVE_INPUT_UNAVAILABLE", "RECEIVER_CONTEXT", "NATIVE_EXPERT_COPY",
				"NATIVE_EXPERT_ROUND_TRIP", "PASSIVE_NATIVE_REPLAY" ->
			app.layerText("Insert.Expert.Reason." + reason);
		default -> plain(reason);
		};
	}

	private static void describePresentation(AppGeoCeDG app, StringBuilder changes,
			String subject, String before, String after) {
		Matcher oldLayer = LAYER.matcher(before);
		Matcher newLayer = LAYER.matcher(after);
		if (oldLayer.find() && newLayer.find() && !oldLayer.group(1).equals(newLayer.group(1))) {
			line(changes, subject + " · " + app.layerText("Insert.Expert.LayerChange",
					oldLayer.group(1), newLayer.group(1)));
		}
		Matcher oldViews = VIEWS.matcher(before);
		Matcher newViews = VIEWS.matcher(after);
		if (oldViews.find() && newViews.find()) {
			for (int view = 1; view <= 2; view++) {
				if (!oldViews.group(view).equals(newViews.group(view))) {
					line(changes, subject + " · " + app.layerText("Insert.Expert.ViewChange",
							Integer.toString(view), app.layerText("Insert.Expert.ViewMembership."
									+ oldViews.group(view)), app.layerText(
											"Insert.Expert.ViewMembership." + newViews.group(view))));
				}
			}
		}
	}

	private static String section(AppGeoCeDG app, String key, StringBuilder body) {
		return app.layerText("Insert.Expert." + key) + "\n" + (body.isEmpty() ? "—\n" : body)
				+ "\n";
	}

	private static String objectName(Map<Phase, Map<String, String>> labels, Phase phase,
			JSONObject object, String key) {
		String ref = object.optString(key, null);
		return object.isNull(key) ? "—" : labels.getOrDefault(phase, Map.of())
				.getOrDefault(ref, plain(ref));
	}

	private static String value(JSONObject object, String key) {
		return object.isNull(key) ? "—" : plain(object.optString(key));
	}

	static String plain(String text) {
		return text == null ? "" : UNSAFE_CONTROLS.matcher(text).replaceAll(match ->
				Matcher.quoteReplacement(String.format("\\u%04X", match.group().codePointAt(0))));
	}

	private static void line(StringBuilder text, String line) {
		text.append(line).append('\n');
	}

	private static Phase phase(JSONObject object, String key) throws JSONException {
		try {
			return Phase.valueOf(object.getString(key));
		} catch (IllegalArgumentException failure) {
			throw new JSONException("Unknown worker phase", failure);
		}
	}

	private static Status status(JSONObject object) throws JSONException {
		try {
			return Status.valueOf(object.getString("status"));
		} catch (IllegalArgumentException failure) {
			throw new JSONException("Unknown worker observation", failure);
		}
	}

	private static String phaseName(AppGeoCeDG app, Phase phase) {
		return app.layerText("Insert.Expert.Phase." + phase.name());
	}

	private static String statusName(AppGeoCeDG app, Status status) {
		return app.layerText("Insert.Expert.Status." + status.name());
	}

	private static String propertyName(AppGeoCeDG app, String property) {
		return switch (property) {
		case "INPUTS", "OUTPUTS", "DEFINITION_TEXT", "CURRENT_VALUE", "DEFINEDNESS",
				"PRESENTATION_FIELDS", "PARAMETER_DOMAIN", "LABEL" ->
			app.layerText("Insert.Expert.Property." + property);
		default -> plain(property);
		};
	}
}

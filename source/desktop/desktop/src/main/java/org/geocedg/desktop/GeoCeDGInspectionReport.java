/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.List;
import java.util.function.Function;

import org.geocedg.desktop.GeoCeDGInspectionResult.Comparison;
import org.geocedg.desktop.GeoCeDGInspectionResult.DocumentContext;
import org.geocedg.desktop.GeoCeDGInspectionResult.ObservedObject;
import org.geocedg.desktop.GeoCeDGInspectionResult.Stage;
import org.geocedg.desktop.GeoCeDGInspectionResult.Status;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/** Versioned worker report projections. */
final class GeoCeDGInspectionReport {
	private GeoCeDGInspectionReport() {
	}

	static String json(GeoCeDGInspectionResult report) throws JSONException {
		return json(report, null);
	}

	static String json(GeoCeDGInspectionResult report, GeoCeDGExpertInspection.ExpertTrial expert)
			throws JSONException {
		var admission = report.admission();
		var result = object("schemaVersion", 2, "policy", "POST_CU4_I2_PASSIVE_1",
				"analysisVersion", "POST_CU4_I2_DIFFERENTIAL_2",
				"inputScope", "NATIVE_ARCHIVE_SNAPSHOTS", "workerProfile", "ISOLATED_PORTABLE_DESKTOP",
				"publication", object("available", expert != null && expert.available(),
						"reason", expert == null ? "INSPECTION_ONLY" : expert.reason()),
				"admission", object("state", admission.state(), "reason", admission.reason(),
						"detail", admission.detail()),
				"stages", array(report.stages(), GeoCeDGInspectionReport::stage),
				"comparisons", array(report.comparisons(), GeoCeDGInspectionReport::comparison),
				"diagnostics", array(report.diagnostics(), diagnostic ->
						object("phase", diagnostic.phase(), "status", diagnostic.status(),
								"code", diagnostic.code(), "detail", diagnostic.detail())));
		if (expert != null) {
			result.put("expert", object("version", GeoCeDGExpertIntegrity.VERSION,
					"available", expert.available(), "reason", expert.reason(),
					"fingerprint", expert.fingerprint(), "previewSha256", expert.previewSha256(),
					"trialContextSha256", expert.trialContextSha256()));
		}
		return result.toString(2);
	}

	private static JSONObject stage(Stage stage) {
		return object("phase", stage.phase(), "sha256", stage.sha256(),
				"documentId", stage.documentId(), "angleUnit", stage.angleUnit(),
				"documentContext", context(stage.context()),
				"nativeEvaluated", stage.angleUnit() != null,
				"entries", array(stage.entries(), entry -> object("name", entry.name(),
						"expandedBytes", entry.expandedBytes(), "sha256", entry.sha256(),
						"disposition", entry.disposition())),
				"archiveDeclarations", array(stage.archiveDeclarations(), declaration ->
						object("label", declaration.label(), "nativeType", declaration.nativeType(),
								"claimedDurableId", declaration.durableId())),
				"objects", array(stage.objects(), GeoCeDGInspectionReport::geo));
	}

	private static JSONObject context(DocumentContext context) {
		if (context == null) {
			return null;
		}
		var units = context.units();
		var usm = units.getUsm();
		return object("constructionUnit", units.getConstructionSelection(),
				"presentationUnit", units.getPresentationSelection(),
				"effectivePresentationUnit", units.effectivePresentationUnit(),
				"dimensionUnitSuffixShown", units.isDimensionUnitSuffixShown(),
				"usm", usm == null ? null : object("metresPerUnit", usm.getMetresPerUnit(),
						"name", usm.getName(), "symbol", usm.getSymbol()),
				"hiddenLayers", new JSONArray(context.hiddenLayers()),
				"workingLayer", context.workingLayer(), "maxLayerUsed", context.maxLayerUsed(),
				"activeView", context.activeView(),
				"displayPrecision", context.displayPrecision(),
				"significantFigures", context.significantFigures());
	}

	private static JSONObject geo(ObservedObject geo) {
		var domain = geo.domain();
		return object("ref", geo.ref(), "label", geo.label(), "identity", geo.identity(),
				"copySourceId", geo.copySourceId(),
				"geoClass", geo.geoClass(), "producer", geo.producer(), "p4Family", geo.p4Family(),
				"inputs", new JSONArray(geo.inputs()), "outputs", new JSONArray(geo.outputs()),
				"definition", geo.definition(), "defined", geo.defined(), "currentValue", geo.value(),
				"parameterDomain", domain == null ? null : object("parameterization", domain.parameterization(),
						"start", domain.start(), "end", domain.end(), "step", domain.step(),
						"orientation", domain.orientation()),
				"presentation", geo.presentation());
	}

	private static JSONObject comparison(Comparison comparison) {
		return object("before", comparison.before(), "after", comparison.after(),
				"basis", comparison.basis(), "status", comparison.status(),
				"assessments", array(comparison.assessments(), assessment ->
						object("dimension", assessment.dimension(), "status", assessment.status(),
								"coverage", assessment.coverage())),
				"added", new JSONArray(comparison.added()),
				"additions", array(comparison.additions(), addition -> object("afterRef", addition.afterRef(),
						"sourceRef", addition.sourceRef(), "basis", addition.basis())),
				"unmatchedAfter", new JSONArray(comparison.unmatchedAfter()),
				"objects", array(comparison.objects(), delta -> object("beforeRef", delta.beforeRef(),
						"afterRef", delta.afterRef(), "outcome", delta.outcome(), "observations",
						array(delta.observations(), observation ->
								object("property", observation.property(), "status",
										observation.status(), "before", observation.before(),
										"after", observation.after())))));
	}

	static String summary(GeoCeDGInspectionResult report) {
		return summary(report, null);
	}

	static String summary(GeoCeDGInspectionResult report, GeoCeDGExpertInspection.ExpertTrial expert) {
		StringBuilder text = new StringBuilder(expert == null ? "GeoCeDG I2 | File snapshot inspection\n"
				: "GeoCeDG | Expert insertion trial\n");
		for (Stage stage : report.stages()) {
			text.append(stage.phase()).append(" ").append(stage.sha256() == null ? ""
					: stage.sha256() + " ").append(stage.angleUnit() == null ? "archive inventory"
							: stage.objects().size() + " inspected objects").append('\n');
		}
		text.append("P4 admission: ").append(report.admission().state()).append(" (")
				.append(report.admission().reason()).append(")\n");
		for (Comparison row : report.comparisons()) {
			text.append(row.before()).append(" -> ").append(row.after()).append(" ")
					.append(row.status()).append(" | ").append(row.added().size())
					.append(" additions");
			for (Status status : List.of(Status.VERIFIED_UNCHANGED, Status.VERIFIED_CHANGED,
					Status.KNOWN_OMISSION, Status.NOT_ESTABLISHED)) {
				long count = row.objects().stream().flatMap(delta -> delta.observations().stream())
						.filter(observation -> observation.status() == status).count();
				text.append(" | ").append(count).append(" fields ").append(status);
			}
			text.append('\n');
		}
		for (var diagnostic : report.diagnostics()) {
			text.append(diagnostic.phase()).append(" ").append(diagnostic.status()).append(" ")
					.append(diagnostic.code()).append(" | ").append(diagnostic.detail()).append('\n');
		}
		return text.append(expert == null ? "Publication unavailable in I2. See JSON for property evidence.\n"
				: "Expert trial: " + expert.reason() + "\n").toString();
	}

	private static <T> JSONArray array(List<T> items, Function<T, JSONObject> projection) {
		JSONArray array = new JSONArray();
		items.forEach(item -> array.put(projection.apply(item)));
		return array;
	}

	private static JSONObject object(Object... pairs) {
		JSONObject json = new JSONObject();
		try {
			for (int i = 0; i < pairs.length; i += 2) {
				Object value = pairs[i + 1];
				json.put((String) pairs[i], value instanceof Enum<?> enumeration
						? enumeration.name() : value == null ? JSONObject.NULL : value);
			}
		} catch (JSONException failure) {
			throw new IllegalStateException("Could not serialize inspection evidence", failure);
		}
		return json;
	}
}

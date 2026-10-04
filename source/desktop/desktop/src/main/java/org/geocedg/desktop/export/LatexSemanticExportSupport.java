/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.WorkLedger;
import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.ApproximationEvidence.Guarantee;
import org.geocedg.common.export.ComponentAddress;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.SemanticCurveExportAdapter2D;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticExportClassification;
import org.geocedg.common.export.SemanticExportClassification.ExportClass;
import org.geocedg.common.export.SemanticExportClassification.SourceClass;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;

/**
 * PRE-G9B-R6-plus-C (C10-C14, author decisions {@code DQ-C4}, {@code DQ-C5},
 * {@code DQ-C12}): read-only semantic Locus V2 and Spline V2 output of the
 * GeoCeDG PSTricks, PGF/TikZ and Asymptote exporters.
 *
 * <p>Every eligible semantic source goes through the shared
 * {@link SemanticCurveExportAdapter2D} with a model-coordinate tolerance
 * (default {@value #DEFAULT_TOLERANCE}) and the G9X1 deterministic budgets. Each
 * certified component that the adapter approximates is written as its own
 * path; failed components, gaps and incomplete coverage are disclosed in
 * deterministic comments and in the dialog report, never bridged and never
 * omitted silently. The export area never narrows a semantic domain: the
 * dialects clip at format level.
 */
public final class LatexSemanticExportSupport {

	/** Default semantic tolerance in model coordinates ({@code DQ-C4}). */
	public static final double DEFAULT_TOLERANCE = 0.001;
	private static final String ENTITY_PREFIX = "latex:";

	/** Writes one certified component in the dialect's polyline idiom. */
	@FunctionalInterface
	public interface PathWriter {
		/**
		 * @param locus source whose own line style applies
		 * @param path certified component approximation
		 */
		void writePath(GeoLocusV2 locus, PolylineGeometry path);
	}

	/** One semantic source of an export. */
	public static final class SourceReport {
		private final String label;
		private final String sourceId;
		private final SourceClass sourceClass;
		private final List<Component> components;

		private SourceReport(String label, String sourceId, SourceClass sourceClass,
				List<Component> components) {
			this.label = label;
			this.sourceId = sourceId;
			this.sourceClass = sourceClass;
			this.components = Collections.unmodifiableList(
					new ArrayList<>(components));
		}

		public String getLabel() {
			return label;
		}

		public String getSourceId() {
			return sourceId;
		}

		public SourceClass getSourceClass() {
			return sourceClass;
		}

		/** @return per-component results in kernel order */
		public List<Component> getComponents() {
			return components;
		}

		/** @return emitted component count */
		public int getEmittedCount() {
			int emitted = 0;
			for (Component component : components) {
				if (component.isEmitted()) {
					emitted++;
				}
			}
			return emitted;
		}
	}

	/** Result of one generation, published to the dialog report. */
	public static final class Result {
		private final ExportClass exportClass;
		private final double tolerance;
		private final List<SourceReport> sources;
		private final String rejection;

		private Result(ExportClass exportClass, double tolerance,
				List<SourceReport> sources, String rejection) {
			this.exportClass = exportClass;
			this.tolerance = tolerance;
			this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
			this.rejection = rejection;
		}

		public ExportClass getExportClass() {
			return exportClass;
		}

		/** @return requested model-coordinate tolerance; NaN when invalid */
		public double getTolerance() {
			return tolerance;
		}

		public List<SourceReport> getSources() {
			return sources;
		}

		/** @return rejection reason, or null when code was generated */
		public String getRejection() {
			return rejection;
		}

		/** @return deterministic report text for the dialog */
		public String reportText() {
			StringBuilder text = new StringBuilder();
			text.append("Export classification: ").append(exportClass).append('\n');
			if (rejection != null) {
				text.append("No code generated: ").append(rejection).append('\n');
			}
			text.append("Semantic curve tolerance: ")
					.append(Double.isNaN(tolerance) ? "invalid" : tolerance + " model units")
					.append(" (ESTIMATED_ERROR; deterministic work limits evaluations=")
					.append(GeometryExportRequest.DEFAULT_MAXIMUM_EVALUATIONS)
					.append(", depth=").append(GeometryExportRequest.DEFAULT_MAXIMUM_DEPTH)
					.append(", vertices/component=")
					.append(GeometryExportRequest.DEFAULT_MAXIMUM_VERTICES_PER_COMPONENT)
					.append(", total vertices=")
					.append(GeometryExportRequest.DEFAULT_MAXIMUM_TOTAL_VERTICES)
					.append(")\n");
			text.append("Semantic sources: ").append(sources.size());
			int complete = 0;
			int incomplete = 0;
			int none = 0;
			for (SourceReport source : sources) {
				switch (source.getSourceClass()) {
				case COMPLETE:
					complete++;
					break;
				case INCOMPLETE_WITH_CERTIFIED_COMPONENTS:
					incomplete++;
					break;
				default:
					none++;
					break;
				}
			}
			text.append(" (complete=").append(complete).append(", incomplete=")
					.append(incomplete).append(", no admissible output=").append(none)
					.append(")\n");
			for (SourceReport source : sources) {
				text.append(sourceLine(source)).append('\n');
				for (Component component : source.getComponents()) {
					text.append("  ").append(componentLine(component)).append('\n');
				}
			}
			return text.toString();
		}
	}

	private final App app;
	private double tolerance = DEFAULT_TOLERANCE;
	private final Map<GeoLocusV2, SourceReport> prepared = new IdentityHashMap<>();
	private final List<SourceReport> order = new ArrayList<>();
	private final List<SemanticCurveExportAdapter2D.SourceResult> guards =
			new ArrayList<>();
	private boolean otherContent;
	private final Set<GeoElement> unsupportedSeen =
			Collections.newSetFromMap(new IdentityHashMap<>());
	private final List<GeoElement> otherCandidates = new ArrayList<>();
	private Result lastResult;
	private Consumer<Result> listener;

	/**
	 * @param app exporting application
	 */
	public LatexSemanticExportSupport(App app) {
		this.app = app;
	}

	/**
	 * Request input of the dialog ({@code DQ-C4}); validated at generation.
	 *
	 * @param tolerance model-coordinate tolerance, NaN for an invalid entry
	 */
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	/** @return requested tolerance */
	public double getTolerance() {
		return tolerance;
	}

	/**
	 * @param listener receiver of each generation result
	 */
	public void setResultListener(Consumer<Result> listener) {
		this.listener = listener;
	}

	/** @return result of the last generation, or null */
	public Result getLastResult() {
		return lastResult;
	}

	/**
	 * Prepares one generation: validates the request and adapts every eligible
	 * semantic source in construction order.
	 *
	 * @return null when generation may proceed, otherwise the rejection, which
	 *         has already been published
	 */
	public Result begin() {
		prepared.clear();
		order.clear();
		guards.clear();
		unsupportedSeen.clear();
		otherCandidates.clear();
		otherContent = false;
		if (!(tolerance > 0) || Double.isInfinite(tolerance)) {
			return publish(new Result(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
					Double.NaN, Collections.<SourceReport>emptyList(),
					"the semantic curve tolerance must be a positive finite number "
							+ "of model units"));
		}
		GeometryExportRequest request = GeometryExportRequest.builder(tolerance)
				.allowedGuarantees(EnumSet.of(Guarantee.ESTIMATED_ERROR))
				.allowPartialOutput(false).build();
		WorkLedger ledger = new WorkLedger(request);
		SemanticCurveExportAdapter2D adapter = new SemanticCurveExportAdapter2D();
		List<GeoLocusV2> eligible = new ArrayList<>();
		Construction construction = app.getKernel().getConstruction();
		for (int step = 0; step < construction.steps(); step++) {
			GeoElementND[] geos = construction.getConstructionElement(step)
					.getGeoElements();
			for (GeoElementND geo : geos) {
				collect(geo.toGeoElement(), eligible);
			}
		}
		for (int ordinal = 0; ordinal < eligible.size(); ordinal++) {
			GeoLocusV2 locus = eligible.get(ordinal);
			if (prepared.containsKey(locus)) {
				continue;
			}
			SemanticCurveExportAdapter2D.SourceResult result = adapter.adaptLocus(
					locus, ordinal, request, ledger, ENTITY_PREFIX,
					SemanticCurveExportAdapter2D.ALL_COMPONENTS);
			guards.add(result);
			List<SourceExportOutcome> outcomes = new ArrayList<>();
			for (Component component : result.getComponents()) {
				outcomes.add(component.getOutcome());
				if (SemanticExportClassification.rejectsExport(
						component.getOutcome())) {
					SourceReport report = new SourceReport(locus.getLabelSimple(),
							result.getSourceId(), SourceClass.NO_ADMISSIBLE_OUTPUT,
							result.getComponents());
					return publish(new Result(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
							tolerance, Collections.singletonList(report),
							"the requested approximation guarantee was not established "
									+ "for " + sourceName(report)));
				}
			}
			SourceReport report = new SourceReport(locus.getLabelSimple(),
					result.getSourceId(),
					SemanticExportClassification.classifySource(outcomes),
					result.getComponents());
			prepared.put(locus, report);
			order.add(report);
		}
		return null;
	}

	private void collect(GeoElement geo, List<GeoLocusV2> eligible) {
		if (geo == null || !app.isLayerShown(geo.getLayer())) {
			return;
		}
		if (geo.isGeoList()) {
			GeoList list = (GeoList) geo;
			for (int i = 0; i < list.size(); i++) {
				collect(list.get(i), eligible);
			}
			return;
		}
		if (!geo.isWhollyIn2DView(app.getEuclidianView1())
				|| !geo.isEuclidianVisible()) {
			return;
		}
		if (geo instanceof GeoLocusV2) {
			eligible.add((GeoLocusV2) geo);
		} else if (!geo.isGeoImage()) {
			otherCandidates.add(geo);
		}
	}

	/**
	 * Dispatch of an element without a host export.
	 *
	 * @param geo element
	 * @param writer dialect path writer
	 * @param comment dialect comment writer of one line
	 * @return whether the element was a semantic source handled here
	 */
	public boolean write(GeoElement geo, PathWriter writer, Consumer<String> comment) {
		if (!(geo instanceof GeoLocusV2)) {
			unsupportedSeen.add(geo);
			return false;
		}
		GeoLocusV2 locus = (GeoLocusV2) geo;
		SourceReport report = prepared.get(locus);
		if (report == null) {
			return false;
		}
		comment.accept(sourceLine(report));
		for (Component component : report.getComponents()) {
			comment.accept("  " + componentLine(component));
			if (component.isEmitted()) {
				writer.writePath(locus,
						(PolylineGeometry) component.getEntity().getGeometry());
			}
		}
		return true;
	}

	/**
	 * Completes one generation: checks currentness, classifies the export and
	 * returns the final text with its header comment, or the empty text of a
	 * rejected export.
	 *
	 * @param code generated dialect code
	 * @param commentPrefix dialect line-comment prefix
	 * @param scaleComment physical scale statement, or null
	 * @return final text
	 */
	public String finish(StringBuilder code, String commentPrefix,
			String scaleComment) {
		for (GeoElement candidate : otherCandidates) {
			if (!unsupportedSeen.contains(candidate)) {
				otherContent = true;
				break;
			}
		}
		for (SemanticCurveExportAdapter2D.SourceResult guard : guards) {
			if (!guard.isCurrent()) {
				publish(new Result(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
						tolerance, order, "STALE_SOURCE_REVISION: a semantic source "
								+ "changed during generation"));
				return "";
			}
		}
		List<SourceClass> classes = new ArrayList<>();
		for (SourceReport report : order) {
			classes.add(report.getSourceClass());
		}
		ExportClass exportClass = SemanticExportClassification.classifyExport(
				classes, otherContent, false);
		if (exportClass == ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT) {
			publish(new Result(exportClass, tolerance, order,
					"no semantic source has an admissible component and nothing "
							+ "else is exported"));
			return "";
		}
		publish(new Result(exportClass, tolerance, order, null));
		if (order.isEmpty() && scaleComment == null) {
			return code.toString();
		}
		StringBuilder header = new StringBuilder();
		if (scaleComment != null) {
			header.append(commentPrefix).append(' ').append(scaleComment).append('\n');
		}
		if (!order.isEmpty()) {
			header.append(commentPrefix).append(" GeoCeDG export classification: ")
					.append(exportClass).append("; semantic curves (Locus V2, ")
					.append("Spline V2): ").append(order.size())
					.append(" source(s), tolerance ").append(tolerance)
					.append(" model units, ESTIMATED_ERROR; export-only ")
					.append("approximations, one path per certified component, ")
					.append("gaps never bridged\n");
			if (exportClass != ExportClass.COMPLETE) {
				header.append(commentPrefix)
						.append(" Incomplete: some semantic output is locally ")
						.append("certified only or failed; see the comments of ")
						.append("each source\n");
			}
		}
		return header.append(code).toString();
	}

	private Result publish(Result result) {
		lastResult = result;
		if (listener != null) {
			listener.accept(result);
		}
		return result;
	}

	private static String sourceName(SourceReport report) {
		return report.getLabel() == null ? report.getSourceId()
				: report.getLabel() + " [" + report.getSourceId() + "]";
	}

	private static String sourceLine(SourceReport report) {
		return "GeoCeDG semantic curve " + sourceName(report) + ": "
				+ report.getSourceClass() + "; components emitted "
				+ report.getEmittedCount() + " of " + report.getComponents().size();
	}

	private static String componentLine(Component component) {
		SourceExportOutcome outcome = component.getOutcome();
		ComponentAddress address = outcome.getComponentAddress();
		StringBuilder line = new StringBuilder();
		line.append(address.getBranchKey() == null ? ""
				: address.getBranchKey() + "/").append(address.getComponentKey());
		if (address.hasSemanticInterval()) {
			line.append(" [").append(address.getParameterStart()).append(", ")
					.append(address.getParameterEnd()).append(']');
		}
		line.append(": ").append(outcome.getFidelity());
		if (outcome.isEmitted()) {
			ApproximationEvidence evidence = outcome.getApproximationEvidence();
			line.append(", ").append(evidence.getGuarantee()).append(" achieved ")
					.append(evidence.getAchievedError()).append(", coverage ")
					.append(outcome.getSemanticCoverage()).append(", vertices ")
					.append(evidence.getVertices());
		} else {
			line.append(" [").append(outcome.getReason()).append("] ")
					.append(outcome.getMessage());
		}
		return line.toString();
	}
}

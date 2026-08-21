/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.main.AppConfig;

/**
 * Immutable Desktop presentation of one completed G9X1 preflight. The callback
 * seam enforces presentation and any approximation acknowledgement before the
 * destination selector can be reached, without depending on Swing in tests.
 */
public final class DxfExportPreflightPresentation {

	/** UI boundary used after geometry classification and before file access. */
	public interface DestinationPort {
		/**
		 * Presents the complete preflight report.
		 *
		 * @return whether the user acknowledged the report
		 */
		boolean presentPreflight(DxfExportPreflightPresentation presentation);

		/**
		 * Obtains explicit consent for fidelity-reduced geometry.
		 *
		 * @return whether approximate export was explicitly accepted
		 */
		boolean confirmApproximateExport(
				DxfExportPreflightPresentation presentation);

		/** Reports that the source revision changed after preflight. */
		void reportStaleSource(DxfExportPreflightPresentation presentation);

		/** @return selected output destination, or null when cancelled */
		Destination chooseDestination(
				DxfExportPreflightPresentation presentation);
	}

	/** Immutable destination and collision decision returned by the UI. */
	public static final class Destination {
		private final Path dxfPath;
		private final boolean replaceExisting;

		/**
		 * @param dxfPath requested final DXF path
		 * @param replaceExisting whether occupied paired destinations may be replaced
		 */
		public Destination(Path dxfPath, boolean replaceExisting) {
			this.dxfPath = Objects.requireNonNull(dxfPath,
					"DXF destination is required");
			this.replaceExisting = replaceExisting;
		}

		public Path getDxfPath() {
			return dxfPath;
		}

		public boolean isReplaceExisting() {
			return replaceExisting;
		}
	}

	private final GeometryExportPreflight preflight;
	private final String approximationEvidenceText;

	private DxfExportPreflightPresentation(GeometryExportPreflight preflight) {
		this.preflight = Objects.requireNonNull(preflight,
				"Export preflight is required");
		approximationEvidenceText = buildApproximationEvidence(preflight);
	}

	/** @return immutable presentation derived from the shared preflight */
	public static DxfExportPreflightPresentation from(
			GeometryExportPreflight preflight) {
		return new DxfExportPreflightPresentation(preflight);
	}

	/**
	 * Exact branch predicate used by the Desktop controller. Keeping it here
	 * makes the default G5 versus opt-in G9X1 decision executable without Swing.
	 *
	 * @param config active application configuration
	 * @return whether the G9X1 Desktop flow is explicitly enabled
	 */
	public static boolean isExtendedDxfEnabled(AppConfig config) {
		return config instanceof AppConfigGeoCeDG
				&& ((AppConfigGeoCeDG) config).getRuntimeFeatureService()
						.isExtendedDxfEnabled();
	}

	/**
	 * Enforces preflight presentation and explicit approximation consent before
	 * invoking the destination selector. A rejected strict preflight can never
	 * reach destination access.
	 *
	 * @param port Desktop presentation callback
	 * @return destination, or null when blocked or cancelled
	 */
	public Destination requestDestination(DestinationPort port) {
		DestinationPort checkedPort = Objects.requireNonNull(port,
				"Destination presentation port is required");
		if (!requireCurrentSource(checkedPort)) {
			return null;
		}
		boolean preflightAccepted = checkedPort.presentPreflight(this);
		if (!requireCurrentSource(checkedPort) || !preflightAccepted
				|| !preflight.isWritable()) {
			return null;
		}
		if (requiresApproximateConfirmation()) {
			if (!requireCurrentSource(checkedPort)) {
				return null;
			}
			boolean approximationAccepted =
					checkedPort.confirmApproximateExport(this);
			if (!requireCurrentSource(checkedPort) || !approximationAccepted) {
				return null;
			}
		}
		if (!requireCurrentSource(checkedPort)) {
			return null;
		}
		return checkedPort.chooseDestination(this);
	}

	private boolean requireCurrentSource(DestinationPort port) {
		if (preflight.isSourceRevisionCurrent()) {
			return true;
		}
		port.reportStaleSource(this);
		return false;
	}

	/** @return current preflight summary text */
	public String getSummaryText() {
		boolean current = preflight.isSourceRevisionCurrent();
		return buildSummary(preflight, current,
				preflight.isWritable() && current);
	}

	public String getApproximationEvidenceText() {
		return approximationEvidenceText;
	}

	/** @return fidelity disclosure used by the successful completion dialog */
	public String getCompletionEvidenceText() {
		if (preflight.getApproximateCount() == 0) {
			return "Approximation evidence: none.";
		}
		return "Requested tolerance: "
				+ preflight.getRequest().getRequestedTolerance()
				+ "\nAllowed guarantees: "
				+ preflight.getRequest().getAllowedGuarantees()
				+ "\nApproximation evidence:\n" + approximationEvidenceText;
	}

	public String getWarningsText() {
		return buildWarnings(preflight, preflight.isSourceRevisionCurrent());
	}

	public boolean isWritable() {
		return preflight.isWritable() && preflight.isSourceRevisionCurrent();
	}

	public boolean isStrictNoPartial() {
		return !preflight.getRequest().isPartialOutputAllowed();
	}

	/** @return whether destination selection requires approximation confirmation */
	public boolean requiresApproximateConfirmation() {
		return preflight.getApproximateCount() > 0;
	}

	public boolean isSidecarRequired() {
		return preflight.isSidecarRequired();
	}

	private static String buildSummary(GeometryExportPreflight preflight,
			boolean sourceRevisionCurrent, boolean writable) {
		GeometryExportRequest request = preflight.getRequest();
		StringBuilder text = new StringBuilder();
		text.append("Component outcomes: exact=").append(preflight.getExactCount())
				.append(", approximate=").append(preflight.getApproximateCount())
				.append(", unsupported=").append(preflight.getUnsupportedCount())
				.append(", invalid=").append(preflight.getInvalidCount())
				.append(", omitted=").append(preflight.getOmittedCount())
				.append(", hidden=").append(preflight.getHiddenCount()).append('\n');
		text.append("Coordinates: ")
				.append(preflight.getModel().getCoordinateSystem())
				.append("; source unit=")
				.append(preflight.getModel().getSourceUnit())
				.append("; target unit=")
				.append(preflight.getModel().getTargetUnit()).append('\n');
		text.append("Approximation policy: allowed=")
				.append(request.isApproximationAllowed())
				.append("; requested tolerance=")
				.append(request.getRequestedTolerance())
				.append("; allowed evidence=")
				.append(allowedGuarantees(request)).append('\n');
		text.append("Work limits: evaluations=")
				.append(request.getMaximumEvaluations()).append("; depth=")
				.append(request.getMaximumDepth()).append("; vertices/component=")
				.append(request.getMaximumVerticesPerComponent())
				.append("; total vertices=")
				.append(request.getMaximumTotalVertices()).append('\n');
		text.append("Semantic domains: ").append(domains(request)).append('\n');
		text.append("Partial output: ")
				.append(request.isPartialOutputAllowed()
						? "enabled by explicit request"
						: "disabled (strict complete-request policy)")
				.append('\n');
		text.append("Sidecar: ").append(sidecarPolicy(preflight)).append('\n');
		text.append("Source revision current: ")
				.append(sourceRevisionCurrent ? "yes" : "no")
				.append('\n');
		text.append("Complete request writable: ")
				.append(writable ? "yes" : "no");
		return text.toString();
	}

	private static String allowedGuarantees(GeometryExportRequest request) {
		List<String> values = new ArrayList<>();
		request.getAllowedGuarantees().forEach(
				guarantee -> values.add(guarantee.name()));
		values.sort(Comparator.naturalOrder());
		return String.join(", ", values);
	}

	private static String domains(GeometryExportRequest request) {
		List<String> values = new ArrayList<>();
		appendDomains(values, "default", request.getDefaultSemanticDomains());
		Map<String, List<SemanticDomain>> sourceDomains = new TreeMap<>(
				request.getSourceSemanticDomains());
		for (Map.Entry<String, List<SemanticDomain>> entry
				: sourceDomains.entrySet()) {
			appendDomains(values, "source " + entry.getKey(), entry.getValue());
		}
		return values.isEmpty() ? "none declared" : String.join("; ", values);
	}

	private static void appendDomains(List<String> values, String owner,
			List<SemanticDomain> domains) {
		for (SemanticDomain domain : domains) {
			String branch = domain.getBranchKey() == null ? ""
					: "[branch=" + domain.getBranchKey() + "]";
			values.add(owner + "/" + domain.getKey() + branch + "="
					+ boundary(domain.isStartClosed(), true)
					+ domain.getStartParameter() + " -> "
					+ domain.getEndParameter()
					+ boundary(domain.isEndClosed(), false));
		}
	}

	private static String boundary(boolean closed, boolean start) {
		if (start) {
			return closed ? "[" : "(";
		}
		return closed ? "]" : ")";
	}

	private static String sidecarPolicy(GeometryExportPreflight preflight) {
		if (preflight.isSidecarRequired()) {
			return "mandatory/requested <drawing>.dxf.manifest.json";
		}
		return "optional and omitted for wholly exact output";
	}

	private static String buildApproximationEvidence(
			GeometryExportPreflight preflight) {
		List<String> lines = new ArrayList<>();
		for (SourceExportOutcome outcome : preflight.getModel().getOutcomes()) {
			ApproximationEvidence evidence = outcome.getApproximationEvidence();
			if (evidence == null) {
				continue;
			}
			lines.add(outcome.getSourceId() + "/" + componentLabel(outcome)
					+ ": fidelity=" + outcome.getFidelity()
					+ ", reason=" + outcome.getReason()
					+ ", method=" + evidence.getMethod()
					+ ", guarantee=" + evidence.getGuarantee()
					+ ", requested=" + evidence.getRequestedTolerance()
					+ ", achieved=" + evidence.getAchievedError()
					+ ", evaluations=" + evidence.getEvaluations()
					+ ", subdivisions=" + evidence.getSubdivisions()
					+ ", segments=" + evidence.getSegments()
					+ ", vertices=" + evidence.getVertices()
					+ ", maximum depth=" + evidence.getMaximumDepth());
		}
		return lines.isEmpty() ? "Approximation evidence: none."
				: String.join("\n", lines);
	}

	private static String buildWarnings(GeometryExportPreflight preflight,
			boolean sourceRevisionCurrent) {
		List<String> lines = new ArrayList<>();
		for (SourceExportOutcome outcome : preflight.getModel().getOutcomes()) {
			String component = componentLabel(outcome);
			if (outcome.getFidelity() == Fidelity.APPROXIMATE) {
				lines.add("APPROXIMATE " + outcome.getSourceId() + "/" + component
						+ ": export-only fidelity reduction; sidecar required.");
			}
			if (!outcome.isVisible()) {
				lines.add("HIDDEN_SOURCE_INCLUDED " + outcome.getSourceId() + "/"
						+ component + ": source remains included.");
			}
			if (!outcome.isEmitted()) {
				lines.add(outcome.getFidelity() + " " + outcome.getSourceId() + "/"
						+ component + " [" + outcome.getReason() + "]: "
						+ outcome.getMessage());
			}
		}
		for (Diagnostic diagnostic : preflight.getModel().getDiagnostics()) {
			lines.add("DIAGNOSTIC " + diagnostic.getSourceId() + " ["
					+ diagnostic.getCode() + "]: " + diagnostic.getMessage());
		}
		if (!sourceRevisionCurrent) {
			lines.add("STALE_SOURCE_REVISION: source changed after preflight.");
		}
		return lines.isEmpty() ? "Warnings: none." : String.join("\n", lines);
	}

	private static String componentLabel(SourceExportOutcome outcome) {
		String branch = outcome.getComponentAddress().getBranchKey();
		return branch == null ? outcome.getComponentAddress().getComponentKey()
				: branch + "/" + outcome.getComponentAddress().getComponentKey();
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.EvaluatorOnlyLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexStatistics2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.desktop.AppGeoCeDG;
import org.geogebra.common.kernel.geos.GeoElement;

/** Opens and refreshes the disposable developer diagnostics surface. */
final class LocusV2LaboratoryController {
	static final int DIAGNOSTICS_WIDTH = 720;
	static final int DIAGNOSTICS_HEIGHT = 520;
	private final AppGeoCeDG app;
	private final LocusV2LaboratoryFixtures.State state;
	private final JTextArea diagnostics = new JTextArea();
	private final Map<GeoLocusV2, LocusRenderCache2D> renderCaches =
			new IdentityHashMap<>();
	private final Map<GeoLocusV2, MetricLaboratoryEntry> metricEntries =
			new IdentityHashMap<>();

	private LocusV2LaboratoryController(AppGeoCeDG app) {
		this.app = app;
		this.state = LocusV2LaboratoryFixtures.create(
				app.getKernel().getConstruction());
	}

	static void open(AppGeoCeDG app) {
		LocusV2LaboratoryController controller =
				new LocusV2LaboratoryController(app);
		controller.showDiagnostics();
		app.getKernel().notifyRepaint();
	}

	private void showDiagnostics() {
		JDialog dialog = new JDialog(app.getFrame(), "Locus V2 diagnostics", false);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				closeMetrics();
			}
		});
		dialog.setLayout(new BorderLayout(8, 8));
		JLabel warning = new JLabel("DEVELOPER-ONLY / EXPERIMENTAL - "
				+ "this construction cannot be saved as a .ggb file");
		warning.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
		dialog.add(warning, BorderLayout.NORTH);

		diagnostics.setEditable(false);
		diagnostics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		diagnostics.setLineWrap(false);
		dialog.add(new JScrollPane(diagnostics), BorderLayout.CENTER);

		JButton refresh = new JButton("Refresh semantic diagnostics");
		refresh.addActionListener(event -> refreshDiagnostics());
		JPanel controls = new JPanel(new BorderLayout());
		controls.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
		controls.add(refresh, BorderLayout.EAST);
		dialog.add(controls, BorderLayout.SOUTH);
		dialog.setPreferredSize(new Dimension(DIAGNOSTICS_WIDTH,
				DIAGNOSTICS_HEIGHT));
		dialog.pack();
		dialog.setLocationRelativeTo(app.getFrame());
		refreshDiagnostics();
		dialog.setVisible(true);
	}

	private void closeMetrics() {
		for (MetricLaboratoryEntry entry : metricEntries.values()) {
			entry.remove();
		}
		metricEntries.clear();
		renderCaches.clear();
	}

	private void refreshDiagnostics() {
		StringBuilder text = new StringBuilder();
		text.append("GeoCeDG Locus V2 developer laboratory\n")
				.append("Public command: none; persistence: none; Path: none\n")
				.append("G7B metric: internal rich Geo + explicit scalar adapter; ")
				.append("XML/3D absent\n")
				.append("Edit g6r* source values in Algebra View, then refresh.\n\n");
		for (LocusV2LaboratoryFixtures.Entry entry : state.getEntries()) {
			appendEntry(text, entry);
		}
		diagnostics.setText(text.toString());
		diagnostics.setCaretPosition(0);
	}

	private void appendEntry(StringBuilder text,
			LocusV2LaboratoryFixtures.Entry entry) {
		GeoLocusV2 locus = entry.getLocus();
		LocusDefinition2D definition = locus.getSemanticDefinition();
		final MetricLaboratoryEntry metrics =
				currentMetrics(entry, definition);
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(128);
		for (LocusBranch2D branch : definition.getBranches()) {
			if (!branch.getValidDomainComponents().isEmpty()) {
				double lower = branch.getValidDomainComponents().get(0).getLower();
				double upper = branch.getValidDomainComponents().get(0).getUpper();
				double parameter = lower + (upper - lower) / 2;
				locus.evaluate(branch.getBranchKey(), parameter, session);
				locus.evaluate(branch.getBranchKey(), parameter, session);
			}
		}
		LocusRenderCache2D renderCache = renderCaches.computeIfAbsent(locus,
				ignored -> new LocusRenderCache2D());
		final LocusRenderData2D renderData = renderCache.getOrBuild(locus,
				LocusRenderPolicy2D.from(app.getActiveEuclidianView()));
		LocusInstrumentationSnapshot2D counters = locus.getInstrumentation()
				.snapshot();

		text.append(entry.getLabel()).append(" - ").append(entry.getPurpose())
				.append('\n');
		text.append("  identity/revision: ").append(locus.getLocusIdentity())
				.append(" / ").append(locus.getSemanticRevision()).append('\n');
		text.append("  provider/version: ").append(definition.getProvider()
				.getProviderId()).append(" / ").append(definition.getProvider()
				.getParameterDescriptor()).append('\n');
		text.append("  definition/determinism: ")
				.append(definition.getDefinitionStatus()).append(" / ")
				.append(definition.getDeterminism()).append('\n');
		for (LocusBranch2D branch : definition.getBranches()) {
			text.append("  branchKey: ").append(branch.getBranchKey()).append('\n')
					.append("    domain: ").append(branch.getDeclaredDriverDomain())
					.append("; valid: ").append(branch.getValidDomainComponents())
					.append('\n').append("    orientation/properties: ")
					.append(branch.getOrientation()).append(" / ")
					.append(branch.getProperties()).append('\n')
					.append("    lineage: ").append(branch.getLineage()).append('\n')
					.append("    quality: ")
					.append(branch.getQuality().getConstructionFidelity()).append(" / ")
					.append(branch.getQuality().getEvaluationMethod()).append(" / ")
					.append(branch.getQuality().getRepresentationRole()).append(" / ")
					.append(branch.getQuality().getNumericGuarantee()).append('\n');
		}
		text.append("  session hits/misses/cache: ").append(session.getHits())
				.append(" / ").append(session.getMisses()).append(" / ")
				.append(session.getCachedEntryCount()).append('\n');
		text.append("  semantic calls/render calls: ")
				.append(counters.getEvaluatorCalls()).append(" / ")
				.append(counters.getRenderEvaluations()).append('\n');
		text.append("  render vertices/cache hits/misses: ")
				.append(renderData.getVertices().size()).append(" / ")
				.append(renderCache.getHits()).append(" / ")
				.append(renderCache.getMisses()).append('\n');
		appendMetricDiagnostics(text, metrics);
		text.append('\n');
		session.close();
	}

	private MetricLaboratoryEntry currentMetrics(
			LocusV2LaboratoryFixtures.Entry entry,
			LocusDefinition2D definition) {
		GeoLocusV2 locus = entry.getLocus();
		MetricLaboratoryEntry current = metricEntries.get(locus);
		if (current != null && current.semanticRevision
				== definition.getSemanticRevision()) {
			return current;
		}
		if (current != null) {
			current.remove();
		}
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.initial();
		LocusMetricCapabilityHierarchy2D capabilities =
				new LocusMetricCapabilityHierarchy2D(Collections.singletonList(
						new EvaluatorOnlyLocusMetricCapability2D(
								"developer-laboratory-evaluator-only/v1")));
		AlgoLocusMetricV2 total = new AlgoLocusMetricV2(
				app.getKernel().getConstruction(), locus,
				new TotalLocusMetricQuery(locus.getLocusIdentity(),
						definition.getSemanticRevision(), policy),
				capabilities,
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
				entry.getLabel() + "/total", new GeoElement[0]);
		total.getResult().setLabel(entry.getLabel() + "MetricTotal");
		AlgoLocusMetricScalarAdapter totalScalar =
				new AlgoLocusMetricScalarAdapter(
						app.getKernel().getConstruction(), total.getResult());
		AlgoLocusMetricV2 between = createBetweenMetric(entry, definition,
				capabilities, policy);
		AlgoLocusMetricScalarAdapter betweenScalar = between == null ? null
				: new AlgoLocusMetricScalarAdapter(
						app.getKernel().getConstruction(), between.getResult());
		MetricLaboratoryEntry created = new MetricLaboratoryEntry(
				definition.getSemanticRevision(), total, totalScalar, between,
				betweenScalar);
		metricEntries.put(locus, created);
		return created;
	}

	private AlgoLocusMetricV2 createBetweenMetric(
			LocusV2LaboratoryFixtures.Entry entry,
			LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricPolicy2D policy) {
		if (definition.getBranches().isEmpty()) {
			return null;
		}
		LocusBranch2D branch = definition.getBranches().get(0);
		if (branch.getValidDomainComponents().isEmpty()) {
			return null;
		}
		LocusInterval2D component =
				branch.getValidDomainComponents().get(0);
		double start = component.getLower()
				+ (component.getUpper() - component.getLower()) * 0.25;
		double target = component.getLower()
				+ (component.getUpper() - component.getLower()) * 0.75;
		if (component.getLower() == component.getUpper()) {
			start = component.getLower();
			target = start;
		}
		LocusMetricPositionBinder2D binder =
				new LocusMetricPositionBinder2D();
		MetricPositionBinding2D startBinding = binder.bind(
				new LocusSemanticPosition2D(definition.getLocusIdentity(),
						branch.getBranchKey(),
						definition.getProvider().getProviderId(), start),
				definition);
		MetricPositionBinding2D targetBinding = binder.bind(
				new LocusSemanticPosition2D(definition.getLocusIdentity(),
						branch.getBranchKey(),
						definition.getProvider().getProviderId(), target),
				definition);
		BetweenPositionsMetricQuery query =
				new BetweenPositionsMetricQuery(startBinding, targetBinding,
						TraversalDirection.FORWARD,
						OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH, policy);
		AlgoLocusMetricV2 metric = new AlgoLocusMetricV2(
				app.getKernel().getConstruction(), entry.getLocus(), query,
				capabilities,
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
				entry.getLabel() + "/between", new GeoElement[0]);
		metric.getResult().setLabel(entry.getLabel() + "MetricBetween");
		return metric;
	}

	private static void appendMetricDiagnostics(StringBuilder text,
			MetricLaboratoryEntry entry) {
		appendMetricResult(text, "total", entry.total.getResult(),
				entry.totalScalar);
		if (entry.between != null) {
			appendMetricResult(text, "between", entry.between.getResult(),
					entry.betweenScalar);
		}
		GeoLocusV2 source = entry.total.getSource();
		LocusMetricIndexStatistics2D index =
				source.getMetricSharedOwnerForDiagnostics().statistics();
		LocusMetricInstrumentationSnapshot2D work =
				source.getMetricInstrumentation().snapshot();
		text.append("  metric component builds/hits/misses/cross-result: ")
				.append(index.getBuilds()).append(" / ")
				.append(index.getHits()).append(" / ")
				.append(index.getMisses()).append(" / ")
				.append(work.getCrossResultHits()).append('\n');
		text.append("  metric retained/evicted/bytes/active: ")
				.append(index.getRetainedEntries()).append(" / ")
				.append(index.getEvictions()).append(" / ")
				.append(index.getApproximateRetainedBytes()).append(" / ")
				.append(index.getActiveBuilds()).append('\n');
		text.append("  forbidden metric render/legacy/whole-locus/per-point-index: ")
				.append(work.getRenderReads()).append(" / ")
				.append(work.getLegacySampleReads()).append(" / ")
				.append(work.getWholeLocusRegenerations()).append(" / ")
				.append(work.getIndexBuildsInsideDownstreamPoint()).append('\n');
	}

	private static void appendMetricResult(StringBuilder text, String name,
			GeoLocusMetricResult result,
			AlgoLocusMetricScalarAdapter scalar) {
		LocusMetricResult2D value = result.getMetricResult();
		text.append("  metric ").append(name).append(": value=")
				.append(value.getMetricValue()).append("; status=")
				.append(value.getComputationStatus()).append("; coverage=")
				.append(value.getCoverage()).append("; rectifiability=")
				.append(value.getRectifiability()).append("; traversal=")
				.append(value.getTraversalOutcome()).append("; guarantee=")
				.append(value.getErrorEvidence().getNumericGuarantee())
				.append("; scalar-admissible=")
				.append(result.isScalarAdmissible())
				.append("; adapter-defined=")
				.append(scalar.getScalarOutput().isDefined()).append('\n');
	}

	private static final class MetricLaboratoryEntry {
		private final long semanticRevision;
		private final AlgoLocusMetricV2 total;
		private final AlgoLocusMetricScalarAdapter totalScalar;
		private final AlgoLocusMetricV2 between;
		private final AlgoLocusMetricScalarAdapter betweenScalar;

		private MetricLaboratoryEntry(long semanticRevision,
				AlgoLocusMetricV2 total,
				AlgoLocusMetricScalarAdapter totalScalar,
				AlgoLocusMetricV2 between,
				AlgoLocusMetricScalarAdapter betweenScalar) {
			this.semanticRevision = semanticRevision;
			this.total = total;
			this.totalScalar = totalScalar;
			this.between = between;
			this.betweenScalar = betweenScalar;
		}

		private void remove() {
			totalScalar.remove();
			if (betweenScalar != null) {
				betweenScalar.remove();
			}
			total.remove();
			if (between != null) {
				between.remove();
			}
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.desktop.AppGeoCeDG;

/** Opens and refreshes the disposable developer diagnostics surface. */
final class LocusV2LaboratoryController {
	static final int DIAGNOSTICS_WIDTH = 720;
	static final int DIAGNOSTICS_HEIGHT = 520;
	private final AppGeoCeDG app;
	private final LocusV2LaboratoryFixtures.State state;
	private final JTextArea diagnostics = new JTextArea();
	private final Map<GeoLocusV2, LocusRenderCache2D> renderCaches =
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

	private void refreshDiagnostics() {
		StringBuilder text = new StringBuilder();
		text.append("GeoCeDG Locus V2 developer laboratory\n")
				.append("Public command: none; persistence: none; Path: none\n")
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
				.append(renderCache.getMisses()).append("\n\n");
		session.close();
	}
}

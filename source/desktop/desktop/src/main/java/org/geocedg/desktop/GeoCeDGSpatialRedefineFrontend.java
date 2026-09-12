/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.util.StringJoiner;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentHandler;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactEntry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactReport;

/** Classic 5 presentation adapter for the kernel-owned A3 assessment. */
final class GeoCeDGSpatialRedefineFrontend
		implements SpatialRedefineAssessmentHandler {

	interface Presentation {
		boolean confirmLegacy(SpatialRedefineAssessment assessment);

		void showUnavailable(SpatialRedefineAssessment assessment);

		void showStaleAssessment();
	}

	private final Presentation presentation;

	GeoCeDGSpatialRedefineFrontend(AppGeoCeDG app) {
		this(new SwingPresentation(app));
	}

	GeoCeDGSpatialRedefineFrontend(Presentation presentation) {
		this.presentation = java.util.Objects.requireNonNull(presentation);
	}

	@Override
	public SpatialRedefineExecutionMode selectExecutionMode(
			SpatialRedefineAssessment assessment) {
		switch (assessment.getStatus()) {
		case ADVANCED_RETAIN_AVAILABLE:
		case ADVANCED_RETAIN_AVAILABLE_WITH_RELOCATION:
			return SpatialRedefineExecutionMode.ADVANCED_RETAIN;
		case LEGACY_REPLACEMENT_AVAILABLE:
			return presentation.confirmLegacy(assessment)
					? SpatialRedefineExecutionMode.LEGACY_REPLACEMENT : null;
		case INVALID_DAG:
		case INCOMPATIBLE_HOST_REDEFINE:
		case UNSUPPORTED:
		case AMBIGUOUS:
		case STALE_ASSESSMENT:
		default:
			presentation.showUnavailable(assessment);
			return null;
		}
	}

	@Override
	public void assessmentBecameStale(SpatialRedefineAssessment assessment) {
		presentation.showStaleAssessment();
	}

	private static final class SwingPresentation implements Presentation {
		private static final int MESSAGE_ROWS = 18;
		private static final int MESSAGE_COLUMNS = 72;
		private final AppGeoCeDG app;

		private SwingPresentation(AppGeoCeDG app) {
			this.app = app;
		}

		@Override
		public boolean confirmLegacy(SpatialRedefineAssessment assessment) {
			SpatialRedefineImpactReport report = assessment.getImpactReport();
			if (report == null || report.getCompleteness()
					!= SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE) {
				showUnavailable(assessment);
				return false;
			}
			JTextArea message = new JTextArea(buildLegacyMessage(assessment,
					language()), MESSAGE_ROWS, MESSAGE_COLUMNS);
			message.setEditable(false);
			message.setLineWrap(true);
			message.setWrapStyleWord(true);
			message.setCaretPosition(0);
			Object[] options = {text("Redefine.Legacy.Proceed"),
					text("Redefine.Cancel")};
			int selected = JOptionPane.showOptionDialog(parent(),
					new JScrollPane(message), text("Redefine.Legacy.Title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[1]);
			return selected == 0;
		}

		@Override
		public void showUnavailable(SpatialRedefineAssessment assessment) {
			String key = "Redefine.Status." + assessment.getStatus().name();
			JOptionPane.showMessageDialog(parent(), text(key),
					text("Redefine.Unavailable.Title"), JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void showStaleAssessment() {
			JOptionPane.showMessageDialog(parent(),
					text("Redefine.Status.STALE_ASSESSMENT"),
					text("Redefine.Unavailable.Title"), JOptionPane.ERROR_MESSAGE);
		}

		private Component parent() {
			return app.getMainComponent();
		}

		private String language() {
			return app.getLocale().getLanguage();
		}

		private String text(String key) {
			return GeoCeDGProfile.getText(key, language());
		}
	}

	static String buildLegacyMessage(SpatialRedefineAssessment assessment,
			String language) {
		SpatialRedefineImpactReport report = assessment.getImpactReport();
		if (report == null || report.getCompleteness()
				!= SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE) {
			throw new IllegalArgumentException("Complete kernel impact is required");
		}
		StringBuilder message = new StringBuilder();
		message.append(text("Redefine.Legacy.Summary", language)).append("\n\n")
				.append(text("Redefine.Legacy.Impact", language)).append('\n');
		for (SpatialRedefineImpactEntry entry : report.getEntries()) {
			StringJoiner sources = new StringJoiner(", ");
			entry.getAffectedSourceIds().forEach(id -> sources.add(id.toString()));
			message.append("\n- ")
					.append(text("Redefine.Impact.Participant", language)).append(": ")
					.append(entry.getParticipantId().toExternalForm()).append('\n')
					.append("  ").append(text("Redefine.Impact.Relation", language))
					.append(": ").append(entry.getRelationType()).append('\n')
					.append("  ").append(text("Redefine.Impact.Sources", language))
					.append(": ").append(sources).append('\n')
					.append("  ").append(text("Redefine.Impact.Status", language))
					.append(": ").append(text("Redefine.Predicted."
							+ entry.getPredictedStatus().name(), language)).append('\n')
					.append("  ").append(text("Redefine.Impact.Reason", language))
					.append(": ").append(text("Redefine.Reason."
							+ entry.getReasonCode().name(), language)).append('\n')
					.append("  ").append(text("Redefine.Impact.Recovery", language))
					.append(": ").append(text("Redefine.Recovery."
							+ entry.getRecoveryClass().name(), language)).append('\n');
		}
		message.append('\n').append(text("Redefine.Legacy.Undo", language));
		return message.toString();
	}

	private static String text(String key, String language) {
		return GeoCeDGProfile.getText(key, language);
	}
}

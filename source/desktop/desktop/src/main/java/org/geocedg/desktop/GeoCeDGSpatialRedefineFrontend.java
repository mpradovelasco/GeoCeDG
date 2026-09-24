/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentHandler;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
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

		/**
		 * Presents only the kernel-authorized operations of a durable-contract
		 * change (PRE-G9B-R3). The default cancels.
		 *
		 * @return the explicitly selected operation, or {@link ContractChoice#CANCEL}
		 */
		default ContractChoice chooseContractChange(
				SpatialRedefineAssessment assessment) {
			return ContractChoice.CANCEL;
		}
	}

	/** Explicit user operation for a durable-contract change. */
	enum ContractChoice {
		RETAIN_IDENTITY,
		EXPLICIT_REPLACEMENT,
		CANCEL
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
		case DURABLE_CONTRACT_CHANGE:
			if (availableContractChoices(assessment).isEmpty()) {
				presentation.showUnavailable(assessment);
				return null;
			}
			return contractMode(assessment,
					presentation.chooseContractChange(assessment));
		case INVALID_DAG:
		case INCOMPATIBLE_HOST_REDEFINE:
		case UNSUPPORTED:
		case AMBIGUOUS:
		case STALE_ASSESSMENT:
		case UNDESCRIBABLE_PROPOSAL:
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
			JOptionPane.showMessageDialog(parent(),
					buildUnavailableMessage(assessment, language()),
					text("Redefine.Unavailable.Title"), JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public ContractChoice chooseContractChange(
				SpatialRedefineAssessment assessment) {
			List<ContractChoice> choices = availableContractChoices(assessment);
			JTextArea message = new JTextArea(buildContractChangeMessage(assessment,
					language()), MESSAGE_ROWS, MESSAGE_COLUMNS);
			message.setEditable(false);
			message.setLineWrap(true);
			message.setWrapStyleWord(true);
			message.setCaretPosition(0);
			ArrayList<Object> options = new ArrayList<>();
			for (ContractChoice choice : choices) {
				options.add(text(choice == ContractChoice.RETAIN_IDENTITY
						? "Redefine.Contract.Retain" : "Redefine.Contract.Replace"));
			}
			options.add(text("Redefine.Cancel"));
			Object[] labels = options.toArray();
			int selected = JOptionPane.showOptionDialog(parent(),
					new JScrollPane(message), text("Redefine.Contract.Title"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
					labels, labels[labels.length - 1]);
			return selected >= 0 && selected < choices.size() ? choices.get(selected)
					: ContractChoice.CANCEL;
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
		appendImpactEntries(message, report, language);
		message.append('\n').append(text("Redefine.Legacy.Undo", language));
		return message.toString();
	}

	/**
	 * @return the kernel-authorized operations of a durable-contract change, in
	 *         presentation order; the frontend never derives compatibility itself
	 */
	static List<ContractChoice> availableContractChoices(
			SpatialRedefineAssessment assessment) {
		ArrayList<ContractChoice> choices = new ArrayList<>();
		if (assessment.getStatus()
				== SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE) {
			if (assessment.isIdentityPreservingUpdateAvailable()) {
				choices.add(ContractChoice.RETAIN_IDENTITY);
			}
			if (assessment.isExplicitReplacementAvailable()
					&& hasCompleteImpact(assessment)) {
				choices.add(ContractChoice.EXPLICIT_REPLACEMENT);
			}
		}
		return Collections.unmodifiableList(choices);
	}

	private static SpatialRedefineExecutionMode contractMode(
			SpatialRedefineAssessment assessment, ContractChoice choice) {
		if (choice == null || !availableContractChoices(assessment).contains(choice)) {
			return null;
		}
		if (choice == ContractChoice.RETAIN_IDENTITY) {
			return SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE;
		}
		return choice == ContractChoice.EXPLICIT_REPLACEMENT
				? SpatialRedefineExecutionMode.LEGACY_REPLACEMENT : null;
	}

	/** @return the localized status text followed by the kernel's own reason */
	static String buildUnavailableMessage(SpatialRedefineAssessment assessment,
			String language) {
		StringBuilder message = new StringBuilder(text("Redefine.Status."
				+ assessment.getStatus().name(), language));
		appendKernelReason(message, assessment, language);
		return message.toString();
	}

	/**
	 * @return the durable-contract change explanation, the kernel reason and the
	 *         consequences of every authorized operation, including the complete
	 *         impact report before an explicit replacement can be chosen
	 */
	static String buildContractChangeMessage(SpatialRedefineAssessment assessment,
			String language) {
		List<ContractChoice> choices = availableContractChoices(assessment);
		StringBuilder message = new StringBuilder(
				text("Redefine.Contract.Summary", language));
		appendKernelReason(message, assessment, language);
		if (choices.contains(ContractChoice.RETAIN_IDENTITY)) {
			message.append("\n\n")
					.append(text("Redefine.Contract.RetainExplanation", language));
		}
		if (choices.contains(ContractChoice.EXPLICIT_REPLACEMENT)) {
			message.append("\n\n")
					.append(text("Redefine.Contract.ReplaceExplanation", language))
					.append('\n');
			appendImpactEntries(message, assessment.getImpactReport(), language);
			message.append('\n').append(text("Redefine.Legacy.Undo", language));
		}
		return message.toString();
	}

	private static boolean hasCompleteImpact(SpatialRedefineAssessment assessment) {
		SpatialRedefineImpactReport report = assessment.getImpactReport();
		return report != null && report.getCompleteness()
				== SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE;
	}

	private static void appendKernelReason(StringBuilder message,
			SpatialRedefineAssessment assessment, String language) {
		String detail = assessment.getDetail();
		if (detail != null && !detail.trim().isEmpty()) {
			message.append("\n\n").append(text("Redefine.Detail", language))
					.append(": ").append(detail.trim());
		}
	}

	private static void appendImpactEntries(StringBuilder message,
			SpatialRedefineImpactReport report, String language) {
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
	}

	private static String text(String key, String language) {
		return GeoCeDGProfile.getText(key, language);
	}
}

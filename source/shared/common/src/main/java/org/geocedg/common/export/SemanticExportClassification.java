/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.Collection;
import java.util.List;

import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.export.SourceExportOutcome.SemanticCoverage;

/**
 * PRE-G9B-R6-plus-C (C14, author direction {@code DQ-C5}): the export-layer
 * classification derived from the per-component outcomes of the shared
 * {@link SemanticCurveExportAdapter2D}. It is reporting only: no kernel state.
 *
 * <p>Local admissibility is not global completeness. A certified, valid,
 * deterministic component may be emitted when global completeness is not
 * established, never as a claim of completeness. DXF does not use this model:
 * it keeps the strict G9X1 writability rule.
 */
public final class SemanticExportClassification {

	/** Classification of one semantic source. */
	public enum SourceClass {
		/** at least one component, every component emitted, coverage complete */
		COMPLETE,
		/** a certified component emitted beside incompleteness or a failure */
		INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
		/** nothing of the source can be emitted */
		NO_ADMISSIBLE_OUTPUT
	}

	/** Classification of one export. */
	public enum ExportClass {
		/** every semantic source complete, no failure evidence */
		COMPLETE,
		/** code generated; every emitted semantic component certified */
		INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
		/** no code generated */
		REJECTED_NO_ADMISSIBLE_OUTPUT
	}

	private SemanticExportClassification() {
		// derivation only
	}

	/**
	 * @param outcomes participating per-component outcomes of one source
	 * @return the source classification
	 */
	public static SourceClass classifySource(List<SourceExportOutcome> outcomes) {
		int emitted = 0;
		boolean incomplete = false;
		for (SourceExportOutcome outcome : outcomes) {
			if (outcome.isEmitted()) {
				emitted++;
				if (outcome.getSemanticCoverage()
						== SemanticCoverage.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED) {
					incomplete = true;
				}
			} else {
				incomplete = true;
			}
		}
		if (emitted == 0) {
			return SourceClass.NO_ADMISSIBLE_OUTPUT;
		}
		return incomplete ? SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS
				: SourceClass.COMPLETE;
	}

	/**
	 * Export-integrity failures reject the whole export: the requested
	 * guarantee could not be established, or a source changed during
	 * generation.
	 *
	 * @param outcome one component outcome
	 * @return whether it rejects the export
	 */
	public static boolean rejectsExport(SourceExportOutcome outcome) {
		return outcome.getReason() == Reason.TOLERANCE_NOT_ESTABLISHED
				|| outcome.getReason() == Reason.STALE_SOURCE_REVISION;
	}

	/**
	 * @param sources classifications of every eligible semantic source
	 * @param otherAdmissibleContent whether non-semantic eligible content is
	 *        exported
	 * @param integrityFailure whether an export-integrity failure occurred
	 * @return the export classification
	 */
	public static ExportClass classifyExport(Collection<SourceClass> sources,
			boolean otherAdmissibleContent, boolean integrityFailure) {
		if (integrityFailure) {
			return ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT;
		}
		boolean admissible = otherAdmissibleContent;
		boolean complete = true;
		for (SourceClass source : sources) {
			if (source != SourceClass.NO_ADMISSIBLE_OUTPUT) {
				admissible = true;
			}
			if (source != SourceClass.COMPLETE) {
				complete = false;
			}
		}
		if (complete) {
			return ExportClass.COMPLETE;
		}
		return admissible ? ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS
				: ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT;
	}
}

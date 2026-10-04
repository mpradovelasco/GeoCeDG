/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

/**
 * PRE-G9B-R6-plus-C: a GeoCeDG LaTeX exporter with semantic-curve support, so
 * that its dialog can set the tolerance and show the export report.
 */
public interface LatexSemanticExporter {

	/** @return the exporter's semantic-curve support */
	LatexSemanticExportSupport getSemanticSupport();
}

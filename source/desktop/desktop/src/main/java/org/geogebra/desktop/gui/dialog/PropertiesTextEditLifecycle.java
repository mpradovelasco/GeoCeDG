/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details.
 */

package org.geogebra.desktop.gui.dialog;

import org.geogebra.common.util.AsyncOperation;

/** Routes the embedded Text editor buttons without owning the draft itself. */
final class PropertiesTextEditLifecycle {

	interface DraftEditor {
		void commit(AsyncOperation<Boolean> callback);

		void discard();
	}

	private final DraftEditor editor;
	private final Runnable closeProperties;

	PropertiesTextEditLifecycle(DraftEditor editor, Runnable closeProperties) {
		this.editor = editor;
		this.closeProperties = closeProperties;
	}

	void apply() {
		editor.commit(success -> {
			// Apply intentionally keeps Properties open.
		});
	}

	void ok() {
		editor.commit(success -> {
			if (success) {
				closeProperties.run();
			}
		});
	}

	void cancel() {
		editor.discard();
		closeProperties.run();
	}

	void discardDraft() {
		editor.discard();
	}
}

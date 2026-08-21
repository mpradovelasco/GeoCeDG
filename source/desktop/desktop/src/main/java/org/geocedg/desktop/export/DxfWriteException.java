/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.IOException;

/** Typed paired-output failure including the attempted rollback disposition. */
public final class DxfWriteException extends IOException {
	private static final long serialVersionUID = 1L;

	/** Operation stage at which safe publication failed. */
	public enum Stage {
		PRECHECK,
		PREPARE_DXF,
		PREPARE_MANIFEST,
		VALIDATE_TEMPORARIES,
		BACKUP_EXISTING,
		PROMOTE_DXF,
		PROMOTE_MANIFEST,
		CLEANUP
	}

	private final Stage stage;
	private final boolean rollbackComplete;

	DxfWriteException(Stage stage, String message, Throwable cause,
			boolean rollbackComplete) {
		super(message, cause);
		this.stage = stage;
		this.rollbackComplete = rollbackComplete;
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isRollbackComplete() {
		return rollbackComplete;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

/**
 * PRE-G9B-R6-plus-C (DQ-C7): the per-window owner of the session engineering
 * drawing scale, as the export package sees it. Changing the value is export
 * interpretation only: no undo point, no modified flag, no serialization.
 */
public interface DrawingScaleHolder {

	/** @return current session drawing scale, 1:1 by default */
	DrawingScale getDrawingScale();

	/**
	 * @param scale new session drawing scale
	 */
	void setDrawingScale(DrawingScale scale);

	/**
	 * @param listener presentation listener of export dialogs
	 */
	void addDrawingScaleListener(Runnable listener);

	/**
	 * @param listener presentation listener to remove
	 */
	void removeDrawingScaleListener(Runnable listener);
}

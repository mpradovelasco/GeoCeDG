/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;

import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.StringUtil;

/** Filename-routing policy; archive contents and geometry never derive from it. */
final class GeoCeDGDocumentPolicy {

	private GeoCeDGDocumentPolicy() {
	}

	static boolean isNative(File file) {
		return file != null && FileExtensions.GEOCEDG.equals(
				StringUtil.getFileExtension(file.getName()));
	}

	static boolean isCompatibilityInput(File file) {
		return file != null && FileExtensions.GEOGEBRA.equals(
				StringUtil.getFileExtension(file.getName()));
	}

	static boolean requiresNativeSaveAs(File currentFile) {
		return !isNative(currentFile);
	}

	static FileExtensions[] documentOpenExtensions() {
		return new FileExtensions[] {
				FileExtensions.GEOCEDG, FileExtensions.GEOGEBRA };
	}

	static File nativeSuggestion(File currentFile) {
		if (currentFile == null) {
			return null;
		}
		String name = currentFile.getName();
		int dot;
		while ((dot = name.lastIndexOf('.')) > 0
				&& !FileExtensions.UNKNOWN.equals(
						StringUtil.getFileExtension(name))) {
			name = name.substring(0, dot);
		}
		return new File(currentFile.getParentFile(), name + ".cedg");
	}

	static boolean hasConflictingSuffix(File nativeTarget) {
		if (!isNative(nativeTarget)) {
			return true;
		}
		String name = nativeTarget.getName();
		String stem = name.substring(0, name.length() - ".cedg".length());
		return !FileExtensions.UNKNOWN.equals(
				StringUtil.getFileExtension(stem));
	}

	static File normalizeNativeSuffix(File nativeTarget) {
		if (!isNative(nativeTarget)) {
			return nativeTarget;
		}
		String name = nativeTarget.getName();
		return new File(nativeTarget.getParentFile(),
				name.substring(0, name.length() - ".cedg".length()) + ".cedg");
	}
}

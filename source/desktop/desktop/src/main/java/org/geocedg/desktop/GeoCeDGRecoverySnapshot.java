/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.io.DocumentArchivePreflight;

/**
 * PRE-G9B-R6-plus-F3 verified native recovery snapshot (recovery design addendum
 * successor section 3, audit gate G1): the complete native archive of the document as
 * it was before an insertion, written through the public native writer. It is never
 * the undo archive, never deleted by an unconditional cleanup and never written over
 * the current file. Its owner is the insertion until a terminal outcome, then either
 * nobody (released) or the isolation controller, which releases it only after the
 * user's exit completed.
 */
final class GeoCeDGRecoverySnapshot {
	/** Lifecycle states. */
	enum State {
		/** archive written, validated and identity-checked */
		VERIFIED_HELD,
		/** ownership moved to the isolation controller */
		RETAINED_FOR_ISOLATION,
		/** bytes discarded */
		RELEASED
	}

	private static final String MAIN_XML = "geogebra.xml";
	private static final Pattern DOCUMENT_ID = Pattern
			.compile("<geogebra\\s[^>]*\\bid=\"([^\"]*)\"");

	private final String operationId;
	private final Instant capturedAt;
	private final String documentId;
	private final File currentFile;
	private final boolean saved;
	private byte[] archive;
	private State state;

	private GeoCeDGRecoverySnapshot(String operationId, byte[] archive, String documentId,
			File currentFile, boolean saved) {
		this.operationId = operationId;
		this.capturedAt = Instant.now();
		this.archive = archive;
		this.documentId = documentId;
		this.currentFile = currentFile;
		this.saved = saved;
		this.state = State.VERIFIED_HELD;
	}

	/**
	 * Captures and verifies the complete native archive of the live document. A ZIP
	 * written without exception is not yet verified: the bytes must pass the native
	 * preflight and carry the live document identity.
	 *
	 * @param app document
	 * @param operationId insertion operation id
	 * @return the verified snapshot
	 * @throws SnapshotException when capture or validation fails
	 */
	static GeoCeDGRecoverySnapshot capture(AppGeoCeDG app, String operationId)
			throws SnapshotException {
		byte[] bytes;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			app.getXMLio().writeGeoGebraFile(out, false);
			bytes = out.toByteArray();
		} catch (IOException | RuntimeException failure) {
			throw new SnapshotException("native archive could not be written", failure);
		}
		String liveId = app.getUniqueId();
		verify(bytes, app, liveId);
		return new GeoCeDGRecoverySnapshot(operationId, bytes, liveId, app.getCurrentFile(),
				app.isSaved());
	}

	/**
	 * @param bytes native archive
	 * @param app document whose product configuration validates it
	 * @param expectedId document identity the archive must carry
	 * @throws SnapshotException when the archive is not a loadable native document with
	 *         that identity
	 */
	static void verify(byte[] bytes, AppGeoCeDG app, String expectedId)
			throws SnapshotException {
		boolean loadable;
		try {
			loadable = DocumentArchivePreflight.validate(bytes,
					app.createDocumentPreflightConfig());
		} catch (Exception failure) {
			throw new SnapshotException("native archive failed the preflight", failure);
		}
		if (!loadable) {
			throw new SnapshotException("native archive failed the preflight", null);
		}
		String archivedId = documentId(bytes);
		if (expectedId == null || !expectedId.equals(archivedId)) {
			throw new SnapshotException("native archive identity " + archivedId
					+ " differs from " + expectedId, null);
		}
	}

	/**
	 * @param bytes native archive
	 * @return the root document identity of its main XML, or null
	 */
	static String documentId(byte[] bytes) {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (MAIN_XML.equals(entry.getName())) {
					String xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
					Matcher matcher = DOCUMENT_ID.matcher(xml);
					return matcher.find() ? matcher.group(1) : null;
				}
			}
			return null;
		} catch (IOException failure) {
			return null;
		}
	}

	/**
	 * Writes the held archive to a new path chosen by the user and re-validates the
	 * published bytes. The caller has already refused the current file and confirmed
	 * any other existing file.
	 *
	 * @param target destination
	 * @param app document whose product configuration validates the copy
	 * @throws SnapshotException when the snapshot is released or the copy is invalid
	 * @throws IOException when writing or publication fails
	 */
	void writeCopy(Path target, AppGeoCeDG app) throws SnapshotException, IOException {
		byte[] bytes = getArchive();
		AtomicDocumentFileWriter.write(target, temporary -> Files.write(temporary, bytes));
		byte[] published = Files.readAllBytes(target);
		if (!Arrays.equals(published, bytes)) {
			throw new SnapshotException("published copy differs from the snapshot", null);
		}
		verify(published, app, documentId);
	}

	/** @return a copy of the held archive */
	synchronized byte[] getArchive() throws SnapshotException {
		if (state == State.RELEASED) {
			throw new SnapshotException("snapshot already released", null);
		}
		return archive.clone();
	}

	/** Moves ownership to the isolation controller. */
	synchronized void retainForIsolation() {
		if (state == State.RELEASED) {
			throw new IllegalStateException("snapshot already released");
		}
		state = State.RETAINED_FOR_ISOLATION;
	}

	/** Discards the bytes; terminal. */
	synchronized void release() {
		archive = null;
		state = State.RELEASED;
	}

	/** @return lifecycle state */
	synchronized State getState() {
		return state;
	}

	/** @return operation that captured the snapshot */
	String getOperationId() {
		return operationId;
	}

	/** @return capture time */
	Instant getCapturedAt() {
		return capturedAt;
	}

	/** @return document identity in the archive (equal to the live one at capture) */
	String getDocumentId() {
		return documentId;
	}

	/** @return current file at capture, or null */
	File getCurrentFile() {
		return currentFile;
	}

	/** @return saved flag at capture */
	boolean wasSaved() {
		return saved;
	}

	/** Capture, validation or copy failure of the native recovery snapshot. */
	static final class SnapshotException extends Exception {
		private static final long serialVersionUID = 1L;

		SnapshotException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}

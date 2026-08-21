/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.geocedg.desktop.export.DxfWriteException.Stage;
import org.geocedg.desktop.export.DxfWriteResult.ArtifactKind;

/**
 * Publishes a DXF and, when required, its fidelity manifest through
 * same-directory temporary files. Each promotion uses a no-replace filesystem
 * move. Two-file atomicity is not claimed: a failed second promotion is
 * compensated by deterministic rollback and restoration.
 */
public final class DxfPairedOutputWriter {

	/** Explicit handling of either occupied destination. */
	public enum CollisionPolicy {
		/** Fail before creating a temporary file. */
		FAIL_IF_EXISTS,
		/** Back up occupied destinations and restore them after a failure. */
		REPLACE_EXISTING
	}

	private static final String MANIFEST_SUFFIX = ".manifest.json";
	private final DxfFileOperations files;

	/** Creates a writer backed by the local NIO filesystem. */
	public DxfPairedOutputWriter() {
		this(new NioDxfFileOperations());
	}

	DxfPairedOutputWriter(DxfFileOperations files) {
		this.files = Objects.requireNonNull(files);
	}

	/**
	 * Safely publishes prepared bytes to the requested DXF destination.
	 * An exact-only replacement also removes an old paired manifest so stale
	 * fidelity metadata can never describe the newly written DXF.
	 *
	 * @param requestedDxfPath final DXF destination
	 * @param output fully serialized and internally consistent artifacts
	 * @param collisionPolicy explicit collision disposition
	 * @return the successfully published final state
	 * @throws DxfWriteException on precheck, preparation, promotion, rollback,
	 *         or cleanup failure
	 */
	public DxfWriteResult write(Path requestedDxfPath, DxfPreparedOutput output,
			CollisionPolicy collisionPolicy) throws DxfWriteException {
		Objects.requireNonNull(requestedDxfPath, "DXF path is required");
		Objects.requireNonNull(output, "Prepared output is required");
		Objects.requireNonNull(collisionPolicy, "Collision policy is required");
		Path dxfPath;
		try {
			dxfPath = requestedDxfPath.toAbsolutePath().normalize();
		} catch (RuntimeException exception) {
			throw failure(Stage.PRECHECK, "Cannot resolve the DXF destination",
					exception, true);
		}
		Path directory = dxfPath.getParent();
		if (directory == null || dxfPath.getFileName() == null) {
			throw failure(Stage.PRECHECK, "DXF destination has no parent directory",
					null, true);
		}
		Path manifestPath = manifestPath(dxfPath);
		boolean hadDxf;
		boolean hadManifest;
		String originalDxfHash;
		String originalManifestHash;
		try {
			hadDxf = files.exists(dxfPath);
			hadManifest = files.exists(manifestPath);
			assertRegularDestination(dxfPath, hadDxf, "DXF");
			assertRegularDestination(manifestPath, hadManifest, "manifest");
			originalDxfHash = hadDxf ? hash(dxfPath) : null;
			originalManifestHash = hadManifest ? hash(manifestPath) : null;
		} catch (IOException | RuntimeException exception) {
			throw failure(Stage.PRECHECK, "Cannot inspect output destinations",
					exception, true);
		}
		if (collisionPolicy == CollisionPolicy.FAIL_IF_EXISTS
				&& (hadDxf || hadManifest)) {
			throw failure(Stage.PRECHECK,
					"DXF or paired-manifest destination already exists", null, true);
		}

		PublicationState state = new PublicationState(dxfPath, manifestPath,
				directory, hadDxf, hadManifest, originalDxfHash,
				originalManifestHash, output.getDxfSha256(),
				output.hasManifest()
						? DxfHashing.sha256(output.getManifest().getBytes()) : null);
		Stage stage = Stage.PREPARE_DXF;
		try {
			state.dxfTemporary = createTemporary(state, ".dxf.tmp");
			files.write(state.dxfTemporary, output.getDxfBytes());
			if (output.hasManifest()) {
				stage = Stage.PREPARE_MANIFEST;
				state.manifestTemporary = createTemporary(state, ".json.tmp");
				files.write(state.manifestTemporary,
						output.getManifest().getBytes());
			}

			stage = Stage.VALIDATE_TEMPORARIES;
			validateTemporary(state.dxfTemporary, output.getDxfBytes(),
					output.getDxfSha256(), "DXF");
			if (output.hasManifest()) {
				validateTemporary(state.manifestTemporary,
						output.getManifest().getBytes(), null, "manifest");
			}
			output.requireSourceRevisionCurrent();

			if (collisionPolicy == CollisionPolicy.REPLACE_EXISTING) {
				stage = Stage.BACKUP_EXISTING;
				backupExisting(state);
			}

			output.requireSourceRevisionCurrent();
			stage = Stage.PROMOTE_DXF;
			promoteDxf(state);
			if (output.hasManifest()) {
				output.requireSourceRevisionCurrent();
				stage = Stage.PROMOTE_MANIFEST;
				promoteManifest(state);
			}

			stage = Stage.CLEANUP;
			cleanupBackups(state);
			ArtifactKind kind = output.hasManifest()
					? ArtifactKind.DXF_AND_MANIFEST : ArtifactKind.EXACT_DXF;
			return new DxfWriteResult(kind, dxfPath,
					output.hasManifest() ? manifestPath : null,
					output.getDxfSha256(), hadDxf, hadManifest);
		} catch (IOException | RuntimeException exception) {
			boolean rollbackComplete = stage == Stage.CLEANUP
					? cleanupUnpublishedTemporaries(state, exception)
					: rollback(state, exception);
			String message = stage == Stage.CLEANUP
					? "DXF output published but cleanup failed"
					: "DXF output publication failed";
			throw failure(stage, message, exception, rollbackComplete);
		}
	}

	/** @return deterministic sidecar path paired with a DXF destination */
	public static Path manifestPath(Path dxfPath) {
		Objects.requireNonNull(dxfPath, "DXF path is required");
		Path fileName = dxfPath.getFileName();
		if (fileName == null) {
			throw new IllegalArgumentException("DXF path has no file name");
		}
		return dxfPath.resolveSibling(fileName + MANIFEST_SUFFIX);
	}

	private Path createTemporary(PublicationState state, String suffix)
			throws IOException {
		String name = state.dxfPath.getFileName().toString();
		return files.createTempFile(state.directory,
				"." + name + ".geocedg-", suffix);
	}

	private void validateTemporary(Path path, byte[] expected,
			String expectedHash, String description) throws IOException {
		byte[] actual = files.read(path);
		if (!Arrays.equals(expected, actual)) {
			throw new IOException(description + " temporary bytes changed");
		}
		if (expectedHash != null
				&& !expectedHash.equals(DxfHashing.sha256(actual))) {
			throw new IOException(description + " temporary hash changed");
		}
	}

	private void backupExisting(PublicationState state) throws IOException {
		if (state.hadDxf) {
			assertHash(state.dxfPath, state.originalDxfHash,
					"DXF destination changed before backup");
			state.dxfBackup = createTemporary(state, ".dxf.backup");
			clearBackupReservation(state.dxfBackup);
			try {
				state.dxfBackupDisposition = moveAndReconcile(state.dxfPath,
						state.dxfBackup, state.originalDxfHash, "DXF backup");
			} catch (ReconciledMoveException exception) {
				state.dxfBackupDisposition = exception.disposition;
				throw exception;
			}
		}
		if (state.hadManifest) {
			assertHash(state.manifestPath, state.originalManifestHash,
					"Manifest destination changed before backup");
			state.manifestBackup = createTemporary(state, ".json.backup");
			clearBackupReservation(state.manifestBackup);
			try {
				state.manifestBackupDisposition = moveAndReconcile(
						state.manifestPath, state.manifestBackup,
						state.originalManifestHash, "manifest backup");
			} catch (ReconciledMoveException exception) {
				state.manifestBackupDisposition = exception.disposition;
				throw exception;
			}
		}
	}

	private void promoteDxf(PublicationState state) throws IOException {
		assertDestinationAbsent(state.dxfPath, "DXF");
		try {
			state.dxfPromotionDisposition = moveAndReconcile(state.dxfTemporary,
					state.dxfPath, state.expectedDxfHash, "DXF promotion");
		} catch (ReconciledMoveException exception) {
			state.dxfPromotionDisposition = exception.disposition;
			if (exception.disposition == MoveDisposition.MOVED) {
				state.dxfTemporary = null;
			}
			throw exception;
		}
		if (state.dxfPromotionDisposition == MoveDisposition.MOVED) {
			state.dxfTemporary = null;
		}
	}

	private void promoteManifest(PublicationState state) throws IOException {
		assertDestinationAbsent(state.manifestPath, "manifest");
		try {
			state.manifestPromotionDisposition = moveAndReconcile(
					state.manifestTemporary, state.manifestPath,
					state.expectedManifestHash, "manifest promotion");
		} catch (ReconciledMoveException exception) {
			state.manifestPromotionDisposition = exception.disposition;
			if (exception.disposition == MoveDisposition.MOVED) {
				state.manifestTemporary = null;
			}
			throw exception;
		}
		if (state.manifestPromotionDisposition == MoveDisposition.MOVED) {
			state.manifestTemporary = null;
		}
	}

	private void clearBackupReservation(Path backup) throws IOException {
		if (!files.deleteIfExists(backup)) {
			throw new IOException("Cannot reserve an empty backup path");
		}
	}

	private MoveDisposition moveAndReconcile(Path source, Path target,
			String expectedHash, String description) throws IOException {
		try {
			files.move(source, target);
		} catch (IOException | RuntimeException exception) {
			MoveDisposition disposition = reconcileMove(source, target,
					expectedHash);
			throw new ReconciledMoveException(description + " failed",
					exception, disposition);
		}
		MoveDisposition disposition = reconcileMove(source, target, expectedHash);
		if (disposition != MoveDisposition.MOVED) {
			throw new ReconciledMoveException(
					description + " did not produce the expected filesystem state",
					null, disposition);
		}
		return disposition;
	}

	private MoveDisposition reconcileMove(Path source, Path target,
			String expectedHash) {
		try {
			boolean sourceExists = files.exists(source);
			boolean targetExists = files.exists(target);
			boolean sourceMatches = sourceExists
					&& expectedHash.equals(hash(source));
			boolean targetMatches = targetExists
					&& expectedHash.equals(hash(target));
			if (!sourceExists && targetMatches) {
				return MoveDisposition.MOVED;
			}
			if (sourceMatches && !targetExists) {
				return MoveDisposition.NOT_MOVED;
			}
			if (sourceMatches && targetMatches) {
				return MoveDisposition.DUPLICATED;
			}
			return MoveDisposition.AMBIGUOUS;
		} catch (IOException | RuntimeException exception) {
			return MoveDisposition.AMBIGUOUS;
		}
	}

	private void assertDestinationAbsent(Path path, String description)
			throws IOException {
		if (files.exists(path)) {
			throw new IOException(description
					+ " destination appeared after collision precheck");
		}
	}

	private void assertRegularDestination(Path path, boolean exists,
			String description) throws IOException {
		if (exists && !files.isRegularFile(path)) {
			throw new IOException(description + " destination is not a regular file");
		}
	}

	private void assertHash(Path path, String expectedHash, String message)
			throws IOException {
		if (!files.exists(path) || !expectedHash.equals(hash(path))) {
			throw new IOException(message);
		}
	}

	private String hash(Path path) throws IOException {
		return DxfHashing.sha256(files.read(path));
	}

	private void cleanupBackups(PublicationState state) throws IOException {
		List<Throwable> failures = new ArrayList<>();
		if (state.dxfBackup != null && deleteIfMatches(state.dxfBackup,
				state.originalDxfHash, "DXF backup", failures)) {
			state.dxfBackup = null;
			state.dxfBackupDisposition = MoveDisposition.NOT_MOVED;
		}
		if (state.manifestBackup != null && deleteIfMatches(state.manifestBackup,
				state.originalManifestHash, "manifest backup", failures)) {
			state.manifestBackup = null;
			state.manifestBackupDisposition = MoveDisposition.NOT_MOVED;
		}
		if (!failures.isEmpty()) {
			IOException failure = new IOException(
					"Published output is valid but backup cleanup failed");
			for (Throwable suppressed : failures) {
				failure.addSuppressed(suppressed);
			}
			throw failure;
		}
	}

	private boolean rollback(PublicationState state, Throwable primary) {
		List<Throwable> failures = new ArrayList<>();
		rollbackPromotion(state.manifestTemporary, state.manifestPath,
				state.expectedManifestHash, state.manifestPromotionDisposition,
				"manifest", failures);
		rollbackPromotion(state.dxfTemporary, state.dxfPath,
				state.expectedDxfHash, state.dxfPromotionDisposition, "DXF",
				failures);
		state.manifestBackupDisposition = rollbackBackup(state.manifestPath,
				state.manifestBackup, state.originalManifestHash,
				state.manifestBackupDisposition, "manifest", failures);
		if (state.manifestBackupDisposition == MoveDisposition.NOT_MOVED
				&& state.originalManifestHash != null
				&& matches(state.manifestPath, state.originalManifestHash)) {
			state.manifestBackup = null;
		}
		state.dxfBackupDisposition = rollbackBackup(state.dxfPath,
				state.dxfBackup, state.originalDxfHash,
				state.dxfBackupDisposition, "DXF", failures);
		if (state.dxfBackupDisposition == MoveDisposition.NOT_MOVED
				&& state.originalDxfHash != null
				&& matches(state.dxfPath, state.originalDxfHash)) {
			state.dxfBackup = null;
		}
		delete(state.dxfTemporary, failures);
		delete(state.manifestTemporary, failures);
		for (Throwable failure : failures) {
			primary.addSuppressed(failure);
		}
		return failures.isEmpty()
				&& state.dxfBackupDisposition != MoveDisposition.AMBIGUOUS
				&& state.manifestBackupDisposition != MoveDisposition.AMBIGUOUS;
	}

	private boolean cleanupUnpublishedTemporaries(PublicationState state,
			Throwable primary) {
		List<Throwable> failures = new ArrayList<>();
		delete(state.dxfTemporary, failures);
		delete(state.manifestTemporary, failures);
		for (Throwable failure : failures) {
			primary.addSuppressed(failure);
		}
		return failures.isEmpty() && state.dxfBackup == null
				&& state.manifestBackup == null;
	}

	private void rollbackPromotion(Path temporary, Path destination,
			String expectedHash, MoveDisposition disposition, String description,
			List<Throwable> failures) {
		if (expectedHash == null || disposition == null
				|| disposition == MoveDisposition.NOT_MOVED) {
			return;
		}
		MoveDisposition current = disposition == MoveDisposition.AMBIGUOUS
				&& temporary != null
						? reconcileMove(temporary, destination, expectedHash)
						: disposition;
		if (current == MoveDisposition.MOVED
				|| current == MoveDisposition.DUPLICATED) {
			deleteIfMatches(destination, expectedHash, description, failures);
		} else if (current == MoveDisposition.AMBIGUOUS) {
			if (!matches(destination, expectedHash)
					|| !deleteIfMatches(destination, expectedHash,
							description, failures)) {
				failures.add(new IOException(description
						+ " promotion state is ambiguous; files were preserved"));
			}
		}
	}

	private MoveDisposition rollbackBackup(Path destination, Path backup,
			String originalHash, MoveDisposition disposition, String description,
			List<Throwable> failures) {
		if (originalHash == null || disposition == null) {
			return MoveDisposition.NOT_MOVED;
		}
		MoveDisposition current = disposition == MoveDisposition.AMBIGUOUS
				? reconcileMove(destination, backup, originalHash) : disposition;
		if (current == MoveDisposition.NOT_MOVED) {
			if (!matches(destination, originalHash)) {
				failures.add(new IOException(description
						+ " original destination could not be verified"));
				return MoveDisposition.AMBIGUOUS;
			}
			return MoveDisposition.NOT_MOVED;
		}
		if (current == MoveDisposition.DUPLICATED) {
			if (matches(destination, originalHash)
					&& deleteIfMatches(backup, originalHash,
							description + " duplicate backup", failures)) {
				return MoveDisposition.NOT_MOVED;
			}
			failures.add(new IOException(description
					+ " duplicated backup could not be reconciled"));
			return MoveDisposition.AMBIGUOUS;
		}
		if (current == MoveDisposition.AMBIGUOUS) {
			failures.add(new IOException(description
					+ " backup state is ambiguous; backup was preserved"));
			return current;
		}
		boolean destinationExists;
		try {
			destinationExists = files.exists(destination);
		} catch (RuntimeException exception) {
			failures.add(exception);
			return MoveDisposition.AMBIGUOUS;
		}
		if (destinationExists) {
			boolean backupExists;
			try {
				backupExists = files.exists(backup);
			} catch (RuntimeException exception) {
				failures.add(exception);
				return MoveDisposition.AMBIGUOUS;
			}
			if (matches(destination, originalHash) && !backupExists) {
				return MoveDisposition.NOT_MOVED;
			}
			failures.add(new IOException(description
					+ " destination is occupied during restore"));
			return MoveDisposition.AMBIGUOUS;
		}
		try {
			MoveDisposition restored = moveAndReconcile(backup, destination,
					originalHash, description + " restore");
			return restored == MoveDisposition.MOVED
					? MoveDisposition.NOT_MOVED : restored;
		} catch (ReconciledMoveException exception) {
			if (exception.disposition == MoveDisposition.MOVED
					&& matches(destination, originalHash)) {
				return MoveDisposition.NOT_MOVED;
			}
			if (exception.disposition == MoveDisposition.DUPLICATED
					&& matches(destination, originalHash)
					&& deleteIfMatches(backup, originalHash,
							description + " duplicate restore", failures)) {
				return MoveDisposition.NOT_MOVED;
			}
			failures.add(exception);
			return exception.disposition;
		} catch (IOException exception) {
			failures.add(exception);
			return MoveDisposition.AMBIGUOUS;
		} catch (RuntimeException exception) {
			failures.add(exception);
			return MoveDisposition.AMBIGUOUS;
		}
	}

	private boolean deleteIfMatches(Path path, String expectedHash,
			String description, List<Throwable> failures) {
		try {
			if (!files.exists(path)) {
				return true;
			}
		} catch (RuntimeException exception) {
			failures.add(exception);
			return false;
		}
		if (!matches(path, expectedHash)) {
			failures.add(new IOException(description
					+ " destination changed concurrently; it was preserved"));
			return false;
		}
		return delete(path, failures);
	}

	private boolean matches(Path path, String expectedHash) {
		try {
			return expectedHash != null && files.exists(path)
					&& expectedHash.equals(hash(path));
		} catch (IOException | RuntimeException exception) {
			return false;
		}
	}

	private boolean delete(Path path, List<Throwable> failures) {
		if (path == null) {
			return true;
		}
		try {
			files.deleteIfExists(path);
			return true;
		} catch (IOException | RuntimeException exception) {
			failures.add(exception);
			return false;
		}
	}

	private static DxfWriteException failure(Stage stage, String message,
			Throwable cause, boolean rollbackComplete) {
		return new DxfWriteException(stage, message, cause, rollbackComplete);
	}

	private enum MoveDisposition {
		NOT_MOVED,
		MOVED,
		DUPLICATED,
		AMBIGUOUS
	}

	private static final class ReconciledMoveException extends IOException {
		private static final long serialVersionUID = 1L;
		private final MoveDisposition disposition;

		private ReconciledMoveException(String message, Throwable cause,
				MoveDisposition disposition) {
			super(message, cause);
			this.disposition = disposition;
		}
	}

	private static final class PublicationState {
		private final Path dxfPath;
		private final Path manifestPath;
		private final Path directory;
		private final boolean hadDxf;
		private final boolean hadManifest;
		private final String originalDxfHash;
		private final String originalManifestHash;
		private final String expectedDxfHash;
		private final String expectedManifestHash;
		private Path dxfTemporary;
		private Path manifestTemporary;
		private Path dxfBackup;
		private Path manifestBackup;
		private MoveDisposition dxfBackupDisposition;
		private MoveDisposition manifestBackupDisposition;
		private MoveDisposition dxfPromotionDisposition;
		private MoveDisposition manifestPromotionDisposition;

		private PublicationState(Path dxfPath, Path manifestPath,
				Path directory, boolean hadDxf, boolean hadManifest,
				String originalDxfHash, String originalManifestHash,
				String expectedDxfHash, String expectedManifestHash) {
			this.dxfPath = dxfPath;
			this.manifestPath = manifestPath;
			this.directory = directory;
			this.hadDxf = hadDxf;
			this.hadManifest = hadManifest;
			this.originalDxfHash = originalDxfHash;
			this.originalManifestHash = originalManifestHash;
			this.expectedDxfHash = expectedDxfHash;
			this.expectedManifestHash = expectedManifestHash;
		}
	}
}

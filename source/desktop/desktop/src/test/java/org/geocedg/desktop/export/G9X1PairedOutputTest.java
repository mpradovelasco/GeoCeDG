/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.desktop.export.DxfPairedOutputWriter.CollisionPolicy;
import org.geocedg.desktop.export.DxfWriteException.Stage;
import org.geocedg.desktop.export.DxfWriteResult.ArtifactKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G9X1PairedOutputTest {

	private static final byte[] EXACT_DXF = ascii("0\r\nEOF\r\n");
	private static final byte[] APPROXIMATE_DXF = ascii(
			"999\r\nFidelity sidecar required\r\n0\r\nEOF\r\n");
	private static final byte[] OLD_DXF = ascii("0\r\nOLD-DXF\r\n");
	private static final byte[] OLD_MANIFEST = ascii("old manifest\n");

	@TempDir
	Path temporaryDirectory;

	@Test
	void d01CollisionChecksBothFinalDestinations() throws IOException { // X1-D01
		Path dxf = temporaryDirectory.resolve("d01.dxf");
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		DxfPairedOutputWriter writer = new DxfPairedOutputWriter();

		Files.write(dxf, OLD_DXF);
		DxfWriteException dxfCollision = assertThrows(DxfWriteException.class,
				() -> writer.write(dxf, exactOutput(),
						CollisionPolicy.FAIL_IF_EXISTS));
		assertEquals(Stage.PRECHECK, dxfCollision.getStage());
		assertTrue(dxfCollision.isRollbackComplete());
		assertArrayEquals(OLD_DXF, Files.readAllBytes(dxf));

		Files.delete(dxf);
		Files.write(manifest, OLD_MANIFEST);
		DxfWriteException manifestCollision = assertThrows(
				DxfWriteException.class, () -> writer.write(dxf, pairedOutput(),
						CollisionPolicy.FAIL_IF_EXISTS));
		assertEquals(Stage.PRECHECK, manifestCollision.getStage());
		assertTrue(manifestCollision.isRollbackComplete());
		assertFalse(Files.exists(dxf));
		assertArrayEquals(OLD_MANIFEST, Files.readAllBytes(manifest));
		assertNoTemporaryFiles(temporaryDirectory);
	}

	@Test
	void d02PreparesTemporaryFilesInTheTargetDirectory() throws Exception { // X1-D02
		Path dxf = temporaryDirectory.resolve("d02.dxf").toAbsolutePath();
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		DxfWriteResult result = new DxfPairedOutputWriter(files).write(dxf,
				pairedOutput(), CollisionPolicy.FAIL_IF_EXISTS);

		assertEquals(ArtifactKind.DXF_AND_MANIFEST, result.getArtifactKind());
		assertEquals(2, files.createdDirectories.size());
		for (Path directory : files.createdDirectories) {
			assertEquals(dxf.getParent(), directory);
		}
		assertArrayEquals(APPROXIMATE_DXF, files.bytes(dxf));
		assertArrayEquals(pairedOutput().getManifest().getBytes(),
				files.bytes(DxfPairedOutputWriter.manifestPath(dxf)));
		assertFalse(files.hasTemporaryArtifacts());
	}

	@Test
	void d03SafelyPromotesExactDxfAndRemovesStaleSidecar() throws Exception { // X1-D03
		Path dxf = temporaryDirectory.resolve("d03.dxf");
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		DxfPairedOutputWriter writer = new DxfPairedOutputWriter();

		DxfWriteResult created = writer.write(dxf, exactOutput(),
				CollisionPolicy.FAIL_IF_EXISTS);
		assertEquals(ArtifactKind.EXACT_DXF, created.getArtifactKind());
		assertArrayEquals(EXACT_DXF, Files.readAllBytes(dxf));
		assertFalse(Files.exists(manifest));

		Files.write(dxf, OLD_DXF);
		Files.write(manifest, OLD_MANIFEST);
		DxfWriteResult replaced = writer.write(dxf, exactOutput(),
				CollisionPolicy.REPLACE_EXISTING);

		assertTrue(replaced.isDxfReplaced());
		assertFalse(replaced.isManifestReplaced());
		assertTrue(replaced.isPreviousManifestRemoved());
		assertArrayEquals(EXACT_DXF, Files.readAllBytes(dxf));
		assertFalse(Files.exists(manifest));
		assertNoTemporaryFiles(temporaryDirectory);
	}

	@Test
	void d04ValidatesPairBytesSchemaAndHashBeforePromotion() throws Exception { // X1-D04
		DxfPreparedOutput output = pairedOutput();
		String manifestText = new String(output.getManifest().getBytes(),
				StandardCharsets.UTF_8);
		assertTrue(manifestText.startsWith(
				"{\"schema\":\"org.geocedg.dxf.fidelity-manifest\","));
		assertTrue(manifestText.contains(
				"\"sha256\":\"" + output.getDxfSha256() + "\""));
		assertThrows(IllegalArgumentException.class,
				() -> new DxfManifestEncoding(ascii("{}\n"),
						output.getDxfSha256()));
		assertThrows(IllegalArgumentException.class, () -> DxfPreparedOutput
				.paired(EXACT_DXF, output.getManifest()));

		Path dxf = temporaryDirectory.resolve("d04.dxf").toAbsolutePath();
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.corruptTemporaryReadSuffix = ".json.tmp";
		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, output,
						CollisionPolicy.FAIL_IF_EXISTS));

		assertEquals(Stage.VALIDATE_TEMPORARIES, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertFalse(files.exists(dxf));
		assertFalse(files.exists(DxfPairedOutputWriter.manifestPath(dxf)));
		assertFalse(files.hasTemporaryArtifacts());
	}

	@Test
	void d05CleansUpAfterDxfPromotionFailureAndLateCollision() throws Exception { // X1-D05
		Path beforeDxf = temporaryDirectory.resolve("d05-before.dxf")
				.toAbsolutePath();
		InMemoryFileOperations before = new InMemoryFileOperations(beforeDxf);
		before.addFault(MoveRole.PROMOTE_DXF, FaultTiming.BEFORE);
		DxfWriteException injected = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(before).write(beforeDxf,
						pairedOutput(), CollisionPolicy.FAIL_IF_EXISTS));
		assertEquals(Stage.PROMOTE_DXF, injected.getStage());
		assertTrue(injected.isRollbackComplete());
		assertFalse(before.exists(beforeDxf));
		assertFalse(before.hasTemporaryArtifacts());

		Path collisionDxf = temporaryDirectory.resolve("d05-collision.dxf")
				.toAbsolutePath();
		InMemoryFileOperations collision = new InMemoryFileOperations(
				collisionDxf);
		collision.collisionAfterPrecheck = OLD_DXF;
		DxfWriteException raced = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(collision).write(collisionDxf,
						pairedOutput(), CollisionPolicy.FAIL_IF_EXISTS));

		assertEquals(Stage.PROMOTE_DXF, raced.getStage());
		assertTrue(raced.isRollbackComplete());
		assertArrayEquals(OLD_DXF, collision.bytes(collisionDxf));
		assertFalse(collision.exists(
				DxfPairedOutputWriter.manifestPath(collisionDxf)));
		assertFalse(collision.hasTemporaryArtifacts());
	}

	@Test
	void d06RollsBackAfterManifestPromotionFailure() throws Exception { // X1-D06
		Path dxf = temporaryDirectory.resolve("d06.dxf").toAbsolutePath();
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.addFault(MoveRole.PROMOTE_MANIFEST, FaultTiming.BEFORE);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.FAIL_IF_EXISTS));

		assertEquals(Stage.PROMOTE_MANIFEST, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertFalse(files.exists(dxf));
		assertFalse(files.exists(DxfPairedOutputWriter.manifestPath(dxf)));
		assertFalse(files.hasTemporaryArtifacts());
	}

	@Test
	void d07RestoresTheExactPreOperationStateOnRollback() throws Exception { // X1-D07
		Path dxf = temporaryDirectory.resolve("d07.dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.put(dxf, OLD_DXF);
		files.put(manifest, OLD_MANIFEST);
		files.addFault(MoveRole.PROMOTE_MANIFEST, FaultTiming.BEFORE);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.PROMOTE_MANIFEST, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	@Test
	void d08ReconcilesSuccessThenThrowFilesystemFailures() { // X1-D08
		assertAll(
				() -> assertBackupThrowBeforeKeepsOriginal("d08-backup-before"),
				() -> assertRestoredAfterSuccessThenThrowBackup(
						"d08-backup-after"),
				() -> assertRestoredAfterCopyThenThrowBackup(
						"d08-backup-duplicated"),
				() -> assertCleanAfterSuccessThenThrowPromotion(
						"d08-dxf-after", MoveRole.PROMOTE_DXF),
				() -> assertCleanAfterSuccessThenThrowPromotion(
						"d08-manifest-after", MoveRole.PROMOTE_MANIFEST),
				() -> assertCleanAfterCopyThenThrowPromotion(
						"d08-dxf-duplicated", MoveRole.PROMOTE_DXF),
				() -> assertCleanAfterCopyThenThrowPromotion(
						"d08-manifest-duplicated", MoveRole.PROMOTE_MANIFEST),
				() -> assertRestoredAfterSuccessThenThrowRestore(
						"d08-restore-after"),
				() -> assertRestoredAfterCopyThenThrowRestore(
						"d08-restore-duplicated"));
	}

	private void assertBackupThrowBeforeKeepsOriginal(String name)
			throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = existingPair(dxf);
		files.addFault(MoveRole.BACKUP_DXF, FaultTiming.BEFORE);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.BACKUP_EXISTING, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertRestoredAfterSuccessThenThrowBackup(String name)
			throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = existingPair(dxf);
		files.addFault(MoveRole.BACKUP_DXF, FaultTiming.AFTER);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.BACKUP_EXISTING, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertRestoredAfterCopyThenThrowBackup(String name)
			throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = existingPair(dxf);
		files.addFault(MoveRole.BACKUP_DXF, FaultTiming.COPY_THEN_THROW);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.BACKUP_EXISTING, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertCleanAfterSuccessThenThrowPromotion(String name,
			MoveRole role) throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.addFault(role, FaultTiming.AFTER);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.FAIL_IF_EXISTS));

		Stage expected = role == MoveRole.PROMOTE_DXF
				? Stage.PROMOTE_DXF : Stage.PROMOTE_MANIFEST;
		assertEquals(expected, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertFalse(files.exists(dxf));
		assertFalse(files.exists(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertCleanAfterCopyThenThrowPromotion(String name,
			MoveRole role) throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.addFault(role, FaultTiming.COPY_THEN_THROW);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.FAIL_IF_EXISTS));

		Stage expected = role == MoveRole.PROMOTE_DXF
				? Stage.PROMOTE_DXF : Stage.PROMOTE_MANIFEST;
		assertEquals(expected, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertFalse(files.exists(dxf));
		assertFalse(files.exists(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertRestoredAfterSuccessThenThrowRestore(String name)
			throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = existingPair(dxf);
		files.addFault(MoveRole.PROMOTE_MANIFEST, FaultTiming.BEFORE);
		files.addFault(MoveRole.RESTORE_MANIFEST, FaultTiming.AFTER);
		files.addFault(MoveRole.RESTORE_DXF, FaultTiming.AFTER);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.PROMOTE_MANIFEST, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private void assertRestoredAfterCopyThenThrowRestore(String name)
			throws Exception {
		Path dxf = temporaryDirectory.resolve(name + ".dxf").toAbsolutePath();
		Path manifest = DxfPairedOutputWriter.manifestPath(dxf);
		InMemoryFileOperations files = existingPair(dxf);
		files.addFault(MoveRole.PROMOTE_MANIFEST, FaultTiming.BEFORE);
		files.addFault(MoveRole.RESTORE_MANIFEST,
				FaultTiming.COPY_THEN_THROW);
		files.addFault(MoveRole.RESTORE_DXF, FaultTiming.COPY_THEN_THROW);

		DxfWriteException failure = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter(files).write(dxf, pairedOutput(),
						CollisionPolicy.REPLACE_EXISTING));

		assertEquals(Stage.PROMOTE_MANIFEST, failure.getStage());
		assertTrue(failure.isRollbackComplete());
		assertArrayEquals(OLD_DXF, files.bytes(dxf));
		assertArrayEquals(OLD_MANIFEST, files.bytes(manifest));
		assertFalse(files.hasTemporaryArtifacts());
	}

	private static InMemoryFileOperations existingPair(Path dxf) {
		InMemoryFileOperations files = new InMemoryFileOperations(dxf);
		files.put(dxf, OLD_DXF);
		files.put(DxfPairedOutputWriter.manifestPath(dxf), OLD_MANIFEST);
		return files;
	}

	private static DxfPreparedOutput exactOutput() {
		return DxfPreparedOutput.exact(EXACT_DXF);
	}

	private static DxfPreparedOutput pairedOutput() {
		String hash = DxfHashing.sha256(APPROXIMATE_DXF);
		String manifest = "{\"schema\":\"org.geocedg.dxf.fidelity-manifest\","
				+ "\"schema_version\":1,\"dxf\":{\"sha256\":\"" + hash
				+ "\"}}\n";
		return DxfPreparedOutput.paired(APPROXIMATE_DXF,
				new DxfManifestEncoding(ascii(manifest), hash));
	}

	private static byte[] ascii(String value) {
		return value.getBytes(StandardCharsets.US_ASCII);
	}

	private static void assertNoTemporaryFiles(Path directory)
			throws IOException {
		try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
			assertFalse(paths.anyMatch(path -> path.getFileName().toString()
					.contains(".geocedg-")));
		}
	}

	private enum MoveRole {
		BACKUP_DXF,
		BACKUP_MANIFEST,
		PROMOTE_DXF,
		PROMOTE_MANIFEST,
		RESTORE_DXF,
		RESTORE_MANIFEST,
		OTHER
	}

	private enum FaultTiming {
		BEFORE,
		AFTER,
		COPY_THEN_THROW
	}

	private static final class InMemoryFileOperations
			implements DxfFileOperations {
		private final Path dxfPath;
		private final Path manifestPath;
		private final Map<Path, byte[]> contents = new LinkedHashMap<>();
		private final Map<MoveRole, Deque<FaultTiming>> moveFaults =
				new EnumMap<>(MoveRole.class);
		private final List<Path> createdDirectories = new ArrayList<>();
		private int temporarySequence;
		private String corruptTemporaryReadSuffix;
		private byte[] collisionAfterPrecheck;

		private InMemoryFileOperations(Path dxfPath) {
			this.dxfPath = canonical(dxfPath);
			manifestPath = canonical(DxfPairedOutputWriter.manifestPath(
					this.dxfPath));
		}

		private void put(Path path, byte[] bytes) {
			contents.put(canonical(path), Arrays.copyOf(bytes, bytes.length));
		}

		private byte[] bytes(Path path) {
			byte[] value = contents.get(canonical(path));
			return value == null ? null : Arrays.copyOf(value, value.length);
		}

		private void addFault(MoveRole role, FaultTiming timing) {
			moveFaults.computeIfAbsent(role, ignored -> new ArrayDeque<>())
					.addLast(timing);
		}

		private boolean hasTemporaryArtifacts() {
			for (Path path : contents.keySet()) {
				if (path.getFileName().toString().contains(".geocedg-")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean exists(Path path) {
			return contents.containsKey(canonical(path));
		}

		@Override
		public Path createTempFile(Path directory, String prefix, String suffix)
				throws IOException {
			Path canonicalDirectory = canonical(directory);
			createdDirectories.add(canonicalDirectory);
			Path temporary = canonicalDirectory.resolve(prefix
					+ ++temporarySequence + suffix);
			if (contents.putIfAbsent(temporary, new byte[0]) != null) {
				throw new FileAlreadyExistsException(temporary.toString());
			}
			return temporary;
		}

		@Override
		public void write(Path path, byte[] bytes) throws IOException {
			Path canonicalPath = canonical(path);
			if (!contents.containsKey(canonicalPath)) {
				throw new NoSuchFileException(canonicalPath.toString());
			}
			contents.put(canonicalPath, Arrays.copyOf(bytes, bytes.length));
			if (collisionAfterPrecheck != null
					&& canonicalPath.getFileName().toString()
							.endsWith(".dxf.tmp")) {
				contents.put(dxfPath, Arrays.copyOf(collisionAfterPrecheck,
						collisionAfterPrecheck.length));
				collisionAfterPrecheck = null;
			}
		}

		@Override
		public byte[] read(Path path) throws IOException {
			Path canonicalPath = canonical(path);
			byte[] value = contents.get(canonicalPath);
			if (value == null) {
				throw new NoSuchFileException(canonicalPath.toString());
			}
			byte[] copy = Arrays.copyOf(value, value.length);
			if (corruptTemporaryReadSuffix != null
					&& canonicalPath.getFileName().toString()
							.endsWith(corruptTemporaryReadSuffix)) {
				corruptTemporaryReadSuffix = null;
				copy[0] ^= 1;
			}
			return copy;
		}

		@Override
		public boolean isRegularFile(Path path) {
			return contents.containsKey(canonical(path));
		}

		@Override
		public void move(Path source, Path target) throws IOException {
			Path canonicalSource = canonical(source);
			Path canonicalTarget = canonical(target);
			MoveRole role = role(canonicalSource, canonicalTarget);
			FaultTiming timing = nextFault(role);
			if (timing == FaultTiming.BEFORE) {
				throw new IOException("Injected failure before " + role);
			}
			byte[] value = contents.get(canonicalSource);
			if (value == null) {
				throw new NoSuchFileException(canonicalSource.toString());
			}
			if (contents.containsKey(canonicalTarget)) {
				throw new FileAlreadyExistsException(canonicalTarget.toString());
			}
			if (timing == FaultTiming.COPY_THEN_THROW) {
				contents.put(canonicalTarget,
						Arrays.copyOf(value, value.length));
				throw new IOException("Injected copy then failure during " + role);
			}
			contents.remove(canonicalSource);
			contents.put(canonicalTarget, value);
			if (timing == FaultTiming.AFTER) {
				throw new IOException("Injected failure after " + role);
			}
		}

		@Override
		public boolean deleteIfExists(Path path) {
			return contents.remove(canonical(path)) != null;
		}

		private FaultTiming nextFault(MoveRole role) {
			Deque<FaultTiming> faults = moveFaults.get(role);
			return faults == null || faults.isEmpty() ? null : faults.removeFirst();
		}

		private MoveRole role(Path source, Path target) {
			if (source.equals(dxfPath)) {
				return MoveRole.BACKUP_DXF;
			}
			if (source.equals(manifestPath)) {
				return MoveRole.BACKUP_MANIFEST;
			}
			String sourceName = source.getFileName().toString();
			if (target.equals(dxfPath)) {
				return sourceName.endsWith(".dxf.backup")
						? MoveRole.RESTORE_DXF : MoveRole.PROMOTE_DXF;
			}
			if (target.equals(manifestPath)) {
				return sourceName.endsWith(".json.backup")
						? MoveRole.RESTORE_MANIFEST
						: MoveRole.PROMOTE_MANIFEST;
			}
			return MoveRole.OTHER;
		}

		private static Path canonical(Path path) {
			return path.toAbsolutePath().normalize();
		}
	}
}

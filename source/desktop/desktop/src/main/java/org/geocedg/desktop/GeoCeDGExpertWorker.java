/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.move.ggtapi.models.json.JSONTokener;
import org.geogebra.common.util.debug.Log;

/** Private transport for the passive I2 worker. It never loads the trial in the live JVM. */
final class GeoCeDGExpertWorker {
	private static final String DISPATCH = "--expert-inspection-worker";
	private static final int TIME_LIMIT_SECONDS = 60;
	private static final int MAX_REPORT_BYTES = 16 * 1024 * 1024;
	private static final int MAX_PREVIEW_BYTES = 2 * 1024 * 1024;
	private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

	private final Path directory;
	private final Path receiver;
	private final Path context;
	private final String receiverSha256;
	private final String contextSha256;
	private final String classPath;

	GeoCeDGExpertWorker(GeoCeDGLiveInspectionSnapshot snapshot) throws IOException {
		this(snapshot, System.getProperty("java.class.path"));
	}

	GeoCeDGExpertWorker(GeoCeDGLiveInspectionSnapshot snapshot, String classPath) throws IOException {
		directory = Files.createTempDirectory("geocedg-expert-");
		receiver = directory.resolve("receiver.cedg");
		context = directory.resolve("receiver-context.bin");
		receiverSha256 = snapshot.archiveSha256();
		this.classPath = classPath;
		try {
			snapshot.stageReceiver(receiver);
			snapshot.stageLayerContext(context);
			contextSha256 = sha256(readBounded(context, 64 * 1024));
		} catch (IOException | RuntimeException failure) {
			deleteScratch();
			throw failure;
		}
	}

	static boolean isDispatch(String[] arguments) {
		return arguments.length > 0 && DISPATCH.equals(arguments[0]);
	}

	record Result(Source source, byte[] sourceBytes, JSONObject report, String reportSha256,
			boolean available, String reason, String fingerprint, BufferedImage preview)
			implements AutoCloseable {
		@Override
		public void close() {
			source.close();
		}
	}

	static final class Timeout extends IOException {
		Timeout() {
			super("Worker timed out");
		}
	}

	/** Watch before reading so an edit followed by byte-for-byte restoration still expires. */
	static final class Source implements AutoCloseable {
		private final Path selected;
		private final Path resolved;
		private final WatchService watch;
		private final BasicFileAttributes initial;
		private boolean changed;

		Source(File file) throws IOException {
			selected = file.toPath().toAbsolutePath().normalize();
			resolved = selected.toRealPath();
			watch = selected.getFileSystem().newWatchService();
			try {
				selected.getParent().register(watch, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				if (!resolved.getParent().equals(selected.getParent())) {
					resolved.getParent().register(watch, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				}
				initial = Files.readAttributes(resolved, BasicFileAttributes.class,
						LinkOption.NOFOLLOW_LINKS);
			} catch (IOException | RuntimeException failure) {
				close();
				throw failure;
			}
		}

		boolean changed() {
			if (changed) {
				return true;
			}
			for (WatchKey key; (key = watch.poll()) != null;) {
				for (var event : key.pollEvents()) {
					if (event.kind() == OVERFLOW || selected.getFileName().equals(event.context())
							|| resolved.getFileName().equals(event.context())) {
						changed = true;
					}
				}
				if (!key.reset()) {
					changed = true;
				}
			}
			try {
				var current = Files.readAttributes(resolved, BasicFileAttributes.class,
						LinkOption.NOFOLLOW_LINKS);
				changed |= !resolved.equals(selected.toRealPath()) || !current.isRegularFile()
						|| !Objects.equals(initial.fileKey(), current.fileKey())
						|| initial.size() != current.size()
						|| !initial.lastModifiedTime().equals(current.lastModifiedTime())
						|| !initial.creationTime().equals(current.creationTime());
			} catch (IOException | SecurityException failure) {
				changed = true;
			}
			return changed;
		}

		File resolvedFile() {
			return resolved.toFile();
		}

		@Override
		public void close() {
			try {
				watch.close();
			} catch (IOException failure) {
				Log.debug(failure);
			}
		}
	}

	Result inspect(File file, BooleanSupplier cancelled) throws IOException, InterruptedException {
		Source source = null;
		boolean handedOff = false;
		try {
			checkCancelled(cancelled);
			source = new Source(file);
			byte[] bytes = readBounded(source.resolved, GeoCeDGInspectionArchive.MAX_ARCHIVE_BYTES);
			Path input = directory.resolve(GeoCeDGDocumentPolicy.isNative(file)
					? "source.cedg" : "source.ggb");
			Files.write(input, bytes);
			Path report = directory.resolve("report.json");
			Path preview = directory.resolve("preview.png");
			run(input, report, preview, cancelled);
			Result result = parse(source, bytes, report, preview);
			handedOff = true;
			return result;
		} finally {
			if (!handedOff && source != null) {
				source.close();
			}
			deleteScratch();
		}
	}

	private void run(Path input, Path report, Path preview, BooleanSupplier cancelled)
			throws IOException, InterruptedException {
		List<String> workerArguments = List.of(input.toString(), receiver.toString(),
				directory.resolve("preferences.properties").toString(), report.toString(),
				context.toString(), preview.toString());
		String appPath = System.getProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY);
		List<String> command = appPath == null ? javaCommand(workerArguments)
				: packagedCommand(appPath, workerArguments);
		ProcessBuilder builder = GeoCeDGClassicDiagnosticLaunch.processBuilder(command);
		builder.environment().remove("JDK_JAVA_OPTIONS");
		builder.environment().remove("_JAVA_OPTIONS");
		builder.environment().remove("JAVA_TOOL_OPTIONS");
		if (appPath != null) {
			builder.environment().put("JAVA_TOOL_OPTIONS", "-Xmx512m \"-Djava.io.tmpdir="
					+ directory + "\"");
		}
		checkCancelled(cancelled);
		Process process = builder.start();
		try {
			process.getOutputStream().close();
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIME_LIMIT_SECONDS);
			while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
				checkCancelled(cancelled);
				if (System.nanoTime() - deadline >= 0) {
					throw new Timeout();
				}
			}
			checkCancelled(cancelled);
			if (process.exitValue() != 0) {
				throw new IOException("Worker exit " + process.exitValue());
			}
		} finally {
			if (process.isAlive()) {
				process.destroyForcibly();
				if (!process.waitFor(5, TimeUnit.SECONDS)) {
					process.onExit().thenRun(this::deleteScratch);
				}
			}
		}
	}

	private List<String> javaCommand(List<String> workerArguments) throws IOException {
		Path bin = Path.of(System.getProperty("java.home"), "bin");
		Path executable = bin.resolve(File.separatorChar == '\\' ? "javaw.exe" : "java");
		if (!Files.isRegularFile(executable)) {
			throw new IOException("Current runtime cannot start an isolated Java worker");
		}
		List<String> arguments = new ArrayList<>(List.of("-Xmx512m", "-Djava.io.tmpdir=" + directory));
		arguments.addAll(GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(
				ManagementFactory.getRuntimeMXBean().getInputArguments()));
		arguments.addAll(List.of("-cp", classPath,
				GeoCeDGExpertInspection.class.getName()));
		arguments.addAll(workerArguments);
		// Windows limits the command-line length. The classpath in Gradle and installDist
		// can exceed that bound, so pass it via the current Java launcher's argument file.
		Path argumentFile = directory.resolve("worker.args");
		Files.write(argumentFile, arguments.stream().map(GeoCeDGExpertWorker::quoteArgument).toList());
		return List.of(executable.toString(), "@" + argumentFile);
	}

	private static String quoteArgument(String argument) {
		return "\"" + argument.replace("\\", "\\\\").replace("\"", "\\\"")
				.replace("\n", "\\n").replace("\r", "\\r") + "\"";
	}

	private static List<String> packagedCommand(String appPath, List<String> arguments)
			throws IOException {
		Path executable = Path.of(appPath).toAbsolutePath().normalize();
		if (!Files.isRegularFile(executable)) {
			throw new IOException("Packaged GeoCeDG launcher is unavailable");
		}
		List<String> command = new ArrayList<>(List.of(executable.toString(), DISPATCH));
		command.addAll(arguments);
		return command;
	}

	private Result parse(Source source, byte[] bytes, Path reportPath, Path previewPath)
			throws IOException {
		byte[] raw = readBounded(reportPath, MAX_REPORT_BYTES);
		try {
			String json = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(raw)).toString();
			requireBoundedDepth(json);
			JSONTokener tokens = new JSONTokener(json);
			JSONObject report = new JSONObject(tokens);
			JSONObject expert = report.getJSONObject("expert");
			Object availability = expert.get("available");
			String fingerprint = optionalString(expert, "fingerprint");
			String reason = expert.getString("reason");
			JSONObject publication = report.getJSONObject("publication");
			if (!(availability instanceof Boolean available)
					|| tokens.nextClean() != 0 || !Integer.valueOf(2).equals(report.get("schemaVersion"))
					|| !"POST_CU4_I2_PASSIVE_1".equals(report.getString("policy"))
					|| !"POST_CU4_I2_DIFFERENTIAL_2".equals(report.getString("analysisVersion"))
					|| !GeoCeDGExpertIntegrity.VERSION.equals(expert.getString("version"))
					|| !availability.equals(publication.get("available"))
					|| !reason.equals(publication.getString("reason"))
					|| (available && (fingerprint == null || !SHA256.matcher(fingerprint).matches()))
					|| !contextSha256.equals(expert.getString("trialContextSha256"))) {
				throw new IOException("Worker report does not match the expert protocol");
			}
			GeoCeDGExpertReviewModel.validateReport(report, available, sha256(bytes), receiverSha256);
			String previewHash = optionalString(expert, "previewSha256");
			BufferedImage preview = previewHash == null ? null : readPreview(previewPath, previewHash);
			return new Result(source, bytes, report, sha256(raw), available, reason, fingerprint, preview);
		} catch (JSONException failure) {
			throw new IOException("Invalid worker report", failure);
		}
	}

	private static String optionalString(JSONObject object, String key) throws JSONException {
		return object.isNull(key) ? null : object.getString(key);
	}

	private static void requireBoundedDepth(String json) throws IOException {
		int depth = 0;
		boolean quoted = false;
		for (int i = 0; i < json.length(); i++) {
			char ch = json.charAt(i);
			if (quoted && ch == '\\') {
				i++;
			} else if (ch == '"') {
				quoted = !quoted;
			} else if (!quoted && (ch == '{' || ch == '[') && ++depth > 24) {
				throw new IOException("Worker report exceeds its nesting budget");
			} else if (!quoted && (ch == '}' || ch == ']') && --depth < 0) {
				throw new IOException("Unbalanced worker report");
			} else if (!quoted && (ch == '\'' || ch == '/' || ch == '#')) {
				throw new IOException("Worker report must be strict JSON");
			}
		}
		if (quoted || depth != 0) {
			throw new IOException("Incomplete worker report");
		}
	}

	private static BufferedImage readPreview(Path path, String expectedSha256) throws IOException {
		byte[] raw = readBounded(path, MAX_PREVIEW_BYTES);
		if (!sha256(raw).equals(expectedSha256)) {
			throw new IOException("Trial preview hash mismatch");
		}
		var readers = ImageIO.getImageReadersByFormatName("PNG");
		if (!readers.hasNext()) {
			throw new IOException("PNG preview is unavailable in this runtime");
		}
		var reader = readers.next();
		try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(raw))) {
			reader.setInput(input, true, true);
			if (reader.getWidth(0) > 1200 || reader.getHeight(0) > 900) {
				throw new IOException("Trial preview exceeds its image bounds");
			}
			return reader.read(0);
		} finally {
			reader.dispose();
		}
	}

	private static void checkCancelled(BooleanSupplier cancelled) {
		if (cancelled.getAsBoolean()) {
			throw new CancellationException();
		}
	}

	static byte[] readBounded(Path path, int limit) throws IOException {
		if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException("Missing regular file " + path.getFileName());
		}
		try (var input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
			byte[] bytes = input.readNBytes(limit + 1);
			if (bytes.length > limit) {
				throw new IOException("File exceeds the review size limit " + path.getFileName());
			}
			return bytes;
		}
	}

	static String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	private void deleteScratch() {
		try (var paths = Files.walk(directory)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		} catch (IOException failure) {
			Log.debug(failure);
		}
	}
}

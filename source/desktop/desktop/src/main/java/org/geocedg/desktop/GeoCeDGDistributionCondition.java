/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.geocedg.desktop.export.GeoCeDGBuildProvenance;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.DistributionProfile;

/**
 * PRE-G9B-R6-plus-G (PM-5) distribution condition of the running copy.
 *
 * <p>The packaging builder writes {@code app/geocedg-distribution.json} from the
 * selected package profile. The jpackage launcher property only <em>locates</em> it;
 * the condition comes from the verified record content. The record must be bounded,
 * strictly parsed, name a profile authorized for packaging whose status, marker and
 * redistribution flag equal the build-embedded profile table, and carry the version,
 * version date, source revision and canonical package-profile digest embedded in
 * this JAR; the notice it names must exist beside it with the recorded hash.
 *
 * <p>These are consistency checks between artifacts of one GeoCeDG pipeline. They
 * provide no cryptographic authenticity and confer no licensing permission: the
 * packaged notice and the licensing documents remain the legal authority. Every
 * failure yields a non-redistributable state; no state is ever derived from a file
 * name, an installation path, an environment variable, a label or an artifact name.
 */
final class GeoCeDGDistributionCondition {

	/** The four runtime states (PM-5). */
	enum State {
		/** a verified INTERNAL package */
		INTERNAL,
		/** a verified PROFILE NC package */
		NC,
		/** not started by a jpackage launcher: a development or test run */
		DEVELOPMENT,
		/** started by a launcher, but the record is missing or does not verify */
		UNVERIFIED
	}

	static final String LAUNCHER_PROPERTY = "jpackage.app-path";
	static final String RECORD_FILE = "geocedg-distribution.json";
	static final String RECORD_KIND = "geocedg-distribution-record";
	static final long RECORD_SCHEMA_VERSION = 1;
	static final int MAX_RECORD_BYTES = 16 * 1024;
	static final long MAX_NOTICE_BYTES = 1024 * 1024;
	private static final String NOT_AUTHORIZED = "not-authorized";
	private static final Set<String> RECORD_KEYS = Set.of("schema_version", "record_kind",
			"profile_id", "status", "redistributable", "marker", "notice_file",
			"notice_sha256", "application", "source_revision", "package_profile_sha256");
	private static final Set<String> APPLICATION_KEYS = Set.of("name", "version",
			"version_date");
	private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
	private static final Pattern COMMIT = Pattern.compile("[0-9a-f]{40}|[0-9a-f]{64}");
	private static final Pattern NOTICE_NAME = Pattern.compile(
			"[A-Za-z0-9][A-Za-z0-9._-]{0,127}");

	private final State state;
	private final String marker;
	private final String noticeFile;
	private final String cause;

	private GeoCeDGDistributionCondition(State state, String marker, String noticeFile,
			String cause) {
		this.state = state;
		this.marker = marker;
		this.noticeFile = noticeFile;
		this.cause = cause;
	}

	/** @return the condition of the running copy */
	static GeoCeDGDistributionCondition current() {
		return evaluate(System.getProperty(LAUNCHER_PROPERTY),
				Expected.of(GeoCeDGProductInfo.provenance()));
	}

	/**
	 * @param launcher value of the jpackage launcher property, or null
	 * @param expected the build binding, or null when the build embeds none
	 * @return the verified condition; never throws
	 */
	static GeoCeDGDistributionCondition evaluate(String launcher, Expected expected) {
		if (launcher == null || launcher.isBlank()) {
			return new GeoCeDGDistributionCondition(State.DEVELOPMENT, null, null,
					"no jpackage launcher");
		}
		try {
			Path parent = Path.of(launcher).toAbsolutePath().normalize().getParent();
			if (parent == null) {
				return unverified("launcher has no directory");
			}
			return verify(parent.resolve("app"), expected);
		} catch (IOException | RuntimeException failure) {
			return unverified(String.valueOf(failure.getMessage()));
		}
	}

	private static GeoCeDGDistributionCondition verify(Path app, Expected expected)
			throws IOException {
		if (expected == null) {
			return unverified("this build embeds no distribution binding");
		}
		Path recordPath = app.resolve(RECORD_FILE);
		if (!Files.isRegularFile(recordPath, LinkOption.NOFOLLOW_LINKS)) {
			return unverified("distribution record missing");
		}
		if (Files.size(recordPath) > MAX_RECORD_BYTES) {
			return unverified("distribution record too large");
		}
		byte[] bytes = readBounded(recordPath, MAX_RECORD_BYTES);
		if (bytes == null) {
			return unverified("distribution record too large");
		}
		Object root;
		try {
			root = GeoCeDGStrictJson.parse(StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes)).toString());
		} catch (CharacterCodingException | IllegalArgumentException invalid) {
			return unverified("distribution record is not strict UTF-8 JSON");
		}
		if (!(root instanceof Map<?, ?> fields) || !fields.keySet().equals(RECORD_KEYS)) {
			return unverified("distribution record members differ");
		}
		if (!Long.valueOf(RECORD_SCHEMA_VERSION).equals(fields.get("schema_version"))
				|| !RECORD_KIND.equals(fields.get("record_kind"))) {
			return unverified("unsupported distribution record");
		}
		DistributionProfile profile = expected.profile(fields.get("profile_id"));
		if (profile == null) {
			return unverified("undeclared distribution profile");
		}
		State verified = packageableState(profile);
		if (verified == null) {
			return unverified("profile not authorized for packaging");
		}
		if (!profile.getStatus().equals(fields.get("status"))
				|| !profile.getMarker().equals(fields.get("marker"))
				|| !Boolean.valueOf(profile.isRedistributable())
						.equals(fields.get("redistributable"))) {
			return unverified("profile values differ from the build");
		}
		if (!(fields.get("application") instanceof Map<?, ?> application)
				|| !application.keySet().equals(APPLICATION_KEYS)
				|| !GeoCeDGProductInfo.PRODUCT_NAME.equals(application.get("name"))
				|| !expected.version.equals(application.get("version"))
				|| !expected.versionDate.equals(application.get("version_date"))) {
			return unverified("application identity differs from the build");
		}
		if (!expected.sourceRevision.equals(fields.get("source_revision"))) {
			return unverified("source revision differs from the build");
		}
		if (!expected.packageProfileSha256.equals(fields.get("package_profile_sha256"))) {
			return unverified("package-profile digest differs from the build");
		}
		if (!(fields.get("notice_file") instanceof String notice)
				|| !NOTICE_NAME.matcher(notice).matches()
				|| !(fields.get("notice_sha256") instanceof String noticeSha)
				|| !SHA256.matcher(noticeSha).matches()) {
			return unverified("invalid notice reference");
		}
		Path noticePath = app.resolve(notice);
		if (!Files.isRegularFile(noticePath, LinkOption.NOFOLLOW_LINKS)
				|| Files.size(noticePath) > MAX_NOTICE_BYTES) {
			return unverified("distribution notice missing");
		}
		byte[] noticeBytes = readBounded(noticePath, MAX_NOTICE_BYTES);
		if (noticeBytes == null || !noticeSha.equals(sha256(noticeBytes))) {
			return unverified("distribution notice hash differs");
		}
		return new GeoCeDGDistributionCondition(verified, profile.getMarker(), notice, null);
	}

	/** Only INTERNAL and NC are packageable; COMMERCIAL and not-authorized never are. */
	private static State packageableState(DistributionProfile profile) {
		if (NOT_AUTHORIZED.equals(profile.getStatus())) {
			return null;
		}
		if ("INTERNAL".equals(profile.getId()) && !profile.isRedistributable()) {
			return State.INTERNAL;
		}
		if ("NC".equals(profile.getId()) && profile.isRedistributable()) {
			return State.NC;
		}
		return null;
	}

	private static byte[] readBounded(Path path, long limit) throws IOException {
		try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
			byte[] bytes = input.readNBytes((int) limit + 1);
			return bytes.length > limit ? null : bytes;
		}
	}

	/**
	 * @param bytes content
	 * @return lowercase hexadecimal SHA-256
	 */
	static String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(bytes));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	private static GeoCeDGDistributionCondition unverified(String cause) {
		return new GeoCeDGDistributionCondition(State.UNVERIFIED, null, null, cause);
	}

	State getState() {
		return state;
	}

	/** @return the verbatim marker of a verified INTERNAL or NC copy, otherwise null */
	String getMarker() {
		return marker;
	}

	/** @return the notice file name in {@code app/} of a verified copy, otherwise null */
	String getNoticeFile() {
		return noticeFile;
	}

	/** @return whether a jpackage launcher started this copy */
	boolean isPackaged() {
		return state != State.DEVELOPMENT;
	}

	/** @return diagnostic cause of a non-verified state (never shown as a condition) */
	String getCause() {
		return cause;
	}

	/** Values the build embedded; a record must carry exactly these. */
	static final class Expected {
		final String version;
		final String versionDate;
		final String sourceRevision;
		final String packageProfileSha256;
		final List<DistributionProfile> profiles;

		Expected(String version, String versionDate, String sourceRevision,
				String packageProfileSha256, List<DistributionProfile> profiles) {
			this.version = Objects.requireNonNull(version);
			this.versionDate = Objects.requireNonNull(versionDate);
			this.sourceRevision = Objects.requireNonNull(sourceRevision);
			this.packageProfileSha256 = Objects.requireNonNull(packageProfileSha256);
			this.profiles = List.copyOf(profiles);
		}

		/**
		 * @param build embedded provenance
		 * @return the binding, or null when the build lacks any part of it (no version
		 *         date, digest, profile table or established commit)
		 */
		static Expected of(GeoCeDGBuildProvenance build) {
			if (build == null || build.getApplicationVersionDate() == null
					|| build.getPackageProfileSha256() == null
					|| build.getDistributionProfiles().isEmpty()
					|| !COMMIT.matcher(build.getRepositoryCommit()).matches()) {
				return null;
			}
			return new Expected(build.getApplicationVersion(),
					build.getApplicationVersionDate(), build.getRepositoryCommit(),
					build.getPackageProfileSha256(), build.getDistributionProfiles());
		}

		DistributionProfile profile(Object id) {
			if (!(id instanceof String text)) {
				return null;
			}
			for (DistributionProfile profile : profiles) {
				if (profile.getId().equals(text)) {
					return profile;
				}
			}
			return null;
		}
	}
}

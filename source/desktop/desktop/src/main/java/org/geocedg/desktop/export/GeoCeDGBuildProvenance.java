/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/** Immutable build-time GeoCeDG repository provenance. */
public final class GeoCeDGBuildProvenance {

	/**
	 * PRE-G9B-R6-plus-G (PM-5): one distribution profile declared by the package
	 * profile, embedded by the build so that the runtime holds no second literal copy
	 * of a marker.
	 */
	public static final class DistributionProfile {
		private static final Pattern ID = Pattern.compile("[A-Z][A-Z0-9_]{0,31}");

		private final String id;
		private final String status;
		private final String marker;
		private final boolean redistributable;

		/**
		 * @param id profile id, for example {@code INTERNAL}
		 * @param status declared status
		 * @param marker declared distribution marker
		 * @param redistributable declared redistribution flag
		 */
		public DistributionProfile(String id, String status, String marker,
				boolean redistributable) {
			if (id == null || !ID.matcher(id).matches()) {
				throw new IllegalArgumentException("Invalid distribution profile id");
			}
			this.id = id;
			this.status = requireText(status, "distribution profile status");
			this.marker = requireText(marker, "distribution profile marker");
			this.redistributable = redistributable;
		}

		public String getId() {
			return id;
		}

		public String getStatus() {
			return status;
		}

		public String getMarker() {
			return marker;
		}

		public boolean isRedistributable() {
			return redistributable;
		}
	}

	/** State of the sources from which the running Desktop classes were built. */
	public enum RepositoryState {
		/** The recorded commit had no observed worktree changes at build time. */
		CLEAN,
		/** The recorded commit had observed worktree changes at build time. */
		DIRTY,
		/** Build-time repository state could not be established. */
		UNAVAILABLE
	}

	/** Build-time authority used to resolve the repository commit. */
	public enum ResolutionSource {
		/** Explicit Gradle build property. */
		GRADLE_PROPERTY,
		/** GitHub Actions checkout commit. */
		GITHUB_SHA,
		/** Git HEAD resolved by the Gradle build. */
		GIT_HEAD,
		/** No repository commit was available. */
		UNAVAILABLE
	}

	private static final String RESOURCE =
			"/org/geocedg/desktop/export/geocedg-build-provenance.properties";
	private static final Pattern COMMIT = Pattern.compile(
			"(?:[0-9a-f]{40}|[0-9a-f]{64})");
	private static final String UNAVAILABLE = "UNAVAILABLE";
	private static final int MAX_PROFILES = 16;
	private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
	private static final Pattern CALENDAR_DATE = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

	private final String applicationVersion;
	private final String repositoryCommit;
	private final RepositoryState repositoryState;
	private final ResolutionSource resolutionSource;
	private final String applicationVersionDate;
	private final String packageProfileSha256;
	private final List<DistributionProfile> distributionProfiles;

	/**
	 * Creates one immutable provenance record without package metadata.
	 *
	 * @param applicationVersion embedded application version
	 * @param repositoryCommit lowercase hexadecimal commit, or {@code UNAVAILABLE}
	 * @param repositoryState build-time repository state
	 * @param resolutionSource build-time resolution authority
	 */
	public GeoCeDGBuildProvenance(String applicationVersion,
			String repositoryCommit, RepositoryState repositoryState,
			ResolutionSource resolutionSource) {
		this(applicationVersion, repositoryCommit, repositoryState, resolutionSource,
				null, null, List.of());
	}

	/**
	 * Creates one immutable provenance record.
	 *
	 * @param applicationVersion embedded application version
	 * @param repositoryCommit lowercase hexadecimal commit, or {@code UNAVAILABLE}
	 * @param repositoryState build-time repository state
	 * @param resolutionSource build-time resolution authority
	 * @param applicationVersionDate canonical version date, or null when the package
	 *        profile declares none
	 * @param packageProfileSha256 canonical package-profile digest, or null
	 * @param distributionProfiles declared distribution profiles, possibly empty
	 */
	public GeoCeDGBuildProvenance(String applicationVersion,
			String repositoryCommit, RepositoryState repositoryState,
			ResolutionSource resolutionSource, String applicationVersionDate,
			String packageProfileSha256, List<DistributionProfile> distributionProfiles) {
		this.applicationVersion = requireText(applicationVersion,
				"application version");
		if (applicationVersionDate != null && !isCalendarDate(applicationVersionDate)) {
			throw new IllegalArgumentException(
					"Application version date must be a valid ISO calendar date");
		}
		this.applicationVersionDate = applicationVersionDate;
		if (packageProfileSha256 != null
				&& !SHA256.matcher(packageProfileSha256).matches()) {
			throw new IllegalArgumentException(
					"Package profile digest must be 64 lowercase hexadecimal digits");
		}
		this.packageProfileSha256 = packageProfileSha256;
		Set<String> ids = new HashSet<>();
		for (DistributionProfile profile : Objects.requireNonNull(distributionProfiles)) {
			if (!ids.add(Objects.requireNonNull(profile).getId())) {
				throw new IllegalArgumentException("Duplicate distribution profile");
			}
		}
		this.distributionProfiles = Collections.unmodifiableList(
				new ArrayList<>(distributionProfiles));
		String canonicalCommit = requireText(repositoryCommit,
				"repository commit").toLowerCase(Locale.ROOT);
		this.repositoryState = Objects.requireNonNull(repositoryState);
		this.resolutionSource = Objects.requireNonNull(resolutionSource);
		boolean unavailable = UNAVAILABLE.toLowerCase(Locale.ROOT).equals(
				canonicalCommit);
		if (!unavailable && !COMMIT.matcher(canonicalCommit).matches()) {
			throw new IllegalArgumentException(
					"Repository commit must be a 40- or 64-digit hexadecimal value");
		}
		if (unavailable
				!= (resolutionSource == ResolutionSource.UNAVAILABLE)
				|| unavailable
						&& repositoryState != RepositoryState.UNAVAILABLE) {
			throw new IllegalArgumentException(
					"Repository commit and provenance source must agree");
		}
		this.repositoryCommit = unavailable ? UNAVAILABLE : canonicalCommit;
	}

	/**
	 * Loads the deterministic resource generated by the Desktop Gradle build.
	 * Runtime export never invokes Git.
	 *
	 * @return embedded build provenance
	 */
	public static GeoCeDGBuildProvenance load() {
		Properties properties = new Properties();
		try (InputStream stream = GeoCeDGBuildProvenance.class
				.getResourceAsStream(RESOURCE)) {
			if (stream == null) {
				throw new IllegalStateException(
						"GeoCeDG build provenance resource is missing");
			}
			properties.load(stream);
		} catch (IOException exception) {
			throw new IllegalStateException(
					"Cannot load GeoCeDG build provenance", exception);
		}
		return read(properties);
	}

	/**
	 * PRE-G9B-R6-plus-G (PM-3, PM-5): reads one generated provenance record. Only schema
	 * {@code 3} is accepted, with exactly its keys: the version and its required
	 * canonical version date, the canonical package-profile digest, the declared
	 * distribution-profile table and the repository identity. Anything else fails
	 * closed.
	 *
	 * @param properties generated record
	 * @return the provenance
	 * @throws IllegalStateException when the record is not a valid schema 3 record
	 */
	public static GeoCeDGBuildProvenance read(Properties properties) {
		if (!"3".equals(properties.getProperty("schema.version"))) {
			throw new IllegalStateException(
					"Unsupported GeoCeDG build provenance schema");
		}
		try {
			int count = Integer.parseInt(properties.getProperty(
					"distribution.profile.count"));
			if (count < 1 || count > MAX_PROFILES) {
				throw new IllegalArgumentException("Invalid distribution profile count");
			}
			Set<String> expectedKeys = new HashSet<>(List.of("schema.version",
					"application.version", "application.version.date",
					"package.profile.sha256", "distribution.profile.count",
					"repository.commit", "repository.state", "resolution.source"));
			List<DistributionProfile> profiles = new ArrayList<>();
			for (int index = 0; index < count; index++) {
				String prefix = "distribution.profile." + index + ".";
				for (String field : List.of("id", "status", "marker", "redistributable")) {
					expectedKeys.add(prefix + field);
				}
				String redistributable = properties.getProperty(prefix + "redistributable");
				if (!"true".equals(redistributable) && !"false".equals(redistributable)) {
					throw new IllegalArgumentException("Invalid redistributable flag");
				}
				profiles.add(new DistributionProfile(properties.getProperty(prefix + "id"),
						properties.getProperty(prefix + "status"),
						properties.getProperty(prefix + "marker"),
						Boolean.parseBoolean(redistributable)));
			}
			if (!properties.stringPropertyNames().equals(expectedKeys)) {
				throw new IllegalArgumentException("Unexpected provenance keys");
			}
			String date = properties.getProperty("application.version.date");
			String digest = properties.getProperty("package.profile.sha256");
			if (date == null || digest == null) {
				throw new IllegalArgumentException("Package metadata is required");
			}
			return new GeoCeDGBuildProvenance(
					properties.getProperty("application.version"),
					properties.getProperty("repository.commit"),
					RepositoryState.valueOf(properties.getProperty(
							"repository.state")),
					ResolutionSource.valueOf(properties.getProperty(
							"resolution.source")), date, digest, profiles);
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new IllegalStateException(
					"Invalid GeoCeDG build provenance resource", exception);
		}
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	/**
	 * @return full semantic product version for visible Desktop surfaces; the
	 *         display form is always the exact package-version authority, never
	 *         a truncated major.minor abbreviation
	 */
	public String getDisplayApplicationVersion() {
		return applicationVersion;
	}

	public String getRepositoryCommit() {
		return repositoryCommit;
	}

	public RepositoryState getRepositoryState() {
		return repositoryState;
	}

	public ResolutionSource getResolutionSource() {
		return resolutionSource;
	}

	/** @return whether an auditable repository commit is embedded */
	public boolean isEstablished() {
		return !UNAVAILABLE.equals(repositoryCommit);
	}

	/**
	 * @return canonical version date of the package profile ({@code YYYY-MM-DD}), or
	 *         null when this build embeds none
	 */
	public String getApplicationVersionDate() {
		return applicationVersionDate;
	}

	/**
	 * @return canonical SHA-256 of the package profile the build read, or null when
	 *         this build embeds none
	 */
	public String getPackageProfileSha256() {
		return packageProfileSha256;
	}

	/** @return distribution profiles declared by the package profile; possibly empty */
	public List<DistributionProfile> getDistributionProfiles() {
		return distributionProfiles;
	}

	/**
	 * @param value candidate date
	 * @return whether it is a valid ISO 8601 calendar date {@code YYYY-MM-DD}
	 */
	public static boolean isCalendarDate(String value) {
		if (value == null || !CALENDAR_DATE.matcher(value).matches()) {
			return false;
		}
		try {
			LocalDate.parse(value, DateTimeFormatter.ofPattern("uuuu-MM-dd")
					.withResolverStyle(ResolverStyle.STRICT));
			return true;
		} catch (DateTimeParseException invalid) {
			return false;
		}
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}

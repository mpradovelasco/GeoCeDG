/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.desktop.export.GeoCeDGBuildProvenance;
import org.geogebra.common.GeoGebraConstants;

/**
 * PRE-G9B-R6-plus-G (PM-6, PM-7) content of the GeoCeDG About surface, in the same
 * order for every product language:
 * <ol>
 * <li>{@code GeoCeDG <version>};</li>
 * <li>the canonical version date, when the build declares one (ISO form);</li>
 * <li>the build identity (PM-4), never next to a date;</li>
 * <li>the distribution condition of this copy (PM-5);</li>
 * <li>the product description;</li>
 * <li>the GeoGebra upstream baseline;</li>
 * <li>the author line;</li>
 * <li>the licensing summary: the composite-product statement, the component
 * permissions, the notice of this copy and the route to the full texts.</li>
 * </ol>
 * The summary is presentation only; the licensing files remain the authority.
 */
final class GeoCeDGAboutText {

	private static final int SHORT_COMMIT = 12;

	private GeoCeDGAboutText() {
		// pure composition
	}

	/**
	 * @param language product language code
	 * @param build embedded build provenance
	 * @param condition distribution condition of the running copy
	 * @return the About text
	 */
	static String compose(String language, GeoCeDGBuildProvenance build,
			GeoCeDGDistributionCondition condition) {
		StringBuilder text = new StringBuilder(GeoCeDGProductInfo.PRODUCT_NAME)
				.append(' ').append(build.getDisplayApplicationVersion());
		if (build.getApplicationVersionDate() != null) {
			text.append('\n').append(text(language, "About.VersionDate")).append(": ")
					.append(build.getApplicationVersionDate());
		}
		text.append('\n').append(buildIdentity(language, build));
		text.append('\n').append(text(language, "About.Distribution")).append(": ")
				.append(distribution(language, condition));
		text.append("\n\n").append(text(language, "Workspace.About"));
		text.append("\n\n").append(text(language, "About.UpstreamBaseline")).append(": ")
				.append(GeoGebraConstants.VERSION_STRING);
		text.append('\n').append(text(language, "About.Author"));
		text.append("\n\n").append(licensing(language, condition));
		return text.toString();
	}

	/**
	 * PM-4: {@code Build: <12 hex digits> (clean)}, {@code (modified)} or
	 * {@code (state unavailable)}, or {@code Build: unavailable} without a commit.
	 *
	 * @param language product language code
	 * @param build embedded build provenance
	 * @return the build-identity line
	 */
	static String buildIdentity(String language, GeoCeDGBuildProvenance build) {
		String label = text(language, "About.Build") + ": ";
		if (!build.isEstablished()) {
			return label + text(language, "About.Build.Unavailable");
		}
		String state;
		switch (build.getRepositoryState()) {
		case CLEAN:
			state = text(language, "About.Build.Clean");
			break;
		case DIRTY:
			state = text(language, "About.Build.Modified");
			break;
		case UNAVAILABLE:
		default:
			state = text(language, "About.Build.StateUnavailable");
			break;
		}
		return label + build.getRepositoryCommit().substring(0, SHORT_COMMIT)
				+ " (" + state + ")";
	}

	private static String distribution(String language,
			GeoCeDGDistributionCondition condition) {
		GeoCeDGDistributionCondition.State state = condition.getState();
		String localized = text(language, "About.Distribution." + state.name());
		if (state == GeoCeDGDistributionCondition.State.INTERNAL
				|| state == GeoCeDGDistributionCondition.State.NC) {
			return condition.getMarker() + " (" + localized + ")";
		}
		return localized;
	}

	private static String licensing(String language,
			GeoCeDGDistributionCondition condition) {
		StringBuilder text = new StringBuilder(text(language, "About.Licensing.Composite"));
		text.append("\n- ").append(text(language, "About.Licensing.Software"));
		text.append("\n- ").append(text(language, "About.Licensing.Documentation"));
		text.append("\n- ").append(text(language, "About.Licensing.Inherited"));
		text.append("\n- ");
		switch (condition.getState()) {
		case INTERNAL:
		case NC:
			text.append(text(language, "About.Licensing.Notice")
					.replace("%0", "app/" + condition.getNoticeFile()));
			break;
		case DEVELOPMENT:
			text.append(text(language, "About.Licensing.Notice.Development"));
			break;
		case UNVERIFIED:
		default:
			text.append(text(language, "About.Licensing.Notice.Unverified"));
			break;
		}
		text.append("\n- ").append(text(language, condition.isPackaged()
				? "About.Licensing.Texts.Package" : "About.Licensing.Texts.Development"));
		return text.toString();
	}

	private static String text(String language, String key) {
		return GeoCeDGProfile.getText(key, language);
	}
}

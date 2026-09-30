/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.common.main.syntax;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.geogebra.common.main.Localization;
import org.geogebra.common.main.syntax.suggestionfilter.SyntaxFilter;

/**
 * Class to get the syntax of the command with the
 * current locale
 *
 * @author Laszlo
 */
public class LocalizedCommandSyntax implements CommandSyntax {

	private final Localization loc;

	private List<SyntaxFilter> syntaxFilters = new ArrayList<>();

	/**
	 *
	 * @param localization the localization.
	 */
	public LocalizedCommandSyntax(Localization localization) {
		this.loc = localization;
	}

	/**
	 * @param internalCommandName internal command name
	 * @param dim dimension override
	 * @return command syntax TODO check whether getSyntaxString works here
	 */
	@Override
	public String getCommandSyntax(String internalCommandName, int dim) {
		String localizedCommandName = getCommandHead(internalCommandName);
		if (dim == 3) {
			String keySyntax3D = internalCommandName + Localization.syntax3D;
			String syntax3D = loc.getCommand(keySyntax3D);
			if (!syntax3D.equals(keySyntax3D)) {
				syntax3D = filterSyntax(internalCommandName, syntax3D);
				return buildSyntax(syntax3D, localizedCommandName);
			}
		}
		String syntax = getLocalizedSyntax(internalCommandName);
		syntax = filterSyntax(internalCommandName, syntax);
		syntax = buildSyntax(syntax, localizedCommandName);
		return syntax;
	}

	/**
	 *
	 * @param internalCommandName internal command name
	 * @return the localized command
	 */
	protected String getLocalizedCommand(String internalCommandName) {
		return loc.getCommand(internalCommandName);
	}

	/**
	 * GeoCeDG (2026-09-30): PRE-G9B-R5-B, ADR 0031 decision 8. The syntax head comes
	 * from the command-head authority, separately from the syntax-bundle key, whose
	 * body stays in the UI language.
	 *
	 * @param internalCommandName internal command name
	 * @return syntax head
	 */
	private String getCommandHead(String internalCommandName) {
		return loc.isCanonicalEnglishCommandHeads()
				? loc.getCommandHead(internalCommandName)
				: getLocalizedCommand(internalCommandName);
	}

	private String getLocalizedSyntax(String internalCommandName) {
		return getLocalizedCommand(internalCommandName + Localization.syntaxStr);
	}

	private String getLocalizedSyntaxCAS(String internalCommandName) {
		return getLocalizedCommand(internalCommandName + Localization.syntaxCAS);
	}

	private String filterSyntax(String internalCommandName, String syntax) {
		String filteredSyntax = syntax;
		for (SyntaxFilter syntaxFilter : syntaxFilters) {
			filteredSyntax = syntaxFilter.getFilteredSyntax(internalCommandName, filteredSyntax);
		}
		return filteredSyntax;
	}

	private String buildSyntax(String syntax, String command) {
		return syntax.replace("[", command + '(').replace(']', ')');
	}

	@Override
	public String getCommandSyntaxCAS(String internalCommandName) {
		String command = getCommandHead(internalCommandName);
		String syntax = getLocalizedSyntaxCAS(internalCommandName);

		String keyCAS = internalCommandName + Localization.syntaxCAS;
		// make sure "PointList.SyntaxCAS" not displayed in dialog
		if (syntax.equals(keyCAS)) {
			syntax = getLocalizedSyntax(internalCommandName);
		}

		syntax = filterSyntax(internalCommandName, syntax);
		syntax = buildSyntax(syntax, command);
		return syntax;
	}

	/**
	 *
	 * @return the localization.
	 */
	protected Localization getLocalization() {
		return loc;
	}

	/**
	 * Add a syntax filter.
	 * @param syntaxFilter a syntax filter.
	 */
	public void addSyntaxFilter(@Nonnull SyntaxFilter syntaxFilter) {
		if (syntaxFilter != null) {
			syntaxFilters.add(syntaxFilter);
		}
	}

	/**
	 * Remove a previously added syntax filter.
	 * @param syntaxFilter a syntax filter.
	 */
	public void removeSyntaxFilter(@Nonnull SyntaxFilter syntaxFilter) {
		if (syntaxFilter != null) {
			syntaxFilters.remove(syntaxFilter);
		}
	}
}

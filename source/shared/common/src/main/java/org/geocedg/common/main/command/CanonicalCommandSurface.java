/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.StringUtil;

/**
 * PRE-G9B-R5-B (ADR 0031): the canonical English command surface of the shared
 * kernel, consumed by the Desktop presentation.
 *
 * <p>It keeps four concepts apart: the semantic command identity (a
 * {@link Commands} constant), the canonical English public name {@code E(k)}
 * from the English command bundle, the UI-language command names that stay
 * input aliases, and the presentation context. It adds no lookup regime: the
 * USER, SCRIPT and XML lookups and the reverse command table are used unchanged.
 */
public final class CanonicalCommandSurface {

	private CanonicalCommandSurface() {
		// utility class
	}

	/**
	 * @param loc localization
	 * @param internalName internal command name
	 * @return {@code E(k)}, or null for {@code NO_ENGLISH_PUBLIC_NAME}
	 */
	public static @CheckForNull String canonicalEnglishName(@Nonnull Localization loc,
			String internalName) {
		return loc.getCanonicalEnglishCommand(internalName);
	}

	/**
	 * Canonical syntax (ADR 0031 decision 8): the head comes from the command-head
	 * authority and the body from the UI-language syntax key, English base as
	 * parent. A missing key is the typed "no syntax" result, never the literal key.
	 *
	 * @param loc localization
	 * @param internalName internal command name
	 * @param cas whether to prefer the CAS syntax
	 * @return syntax lines; empty when the command has no syntax
	 */
	public static @Nonnull List<String> syntaxLines(@Nonnull Localization loc,
			String internalName, boolean cas) {
		String syntax = cas ? loc.getCommandSyntaxCAS(internalName)
				: loc.getCommandSyntax(internalName);
		if (syntax == null || syntax.endsWith(Localization.syntaxStr)
				|| syntax.endsWith(Localization.syntax3D)
				|| syntax.endsWith(Localization.syntaxCAS)) {
			return Collections.emptyList();
		}
		List<String> lines = new ArrayList<>();
		for (String line : syntax.split("\n")) {
			if (!line.isEmpty()) {
				lines.add(line);
			}
		}
		return lines;
	}

	/**
	 * @param app application
	 * @param cas whether to use the CAS dictionary
	 * @return the offered commands with their identity, head and UI-language alias
	 */
	public static @Nonnull List<CanonicalCommandEntry> offeredEntries(@Nonnull App app,
			boolean cas) {
		return entries(app, app.getOfferedCommands(cas));
	}

	/**
	 * @param app application
	 * @param commands command constants
	 * @return their entries with head and UI-language alias; like the inherited
	 *         case-insensitive dictionary, two constants of one identity whose heads
	 *         differ only in case give one entry, the later one
	 */
	public static @Nonnull List<CanonicalCommandEntry> entries(@Nonnull App app,
			@Nonnull List<Commands> commands) {
		Localization loc = app.getLocalization();
		Map<String, CanonicalCommandEntry> entries = new LinkedHashMap<>();
		for (Commands command : commands) {
			String head = loc.getCommandHead(command.name()).trim();
			entries.put(identity(command).name() + '\u0000' + StringUtil.removeAccents(head),
					new CanonicalCommandEntry(command, head,
							loc.getCommand(command.name()).trim()));
		}
		return new ArrayList<>(entries.values());
	}

	/**
	 * Semantic command identity (ADR 0031 terminology): constants that
	 * {@link Commands#englishToInternal(Commands)} maps together are one identity.
	 *
	 * @param command command constant
	 * @return its identity
	 */
	public static @Nonnull Commands identity(@Nonnull Commands command) {
		return Commands.englishToInternal(command);
	}

	/**
	 * Completion search (ADR 0031 decision 9). An entry matches when its head or its
	 * UI-language alias contains the prefix, ignoring case and accents, like the
	 * inherited dictionary. Entries matching at the start of a word come first; both
	 * groups are sorted by head. Each entry keeps its identity.
	 *
	 * @param app application
	 * @param prefix typed word
	 * @param cas whether to use the CAS dictionary
	 * @return matching entries, one per identity
	 */
	public static @Nonnull List<CanonicalCommandEntry> completions(@Nonnull App app,
			String prefix, boolean cas) {
		if (StringUtil.empty(prefix)) {
			return Collections.emptyList();
		}
		String needle = StringUtil.removeAccents(prefix);
		TreeMap<String, CanonicalCommandEntry> initial = new TreeMap<>();
		TreeMap<String, CanonicalCommandEntry> inner = new TreeMap<>();
		for (CanonicalCommandEntry entry : offeredEntries(app, cas)) {
			int head = StringUtil.removeAccents(entry.getHead()).indexOf(needle);
			int alias = StringUtil.removeAccents(entry.getAlias()).indexOf(needle);
			String key = StringUtil.removeAccents(entry.getHead()) + '\u0000'
					+ entry.getInternalName();
			if (head == 0 || alias == 0) {
				initial.put(key, entry);
			} else if (head > 0 || alias > 0) {
				inner.put(key, entry);
			}
		}
		List<CanonicalCommandEntry> matches = new ArrayList<>(initial.values());
		matches.addAll(inner.values());
		return matches;
	}

	/**
	 * Canonical-first script-editor save (ADR 0031 decision 10). A token the SCRIPT
	 * lookup already resolves keeps its identity: it is stored as written, except
	 * that the canonical English head the editor displays is stored as its internal
	 * name, a spelling of the same identity. Only a token SCRIPT cannot resolve is
	 * converted through the UI-language aliases, as before.
	 *
	 * @param app application
	 * @param token command-position token of the edited script
	 * @return token to store, or null when the token names no command
	 * @throws CanonicalCommandSurfaceError {@code SCRIPT_TOKEN_AMBIGUOUS} when the
	 *         token resolves under SCRIPT and is also an alias of another command
	 */
	public static @CheckForNull String delocalizeScriptCommandToken(@Nonnull App app,
			String token) {
		String scriptIdentity = Commands.lookupInternal(token);
		String aliasIdentity = app.getInternalCommand(token);
		if (scriptIdentity == null) {
			return aliasIdentity;
		}
		if (aliasIdentity != null && !aliasIdentity.equals(scriptIdentity)) {
			throw CanonicalCommandSurfaceError.scriptTokenAmbiguous(app.getLocalization(),
					token, scriptIdentity, aliasIdentity);
		}
		String english = app.getLocalization().getCanonicalEnglishCommand(scriptIdentity);
		return english != null && english.equalsIgnoreCase(token) ? scriptIdentity : token;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.parser.function.ParserFunctions;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.StringUtil;

/**
 * PRE-G9B-R5-B (ADR 0031 decision 5): the canonical-head admissibility gate over
 * the complete command inventory of one application, for one UI language and every
 * captured reverse-table state.
 *
 * <p>For every stored name {@code k} with {@code h = E(k)} it checks that the
 * unchanged USER, SCRIPT and exact-case XML lookups and the canonical-first
 * script save all give back the identity of {@code k}, that no parser function
 * pre-empts {@code h} except the recorded equivalences, and that {@code h} is
 * injective. Every displayed identity must have an English public name. The gate
 * relies on no property of the current bundle content: any failure rejects.
 */
public final class CanonicalCommandHeadGate {

	/** Typed gate failures. */
	public enum Failure {
		/** a displayed identity has no English command-bundle entry */
		NO_ENGLISH_PUBLIC_NAME,
		/** USER does not resolve the head to its identity */
		HEAD_NOT_SELF_RESOLVING,
		/** USER resolves the head differently in two reverse-table states */
		HEAD_STATE_DEPENDENT,
		/** SCRIPT does not resolve the head to its identity */
		HEAD_NOT_SCRIPT_RESOLVABLE,
		/** the head is not an exact-case enum constant of its identity */
		HEAD_NOT_XML_EXACT,
		/** the script-editor save of the head changes its identity */
		SCRIPT_SAVE_NOT_IDENTITY_PRESERVING,
		/** a parser function pre-empts the head */
		HEAD_PREEMPTED_BY_PARSER_FUNCTION,
		/** another identity has the same lower-case head */
		HEAD_NOT_INJECTIVE
	}

	/** Parser functions that produce exactly the operation their command produces. */
	static final Set<String> PARSER_FUNCTION_EQUIVALENCES = Set.of("nCr/2", "nPr/2");
	private static final int MAX_CHECKED_ARITY = 8;

	/** One typed gate failure. */
	public static final class Finding {
		private final String storedName;
		private final String head;
		private final Failure failure;
		private final String detail;

		Finding(String storedName, String head, Failure failure, String detail) {
			this.storedName = storedName;
			this.head = head;
			this.failure = failure;
			this.detail = detail;
		}

		/** @return stored command name {@code k} */
		public String getStoredName() {
			return storedName;
		}

		/** @return canonical head {@code E(k)}, or null */
		public String getHead() {
			return head;
		}

		/** @return typed failure */
		public Failure getFailure() {
			return failure;
		}

		@Override
		public String toString() {
			return failure + " " + storedName + " -> " + head + " (" + detail + ")";
		}
	}

	private final App app;
	private final TreeMap<String, String> heads = new TreeMap<>();
	private final Map<String, Map<String, String>> userByState = new LinkedHashMap<>();
	private final Set<String> displayed = new TreeSet<>();

	/**
	 * @param app application whose UI language and profile are gated
	 */
	public CanonicalCommandHeadGate(@Nonnull App app) {
		this.app = app;
		Localization loc = app.getLocalization();
		for (Commands command : Commands.values()) {
			String head = loc.getCanonicalEnglishCommand(command.name());
			if (head != null) {
				heads.put(command.name(), head);
			}
		}
	}

	/**
	 * Captures the USER resolution of every head in the current reverse-table state.
	 * The displayed inventory is the input-bar dictionary, 3D included (ADR 0031:
	 * the displayable GeoCeDG commands).
	 *
	 * @param state state name, for example {@code fresh} or {@code casFilled}
	 * @param fillCas whether to fill the CAS dictionary first, the second
	 *            reverse-table state
	 */
	public void captureState(@Nonnull String state, boolean fillCas) {
		if (fillCas) {
			app.getOfferedCommands(true);
		}
		for (Commands command : app.getOfferedCommands(false)) {
			displayed.add(command.name());
		}
		Map<String, String> resolved = new TreeMap<>();
		for (String head : heads.values()) {
			resolved.put(head, String.valueOf(app.getReverseCommand(head)));
		}
		userByState.put(state, resolved);
	}

	/** @return canonical heads by stored name */
	public Map<String, String> getHeads() {
		return heads;
	}

	/** @return stored names displayed in any captured state */
	public Set<String> getDisplayed() {
		return displayed;
	}

	/**
	 * @return every typed failure; empty when the gate passes
	 */
	public @Nonnull List<Finding> findings() {
		List<Finding> findings = new ArrayList<>();
		for (String name : displayed) {
			if (!heads.containsKey(name)) {
				findings.add(new Finding(name, null, Failure.NO_ENGLISH_PUBLIC_NAME,
						"displayed without an English command-bundle entry"));
			}
		}
		Map<String, Set<String>> identitiesByHead = new TreeMap<>();
		for (Map.Entry<String, String> entry : heads.entrySet()) {
			String name = entry.getKey();
			String head = entry.getValue();
			String identity = identity(name);
			identitiesByHead.computeIfAbsent(StringUtil.toLowerCaseUS(head),
					key -> new TreeSet<>()).add(identity);
			checkUser(findings, name, head, identity);
			if (!identity.equals(Commands.lookupInternal(head))) {
				findings.add(new Finding(name, head, Failure.HEAD_NOT_SCRIPT_RESOLVABLE,
						String.valueOf(Commands.lookupInternal(head))));
			}
			Commands exact = Commands.stringToCommand(head);
			if (exact == null || !identity.equals(identity(exact.name()))) {
				findings.add(new Finding(name, head, Failure.HEAD_NOT_XML_EXACT,
						String.valueOf(exact)));
			}
			checkScriptSave(findings, name, head, identity);
			checkParserFunctions(findings, name, head);
		}
		for (Map.Entry<String, String> entry : heads.entrySet()) {
			Set<String> identities = identitiesByHead.get(
					StringUtil.toLowerCaseUS(entry.getValue()));
			if (identities.size() > 1) {
				findings.add(new Finding(entry.getKey(), entry.getValue(),
						Failure.HEAD_NOT_INJECTIVE, identities.toString()));
			}
		}
		return findings;
	}

	private void checkUser(List<Finding> findings, String name, String head,
			String identity) {
		String first = null;
		for (Map.Entry<String, Map<String, String>> state : userByState.entrySet()) {
			String resolved = identity(state.getValue().get(head));
			if (!identity.equals(resolved)) {
				findings.add(new Finding(name, head, Failure.HEAD_NOT_SELF_RESOLVING,
						state.getKey() + ": " + resolved));
			}
			if (first != null && !first.equals(resolved)) {
				findings.add(new Finding(name, head, Failure.HEAD_STATE_DEPENDENT,
						first + " / " + resolved));
			}
			first = resolved;
		}
	}

	private void checkScriptSave(List<Finding> findings, String name, String head,
			String identity) {
		try {
			String saved = CanonicalCommandSurface.delocalizeScriptCommandToken(app, head);
			if (saved == null || !identity.equals(Commands.lookupInternal(saved))) {
				findings.add(new Finding(name, head,
						Failure.SCRIPT_SAVE_NOT_IDENTITY_PRESERVING, String.valueOf(saved)));
			}
		} catch (CanonicalCommandSurfaceError ambiguous) {
			findings.add(new Finding(name, head,
					Failure.SCRIPT_SAVE_NOT_IDENTITY_PRESERVING, ambiguous.getOutcome().name()));
		}
	}

	private void checkParserFunctions(List<Finding> findings, String name, String head) {
		ParserFunctions functions = app.getParserFunctions();
		for (int arity = 0; arity <= MAX_CHECKED_ARITY; arity++) {
			if (functions.get(head, arity) != null
					&& !PARSER_FUNCTION_EQUIVALENCES.contains(head + "/" + arity)) {
				findings.add(new Finding(name, head,
						Failure.HEAD_PREEMPTED_BY_PARSER_FUNCTION,
						head + "/" + arity + " -> " + functions.get(head, arity)));
			}
		}
	}

	private static String identity(String commandName) {
		if (commandName == null) {
			return "null";
		}
		Commands command = Commands.stringToCommand(commandName);
		return command == null ? commandName : CanonicalCommandSurface.identity(command).name();
	}
}

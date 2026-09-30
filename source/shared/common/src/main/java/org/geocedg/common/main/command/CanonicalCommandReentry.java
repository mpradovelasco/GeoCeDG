/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.geogebra.common.kernel.CommandLookupStrategy;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionNVar;
import org.geogebra.common.kernel.arithmetic.FunctionVariable;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;

/**
 * PRE-G9B-R5-B (ADR 0031 decision 6): typed fail-closed re-entry of presented
 * canonical command heads.
 *
 * <p>When an element is redefined from editable text, the heads the product
 * presented for that element are recorded again from its current definition,
 * each with the command key the printer passed, so identity never comes from text.
 * A presented head that the submission keeps in command-call position must parse
 * as its presented identity under the unchanged USER regime. When a label, a CAS
 * label, a function variable, a macro or the command table would make it parse as
 * something else, including no command at all, the submission is rejected with
 * {@code CANONICAL_HEAD_SHADOWED} before any mutation. Text the user rewrote into
 * other heads follows ordinary USER semantics. The parser and the lookup are not
 * changed; they are only consulted.
 */
public final class CanonicalCommandReentry {

	private static final CanonicalCommandReentry INACTIVE = new CanonicalCommandReentry(
			null, Collections.emptyList());

	private final Kernel kernel;
	private final List<PresentedHead> kept;

	private CanonicalCommandReentry(Kernel kernel, List<PresentedHead> kept) {
		this.kernel = kernel;
		this.kept = kept;
	}

	/**
	 * Checks a redefinition before it is parsed.
	 *
	 * @param kernel kernel
	 * @param element element being redefined
	 * @param submitted submitted editable text
	 * @return guard to complete with {@link #afterParse(ValidExpression)}
	 * @throws CanonicalCommandSurfaceError {@code CANONICAL_HEAD_SHADOWED}
	 */
	public static @Nonnull CanonicalCommandReentry beforeParse(@Nonnull Kernel kernel,
			@CheckForNull GeoElementND element, @CheckForNull String submitted) {
		Localization loc = kernel.getLocalization();
		if (element == null || submitted == null || !loc.isCanonicalEnglishCommandHeads()
				|| kernel.getCommandLookupStrategy() != CommandLookupStrategy.USER) {
			return INACTIVE;
		}
		Map<String, String> presented = loc.recordCommandHeads(() -> present(element));
		if (presented.isEmpty()) {
			return INACTIVE;
		}
		List<PresentedHead> kept = keptHeads(submitted, presented);
		CanonicalCommandReentry guard = new CanonicalCommandReentry(kernel, kept);
		for (PresentedHead head : kept) {
			guard.checkNameLevel(head);
		}
		return guard;
	}

	/**
	 * Checks the parsed submission, before evaluation, for function variables that
	 * shadow a kept head.
	 *
	 * @param parsed parsed submission
	 * @throws CanonicalCommandSurfaceError {@code CANONICAL_HEAD_SHADOWED}
	 */
	public void afterParse(@CheckForNull ValidExpression parsed) {
		if (kept.isEmpty() || parsed == null) {
			return;
		}
		Set<String> variables = functionVariables(parsed);
		for (PresentedHead head : kept) {
			if (head.parenthesis && variables.contains(head.head)) {
				throw shadowed(head, head.head);
			}
		}
	}

	/** @return the presented heads the submission keeps in call position */
	List<PresentedHead> getKeptHeads() {
		return kept;
	}

	private void checkNameLevel(PresentedHead head) {
		App app = kernel.getApplication();
		Localization loc = kernel.getLocalization();
		if (head.parenthesis) {
			// the parser applies an existing label before it considers a command
			GeoElement label = kernel.lookupLabel(head.head);
			if (label != null) {
				throw shadowed(head, label.getNameDescription());
			}
			if (kernel.lookupCasCellLabel(head.head) != null) {
				throw shadowed(head, loc.getMenu("CAS") + " " + head.head);
			}
		}
		String resolved = app.getReverseCommand(head.head);
		if (resolved == null || !identity(resolved).equals(identity(head.internalName))) {
			throw shadowed(head, resolved == null ? head.head : resolved);
		}
		// the dispatcher consults macros by the resolved name before the command table
		if (kernel.getMacro(resolved) != null) {
			throw shadowed(head, loc.getMenu("Tool") + " " + resolved);
		}
	}

	private CanonicalCommandSurfaceError shadowed(PresentedHead head, String shadowing) {
		return CanonicalCommandSurfaceError.headShadowed(kernel.getLocalization(),
				head.head, shadowing, head.internalName);
	}

	private static void present(GeoElementND element) {
		List<Runnable> presentations = List.of(
				() -> element.getRedefineString(false, true),
				() -> element.getRedefineString(true, true),
				element::getDefinitionForInputBar,
				() -> element.getDefinition(StringTemplate.defaultTemplate),
				() -> element.toGeoElement().getDefinitionForEditor());
		for (Runnable presentation : presentations) {
			try {
				presentation.run();
			} catch (RuntimeException unavailable) {
				// A presentation this element cannot produce presents no head; the
				// heads of the other presentations are still recorded and checked.
			}
		}
	}

	/**
	 * @param submitted submitted text
	 * @param presented presented head to command key
	 * @return presented heads that occur in command-call position, outside strings
	 */
	static List<PresentedHead> keptHeads(String submitted, Map<String, String> presented) {
		List<PresentedHead> kept = new ArrayList<>();
		boolean inString = false;
		int i = 0;
		while (i < submitted.length()) {
			char ch = submitted.charAt(i);
			if (ch == '"') {
				inString = !inString;
				i++;
			} else if (!inString && isNameStart(ch)
					&& (i == 0 || !isNamePart(submitted.charAt(i - 1)))) {
				int end = i + 1;
				while (end < submitted.length() && isNamePart(submitted.charAt(end))) {
					end++;
				}
				String name = submitted.substring(i, end);
				String internal = presented.get(name);
				if (internal != null && end < submitted.length()
						&& (submitted.charAt(end) == '(' || submitted.charAt(end) == '[')) {
					kept.add(new PresentedHead(name, internal, submitted.charAt(end) == '('));
				}
				i = end;
			} else {
				i++;
			}
		}
		return kept;
	}

	private static boolean isNameStart(char ch) {
		return Character.isLetter(ch) || ch == '_' || ch == '$';
	}

	private static boolean isNamePart(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$' || ch == '\'';
	}

	private static String identity(String commandName) {
		Commands command = Commands.stringToCommand(commandName);
		return command == null ? commandName : Commands.englishToInternal(command).name();
	}

	private static Set<String> functionVariables(ValidExpression parsed) {
		Set<String> names = new HashSet<>();
		ExpressionValue value = parsed.unwrap();
		if (value instanceof FunctionNVar) {
			FunctionVariable[] variables = ((FunctionNVar) value).getFunctionVariables();
			if (variables != null) {
				for (FunctionVariable variable : variables) {
					names.add(variable.getSetVarString());
				}
			}
		}
		return names;
	}

	/** A presented canonical head kept by the submission. */
	static final class PresentedHead {
		final String head;
		final String internalName;
		final boolean parenthesis;

		PresentedHead(String head, String internalName, boolean parenthesis) {
			this.head = head;
			this.internalName = internalName;
			this.parenthesis = parenthesis;
		}
	}
}

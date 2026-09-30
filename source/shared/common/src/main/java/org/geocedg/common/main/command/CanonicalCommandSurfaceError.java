/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.command;

import javax.annotation.Nonnull;

import org.geogebra.common.main.Localization;
import org.geogebra.common.main.MyError;

/**
 * PRE-G9B-R5-B (ADR 0031 decisions 6 and 10): a typed fail-closed outcome of the
 * canonical command surface. It is raised before any mutation, so construction,
 * XML and undo state stay unchanged.
 */
public final class CanonicalCommandSurfaceError extends MyError {

	private static final long serialVersionUID = 1L;

	/** Typed outcomes. */
	public enum Outcome {
		/** A presented canonical head would not re-enter as its presented identity. */
		CANONICAL_HEAD_SHADOWED,
		/** A script token resolves to one command and is an alias of another. */
		SCRIPT_TOKEN_AMBIGUOUS
	}

	private final Outcome outcome;
	private final String token;
	private final String conflict;
	private final String expectedIdentity;

	private CanonicalCommandSurfaceError(Localization loc, Outcome outcome, String key,
			String token, String conflict, String expectedIdentity) {
		super(loc, loc.getPlain(key, token, conflict, expectedIdentity));
		this.outcome = outcome;
		this.token = token;
		this.conflict = conflict;
		this.expectedIdentity = expectedIdentity;
		this.commandName = expectedIdentity;
	}

	/**
	 * @param loc localization
	 * @param head presented canonical head
	 * @param shadowing description of what shadows it
	 * @param expectedIdentity internal name of the presented identity
	 * @return typed {@link Outcome#CANONICAL_HEAD_SHADOWED} error
	 */
	static CanonicalCommandSurfaceError headShadowed(Localization loc, String head,
			String shadowing, String expectedIdentity) {
		return new CanonicalCommandSurfaceError(loc, Outcome.CANONICAL_HEAD_SHADOWED,
				"CommandSurface.CanonicalHeadShadowed", head, shadowing, expectedIdentity);
	}

	/**
	 * @param loc localization
	 * @param token script token
	 * @param scriptIdentity identity the SCRIPT lookup gives the token
	 * @param aliasIdentity identity the token names as a UI-language alias
	 * @return typed {@link Outcome#SCRIPT_TOKEN_AMBIGUOUS} error
	 */
	static CanonicalCommandSurfaceError scriptTokenAmbiguous(Localization loc,
			String token, String scriptIdentity, String aliasIdentity) {
		return new CanonicalCommandSurfaceError(loc, Outcome.SCRIPT_TOKEN_AMBIGUOUS,
				"CommandSurface.ScriptTokenAmbiguous", token, aliasIdentity, scriptIdentity);
	}

	/** @return typed outcome */
	public @Nonnull Outcome getOutcome() {
		return outcome;
	}

	/** @return presented head or script token */
	public String getToken() {
		return token;
	}

	/** @return what shadows the head, or the competing alias identity */
	public String getConflict() {
		return conflict;
	}

	/** @return internal name of the identity the text was expected to keep */
	public String getExpectedIdentity() {
		return expectedIdentity;
	}
}

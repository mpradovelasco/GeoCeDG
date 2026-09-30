/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.command;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.geogebra.common.kernel.commands.Commands;

/**
 * PRE-G9B-R5-B (ADR 0031 decisions 3 and 9): one offered command as presentation
 * consumers see it. The semantic identity travels with the entry, so no consumer
 * re-derives it from the displayed head. The head and the alias are two separate
 * concepts even where a bundle gives them the same spelling.
 */
public final class CanonicalCommandEntry {

	private final Commands identity;
	private final String head;
	private final String alias;

	/**
	 * @param identity semantic command identity
	 * @param head presented command head
	 * @param alias UI-language command name, an input alias
	 */
	public CanonicalCommandEntry(@Nonnull Commands identity, @Nonnull String head,
			@Nonnull String alias) {
		this.identity = identity;
		this.head = head;
		this.alias = alias;
	}

	/** @return semantic command identity */
	public @Nonnull Commands getIdentity() {
		return identity;
	}

	/** @return internal command name of the identity */
	public @Nonnull String getInternalName() {
		return identity.name();
	}

	/** @return presented command head */
	public @Nonnull String getHead() {
		return head;
	}

	/** @return UI-language command name, searchable as an input alias */
	public @Nonnull String getAlias() {
		return alias;
	}

	/**
	 * @return the presented head, so that Swing trees and lists show it
	 */
	@Override
	public String toString() {
		return head;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CanonicalCommandEntry)) {
			return false;
		}
		CanonicalCommandEntry entry = (CanonicalCommandEntry) other;
		return identity == entry.identity && head.equals(entry.head)
				&& alias.equals(entry.alias);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identity, head, alias);
	}
}

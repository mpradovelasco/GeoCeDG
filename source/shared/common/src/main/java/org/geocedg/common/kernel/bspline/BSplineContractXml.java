/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.bspline.BSplineContractException.Code;
import org.geogebra.common.io.XMLStringBuilder;

/**
 * Versioned persistence of the family contract of a {@code BSpline} command
 * ({@code bspline-2d.md} section 8): one child element of {@code <command name="BSpline">},
 * written before {@code <input>}, that carries the contract identifier and the knot rule,
 * and optionally the passive approximation provenance block of section 10.5.
 *
 * <p>The reader fails closed with a typed {@link BSplineContractException}; it is active only
 * for a command that resolves to the native {@code BSpline}, so a document whose macro is
 * named {@code BSpline} keeps its previous behaviour. The provenance block is kept as the
 * ordered (name, value) attribute list of the XML infoset and written back with the standard
 * escaping; its grammar belongs to MAINT-A-DXF1 and is not interpreted here.</p>
 */
public final class BSplineContractXml {
	/** Internal and XML name of the native command. */
	public static final String COMMAND = "BSpline";
	/** Family element, first child of the command. */
	public static final String ELEMENT = "geocedgBSpline";
	/** Contract attribute of the family element. */
	public static final String CONTRACT_ATTRIBUTE = "contract";
	/** Knot-rule attribute of the family element. */
	public static final String KNOTS_ATTRIBUTE = "knots";
	/** The only contract a v1 reader accepts. */
	public static final String CONTRACT_V1 = "bspline-2d/v1";
	/** Knot rule of the three-argument form. */
	public static final String KNOTS_EXPLICIT = "explicit";
	/** Knot rule of the forms that generate knots (section 4.2). */
	public static final String KNOTS_CLAMPED_UNIFORM_V1 = "clamped-uniform/v1";
	/** Passive approximation provenance child (section 10.5). */
	public static final String PROVENANCE_ELEMENT = "approximationProvenance";
	/** Version attribute of the provenance block. */
	public static final String PROVENANCE_VERSION_ATTRIBUTE = "version";
	/** The only provenance version a v1 reader accepts. */
	public static final String PROVENANCE_VERSION_1 = "1";

	private BSplineContractXml() {
	}

	/** Immutable persisted contract of one command. */
	public static final class PersistedContract {
		private final String knotRule;
		private final List<Map.Entry<String, String>> provenance;

		private PersistedContract(String knotRule, List<Map.Entry<String, String>> provenance) {
			this.knotRule = knotRule;
			this.provenance = provenance;
		}

		/**
		 * @param knotRule {@link #KNOTS_EXPLICIT} or {@link #KNOTS_CLAMPED_UNIFORM_V1}
		 * @return contract without provenance block
		 */
		public static PersistedContract of(String knotRule) {
			return new PersistedContract(requireKnotRule(knotRule), null);
		}

		/**
		 * @param knotRule new knot rule, for example after a compatible redefinition
		 * @return the same provenance under another knot rule
		 */
		public PersistedContract withKnotRule(String knotRule) {
			return new PersistedContract(requireKnotRule(knotRule), provenance);
		}

		/** @return persisted knot rule */
		public String getKnotRule() {
			return knotRule;
		}

		/** @return whether a passive approximation provenance block was read */
		public boolean hasApproximationProvenance() {
			return provenance != null;
		}

		/** @return ordered attributes of the provenance block, empty when absent */
		public List<Map.Entry<String, String>> getApproximationProvenanceAttributes() {
			return provenance == null ? Collections.emptyList() : provenance;
		}
	}

	/**
	 * @param argumentCount number of command arguments of the persisted form
	 * @return knot rule implied by the form, or null for an arity that has none
	 */
	public static String knotRuleForArgumentCount(int argumentCount) {
		switch (argumentCount) {
		case 3:
			return KNOTS_EXPLICIT;
		case 2:
			return KNOTS_CLAMPED_UNIFORM_V1;
		default:
			return null;
		}
	}

	/**
	 * Writes the family element at the indent of the other command children.
	 *
	 * @param builder command XML builder, positioned before {@code <input>}
	 * @param contract contract to persist
	 */
	public static void write(XMLStringBuilder builder, PersistedContract contract) {
		if (!contract.hasApproximationProvenance()) {
			builder.startTag(ELEMENT, 1).attr(CONTRACT_ATTRIBUTE, CONTRACT_V1)
					.attr(KNOTS_ATTRIBUTE, contract.getKnotRule()).endTag();
			return;
		}
		builder.startOpeningTag(ELEMENT, 1).attr(CONTRACT_ATTRIBUTE, CONTRACT_V1)
				.attr(KNOTS_ATTRIBUTE, contract.getKnotRule()).endTag();
		builder.startTag(PROVENANCE_ELEMENT, 2);
		for (Map.Entry<String, String> attribute
				: contract.getApproximationProvenanceAttributes()) {
			builder.attr(attribute.getKey(), attribute.getValue());
		}
		builder.endTag();
		builder.closeTag(ELEMENT);
	}

	private static String requireKnotRule(String knotRule) {
		if (!KNOTS_EXPLICIT.equals(knotRule) && !KNOTS_CLAMPED_UNIFORM_V1.equals(knotRule)) {
			throw new IllegalArgumentException("Unknown BSpline knot rule: " + knotRule);
		}
		return knotRule;
	}

	/**
	 * Reading state of one native {@code <command name="BSpline">}. Every rejection is a
	 * {@link BSplineContractException} thrown from the element handler itself, outside the
	 * inherited command-processing error collection.
	 */
	public static final class CommandReader {
		private boolean childSeen;
		private boolean familyElementSeen;
		private String knotRule;
		private List<Map.Entry<String, String>> provenance;
		private int depth;

		private CommandReader() {
		}

		/**
		 * @param commandName name attribute of the command
		 * @param macroShadows whether a macro of that name resolves before the native command
		 * @return reader, or null when the command is not the native {@code BSpline}
		 */
		public static CommandReader begin(String commandName, boolean macroShadows) {
			return COMMAND.equals(commandName) && !macroShadows ? new CommandReader() : null;
		}

		/**
		 * Opens the family element (a direct child of the command).
		 *
		 * @param attributes its attributes, in document order
		 */
		public void startFamilyElement(Map<String, String> attributes) {
			if (familyElementSeen || childSeen) {
				throw malformed(ELEMENT + " must be the single first child of the command");
			}
			familyElementSeen = true;
			childSeen = true;
			depth = 1;
			String contract = attributes.get(CONTRACT_ATTRIBUTE);
			if (contract != null && !CONTRACT_V1.equals(contract)) {
				throw new BSplineContractException(Code.UNSUPPORTED_CONTRACT_VERSION,
						"contract " + contract + " is not " + CONTRACT_V1);
			}
			for (String name : attributes.keySet()) {
				if (!CONTRACT_ATTRIBUTE.equals(name) && !KNOTS_ATTRIBUTE.equals(name)) {
					throw malformed("unknown attribute " + name);
				}
			}
			if (contract == null) {
				throw malformed("missing " + CONTRACT_ATTRIBUTE);
			}
			String knots = attributes.get(KNOTS_ATTRIBUTE);
			if (!KNOTS_EXPLICIT.equals(knots) && !KNOTS_CLAMPED_UNIFORM_V1.equals(knots)) {
				throw malformed("unknown or missing knot rule " + knots);
			}
			knotRule = knots;
		}

		/**
		 * Handles an element nested in the family element.
		 *
		 * @param name element name
		 * @param attributes its attributes, in document order
		 */
		public void startNested(String name, Map<String, String> attributes) {
			if (depth != 1 || !PROVENANCE_ELEMENT.equals(name)) {
				throw malformed("unknown child " + name);
			}
			if (provenance != null) {
				throw malformed("more than one " + PROVENANCE_ELEMENT);
			}
			String version = attributes.get(PROVENANCE_VERSION_ATTRIBUTE);
			if (version == null) {
				throw malformed(PROVENANCE_ELEMENT + " without " + PROVENANCE_VERSION_ATTRIBUTE);
			}
			if (!PROVENANCE_VERSION_1.equals(version)) {
				throw new BSplineContractException(Code.UNSUPPORTED_PROVENANCE_VERSION,
						PROVENANCE_ELEMENT + " version " + version);
			}
			List<Map.Entry<String, String>> ordered = new ArrayList<>();
			for (Map.Entry<String, String> attribute : attributes.entrySet()) {
				ordered.add(new AbstractMap.SimpleImmutableEntry<>(attribute.getKey(),
						attribute.getValue()));
			}
			provenance = Collections.unmodifiableList(ordered);
			depth = 2;
		}

		/**
		 * @param name name of the closed element
		 * @return whether the family element itself was closed
		 */
		public boolean endNested(String name) {
			if (depth == 2 && PROVENANCE_ELEMENT.equals(name)) {
				depth = 1;
				return false;
			}
			if (depth == 1 && ELEMENT.equals(name)) {
				depth = 0;
				return true;
			}
			throw malformed("unexpected end of " + name);
		}

		/**
		 * @param text character data inside the family element
		 */
		public void text(String text) {
			if (!text.trim().isEmpty()) {
				throw malformed(ELEMENT + " cannot contain text");
			}
		}

		/**
		 * @param attributeTag tag whose attribute was duplicated
		 */
		public void duplicateAttribute(String attributeTag) {
			if (ELEMENT.equals(attributeTag) || PROVENANCE_ELEMENT.equals(attributeTag)) {
				throw malformed("duplicate attribute in " + attributeTag);
			}
		}

		/**
		 * Called before any other child of the command is handled.
		 *
		 * @param name child element name
		 * @param argumentCount arguments read so far
		 */
		public void beforeCommandChild(String name, int argumentCount) {
			childSeen = true;
			boolean input = "input".equals(name);
			boolean output = "output".equals(name);
			if ((input || output) && !familyElementSeen) {
				throw new BSplineContractException(Code.CONTRACT_MISSING,
						"BSpline command without " + ELEMENT);
			}
			if (output && !knotRule.equals(knotRuleForArgumentCount(argumentCount))) {
				throw malformed("knot rule " + knotRule + " with " + argumentCount
						+ " arguments");
			}
		}

		/** @return the contract read so far, or null before the family element */
		public PersistedContract getContract() {
			return knotRule == null ? null : new PersistedContract(knotRule, provenance);
		}

		private static BSplineContractException malformed(String detail) {
			return new BSplineContractException(Code.MALFORMED_CONTRACT, detail);
		}
	}
}

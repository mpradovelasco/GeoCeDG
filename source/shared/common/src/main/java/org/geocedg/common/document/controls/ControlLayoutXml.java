/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.document.controls;

import static org.geocedg.common.document.controls.ControlLayout.failure;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayout.Slot;
import org.geocedg.common.document.controls.ControlLayoutException.Code;
import org.geogebra.common.io.DocHandler;
import org.geogebra.common.io.QDParser;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.geos.GeoElement;

/** Bounded v1 XML values for a full-document control layout. */
public final class ControlLayoutXml {
	public static final String LAYOUT_ATTRIBUTE = "geocedgControlLayout";
	public static final String SLOT_ATTRIBUTE = "geocedgControlSlot";
	public static final String APPEARANCE_ATTRIBUTE = "geocedgControlAppearance";
	public static final int MAX_BYTES = 65_536;
	private static final int MAX_NAME_HEX_LENGTH = 8 * ControlLayout.MAX_NAME_CODE_POINTS;

	private ControlLayoutXml() {
	}

	/** Immutable values scoped to the XML builder for this one complete document write. */
	public static final class Output {
		private final String header;
		private final String appearance;
		private final Map<GeoElement, String> slots;

		private Output(String header, String appearance, Map<GeoElement, String> slots) {
			this.header = header;
			this.appearance = appearance;
			this.slots = slots;
		}

		public String header() {
			return header;
		}

		public String appearance() {
			return appearance;
		}

		public String slot(GeoElement owner) {
			return slots.get(owner);
		}
	}

	/** @return null when the document has no control layout */
	public static Output capture(ControlLayout layout) {
		if (layout.isEmpty()) {
			return null;
		}
		StringJoiner records = new StringJoiner(";", "1|", "");
		Map<GeoElement, String> slots = new IdentityHashMap<>();
		List<Panel> panels = layout.panels();
		List<Panel> canonical = panels.stream().sorted(Comparator.comparing(Panel::id)).toList();
		for (Panel panel : canonical) {
			StringJoiner ids = new StringJoiner(":");
			for (Slot slot : layout.slots(panel.id()).stream()
					.sorted(Comparator.comparing(Slot::id)).toList()) {
				layout.requireEligible(slot.control());
				ids.add(slot.id());
				slots.put(slot.control(), String.join(",", panel.id(), slot.id(),
						ControlLayout.family(slot.control()), Double.toString(slot.dx()),
						Double.toString(slot.dy())));
			}
			records.add(String.join(",", panel.id(), "1",
					panel.anchor() == Anchor.FIXED ? "F" : "W",
					Double.toString(panel.x()), Double.toString(panel.y()),
					Integer.toString(panel.width()), Integer.toString(panel.height()),
					panel.visible() ? "1" : "0", ids.toString()));
		}
		return new Output(records.toString(), appearance(panels, canonical), slots);
	}

	private static String appearance(List<Panel> panels, List<Panel> canonical) {
		if (panels.equals(canonical) && panels.stream().allMatch(Panel::hasDefaultStyle)) {
			return null;
		}
		StringJoiner records = new StringJoiner(";", "1|", "");
		for (Panel panel : panels) {
			records.add(String.join(",", panel.id(),
					Integer.toHexString(0x1000000 | panel.rgb()).substring(1),
					Integer.toString(panel.opacity()), encodeName(panel.name())));
		}
		return records.toString();
	}

	/** Detects the attributes structurally without touching a document, even for a merge. */
	public static boolean inspect(String xml) throws XMLParseException {
		if (!xml.contains(LAYOUT_ATTRIBUTE) && !xml.contains(SLOT_ATTRIBUTE)
				&& !xml.contains(APPEARANCE_ATTRIBUTE)) {
			return false;
		}
		MarkerInspection inspection = new MarkerInspection();
		try {
			new QDParser().parse(inspection, new StringReader(xml));
		} catch (XMLParseException malformed) {
			throw failure(Code.GRAMMAR, malformed.getMessage());
		} catch (IOException impossible) {
			throw new XMLParseException(impossible);
		}
		return inspection.marked;
	}

	private static final class MarkerInspection implements DocHandler {
		private final Deque<String> elements = new ArrayDeque<>();
		private boolean marked;
		private boolean duplicateAttribute;
		private int constructions;

		@Override
		public void recordDuplicateAttribute(String tag) {
			duplicateAttribute = true;
		}

		@Override
		public void startElement(String tag, Map<String, String> attributes) {
			if (elements.size() == 1 && "geogebra".equals(elements.peek())
					&& "construction".equals(tag)) {
				constructions++;
			}
			boolean header = attributes.containsKey(LAYOUT_ATTRIBUTE);
			boolean slot = attributes.containsKey(SLOT_ATTRIBUTE);
			boolean appearance = attributes.containsKey(APPEARANCE_ATTRIBUTE);
			if (header || slot || appearance) {
				if (duplicateAttribute) {
					throw failure(Code.GRAMMAR, "Duplicate XML attribute on control layout block");
				}
				if (((header || appearance)
						&& !(elements.size() == 1 && "geogebra".equals(elements.peek())
						&& "construction".equals(tag)))
						|| (slot && !(elements.size() == 2 && "construction".equals(elements.peek())
								&& "geogebra".equals(elements.peekLast()) && "element".equals(tag)
								&& !attributes.containsKey("default")))) {
					throw failure(Code.CONTEXT, "Control layout attribute has no native owner block");
				}
				if (appearance && !header) {
					throw failure(Code.ORPHAN, "Control appearance has no layout header");
				}
				marked = true;
			}
			duplicateAttribute = false;
			elements.push(tag);
		}

		@Override
		public void endElement(String tag) {
			if (elements.isEmpty() || !elements.pop().equals(tag.trim())) {
				throw failure(Code.GRAMMAR, "Mismatched XML close tag in control layout input");
			}
		}

		@Override
		public void startDocument() {
		}

		@Override
		public void endDocument() {
			if (!elements.isEmpty()) {
				throw failure(Code.GRAMMAR, "Unclosed XML block in control layout input");
			}
			if (marked && constructions != 1) {
				throw failure(Code.DUPLICATE, "Multiple main construction blocks");
			}
		}

		@Override
		public void text(String text) {
		}
	}

	/** Stages a complete input until the ordinary reader has resolved all of its controls. */
	public static final class Input {
		private final Map<String, Panel> panelsById = new TreeMap<>();
		private final Map<String, Set<String>> expected = new LinkedHashMap<>();
		private final Map<GeoElement, String> owners = new IdentityHashMap<>();
		private int bytes;
		private boolean header;
		private List<Panel> appearance;

		public void readHeader(String value) {
			if (header) {
				throw failure(Code.DUPLICATE, "Duplicate control layout header");
			}
			header = true;
			account(value);
			int remainingSlots = ControlLayout.MAX_SLOTS;
			for (String record : records(value, "control layout")) {
				String[] fields = record.split(",", 10);
				if (fields.length != 9 || !"1".equals(fields[1])) {
					throw failure(Code.GRAMMAR, "Invalid control panel fields");
				}
				Panel panel = new Panel(identifier(fields[0]), anchor(fields[2]), number(fields[3]),
						number(fields[4]), positiveInteger(fields[5]), positiveInteger(fields[6]),
						visible(fields[7]));
				if (panelsById.putIfAbsent(panel.id(), panel) != null) {
					throw failure(Code.DUPLICATE, "Duplicate control panel");
				}
				Set<String> slotIds = new HashSet<>();
				if (!fields[8].isEmpty()) {
					String[] ids = fields[8].split(":", remainingSlots + 1);
					if (ids.length > remainingSlots) {
						throw failure(Code.LIMIT, "Too many expected control slots");
					}
					for (String id : ids) {
						if (!slotIds.add(identifier(id))) {
							throw failure(Code.DUPLICATE, "Duplicate expected control slot");
						}
					}
					remainingSlots -= ids.length;
				}
				expected.put(panel.id(), slotIds);
			}
		}

		/** Resolves the complete optional style and order table against the preceding header. */
		public void readAppearance(String value) {
			if (!header) {
				throw failure(Code.ORPHAN, "Control appearance has no layout header");
			}
			if (appearance != null) {
				throw failure(Code.DUPLICATE, "Duplicate control appearance table");
			}
			account(value);
			Map<String, Panel> ordered = new LinkedHashMap<>();
			for (String record : records(value, "control appearance")) {
				String[] fields = record.split(",", 5);
				if (fields.length != 4 || !fields[1].matches("[0-9a-f]{6}")
						|| !fields[2].matches("[1-9][0-9]")) {
					throw failure(Code.GRAMMAR, "Invalid control appearance fields");
				}
				String id = identifier(fields[0]);
				Panel panel = panelsById.get(id);
				if (panel == null) {
					throw failure(Code.ORPHAN, "Unknown panel in control appearance");
				}
				Panel styled = panel.withStyle(decodeName(fields[3]),
						Integer.parseInt(fields[1], 16), Integer.parseInt(fields[2]));
				if (ordered.putIfAbsent(id, styled) != null) {
					throw failure(Code.DUPLICATE, "Duplicate panel in control appearance");
				}
			}
			if (ordered.size() != panelsById.size()) {
				throw failure(Code.ORPHAN, "Control appearance does not cover all panels");
			}
			appearance = List.copyOf(ordered.values());
		}

		public void claim(GeoElement owner, String value) {
			account(value);
			if (owner == null) {
				throw failure(Code.OWNER, "Control layout slot has no native owner instance");
			}
			if (owners.size() == ControlLayout.MAX_SLOTS) {
				throw failure(Code.LIMIT, "Too many control slot claims");
			}
			if (owners.putIfAbsent(owner, value) != null) {
				throw failure(Code.DUPLICATE, "Control instance claimed more than one slot");
			}
		}

		/** Validates all claims before replacing the live record in a single publication. */
		public void publish(ControlLayout layout) {
			ControlLayout.State state = new ControlLayout.State();
			(appearance == null ? panelsById.values() : appearance).forEach(state::addPanel);
			for (var attachment : owners.entrySet()) {
				GeoElement owner = attachment.getKey();
				String[] fields = attachment.getValue().split(",", 6);
				if (fields.length != 5) {
					throw failure(Code.GRAMMAR, "Invalid control slot fields");
				}
				String panel = identifier(fields[0]);
				String slot = identifier(fields[1]);
				Set<String> expectedSlots = expected.get(panel);
				if (expectedSlots == null || !expectedSlots.contains(slot)) {
					throw failure(Code.ORPHAN, "Control slot is absent from the document header");
				}
				layout.requireEligible(owner);
				if (!ControlLayout.family(owner).equals(fields[2])) {
					throw failure(Code.OWNER, "Control slot family differs from its native owner");
				}
				state.addSlot(new Slot(panel, slot, owner, number(fields[3]), number(fields[4])));
			}
			int expectedCount = expected.values().stream().mapToInt(Set::size).sum();
			if (owners.size() != expectedCount) {
				throw failure(Code.ORPHAN, "Expected control slot has no native owner");
			}
			layout.publish(state);
		}

		private void account(String value) {
			if (value.length() > MAX_BYTES - bytes) {
				throw failure(Code.LIMIT, "Control layout XML exceeds the byte limit");
			}
			for (int index = 0; index < value.length(); index++) {
				if (value.charAt(index) > 127) {
					throw failure(Code.GRAMMAR, "Control layout XML must be ASCII");
				}
			}
			bytes += value.length();
		}
	}

	private static String[] records(String value, String kind) {
		int divider = value.indexOf('|');
		String version = divider < 0 ? value : value.substring(0, divider);
		if (!"1".equals(version)) {
			throw failure(version.matches("[0-9]+") ? Code.VERSION : Code.GRAMMAR,
					"Unsupported " + kind + " version");
		}
		if (divider < 0) {
			throw failure(Code.GRAMMAR, "Missing " + kind + " records");
		}
		String[] records = value.substring(divider + 1)
				.split(";", ControlLayout.MAX_PANELS + 1);
		if (records.length > ControlLayout.MAX_PANELS) {
			throw failure(Code.LIMIT, "Too many " + kind + " records");
		}
		return records;
	}

	private static String encodeName(String name) {
		byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
		StringBuilder encoded = new StringBuilder(bytes.length * 2);
		for (byte current : bytes) {
			encoded.append(Character.forDigit((current & 0xff) >>> 4, 16));
			encoded.append(Character.forDigit(current & 0x0f, 16));
		}
		return encoded.toString();
	}

	private static String decodeName(String value) {
		if (value.length() > MAX_NAME_HEX_LENGTH) {
			throw failure(Code.LIMIT, "Encoded control panel name exceeds its byte limit");
		}
		if ((value.length() & 1) != 0 || !value.matches("[0-9a-f]*")) {
			throw failure(Code.GRAMMAR, "Invalid hexadecimal control panel name");
		}
		byte[] bytes = new byte[value.length() / 2];
		for (int index = 0; index < bytes.length; index++) {
			int high = Character.digit(value.charAt(index * 2), 16);
			int low = Character.digit(value.charAt(index * 2 + 1), 16);
			bytes[index] = (byte) ((high << 4) | low);
		}
		String decoded = new String(bytes, StandardCharsets.UTF_8);
		if (!Arrays.equals(bytes, decoded.getBytes(StandardCharsets.UTF_8))) {
			throw failure(Code.GRAMMAR, "Malformed UTF-8 control panel name");
		}
		return decoded;
	}

	private static String identifier(String text) {
		if (!text.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
			throw failure(Code.GRAMMAR, "Invalid control layout UUID");
		}
		return text;
	}

	private static Anchor anchor(String text) {
		return switch (text) {
		case "F" -> Anchor.FIXED;
		case "W" -> Anchor.WORLD;
		default -> throw failure(Code.GRAMMAR, "Unknown control panel anchoring");
		};
	}

	private static boolean visible(String text) {
		return switch (text) {
		case "0" -> false;
		case "1" -> true;
		default -> throw failure(Code.GRAMMAR, "Invalid control panel visibility");
		};
	}

	private static double number(String text) {
		if (text.length() > 24 || !text.matches("-?[0-9]+(\\.[0-9]+)?([Ee]-?[0-9]+)?")) {
			throw failure(Code.GRAMMAR, "Invalid control layout coordinate");
		}
		double number = Double.parseDouble(text);
		if (!Double.isFinite(number)) {
			throw failure(Code.GRAMMAR, "Non-finite control layout coordinate");
		}
		return number;
	}

	private static int positiveInteger(String text) {
		if (text.length() <= 10 && text.matches("[1-9][0-9]*")) {
			long value = Long.parseLong(text);
			if (value <= Integer.MAX_VALUE) {
				return (int) value;
			}
		}
		throw failure(Code.GRAMMAR, "Invalid positive panel dimension");
	}
}

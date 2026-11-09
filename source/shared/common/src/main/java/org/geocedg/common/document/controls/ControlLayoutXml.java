/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.document.controls;

import static org.geocedg.common.document.controls.ControlLayout.failure;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

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
	public static final int MAX_BYTES = 65_536;

	private ControlLayoutXml() {
	}

	/** Immutable values scoped to the XML builder for this one complete document write. */
	public static final class Output {
		private final String header;
		private final Map<GeoElement, String> slots;

		private Output(String header, Map<GeoElement, String> slots) {
			this.header = header;
			this.slots = slots;
		}

		public String header() {
			return header;
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
		for (Panel panel : layout.panels().stream().sorted(Comparator.comparing(Panel::id)).toList()) {
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
		return new Output(records.toString(), slots);
	}

	/** Detects the attributes structurally without touching a document, even for a merge. */
	public static boolean inspect(String xml) throws XMLParseException {
		if (!xml.contains(LAYOUT_ATTRIBUTE) && !xml.contains(SLOT_ATTRIBUTE)) {
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
			if (header || slot) {
				if (duplicateAttribute) {
					throw failure(Code.GRAMMAR, "Duplicate XML attribute on control layout block");
				}
				if ((header && !(elements.size() == 1 && "geogebra".equals(elements.peek())
						&& "construction".equals(tag)))
						|| (slot && !(elements.size() == 2 && "construction".equals(elements.peek())
								&& "geogebra".equals(elements.peekLast()) && "element".equals(tag)
								&& !attributes.containsKey("default")))) {
					throw failure(Code.CONTEXT, "Control layout attribute has no native owner block");
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
		private final ControlLayout.State state = new ControlLayout.State();
		private final Map<String, Set<String>> expected = new LinkedHashMap<>();
		private final Map<GeoElement, String> owners = new IdentityHashMap<>();
		private int bytes;
		private boolean header;

		public void readHeader(String value) {
			if (header) {
				throw failure(Code.DUPLICATE, "Duplicate control layout header");
			}
			header = true;
			account(value);
			int divider = value.indexOf('|');
			String version = divider < 0 ? value : value.substring(0, divider);
			if (!"1".equals(version)) {
				throw failure(version.matches("[0-9]+") ? Code.VERSION : Code.GRAMMAR,
						"Unsupported control layout version");
			}
			if (divider < 0) {
				throw failure(Code.GRAMMAR, "Missing control panel records");
			}
			int remainingSlots = ControlLayout.MAX_SLOTS;
			String[] records = value.substring(divider + 1)
					.split(";", ControlLayout.MAX_PANELS + 1);
			if (records.length > ControlLayout.MAX_PANELS) {
				throw failure(Code.LIMIT, "Too many control panels");
			}
			for (String record : records) {
				String[] fields = record.split(",", 10);
				if (fields.length != 9 || !"1".equals(fields[1])) {
					throw failure(Code.GRAMMAR, "Invalid control panel fields");
				}
				Panel panel = new Panel(identifier(fields[0]), anchor(fields[2]), number(fields[3]),
						number(fields[4]), positiveInteger(fields[5]), positiveInteger(fields[6]),
						visible(fields[7]));
				state.addPanel(panel);
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

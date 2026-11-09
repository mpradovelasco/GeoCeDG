/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGInspectionArchive.addFinding;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geocedg.common.kernel.units.UnitMetadataException;
import org.geocedg.common.kernel.units.UnitStateXml;
import org.geocedg.common.document.controls.ControlLayoutXml;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Code;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Declaration;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Disposition;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Finding;
import org.geogebra.common.io.DocHandler;
import org.geogebra.common.io.QDParser;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.plugin.ScriptType;

/**
 * Positive XML vocabulary for native evaluation in the I2 pilot. This parser
 * inspects syntax and capabilities only. The native reader owns the construction.
 */
final class GeoCeDGInspectionXml {
	private static final int MAX_DEPTH = 16;
	private static final int MAX_ELEMENTS = 6000;
	private static final int MAX_ATTRIBUTES = 32;
	private static final int MAX_ATTR_LENGTH = 2048;
	private static final int MAX_OBJECTS = 128;
	private static final int MAX_PORTS = 12;
	private static final int MAX_SEQUENCE_COUNT = 128;
	private static final String UNSIGNED_DECIMAL = "(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)"
			+ "(?:[eE][+-]?[0-9]{1,3})?";
	private static final String IDENTIFIER = "[A-Za-z][A-Za-z0-9']*(?:_[A-Za-z0-9]+"
			+ "|_\\{[A-Za-z0-9]+\\})?";
	private static final Pattern NUMBER = Pattern.compile("[+-]?" + UNSIGNED_DECIMAL);
	private static final Pattern LABEL = Pattern.compile(IDENTIFIER);
	private static final Pattern DURABLE_ID = Pattern.compile("geo:[a-f0-9]{32}");
	private static final Pattern SAFE_TEXT = Pattern.compile("[\\p{L}\\p{M}\\p{N} .,;:_!?()+-]*");
	private static final Pattern TOKEN = Pattern.compile(UNSIGNED_DECIMAL + "|"
			+ IDENTIFIER + "|[+\\-*/^(),{}<>°=]");
	private static final Set<String> GEO_TYPES = Set.of("angle", "boolean", "conic", "conicpart",
			"curvecartesian", "function", "line", "list", "numeric", "point", "polygon",
			"polyline", "ray", "segment", "text", "vector");
	private static final Map<String, String> DEFAULT_TYPES = Map.ofEntries(
			Map.entry("10", "point"), Map.entry("11", "point"), Map.entry("12", "point"),
			Map.entry("13", "point"), Map.entry("14", "point"), Map.entry("16", "point"),
			Map.entry("20", "line"), Map.entry("21", "segment"), Map.entry("23", "functionnvar"),
			Map.entry("24", "function"), Map.entry("25", "ray"), Map.entry("30", "vector"),
			Map.entry("40", "conic"), Map.entry("41", "conicpart"), Map.entry("50", "numeric"),
			Map.entry("52", "angle"), Map.entry("60", "function"), Map.entry("65", "functionnvar"),
			Map.entry("70", "polygon"), Map.entry("71", "polyline"), Map.entry("80", "locus"),
			Map.entry("100", "text"), Map.entry("110", "image"), Map.entry("120", "boolean"),
			Map.entry("130", "list"), Map.entry("3103", "axis3D"), Map.entry("3106", "curvecartesian3d"),
			Map.entry("3150", "conic3d"), Map.entry("3200", "plane3d"), Map.entry("3300", "polyhedron"),
			Map.entry("3301", "quadric"), Map.entry("3304", "surfacecartesian3d"),
			Map.entry("3305", "net"), Map.entry("3310", "polyhedron"), Map.entry("3311", "polyhedron"),
			Map.entry("3312", "polyhedron"), Map.entry("3400", "function"));
	private static final Map<String, String> SETTINGS_ATTRIBUTES = Map.ofEntries(
			Map.entry("gui/window", "width height"),
			Map.entry("gui/perspectives", ""),
			Map.entry("perspectives/perspective", "id"),
			Map.entry("perspective/panes", ""),
			Map.entry("perspective/views", ""),
			Map.entry("panes/pane", "location divider orientation"),
			Map.entry("views/view", "id toolbar visible inframe stylebar location size tab window"),
			Map.entry("perspective/toolbar", "show items position help"),
			Map.entry("perspective/input", "show cmd top"),
			Map.entry("perspective/dockBar", "show east"),
			Map.entry("gui/labelingStyle", "val"),
			Map.entry("gui/font", "size"),
			Map.entry("gui/consProtColumns", "col0 col1 col2 col3 col4 col5 col6 col7"),
			Map.entry("gui/consProtocol", "useColors addIcons showOnlyBreakpoints"),
			Map.entry("gui/consProtNavigationBar", "id playButton playDelay protButton consStep"),
			Map.entry("euclidianView/viewNumber", "viewNo"),
			Map.entry("euclidianView/size", "width height"),
			Map.entry("euclidianView/coordSystem", "xZero yZero scale yscale"),
			Map.entry("euclidianView/evSettings",
					"axes grid gridIsBold pointCapturing rightAngleStyle checkboxSize gridType"),
			Map.entry("euclidianView/bgColor", "r g b"),
			Map.entry("euclidianView/axesColor", "r g b"),
			Map.entry("euclidianView/gridColor", "r g b"),
			Map.entry("euclidianView/lineStyle", "axes grid"),
			Map.entry("euclidianView/gridDist", "distX distY distTheta"),
			Map.entry("euclidianView/axis",
					"id show label unitLabel tickStyle showNumbers tickDistance tickExpression"),
			Map.entry("algebraView/mode", "val"),
			Map.entry("algebraView/auxiliary", "show"),
			Map.entry("kernel/continuous", "val"),
			Map.entry("kernel/usePathAndRegionParameters", "val"),
			Map.entry("kernel/decimals", "val"),
			Map.entry("kernel/significantfigures", "val"),
			Map.entry("kernel/angleUnit", "val"),
			Map.entry("kernel/algebraStyle", "val spreadsheet"),
			Map.entry("kernel/coordStyle", "val"),
			Map.entry("kernel/angleFromInvTrig", "val"),
			Map.entry("kernel/startAnimation", "val"));
	private static final Set<String> SETTINGS_CONTAINERS =
			Set.of("perspectives", "perspective", "panes", "views");
	private static final Set<String> SCALAR_TYPES = Set.of("angle", "numeric");
	private static final Set<String> ARITHMETIC_TYPES = Set.of("angle", "boolean", "numeric");
	private static final Set<String> TUPLE_TYPES = Set.of("angle", "boolean", "numeric", "point");
	private static final Map<String, String> STYLE_ATTRIBUTES = Map.ofEntries(
			Map.entry("algebra", "val"),
			Map.entry("angleStyle", "val"), Map.entry("arcSize", "val"),
			Map.entry("autocolor", "val"), Map.entry("auxiliary", "val"),
			Map.entry("bgColor", "r g b alpha"), Map.entry("caption", "val"),
			Map.entry("checkbox", "fixed"), Map.entry("coordStyle", "style"),
			Map.entry("coords", "x y z w vx vy vz wx wy wz ox oy oz ow"),
			Map.entry("decimals", "val"), Map.entry("decoration", "type"),
			Map.entry("eigenvectors", "x0 y0 z0 x1 y1 z1 x2 y2 z2"),
			Map.entry("endStyle", "val"), Map.entry("eqnStyle", "style parameter"),
			Map.entry("fading", "val"), Map.entry("fixed", "val"),
			Map.entry("font", "serif size sizeM style"),
			Map.entry("headStyle", "val"), Map.entry("inBackground", "val"),
			Map.entry("keepTypeOnTransform", "val"), Map.entry("labelMode", "val"),
			Map.entry("labelOffset", "x y"), Map.entry("layer", "val"),
			Map.entry("lineStyle", "opacity thickness type typeHidden"),
			Map.entry("matrix", "A0 A1 A2 A3 A4 A5 A6 A7 A8 A9"),
			Map.entry("objColor", "r g b alpha hatchAngle hatchDistance fillType inverseFill"),
			Map.entry("outlyingIntersections", "val"),
			Map.entry("pointSize", "val"), Map.entry("pointStyle", "val"),
			Map.entry("selectionAllowed", "val"), Map.entry("show", "ev label object"),
			Map.entry("showTrimmed", "val"), Map.entry("simplifyCoefficients", "val"),
			Map.entry("significantfigures", "val"), Map.entry("startStyle", "val"),
			Map.entry("tooltipMode", "val"));

	record DocumentMetadata(String documentId, List<Declaration> declarations, boolean safe) {
		static final DocumentMetadata EMPTY = new DocumentMetadata(null, List.of(), false);

		DocumentMetadata {
			declarations = List.copyOf(declarations);
		}
	}

	private record Node(String name, Map<String, String> attrs, List<Node> children) {
		String attr(String key) {
			return attrs.getOrDefault(key, "");
		}

		boolean attributes(String allowed) {
			List<String> keys = List.of(allowed.split(" "));
			return keys.containsAll(attrs.keySet());
		}

		boolean leaf(String allowed) {
			return children.isEmpty() && attributes(allowed);
		}
	}

	private final String entry;
	private final List<Finding> findings;
	private final List<Declaration> declarations = new ArrayList<>();
	private final Map<String, String> labels = new HashMap<>();
	private final Map<String, String> referencedLabels = new HashMap<>();
	private final Set<String> declared = new HashSet<>();
	private boolean safe = true;
	private boolean controlLayout;

	private GeoCeDGInspectionXml(String entry, List<Finding> findings) {
		this.entry = entry;
		this.findings = findings;
	}

	static DocumentMetadata scanDocument(String entry, byte[] raw, List<Finding> findings) {
		GeoCeDGInspectionXml scan = new GeoCeDGInspectionXml(entry, findings);
		Node root = scan.parse(raw);
		if (root == null) {
			return DocumentMetadata.EMPTY;
		}
		scan.scanCapabilities(root);
		scan.checkDocument(root);
		String id = root.attr("id");
		return new DocumentMetadata(id.matches("[A-Za-z0-9_-]{1,128}") ? id : null,
				scan.declarations, scan.safe);
	}

	static Disposition scanDefaults(String entry, byte[] raw, List<Finding> findings) {
		GeoCeDGInspectionXml scan = new GeoCeDGInspectionXml(entry, findings);
		Node root = scan.parse(raw);
		if (root == null) {
			return Disposition.STATIC_ONLY;
		}
		scan.scanCapabilities(root);
		scan.checkDefaults(root);
		return scan.safe ? Disposition.NATIVE_XML : Disposition.STATIC_ONLY;
	}

	static void scanMacros(String entry, byte[] raw, List<Finding> findings) {
		GeoCeDGInspectionXml scan = new GeoCeDGInspectionXml(entry, findings);
		Node root = scan.parse(raw);
		scan.refuse(Code.MACRO, entry, "Macro registration and evaluation are outside the pilot");
		if (root != null) {
			scan.scanCapabilities(root);
		}
	}

	private Node parse(byte[] raw) {
		XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		try {
			String xml = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(raw)).toString();
			// QDParser copies growing comment/CDATA text when it sees a closing angle bracket.
			if (xml.contains("<!--") || xml.contains("<![CDATA[")) {
				refuse(Code.UNSUPPORTED_XML, "", "XML comments and CDATA are outside the pilot");
				return null;
			}
			XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
			try {
				String encoding = reader.getCharacterEncodingScheme();
				if (encoding != null && !"UTF-8".equalsIgnoreCase(encoding)
						|| reader.getVersion() != null && !"1.0".equals(reader.getVersion())) {
					throw new XMLStreamException("Unsupported XML encoding or version");
				}
				Node root = readTree(reader);
				return root == null || !nativeSyntaxAgrees(xml, root) ? null : root;
			} finally {
				reader.close();
			}
		} catch (IOException | XMLStreamException malformed) {
			refuse(Code.INVALID_XML, "", "Malformed UTF-8 native XML or forbidden XML construct");
			return null;
		}
	}

	private boolean nativeSyntaxAgrees(String xml, Node root) {
		try {
			new QDParser().parse(new NativeSyntax(root), new StringReader(xml));
			return true;
		} catch (IOException | XMLParseException | IllegalArgumentException mismatch) {
			refuse(Code.UNSUPPORTED_XML, "", "XML interpretation differs in the native parser");
			return false;
		}
	}

	private record XmlEvent(Node node, boolean start) {
	}

	/**
	 * Compare the native tokenizer with the securely parsed syntax before the
	 * same bytes reach MyXMLHandler. This handler has no application or kernel.
	 */
	private static final class NativeSyntax implements DocHandler {
		private final Node root;
		private final Iterator<XmlEvent> expected;

		NativeSyntax(Node root) {
			this.root = root;
			List<XmlEvent> events = new ArrayList<>();
			append(root, events);
			this.expected = events.iterator();
		}

		private static void append(Node node, List<XmlEvent> events) {
			events.add(new XmlEvent(node, true));
			node.children().forEach(child -> append(child, events));
			events.add(new XmlEvent(node, false));
		}

		private Node check(String name, boolean start) throws XMLParseException {
			if (expected.hasNext()) {
				XmlEvent event = expected.next();
				if (event.start() == start && event.node().name().equals(name)) {
					return event.node();
				}
			}
			throw mismatch();
		}

		@Override
		public void startElement(String tag, Map<String, String> attrs) throws XMLParseException {
			Node node = check(tag, true);
			var attributes = attrs.entrySet().stream()
					.filter(attribute -> node != root || !"xmlns".equals(attribute.getKey())
							&& !"xmlns:xsi".equals(attribute.getKey()))
					.toList();
			if (!attributes.equals(List.copyOf(node.attrs().entrySet()))) {
				throw mismatch();
			}
		}

		@Override
		public void endElement(String tag) throws XMLParseException {
			check(tag, false);
		}

		@Override
		public void startDocument() {
			// All events are accounted for in the expected iterator.
		}

		@Override
		public void endDocument() throws XMLParseException {
			if (expected.hasNext()) {
				throw mismatch();
			}
		}

		@Override
		public void text(String text) throws XMLParseException {
			if (!text.isBlank()) {
				throw mismatch();
			}
		}

		private static XMLParseException mismatch() {
			return new XMLParseException("Native syntax differs from securely parsed XML");
		}
	}

	private Node readTree(XMLStreamReader reader) throws XMLStreamException {
		ArrayDeque<Node> stack = new ArrayDeque<>();
		int elements = 0;
		Node root = null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if (++elements > MAX_ELEMENTS || stack.size() >= MAX_DEPTH
						|| reader.getAttributeCount() > MAX_ATTRIBUTES) {
					refuse(Code.LIMIT_EXCEEDED, "", "XML element, depth or attribute limit");
					return null;
				}
				Node node = readNode(reader, stack.isEmpty());
				if (stack.isEmpty()) {
					root = node;
				} else {
					stack.getLast().children().add(node);
				}
				stack.addLast(node);
				break;
			case XMLStreamConstants.END_ELEMENT:
				stack.removeLast();
				break;
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.SPACE:
				if (!reader.isWhiteSpace()) {
					throw new XMLStreamException("Unexpected free XML text");
				}
				break;
			case XMLStreamConstants.DTD:
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.ENTITY_REFERENCE:
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				throw new XMLStreamException("Forbidden XML construct");
			default:
				break;
			}
		}
		if (root == null) {
			throw new XMLStreamException("Missing XML root");
		}
		return root;
	}

	private Node readNode(XMLStreamReader reader, boolean root) throws XMLStreamException {
		String name = reader.getLocalName();
		Map<String, String> attrs = new LinkedHashMap<>();
		if (!empty(reader.getNamespaceURI()) || !empty(reader.getPrefix()) || name.length() > 64) {
			throw new XMLStreamException("Unexpected XML namespace");
		}
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String prefix = reader.getNamespacePrefix(i);
			String uri = reader.getNamespaceURI(i);
			if (!root || !(empty(prefix) && empty(uri)
					|| "xsi".equals(prefix) && XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(uri))) {
				throw new XMLStreamException("Unexpected namespace declaration");
			}
		}
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String key = reader.getAttributeLocalName(i);
			boolean layoutAttribute = empty(reader.getAttributeNamespace(i))
					&& ("construction".equals(name) && (ControlLayoutXml.LAYOUT_ATTRIBUTE.equals(key)
							|| ControlLayoutXml.APPEARANCE_ATTRIBUTE.equals(key))
							|| "element".equals(name) && ControlLayoutXml.SLOT_ATTRIBUTE.equals(key));
			if (layoutAttribute && !controlLayout) {
				controlLayout = true;
				refuse(Code.CONTROL_LAYOUT, name, "Control layout outside the passive vocabulary");
			}
			String value = reader.getAttributeValue(i);
			int maxLength = layoutAttribute ? ControlLayoutXml.MAX_BYTES : MAX_ATTR_LENGTH;
			if (key.length() > 64 || value.length() > maxLength) {
				throw new XMLStreamException("XML attribute size limit");
			}
			if (!empty(reader.getAttributeNamespace(i))) {
				if (!root || !"xsi".equals(reader.getAttributePrefix(i))
						|| !"noNamespaceSchemaLocation".equals(key)) {
					throw new XMLStreamException("Unexpected XML attribute namespace");
				}
				key = "xsi:" + key;
			}
			attrs.put(key, value);
		}
		return new Node(name, attrs, new ArrayList<>());
	}

	private void scanCapabilities(Node node) {
		String name = node.name();
		if (ScriptType.getTypeWithXMLName(name) != null || "listener".equals(name)) {
			refuse(Code.SCRIPT, name, "Object script or listener");
		} else if ("macro".equals(name)) {
			String command = node.attr("cmdName");
			refuse(Code.MACRO, name, label(command) ? command : "Embedded macro");
		} else if ("animation".equals(name) && !optionalFalse(node, "playing")
				|| "startAnimation".equals(name) && !"false".equals(node.attr("val"))) {
			refuse(Code.ANIMATION, name, "Active or scheduled animation");
		} else if (Set.of("audio", "video", "embed", "embedSettings").contains(name)
				|| "file".equals(name) && !"".equals(node.attr("name"))
				|| "objColor".equals(name) && node.attrs().containsKey("image")) {
			refuse(Code.RESOURCE, name, "File, image or embedded resource reference");
		}
		node.children().forEach(this::scanCapabilities);
	}

	private boolean root(Node node) {
		String app = node.attr("app");
		return "geogebra".equals(node.name()) && "5.0".equals(node.attr("format"))
				&& (app.isEmpty() || "classic".equals(app))
				&& node.attributes("format version app platform id xsi:noNamespaceSchemaLocation")
				&& (node.attr("id").isEmpty() || node.attr("id").matches("[A-Za-z0-9_-]{1,128}"));
	}

	private void checkDocument(Node root) {
		if (!root(root)) {
			refuse(Code.UNSUPPORTED_XML, "", "Unsupported native root or file format");
			return;
		}
		Set<String> singletons = new HashSet<>();
		int views = 0;
		for (Node node : root.children()) {
			String name = node.name();
			if ("euclidianView".equals(name) ? ++views > 2 : !singletons.add(name)) {
				refuse(Code.UNSUPPORTED_XML, name, "Duplicate document section");
				continue;
			}
			boolean allowed = switch (name) {
			case "gui", "euclidianView", "algebraView", "kernel" -> {
				checkSettingsSection(node);
				yield true;
			}
			case "tableview" -> node.leaf("min max step") && numbers(node, "min", "max", "step");
			case "scripting" -> node.leaf("blocked disabled");
			case "geocedgHiddenLayers" -> node.leaf("version layers")
					&& "1".equals(node.attr("version")) && node.attr("layers").matches("[0-9 ]+");
			case "construction" -> construction(node);
			default -> false;
			};
			if (!allowed) {
				refuse(Code.UNSUPPORTED_XML, name, "Document section outside the pilot vocabulary");
			}
		}
		if (!singletons.contains("construction")) {
			refuse(Code.INVALID_XML, "construction", "Missing construction section");
		}
	}

	private void checkSettingsSection(Node node) {
		if (!node.attrs().isEmpty()) {
			refuse(Code.UNSUPPORTED_XML, node.name(), "Unsupported settings section attributes");
			return;
		}
		node.children().forEach(child -> checkSettingsChild(node.name(), node.name(), child));
	}

	private void checkSettingsChild(String parent, String parentPath, Node node) {
		String name = node.name();
		String allowed = SETTINGS_ATTRIBUTES.get(parent + "/" + name);
		String path = parentPath + "/" + name;
		if (allowed == null) {
			refuse(Code.UNSUPPORTED_XML, path, "Unrecognized settings element");
			return;
		}
		if (!node.attributes(allowed)) {
			refuse(Code.UNSUPPORTED_XML, path, "Unsupported settings attributes");
			return;
		}
		if (SETTINGS_CONTAINERS.contains(name)) {
			node.children().forEach(child -> checkSettingsChild(name, path, child));
			return;
		}
		if (!node.children().isEmpty()) {
			refuse(Code.UNSUPPORTED_XML, path, "Unexpected nested settings");
			return;
		}
		String refusal = switch (name) {
		case "axis" -> numbers(node, "tickDistance", "tickExpression")
				? null : "Unsupported axis tick distance or expression";
		case "coordSystem" -> numbers(node, "xZero", "yZero", "scale", "yscale")
				? null : "Unsupported coordinate system values";
		case "view" -> viewRefusal(node);
		case "viewNumber" -> Set.of("1", "2").contains(node.attr("viewNo"))
				? null : "Unsupported graphics view number";
		case "startAnimation" -> "false".equals(node.attr("val"))
				? null : "The passive trial cannot start animations";
		default -> null;
		};
		if (refusal != null) {
			refuse(Code.UNSUPPORTED_XML, path, refusal);
		}
	}

	private static String viewRefusal(Node node) {
		boolean hidden = "false".equals(node.attr("visible"));
		if ("true".equals(node.attr("inframe"))) {
			// The native layout reader records this flag but only opens visible views.
			return hidden ? null : "Visible detached views are outside the passive trial";
		}
		if (!optionalFalse(node, "inframe")) {
			return "Unsupported detached view setting";
		}
		return hidden || Set.of("1", "2", "16", "32", "64").contains(node.attr("id"))
				? null : "Visible view is outside the passive trial";
	}

	private boolean construction(Node construction) {
		if (!construction.attributes("author date title")
				|| construction.children().size() > 3 * MAX_OBJECTS) {
			return false;
		}
		Set<String> singletons = new HashSet<>();
		for (Node node : construction.children()) {
			switch (node.name()) {
			case "expression":
				if (!expression(node)) {
					refuse(Code.UNSUPPORTED_EXPRESSION, "expression",
							"Definition outside the passive expression vocabulary");
				}
				break;
			case "command":
				if (!command(node)) {
					String name = node.attr("name");
					refuse(Code.UNSUPPORTED_COMMAND, "command",
							label(name) ? name : "Command outside the passive vocabulary");
				}
				break;
			case "element":
				if (!element(node, false)) {
					refuse(Code.UNSUPPORTED_XML, "element", "Object type or property outside the pilot");
				}
				break;
			case UnitStateXml.ELEMENT:
				if (!singletons.add(node.name()) || !validUnits(node)) {
					refuse(Code.UNSUPPORTED_XML, node.name(), "Unsupported native unit metadata");
				}
				break;
			case "geocedgSpatial":
				if (!singletons.add(node.name()) || !constructionIdentity(node)) {
					refuse(Code.UNSUPPORTED_XML, node.name(),
							"Only construction-defined passive geo records are supported");
				}
				break;
			default:
				refuse(Code.UNSUPPORTED_XML, node.name(), "Unsupported construction element");
				break;
			}
			if (labels.size() > MAX_OBJECTS || declarations.size() > MAX_OBJECTS) {
				refuse(Code.LIMIT_EXCEEDED, "construction", "Object count limit");
				break;
			}
		}
		for (var reference : referencedLabels.entrySet()) {
			if (!declared.contains(reference.getKey())
					|| !reference.getValue().equals(labels.get(reference.getKey()))) {
				refuse(Code.INVALID_XML, "construction", "Missing or incompatible reference "
						+ reference.getKey());
			}
		}
		// Every serialized creation must be represented by an element declaration.
		return declared.containsAll(labels.keySet());
	}

	private static boolean validUnits(Node node) {
		if (!node.children().isEmpty()) {
			return false;
		}
		try {
			UnitStateXml.read(node.attrs());
			return true;
		} catch (UnitMetadataException invalid) {
			return false;
		}
	}

	private boolean checkDefaults(Node root) {
		if (!root(root) || root.attrs().containsKey("id") || root.children().size() != 1
				|| !"defaults".equals(root.children().get(0).name())
				|| !root.children().get(0).attrs().isEmpty()) {
			refuse(Code.UNSUPPORTED_XML, "defaults", "Unsupported construction defaults");
			return false;
		}
		Set<String> ids = new HashSet<>();
		for (Node node : root.children().get(0).children()) {
			if (!ids.add(node.attr("default")) || !element(node, true)) {
				refuse(Code.UNSUPPORTED_XML, "defaults", "Unsupported native defaults prototype");
			}
		}
		return safe;
	}

	private boolean constructionIdentity(Node section) {
		return section.attributes("version") && "1".equals(section.attr("version"))
				&& section.children().size() <= MAX_OBJECTS
				&& section.children().stream().allMatch(this::geoIdentity);
	}

	private boolean geoIdentity(Node node) {
		return "geo".equals(node.name()) && node.leaf("id provider family schema schemaVersion authority"
				+ " bindingRole outputRole cardinality dependencies definitionRevision topologyRevision"
				+ " copySourceId")
				&& id(node.attr("id")) && "geocedg-construction-provider/v1".equals(node.attr("provider"))
				&& "geocedg-construction-geo".equals(node.attr("schema"))
				&& "2".equals(node.attr("schemaVersion"))
				&& Set.of("POINT", "NUMERIC", "LIST", "ANGLE", "LINE", "SEGMENT",
						"VECTOR", "BOOLEAN", "TEXT", "CONIC").contains(node.attr("family"))
				&& "CONSTRUCTION_DEFINED".equals(node.attr("authority"))
				&& "NOT_APPLICABLE".equals(node.attr("bindingRole"))
				&& "VALUE".equals(node.attr("outputRole")) && "1".equals(node.attr("cardinality"))
				&& (node.attr("copySourceId").isEmpty() || id(node.attr("copySourceId")))
				&& (node.attr("dependencies").isEmpty()
						|| List.of(node.attr("dependencies").split(" ")).stream()
								.allMatch(GeoCeDGInspectionXml::id));
	}

	private boolean element(Node node, boolean defaults) {
		String type = node.attr("type");
		String label = node.attr("label");
		if (!"element".equals(node.name())) {
			return false;
		}
		if (defaults) {
			if (!node.attributes("type label default")
					|| !type.equals(DEFAULT_TYPES.get(node.attr("default")))) {
				return false;
			}
		} else {
			String durable = node.attr("geocedgId");
			if (!label(label) || !declared.add(label)) {
				return false;
			}
			declarations.add(new Declaration(label, type, durable.isEmpty() ? null : durable));
			if (!node.attributes("type label geocedgId") || !GEO_TYPES.contains(type)
					|| labels.containsKey(label) && !labels.get(label).equals(type)
					|| !durable.isEmpty() && !id(durable)) {
				return false;
			}
			labels.put(label, type);
		}
		Set<String> properties = new HashSet<>();
		return node.children().stream().allMatch(child ->
				properties.add(child.name()) && property(child, defaults, type));
	}

	private boolean property(Node node, boolean defaults, String type) {
		return switch (node.name()) {
		case "animation" -> node.leaf("step speed type playing") && optionalFalse(node, "playing")
				&& angularNumbers(node, "step", "speed");
		case "slider" -> node.leaf("min max width x y fixed horizontal absoluteScreenLocation showAlgebra")
				&& angularNumbers(node, "min", "max") && numbers(node, "width", "x", "y");
		case "value" -> node.leaf("val") && (number(node.attr("val"))
				|| Set.of("?", "NaN", "true", "false").contains(node.attr("val")));
		case "startPoint" -> !defaults && (node.leaf("exp") && reference(node.attr("exp"), "point")
				|| node.leaf("x y z") && numbers(node, "x", "y", "z"));
		case "file" -> defaults && "image".equals(type) && node.leaf("name") && node.attr("name").isEmpty();
		case "condition" -> !defaults && node.leaf("showObject") && reference(node.attr("showObject"), "boolean");
		default -> {
			String style = STYLE_ATTRIBUTES.get(node.name());
			yield style != null && node.leaf(style);
		}
		};
	}

	private boolean reference(String label, String type) {
		if (!label(label)) {
			return false;
		}
		String existing = referencedLabels.putIfAbsent(label, type);
		return existing == null || existing.equals(type);
	}

	private boolean expression(Node node) {
		String label = node.attr("label");
		String exp = node.attr("exp");
		String type = node.attr("type");
		if (!node.leaf("label exp type") || !label(label) || labels.containsKey(label)
				|| !type.isEmpty() && !GEO_TYPES.contains(type) || exp.length() > 256) {
			return false;
		}
		String inferred = expressionType(label, exp);
		if (inferred == null || !type.isEmpty() && !type.equals(inferred)) {
			return false;
		}
		labels.put(label, inferred);
		return true;
	}

	private String expressionType(String label, String exp) {
		if (exp.startsWith("\"")) {
			return text(exp) ? "text" : null;
		}
		String function = label + "(x) = ";
		if (exp.startsWith(function)) {
			return arithmetic(exp.substring(function.length()), Set.of("x"), SCALAR_TYPES, "")
					? "function" : null;
		}
		if (exp.startsWith("y = ")) {
			return arithmetic(exp.substring(4), Set.of("x"), SCALAR_TYPES, "") ? "line" : null;
		}
		if (exp.startsWith("{") && exp.endsWith("}")) {
			return arithmetic(exp, Set.of(), TUPLE_TYPES, ",{}") ? "list" : null;
		}
		if (exp.startsWith("(") && exp.endsWith(")") && exp.contains(",")) {
			return coordinatePair(exp, Set.of()) ? "point" : null;
		}
		if (exp.contains("<") || exp.contains(">") || Set.of("true", "false").contains(exp)) {
			return arithmetic(exp, Set.of(), ARITHMETIC_TYPES, "<>=") ? "boolean" : null;
		}
		if (exp.endsWith("°")) {
			return arithmetic(exp, Set.of(), SCALAR_TYPES, "°") ? "angle" : null;
		}
		return arithmetic(exp, Set.of(), SCALAR_TYPES, "") ? "numeric" : null;
	}

	private boolean text(String exp) {
		int close = exp.indexOf('"', 1);
		if (close < 0 || !SAFE_TEXT.matcher(exp.substring(1, close)).matches()) {
			return false;
		}
		String tail = exp.substring(close + 1);
		return tail.isEmpty() || tail.startsWith(" + ") && known(tail.substring(3));
	}

	private boolean command(Node node) {
		String name = node.attr("name");
		if (!node.attributes("name") || node.children().size() != 2
				|| !"input".equals(node.children().get(0).name())
				|| !"output".equals(node.children().get(1).name())) {
			return false;
		}
		List<String> input = ports(node.children().get(0), false);
		List<String> output = ports(node.children().get(1), "Sequence".equals(name));
		if (input.isEmpty() || output.isEmpty() || new HashSet<>(output).size() != output.size()
				|| output.stream().anyMatch(label -> !label(label) || labels.containsKey(label))
				|| !allowedCommand(name, input, output.size())) {
			return false;
		}
		for (int index = 0; index < output.size(); index++) {
			labels.put(output.get(index), commandOutputType(name, index));
		}
		return true;
	}

	private static String commandOutputType(String name, int port) {
		return switch (name) {
		case "Angle" -> "angle";
		case "CircleArc", "CircleSector", "CircumcircleArc", "CircumcircleSector" -> "conicpart";
		case "Line" -> "line";
		case "Ray" -> "ray";
		case "Segment" -> "segment";
		case "Vector" -> "vector";
		case "Midpoint", "Point" -> "point";
		case "Circle", "Ellipse", "Hyperbola", "Parabola" -> "conic";
		case "Text" -> "text";
		case "CurveCartesian" -> "curvecartesian";
		case "Sequence" -> "list";
		case "Polygon" -> port == 0 ? "polygon" : "segment";
		default -> throw new IllegalArgumentException("Unclassified command output");
		};
	}

	private boolean allowedCommand(String name, List<String> in, int outputs) {
		int n = in.size();
		if ("Polygon".equals(name)) {
			return n >= 3 && outputs == n + 1 && in.stream().allMatch(arg -> ofType(arg, "point"));
		}
		if (outputs != 1) {
			return false;
		}
		return switch (name) {
		case "Angle", "CircleArc", "CircleSector", "CircumcircleArc", "CircumcircleSector" ->
				n == 3 && in.stream().allMatch(arg -> ofType(arg, "point"));
		case "Line", "Ray", "Segment", "Vector" ->
				n == 2 && in.stream().allMatch(arg -> ofType(arg, "point"));
		case "Midpoint" -> n == 2 && in.stream().allMatch(arg -> ofType(arg, "point"))
				|| n == 1 && ofType(in.get(0), "segment");
		case "Circle" -> n == 2 && ofType(in.get(0), "point")
				&& (ofType(in.get(1), "point") || numericInput(in.get(1)));
		case "Ellipse", "Hyperbola" -> n == 3 && ofType(in.get(0), "point")
				&& ofType(in.get(1), "point") && numericInput(in.get(2));
		case "Parabola" -> n == 2 && ofType(in.get(0), "point") && ofType(in.get(1), "line");
		case "Point" -> n == 2 && ofType(in.get(0), "conicpart", "conic", "function", "segment", "line")
				&& numericInput(in.get(1));
		case "Text" -> n == 1 && numericInput(in.get(0));
		case "CurveCartesian" -> n == 5 && label(in.get(2))
				&& arithmetic(in.get(0), Set.of(in.get(2)), SCALAR_TYPES, "")
				&& arithmetic(in.get(1), Set.of(in.get(2)), SCALAR_TYPES, "")
				&& numericInput(in.get(3)) && numericInput(in.get(4));
		case "Sequence" -> sequence(in);
		default -> false;
		};
	}

	private boolean sequence(List<String> input) {
		if (input.size() != 4 || !label(input.get(1))
				|| !number(input.get(2)) || !number(input.get(3))) {
			return false;
		}
		Set<String> local = Set.of(input.get(1));
		if (!arithmetic(input.get(0), local, SCALAR_TYPES, "")
				&& !coordinatePair(input.get(0), local)) {
			return false;
		}
		double start = Double.parseDouble(input.get(2));
		double end = Double.parseDouble(input.get(3));
		return Math.abs(start) <= 10000 && Math.abs(end) <= 10000
				&& Math.abs(end - start) < MAX_SEQUENCE_COUNT;
	}

	private boolean coordinatePair(String expression, Set<String> local) {
		int comma = expression.indexOf(',');
		return expression.startsWith("(") && expression.endsWith(")") && comma > 1
				&& arithmetic(expression.substring(1, comma), local, SCALAR_TYPES, "")
				&& arithmetic(expression.substring(comma + 1, expression.length() - 1),
						local, SCALAR_TYPES, "");
	}

	private static List<String> ports(Node node, boolean sequenceOutput) {
		int count = node.attrs().size();
		if (!node.children().isEmpty() || count > MAX_PORTS) {
			return List.of();
		}
		if (node.attrs().containsKey("randomResult")) {
			if (!sequenceOutput || !literalList(node.attr("randomResult"))) {
				return List.of();
			}
			count--;
		}
		List<String> ports = new ArrayList<>(count);
		for (Map.Entry<String, String> attribute : node.attrs().entrySet()) {
			if ("randomResult".equals(attribute.getKey())) {
				continue;
			}
			// The native reader consumes ports in XML order.
			if (!("a" + ports.size()).equals(attribute.getKey())
					|| attribute.getValue().length() > 256) {
				return List.of();
			}
			ports.add(attribute.getValue());
		}
		return ports;
	}

	private static boolean literalList(String expression) {
		if (!expression.startsWith("{") || !expression.endsWith("}")) {
			return false;
		}
		String values = expression.substring(1, expression.length() - 1);
		String[] parts = values.split(",", -1);
		return values.isEmpty() || parts.length <= MAX_SEQUENCE_COUNT
				&& List.of(parts).stream().allMatch(part -> number(part.strip()));
	}

	/**
	 * Token gate for arithmetic, comparisons, 2D tuples and finite lists.
	 * Known labels are only available once introduced in this document.
	 */
	private boolean arithmetic(String expression, Set<String> local, Set<String> referenceTypes,
			String extraTokens) {
		if (expression.isEmpty() || expression.length() > 256) {
			return false;
		}
		var matcher = TOKEN.matcher(expression);
		var nesting = new ArrayDeque<String>();
		int next = 0;
		String previous = "";
		while (next < expression.length()) {
			if (expression.charAt(next) == ' ') {
				next++;
				continue;
			}
			if (!matcher.region(next, expression.length()).lookingAt()) {
				return false;
			}
			String token = matcher.group();
			next = matcher.end();
			if (label(token)) {
				if ((!local.contains(token) && !typedReference(token, referenceTypes)
						&& !Set.of("true", "false").contains(token)) || label(previous)) {
					return false;
				}
			} else if ("(".equals(token) && label(previous)
					|| ",{}<>°=".contains(token) && !extraTokens.contains(token)) {
				return false;
			}
			if ("(".equals(token) || "{".equals(token)) {
				nesting.addLast(token);
				if (nesting.size() > 8) {
					return false;
				}
			} else if (")".equals(token) || "}".equals(token)) {
				if (nesting.isEmpty()
						|| !(")".equals(token) ? "(" : "{").equals(nesting.removeLast())) {
					return false;
				}
			} else if (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.') {
				if (!number(token)) {
					return false;
				}
			}
			previous = token;
		}
		return nesting.isEmpty() && !previous.isEmpty();
	}

	private boolean numericInput(String input) {
		return number(input) || ofType(input, "angle", "numeric");
	}

	private boolean known(String address) {
		return labels.containsKey(address) && declared.contains(address);
	}

	private boolean typedReference(String address, Set<String> types) {
		return known(address) && types.contains(labels.get(address));
	}

	private boolean ofType(String address, String... types) {
		return known(address) && List.of(types).contains(labels.get(address));
	}

	private static boolean number(String value) {
		return value.length() <= 32 && NUMBER.matcher(value).matches()
				&& Double.isFinite(Double.parseDouble(value));
	}

	private static boolean numbers(Node node, String... keys) {
		for (String key : keys) {
			if (node.attrs().containsKey(key) && !number(node.attr(key))) {
				return false;
			}
		}
		return true;
	}

	private static boolean angularNumbers(Node node, String... keys) {
		for (String key : keys) {
			String value = node.attr(key);
			if (node.attrs().containsKey(key) && !number(value)
					&& !(value.endsWith("°") && number(value.substring(0,
							value.length() - (value.endsWith("*°") ? 2 : 1))))) {
				return false;
			}
		}
		return true;
	}

	private static boolean optionalFalse(Node node, String key) {
		return !node.attrs().containsKey(key) || "false".equals(node.attr(key));
	}

	private static boolean label(String label) {
		return label.length() <= 64 && LABEL.matcher(label).matches();
	}

	private static boolean id(String id) {
		return DURABLE_ID.matcher(id).matches();
	}

	private static boolean empty(String value) {
		return value == null || value.isEmpty();
	}

	private void refuse(Code code, String location, String detail) {
		safe = false;
		addFinding(findings, code, location.isEmpty() ? entry : entry + "/" + location, detail);
	}
}

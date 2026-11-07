/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geogebra.common.io.MyXMLio.JAVASCRIPT_FILE;
import static org.geogebra.common.io.MyXMLio.XML_FILE;
import static org.geogebra.common.io.MyXMLio.XML_FILE_DEFAULTS_2D;
import static org.geogebra.common.io.MyXMLio.XML_FILE_DEFAULTS_3D;
import static org.geogebra.common.io.MyXMLio.XML_FILE_MACRO;
import static org.geogebra.common.io.MyXMLio.XML_FILE_THUMBNAIL;
import static org.geogebra.common.main.GeoGebraPreferencesXML.ANGLE_DEGREES_MINUTES_SECONDS_XML_NAME;
import static org.geogebra.common.main.GeoGebraPreferencesXML.ANGLE_DEGREE_XML_NAME;
import static org.geogebra.common.main.GeoGebraPreferencesXML.ANGLE_RADIANT_XML_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geocedg.common.kernel.geos.V2StyleDefaults;
import org.geogebra.common.geogebra3D.kernel3D.ConstructionDefaults3D;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.CommandsConstants;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.headless.GFileHandler;

/** Non-evaluating source guard for the cross-mode CU4 insertion class. */
final class GeoCeDGAngularSourceArchive {
	private static final int UNKNOWN_MODE = -1;
	private static final int MAX_ARCHIVE_BYTES = 8 * 1024 * 1024;
	private static final int MAX_INFLATED_BYTES = 8 * 1024 * 1024;
	private static final int MAX_ENTRY_BYTES = 4 * 1024 * 1024;
	private static final int MAX_ENTRIES = 32;
	private static final int MAX_XML_DEPTH = 32;
	private static final String DECIMAL = "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)"
			+ "(?:[eE][+-]?[0-9]+)?";
	private static final String LABEL = "[\\p{L}][\\p{L}\\p{M}\\p{N}_']*"
			+ "(?:\\{[\\p{L}\\p{M}\\p{N}]+\\})?";
	private static final String COMPONENT = "(?:" + DECIMAL + "|" + LABEL + ")";
	private static final Pattern NUMBER = Pattern.compile(DECIMAL);
	private static final Pattern IDENTIFIER = Pattern.compile(LABEL);
	private static final Pattern PURE_EXPRESSION = Pattern.compile(DECIMAL + "(?:\\*?°)?"
			+ "|(?:sin|cos)\\(" + LABEL + "\\)"
			+ "|\\( *" + COMPONENT + " *, *" + COMPONENT + " *\\)");
	private static final Pattern CARTESIAN_LITERAL = Pattern.compile(
			"\\( *" + DECIMAL + " *, *" + DECIMAL + " *\\)");
	private static final Set<String> DOCUMENT_SECTIONS = Set.of("gui", "euclidianView",
			"algebraView", "kernel", "tableview", "scripting", "construction");
	private static final Set<String> KERNEL_SETTINGS = Set.of("angleUnit", "algebraStyle",
			"coordStyle", "angleFromInvTrig", "continuous", "usePathAndRegionParameters",
			"decimals", "significantfigures", "startAnimation", "localization", "casSettings");
	private static final Set<String> GEO_TYPES = Set.of("angle", "numeric", "point", "text");
	private static final Set<String> STYLE_LEAVES = Set.of("algebra", "angleStyle", "arcSize",
			"autocolor", "auxiliary", "bgColor", "caption", "checkbox", "coordStyle",
			"coords", "decoration", "eigenvectors", "endStyle", "eqnStyle", "fading",
			"fixed", "font", "headStyle", "inBackground", "keepTypeOnTransform", "labelMode",
			"labelOffset", "layer", "lineStyle", "matrix", "outlyingIntersections", "pointSize",
			"pointStyle", "selectionAllowed", "show", "showTrimmed", "simplifyCoefficients",
			"startStyle", "symbolic", "tooltipMode");
	private static final Set<String> STATIC_COLOR_ATTRIBUTES = Set.of("r", "g", "b",
			"alpha", "hatchAngle", "hatchDistance", "fillType", "fillSymbol", "inverseFill");
	private static final Set<Integer> NATIVE_PROTOTYPES = Set.of(
			ConstructionDefaults.DEFAULT_POINT_FREE, ConstructionDefaults.DEFAULT_POINT_DEPENDENT,
			ConstructionDefaults.DEFAULT_POINT_ON_PATH, ConstructionDefaults.DEFAULT_POINT_IN_REGION,
			ConstructionDefaults.DEFAULT_POINT_COMPLEX, ConstructionDefaults.DEFAULT_POINT_PREVIEW,
			ConstructionDefaults.DEFAULT_LINE, ConstructionDefaults.DEFAULT_SEGMENT,
			ConstructionDefaults.DEFAULT_INEQUALITY, ConstructionDefaults.DEFAULT_INEQUALITY_1VAR,
			ConstructionDefaults.DEFAULT_RAY, ConstructionDefaults.DEFAULT_VECTOR,
			ConstructionDefaults.DEFAULT_CONIC, ConstructionDefaults.DEFAULT_CONIC_SECTOR,
			ConstructionDefaults.DEFAULT_NUMBER, ConstructionDefaults.DEFAULT_ANGLE,
			ConstructionDefaults.DEFAULT_FUNCTION, ConstructionDefaults.DEFAULT_FUNCTION_NVAR,
			ConstructionDefaults.DEFAULT_POLYGON, ConstructionDefaults.DEFAULT_POLYLINE,
			ConstructionDefaults.DEFAULT_LOCUS, ConstructionDefaults.DEFAULT_TEXT,
			ConstructionDefaults.DEFAULT_IMAGE, ConstructionDefaults.DEFAULT_BOOLEAN,
			ConstructionDefaults.DEFAULT_LIST, ConstructionDefaults.DEFAULT_CURVE_CARTESIAN,
			ConstructionDefaults.DEFAULT_POLYHEDRON, ConstructionDefaults.DEFAULT_PYRAMID_AND_CONE,
			ConstructionDefaults.DEFAULT_PRISM_AND_CYLINDER, ConstructionDefaults.DEFAULT_ARCHIMDEAN_SOLID,
			ConstructionDefaults3D.DEFAULT_INTERSECTION_CURVE, ConstructionDefaults3D.DEFAULT_AXIS3D,
			ConstructionDefaults3D.DEFAULT_CURVECARTESIAN3D, ConstructionDefaults3D.DEFAULT_PLANE3D,
			ConstructionDefaults3D.DEFAULT_QUADRIC, ConstructionDefaults3D.DEFAULT_SURFACECARTESIAN3D,
			ConstructionDefaults3D.DEFAULT_NET);

	enum Status {
		ORDINARY, ADAPTABLE, REJECTED
	}

	// Priority is independent of ZIP entry and construction order.
	enum Refusal {
		INVALID("Insert.Reject.Invalid"),
		SCRIPTS("Insert.Reject.Scripts"),
		SCRIPT_LIBRARY("Insert.Reject.ScriptLibrary"),
		P4("Insert.Reject.Unsupported"),
		MACROS("Insert.Angle.SourceTools"),
		MODE("Insert.Angle.Unverified"),
		UNDETERMINED("Insert.Angle.EligibilityUnknown");

		final String key;

		Refusal(String key) {
			this.key = key;
		}
	}

	record Finding(Refusal cause, String detail) {
		boolean independentlyKnown() {
			return cause != Refusal.MODE && cause != Refusal.UNDETERMINED;
		}
	}

	record Inspection(Status status, int sourceMode, boolean differentModes,
			List<Finding> findings, String detail) {
	}

	private enum Section {
		DOCUMENT, DEFAULTS, MACRO_METADATA
	}

	private GeoCeDGAngularSourceArchive() {
	}

	/** Inspect the operation's captured bytes without starting any native reader. */
	static Inspection inspect(byte[] bytes, int targetMode) {
		XmlScan main = new XmlScan(Section.DOCUMENT);
		boolean safe = bytes.length <= MAX_ARCHIVE_BYTES && scanZip(bytes, main);
		boolean identifiedMode = main.documentEntries == 1 && main.modeTags == 1
				&& main.kernels == 1 && main.sourceMode != UNKNOWN_MODE;
		if (!main.angleSlider || identifiedMode && main.sourceMode == targetMode) {
			return new Inspection(Status.ORDINARY, main.sourceMode, false, List.of(), "");
		}
		boolean differentModes = identifiedMode && knownMode(targetMode);
		if (!safe || !identifiedMode || !knownMode(targetMode)) {
			if (!differentModes) {
				main.refuse(Refusal.MODE, null);
			}
			return new Inspection(Status.REJECTED, main.sourceMode, differentModes,
					main.findings(), main.detail);
		}
		return new Inspection(Status.ADAPTABLE, main.sourceMode, true, List.of(), "");
	}

	private static boolean scanZip(byte[] bytes, XmlScan main) {
		Set<String> names = new HashSet<>();
		int remaining = MAX_INFLATED_BYTES;
		int entryCount = 0;
		boolean safe = true;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (++entryCount > MAX_ENTRIES) {
					main.refuse(Refusal.UNDETERMINED, null);
					return false;
				}
				String name = entry.getName();
				if (!names.add(name)) {
					main.refuse(Refusal.INVALID, null);
					safe = false;
				}
				if (XML_FILE.equals(name)) {
					main.documentEntries++;
				}
				int limit = Math.min(remaining, MAX_ENTRY_BYTES);
				byte[] raw = zip.readNBytes(limit + 1);
				if (raw.length > limit) {
					main.refuse(Refusal.UNDETERMINED, null);
					return false;
				}
				remaining -= raw.length;
				try {
					if (entry.isDirectory()) {
						main.refuse(Refusal.UNDETERMINED, null);
						safe = false;
					} else if (!safeEntry(name, raw, main)) {
						safe = false;
					}
				} catch (IOException malformedEntry) {
					// Keep scanning for the construction header even if a prior entry was unsafe.
					safe = false;
					main.refuse(Refusal.INVALID, null);
				}
				zip.closeEntry();
			}
		} catch (IOException | IllegalArgumentException malformed) {
			main.refuse(Refusal.INVALID, null);
			return false;
		}
		return safe && main.safe && main.documentEntries == 1;
	}

	private static boolean safeEntry(String name, byte[] raw, XmlScan main) throws IOException {
		return switch (name) {
		case XML_FILE -> scanXml(raw, main);
		case XML_FILE_DEFAULTS_2D, XML_FILE_DEFAULTS_3D -> main.checkStructure(
				scanXml(raw, new XmlScan(Section.DEFAULTS)), name);
		case JAVASCRIPT_FILE -> {
			String javascript = utf8(raw);
			boolean standard = Kernel.defaultLibraryJavaScript.equals(javascript)
					|| (Kernel.defaultLibraryJavaScript + "\n").equals(javascript);
			if (!standard) {
				main.refuse(Refusal.SCRIPT_LIBRARY, null);
			}
			yield standard;
		}
		case V2StyleDefaults.ZIP_ENTRY -> main.checkStructure(safeCurveDefaults(raw), name);
		case XML_FILE_MACRO -> {
			XmlScan macros = new XmlScan(Section.MACRO_METADATA);
			boolean readable = scanXml(raw, macros);
			if (readable && !macros.macroNames.isEmpty()) {
				macros.macroNames.forEach(command -> main.refuse(Refusal.MACROS, command));
			} else {
				main.refuse(Refusal.MACROS, XML_FILE_MACRO);
			}
			yield false;
		}
		case XML_FILE_THUMBNAIL -> true;
		default -> main.checkStructure(false, safeDiagnosticName(name));
		};
	}

	private static boolean safeCurveDefaults(byte[] raw) throws IOException {
		if (raw.length > V2StyleDefaults.MAX_BYTES) {
			return false;
		}
		try {
			V2StyleDefaults.read(utf8(raw));
			return true;
		} catch (XMLParseException malformed) {
			return false;
		}
	}

	private static String utf8(byte[] raw) throws IOException {
		return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(raw)).toString();
	}

	private static boolean scanXml(byte[] raw, XmlScan scan) throws IOException {
		XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		try {
			XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(utf8(raw)));
			try {
				String encoding = reader.getCharacterEncodingScheme();
				scan.safe &= encoding == null || "UTF-8".equalsIgnoreCase(encoding);
				while (reader.hasNext()) {
					switch (reader.next()) {
					case XMLStreamConstants.START_ELEMENT:
						if (scan.path.size() >= MAX_XML_DEPTH) {
							return scan.checkStructure(false, null);
						}
						scan.start(reader);
						break;
					case XMLStreamConstants.END_ELEMENT:
						scan.end();
						break;
					case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA:
						scan.safe &= reader.isWhiteSpace();
						break;
					case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE:
						scan.refuse(Refusal.INVALID, null);
						scan.safe = false;
						break;
					case XMLStreamConstants.PROCESSING_INSTRUCTION:
						scan.safe = scan.checkStructure(false, null);
						break;
					default:
						break;
					}
				}
			} finally {
				reader.close();
			}
		} catch (XMLStreamException malformed) {
			throw new IOException("Invalid insertion XML", malformed);
		}
		return scan.safe;
	}

	private static final class XmlScan {
		private final ArrayDeque<String> path = new ArrayDeque<>();
		private final Section section;
		private final Set<Integer> prototypeIds = new HashSet<>();
		private final Set<String> angleSliders = new HashSet<>();
		private final Set<String> macroNames = new LinkedHashSet<>();
		private final Map<Refusal, Set<String>> refusals = new EnumMap<>(Refusal.class);
		private boolean safe = true;
		private boolean angleElement;
		private boolean angleElementHasSlider;
		private boolean angleSlider;
		private int documentEntries;
		private int sourceMode = UNKNOWN_MODE;
		private int modeTags;
		private int kernels;
		private int constructions;
		private int defaultsSections;
		private String documentSection = "";
		private String elementLabel;
		private String commandName;
		private int commandParts;
		private String detail = "";

		private XmlScan(Section section) {
			this.section = section;
		}

		private void start(XMLStreamReader reader) {
			String name = reader.getLocalName();
			String parent = path.peekLast();
			int depth = path.size();
			boolean script = section != Section.MACRO_METADATA
					&& (ScriptType.getTypeWithXMLName(name) != null || "listener".equals(name));
			if (script && section == Section.DOCUMENT && "construction".equals(documentSection)
					&& "element".equals(parent)) {
				refuse(Refusal.SCRIPTS, elementLabel == null ? name : elementLabel);
			}
			safe &= checkStructure(plainXml(reader, depth), null) && !script;
			if (depth == 0) {
				safe &= checkStructure("geogebra".equals(name), null);
			} else {
				safe &= switch (section) {
				case DOCUMENT -> documentNode(reader, name, parent, depth);
				case DEFAULTS -> defaultNode(reader, name, parent, depth);
				case MACRO_METADATA -> depth > 1 || macroName(reader, name);
				};
			}
			path.addLast(name);
		}

		private void end() {
			String name = path.removeLast();
			if (section != Section.DOCUMENT || path.size() != 2
					|| !"construction".equals(documentSection)) {
				return;
			}
			if ("command".equals(name) && commandParts != 2) {
				safe = false;
				note(commandName);
			}
			if ("element".equals(name) && angleElement && !angleElementHasSlider) {
				// Without a slider, admission can depend on the precise angle producer.
				safe = checkStructure(false, objectDetail(elementLabel, "angle"));
			}
		}

		private void note(String object) {
			if (detail.isEmpty() && object != null) {
				detail = object;
			}
		}

		private void refuse(Refusal cause, String object) {
			Set<String> details = refusals.computeIfAbsent(cause, ignored -> new LinkedHashSet<>());
			if (object != null && details.size() < 4) {
				details.add(object);
			}
		}

		private List<Finding> findings() {
			return refusals.entrySet().stream().map(entry ->
					new Finding(entry.getKey(), String.join(", ", entry.getValue()))).toList();
		}

		private boolean checkStructure(boolean allowed, String object) {
			if (!allowed) {
				refuse(Refusal.UNDETERMINED, object);
			}
			return allowed;
		}

		private boolean macroName(XMLStreamReader reader, String name) {
			String command = safeDiagnosticName(attr(reader, "cmdName"));
			return "macro".equals(name) && command != null && macroNames.add(command);
		}

		private boolean documentNode(XMLStreamReader reader, String name, String parent, int depth) {
			if (depth == 1) {
				documentSection = name;
			}
			if ("angleUnit".equals(name)) {
				modeTags++;
				boolean nativeLocation = depth == 2 && "kernel".equals(parent)
						&& "geogebra".equals(path.peekFirst()) && reader.getAttributeCount() == 1;
				sourceMode = nativeLocation ? mode(attr(reader, "val")) : UNKNOWN_MODE;
				return nativeLocation;
			}
			if (depth == 1) {
				return switch (name) {
				case "kernel" -> ++kernels == 1;
				case "construction" -> ++constructions == 1;
				case "tableview" -> attr(reader, "xValues") == null
						&& decimal(attr(reader, "min")) && decimal(attr(reader, "max"))
						&& decimal(attr(reader, "step"));
				default -> checkStructure(DOCUMENT_SECTIONS.contains(name), safeDiagnosticName(name));
				};
			}
			return switch (documentSection) {
			case "construction" -> constructionNode(reader, name, parent, depth);
			case "kernel" -> depth == 2 && KERNEL_SETTINGS.contains(name)
					&& (!"startAnimation".equals(name) || "false".equals(attr(reader, "val")));
			case "euclidianView" -> checkStructure(depth == 2 && safeViewNode(reader, name), name);
			case "gui", "algebraView" -> !DOCUMENT_SECTIONS.contains(name)
					&& !"macro".equals(name) && !"defaults".equals(name);
			default -> checkStructure(false, safeDiagnosticName(documentSection));
			};
		}

		private boolean constructionNode(XMLStreamReader reader, String name, String parent, int depth) {
			if (depth == 2) {
				if ("element".equals(name)) {
					String type = attr(reader, "type");
					elementLabel = safeDiagnosticName(attr(reader, "label"));
					angleElement = "angle".equals(type);
					angleElementHasSlider = false;
					if (type != null && GEO_TYPES.contains(type) && attr(reader, "default") == null) {
						return true;
					}
					String object = objectDetail(elementLabel, safeDiagnosticName(type));
					switch (GeoCeDGInsertionAdmission.archiveType(type)) {
					case EXCLUDED -> refuse(Refusal.P4, object);
					case UNDETERMINED -> refuse(Refusal.UNDETERMINED, object);
					case POTENTIAL -> note(object);
					}
					return false;
				}
				return switch (name) {
				case "expression" -> {
					boolean pure = pureExpression(attr(reader, "exp"));
					if (!pure) {
						note(safeDiagnosticName(attr(reader, "label")));
					}
					yield pure;
				}
				case "command" -> beginCommand(reader);
				case "cascell" -> {
					refuse(Refusal.P4, "CAS");
					yield false;
				}
				case "geocedgUnits", "worksheetText" -> true;
				default -> checkStructure(false, safeDiagnosticName(name));
				};
			}
			if (depth == 3 && "command".equals(parent)) {
				return textCommandPart(reader, name);
			}
			if (depth != 3 || !"element".equals(parent)) {
				return false;
			}
			if (angleElement && "slider".equals(name)) {
				angleSlider = true;
				angleElementHasSlider = true;
				if (elementLabel != null) {
					angleSliders.add(elementLabel);
				}
			}
			boolean safeLeaf = safeGeoLeaf(reader, name, false);
			if (!safeLeaf) {
				if ("slider".equals(name) || "animation".equals(name) || "value".equals(name)) {
					return checkStructure(false, elementLabel);
				}
				note(elementLabel);
			}
			return safeLeaf;
		}

		private boolean beginCommand(XMLStreamReader reader) {
			commandName = safeDiagnosticName(attr(reader, "name"));
			commandParts = 0;
			boolean text = "Text".equals(commandName) && reader.getAttributeCount() == 1;
			if (!text) {
				String internal = Commands.lookupInternal(commandName);
			if (internal == null
					|| Commands.valueOf(internal).getTable() == CommandsConstants.TABLE_SCRIPTING) {
					refuse(Refusal.UNDETERMINED, commandName);
				}
				note(commandName);
			}
			return text;
		}

		private boolean textCommandPart(XMLStreamReader reader, String name) {
			int part = ++commandParts;
			String label = safeDiagnosticName(attr(reader, "a0"));
			boolean valid = "Text".equals(commandName) && reader.getAttributeCount() == 1
					&& label != null && (part == 1 && "input".equals(name) && angleSliders.contains(label)
							|| part == 2 && "output".equals(name));
			if (!valid) {
				note(objectDetail(label, commandName));
			}
			return valid;
		}

		private boolean defaultNode(XMLStreamReader reader, String name, String parent, int depth) {
			if (depth == 1) {
				return "defaults".equals(name) && ++defaultsSections == 1;
			}
			if (depth == 2 && "defaults".equals(parent) && "element".equals(name)) {
				String id = attr(reader, "default");
				if (id == null || id.length() > 4 || !id.matches("[0-9]+")) {
					return false;
				}
				int type = Integer.parseInt(id);
				return NATIVE_PROTOTYPES.contains(type) && prototypeIds.add(type);
			}
			return depth == 3 && "element".equals(parent) && safeGeoLeaf(reader, name, true);
		}
	}

	private static String safeDiagnosticName(String name) {
		return name != null && name.length() <= 128 && IDENTIFIER.matcher(name).matches() ? name : null;
	}

	private static String objectDetail(String label, String type) {
		return label == null ? type : type == null ? label : label + " (" + type + ")";
	}

	private static boolean plainXml(XMLStreamReader reader, int depth) {
		if (!empty(reader.getNamespaceURI())) {
			return false;
		}
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String namespace = reader.getAttributeNamespace(i);
			if (!empty(namespace) && !(depth == 0
					&& "http://www.w3.org/2001/XMLSchema-instance".equals(namespace)
					&& "noNamespaceSchemaLocation".equals(reader.getAttributeLocalName(i)))) {
				return false;
			}
		}
		return true;
	}

	private static boolean empty(String value) {
		return value == null || value.isEmpty();
	}

	private static boolean safeViewNode(XMLStreamReader reader, String name) {
		return switch (name) {
		case "coordSystem" -> numericAttributes(reader, false, "xMin", "xMax", "yMin", "yMax");
		case "axis" -> numericAttributes(reader, true, "tickExpression");
		case "viewNumber", "size", "evSettings", "bgColor", "axesColor", "gridColor",
				"lineStyle", "gridDist", "ruler" -> true;
		default -> false;
		};
	}

	private static boolean safeGeoLeaf(XMLStreamReader reader, String name, boolean defaults) {
		return switch (name) {
		case "objColor" -> attributesIn(reader, STATIC_COLOR_ATTRIBUTES);
		case "slider" -> numericAttributes(reader, true, "min", "max")
				&& !Boolean.parseBoolean(attr(reader, "arbitraryConstant"));
		case "animation" -> numericAttributes(reader, true, "step", "speed")
				&& !Boolean.parseBoolean(attr(reader, "playing"));
		case "value" -> safeValue(reader, defaults);
		case "startPoint" -> !defaults && safeAnchor(reader);
		case "file" -> defaults && "".equals(attr(reader, "name"));
		default -> STYLE_LEAVES.contains(name);
		};
	}

	private static boolean attributesIn(XMLStreamReader reader, Set<String> allowed) {
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			if (!allowed.contains(reader.getAttributeLocalName(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean safeValue(XMLStreamReader reader, boolean defaults) {
		String value = attr(reader, "val");
		return !Boolean.parseBoolean(attr(reader, "random")) && value != null
				&& (decimal(value) || defaults && ("NaN".equals(value) || "?".equals(value)
						|| "true".equals(value) || "false".equals(value)));
	}

	private static boolean safeAnchor(XMLStreamReader reader) {
		String exp = attr(reader, "exp");
		if (exp == null) {
			exp = attr(reader, "label");
		}
		if (exp != null) {
			return exp.length() <= 512 && (IDENTIFIER.matcher(exp).matches()
					|| CARTESIAN_LITERAL.matcher(exp).matches());
		}
		return decimal(attr(reader, "x")) && decimal(attr(reader, "y"))
				&& decimal(attr(reader, "z"));
	}

	private static boolean pureExpression(String exp) {
		if (exp == null) {
			return false;
		}
		if (exp.length() >= 2 && exp.charAt(0) == '"') {
			// One native string token. A more involved expression belongs to the ordinary route.
			return exp.indexOf('"', 1) == exp.length() - 1;
		}
		return exp.length() <= 512 && PURE_EXPRESSION.matcher(exp).matches();
	}

	private static boolean numericAttributes(XMLStreamReader reader, boolean degree, String... keys) {
		for (String key : keys) {
			String value = attr(reader, key);
			if (value != null && !(decimal(value) || degree && angularLiteral(value))) {
				return false;
			}
		}
		return true;
	}

	private static boolean angularLiteral(String value) {
		int length = value.length();
		return value.endsWith("°")
				&& decimal(value.substring(0, length - (value.endsWith("*°") ? 2 : 1)));
	}

	private static boolean decimal(String value) {
		return value != null && value.length() <= 128 && NUMBER.matcher(value).matches()
				&& Double.isFinite(Double.parseDouble(value));
	}

	private static String attr(XMLStreamReader reader, String name) {
		return reader.getAttributeValue(null, name);
	}

	private static int mode(String value) {
		if (ANGLE_DEGREE_XML_NAME.equals(value)) {
			return Kernel.ANGLE_DEGREE;
		}
		if (ANGLE_RADIANT_XML_NAME.equals(value)) {
			return Kernel.ANGLE_RADIANT;
		}
		return ANGLE_DEGREES_MINUTES_SECONDS_XML_NAME.equals(value)
				? Kernel.ANGLE_DEGREES_MINUTES_SECONDS : UNKNOWN_MODE;
	}

	private static boolean knownMode(int mode) {
		return mode == Kernel.ANGLE_DEGREE || mode == Kernel.ANGLE_RADIANT
				|| mode == Kernel.ANGLE_DEGREES_MINUTES_SECONDS;
	}

	/** Load the already screened immutable bytes into a disposable helper. */
	static boolean load(AppGeoCeDG helper, byte[] bytes, int seed, int expectedMode)
			throws IOException, XMLParseException {
		if (!helper.isInsertionHelper() || !knownMode(seed) || !knownMode(expectedMode)) {
			return false;
		}
		Kernel kernel = helper.getKernel();
		kernel.setUndoActive(false);
		kernel.setAngleUnit(seed);
		LoadErrors errors = new LoadErrors();
		helper.getXMLio().setErrorHandler(errors);
		return GFileHandler.loadPreflightedNativeXML(helper, new ByteArrayInputStream(bytes))
				&& !errors.failed && !helper.getXMLio().hasErrors()
				&& kernel.getAngleUnit() == expectedMode && !kernel.wantAnimationStarted()
				&& !kernel.getAnimationManager().isRunning()
				&& !helper.getScriptManager().hasListeners();
	}

	private static final class LoadErrors implements ErrorHandler {
		private boolean failed;

		@Override
		public void showError(String message) {
			failed = true;
		}

		@Override
		public void showCommandError(String command, String message) {
			failed = true;
		}

		@Override
		public String getCurrentCommand() {
			return null;
		}

		@Override
		public boolean onUndefinedVariables(String variables, AsyncOperation<String[]> callback) {
			failed = true;
			return false;
		}

		@Override
		public void resetError() {
			// Errors are cumulative across the construction and defaults entries.
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geogebra.common.jre.headless.Utf8Control;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/**
 * PRE-G9B-R5-B deterministic fingerprints of the command-name surface.
 *
 * <p>The same canonical serializations were computed at the implementation base
 * {@code P_DOTXML} and committed as test data. The tests recompute them on the
 * candidate. Every fingerprint uses only APIs whose semantics R5-B leaves
 * unchanged: the reverse command table, the three lookup regimes, the command
 * bundles and the construction XML.
 */
final class PreG9BR5BFingerprints {

	/** Command bundle base name. */
	static final String COMMAND_BUNDLE = "org.geogebra.common.jre.properties.command";
	/** Spanish UI locale of the product. */
	static final Locale SPANISH = new Locale("es");

	/**
	 * Witness construction: label, English input with canonical heads, Spanish
	 * input with localized aliases. It covers every English-versus-internal group
	 * of ADR 0031, the GeoCeDG commands, the Length adapter, Dot, If, the Vector
	 * wrapper, dynamic text, a value-string head and a localized function name.
	 */
	static final String[][] WITNESS = {
			{"A", "A=(0,0)", "A=(0,0)"},
			{"B", "B=(4,0)", "B=(4,0)"},
			{"C", "C=(1,3)", "C=(1,3)"},
			{"Dp", "Dp=(3,1)", "Dp=(3,1)"},
			{"f", "f=Line(A,B)", "f=Recta(A,B)"},
			{"p", "p=PerpendicularLine(C,f)", "p=Perpendicular(C,f)"},
			{"m", "m=Reflect(C,f)", "m=Refleja(C,f)"},
			{"bi", "bi=AngleBisector(A,C,B)", "bi=Bisectriz(A,C,B)"},
			{"mb", "mb=PerpendicularBisector(A,B)", "mb=Mediatriz(A,B)"},
			{"ar", "ar=CircularArc(A,B,C)", "ar=ArcoCircunferencia(A,B,C)"},
			{"q", "q=Polyline(A,B,C)", "q=Poligonal(A,B,C)"},
			{"c", "c=Circle(A,2)", "c=Circunferencia(A,2)"},
			{"k", "k=Circle((0,0),sin(1))", "k=Circunferencia((0,0),sen(1))"},
			{"el", "el=Ellipse(A,B,3)", "el=Elipse(A,B,3)"},
			{"ma", "ma=MajorAxis(el)", "ma=EjeMayor(el)"},
			{"ex", "ex=LinearEccentricity(el)", "ex=SemiFocal(el)"},
			{"u", "u=(1,2)", "u=(1,2)"},
			{"v", "v=(3,4)", "v=(3,4)"},
			{"d", "d=Dot(u,v)", "d=ProductoEscalar(u,v)"},
			{"e", "e=2 Dot(u,v)", "e=2 ProductoEscalar(u,v)"},
			{"ov", "ov=PerpendicularVector(u)", "ov=VectorNormal(u)"},
			{"uv", "uv=UnitPerpendicularVector(u)", "uv=VectorNormalUnitario(u)"},
			{"w", "w=Translate(C,Vector((1,1)))", "w=Traslada(C,Vector((1,1)))"},
			{"g", "g(x)=If(x>0,x,-x)", "g(x)=Si(x>0,x,-x)"},
			{"ty", "ty=TaylorPolynomial(sin(x),0,3)", "ty=PolinomioTaylor(sen(x),0,3)"},
			{"t1", "t1=FormulaText(p)", "t1=F\u00f3rmulaTexto(p)"},
			{"t2", "t2=\"Val: \"+FormulaText(p)", "t2=\"Val: \"+F\u00f3rmulaTexto(p)"},
			{"t3", "t3=\"N: \"+Name(p)", "t3=\"N: \"+Nombre(p)"},
			{"i1", "i1=IsDefined(A)", "i1=Est\u00e1Definido(A)"},
			{"mu", "mu=mean({1,2,3})", "mu=media({1,2,3})"},
			{"q1", "q1=Quartile1({1,2,3,4})", "q1=Cuartil1({1,2,3,4})"},
			{"fl", "fl=FitLine({(1,2),(2,3),(3,5)})",
					"fl=AjusteLineal({(1,2),(2,3),(3,5)})"},
			{"bc", "bc=BinomialCoefficient(5,2)", "bc=N\u00fameroCombinatorio(5,2)"},
			{"pc", "pc=PieChart({1,2,3})", "pc=Gr\u00e1ficoCircular({1,2,3})"},
			{"s", "s=0", "s=0"},
			{"Q", "Q=(s,0)", "Q=(s,0)"},
			{"D", "D={false,{-2,2,true,true}}", "D={false,{-2,2,true,true}}"},
			{"L", "L=LocusV2(Q,s,D)", "L=LugarGeom\u00e9tricoV2(Q,s,D)"},
			{"M", "M=LocusLength(L)", "M=LongitudLugarGeom\u00e9trico(L)"},
			{"n", "n=Length(L)", "n=Longitud(L)"},
			{"S", "S=SplineV2({A,B,C,Dp},3)", "S=SplineV2({A,B,C,Dp},3)"},
			{"pp", "pp=PerpendicularPlane(A,f)", "pp=PlanoPerpendicular(A,f)"}};

	/** A stored GGBScript as XML loads it: internal and English tokens, untranslated. */
	static final String STORED_SCRIPT = "OrthogonalLine(C, f)\nPerpendicularLine(C, f)\n"
			+ "LaTeX(p)\nSplineV2({A, B, C, Dp}, 3)\nLocusV2(Q, s, D)\nPerimeter(c)\n"
			+ "Circumference(c)\nMirror(C, f)";

	/** Fingerprints computed at {@code P_DOTXML}, committed as test data. */
	static final String BASE_FIXTURE =
			"/org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-base-fingerprints.json";
	/** Spanish-UI native document saved at {@code P_DOTXML} with the witness. */
	static final String HISTORICAL_DOCUMENT =
			"/org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-historical-es.cedg";

	private PreG9BR5BFingerprints() {
		// utility class
	}

	/**
	 * @return the fingerprints computed at the implementation base
	 */
	static JSONObject baseFixture() {
		try (InputStream input = PreG9BR5BFingerprints.class
				.getResourceAsStream(BASE_FIXTURE)) {
			if (input == null) {
				throw new IllegalStateException("missing " + BASE_FIXTURE);
			}
			return new JSONObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param root fixture object
	 * @param path member names
	 * @return the nested object
	 */
	static JSONObject object(JSONObject root, String... path) {
		JSONObject current = root;
		try {
			for (String name : path) {
				current = current.getJSONObject(name);
			}
		} catch (JSONException e) {
			throw new IllegalStateException(String.join(".", path), e);
		}
		return current;
	}

	/**
	 * @param root fixture object
	 * @param path member names, the last one naming a string
	 * @return the string
	 */
	static String string(JSONObject root, String... path) {
		String[] parents = Arrays.copyOf(path, path.length - 1);
		try {
			return object(root, parents).getString(path[path.length - 1]);
		} catch (JSONException e) {
			throw new IllegalStateException(String.join(".", path), e);
		}
	}

	/**
	 * @param root fixture object
	 * @param path member names, the last one naming an integer
	 * @return the integer
	 */
	static int integer(JSONObject root, String... path) {
		String[] parents = Arrays.copyOf(path, path.length - 1);
		try {
			return object(root, parents).getInt(path[path.length - 1]);
		} catch (JSONException e) {
			throw new IllegalStateException(String.join(".", path), e);
		}
	}

	/**
	 * @param root fixture object
	 * @param path member names, the last one naming a string array
	 * @return the strings
	 */
	static List<String> strings(JSONObject root, String... path) {
		String[] parents = Arrays.copyOf(path, path.length - 1);
		try {
			JSONArray array = object(root, parents).getJSONArray(path[path.length - 1]);
			List<String> values = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				values.add(array.getString(i));
			}
			return values;
		} catch (JSONException e) {
			throw new IllegalStateException(String.join(".", path), e);
		}
	}

	/**
	 * @param spanish whether to use the localized Spanish inputs
	 * @return the witness inputs in construction order
	 */
	static String[] witnessInputs(boolean spanish) {
		String[] inputs = new String[WITNESS.length];
		for (int i = 0; i < WITNESS.length; i++) {
			inputs[i] = WITNESS[i][spanish ? 2 : 1];
		}
		return inputs;
	}

	/**
	 * @param app application with a filled command dictionary
	 * @return the reverse command table as sorted {@code key\tvalue} lines
	 */
	static String reverseTable(App app) {
		return lines(new TreeMap<>(app.getLocalization().getTranslateCommandTable()));
	}

	/**
	 * @return every enum constant and every English and Spanish command-bundle
	 *         value of an enum constant, sorted
	 */
	static SortedSet<String> tokenUniverse() {
		TreeSet<String> tokens = new TreeSet<>();
		ResourceBundle english = bundle(Locale.ENGLISH);
		ResourceBundle spanish = bundle(SPANISH);
		for (Commands command : Commands.values()) {
			tokens.add(command.name());
			addValue(tokens, english, command.name());
			addValue(tokens, spanish, command.name());
		}
		return tokens;
	}

	/**
	 * @param locale bundle locale
	 * @return the command bundle of that locale, read as UTF-8
	 */
	static ResourceBundle bundle(Locale locale) {
		return ResourceBundle.getBundle(COMMAND_BUNDLE, locale, new Utf8Control());
	}

	/**
	 * @param app application in the USER lookup regime
	 * @param tokens tokens to resolve
	 * @return sorted {@code token\tresolution} lines of the ordinary USER lookup
	 */
	static String userResolution(App app, Collection<String> tokens) {
		TreeMap<String, String> resolved = new TreeMap<>();
		for (String token : tokens) {
			resolved.put(token, String.valueOf(app.getReverseCommand(token)));
		}
		return lines(resolved);
	}

	/**
	 * @param tokens tokens to resolve
	 * @return sorted {@code token\tresolution} lines of the SCRIPT lookup
	 */
	static String scriptResolution(Collection<String> tokens) {
		TreeMap<String, String> resolved = new TreeMap<>();
		for (String token : tokens) {
			resolved.put(token, String.valueOf(Commands.lookupInternal(token)));
		}
		return lines(resolved);
	}

	/**
	 * @param tokens tokens to resolve
	 * @return sorted {@code token\tidentity} lines of the exact-case XML lookup
	 */
	static String xmlExactResolution(Collection<String> tokens) {
		TreeMap<String, String> resolved = new TreeMap<>();
		for (String token : tokens) {
			Commands command = Commands.stringToCommand(token);
			resolved.put(token, command == null ? "null"
					: Commands.englishToInternal(command).name());
		}
		return lines(resolved);
	}

	/**
	 * @param app application
	 * @return the {@code <construction>} element of the current document XML
	 */
	static String constructionXml(App app) {
		String xml = app.getXML();
		int start = xml.indexOf("<construction");
		String end = "</construction>";
		return xml.substring(start, xml.indexOf(end, start) + end.length());
	}

	/**
	 * The persisted command identity of a construction: its command, input, output
	 * and expression elements. Construction-provider identifiers and default
	 * colours differ between two separately built constructions; these lines do
	 * not.
	 *
	 * @param app application
	 * @return the identity lines of the construction XML, in document order
	 */
	static String constructionCore(App app) {
		StringBuilder core = new StringBuilder();
		for (String line : constructionXml(app).split("\n")) {
			String trimmed = line.trim();
			if (trimmed.startsWith("<command ") || trimmed.startsWith("</command>")
					|| trimmed.startsWith("<input ") || trimmed.startsWith("<output ")
					|| trimmed.startsWith("<expression ")) {
				core.append(trimmed).append('\n');
			}
		}
		return core.toString();
	}

	/**
	 * @param text canonical text
	 * @return lower-case hexadecimal SHA-256 of its UTF-8 bytes
	 */
	static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte value : digest) {
				hex.append(String.format(Locale.ROOT, "%02x", value));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param text canonical text
	 * @return number of lines in it
	 */
	static int lineCount(String text) {
		return text.isEmpty() ? 0 : text.split("\n", -1).length - 1;
	}

	private static void addValue(TreeSet<String> tokens, ResourceBundle bundle,
			String key) {
		try {
			tokens.add(bundle.getString(key));
		} catch (MissingResourceException missing) {
			// no bundle value for this constant
		}
	}

	private static String lines(Map<String, String> sorted) {
		StringBuilder text = new StringBuilder();
		for (Map.Entry<String, String> entry : sorted.entrySet()) {
			text.append(entry.getKey()).append('\t').append(entry.getValue()).append('\n');
		}
		return text.toString();
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.emf.EMFPlusGraphics2D;
import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geocedg.desktop.export.DxfPreparedOutput;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.AutoColor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-C T-G9X1-IDENTITY, T-CLASSIC and the FreeHEP default: SHA-256 of
 * outputs that C must not change, recorded by the same code on a {@code git
 * archive} of the published base {@code e6135028} (A-2 closeout). Without a unit,
 * an explicit area or a hidden layer the G5 and G9X1 DXF bytes are the base bytes;
 * a sidecar equals the base sidecar once the declared version-2 additions (schema
 * version, units, layers, export area, outside-area records) and the build
 * provenance are removed; Classic LaTeX and picture output, the default FreeHEP EMF
 * writers and the GeoCeDG LaTeX output of a document without semantic curves, unit
 * or area are byte-identical. PRE-G9B-R6-plus-E3-R1 completes the AC1015 container
 * (tables, blocks, objects): each complete DXF digest is the corrected container's,
 * recorded on the corrective tree, and each {@code .dxf.core} digest (leading comments
 * and ENTITIES section) equals the core of the base bytes, recorded on a worktree of
 * the frozen E3 candidate {@code 257c854a}, whose complete DXF digests are the base
 * digests above.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusCBaseIdentityTest {

	/** Recorded on a git archive of e613502831e3b412780d69424d4a1e433a4ae688. */
	private static final String[][] BASE = {
		{"g9x1.locus.components.dxf",
				"d8c1223b1ec8f529ae2eec7dbad1be622926fa3be40942013dda76901c2c935a"},
		{"g9x1.locus.components.dxf.core",
				"fb8dc4b676e39ae0719bda4524bfc7df9236b358c23b13e3dd5539e975094fa7"},
		{"g9x1.locus.components.sidecar",
				"f16f327cf6e8d4e77c7bc233699776afca2499d03b3a6a26752c882659460e3b"},
		{"g9x1.locus.closed.dxf",
				"3d2200a3e1bb1698bf9857bc378501bfa71f2285a79ace900afddb6f75206e4c"},
		{"g9x1.locus.closed.dxf.core",
				"302b83191cba0a449ccb4f3a025e12d379e99e2bb9c2432fe4cc4b171fd4fb88"},
		{"g9x1.locus.closed.sidecar",
				"c43e26d3283a62dcfa7f57e512bbf21e52f26002cc243fa02443005054074f48"},
		{"g9x1.spline.dxf",
				"d2a4780754b8ccebc07b9148d09d536496482ba9bb588bead993a91ada8ea407"},
		{"g9x1.spline.dxf.core",
				"70d04d9935a732636a8df40e2ed0b9db8fa01e699e65fcc71ce495bc389aaad1"},
		{"g9x1.spline.sidecar",
				"df4ec6208cf3af1db71277fd00a644675695dbc91b4f701a9fa02d28cac46e5f"},
		{"g9x1.function.dxf",
				"0c4340ea918dbfa8952a674a60d007f6294c2da5938b8e213a7534301014b213"},
		{"g9x1.function.dxf.core",
				"36cad8c1475c603d009e6b2a9283f9e71247b9d888f3fa5091f3914fb1460c89"},
		{"g9x1.function.sidecar",
				"e23beceb412d3ad455174500d0d695edd9aa937cc3a17e0c07de334bcedfee42"},
		{"g5.exact.dxf",
				"c46f613486850e7fee5af280d23c7487c4c126cbaa929fc0ee6978b2cfe67c38"},
		{"g5.exact.dxf.core",
				"a711533a284b11e971b5185cb14266f5742b966251a20a38b9a4962bce557619"},
		{"g9x1.exact.dxf",
				"c46f613486850e7fee5af280d23c7487c4c126cbaa929fc0ee6978b2cfe67c38"},
		{"g9x1.exact.dxf.core",
				"a711533a284b11e971b5185cb14266f5742b966251a20a38b9a4962bce557619"},
		{"g9x1.exact.sidecar",
				"b75e1a456e00818fc582de004061d505dd76ab6bbaec7baa745b07e55592f04a"},
		{"geocedg.latex.pgf.unspecified",
				"b8216cf30d89509717c0d5d2ba502fe07491ee2698e144291f2fe05cf4d21ad6"},
		{"classic.latex.pgf",
				"4106e0a6abbb5774c6965ac2dd6a30b5c6e785f9d46327df25f56fe33bb81024"},
		{"classic.latex.pstricks",
				"78bbe0b36bb384972649e7138e9e498a21d25657d6e71e5f31860f3b805f77d5"},
		{"classic.latex.asymptote",
				"209f2770b4261df5e46e2311e2ecc15769b544de5201623d4a01b23c83ae4b95"},
		{"classic.pdf",
				"e78e35e2d848f69d48220e654e9ce7422305380839a5439b657c11b4f02d673b"},
		{"classic.svg",
				"2887f5d6107332c203b4d9a3afe18fc8e36624307854bda011e991d3e837831b"},
		{"classic.emf",
				"c7c9295e0e1ef83c84f31b0851acd46b0eced7191be2f9786d48404c24570074"},
		{"freehep.emf.default",
				"f7f345f13e99dca8fc40c28f69bf843b6f9bbba4b7ca45899d4f0ea11b47ff97"},
		{"freehep.emfplus.default",
				"a7c1ede1fab8334973468130f34949ae03c22daf20dfc02862dccb17c714bad8"},
	};

	@TempDir
	Path temporary;

	@Test
	void outputsThatCMustNotChangeAreTheBaseBytes() throws Exception {
		Map<String, String> digests = digests(temporary);
		Map<String, String> base = new LinkedHashMap<>();
		for (String[] entry : BASE) {
			base.put(entry[0], entry[1]);
		}
		assertEquals(base, digests);
	}

	@Test
	void theUpstreamSeamsKeepTheHostDefaults() throws Exception {
		AppD classic = classic();
		EuclidianView view = classic.getEuclidianView1();
		assertTrue(Double.isNaN(classic.getPhysicalExportScale()));
		assertNull(classic.getExportAreaWorldBounds(view));
		assertFalse(classic.exportAreaBoundsEdited(view, 0, 1, 0, 1));
		assertNull(classic.getExportScalePresentation());
		AtomicReference<String> ran = new AtomicReference<>();
		classic.runApiDocumentReplacement(() -> ran.set("ran"));
		assertEquals("ran", ran.get());
		AtomicReference<GeoGebraExport> export = new AtomicReference<>();
		classic.newGeoGebraToPgf(export::set);
		assertEquals("org.geogebra.common.export.pstricks.GeoGebraToPgf",
				export.get().getClass().getName(), "Classic keeps the host exporter");
		export.get().setxmin(-0.5);
		assertNotNull(view.getSelectionRectangle(),
				"Classic bound edits still write the selection rectangle");
		view.setSelectionRectangle(null);
	}

	@Test
	void theUpstreamLatexPackageStaysWebCompatible() throws Exception {
		Path pstricks = Paths.get(System.getProperty("user.dir")).getParent().getParent()
				.resolve("shared/common/src/main/java/org/geogebra/common/export/pstricks");
		try (var files = Files.list(pstricks)) {
			for (Path file : files.toList()) {
				String text = Files.readString(file);
				for (String forbidden : new String[] {"import org.geocedg", "import java.io",
						"import java.awt", "import java.security", "java.lang.reflect"}) {
					assertFalse(text.contains(forbidden), file + ": " + forbidden);
				}
			}
		}
		String app = Files.readString(Paths.get(System.getProperty("user.dir")).getParent()
				.getParent().resolve("shared/common/src/main/java/org/geogebra/common/main"
						+ "/App.java"));
		assertTrue(app.contains("public double getPhysicalExportScale() {"));
		assertTrue(app.contains("public double[] getExportAreaWorldBounds(EuclidianView view) {"));
		assertTrue(app.contains("public boolean exportAreaBoundsEdited(EuclidianView view, "
				+ "double xmin,"));
		assertFalse(app.contains("import org.geocedg.common.export"),
				"no GeoCeDG export type in the shared App seams");
	}

	// ----------------------------------------------------------------- digests

	static Map<String, String> digests(Path temporary) throws Exception {
		final Map<String, String> digests = new LinkedHashMap<>();
		final GeometryExportService service = new GeometryExportService();
		resetAutomaticColors();
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "s=0");
		eval(app, "Q=(s,s^2/4)");
		eval(app, "D={false,{-2,-1,true,true},{1,2,true,true}}");
		GeoElement components = eval(app, "L=LocusV2(Q,s,D)");
		eval(app, "t=0");
		eval(app, "P=(cos(t),sin(t))");
		eval(app, "E={true,{0,2*pi,true,false}}");
		GeoElement closed = eval(app, "C=LocusV2(P,t,E)");
		GeoElement spline = eval(app, "S=SplineV2({(0,0),(1,1),(2,0),(3,1)},3)");
		GeoElement function = eval(app, "f(x)=x^2/3");
		putG9x1(digests, service, "g9x1.locus.components", components,
				request(0.01));
		putG9x1(digests, service, "g9x1.locus.closed", closed,
				request(0.01));
		putG9x1(digests, service, "g9x1.spline", spline,
				request(0.001));
		putG9x1(digests, service, "g9x1.function", function,
				GeometryExportRequest.builder(0.01).addDefaultSemanticDomain(
						new GeometryExportRequest.SemanticDomain("d", -1, 2, true, true))
						.allowApproximation(true).allowPartialOutput(false).requestSidecar(true)
						.build());

		resetAutomaticColors();
		AppGeoCeDG exact = G9U1TestApp.create();
		List<GeoElement> scene = new ArrayList<>();
		for (String command : new String[] {"A=(1,2)", "B=(4,3)", "g=Segment(A,B)",
				"r=Ray(A,(2,5))", "l=Line((0,0),(1,3))", "c=Circle((0,0),2)",
				"a=CircularArc((0,0),(2,0),(0,2))", "e=Ellipse((0,0),(3,0),4)",
				"poly=Polygon((5,5),(6,5),(6,7))", "pl=Polyline((0,-1),(1,-2),(2,-1))"}) {
			scene.add(eval(exact, command));
		}
		scene.get(1).setEuclidianVisible(false);
		putDxf(digests, "g5.exact", service.exportDxf(service.createModel(scene,
				SelectionMode.CURRENT_SELECTION)));
		putG9x1(digests, service, "g9x1.exact", scene,
				request(0.01));

		resetAutomaticColors();
		AppGeoCeDG latex = G9U1TestApp.create();
		EuclidianView geocedgView = sized(latex.getEuclidianView1());
		classicScene(latex);
		AtomicReference<GeoGebraExport> pgf = new AtomicReference<>();
		latex.newGeoGebraToPgf(pgf::set);
		digests.put("geocedg.latex.pgf.unspecified", sha256(latex(pgf.get(),
				geocedgView)));

		resetAutomaticColors();
		AppD classic = classic();
		EuclidianView view = classic.getEuclidianView1();
		classicScene(classic);
		AtomicReference<GeoGebraExport> classicExport = new AtomicReference<>();
		classic.newGeoGebraToPgf(classicExport::set);
		digests.put("classic.latex.pgf", sha256(latex(classicExport.get(), view)));
		classic.newGeoGebraToPstricks(classicExport::set);
		digests.put("classic.latex.pstricks", sha256(latex(classicExport.get(), view)));
		classic.newGeoGebraToAsymptote(classicExport::set);
		digests.put("classic.latex.asymptote", sha256(latex(classicExport.get(), view)));
		double printing = view.getPrintingScale();
		double scale = printing * 300 / 2.54 / view.getXscale();
		int pixelWidth = (int) Math.floor(view.getExportWidth() * scale);
		int pixelHeight = (int) Math.floor(view.getExportHeight() * scale);
		File pdf = temporary.resolve("classic.pdf").toFile();
		GraphicExportDialog.exportPDF((EuclidianViewD) view, pdf, true, pixelWidth,
				pixelHeight, scale);
		digests.put("classic.pdf", sha256(new String(Files.readAllBytes(pdf.toPath()),
				StandardCharsets.ISO_8859_1).replaceAll("\\(D:[0-9+\\-Z']*\\)", "(D:)")));
		ByteArrayOutputStream svg = new ByteArrayOutputStream();
		GraphicExportDialog.exportSVG(classic, view, svg, true, pixelWidth, pixelHeight,
				-1, -1, scale, false);
		digests.put("classic.svg", sha256(svg.toString(StandardCharsets.UTF_8)
				.replaceAll("clip[0-9a-f-]+", "clip")));
		File emf = temporary.resolve("classic.emf").toFile();
		GraphicExportDialog.exportEMF((EuclidianViewD) view, emf, false, pixelWidth,
				pixelHeight, scale);
		digests.put("classic.emf", sha256(Files.readAllBytes(emf.toPath())));

		digests.put("freehep.emf.default", sha256(freehep(temporary, false)));
		digests.put("freehep.emfplus.default", sha256(freehep(temporary, true)));
		return digests;
	}

	private static void putG9x1(Map<String, String> digests,
			GeometryExportService service, String name, Object sources,
			GeometryExportRequest request) {
		List<GeoElement> list = new ArrayList<>();
		if (sources instanceof GeoElement) {
			list.add((GeoElement) sources);
		} else {
			for (Object source : (List<?>) sources) {
				list.add((GeoElement) source);
			}
		}
		GeometryExportPreflight preflight = service.preflight(list,
				SelectionMode.CURRENT_SELECTION, request);
		DxfEncodingResult encoding = service.encode(preflight);
		putDxf(digests, name, withoutSessionIdentity(encoding.getDxfText()));
		DxfPreparedOutput output = new DxfFidelityManifestWriter().prepare(preflight,
				encoding);
		if (output.hasManifest()) {
			digests.put(name + ".sidecar", sha256(normalizeSidecar(new String(
					output.getManifest().getBytes(), StandardCharsets.UTF_8))));
		}
	}

	/**
	 * PRE-G9B-R6-plus-E3-R1: the complete DXF and its core — the leading comments and
	 * the ENTITIES section — whose digest is the base digest, because the corrective
	 * container changes only the structure around the entities.
	 */
	private static void putDxf(Map<String, String> digests, String name, String dxf) {
		digests.put(name + ".dxf", sha256(dxf));
		digests.put(name + ".dxf.core", sha256(dxfCore(dxf)));
	}

	/**
	 * @param dxf complete DXF text
	 * @return its leading 999 comments followed by its ENTITIES section
	 */
	static String dxfCore(String dxf) {
		StringBuilder core = new StringBuilder();
		String[] lines = dxf.split("\r\n", -1);
		int index = 0;
		while (index + 1 < lines.length && "999".equals(lines[index])) {
			core.append(lines[index]).append("\r\n").append(lines[index + 1]).append("\r\n");
			index += 2;
		}
		int start = dxf.indexOf("0\r\nSECTION\r\n2\r\nENTITIES\r\n");
		int end = dxf.indexOf("0\r\nENDSEC\r\n", start);
		return core.append(dxf, start, end).toString();
	}

	/**
	 * Replaces the per-session parts of a source identity: the construction hash of a
	 * Locus V2 source and the construction revision; they differ between two runs of
	 * the same base.
	 */
	static String withoutSessionIdentity(String text) {
		return text.replaceAll("geo:[0-9a-f]{32}", "geo:session")
				.replaceAll("\"source_revision\":-?[0-9]+", "\"source_revision\":0");
	}

	/**
	 * Removes the build provenance and the declared version-2 additions so that a
	 * sidecar of an unspecified document without area or hidden layer can be
	 * compared with its version-1 base. The paired DXF hash covers the session
	 * identity; the DXF itself is compared without it.
	 */
	static String normalizeSidecar(String manifest) {
		return withoutSessionIdentity(manifest)
				.replaceAll("\"sha256\":\"[0-9a-f]{64}\"", "\"sha256\":\"paired-dxf\"")
				.replace("\"schema_version\":2,", "\"schema_version\":1,")
				.replaceAll("\"application\":\\{[^}]*\\},", "")
				.replace(",\"insunits\":0,\"sha256\"", ",\"sha256\"")
				.replaceAll("\"units\":\\{\"metadata_schema\":[^}]*\\},", "")
				.replaceAll("\"layers\":\\{\"hidden_geocedg_layers\":\\[\\],"
						+ "\"dxf_layers_off\":\\[\\],\"rule\":\"[^\"]*\"\\},", "")
				.replaceAll("\"export_area\":\\{\"rule\":\"[^\"]*\",\"resolved_producer\":"
						+ "null,\"boundary\":false,\"bounds\":null,\"source_view_id\":null,"
						+ "\"outside_export_area\":0\\},", "")
				.replace("\"outside_export_area\":0,\"custom_unit_usm\":false,", "")
				.replace(",\"outside_export_area\":[]", "");
	}

	/**
	 * Upstream {@link AutoColor} keeps one curve-colour index per JVM, so the default
	 * colour of a function depends on the curves created before it by any test.
	 */
	private static void resetAutomaticColors() throws ReflectiveOperationException {
		Field index = AutoColor.class.getDeclaredField("index");
		index.setAccessible(true);
		for (AutoColor scheme : AutoColor.values()) {
			index.setInt(scheme, 0);
		}
	}

	private static byte[] freehep(Path temporary, boolean plus) throws Exception {
		File file = temporary.resolve(plus ? "default-plus.emf" : "default.emf").toFile();
		Dimension size = new Dimension(320, 200);
		VectorGraphics graphics = plus ? new EMFPlusGraphics2D(file, size)
				: new EMFGraphics2D(file, size);
		graphics.setCreator("probe");
		graphics.setDeviceIndependent(true);
		graphics.startExport();
		graphics.setColor(Color.RED);
		graphics.fill(new Rectangle2D.Double(10.25, 20.5, 60.125, 30.75));
		graphics.setColor(Color.BLUE);
		graphics.draw(new Ellipse2D.Double(5.5, 5.5, 100, 50));
		graphics.endExport();
		return Files.readAllBytes(file.toPath());
	}

	private static void classicScene(App app) {
		for (String command : new String[] {"A=(1,1)", "B=(3,2)", "s=Segment(A,B)",
				"c=Circle(A,1)", "f(x)=x^2/4", "poly=Polygon((0,0),(1,0),(0,1))",
				"g=Line((0,2),(1,3))", "loc=Locus(Midpoint(A,Point(c)),Point(c))"}) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
	}

	private static String latex(GeoGebraExport export, EuclidianView view) {
		ExportFrameMinimal frame = new ExportFrameMinimal(view.getYmin(), view.getYmax());
		frame.setKeepColor();
		export.setFrame(frame);
		export.generateAllCode();
		return frame.getCode().replace("\r\n", "\n");
	}

	private static AppD classic() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		sized(classic.getEuclidianView1());
		return classic;
	}

	private static EuclidianView sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(400, 300);
			view.updateSize();
			view.setCoordSystem(50, 200, 50, 50);
		});
		return view;
	}

	private static GeometryExportRequest request(double tolerance) {
		return GeometryExportRequest.builder(tolerance).allowApproximation(true)
				.allowPartialOutput(false).requestSidecar(true).build();
	}

	static String sha256(String text) {
		return sha256(text.getBytes(StandardCharsets.UTF_8));
	}

	static String sha256(byte[] bytes) {
		try {
			StringBuilder hex = new StringBuilder();
			for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}

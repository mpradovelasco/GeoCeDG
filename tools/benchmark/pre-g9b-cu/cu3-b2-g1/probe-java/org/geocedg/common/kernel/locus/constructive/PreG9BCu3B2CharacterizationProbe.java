/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 G-B2-1 characterization probe (author authorization of 2026-10-28; not a test of
 * the product and not part of any inventory). Run only through
 * tools/benchmark/pre-g9b-cu/run-cu3-b2-g1-characterization.ps1, which adds it and an
 * instrumented shadow copy of LocusIntersectionTokenLedger2D to the common-jre test source set.
 */
final class PreG9BCu3B2CharacterizationProbe extends ConstructiveModelReplicaTest {

	private static void out(String line) {
		System.out.println("PROBE|" + line);
	}

	// ------------------------------------------------------------- 1. exact scope

	@Test
	void s1AuthorReplicaRegularTangency() {
		buildAuthorReplica();
		add("U=Intersect(o_1,h_2)");
		dump("s1.replica.legacy", (GeoLocusIntersectionResult) lookup("U"));
		AuthorityPolicyAdoption.adopt(getConstruction());
		dump("s1.replica.adopted", (GeoLocusIntersectionResult) lookup("U"));
	}

	@Test
	void s1FreeTargetsOnARegularCircleLocus() {
		regularCircle();
		AuthorityPolicyAdoption.adopt(getConstruction());
		for (String b : new String[] {"0.99", "0.9999999", "1", "1.0000001", "1.01"}) {
			add("b=" + b);
			dump("s1.line.b=" + b, result());
		}
		add("b=0.5");
		add("w=Circle((2,3),2)");
		dump("s1.circleTarget.external", add("W=Intersect(lc,w)"));
		add("v=Line((2,1),(3,1))");
		dump("s1.lineThroughExactPoint", add("V=Intersect(lc,v)"));
		add("T=Midpoint((0,2),(4,0))");
		add("tl=Line(T,(5,1))");
		dump("s1.structurallyBuiltTangent", add("TL=Intersect(lc,tl)"));
	}

	// ------------------------------------------------------- 2. transverse tokens

	@Test
	void s2TransverseTokensAcrossATangency() {
		for (boolean adopt : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			regularCircle();
			if (adopt) {
				AuthorityPolicyAdoption.adopt(getConstruction());
			}
			add("b=0.9");
			GeoLocusIntersectionResult result = result();
			List<String> before = tokens(result);
			GeoPoint point = claim(result, "X", 0);
			GeoPoint other = claim(result, "Y", 1);
			out("s2.adopt=" + adopt + " before=" + before + " X=" + at(point) + " Y=" + at(other));
			for (String b : new String[] {"0.99", "1", "1.01", "0.99", "0.9", "1", "0.9"}) {
				add("b=" + b);
				out("s2.adopt=" + adopt + " b=" + b + " tokens=" + tokens(result()) + " admissible="
						+ admissible(result()) + " X=" + at(point) + " Y=" + at(other)
						+ " diag=" + codes(rich(result())));
			}
		}
	}

	// --------------------------------------------- 3. driver-support redefinition

	@Test
	void s3DriverSupportRedefinition() {
		for (boolean adopt : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			add("c=Circle((0,0),2)");
			add("c3=Circle((0,0),2.5)");
			add("N=Point(c)");
			add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
			add("lx=LocusV2(X0,N)");
			add("A=(" + (adopt ? "1.9999" : "1.99") + ",0)");
			add("g=Line(A,Line((0,0),(0,1)))");
			if (adopt) {
				AuthorityPolicyAdoption.adopt(getConstruction());
			}
			GeoLocusIntersectionResult result = add("R=Intersect(lx,g)");
			GeoPoint point = claim(result, "X", 0);
			String token = point == null ? "-" : tokens(result).toString();
			out("s3.adopt=" + adopt + " start tokens=" + token + " lineage=" + lineages(result));
			attempt(adopt, point, "N", "Point(c3)");
			attempt(adopt, point, "c", "Circle((0,0),2.5)");
			attempt(adopt, point, "c", "Circle((0,0),2)");
			attempt(adopt, point, "N", "PointIn(c3)");
			attempt(adopt, point, "lx", "LocusV2(X0,N)");
		}
	}

	private void attempt(boolean adopt, GeoPoint point, String label, String definition) {
		List<String> errors = new ArrayList<>();
		try {
			getApp().getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
					lookup(label), definition,
					new org.geogebra.common.kernel.commands.EvalInfo(true, true), false, null,
					org.geogebra.common.main.error.ErrorHelper.silent());
		} catch (Throwable error) {
			errors.add(error.getClass().getSimpleName() + ":" + error.getMessage());
		}
		GeoElement geo = lookup(label);
		GeoLocusIntersectionResult result = result();
		out("s3.adopt=" + adopt + " redefine " + label + "=" + definition + " -> now "
				+ (geo == null ? "null" : geo.getDefinitionForInputBar()) + " errors=" + errors
				+ " tokens=" + tokens(result) + " admissible=" + admissible(result)
				+ " lineage=" + lineages(result) + " X=" + at(point)
				+ " Xdef=" + (lookup("X") == null ? "null" : lookup("X").getDefinitionForInputBar())
				+ " diag=" + codes(rich(result)));
	}

	// ------------------------------------------------- 4. ledger reservations

	@Test
	void s4LedgerReservationsOnReopen() {
		for (int variant = 0; variant < 4; variant++) {
			getApp().getKernel().clearConstruction(true);
			GeoLocusIntersectionResult result;
			if (variant < 2) {
				add("c=Circle((0,0),2)");
				add("N=Point(c)");
				add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
				add("lx=LocusV2(X0,N)");
				add("A=(1.9999,0)");
				add("g=Line(A,Line((0,0),(0,1)))");
				AuthorityPolicyAdoption.adopt(getConstruction());
				result = add("R=Intersect(lx,g)");
				if (variant == 0) {
					claim(result, "X", 0);
				}
			} else {
				buildAuthorReplica();
				add("R=Intersect(e_1,c_1)");
				AuthorityPolicyAdoption.adopt(getConstruction());
				result = result();
				if (variant == 2) {
					claim(result, "X", 0);
					claim(result, "Y", 1);
				}
			}
			out("s4.variant=" + variant + " state0=" + head(result().getTokenLedgerState()));
			LocusIntersectionTokenLedger2D.PROBE = true;
			out("s4.variant=" + variant + " reopen-1");
			getApp().setXML(getApp().getXML(), true);
			LocusIntersectionTokenLedger2D.PROBE = false;
			out("s4.variant=" + variant + " state1=" + head(result().getTokenLedgerState()));
			getApp().setXML(getApp().getXML(), true);
			out("s4.variant=" + variant + " state2=" + head(result().getTokenLedgerState()));
		}
	}

	// ------------------------------------------------- 5. singular sources

	@Test
	void s5SingularSources() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("u=x(N)");
		add("K0=(u^2,u^3)");
		add("lk=LocusV2(K0,N)");
		add("q=Line((0,0),(0,1))");
		dump("s5.cusp.vertical", add("Q1=Intersect(lk,q)"));
		add("q2=Line((0,0),(1,0))");
		dump("s5.cusp.horizontal", add("Q2=Intersect(lk,q2)"));
		add("K1=(u^2,u^2)");
		add("lr=LocusV2(K1,N)");
		add("q3=Line((0,0),(1,-1))");
		dump("s5.stall.ray", add("Q3=Intersect(lr,q3)"));
		add("P0=Midpoint(N,(4,0))");
		add("lc=LocusV2(P0,N)");
		add("q4=Line((0,1),(1,1))");
		dump("s5.regular.reference", add("Q4=Intersect(lc,q4)"));
	}


	// ------------------------------------------------------------ iteration 2

	@Test
	void t2TransverseReactivationAndAdmission() {
		for (boolean adopt : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			regularCircle();
			if (adopt) {
				AuthorityPolicyAdoption.adopt(getConstruction());
			}
			add("b=0.9");
			dump("t2.adopt=" + adopt + " b=0.9", result());
			claim(result(), "X", 0);
			claim(result(), "Y", 1);
			for (String b : new String[] {"0.99", "1", "0.99", "0.9", "1", "0.9", "0.8"}) {
				add("b=" + b);
				out("t2.adopt=" + adopt + " b=" + b + " tokens=" + tokens(result()) + " nAdm="
						+ admissible(result()).size() + " X=" + at((GeoPoint) lookup("X"))
						+ " Y=" + at((GeoPoint) lookup("Y")) + " diag=" + codes(rich(result())));
			}
		}
	}

	@Test
	void t3LineageChangingRedefinitions() {
		for (boolean adopt : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			add("O=(0,0)");
			add("c=Circle((0,0),2)");
			add("c3=Circle((0,0),2)");
			add("N=Point(c)");
			add("N2=Point(c3)");
			add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
			add("X2=Intersect(Line(N2,Line((0,0),(0,1))),Line((0,3),(1,3)))");
			add("lx=LocusV2(X0,N)");
			add("A=(" + (adopt ? "1.9999" : "1.99") + ",0)");
			add("g=Line(A,Line((0,0),(0,1)))");
			if (adopt) {
				AuthorityPolicyAdoption.adopt(getConstruction());
			}
			GeoLocusIntersectionResult result = add("R=Intersect(lx,g)");
			claim(result, "X", 0);
			out("t3.adopt=" + adopt + " start tokens=" + tokens(result()) + " lineage="
					+ lineages(result()) + " X=" + at((GeoPoint) lookup("X")));
			attempt2(adopt, "c", "Circle(O,2)");
			attempt2(adopt, "c", "Circle((0,0),2)");
			attempt2(adopt, "lx", "LocusV2(X2,N2)");
			attempt2(adopt, "lx", "LocusV2(X0,N)");
			attempt2(adopt, "c", "Dilate(c3,1,O)");
		}
	}

	private void attempt2(boolean adopt, String label, String definition) {
		List<String> errors = new ArrayList<>();
		try {
			getApp().getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
					lookup(label), definition,
					new org.geogebra.common.kernel.commands.EvalInfo(true, true), false, null,
					org.geogebra.common.main.error.ErrorHelper.silent());
		} catch (Throwable error) {
			errors.add(error.getClass().getSimpleName() + ":" + error.getMessage());
		}
		GeoElement geo = lookup(label);
		out("t3.adopt=" + adopt + " redefine " + label + "=" + definition + " -> "
				+ (geo == null ? "null" : geo.getDefinitionForInputBar()) + " errors=" + errors
				+ " tokens=" + tokens(result()) + " nAdm=" + admissible(result()).size()
				+ " lineage=" + lineages(result()) + " X=" + at((GeoPoint) lookup("X"))
				+ " diag=" + codes(rich(result())));
	}


	// ------------------------------------------------------------ iteration 3

	@Test
	void u5SingularTurningPointWithoutSymmetry() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("V=(0.001,1)");
		add("X0=Intersect(Line(N,Line((0,0),V)),Line((0,3),(1,3)))");
		add("ls=LocusV2(X0,N)");
		double max = Math.sqrt(4 + 1E-6) + 0.003;
		double[] values = {max - 1E-4, max, max + 1E-4};
		for (int i = 0; i < values.length; i++) {
			add("A" + i + "=(" + values[i] + ",0)");
			add("g" + i + "=Line(A" + i + ",Line((0,0),(0,1)))");
			dump("u5.a=" + values[i], add("S" + i + "=Intersect(ls,g" + i + ")"));
		}
		add("Vs=(0,1)");
		add("Xs=Intersect(Line(N,Line((0,0),Vs)),Line((0,3),(1,3)))");
		add("lf=LocusV2(Xs,N)");
		add("gf=Line((2,0),(2,1))");
		dump("u5.symmetricFoldExact", add("SF=Intersect(lf,gf)"));
	}

	@Test
	void u6RegularSourceTangencyAtAnExactDriverParameter() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("N=Point(c)");
		add("P0=Rotate(N,pi/2,O)");
		add("lc=LocusV2(P0,N)");
		String[] targets = {"Line((0,2),(1,2))", "Line((0,1.99),(1,1.99))",
				"Line((0,2.01),(1,2.01))", "Circle((0,3),1)", "Circle((0,2.5),0.5)"};
		for (int i = 0; i < targets.length; i++) {
			add("h" + i + "=" + targets[i]);
			dump("u6.target=" + targets[i], add("U" + i + "=Intersect(lc,h" + i + ")"));
		}
		add("tN=Tangent(Rotate((2,0),pi/2,O),Circle(O,2))");
		dump("u6.structuralTangent", add("UT=Intersect(lc,tN)"));
	}

	// ----------------------------------------------------------------- helpers

	private void regularCircle() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("P0=Midpoint(N,(4,0))");
		add("lc=LocusV2(P0,N)");
		add("b=0.5");
		add("g=Line((0,b),(1,b))");
		add("R=Intersect(lc,g)");
	}

	private GeoLocusIntersectionResult result() {
		return (GeoLocusIntersectionResult) lookup("R");
	}

	private GeoPoint claim(GeoLocusIntersectionResult result, String label, int index) {
		List<String> admissible = admissible(result);
		if (admissible.size() <= index) {
			out("claim " + label + ": no admissible token at " + index + " of " + admissible);
			return null;
		}
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), label, result,
				new GeoText(getConstruction(), admissible.get(index)));
	}

	private static String at(GeoPoint point) {
		return point == null ? "none" : point.isDefined()
				? String.format(Locale.ROOT, "(%.9f,%.9f)", point.getInhomX(), point.getInhomY())
				: "undefined";
	}

	private static String head(String state) {
		String[] parts = state.split("\\|");
		return parts[0] + "|" + parts[1] + "|entries=" + (parts.length > 2 ? parts[2].split("~").length - 5 : 0);
	}

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		return result.getIntersectionResult();
	}

	private static List<String> tokens(GeoLocusIntersectionResult result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			tokens.add(token == null ? "null" : token.substring(Math.max(0, token.length() - 8)));
		}
		return tokens;
	}

	private static List<String> admissible(GeoLocusIntersectionResult result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			if (token != null && result.isPointAdmissible(token)) {
				tokens.add(token);
			}
		}
		return tokens;
	}

	private static List<String> lineages(GeoLocusIntersectionResult result) {
		List<String> lineages = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			String text = String.valueOf(solution.getLineage());
			lineages.add(Integer.toHexString(text.hashCode()));
		}
		return lineages;
	}

	private static List<String> codes(LocusIntersectionResult2D result) {
		List<String> codes = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : result.getDiagnostics()) {
			codes.add(diagnostic.getCode().name());
		}
		return codes;
	}

	private void dump(String tag, GeoLocusIntersectionResult result) {
		LocusIntersectionResult2D rich = rich(result);
		out(tag + " geometry=" + rich.getGeometryKind() + " solutions="
				+ rich.getFiniteSolutions().size() + " diag=" + codes(rich));
		for (IntersectionDiagnostic2D diagnostic : rich.getDiagnostics()) {
			String message = String.valueOf(diagnostic.getMessage());
			out(tag + "   diagnostic " + diagnostic.getCode() + ": "
					+ message.substring(0, Math.min(220, message.length())));
		}
		int index = 0;
		for (LocusIntersectionSolution2D solution : rich.getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			out(tag + "   [" + index++ + "] admissible="
					+ (token != null && result.isPointAdmissible(token))
					+ " status=" + solution.getIdentity().getIdentityStatus()
					+ " germ=" + solution.getRevisionEvidence().getCurrentRootGerm().orElse("-")
					+ getters(solution.getRevisionEvidence())
					+ getters(solution.getRevisionEvidence().getExistenceEvidence()));
		}
	}

	private static String getters(Object object) {
		StringBuilder text = new StringBuilder();
		Method[] methods = object.getClass().getMethods();
		java.util.Arrays.sort(methods, java.util.Comparator.comparing(Method::getName));
		for (Method method : methods) {
			if (method.getParameterCount() == 0 && method.getName().startsWith("get")
					&& method.getDeclaringClass() != Object.class
					&& (method.getReturnType().isEnum() || method.getReturnType() == double.class
					|| method.getReturnType() == int.class
					|| method.getReturnType() == boolean.class)) {
				try {
					text.append(' ').append(method.getName().substring(3)).append('=')
							.append(method.invoke(object));
				} catch (ReflectiveOperationException error) {
					text.append(' ').append(method.getName()).append("=!");
				}
			}
		}
		return text.toString();
	}
}

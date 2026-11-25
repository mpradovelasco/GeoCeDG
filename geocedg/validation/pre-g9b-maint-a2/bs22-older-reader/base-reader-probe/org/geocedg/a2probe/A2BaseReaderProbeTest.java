package org.geocedg.a2probe;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.util.debug.Log;
import org.junit.jupiter.api.Test;

/** Scratch characterization probe for PRE-G9B-MAINT-A2 stage 1 (never committed). */
public class A2BaseReaderProbeTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void say(String text) {
		System.out.println("PROBE " + text);
	}

	private String chain(Throwable t) {
		StringBuilder b = new StringBuilder();
		Throwable c = t;
		int d = 0;
		while (c != null && d < 8) {
			b.append(" <- ").append(c.getClass().getName()).append(": ").append(c.getMessage());
			for (Throwable s : c.getSuppressed()) {
				b.append(" [suppressed ").append(s.getClass().getName()).append(": ")
						.append(s.getMessage()).append("]");
			}
			c = c.getCause();
			d++;
		}
		return b.toString();
	}

	private String inventory() {
		List<String> out = new ArrayList<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			out.add(geo.getLabelSimple() + ":" + geo.getXMLTypeString() + ":" + geo.isDefined()
					+ (geo.getParentAlgorithm() == null ? ""
							: ":" + geo.getParentAlgorithm().getClassName()));
		}
		return String.join(", ", out);
	}

	private void load(String name, String xml) {
		try {
			getApp().getXMLio().processXMLString(xml, true, false);
			say(name + " LOADED hasErrors=" + getApp().getXMLio().hasErrors());
		} catch (Throwable t) {
			say(name + " THREW" + chain(t));
		}
		say(name + " AFTER " + inventory());
	}

	@Test
	void probe() {
		add("A=(0,0)");
		add("B=(1,2)");
		add("C=(3,2)");
		add("D=(4,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D},3)");
		say("S defined=" + spline.isDefined() + " id=" + spline.getPersistentLocusId());
		String xml = getApp().getXML();
		int start = xml.indexOf("<command name=\"SplineV2\">");
		int end = xml.indexOf("</command>", start);
		say("WRITER " + xml.substring(start, end + 10).replace('\n', '|'));
		int s0 = xml.indexOf("<geocedgSpatial");
		int s1 = xml.indexOf("</geocedgSpatial>");
		say("SPATIAL " + (s0 < 0 ? "none" : xml.substring(s0, s1 + 17).replace('\n', '|')));

		String unknownChild = xml.replace("<command name=\"SplineV2\">",
				"<command name=\"SplineV2\">\n<geocedgBSpline contract=\"bspline-2d/v1\""
						+ " knots=\"clamped-uniform/v1\"/>");
		load("P1-unknown-child-in-SplineV2", unknownChild);

		load("P0-reload-original", xml);

		String future = xml.replace("<command name=\"SplineV2\">",
				"<command name=\"BSpline\">\n<geocedgBSpline contract=\"bspline-2d/v1\""
						+ " knots=\"clamped-uniform/v1\"/>");
		load("P2-future-BSpline-document", future);

		load("P0b-reload-original", xml);
		String futureNoIdentity = future;
		int f0 = futureNoIdentity.indexOf("<geocedgSpatial");
		int f1 = futureNoIdentity.indexOf("</geocedgSpatial>");
		if (f0 >= 0 && f1 > f0) {
			futureNoIdentity = futureNoIdentity.substring(0, f0)
					+ futureNoIdentity.substring(f1 + 17);
		}
		load("P3-future-BSpline-without-identity-section", futureNoIdentity);
		Log.debug("probe end");
	}
}

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import javax.swing.JPanel;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.desktop.CommandLineArguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Scratch probe (never committed): writes the BS-22 BSpline fixture document. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class A2FixtureWriterProbeTest {
	@Test
	void writeFixture() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel());
		app.setErrorDialogsActive(false);
		for (String command : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2)", "D=(4,0)",
				"E=(5,1)", "b=BSpline({A,B,C,D,E})", "M=LocusLength(b)"}) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		File target = new File(System.getProperty("a2.fixtureOut"));
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(target));
		System.out.println("PROBE wrote " + target + " " + target.length());
	}
}

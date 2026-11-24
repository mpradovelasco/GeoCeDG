package org.geocedg.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.CommandLineArguments;

/** Scratch probe for PRE-G9B-MAINT-A2 BS-22: an older reader opening a BSpline document. */
public final class A2OlderReaderProbe {
	private A2OlderReaderProbe() {
	}

	public static void main(String[] args) throws Exception {
		File document = new File(args[0]);
		List<String> logged = new ArrayList<>();
		Log.setLogger(new Log() {
			@Override
			public void print(Level level, Object message) {
				if (message instanceof Throwable) {
					Throwable current = (Throwable) message;
					StringBuilder chain = new StringBuilder();
					while (current != null) {
						chain.append(" <- ").append(current.getClass().getName()).append(": ")
								.append(current.getMessage());
						current = current.getCause();
					}
					logged.add(level + chain.toString());
				} else if (String.valueOf(message).contains("BSpline")
						|| String.valueOf(message).contains("geocedgBSpline")) {
					logged.add(level + " " + message);
				}
			}
		});
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		SwingUtilities.invokeAndWait(() -> {
			AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {"--silent",
					"--settingsfile=" + args[1]}), new JPanel());
			app.setErrorDialogsActive(false);
			List<String> messages = new ArrayList<>();
			app.setUnitLoadErrorSink(messages::add);
			app.getKernel().getAlgebraProcessor().processAlgebraCommand("Kept=(7,7)", false);
			boolean loaded = app.loadFile(document, false);
			System.out.println("PROBE loaded=" + loaded);
			System.out.println("PROBE keptStillPresent="
					+ (app.getKernel().lookupLabel("Kept") != null));
			System.out.println("PROBE objectCount="
					+ app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
			System.out.println("PROBE typedMessages=" + messages);
		});
		for (String line : logged) {
			System.out.println("PROBE log " + line);
		}
		Runtime.getRuntime().halt(0);
	}
}

/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.common.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.util.StringUtil;

/**
 * Manages macros (user defined tools).
 * 
 * @author Markus Hohenwarter
 */
public class MacroManager {

	private HashMap<String, Macro> macroMap; // maps macro name to macro object
	private ArrayList<Macro> macroList; // lists all macros
	private HashMap<String, Macro> commandAuthorities;

	/**
	 * Creates new macro manager
	 * 
	 */
	public MacroManager() {
		macroMap = new HashMap<>();
		macroList = new ArrayList<>();
		commandAuthorities = new HashMap<>();
	}

	/**
	 * @param macro
	 *            macro to be added
	 */
	public void addMacro(Macro macro) {
		String key = commandKey(macro);
		Macro authority = commandAuthorities.get(key);
		if (authority == null || !macroList.contains(authority)) {
			commandAuthorities.remove(key);
			macroMap.put(key, macro);
		}
		macroList.add(macro);
	}

	/**
	 * Bind command resolution to a concrete macro selected by an external semantic
	 * authority. The object reference is only the current-session execution handle;
	 * callers remain responsible for establishing and reconstructing the binding.
	 *
	 * @param macro authoritative current-session macro
	 */
	public void bindCommandAuthority(Macro macro) {
		if (!macroList.contains(macro)) {
			throw new IllegalArgumentException("Macro authority must be registered");
		}
		String key = commandKey(macro);
		commandAuthorities.put(key, macro);
		macroMap.put(key, macro);
	}

	/** Bind every current macro as the authority for its current command name. */
	public void bindAllCommandAuthorities() {
		for (Macro macro : macroList) {
			bindCommandAuthority(macro);
		}
	}

	/**
	 * @param macro macro to inspect
	 * @return whether it owns the explicit command binding
	 */
	public boolean isCommandAuthority(Macro macro) {
		return macro != null && commandAuthorities.get(commandKey(macro)) == macro;
	}

	/**
	 * Returns macro with given name
	 * 
	 * @param name
	 *            macro's command name
	 * @return macro
	 */
	public Macro getMacro(String name) {
		return macroMap.get(StringUtil.toLowerCaseUS(name));
	}

	/**
	 * Removes given macro
	 * 
	 * @param macro
	 *            macro for removal
	 */
	public void removeMacro(Macro macro) {
		String key = commandKey(macro);
		macroList.remove(macro);
		commandAuthorities.remove(key, macro);
		if (macroMap.get(key) == macro) {
			Macro replacement = commandAuthorities.get(key);
			if (replacement == null) {
				replacement = lastMacro(key);
			}
			if (replacement == null) {
				macroMap.remove(key);
			} else {
				macroMap.put(key, replacement);
			}
		}
	}

	/**
	 * Removes all macros
	 */
	public void removeAllMacros() {
		macroMap.clear();
		macroList.clear();
		commandAuthorities.clear();
	}

	/**
	 * Sets the command name of a macro.
	 * 
	 * @param macro
	 *            macro
	 * @param cmdName
	 *            command name
	 */
	public void setMacroCommandName(Macro macro, String cmdName) {
		String previousKey = commandKey(macro);
		boolean authority = commandAuthorities.remove(previousKey, macro);
		macroMap.remove(previousKey, macro);
		macro.setCommandName(cmdName);
		String nextKey = commandKey(macro);
		macroMap.put(nextKey, macro);
		if (authority) {
			commandAuthorities.put(nextKey, macro);
		}
	}

	/**
	 * @param i
	 *            index
	 * @return i-th macro from the list
	 */
	public Macro getMacro(int i) {
		return macroList.get(i);
	}

	/**
	 * @param macro
	 *            macro
	 * @return order of the macro in macro list
	 */
	public int getMacroID(Macro macro) {
		for (int i = 0; i < macroList.size(); i++) {
			if (macro == macroList.get(i)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * All macros are marked as unused
	 */
	public void setAllMacrosUnused() {
		for (int i = 0; i < macroList.size(); i++) {
			macroList.get(i).setUnused();
		}
	}

	/**
	 * Returns the current number of macros handled by this MacroManager.
	 * 
	 * @return current number of macros
	 */
	public int getMacroNumber() {
		return macroList.size();
	}

	/**
	 * Returns an array of all macros handled by this MacroManager.
	 * 
	 * @return an array of all macros handled by this MacroManager.
	 */
	public ArrayList<Macro> getAllMacros() {
		return macroList;
	}

	private Macro lastMacro(String key) {
		for (int i = macroList.size() - 1; i >= 0; i--) {
			Macro candidate = macroList.get(i);
			if (key.equals(commandKey(candidate))) {
				return candidate;
			}
		}
		return null;
	}

	private static String commandKey(Macro macro) {
		return StringUtil.toLowerCaseUS(macro.getCommandName());
	}

	/**
	 * Updates all macros that need to be
	 * 
	 * @param prop
	 *            what property changed
	 */
	public final void notifyEuclidianViewCE(EVProperty prop) {
		// save selected macros
		for (int i = 0; i < macroList.size(); i++) {
			Macro macro = macroList.get(i);
			macro.getMacroConstruction().notifyEuclidianViewCE(prop);
		}
	}

	/**
	 * Appends an XML representation of the specified macros in this kernel to a XML string.
	 * 
	 * @param macros
	 *            list of macros
	 * @param builder XML string builder
	 */
	public static void getMacroXML(List<Macro> macros, XMLStringBuilder builder) {
		if (macros == null) {
			return;
		}
		// save selected macros
		for (Macro macro : macros) {
			if (macro != null) {
				macro.getXML(builder);
			}
		}
	}

}

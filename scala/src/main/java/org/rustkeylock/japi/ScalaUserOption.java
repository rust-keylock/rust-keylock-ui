package org.rustkeylock.japi;

import static java.util.Arrays.asList;

import java.util.List;

import com.sun.jna.Structure;

public class ScalaUserOption extends Structure {
	public static class ByReference extends ScalaUserOption implements Structure.ByReference {
	}

	public static class ByValue extends ScalaUserOption implements Structure.ByValue {
	}

	public String label;
	public String value;
	public String shortLabel;

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	@Override
	protected List<String> getFieldOrder() {
		return asList("label", "value", "shortLabel");
	}
}

package org.rustkeylock.japi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ScalaUserOptionsSet extends Structure {
	public static class ByReference extends ScalaUserOptionsSet implements Structure.ByReference {
	}

	public static class ByValue extends ScalaUserOptionsSet implements Structure.ByValue {
	}

	public ScalaUserOption.ByReference options;

	public int numberOfOptions;

	public List<ScalaUserOption> getOptions() {
		ScalaUserOption[] array = (ScalaUserOption[]) options.toArray(numberOfOptions);
		return Arrays.asList(array);
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("options", "numberOfOptions");
	}
}

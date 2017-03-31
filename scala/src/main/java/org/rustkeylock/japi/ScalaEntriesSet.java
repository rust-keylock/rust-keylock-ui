package org.rustkeylock.japi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ScalaEntriesSet extends Structure {
	public static class ByReference extends ScalaEntriesSet implements Structure.ByReference {
	}

	public static class ByValue extends ScalaEntriesSet implements Structure.ByValue {
	}

	public ScalaEntry.ByReference entries;

	public int numberOfEntries;

	public List<ScalaEntry> getEntries() {
		ScalaEntry[] array = (ScalaEntry[]) entries.toArray(numberOfEntries);
		return Arrays.asList(array);
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("entries", "numberOfEntries");
	}
}

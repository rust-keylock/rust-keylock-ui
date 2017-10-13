package org.rustkeylock.api

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Callback
import org.rustkeylock.japi.ScalaEntry
import org.rustkeylock.japi.ScalaEntriesSet

object InterfaceWithRust {
  val JNA_LIBRARY_NAME = "rustkeylockui";
  lazy val JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME);
  lazy val INSTANCE = Native.loadLibrary(JNA_LIBRARY_NAME, classOf[InterfaceWithRust]);
}

/**
 * Callback to be used for logging
 */
trait LoggingCallback extends Callback {
  def apply(level: String, path: String, file: String, line: Int, message: String): Unit
}

/**
 * Simple callback with String as argument
 */
trait RustCallback extends Callback {
  def apply(aString: String): Unit
}

/**
 * Callback containing an Entry
 */
trait EntryCallback extends Callback {
  def apply(anEntry: ScalaEntry.ByReference, entryIndex: Int, edit: Boolean, delete: Boolean): Unit
}

/**
 * Callback containing a Set of Entries
 */
trait EntriesSetCallback extends Callback {
  def apply(entriesSet: ScalaEntriesSet.ByReference, filter: String): Unit
}

trait InterfaceWithRust extends Library {

  def execute(showMenuCb: RustCallback,
    showEntryCb: EntryCallback,
    showEntriesSetCb: EntriesSetCallback,
    showMessageCb: RustCallback,
    logCb: LoggingCallback): Unit

  /**
   * Passes the Username and Password to Rust
   *
   * @param password
   * @param number
   */
  def set_password(password: String, number: Int): Unit

  /**
   * Passes a selected Entry to Rust
   *
   * @param entry
   */
  def entry_selected(anEntry: ScalaEntry): Unit

  /**
   * Passes a Menu name to Rust. Rust instructs the callback to go there
   *
   * @param menuName
   */
  def go_to_menu(menuName: String): Unit

  /**
   * Passes a Menu name to Rust and two arguments; an int and a String. Rust instructs the
   * callback to go to this menu and use the passed arguments.
   * An argNum of -1 means no argument and an argStr of zero length means no argument.
   *
   * @param menuName
   * @param argNum A String representing an Integer
   * @param argStr
   */
  def go_to_menu_plus_arg(menuName: String, argNum: String, argStr: String): Unit

  /**
   * Adds this JavaEntry to the list of Entries in memory. Note: The entry is
   * not yet encrypted and saved in the file.
   *
   * @param javaEntry
   */
  def add_entry(javaEntry: ScalaEntry): Unit

  /**
   * Replaces the Entry located at the provided index.
   *
   * @param javaEntry
   * @param index
   */
  def replace_entry(javaEntry: ScalaEntry, index: Int): Unit

  /**
   * Deletes the Entry located at the provided index.
   *
   * @param index
   */
  def delete_entry(index: Int): Unit

  /**
   * Provides to Rust a path to export to / import from
   * @param path
   * @param export. Bypass issues with boolean. 0 for false, > 0 for true
   * @param password
   * @param number
   */
  def export_import(path: String, export: Int, password: String, number: Int): Unit
}
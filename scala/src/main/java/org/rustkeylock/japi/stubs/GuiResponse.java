// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
package org.rustkeylock.japi.stubs;

import org.rustkeylock.japi.ScalaEntry;
import org.rustkeylock.japi.ScalaUserOption;

import java.util.HashMap;
import java.util.Map;

public class GuiResponse {
    public static Map<String, Object> ChangePassword(String password, Integer number) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("password", password);
        inner.put("number", number);
        Map<String, Object> map = new HashMap<>();
        map.put("ProvidedPassword", inner);
        return map;
    }

    public static Map<String, Object> GoToMenu(String menu) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("menu", menu);
        Map<String, Object> map = new HashMap<>();
        map.put("GoToMenu", inner);
        return map;
    }

    public static Map<String, Object> GoToMenuPlusArgs(String menu, String intArg, String stringArg) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("menu", menu);
        inner.put("intarg", intArg);
        inner.put("stringarg", stringArg);
        Map<String, Object> map = new HashMap<>();
        map.put("GoToMenuPlusArgs", inner);
        return map;
    }

    public static Map<String, Object> DeleteEntry(Integer index) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("index", index);
        Map<String, Object> map = new HashMap<>();
        map.put("DeleteEntry", inner);
        return map;
    }

    public static Map<String, Object> ReplaceEntry(ScalaEntry entry, Integer index) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("entry", entry);
        inner.put("index", index);
        Map<String, Object> map = new HashMap<>();
        map.put("ReplaceEntry", inner);
        return map;
    }

    public static Map<String, Object> AddEntry(ScalaEntry entry) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("entry", entry);
        Map<String, Object> map = new HashMap<>();
        map.put("AddEntry", inner);
        return map;
    }

    public static Map<String, Object> SetConfiguration(java.util.List<String> strings) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("strings", strings);
        Map<String, Object> map = new HashMap<>();
        map.put("SetConfiguration", inner);
        return map;
    }

    public static Map<String, Object> UserOptionSelected(ScalaUserOption userOption) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("user_option", userOption);
        Map<String, Object> map = new HashMap<>();
        map.put("UserOptionSelected", inner);
        return map;
    }

    public static Map<String, Object> ExportImport(String path, Integer mode, String password, Integer number) {
        Map<String, Object> inner = new HashMap<>();
        inner.put("path", path);
        inner.put("mode", mode);
        inner.put("password", password);
        inner.put("number", number);
        Map<String, Object> map = new HashMap<>();
        map.put("ExportImport", inner);
        return map;
    }
}

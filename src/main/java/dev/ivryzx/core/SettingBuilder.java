package dev.ivryzx.core;

import java.util.List;

/** Handed to Module#defineSettings so each section gets its OWN setting instances. */
public final class SettingBuilder {
    private final List<Setting<?>> list;
    SettingBuilder(List<Setting<?>> list) { this.list = list; }

    public Setting.Bool bool(String n, boolean d) { var s = new Setting.Bool(n, d); list.add(s); return s; }
    public Setting.Number number(String n, double d, double min, double max, double step) {
        var s = new Setting.Number(n, d, min, max, step); list.add(s); return s;
    }
    public Setting.Mode mode(String n, String d, String... opts) {
        var s = new Setting.Mode(n, d, opts); list.add(s); return s;
    }
}

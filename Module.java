package dev.ivryzx.core;

import net.minecraft.client.Minecraft;
import java.util.*;

/**
 * One class, many sections. A module registered in both PVP and PVE gets two
 * independent State objects (own enabled flag, keybind, settings, scratch values).
 */
public abstract class Module {
    public final String name, description;
    public final Category category;
    public final Status status;
    private final EnumMap<Section, State> states = new EnumMap<>(Section.class);

    protected Module(String name, String description, Category category, Status status) {
        this.name = name; this.description = description; this.category = category; this.status = status;
    }

    public static final class State {
        public boolean enabled;
        public int keyCode = -1;
        public final List<Setting<?>> settings = new ArrayList<>();

        /** Scratch values a module may use between ticks (timers, previous state). */
        public int counter;
        public boolean flag;

        private Setting<?> find(String n) {
            for (Setting<?> s : settings) if (s.name.equals(n)) return s;
            throw new IllegalArgumentException("No setting named " + n);
        }
        public boolean bool(String n) { return ((Setting.Bool) find(n)).get(); }
        public double num(String n) { return ((Setting.Number) find(n)).get(); }
        public String mode(String n) { return ((Setting.Mode) find(n)).get(); }
    }

    /** Called once per section this module is registered in. */
    protected abstract void defineSettings(Section section, SettingBuilder b);
    protected void onEnable(Section s, Minecraft mc) {}
    protected void onDisable(Section s, Minecraft mc) {}
    protected void onTick(Section s, Minecraft mc, State st) {}

    // ---- framework internals ----
    void bind(Section s) {
        State st = new State();
        defineSettings(s, new SettingBuilder(st.settings));
        states.put(s, st);
    }
    public Set<Section> sections() { return states.keySet(); }
    public State state(Section s) { return states.get(s); }

    public void setEnabled(Section s, boolean on, Minecraft mc) {
        State st = states.get(s);
        if (st == null || st.enabled == on) return;
        st.enabled = on;
        if (on) onEnable(s, mc); else onDisable(s, mc);
    }
    public void toggle(Section s, Minecraft mc) { setEnabled(s, !states.get(s).enabled, mc); }

    void tick(Minecraft mc) {
        for (var e : states.entrySet())
            if (e.getValue().enabled) onTick(e.getKey(), mc, e.getValue());
    }
}

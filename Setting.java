package dev.ivryzx.core;

import java.util.function.Consumer;

public abstract class Setting<T> {
    public final String name;
    protected T value;
    private Consumer<T> onChange = v -> {};

    protected Setting(String name, T def) { this.name = name; this.value = def; }

    public T get() { return value; }
    public void set(T v) { this.value = v; onChange.accept(v); }
    public Setting<T> onChange(Consumer<T> c) { this.onChange = c; return this; }

    public static class Bool extends Setting<Boolean> {
        public Bool(String n, boolean d) { super(n, d); }
        public void toggle() { set(!value); }
    }
    public static class Number extends Setting<Double> {
        public final double min, max, step;
        public Number(String n, double d, double min, double max, double step) {
            super(n, d); this.min = min; this.max = max; this.step = step;
        }
        @Override public void set(Double v) { super.set(Math.max(min, Math.min(max, v))); }
        public int asInt() { return (int) Math.round(value); }
        public float asFloat() { return value.floatValue(); }
    }
    public static class Mode extends Setting<String> {
        public final String[] options;
        public Mode(String n, String d, String... options) { super(n, d); this.options = options; }
        public void cycle() {
            int i = java.util.Arrays.asList(options).indexOf(value);
            set(options[(i + 1) % options.length]);
        }
    }
}

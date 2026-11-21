package dev.ivryzx.core;

import net.minecraft.client.Minecraft;
import java.util.function.Consumer;

/**
 * Lambda-based module so small modules need no boilerplate class.
 * Settings are rebuilt for every section, so PvP and PvE copies stay independent.
 */
public final class Simple extends Module {
    @FunctionalInterface
    public interface Hook { void run(Minecraft mc, Module.State st); }

    private final Consumer<SettingBuilder> settings;
    private final Hook tick;
    private Hook enabled = (mc, st) -> {};
    private Hook disabled = (mc, st) -> {};

    public Simple(String name, String description, Category category, Status status,
                  Consumer<SettingBuilder> settings, Hook tick) {
        super(name, description, category, status);
        this.settings = settings;
        this.tick = tick;
    }

    public Simple whenEnabled(Hook h) { this.enabled = h; return this; }
    public Simple whenDisabled(Hook h) { this.disabled = h; return this; }

    @Override protected void defineSettings(Section s, SettingBuilder b) { settings.accept(b); }
    @Override protected void onEnable(Section s, Minecraft mc) { enabled.run(mc, state(s)); }
    @Override protected void onDisable(Section s, Minecraft mc) { disabled.run(mc, state(s)); }
    @Override protected void onTick(Section s, Minecraft mc, Module.State st) { tick.run(mc, st); }
}

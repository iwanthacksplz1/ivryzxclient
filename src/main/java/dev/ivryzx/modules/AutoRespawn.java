package dev.ivryzx.modules;

import dev.ivryzx.core.*;
import net.minecraft.client.Minecraft;
import java.util.EnumMap;

/** Example module. Registered in both sections with independent settings. */
public final class AutoRespawn extends Module {
    private final EnumMap<Section, Setting.Number> delay = new EnumMap<>(Section.class);
    private final EnumMap<Section, Integer> timer = new EnumMap<>(Section.class);

    public AutoRespawn() {
        super("AutoRespawn", "Respawns automatically after dying.", Category.PLAYER, Status.EXPERIMENTAL);
    }

    @Override protected void defineSettings(Section s, SettingBuilder b) {
        delay.put(s, b.number("Delay (ticks)", 10, 0, 100, 1));
    }

    @Override protected void onTick(Section s, Minecraft mc, State st) {
        if (mc.player == null) return;
        if (mc.player.isDeadOrDying()) {
            int t = timer.merge(s, 1, Integer::sum);
            if (t >= delay.get(s).asInt()) { mc.player.respawn(); timer.put(s, 0); }
        } else timer.put(s, 0);
    }
}

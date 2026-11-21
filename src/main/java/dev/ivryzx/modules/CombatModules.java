package dev.ivryzx.modules;

import dev.ivryzx.core.Category;
import dev.ivryzx.core.Module;
import dev.ivryzx.core.ModuleManager;
import dev.ivryzx.core.Section;
import dev.ivryzx.core.Simple;
import dev.ivryzx.core.Status;
import dev.ivryzx.util.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import java.util.function.Predicate;

public final class CombatModules {
    private static final Section[] BOTH = {Section.PVP, Section.PVE};
    private static final Section[] PVP = {Section.PVP};
    private static final Section[] PVE = {Section.PVE};

    private CombatModules() {}

    private static boolean living(Entity e) {
        return e instanceof LivingEntity le && !le.isDeadOrDying();
    }

    /** Shared attack loop used by KillAura (players) and MobAura (mobs). */
    private static void aura(net.minecraft.client.Minecraft mc, Module.State st, Predicate<Entity> filter) {
        var p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) return;
        if (!p.isAlive() || mc.gui.screen() != null) return;
        if (st.counter > 0) st.counter--;
        Entity target = Util.nearest(mc, st.num("Range"), e -> living(e) && filter.test(e));
        if (target == null) return;
        if (st.bool("Rotate")) Util.aim(mc, target, 1.0f);
        if (st.bool("Use vanilla cooldown")) {
            if (!Util.cooldownReady(mc)) return;
        } else if (st.counter > 0) {
            return;
        }
        Util.attack(mc, target);
        st.counter = (int) st.num("Delay (ticks)");
    }

    public static void register() {
        // KillAura (PvP)
        ModuleManager.register(new Simple("KillAura", "Attacks the nearest player in range.",
            Category.COMBAT, Status.EXPERIMENTAL,
            b -> {
                b.number("Range", 3.5, 1.0, 6.0, 0.1);
                b.bool("Use vanilla cooldown", true);
                b.number("Delay (ticks)", 10, 0, 40, 1);
                b.bool("Rotate", false);
            },
            (mc, st) -> aura(mc, st, e -> e instanceof Player)), PVP);

        // MobAura (PvE)
        ModuleManager.register(new Simple("MobAura", "Attacks hostile mobs (or any mob) in range.",
            Category.COMBAT, Status.EXPERIMENTAL,
            b -> {
                b.number("Range", 4.0, 1.0, 6.0, 0.1);
                b.bool("Hostile only", true);
                b.bool("Use vanilla cooldown", true);
                b.number("Delay (ticks)", 10, 0, 40, 1);
                b.bool("Rotate", false);
            },
            (mc, st) -> {
                boolean hostileOnly = st.bool("Hostile only");
                aura(mc, st, e -> !(e instanceof Player)
                    && (!hostileOnly || e.getType().getCategory() == MobCategory.MONSTER));
            }), PVE);

        // TriggerBot (PvP): attacks whatever your crosshair is on
        ModuleManager.register(new Simple("TriggerBot", "Attacks the entity under your crosshair once the weapon is charged.",
            Category.COMBAT, Status.EXPERIMENTAL,
            b -> b.bool("Players only", true),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || mc.gameMode == null || mc.gui.screen() != null) return;
                if (!(mc.hitResult instanceof EntityHitResult hit)) return;
                Entity e = hit.getEntity();
                if (!living(e)) return;
                if (st.bool("Players only") && !(e instanceof Player)) return;
                if (!Util.cooldownReady(mc)) return;
                Util.attack(mc, e);
            }), PVP);

        // AimAssist (PvP)
        ModuleManager.register(new Simple("AimAssist", "Gently steers your aim toward the nearest player.",
            Category.COMBAT, Status.EXPERIMENTAL,
            b -> {
                b.number("Range", 5.0, 1.0, 8.0, 0.1);
                b.number("FOV", 90, 10, 360, 5);
                b.number("Smoothness", 0.15, 0.02, 1.0, 0.01);
                b.bool("Only while attacking", true);
            },
            (mc, st) -> {
                var p = mc.player;
                if (p == null || mc.gui.screen() != null) return;
                if (st.bool("Only while attacking") && !mc.options.keyAttack.isDown()) return;
                double half = st.num("FOV") / 2.0;
                Entity t = Util.nearest(mc, st.num("Range"), e ->
                    living(e) && e instanceof Player
                        && Math.abs(Mth.wrapDegrees(Util.yawTo(mc, e) - p.getYRot())) <= half);
                if (t != null) Util.aim(mc, t, (float) st.num("Smoothness"));
            }), PVP);

        // AutoTotem (both)
        ModuleManager.register(new Simple("AutoTotem", "Keeps a Totem of Undying in your offhand.",
            Category.COMBAT, Status.EXPERIMENTAL,
            b -> b.number("Health threshold", 20, 1, 20, 1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || mc.gameMode == null || mc.gui.screen() != null) return;
                if (st.counter > 0) { st.counter--; return; }
                if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
                if (p.getHealth() > st.num("Health threshold")) return;
                var inv = p.getInventory();
                for (int i = 0; i < 36; i++) {
                    if (!inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) continue;
                    int slot = i < 9 ? 36 + i : i;
                    mc.gameMode.handleContainerInput(p.containerMenu.containerId, slot, 40, ContainerInput.SWAP, p);
                    st.counter = 5;
                    return;
                }
            }), BOTH);
    }
}

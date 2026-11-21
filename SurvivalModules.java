package dev.ivryzx.modules;

import dev.ivryzx.core.Category;
import dev.ivryzx.core.ModuleManager;
import dev.ivryzx.core.Section;
import dev.ivryzx.core.Simple;
import dev.ivryzx.core.Status;
import dev.ivryzx.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class SurvivalModules {
    private static final Section[] BOTH = {Section.PVP, Section.PVE};
    private static final Section[] PVE = {Section.PVE};

    private SurvivalModules() {}

    /** Counts down a cooldown in st.counter; returns true when a warning may fire. */
    private static boolean ready(dev.ivryzx.core.Module.State st) {
        if (st.counter > 0) { st.counter--; return false; }
        return true;
    }

    public static void register() {
        // AutoEat
        ModuleManager.register(new Simple("AutoEat", "Eats food from your hotbar when hunger drops low.",
            Category.PLAYER, Status.EXPERIMENTAL,
            b -> b.number("Hunger threshold", 14, 1, 19, 1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || mc.gui.screen() != null) return;
                boolean hungry = p.getFoodData().getFoodLevel() <= st.num("Hunger threshold");
                if (!hungry) {
                    if (st.flag) {
                        mc.options.keyUse.setDown(false);
                        p.getInventory().setSelectedSlot(st.counter);
                        st.flag = false;
                    }
                    return;
                }
                if (st.flag) { mc.options.keyUse.setDown(true); return; }
                var inv = p.getInventory();
                for (int i = 0; i < 9; i++) {
                    if (inv.getItem(i).get(DataComponents.FOOD) == null) continue;
                    st.counter = inv.getSelectedSlot();   // remember where we were
                    inv.setSelectedSlot(i);
                    mc.options.keyUse.setDown(true);
                    st.flag = true;
                    return;
                }
            }).whenDisabled((mc, st) -> {
                mc.options.keyUse.setDown(false);
                st.flag = false;
            }), BOTH);

        // AutoRespawn
        ModuleManager.register(new AutoRespawn(), BOTH);

        // LowHealthWarning
        ModuleManager.register(new Simple("LowHealthWarning", "Warns you in chat when your health is low.",
            Category.PLAYER, Status.IMPLEMENTED,
            b -> b.number("Health (half-hearts)", 6, 1, 20, 1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                if (p.getHealth() <= st.num("Health (half-hearts)")) {
                    Util.chat(mc, "Low health!");
                    st.counter = 100;
                }
            }), BOTH);

        // HungerWarning
        ModuleManager.register(new Simple("HungerWarning", "Warns you in chat when you are getting hungry.",
            Category.PLAYER, Status.IMPLEMENTED,
            b -> b.number("Food level", 6, 1, 20, 1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                if (p.getFoodData().getFoodLevel() <= st.num("Food level")) {
                    Util.chat(mc, "You are hungry!");
                    st.counter = 200;
                }
            }), PVE);

        // DurabilityAlert
        ModuleManager.register(new Simple("DurabilityAlert", "Warns you when armor or your held item is nearly broken.",
            Category.PLAYER, Status.EXPERIMENTAL,
            b -> b.number("Percent left", 10, 1, 50, 1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
                for (EquipmentSlot slot : slots) {
                    ItemStack stack = p.getItemBySlot(slot);
                    if (stack.isEmpty() || !stack.isDamageableItem()) continue;
                    double left = 100.0 * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
                    if (left <= st.num("Percent left")) {
                        Util.chat(mc, stack.getHoverName().getString() + " is almost broken!");
                        st.counter = 600;
                        return;
                    }
                }
            }), BOTH);

        // FallDamageWarning
        ModuleManager.register(new Simple("FallDamageWarning", "Warns you while you are falling far enough to take damage.",
            Category.PLAYER, Status.EXPERIMENTAL,
            b -> b.number("Fall distance", 3.5, 2.0, 20.0, 0.5),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                if (!p.onGround() && p.fallDistance > st.num("Fall distance")) {
                    Util.chat(mc, "You are about to take fall damage!");
                    st.counter = 60;
                }
            }), PVE);

        // DrowningWarning
        ModuleManager.register(new Simple("DrowningWarning", "Warns you when you are running out of air.",
            Category.PLAYER, Status.EXPERIMENTAL,
            b -> b.number("Air left (ticks)", 100, 20, 300, 10),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                if (p.isUnderWater() && p.getAirSupply() <= st.num("Air left (ticks)")) {
                    Util.chat(mc, "You are running out of air!");
                    st.counter = 60;
                }
            }), PVE);

        // FireWarning
        ModuleManager.register(new Simple("FireWarning", "Warns you when you are on fire.",
            Category.PLAYER, Status.IMPLEMENTED,
            b -> {},
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !ready(st)) return;
                if (p.isOnFire()) {
                    Util.chat(mc, "You are on fire!");
                    st.counter = 100;
                }
            }), PVE);

        // CreeperWarning
        ModuleManager.register(new Simple("CreeperWarning", "Warns you when a creeper is close.",
            Category.PLAYER, Status.EXPERIMENTAL,
            b -> b.number("Distance", 7.0, 2.0, 20.0, 0.5),
            (mc, st) -> {
                if (mc.player == null || !ready(st)) return;
                var c = Util.nearest(mc, st.num("Distance"), e -> e.getType() == EntityType.CREEPER);
                if (c != null) {
                    Util.chat(mc, "Creeper nearby (" + Math.round(mc.player.distanceTo(c)) + " blocks)!");
                    st.counter = 60;
                }
            }), PVE);

        // DeathCoordinates
        ModuleManager.register(new Simple("DeathCoordinates", "Prints your coordinates in chat when you die.",
            Category.MISC, Status.EXPERIMENTAL,
            b -> {},
            (mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                boolean dead = p.isDeadOrDying();
                if (dead && !st.flag) {
                    var pos = p.blockPosition();
                    Util.chat(mc, "You died at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                }
                st.flag = dead;
            }), BOTH);

        // AutoMine
        ModuleManager.register(new Simple("AutoMine", "Holds the attack/mine button for you.",
            Category.AUTOMATION, Status.IMPLEMENTED,
            b -> {},
            (mc, st) -> {
                if (mc.player == null || mc.gui.screen() != null) return;
                mc.options.keyAttack.setDown(true);
            }).whenDisabled((mc, st) -> mc.options.keyAttack.setDown(false)), PVE);
    }
}

package dev.ivryzx.modules;

import dev.ivryzx.core.Category;
import dev.ivryzx.core.ModuleManager;
import dev.ivryzx.core.Section;
import dev.ivryzx.core.Simple;
import dev.ivryzx.core.Status;
import dev.ivryzx.util.Util;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

public final class MovementModules {
    private static final Section[] BOTH = {Section.PVP, Section.PVE};
    private static final Section[] PVP = {Section.PVP};
    private static final Section[] PVE = {Section.PVE};

    private MovementModules() {}

    public static void register() {
        // Sprint
        ModuleManager.register(new Simple("Sprint", "Sprints automatically while you move forward.",
            Category.MOVEMENT, Status.IMPLEMENTED,
            b -> b.bool("Not in water", true),
            (mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                if (st.bool("Not in water") && p.isInWater()) return;
                if (p.zza > 0 && !p.isShiftKeyDown()) p.setSprinting(true);
            }), BOTH);

        // Fly (abilities based)
        ModuleManager.register(new Simple("Fly", "Creative-style flight using the abilities system.",
            Category.MOVEMENT, Status.EXPERIMENTAL,
            b -> b.number("Speed", 0.05, 0.01, 0.5, 0.01),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || p.isSpectator()) return;
                p.getAbilities().setFlyingSpeed((float) st.num("Speed"));
                p.getAbilities().mayfly = true;
                p.getAbilities().flying = true;
            }).whenDisabled((mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                p.getAbilities().flying = false;
                p.getAbilities().mayfly = p.getAbilities().instabuild || p.isSpectator();
                p.getAbilities().setFlyingSpeed(0.05f);
            }), BOTH);

        // Speed (also covers the old BunnyHop idea via the Hop mode)
        ModuleManager.register(new Simple("Speed", "Raises ground speed. Hop mode also jumps for you.",
            Category.MOVEMENT, Status.EXPERIMENTAL,
            b -> {
                b.mode("Mode", "Ground", "Ground", "Hop");
                b.number("Multiplier", 1.3, 1.0, 3.0, 0.05);
            },
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !Util.moving(mc) || p.isInWater() || p.isShiftKeyDown()) return;
                if (st.mode("Mode").equals("Hop") && p.onGround()) p.jumpFromGround();
                var v = p.getDeltaMovement();
                double h = Math.hypot(v.x, v.z);
                double target = 0.28 * st.num("Multiplier");
                if (h > 0.001 && h < target) {
                    double f = target / h;
                    p.setDeltaMovement(v.x * f, v.y, v.z * f);
                }
            }), BOTH);

        // Step
        ModuleManager.register(new Simple("Step", "Lets you walk up blocks higher than one step.",
            Category.MOVEMENT, Status.IMPLEMENTED,
            b -> b.number("Height", 1.0, 0.6, 2.5, 0.1),
            (mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                var attr = p.getAttribute(Attributes.STEP_HEIGHT);
                if (attr != null) attr.setBaseValue(st.num("Height"));
            }).whenDisabled((mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                var attr = p.getAttribute(Attributes.STEP_HEIGHT);
                if (attr != null) attr.setBaseValue(0.6);
            }), BOTH);

        // AirJump
        ModuleManager.register(new Simple("AirJump", "Jump again while already in the air.",
            Category.MOVEMENT, Status.EXPERIMENTAL,
            b -> {},
            (mc, st) -> {
                var p = mc.player;
                if (p == null) return;
                boolean down = mc.options.keyJump.isDown();
                if (down && !st.flag && !p.onGround() && mc.gui.screen() == null) p.jumpFromGround();
                st.flag = down;
            }), PVP);

        // Spider
        ModuleManager.register(new Simple("Spider", "Climb walls by pushing against them.",
            Category.MOVEMENT, Status.IMPLEMENTED,
            b -> b.number("Speed", 0.2, 0.05, 0.6, 0.01),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !p.horizontalCollision) return;
                var v = p.getDeltaMovement();
                double s = st.num("Speed");
                if (v.y < s) p.setDeltaMovement(v.x, s, v.z);
            }), BOTH);

        // AutoJump
        ModuleManager.register(new Simple("AutoJump", "Jumps automatically whenever you are on the ground.",
            Category.MOVEMENT, Status.IMPLEMENTED,
            b -> b.bool("Only when moving", true),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || !p.onGround() || p.isShiftKeyDown()) return;
                if (st.bool("Only when moving") && !Util.moving(mc)) return;
                p.jumpFromGround();
            }), PVE);

        // Parkour
        ModuleManager.register(new Simple("Parkour", "Jumps for you at the edge of a block.",
            Category.MOVEMENT, Status.EXPERIMENTAL,
            b -> b.number("Edge distance", 0.001, 0.001, 0.1, 0.001),
            (mc, st) -> {
                var p = mc.player;
                if (p == null || mc.level == null) return;
                if (!p.onGround() || mc.options.keyJump.isDown() || p.isShiftKeyDown()) return;
                double e = st.num("Edge distance");
                AABB box = p.getBoundingBox().move(0, -0.5, 0).inflate(-e, 0, -e);
                if (!mc.level.getBlockCollisions(p, box).iterator().hasNext()) p.jumpFromGround();
            }), PVE);

        // AutoWalk
        ModuleManager.register(new Simple("AutoWalk", "Holds the forward key for you.",
            Category.MOVEMENT, Status.IMPLEMENTED,
            b -> {},
            (mc, st) -> {
                if (mc.player == null || mc.gui.screen() != null) return;
                mc.options.keyUp.setDown(true);
            }).whenDisabled((mc, st) -> mc.options.keyUp.setDown(false)), PVE);
    }
}

package dev.ivryzx.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import java.util.function.Predicate;

/** Every call into Minecraft that other modules share lives here, so API changes get fixed in one place. */
public final class Util {
    private Util() {}

    public static void chat(Minecraft mc, String text) {
        if (mc.player != null) mc.player.sendSystemMessage(Component.literal("[iVryzx] " + text));
    }

    public static boolean moving(Minecraft mc) {
        return mc.player != null && (mc.player.zza != 0 || mc.player.xxa != 0);
    }

    /** Closest living entity within range that passes the filter, or null. */
    public static Entity nearest(Minecraft mc, double range, Predicate<Entity> filter) {
        if (mc.level == null || mc.player == null) return null;
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !e.isAlive()) continue;
            double d = mc.player.distanceTo(e);
            if (d > range || d >= bestDist || !filter.test(e)) continue;
            best = e;
            bestDist = d;
        }
        return best;
    }

    public static float yawTo(Minecraft mc, Entity e) {
        double dx = e.getX() - mc.player.getX();
        double dz = e.getZ() - mc.player.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
    }

    public static float pitchTo(Minecraft mc, Entity e) {
        double dx = e.getX() - mc.player.getX();
        double dz = e.getZ() - mc.player.getZ();
        double dy = e.getEyeY() - mc.player.getEyeY();
        return (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
    }

    /** Turn toward an entity. smooth 1.0 = snap, smaller = gradual. */
    public static void aim(Minecraft mc, Entity e, float smooth) {
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        float dyaw = Mth.wrapDegrees(yawTo(mc, e) - yaw);
        float dpitch = pitchTo(mc, e) - pitch;
        mc.player.setYRot(yaw + dyaw * smooth);
        mc.player.setXRot(Mth.clamp(pitch + dpitch * smooth, -90f, 90f));
    }

    public static boolean cooldownReady(Minecraft mc) {
        return mc.player.getAttackStrengthScale(0.5f) >= 1.0f;
    }

    public static void attack(Minecraft mc, Entity e) {
        mc.gameMode.attack(mc.player, e);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}

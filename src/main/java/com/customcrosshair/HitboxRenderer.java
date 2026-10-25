package com.customcrosshair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

/**
 * Draws coloured hitboxes around players (and optionally mobs).
 * The box of the entity you're looking at (and close enough to hit) turns the
 * "looking at" colour, just like the crosshair's entity indicator.
 * Called every frame just before Minecraft draws its own debug shapes.
 */
public final class HitboxRenderer {
    private HitboxRenderer() { }

    public static void render() {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.hitboxesEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = mc.player;
        if (me == null || mc.world == null) return;

        float tick = mc.getRenderTickCounter().getTickProgress(true);

        // Minecraft only reports an entity here when it's under your crosshair AND within hit range.
        Entity lookedAt = (mc.crosshairTarget instanceof EntityHitResult ehr) ? ehr.getEntity() : null;

        switch (cfg.hitboxTargets) {
            case PLAYERS -> { for (PlayerEntity p : mc.world.getPlayers()) drawFor(cfg, me, p, lookedAt, tick); }
            default -> {
                for (Entity e : mc.world.getEntities()) {
                    if (cfg.hitboxTargets == CrosshairConfig.HitboxTargets.LIVING && !(e instanceof LivingEntity)) continue;
                    drawFor(cfg, me, e, lookedAt, tick);
                }
            }
        }
    }

    private static void drawFor(CrosshairConfig cfg, ClientPlayerEntity me, Entity e, Entity lookedAt, float tick) {
        if (e == me || e.isSpectator() || e.isInvisibleTo(me)) return;

        boolean targeted = e == lookedAt;
        if (!targeted && !cfg.hitboxShowOutOfRange) return;

        Box box = e.getBoundingBox();

        // move the box to where the entity is drawn this frame so it doesn't lag behind
        Vec3d smooth = e.getLerpedPos(tick).subtract(e.getEntityPos());
        Box drawn = box.offset(smooth);

        int color = targeted ? cfg.hitboxColor : cfg.hitboxOutColor;
        VisibilityConfigurable gizmo = GizmoDrawing.box(drawn, DrawStyle.stroked(color, cfg.hitboxWidth));
        if (cfg.hitboxThroughWalls) gizmo.ignoreOcclusion();
    }
}

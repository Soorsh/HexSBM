package com.hexsbm.screen.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public final class RadialRenderer {

    private RadialRenderer() {}

    public static void fillSegment(DrawContext ctx, int cx, int cy, int rIn, int rOut, double a1, double a2, int cIn, int cOut, int segmentResolution) {
        if (rIn < 0) rIn = 0;
        if (rOut <= rIn) rOut = rIn + 1;

        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();

        float ir = ((cIn >> 16) & 0xFF) / 255f, ig = ((cIn >> 8) & 0xFF) / 255f, ib = (cIn & 0xFF) / 255f, ia = ((cIn >> 24) & 0xFF) / 255f;
        float or = ((cOut >> 16) & 0xFF) / 255f, og = ((cOut >> 8) & 0xFF) / 255f, ob = (cOut & 0xFF) / 255f, oa = ((cOut >> 24) & 0xFF) / 255f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segmentResolution; i++) {
            double a = MathHelper.lerp((double) i / segmentResolution, a1, a2);
            float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
            b.vertex(m, cx + rOut * cos, cy + rOut * sin, 0).color(or, og, ob, oa).next();
            b.vertex(m, cx + rIn * cos, cy + rIn * sin, 0).color(ir, ig, ib, ia).next();
        }
        t.draw();
        RenderSystem.disableBlend();
    }

    public static boolean isPointInSegment(int px, int py, int cx, int cy, int rIn, int rOut, double a1, double a2) {
        double dx = px - cx, dy = py - cy;
        double distSq = dx * dx + dy * dy;
        int rInSq = rIn * rIn, rOutSq = rOut * rOut;
        if (distSq < rInSq || distSq > rOutSq) return false;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;
        double start = a1 < 0 ? a1 + 2 * Math.PI : a1;
        double end = a2 < 0 ? a2 + 2 * Math.PI : a2;

        if (start < end) {
            return angle >= start && angle <= end;
        } else {
            return angle >= start || angle <= end;
        }
    }

    public static class SectorAngles {
        public final double start, mid, end;

        public SectorAngles(int index, int totalSegments) {
            double segmentAngle = 2.0 * Math.PI / totalSegments;
            this.mid = -Math.PI / 2.0 + segmentAngle * index;
            this.start = this.mid - segmentAngle / 2.0;
            this.end = this.mid + segmentAngle / 2.0;
        }
    }
}
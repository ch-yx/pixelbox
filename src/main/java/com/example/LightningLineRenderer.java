package com.example;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

@Environment(value = EnvType.CLIENT)
public class LightningLineRenderer<T extends Entity>
        extends EntityRenderer<T> {
    // static Tesselator tessellator = Tesselator.getInstance();
    // static BufferBuilder bufferBuilder = tessellator.getBuilder();
    static Minecraft ins = Minecraft.getInstance();

    public LightningLineRenderer(Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(T var1) {
        return null;
    }

    public void render(T _entity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i) {
        VertexConsumer bufferBuilder = multiBufferSource.getBuffer(RenderType.debugQuads());
        // super.render(entity, f, g, poseStack, multiBufferSource, i);
        // RenderSystem.disableCull();
        // RenderSystem.enableDepthTest();

        // PoseStack matrixStack = RenderSystem.getModelViewStack();
        // matrixStack.pushPose();
        // matrixStack.mulPoseMatrix(poseStack.last().pose());
        // RenderSystem.applyModelViewMatrix();

        // bufferBuilder.begin(VertexFormat.Mode.QUADS,
        // DefaultVertexFormat.POSITION_COLOR);
        LineEntity entity = (LineEntity) _entity;
        var target = entity.getTarget();

        var v = ins.gameRenderer.getMainCamera().getPosition().subtract(entity.position());
        if(entity.getMvprogress1()<entity.getSp())return;
        var p1 = Math.min(entity.getMvprogress1(), entity.getEp());
        var p2 = Math.max(entity.getMvprogress2(), entity.getSp());
        var t1 = fp(p1, entity.getSp(), entity.getEp(), target);
        var t2 = fp(p2, entity.getSp(), entity.getEp(), target);
        drawline(poseStack.last().pose(), v, bufferBuilder, (float) t1.x, (float) t1.y, (float) t1.z, (float) t2.x,
                (float) t2.y,
                (float) t2.z, 1f, 1f, 1f, 1f, 0.1f);
        // tessellator.end();
    }

    static Vec3 fp(double p, double sp, double ep, Vec3 target) {
        return target.scale((p - sp) / (ep - sp));
    }

    public static void drawline(Matrix4f mat, Vec3 v, VertexConsumer builder,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float red1, float grn1, float blu1, float alpha, float linew) {

        // RenderSystem.getInverseViewRotationMatrix().getColumn(2, front);

        Vector3f front = new Vector3f((float) v.x, (float) v.y, (float) v.z);
        front.cross(x1 - x2, y1 - y2, z1 - z2).normalize(linew);
        var fx = front.x;
        var fy = front.y;
        var fz = front.z;
        builder.vertex(mat, x1 + fx, y1 + fy, z1 + fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2 + fx, y2 + fy, z2 + fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2 - fx, y2 - fy, z2 - fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1 - fx, y1 - fy, z1 - fz).color(red1, grn1, blu1, alpha).endVertex();
    }
}

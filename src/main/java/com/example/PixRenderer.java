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
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

@Environment(value = EnvType.CLIENT)
public class PixRenderer<T extends Entity>
        extends EntityRenderer<T> {
    static Tesselator tessellator = Tesselator.getInstance();
    static BufferBuilder bufferBuilder = tessellator.getBuilder();
    static Minecraft ins = Minecraft.getInstance();

    public PixRenderer(Context context) {
        super(context);
    }

    public static net.minecraft.client.renderer.ShaderInstance beam_shader;
    static {
        try {
            beam_shader = new net.minecraft.client.renderer.ShaderInstance(
                    new ResourceProvider() {
                        public Optional<Resource> getResource(ResourceLocation shadernamespacedname) {
                            return Optional.ofNullable(new net.minecraft.server.packs.resources.Resource(null,
                                    () -> this.getClass().getClassLoader().getResource(
                                            "assets/" + shadernamespacedname.getNamespace() + "/"
                                                    + shadernamespacedname.getPath())
                                            .openStream()) {
                                public String sourcePackId() {
                                    return "this.source.packId()";
                                }
                            });
                        }
                    }, "beam", DefaultVertexFormat.POSITION_COLOR_TEX);
        } catch (Exception e) {
            beam_shader = GameRenderer.getPositionColorTexShader();
        }
    }
    public static RenderType beamrendertype;

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(T var1) {
        return null;
    }


    public void render(T childentity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i) {
        VertexConsumer bufferBuilder = multiBufferSource.getBuffer(RenderType.debugQuads());
        // super.render(entity, f, g, poseStack, multiBufferSource, i);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        // PoseStack matrixStack = RenderSystem.getModelViewStack();
        // matrixStack.pushPose();
        // matrixStack.mulPoseMatrix(poseStack.last().pose());
        // RenderSystem.applyModelViewMatrix();

        // bufferBuilder.begin(VertexFormat.Mode.QUADS,
        // DefaultVertexFormat.POSITION_COLOR);

        var color = ((PixelEntity) childentity).getpixelcolor();

        drawBoxFaces(poseStack.last().pose(), bufferBuilder, -com.example.ExampleMod.pixsize / 2, 0,
                -com.example.ExampleMod.pixsize / 2,
                com.example.ExampleMod.pixsize / 2, com.example.ExampleMod.pixsize,
                com.example.ExampleMod.pixsize / 2, color.x, color.y, color.z, 0.8f);

    }

    public static void drawBoxFaces(Matrix4f mat, VertexConsumer builder,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float red1, float grn1, float blu1, float alpha) {

        builder.vertex(mat, x1, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y2, z1).color(red1, grn1, blu1, alpha).endVertex();

        builder.vertex(mat, x1, y1, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y1, z2).color(red1, grn1, blu1, alpha).endVertex();

        builder.vertex(mat, x1, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y2, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y1, z2).color(red1, grn1, blu1, alpha).endVertex();

        builder.vertex(mat, x2, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y1, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z1).color(red1, grn1, blu1, alpha).endVertex();

        builder.vertex(mat, x1, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y1, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y1, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y1, z2).color(red1, grn1, blu1, alpha).endVertex();

        builder.vertex(mat, x1, y2, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z1).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1, y2, z2).color(red1, grn1, blu1, alpha).endVertex();
        ///

    }

}

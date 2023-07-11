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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

@Environment(value = EnvType.CLIENT)
public class Renderer<T extends Entity>
        extends EntityRenderer<T> {
    static Tesselator tessellator = Tesselator.getInstance();
    static BufferBuilder bufferBuilder = tessellator.getBuilder();
    static Minecraft ins = Minecraft.getInstance();

    public Renderer(Context context) {
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

    @Override
    public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int i) {
        for (int childentityindex : ((CubeEntity) entity).CgetKIDS()) {
            var childentity = entity.level().getEntity(childentityindex);
            if (childentity instanceof PixelEntity pix) {
                renderP(pix, f, g, poseStack, multiBufferSource, i);
            }
        }
    }

    public void renderP(PixelEntity childentity, float f, float g, PoseStack poseStack,
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

        var con = ((PixelEntity) childentity).getconer();
        if (con != null && con.getId() > childentity.getId()) {

            if (beamrendertype == null) {

                com.example.Renderer.beamrendertype = new net.minecraft.client.renderer.RenderType.CompositeRenderType(
                        "laser_beam", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 131072, false,
                        true,
                        net.minecraft.client.renderer.RenderType.CompositeState.builder()
                                .setShaderState(new ShaderStateShard(() -> com.example.Renderer.beam_shader))
                                .setTransparencyState(com.example.mixin.RenderStateShardMixin.trans_para())
                                .setCullState(com.example.mixin.RenderStateShardMixin.cull_para())
                                .setOutputState(com.example.mixin.RenderStateShardMixin.target_para())
                                .createCompositeState(false));

            }
            // Minecraft.getInstance().levelRenderer.getItemEntityTarget().blitToScreen(Minecraft.getInstance().getWindow().getWidth()/3,
            // Minecraft.getInstance().getWindow().getHeight()/3,false);
            bufferBuilder = multiBufferSource.getBuffer(beamrendertype);
            /*
             * RenderSystem.setShader(() -> beam_shader);//
             * GameRenderer::getPositionColorShader);
             * RenderSystem.enableDepthTest();
             * RenderSystem.depthFunc(515);
             * RenderSystem.enableBlend();
             * RenderSystem.defaultBlendFunc();
             * this.bufferBuilder.begin(VertexFormat.Mode.QUADS,
             * DefaultVertexFormat.POSITION_COLOR_TEX);
             */

            // var ssssss = Float.floatToIntBits(g)^childentity.tickCount;
            var v = ins.gameRenderer.getMainCamera().getPosition().subtract(childentity.position());
            if (((PixelEntity) childentity).isattacking()) {
                drawline(poseStack.last().pose(), v, bufferBuilder, 0f, com.example.ExampleMod.pixsize / 2, 0f,
                        (float) (con.getX() - childentity.getX()),
                        (float) (com.example.ExampleMod.pixsize / 2 + con.getY() - childentity.getY()),
                        (float) (con.getZ() - childentity.getZ()), 1.0f, 0.0f, 0.0f, 0.8f, ExampleMod.pixsize / 2);
            } else {
                drawline(poseStack.last().pose(), v, bufferBuilder, 0f, com.example.ExampleMod.pixsize / 2, 0f,
                        (float) (con.getX() - childentity.getX()),
                        (float) (com.example.ExampleMod.pixsize / 2 + con.getY() - childentity.getY()),
                        (float) (con.getZ() - childentity.getZ()), 1.0f, 0.0f, 0.0f, 0.5f, ExampleMod.pixsize / 4);
            }
            // tessellator.end();
        }
        // tessellator.end();

        // matrixStack.popPose();
        // RenderSystem.applyModelViewMatrix();
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
        builder.vertex(mat, x1 + fx, y1 + fy, z1 + fz).color(red1, grn1, blu1, alpha).uv(0, 1).endVertex();
        builder.vertex(mat, x2 + fx, y2 + fy, z2 + fz).color(red1, grn1, blu1, alpha).uv(1, 1).endVertex();
        builder.vertex(mat, x2 - fx, y2 - fy, z2 - fz).color(red1, grn1, blu1, alpha).uv(1, -1).endVertex();
        builder.vertex(mat, x1 - fx, y1 - fy, z1 - fz).color(red1, grn1, blu1, alpha).uv(0, -1).endVertex();
    }
}

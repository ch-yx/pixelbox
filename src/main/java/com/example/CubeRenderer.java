package com.example;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value = EnvType.CLIENT)
public class CubeRenderer<T extends Entity>
        extends EntityRenderer<T> {
    //static Tesselator tessellator = Tesselator.getInstance();
    //static BufferBuilder bufferBuilder = tessellator.getBuilder();
    static Minecraft ins = Minecraft.getInstance();

    public CubeRenderer(Context context) {
        super(context);
    }

    public record Slink(Matrix4f pose, boolean isattacking, Vec3 mul, Vec3 mul2, VertexConsumer bufferBuilder,
            double dist, Vector3f front) implements Comparable<Slink> {
        Slink(Matrix4f pose, boolean isattacking, Vec3 mul, Vec3 mul2, VertexConsumer bufferBuilder, Vector3f front) {
            this(pose, isattacking, mul, mul2, bufferBuilder, mul.add(mul2).lengthSqr(), front);
        }

        @Override
        public int compareTo(Slink s) {
            return this.dist < s.dist ? -1 : this.dist > s.dist ? 1 : this.hashCode()<s.hashCode()? 1:this.hashCode()>s.hashCode()?-1:0;
        }


        public void action() {
            drawline(pose, bufferBuilder, (float) mul.x, (float) mul.y, (float) mul.z, (float) mul2.x, (float) mul2.y,
                    (float) mul2.z, 1.0f, 0.0f, 0.0f, isattacking ? 0.8f : 0.5f, front);
        }
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
    public static VertexConsumer bufferBuilder;

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
        poseStack.pushPose();
        poseStack.translate(-entity.getX(), (com.example.ExampleMod.pixsize / 2) - entity.getY(), -entity.getZ());

        java.util.TreeSet<Slink> lks = new java.util.TreeSet<Slink>();

        for (int childentityindex : ((CubeEntity) entity).CgetKIDS()) {
            var childentity = entity.level().getEntity(childentityindex);
            if (childentity instanceof PixelEntity pix) {
                poseStack.pushPose();
                double x = Mth.lerp((double) g, pix.xOld, pix.getX());
                double y = Mth.lerp((double) g, pix.yOld, pix.getY());
                double z = Mth.lerp((double) g, pix.zOld, pix.getZ());
                poseStack.translate(x, y, z);
                renderP(pix, f, g, poseStack, multiBufferSource, i, lks);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
        lks.forEach(Slink::action);
    }

    public void renderP(PixelEntity childentity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i, java.util.TreeSet<Slink> lks) {
        
        





        var con = ((PixelEntity) childentity).getconer();
        if (con != null && con.getId() > childentity.getId()) {

            if (beamrendertype == null) {

                com.example.CubeRenderer.beamrendertype = new net.minecraft.client.renderer.RenderType.CompositeRenderType(
                        "laser_beam", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 131072, false,
                        true,
                        net.minecraft.client.renderer.RenderType.CompositeState.builder()
                                .setShaderState(new ShaderStateShard(() -> com.example.CubeRenderer.beam_shader))
                                .setTransparencyState(com.example.mixin.RenderStateShardMixin.trans_para())
                                .setCullState(com.example.mixin.RenderStateShardMixin.cull_para())
                                .setOutputState(com.example.mixin.RenderStateShardMixin.target_para())
                                .createCompositeState(false));

            }

            bufferBuilder = multiBufferSource.getBuffer(beamrendertype);

            var v = ins.gameRenderer.getMainCamera().getPosition().subtract(childentity.position());
            var step = new Vec3((float) (con.getX() - childentity.getX()),
                    (float) (con.getY() - childentity.getY()),
                    (float) (con.getZ() - childentity.getZ()));
            int steps=(int)(step.length()/ExampleMod.pixsize)*2+1;
            Vector3f front = new Vector3f((float) v.x, (float) v.y, (float) v.z);
            front.cross((float) step.x, (float) step.y, (float) step.z).normalize(ExampleMod.pixsize / 2);
            for (int index = 0; index < steps; index++) {
                lks.add(new Slink(poseStack.last().pose(), ((PixelEntity) childentity).isattacking(),
                        step.scale(index / (double)steps), step.scale((index + 1) / (double)steps),  bufferBuilder, front));
            }
            // if (((PixelEntity) childentity).isattacking()) {
            // drawline(poseStack.last().pose(), v, bufferBuilder, 0f, 0f, 0f,
            // (float) (con.getX() - childentity.getX()),
            // (float) (con.getY() - childentity.getY()),
            // (float) (con.getZ() - childentity.getZ()), 1.0f, 0.0f, 0.0f, 0.8f,
            // ExampleMod.pixsize / 2);
            // } else {
            // drawline(poseStack.last().pose(), v, bufferBuilder, 0f, 0f, 0f,
            // (float) (con.getX() - childentity.getX()),
            // (float) (con.getY() - childentity.getY()),
            // (float) (con.getZ() - childentity.getZ()), 1.0f, 0.0f, 0.0f, 0.5f,
            // ExampleMod.pixsize / 4);
            // }
            // tessellator.end();
        }
        // tessellator.end();

        // matrixStack.popPose();
        // RenderSystem.applyModelViewMatrix();
    }

    public static void drawline(Matrix4f mat, VertexConsumer builder,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float red1, float grn1, float blu1, float alpha, Vector3f front) {

        // RenderSystem.getInverseViewRotationMatrix().getColumn(2, front);

        var fx = front.x;
        var fy = front.y;
        var fz = front.z;
        builder.vertex(mat, x1 + fx, y1 + fy, z1 + fz).color(red1, grn1, blu1, alpha).uv(0, 1).endVertex();
        builder.vertex(mat, x2 + fx, y2 + fy, z2 + fz).color(red1, grn1, blu1, alpha).uv(1, 1).endVertex();
        builder.vertex(mat, x2 - fx, y2 - fy, z2 - fz).color(red1, grn1, blu1, alpha).uv(1, -1).endVertex();
        builder.vertex(mat, x1 - fx, y1 - fy, z1 - fz).color(red1, grn1, blu1, alpha).uv(0, -1).endVertex();
    }
}

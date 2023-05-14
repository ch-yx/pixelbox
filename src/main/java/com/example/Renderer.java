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
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;

@Environment(value = EnvType.CLIENT)
public class Renderer<T extends Entity>
        extends EntityRenderer<T> {
    static Tesselator tessellator = Tesselator.getInstance();
    static BufferBuilder bufferBuilder = tessellator.getBuilder();
    static Minecraft ins = Minecraft.getInstance();
    private static int shaderindex;
    private static int cprogram;
    static {
        if(false){
        shaderindex = GlStateManager.glCreateShader(35632);
        GlStateManager.glShaderSource(shaderindex, java.util.List.of(

        "#version 150\n",

        "in vec4 vertexColor;\n",
        
        "uniform vec4 ColorModulator;\n",
        
        "out vec4 fragColor;\n",
        
        "void main() {\n",
        "    vec4 color = vertexColor;\n",
        "    if (color.a == 0.0) {\n",
        "        discard;\n",
        "    }\n",
        "    fragColor = vec4(1.0);// color * ColorModulator;\n",
        "}\n"
        
));
        GlStateManager.glCompileShader(shaderindex);
        if (GlStateManager.glGetShaderi(shaderindex, 35713) == 0) {
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n\n\n################");
        } else {
            System.out.println("%%%%%%" + shaderindex + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n\n\n################");
        }

        
        cprogram= GlStateManager.glCreateProgram();
        GL20.glUseProgram(cprogram);
        GL20.glLinkProgram(shaderindex);
    }
    }

    public Renderer(Context context) {
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

    @Override
    public void render(T childentity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int i) {
        //VertexConsumer bufferBuilder = multiBufferSource.getBuffer(RenderType.debugQuads());
        // super.render(entity, f, g, poseStack, multiBufferSource, i);
        Program prog;
        if(false){
        prog=GameRenderer.getPositionColorShader().getFragmentProgram();
        GL20.glDetachShader(GameRenderer.getPositionColorShader().getId(), ((EntityGetter)prog).id_get());
        GL20.glAttachShader(GameRenderer.getPositionColorShader().getId(), shaderindex);
        GL20.glUseProgram(GameRenderer.getPositionColorShader().getId());
        }
        //
        RenderSystem.setShader(GameRenderer::getPositionColorShader);GL20.glUseProgram(cprogram);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // PoseStack matrixStack = RenderSystem.getModelViewStack();
        // matrixStack.pushPose();
        // matrixStack.mulPoseMatrix(poseStack.last().pose());
        // RenderSystem.applyModelViewMatrix();


        var color = ((PixelEntity) childentity).getpixelcolor();

        drawBoxFaces(poseStack.last().pose(), bufferBuilder, -com.example.ExampleMod.pixsize / 2, 0,
                -com.example.ExampleMod.pixsize / 2,
                com.example.ExampleMod.pixsize / 2, com.example.ExampleMod.pixsize,
                com.example.ExampleMod.pixsize / 2, color.x, color.y, color.z, 0.8f);

        var con = ((PixelEntity) childentity).getconer();
        if (con != null && con.getId() > childentity.getId()) {

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

        }
        tessellator.end();

        // matrixStack.popPose();
        // RenderSystem.applyModelViewMatrix();
        if(false){
        GL20.glDetachShader(GameRenderer.getPositionColorShader().getId(), shaderindex);
        prog.attachToShader(GameRenderer.getPositionColorShader());
        }
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
        builder.vertex(mat, x1 + fx, y1 + fy, z1 + fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2 + fx, y2 + fy, z2 + fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x2 - fx, y2 - fy, z2 - fz).color(red1, grn1, blu1, alpha).endVertex();
        builder.vertex(mat, x1 - fx, y1 - fy, z1 - fz).color(red1, grn1, blu1, alpha).endVertex();
    }
}

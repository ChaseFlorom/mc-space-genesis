package com.florodude.spacegenesis;

import com.florodude.spacegenesis.dimension.SpaceDimension;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = SpaceGenesis.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SpaceSkyRenderer {
    private static final ResourceLocation STARFIELD_TEXTURE = ResourceLocation.parse("spacegenesis:textures/environment/starfield.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getEntity() == null) return;
        Level level = camera.getEntity().level();
        if (!level.dimension().equals(SpaceDimension.SPACE_LEVEL)) return;
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        // Don't translate to camera position - stars are infinitely far away
        // Apply the inverse of the camera's rotation to make stars appear fixed
        float yaw = camera.getYRot();  // Positive = counter-rotate against camera
        float pitch = camera.getXRot(); // Positive = counter-rotate against camera
        
        // Apply rotations in the correct order: pitch first, then yaw
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));  // Changed to XP and positive pitch
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, STARFIELD_TEXTURE);

        float size = 100.0F;
        for (int i = 0; i < 6; ++i) {
            poseStack.pushPose();
            Matrix4f matrix = poseStack.last().pose();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            switch (i) {
                case 0: // Up (+Y)
                    buffer.addVertex(matrix, -size, size, -size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, -size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, size, size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
                case 1: // Down (-Y)
                    buffer.addVertex(matrix, -size, -size, size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, -size, size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, -size, -size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, -size, -size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
                case 2: // East (+X)
                    buffer.addVertex(matrix, size, -size, -size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, -size, size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, -size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
                case 3: // West (-X)
                    buffer.addVertex(matrix, -size, -size, size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, -size, -size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, size, -size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, size, size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
                case 4: // North (+Z)
                    buffer.addVertex(matrix, -size, -size, size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, -size, size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, size, size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
                case 5: // South (-Z)
                    buffer.addVertex(matrix, size, -size, -size).setUv(0, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, -size, -size).setUv(1, 0).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, -size, size, -size).setUv(1, 1).setColor(255, 255, 255, 255);
                    buffer.addVertex(matrix, size, size, -size).setUv(0, 1).setColor(255, 255, 255, 255);
                    break;
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        poseStack.popPose();
    }
} 
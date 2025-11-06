package com.silvermoon.smallphone;

import java.awt.*;
import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.gtnewhorizon.gtnhlib.client.renderer.shader.ShaderProgram;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class PhoneScreenRenderer {

    public static final PhoneScreenRenderer INSTANCE = new PhoneScreenRenderer();
    private static final String MODID = "smallphone";

    private ShaderProgram shaderProgram;
    private ResourceLocation backgroundTexture;
    private Framebuffer postFramebuffer;

    private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
    private int oldViewportX, oldViewportY, oldViewportW, oldViewportH;

    private int u_GameTexture = -1;
    private int u_BackgroundTexture = -1;
    private int u_Resolution = -1;
    private int u_Time = -1;

    public boolean isEnabled = false;

    private static final float BG_SIZE_X = 2560.0f;
    private static final float BG_SIZE_Y = 1440.0f;

    private PhoneScreenRenderer() {
        this.backgroundTexture = new ResourceLocation(MODID, "textures/gui/phone_background.png");
    }

    public void init(boolean debug) {
        try {
            shaderProgram = new ShaderProgram(MODID, "shaders/phonescreen.vert.glsl", "shaders/phonescreen.frag.glsl");

            if (shaderProgram.getProgram() == 0) {
                this.shaderProgram = null;
                throw new RuntimeException("Failed to load phone screen shader!");
            }

            shaderProgram.use();
            u_GameTexture = shaderProgram.getUniformLocation("u_GameTexture");
            u_BackgroundTexture = shaderProgram.getUniformLocation("u_BackgroundTexture");
            u_Resolution = shaderProgram.getUniformLocation("u_Resolution");
            u_Time = shaderProgram.getUniformLocation("u_Time");
            ShaderProgram.clear();

            FMLCommonHandler.instance()
                .bus()
                .register(this);

        } catch (Exception e) {
            System.err.println("Failed to initialize PhoneScreenRenderer!");
            e.printStackTrace();
            this.shaderProgram = null;
        }
    }

    public void toggle() {
        this.isEnabled = !this.isEnabled;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!isEnabled || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        this.renderShaderEffect(res, mc.displayWidth, mc.displayHeight);
    }

    public Point getProjectedMouse(int screenMouseX, int screenMouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isEnabled || shaderProgram == null || mc == null || mc.displayWidth == 0 || mc.displayHeight == 0) {
            return new Point(screenMouseX, screenMouseY);
        }

        final float screenWidth = (float) mc.displayWidth;
        final float screenHeight = (float) mc.displayHeight;
        float screen_uv_x = (float) screenMouseX / screenWidth;
        float screen_uv_y = (float) screenMouseY / screenHeight;
        float window_aspect = screenWidth / screenHeight;
        float texture_aspect = BG_SIZE_X / BG_SIZE_Y;
        float scale_x = 1.0f;
        float scale_y = 1.0f;
        float offset_x = 0.0f;
        float offset_y = 0.0f;

        if (window_aspect > texture_aspect) {
            scale_y = window_aspect / texture_aspect;
            offset_y = (1.0f - scale_y) / 2.0f;
        } else {
            scale_x = texture_aspect / window_aspect;
            offset_x = (1.0f - scale_x) / 2.0f;
        }
        float bg_uv_x = (screen_uv_x - offset_x) / scale_x;
        float bg_uv_y = (screen_uv_y - offset_y) / scale_y;

        if (bg_uv_x < 0.0f || bg_uv_x > 1.0f || bg_uv_y < 0.0f || bg_uv_y > 1.0f) {
            return new Point(screenMouseX, screenMouseY);
        }
        float game_uvw_x = (1.98757398f * bg_uv_x) + (-0.03897204f * bg_uv_y) - 0.22720699f;
        float game_uvw_y = (0.69139027f * bg_uv_x) + (2.29415864f * bg_uv_y) - 0.74827284f;
        float game_uvw_z = (0.52946318f * bg_uv_x) + (0.13513074f * bg_uv_y) + 1.0f;
        float game_uv_x = game_uvw_x / game_uvw_z;
        float game_uv_y = game_uvw_y / game_uvw_z;

        final float bottom_tolerance = 0.02f;
        if (game_uv_x >= 0.0f && game_uv_x <= 1.0f && game_uv_y >= -bottom_tolerance && game_uv_y <= 1.0f) {
            game_uv_x = Math.max(0.0f, Math.min(1.0f, game_uv_x));
            game_uv_y = Math.max(0.0f, Math.min(1.0f, game_uv_y));
            int projectedX = (int) (game_uv_x * screenWidth);
            int projectedY = (int) (game_uv_y * screenHeight);
            return new Point(projectedX, projectedY);
        } else {
            return new Point(screenMouseX, screenMouseY);
        }
    }

    private void renderShaderEffect(ScaledResolution res, int screenWidth, int screenHeight) {
        if (!isEnabled || shaderProgram == null || shaderProgram.getProgram() == 0) {
            return;
        }

        // FBO
        if (this.postFramebuffer == null || this.postFramebuffer.framebufferWidth != screenWidth
            || this.postFramebuffer.framebufferHeight != screenHeight) {
            if (this.postFramebuffer != null) {
                this.postFramebuffer.deleteFramebuffer();
            }
            this.postFramebuffer = new Framebuffer(screenWidth, screenHeight, false);
        }

        // GL status
        viewportBuffer.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);
        oldViewportX = viewportBuffer.get(0);
        oldViewportY = viewportBuffer.get(1);
        oldViewportW = viewportBuffer.get(2);
        oldViewportH = viewportBuffer.get(3);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        // Fullscreen
        GL11.glViewport(0, 0, screenWidth, screenHeight);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double) screenWidth, (double) screenHeight, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

        // Render postFramebuffer
        this.postFramebuffer.bindFramebuffer(true);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        shaderProgram.use();

        OpenGlHelper.setActiveTexture(GL13.GL_TEXTURE1);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(backgroundTexture);
        OpenGlHelper.setActiveTexture(GL13.GL_TEXTURE0);
        Minecraft.getMinecraft()
            .getFramebuffer()
            .bindFramebufferTexture();

        GL20.glUniform1i(u_GameTexture, 0);
        GL20.glUniform1i(u_BackgroundTexture, 1);
        GL20.glUniform2f(u_Resolution, (float) screenWidth, (float) screenHeight);
        GL20.glUniform1f(u_Time, (float) (Minecraft.getSystemTime() % 100000) / 1000.0f);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, screenHeight, 0, 0.0, 0.0);
        tessellator.addVertexWithUV(screenWidth, screenHeight, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(screenWidth, 0, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(0, 0, 0, 0.0, 1.0);
        tessellator.draw();

        ShaderProgram.clear();
        OpenGlHelper.setActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        OpenGlHelper.setActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Back to framebuffer
        Minecraft.getMinecraft()
            .getFramebuffer()
            .bindFramebuffer(true);
        this.postFramebuffer.framebufferRender(screenWidth, screenHeight);

        // GL status again
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glViewport(oldViewportX, oldViewportY, oldViewportW, oldViewportH);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }
}

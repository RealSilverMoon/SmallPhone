package com.silvermoon.smallphone.mixins;

import java.awt.Point;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.silvermoon.smallphone.PhoneScreenRenderer;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    // 我们需要一个地方来存储 Y 坐标，因为 getEventX() 总是在 getEventY() 之前被调用
    private int smallphone_projectedEventY = 0;

    /**
     * 重定向对 Mouse.getEventX() 的调用。
     * 我们在这里同时计算 X 和 Y，存储 Y 供下次调用。
     */
    @Redirect(
        method = "handleMouseInput()V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventX()I", remap = false // 必须为 false，因为 Mouse 是
                                                                                                 // LWJGL 类
        ))
    private int smallphone_redirectGetEventX() {
        int rawX = Mouse.getEventX();
        int rawY = Mouse.getEventY(); // 立即获取原始 Y

        // 运行投影
        Point projected = PhoneScreenRenderer.INSTANCE.getProjectedMouse(rawX, rawY);

        // 存储投影后的 Y，供 getEventY() 重定向使用
        this.smallphone_projectedEventY = projected.y;

        // 返回投影后的 X
        return projected.x;
    }

    /**
     * 重定向对 Mouse.getEventY() 的调用。
     * 我们只返回上一个方法中存储的值。
     */
    @Redirect(
        method = "handleMouseInput()V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventY()I", remap = false // 必须为 false
        ))
    private int smallphone_redirectGetEventY() {
        // 返回之前存储的 Y 值
        return this.smallphone_projectedEventY;
    }
}

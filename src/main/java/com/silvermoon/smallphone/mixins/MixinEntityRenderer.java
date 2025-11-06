package com.silvermoon.smallphone.mixins;

import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.silvermoon.smallphone.PhoneScreenRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @ModifyArgs(
        method = "updateCameraAndRender(F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V"))
    private void smallphone_modifyDrawScreenMouseCoords(Args args) {
        // 这是一个高频调用的方法，快速检查
        if (this.mc == null || !PhoneScreenRenderer.INSTANCE.isEnabled) {
            return;
        }

        // 1. 获取原始的 LWJGL 鼠标坐标 (0,0 在左下角)
        int rawX = Mouse.getX();
        int rawY = Mouse.getY();

        // 2. 调用我们的 Java 投影逻辑
        Point projected = PhoneScreenRenderer.INSTANCE.getProjectedMouse(rawX, rawY);

        // 3. 如果坐标没有变化 (例如，Mod被禁用或鼠标在屏幕外)，
        // 我们不需要做任何事，原始的 args[0] (i1) 和 args[1] (j1) 已经是正确的了。
        if (projected.x == rawX && projected.y == rawY) {
            return;
        }

        // 4. 坐标被投影了！我们必须重新计算 *缩放后* 的坐标
        // (这部分逻辑与 EntityRenderer 中原始代码匹配)

        // 我们不能在这里访问局部变量 k 和 l，所以我们重新创建它们
        ScaledResolution sr = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int k_scaledWidth = sr.getScaledWidth();
        int l_scaledHeight = sr.getScaledHeight();

        // 5. 使用 *投影后* 的坐标 (projected.x, projected.y) 重新计算 i1 和 j1
        int projectedScaledX = projected.x * k_scaledWidth / this.mc.displayWidth;
        int projectedScaledY = l_scaledHeight - projected.y * l_scaledHeight / this.mc.displayHeight - 1;

        // 6. 设置新的参数
        args.set(0, projectedScaledX); // mouseX (i1)
        args.set(1, projectedScaledY); // mouseY (j1)
    }
}

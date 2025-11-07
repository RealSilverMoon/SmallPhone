package com.silvermoon.smallphone;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {

    public static KeyBinding toggleKey;

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        PhoneScreenRenderer.INSTANCE.init(true);
        toggleKey = new KeyBinding("key.shot", Keyboard.KEY_P, "key.categories.smallphone");
        ClientRegistry.registerKeyBinding(toggleKey);
    }

    @Override
    public void setRenderStatus(boolean status) {
        PhoneScreenRenderer.INSTANCE.isEnabled = status;
    }
}

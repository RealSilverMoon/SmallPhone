package com.silvermoon.smallphone;

import net.minecraft.entity.player.EntityPlayerMP;

import com.silvermoon.smallphone.network.PacketHandler;
import com.silvermoon.smallphone.network.PacketServerPing;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class EventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PacketHandler.INSTANCE.sendTo(new PacketServerPing(), (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ClientProxy.toggleKey.isPressed()) {
            if (!PhoneScreenRenderer.INSTANCE.isServerModded) {
                SmallPhone.proxy.setRenderStatus(!PhoneScreenRenderer.INSTANCE.isEnabled);
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PhoneScreenRenderer.INSTANCE.isServerModded = false;
    }
}

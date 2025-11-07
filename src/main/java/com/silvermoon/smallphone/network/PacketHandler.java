package com.silvermoon.smallphone.network;

import com.silvermoon.smallphone.SmallPhone;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SmallPhone.MODID);

    public static void register() {
        INSTANCE.registerMessage(PacketServerPing.Handler.class, PacketServerPing.class, 0, Side.CLIENT);
    }
}

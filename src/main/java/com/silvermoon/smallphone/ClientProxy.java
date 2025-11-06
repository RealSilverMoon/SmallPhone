package com.silvermoon.smallphone;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        PhoneScreenRenderer.INSTANCE.init(true);
    }
}

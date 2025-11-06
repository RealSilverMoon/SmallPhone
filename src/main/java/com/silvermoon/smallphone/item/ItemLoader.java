package com.silvermoon.smallphone.item;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLoader {

    public ItemPhone smallphone;
    public static ItemLoader instance = new ItemLoader();

    public void registerItem(FMLPreInitializationEvent event) {
        smallphone = new ItemPhone();
        GameRegistry.registerItem(smallphone, "smallphone");
    }
}

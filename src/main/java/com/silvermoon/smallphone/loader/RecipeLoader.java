package com.silvermoon.smallphone.loader;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.silvermoon.smallphone.item.ItemLoader;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader {

    public static void loadRecipes() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ItemLoader.instance.smallphone,
                "SSS",
                "MAL",
                "PHO",
                'S',
                Blocks.glass_pane,
                'M',
                Blocks.redstone_torch,
                'A',
                Items.nether_star,
                'L',
                Items.ender_pearl,
                'P',
                Items.comparator,
                'H',
                Blocks.stone_button,
                'O',
                Items.repeater));
    }
}

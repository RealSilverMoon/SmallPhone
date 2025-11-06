package com.silvermoon.smallphone.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.silvermoon.smallphone.PhoneScreenRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPhone extends Item {

    private static final String NBT_ON_SHOT = "onShot";
    private static final String NBT_TIMESTAMP = "timestamp";
    private static final long COOLDOWN_SECONDS = 300;
    private static final int POTION_DURATION_TICKS = (int)COOLDOWN_SECONDS * 20;
    private static final int POTION_AMPLIFIER_V = 4;

    public ItemPhone() {
        super();
        setUnlocalizedName("smallphone");
        setTextureName("smallphone:smallphone");
        setCreativeTab(CreativeTabs.tabTools);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_,
                               boolean p_77624_4_) {
        p_77624_3_.add(StatCollector.translateToLocal("item.smallphone.desc.1"));
        p_77624_3_.add(" ");
        p_77624_3_.add(" ");
        p_77624_3_.add(StatCollector.translateToLocal("item.smallphone.desc.2"));
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x,
                             int y, int z, int side, float hitX, float hitY, float hitZ) {

        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = itemStack.stackTagCompound;

        if (nbt.hasKey(NBT_ON_SHOT) && nbt.hasKey(NBT_TIMESTAMP)) {
            long savedTimestamp = nbt.getLong(NBT_TIMESTAMP);
            long timeDifference = currentTimeSeconds - savedTimestamp;

            if (timeDifference < COOLDOWN_SECONDS) {
                if (world.isRemote) {
                    long remainingSeconds = COOLDOWN_SECONDS - timeDifference;
                    player.addChatMessage(new ChatComponentText(String.format(StatCollector.translateToLocal("chat.smallphone.cooldown"),remainingSeconds)));
                }
            } else {
                int onShot = nbt.getInteger(NBT_ON_SHOT);
                if (onShot == 1) {

                    if (!world.isRemote) {
                        itemStack.setTagCompound(null);

                        player.removePotionEffect(Potion.resistance.id);
                        player.removePotionEffect(Potion.moveSpeed.id);
                        player.removePotionEffect(Potion.jump.id);
                        player.removePotionEffect(Potion.waterBreathing.id);
                        player.removePotionEffect(Potion.digSpeed.id);
                        player.removePotionEffect(Potion.regeneration.id);
                    }

                    if (world.isRemote) {
                        PhoneScreenRenderer.INSTANCE.isEnabled = false;
                    }
                }
            }

        } else {
            if (!world.isRemote) {
                nbt.setInteger(NBT_ON_SHOT, 1);
                nbt.setLong(NBT_TIMESTAMP, currentTimeSeconds);

                player.addPotionEffect(new PotionEffect(Potion.resistance.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
                player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
                player.addPotionEffect(new PotionEffect(Potion.jump.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
                player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
                player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
                player.addPotionEffect(new PotionEffect(Potion.regeneration.id, POTION_DURATION_TICKS, POTION_AMPLIFIER_V));
            }

            if (world.isRemote) {
                PhoneScreenRenderer.INSTANCE.isEnabled = true;
            }
        }

        return true;
    }
}

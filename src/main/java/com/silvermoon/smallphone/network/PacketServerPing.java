package com.silvermoon.smallphone.network;

import com.silvermoon.smallphone.PhoneScreenRenderer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketServerPing implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketServerPing, IMessage> {

        @Override
        public IMessage onMessage(PacketServerPing message, MessageContext ctx) {
            PhoneScreenRenderer.INSTANCE.isServerModded = true;
            return null;
        }
    }
}

package com.leclowndu93150.framedblocksadditions.network;

import com.leclowndu93150.framedblocksadditions.Framedblocksadditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Framedblocksadditions.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void init() {
        INSTANCE.registerMessage(id++,
                BoatBuoyancyPacket.class,
                BoatBuoyancyPacket::encode,
                BoatBuoyancyPacket::new,
                BoatBuoyancyPacket::handle
        );
    }
}
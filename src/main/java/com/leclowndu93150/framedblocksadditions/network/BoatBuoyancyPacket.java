package com.leclowndu93150.framedblocksadditions.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BoatBuoyancyPacket {
    private final int entityId;
    private final double motionX;
    private final double motionY;
    private final double motionZ;

    public BoatBuoyancyPacket(int entityId, Vec3 motion) {
        this.entityId = entityId;
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
    }

    public BoatBuoyancyPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.motionX = buf.readDouble();
        this.motionY = buf.readDouble();
        this.motionZ = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(motionX);
        buf.writeDouble(motionY);
        buf.writeDouble(motionZ);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ctx.get().getSender().level().getEntity(entityId);
            if (entity instanceof Boat boat) {
                boat.setDeltaMovement(motionX, motionY, motionZ);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
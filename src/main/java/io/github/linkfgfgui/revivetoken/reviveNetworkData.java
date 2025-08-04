package io.github.linkfgfgui.revivetoken;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public record reviveNetworkData(String playerUUIDString, Vector3f pos,Vector3f look,String dim) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<reviveNetworkData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("revivetoken", "revive_player"));

    public static final StreamCodec<ByteBuf, reviveNetworkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            reviveNetworkData::playerUUIDString,
            ByteBufCodecs.VECTOR3F,
            reviveNetworkData::pos,
            ByteBufCodecs.VECTOR3F,
            reviveNetworkData::look,
            ByteBufCodecs.STRING_UTF8,
            reviveNetworkData::dim,
            reviveNetworkData::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
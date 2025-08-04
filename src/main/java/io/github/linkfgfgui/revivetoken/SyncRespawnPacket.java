package io.github.linkfgfgui.revivetoken;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncRespawnPacket(boolean idk) implements CustomPacketPayload {

    public static final Type<SyncRespawnPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("revivetoken", "respawn_event"));

    public static final StreamCodec<ByteBuf, SyncRespawnPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SyncRespawnPacket::idk,
            SyncRespawnPacket::new
    );
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
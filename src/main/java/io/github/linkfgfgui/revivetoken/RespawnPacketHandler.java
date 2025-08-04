package io.github.linkfgfgui.revivetoken;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RespawnPacketHandler{

    public static void handleDataOnMain(final SyncRespawnPacket data, final IPayloadContext context) {
        context.player().respawn();
    }

}

package io.github.linkfgfgui.revivetoken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ClientPayloadHandler {

    public static void handleDataOnMain(final reviveNetworkData data, final IPayloadContext context) {
        Player player = context.player();
        if (!player.getStringUUID().equals(data.playerUUIDString())) {
            Revivetoken.LOGGER.error("UUID not same!!!");
            return;
        }
        Revivetoken.LOGGER.warn(data.dim());


        Revivetoken.reviveMap.put(data.playerUUIDString(),
                new Revivetoken.Triple<>(
                        new Vec3(data.pos()),
                        new Vec3(data.look()),
                        ResourceLocation.parse(data.dim()))
        );
        if (Config.cost_count > 0) {
            player.getInventory().clearOrCountMatchingItems(
                    itemStack -> itemStack.getItem() == Revivetoken.TokenItem,
                    Config.cost_count, player.getInventory()
            );
        }
        player.respawn();
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncRespawnPacket(false));

    }

}

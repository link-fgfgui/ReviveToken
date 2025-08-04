package io.github.linkfgfgui.revivetoken.mixin;

import com.google.common.collect.ImmutableList;
import io.github.linkfgfgui.revivetoken.Revivetoken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(Player.class)
public class NoLootCoin {
    @Redirect(
            method = "dropEquipment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"
            )
    )
    private void preventInventoryDrop(Inventory inventory) {
        for (List<ItemStack> list : ImmutableList.of(inventory.items, inventory.armor, inventory.offhand)) {
            for (int i = 0; i < list.size(); i++) {
                ItemStack itemstack = list.get(i);
                if (!itemstack.isEmpty() && !itemstack.getItem().equals(Revivetoken.TokenItem)) {
                    inventory.player.drop(itemstack, true, false);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}

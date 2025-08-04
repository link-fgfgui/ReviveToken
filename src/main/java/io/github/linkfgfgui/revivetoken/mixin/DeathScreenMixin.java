package io.github.linkfgfgui.revivetoken.mixin;


import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.github.linkfgfgui.revivetoken.Config;
import io.github.linkfgfgui.revivetoken.Revivetoken;
import io.github.linkfgfgui.revivetoken.reviveNetworkData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {


    private Button reviveButton = null;

    @Shadow
    @Final
    private boolean hardcore;

    @Shadow
    private int delayTicker;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {

        ScreenAccessorMixin screen = (ScreenAccessorMixin) this;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (this.hardcore && !Config.hardcore_enabled) {
            return;
        }
        if (player == null) {
            return;
        }
        int coin_count = player.getInventory().countItem(Revivetoken.TokenItem);

        if (coin_count < Config.cost_count) {
            return;
        }

        int ButtonHeight = 20;
        reviveButton = Button.builder(

                        (Config.cost_count==1 ? Component.translatable("revivetoken.revive", coin_count):Component.translatable("revivetoken.revive_plural",Config.cost_count, coin_count)),
                        btn -> {
                            PacketDistributor.sendToServer(
                                    new reviveNetworkData(
                                            player.getStringUUID(),
                                            player.getPosition(client.getFrameTimeNs()).toVector3f(),
                                            player.getLookAngle().toVector3f(),
                                            player.level().dimension().location().toString()));
                            btn.active=false;
                        })
                .pos(screen.getWidth() / 2 - 100, screen.getHeight() / 4 + 96 + ButtonHeight + 4)  // 按钮位置
                .size(200, ButtonHeight) // 按钮大小
                .build();
        reviveButton.active = false;
        screen.getRenderables().add(reviveButton);
        screen.getChildren().add(reviveButton);
    }

    @Inject(method = "setButtonsActive", at = @At("TAIL"))
    private void onSetButtonsActive(boolean active, CallbackInfo ci) {
        if (reviveButton != null) {
            reviveButton.active = active;
        }
    }
}

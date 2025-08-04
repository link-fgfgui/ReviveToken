package io.github.linkfgfgui.revivetoken;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import java.util.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Revivetoken.MODID)
public class Revivetoken {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "revivetoken";
//    public static final String MOD_ID = MODID;
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public record Triple<A, B, C>(A pos, B look, C dim) {}

    public static final Map<String,Triple<Vec3,Vec3,ResourceLocation>> reviveMap = new HashMap<>();

    public static Item TokenItem = null;




    // Create a Deferred Register to hold Blocks which will all be registered under the "revivetoken" namespace
//    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "revivetoken" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "revivetoken" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

//    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);


    public static final DeferredItem<Item> COIN_ITEM = ITEMS.registerSimpleItem("coin");

    // Creates a new food item with the id "revivetoken:example_id", nutrition 1 and saturation 2
//    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
//    public static final Supplier<AttachmentType<FlagData>> FLAG =
//            ATTACHMENT_TYPES.register("revive_flag",
//                    () -> AttachmentType.<FlagData>builder(FlagData::new)
//                            .serialize(FlagData.CODEC)
//                            .copyOnDeath() // 自动克隆玩家时保留数据
//                            .build()
//            );
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Revivetoken(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
//        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Revivetoken) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerNetwork);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onWandererTrades(WandererTradesEvent event) {
        if (Config.notrade) return;
        event.getGenericTrades().add((entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 5),
                new ItemStack(COIN_ITEM.get(), 1),
                6,
                2,
                0.01F
        ));
        event.getRareTrades().add((entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 2),
                new ItemStack(COIN_ITEM.get(), 1),
                4,
                10,
                0.01F
        ));
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (Config.notrade) return;
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            trades.get(3).add((entity, random) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 24),
                    new ItemStack(COIN_ITEM.get(), 1),
                    16,
                    2,
                    0.2F
            ));
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        event.getDrops().removeIf(entityItem -> entityItem.getItem().getItem().equals(TokenItem));
    }

    @SubscribeEvent
    public void onRespawnPos(PlayerRespawnPositionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        String playerUUIDString=p.getStringUUID();
        if (reviveMap.containsKey(playerUUIDString)){
            Triple<Vec3, Vec3, ResourceLocation> t = reviveMap.remove(playerUUIDString);
            ServerLevel level=e.getEntity().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,t.dim));
            float yRot = (float)(Mth.atan2(-t.look.x, t.look.z) * (180.0F / Mth.PI));
            float xRot = (float)(Mth.atan2(-t.look.y, Mth.sqrt((float) (t.look.x * t.look.x + t.look.z * t.look.z))) * (180.0F / Mth.PI));
            e.setDimensionTransition(new DimensionTransition(level,t.pos,Vec3.ZERO,yRot,xRot,DimensionTransition.DO_NOTHING));
        }
//        e.getDimensionTransition()
//        // 示例: 利用玩家权限 “revive.override” 在死亡时传送至特别 spawn
//        if (!e.isFromEndFight()) {
//            // 指定到自定义维度（如 The_Nether）
//            ServerLevel tgt = p.getServer()
//                    .getLevel(ResourceKey.create(RegistryKey.createRegistryKey("dim"), new ResourceLocation("the_nether")));
//            e.setRespawnLevel(tgt);
//
//            // 固定至坐标 (123,80,-456)
//            e.setRespawnPosition(new Vec3(123.5, 80, -456.5f));
//            e.setRespawnAngle(180.0f); // 面向南
//
//            e.setChangePlayerSpawnPosition(false);
//            p.sendSystemMessage(Component.literal("已传送至复活点"));
//        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        newPlayer.getInventory().replaceWith(original.getInventory());
    }

    // on the mod event bus
    public void registerNetwork(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                reviveNetworkData.TYPE,
                reviveNetworkData.STREAM_CODEC,
                ClientPayloadHandler::handleDataOnMain
        );
        registrar.playToClient(
                SyncRespawnPacket.TYPE,
                SyncRespawnPacket.STREAM_CODEC,
                RespawnPacketHandler::handleDataOnMain
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(COIN_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        RegistryAccess access = event.getServer().registryAccess();
        TokenItem = access.registryOrThrow(Registries.ITEM).get(Config.token_item);

    }



    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
        }
    }
}

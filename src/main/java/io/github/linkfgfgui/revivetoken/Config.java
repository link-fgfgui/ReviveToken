package io.github.linkfgfgui.revivetoken;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Revivetoken.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> TOKEN_ITEM = BUILDER.comment("Specify the Token item to consume").define("token","revivetoken:coin",obj -> obj instanceof String && ResourceLocation.tryParse((String) obj) != null);

    private static final ModConfigSpec.IntValue COST_COUNT = BUILDER.comment("Specify the Item count to consume").defineInRange("cost", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue NOTRADE = BUILDER.comment("Disable the trade recipes of Coin").define("notrade",false);

    private static final ModConfigSpec.BooleanValue HARDCORE_ENABLED = BUILDER.comment("(WIP)Make this mod available in Hardcore").define("hardcore",false);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean hardcore_enabled;

    public static boolean notrade;

    public static ResourceLocation token_item;

    public static int cost_count;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
//        hardcore_enabled = HARDCORE_ENABLED.get();
        hardcore_enabled=false;
        token_item= ResourceLocation.parse(TOKEN_ITEM.get());
        cost_count=COST_COUNT.get();
        notrade=NOTRADE.get();
    }
}

package com.unddefined.natureinabottle;

import com.unddefined.natureinabottle.config.ModSettings;
import com.unddefined.natureinabottle.crops.HarvestTracker;
import com.unddefined.natureinabottle.items.NatureInABottleItem;
import com.unddefined.natureinabottle.registry.ModRecipes;
import com.unddefined.natureinabottle.server.events.BottleTicker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;

/**
 * Nature in a Bottle（NeoForge 1.21.1），时间之瓶（Time in a Bottle）的附属。
 * <p>
 * 时间之瓶的机制是“右击方块叠加速倍率，让方块一直跑随机刻”；瓶中自然是同一套机制换一种功能：
 * 拿它右击成熟作物会给这块作物叠一层倍率，停手一小会儿后按倍率直接掉落作物产物。
 * <p>
 * 两个物品共用时间之瓶的 {@code stored_time} 组件和计费公式，堆叠数同样是 1，配方上可以互相转化。
 * 数值走 common 配置，见 {@link ModSettings}。
 */
@Mod(NatureInABottle.MOD_ID)
public final class NatureInABottle {

    public static final String MOD_ID = "nature_in_a_bottle";

    /**
     * 时间之瓶自己的创造页，瓶中自然跟它摆在一起。
     */
    public static final ResourceKey<CreativeModeTab> TIAB_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("tiab", "tiab"));

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    /**
     * 瓶中自然：与时间之瓶共用 stored_time 组件，初始也是空的（0/0），堆叠数为 1。
     */
    public static final DeferredItem<NatureInABottleItem> NATURE_IN_A_BOTTLE = ITEMS.register("nature_in_a_bottle",
            () -> new NatureInABottleItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ICommonTimeInABottleAPI.COMMON_API.get().getRegistration().getStoredTime(),
                            ICommonTimeInABottleAPI.COMMON_API.get().createStoredTimeComponent(0, 0))));

    public NatureInABottle(IEventBus modBus, ModContainer container) {
        ITEMS.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, ModSettings.SPEC);

        modBus.addListener(NatureInABottle::buildCreativeTab);
        NeoForge.EVENT_BUS.addListener(BottleTicker::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HarvestTracker::onLevelTick);
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(TIAB_TAB)) {
            event.accept(new ItemStack(NATURE_IN_A_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}

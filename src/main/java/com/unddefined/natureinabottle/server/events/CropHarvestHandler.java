package com.unddefined.natureinabottle.server.events;

import com.unddefined.natureinabottle.config.ModSettings;
import com.unddefined.natureinabottle.crops.CropHarvest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.api.ITiabConfig;
import org.mangorage.tiab.common.api.impl.IStoredTimeComponent;
import org.mangorage.tiab.common.api.impl.ITiabItem;
import org.mangorage.tiab.common.misc.CommonHelper;
import org.mangorage.tiab.common.misc.CommonSoundHelper;

import java.util.List;

/**
 * 拿着时间之瓶右击成熟作物时的处理。
 * <p>
 * 时间之瓶本身对成熟作物只会失败（成熟作物不再随机刻），所以这里接管不会抢走它原有的行为：
 * 没熟透的作物照旧回落到时间之瓶的加速流程。
 */
public final class CropHarvestHandler {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack bottle = event.getItemStack();
        if (!(bottle.getItem() instanceof ITiabItem)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = CropHarvest.cropPos(level, event.getPos());
        BlockState state = level.getBlockState(pos);

        if (!CropHarvest.isCrop(state) || !CropHarvest.isMature(state)
                || state.is(ICommonTimeInABottleAPI.COMMON_API.get().getTagKey())) {
            return;
        }

        Player player = event.getEntity();
        int multiplier = harvestMultiplier();
        int cost = useCost(multiplier);
        boolean pays = ModSettings.CONSUME_STORED_TIME.get() && !player.isCreative();

        if (level.isClientSide()) {
            // 客户端也要拦下来，否则本地预测会照旧走时间之瓶的加速流程。
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (pays && stored(bottle).stored() < cost) {
            // 时间不够：不拦截，交回给时间之瓶（它对成熟作物本来就只会失败）。
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!CropHarvest.harvest((ServerLevel) level, pos, player, bottle, multiplier)) {
            return;
        }
        if (pays) {
            consume(bottle, cost);
        }
        if (ModSettings.PLAY_SOUND.get()) {
            CommonSoundHelper.playSound(level, pos, multiplier);
        }
    }

    /**
     * 配置里的倍率，按时间之瓶自己的上限收口（最大 2^(MAX_RATE_MULTI-1)）。
     */
    private static int harvestMultiplier() {
        ITiabConfig config = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        int cap = 1 << Math.max(0, config.MAX_RATE_MULTI() - 1);
        return Mth.clamp(ModSettings.HARVEST_MULTIPLIER.get(), 1, cap);
    }

    /**
     * 与 {@code TiabItem#getEnergyCost} 同一套算法：倍率越高越贵，1 倍就是一次普通使用。
     */
    private static int useCost(int multiplier) {
        ITiabConfig config = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        int eachUse = config.TICKS_CONST() * config.EACH_USE_DURATION();
        return multiplier <= 1 ? eachUse : multiplier / 2 * eachUse;
    }

    private static IStoredTimeComponent stored(ItemStack bottle) {
        ICommonTimeInABottleAPI api = ICommonTimeInABottleAPI.COMMON_API.get();
        return bottle.getOrDefault(api.getRegistration().getStoredTime(), api.createStoredTimeComponent(0, 0));
    }

    /**
     * 扣掉瓶内时间，并立刻刷新物品描述；时间之瓶自己每秒也会刷新一次，这里只是让手感不掉一拍。
     */
    private static void consume(ItemStack bottle, int ticks) {
        ICommonTimeInABottleAPI api = ICommonTimeInABottleAPI.COMMON_API.get();
        IStoredTimeComponent old = stored(bottle);
        int stored = Math.max(0, old.stored() - ticks);

        bottle.set(api.getRegistration().getStoredTime(), api.createStoredTimeComponent(stored, old.total()));
        bottle.set(DataComponents.LORE, new ItemLore(List.of(
                CommonHelper.getStoredTimeTranslated(stored),
                CommonHelper.getTotalTimeTranslated(old.total()))));
    }

    private CropHarvestHandler() {
    }
}

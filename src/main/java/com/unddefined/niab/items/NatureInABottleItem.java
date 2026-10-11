package com.unddefined.niab.items;

import com.unddefined.niab.config.ModSettings;
import com.unddefined.niab.crops.CropHarvest;
import com.unddefined.niab.crops.HarvestTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.api.ITiabConfig;
import org.mangorage.tiab.common.api.impl.IStoredTimeComponent;
import org.mangorage.tiab.common.api.impl.ITimeAcceleratorEntity;
import org.mangorage.tiab.common.misc.CommonHelper;
import org.mangorage.tiab.common.misc.CommonSoundHelper;

import java.util.List;
import java.util.Optional;

/**
 * 瓶中自然：时间之瓶“同机制、不同功能”的另一面。
 * <p>
 * 右击作物给这块作物叠一层倍率（第一次 1 倍，之后每点一次翻一倍，上限与时间之瓶的加速倍率同口径），
 * 每次点都按时间之瓶的公式扣瓶内时间；停手 {@code harvest_delay_ticks} 之后按倍率直接掉落作物产物。
 * <p>
 * 加速本身还是时间之瓶那一套：每点一次都会在作物上挂/顶起一个 {@code TimeAcceleratorEntity}，
 * 所以作物在这段时间里是真的在长，玩家也能从实体的倍率/倒计时看到状态。没熟透的作物照样能点，
 * 只是到点收成时还没熟就什么都不掉（别拿幼苗刷种子）。
 */
public class NatureInABottleItem extends Item {

    public NatureInABottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // onItemUseFirst 跑在方块自身交互之前：像甜浆果丛这种“右击就收”的方块才不会抢走这一下。
        Level level = context.getLevel();
        BlockPos pos = CropHarvest.cropPos(level, context.getClickedPos());
        BlockState state = level.getBlockState(pos);

        if (!CropHarvest.isCrop(state)) return InteractionResult.PASS;
        // 倍率与倒计时都归服务端管，客户端只要知道这一下被消耗了。
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        boolean isCreativeMode = player != null && player.isCreative();

        HarvestTracker.Pending pending = HarvestTracker.get(level, pos);
        // 不点就是普通收成（1 倍），所以点一下直接从 2 倍起，之后每点一次翻一倍。
        int nextRate = 2;
        int energyRequired = getEnergyCost(nextRate);

        if (pending != null) {
            int currentRate = pending.multiplier();
            if (currentRate >= getMaxMultiplier()) return InteractionResult.SUCCESS;
            nextRate = currentRate * 2;
            energyRequired = getEnergyCost(nextRate);
        }

        if (!canUse(stack, isCreativeMode, energyRequired)) return InteractionResult.SUCCESS;

        if (pending == null) HarvestTracker.start((ServerLevel) level, pos, nextRate);
        else pending.raiseTo(nextRate);

        accelerate((ServerLevel) level, pos, nextRate);

        if (!isCreativeMode && ModSettings.CONSUME_STORED_TIME.get()) consume(stack, energyRequired);
        if (ModSettings.PLAY_SOUND.get()) CommonSoundHelper.playSound(level, pos, nextRate);

        return InteractionResult.SUCCESS;
    }

    /**
     * 背包里每秒给瓶内时间加 20 tick，节奏和时间之瓶的 {@code TiabMod#tickPlayer} 一样。
     */
    public void tickPlayer(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null || server.getTickCount() % 20 != 0) return;

        ItemStack bottle = findBottle(player);
        if (!bottle.isEmpty()) tickBottle(bottle, 20);
    }

    /**
     * 背包里第一瓶瓶中自然；找不到返回空。取“第一瓶”是跟着时间之瓶的 {@code findTiabItem} 走的。
     */
    public static ItemStack findBottle(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof NatureInABottleItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 给瓶子加时间，并把物品描述刷新成当前值（时间之瓶也是这么写的）。
     */
    public void tickBottle(ItemStack stack, int ticks) {
        if (!(stack.getItem() instanceof NatureInABottleItem) || ticks <= 0) return;

        ITiabConfig config = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        IStoredTimeComponent stored = storedTime(stack);
        setStoredTime(stack, Math.min(stored.stored() + ticks, config.MAX_STORED_TIME()), stored.total() + ticks);
    }

    /**
     * 与 {@code TiabItem#getEnergyCost} 同一套算法：倍率越高越贵，1 倍就是一次普通使用。
     */
    public static int getEnergyCost(int timeRate) {
        int eachUseDuration = getEachUseDuration();
        return timeRate <= 1 ? eachUseDuration : timeRate / 2 * eachUseDuration;
    }

    /**
     * 一次使用给方块加速多久，口径与 {@code TiabItem#getEachUseDuration} 相同。
     */
    public static int getEachUseDuration() {
        ITiabConfig config = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        return config.TICKS_CONST() * config.EACH_USE_DURATION();
    }

    /**
     * 倍率上限跟时间之瓶的加速倍率同一个口径：2^(MAX_RATE_MULTI - 1)。
     */
    public static int getMaxMultiplier() {
        ITiabConfig config = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        return 1 << Math.max(0, config.MAX_RATE_MULTI() - 1);
    }

    /**
     * 跟时间之瓶一样在方块上挂一个 {@code TimeAcceleratorEntity}：第一次起一层倍率，
     * 之后再点就把倍率顶上去。区别只在寿命——这里让它跟收成倒计时同步（默认 3 秒），
     * 到点实体自己消失，也正好是作物收成的那一刻。
     */
    private static void accelerate(ServerLevel level, BlockPos pos, int rate) {
        ICommonTimeInABottleAPI api = ICommonTimeInABottleAPI.COMMON_API.get();
        Optional<? extends ITimeAcceleratorEntity> existing =
                api.getEntities(level, new AABB(pos)).stream().findFirst();

        if (existing.isPresent()) {
            ITimeAcceleratorEntity accelerator = existing.get();
            accelerator.setTimeRate(rate);
            accelerator.setRemainingTime(ModSettings.HARVEST_DELAY_TICKS.get());
            return;
        }

        ITimeAcceleratorEntity accelerator = api.createEntity(level);
        accelerator.setBlockPos(pos);
        accelerator.setTimeRate(rate);
        accelerator.setRemainingTime(ModSettings.HARVEST_DELAY_TICKS.get());
        level.addFreshEntity(accelerator.asEntity());
    }

    private static boolean canUse(ItemStack stack, boolean isCreativeMode, int energyRequired) {
        return isCreativeMode || !ModSettings.CONSUME_STORED_TIME.get()
                || storedTime(stack).stored() >= energyRequired;
    }

    private static void consume(ItemStack stack, int ticks) {
        IStoredTimeComponent stored = storedTime(stack);
        setStoredTime(stack, Math.max(0, stored.stored() - ticks), stored.total());
    }

    /**
     * 写回瓶内时间，并刷新物品描述。转化配方也复用这里，让合成出来的瓶子直接带上时间。
     */
    public static void setStoredTime(ItemStack stack, int stored, int total) {
        ICommonTimeInABottleAPI api = ICommonTimeInABottleAPI.COMMON_API.get();
        stack.set(api.getRegistration().getStoredTime(), api.createStoredTimeComponent(stored, total));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                CommonHelper.getStoredTimeTranslated(stored),
                CommonHelper.getTotalTimeTranslated(total))));
    }

    /**
     * 读瓶内时间；没有这个组件（例如别的物品）就当成空瓶。
     */
    public static IStoredTimeComponent storedTime(ItemStack stack) {
        ICommonTimeInABottleAPI api = ICommonTimeInABottleAPI.COMMON_API.get();
        return stack.getOrDefault(api.getRegistration().getStoredTime(), api.createStoredTimeComponent(0, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.niab.nature_in_a_bottle.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}

package com.unddefined.niab.crops;

import com.unddefined.niab.config.ModSettings;
import com.unddefined.niab.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collections;
import java.util.List;

/**
 * “什么能收”“熟没熟”“一次收成掉什么”的判定与执行。
 * <p>
 * 分三个家族（判定顺序即优先级）：
 * <ol>
 *   <li><b>年龄型</b>：{@link CropBlock} 子类或标签 {@code niab:harvestable_crops}。age 到最大值算熟，
 *       产物走方块战利品表，收完退回最小 age（受 {@code reset_crop} 控制）。</li>
 *   <li><b>结瓜型</b>：{@link StemBlock} / {@link AttachedStemBlock}。成熟的标志是“旁边结着瓜”，
 *       收成就是摘瓜；摘掉后原版会把藤变回 age 7 继续结。</li>
 *   <li><b>竖长型</b>：标签 {@code niab:vertical_crops}（甘蔗、竹子、仙人掌）。同一个方块叠成柱，
 *       够高（{@code vertical_min_height}）才算熟，收成是把底部以上全砍掉、留最下面 1 格。</li>
 * </ol>
 * 甘蔗/仙人掌的 age 是“再长一格的计时器”、竹子的 age 是粗细（{@code stage} 才是长没长完），
 * 都不是熟度，所以这三类绝不能按年龄型处理。
 */
public final class CropHarvest {

    /**
     * 双高作物（瓶子草）点上半时算在下半身上，其余方块原样返回。
     */
    public static BlockPos cropPos(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
            return pos;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).is(state.getBlock()) ? below : pos;
    }

    /**
     * 能不能用瓶中自然点：三个家族任一。点击本身不判熟，熟不熟到收成那一刻再说。
     */
    public static boolean isHarvestable(BlockState state) {
        return isAgeCrop(state) || isStem(state) || isVertical(state);
    }

    /**
     * 年龄型作物：所有 extends {@link CropBlock} 的（原版与模组的都算），外加数据包标签里额外声明的。
     */
    public static boolean isAgeCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.is(ModTags.Blocks.HARVESTABLE_CROPS);
    }

    /**
     * 结瓜型：西瓜/南瓜藤。已结瓜时方块会变成 attached 版本，同样认。
     */
    public static boolean isStem(BlockState state) {
        return state.getBlock() instanceof StemBlock || state.getBlock() instanceof AttachedStemBlock;
    }

    /**
     * 竖长型：同一个方块叠成柱的那种植物（甘蔗、竹子、仙人掌）。
     */
    public static boolean isVertical(BlockState state) {
        return state.is(ModTags.Blocks.VERTICAL_CROPS);
    }

    /**
     * 方块状态里的年龄属性：叫 {@code age} 或 {@code *_age}（例如 {@code colony_age}）的整数属性。
     * {@code stage} 这种不算——竹子的 stage 是"长没长完"，不是年龄；{@code *_age} 一律认，
     * 让数据包标签能覆盖更多第三方作物。
     */
    public static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && isAgePropertyName(age.getName())) return age;
        }
        return null;
    }

    private static boolean isAgePropertyName(String name) {
        return "age".equals(name) || name.endsWith("_age");
    }

    /**
     * 熟没熟：{@link CropBlock} 直接问方块自己（它可能重写过属性名或最大年龄，比如某些附属的番茄），
     * 其余方块看年龄属性有没有到顶。
     */
    public static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age) >= Collections.max(age.getPossibleValues());
    }

    /**
     * 到点收成，按家族分派。
     *
     * @return 只有真的产出了东西才返回 true。
     */
    public static boolean harvest(ServerLevel level, BlockPos pos, int multiplier) {
        BlockState state = level.getBlockState(pos);
        if (isAgeCrop(state)) return harvestAgeCrop(level, pos, state, multiplier);
        if (isStem(state)) return harvestStem(level, pos, state, multiplier);
        if (isVertical(state)) return harvestVertical(level, pos, state, multiplier);
        return false;
    }

    // ---- 年龄型 ----

    private static boolean harvestAgeCrop(ServerLevel level, BlockPos pos, BlockState state, int multiplier) {
        if (!isMature(state)) return false;

        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
        boolean harvested = false;
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            deliver(level, pos, drop, multiplier);
            harvested = true;
        }

        // 只有年龄型受 reset_crop 控制：另外两族“取走产物”本身就是收成，必须做。
        if (harvested && ModSettings.RESET_CROP.get()) level.setBlock(pos, youngest(state), Block.UPDATE_ALL);
        return harvested;
    }

    /**
     * 收完把作物退回最小年龄，让它重新长。CropBlock 走它自己的 {@code getStateForAge}，
     * 别的方块退回年龄属性的最小值。
     */
    private static BlockState youngest(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) return crop.getStateForAge(0);
        IntegerProperty age = ageProperty(state);
        return age == null ? state : state.setValue(age, Collections.min(age.getPossibleValues()));
    }

    // ---- 结瓜型 ----

    /**
     * 摘瓜：已结瓜的藤上，瓜在 {@code FACING} 指着的那一格。按瓜自己的战利品表 ×倍数掉在原地，
     * 然后把瓜移除——原版 {@code AttachedStemBlock.updateShape} 会把藤变回 age 7 的普通藤继续结。
     * 没结瓜（还是普通藤）就什么都不掉。
     */
    private static boolean harvestStem(ServerLevel level, BlockPos pos, BlockState state, int multiplier) {
        if (!(state.getBlock() instanceof AttachedStemBlock)) return false;

        BlockPos fruitPos = pos.relative(state.getValue(AttachedStemBlock.FACING));
        BlockState fruitState = level.getBlockState(fruitPos);
        if (fruitState.isAir()) return false;

        List<ItemStack> drops = Block.getDrops(fruitState, level, fruitPos, level.getBlockEntity(fruitPos));
        boolean harvested = false;
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            deliver(level, fruitPos, drop, multiplier);
            harvested = true;
        }

        if (harvested) level.removeBlock(fruitPos, false);
        return harvested;
    }

    // ---- 竖长型 ----

    /**
     * 砍杆：先向下走到最底那一格，再向上数出整株高度。高度不到 {@code vertical_min_height} 不算熟；
     * 结算从顶往下逐格做（先取战利品再砍），最下面 1 格留着继续长。
     */
    private static boolean harvestVertical(ServerLevel level, BlockPos pos, BlockState state, int multiplier) {
        Block plant = state.getBlock();

        BlockPos basePos = pos;
        while (level.getBlockState(basePos.below()).is(plant)) basePos = basePos.below();

        int height = 1;
        while (level.getBlockState(basePos.above(height)).is(plant)) height++;
        if (height < ModSettings.VERTICAL_MIN_HEIGHT.get()) return false;

        boolean harvested = false;
        for (int i = height - 1; i >= 1; i--) {
            BlockPos abovePos = basePos.above(i);
            BlockState aboveState = level.getBlockState(abovePos);
            if (!aboveState.is(plant)) continue;

            for (ItemStack drop : Block.getDrops(aboveState, level, abovePos, level.getBlockEntity(abovePos))) {
                if (drop.isEmpty()) continue;
                deliver(level, abovePos, drop, multiplier);
                harvested = true;
            }
            level.removeBlock(abovePos, false);
        }
        return harvested;
    }

    /**
     * 把一份产物按倍率掉出去。数量超过一组就拆成多份，免得出现堆叠数超过上限的物品实体。
     */
    private static void deliver(Level level, BlockPos pos, ItemStack drop, int multiplier) {
        int remaining = drop.getCount() * Math.max(1, multiplier);
        int perStack = Math.max(1, drop.getMaxStackSize());

        while (remaining > 0) {
            ItemStack part = drop.copyWithCount(Math.min(remaining, perStack));
            remaining -= part.getCount();
            Block.popResource(level, pos, part);
        }
    }

    private CropHarvest() {
    }
}

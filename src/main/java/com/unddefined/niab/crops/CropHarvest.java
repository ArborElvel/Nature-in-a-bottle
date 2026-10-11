package com.unddefined.niab.crops;

import com.unddefined.niab.config.ModSettings;
import com.unddefined.niab.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collections;
import java.util.List;

/**
 * “什么算作物”“熟没熟”“一次收成掉什么”的判定与执行。
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
     * 作物：所有 extends {@link CropBlock} 的作物（原版与模组的都算），
     * 外加数据包标签里额外声明的方块。西瓜/南瓜茎虽然带 age，但不是这里说的作物。
     */
    public static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.is(ModTags.Blocks.HARVESTABLE_CROPS);
    }

    /**
     * 方块状态里的 age 属性（原版作物都叫 age），没有就返回 null。
     */
    public static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
                return age;
            }
        }
        return null;
    }

    public static boolean isMature(BlockState state) {
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age) >= Collections.max(age.getPossibleValues());
    }

    /**
     * 到点收成：按战利品表取一份原版产物，乘上倍率直接掉在作物旁边，
     * 再把作物退回最小年龄让它重新生长。
     *
     * @return 只有真的产出了东西才返回 true。
     */
    public static boolean harvest(ServerLevel level, BlockPos pos, int multiplier) {
        BlockState state = level.getBlockState(pos);
        IntegerProperty age = ageProperty(state);
        if (!isCrop(state) || age == null || !isMature(state)) {
            return false;
        }

        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
        boolean harvested = false;
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            deliver(level, pos, drop, multiplier);
            harvested = true;
        }

        if (harvested && ModSettings.RESET_CROP.get()) {
            level.setBlock(pos, state.setValue(age, Collections.min(age.getPossibleValues())), Block.UPDATE_ALL);
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

package com.unddefined.niab.crops;

import com.unddefined.niab.config.ModSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端的“待收成”表，键是维度 + 坐标，值是这块作物攒下来的倍率和剩余 tick。
 * <p>
 * 时间之瓶把这件事挂在加速实体上；瓶中自然只需要撑住几秒，就用一张内存表，
 * 不落盘（服务器在这几秒里重启就白点了，代价可以接受）。
 */
public final class HarvestTracker {

    private static final Map<ResourceKey<Level>, Map<BlockPos, Pending>> PENDING = new HashMap<>();

    /**
     * 登记一块刚被点过的作物，倒计时从满开始。
     */
    public static void start(ServerLevel level, BlockPos pos, int multiplier) {
        PENDING.computeIfAbsent(level.dimension(), key -> new HashMap<>())
                .put(pos.immutable(), new Pending(multiplier));
    }

    public static Pending get(Level level, BlockPos pos) {
        Map<BlockPos, Pending> pending = PENDING.get(level.dimension());
        return pending == null ? null : pending.get(pos);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) tick(serverLevel);
    }

    private static void tick(ServerLevel level) {
        Map<BlockPos, Pending> pending = PENDING.get(level.dimension());
        if (pending == null || pending.isEmpty()) return;

        List<BlockPos> ready = null;
        for (Map.Entry<BlockPos, Pending> entry : pending.entrySet()) {
            if (!entry.getValue().countDown()) continue;
            if (ready == null) ready = new ArrayList<>();
            ready.add(entry.getKey());
        }
        if (ready == null) return;

        for (BlockPos pos : ready) {
            Pending finished = pending.remove(pos);
            CropHarvest.harvest(level, pos, finished.multiplier());
        }
        if (pending.isEmpty()) PENDING.remove(level.dimension());
    }

    /**
     * 一块作物攒着的倍率与倒计时。
     */
    public static final class Pending {

        private int multiplier;
        private int ticksRemaining;

        private Pending(int multiplier) {
            this.multiplier = multiplier;
            this.ticksRemaining = ModSettings.HARVEST_DELAY_TICKS.get();
        }

        public int multiplier() {
            return multiplier;
        }

        /**
         * 再点一次：倍率翻倍，倒计时回到满。
         */
        public void raiseTo(int multiplier) {
            this.multiplier = multiplier;
            this.ticksRemaining = ModSettings.HARVEST_DELAY_TICKS.get();
        }

        private boolean countDown() {
            return --this.ticksRemaining <= 0;
        }
    }

    private HarvestTracker() {
    }
}

package com.unddefined.niab.mixin;

import com.unddefined.niab.crops.HarvestTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.mangorage.tiab.common.entities.TimeAcceleratorEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让收成窗口里的加速实体留在成熟作物上。
 * <p>
 * 时间之瓶的 {@code TimeAcceleratorEntity} 每 tick 会判断方块能不能随机刻，不能就原地自杀——
 * 成熟作物正好不能。于是瓶中自然一点上去，实体闪一下就没了。
 * <p>
 * 收成窗口里（{@link HarvestTracker} 有这块作物的记录）把这次判断顶成 true：成熟作物的
 * {@code randomTick} 本来就不长东西（空转），实体却能一直撑到收成那一刻，玩家也能靠它看到
 * 倍率与倒计时。窗口之外一律返回原值，时间之瓶自己的行为一点没动。
 */
@Mixin(TimeAcceleratorEntity.class)
public abstract class TimeAcceleratorEntityMixin {

    @Shadow
    private BlockPos pos;

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isRandomlyTicking()Z"))
    private boolean niab$stayForHarvest(BlockState state) {
        boolean randomlyTicking = state.isRandomlyTicking();
        if (randomlyTicking || this.pos == null) return randomlyTicking;

        TimeAcceleratorEntity self = (TimeAcceleratorEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return false;
        return HarvestTracker.get(level, this.pos) != null;
    }
}

package com.unddefined.natureinabottle;

import com.unddefined.natureinabottle.config.ModSettings;
import com.unddefined.natureinabottle.server.events.CropHarvestHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Nature in a Bottle（NeoForge 1.21.1），时间之瓶（Time in a Bottle）的附属。
 * <p>
 * 时间之瓶对已经长熟的作物本来就做不了事——成熟作物不再随机刻，瓶子会直接失败。
 * 本模组补上这一段：手持时间之瓶右击成熟作物，直接按战利品表收一份产物、乘上倍率掉出来，
 * 并把方块退回最小年龄让它重新长。未成熟的作物仍然完全交给时间之瓶去加速。
 * <p>
 * 数值走 common 配置，见 {@link ModSettings}。
 */
@Mod(NatureInABottle.MOD_ID)
public final class NatureInABottle {

    public static final String MOD_ID = "nature_in_a_bottle";

    public NatureInABottle(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ModSettings.SPEC);

        NeoForge.EVENT_BUS.addListener(CropHarvestHandler::onRightClickBlock);
    }
}

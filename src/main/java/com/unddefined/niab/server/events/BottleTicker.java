package com.unddefined.niab.server.events;

import com.unddefined.niab.NatureInABottle;
import com.unddefined.niab.config.ModSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.mangorage.tiab.common.api.impl.ITiabItem;

/**
 * 让瓶中自然在背包里自己攒时间，节奏与时间之瓶的 {@code TiabMod#tickPlayer} 一致。
 * <p>
 * 一条硬规则：背包里同一时间只准一个瓶子在攒时间。时间之瓶自己会 tick 背包里第一个
 * {@link ITiabItem}（瓶中自然没实现这个接口，所以它只认自己那一款），带着时间之瓶时我们让开，
 * 只有没带时间之瓶时才轮到瓶中自然，两个瓶子不会同时涨。
 */
public final class BottleTicker {

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ModSettings.ACCUMULATE_STORED_TIME.get()) return;
        if (hasTimeInABottle(event.getEntity())) return;
        NatureInABottle.NATURE_IN_A_BOTTLE.get().tickPlayer(event.getEntity());
    }

    /**
     * 背包（主物品栏）里有没有时间之瓶。扫描范围跟时间之瓶自己的 {@code findTiabItem} 一致。
     */
    private static boolean hasTimeInABottle(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ITiabItem) return true;
        }
        return false;
    }

    private BottleTicker() {
    }
}

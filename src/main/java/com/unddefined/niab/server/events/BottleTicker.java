package com.unddefined.niab.server.events;

import com.unddefined.niab.NatureInABottle;
import com.unddefined.niab.config.ModSettings;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 让瓶中自然在背包里自己攒时间，节奏与时间之瓶的 {@code TiabMod#tickPlayer} 一致。
 */
public final class BottleTicker {

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ModSettings.ACCUMULATE_STORED_TIME.get()) {
            return;
        }
        NatureInABottle.NATURE_IN_A_BOTTLE.get().tickPlayer(event.getEntity());
    }

    private BottleTicker() {
    }
}

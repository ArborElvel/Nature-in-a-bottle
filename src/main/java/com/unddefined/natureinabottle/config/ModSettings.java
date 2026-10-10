package com.unddefined.natureinabottle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common 配置（config/nature_in_a_bottle-common.toml），NeoForge 会自动同步到客户端。
 * 数值都留给整合包调整：产物倍率、是否消耗瓶内时间、是否让作物退回幼苗。
 */
public final class ModSettings {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HARVEST_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue RESET_CROP;
    public static final ModConfigSpec.BooleanValue DROP_AS_ITEMS;
    public static final ModConfigSpec.BooleanValue CONSUME_STORED_TIME;
    public static final ModConfigSpec.BooleanValue PLAY_SOUND;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        HARVEST_MULTIPLIER = builder
                .comment("How many copies of the crop loot one harvest drops.",
                        "The stored time it costs follows Time in a Bottle's own formula:",
                        "multiplier / 2 * (TICKS_CONST * EACH_USE_DURATION).",
                        "Clamped to Time in a Bottle's maximum rate, 2^(max_rate_multi - 1).")
                .defineInRange("harvest_multiplier", 8, 1, 128);
        RESET_CROP = builder
                .comment("True: the crop is set back to its youngest age and grows again.",
                        "False: the crop stays fully grown and can be harvested again right away.")
                .define("reset_crop", true);
        DROP_AS_ITEMS = builder
                .comment("True: the multiplied produce pops out at the crop.",
                        "False: the produce goes straight into the player's inventory.")
                .define("drop_as_items", true);
        CONSUME_STORED_TIME = builder
                .comment("True: the bottle pays its stored time for the harvest (creative players never pay).",
                        "False: harvesting is free.")
                .define("consume_stored_time", true);
        PLAY_SOUND = builder
                .comment("Play the Time in a Bottle note block sound on a successful harvest.")
                .define("play_sound", true);

        SPEC = builder.build();
    }

    private ModSettings() {
    }
}

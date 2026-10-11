package com.unddefined.niab.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common 配置（config/niab-common.toml），NeoForge 会自动同步到客户端。
 * 数值都留给整合包调整：停手多久收成、是否消耗/积累瓶内时间、是否让作物退回幼苗。
 */
public final class ModSettings {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HARVEST_DELAY_TICKS;
    public static final ModConfigSpec.BooleanValue RESET_CROP;
    public static final ModConfigSpec.BooleanValue CONSUME_STORED_TIME;
    public static final ModConfigSpec.BooleanValue ACCUMULATE_STORED_TIME;
    public static final ModConfigSpec.BooleanValue PLAY_SOUND;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        HARVEST_DELAY_TICKS = builder
                .comment("How long a crop waits for the next right click before it is harvested, in ticks.",
                        "20 ticks = 1 second, so the default 60 is the 3 seconds after the last click.",
                        "Every further right click doubles the multiplier and restarts this countdown.")
                .defineInRange("harvest_delay_ticks", 60, 1, 1200);
        RESET_CROP = builder
                .comment("True: the crop is set back to its youngest age and grows again.",
                        "False: the crop stays fully grown and can be harvested again right away.")
                .define("reset_crop", true);
        CONSUME_STORED_TIME = builder
                .comment("True: stacking the multiplier costs stored time, using Time in a Bottle's own",
                        "formula: multiplier / 2 * (TICKS_CONST * EACH_USE_DURATION).",
                        "Creative players never pay. False: stacking is free.")
                .define("consume_stored_time", true);
        ACCUMULATE_STORED_TIME = builder
                .comment("True: the bottle gathers 20 ticks of stored time per second while it sits in an",
                        "inventory, exactly like Time in a Bottle does.",
                        "Only one bottle in the inventory ever gathers at a time: while a Time in a Bottle",
                        "is carried, it keeps that job and Nature in a Bottle stays put.")
                .define("accumulate_stored_time", true);
        PLAY_SOUND = builder
                .comment("Play the Time in a Bottle note block sound whenever the multiplier goes up.")
                .define("play_sound", true);

        SPEC = builder.build();
    }

    private ModSettings() {
    }
}

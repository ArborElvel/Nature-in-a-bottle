package com.unddefined.niab.registry;

import com.unddefined.niab.NatureInABottle;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 数据包标签。只放注册表里无法一刀切判定的东西。
 */
public final class ModTags {

    public static final class Blocks {

        /**
         * 除了所有 extends {@code CropBlock} 的作物之外，还算作“能收成的作物”的方块。
         * 默认值是下界疣、可可豆、甜浆果丛这类走 age 属性但不是 CropBlock 的原版作物；
         * 数据包和模组可以往里追加自己的作物。
         */
        public static final TagKey<Block> HARVESTABLE_CROPS = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(NatureInABottle.MOD_ID, "harvestable_crops"));

        /**
         * 竖着长的植物：同一个方块叠成柱的那种（甘蔗、竹子、仙人掌之类）。
         * “熟”看柱高，收成是把底部以上全砍掉、留最下面 1 格。
         * 注意海带/洞穴藤蔓的头和身是两种方块，套不进这个模型，别往里加。
         */
        public static final TagKey<Block> VERTICAL_CROPS = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(NatureInABottle.MOD_ID, "vertical_crops"));

        private Blocks() {
        }
    }

    private ModTags() {
    }
}

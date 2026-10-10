package com.unddefined.natureinabottle.registry;

import com.unddefined.natureinabottle.NatureInABottle;
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

        private Blocks() {
        }
    }

    private ModTags() {
    }
}

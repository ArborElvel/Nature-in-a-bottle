package com.unddefined.natureinabottle.registry;

import com.unddefined.natureinabottle.NatureInABottle;
import com.unddefined.natureinabottle.crafting.BottleConversionRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 配方序列化器注册。配方本身（JSON）放在 resources 里，类型仍挂在原版 crafting 下。
 */
public final class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NatureInABottle.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BottleConversionRecipe>> BOTTLE_CONVERSION =
            RECIPE_SERIALIZERS.register("bottle_conversion", BottleConversionRecipe.Serializer::new);

    private ModRecipes() {
    }
}

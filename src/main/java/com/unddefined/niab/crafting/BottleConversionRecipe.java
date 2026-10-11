package com.unddefined.niab.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unddefined.niab.items.NatureInABottleItem;
import com.unddefined.niab.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.mangorage.tiab.common.api.impl.IStoredTimeComponent;

/**
 * “瓶子互相转化”的配方：一瓶 + 一份材料 → 另一瓶，瓶内时间（stored_time）原样继承。
 * <p>
 * 为什么不直接用普通 shapeless 配方：原版 shapeless 的产物是一个固定堆叠，改不了数据组件，
 * 而 {@code ItemCraftedEvent} 那条路在 shift 连点时拿到的是合成产物的拷贝，改了也白改。
 * 合成结果是在 {@code CraftingMenu#slotsChanged} 里由 {@link #assemble} 现算出来再放进结果槽的，
 * 所以在这里把时间抄过去，普通点取和 shift 连点两条路都会带上时间。
 * <p>
 * 这条配方仍然挂在原版 {@code minecraft:crafting} 类型下（{@link CraftingRecipe#getType()}），
 * 所以 JEI/EMI 会像普通合成配方一样显示它。
 */
public class BottleConversionRecipe implements CraftingRecipe {

    public static final MapCodec<BottleConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                    .forGetter(BottleConversionRecipe::category),
            Ingredient.CODEC_NONEMPTY.fieldOf("bottle").forGetter(BottleConversionRecipe::bottle),
            Ingredient.CODEC_NONEMPTY.fieldOf("material").forGetter(BottleConversionRecipe::material),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(BottleConversionRecipe::result)
    ).apply(instance, BottleConversionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BottleConversionRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                buffer.writeEnum(recipe.category);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.bottle);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.material);
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            },
            buffer -> new BottleConversionRecipe(
                    buffer.readEnum(CraftingBookCategory.class),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    ItemStack.STREAM_CODEC.decode(buffer)));

    private final CraftingBookCategory category;
    private final Ingredient bottle;
    private final Ingredient material;
    private final ItemStack result;

    public BottleConversionRecipe(CraftingBookCategory category, Ingredient bottle, Ingredient material, ItemStack result) {
        this.category = category;
        this.bottle = bottle;
        this.material = material;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }

        boolean foundBottle = false;
        boolean foundMaterial = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!foundBottle && this.bottle.test(stack)) {
                foundBottle = true;
            } else if (!foundMaterial && this.material.test(stack)) {
                foundMaterial = true;
            } else {
                return false;
            }
        }
        return foundBottle && foundMaterial;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack output = this.result.copy();

        for (ItemStack stack : input.items()) {
            if (!this.bottle.test(stack)) {
                continue;
            }
            // 时间跟着瓶子走：把输入那瓶的 stored_time 抄到产物上，顺带刷新描述。
            IStoredTimeComponent stored = NatureInABottleItem.storedTime(stack);
            NatureInABottleItem.setStoredTime(output, stored.stored(), stored.total());
            break;
        }
        return output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.bottle, this.material);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BOTTLE_CONVERSION.get();
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    public Ingredient bottle() {
        return this.bottle;
    }

    public Ingredient material() {
        return this.material;
    }

    public ItemStack result() {
        return this.result;
    }

    public static class Serializer implements RecipeSerializer<BottleConversionRecipe> {

        @Override
        public MapCodec<BottleConversionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BottleConversionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

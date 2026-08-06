package com.abo47.kubejslab.client.ui.picker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

public final class LabPickerEntries {
    private static final List<ResourceLocation> ITEM_IDS = new ArrayList<>();
    private static final List<ResourceLocation> TAG_IDS = new ArrayList<>();
    private static final List<Fluid> FLUIDS = new ArrayList<>();

    static {
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (!id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) {
                ITEM_IDS.add(id);
            }
        }
        ITEM_IDS.sort(Comparator.comparing(ResourceLocation::toString));
        BuiltInRegistries.ITEM.getTagNames().map(TagKey::location).forEach(TAG_IDS::add);
        TAG_IDS.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation id : BuiltInRegistries.FLUID.keySet()) {
            if (!id.getPath().startsWith("flowing_")) {
                Fluid fluid = BuiltInRegistries.FLUID.get(id);
                if (fluid != null && fluid != Fluids.EMPTY) {
                    FLUIDS.add(fluid);
                }
            }
        }
        FLUIDS.sort(Comparator.comparing(fluid -> BuiltInRegistries.FLUID.getKey(fluid).toString()));
    }

    private LabPickerEntries() {
    }

    public static List<LabPick> entries(boolean tags, boolean fluids, String query) {
        List<LabPick> result = new ArrayList<>();
        String raw = LabSearchFilter.normalize(query);
        if (fluids) {
            for (Fluid fluid : FLUIDS) {
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                String display = FluidStack.create(fluid, 1000).getDisplayName().getString();
                if (LabSearchFilter.matches(raw, id.toString(), display)) {
                    result.add(new LabPick.Fluid(FluidStack.create(fluid, 1000)));
                }
            }
            return result;
        }
        if (tags) {
            for (ResourceLocation id : TAG_IDS) {
                String display = "#" + id;
                if (LabSearchFilter.matches(raw, display, display)) {
                    result.add(new LabPick.Tag(id));
                }
            }
            return result;
        }
        for (ResourceLocation id : ITEM_IDS) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                continue;
            }
            String display = item.getDescription().getString();
            if (LabSearchFilter.matches(raw, id.toString(), display)) {
                result.add(new LabPick.Item(new ItemStack(item)));
            }
        }
        return result;
    }

    public static ItemStack tagPreview(ResourceLocation tagId) {
        List<ItemStack> previews = tagPreviews(tagId);
        return previews.isEmpty() ? ItemStack.EMPTY : previews.get(0);
    }

    public static List<ItemStack> tagPreviews(ResourceLocation tagId) {
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
        List<ItemStack> previews = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            Item item = holder.value();
            if (item != null && item != Items.AIR) {
                previews.add(new ItemStack(item));
            }
        }
        return previews;
    }
}

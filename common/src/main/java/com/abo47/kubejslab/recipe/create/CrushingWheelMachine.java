package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotLayouts;

import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;


public class CrushingWheelMachine extends ProcessingRecipeMachine {
    public CrushingWheelMachine() {
        super(new ResourceLocation("create", "crushing"), "create:crushing", true, false, false, true);
    }

    @Override
    public String jsonTypeFor(Recipe<?> original) {
        if (original instanceof CrushingRecipe) {
            return "create:crushing";
        }
        if (original instanceof MillingRecipe) {
            return "create:milling";
        }
        return jsonType();
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return SlotLayouts.oneInput();
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 3, 0, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 3, 1, true),
                new SlotDescriptor(false, com.abo47.kubejslab.recipe.model.SlotKind.ITEM, 2, 2, true));
    }
}

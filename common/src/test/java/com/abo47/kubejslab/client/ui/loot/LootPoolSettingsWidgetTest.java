package com.abo47.kubejslab.client.ui.loot;

import java.util.List;

import com.abo47.kubejslab.loot.model.LootEntryValues;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LootPoolSettingsWidgetTest {

    @Test
    void explicitToolRequirementIsSelectedVerbatim() {
        assertEquals("shears", LootPoolSettingsWidget.toolSelection(toolEntry("shears", "")));
    }

    @Test
    void forgeShearsActionSelectsPreservedMarker() {
        String selected = LootPoolSettingsWidget.toolSelection(toolEntry("",
                "[{condition: 'forge:can_tool_perform_action', action: 'shears_dig'}]"));
        assertEquals("preserved:shears", selected);
    }

    @Test
    void tagToolSelectsPreservedMarker() {
        String selected = LootPoolSettingsWidget.toolSelection(toolEntry("",
                "[{condition: 'minecraft:match_tool', predicate: {tag: 'notreepunching:shears'}}]"));
        assertEquals("preserved:#notreepunching:shears", selected);
    }

    @Test
    void blankEntrySelectsNone() {
        assertEquals("none", LootPoolSettingsWidget.toolSelection(toolEntry("", "")));
    }

    @Test
    void preservedMarkerMapsBackToBlankRequirement() {
        assertEquals("", LootPoolSettingsWidget.toolRequirementOf("preserved:shears"));
        assertEquals("", LootPoolSettingsWidget.toolRequirementOf("preserved:#notreepunching:shears"));
        assertEquals("", LootPoolSettingsWidget.toolRequirementOf("none"));
        assertEquals("", LootPoolSettingsWidget.toolRequirementOf("#notreepunching:shears"));
        assertEquals("shears", LootPoolSettingsWidget.toolRequirementOf("shears"));
    }

    private static LootEntryValues toolEntry(String tool, String extraConditions) {
        LootEntryValues base = LootEntryValues.defaults();
        return new LootEntryValues("item", "minecraft:grass", base.tag(), base.lootTable(), "constant", 1f,
                base.countMin(), base.countMax(), base.weight(), base.quality(), base.lootBonusMin(),
                base.lootBonusMax(), List.of(), tool, base.entryKilledByPlayer(),
                base.entryChance(), base.entryChanceLooting(), base.alternativeGroup(), base.fortuneBonus(),
                base.lootBonusLimit(), base.explosionDecay(), extraConditions, base.extraFunctions());
    }
}

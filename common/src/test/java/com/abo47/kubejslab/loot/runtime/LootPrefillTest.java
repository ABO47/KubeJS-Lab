package com.abo47.kubejslab.loot.runtime;

import com.abo47.kubejslab.loot.model.LootEntryValues;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LootPrefillTest {

    @Test
    void shearsItemsArrayMapsToTool() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'minecraft:match_tool', predicate: {items: ['minecraft:shears']}}"));
        assertEquals("shears", entry.toolRequirement());
        assertTrue(entry.extraConditions().isBlank(), "mapped tool must not leak into raw: "
                + entry.extraConditions());
    }

    @Test
    void shearsItemsStringMapsToTool() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'minecraft:match_tool', predicate: {items: 'minecraft:shears'}}"));
        assertEquals("shears", entry.toolRequirement());
    }

    @Test
    void silkTouchLevelsObjectMapsToTool() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass_block",
                "{condition: 'minecraft:match_tool', predicate: {enchantments: "
                        + "[{enchantment: 'minecraft:silk_touch', levels: {min: 1}}]}}"));
        assertEquals("silk_touch", entry.toolRequirement());
    }

    @Test
    void invertedSilkTouchMapsToNoSilkTouch() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'minecraft:inverted', term: {condition: 'minecraft:match_tool', "
                        + "predicate: {enchantments: [{enchantment: 'minecraft:silk_touch', levels: 1}]}}}"));
        assertEquals("no_silk_touch", entry.toolRequirement());
        assertTrue(entry.extraConditions().isBlank(), entry.extraConditions());
    }

    @Test
    void anyOfSilkAndShearsMapsToCombo() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:oak_leaves",
                "{condition: 'minecraft:any_of', terms: ["
                        + "{condition: 'minecraft:match_tool', predicate: {enchantments: "
                        + "[{enchantment: 'minecraft:silk_touch', levels: 1}]}}, "
                        + "{condition: 'minecraft:match_tool', predicate: {items: ['minecraft:shears']}}]}"));
        assertEquals("silk_touch_or_shears", entry.toolRequirement());
        assertTrue(entry.extraConditions().isBlank(), entry.extraConditions());
    }

    @Test
    void unmappedConditionIsPreservedAsRaw() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'minecraft:location_check', predicate: {biome: 'minecraft:plains'}}"));
        assertTrue(entry.toolRequirement().isBlank());
        assertTrue(entry.extraConditions().contains("location_check"),
                "unmapped condition must survive the roundtrip: " + entry.extraConditions());
    }

    @Test
    void nonShearsItemToolIsPreservedAsRaw() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'minecraft:match_tool', predicate: {items: ['minecraft:stick']}}"));
        assertTrue(entry.toolRequirement().isBlank());
        assertTrue(entry.extraConditions().contains("match_tool"), entry.extraConditions());
    }

    @Test
    void preservedConditionsAreWrittenBack() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'minecraft:location_check', predicate: {biome: 'minecraft:plains'}}"));
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, entry);
        assertTrue(sb.toString().contains("location_check"),
                "preserved raw must be re-emitted on save: " + sb);
    }

    private static JsonObject entryJson(String item, String condition) {
        return JsonParser.parseString("{type: 'minecraft:item', name: '" + item + "', conditions: ["
                + condition + "]}").getAsJsonObject();
    }
}

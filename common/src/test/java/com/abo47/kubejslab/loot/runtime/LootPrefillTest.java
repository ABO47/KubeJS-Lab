package com.abo47.kubejslab.loot.runtime;

import java.util.List;

import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPayload;
import com.abo47.kubejslab.loot.model.LootPoolValues;

import com.google.gson.JsonObject;import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void unmappedEntryFunctionIsPreservedAsRaw() {
        LootEntryValues entry = LootPrefill.parseEntry(JsonParser.parseString(
                "{type: 'minecraft:item', name: 'minecraft:stick', functions: ["
                        + "{function: 'minecraft:set_nbt', tag: '{display:{Name:\"x\"}}'}]}")
                .getAsJsonObject());
        assertTrue(entry.extraFunctions().contains("set_nbt"),
                "unmapped function must survive the roundtrip: " + entry.extraFunctions());
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, entry);
        assertTrue(sb.toString().contains("set_nbt"), "preserved function must be re-emitted: " + sb);
    }

    @Test
    void enchantWithLevelsIsNotReadAsLooting() {
        LootPoolValues pool = LootPrefill.parsePool(JsonParser.parseString(
                "{rolls: 1, entries: [{type: 'minecraft:item', name: 'minecraft:stick'}], functions: ["
                        + "{function: 'minecraft:enchant_with_levels', levels: 30}]}")
                .getAsJsonObject(), null);
        assertFalse(pool.lootingEnchant(), "enchant_with_levels must not set the looting toggle");
        assertTrue(pool.poolExtraFunctions().contains("enchant_with_levels"),
                "enchant_with_levels must be preserved raw: " + pool.poolExtraFunctions());
    }

    @Test
    void nestedAlternativesChildrenAreKept() {
        LootPoolValues pool = LootPrefill.parsePool(JsonParser.parseString(
                "{rolls: 1, entries: [{type: 'minecraft:alternatives', children: ["
                        + "{type: 'minecraft:item', name: 'minecraft:a'}, "
                        + "{type: 'minecraft:alternatives', children: ["
                        + "{type: 'minecraft:item', name: 'minecraft:b'}, "
                        + "{type: 'minecraft:item', name: 'minecraft:c'}]}]}]}")
                .getAsJsonObject(), null);
        assertEquals(3, pool.entries().size(), "nested choices must not be dropped");
        assertEquals(pool.entries().get(0).alternativeGroup(), pool.entries().get(1).alternativeGroup());
        assertEquals(pool.entries().get(1).alternativeGroup(), pool.entries().get(2).alternativeGroup());
    }

    @Test
    void unmappedPoolConditionIsPreservedAsRaw() {
        LootPoolValues pool = LootPrefill.parsePool(JsonParser.parseString(
                "{rolls: 1, entries: [{type: 'minecraft:item', name: 'minecraft:stick'}], conditions: ["
                        + "{condition: 'minecraft:weather_check', raining: true}]}")
                .getAsJsonObject(), null);
        assertTrue(pool.poolExtraConditions().contains("weather_check"),
                "unmapped pool condition must survive: " + pool.poolExtraConditions());
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writePoolConditions(sb, pool);
        assertTrue(sb.toString().contains("weather_check"), sb.toString());
    }

    @Test
    void tagShearsToolStaysPreservedButHasDisplay() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'minecraft:match_tool', predicate: {tag: 'notreepunching:shears'}}"));
        assertTrue(entry.toolRequirement().isBlank(), "tag tools must not map to a writable tool");
        assertTrue(entry.extraConditions().contains("notreepunching:shears"), entry.extraConditions());
        assertEquals("#notreepunching:shears",
                LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
    }

    @Test
    void anyOfWithTagToolYieldsDisplay() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'minecraft:any_of', terms: ["
                        + "{condition: 'minecraft:match_tool', predicate: {tag: 'biomesoplenty:shears'}}, "
                        + "{condition: 'minecraft:weather_check', raining: true}]}"));
        assertTrue(entry.toolRequirement().isBlank());
        assertEquals("#biomesoplenty:shears",
                LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
    }

    @Test
    void forgeShearsActionStaysPreservedButHasDisplay() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'forge:can_tool_perform_action', action: 'shears_dig'}"));
        assertTrue(entry.toolRequirement().isBlank(), "forge actions must not map to a writable tool");
        assertTrue(entry.extraConditions().contains("can_tool_perform_action"), entry.extraConditions());
        assertEquals("shears", LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
        assertFalse(entry.conditionNotes().toString().contains("can tool perform action"),
                "shears action must not leave a generic note, Requires covers it: "
                        + entry.conditionNotes());
    }

    @Test
    void forgeNonShearsActionKeepsGenericNote() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'forge:can_tool_perform_action', action: 'axe_dig'}"));
        assertTrue(entry.toolRequirement().isBlank());
        assertNull(LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
        assertTrue(entry.extraConditions().contains("can_tool_perform_action"), entry.extraConditions());
    }

    @Test
    void neoforgeShearsActionHasDisplay() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:grass",
                "{condition: 'neoforge:can_tool_perform_action', action: 'shears_harvest'}"));
        assertEquals("shears", LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
    }

    @Test
    void unmappedConditionHasNoToolDisplay() {
        LootEntryValues entry = LootPrefill.parseEntry(entryJson("minecraft:stick",
                "{condition: 'minecraft:location_check', predicate: {biome: 'minecraft:plains'}}"));
        assertNull(LootPrefill.firstPreservedToolDisplay(entry.extraConditions()));
        assertNull(LootPrefill.firstPreservedToolDisplay(""));
    }

    private static JsonObject entryJson(String item, String condition) {
        return JsonParser.parseString("{type: 'minecraft:item', name: '" + item + "', conditions: ["
                + condition + "]}").getAsJsonObject();
    }

    @Test
    void truncatedTableIsRefused() {
        LootPayload truncated = new LootPayload(null, LootService.LOOT_TYPE_BLOCK,
                new LootFieldValues("minecraft:grass", "", List.of(LootPoolValues.defaults()), 2, 0),
                List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> LootService.requireCompleteTable(truncated));
        LootPayload complete = new LootPayload(null, LootService.LOOT_TYPE_BLOCK,
                new LootFieldValues("minecraft:grass", "", List.of(LootPoolValues.defaults()), 0, 0),
                List.of(), List.of());
        LootService.requireCompleteTable(complete);
    }

    @Test
    void droppedCountsSurviveStateRoundTrip() {
        LootPoolValues pool = LootPoolValues.defaults();
        LootFieldValues values = new LootFieldValues("minecraft:grass", "", List.of(pool), 2, 3);
        com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
        LootStateIo.writeValues(obj, values);
        LootFieldValues back = LootStateIo.readValues(obj);
        assertEquals(2, back.droppedPools());
        assertEquals(3, back.droppedEntries());
        assertEquals("minecraft:grass", back.targetId());
    }

    @Test
    void prefillKeepsUpToSixtyFourEntries() {
        StringBuilder entries = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                entries.append(", ");
            }
            entries.append("{type: 'minecraft:item', name: 'minecraft:stick'}");
        }
        LootPoolValues pool = LootPrefill.parsePool(JsonParser.parseString(
                "{rolls: 1, entries: [" + entries + "]}").getAsJsonObject(), null);
        assertEquals(20, pool.entries().size(), "editor supports 64 entries, prefill must not truncate at 6");
    }
}

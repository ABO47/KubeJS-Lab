package com.abo47.kubejslab.loot.runtime;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.model.LootStatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LootScriptWriterTest {

    @Test
    void itemWithCountWritesBalancedQuotes() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntry(sb, itemEntry("kubejs:lab/fiber", 3f));
        String line = sb.toString();
        assertTrue(line.contains("Item.of('kubejs:lab/fiber', 3)"),
                "counted item must close the id quote before the count: " + line);
        assertFalse(line.contains("3')"), "stray quote after count breaks the script: " + line);
    }

    @Test
    void singleItemWritesPlainItemOf() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntry(sb, itemEntry("minecraft:stick", 1f));
        assertTrue(sb.toString().contains("Item.of('minecraft:stick')"), sb.toString());
    }

    @Test
    void blankItemWritesNothing() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntry(sb, itemEntry("", 1f));
        assertTrue(sb.length() == 0, "blank drop must not emit script: " + sb);
    }

    @Test
    void poolWithOnlyBlankEntriesIsSkipped() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_BLOCK,
                new ResourceLocation("kubejs:lab/fiber_block"),
                saveEntry(new LootFieldValues("minecraft:grass", "",
                        List.of(poolWith(itemEntry("", 1f))), 0, 0)));
        assertFalse(sb.toString().contains("addPool"),
                "pool without drops must not emit an empty addPool block: " + sb);
        assertFalse(sb.toString().contains("addItem"), sb.toString());
    }

    @Test
    void poolWithValidEntryIsWritten() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_BLOCK,
                new ResourceLocation("kubejs:lab/fiber_block"),
                saveEntry(new LootFieldValues("minecraft:grass", "",
                        List.of(poolWith(itemEntry("kubejs:lab/fiber", 3f))), 0, 0)));
        assertTrue(sb.toString().contains("addPool"), sb.toString());
        assertTrue(sb.toString().contains("Item.of('kubejs:lab/fiber', 3)"), sb.toString());
    }

    @Test
    void entryWithBlankTargetWritesNothing() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_BLOCK,
                new ResourceLocation("kubejs:lab/fiber_block"),
                saveEntry(new LootFieldValues("", "",
                        List.of(poolWith(itemEntry("kubejs:lab/fiber", 1f))), 0, 0)));
        assertTrue(sb.length() == 0, "blank target must not emit script: " + sb);
    }

    @Test
    void writesPoolMatchesWriter() {
        assertFalse(LootScriptWriter.writesPool(poolWith(itemEntry("", 1f))));
        assertTrue(LootScriptWriter.writesPool(poolWith(itemEntry("kubejs:lab/fiber", 1f))));
    }

    @Test
    void disabledBlockClearsPools() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_BLOCK,
                new ResourceLocation("minecraft:stone"),
                disabledEntry(LootService.LOOT_TYPE_BLOCK, new LootFieldValues("minecraft:stone", "",
                        List.of(poolWith(itemEntry("minecraft:cobblestone", 1f))), 0, 0)));
        assertTrue(sb.toString().contains("modifyBlock('minecraft:stone'"), sb.toString());
        assertTrue(sb.toString().contains("clearPools()"), sb.toString());
        assertFalse(sb.toString().contains("addBlock"), sb.toString());
    }

    @Test
    void disabledEntityClearsPools() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_ENTITY,
                new ResourceLocation("minecraft:zombie"),
                disabledEntry(LootService.LOOT_TYPE_ENTITY, new LootFieldValues("minecraft:zombie", "",
                        List.of(poolWith(itemEntry("minecraft:rotten_flesh", 1f))), 0, 0)));
        assertTrue(sb.toString().contains("modifyEntity("), sb.toString());
        assertTrue(sb.toString().contains("clearPools()"), sb.toString());
        assertFalse(sb.toString().contains("addEntity"), sb.toString());
    }

    @Test
    void disabledChestClearsPools() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_CHEST,
                new ResourceLocation("minecraft:chests/simple_dungeon"),
                disabledEntry(LootService.LOOT_TYPE_CHEST, new LootFieldValues("minecraft:chests/simple_dungeon",
                        "", List.of(poolWith(itemEntry("minecraft:stick", 1f))), 0, 0)));
        assertTrue(sb.toString().contains("modify('minecraft:chests/simple_dungeon'"), sb.toString());
        assertTrue(sb.toString().contains("clearPools()"), sb.toString());
        assertFalse(sb.toString().contains("addChest"), sb.toString());
    }

    private static LootSaveEntry saveEntry(LootFieldValues values) {
        return new LootSaveEntry(LootService.LOOT_TYPE_BLOCK, LootStatus.CREATED, values.targetId(), false,
                values, List.of(), List.of());
    }

    @Test
    void disabledEntryWithBlankTargetFallsBackToStateKey() {
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeLootEntry(sb, LootService.LOOT_TYPE_BLOCK,
                new ResourceLocation("minecraft:stone"),
                disabledEntry(LootService.LOOT_TYPE_BLOCK, new LootFieldValues("", "",
                        List.of(poolWith(itemEntry("minecraft:cobblestone", 1f))), 0, 0)));
        assertTrue(sb.toString().contains("modifyBlock('minecraft:stone'"), sb.toString());
        assertTrue(sb.toString().contains("clearPools()"), sb.toString());
    }

    private static LootSaveEntry disabledEntry(String lootType, LootFieldValues values) {
        return new LootSaveEntry(lootType, LootStatus.DISABLED, values.targetId(), false,
                values, List.of(), List.of());
    }

    private static LootPoolValues poolWith(LootEntryValues entry) {
        LootPoolValues base = LootPoolValues.defaults();
        return new LootPoolValues(base.rollsType(), base.rollsValue(), base.rollsMin(), base.rollsMax(),
                base.rollsN(), base.rollsP(), base.survivesExplosion(), base.randomChance(),
                base.killedByPlayer(), base.furnaceSmelt(), base.lootingEnchant(), base.lootingCount(),
                base.lootingLimit(), List.of(entry), base.bonusRolls(), base.poolConditionNotes(),
                base.poolExtraConditions(), base.poolExtraFunctions());
    }

    private static LootEntryValues itemEntry(String item, float countValue) {
        LootEntryValues base = LootEntryValues.defaults();
        return new LootEntryValues("item", item, base.tag(), base.lootTable(), "constant", countValue,
                base.countMin(), base.countMax(), base.weight(), base.quality(), base.lootBonusMin(),
                base.lootBonusMax(), List.of(), base.toolRequirement(), base.entryKilledByPlayer(),
                base.entryChance(), base.entryChanceLooting(), base.alternativeGroup(), base.fortuneBonus(),
                base.lootBonusLimit(), base.explosionDecay(), base.extraConditions(), base.extraFunctions());
    }

    private static LootEntryValues toolEntry(String item, String tool, String extraConditions) {
        LootEntryValues base = LootEntryValues.defaults();
        return new LootEntryValues("item", item, base.tag(), base.lootTable(), "constant", 1f,
                base.countMin(), base.countMax(), base.weight(), base.quality(), base.lootBonusMin(),
                base.lootBonusMax(), List.of(), tool, base.entryKilledByPlayer(),
                base.entryChance(), base.entryChanceLooting(), base.alternativeGroup(), base.fortuneBonus(),
                base.lootBonusLimit(), base.explosionDecay(), extraConditions, base.extraFunctions());
    }

    @Test
    void explicitToolSkipsPreservedToolCondition() {
        String preserved = "[{condition: 'minecraft:match_tool', predicate: {tag: 'notreepunching:shears'}}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:grass", "shears", preserved));
        String out = sb.toString();
        assertTrue(out.contains("minecraft:shears"), "explicit tool must be emitted: " + out);
        assertFalse(out.contains("notreepunching:shears"),
                "preserved tag tool must be skipped when explicit tool is set: " + out);
    }

    @Test
    void explicitToolKeepsNonToolPreservedCondition() {
        String preserved = "[{condition: 'minecraft:match_tool', predicate: {tag: 'notreepunching:shears'}}, "
                + "{condition: 'minecraft:location_check', predicate: {biome: 'minecraft:plains'}}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:stick", "shears", preserved));
        String out = sb.toString();
        assertTrue(out.contains("location_check"), "non-tool preserved must survive: " + out);
        assertFalse(out.contains("notreepunching:shears"), out);
    }

    @Test
    void noExplicitToolKeepsPreservedToolCondition() {
        String preserved = "[{condition: 'minecraft:match_tool', predicate: {tag: 'notreepunching:shears'}}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:grass", "", preserved));
        assertTrue(sb.toString().contains("notreepunching:shears"), sb.toString());
    }

    @Test
    void alternativesChildSkipsPreservedToolWhenExplicit() {
        String preserved = "[{condition: 'minecraft:match_tool', predicate: {tag: 'biomesoplenty:shears'}}]";
        LootEntryValues entry = toolEntry("minecraft:grass", "fortune", preserved);
        com.google.gson.JsonObject child = LootScriptWriter.childJson(entry);
        String json = child.toString();
        assertTrue(json.contains("fortune"), "explicit fortune must be in child conditions: " + json);
        assertFalse(json.contains("biomesoplenty:shears"),
                "preserved tag tool must be skipped in alternatives child: " + json);
    }

    @Test
    void mixedAnyOfWithWeatherIsKeptWhenExplicit() {
        String preserved = "[{condition: 'minecraft:any_of', terms: ["
                + "{condition: 'minecraft:match_tool', predicate: {tag: 'biomesoplenty:shears'}}, "
                + "{condition: 'minecraft:weather_check', raining: true}]}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:grass", "shears", preserved));
        assertTrue(sb.toString().contains("weather_check"),
                "mixed any_of with non-tool term must not be dropped wholesale: " + sb);
    }

    @Test
    void explicitToolSkipsPreservedForgeShearsAction() {
        String preserved = "[{condition: 'forge:can_tool_perform_action', action: 'shears_dig'}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:grass", "shears", preserved));
        String out = sb.toString();
        assertTrue(out.contains("minecraft:shears"), "explicit tool must be emitted: " + out);
        assertFalse(out.contains("can_tool_perform_action"),
                "preserved forge shears action must be skipped when explicit tool is set: " + out);
    }

    @Test
    void forgeShearsActionIsEmittedWithoutExplicitTool() {
        String preserved = "[{condition: 'forge:can_tool_perform_action', action: 'shears_dig'}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:grass", "", preserved));
        assertTrue(sb.toString().contains("can_tool_perform_action"), sb.toString());
    }

    @Test
    void forgeNonShearsActionIsKeptWithExplicitTool() {
        String preserved = "[{condition: 'forge:can_tool_perform_action', action: 'axe_dig'}]";
        StringBuilder sb = new StringBuilder();
        LootScriptWriter.writeEntryChains(sb, toolEntry("minecraft:stick", "shears", preserved));
        assertTrue(sb.toString().contains("axe_dig"),
                "non-shears tool action carries distinct meaning and must survive: " + sb);
    }
}

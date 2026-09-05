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

    private static LootSaveEntry saveEntry(LootFieldValues values) {
        return new LootSaveEntry(LootService.LOOT_TYPE_BLOCK, LootStatus.CREATED, values.targetId(), false,
                values, List.of(), List.of());
    }

    private static LootPoolValues poolWith(LootEntryValues entry) {
        LootPoolValues base = LootPoolValues.defaults();
        return new LootPoolValues(base.rollsType(), base.rollsValue(), base.rollsMin(), base.rollsMax(),
                base.rollsN(), base.rollsP(), base.survivesExplosion(), base.randomChance(),
                base.killedByPlayer(), base.furnaceSmelt(), base.lootingEnchant(), base.lootingCount(),
                base.lootingLimit(), List.of(entry), base.bonusRolls(), base.poolConditionNotes());
    }

    private static LootEntryValues itemEntry(String item, float countValue) {
        LootEntryValues base = LootEntryValues.defaults();
        return new LootEntryValues("item", item, base.tag(), base.lootTable(), "constant", countValue,
                base.countMin(), base.countMax(), base.weight(), base.quality(), base.lootBonusMin(),
                base.lootBonusMax(), List.of(), base.toolRequirement(), base.entryKilledByPlayer(),
                base.entryChance(), base.entryChanceLooting(), base.alternativeGroup(), base.fortuneBonus(),
                base.lootBonusLimit(), base.explosionDecay(), base.extraConditions(), base.extraFunctions());
    }
}

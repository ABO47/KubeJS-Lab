package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.abo47.kubejslab.loot.model.LabLootNotes;


public final class LabLootNoteText {
    private LabLootNoteText() {
    }

    @Nullable
    public static Component resolve(@Nullable String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        if (!LabLootNotes.isEncoded(note)) {
            return Component.literal(note);
        }
        String key = LabLootNotes.keyOf(note);
        List<String> args = LabLootNotes.argsOf(note);
        if (LabLootNotes.ANY_OF.equals(key)) {
            return joinComponents(args, I18n.get(LabLootNotes.OR));
        }
        if (LabLootNotes.ALL_OF.equals(key)) {
            return joinComponents(args, I18n.get(LabLootNotes.AND));
        }
        Object[] resolved = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            resolved[i] = resolveArg(args.get(i));
        }
        return Component.translatable(key, resolved);
    }

    private static Object resolveArg(String arg) {
        if (LabLootNotes.isVanillaRef(arg)) {
            return Component.translatable(arg.substring(1));
        }
        if (LabLootNotes.isEncoded(arg)) {
            Component nested = resolve(arg);
            return nested == null ? "" : nested;
        }
        return arg;
    }

    @Nullable
    private static Component joinComponents(List<String> args, String joiner) {
        MutableComponent out = null;
        for (String arg : args) {
            Component part = resolve(arg);
            if (part == null) {
                continue;
            }
            out = out == null ? part.copy() : out.append(joiner).append(part);
        }
        return out;
    }

    public static String resolveString(@Nullable String note) {
        if (note == null || note.isBlank()) {
            return "";
        }
        if (!LabLootNotes.isEncoded(note)) {
            return note;
        }
        String key = LabLootNotes.keyOf(note);
        List<String> args = LabLootNotes.argsOf(note);
        if (LabLootNotes.ANY_OF.equals(key)) {
            return joinStrings(args, I18n.get(LabLootNotes.OR));
        }
        if (LabLootNotes.ALL_OF.equals(key)) {
            return joinStrings(args, I18n.get(LabLootNotes.AND));
        }
        Object[] resolved = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            resolved[i] = resolveStringArg(args.get(i));
        }
        try {
            return I18n.get(key, resolved);
        } catch (Exception ignored) {
            return note;
        }
    }

    private static String resolveStringArg(String arg) {
        if (LabLootNotes.isVanillaRef(arg)) {
            return I18n.get(arg.substring(1));
        }
        if (LabLootNotes.isEncoded(arg)) {
            return resolveString(arg);
        }
        return arg;
    }

    public static String joinStrings(List<String> notes, String joiner) {
        List<String> parts = new ArrayList<>();
        for (String note : notes) {
            String resolved = resolveString(note);
            if (!resolved.isBlank()) {
                parts.add(resolved);
            }
        }
        return String.join(joiner, parts);
    }
}

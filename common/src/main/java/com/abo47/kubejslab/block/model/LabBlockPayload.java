package com.abo47.kubejslab.block.model;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;


public record LabBlockPayload(@Nullable ResourceLocation target, String type, LabBlockFieldValues values,
        List<String> tags, List<LabBlockAction> actions) {

    public LabBlockPayload {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        if (tags == null) {
            throw new IllegalArgumentException("tags must not be null");
        }
        if (actions == null) {
            throw new IllegalArgumentException("actions must not be null");
        }
    }
}

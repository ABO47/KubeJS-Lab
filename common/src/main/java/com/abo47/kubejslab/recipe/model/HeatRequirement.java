package com.abo47.kubejslab.recipe.model;

public enum HeatRequirement {
    NONE,
    HEATED,
    SUPERHEATED;

    public static HeatRequirement cycle(HeatRequirement current) {
        return values()[(current.ordinal() + 1) % values().length];
    }

    public static HeatRequirement byName(String name) {
        for (HeatRequirement heat : values()) {
            if (heat.name().equalsIgnoreCase(name)) {
                return heat;
            }
        }
        return NONE;
    }
}

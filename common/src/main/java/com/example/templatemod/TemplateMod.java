package com.example.templatemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TemplateMod {
    public static final String MOD_ID = "templatemod";
    public static final String MOD_NAME = "Template Mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private TemplateMod() {
    }

    public static void bootstrap() {
        LOGGER.info("{} bootstrapped", MOD_NAME);
    }
}

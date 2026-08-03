package com.abo47.kubejslab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KubeJSLab {
    public static final String MOD_ID = "kubejslab";
    public static final String MOD_NAME = "KubeJS Lab";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private KubeJSLab() {
    }

    public static void bootstrap() {
        LOGGER.info("{} bootstrapped", MOD_NAME);
    }
}

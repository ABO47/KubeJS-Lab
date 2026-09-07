package com.abo47.kubejslab.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ScriptWriterTest {

    @Test
    void generatedScriptsLiveInBuiltInRoots() {
        assertEquals("kubejslab.loot.js", ScriptWriter.labFileName("loot.js"));
        assertEquals("kubejslab.disabled.js", ScriptWriter.labFileName("disabled.js"));
        assertEquals("kubejslab.items.js", ScriptWriter.labFileName("items.js"));
    }

    @Test
    void generatedScriptNamesStayNamespaced() {
        String name = ScriptWriter.labFileName("loot.js");
        assertTrue(name.endsWith(".js"), "KubeJS only loads .js scripts: " + name);
        assertTrue(!name.contains("/") && !name.contains("\\"), "no custom subfolders: " + name);
    }
}

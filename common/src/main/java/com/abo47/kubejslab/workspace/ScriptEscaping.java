package com.abo47.kubejslab.workspace;


public final class ScriptEscaping {
    public static String js(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    public static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    private ScriptEscaping() {
    }
}

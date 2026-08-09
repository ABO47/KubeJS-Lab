package com.abo47.kubejslab.recipe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LabVocabularyTest {

    private static final Path SOURCE_ROOT = findRepoRoot()
            .resolve("common/src/main/java/com/abo47/kubejslab");

    @Test
    void retiredRecipeTermsAbsent() throws IOException {
        List<String> terms = List.of("bulk_blasting", "bulk_smoking", "bulk_haunting", "bulk_washing");
        assertTermsAbsent(terms, "retired recipe terms must never be registered as machines");
    }

    @Test
    void outputCountMachineryRemoved() throws IOException {
        List<String> terms = List.of("supportsOutputCount", "defaultOutputCount", "minOutputCount",
                "maxOutputCount", "countAppliesToInputs", "OUTPUT_COUNT");
        assertTermsAbsent(terms, "output-count machinery was removed in the cleanup refactor");
    }

    private static void assertTermsAbsent(List<String> terms, String message) throws IOException {
        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            List<Path> scanned = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(LabVocabularyTest::isScannedSource)
                    .toList();
            for (Path file : scanned) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                List<String> hits = new ArrayList<>();
                for (String term : terms) {
                    if (text.contains(term)) {
                        hits.add(term);
                    }
                }
                assertTrue(hits.isEmpty(),
                        message + ": " + file.getFileName() + " contains " + hits);
            }
        }
    }

    private static boolean isScannedSource(Path path) {
        String relative = SOURCE_ROOT.relativize(path).toString().replace('\\', '/');
        return relative.startsWith("recipe/") || relative.startsWith("client/ui/");
    }

    private static Path findRepoRoot() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (dir != null && !Files.isRegularFile(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("repo root not found from " + System.getProperty("user.dir"));
        }
        return dir;
    }
}